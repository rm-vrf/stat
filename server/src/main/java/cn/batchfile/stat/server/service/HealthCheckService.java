package cn.batchfile.stat.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.domain.HealthCheckResult;
import cn.batchfile.stat.domain.Node;

@Service
public class HealthCheckService {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private RestTemplate restTemplate;

	public HealthCheckResult[] getResults(String node, long pid) {
		Node n = nodeService.getNode(node);
		String url = String.format("%s/v1/proc/%s/_health", n.getAgentAddress(), pid);
		HealthCheckResult[] results = restTemplate.getForObject(url, HealthCheckResult[].class);
		return results;
	}
}
