package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.domain.Service;
import cn.batchfile.stat.service.ServiceService;

@RestController
public class ServiceController {
	
	protected static final Logger log = LoggerFactory.getLogger(ServiceController.class);
	
	@Autowired
	private ServiceService serviceService;
	
	@GetMapping(value="/api/v2/service")
	public ResponseEntity<List<Service>> getServices(WebRequest request, 
			@RequestParam(value = "query", required = false, defaultValue = StringUtils.EMPTY) String query) throws IOException {
		
		long lastModified = serviceService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<Service>>(HttpStatus.NOT_MODIFIED);
		}
		
		List<Service> ss = serviceService.getServices();
		if (StringUtils.isNotEmpty(query)) {
			Iterator<Service> iter = ss.iterator();
			while (iter.hasNext()) {
				Service s = iter.next();
				if (!StringUtils.containsIgnoreCase(s.getName(), query)) {
					iter.remove();
				}
			}
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		//headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Service>>(ss, headers, HttpStatus.OK);
	}
	
	@GetMapping("/api/v2/service/{name}")
	public ResponseEntity<Service> getService(WebRequest request, @PathVariable("name") String name) throws IOException {
		
		long lastModified = serviceService.getLastModified(name);
		if (lastModified < 0) {
			return new ResponseEntity<Service>(HttpStatus.NOT_FOUND);
		}
		
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<Service>(HttpStatus.NOT_MODIFIED);
		}
		
		Service s = serviceService.getService(name);
		if (s == null) {
			return new ResponseEntity<Service>(HttpStatus.NOT_FOUND);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		//headers.setCacheControl("no-cache");
		return new ResponseEntity<Service>(s, headers, HttpStatus.OK);
	}
	
	@PostMapping("/api/v2/service")
	public ResponseEntity<Boolean> postService(WebRequest request,
			@RequestBody Service service) {
		
		try {
			serviceService.postService(service);
			return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PutMapping("/api/v2/service/{name}")
	public ResponseEntity<Boolean> putService(WebRequest request,
			@PathVariable("name") String name,
			@RequestBody Service service) {
		
		try {
			service.setName(name);
			serviceService.putService(service);
			return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DeleteMapping("/api/v2/service/{name}")
	public ResponseEntity<Boolean> deleteService(WebRequest request,
			@PathVariable("name") String name) {
		
		try {
			serviceService.deleteService(name);
			return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PutMapping("/api/v2/service/{name}/deploy/replicas")
	public ResponseEntity<Boolean> setReplicas(WebRequest request,
			@PathVariable("Boolean") String name, 
			@RequestBody Integer replicas) {
		
		try {
			serviceService.setReplicas(name, replicas);
			return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
