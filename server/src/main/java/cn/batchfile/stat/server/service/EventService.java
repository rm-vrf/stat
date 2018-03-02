package cn.batchfile.stat.server.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.Node;

@Service
public class EventService {
	protected static final Logger log = LoggerFactory.getLogger(EventService.class);
	private static final String INDEX_PREFIX = "event-data-";
	private static final String TYPE_NAME = "event";
	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}
	};
	private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	
	@Autowired
	private ElasticService elasticService;

	public void postEvents(List<Event> events) {
		events.stream().forEach(event -> {
			postEvent(event);
		});
	}
	
	public void putNodeUpEvent(Node node) {
		Event e = new Event();
		e.setAction("nodeUp");
		e.setTimestamp(new Date());
		e.setDesc(String.format("节点上线，主机名：%s，地址：%s", node.getHostname(), node.getAgentAddress()));
		postEvent(e);
	}
	
	public void putNodeDownEvent(Map<String, Object> node) {
		Event e = new Event();
		e.setAction("nodeDown");
		e.setTimestamp(new Date());
		e.setDesc(String.format("节点下线，主机名：%s，地址：%s", node.get("hostname"), node.get("agentAddress")));
		postEvent(e);
	}

	private void postEvent(Event event) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("action", event.getAction());
		map.put("timestamp", TIME_FORMAT.get().format(event.getTimestamp()));
		map.put("node", event.getNode());
		map.put("hostname", event.getHostname());
		map.put("address", event.getAddress());
		map.put("app", event.getApp());
		map.put("pid", event.getPid());
		map.put("desc", event.getDesc());
		String json = JSON.toJSONString(map);
		
		String indexName = INDEX_PREFIX + DATE_FORMAT.get().format(event.getTimestamp());
		IndexResponse resp = elasticService.getNode().client().prepareIndex().setIndex(indexName).setType(TYPE_NAME)
				.setSource(json, XContentType.JSON).execute().actionGet();
		
		long version = resp.getVersion();
		log.debug("index event, id: {}, version: {}", resp.getId(), version);
	}

}
