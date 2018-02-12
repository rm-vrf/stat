package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.NodeService;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.RestResponse;

@RestController
public class NodeController {
	
	@Autowired
	private NodeService nodeService;

	@GetMapping("/v1/node")
	public Node getNode() throws IOException {
		return nodeService.getNode();
	}
	
	@GetMapping("/v1/node/tag")
	public List<String> getTags() throws IOException {
		return nodeService.getNode().getTags();
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
	public RestResponse<String> setEnvs(HttpServletResponse response,
			@RequestBody Map<String, String> envs) throws UnsupportedEncodingException, IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			nodeService.putEnvs(envs);
			resp.setOk(true);
			resp.setBody(getNode().getId());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
}
