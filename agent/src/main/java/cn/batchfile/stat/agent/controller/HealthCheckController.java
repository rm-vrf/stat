package cn.batchfile.stat.agent.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.HealthCheckService;
import cn.batchfile.stat.agent.types.HealthCheckResult;

@RestController
public class HealthCheckController {

	@Autowired
	private HealthCheckService healthCheckService;
	
	@RequestMapping(value="/v1/proc/{pid}/_health", method=RequestMethod.GET)
	public List<HealthCheckResult> getHealthCheck(@PathVariable("pid") long pid) {
		return healthCheckService.getResults(pid);
	}

	@RequestMapping(value="/v1/node/_health", method=RequestMethod.GET)
	public List<HealthCheckResult> getHealthCheck() {
		return healthCheckService.getResults();
	}
}
