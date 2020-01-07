package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Version;

import cn.batchfile.stat.server.dao.ContainerRepository;
import cn.batchfile.stat.server.dao.NodeRepository;
import cn.batchfile.stat.server.domain.node.Containers;
import cn.batchfile.stat.server.domain.node.Info;
import cn.batchfile.stat.server.domain.node.Node;
import cn.batchfile.stat.server.domain.service.Resources;
import cn.batchfile.stat.server.domain.service.ResourcesControl;
import cn.batchfile.stat.server.dto.NodeTable;
import cn.batchfile.stat.server.exception.DuplicateEntryException;
import cn.batchfile.stat.server.exception.NotFoundException;

@org.springframework.stereotype.Service
public class NodeService {
	private static final Logger LOG = LoggerFactory.getLogger(NodeService.class);
	private Map<String, Integer> offlineCounts = new ConcurrentHashMap<String, Integer>();
	
	@Autowired
	private NodeRepository nodeRepository;
	
	@Autowired
	private DockerService dockerService;
	
	@Autowired
	private ContainerRepository containerRepository;
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public int getNodeCount() {
    	return (int)nodeRepository.count();
    }
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<Node> getNodes() {
    	LOG.debug("get all nodes");
    	Iterable<NodeTable> nts = nodeRepository.findMany();
    	List<Node> nodes = new ArrayList<Node>();
    	nts.forEach(nt -> {
    		nodes.add(composeNode(nt));
    	});
    	LOG.debug("nodes count: {}", nodes.size());
    	return nodes;
    }

	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<Info> getInfos() {
    	LOG.debug("get all infos");
    	Iterable<NodeTable> nts = nodeRepository.findMany();
    	List<Info> infos = new ArrayList<Info>();
    	nts.forEach(nt -> {
    		infos.add(composeInfo(nt));
    	});
    	LOG.debug("info count: {}", infos.size());
    	return infos;
    }
    
	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public Node getNode(String id) {
    	LOG.debug("get node: {}", id);
    	Optional<NodeTable> nt = nodeRepository.findOne(id);
    	if (nt.isPresent()) {
    		LOG.debug("get one node");
    		return composeNode(nt.get());
    	} else {
    		LOG.debug("no node: {}", id);
    		return null;
    	}
    }
    
	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public Info getInfo(String dockerHost) {
    	LOG.debug("get info: {}", dockerHost);
    	Optional<NodeTable> op = nodeRepository.findById(dockerHost);
    	if (op.isPresent()) {
    		LOG.debug("get one info");
    		return composeInfo(op.get());
    	} else {
    		LOG.debug("no info: {}", dockerHost);
    		return null;
    	}
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Info createInfo(Info info) {
    	LOG.info("create info {}", info.getDockerHost());
    	Optional<NodeTable> op = nodeRepository.findById(info.getDockerHost());
    	if (op.isPresent()) {
    		throw new DuplicateEntryException("docker node already exist");
    	}
    	
    	if (info.getLabels() == null) {
    		info.setLabels(new ArrayList<String>());
    	}
    	LOG.info("input labels: {}", info.getLabels());
    	
    	NodeTable nt = new NodeTable();
    	compose(nt, info);
    	nt.setStatus(Node.STATUS_NEW);
    	nodeRepository.save(nt);
    	
    	LOG.info("saved info");
    	return info;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Info updateInfo(Info info) {
    	LOG.info("update info, {}", info.getDockerHost());
    	Optional<NodeTable> op = nodeRepository.findById(info.getDockerHost());
    	if (!op.isPresent()) {
    		throw new NotFoundException("node not exist");
    	}
    	
    	NodeTable nt = op.get();
    	compose(nt, info);
    	nodeRepository.save(nt);
    	LOG.info("saved");
        return info;
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Info deleteInfo(String dockerHost) {
    	LOG.info("delete node, {}", dockerHost);
    	Optional<NodeTable> op = nodeRepository.findById(dockerHost);
    	if (!op.isPresent()) {
    		return null;
    	}
    	
    	Info info = composeInfo(op.get());
    	nodeRepository.deleteById(dockerHost);
    	LOG.info("deleted node, id: {}", op.get().getId());
    	
    	if (StringUtils.isNotEmpty(op.get().getId())) {
    		containerRepository.deleteMany(op.get().getId());
    		LOG.info("deleted containers");
    	}
    	
    	return info;
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Node refreshNode(Node node) {
    	String dockerHost = node.getInfo().getDockerHost();
    	String apiVersion = node.getApiVersion();
    	LOG.debug("connect to docker host: {}, version: {}", dockerHost, apiVersion);

    	//记录原状态
    	String oldStatus = node.getStatus();
    	
    	try {
        	//按照远程接口刷新数据库
    		refreshNode(node, dockerHost, apiVersion);

    		//成功刷新，设置Online状态
        	node.setStatus(Node.STATUS_ONLINE);
        	offlineCounts.put(node.getId(), 0);
    	} catch (Exception e) {
    		//set state
    		int count = offlineCounts.containsKey(node.getId()) ? offlineCounts.get(node.getId()) + 1 : 1;
    		offlineCounts.put(node.getId(), count);
    		if (offlineCounts.get(node.getId()) > 3) {
        		node.setStatus(Node.STATUS_OFFLINE);
    		} else {
        		node.setStatus(Node.STATUS_UNKNOWN);
    		}
    	}
    	
    	//compose dto
    	NodeTable nt = new NodeTable();
    	compose(nt, node);
    	nodeRepository.save(nt);
    	LOG.debug("saved node");
    	
    	//比较状态，设置容器状态
    	String newStatus = node.getStatus();
    	if ((StringUtils.equals(newStatus, Node.STATUS_OFFLINE) 
    			|| StringUtils.equals(newStatus, Node.STATUS_UNKNOWN)) 
    			&& !StringUtils.equals(oldStatus, newStatus)) {
    		containerRepository.updateStatus(node.getId(), newStatus);
    	}
    	
    	return node;
    }
    
    private void compose(NodeTable nodeTable, Node node) {
    	nodeTable.setId(node.getId());
    	nodeTable.setName(node.getName());
    	
    	compose(nodeTable, node.getInfo());
    	nodeTable.setApiVersion(node.getApiVersion());
    	nodeTable.setEngineVersion(node.getEngineVersion());
    	if (node.getContainers() != null) {
    		nodeTable.setContainersTotal(node.getContainers().getTotal());
    		nodeTable.setContainersRunning(node.getContainers().getRunning());
    		nodeTable.setContainersPaused(node.getContainers().getPaused());
    		nodeTable.setContainersStopped(node.getContainers().getStopped());
    	}
    	nodeTable.setImages(node.getImages());
    	nodeTable.setStatus(node.getStatus());
    	nodeTable.setDescription(node.getDescription());
    	nodeTable.setSlow(node.getSlow());
    	if (node.getResources() != null) {
    		if (node.getResources().getLimits() != null) {
    			nodeTable.setLimitCpus(node.getResources().getLimits().getCpus());
    			nodeTable.setLimitMemory(node.getResources().getLimits().getMemory());
    		}
    		if (node.getResources().getRequests() != null) {
    			nodeTable.setRequestCpus(node.getResources().getRequests().getCpus());
    			nodeTable.setRequestMemory(node.getResources().getRequests().getMemory());
    		}
    	}
    }
    
    private Node composeNode(NodeTable nodeTable) {
    	Node node = new Node();
    	node.setId(nodeTable.getId());
    	node.setName(nodeTable.getName());
    	node.setInfo(composeInfo(nodeTable));
    	node.setApiVersion(nodeTable.getApiVersion());
    	node.setEngineVersion(nodeTable.getEngineVersion());
    	Containers cs = new Containers();
    	cs.setTotal(nodeTable.getContainersTotal());
    	cs.setRunning(nodeTable.getContainersRunning());
    	cs.setPaused(nodeTable.getContainersPaused());
    	cs.setStopped(nodeTable.getContainersStopped());
    	node.setContainers(cs);
    	node.setImages(nodeTable.getImages());
    	node.setStatus(nodeTable.getStatus());
    	node.setDescription(nodeTable.getDescription());
    	node.setSlow(nodeTable.getSlow());
    	Resources res = new Resources();
    	ResourcesControl limits = new ResourcesControl();
    	limits.setCpus(nodeTable.getLimitCpus());
    	limits.setMemory(nodeTable.getLimitMemory());
    	res.setLimits(limits);
    	ResourcesControl requests = new ResourcesControl();
    	requests.setCpus(nodeTable.getRequestCpus());
    	requests.setMemory(nodeTable.getRequestMemory());
    	res.setRequests(requests);
    	node.setResources(res);
    	return node;
    }
    
    private void compose(NodeTable nodeTable, Info info) {
    	nodeTable.setDockerHost(info.getDockerHost());
    	nodeTable.setPublicIp(info.getPublicIp());
    	nodeTable.setLabels(JSON.toJSONString(info.getLabels()));
    	if (info.getOs() != null) {
    		nodeTable.setOs(info.getOs());
    	}
    	if (info.getArchitecture() != null) {
    		nodeTable.setArchitecture(info.getArchitecture());
    	}
    	if (info.getResources() != null) {
    		nodeTable.setTotalCpus(info.getResources().getCpus());
    		nodeTable.setTotalMemory(info.getResources().getMemory());
    	}
    }
    
    private Info composeInfo(NodeTable nodeTable) {
    	Info info = new Info();
    	info.setArchitecture(nodeTable.getArchitecture());
    	info.setDockerHost(nodeTable.getDockerHost());
    	info.setLabels(JSON.parseArray(nodeTable.getLabels(), String.class));
    	info.setOs(nodeTable.getOs());
    	info.setPublicIp(nodeTable.getPublicIp());
    	ResourcesControl rc = new ResourcesControl();
    	rc.setCpus(nodeTable.getTotalCpus());
    	rc.setMemory(nodeTable.getTotalMemory());
    	info.setResources(rc);
    	return info;
    }
    
    private void refreshNode(Node node, String dockerHost, String apiVersion) {
    	DockerClient docker = dockerService.getDockerClient(dockerHost, apiVersion);
    	if (StringUtils.isEmpty(apiVersion)) {
    		Version version = docker.versionCmd().exec();
    		LOG.info("get docker version, kernel: {}, api: {}", 
    				version.getKernelVersion(), version.getApiVersion());
    		
    		// sync values
    		node.setApiVersion(version.getApiVersion());
    		node.setEngineVersion(version.getVersion());
    	}
    	
    	long begin = System.currentTimeMillis();
    	com.github.dockerjava.api.model.Info info = docker.infoCmd().exec();
    	long end = System.currentTimeMillis();
    	node.setSlow(end - begin > 1000);
    	LOG.debug("get docker info, name: {}, slow: {}, id: {}", info.getName(), node.getSlow(), info.getId());
    	
		// sync values
    	node.setId(StringUtils.remove(info.getId(), ':').toLowerCase());
    	node.setName(info.getName());
    	node.setImages(info.getImages());
    	
    	node.getInfo().setArchitecture(info.getArchitecture());
    	node.getInfo().setOs(info.getOsType());
    	
    	ResourcesControl rc = new ResourcesControl();
    	rc.setCpus((float)info.getNCPU());
    	rc.setMemory(FileUtils.byteCountToDisplaySize(info.getMemTotal()));
    	node.getInfo().setResources(rc);
    	
    	Containers containers = new Containers();
    	containers.setTotal(info.getContainers());
    	containers.setRunning(info.getContainersRunning());
    	containers.setPaused(info.getContainersPaused());
    	containers.setStopped(info.getContainersStopped());
    	node.setContainers(containers);
    	
    	Resources resources = new Resources();
    	ResourcesControl limits = new ResourcesControl();
    	limits.setCpus(0F);
    	limits.setMemory("0");
    	resources.setLimits(limits);
    	ResourcesControl requests = new ResourcesControl();
    	requests.setCpus(0F);
    	requests.setMemory("0");
    	resources.setRequests(requests);
    	node.setResources(resources);
    }
}
