package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.agent.service.InstanceService;
import cn.batchfile.stat.domain.Instance;

@RestController
public class InstanceController {

	protected static final Logger log = LoggerFactory.getLogger(InstanceController.class);
	
	@Autowired
	private InstanceService instanceService;
	
	@GetMapping("/api/v2/instance")
	public ResponseEntity<List<Instance>> getInstances(WebRequest request) throws IOException {
		
		long lastModified = instanceService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<Instance>>(HttpStatus.NOT_MODIFIED);
		}
		
		List<Instance> ins = instanceService.getInstances();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Instance>>(ins, headers, HttpStatus.OK);
	}
	
	@GetMapping("/api/v2/service/{name}/instance")
	public ResponseEntity<List<Instance>> getInstances(WebRequest request, @PathVariable("name") String name) throws IOException {
		
		long lastModified = instanceService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<Instance>>(HttpStatus.NOT_MODIFIED);
		}
		
		List<Instance> ins = instanceService.getInstacnces(name);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Instance>>(ins, headers, HttpStatus.OK);
		
	}
	
	@GetMapping("/api/v2/instance/{pid}")
	public ResponseEntity<Instance> getInstance(WebRequest request,
			@PathVariable("pid") Long pid) throws IOException {
		
		long lastModified = instanceService.getLastModified(pid);
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<Instance>(HttpStatus.NOT_MODIFIED);
		}
		
		Instance in = instanceService.getInstance(pid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Instance>(in, headers, HttpStatus.OK);
	}
	
	@GetMapping("/api/v2/instance/{pid}/stdout")
	public ResponseEntity<List<String>> getSystemOut(WebRequest request,
			@PathVariable("pid") Long pid) throws IOException {
		
		List<String> list = instanceService.getSystemOut(pid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<String>>(list, headers, HttpStatus.OK);
	}

	@GetMapping("/api/v2/instance/{pid}/stderr")
	public ResponseEntity<List<String>> getSystemErr(WebRequest request,
			@PathVariable("pid") Long pid) throws IOException {
		
		List<String> list = instanceService.getSystemErr(pid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<String>>(list, headers, HttpStatus.OK);
	}
	
	@DeleteMapping("/api/v2/instance/{pid}")
	public ResponseEntity<Boolean> killInstance(WebRequest request,
			@PathVariable("pid") Long pid) throws IOException {
		
		HttpHeaders headers = new HttpHeaders();
		try {
			instanceService.killInstances(Arrays.asList(new Long[] {pid}));
			return new ResponseEntity<Boolean>(Boolean.TRUE, headers, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Boolean>(Boolean.FALSE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/api/v2/instance/{pid}/_kill")
	public ResponseEntity<Boolean> postKillInstance(WebRequest request,
			@PathVariable("pid") Long pid) throws IOException {
		return killInstance(request, pid);
	}
	
//	@DeleteMapping("/api/v2/instance")
//	public ResponseEntity<Boolean> killInstances(WebRequest request,
//			@RequestBody Long[] pids) throws IOException {
//		
//		HttpHeaders headers = new HttpHeaders();
//		try {
//			instanceService.killInstances(Arrays.asList(pids));
//			return new ResponseEntity<Boolean>(Boolean.TRUE, headers, HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<Boolean>(Boolean.FALSE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

}
