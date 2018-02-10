package cn.batchfile.stat.server.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Node;

@Service
public class NodeService {
	protected static final Logger log = LoggerFactory.getLogger(NodeService.class);
	private static final String INDEX_NAME = "node-data";
	private static final String TYPE_NAME = "data";
	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}
	};

	@Autowired
	private ElasticService elasticService;

	public void putNode(Node node) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", node.getId());
		map.put("hostname", node.getHostname());
		map.put("agentAddress", node.getAgentAddress());
		map.put("os", node.getOs());
		map.put("memory", node.getMemory());
		map.put("networks", node.getNetworks());
		map.put("disks", node.getDisks());
		map.put("avail", true);
		map.put("shakehandTime", TIME_FORMAT.get().format(new Date()));
		String json = JSON.toJSONString(map);

		UpdateResponse resp = elasticService.getNode().client().prepareUpdate().setIndex(INDEX_NAME).setType(TYPE_NAME)
				.setId(node.getId()).setDoc(json, XContentType.JSON).setUpsert(json, XContentType.JSON).execute().actionGet();
		
		long version = resp.getVersion();
		log.debug("index node data, id: {}, version: {}", node.getId(), version);
	}

	public void putTags(String id, List<String> tags) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tags", tags);
		String json = JSON.toJSONString(map);

		UpdateResponse resp = elasticService.getNode().client().prepareUpdate().setIndex(INDEX_NAME).setType(TYPE_NAME)
				.setId(id).setDoc(json, XContentType.JSON).setUpsert(json, XContentType.JSON).execute().actionGet();
		
		long version = resp.getVersion();
		log.debug("index tags data, id: {}, version: {}", id, version);
	}
}
