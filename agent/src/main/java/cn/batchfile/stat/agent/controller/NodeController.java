package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.agent.service.NodeService;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.RestResponse;

@RestController
public class NodeController {
	
	@Autowired
	private NodeService nodeService;

	@GetMapping("/v1/node")
	public ResponseEntity<Node> getNode(WebRequest request) throws IOException {
		long lastModified = nodeService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<Node>(HttpStatus.NOT_MODIFIED);
		}
		
		Node node = nodeService.getNode();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Node>(node, headers, HttpStatus.OK);
	}
	
	@GetMapping("/v1/node/tag")
	public ResponseEntity<List<String>> getTags(WebRequest request) throws IOException {
		long lastModified = nodeService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<String>>(HttpStatus.NOT_MODIFIED);
		}
		
		List<String> tags = nodeService.getNode().getTags();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<String>>(tags, headers, HttpStatus.OK);
	}
	
	@PutMapping("/v1/node/tag")
	public RestResponse<Integer> putTags(HttpServletResponse response, 
			@RequestBody List<String> tags) throws IOException {
		
		RestResponse<Integer> resp = new RestResponse<Integer>();
		try {
			nodeService.putTags(tags);
			resp.setOk(true);
			resp.setBody(tags == null ? 0 : tags.size());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}

	@GetMapping("/v1/node/env")
	public ResponseEntity<Map<String, String>> getEnvs(WebRequest request) throws IOException {
		long lastModified = nodeService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<Map<String, String>>(HttpStatus.NOT_MODIFIED);
		}
		
		Map<String, String> envs = nodeService.getEnvs();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Map<String, String>>(envs, headers, HttpStatus.OK);
	}
	
	@PutMapping("/v1/node/env")
	public RestResponse<String> setEnvs(HttpServletResponse response,
			@RequestBody Map<String, String> envs) throws UnsupportedEncodingException, IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			nodeService.putEnvs(envs);
			resp.setOk(true);
			resp.setBody(nodeService.getNode().getId());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
}
