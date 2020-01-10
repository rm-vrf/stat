package cn.batchfile.stat.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import cn.batchfile.stat.server.domain.node.Info;
import cn.batchfile.stat.server.domain.node.Node;
import cn.batchfile.stat.server.service.NodeService;

@RestController
public class NodeController {
	
	private static final Logger LOG = LoggerFactory.getLogger(NodeController.class);
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/api/info")
	public ResponseEntity<List<Info>> getInfos() {
		List<Info> infos = nodeService.getInfos();
		return new ResponseEntity<List<Info>>(infos, HttpStatus.OK);
	}

	@GetMapping("/api/info/{dockerHost}")
	public ResponseEntity<Info> getInfo(@PathVariable("dockerHost") String dockerHost) {
		Info info = nodeService.getInfo(dockerHost);
		if (info == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(info, HttpStatus.OK);
		}
	}
	
	@PutMapping("/api/info/{dockerHost}")
	public ResponseEntity<Info> updateInfo(@PathVariable("dockerHost") String dockerHost, @RequestBody Info info) {
		LOG.info("put info: {}", dockerHost);
		info.setDockerHost(dockerHost);
		Info i = nodeService.updateInfo(info);
		if (i == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(i, HttpStatus.OK);
		}
	}
	
	@DeleteMapping("/api/info/{dockerHost}")
	public ResponseEntity<Info> deleteInfo(@PathVariable("dockerHost") String dockerHost) {
		LOG.info("delete info: {}", dockerHost);
		Info i = nodeService.deleteInfo(dockerHost);
		if (i == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(i, HttpStatus.OK);
		}
	}
	
	@PostMapping("/api/info")
	public ResponseEntity<Info> createInfo(@RequestBody Info info) {
		LOG.info("import info, {}", info.getDockerHost());
		Info i  = nodeService.createInfo(info);
		return new ResponseEntity<Info>(i, HttpStatus.OK);
	}
	
	@GetMapping("/api/node")
	public ResponseEntity<Page<Node>> getNodes(
			@PageableDefault(value = 10, sort = {"dockerHost"}, direction = Sort.Direction.ASC) Pageable pageable) {
		Page<Node> nodes = nodeService.getNodes(pageable);
		return new ResponseEntity<>(nodes, HttpStatus.OK);
	}

	@PostMapping("/api/node/_search")
	public ResponseEntity<Page<Node>> searchNodes(
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "publicIp", required = false) String publicIp,
			@RequestParam(value = "label", required = false) String label,
			@PageableDefault(value = 10, sort = {"dockerHost"}, direction = Sort.Direction.ASC) Pageable pageable) {
		Page<Node> nodes = nodeService.searchNodes(name, publicIp, label, pageable);
		return new ResponseEntity<>(nodes, HttpStatus.OK);
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
}
