//package cn.batchfile.stat.server.service.impl;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import cn.batchfile.stat.server.dao.ProcessDao;
//import cn.batchfile.stat.server.domain.ProcessData;
//import cn.batchfile.stat.server.domain.ProcessInstance;
//import cn.batchfile.stat.server.domain.ProcessMonitor;
//import cn.batchfile.stat.server.service.ProcessService;
//
//@Service
//public class ProcessServiceImpl implements ProcessService {
//
//	@Autowired
//	private ProcessDao processDao;
//	
//	@Override
//	public List<ProcessMonitor> getEnabledMonitors() {
//		return processDao.getEnabledMonitors();
//	}
//
//	@Override
//	public List<ProcessInstance> getInstancesByAgentNameStatus(String agentId, String name, String status) {
//		return processDao.getInstancesByAgentNameStatus(agentId, name, status);
//	}
//
//	@Override
//	public List<ProcessInstance> getInstancesByAgentMonitorStatus(String agentId, String monitor, String status) {
//		return processDao.getInstancesByAgentMonitorStatus(agentId, monitor, status);
//	}
//	
//	@Override
//	public void insertInstance(ProcessInstance instance) {
//		processDao.insertInstance(instance);
//	}
//
//	@Override
//	public void insertData(ProcessData data) {
//		processDao.insertData(data);
//	}
//
//	@Override
//	public void updateInstanceStatus(String instanceId, String status) {
//		processDao.updateInstanceStatus(instanceId, status);
//	}
//
//	@Override
//	public void updateMonitorInstanceCount(String name, int instanceCount) {
//		processDao.updateMonitorInstanceCount(name, instanceCount);
//	}
//}
