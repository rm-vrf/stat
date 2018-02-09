package cn.batchfile.stat.agent.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.HealthCheckService;
import cn.batchfile.stat.domain.HealthCheckResult;

@RestController
public class HealthCheckController {

	@Autowired
	private HealthCheckService healthCheckService;
	
	@GetMapping("/v1/proc/{pid}/_health")
	public List<HealthCheckResult> getHealthCheck(@PathVariable("pid") long pid) {
		return healthCheckService.getResults(pid);
	}

	@GetMapping("/v1/node/_health")
	public List<HealthCheckResult> getHealthCheck() {
		return healthCheckService.getResults();
	}
}
