//package cn.batchfile.stat.server.service.impl;
//
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.commons.lang.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import cn.batchfile.stat.server.dao.ProcessDao;
//import cn.batchfile.stat.server.dao.StackDao;
//import cn.batchfile.stat.server.domain.ProcessInstance;
//import cn.batchfile.stat.server.domain.Stack;
//import cn.batchfile.stat.server.domain.StackData;
//import cn.batchfile.stat.server.service.StackService;
//
//@Service
//public class StackServiceImpl implements StackService {
//	
//	@Autowired
//	private ProcessDao processDao;
//	
//	@Autowired
//	private StackDao stackDao;
//
//	@Override
//	public String startStack(String agentId, long pid, String name) {
//		if (StringUtils.isEmpty(name)) {
//			List<ProcessInstance> pis = processDao.getInstancesByAgentPidStatus(agentId, pid, "running");
//			if (pis != null && pis.size() > 0) {
//				ProcessInstance pi = pis.get(0);
//				name = pi.getName();
//			}
//		}
//		
//		Stack stack = new Stack();
//		stack.setAgentId(agentId);
//		stack.setBeginTime(new Date());
//		stack.setCommandId(UUID.randomUUID().toString().replaceAll("-", ""));
//		stack.setName(name);
//		stack.setPid(pid);
//		stack.setStatus("running");
//		
//		stackDao.insertStack(stack);
//		return stack.getCommandId();
//	}
//
//	@Override
//	public List<Stack> getRunningStacks() {
//		return stackDao.getRunningStacks();
//	}
//
//	@Override
//	public void updateStackStatus(Stack stack) {
//		stackDao.updateStackStatus(stack);
//	}
//
//	@Override
//	public void insertData(StackData stackData) {
//		stackDao.insertData(stackData);
//	}
//}
