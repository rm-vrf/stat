package cn.batchfile.stat.server.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.server.dao.ProcessDao;
import cn.batchfile.stat.server.domain.Process;
import cn.batchfile.stat.server.domain.ProcessData;
import cn.batchfile.stat.server.domain.ProcessInstance;
import cn.batchfile.stat.server.service.ProcessService;

@Service
public class ProcessServiceImpl implements ProcessService {
	
	@Autowired
	private ProcessDao processDao;

	@Override
	public List<Process> getProcessByAgentId(String agentId) {
		return processDao.getProcessByAgentId(agentId);
	}

	@Override
	public List<ProcessInstance> getRunningInstance(List<cn.batchfile.stat.agent.domain.Process> ps, Process process) {
		List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
		
		//remove instance in db
		List<ProcessInstance> is = processDao.getRunningProcessInstanceByAgentId(process.getAgentId());
		for (ProcessInstance i : is) {
			if (!exists(i.getPid(), ps)) {
				i.setStatus("stop");
				processDao.updateProcessInstanceStatus(i);
			}
		}
		
		//add instance into db
		for (cn.batchfile.stat.agent.domain.Process p : ps) {
			if (contains(p.getCommand(), process.getContainsEvery(), process.getContainsAny(), process.getContainsNo())) {
				
				ProcessInstance instance = new ProcessInstance();
				ProcessData data = new ProcessData();
				
				instance.setAgentId(process.getAgentId());
				instance.setName(process.getName());
				instance.setPid(p.getPid());
				instance.setCommand(p.getCommand());
				instance.setMainClass(p.getMainClass());
				instance.setStarted(p.getStarted());
				instance.setStatus("running");
				instance.setType(p.getType());
				instance.setUser(p.getUser());
				
				data.setAgentId(process.getAgentId());
				data.setName(process.getName());
				data.setPid(p.getPid());
				data.setTime(new Date());
				data.setCpuPercent(p.getCpuPercent());
				data.setMemoryPercent(p.getMemoryPercent());
				data.setVsz(p.getVsz());
				data.setRss(p.getRss());
				data.setTt(p.getTt());
				data.setStat(p.getStat());
				data.setUptime(p.getTime());
				
				processDao.insertProcessInstance(instance);
				processDao.insertProcessData(data);
				instances.add(instance);
			}
		}
		
		return instances;
	}

	@Override
	public void updateRunningInstance(Process process) {
		processDao.updateRunningInstance(process);
	}
	
	private boolean contains(String command, String containsEvery, String containsAny, String containsNo) {
		if (StringUtils.isNotBlank(containsEvery) && !containsEvery(command, containsEvery)) {
			return false;
		} else if (StringUtils.isNotBlank(containsAny) && !containsAny(command, containsAny)) {
			return false;
		} else if (StringUtils.isNotBlank(containsNo) && containsAny(command, containsNo)) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean containsAny(String s1, String s2) {
		String[] ary = StringUtils.split(s2, ' ');
		for (String e : ary) {
			if (StringUtils.isNotEmpty(e) && StringUtils.contains(s1, e)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsEvery(String s1, String s2) {
		String[] ary = StringUtils.split(s2, ' ');
		for (String e : ary) {
			if (StringUtils.isNotEmpty(e) && !StringUtils.contains(s1, e)) {
				return false;
			}
		}
		return true;
	}

	private boolean exists(long pid, List<cn.batchfile.stat.agent.domain.Process> ps) {
		for (cn.batchfile.stat.agent.domain.Process p : ps) {
			if (p.getPid() == pid) {
				return true;
			}
		}
		return false;
	}
}
