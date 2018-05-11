package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppService extends cn.batchfile.stat.service.AppService {
	
	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private AppService appService;

	@Value("${store.directory}")
	@Override
	public void setStoreDirectory(String storeDirectory) {
		super.setStoreDirectory(storeDirectory);
	}
	
	@Autowired
	@Override
	public void setChoreoService(cn.batchfile.stat.service.ChoreoService choreoService) {
		super.setChoreoService(choreoService);
	}
	
	@PostConstruct
	public void init() throws IOException {
		super.init();

		//启动定时器，每5秒刷新一次定时器，清理已经删除的应用
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(() -> {
			refresh();
		}, 5, 5, TimeUnit.SECONDS);
	}

	/**
	 * 清理数据库中已经删除的应用
	 */
	private void refresh() {
		
		//查询进程缓存
		List<String> names = new ArrayList<String>();
		try {
			int size = 100;
			long total = size;
			for (int from = 0; from + size <= total; from += size) {
				//查询数据
				SearchResponse resp = elasticService.getNode().client().prepareSearch()
						.setIndices(ProcService.INDEX_NAME).setTypes(ProcService.TYPE_NAME_APP)
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
							.setIndex(ProcService.INDEX_NAME).setType(ProcService.TYPE_NAME_APP)
							.setId(name).execute().actionGet();
					log.info("Deleted index: {}/{}/{}", 
							ProcService.INDEX_NAME, ProcService.TYPE_NAME_APP, resp.getId());
				}
			}
		} catch (IndexNotFoundException e) {
			//pass
		}
	}

}
