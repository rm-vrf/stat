package cn.batchfile.stat.server.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
		if (this.storeDirectory.exists()) {
			FileUtils.deleteQuietly(this.storeDirectory);
		}
		
		FileUtils.forceMkdir(this.storeDirectory);
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
	
	public List<Instance> getInstances() throws IOException {
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
	
	public List<Instance> getInstancesOfNode(String nodeId) throws IOException {
		List<Instance> instances = null;
		File f = new File(storeDirectory, PREFIX_NODE + nodeId);
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
	
	public List<Instance> getInstancesOfService(String serviceName) throws IOException {
		List<Instance> instances = null;
		File f = new File(storeDirectory, PREFIX_SERVICE + serviceName);
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
	
	private void refresh() throws IOException {
		
		//得到当前的进程
		List<Instance> instances = getInstances();
		LOG.debug("get instances from store, size: {}", instances.size());
		
		//得到变动的进程
		List<Node> nodes = nodeService.getNodes(Node.STATUS_UP);
		List<Instance> changeInstances = getChangeInstances(nodes);
		LOG.debug("get change instances, size: {}", changeInstances.size());
		
		//合并进程列表
		instances = mergeInstances(instances, changeInstances);
		LOG.debug("merge instance list, size: {}", instances.size());
		
		//得到所有的服务
		List<cn.batchfile.stat.domain.Service> services = serviceService.getServices();
		
		//更新进程表
		updateInstances(instances, nodes, services);
	}

	private void updateInstances(List<Instance> instances, List<Node> nodes,
			List<cn.batchfile.stat.domain.Service> services) throws UnsupportedEncodingException, IOException {
		
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
					FileUtils.deleteQuietly(file);
					LOG.info("delete instance file: {}", file.getName());
				}
			} else if (StringUtils.startsWith(name, PREFIX_SERVICE)) {
				String serviceName = StringUtils.substring(name, PREFIX_SERVICE.length());
				if (!serviceNames.contains(serviceName)) {
					FileUtils.deleteQuietly(file);
					LOG.info("delete instance file: {}", file.getName());
				}
			}
		}
		
		// 按照节点代号归类
		Map<String, List<Instance>> collector = instances.stream().collect(Collectors.groupingBy(in -> in.getNode()));
		for (Entry<String, List<Instance>> entry : collector.entrySet()) {
			String json = JSON.toJSONString(entry.getValue(), SerializerFeature.PrettyFormat);
			File file = new File(storeDirectory, PREFIX_NODE + entry.getKey());
			FileUtils.writeByteArrayToFile(file, json.getBytes("UTF-8"));
		}

		// 按照服务名称归类
		collector = instances.stream().collect(Collectors.groupingBy(in -> in.getService()));
		for (Entry<String, List<Instance>> entry : collector.entrySet()) {
			String json = JSON.toJSONString(entry.getValue(), SerializerFeature.PrettyFormat);
			File file = new File(storeDirectory, PREFIX_SERVICE + entry.getKey());
			FileUtils.writeByteArrayToFile(file, json.getBytes("UTF-8"));
		}
	}

	private List<Instance> mergeInstances(List<Instance> list1, List<Instance> list2) {
		Map<String, Instance> map = new HashMap<>();
		for (Instance e : list1) {
			map.put(String.format("%s %s", e.getNode(), e.getPid()), e);
		}
		
		for (Instance e : list2) {
			map.put(String.format("%s %s", e.getNode(), e.getPid()), e);
		}
		
		return map.values().stream().collect(Collectors.toList());
	}
	
	private List<Instance> getChangeInstances(List<Node> nodes) {
		List<Instance> instances = new ArrayList<>();
		Map<String, Long> timestamps = new HashMap<>();
		
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
					for (Instance in : resp.getBody()) {
						in.setNode(node.getId());
						instances.add(in);
					}
				}
				
				//更新变动节点的时间戳
				timestamps.put(node.getId(), resp.getHeaders().getLastModified());
			} catch (ResourceAccessException e) {
				LOG.error("error when get instance list, id: " + node.getId() + ", address: " + node.getAddress(), e);
			}
		});
		
		//更新所有的时间戳
		this.timestamps.clear();
		this.timestamps.putAll(timestamps);
		
		//返回值
		return instances;
	}

}
