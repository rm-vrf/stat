package cn.batchfile.stat.agent.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarNotImplementedException;

import cn.batchfile.stat.agent.domain.Process;
import cn.batchfile.stat.agent.domain.Stack;
import cn.batchfile.stat.agent.service.CommandService;
import cn.batchfile.stat.agent.service.ProcessService;

public class ProcessServiceImpl implements ProcessService {
	private static final Logger LOG = Logger.getLogger(ProcessServiceImpl.class);
	private Map<Long, Process> ps = new ConcurrentHashMap<Long, Process>();
	private Sigar sigar;
	
	@Resource(name="commandService")
	private CommandService commandService;
	
	public void init() throws SigarException {
		sigar = new Sigar();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						long[] pids = sigar.getProcList();
						Map<Long, String> jps = jps();
						Map<Long, Process> tps = new ConcurrentHashMap<Long, Process>();
						for (long pid : pids) {
							try {
								Process p = compose_process(pid, jps, sigar);
								tps.put(pid, p);
							} catch (Exception e) {}
						}
						ps = tps;
						Thread.sleep(10000);
					} catch (Exception e) {}
				}
			}
		}).start();
	}
	
	@Override
	public List<Process> findProcesses(String[] query) {
		List<Process> list = new ArrayList<Process>();
		for (Process process : ps.values()) {
			String cmd = String.format("%s %s %s %s", 
					process.getName(), 
					process.getExe(),
					process.getJavaArgs(),
					StringUtils.join(process.getArgs(), " "));
			if (match(cmd, query)) {
				list.add(process);
			}
		}
		return list;
	}

	@Override
	public Process getProcess(long pid) {
		return ps.get(pid);
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
		try {
			sigar.kill(pid, signum);
		} catch (SigarException e) {
			throw new RuntimeException("error when kill process", e);
		}
	}

	private boolean match(String cmd, String[] query) {
		if (query == null || query.length == 0) {
			return true;
		} else {
			for (String q : query) {
				if (!StringUtils.contains(cmd, q)) {
					return false;
				}
			}
			return true;
		}
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

	private Process compose_process(long pid, Map<Long, String> jps, Sigar sigar) throws SigarException {
		Process process = new Process();
		process.setPid(pid);
		process.setJavaArgs(jps.get(pid));

		try {
			String[] args = sigar.getProcArgs(pid);
			process.setArgs(args);
		} catch (SigarNotImplementedException ex) {}
		
		try {
			ProcCpu cpu = sigar.getProcCpu(pid);
			process.setStartTime(new Date(cpu.getStartTime()));
			process.setCpuPercent(cpu.getPercent());
			process.setCpuSys(cpu.getSys());
			process.setCpuTotal(cpu.getTotal());
			process.setCpuUser(cpu.getUser());
		} catch (SigarNotImplementedException ex) {}

		try {
			ProcCredName credName = sigar.getProcCredName(pid);
			process.setUser(credName.getUser());
			process.setGroup(credName.getGroup());
		} catch (SigarNotImplementedException ex) {}

		try {
			ProcExe exe = sigar.getProcExe(pid);
			process.setWorkDirectory(exe.getCwd());
			process.setExe(exe.getName());
		} catch (SigarNotImplementedException ex) {}

		try {
			ProcMem mem = sigar.getProcMem(pid);
			process.setVsz(mem.getSize());
			process.setRss(mem.getResident());
		} catch (SigarNotImplementedException ex) {}

		try {
			ProcState state = sigar.getProcState(pid);
			process.setThreads(state.getThreads());
			process.setPpid(state.getPpid());
			process.setName(state.getName());
		} catch (SigarNotImplementedException ex) {}

		return process;
	}
	
	private Map<Long, String> jps() {
		Map<Long, String> map = new HashMap<Long, String>();
		try {
			String cmd = "jps -lm";
			String out = commandService.execute(cmd);
			out = StringUtils.remove(out, '\r');
			String[] lines = StringUtils.split(out, '\n');
			for (String line : lines) {
				String[] arr = StringUtils.split(line, ' ');
				String pid = arr == null || arr.length == 0 ? StringUtils.EMPTY : arr[0];
				String main_class = StringUtils.substring(line, StringUtils.length(pid) + 1);
				map.put(Long.valueOf(pid), main_class);
			}
		} catch (Exception e) {}
		return map;
	}
}
