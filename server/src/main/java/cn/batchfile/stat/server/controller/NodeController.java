package cn.batchfile.stat.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.RestResponse;

@RestController
public class NodeController {

	@GetMapping("/v1/node")
	public List<Node> getNodes() {
		return new ArrayList<Node>();
	}
	
	@GetMapping("/v1/node/{id}")
	public Node getNode(String id) {
		return new Node();
	}

	@PostMapping("/v1/node")
	public RestResponse<String> postNode(Node node) {
		return new RestResponse<String>();
	}
	
	@PutMapping("/v1/node/{id}")
	public RestResponse<String> putNode(Node node) {
		return new RestResponse<String>();
	}
	
	@DeleteMapping("/v1/node/{id}")
	public RestResponse<String> deleteNode(String id) {
		return new RestResponse<String>();
	}

	@PostMapping("/v1/node/_search")
	public List<Node> searchNodes(String query) {
		return new ArrayList<Node>();
	}

}
