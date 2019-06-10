package cn.batchfile.stat.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.HealthCheckResult;
import cn.batchfile.stat.server.service.HealthCheckService;

@RestController
public class HealthCheckController {
	
	@Autowired
	private HealthCheckService healthCheckService;

	@GetMapping("/v1/node/{id}/proc/{pid}/_health")
	public HealthCheckResult[] getHealthCheck(@PathVariable("id") String node, 
			@PathVariable("pid") long pid) {
		
		return healthCheckService.getResults(node, pid);
	}
}
