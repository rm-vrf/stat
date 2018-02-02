//package cn.batchfile.stat.server.service.impl;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import cn.batchfile.stat.server.dao.NodeDao;
//import cn.batchfile.stat.server.domain.Node;
//import cn.batchfile.stat.server.domain.NodeData;
//import cn.batchfile.stat.server.service.NodeService;
//
//@Service
//public class NodeServiceImpl implements NodeService {
//
//	@Autowired
//	private NodeDao nodeDao;
//	
//	@Override
//	public List<Node> getNodes() {
//		return nodeDao.getNodes();
//	}
//
//	@Override
//	public List<Node> getEnableNodes() {
//		return nodeDao.getEnableNodes();
//	}
//	
//	@Override
//	public Node getNode(String agentId) {
//		return nodeDao.getNode(agentId);
//	}
//
//	@Override
//	public void updateNode(Node node) {
//		nodeDao.updateNode(node);
//	}
//	
//	@Override
//	public void insertData(NodeData nodeData) {
//		nodeDao.insertData(nodeData);
//	}
//}
