package cn.batchfile.stat.server.service;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.PaginationList;

@Service
public class EventService {
	protected static final Logger log = LoggerFactory.getLogger(EventService.class);
	private static final String INDEX_PREFIX = "event-data-";
	private static final String TYPE_NAME = "event";
	private static final String TS_FILE = "event_time";
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
	private File eventTimeFile;
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	@Autowired
	private ElasticService elasticService;
	
	@PostConstruct
	public void init() throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		eventTimeFile = new File(f, TS_FILE);
		if (!eventTimeFile.exists()) {
			FileUtils.writeByteArrayToFile(eventTimeFile, "0".getBytes());
		}
	}

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
	
	public Date getTimestamp() throws IOException {
		String s = FileUtils.readFileToString(eventTimeFile, "UTF-8");
		return new Date(Long.valueOf(s));
	}
	
	public void setTimestamp(Date date) throws IOException {
		FileUtils.writeByteArrayToFile(eventTimeFile, String.valueOf(date.getTime()).getBytes());
	}
	
	public PaginationList<Event> searchEvent(Date beginTime, int size) {
		List<Event> events = new ArrayList<Event>();
		Date to = new Date(System.currentTimeMillis() - 1000);
		QueryBuilder query = QueryBuilders.rangeQuery("timestamp")
				.from(TIME_FORMAT.get().format(beginTime))
				.to(TIME_FORMAT.get().format(to))
				.includeLower(false).includeUpper(true);
		
		SearchRequestBuilder search = elasticService.getNode().client().prepareSearch()
				.setIndices("event-data-*").setTypes(TYPE_NAME).setQuery(query)
				.setFrom(0).setSize(size);
		
		if (size > 0) {
			search.addSort("timestamp", SortOrder.DESC);
		}
		
		try {
			SearchResponse resp = search.execute().actionGet();
			long total = resp.getHits().getTotalHits();
			SearchHit[] hits = resp.getHits().getHits();
			for (int i = hits.length - 1; i >= 0; i --) {
				SearchHit hit = hits[i];
				String json = hit.getSourceAsString();
				if (StringUtils.isNotEmpty(json)) {
					Event event = JSON.parseObject(json, Event.class);
					events.add(event);
				}
			}
			return new PaginationList<Event>(total, events);
		} catch (IndexNotFoundException e) {
			return new PaginationList<Event>(0, new ArrayList<Event>());
		}
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
