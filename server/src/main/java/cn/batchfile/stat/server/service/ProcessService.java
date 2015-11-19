package cn.batchfile.stat.server.service;

import java.util.List;

import cn.batchfile.stat.server.domain.Process;
import cn.batchfile.stat.server.domain.ProcessInstance;

public interface ProcessService {

	List<Process> getProcessByAgentId(String agentId);
	
	List<ProcessInstance> syncInstance(List<cn.batchfile.stat.agent.domain.Process> ps, Process process);
	
	void updateRunningInstance(Process process);
}
