package cn.batchfile.stat.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.node.Node;
import cn.batchfile.stat.server.service.NodeService;

@RestController
public class NodeController {
	
	private static final Logger LOG = LoggerFactory.getLogger(NodeController.class);
	
	@Autowired
	private NodeService nodeService;
	
	@PostMapping("/api/node/_count")
	public ResponseEntity<Integer> getNodeCount() {
		Integer count = nodeService.getNodeCount();
		return new ResponseEntity<>(count, HttpStatus.OK);
	}
	
	@GetMapping("/api/node")
	public ResponseEntity<List<Node>> getNodes() {
		List<Node> nodes = nodeService.getNodes();
		return new ResponseEntity<List<Node>>(nodes, HttpStatus.OK);
	}

	@GetMapping("/api/node/{id}")
	public ResponseEntity<Node> getNode(@PathVariable("id") String id) {
		Node node = nodeService.getNode(id);
		if (node == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(node, HttpStatus.OK);
		}
	}
	
	@PutMapping("/api/node/{id}")
	public ResponseEntity<Node> putNode(@PathVariable("id") String id, @RequestBody Node node) {
		LOG.info("put node: {}", id);
		node.setId(id);
		Node n = nodeService.updateNode(node);
		if (n == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(n, HttpStatus.OK);
		}
	}
	
	@DeleteMapping("/api/node/{id}")
	public ResponseEntity<Node> deleteNode(@PathVariable("id") String id) {
		LOG.info("delete node: {}", id);
		Node n = nodeService.deleteNode(id);
		if (n == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(n, HttpStatus.OK);
		}
	}
	
	@PostMapping("/api/node/_import")
	public ResponseEntity<Node> importNode(@RequestParam("dockerHost") String dockerHost, 
			@RequestParam("publicIp") String publicIp) {
		
		LOG.info("import node, {}, {}", dockerHost, publicIp);
		Node node = nodeService.importNode(dockerHost, publicIp);
		return new ResponseEntity<Node>(node, HttpStatus.OK);
	}
	
}
