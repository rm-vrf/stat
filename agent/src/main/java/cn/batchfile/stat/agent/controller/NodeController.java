package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.NodeService;
import cn.batchfile.stat.agent.types.Node;

@RestController
public class NodeController {
	
	@Autowired
	private NodeService nodeService;

	@GetMapping("/v1/node")
	public Node getNode() {
		return nodeService.getNode();
	}
	
	@GetMapping("/v1/node/tag")
	public List<String> getTags() throws IOException {
		return nodeService.getTags();
	}
	
	@PutMapping("/v1/node/tag")
	public void setTags(@RequestBody List<String> tags) throws UnsupportedEncodingException, IOException {
		nodeService.putTags(tags);
	}

	@GetMapping("/v1/node/env")
	public Map<String, String> getEnvs() throws IOException {
		return nodeService.getEnvs();
	}
	
	@PutMapping("/v1/node/env")
	public void setEnvs(@RequestBody Map<String, String> envs) throws UnsupportedEncodingException, IOException {
		nodeService.putEnvs(envs);
	}
}
