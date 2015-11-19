package cn.batchfile.stat.server.service;

import java.util.List;

import cn.batchfile.stat.server.domain.Stack;

public interface StackService {

	String startStack(String agentId, long pid);

	List<Stack> getRunningStacks();

	void updateStackStatus(Stack stack);

}
