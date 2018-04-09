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
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
	
	@Autowired
	private RestTemplate restTemplate;

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
		//从汇报数据里得到可用节点和进程
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
	
	public List<Proc> getProcsByNode(String node, String query) {
		List<Proc> ps = getPs(INDEX_NAME, TYPE_NAME_NODE, node);
		final Node n = nodeService.getNode(node);
		if (StringUtils.isNotEmpty(query)) {
			ps = ps.stream().filter(p -> {
				if (StringUtils.containsIgnoreCase(p.getApp(), query)) {
					return true;
				} else {
					if (StringUtils.containsIgnoreCase(n.getHostname(), query) 
							|| StringUtils.containsIgnoreCase(n.getAgentAddress(), query)) {
						return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
		}
		return ps;
	}
	
	public Proc getProcByNode(String node, long pid) {
		List<Proc> ps = getProcsByNode(node, StringUtils.EMPTY);
		ps = ps.stream().filter(p -> p.getPid() == pid).collect(Collectors.toList());
		return ps.size() > 0 ? ps.get(0) : null;
	}
	
	public List<Proc> getProcsByApp(String app, final String query) {
		List<Proc> ps = getPs(INDEX_NAME, TYPE_NAME_APP, app);
		if (StringUtils.isNotEmpty(query)) {
			ps = ps.stream().filter(p -> {
				if (StringUtils.containsIgnoreCase(p.getApp(), query)) {
					return true;
				} else {
					Node n = nodeService.getNode(p.getNode());
					if (StringUtils.containsIgnoreCase(n.getHostname(), query) 
							|| StringUtils.containsIgnoreCase(n.getAgentAddress(), query)) {
						return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
		}
		return ps;
	}
	
	public Proc getProcByApp(String app, long pid) {
		List<Proc> ps = getProcsByApp(app, StringUtils.EMPTY);
		ps = ps.stream().filter(p -> p.getPid() == pid).collect(Collectors.toList());
		return ps.size() > 0 ? ps.get(0) : null;
	}
	
	private List<Proc> getPs(String indexName, String typeName, String id) {
		List<Proc> ps = new ArrayList<Proc>();
		try {
			GetResponse resp = elasticService.getNode().client().prepareGet()
					.setIndex(indexName).setType(typeName).setId(id).execute().actionGet();
			if (resp.isExists()) {
				Map<String, Object> map = resp.getSourceAsMap();
				Object obj = map.get("ps");
				String json = JSON.toJSONString(obj);
				if (StringUtils.isNotEmpty(json)) {
					ps = JSON.parseArray(json, Proc.class);
				}
			}
		} catch (IndexNotFoundException e) {
			//
		}
		return ps;
	}
	
	public List<Proc> getProcs() {
		List<Proc> ps = new ArrayList<Proc>();

		try {
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
		} catch (IndexNotFoundException e) {
			//pass
		}
		return ps;
	}
	
	public void killProcs(String node, List<Long> pids) {
		Node n = nodeService.getNode(node);
		if (n != null) {
			for (Long pid : pids) {
				String url = String.format("%s/v1/proc/%s/_kill", n.getAgentAddress(), pid);
				restTemplate.postForLocation(url, null);
			}
		}
	}
	
	public String[] getSystemOut(String node, long pid) {
		Node n = nodeService.getNode(node);
		String url = String.format("%s/v1/proc/%s/_stdout", n.getAgentAddress(), pid);
		return restTemplate.getForObject(url, String[].class);
	}

	public String[] getSystemErr(String node, long pid) {
		Node n = nodeService.getNode(node);
		String url = String.format("%s/v1/proc/%s/_stderr", n.getAgentAddress(), pid);
		return restTemplate.getForObject(url, String[].class);
	}
	
	private void scheduleProc(List<Choreo> choreos) throws IOException {
		for (Choreo choreo : choreos) {
			//实际分配的进程数量
			if (choreo.getDist() == null) {
				choreo.setDist(new ArrayList<String>());
			}
			int dist = choreo.getDist().size();

			//计划中的进程数量
			App app = appService.getApp(choreo.getApp());
			int scale = app != null && app.isStart() ? choreo.getScale() : 0;
			
			//比较实际数量和计划数量
			if (dist > scale) {
				//实际数量大，需要撤销
				for (int i = 0; i < dist - scale; i ++) {
					choreo.getDist().remove(0);
				}
			} else if (dist < scale) {
				//实际数量小，需要分配
				List<String> nodes = distribute(choreo.getQuery(), choreo.getDist(), scale - dist);
				if (nodes != null) {
					choreo.getDist().addAll(nodes);
				}
			}
			
			//保存新的分配方案
			if (choreo.getDist().size() != dist) {
				choreoService.putDist(choreo.getApp(), choreo.getDist());
			}
		}
	}

	private List<String> distribute(String query, List<String> exists, int count) {
		List<String> dist = new ArrayList<String>();
		PaginationList<Node> nodes = nodeService.searchNodes(query, 0, 4096, false);
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
				//超时，把节点设置成宕机状态
				nodeService.downNode(n.getId());
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
			int len = choreo.getDist().size();
			Iterator<String> i = choreo.getDist().iterator();
			while (i.hasNext()) {
				String e = i.next();
				if (!validNodeIds.contains(e)) {
					i.remove();
				}
			}
			
			if (len != choreo.getDist().size()) {
				choreoService.putDist(choreo.getApp(), choreo.getDist());
			}
		}
	}
	
	private Map<Node, Date> getNodes() throws ParseException {
		Map<Node, Date> nodes = new HashMap<Node, Date>();
		
		try {
			int size = 100;
			long total = size;
			for (int from = 0; from + size <=total; from += size) {
				//查询数据
				SearchResponse resp = elasticService.getNode().client().prepareSearch()
						.setIndices(NodeService.INDEX_NAME).setTypes(NodeService.TYPE_NAME_UP)
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
		} catch (IndexNotFoundException e) {
			//pass
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
		try {
			DeleteResponse resp = elasticService.getNode().client().prepareDelete().setIndex(INDEX_NAME).setType(TYPE_NAME_NODE)
					.setId(node).execute().actionGet();
			log.debug("delete node: {}", resp.getId());
		} catch (IndexNotFoundException e) {
			//pass
		}
	}

	private void delApps() {
		//查询进程缓存
		List<String> names = new ArrayList<String>();
		try {
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
		} catch (IndexNotFoundException e) {
			//pass
		}
		
		//循环判断缓存应用名是否已经被删除了
		List<String> apps = appService.getApps();
		try {
			for (String name : names) {
				if (!apps.contains(name)) {
					//这个应用已经不存在了
					DeleteResponse resp = elasticService.getNode().client().prepareDelete()
							.setIndex(INDEX_NAME).setType(TYPE_NAME_APP)
							.setId(name).execute().actionGet();
					log.debug("delete app: {}", resp.getId());
				}
			}
		} catch (IndexNotFoundException e) {
			//pass
		}
	}

}
