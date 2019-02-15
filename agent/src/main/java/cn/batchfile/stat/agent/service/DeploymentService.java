package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.domain.Deployment;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

@org.springframework.stereotype.Service
public class DeploymentService extends cn.batchfile.stat.service.DeploymentService {
	
	protected static final Logger LOG = LoggerFactory.getLogger(DeploymentService.class);
	private long timestamp = 0;
	
	@Autowired
	private ApplicationRegistrator applicationRegistrator;

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${master.schema}")
	private String masterSchema;

	@Value("${master.address}")
	private String masterAddress;

	@Value("${master.port}")
	private int masterPort;

	@Value("${store.directory}")
	public void setStoreDirectory(String storeDirectory) throws IOException {
		super.setStoreDirectory(storeDirectory);
	}
	
	@PostConstruct
	public void init() {
		//启动定时器
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(() -> {
			try {
				refresh();
			} catch (Exception e) {
				LOG.error("error when refresh data", e);
			}
		}, 5, 5, TimeUnit.SECONDS);
	}
	
	private void refresh() throws IOException {
		LOG.debug("self id: {}", applicationRegistrator.getRegisteredId());
		String registeredId = applicationRegistrator.getRegisteredId();
		if (StringUtils.isEmpty(registeredId)) {
			return;
		}
		
		//得到部署计划，添加时间戳消息头
		HttpHeaders headers = new HttpHeaders();
		headers.setIfModifiedSince(timestamp);
		HttpEntity<?> entity = new HttpEntity<>("parameters", headers);
		String url = String.format("%s://%s:%s/api/v2/deployment?node=%s", masterSchema, masterAddress, masterPort, registeredId);
		ResponseEntity<Deployment[]> resp = null;
		try {
			resp = restTemplate.exchange(url, HttpMethod.GET, entity, Deployment[].class);
		} catch (ResourceAccessException e) {
			return;
		}
		
		//如果没有返回实际内容，退出操作
		if (resp.getStatusCode() != HttpStatus.OK) {
			return;
		}
		List<Deployment> remoteDeployments = new ArrayList<>();
		for (Deployment d : resp.getBody()) {
			remoteDeployments.add(d);
		}
		LOG.info("get deployment list from master, count: {}", remoteDeployments.size());
		
		//得到本地存储的服务
		List<Deployment> localDeployments = getDeployments();
		
		//转换数据结构
		Map<String, List<Deployment>> remoteMap = remoteDeployments.stream().collect(Collectors.groupingBy(d -> d.getService()));
		Map<String, List<Deployment>> localMap = localDeployments.stream().collect(Collectors.groupingBy(d -> d.getService()));
		
		//更新远程数据
		for (Entry<String, List<Deployment>> entry : remoteMap.entrySet()) {
			String serviceName = entry.getKey();
			List<Deployment> deployments = entry.getValue();
			putDeployments(serviceName, deployments);
			LOG.info("put deployments, service: {}, count: {}", serviceName, deployments.size());
		}
		
		//删除多余的数据
		for (String serviceName : localMap.keySet()) {
			if (!remoteMap.containsKey(serviceName)) {
				deleteDeployment(serviceName);
				LOG.info("delete deployments, service: {}", serviceName);
			}
		}
		
		//更新时间戳
		timestamp = resp.getHeaders().getLastModified();
	}

}
