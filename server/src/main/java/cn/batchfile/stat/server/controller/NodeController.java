package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import cn.batchfile.stat.domain.node.Node;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.server.service.NodeService;

@RestController
public class NodeController {

	@Autowired
	private NodeService nodeService = null;

	@PutMapping("/api/v2/node")
	public ResponseEntity<String> putNode(@RequestBody Node node) {
        String id = nodeService.putNode(node);
        return new ResponseEntity<>(id, HttpStatus.OK);
	}

	@GetMapping("/api/v2/node")
	public ResponseEntity<List<Node>> getNode() {
		return new ResponseEntity<>(nodeService.getNodes(), HttpStatus.OK);
	}

	@GetMapping("/api/v2/node/{id}")
	public ResponseEntity<Node> getNode(@PathVariable("id") String id) {
		Node node = nodeService.getNode(id);
		if (node == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
            return new ResponseEntity<>(node, HttpStatus.OK);
        }
	}

	@DeleteMapping("/api/v2/node/{id}")
	public ResponseEntity<String> deleteNode(@PathVariable("id") String id) {
		String s = nodeService.deleteNode(id);
		if (StringUtils.isEmpty(s)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(s, HttpStatus.OK);
		}
	}

	@PutMapping("/api/v2/node/{id}/tag")
	public ResponseEntity<List<String>> putTags(@PathVariable("id") String id,
												@RequestBody List<String> tags) {

		List<String> list = nodeService.putTags(id, tags);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@GetMapping("/api/v2/node/{id}/tag")
	public ResponseEntity<List<String>> getTags(@PathVariable("id") String id) {
		Node node = nodeService.getNode(id);
		if (node == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(node.getTags(), HttpStatus.OK);
		}
	}

	@PostMapping("/v1/node/_search")
	public PaginationList<Node> searchNodes(@RequestBody String query,
			@RequestParam(name="from", defaultValue="0") int from,
			@RequestParam(name="size", defaultValue="20") int size,
			@RequestParam(name="upOnly", defaultValue="true") boolean upOnly) {

		return nodeService.searchNodes(query, upOnly, from, size);
	}

}
