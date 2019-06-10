package cn.batchfile.stat.server.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Everything;

@Service
public class SysService {
	protected static final Logger log = LoggerFactory.getLogger(SysService.class);
	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:'00.000'Z");
		}
	};
	private static final ThreadLocal<DateFormat> ID_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMddHHmm");
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

	public void putEverything(Everything everything) {
		Date time = new Date();
		
		index("net-stat-", everything.getId(), everything.getHostname(), time, everything.getNetworkStats());
		index("mem-stat-", everything.getId(), everything.getHostname(), time, everything.getMemoryStat());
		index("disk-stat-", everything.getId(), everything.getHostname(), time, everything.getDiskStats());
		index("os-stat-", everything.getId(), everything.getHostname(), time, everything.getOsStat());
		index("cpu-stat-", everything.getId(), everything.getHostname(), time, everything.getCpuStat());
		index("proc-stat-", everything.getId(), everything.getHostname(), time, everything.getProcStats());
	}

	private void index(String prefix, String node, String hostname, Date time, Object data) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("node", node);
		map.put("hostname", hostname);
		map.put("time", TIME_FORMAT.get().format(time));
		map.put("data", data);
		
		String json = JSON.toJSONString(map);
		
		IndexResponse resp = elasticService.getNode().client().prepareIndex()
				.setIndex(prefix + DATE_FORMAT.get().format(time)).setType("stat")
				.setId(node + ID_FORMAT.get().format(time)).setSource(json, XContentType.JSON)
				.execute().actionGet();
		
		log.debug("index stat: {}/{}/{}", resp.getIndex(), resp.getType(), resp.getId());
	}
}
