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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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

import cn.batchfile.stat.agent.util.cmd.CommandLineUtils;
import cn.batchfile.stat.domain.HealthCheckResult;
import cn.batchfile.stat.domain.Instance;
import cn.batchfile.stat.domain.Service;
import cn.batchfile.stat.service.ServiceService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@org.springframework.stereotype.Service
public class HealthCheckService {
	protected static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);
	private static final int CACHE_LINE_COUNT = 500;
	private Map<Long, HealthCheckHandler> handlers = new ConcurrentHashMap<Long, HealthCheckHandler>();
	private Counter okCounter;
	private Counter failCounter;
	
	public HealthCheckService(MeterRegistry registry) {
		okCounter = Counter.builder("health.check.ok").register(registry);
		failCounter = Counter.builder("health.check.fail").register(registry);
		Gauge.builder("health.check.running", "/", s -> handlers.size()).register(registry);
	}
	
	@Autowired
	private ServiceService serviceService;
	
	@Autowired
	private InstanceService instanceService;
	
	@Autowired
	private EventService eventService;

	@Autowired
	private SystemService systemService;

	@PostConstruct
	public void init() {
		//启动定时器
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					refresh();
				} catch (Exception e) {
					log.error("error when health check", e);
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
	}
	
	private void refresh() throws IOException {
		//移除多余的健康检查器
		List<Instance> ins = instanceService.getInstances();
		cleanHealthCheck(ins);
		
		//寻找本地的所有进程，重建健康检查
		restoreHealthCheck(ins);
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
	
	public void register(Service service, Instance instance) throws IOException {
		
		if (service.getHealthCheck() == null || !service.getHealthCheck().getEnabled()) {
			log.info("no health check for app: {}", service.getName());
			return;
		}
		
		ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
		timer.scheduleWithFixedDelay(  
	            new Runnable() {
					@Override
					public void run() {
						try {
							check(service, instance);
						} catch (Exception e) {}
					}
				},  
	            service.getHealthCheck().getStartPeriod(),
	            service.getHealthCheck().getInterval(),  
	            TimeUnit.SECONDS);
		
		HealthCheckHandler handler = new HealthCheckHandler();
		handler.consecutiveFailures = 0;
		handler.results = new LinkedBlockingQueue<HealthCheckResult>(CACHE_LINE_COUNT);
		handler.timer = timer;
		
		handlers.put(instance.getPid(), handler);
	}
	
	private void check(Service service, Instance in) throws Exception {
		if (service == null 
				|| service.getHealthCheck() == null 
				|| !service.getHealthCheck().getEnabled() 
				|| in == null) {
			return;
		}
		
		log.debug("health check service: {}, pid: {}", service.getName(), in.getPid());
		HealthCheckHandler handler = handlers.get(in.getPid());
		HealthCheckResult ret = null;

		//检查 HTTP 端口
		if (service.getHealthCheck().getHttpGet() != null) {
			cn.batchfile.stat.domain.HttpGet hg = service.getHealthCheck().getHttpGet();
			ret = checkHttpRequest(service, in, 
					hg.getProtocol(), 
					in.getPorts().get(hg.getPortIndex()), 
					hg.getUri(), 
					service.getHealthCheck().getTimeout() * 1000);
		} 

		//检查命令行
		if (service.getHealthCheck().getCommand() != null) {
			String test = service.getHealthCheck().getCommand().getTest();
			String check = service.getHealthCheck().getCommand().getCheck();
			ret = checkCommand(service, in, test, check, service.getHealthCheck().getTimeout());
		}
		
		if (ret == null) {
			//无效的检查设置
			return;
		}
		log.debug("health check, service: {}, pid: {}, result: {}", service.getName(), in.getPid(), ret.getMessage());

		//保存检查结果
		if (handler.results.remainingCapacity() < 1) {
			handler.results.poll();
		}
		handler.results.offer(ret);
		
		//判断检查结果
		if (ret.isOk()) {
			//计数器归零
			handler.consecutiveFailures = 0;
			okCounter.increment();
		} else {
			failCounter.increment();
			//累加计数器
			handler.consecutiveFailures ++;
			
			//报告事件
			eventService.putHealthCheckFailEvent(ret);
			
			//超过失败次数，杀进程
			if (handler.consecutiveFailures >= service.getHealthCheck().getRetries()) {
				log.info("EXCEED MAX CONSECUTIVE FAILURES, KILL PROCESS: {}, SERVICE: {}", in.getPid(), service.getName());
				instanceService.killInstances(Arrays.asList(new Long[] {in.getPid()}));
			}
		}
	}
	
	private HealthCheckResult checkCommand(Service service, Instance instance, 
			String command, String check, int timeoutInSeconds) throws Exception {
		
		HealthCheckResult ret = new HealthCheckResult();
		ret.setService(service.getName());
		ret.setPid(instance.getPid());
		ret.setTime(new Date());
		
		StringBuilder out = new StringBuilder(StringUtils.EMPTY);
		StringBuilder err = new StringBuilder(StringUtils.EMPTY);
		Map<String, String> vars = systemService.createVars(instance.getPorts(), service.getEnvironment());
		
		long beginTime = System.currentTimeMillis();
		int i = CommandLineUtils.execute(command, service.getWorkDirectory(), vars, timeoutInSeconds, out, err);
		ret.setResponseTime(System.currentTimeMillis() - beginTime);
		
		ret.setEndpoint(command);
		String message = String.format("RET: %s, OUT: %s, ERR: %s", i, out.toString(), err.toString());
		ret.setMessage(message);
		ret.setOk(StringUtils.contains(out.toString(), check));
		
		return ret;
	}

	private HealthCheckResult checkHttpRequest(Service service, Instance instance, String protocal, int port, String path, int timeout) {
		HealthCheckResult ret = new HealthCheckResult();
		ret.setService(service.getName());
		ret.setPid(instance.getPid());
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
			ret.setMessage(code + " " + resp.getStatusLine().getReasonPhrase());
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
	
	private void restoreHealthCheck(List<Instance> ins) throws IOException {
		List<Long> pids = ins.stream().map(in -> in.getPid()).collect(Collectors.toList());
		for (Long pid : pids) {
			if (!handlers.containsKey(pid)) {
				Instance in = instanceService.getInstance(pid);
				Service service = in == null ? null : serviceService.getService(in.getService());
				if (in != null && service != null 
						&& service.getHealthCheck() != null 
						&& service.getHealthCheck().getEnabled()) {
					register(service, in);
				}
			}
		}
	}
	
	private void cleanHealthCheck(List<Instance> ins) {
		//找到已经停止的进程
		List<Long> removePids = new ArrayList<Long>();
		List<Long> pids = ins.stream().map(in -> in.getPid()).collect(Collectors.toList());
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
