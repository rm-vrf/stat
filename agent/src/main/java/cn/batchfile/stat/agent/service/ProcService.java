package cn.batchfile.stat.agent.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.agent.types.App;
import cn.batchfile.stat.agent.types.Proc;
import cn.batchfile.stat.util.Lock;
import cn.batchfile.stat.util.PortUtil;
import cn.batchfile.stat.util.cmd.CommandLineCallable;
import cn.batchfile.stat.util.cmd.CommandLineExecutor;

@Service
public class ProcService {

	protected static final Logger log = LoggerFactory.getLogger(ProcService.class);
	
	private File procDirectory;
	private Map<Long, LinkedBlockingQueue<String>> systemOuts = new ConcurrentHashMap<Long, LinkedBlockingQueue<String>>();
	private Map<Long, LinkedBlockingQueue<String>> systemErrs = new ConcurrentHashMap<Long, LinkedBlockingQueue<String>>();
	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat> () {
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
	private SysService sysService;
	
	@Autowired
	private NodeService nodeService;
	
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
	public void job() throws IOException, CommandLineException, InterruptedException {
		refresh();
	}
	
	public void refresh() throws IOException, CommandLineException, InterruptedException {
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
			int scale = app == null || !app.isStart() ? 0 : app.getScale();

			//杀掉多余的进程
			for (int i = scale; i < entry.getValue().size(); i ++) {
				//杀进程树
				Proc proc = entry.getValue().get(i);
				killProcTree(proc, app.getKillSignal());
				
				//删除登记信息
				deleteProc(proc.getPid());
				log.info("stop process, pid: {}, app name: {}", proc.getPid(), proc.getApp());
			}
		}
	}
	
	private void killProcTree(Proc proc, int signal) {
		//杀子进程
		for (int i = proc.getTree().size() - 1; i >= 0; i --) {
			try {
				sysService.kill(proc.getTree().get(i), signal);
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
			int scale = app.isStart() ? app.getScale() : 0;
			log.debug("schedule app: {}, scale: {}, proc instance: {}", app.getName(), scale, procCount);

			//如果登记的进程比计划的少，启动
			for (int i = procCount; i < scale; i ++) {
				//下载程序包
				downloadArtifacts(app);
				
				//启动进程，得到端口号
				Proc proc = new Proc();
				log.info("start proc of app: {}, #{}", app.getName(), i);
				long pid = startProc(app, proc.getPorts());

				//获取子进程编号
				List<Proc> ps = sysService.ps();
				getTree(proc.getTree(), pid, ps);
				log.info("started, pid: {}, tree: {}", pid, proc.getTree().toString());

				//补充进程的基本信息
				proc.setApp(app.getName());
				proc.setPid(pid);
				composeProc(proc, ps);
				proc.setStartTime(TIME_FORMAT.get().format(new Date()));
				
				//保存进程信息
				putProc(proc);
			}
		}
	}
	
	private void composeProc(Proc proc, List<Proc> ps) {
		//在列表中寻找进程
		Proc p = null;
		for (Proc e : ps) {
			if (e.getPid() == proc.getPid()) {
				p = e;
				break;
			}
		}

		//一定要有，没有就不正常了
		if (p != null) {
			proc.setCmd(p.getCmd());
			proc.setPpid(p.getPpid());
			proc.setStartTime(p.getStartTime());
			proc.setTty(p.getTty());
			proc.setUid(p.getUid());
		}
	}

	private long startProc(App app, List<Integer> ports) throws CommandLineException, IOException {
		//构建命令行
		Commandline commandline = composeCommandLine(app, ports);
		
		//执行命令，记录输出
		log.info("cmd: " + commandline.toString());
		
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
	
	private void checkFileStore() throws IOException {
		List<Long> list = getProcs();
		for (long pid : list) {
			Proc proc = getProc(pid);

			//有一种异常的情况：主进程已经没了，孩子进程还在。全部重启，也可以等到健康检查
			if (!runningProc(proc)) {
				//为了保险，把进程杀干净
				App app = appService.getApp(proc.getApp());
				int signal = app == null ? 9 : app.getKillSignal();
				killProcTree(proc, signal);
				
				//删除进程信息
				deleteProc(pid);
			}
		}
	}
	
	private boolean runningProc(Proc proc) {
		List<Long> pids = new ArrayList<Long>();
		pids.add(proc.getPid());
		//判断进程状态的时候，以主进程为准，子进程不算
		//pids.addAll(proc.getTree());
		
		for (long pid : pids) {
			if (runningProc(pid)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean runningProc(long pid) {
		Proc p = sysService.ps(pid);
		return p != null;
	}
	
	private void cleanSystemOut(Map<Long, LinkedBlockingQueue<String>> map) {
		List<Long> pids = getProcs();
		Iterator<Long> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Long key = iter.next();
			if (!pids.contains(key)) {
				map.remove(key);
			}
		}
	}

	private void downloadArtifacts(App app) throws IOException, InterruptedException {
		File dir = new File(app.getWorkingDirectory());
		if (!dir.exists()) {
			FileUtils.forceMkdir(dir);
		}
		log.info("working directory: {}", dir);
		
		if (app.getUris() != null) {
			for (String uri : app.getUris()) {
				downloadArtifact(dir, uri);
			}
		}
	}

	private void downloadArtifact(File dir, String uri) throws ClientProtocolException, IOException, InterruptedException {
		log.info("check artifact, uri: " + uri);
		
		//检查下载地址
		if (StringUtils.isEmpty(uri)) {
			return;
		}
		
		//从下载地址上得到文件名
		String fileBaseName = getBaseName(uri);
		
		//得到文件的最近修改时间
		String remoteLastModified = getRemoteLastModified(uri);
		log.info("remote file timestamp: " + remoteLastModified);
		if (StringUtils.isEmpty(remoteLastModified)) {
			//如果得不到远程时间戳，不必下载文件了，直接使用本地的包，没有就不能运行
			return;
		}
		
		//锁定文件
		Lock lock = null;
		try {
			lock = createLock(dir, fileBaseName);
			
			//得到本地的最近修改时间
			String localLastModified = getLocalLastModified(dir, fileBaseName);
			log.info("local file timestamp: " + localLastModified);
			//如果时间不相同，下载最新的包
			if (!StringUtils.equals(remoteLastModified, localLastModified)) {
				log.info("downloading...");
				File pkg = download(uri, dir, fileBaseName, remoteLastModified);
				log.info("downloaded");
				
				//解压到根目录
				if (StringUtils.endsWithIgnoreCase(pkg.getName(), ".zip")) {
					unzip(dir, pkg);
					log.info("unzip ok");
				}
			}
		} finally {
			try {
				lock.getFileLock().release();
			} catch (Exception e) {}
			IOUtils.closeQuietly(lock.getRandomAccessFile());
		}
	}

	private void unzip(File dir, File zipFile) throws IOException {
		InputStream inputStream = null;
		ZipInputStream zis = null;
		try {
			inputStream = new FileInputStream(zipFile);
			zis = new ZipInputStream(inputStream);
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				File f = new File(dir, name);
				if (f.exists()) {
					FileUtils.forceDelete(f);
				}
				if (StringUtils.endsWith(name, "/")) {
					log.debug(" creating: {}", name);
					FileUtils.forceMkdir(f);
				} else {
					log.debug("inflating: {}", name);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[2048];
					int num;
					while ((num = zis.read(buf, 0, 2048)) != -1) {
						out.write(buf, 0, num);
					}
					FileUtils.writeByteArrayToFile(f, out.toByteArray());
				}
			}
		} finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	private File download(String uri, File dir, String fileBaseName, String lastModified) throws IOException {
		
		//目标文件地址
		File distFile = new File(dir, fileBaseName);
		File timestampFile = new File(dir, String.format("%s.%s", fileBaseName, "lastModified"));
		
		//删除当前的文件
		FileUtils.deleteQuietly(distFile);
		FileUtils.deleteQuietly(timestampFile);
		
		//创建目标文件
		distFile.createNewFile();
		timestampFile.createNewFile();
		
		//下载文件流，向目标文件写入
		OutputStream out = null;
		InputStream in = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		try {
			//创建文件写入流
			out = new FileOutputStream(distFile);
			
			//构建请求对象
			RequestConfig config = RequestConfig.custom().setConnectTimeout(20000).build();
			
			//执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			HttpGet req = new HttpGet(uri);
			resp = httpClient.execute(req);

			//检查返回代码
			int code = resp.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				in = resp.getEntity().getContent();
				IOUtils.copyLarge(in, out);
			} else {
				throw new RuntimeException("error when get file: " + uri + ", code: " + code);
			}
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(resp);
			IOUtils.closeQuietly(httpClient);
		}
		
		//写入时间戳文件
		FileUtils.writeByteArrayToFile(timestampFile, lastModified.getBytes());
		
		return distFile;
	}
	
	private String getRemoteLastModified(String uri) throws ClientProtocolException, IOException {
		String lastModified = StringUtils.EMPTY;
		
		//构建请求对象
		RequestConfig config = RequestConfig.custom().setConnectTimeout(20000).build();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		
		try {
			//执行请求
			httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			HttpHead req = new HttpHead(uri);
			resp = httpClient.execute(req);
			
			//检查返回代码
			int code = resp.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				String value = getHeaderValue(resp, new String[]{"Last-Modified", "ETag"});
				if (StringUtils.isNotBlank(value)) {
					lastModified = value;
				}
			} else {
				throw new RuntimeException("error when head file: " + uri + ", code: " + code);
			}
		} catch (Exception e) {
			log.error("error when send head", e);
		} finally {
			IOUtils.closeQuietly(resp);
			IOUtils.closeQuietly(httpClient);
		}
		return lastModified;
	}
	
	private String getLocalLastModified(File dir, String fileBaseName) throws IOException {
		String lastModified = StringUtils.EMPTY;
		
		//构建时间戳文件的名称
		String fileName = String.format("%s.%s", fileBaseName, "lastModified");
		File file = new File(dir, fileName);
		
		//检查文件是否存在
		if (!file.exists()) {
			return lastModified;
		}
		
		//读文件内容
		String s = FileUtils.readFileToString(file);
		if (StringUtils.isNotEmpty(s)) {
			lastModified = s;
		}
		
		return lastModified;
	}
	
	private String getHeaderValue(CloseableHttpResponse resp, String[] headerNames) {
		for (String name : headerNames) {
			Header[] headers = resp.getHeaders(name);
			if (headers != null) {
				for (Header header : headers) {
					if (StringUtils.isNotEmpty(header.getValue())) {
						return header.getValue();
					}
				}
			}
		}
		return null;
	}
	
	private Lock createLock(File dir, String fileBaseName) throws IOException, InterruptedException {
		//创建锁文件
		File lockFile = new File(dir, String.format("%s.%s", fileBaseName, "lock"));
		if (!lockFile.exists()) {
			lockFile.createNewFile();
		}
		
		//对lock文件加锁
		RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
		FileChannel fc = raf.getChannel();
		FileLock fl = fc.lock();
		
		//返回锁对象
		return new Lock(raf, fl);
	}
	
	private String getBaseName(String uri) {
		String fileBaseName = StringUtils.substringAfterLast(uri, "/");
		if (StringUtils.contains(fileBaseName, "?")) {
			fileBaseName = StringUtils.substringBefore(fileBaseName, "?");
		}
		return fileBaseName;
	}
}
