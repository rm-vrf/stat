package cn.batchfile.stat.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Node;
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
	
//	@GetMapping("/v1/node/{id}")
//	public Node getNode(String id) {
//		return new Node();
//	}
//	
//	@GetMapping("/v1/node/{id}/tag")
//	public List<String> getTags(String id) {
//		return new ArrayList<String>();
//	}
//
//	@PostMapping("/v1/node/_search")
//	public PaginationList<Node> searchNodes(Query query) {
//		return new PaginationList<Node>();
//	}

}
