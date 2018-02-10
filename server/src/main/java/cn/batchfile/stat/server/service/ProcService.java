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

import cn.batchfile.stat.domain.Proc;

@Service
public class ProcService {
	protected static final Logger log = LoggerFactory.getLogger(ProcService.class);
	private static final String INDEX_NAME = "proc-data";
	private static final String TYPE_NAME = "data";
	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}
	};
	
	@Autowired
	private ElasticService elasticService;

	public void putProcs(String id, List<Proc> ps) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("ps", ps);
		map.put("shakehandTime", TIME_FORMAT.get().format(new Date()));
		String json = JSON.toJSONString(map);
		
		UpdateResponse resp = elasticService.getNode().client().prepareUpdate().setIndex(INDEX_NAME).setType(TYPE_NAME)
				.setId(id).setDoc(json, XContentType.JSON).setUpsert(json, XContentType.JSON).execute().actionGet();
		
		long version = resp.getVersion();
		log.debug("index ps data, id: {}, version: {}", id, version);
	}
}
