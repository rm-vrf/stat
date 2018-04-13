package cn.batchfile.stat.server.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.PaginationList;

@Service
public class NodeService {
	protected static final Logger log = LoggerFactory.getLogger(NodeService.class);
	public static final String INDEX_NAME = "node-data";
	public static final String TYPE_NAME_UP = "up";
	public static final String TYPE_NAME_DOWN = "down";
	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}
	};

	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private EventService eventService;
	
	public void putNode(Node node) {
		
		//更新时间戳
		node.setTimestamp(new Date());
		//TODO 检查时间戳的差值，不能太大。时差太大有隐患
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", node.getId());
		map.put("hostname", node.getHostname());
		map.put("agentAddress", node.getAgentAddress());
		map.put("os", node.getOs());
		map.put("memory", node.getMemory());
		map.put("networks", node.getNetworks());
		map.put("disks", node.getDisks());
		map.put("tags", node.getTags());
		map.put("timestamp", TIME_FORMAT.get().format(node.getTimestamp()));
		String json = JSON.toJSONString(map);

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

	public void downNode(String id) {
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
