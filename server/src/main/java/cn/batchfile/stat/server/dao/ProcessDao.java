package cn.batchfile.stat.server.dao;

import java.util.List;

import cn.batchfile.stat.server.domain.Process;
import cn.batchfile.stat.server.domain.ProcessData;
import cn.batchfile.stat.server.domain.ProcessInstance;

public interface ProcessDao {

	List<Process> getProcessByAgentId(String agentId);
	
	void updateRunningInstance(Process process);

	void updateProcessInstanceStatus(ProcessInstance processInstance);

	List<ProcessInstance> getRunningProcessInstanceByAgentId(String agentId);

	void insertProcessInstance(ProcessInstance processInstance);

	void insertProcessData(ProcessData processData);
	
	ProcessInstance getRunningProcessInstanceByAgentIdAndPid(String agentId, long pid);
}
