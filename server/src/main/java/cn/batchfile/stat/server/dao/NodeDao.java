package cn.batchfile.stat.server.dao;

import java.util.List;

import cn.batchfile.stat.server.domain.Node;

public interface NodeDao {

	List<Node> getNodes();
	
	Node getNode(String agentId);
}
