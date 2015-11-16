package cn.batchfile.stat.agent.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.batchfile.stat.agent.domain.Process;
import cn.batchfile.stat.agent.domain.Stack;
import cn.batchfile.stat.agent.service.CommandService;
import cn.batchfile.stat.agent.service.ProcessService;

public class ProcessServiceImpl implements ProcessService {
	private static final Logger LOG = Logger.getLogger(ProcessServiceImpl.class);
	
	@Resource(name="commandService")
	private CommandService commandService;

	@Override
	public List<Process> findProcesses(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Process getProcess(long pid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo(long pid) {
		String cmd = String.format("jinfo %s", pid);
		return commandService.execute(cmd);
	}

	@Override
	public List<Stack> getStacks(long pid) {
		String cmd = String.format("jstack %s", pid);
		String out = commandService.execute(cmd);
		out = StringUtils.remove(out, '\r');
		String[] lines = StringUtils.split(out, '\n');
		List<Stack> stacks = new ArrayList<Stack>();
		for (String line : lines) {
			if (StringUtils.startsWith(line, "\"")) {
				stacks.add(parse_stack(line));
			}
		}
		return stacks;
	}

	@Override
	public void kill(long pid, int signum) {
		LOG.info(String.format("kill process, pid: %s, signum: %s", pid, signum));
		String cmd = String.format("kill -%s %s", signum, pid);
		commandService.execute(cmd);
	}

	private Stack parse_stack(String line) {
		//parse name
		String name = StringUtils.substringBetween(line, "\"", "\"");
		
		//parse id
		line = StringUtils.substring(line, StringUtils.length(name) + 3);
		String id = StringUtils.EMPTY;
		if (StringUtils.startsWith(line, "#")) {
			id = StringUtils.substringBefore(line, " ");
		}
		
		//parse prio
		String prio = StringUtils.substringBetween(line, " prio=", " ");
		
		//parse os_prio
		String os_prio = StringUtils.substringBetween(line, " os_prio=", " ");
		
		//parse tid
		String tid = StringUtils.substringBetween(line, " tid=", " ");
		
		//parse nid
		String nid = StringUtils.substringBetween(line, " nid=", " ");
		
		//parse status
		String status = StringUtils.substringAfterLast(line, String.format("nid=%s ", nid));
		if (StringUtils.contains(status, " [")) {
			status = StringUtils.substringBefore(status, " [");
		}
		
		Stack stack = new Stack();
		stack.setId(id);
		stack.setName(name);
		stack.setNid(nid);
		stack.setOsPrio(StringUtils.isEmpty(os_prio) ? 0 : Integer.valueOf(os_prio));
		stack.setPrio(StringUtils.isEmpty(prio) ? 0 : Integer.valueOf(prio));
		stack.setStatus(status);
		stack.setTid(tid);
		return stack;
	}
}
