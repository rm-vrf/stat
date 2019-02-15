package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.domain.Instance;
import cn.batchfile.stat.server.service.InstanceService;

@RestController
public class InstanceController {

	@Autowired
	private InstanceService instanceService;
	
	@GetMapping("/api/v2/node/{id}/instance")
	public ResponseEntity<List<Instance>> getInstancesOfNode(WebRequest request,
			@PathVariable("id") String id) throws IOException {
		
		List<Instance> ins = instanceService.getInstancesOfNode(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Instance>>(ins, headers, HttpStatus.OK);
	}
	
	@GetMapping("/api/v2/service/{name}/instance")
	public ResponseEntity<List<Instance>> getInstancesOfService(WebRequest request,
			@PathVariable("name") String name) throws IOException {
		
		List<Instance> ins = instanceService.getInstancesOfService(name);
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Instance>>(ins, headers, HttpStatus.OK);
	}
	
	@GetMapping("/api/v2/node/{id}/instance/{pid}/stdout")
	public ResponseEntity<String[]> getSystemOut(WebRequest request,
			@PathVariable("id") String node, 
			@PathVariable("pid") long pid) {

		String[] ary = instanceService.getSystemOut(node, pid);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<String[]>(ary, headers, HttpStatus.OK);
	}

	@GetMapping("/api/v2/node/{id}/instance/{pid}/stderr")
	public ResponseEntity<String[]> getSystemErr(WebRequest request,
			@PathVariable("id") String node, 
			@PathVariable("pid") long pid) {
		
		String[] ary = instanceService.getSystemErr(node, pid);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<String[]>(ary, headers, HttpStatus.OK);
	}

	@PostMapping("/api/v2/node/{id}/instance/{pid}/_kill")
	public ResponseEntity<Boolean> killInstance(WebRequest request,
			@PathVariable("id") String id,
			@PathVariable("pid") long pid) {

		HttpHeaders headers = new HttpHeaders();
		try {
			instanceService.killInstance(id, pid);
			return new ResponseEntity<Boolean>(Boolean.TRUE, headers, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Boolean>(Boolean.FALSE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
