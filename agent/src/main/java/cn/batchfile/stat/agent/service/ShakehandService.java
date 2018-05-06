package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.Proc;
import cn.batchfile.stat.domain.RestResponse;

@Service
public class ShakehandService {
	protected static final Logger log = LoggerFactory.getLogger(ShakehandService.class);
	
	@Value("${master.address:}")
	private String masterAddress;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ProcService procService;
	
	@Autowired
	private AppService appService;
	
	@Autowired
	private ChoreoService choreoService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Scheduled(fixedDelay = 5000)
	public void refresh() throws IOException {
		//初始化提交节点数据
		submitNode();
		
		//更新应用数据
		fetchApps();
		
		//提交事件
		submitEvents();
		
		//提交进程列表
		submitProcs();
	}
	
	private void fetchApps() throws IOException {
		if (StringUtils.isEmpty(masterAddress)) {
			return;
		}

		//获取本地的应用名称
		List<String> existNames = appService.getApps();
		
		//抓远程的应用名称，添加时间戳消息头
		String url = String.format("%s/v1/app", masterAddress);
		String[] ary = restTemplate.getForObject(url, String[].class);
		List<String> appNames = Arrays.asList(ary);
		log.debug("get app names: {}", appNames);

		//遍历应用数据，更新应用数据
		for (String appName : appNames) {
			url = String.format("%s/v1/app/%s", masterAddress, appName);
			App app = restTemplate.getForObject(url, App.class);
			if (existNames.contains(appName)) {
				App existApp = appService.getApp(appName);
				if (!existApp.equals(app)) {
					appService.putApp(app);
					log.info("change app: {}", appName);
				}
			} else {
				appService.postApp(app);
				log.info("add app: {}", appName);
			}
		}
		
		//更新编排数据
		url = String.format("%s/v1/choreo?node=%s", masterAddress, nodeService.getNode().getId());
		Choreo[] choreos = restTemplate.getForObject(url, Choreo[].class);
		for (Choreo choreo : choreos) {
			Choreo existChoreo = choreoService.getChoreo(choreo.getApp());
			if (choreo.getScale() != existChoreo.getScale()) {
				choreoService.putScale(choreo.getApp(), choreo.getScale());
				log.info("change scale, app: {}, {}", choreo.getApp(), choreo.getScale());
			}
		}
		
		//删除多余的应用
		for (String existName : existNames) {
			if (!appNames.contains(existName)) {
				appService.deleteApp(existName);
				log.info("delete app: {}", existName);
			}
		}
	}
	
	private int submitEvents() {
		if (StringUtils.isEmpty(masterAddress)) {
			return 0;
		}
		
		List<Event> events = eventService.getEvents();
		if (events.size() > 0) {
			String url = String.format("%s/v1/event", masterAddress);
			RestResponse<?> resp = restTemplate.postForObject(url, events, RestResponse.class);
			log.debug("submit events, count: {}, resp: {}", events.size(), resp.isOk());
		}
		return events.size();
	}

	private void submitNode() throws IOException {
		if (StringUtils.isEmpty(masterAddress)) {
			return;
		}
		
		Node node = nodeService.getNode();
		String url = String.format("%s/v1/node", masterAddress);
		RestResponse<?> resp = restTemplate.postForObject(url, node, RestResponse.class);
		log.debug("submit node, resp: {}", resp.isOk());
	}
	
	private int submitProcs() throws IOException {
		if (StringUtils.isEmpty(masterAddress)) {
			return 0;
		}
		
		Node node = nodeService.getNode();
		List<Proc> ps = new ArrayList<Proc>();
		List<Long> pids = procService.getProcs();
		for (Long pid : pids) {
			Proc p = procService.getProc(pid);
			if (p != null) {
				ps.add(p);
			}
		}

		String url = String.format("%s/v1/proc?id=%s", masterAddress, 
				new URLEncoder().encode(node.getId(), Charset.forName("UTF-8")));
		
		RestResponse<?> resp = restTemplate.postForObject(url, ps, RestResponse.class);
		log.debug("submit ps, count: {}, resp: {}", ps.size(), resp.isOk());
		
		return ps.size();
	}
}
