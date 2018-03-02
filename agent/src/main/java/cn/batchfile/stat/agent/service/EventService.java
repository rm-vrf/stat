package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.HealthCheckResult;
import cn.batchfile.stat.domain.Node;

@Service
public class EventService {
	private LinkedBlockingQueue<Event> events;
	
	@Autowired
	private NodeService nodeService;

	@Value("${out.cache.line.count:500}")
	private int outCacheLineCount;

	@PostConstruct
	public void init() {
		events = new LinkedBlockingQueue<Event>(outCacheLineCount);
	}
	
	public void putStartProcessEvent(String app, long pid) throws IOException {
		Node n = nodeService.getNode();
		
		Event e = new Event();
		e.setAction("startProcess");
		e.setAddress(n.getAgentAddress());
		e.setApp(app);
		e.setHostname(n.getHostname());
		e.setNode(n.getId());
		e.setPid(pid);
		e.setTimestamp(new Date());
		e.setDesc(String.format("启动进程，应用名称：%s，进程号：%s", app, pid));
		
		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
	}
	
	public void putStopProcessEvent(String app, long pid) throws IOException {
		Node n = nodeService.getNode();
		
		Event e = new Event();
		e.setAction("stopProcess");
		e.setAddress(n.getAgentAddress());
		e.setApp(app);
		e.setHostname(n.getHostname());
		e.setNode(n.getId());
		e.setPid(pid);
		e.setTimestamp(new Date());
		e.setDesc(String.format("进程终止，应用名称：%s，进程号：%s", app, pid));
		
		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
	}
	
	public void putKillProcessEvent(String app, long pid) throws IOException {
		Node n = nodeService.getNode();
		
		Event e = new Event();
		e.setAction("killProcess");
		e.setAddress(n.getAgentAddress());
		e.setApp(app);
		e.setHostname(n.getHostname());
		e.setNode(n.getId());
		e.setPid(pid);
		e.setTimestamp(new Date());
		e.setDesc(String.format("杀死进程，应用名称：%s，进程号：%s", app, pid));
		
		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
	}
	
	public void putHealthCheckFailEvent(HealthCheckResult healthCheckResult) throws IOException {
		Node n = nodeService.getNode();
		
		Event e = new Event();
		e.setAction("healthCheckFail");
		e.setAddress(n.getAgentAddress());
		e.setApp(healthCheckResult.getApp());
		e.setHostname(n.getHostname());
		e.setNode(n.getId());
		e.setPid(healthCheckResult.getPid());
		e.setTimestamp(healthCheckResult.getTime());
		e.setDesc(String.format("健康检查失败，应用名称：%s，进程号：%s [%s] %s (%s ms) %s", 
				healthCheckResult.getApp(), 
				healthCheckResult.getPid(), 
				healthCheckResult.getEndpoint(),
				healthCheckResult.getCode(), 
				healthCheckResult.getResponseTime(), 
				healthCheckResult.getMessage()));

		if (events.remainingCapacity() < 10) {
			events.poll();
		}
		events.offer(e);
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
