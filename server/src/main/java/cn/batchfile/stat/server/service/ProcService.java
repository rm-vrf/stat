//package cn.batchfile.stat.server.service;
//
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import javax.annotation.PostConstruct;
//
//import org.apache.commons.lang.StringUtils;
//import org.elasticsearch.action.delete.DeleteResponse;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.index.IndexResponse;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.index.IndexNotFoundException;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.alibaba.fastjson.JSON;
//
//import cn.batchfile.stat.domain.App;
//import cn.batchfile.stat.domain.Choreo;
//import cn.batchfile.stat.domain.Node;
//import cn.batchfile.stat.domain.PaginationList;
//import cn.batchfile.stat.domain.Proc;
//
//@Service
//public class ProcService {
//	protected static final Logger log = LoggerFactory.getLogger(ProcService.class);
//	public static final String INDEX_NAME = "proc-data";
//	public static final String TYPE_NAME_NODE = "node";
//	public static final String TYPE_NAME_APP = "app";
//	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
//		@Override
//		protected DateFormat initialValue() {
//			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//		}
//	};
//	
//	@Autowired
//	private ElasticService elasticService;
//	
//	@Autowired
//	private NodeService nodeService;
//	
//	@Autowired
//	private AppService appService;
//	
//	@Autowired
//	private ChoreoService choreoService;
//	
//	@Autowired
//	private RestTemplate restTemplate;
//
//	@PostConstruct
//	public void init() {
//		//启动定时器
//		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
//		es.scheduleWithFixedDelay(() -> {
//			try {
//				refresh();
//			} catch (Exception e) {
//				//pass
//			}
//		}, 30, 5, TimeUnit.SECONDS);
//	}
//	
//	private void refresh() throws ParseException, IOException {
//		//按照运行计划，停止或者启动进程
//		List<Choreo> choreos = choreoService.getChoreos();
//		for (Choreo choreo : choreos) {
//			//实际分配的进程数量
//			if (choreo.getDist() == null) {
//				choreo.setDist(new ArrayList<String>());
//			}
//			int dist = choreo.getDist().size();
//
//			//计划中的进程数量
//			App app = appService.getApp(choreo.getApp());
//			int scale = app != null && app.isStart() ? choreo.getScale() : 0;
//			
//			//比较实际数量和计划数量
//			if (dist > scale) {
//				//实际数量大，需要撤销
//				for (int i = 0; i < dist - scale; i ++) {
//					String id = choreo.getDist().remove(0);
//					log.info("Remove dist, app: {}, node: {}", choreo.getApp(), id);
//				}
//			} else if (dist < scale) {
//				//实际数量小，需要分配
//				List<String> nodes = distribute(choreo.getQuery(), choreo.getDist(), scale - dist);
//				if (nodes != null && nodes.size() > 0) {
//					choreo.getDist().addAll(nodes);
//					log.info("Add dist, app: {}, nodes: {}", choreo.getApp(), nodes.toString());
//				}
//			}
//			
//			//保存新的分配方案
//			if (choreo.getDist().size() != dist) {
//				choreoService.putDist(choreo.getApp(), choreo.getDist());
//			}
//		}
//	}
//	
//	public void putProcs(String id, List<Proc> ps) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("id", id);
//		map.put("ps", ps);
//		map.put("timestamp", TIME_FORMAT.get().format(new Date()));
//		String json = JSON.toJSONString(map);
//		
//		IndexResponse resp = elasticService.getNode().client().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME_NODE)
//				.setId(id).setSource(json, XContentType.JSON).execute().actionGet();
//		
//		long version = resp.getVersion();
//		log.debug("index ps data, id: {}, version: {}", id, version);
//	}
//	
//	public List<Proc> getProcsByNode(String node, String query) {
//		List<Proc> ps = getPs(INDEX_NAME, TYPE_NAME_NODE, node);
//		final Node n = nodeService.getNode(node);
//		if (StringUtils.isNotEmpty(query)) {
//			ps = ps.stream().filter(p -> {
//				if (StringUtils.containsIgnoreCase(p.getApp(), query)) {
//					return true;
//				} else {
//					if (StringUtils.containsIgnoreCase(n.getHostname(), query) 
//							|| StringUtils.containsIgnoreCase(n.getAgentAddress(), query)) {
//						return true;
//					}
//				}
//				return false;
//			}).collect(Collectors.toList());
//		}
//		return ps;
//	}
//	
//	public Proc getProcByNode(String node, long pid) {
//		List<Proc> ps = getProcsByNode(node, StringUtils.EMPTY);
//		ps = ps.stream().filter(p -> p.getPid() == pid).collect(Collectors.toList());
//		return ps.size() > 0 ? ps.get(0) : null;
//	}
//	
//	public List<Proc> getProcsByApp(String app, final String query) {
//		List<Proc> ps = getPs(INDEX_NAME, TYPE_NAME_APP, app);
//		if (StringUtils.isNotEmpty(query)) {
//			ps = ps.stream().filter(p -> {
//				if (StringUtils.containsIgnoreCase(p.getApp(), query)) {
//					return true;
//				} else {
//					Node n = nodeService.getNode(p.getNode());
//					if (StringUtils.containsIgnoreCase(n.getHostname(), query) 
//							|| StringUtils.containsIgnoreCase(n.getAgentAddress(), query)) {
//						return true;
//					}
//				}
//				return false;
//			}).collect(Collectors.toList());
//		}
//		return ps;
//	}
//	
//	public Proc getProcByApp(String app, long pid) {
//		List<Proc> ps = getProcsByApp(app, StringUtils.EMPTY);
//		ps = ps.stream().filter(p -> p.getPid() == pid).collect(Collectors.toList());
//		return ps.size() > 0 ? ps.get(0) : null;
//	}
//	
//	private List<Proc> getPs(String indexName, String typeName, String id) {
//		List<Proc> ps = new ArrayList<Proc>();
//		try {
//			GetResponse resp = elasticService.getNode().client().prepareGet()
//					.setIndex(indexName).setType(typeName).setId(id).execute().actionGet();
//			if (resp.isExists()) {
//				Map<String, Object> map = resp.getSourceAsMap();
//				Object obj = map.get("ps");
//				String json = JSON.toJSONString(obj);
//				if (StringUtils.isNotEmpty(json)) {
//					ps = JSON.parseArray(json, Proc.class);
//				}
//			}
//			Collections.sort(ps, (p1, p2) -> {
//				return (int)(p1.getPid() - p2.getPid());
//			});
//		} catch (IndexNotFoundException e) {
//			//
//		}
//		return ps;
//	}
//	
//	public List<Proc> getProcs() {
//		List<Proc> ps = new ArrayList<Proc>();
//
//		try {
//			int size = 100;
//			long total = size;
//			for (int from = 0; from + size <= total; from += size) {
//				//查询数据
//				SearchResponse resp = elasticService.getNode().client().prepareSearch()
//						.setIndices(INDEX_NAME).setTypes(TYPE_NAME_NODE)
//						.setQuery(QueryBuilders.matchAllQuery())
//						.setFrom(from).setSize(size).execute().actionGet();
//				total = resp.getHits().getTotalHits();
//				SearchHit[] hits = resp.getHits().getHits();
//				
//				//解析查询结果
//				for (SearchHit hit : hits) {
//					Map<String, Object> map = hit.getSourceAsMap();
//					Object obj = map.get("ps");
//					String json = JSON.toJSONString(obj);
//					if (StringUtils.isNotEmpty(json)) {
//						List<Proc> list = JSON.parseArray(json, Proc.class);
//						if (list != null) {
//							ps.addAll(list);
//						}
//					}
//				}
//			}
//		} catch (IndexNotFoundException e) {
//			//pass
//		}
//		return ps;
//	}
//	
//	public void killProcs(String node, List<Long> pids) {
//		Node n = nodeService.getNode(node);
//		if (n != null) {
//			for (Long pid : pids) {
//				String url = String.format("%s/v1/proc/%s/_kill", n.getAgentAddress(), pid);
//				restTemplate.postForLocation(url, null);
//			}
//		}
//	}
//	
//	public String[] getSystemOut(String node, long pid) {
//		Node n = nodeService.getNode(node);
//		String url = String.format("%s/v1/proc/%s/_stdout", n.getAgentAddress(), pid);
//		return restTemplate.getForObject(url, String[].class);
//	}
//
//	public String[] getSystemErr(String node, long pid) {
//		Node n = nodeService.getNode(node);
//		String url = String.format("%s/v1/proc/%s/_stderr", n.getAgentAddress(), pid);
//		return restTemplate.getForObject(url, String[].class);
//	}
//	
//	public void deleteProcs(String node) {
//		try {
//			DeleteResponse resp = elasticService.getNode().client().prepareDelete().setIndex(INDEX_NAME).setType(TYPE_NAME_NODE)
//					.setId(node).execute().actionGet();
//			log.debug("delete node: {}", resp.getId());
//		} catch (IndexNotFoundException e) {
//			//pass
//		}
//	}
//
//	private List<String> distribute(String query, List<String> exists, int count) {
//		List<String> dist = new ArrayList<String>();
//		PaginationList<Node> nodes = nodeService.searchNodes(query, 0, 4096, false);
//		//TODO 考虑资源占用，排除资源不足的节点
//		//TODO 按照进程数排序，尽量平衡分配进程
//		if (nodes.getTotal() > 0) {
//			for (int i = 0; i < count; i ++) {
//				int index = i % nodes.getRows().size();
//				Node n = nodes.getRows().get(index);
//				dist.add(n.getId());
//			}
//		}
//		return dist;
//	}
//
//	public void groupProcs(List<Proc> ps) {
//		//按照app名称归类
//		Map<String, List<Proc>> groups = ps.stream().collect(Collectors.groupingBy(p -> p.getApp()));
//		
//		//运行中的进程加入进程索引
//		for (Entry<String, List<Proc>> entry : groups.entrySet()) {
//			String app = entry.getKey();
//			List<Proc> list = entry.getValue();
//			list = list == null ? new ArrayList<Proc>() : list;
//			indexApp(app, list);
//			log.debug("app name: {}, ps count: {}", app, list.size());
//		}
//		
//		//没有运行的应用要索引一个空节点
//		List<String> appNames = appService.getApps();
//		for (String appName : appNames) {
//			if (!groups.containsKey(appName)) {
//				indexApp(appName, new ArrayList<Proc>());
//				log.debug("app name: {}, ps count: {}", appName, 0);
//			}
//		}
//	}
//	
//	private void indexApp(String app, List<Proc> ps) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("app", app);
//		map.put("ps", ps);
//		map.put("timestamp", TIME_FORMAT.get().format(new Date()));
//		String json = JSON.toJSONString(map);
//		
//		IndexResponse resp = elasticService.getNode().client().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME_APP)
//				.setId(app).setSource(json, XContentType.JSON).execute().actionGet();
//		
//		long version = resp.getVersion();
//		log.debug("index ps data, app: {}, version: {}", app, version);
//	}
//	
//
//}
