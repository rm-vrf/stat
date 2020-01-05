package cn.batchfile.stat.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.batchfile.stat.domain.container.Container;

@org.springframework.stereotype.Service
public class ContainerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ContainerService.class);
	
	public List<Container> getContainersOfNode(String nodeId) {
		return null;
	}

}
