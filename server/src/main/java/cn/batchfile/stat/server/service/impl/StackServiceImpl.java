package cn.batchfile.stat.server.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import cn.batchfile.stat.server.domain.Stack;
import cn.batchfile.stat.server.service.StackService;

@Service
public class StackServiceImpl implements StackService {

	@Override
	public String startStack(String agentId, long pid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Stack> getRunningStacks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateStackStatus(Stack stack) {
		// TODO Auto-generated method stub

	}

}
