package cn.batchfile.stat.agent.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public List<Process> findProcesses(String name, String type) {
		return find_process("ps aux", name, type);
	}

	@Override
	public Process getProcess(long pid) {
		String cmd = String.format("ps ux -p %s", pid);
		List<Process> ps = find_process(cmd, null, null);
		if (ps == null || ps.size() == 0) {
			return null;
		} else {
			return ps.get(0);
		}
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
				Stack s = parse_stack(line);
				if (s != null) {
					stacks.add(s);
				}
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
		if (!StringUtils.contains(line, "os_prio") 
				|| !StringUtils.contains(line, "tid") 
				|| !StringUtils.contains(line, "nid")) {
			return null;
		}
		
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

	private List<Process> find_process(String cmd, String name, String type) {
		Map<String, String> jps = jps();
		String out = commandService.execute(cmd);
		out = StringUtils.remove(out, '\r');
		String[] lines = StringUtils.split(out, '\n');
		List<Process> list = new ArrayList<Process>();
		for (String line : lines) {
			if (!StringUtils.startsWithIgnoreCase(line, "USER")) {
				Process process = compose_process(line, jps);
				if ((StringUtils.isBlank(name) 
						|| StringUtils.contains(process.getCommand(), name) 
						|| StringUtils.contains(process.getMainClass(), name)) 
						&& (StringUtils.isBlank(type) 
								|| StringUtils.equals("all", type) 
								|| StringUtils.equals(type, process.getType()))) {
					list.add(process);
				}
			}
		}
		return list;
	}

	private Process compose_process(String line, Map<String, String> jps) {
		String[] array = StringUtils.split(line, ' ');
		String user = safe_get(array, 0);
		String pid = safe_get(array, 1);
		String cpu = safe_get(array, 2);
		String mem = safe_get(array, 3);
		String vsz = safe_get(array, 4);
		String rss = safe_get(array, 5);
		String tt = safe_get(array, 6);
		String stat = safe_get(array, 7);
		String started = safe_get(array, 8);
		String time = safe_get(array, 9);
		String cmd = StringUtils.EMPTY;
		for (int i = 10; i < array.length; i ++) {
			cmd += safe_get(array, i) + " ";
		}
		
		Process process = new Process();
		process.setCpuPercent(StringUtils.isEmpty(cpu) ? 0 : Double.valueOf(cpu));
		process.setMainClass(jps.get(pid));
		process.setMemoryPercent(StringUtils.isEmpty(mem) ? 0 : Double.valueOf(mem));
		process.setCommand(cmd);
		process.setPid(StringUtils.isEmpty(pid) ? 0 : Long.valueOf(pid));
		process.setRss(StringUtils.isEmpty(rss) ? 0 : Long.valueOf(rss));
		process.setStarted(started);
		process.setStat(stat);
		process.setTime(time);
		process.setTt(tt);
		process.setType(jps.containsKey(pid) ? "java" : "native");
		process.setUser(user);
		process.setVsz(StringUtils.isEmpty(vsz) ? 0 : Long.valueOf(vsz));
		return process;
	}
	
	private String safe_get(String[] array, int index) {
		if (array == null || index < 0 || array.length <= index) {
			return null;
		} else {
			return array[index];
		}
	}

	private Map<String, String> jps() {
		String cmd = "jps -lm";
		String out = commandService.execute(cmd);
		out = StringUtils.remove(out, '\r');
		String[] lines = StringUtils.split(out, '\n');
		Map<String, String> map = new HashMap<String, String>();
		for (String line : lines) {
			String[] arr = StringUtils.split(line, ' ');
			String pid = arr == null || arr.length == 0 ? StringUtils.EMPTY : arr[0];
			String cl = StringUtils.substring(line, StringUtils.length(pid) + 1);
			map.put(pid, cl);
		}
		return map;
	}
}
