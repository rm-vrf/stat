package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.TopContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerMount;
import com.github.dockerjava.api.model.ContainerPort;

import cn.batchfile.stat.server.dao.ContainerRepository;
import cn.batchfile.stat.server.domain.container.ContainerInstance;
import cn.batchfile.stat.server.domain.container.MountInstance;
import cn.batchfile.stat.server.domain.container.PortInstance;
import cn.batchfile.stat.server.domain.node.Node;
import cn.batchfile.stat.server.domain.service.Resources;
import cn.batchfile.stat.server.domain.service.ResourcesControl;
import cn.batchfile.stat.server.dto.ContainerTable;

@org.springframework.stereotype.Service
public class ContainerService {
	private static final Logger LOG = LoggerFactory.getLogger(ContainerService.class);
	
	@Autowired
	private ContainerRepository containerRepository;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private DockerService dockerService;
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
	public Page<ContainerInstance> getContainersByService(String namespace, 
			String serviceName, 
			Pageable pageable) {
		
		LOG.debug("get containers of servie: {}/{}", namespace, serviceName);
		List<ContainerInstance> containers = new ArrayList<ContainerInstance>();
		Page<ContainerTable> page = containerRepository.findMany(namespace, serviceName, pageable);
		page.forEach(i -> {
			containers.add(compose(i));
		});
		LOG.debug("containers count: {}", containers.size());
		return new PageImpl<>(containers, pageable, page.getTotalElements());
	}
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
	public Page<ContainerInstance> getContainersByNode(String nodeId, Pageable pageable) {
		LOG.debug("get containers on node: {}", nodeId);
		List<ContainerInstance> containers = new ArrayList<ContainerInstance>();
		Page<ContainerTable> page = containerRepository.findMany(nodeId, pageable);
		page.forEach(i -> {
			containers.add(compose(i));
		});
		LOG.debug("containers count: {}", containers.size());
		return new PageImpl<>(containers, pageable, page.getTotalElements());
	}
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
	public ContainerInstance getContainer(String id) {
		LOG.debug("get container, id: {}", id);
		Optional<ContainerTable> op = containerRepository.findById(id);
		if (op.isPresent()) {
			return compose(op.get());
		} else {
			return null;
		}
	}
	
	public InspectContainerResponse getContainerInspect(String containerId) {
		ContainerInstance container = getContainer(containerId);
		Node node = nodeService.getNode(container.getNode());
		return dockerService.inspectContainer(node.getInfo().getDockerHost(), node.getApiVersion(), containerId);
	}
	
	public TopContainerResponse getContainerTop(String containerId) {
		ContainerInstance container = getContainer(containerId);
		Node node = nodeService.getNode(container.getNode());
		return dockerService.topContainer(node.getInfo().getDockerHost(), node.getApiVersion(), containerId);
	}
	
	public void startContainer(String containerId) {
		ContainerInstance container = getContainer(containerId);
		Node node = nodeService.getNode(container.getNode());
		dockerService.startContainer(node.getInfo().getDockerHost(), node.getApiVersion(), containerId);
	}
	
	public void stopContainer(String containerId) {
		ContainerInstance container = getContainer(containerId);
		Node node = nodeService.getNode(container.getNode());
		dockerService.stopContainer(node.getInfo().getDockerHost(), node.getApiVersion(), containerId);
	}
	
	public void removeContainer(String containerId) {
		ContainerInstance container = getContainer(containerId);
		Node node = nodeService.getNode(container.getNode());
		dockerService.removeContainer(node.getInfo().getDockerHost(), node.getApiVersion(), containerId);
	}
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public ContainerInstance refreshContainer(ContainerInstance containerInstance, 
			Container remoteContainer, 
			Node node) {
		
		if (remoteContainer == null) {
			//远程没有这个容器，数据库里面的数据也要删除
			containerRepository.deleteById(containerInstance.getId());
			LOG.info("removed container instance, id: {}, node: {}", 
					containerInstance.getId(), node.getInfo().getDockerHost());
			return null;
		}
		
		if (containerInstance == null) {
			//数据库里面没有这个容器，这是新容器，需要添加
			containerInstance = new ContainerInstance();
			LOG.info("add container instance, id: {}, node: {}", 
					remoteContainer.getId(), node.getInfo().getDockerHost());
		}
		
		//容器的老状态
		String oldState = containerInstance.getState();
		
		//从接口数据中同步容器属性
		compose(containerInstance, remoteContainer);

		//同步节点属性
		containerInstance.setNode(node.getId());
		containerInstance.setIp(node.getInfo().getPublicIp());

		//判断容器的启动和停止时间
		refreshTime(containerInstance, oldState);

		//更新数据库
		ContainerTable ct = compose(containerInstance);
		containerRepository.save(ct);
		return containerInstance;
	}
	
	private void refreshTime(ContainerInstance container, String oldState) {
		//如果从其他状态转入running状态，设置启动时间
		String newState = container.getState();
		if (!StringUtils.equals(oldState, "running") && StringUtils.equals(newState, "running")) {
			container.setStartTime(new Date());
		}

		//如果从running状态转入其他状态，设置停止时间
		if (StringUtils.equals(oldState, "running") && !StringUtils.equals(newState, "running")) {
			container.setStopTime(new Date());
		}
	}
	
	private ContainerInstance compose(ContainerInstance ci, Container container) {
		ci.setId(container.getId());
		
		Resources res = new Resources();
		ResourcesControl limits = new ResourcesControl();
		ResourcesControl requests = new ResourcesControl();
		res.setLimits(limits);
		res.setRequests(requests);
		ci.setResources(res);
		
		Map<String, String> labels = container.getLabels();
		if (StringUtils.equals("__stat", labels.get("__creator"))) {
			ci.setNamespace(labels.get("__namespace"));
			ci.setService(labels.get("__service_name"));
			ci.setDomainNames(JSON.parseArray(labels.get("__domain_names"), String.class));
			limits.setCpus(Float.valueOf(labels.get("__limits_cpus")));
			limits.setMemory(labels.get("__limits_memory"));
			requests.setCpus(Float.valueOf(labels.get("__requests_cpus")));
			requests.setMemory(labels.get("__requests_memory"));
		} else {
			limits.setCpus(0F);
			limits.setMemory("0");
			requests.setCpus(0F);
			requests.setMemory("0");
		}
		
		ci.setName(container.getNames()[0]);
		ci.setImage(container.getImage());
		ci.setCommand(container.getCommand());
		
		List<PortInstance> ports = new ArrayList<PortInstance>();
		for (ContainerPort p : container.getPorts()) {
			PortInstance pi = new PortInstance();
			pi.setIp(p.getIp());
			pi.setPrivatePort(p.getPrivatePort());
			pi.setPublicPort(p.getPublicPort());
			pi.setType(p.getType());
			ports.add(pi);
		}
		ci.setPorts(ports);
		
		List<MountInstance> mounts = new ArrayList<MountInstance>();
		for (ContainerMount m : container.getMounts()) {
			MountInstance mi = new MountInstance();
			mi.setDestination(m.getDestination());
			mi.setMode(m.getMode());
			mi.setReadOnly(!m.getRw());
			mi.setSource(m.getSource());
			mi.setType(m.getDriver());
			mounts.add(mi);
		}
		ci.setMounts(mounts);
		
		ci.setState(container.getState());
		ci.setDescription(container.getStatus());
		ci.setCreateTime(new Date(container.getCreated() * 1000L));
		
		return ci;
	}
	
	private ContainerTable compose(ContainerInstance containerInstance) {
		ContainerTable containerTable = new ContainerTable();
		containerTable.setId(containerInstance.getId());
		containerTable.setNode(containerInstance.getNode());
		containerTable.setNamespace(containerInstance.getNamespace());
		containerTable.setService(containerInstance.getService());
		containerTable.setDomainNames(JSON.toJSONString(containerInstance.getDomainNames()));
		containerTable.setIp(containerInstance.getIp());
		containerTable.setName(containerInstance.getName());
		containerTable.setImage(containerInstance.getImage());
		containerTable.setCommand(containerInstance.getCommand());
		containerTable.setPorts(JSON.toJSONString(containerInstance.getPorts()));
		containerTable.setMounts(JSON.toJSONString(containerInstance.getMounts()));
		
		if (containerInstance.getResources() != null) {
			if (containerInstance.getResources().getLimits() != null) {
				containerTable.setLimitCpus(containerInstance.getResources().getLimits().getCpus());
				containerTable.setLimitMemory(containerInstance.getResources().getLimits().getMemory());
			}
			if (containerInstance.getResources().getRequests() != null) {
				containerTable.setRequestCpus(containerInstance.getResources().getRequests().getCpus());
				containerTable.setRequestMemory(containerInstance.getResources().getRequests().getMemory());
			}
		}
		
		containerTable.setState(containerInstance.getState());
		containerTable.setDescription(containerInstance.getDescription());
		containerTable.setCreateTime(containerInstance.getCreateTime());
		containerTable.setStartTime(containerInstance.getStartTime());
		containerTable.setStopTime(containerInstance.getStopTime());
		return containerTable;
	}

	private ContainerInstance compose(ContainerTable containerTable) {
		ContainerInstance container = new ContainerInstance();
		container.setId(containerTable.getId());
		container.setNode(containerTable.getNode());
		container.setNamespace(containerTable.getNamespace());
		container.setService(containerTable.getService());
		container.setDomainNames(JSON.parseArray(containerTable.getDomainNames(), String.class));
		container.setIp(containerTable.getIp());
		container.setName(containerTable.getName());
		container.setImage(containerTable.getImage());
		container.setCommand(containerTable.getCommand());
		container.setPorts(JSON.parseArray(containerTable.getPorts(), PortInstance.class));
		container.setMounts(JSON.parseArray(containerTable.getMounts(), MountInstance.class));
		
		Resources res = new Resources();
		ResourcesControl limits = new ResourcesControl();
		limits.setCpus(containerTable.getLimitCpus());
		limits.setMemory(containerTable.getLimitMemory());
		res.setLimits(limits);
		
		ResourcesControl requests = new ResourcesControl();
		requests.setCpus(containerTable.getRequestCpus());
		requests.setMemory(containerTable.getRequestMemory());
		res.setRequests(requests);
		
		container.setResources(res);
		container.setState(containerTable.getState());
		container.setDescription(containerTable.getDescription());
		container.setCreateTime(containerTable.getCreateTime());
		container.setStartTime(containerTable.getStartTime());
		container.setStopTime(containerTable.getStopTime());
		return container;
	}
}
