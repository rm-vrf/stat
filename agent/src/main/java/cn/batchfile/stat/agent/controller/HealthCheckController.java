package cn.batchfile.stat.agent.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.agent.service.HealthCheckService;
import cn.batchfile.stat.domain.HealthCheckResult;

@RestController
public class HealthCheckController {

	@Autowired
	private HealthCheckService healthCheckService;
	
	@GetMapping("/api/v2/instance/{pid}/health")
	public ResponseEntity<List<HealthCheckResult>> getHealthCheck(WebRequest request,
			@PathVariable("pid") long pid) {
		
		List<HealthCheckResult> list = healthCheckService.getResults(pid);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<HealthCheckResult>>(list, headers, HttpStatus.OK);
	}

}
