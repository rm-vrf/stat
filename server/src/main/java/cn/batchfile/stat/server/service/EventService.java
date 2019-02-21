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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.server.domain.Node;

@Service
public class EventService {
	protected static final Logger LOG = LoggerFactory.getLogger(EventService.class);
	private static final String INDEX_PREFIX = "event-";
	private static final String TYPE_NAME = "event";
	private static final String TS_FILE = "event-time";
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
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private NodeService nodeService;
	
	@Value("${store.directory}")
	public void setStoreDirectory(String storeDirectory) throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		eventTimeFile = new File(f, TS_FILE);
		if (!eventTimeFile.exists()) {
			FileUtils.writeByteArrayToFile(eventTimeFile, "0".getBytes());
		}
	}
	
	@PostConstruct
	public void init() throws IOException {
		// 启动定时器
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(() -> {
			try {
				refresh();
			} catch (Exception e) {
				LOG.error("error when refresh data", e);
			}
		}, 20, 10, TimeUnit.SECONDS);
	}

	public void putNodeUpEvent(Node node) {
		Event e = new Event();
		e.setAction("nodeUp");
		e.setTime(new Date());
		e.setMessage(String.format("节点上线，主机名：%s，地址：%s", node.getHostname(), node.getAddress()));
		postEvent(e);
	}
	
	public void putNodeDownEvent(Node node) {
		Event e = new Event();
		e.setAction("nodeDown");
		e.setTime(new Date());
		e.setMessage(String.format("节点下线，主机名：%s，地址：%s", node.getHostname(), node.getAddress()));
		postEvent(e);
	}
	
	public Date getTimestamp() throws IOException {
		String s = FileUtils.readFileToString(eventTimeFile, "UTF-8");
		return new Date(Long.valueOf(s));
	}
	
	public void setTimestamp(Date date) throws IOException {
		FileUtils.writeByteArrayToFile(eventTimeFile, String.valueOf(date.getTime()).getBytes());
	}
	
	public PaginationList<Event> searchEvent(Date from, int size) {
		List<Event> events = new ArrayList<Event>();
		Date to = new Date(System.currentTimeMillis() - 1000);
		QueryBuilder query = QueryBuilders.rangeQuery("time")
				.from(TIME_FORMAT.get().format(from))
				.to(TIME_FORMAT.get().format(to))
				.includeLower(false).includeUpper(true);
		
		SearchRequestBuilder search = elasticService.getNode().prepareSearch()
				.setIndices(INDEX_PREFIX + "*").setTypes(TYPE_NAME).setQuery(query)
				.setFrom(0).setSize(size);
		
		if (size > 0) {
			search.addSort("time", SortOrder.DESC);
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
		map.put("time", TIME_FORMAT.get().format(event.getTime()));
		map.put("hostname", event.getHostname());
		map.put("address", event.getAddress());
		map.put("service", event.getService());
		map.put("pid", event.getPid());
		map.put("message", event.getMessage());
		String json = JSON.toJSONString(map);
		
		String indexName = INDEX_PREFIX + DATE_FORMAT.get().format(event.getTime());
		IndexResponse resp = elasticService.getNode().prepareIndex().setIndex(indexName).setType(TYPE_NAME)
				.setSource(json, XContentType.JSON).execute().actionGet();
		
		long version = resp.getVersion();
		LOG.debug("index event, id: {}, version: {}", resp.getId(), version);
	}

	private void refresh() {
		List<Node> nodes = nodeService.getNodes(Node.STATUS_UP);
		nodes.parallelStream().forEach(node -> {
			String url = String.format("%s/api/v2/event", node.getAddress());
			Event[] events = restTemplate.getForObject(url, Event[].class);
			if (events != null) {
				for (Event event : events) {
					event.setAddress(node.getAddress());
					event.setHostname(node.getHostname());
					postEvent(event);
				}
			}
		});
	}
}
