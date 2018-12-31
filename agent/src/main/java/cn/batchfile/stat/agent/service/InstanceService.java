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
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.agent.util.PortUtil;
import cn.batchfile.stat.agent.util.cmd.CommandLineCallable;
import cn.batchfile.stat.agent.util.cmd.CommandLineExecutor;
import cn.batchfile.stat.domain.Instance;
import cn.batchfile.stat.domain.Process_;
import cn.batchfile.stat.domain.Service;
import cn.batchfile.stat.service.ServiceService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@org.springframework.stereotype.Service
public class InstanceService {

	protected static final Logger LOG = LoggerFactory.getLogger(InstanceService.class);
	private static final int CACHE_LINE_COUNT = 500;
	public static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};
	private File instanceStoreDirectory;
	private Map<Long, LinkedBlockingQueue<String>> systemOuts = new ConcurrentHashMap<Long, LinkedBlockingQueue<String>>();
	private Map<Long, LinkedBlockingQueue<String>> systemErrs = new ConcurrentHashMap<Long, LinkedBlockingQueue<String>>();
	private String address;
	private String hostname;
	private Counter startInstanceCounter;
	private Counter stopInstanceCounter;
	private Counter killInstanceCounter;

	@Value("${store.directory}")
	private String storeDirectory;

	@Autowired
	private ServiceService serviceService;

	@Autowired
	private SystemService systemService;

	@Autowired
	private ArtifactService artifactService;

	@Autowired
	private HealthCheckService healthCheckService;

	@Autowired
	private EventService eventService;
	
	@Autowired
	private LoggingService loggingService;

	public InstanceService(MeterRegistry registry) {
		Gauge.builder("instance.running", "/", s -> {
			try {
				return getInstances().size();
			} catch (IOException e) {
				return 0;
			}
		}).register(registry);

		startInstanceCounter = Counter.builder("instance.start").register(registry);
		stopInstanceCounter = Counter.builder("instance.stop").register(registry);
		killInstanceCounter = Counter.builder("instance.kill").register(registry);
	}

	@PostConstruct
	public void init() throws IOException {
		hostname = systemService.getHostname();
		address = systemService.getAddress();

		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}

		instanceStoreDirectory = new File(f, "instance");
		if (!instanceStoreDirectory.exists()) {
			FileUtils.forceMkdir(instanceStoreDirectory);
		}

		// 启动定时器
		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					refresh();
				} catch (Exception e) {
					LOG.error("error when refresh instance", e);
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	private void refresh() throws IOException, SigarException, InterruptedException, Exception {
		// 检查文件存储，把废弃的进程号清理掉
		checkInstanceStore();

		// 停止多余的进程
		checkRunningInstance();

		// 按计划拉起进程
		startScheduleInstance();

		// 清理输出信息
		cleanSystemOut(systemErrs);
		cleanSystemOut(systemOuts);
	}

	public long getLastModified() {
		long l = instanceStoreDirectory.lastModified();
		for (File f : instanceStoreDirectory.listFiles()) {
			if (!StringUtils.startsWith(f.getName(), ".") && StringUtils.isNumeric(f.getName())) {
				if (f.lastModified() > l) {
					l = f.lastModified();
				}
			}
		}
		return l;
	}

	public long getLastModified(long pid) {
		File f = new File(instanceStoreDirectory, String.valueOf(pid));
		if (f.exists()) {
			return f.lastModified();
		} else {
			return -1;
		}
	}

	public List<Instance> getInstances() throws IOException {
		List<Instance> list = new ArrayList<>();
		File[] files = instanceStoreDirectory.listFiles();
		for (File file : files) {
			if (!StringUtils.startsWith(file.getName(), ".") && StringUtils.isNumeric(file.getName())) {
				String json = FileUtils.readFileToString(file, "UTF-8");
				if (StringUtils.isNotEmpty(json)) {
					Instance ins = JSON.parseObject(json, Instance.class);
					if (ins != null) {
						list.add(ins);
					}
				}
			}
		}
		return list;
	}

	public Instance getInstance(long pid) throws IOException {
		Instance ins = null;
		File f = new File(instanceStoreDirectory, String.valueOf(pid));
		if (f.exists()) {
			String json = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(json)) {
				ins = JSON.parseObject(json, Instance.class);
			}
		}
		return ins;
	}

	public List<Instance> getInstacnces(String service) throws IOException {
		List<Instance> list = getInstances();
		return list.stream().filter(i -> i.getService().equals(service)).collect(Collectors.toList());
	}

	public void putInstance(Instance instance) throws UnsupportedEncodingException, IOException {
		String json = JSON.toJSONString(instance, SerializerFeature.PrettyFormat);
		File f = new File(instanceStoreDirectory, String.valueOf(instance.getPid()));
		FileUtils.writeByteArrayToFile(f, json.getBytes("UTF-8"));
	}

	public void deleteInstance(long pid) {
		File f = new File(instanceStoreDirectory, String.valueOf(pid));
		FileUtils.deleteQuietly(f);
	}

	public List<String> getSystemOut(long pid) {
		return getOutputCache(pid, systemOuts);
	}

	public List<String> getSystemErr(long pid) {
		return getOutputCache(pid, systemErrs);
	}

	public void killInstances(List<Long> pids) throws IOException {
		for (Long pid : pids) {
			// 杀进程树
			Instance in = getInstance(pid);
			Service service = serviceService.getService(in.getService());
			killInstanceTree(in, service.getStopSignal());

			// 删除登记信息
			deleteInstance(in.getPid());
			LOG.info("kill instance, pid: {}, service: {}", in.getPid(), in.getService());

			// 报告事件
			eventService.putKillProcessEvent(in.getService(), in.getPid());
			killInstanceCounter.increment();
		}
	}

	public void startScheduleInstance() throws IOException, InterruptedException, Exception {
		// 登记应用
		List<Service> services = serviceService.getServices();
		LOG.debug("service count: {}", services.size());

		// 登记进程
		List<Instance> ins = getInstances();
		LOG.debug("instance count: {}", ins.size());

		// 按照应用名称归类进程
		Map<String, List<Instance>> groups = ins.stream().collect(Collectors.groupingBy(in -> in.getService()));

		// 检查应用，比较计划数量和实际数量
		for (Service service : services) {
			List<Instance> instanceList = groups.get(service.getName());
			int instanceCount = instanceList == null ? 0 : instanceList.size();
			int replicas = service == null || service.getDeploy() == null ? 0 : service.getDeploy().getReplicas();
			LOG.debug("schedule service: {}, replicas: {}, instance: {}", service.getName(), replicas, instanceCount);

			// 如果登记的进程比计划的少，启动
			for (int i = instanceCount; i < replicas; i++) {
				// 下载程序包
				artifactService.downloadArtifacts(service);

				// 启动进程，得到端口号&进程命令
				Instance in = new Instance();
				LOG.info("start instance of service: {}#{}", service.getName(), i);
				long pid = startInstance(service, in);

				// 补充进程的基本信息
				in.setService(service.getName());
				in.setLabels(service.getLabels());
				in.setWorkDirectory(service.getWorkDirectory());
				in.setPid(pid);
				in.setStartTime(TIME_FORMAT.get().format(new Date()));

				// 保存进程信息
				putInstance(in);

				// 注册健康检查
				healthCheckService.register(service, in);

				// 报告事件
				eventService.putStartProcessEvent(service.getName(), in.getPid());
				startInstanceCounter.increment();
			}
		}
	}

	private long startInstance(Service service, Instance instance) throws IOException, Exception {
		// 构建命令行
		if (instance.getPorts() == null) {
			instance.setPorts(new ArrayList<Integer>());
		}
		Commandline cmd = new Commandline();

		String cmdText = composeCommand(cmd, instance.getPorts(), service);
		instance.setCommand(cmdText);
		LOG.info("EXECUTE: {}", cmdText);

		// 构建输出存储器
		LinkedBlockingQueue<String> systemOut = new LinkedBlockingQueue<String>(CACHE_LINE_COUNT);
		LinkedBlockingQueue<String> systemErr = new LinkedBlockingQueue<String>(CACHE_LINE_COUNT);
		
		// 创建日志记录器
		Logger logger = loggingService.createLogger(service.getName(), service.getLogging().getOptions());

		// 启动进程
		CommandLineCallable callable = CommandLineExecutor.executeCommandLine(cmd, null, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				if (systemOut.remainingCapacity() < 1) {
					systemOut.poll();
				}
				systemOut.offer(line);
				logger.info(line);
			}
		}, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				if (systemErr.remainingCapacity() < 1) {
					systemErr.poll();
				}
				systemErr.offer(line);
				logger.info(line);
			}
		}, 0);

		long pid = callable.getPid();
		systemOuts.put(pid, systemOut);
		systemErrs.put(pid, systemErr);

		return pid;
	}

	private String composeCommand(Commandline cmd, List<Integer> ports, Service service) throws Exception {

		// 设置工作目录
		if (StringUtils.isNotEmpty(service.getWorkDirectory())) {
			cmd.setWorkingDirectory(new File(service.getWorkDirectory()));
		}
		
		// 创建通配符容器
		Map<String, String> vars = createPlaceHolderVars();

		// 选择端口
		usePorts(ports, service);
		LOG.info("PORTS: {}", ports.toString());

		// 把端口加入环境变量
		for (int i = 0; i < ports.size(); i++) {
			if (i == 0) {
				vars.put("PORT", String.valueOf(ports.get(i)));
			}
			vars.put(String.format("PORT_%s", i + 1), String.valueOf(ports.get(i)));
		}

		// 添加vars里面的环境变量
		for (Entry<String, String> entry : vars.entrySet()) {
			cmd.addEnvironment(entry.getKey(), entry.getValue());
		}

		// 添加用户自定义的环境变量
		if (service.getEnvironment() != null) {
			for (Entry<String, String> entry : service.getEnvironment().entrySet()) {
				cmd.addEnvironment(entry.getKey(), replacePlaceholder(entry.getValue(), vars));
			}
		}

		// 构建命令内容
		String cmdText = replacePlaceholder(service.getCommand(), vars);

		// 命令内容写临时文件
		List<File> cmdFiles = writeCommandFiles(service, cmdText);
		cmd.setExecutable(cmdFiles.get(0).getAbsolutePath());

		return cmdText;
	}

	private List<File> writeCommandFiles(Service service, String cmdText)
			throws UnsupportedEncodingException, IOException, CommandLineException {

		List<File> files = new ArrayList<>();
		File tmpDir = new File(System.getProperty("java.io.tmpdir", "."));
		String ext = SystemUtils.IS_OS_WINDOWS ? "bat" : "sh";
		
		// write command file
		File cmdFile = new File(tmpDir, service.getName() + "-cmd." + ext);
		FileUtils.writeByteArrayToFile(cmdFile, cmdText.getBytes("UTF-8"));
		LOG.info("command file: {}", cmdFile.getAbsolutePath());
		files.add(0, cmdFile);
		
		// sudo file
		if (StringUtils.isNotEmpty(service.getUid()) 
				&& SystemUtils.IS_OS_LINUX) {
			
			File suFile = new File(tmpDir, service.getName() + "-su." + ext);
			String suText = String.format("/bin/su -s /bin/sh -c '%s' '%s'", 
					files.get(0).getAbsolutePath(), service.getUid());
			
			FileUtils.writeByteArrayToFile(suFile, suText.getBytes("UTF-8"));
			LOG.info("su file: {}", suFile.getAbsolutePath());
			files.add(0, suFile);
		}

		// chmod files
		if (!SystemUtils.IS_OS_WINDOWS) {
			for (File file : files) {
				Commandline cmd = new Commandline();
				String[] ary = new String[] {"chmod", "a+x", "'" + file.getAbsolutePath() + "'"};
				for (String s : ary) {
					Arg argObject = cmd.createArg();
					argObject.setValue(s);
				}
				CommandLineCallable callable = CommandLineExecutor.executeCommandLine(cmd, null, null, null, 0);
				int ret = callable.call();
				LOG.info("{}, RET: {}", cmd.toString(), ret);
			}
		}

		return files;
	}

	private void usePorts(List<Integer> ports, Service service) {
		for (int i = 0; service.getPorts() != null && i < service.getPorts().size(); i++) {
			int port = 0;
			Integer configPort = service.getPorts().get(i);
			if (configPort == null || configPort == 0) {
				port = systemService.randomPort();
			} else {
				if (PortUtil.isUsedPort(configPort)) {
					throw new RuntimeException("Error: The port " + configPort + " is being used");
				}
				port = configPort;
			}
			ports.add(port);
		}
	}

	private Map<String, String> createPlaceHolderVars() {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("ADDRESS", address);
		vars.put("HOSTNAME", hostname);
		vars.putAll(System.getenv());
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			String key = entry.getKey() == null ? StringUtils.EMPTY : entry.getKey().toString();
			String value = entry.getValue() == null ? StringUtils.EMPTY : entry.getValue().toString();
			vars.put(key, value);
		}
		return vars;
	}

	private String replacePlaceholder(String s, Map<String, String> vars) {
		String ret = s;
		String[] phs = StringUtils.substringsBetween(ret, "${", "}");
		if (phs != null) {
			for (String ph : phs) {
				// 替换环境变量
				String t = vars.get(ph);
				if (StringUtils.isNotEmpty(t)) {
					ret = StringUtils.replace(ret, String.format("${%s}", ph), t);
				}
			}
		}
		return ret;
	}

	private void checkRunningInstance() throws IOException {
		// 获取登记的进程
		List<Instance> instances = getInstances();

		// 按照应用名称归类
		Map<String, List<Instance>> groups = instances.stream().collect(Collectors.groupingBy(in -> in.getService()));

		// 判断进程是不是超过了计划的数量
		for (Entry<String, List<Instance>> entry : groups.entrySet()) {
			// 得到计划的进程数量
			String serviceName = entry.getKey();
			Service service = serviceService.getService(serviceName);
			int replicas = service == null || service.getDeploy() == null ? 0 : service.getDeploy().getReplicas();

			// 杀掉多余的进程
			for (int i = replicas; i < entry.getValue().size(); i++) {
				// 杀进程树
				Instance in = entry.getValue().get(i);
				int stopSignal = service == null ? 9 : service.getStopSignal();
				killInstanceTree(in, stopSignal);

				// 删除登记信息
				deleteInstance(in.getPid());
				LOG.info("stop instance, pid: {}, service: {}", in.getPid(), in.getService());

				// 报告事件
				eventService.putKillProcessEvent(in.getService(), in.getPid());
				killInstanceCounter.increment();
			}
		}
	}

	private void killInstanceTree(Instance instance, int signal) {
		// 杀子进程
		for (int i = instance.getChildren() == null ? 0 : instance.getChildren().size() - 1; i >= 0; i--) {
			try {
				systemService.kill(instance.getChildren().get(i), signal);
			} catch (Exception e) {
			}
		}

		// 杀进程
		try {
			systemService.kill(instance.getPid(), signal);
		} catch (Exception e) {
		}
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

	private void cleanSystemOut(Map<Long, LinkedBlockingQueue<String>> map) throws IOException {
		List<Long> removes = new ArrayList<Long>();
		List<Instance> ins = getInstances();
		List<Long> pids = ins.stream().map(in -> in.getPid()).collect(Collectors.toList());

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

	private void checkInstanceStore() throws SigarException, IOException {
		List<Process_> ps = systemService.ps();
		List<Instance> ins = getInstances();
		for (Instance in : ins) {
			// 有一种异常的情况：主进程已经没了，孩子进程还在，全部重启
			if (!runningProc(in, ps)) {
				// 为了保险，把进程杀干净
				Service service = serviceService.getService(in.getService());
				int signal = service == null ? 9 : service.getStopSignal();
				killInstanceTree(in, signal);

				// 删除进程信息
				deleteInstance(in.getPid());

				// 报告事件
				eventService.putStopProcessEvent(in.getService(), in.getPid());
				stopInstanceCounter.increment();
			} else if (in.getPpid() == 0) {
				// 没有ppid，补充进程信息
				if (in.getChildren() == null) {
					in.setChildren(new ArrayList<Long>());
				}

				composeInstance(in, ps);

				// 补充子进程信息
				getTree(in.getChildren(), in.getPid(), ps);
				LOG.info("compose process, pid: {}, ppid: {}, children: {}", in.getPid(), in.getPpid(),
						in.getChildren().toString());

				// 保存进程信息
				putInstance(in);
			}

		}
	}

	private void getTree(List<Long> tree, long pid, List<Process_> ps) {
		List<Process_> children = ps.stream().filter(p -> p.getPpid() == pid).collect(Collectors.toList());
		if (children != null) {
			for (Process_ child : children) {
				tree.add(child.getPid());
				getTree(tree, child.getPid(), ps);
			}
		}
	}

	private void composeInstance(Instance instance, List<Process_> ps) {
		// 在列表中寻找进程
		List<Process_> list = ps.stream().filter(p -> p.getPid() == instance.getPid()).collect(Collectors.toList());
		Process_ p = list != null && list.size() > 0 ? list.get(0) : null;

		// 一定要有，没有就不正常了
		if (p != null) {
			if (instance.getPpid() == 0) {
				instance.setPpid(p.getPpid());
			}
			if (StringUtils.isEmpty(instance.getStartTime())) {
				instance.setStartTime(p.getTime());
			}
			if (StringUtils.isEmpty(instance.getUid())) {
				instance.setUid(p.getUid());
			}
		}
	}

	private boolean runningProc(Instance instance, List<Process_> ps) {
		List<Long> pids = new ArrayList<Long>();
		pids.add(instance.getPid());

		// 判断进程状态的时候，以主进程为准，子进程不算
		// pids.addAll(proc.getTree());

		for (long pid : pids) {
			if (runningProc(pid, ps)) {
				return true;
			}
		}

		return false;
	}

	private boolean runningProc(long pid, List<Process_> ps) {
		List<Process_> list = ps.stream().filter(p -> p.getPid() == pid).collect(Collectors.toList());
		return list != null && list.size() > 0;
	}

}
