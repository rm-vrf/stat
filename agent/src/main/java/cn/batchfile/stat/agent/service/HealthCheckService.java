package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.agent.types.App;
import cn.batchfile.stat.agent.types.HealthCheck;
import cn.batchfile.stat.agent.types.HealthCheckResult;
import cn.batchfile.stat.agent.types.Proc;

@Service
public class HealthCheckService {
	protected static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);
	private Map<Long, HealthCheckHandler> handlers = new ConcurrentHashMap<Long, HealthCheckHandler>();
	
	@Value("${out.cache.line.count:500}")
	private int outCacheLineCount;

	@Autowired
	private AppService appService;
	
	@Autowired
	private ProcService procService;
	
	@Scheduled(fixedDelay = 1000)
	public void init() throws IOException {
		//找到已经停止的进程
		List<Long> removePids = new ArrayList<Long>();
		Iterator<Long> iter = handlers.keySet().iterator();
		while (iter.hasNext()) {
			Long pid = iter.next();
			Proc p = procService.getProc(pid);
			if (p == null) {
				removePids.add(pid);
			}
		}
		
		for (Long pid : removePids) {
			//废除计时器
			HealthCheckHandler handler = handlers.remove(pid);
			try {
				handler.timer.shutdownNow();
			} catch (Exception e) {}
			
			//清空输出
			try {
				handler.results.clear();
			} catch (Exception e) {}
		}
	}
	
	public void register(String app, long pid) throws IOException {
		
		App appObject = appService.getApp(app);
		HealthCheck hc = appObject.getHealthChecks() == null || appObject.getHealthChecks().size() == 0 ? 
				null : appObject.getHealthChecks().get(0);

		if (hc == null) {
			log.info("no health check for app: {}", app);
			return;
		}
		
		ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
		timer.scheduleWithFixedDelay(  
	            new Runnable() {
					@Override
					public void run() {
						try {
							check(app, pid);
						} catch (Exception e) {}
					}
				},  
	            hc.getInitialDelaySeconds(),  
	            hc.getIntervalSeconds(),  
	            TimeUnit.SECONDS);
		
		HealthCheckHandler handler = new HealthCheckHandler();
		handler.consecutiveFailures = 0;
		handler.results = new LinkedBlockingQueue<HealthCheckResult>();
		handler.timer = timer;
		
		handlers.put(pid, handler);
	}
	
	private void check(String app, long pid) throws Exception {
		log.debug("health check app: {}, pid: {}", app, pid);
		HealthCheckHandler handler = handlers.get(pid);
		
		App appObject = appService.getApp(app);
		HealthCheck hc = appObject.getHealthChecks() == null || appObject.getHealthChecks().size() == 0 ? 
				null : appObject.getHealthChecks().get(0);
		
		Proc p = procService.getProc(pid);
		
		if (p == null || hc == null) {
			return;
		}

		//执行检查
		HealthCheckResult ret = check(appObject, p, hc);
		
		//保存检查结果
		if (handler.results.remainingCapacity() < 1) {
			handler.results.poll();
		}
		handler.results.offer(ret);
		
		//判断检查结果
		if (ret.isOk()) {
			//计数器归零
			handler.consecutiveFailures = 0;
		} else {
			//累加计数器
			handler.consecutiveFailures ++;
			
			//超过失败次数，杀进程
			if (handler.consecutiveFailures > hc.getMaxConsecutiveFailures()) {
				log.info("EXCEED MAX CONSECUTIVE FAILURES, KILL PROCESS: {}, APP: {}", pid, app);
				procService.killProcs(Arrays.asList(new Long[] {pid}));
			}
		}
	}

	private HealthCheckResult check(App app, Proc p, HealthCheck hc) {
		HealthCheckResult ret = new HealthCheckResult();
		ret.setApp(app.getName());
		ret.setPid(p.getPid());
		ret.setTime(new Date());
		
		String protocal = hc.getProtocol();
		String uri = String.format("%s://%s:%s%s", StringUtils.lowerCase(protocal), "127.0.0.1", p.getPorts().get(hc.getPortIndex()), hc.getPath());
		
		//构建请求对象
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(hc.getTimeoutSeconds() * 1000)
				.setSocketTimeout(hc.getTimeoutSeconds() * 1000)
				.setConnectionRequestTimeout(hc.getTimeoutSeconds() * 1000)
				.build();
		
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;

		long beginTime = System.currentTimeMillis();
		try {
			//执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			HttpGet req = new HttpGet(uri);
			resp = httpClient.execute(req);
			
			//检查返回代码
			int code = resp.getStatusLine().getStatusCode();
			ret.setCode(code);
			if (code >= 200 && code < 300) {
				ret.setOk(true);
				ret.setResponseTime(System.currentTimeMillis() - beginTime);
			} else {
				ret.setOk(false);
				ret.setMessage(resp.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			ret.setOk(false);
			ret.setMessage(e.getMessage());
		} finally {
			IOUtils.closeQuietly(resp);
			IOUtils.closeQuietly(httpClient);
		}
		
		return ret;
	}

	class HealthCheckHandler {
		int consecutiveFailures = 0;
		LinkedBlockingQueue<HealthCheckResult> results;
		ScheduledExecutorService timer;
	}
}
