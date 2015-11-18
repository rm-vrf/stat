package cn.batchfile.stat.server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.server.service.NodeService;

@Controller
public class NodeController {

	@Autowired
	private NodeService nodeService;
	
	@RequestMapping(value="/a/node", method=RequestMethod.GET)
	@ResponseBody
	public List<Node> getNodes() {
		return nodeService.getNodes();
	}
	
	@RequestMapping(value="/a/node/{agent_id}", method=RequestMethod.GET)
	@ResponseBody
	public Node getNodes(@PathVariable("agent_id") String agentId) {
		return nodeService.getNode(agentId);
	}
}
