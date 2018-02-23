package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.domain.Proc;

@Service
public class ProcService {
	protected static final Logger log = LoggerFactory.getLogger(ProcService.class);
	private static final int EXP_TIME = 30 * 1000;
	private static final String INDEX_NAME = "proc-data";
	private static final String TYPE_NAME_NODE = "node";
	private static final String TYPE_NAME_APP = "app";
	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}
	};
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private AppService appService;
	
	@Autowired
	private ChoreoService choreoService;

	public void putProcs(String id, List<Proc> ps) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("ps", ps);
		map.put("timestamp", TIME_FORMAT.get().format(new Date()));
		String json = JSON.toJSONString(map);
		
		IndexResponse resp = elasticService.getNode().client().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME_NODE)
				.setId(id).setSource(json, XContentType.JSON).execute().actionGet();
		
		long version = resp.getVersion();
		log.debug("index ps data, id: {}, version: {}", id, version);
	}
	
	@Scheduled(fixedDelay = 5000)
	public void refresh() throws ParseException, IOException {
		//从汇报数据里得到节点和进程
		Map<Node, Date> nodes = getNodes();
		List<Proc> ps = getProcs();
		List<Choreo> choreos = choreoService.getChoreos();
		
		//剔除不可用节点
		delUnavailNodes(nodes, ps, choreos);
		
		//按照应用名称归并进程
		groupProcs(ps);
		
		//剔除删除的应用
		delApps();
		
		//按照运行计划，停止或者启动进程
		scheduleProc(choreos);
	}
	
	private void scheduleProc(List<Choreo> choreos) throws IOException {
		for (Choreo choreo : choreos) {
			//实际分配的进程数量
			if (choreo.getDistribution() == null) {
				choreo.setDistribution(new ArrayList<String>());
			}
			int dist = choreo.getDistribution().size();

			//计划中的进程数量
			App app = appService.getApp(choreo.getApp());
			int scale = app != null && app.isStart() ? choreo.getScale() : 0;
			
			//比较实际数量和计划数量
			if (dist > scale) {
				//实际数量大，需要撤销
				for (int i = 0; i < dist - scale; i ++) {
					choreo.getDistribution().remove(0);
				}
			} else if (dist < scale) {
				//实际数量小，需要分配
				List<String> nodes = distribute(choreo.getQuery(), choreo.getDistribution(), scale - dist);
				if (nodes != null) {
					choreo.getDistribution().addAll(nodes);
				}
			}
			
			//保存新的分配方案
			if (choreo.getDistribution().size() != dist) {
				choreoService.putDistribution(choreo.getApp(), choreo.getDistribution());
			}
		}
	}

	private List<String> distribute(String query, List<String> exists, int count) {
		List<String> dist = new ArrayList<String>();
		PaginationList<Node> nodes = nodeService.searchNodes(query, 0, 4096);
		//TODO 考虑资源占用，排除资源不足的节点
		//TODO 按照进程数排序，尽量平衡分配进程
		if (nodes.getTotal() > 0) {
			for (int i = 0; i < count; i ++) {
				int index = i % nodes.getRows().size();
				Node n = nodes.getRows().get(index);
				dist.add(n.getId());
			}
		}
		return dist;
	}

	private List<Proc> getProcs() {
		List<Proc> ps = new ArrayList<Proc>();
		
		int size = 100;
		long total = size;
		for (int from = 0; from + size <= total; from += size) {
			//查询数据
			SearchResponse resp = elasticService.getNode().client().prepareSearch()
					.setIndices(INDEX_NAME).setTypes(TYPE_NAME_NODE)
					.setQuery(QueryBuilders.matchAllQuery())
					.setFrom(from).setSize(size).execute().actionGet();
			total = resp.getHits().getTotalHits();
			SearchHit[] hits = resp.getHits().getHits();
			
			//解析查询结果
			for (SearchHit hit : hits) {
				Map<String, Object> map = hit.getSourceAsMap();
				Object obj = map.get("ps");
				String json = JSON.toJSONString(obj);
				if (StringUtils.isNotEmpty(json)) {
					List<Proc> list = JSON.parseArray(json, Proc.class);
					if (list != null) {
						ps.addAll(list);
					}
				}
			}
		}
		return ps;
	}
	
	private void delUnavailNodes(Map<Node, Date> nodes, List<Proc> ps, List<Choreo> choreos) throws IOException {
		//清理不可用节点，30秒以上没有握手的节点清理掉。不清理会造成任务无法正常分配
		//清理范围包括：节点数据，进程数据，分配数据
		List<String> validNodeIds = new ArrayList<String>();
		long now = new Date().getTime();
		Iterator<Entry<Node, Date>> it = nodes.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Node, Date> entry = it.next();
			Date ts = entry.getValue();
			Node n = entry.getKey();
			if (now - ts.getTime() > EXP_TIME) {
				//超时，删除节点数据
				nodeService.deleteNode(n.getId());
				deleteProcs(n.getId());
				
				//删除进程数据
				it.remove();
			} else {
				//把节点编号加入有效列表
				validNodeIds.add(n.getId());
			}
		}
		
		//清理进程数据
		Iterator<Proc> iter = ps.iterator();
		while (iter.hasNext()) {
			Proc p = iter.next();
			if (!validNodeIds.contains(p.getNode())) {
				//已经失效的节点，相应的进程删掉
				iter.remove();
			}
		}
		
		//清理分配数据
		for (Choreo choreo : choreos) {
			int len = choreo.getDistribution().size();
			Iterator<String> i = choreo.getDistribution().iterator();
			while (i.hasNext()) {
				String e = i.next();
				if (!validNodeIds.contains(e)) {
					i.remove();
				}
			}
			
			if (len != choreo.getDistribution().size()) {
				choreoService.putDistribution(choreo.getApp(), choreo.getDistribution());
			}
		}
	}
	
	private Map<Node, Date> getNodes() throws ParseException {
		Map<Node, Date> nodes = new HashMap<Node, Date>();
		
		int size = 100;
		long total = size;
		for (int from = 0; from + size <=total; from += size) {
			//查询数据
			SearchResponse resp = elasticService.getNode().client().prepareSearch()
					.setIndices(NodeService.INDEX_NAME).setTypes(NodeService.TYPE_NAME)
					.setQuery(QueryBuilders.matchAllQuery())
					.setFrom(from).setSize(size).execute().actionGet();
			total = resp.getHits().getTotalHits();
			SearchHit[] hits = resp.getHits().getHits();
			
			//解析查询结果
			for (SearchHit hit : hits) {
				String ts = hit.getSourceAsMap().get("timestamp").toString();
				String json = hit.getSourceAsString();
				
				Date date = TIME_FORMAT.get().parse(ts);
				Node n = JSON.parseObject(json, Node.class);
				nodes.put(n, date);
			}
		}
		
		return nodes;
	}

	private void groupProcs(List<Proc> ps) {
		//按照app名称归类
		Map<String, List<Proc>> groups = ps.stream().collect(Collectors.groupingBy(p -> p.getApp()));
		
		//运行中的进程加入进程索引
		for (Entry<String, List<Proc>> entry : groups.entrySet()) {
			String app = entry.getKey();
			List<Proc> list = entry.getValue();
			list = list == null ? new ArrayList<Proc>() : list;
			indexApp(app, list);
		}
		
		//没有运行的应用要索引一个空节点
		List<String> appNames = appService.getApps();
		for (String appName : appNames) {
			if (!groups.containsKey(appName)) {
				indexApp(appName, new ArrayList<Proc>());
			}
		}
	}
	
	private void indexApp(String app, List<Proc> ps) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("app", app);
		map.put("ps", ps);
		map.put("timestamp", TIME_FORMAT.get().format(new Date()));
		String json = JSON.toJSONString(map);
		
		IndexResponse resp = elasticService.getNode().client().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME_APP)
				.setId(app).setSource(json, XContentType.JSON).execute().actionGet();
		
		long version = resp.getVersion();
		log.debug("index ps data, app: {}, version: {}", app, version);
	}
	
	private void deleteProcs(String node) {
		DeleteResponse resp = elasticService.getNode().client().prepareDelete().setIndex(INDEX_NAME).setType(TYPE_NAME_NODE)
				.setId(node).execute().actionGet();
		log.debug("delete node: {}", resp.getId());
	}

	private void delApps() {
		//查询进程缓存
		List<String> names = new ArrayList<String>();
		int size = 100;
		long total = size;
		for (int from = 0; from + size <= total; from += size) {
			//查询数据
			SearchResponse resp = elasticService.getNode().client().prepareSearch()
					.setIndices(INDEX_NAME).setTypes(TYPE_NAME_APP)
					.setQuery(QueryBuilders.matchAllQuery())
					.setFrom(from).setSize(size).execute().actionGet();
			total = resp.getHits().getTotalHits();
			SearchHit[] hits = resp.getHits().getHits();
			
			//得到查询结果
			for (SearchHit hit : hits) {
				names.add(hit.getId());
			}
		}
		
		//循环判断缓存应用名是否已经被删除了
		List<String> apps = appService.getApps();
		for (String name : names) {
			if (!apps.contains(name)) {
				//这个应用已经不存在了
				DeleteResponse resp = elasticService.getNode().client().prepareDelete()
						.setIndex(INDEX_NAME).setType(TYPE_NAME_APP)
						.setId(name).execute().actionGet();
				log.debug("delete app: {}", resp.getId());
			}
		}
	}

}
