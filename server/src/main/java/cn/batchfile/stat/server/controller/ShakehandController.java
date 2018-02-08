package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.types.Node;
import cn.batchfile.stat.agent.types.Proc;
import cn.batchfile.stat.agent.types.RestResponse;

@RestController
public class ShakehandController {

	@PutMapping("/v1/shakehand/node")
	public RestResponse<String> putNode(HttpServletResponse response,
			@RequestBody Node node) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			//TODO
			resp.setOk(true);
			resp.setBody(node.getId());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	@PutMapping("/v1/shakehand/tag")
	public RestResponse<String> putTags(HttpServletResponse response, 
			@RequestBody List<String> tags,
			@RequestParam("id") String id) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			//TODO
			resp.setOk(true);
			resp.setBody(id);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	public RestResponse<String> putProcs(HttpServletResponse response, 
			@RequestBody List<Proc> ps,
			@RequestParam("id") String id,
			@RequestParam("hostname") String hostname,
			@RequestParam("agentAddress") String agentAddress) throws IOException {
		
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			//TODO
			resp.setOk(true);
			resp.setBody(id);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
}
