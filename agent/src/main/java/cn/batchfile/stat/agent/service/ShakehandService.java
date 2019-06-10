package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.RestResponse;

@Service
public class ShakehandService {
	protected static final Logger log = LoggerFactory.getLogger(ShakehandService.class);
	private long putNodeTime = 0;
	private long appsTime = 0;
	private long choreoTime = 0;
	
	@Value("${master.address:}")
	private String masterAddress;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private AppService appService;
	
	@Autowired
	private ChoreoService choreoService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@PostConstruct
	public void init() {
		//启动定时器
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(() -> {
			try {
				refresh();
			} catch (Exception e) {
				//pass
			}
		}, 2, 2, TimeUnit.SECONDS);
	}
	
	private void refresh() throws IOException, InterruptedException {
		try {
			//每隔120秒向主节点汇报自己在线的消息
			if (System.currentTimeMillis() - putNodeTime >= 120000) {
				putNode();
				putNodeTime = System.currentTimeMillis();
			}
			
			//提交事件
			putEvents();
			
			//更新应用数据
			refreshApps();
		} catch (ResourceAccessException e) {
			log.error("cannot connect to master");
			Thread.sleep(30000);
		}
	}
	
	private void refreshApps() throws IOException {
		if (StringUtils.isEmpty(masterAddress)) {
			return;
		}

		//得到应用名称，添加时间戳消息头
		HttpHeaders appHeaders = new HttpHeaders();
		appHeaders.setIfModifiedSince(appsTime);
		HttpEntity<?> appEntity = new HttpEntity<>("parameters", appHeaders);
		ResponseEntity<String[]> appResp = restTemplate.exchange(String.format("%s/v1/app", masterAddress), 
				HttpMethod.GET, appEntity, String[].class);
		
		//如果返回实际内容，更新应用列表
		if (appResp.getStatusCode() == HttpStatus.OK) {
			List<String> remoteAppNames = Arrays.asList(appResp.getBody());
			log.info("get app names: {}", remoteAppNames);
			
			//获取本地的应用名称
			List<String> localAppNames = appService.getApps();

			//循环判断每一个应用，更新或者添加应用
			for (String appName : remoteAppNames) {
				String url = String.format("%s/v1/app/%s", masterAddress, appName);
				App remoteApp = restTemplate.getForObject(url, App.class);
				if (remoteApp != null && localAppNames.contains(appName)) {
					App localApp = appService.getApp(appName);
					if (localApp != null && !localApp.equals(remoteApp)) {
						appService.putApp(remoteApp);
						log.info("change app: {}", appName);
					}
				} else if (remoteApp != null) {
					appService.postApp(remoteApp);
					log.info("add app: {}", appName);
				}
			}
			
			//删除多余的应用
			for (String localAppName : localAppNames) {
				if (!remoteAppNames.contains(localAppName)) {
					appService.deleteApp(localAppName);
					log.info("delete app: {}", localAppName);
				}
			}
		}
		
		//得到编排信息，加时间戳消息头
		HttpHeaders chHeaders = new HttpHeaders();
		chHeaders.setIfModifiedSince(choreoTime);
		HttpEntity<?> chEntity = new HttpEntity<>("parameters", chHeaders);
		ResponseEntity<Choreo[]> chResp = restTemplate.exchange(
				String.format("%s/v1/choreo?node=%s", masterAddress, nodeService.getNode().getId()), 
				HttpMethod.GET, chEntity, Choreo[].class);
		
		//如果返回实际内容，更新应用列表
		if (chResp.getStatusCode() == HttpStatus.OK) {
			for (Choreo choreo : chResp.getBody()) {
				Choreo existChoreo = choreoService.getChoreo(choreo.getApp());
				if (choreo.getScale() != existChoreo.getScale()) {
					choreoService.putScale(choreo.getApp(), choreo.getScale());
					log.info("change scale, app: {}, {}", choreo.getApp(), choreo.getScale());
				}
			}
		}

		//更新时间戳
		appsTime = appResp.getHeaders().getLastModified();
		choreoTime = chResp.getHeaders().getLastModified();
	}
	
	private int putEvents() {
		if (StringUtils.isEmpty(masterAddress)) {
			return 0;
		}
		
		List<Event> events = eventService.getEvents();
		if (events.size() > 0) {
			String url = String.format("%s/v1/event", masterAddress);
			RestResponse<?> resp = restTemplate.postForObject(url, events, RestResponse.class);
			log.info("submit events, count: {}, resp: {}", events.size(), resp.isOk());
		}
		return events.size();
	}

	private void putNode() throws IOException {
		if (StringUtils.isEmpty(masterAddress)) {
			return;
		}
		
		Node node = nodeService.getNode();
		String url = String.format("%s/v1/node", masterAddress);
		restTemplate.put(url, node);
		log.info("put node successfully");
	}
	
}
