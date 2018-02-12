package cn.batchfile.stat.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.domain.Query;

@RestController
public class NodeController {

	@GetMapping("/v1/node/{id}")
	public Node getNode(String id) {
		return new Node();
	}
	
	@GetMapping("/v1/node/{id}/tag")
	public List<String> getTags(String id) {
		return new ArrayList<String>();
	}

	@PostMapping("/v1/node/_search")
	public PaginationList<Node> searchNodes(Query query) {
		return new PaginationList<Node>();
	}

}
