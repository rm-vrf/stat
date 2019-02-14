package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.domain.Disk;
import cn.batchfile.stat.domain.Memory;
import cn.batchfile.stat.domain.Network;
import cn.batchfile.stat.domain.Os;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.server.domain.Node;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import reactor.core.publisher.Flux;

@Service
public class NodeService {
	protected static final Logger LOG = LoggerFactory.getLogger(NodeService.class);
	private static final String APPLICATION_NAME = "stat-agent";
	private static final String INDEX_NAME = "node";
	private static final String TYPE_NAME = "node";
	private Map<String, Long> timestamps = new HashMap<String, Long>();

	@Autowired
	private ElasticService elasticService;
	
	@Autowired
	private InstanceRegistry registry;

	@Autowired
	private RestTemplate restTemplate;

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
		}, 5, 5, TimeUnit.SECONDS);
	}

	public Node getNode(String id) {
		Node node = null;
		try {
			// 获取数据
			GetResponse resp = elasticService.getNode().prepareGet().setIndex(INDEX_NAME).setType(TYPE_NAME).setId(id)
					.execute().actionGet();

			if (resp.isExists()) {
				node = JSON.parseObject(resp.getSourceAsString(), Node.class);
			}
		} catch (IndexNotFoundException e) {
			// pass
		}

		return node;
	}
	
	public void putNode(Node node) {
		String id = node.getId();
		String json = JSON.toJSONString(node);
		IndexResponse resp = elasticService.getNode().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME)
				.setId(id).setSource(json, XContentType.JSON).execute().actionGet();
		long version = resp.getVersion();
		LOG.info("put node, id: {}, version: {}", id, version);
	}
	
	public List<Node> getNodes(String status) {
		List<Node> list = new ArrayList<>();
		int from = 0;
		int size = 100;
		long total = 0;
		while (from <= total) {
			PaginationList<Node> nodes = searchNodes("*", status, from, size);
			total = nodes.getTotal();
			list.addAll(nodes.getRows());
			from += size;
		}
		return list;
	}

	public PaginationList<Node> searchNodes(String query, String status, int from, int size) {
		List<Node> nodes = new ArrayList<Node>();

		// 构建查询条件
		BoolQueryBuilder bool = QueryBuilders.boolQuery();
		bool.must(QueryBuilders.queryStringQuery(query));
		if (StringUtils.isNotEmpty(status)) {
			bool.must(QueryBuilders.termQuery("status", StringUtils.lowerCase(status)));
		}

		// 查询数据
		SearchRequestBuilder search = elasticService.getNode().prepareSearch().setIndices(INDEX_NAME)
				.setTypes(TYPE_NAME).setQuery(bool).addSort("hostname.keyword", SortOrder.ASC).setFrom(from)
				.setSize(size);

		try {
			SearchResponse resp = search.execute().actionGet();
			long total = resp.getHits().getTotalHits();
			SearchHit[] hits = resp.getHits().getHits();

			// 解析查询结果
			for (SearchHit hit : hits) {
				String json = hit.getSourceAsString();
				if (StringUtils.isNotEmpty(json)) {
					Node node = JSON.parseObject(json, Node.class);
					nodes.add(node);
				}
			}

			return new PaginationList<Node>(total, nodes);
		} catch (IndexNotFoundException e) {
			return new PaginationList<Node>(0, nodes);
		}
	}

	private void refresh() throws IOException {
		Flux<Instance> flux = registry.getInstances().filter(Instance::isRegistered);
		List<Instance> instances = flux.toStream().collect(Collectors.toList());
		instances.parallelStream().forEach(instance -> {
			String nodeId = instance.getId().toString();
			LOG.debug("refresh node: {}, {}", nodeId, instance.getRegistration().getName());
			if (StringUtils.equals(instance.getRegistration().getName(), APPLICATION_NAME)) {
				long timestamp = instance.getStatusTimestamp().getEpochSecond();
				if (timestamps.get(nodeId) == null || timestamps.get(nodeId) < timestamp) {
					LOG.info("node status change, id: {}, status: {}", nodeId, instance.getStatusInfo().getStatus());
					if (StringUtils.equalsIgnoreCase(instance.getStatusInfo().getStatus(), Node.STATUS_UP)) {
						up(instance);
					} else {
						down(instance);
					}
					timestamps.put(nodeId, timestamp);
				}
			}
		});
	}

	private void up(Instance instance) {
		Node node = new Node();

		String id = instance.getId().toString();
		String address = instance.getRegistration().getServiceUrl();
		if (StringUtils.endsWith(address, "/")) {
			address = StringUtils.left(address, StringUtils.length(address) - 1);
		}

		String status = instance.getStatusInfo().getStatus();
		LOG.info("node up, address: {}, id: {}, status: {}", address, id, status);
		Map<String, String> metadata = instance.getRegistration().getMetadata();

		node.setId(id);
		node.setAddress(address);
		node.setStatus(status);
		node.setMetadata(metadata);

		// get hostname
		String hostname = restTemplate.getForObject(address + "/api/v2/system/hostname", String.class);
		node.setHostname(hostname);
		LOG.info("hostname: {}", hostname);

		// get os
		Os os = restTemplate.getForObject(address + "/api/v2/system/os", Os.class);
		node.setOs(os);
		LOG.info("os: {} {} {}", os.getName(), os.getVersion(), os.getArchitecture());

		// get memory
		Memory memory = restTemplate.getForObject(address + "/api/v2/system/memory", Memory.class);
		node.setMemory(memory);

		// get disk
		Disk[] disks = restTemplate.getForObject(address + "/api/v2/system/disk", Disk[].class);
		node.setDisks(Arrays.asList(disks));

		@SuppressWarnings("unchecked")
		Map<String, Object> map = restTemplate.getForObject(address + "/actuator/metrics/system.disk.total", Map.class);
		node.setDiskTotal((getValue(map).longValue()));

		// get network
		Network[] networks = restTemplate.getForObject(address + "/api/v2/system/network", Network[].class);
		node.setNetworks(Arrays.asList(networks));

		// create index
		String json = JSON.toJSONString(node);
		IndexResponse resp = elasticService.getNode().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME).setId(id)
				.setSource(json, XContentType.JSON).execute().actionGet();
		long version = resp.getVersion();
		LOG.info("index node, id: {}, version: {}", id, version);
	}

	private void down(Instance instance) {
		//设置索引状态
		String id = instance.getId().toString();
		String status = instance.getStatusInfo().getStatus();
		LOG.info("node down, id: {}, status: {}", id, status);

		GetResponse resp = elasticService.getNode().prepareGet().setIndex(INDEX_NAME).setType(TYPE_NAME).setId(id)
				.execute().actionGet();

		if (resp.isExists()) {
			Node node = JSON.parseObject(resp.getSourceAsString(), Node.class);
			node.setStatus(status);

			String json = JSON.toJSONString(node);
			IndexResponse resp2 = elasticService.getNode().prepareIndex().setIndex(INDEX_NAME).setType(TYPE_NAME)
					.setId(id).setSource(json, XContentType.JSON).execute().actionGet();
			long version = resp2.getVersion();
			LOG.info("index node, id: {}, version: {}", id, version);
		}
	}

	@SuppressWarnings("unchecked")
	private Double getValue(Map<String, Object> map) {
		Object measurements = map.get("measurements");
		if (measurements instanceof List) {
			Object measurement = ((List<Map<String, Object>>) measurements).get(0);
			if (measurement instanceof Map) {
				Object value = ((Map<String, Object>) measurement).get("value");
				if (value instanceof Double) {
					return (Double) value;
				}
			}
		}
		return 0D;
	}

}
