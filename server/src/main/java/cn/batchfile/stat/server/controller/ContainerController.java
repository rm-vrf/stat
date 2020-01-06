package cn.batchfile.stat.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.dockerjava.api.command.InspectContainerResponse;

import cn.batchfile.stat.server.domain.container.ContainerInstance;
import cn.batchfile.stat.server.service.ContainerService;

@RestController
public class ContainerController {
	private static final Logger LOG = LoggerFactory.getLogger(ContainerController.class);
	
	@Autowired
	private ContainerService containerService;
	
	@GetMapping("/api/node/{nodeId}/container")
	public ResponseEntity<List<ContainerInstance>> getContainersByNode(@PathVariable("nodeId") String nodeId) {
		LOG.debug("get containers on node: {}", nodeId);
		List<ContainerInstance> list = containerService.getContainersByNode(nodeId);
		if (list == null) {
			return new ResponseEntity<List<ContainerInstance>>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<List<ContainerInstance>>(list, HttpStatus.OK);
		}
	}
	
	@GetMapping("/api/container/{id}")
	public ResponseEntity<ContainerInstance> getContainer(@PathVariable("id") String id) {
		LOG.debug("get container: {}", id);
		ContainerInstance ci = containerService.getContainer(id);
		if (ci == null) {
			return new ResponseEntity<ContainerInstance>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<ContainerInstance>(ci, HttpStatus.OK);
		}
	}
	
	@GetMapping("/api/container/{id}/inspect")
	public ResponseEntity<InspectContainerResponse> getContainerInSpect(@PathVariable("id") String id) {
		LOG.debug("inspect container: {}", id);
		InspectContainerResponse spec = containerService.getContainerInSpect(id);
		if (spec == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(spec, HttpStatus.OK);
		}
	}
	
	@PostMapping("/api/container/{id}/_start")
	public ResponseEntity<Void> startContainer(@PathVariable("id") String id) {
		LOG.debug("start container: {}", id);
		containerService.startContainer(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@PostMapping("/api/container/{id}/_stop")
	public ResponseEntity<Void> stopContainer(@PathVariable("id") String id) {
		LOG.debug("stop container: {}", id);
		containerService.stopContainer(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/api/container/{id}/_remove")
	public ResponseEntity<Void> removeContainer(@PathVariable("id") String id) {
		LOG.debug("remove container: {}", id);
		containerService.removeContainer(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
