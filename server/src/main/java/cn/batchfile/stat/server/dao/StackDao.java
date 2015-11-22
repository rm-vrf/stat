package cn.batchfile.stat.server.dao;

import java.util.List;

import cn.batchfile.stat.server.domain.Stack;
import cn.batchfile.stat.server.domain.StackData;

public interface StackDao {

	void insertStack(Stack stack);

	List<Stack> getRunningStacks();

	void updateStackStatus(Stack stack);

	void insertData(StackData stackData);

}
