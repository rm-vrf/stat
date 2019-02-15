package cn.batchfile.stat.agent.service;

import java.io.IOException;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.domain.Service;
import cn.batchfile.stat.service.ServiceService;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

@org.springframework.stereotype.Service
public class DeploymentService {
	
	protected static final Logger LOG = LoggerFactory.getLogger(DeploymentService.class);
	private long timestamp = 0;
	
	@Autowired
	private ApplicationRegistrator applicationRegistrator;

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ServiceService serviceService;
	
	@Value("${master.schema}")
	private String masterSchema;

	@Value("${master.address}")
	private String masterAddress;

	@Value("${master.port}")
	private int masterPort;

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
		
		//得到应用名称，添加时间戳消息头
		HttpHeaders headers = new HttpHeaders();
		headers.setIfModifiedSince(timestamp);
		HttpEntity<?> entity = new HttpEntity<>("parameters", headers);
		String url = String.format("%s://%s:%s/api/v2/service", 
				masterSchema, masterAddress, masterPort);
		ResponseEntity<Service[]> resp = null;
		try {
			resp = restTemplate.exchange(url, HttpMethod.GET, entity, Service[].class);
		} catch (ResourceAccessException e) {
			return;
		}
		
		//如果没有返回实际内容，退出操作
		if (resp.getStatusCode() != HttpStatus.OK) {
			return;
		}
		Service[] remoteServices = resp.getBody();

		//得到本地存储的服务
		Service[] localServices = serviceService.getServices().toArray(new Service[] {});
		
		//循环判断每一个远程应用，更新或者添加应用
		for (Service remoteService : remoteServices) {
			if (exist(localServices, remoteService)) {
				serviceService.putService(remoteService);
				LOG.info("change service: {}", remoteService.getName());
			} else {
				serviceService.postService(remoteService);
				LOG.info("add service: {}", remoteService.getName());
			}
		}
		
		//循环判断每一个本地应用，删除不存在的应用
		for (Service localService : localServices) {
			if (!exist(remoteServices, localService)) {
				serviceService.deleteService(localService.getName());
				LOG.info("delete service: {}", localService.getName());
			}
		}
		
		//更新时间戳
		timestamp = resp.getHeaders().getLastModified();
	}
	
	private boolean exist(Service[] list, Service element) {
		for (Service service : list) {
			if (StringUtils.equals(service.getName(), element.getName())) {
				return true;
			}
		}
		return false;
	}

}
