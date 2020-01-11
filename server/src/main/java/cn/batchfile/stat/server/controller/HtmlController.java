package cn.batchfile.stat.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.batchfile.stat.server.domain.container.ContainerInstance;
import cn.batchfile.stat.server.domain.node.Node;
import cn.batchfile.stat.server.service.ContainerService;
import cn.batchfile.stat.server.service.DockerService;
import cn.batchfile.stat.server.service.NodeService;

@Controller
public class HtmlController {
	
	@Autowired
	private ContainerService containerService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private DockerService dockerService;
	
	@RequestMapping("/")
	public String index() {
		return "ui/index";
	}
	
	@RequestMapping("/dashboard")
	public String dashboard() {
		return "ui/dashboard";
	}
	
	@RequestMapping("/node")
	public String nodeList() {
		return "ui/node-list";
	}
	
	@RequestMapping("/node/{nodeId}")
	public String node(@PathVariable("nodeId") String nodeId, ModelMap model) {
		model.addAttribute("nodeId", nodeId);
		return "ui/node-view";
	}

	@RequestMapping("/node/{nodeId}/container")
	public String nodeContainer(@PathVariable("nodeId") String nodeId, ModelMap model) {
		model.addAttribute("nodeId", nodeId);
		return "ui/node-container";
	}
	
	@RequestMapping("/container/{containerId}")
	public String containerView(@PathVariable("containerId") String containerId, ModelMap model) {
		model.addAttribute("containerId", containerId);
		return "ui/container-view";
	}
	
	@RequestMapping("/container/{containerId}/terminal")
	public String containerTerminal(@PathVariable("containerId") String containerId, ModelMap model) {
		TerminalSocketHandler.nodeService = nodeService;
		TerminalSocketHandler.dockerService = dockerService;
		
		ContainerInstance container = containerService.getContainer(containerId);
		Node node = nodeService.getNode(container.getNode());
		
		model.addAttribute("container", container);
		model.addAttribute("node", node);
		return "ui/container-terminal";
	}
	
	@RequestMapping("/container/{containerId}/log")
	public String containerLog(@PathVariable("containerId") String containerId, ModelMap model) {
		LogSocketHandler.nodeService = nodeService;
		
		ContainerInstance container = containerService.getContainer(containerId);
		Node node = nodeService.getNode(container.getNode());
		
		model.addAttribute("container", container);
		model.addAttribute("node", node);
		return "ui/container-log";
	}
}
