package cn.batchfile.stat.server.service;

import java.util.Date;
import java.util.List;

import cn.batchfile.stat.server.domain.Process;
import cn.batchfile.stat.server.domain.ProcessInstance;

public interface ProcessService {

	List<Process> getProcessByAgentId(String agentId);
	
	List<ProcessInstance> getRunningInstance(List<cn.batchfile.stat.agent.domain.Process> ps, Process process, Date time);
	
	void updateRunningInstance(Process process);
}
