package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

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
	
	@Autowired
	private NodeRepository nodeRepository;
	
    public int getNodeCount() {
    	return (int)nodeRepository.count();
    }
    
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

    public List<Info> getInfos() {
    	LOG.debug("get infos");
    	Iterable<NodeTable> nts = nodeRepository.findMany();
    	List<Info> infos = new ArrayList<Info>();
    	nts.forEach(nt -> {
    		infos.add(composeInfo(nt));
    	});
    	LOG.debug("count: {}", infos.size());
    	return infos;
    }
    
    public Node getNode(String id) {
    	LOG.debug("get node: {}", id);
    	Optional<NodeTable> nt = nodeRepository.findById(id);
    	if (nt.isPresent()) {
    		LOG.debug("get one node");
    		return composeNode(nt.get());
    	} else {
    		LOG.debug("no node: {}", id);
    		return null;
    	}
    }
    
    public Info getInfo(String dockerHost) {
    	LOG.debug("get info: {}", dockerHost);
    	Optional<NodeTable> op = nodeRepository.findOne(dockerHost);
    	if (!op.isPresent()) {
    		return null;
    	} else {
    		return composeInfo(op.get());
    	}
    }
    
    public Info createInfo(Info info) {
    	LOG.info("create info {}", info.getDockerHost());
    	Optional<NodeTable> op = nodeRepository.findOne(info.getDockerHost());
    	if (op.isPresent()) {
    		throw new DuplicateEntryException("docker node already exist");
    	}
    	
    	LOG.info("connect to docker host: {}", info.getDockerHost());
    	DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
    			.withDockerHost("tcp://" + info.getDockerHost()).build();
    	DockerClient docker = DockerClientBuilder.getInstance(config).build();
    	Version dockerVersion = docker.versionCmd().exec();
    	LOG.info("get docker version, kernel: {}, api: {}", dockerVersion.getKernelVersion(), dockerVersion.getApiVersion());

    	long begin = System.currentTimeMillis();
    	com.github.dockerjava.api.model.Info dockerInfo = docker.infoCmd().exec();
    	long end = System.currentTimeMillis();
    	LOG.info("get docker info, name: {}, id: {}", dockerInfo.getName(), dockerInfo.getId());

    	if (info.getLabels() == null) {
    		info.setLabels(new ArrayList<String>());
    	}
    	LOG.info("input labels: {}", info.getLabels());
    	
    	Node node = composeNode(dockerInfo, dockerVersion, info);
    	node.setStatus(Node.STATUS_ONLINE);
    	node.setSlow(end - begin > 1000);
    	LOG.info("slow node: {}", node.getSlow());
    	
    	NodeTable nt = new NodeTable();
    	compose(nt, node);
    	nodeRepository.save(nt);
    	LOG.info("saved node");
    	
    	return info;
    }

    public Info updateInfo(Info info) {
    	LOG.info("update info, {}", info.getDockerHost());
    	Optional<NodeTable> op = nodeRepository.findOne(info.getDockerHost());
    	if (!op.isPresent()) {
    		throw new NotFoundException("node not exist");
    	}
    	
    	NodeTable nt = op.get();
    	compose(nt, info);
    	nodeRepository.save(nt);
    	LOG.info("saved");
        return info;
    }
    
    public Info deleteInfo(String dockerHost) {
    	LOG.info("delete node, {}", dockerHost);
    	Optional<NodeTable> op = nodeRepository.findOne(dockerHost);
    	if (!op.isPresent()) {
    		return null;
    	}
    	
    	Info info = composeInfo(op.get());
    	nodeRepository.deleteById(op.get().getId());
    	LOG.info("deleted, id: {}", op.get().getId());
    	return info;
    }
    
    private Node composeNode(com.github.dockerjava.api.model.Info dockerInfo, Version dockerVersion, Info info) {
    	Node node = new Node();
    	node.setId(StringUtils.remove(dockerInfo.getId(), ':').toLowerCase());
    	node.setName(dockerInfo.getName());
    	node.setImages(dockerInfo.getImages());
    	
    	node.setInfo(info);
    	info.setArchitecture(dockerInfo.getArchitecture());
    	info.setOs(dockerInfo.getOsType());
    	ResourcesControl rc = new ResourcesControl();
    	rc.setCpus((float)dockerInfo.getNCPU());
    	rc.setMemory(FileUtils.byteCountToDisplaySize(dockerInfo.getMemTotal()));
    	info.setResources(rc);
    	
    	Containers containers = new Containers();
    	containers.setTotal(dockerInfo.getContainers());
    	containers.setRunning(dockerInfo.getContainersRunning());
    	containers.setPaused(dockerInfo.getContainersPaused());
    	containers.setStopped(dockerInfo.getContainersStopped());
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

    	if (dockerVersion != null) {
    		node.setApiVersion(dockerVersion.getApiVersion());
    		node.setEngineVersion(dockerVersion.getVersion());
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
    
}
