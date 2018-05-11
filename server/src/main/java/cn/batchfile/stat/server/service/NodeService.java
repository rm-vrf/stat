package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.domain.Proc;

@Service
public class NodeService {
	protected static final Logger log = LoggerFactory.getLogger(NodeService.class);
	public static final String INDEX_NAME = "node-data";
	public static final String TYPE_NAME_UP = "up";
	public static final String TYPE_NAME_DOWN = "down";
	private Map<String, Long> timestamps = new HashMap<String, Long>();
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private ChoreoService choreoService;
	
	@Autowired
	private ProcService procService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@PostConstruct
	public void init() {
		//启动定时器
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(() -> {
			try {
				refresh();
			} catch (Exception e) {
				//pass
			}
		}, 1, 1, TimeUnit.SECONDS);
	}
	
	private void refresh() throws IOException {
		//得到所有在线节点
		long begin = System.currentTimeMillis();
		List<Node> nodes = getUpNodes();
		
		//获取每一个节点的进程信息
		List<String> changeNodeIds = new ArrayList<String>();
		List<String> downNodeIds = new ArrayList<String>();
		List<Proc> changePs = new ArrayList<Proc>();
		Map<String, Long> timestamps = new ConcurrentHashMap<String, Long>();
		nodes.parallelStream().forEach((node) -> {
			String url = String.format("%s/v1/proc", node.getAgentAddress());
			try {
				//向节点发出询问消息，带时间戳
				HttpHeaders headers = new HttpHeaders();
				headers.setIfModifiedSince(this.timestamps.containsKey(node.getId()) ? this.timestamps.get(node.getId()) : 0);
				HttpEntity<?> entity = new HttpEntity<>("parameters", headers);
				ResponseEntity<Long[]> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Long[].class);
				
				//如果有实际内容，加入进程列表
				if (resp.getStatusCode() == HttpStatus.OK) {
					log.info("GET {} {}", url, resp.toString());
					for (Long pid : resp.getBody()) {
						url = String.format("%s/v1/proc/%s", node.getAgentAddress(), pid);
						Proc p = restTemplate.getForObject(url, Proc.class);
						log.info("GET {} <200 OK>", url);
						if (p != null) {
							changePs.add(p);
						}
					}
				}
				
				//添加变动节点列表
				changeNodeIds.add(node.getId());
				
				//更新时间戳
				timestamps.put(node.getId(), resp.getHeaders().getLastModified());
			} catch (ResourceAccessException e) {
				//如果访问超时，加入离线进程列表
				downNodeIds.add(node.getId());
				log.error("GET {} <{}>", url, e.getMessage());
			}
		});

		//记录时间日志
		log.debug("get ps from node(s), count: {}, cost: {}", nodes.size(), (System.currentTimeMillis() - begin));
		
		//处理离线的节点
		for (String downNodeId : downNodeIds) {
			//把节点设置成宕机状态
			downNode(nodes, downNodeId);
			log.info("Move index: {}/{}/{} -> {}/{}/{}", 
					INDEX_NAME, TYPE_NAME_UP, downNodeId, INDEX_NAME, TYPE_NAME_DOWN, downNodeId);
			
			//删除节点相关的进程信息
			procService.deleteProcs(downNodeId);
			log.info("Delete index: {}/{}/{}", ProcService.INDEX_NAME, ProcService.TYPE_NAME_NODE, downNodeId);
		}
		
		//清理分配数据
		if (downNodeIds.size() > 0) {
			List<Choreo> choreos = choreoService.getChoreos();
			for (Choreo choreo : choreos) {
				//遍历分配数据，把分配列表中的下线节点去掉
				int len = choreo.getDist().size();
				Iterator<String> iter = choreo.getDist().iterator();
				while (iter.hasNext()) {
					String nodeId = iter.next();
					if (downNodeIds.contains(nodeId)) {
						iter.remove();
					}
				}
				
				if (len != choreo.getDist().size()) {
					choreoService.putDist(choreo.getApp(), choreo.getDist());
					log.info("Remove dist, app: {}, count: {}", choreo.getApp(), choreo.getDist().size());
				}
			}
		}
		
		//更新进程信息
		if (changePs.size() > 0) {
			Map<String, List<Proc>> groups = changePs.stream().collect(Collectors.groupingBy(p -> p.getNode()));
			for (Entry<String, List<Proc>> entry : groups.entrySet()) {
				procService.putProcs(entry.getKey(), entry.getValue());
				log.info("Update index: {}/{}/{}, ps count: {}", 
						ProcService.INDEX_NAME, ProcService.TYPE_NAME_NODE, entry.getKey(), entry.getValue().size());
			}
		}
		
		//重新归类进程信息
		if (changePs.size() > 0 || downNodeIds.size() > 0) {
			List<Proc> ps = procService.getProcs();
			
			//用新数据代替ps里面的缓存数据，es查询存在n秒延时
			Iterator<Proc> iter = ps.iterator();
			while (iter.hasNext()) {
				Proc p = iter.next();
				if (changeNodeIds.contains(p.getNode()) || downNodeIds.contains(p.getNode())) {
					iter.remove();
				}
			}
			ps.addAll(changePs);
			
			//按照应用名归类保存
			procService.groupProcs(ps);
			log.info("Group by app, ps count: {}", ps.size());
		}
		
		//更新时间戳
		this.timestamps.clear();
		this.timestamps.putAll(timestamps);
	}
	
	public void putNode(Node node) {
		
		String json = JSON.toJSONString(node);

		//插入在线节点
		IndexResponse indexResp = elasticService.getNode().client().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME_UP)
				.setId(node.getId()).setSource(json, XContentType.JSON).execute().actionGet();
		long version = indexResp.getVersion();
		log.debug("index node data, id: {}, version: {}", node.getId(), version);
		
		//判断是否存在离线节点
		try {
			GetResponse getResponse = elasticService.getNode().client().prepareGet()
					.setIndex(INDEX_NAME).setType(TYPE_NAME_DOWN)
					.setId(node.getId()).execute().actionGet();
			
			if (getResponse.isExists()) {
				//删除离线节点
				DeleteResponse deleteResp = elasticService.getNode().client().prepareDelete()
						.setIndex(INDEX_NAME).setType(TYPE_NAME_DOWN)
						.setId(node.getId()).execute().actionGet();
				log.debug("delete index: {}/{}/{}", INDEX_NAME, TYPE_NAME_DOWN, deleteResp.getId());
				
				//报告事件
				eventService.putNodeUpEvent(node);
				log.info("Node up: {'id':'{}', 'hostname':'{}', 'agentAddress':'{}'}", 
						node.getId(), node.getHostname(), node.getAgentAddress());
			}
		} catch (IndexNotFoundException e) {
			//pass
		}
	}
	
	public Node getNode(String id) {
		try {
			//获取数据
			GetResponse resp = elasticService.getNode().client().prepareGet()
					.setIndex(INDEX_NAME).setId(id).execute().actionGet();
			
			Node node = null;
			if (resp.isExists()) {
				String json = resp.getSourceAsString();
				if (StringUtils.isNotEmpty(json)) {
					node = JSON.parseObject(json, Node.class);
				}
			}
			
			return node;
		} catch (IndexNotFoundException e) {
			return null;
		}
	}
	
	public Map<String, String> getEnvs(String id) {
		Node node = getNode(id);
		if (StringUtils.isEmpty(node.getAgentAddress())) {
			throw new RuntimeException("cannot get envs of a offline node");
		}
		String url = String.format("%s/v1/node/env", node.getAgentAddress());
		
		@SuppressWarnings("unchecked")
		Map<String, String> envs = restTemplate.getForObject(url, Map.class);
		return envs;
	}
	
	public void putEnvs(String id, Map<String, String> envs) {
		Node node = getNode(id);
		if (StringUtils.isEmpty(node.getAgentAddress())) {
			throw new RuntimeException("cannot change envs of a offline node");
		}
		String url = String.format("%s/v1/node/env", node.getAgentAddress());
		restTemplate.put(url, envs);
	}
	
	public void putTags(String id, List<String> tags) {
		Node node = getNode(id);
		if (StringUtils.isEmpty(node.getAgentAddress())) {
			throw new RuntimeException("cannot change tags of a offline node");
		}
		node.setTags(tags);
		
		String url = String.format("%s/v1/node/tag", node.getAgentAddress());
		restTemplate.put(url, tags);
		
		putNode(node);
	}

	public PaginationList<Node> searchNodes(String query, int from, int size, boolean includeDownNode) {
		List<Node> nodes = new ArrayList<Node>();
		
		//查询数据
		SearchRequestBuilder search = elasticService.getNode().client().prepareSearch().setIndices(INDEX_NAME)
				.setQuery(QueryBuilders.queryStringQuery(query))
				.addSort("networks.address", SortOrder.ASC)
				.setFrom(from).setSize(size);
		if (includeDownNode) {
			search.setTypes(TYPE_NAME_UP, TYPE_NAME_DOWN);
		} else {
			search.setTypes(TYPE_NAME_UP);
		}
		
		try {
			SearchResponse resp = search.execute().actionGet();
			long total = resp.getHits().getTotalHits();
			SearchHit[] hits = resp.getHits().getHits();
	
			//解析查询结果
			for (SearchHit hit : hits) {
				String json = hit.getSourceAsString();
				if (StringUtils.isNotEmpty(json)) {
					Node node = JSON.parseObject(json, Node.class);
					nodes.add(node);
				}
			}
			
			return new PaginationList<Node>(total, nodes);
		} catch (IndexNotFoundException e) {
			return new PaginationList<Node>(0, nodes);
		}
	}

	private List<Node> getUpNodes() {
		List<Node> nodes = new ArrayList<Node>();
		
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
					Node node = JSON.parseObject(hit.getSourceAsString(), Node.class);
					nodes.add(node);
				}
			}
		} catch (IndexNotFoundException e) {
			//pass
		}
		
		return nodes;
	}

	private void downNode(List<Node> nodes, String id) {
		
		//从列表上寻找节点
		Node node = nodes.stream().filter((n) -> {return StringUtils.equals(id, n.getId());}).collect(Collectors.toList()).get(0);
		
		//删除在线节点
		DeleteResponse deleteResp = elasticService.getNode().client().prepareDelete().setIndex(INDEX_NAME).setType(TYPE_NAME_UP)
				.setId(id).execute().actionGet();
		log.debug("delete node: {}", deleteResp.getId());
		
		//把agent地址去掉，用这个属性标注节点的在线状态
		String agentAddress = node.getAgentAddress();
		node.setAgentAddress(StringUtils.EMPTY);
		
		//添加离线节点
		String json = JSON.toJSONString(node);
		IndexResponse indexResp = elasticService.getNode().client().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME_DOWN)
				.setId(id).setSource(json, XContentType.JSON).execute().actionGet();
		
		long version = indexResp.getVersion();
		log.debug("index node data to down type, id: {}, version: {}", id, version);
		
		//报告事件
		node.setAgentAddress(agentAddress);
		eventService.putNodeDownEvent(node);
		
		log.info("Node down: {'id':'{}', 'hostname':'{}', 'agentAddress':'{}'}", id, node.getHostname(), agentAddress);
	}
}
