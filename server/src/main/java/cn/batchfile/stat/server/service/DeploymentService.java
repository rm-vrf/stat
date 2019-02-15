package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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

import cn.batchfile.stat.domain.ControlGroup;
import cn.batchfile.stat.domain.Deploy;
import cn.batchfile.stat.domain.Deployment;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.domain.Placement;
import cn.batchfile.stat.domain.Resources;
import cn.batchfile.stat.domain.Service;
import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.service.ServiceService;

@org.springframework.stereotype.Service
public class DeploymentService extends cn.batchfile.stat.service.DeploymentService {
	protected static final Logger LOG = LoggerFactory.getLogger(DeploymentService.class);
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ServiceService serviceService;

	@Value("${store.directory}")
	public void setStoreDirectory(String storeDirectory) throws IOException {
		super.setStoreDirectory(storeDirectory);
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

	private void refresh() throws IOException {
		//得到服务列表
		List<Service> services = serviceService.getServices();
		List<String> serviceNames = services.stream().map(service -> service.getName()).collect(Collectors.toList());
		
		//得到节点列表
		List<Node> nodes = nodeService.getNodes(Node.STATUS_UP);
		List<String> nodeIds = nodes.stream().map(node -> node.getId()).collect(Collectors.toList());
		
		//按照运行计划修改部署策略
		for (Service service : services) {
			List<Deployment> ds = getDeployments(service.getName());
			boolean change = deploy(ds, service);
			if (change) {
				//更新变动的部署数据
				putDeployments(service.getName(), ds);
				LOG.info("change deployment, service: {}, nodes: {}", service.getName(), ds.size());
			}
		}
		
		//删除多余的部署数据
		List<Deployment> deployments = getDeployments();
		Map<String, List<Deployment>> map = deployments.stream().collect(Collectors.groupingBy(d -> d.getService()));
		for (Entry<String, List<Deployment>> entry : map.entrySet()) {
			String serviceName = entry.getKey();
			List<Deployment> ds = entry.getValue();
			
			if (!serviceNames.contains(serviceName)) {
				//清理已经不存在的服务
				deleteDeployment(serviceName);
				LOG.info("delete deployment, name: {}", serviceName);
			} else {
				//清理下线的节点
				int count = ds.size();
				ds = ds.stream().filter(d -> nodeIds.contains(d.getNode())).collect(Collectors.toList());
				if (ds.size() < count) {
					putDeployments(serviceName, ds);
					LOG.info("remove deployment of down nodes, name: {}", serviceName);
				}
			}
		}
	}

	private boolean deploy(List<Deployment> deployments, Service service) throws IOException {
		boolean change = false;
		String mode = service.getDeploy() == null ? null : service.getDeploy().getMode();
		if (StringUtils.equals(mode, Deploy.MODE_GLOBAL)) {
			change = deployGlobal(deployments, service);
		} else if (StringUtils.equals(mode, Deploy.MODE_REPLICATED)) {
			change = deployReplicated(deployments, service);
		}
		return change;
	}
	
	private boolean deployGlobal(List<Deployment> deployments, Service service) {
		
		Deploy dep = service.getDeploy();
		Resources res = dep == null ? null : dep.getResources();
		Placement placement = dep == null ? null : dep.getPlacement();
		List<String> constraints = placement == null ? null : placement.getConstraints();
		List<String> preferences = placement == null ? null : placement.getPreferences();
		
		int replicas = dep == null ? 0 : dep.getReplicas();
		ControlGroup reservations = res == null ? null : res.getReservations();
		boolean change = false;
		
		//TODO 1. 处理节点限定 OK
		// 2. 资源检查，满足保留资源的要求
		// 3. 依赖项检查，确定当前节点有依赖服务
		
		List<Node> nodes = searchAndSort(constraints, preferences);
		for (Node node : nodes) {
			List<Deployment> existDeployments = findDeployments(deployments, node.getId());
			int exists = existDeployments.size();
			
			if (exists > replicas) {
				//实际数量大，需要撤销部署
				for (int i = 0; i < exists - replicas; i ++) {
					if (removeDeployment(deployments, node.getId())) {
						change = true;
					}
				}
			} else if (replicas > exists) {
				//实际数量小，需要分配
				for (int i = 0; i < replicas - exists; i ++) {
					Deployment deployment = new Deployment();
					deployment.setNode(node.getId());
					deployment.setReservations(reservations);
					deployment.setService(service.getName());
					deployments.add(deployment);
					change = true;
				}
			}
		}
		
		return change;
	}
	
	private boolean deployReplicated(List<Deployment> deployments, Service service) throws IOException {
		int replicas = service.getDeploy() == null ? 0 : service.getDeploy().getReplicas();
		int exists = deployments.size();
		boolean change = false;
		
		if (exists > replicas) {
			//实际数量大，需要撤销部署
			for (int i = 0; i < exists - replicas; i ++) {
				Deployment remove = deployments.remove(0);
				LOG.info("remove deployment, service: {}, node: {}", remove.getService(), remove.getNode());
				change = true;
			}
		} else if (replicas > exists) {
			//实际数量小，需要分配
			List<Deployment> ds = distribute(service, replicas - exists);
			if (ds != null && ds.size() > 0) {
				deployments.addAll(ds);
				change = true;
			}
		}
		
		return change;
	}
	
	private List<Deployment> distribute(Service service, int count) {
		//TODO 1. 处理节点限定	OK
		// 2. 资源检查，满足保留资源的要求
		// 3. 依赖项检查，确定当前节点有依赖服务
		
		List<Deployment> deployments = new ArrayList<>();
		
		Deploy dep = service.getDeploy();
		Placement placement = dep == null ? null : dep.getPlacement();
		List<String> constraints = placement == null ? null : placement.getConstraints();
		List<String> preferences = placement == null ? null : placement.getPreferences();
		
		Resources res = dep == null ? null : dep.getResources();
		ControlGroup reservations = res == null ? null : res.getReservations();
		
		List<Node> nodes = searchAndSort(constraints, preferences);
		
		if (nodes.size() > 0) {
			for (int i = 0; i < count; i ++) {
				int index = i % nodes.size();
				Node node = nodes.get(index);
				
				Deployment d = new Deployment();
				d.setNode(node.getId());
				d.setService(service.getName());
				d.setReservations(reservations);
				deployments.add(d);
			}
		}
		
		return deployments;
	}
	
	private List<Node> searchAndSort(List<String> constraints, List<String> preferences) {
		List<Node> constNodes = searchNodes(constraints);
		List<Node> preferNodes = searchNodes(preferences);
		Map<Object, Object> preferIds = preferNodes.stream().collect(Collectors.toMap(n -> n.getId(), n -> n));
		Collections.sort(constNodes, new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				if (preferIds.containsKey(o1.getId()) && !preferIds.containsKey(o2.getId())) {
					return -1;
				} else if (!preferIds.containsKey(o1.getId()) && preferIds.containsKey(o2.getId())) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		return constNodes;
	}
	
	private List<Node> searchNodes(List<String> queries) {
		if (queries == null) {
			queries = new ArrayList<>();
		}
		
		if (queries.size() == 0) {
			queries.add("*");
		}
		
		List<Node> nodes = new ArrayList<>();
		for (String query : queries) {
			List<Node> list = searchNodes(query);
			nodes = mergeNodes(nodes, list);
		}
		return nodes;
	}
	
	private List<Node> mergeNodes(List<Node> list1, List<Node> list2) {
		Map<String, Node> map = new HashMap<>();
		for (Node e : list1) {
			map.put(e.getId(), e);
		}
		
		for (Node e : list2) {
			map.put(e.getId(), e);
		}
		return map.values().stream().collect(Collectors.toList());
	}

	private List<Node> searchNodes(String query) {
		List<Node> nodes = new ArrayList<>();
		long total = 0;
		int from = 0, size = 100;
		while (from <= total) {
			PaginationList<Node> list = nodeService.searchNodes(query, Node.STATUS_UP, from, size);
			total = list.getTotal();
			if (list.getRows() != null) {
				nodes.addAll(list.getRows());
			}
			from += size;
		}
		return nodes;
	}
	
	private boolean removeDeployment(List<Deployment> deployments, String node) {
		boolean remove = false;
		Iterator<Deployment> iter = deployments.iterator();
		while (iter.hasNext()) {
			Deployment deployment = iter.next();
			if (StringUtils.equals(deployment.getNode(), node)) {
				iter.remove();
				remove = true;
			}
		}
		return remove;
	}
	
	private List<Deployment> findDeployments(List<Deployment> deployments, String node) {
		List<Deployment> list = new ArrayList<>();
		for (Deployment deployment : deployments) {
			if (StringUtils.equals(deployment.getNode(), node)) {
				list.add(deployment);
			}
		}
		return list;
	}

}
