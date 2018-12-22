package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.domain.Memory;
import cn.batchfile.stat.domain.Os;
import cn.batchfile.stat.domain.Process_;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class SystemService {

	protected static final Logger log = LoggerFactory.getLogger(SystemService.class);
	public static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};
	private static String[] FSS = new String[] { "btrfs", "f2fs", "ext3", "ext4", "hfs", "jfs", "nilfs", "reiser4",
			"reiserfs", "udf", "xfs", "zfs", "ntfs", "refs", "exfat", "fat", "vfat", "apfs", "adbfs", "encfs",
			"fuseiso", "gitfs", "gocryptfs", "sshfs", "vdfuse", "bfuse", "xmlfs", "aufs", "ecryptfs", "mergerfs",
			"mhddfs", "overlayfs", "unionfs", "squashfs", "ceph", "glusterfs", "go-ipfs", "moosefs", "openafs",
			"orangefs", "sheepdog", "tahoe-lafs" };
	private Sigar sigar;
	// private List<DiskStat> diskStats;
	// private List<NetworkStat> networkStats = new ArrayList<NetworkStat>();

	@Value("${store.directory}")
	private String storeDirectory;
	
	public SystemService(MeterRegistry registry) {
		// Gauge.builder("system.load", "", s ->
		// Runtime.getRuntime().).baseUnit("bytes").register(registry);
	}

	@PostConstruct
	public void init() throws IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		// copy lib into data path
		copyResources();

		// change lib path
		String libPath = System.getProperty("java.library.path");
		File sigarPath = new File(new File(storeDirectory), "sigar");
		if (StringUtils.isEmpty(libPath)) {
			libPath = sigarPath.getAbsolutePath();
		} else {
			libPath = String.format("%s%s%s", sigarPath.getAbsolutePath(), File.pathSeparatorChar, libPath);
		}
		log.info("lib path: {}", libPath);
		System.setProperty("java.library.path", libPath);

		// set sys_paths to null
		final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		sysPathsField.setAccessible(true);
		sysPathsField.set(null, null);

		// init sigar object
		sigar = new Sigar();
	}
	
	public List<Process_> ps(String grep) throws SigarException {
		List<Process_> ps = new ArrayList<Process_>();
		long[] pids = sigar.getProcList();
		for (long pid : pids) {
			Process_ p = ps(pid);
			if (p != null) {
				ps.add(p);
			}
		}
		
		if (StringUtils.isNotEmpty(grep)) {
			ps = ps.stream().filter(p -> match(p, grep)).collect(Collectors.toList());
		}
		
		return ps;
	}

	public List<Process_> ps() throws SigarException {
		return ps(null);
	}

	public Process_ ps(long pid) {
		// ppid
		long ppid = 0;
		try {
			ProcState state = sigar.getProcState(pid);
			ppid = state.getPpid();
		} catch (Exception e) {
			// pass
		}

		// start time
		String time = StringUtils.EMPTY;
		try {
			ProcCpu cpu = sigar.getProcCpu(pid);
			time = TIME_FORMAT.get().format(new Date(cpu.getStartTime()));
		} catch (Exception e) {
			// pass
		}

		// user
		String uid = StringUtils.EMPTY;
		String gid = StringUtils.EMPTY;
		try {
			ProcCredName credName = sigar.getProcCredName(pid);
			uid = credName.getUser();
			gid = credName.getGroup();
		} catch (Exception e) {
			// pass
		}
		
		//command
		String cwd = StringUtils.EMPTY;
		String cmd = StringUtils.EMPTY;
		try {
			ProcExe exe = sigar.getProcExe(pid);
			cwd = exe.getCwd();
			cmd = exe.getName();
		} catch (Exception e) {
			//pass
		} 
		
		//args
		String[] args = null;
		try {
			args = sigar.getProcArgs(pid);
		} catch (Exception e) {
			//
		}

		Process_ p = new Process_();
		p.setPid(pid);
		p.setPpid(ppid);
		p.setTime(time);
		p.setUid(uid);
		p.setGid(gid);
		p.setCwd(cwd);
		p.setCmd(cmd);
		p.setArgs(args);

		return p.getPpid() > 0 && StringUtils.isNotEmpty(time) && StringUtils.isNotEmpty(uid) ? p : null;
	}

	public void kill(long pid, int signal) throws SigarException {
		sigar.kill(pid, signal);
	}

	public int randomPort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			int port = socket.getLocalPort();
			return port;
		} catch (IOException e) {
			throw new RuntimeException("error when get random port", e);
		} finally {
			IOUtils.closeQuietly(socket);
		}
	}

	public List<String> execute(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()));
			String line;
			List<String> lines = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} catch (IOException e) {
			throw new RuntimeException("error when execute cmd: " + cmd, e);
		}
	}

	public String getHostname() {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			return ip.getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException("error when get hostname", e);
		}
	}

	public Os getOs() {
		OperatingSystemMXBean oper = ManagementFactory.getOperatingSystemMXBean();
		Os os = new Os();
		os.setArchitecture(oper.getArch());
		os.setCpus(oper.getAvailableProcessors());
		os.setName(oper.getName());
		os.setVersion(oper.getVersion());
		return os;
	}

	public Memory getMemory() throws SigarException {
		Memory memory = new Memory();
		Mem mem = sigar.getMem();
		memory.setRam(mem.getRam() * 1024 * 1024);
		memory.setTotal(mem.getTotal());
		return memory;
	}

	private void copyResources() throws IOException {
		File store = new File(storeDirectory);
		if (!store.exists()) {
			FileUtils.forceMkdir(store);
		}

		File sigarPath = new File(store, "sigar");
		if (!sigarPath.exists()) {
			FileUtils.forceMkdir(sigarPath);
		}

		log.info("cp lib file to {}", sigarPath.getAbsolutePath());
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources("sigar/*");

		for (Resource resource : resources) {
			log.info("find so file: {}, [{}]", resource.toString(), resource.getClass().getName());

			// get file name
			String path = getPathOfResource(resource);
			File file = new File(path);
			String fileName = file.getName();

			// write file
			File dist = new File(sigarPath, fileName);
			if (!dist.exists()) {
				// get file content
				byte[] content = getContentOfResource(resource);
				log.info("get so: {}, length: {}", fileName, content.length);

				FileUtils.writeByteArrayToFile(dist, content);
			}
		}
	}

	private String getPathOfResource(Resource resource) {
		if (resource instanceof ClassPathResource) {
			return ((ClassPathResource) resource).getPath();
		} else if (resource instanceof FileSystemResource) {
			return ((FileSystemResource) resource).getPath();
		} else {
			throw new RuntimeException("error when get resouece path");
		}
	}

	private byte[] getContentOfResource(Resource resource) throws IOException {
		if (resource instanceof ClassPathResource) {
			String path = ((ClassPathResource) resource).getPath();
			InputStream stream = getClass().getClassLoader().getResource(path).openStream();
			try {
				return IOUtils.toByteArray(stream);
			} finally {
				IOUtils.closeQuietly(stream);
			}
		} else if (resource instanceof FileSystemResource) {
			String path = ((FileSystemResource) resource).getPath();
			File file = new File(path);
			return FileUtils.readFileToByteArray(file);
		} else {
			throw new RuntimeException("error when get resouece content");
		}
	}
	
	private boolean match(Process_ p, String s) {
		if (StringUtils.equals(p.getPid().toString(), s)) {
			return true;
		}
		
		if (StringUtils.equals(p.getPpid().toString(), s)) {
			return true;
		}
		
		if (StringUtils.containsIgnoreCase(p.getUid(), s)) {
			return true;
		}
		
		if (StringUtils.containsIgnoreCase(p.getGid(), s)) {
			return true;
		}
		
		if (StringUtils.containsIgnoreCase(p.getTime(), s)) {
			return true;
		}
		
		if (StringUtils.containsIgnoreCase(p.getCwd(), s)) {
			return true;
		}
		
		if (StringUtils.containsIgnoreCase(p.getCmd(), s)) {
			return true;
		}
		
		String args = Arrays.asList(p.getArgs()).toString();
		if (StringUtils.containsIgnoreCase(args, s)) {
			return true;
		}
		
		return false;
	}

}
