package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.HealthCheckResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class EventService {
	private static final int CACHE_LINE_COUNT = 500;
	private LinkedBlockingQueue<Event> events;
	private String address;
	private String hostname;
	private Counter eventCounter;

	@Autowired
	private SystemService systemService;
	
	public EventService(MeterRegistry registry) {
		eventCounter = Counter.builder("event.count").register(registry);
	}
	
	@PostConstruct
	public void init() {
		events = new LinkedBlockingQueue<Event>(CACHE_LINE_COUNT);
		address = systemService.getAddress();
		hostname = systemService.getHostname();
	}
	
	public void putStartProcessEvent(String service, long pid) throws IOException {
		Event e = new Event();
		e.setAction("startProcess");
		e.setAddress(address);
		e.setHostname(hostname);
		e.setService(service);
		e.setPid(pid);
		e.setTime(new Date());
		e.setMessage(String.format("启动进程，服务：%s，进程号：%s", service, pid));
		
		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
		eventCounter.increment();
	}
	
	public void putStopProcessEvent(String service, long pid) throws IOException {
		Event e = new Event();
		e.setAction("stopProcess");
		e.setAddress(address);
		e.setHostname(hostname);
		e.setService(service);
		e.setPid(pid);
		e.setTime(new Date());
		e.setMessage(String.format("进程终止，服务：%s，进程号：%s", service, pid));
		
		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
		eventCounter.increment();
	}
	
	public void putKillProcessEvent(String service, long pid) throws IOException {
		Event e = new Event();
		e.setAction("killProcess");
		e.setAddress(address);
		e.setHostname(hostname);
		e.setService(service);
		e.setPid(pid);
		e.setTime(new Date());
		e.setMessage(String.format("杀死进程，服务：%s，进程号：%s", service, pid));
		
		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
		eventCounter.increment();
	}
	
	public void putHealthCheckFailEvent(HealthCheckResult healthCheckResult) throws IOException {
		Event e = new Event();
		e.setAction("healthCheckFail");
		e.setAddress(address);
		e.setHostname(hostname);
		e.setService(healthCheckResult.getService());
		e.setPid(healthCheckResult.getPid());
		e.setTime(healthCheckResult.getTime());
		e.setMessage(String.format("健康检查失败，服务：%s，进程号：%s (%s) (%s ms) %s", 
				healthCheckResult.getService(), 
				healthCheckResult.getPid(), 
				healthCheckResult.getEndpoint(),
				healthCheckResult.getResponseTime(), 
				healthCheckResult.getMessage()));

		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
		eventCounter.increment();
	}

	public List<Event> getEvents() {
		List<Event> list = new ArrayList<Event>();
		Event e = null;
		while ((e = events.poll()) != null) {
			list.add(e);
		}
		return list;
	}

}
