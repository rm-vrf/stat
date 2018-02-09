package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.HealthCheck;
import cn.batchfile.stat.domain.HealthCheckResult;
import cn.batchfile.stat.domain.Proc;

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
	
	@Scheduled(fixedDelay = 5000)
	public void refresh() throws IOException {
		synchronized (this) {
			//移除多余的健康检查器
			List<Long> pids = procService.getProcs();
			cleanHealthCheck(pids);
			
			//寻找本地的所有进程，重建健康检查
			restoreHealthCheck(pids);
		}
	}
	
	public List<HealthCheckResult> getResults(long pid) {
		List<HealthCheckResult> list = new ArrayList<HealthCheckResult>();
		HealthCheckHandler handler = handlers.get(pid);
		if (handler != null) {
			LinkedBlockingQueue<HealthCheckResult> queue = handler.results;
			if (queue != null) {
				HealthCheckResult e = null;
				while ((e = queue.poll()) != null) {
					list.add(e);
				}
			}
		}
		return list;
	}
	
	public List<HealthCheckResult> getResults() {
		List<HealthCheckResult> list = new ArrayList<HealthCheckResult>();
		for (long pid : handlers.keySet()) {
			list.addAll(getResults(pid));
		}
		return list;
	}
	
	public void register(App app, Proc p) throws IOException {
		
		HealthCheck hc = app.getHealthChecks() == null || app.getHealthChecks().size() == 0 ? 
				null : app.getHealthChecks().get(0);

		if (hc == null) {
			log.info("no health check for app: {}", app.getName());
			return;
		}
		
		ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
		timer.scheduleWithFixedDelay(  
	            new Runnable() {
					@Override
					public void run() {
						try {
							check(app, hc, p);
						} catch (Exception e) {}
					}
				},  
	            hc.getInitialDelaySeconds(),  
	            hc.getIntervalSeconds(),  
	            TimeUnit.SECONDS);
		
		HealthCheckHandler handler = new HealthCheckHandler();
		handler.consecutiveFailures = 0;
		handler.results = new LinkedBlockingQueue<HealthCheckResult>(outCacheLineCount);
		handler.timer = timer;
		
		handlers.put(p.getPid(), handler);
	}
	
	private void check(App app, HealthCheck hc, Proc p) throws Exception {
		log.debug("health check app: {}, pid: {}", app.getName(), p.getPid());
		HealthCheckHandler handler = handlers.get(p.getPid());
		
		if (p == null || hc == null || app == null) {
			return;
		}

		//执行检查
		String protocal = hc.getProtocol();
		int port = p.getPorts().get(hc.getPortIndex());
		String path = hc.getPath();
		int timeout = hc.getTimeoutSeconds() * 1000;
		HealthCheckResult ret = sendRequest(app.getName(), p.getPid(), protocal, port, path, timeout);
		
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
				log.info("EXCEED MAX CONSECUTIVE FAILURES, KILL PROCESS: {}, APP: {}", p.getPid(), app.getName());
				procService.killProcs(Arrays.asList(new Long[] {p.getPid()}));
			}
		}
	}

	private HealthCheckResult sendRequest(String app, long pid, String protocal, int port, String path, int timeout) {
		HealthCheckResult ret = new HealthCheckResult();
		ret.setApp(app);
		ret.setPid(pid);
		ret.setTime(new Date());
		
		String uri = String.format("%s://%s:%s%s", StringUtils.lowerCase(protocal), "127.0.0.1", port, path);
		
		//构建请求对象
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.build();
		
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;

		try {
			//执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			HttpGet req = new HttpGet(uri);
			ret.setEndpoint(req.toString());
			long beginTime = System.currentTimeMillis();
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
	
	private void restoreHealthCheck(List<Long> pids) throws IOException {
		for (Long pid : pids) {
			if (!handlers.containsKey(pid)) {
				Proc p = procService.getProc(pid);
				App app = p == null ? null : appService.getApp(p.getApp());
				if (p != null && app != null 
						&& app.getHealthChecks() != null 
						&& app.getHealthChecks().size() > 0) {
					register(app, p);
				}
			}
		}
	}
	
	private void cleanHealthCheck(List<Long> pids) {
		//找到已经停止的进程
		List<Long> removePids = new ArrayList<Long>();
		for (Long pid : handlers.keySet()) {
			if (!pids.contains(pid)) {
				removePids.add(pid);
			}
		}

		//移除多余的健康检查器
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

	class HealthCheckHandler {
		int consecutiveFailures = 0;
		LinkedBlockingQueue<HealthCheckResult> results;
		ScheduledExecutorService timer;
	}
}
