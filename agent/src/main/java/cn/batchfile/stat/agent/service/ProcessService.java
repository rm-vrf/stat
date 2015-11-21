package cn.batchfile.stat.agent.service;

import java.util.List;

import cn.batchfile.stat.agent.domain.Process;
import cn.batchfile.stat.agent.domain.Stack;

public interface ProcessService {

	List<Process> findProcesses(String[] query);
	
	Process getProcess(long pid);
	
	String getInfo(long pid);
	
	List<Stack> getStacks(long pid);
	
	void kill(long pid, int signum);
}
