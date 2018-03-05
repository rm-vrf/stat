package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.Proc;
import cn.batchfile.stat.util.PortUtil;
import cn.batchfile.stat.util.cmd.CommandLineCallable;
import cn.batchfile.stat.util.cmd.CommandLineExecutor;

@Service
public class ProcService {

	protected static final Logger log = LoggerFactory.getLogger(ProcService.class);
	
	private File procDirectory;
	private Map<Long, LinkedBlockingQueue<String>> systemOuts = new ConcurrentHashMap<Long, LinkedBlockingQueue<String>>();
	private Map<Long, LinkedBlockingQueue<String>> systemErrs = new ConcurrentHashMap<Long, LinkedBlockingQueue<String>>();
	public static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat> () {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("MM-dd HH:mm");
		}
	};
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	@Value("${out.cache.line.count:500}")
	private int outCacheLineCount;
	
	@Autowired
	private AppService appService;
	
	@Autowired
	private ChoreoService choreoService;
	
	@Autowired
	private SysService sysService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private HealthCheckService healthCheckService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private DownloadService downloadService;
	
	@PostConstruct
	public void init() throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		procDirectory = new File(f, "proc");
		if (!procDirectory.exists()) {
			FileUtils.forceMkdir(procDirectory);
		}
	}
	
	@Scheduled(fixedDelay = 5000)
	public void refresh() throws IOException, CommandLineException, InterruptedException, SigarException {
		synchronized (this) {
			//检查文件存储，把废弃的进程号清理掉
			checkFileStore();
			
			//停止多余的进程
			checkRunningProc();
			
			//按计划拉起进程
			startScheduleProc();
			
			//清理输出信息
			cleanSystemOut(systemErrs);
			cleanSystemOut(systemOuts);
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
		Proc proc = null;
		File f = new File(procDirectory, String.valueOf(pid));
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				proc = JSON.parseObject(s, Proc.class);
			}
		}
		return proc;
	}
	
	public List<Long> getProcs(String app) throws IOException {
		List<Long> list = new ArrayList<>();
		File[] files = procDirectory.listFiles();
		for (File file : files) {
			if (!StringUtils.startsWith(file.getName(), ".") && StringUtils.isNumeric(file.getName())) {
				String s = FileUtils.readFileToString(file, "UTF-8");
				if (StringUtils.isNotEmpty(s)) {
					Proc proc = JSON.parseObject(s, Proc.class);
					if (StringUtils.equals(proc.getApp(), app)) {
						Long l = Long.valueOf(file.getName());
						list.add(l);
					}
				}
			}
		}
		return list;
	}
	
	public List<String> getSystemOut(long pid) {
		return getOutputCache(pid, systemOuts);
	}
	
	public List<String> getSystemErr(long pid) {
		return getOutputCache(pid, systemErrs);
	}
	
	public void killProcs(List<Long> pids) throws IOException {
		for (Long pid : pids) {
			//杀进程树
			Proc proc = getProc(pid);
			App app = appService.getApp(proc.getApp());
			killProcTree(proc, app.getKillSignal());
			
			//删除登记信息
			deleteProc(proc.getPid());
			log.info("stop process, pid: {}, app name: {}", proc.getPid(), proc.getApp());
			
			//报告事件
			eventService.putKillProcessEvent(app.getName(), proc.getPid());
		}
	}
	
	private void deleteProc(long pid) {
		File f = new File(procDirectory, String.valueOf(pid));
		FileUtils.deleteQuietly(f);
	}

	private List<String> getOutputCache(long pid, Map<Long, LinkedBlockingQueue<String>> queues) {
		List<String> list = new ArrayList<String>();
		LinkedBlockingQueue<String> queue = queues.get(pid);
		if (queue != null) {
			String s = null;
			while ((s = queue.poll()) != null) {
				list.add(s);
			}
		}
		return list;
	}

	private void putProc(Proc proc) throws UnsupportedEncodingException, IOException {
		String s = JSON.toJSONString(proc, SerializerFeature.PrettyFormat);
		File f = new File(procDirectory, String.valueOf(proc.getPid()));
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	private void checkRunningProc() throws IOException {
		//获取登记的进程
		List<Long> pids = getProcs();
		List<Proc> procs = new ArrayList<Proc>();
		for (long pid : pids) {
			Proc proc = getProc(pid);
			procs.add(proc);
		}
		
		//按照应用名称归类
		Map<String, List<Proc>> groups = procs.stream().collect(Collectors.groupingBy(p -> p.getApp()));
		
		//判断进程是不是超过了计划的数量
		for (Entry<String, List<Proc>> entry : groups.entrySet()) {
			//得到计划的进程数量
			String appName = entry.getKey();
			App app = appService.getApp(appName);
			Choreo choreo = choreoService.getChoreo(appName);
			int scale = (app == null || choreo == null || !app.isStart()) ? 0 : choreo.getScale();

			//杀掉多余的进程
			for (int i = scale; i < entry.getValue().size(); i ++) {
				//杀进程树
				Proc proc = entry.getValue().get(i);
				killProcTree(proc, app.getKillSignal());
				
				//删除登记信息
				deleteProc(proc.getPid());
				log.info("stop process, pid: {}, app name: {}", proc.getPid(), proc.getApp());
				
				//报告事件
				eventService.putKillProcessEvent(app.getName(), proc.getPid());
			}
		}
	}
	
	private void killProcTree(Proc proc, int signal) {
		//杀子进程
		for (int i = proc.getChildren().size() - 1; i >= 0; i --) {
			try {
				sysService.kill(proc.getChildren().get(i), signal);
			} catch (Exception e) {}
		}
		
		//杀进程
		try {
			sysService.kill(proc.getPid(), signal);
		} catch (Exception e) {}
	}
	
	private void getTree(List<Long> tree, long pid, List<Proc> ps) {
		List<Proc> children = findProcByPpid(pid, ps);
		for (Proc child : children) {
			tree.add(child.getPid());
			getTree(	tree, child.getPid(), ps);
		}
	}
	
	private List<Proc> findProcByPpid(long ppid, List<Proc> ps) {
		List<Proc> list = new ArrayList<Proc>();
		for (Proc p : ps) {
			if (p.getPpid() == ppid) {
				list.add(p);
			}
		}
		return list;
	}
	
	private void startScheduleProc() throws IOException, CommandLineException, InterruptedException {
		//登记应用
		List<App> apps = new ArrayList<App>();
		List<String> appNames = appService.getApps();
		for (String name : appNames) {
			App app = appService.getApp(name);
			apps.add(app);
		}
		log.debug("app count: {}", apps.size());
		
		//登记进程
		List<Proc> procs = new ArrayList<Proc>();
		List<Long> pids = getProcs();
		for (Long pid : pids) {
			Proc proc = getProc(pid);
			procs.add(proc);
		}
		log.debug("proc count: {}", procs.size());
		
		//按照应用名称归类进程
		Map<String, List<Proc>> groups = procs.stream().collect(Collectors.groupingBy(p -> p.getApp()));
		
		//检查应用，比较计划数量和实际数量
		for (App app : apps) {
			List<Proc> procList = groups.get(app.getName());
			int procCount = procList == null ? 0 : procList.size();
			Choreo choreo = choreoService.getChoreo(app.getName());
			int scale = choreo != null && app.isStart() ? choreo.getScale() : 0;
			log.debug("schedule app: {}, scale: {}, proc instance: {}", app.getName(), scale, procCount);

			//如果登记的进程比计划的少，启动
			for (int i = procCount; i < scale; i ++) {
				//下载程序包
				downloadService.downloadArtifacts(app);
				
				//启动进程，得到端口号&进程命令
				Proc proc = new Proc();
				log.info("start proc of app: {}, #{}", app.getName(), i);
				long pid = startProc(app, proc);

				//补充进程的基本信息
				proc.setApp(app.getName());
				proc.setNode(nodeService.getNode().getId());
				proc.setPid(pid);
				proc.setStartTime(TIME_FORMAT.get().format(new Date()));
				
				//保存进程信息
				putProc(proc);
				
				//注册健康检查
				healthCheckService.register(app, proc);
				
				//报告事件
				eventService.putStartProcessEvent(app.getName(), proc.getPid());
			}
		}
	}
	
	private void composeProc(Proc proc, List<Proc> ps) {
		//在列表中寻找进程
		List<Proc> list = ps.stream().filter(p -> p.getPid() == proc.getPid()).collect(Collectors.toList());
		Proc p = list != null && list.size() > 0 ? list.get(0) : null;

		//一定要有，没有就不正常了
		if (p != null) {
			if (proc.getPpid() == 0) {
				proc.setPpid(p.getPpid());
			}
			if (StringUtils.isEmpty(proc.getStartTime())) {
				proc.setStartTime(p.getStartTime());
			}
			if (StringUtils.isEmpty(proc.getUid())) {
				proc.setUid(p.getUid());
			}
		}
	}

	private long startProc(App app, Proc proc) throws CommandLineException, IOException {
		//构建命令行
		Commandline commandline = composeCommandLine(app, proc.getPorts());
		proc.setCmd(commandline.toString());
		log.info("CMD: " + commandline.toString());
		
		//构建输出存储器
		LinkedBlockingQueue<String> systemOut = new LinkedBlockingQueue<String>(outCacheLineCount);
		LinkedBlockingQueue<String> systemErr = new LinkedBlockingQueue<String>(outCacheLineCount);
		
		CommandLineCallable callable = CommandLineExecutor.executeCommandLine(commandline, null, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				if (systemOut.remainingCapacity() < 1) {
					systemOut.poll();
				}
				systemOut.offer(line);
			}
		}, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				if (systemErr.remainingCapacity() < 1) {
					systemErr.poll();
				}
				systemErr.offer(line);
			}
		}, 0);
		
		long pid = callable.getPid();
		systemOuts.put(pid, systemOut);
		systemErrs.put(pid, systemErr);
		
		return pid;
	}

	private Commandline composeCommandLine(App app, List<Integer> ports) throws IOException {
		
		//设置通配容器，加入顺序：系统环境变量，进程属性，节点全局环境变量
		Map<String, String> vars = new HashMap<String, String>();
		vars.putAll(System.getenv());
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				vars.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		if (nodeService.getEnvs() != null) {
			vars.putAll(nodeService.getEnvs());
		}
		
		//创建命令行工具
		Commandline commandline = new Commandline(app.getToProcess());
		
		//设置工作目录
		if (StringUtils.isNotEmpty(app.getWorkingDirectory())) {
			commandline.setWorkingDirectory(new File(app.getWorkingDirectory()));
		}
		
		//选择端口
		if (app.getPorts() != null) {
			for (int i = 0; i < app.getPorts().size(); i ++) {
				int port = 0;
				Integer configPort = app.getPorts().get(i);
				if (configPort == null || configPort == 0) {
					port = sysService.randomPort();
					ports.add(port);
				} else {
					if (PortUtil.isUsedPort(configPort)) {
						throw new RuntimeException("Error: The port " + configPort + " is being used");
					}
					ports.add(configPort);
				}

				//把端口加入环境变量
				commandline.addEnvironment(String.format("PORT_%s", i + 1), String.valueOf(port));
				vars.put(String.format("PORT_%s", i + 1), String.valueOf(port));
				if (i == 0) {
					commandline.addEnvironment("PORT", String.valueOf(port));
					vars.put("PORT", String.valueOf(port));
				}
			}
		}
		
		//全局环境变量
		if (nodeService.getEnvs() != null) {
			for (Entry<String, String> env : nodeService.getEnvs().entrySet()) {
				commandline.addEnvironment(env.getKey(), replacePlaceholder(env.getValue(), vars));
			}
		}
		
		//应用环境变量
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
	
	private String replacePlaceholder(String s, Map<String, String> vars) {
		String ret = s;
		String[] phs = StringUtils.substringsBetween(ret, "${", "}");
		if (phs != null) {
			for (String ph : phs) {
				//替换环境变量
				String t = vars.get(ph);
				if (StringUtils.isNotEmpty(t)) {
					ret = StringUtils.replace(ret, String.format("${%s}", ph), t);
				}
			}
		}
		return ret;
	}
	
	private void checkFileStore() throws IOException, SigarException {
		List<Proc> ps = sysService.ps();
		List<Long> list = getProcs();
		for (long pid : list) {
			Proc proc = getProc(pid);

			//有一种异常的情况：主进程已经没了，孩子进程还在。全部重启，也可以等到健康检查
			if (!runningProc(proc, ps)) {
				//为了保险，把进程杀干净
				App app = appService.getApp(proc.getApp());
				int signal = app == null ? 9 : app.getKillSignal();
				killProcTree(proc, signal);
				
				//删除进程信息
				deleteProc(pid);
				
				//报告事件
				eventService.putStopProcessEvent(app.getName(), pid);
				
			} else if (proc.getPpid() == 0) {
				//没有ppid，补充进程信息
				composeProc(proc, ps);
				
				//补充子进程信息
				getTree(proc.getChildren(), proc.getPid(), ps);
				log.info("compose ppid, pid: {}, ppid: {}, children: {}", 
						proc.getPid(), proc.getPpid(), proc.getChildren().toString());
				
				//保存进程信息
				putProc(proc);
			}
		}
	}
	
	private boolean runningProc(Proc proc, List<Proc> ps) {
		List<Long> pids = new ArrayList<Long>();
		pids.add(proc.getPid());
		//判断进程状态的时候，以主进程为准，子进程不算
		//pids.addAll(proc.getTree());
		
		for (long pid : pids) {
			if (runningProc(pid, ps)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean runningProc(long pid, List<Proc> ps) {
		List<Proc> list = ps.stream().filter(p -> p.getPid() == pid).collect(Collectors.toList());
		return list != null && list.size() > 0;
	}
	
	private void cleanSystemOut(Map<Long, LinkedBlockingQueue<String>> map) {
		List<Long> removes = new ArrayList<Long>();
		List<Long> pids = getProcs();
		
		for (long key : map.keySet()) {
			if (!pids.contains(key)) {
				removes.add(key);
			}
		}
		
		for (Long remove : removes) {
			LinkedBlockingQueue<String> queue = map.remove(remove);
			queue.clear();
		}
	}

}
