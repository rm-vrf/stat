package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.PaginationList;

@Service
public class NodeService {
	protected static final Logger log = LoggerFactory.getLogger(NodeService.class);
	public static final String INDEX_NAME = "node-data";
	public static final String TYPE_NAME_UP = "up";
	public static final String TYPE_NAME_DOWN = "down";
	
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
	
	@Scheduled(fixedDelay = 5000)
	public void refresh() throws IOException {
		//得到所有在线节点
		List<Node> nodes = getUpNodes();
		if (nodes.size() == 0) {
			return;
		}
		
		//调用节点地址, 判断离线节点
		List<String> nodeIds = new ArrayList<String>();
		nodes.parallelStream().forEach((node) -> {
			try {
				String url = String.format("%s/v1/node", node.getAgentAddress());
				restTemplate.getForObject(url, Node.class);
			} catch (Exception e) {
				nodeIds.add(node.getId());
			}
		});
		
		//去掉离线的节点
		for (String nodeId : nodeIds) {
			//把节点设置成宕机状态
			downNode(nodeId);
			
			//删掉进程信息
			procService.deleteProcs(nodeId);
		}
		
		//清理分配数据
		if (nodeIds.size() > 0) {
			List<Choreo> choreos = choreoService.getChoreos();
			for (Choreo choreo : choreos) {
				int len = choreo.getDist().size();
				Iterator<String> iter = choreo.getDist().iterator();
				while (iter.hasNext()) {
					String nodeId = iter.next();
					if (nodeIds.contains(nodeId)) {
						iter.remove();
					}
				}
				
				if (len != choreo.getDist().size()) {
					choreoService.putDist(choreo.getApp(), choreo.getDist());
				}
			}
		}
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
				log.debug("delete node from down type: {}", deleteResp.getId());
				
				//报告事件
				eventService.putNodeUpEvent(node);
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

	private void downNode(String id) {
		//获取节点数据
		GetResponse getResp = elasticService.getNode().client().prepareGet()
				.setIndex(INDEX_NAME).setType(TYPE_NAME_UP).setId(id).execute().actionGet();
		
		if (getResp.isExists()) {
			//把agent地址去掉，用这个属性标注节点的在线状态
			Map<String, Object> node = getResp.getSourceAsMap();
			Object agentAddress = node.remove("agentAddress");
			
			//删除在线节点
			DeleteResponse deleteResp = elasticService.getNode().client().prepareDelete().setIndex(INDEX_NAME).setType(TYPE_NAME_UP)
					.setId(id).execute().actionGet();
			log.debug("delete node: {}", deleteResp.getId());
			
			//插入离线节点
			String json = JSON.toJSONString(node);
			IndexResponse indexResp = elasticService.getNode().client().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME_DOWN)
					.setId(id).setSource(json, XContentType.JSON).execute().actionGet();
			
			long version = indexResp.getVersion();
			log.debug("index node data to down type, id: {}, version: {}", id, version);
			
			//报告事件
			node.put("agentAddress", agentAddress);
			eventService.putNodeDownEvent(node);
		}
	}
}
