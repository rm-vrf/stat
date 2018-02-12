package cn.batchfile.stat.server.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
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

import cn.batchfile.stat.domain.Proc;

@Service
public class ProcService {
	protected static final Logger log = LoggerFactory.getLogger(ProcService.class);
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
	private AppService appService;

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
	
	/**
	 * 按照应用名称归类数据
	 */
	@Scheduled(fixedDelay = 5000)
	public void groupProcs() {
		//查询所有的节点
		List<Proc> ps = ps();
		
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
		
		//TODO 清理已经删除的app，不清理会留下垃圾数据，没有实际影响
	}
	
	//TODO 刷新分配表，这是核心的功能
	
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
	
	private List<Proc> ps() {
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
	
	
}
