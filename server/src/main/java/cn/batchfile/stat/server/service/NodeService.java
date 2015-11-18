package cn.batchfile.stat.server.service;

import java.util.List;

import cn.batchfile.stat.server.domain.Node;

public interface NodeService {

	List<Node> getNodes();
	
	Node getNode(String agentId);
}
