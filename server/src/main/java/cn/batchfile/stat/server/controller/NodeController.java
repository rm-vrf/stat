package cn.batchfile.stat.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.server.service.NodeService;

@RestController
public class NodeController {
	
	protected static final Logger log = LoggerFactory.getLogger(NodeController.class);

	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/api/v2/node/{id}")
	public ResponseEntity<Node> getNode(WebRequest request,
			@PathVariable("id") String id) {
		
		Node node = nodeService.getNode(id);
		if (node == null) {
			return new ResponseEntity<Node>(HttpStatus.NOT_FOUND);
		}
		
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<Node>(node, headers, HttpStatus.OK);
	}
	
	@PostMapping("/api/v2/node/_search")
	public ResponseEntity<PaginationList<Node>> searchNodes(@RequestParam(name="query", defaultValue="*") String query,
			@RequestParam(name="status", defaultValue="*") String status,
			@RequestParam(name="from", defaultValue="0") int from, 
			@RequestParam(name="size", defaultValue="20") int size) {
		
		PaginationList<Node> list = nodeService.searchNodes(query, status, from, size);
		
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<PaginationList<Node>>(list, headers, HttpStatus.OK);
	}

	@PutMapping("/api/v2/node/{id}/tag")
	public ResponseEntity<List<String>> putTags(
			@PathVariable("id") String id,
			@RequestBody List<String> tags) {
		
		Node node = nodeService.getNode(id);
		if (node == null) {
			return new ResponseEntity<List<String>>(HttpStatus.NOT_FOUND);
		}
		
		node.setTags(tags);
		nodeService.putNode(node);
		
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<List<String>>(tags, headers, HttpStatus.OK);
	}
	
}
