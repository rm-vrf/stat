package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.NodeService;
import cn.batchfile.stat.agent.types.Node;

@RestController
public class NodeController {
	
	@Autowired
	private NodeService nodeService;

	@RequestMapping(value="/v1/node", method=RequestMethod.GET)
	public Node getNode() {
		return nodeService.getNode();
	}
	
	@RequestMapping(value="/v1/tag", method=RequestMethod.GET)
	public List<String> getTags() throws IOException {
		return nodeService.getTags();
	}
	
	@RequestMapping(value="/v1/tag", method=RequestMethod.PUT)
	public void setTags(@RequestBody List<String> tags) throws UnsupportedEncodingException, IOException {
		nodeService.putTags(tags);
	}

	@RequestMapping(value="/v1/env", method=RequestMethod.GET)
	public Map<String, String> getEnvs() throws IOException {
		return nodeService.getEnvs();
	}
	
	@RequestMapping(value="/v1/env", method=RequestMethod.PUT)
	public void setEnvs(@RequestBody Map<String, String> envs) throws UnsupportedEncodingException, IOException {
		nodeService.putEnvs(envs);
	}
}
