package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.agent.types.App;
import cn.batchfile.stat.agent.types.Proc;
import cn.batchfile.stat.util.PortUtil;
import cn.batchfile.stat.util.cmd.CommandLineCallable;
import cn.batchfile.stat.util.cmd.CommandLineExecutor;

@Service
public class ProcService {

	protected static final Logger log = LoggerFactory.getLogger(ProcService.class);
	
	private Sigar sigar;
	private File procDirectory;
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	@Autowired
	private AppService appService;
	
	@PostConstruct
	public void init() throws IOException {
		sigar = new Sigar();
		
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		procDirectory = new File(f, "proc");
		if (!procDirectory.exists()) {
			FileUtils.forceMkdir(procDirectory);
		}
	}
	
	@Scheduled(fixedDelay = 10000)
	public void job() throws SigarException, IOException, CommandLineException {
		
		List<Proc> ps = ps();
		
		//清理多余的文件
		cleanFile(ps);
		
		//停止多余的进程
		stopProc(ps);
		
		//按计划拉起进程
		startProc(ps);
	}

	private List<Proc> ps() throws SigarException {
		List<Proc> ps = new ArrayList<Proc>();
		long[] pids = sigar.getProcList();
		for (long pid : pids) {
			Proc p = new Proc();
			composeProc(p, pid, null);
			ps.add(p);
		}
		return ps;
	}

	private void stopProc(List<Proc> ps) throws IOException {
		//获取登记的进程
		List<Long> pids = getProcs();
		List<Proc> procs = new ArrayList<Proc>();
		for (long pid : pids) {
			Proc proc = getProc(pid);
			procs.add(proc);
		}
		
		//按照应用名称归类
		Map<String, List<Proc>> groups = procs.stream().collect(Collectors.groupingBy(e -> e.getApp()));
		
		//判断进程是不是超过了计划的数量
		for (Entry<String, List<Proc>> entry : groups.entrySet()) {
			//得到计划的进程数量
			String appName = entry.getKey();
			App app = appService.getApp(appName);
			int scale = app == null ? 0 : app.getScale();
			
			for (int i = scale; i < entry.getValue().size(); i ++) {
				//杀进程
				Proc proc = entry.getValue().get(i);
				killProcTree(proc.getPid(), ps, app.getKillSignal());
				
				//删除登记信息
				deleteProc(proc.getPid());
				log.info("stop process, pid: {}, app name: {}", proc.getPid(), proc.getApp());
			}
		}
	}
	
	private void killProcTree(long pid, List<Proc> ps, int signal) {
		List<Long> tree = new ArrayList<Long>();
		tree.add(pid);
		getTree(tree, pid, ps);
		for (int i = tree.size() - 1; i >= 0; i --) {
			try {
				sigar.kill(tree.get(i), signal);
			} catch (Exception e) {}
			deleteProc(tree.get(i), ps);
		}
	}
	
	private void getTree(List<Long> tree, long pid, List<Proc> ps) {
		Proc child = findProcByPpid(pid, ps);
		if (child != null) {
			tree.add(child.getPid());
			pid = child.getPid();
		}
	}
	
	private Proc findProcByPpid(long ppid, List<Proc> ps) {
		for (Proc p : ps) {
			if (p.getPpid() == ppid) {
				return p;
			}
		}
		return null;
	}
	
	private void deleteProc(long pid, List<Proc> ps) {
		Iterator<Proc> it = ps.iterator();
		while (it.hasNext()) {
			if (it.next().getPid() == pid) {
				it.remove();
				break;
			}
		}
	}

	public List<Long> getProcs() {
		List<Long> list = new ArrayList<>();
		String[] files = procDirectory.list();
		for (String file : files) {
			if (!StringUtils.startsWith(file, ".") && StringUtils.isNumeric(file)) {
				Long l = Long.valueOf(file);
				list.add(l);
			}
		}
		return list;
	}

	public Proc getProc(long pid) throws IOException {
		Proc proc = new Proc();
		File f = new File(procDirectory, String.valueOf(pid));
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				proc = JSON.parseObject(s, Proc.class);
			}
		}
		return proc;
	}
	
	public void putProc(Proc proc) throws UnsupportedEncodingException, IOException {
		String s = JSON.toJSONString(proc);
		File f = new File(procDirectory, String.valueOf(proc.getPid()));
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public void deleteProc(long pid) {
		File f = new File(procDirectory, String.valueOf(pid));
		FileUtils.deleteQuietly(f);
	}

	private void startProc(List<Proc> ps) throws IOException, CommandLineException, SigarException {
		//应用列表
		List<App> apps = new ArrayList<App>();
		List<String> appNames = appService.getApps();
		for (String name : appNames) {
			App app = appService.getApp(name);
			apps.add(app);
		}
		log.debug("app count: {}", apps.size());
		
		//进程列表
		List<Proc> procs = new ArrayList<Proc>();
		List<Long> pids = getProcs();
		for (Long pid : pids) {
			Proc proc = getProc(pid);
			procs.add(proc);
		}
		log.debug("proc count: {}", procs.size());
		
		//检查应用，比较计划数量和实际数量
		for (App app : apps) {
			int procCount = count(app.getName(), procs);
			log.debug("schedule app: {}, scale: {}, proc instance: {}", app.getName(), app.getScale(), procCount);
			
			for (int i = procCount; i < app.getScale(); i ++) {
				//启动进程
				log.info("start proc of app: {}, #{}", app.getName(), i);
				Proc proc = new Proc();
				long pid = startProc(app, proc.getPorts());
				log.info("started, pid: {}", pid);
				
				//登记进程信息
				composeProc(proc, pid, app.getName());
				putProc(proc);
				
				//在列表中添加进程信息，这一步必要性不大，只是为了数据完整
				ps.add(proc);
			}
		}
	}
	
	private void composeProc(Proc proc, long pid, String app) {
		proc.setPid(pid);
		proc.setApp(app);
		
		try {
			String[] args = sigar.getProcArgs(pid);
			proc.setArgs(args);
		} catch (Exception ex) {}
		
		try {
			ProcCpu cpu = sigar.getProcCpu(pid);
			proc.setStartTime(new Date(cpu.getStartTime()));
		} catch (Exception ex) {}
		
		try {
			ProcCredName credName = sigar.getProcCredName(pid);
			proc.setUser(credName.getUser());
			proc.setGroup(credName.getGroup());
		} catch (Exception ex) {}
		
		try {
			ProcExe exe = sigar.getProcExe(pid);
			proc.setWorkDirectory(exe.getCwd());
			proc.setExe(exe.getName());
		} catch (Exception ex) {}
		
		try {
			ProcState state = sigar.getProcState(pid);
			proc.setPpid(state.getPpid());
			proc.setName(state.getName());
		} catch (Exception ex) {}
	}

	private long startProc(App app, List<Integer> ports) throws CommandLineException, IOException {
		//构建命令行
		Commandline commandline = composeCommandLine(app, ports);
		
		//执行命令，记录输出
		log.info("cmd: " + commandline.toString());
		CommandLineCallable callable = CommandLineExecutor.executeCommandLine(commandline, null, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				System.out.println(line);
			}
		}, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				System.err.println(line);
			}
		}, 0);
		
		return callable.getPid();
	}

	private Commandline composeCommandLine(App app, List<Integer> ports) throws IOException {
		
		//设置通配替换符号
		Map<String, String> vars = new HashMap<String, String>();
		vars.putAll(System.getenv());
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				vars.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		
		//创建命令行工具
		Commandline commandline = new Commandline(app.getToProcess());
		
		//设置工作目录
		if (StringUtils.isNotEmpty(app.getWorkingDirectory())) {
			commandline.setWorkingDirectory(new File(app.getWorkingDirectory()));
		}
		
		//设置端口
		if (app.getPorts() != null) {
			for (int i = 0; i < app.getPorts().size(); i ++) {
				int port = 0;
				Integer configPort = app.getPorts().get(i);
				if (configPort == null || configPort == 0) {
					port = randomPort();
					ports.add(port);
				} else {
					if (PortUtil.isUsedPort(configPort)) {
						throw new RuntimeException("Error: The port " + configPort + " is being used");
					}
					ports.add(configPort);
				}
				
				commandline.addEnvironment(String.format("PORT_%s", i + 1), String.valueOf(port));
				vars.put(String.format("PORT_%s", i + 1), String.valueOf(port));
				if (i == 0) {
					commandline.addEnvironment("PORT", String.valueOf(port));
					vars.put("PORT", String.valueOf(port));
				}
			}
		}
		
		//设置环境变量
		if (app.getEnvs() != null) {
			for (Entry<String, String> env : app.getEnvs().entrySet()) {
				commandline.addEnvironment(env.getKey(), replacePlaceholder(env.getValue(), vars));
			}
		}
		
		//设置参数
		if (app.getArgs() != null) {
			for (String arg : app.getArgs()) {
				Arg argObject = commandline.createArg();
				argObject.setValue(replacePlaceholder(arg, vars));
			}
		}
		
		return commandline;
	}
	
	private int randomPort() throws IOException {
		ServerSocket socket = new ServerSocket(0);
		int port = socket.getLocalPort();
		IOUtils.closeQuietly(socket);
		return port;
	}

	private String replacePlaceholder(String s, Map<String, String> vars) {
		String ret = s;
		String var = StringUtils.substringBetween(ret, "${", "}");
		
		//替换通配符
		while (StringUtils.isNotEmpty(var)) {
			//替换环境变量
			ret = StringUtils.replace(ret, String.format("${%s}", var), vars.get(var));
			
			//寻找下一个通配符
			var = StringUtils.substringBetween(ret, "${", "}");
		}
		return ret;
	}
	
	private int count(String app, List<Proc> procs) {
		int count = 0;
		for (Proc proc : procs) {
			if (StringUtils.equals(app, proc.getApp())) {
				count ++;
			}
		}
		return count;
	}

	private void cleanFile(List<Proc> ps) {
		List<Long> list = getProcs();
		for (long pid : list) {
			if (!existProc(pid, ps)) {
				deleteProc(pid);
			}
		}
	}
	
	private boolean existProc(long pid, List<Proc> ps) {
		for (Proc p : ps) {
			if (p.getPid() == pid) {
				return true;
			}
		}
		return false;
	}
	
}
