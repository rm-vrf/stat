package cn.batchfile.stat.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.domain.RestResponse;
import cn.batchfile.stat.server.service.NodeService;

@RestController
public class NodeController {

	@Autowired
	private NodeService nodeService;
	
	@PostMapping("/v1/node")
	public RestResponse<String> putNode(HttpServletResponse response,
			@RequestBody Node node) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			nodeService.putNode(node);
			resp.setOk(true);
			resp.setBody(node.getId());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	@GetMapping("/v1/node/{id}")
	public Node getNode(HttpServletResponse response,
			@PathVariable("id") String id) {
		
		Node node = nodeService.getNode(id);
		if (node == null) {
			response.setStatus(404);
		}
		return node;
	}
	
	@PostMapping("/v1/node/_search")
	public PaginationList<Node> searchNodes(@RequestBody String query, 
			@RequestParam(name="from", defaultValue="0") int from, 
			@RequestParam(name="size", defaultValue="20") int size,
			@RequestParam(name="includeDownNode", defaultValue="true") boolean includeDownNode) {
		
		return nodeService.searchNodes(query, from, size, includeDownNode);
	}

}
