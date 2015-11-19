package cn.batchfile.stat.server.service;

import java.util.List;

import cn.batchfile.stat.server.domain.Stack;
import cn.batchfile.stat.server.domain.StackData;

public interface StackService {

	String startStack(String agentId, long pid, String name);

	List<Stack> getRunningStacks();

	void updateStackStatus(Stack stack);

	void insertStackData(StackData sd);

}
