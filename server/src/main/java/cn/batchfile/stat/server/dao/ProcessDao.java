package cn.batchfile.stat.server.dao;

import java.util.List;

import cn.batchfile.stat.server.domain.ProcessData;
import cn.batchfile.stat.server.domain.ProcessInstance;
import cn.batchfile.stat.server.domain.ProcessMonitor;

public interface ProcessDao {

	List<ProcessMonitor> getEnabledMonitors();
	
	List<ProcessInstance> getInstancesByAgentPidStatus(String agentId, long pid, String status);

	List<ProcessInstance> getInstancesByAgentNameStatus(String agentId, String name, String status);

	List<ProcessInstance> getInstancesByAgentMonitorStatus(String agentId, String monitor, String status);
	
	void insertInstance(ProcessInstance instance);

	void insertData(ProcessData data);

	void updateInstanceStatus(String instanceId, String status);

	void updateMonitorInstanceCount(String name, int instanceCount);

}
