package cn.batchfile.stat.server.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.domain.Instance;
import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.service.ServiceService;

@Service
public class InstanceService {
	protected static final Logger LOG = LoggerFactory.getLogger(InstanceService.class);
	private static final String PREFIX_NODE = "node__";
	private static final String PREFIX_SERVICE = "service__";
	private Map<String, Long> timestamps = new HashMap<>();
	private File storeDirectory;
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ServiceService serviceService;
	
	@Value("${store.directory}")
	public void setStoreDirectory(String storeDirectory) throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		this.storeDirectory = new File(f, "instance");
		if (!this.storeDirectory.exists()) {
			FileUtils.forceMkdir(this.storeDirectory);
		}
	}
	
	@PostConstruct
	public void init() {
		// 启动定时器
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(() -> {
			try {
				refresh();
			} catch (Exception e) {
				LOG.error("error when refresh data", e);
			}
		}, 20, 10, TimeUnit.SECONDS);
	}
	
	public List<Instance> getInstancesOfNode(String node) throws IOException {
		List<Instance> instances = null;
		File f = new File(storeDirectory, PREFIX_NODE + node);
		if (f.exists()) {
			String json = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(json)) {
				instances = JSON.parseArray(json, Instance.class);
			}
		}
		
		if (instances == null) {
			instances = new ArrayList<>();
		}
		return instances;
	}
	
	public List<Instance> getInstancesOfService(String service) throws IOException {
		List<Instance> instances = null;
		File f = new File(storeDirectory, PREFIX_SERVICE + service);
		if (f.exists()) {
			String json = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(json)) {
				instances = JSON.parseArray(json, Instance.class);
			}
		}
		
		if (instances == null) {
			instances = new ArrayList<>();
		}
		return instances;
	}
	
	public void killInstance(String node, long pid) {
		Node n = nodeService.getNode(node);
		if (n != null) {
			String url = String.format("%s/api/v2/instance/%s/_kill", n.getAddress(), pid);
			restTemplate.postForLocation(url, null);
		}
	}
	
	public String[] getSystemOut(String node, long pid) {
		Node n = nodeService.getNode(node);
		if (n != null) {
			String url = String.format("%s/api/v2/instance/%s/stdout", n.getAddress(), pid);
			return restTemplate.getForObject(url, String[].class);
		} else {
			return new String[] {};
		}
	}

	public String[] getSystemErr(String node, long pid) {
		Node n = nodeService.getNode(node);
		if (n != null) {
			String url = String.format("%s/api/v2/instance/%s/stderr", n.getAddress(), pid);
			return restTemplate.getForObject(url, String[].class);
		} else {
			return new String[] {};
		}
	}
	
	private void putInstancesOfNode(String node, List<Instance> instances) throws IOException {
		String json = JSON.toJSONString(instances, SerializerFeature.PrettyFormat);
		File file = new File(storeDirectory, PREFIX_NODE + node);
		FileUtils.writeByteArrayToFile(file, json.getBytes("UTF-8"));
	}
	
	private void putInstancesOfService(String service, List<Instance> instances) throws IOException {
		String json = JSON.toJSONString(instances, SerializerFeature.PrettyFormat);
		File file = new File(storeDirectory, PREFIX_SERVICE + service);
		FileUtils.writeByteArrayToFile(file, json.getBytes("UTF-8"));
	}
	
	private void deleteInstancesOfNode(String node) {
		File file = new File(storeDirectory, PREFIX_NODE + node);
		FileUtils.deleteQuietly(file);
	}
	
	private void deleteInstancesOfService(String service) {
		File file = new File(storeDirectory, PREFIX_SERVICE + service);
		FileUtils.deleteQuietly(file);
	}
	
	private void refresh() throws IOException {
		
		//获取所有的服务
		List<cn.batchfile.stat.domain.Service> services = serviceService.getServices();
		
		//获取所有的在线节点
		List<Node> nodes = nodeService.getNodes(Node.STATUS_UP);
		
		//更新变动的进程
		boolean change = updateInstancesOfNodes(nodes);
		
		//删除已经不存在的节点和服务
		change = change || deleteInstances(nodes, services);
		
		//如果发生了更新，重新聚合数据
		if (change) {
			List<Instance> instances = getInstances();
			groupByService(instances, services);
		}
	}

	private boolean deleteInstances(List<Node> nodes, List<cn.batchfile.stat.domain.Service> services) {
		boolean change = false;
		
		// 转换列表的数据结构
		List<String> nodeIds = nodes.stream().map(node -> node.getId()).collect(Collectors.toList());
		List<String> serviceNames = services.stream().map(service -> service.getName()).collect(Collectors.toList());
		
		// 删掉多余的数据
		File[] files = storeDirectory.listFiles();
		for (File file : files) {
			String name = file.getName();
			if (StringUtils.startsWith(name, PREFIX_NODE)) {
				String nodeId = StringUtils.substring(name, PREFIX_NODE.length());
				if (!nodeIds.contains(nodeId)) {
					deleteInstancesOfNode(nodeId);
					LOG.info("delete instances of node: {}", nodeId);
					change = true;
				}
			} else if (StringUtils.startsWith(name, PREFIX_SERVICE)) {
				String serviceName = StringUtils.substring(name, PREFIX_SERVICE.length());
				if (!serviceNames.contains(serviceName)) {
					deleteInstancesOfService(serviceName);
					LOG.info("delete instances of servie: {}", serviceName);
					change = true;
				}
			}
		}
		
		return change;
	}

	private void groupByService(List<Instance> instances, List<cn.batchfile.stat.domain.Service> services) throws IOException {
		
		// 按照服务名称归类
		Map<String, List<Instance>> collector = instances.stream().collect(Collectors.groupingBy(in -> in.getService()));
		for (Entry<String, List<Instance>> entry : collector.entrySet()) {
			String service = entry.getKey();
			List<Instance> ins = entry.getValue();
			if (ins == null) {
				ins = new ArrayList<>();
			}
			putInstancesOfService(service, ins);
			LOG.info("update instance list, service: {}, count: {}", service, ins.size());
		}

		// 对缺失的进程设置空集合
		for (cn.batchfile.stat.domain.Service service : services) {
			if (!collector.containsKey(service.getName())) {
				putInstancesOfService(service.getName(), new ArrayList<>());
				LOG.info("update instance list, service: {}, count: {}", service.getName(), 0);
			}
		}
	}

	private boolean updateInstancesOfNodes(List<Node> nodes) {
		Map<String, Long> timestamps = new HashMap<>();
		AtomicBoolean change = new AtomicBoolean(false);
		
		nodes.parallelStream().forEach(node -> {
			String url = String.format("%s/api/v2/instance", node.getAddress());
			try {
				//向节点发出询问消息，带时间戳
				HttpHeaders headers = new HttpHeaders();
				headers.setIfModifiedSince(this.timestamps.containsKey(node.getId()) ? this.timestamps.get(node.getId()) : 0L);
				HttpEntity<?> entity = new HttpEntity<>("parameters", headers);
				ResponseEntity<Instance[]> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Instance[].class);
				
				//如果有实际内容，加入进程列表
				if (resp.getStatusCode() == HttpStatus.OK) {
					LOG.info("GET {} {}", url, entity.toString());
					List<Instance> ins = new ArrayList<>();
					for (Instance in : resp.getBody()) {
						in.setNode(node.getId());
						ins.add(in);
					}
					
					//更新进程列表
					putInstancesOfNode(node.getId(), ins);
					LOG.info("update instance list, node: {}, count: {}", node.getId(), ins.size());
					change.set(true);
				}
				
				//更新变动节点的时间戳
				timestamps.put(node.getId(), resp.getHeaders().getLastModified());
				
			} catch (ResourceAccessException e) {
				LOG.error("error when get instance list, id: " + node.getId() + ", address: " + node.getAddress(), e);
			} catch (IOException e) {
				LOG.error("error when update list, id: " + node.getId() + ", address: " + node.getAddress(), e);
			}
		});
		
		//更新所有的时间戳
		this.timestamps.clear();
		this.timestamps.putAll(timestamps);
		
		//返回值
		return change.get();
	}

	private List<Instance> getInstances() throws IOException {
		List<Instance> instances = new ArrayList<>();
		File[] files = storeDirectory.listFiles();
		for (File file : files) {
			if (StringUtils.startsWith(file.getName(), PREFIX_NODE)) {
				String json = FileUtils.readFileToString(file, "UTF-8");
				if (StringUtils.isNotEmpty(json)) {
					List<Instance> list = JSON.parseArray(json, Instance.class);
					if (list != null) {
						instances.addAll(list);
					}
				}
			}
		}
		return instances;
	}
	
}
