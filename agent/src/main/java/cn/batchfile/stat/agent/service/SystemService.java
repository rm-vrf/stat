package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
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

import cn.batchfile.stat.agent.domain.CpuStat;
import cn.batchfile.stat.agent.domain.DiskStat;
import cn.batchfile.stat.agent.domain.MemoryStat;
import cn.batchfile.stat.agent.domain.NetworkStat;
import cn.batchfile.stat.domain.Disk;
import cn.batchfile.stat.domain.Memory;
import cn.batchfile.stat.domain.Network;
import cn.batchfile.stat.domain.Os;
import cn.batchfile.stat.domain.Process_;
import io.micrometer.core.instrument.Gauge;
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
	private List<DiskStat> diskStats = new ArrayList<>();
	private MemoryStat memoryStat = new MemoryStat();
	private List<NetworkStat> networkStats = new ArrayList<>();
	private CpuStat cpuStat = new CpuStat();

	@Value("${store.directory}")
	private String storeDirectory;

	public SystemService(MeterRegistry registry) {
		
		// 设置指标
		setupCpuStat(registry);
		setupMemoryStat(registry);
		setupDiskStat(registry);
		setupNetworkStat(registry);
		
		// 设置定时器，每分钟做一次指标统计
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					getDiskStats();
					getCpuStat();
					getMemoryStat();
					getNetworkStats();
				} catch (Exception e) {
					log.error("error when refresh instance", e);
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	@PostConstruct
	public void init() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {

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

		// command
		String cwd = StringUtils.EMPTY;
		String cmd = StringUtils.EMPTY;
		try {
			ProcExe exe = sigar.getProcExe(pid);
			cwd = exe.getCwd();
			cmd = exe.getName();
		} catch (Exception e) {
			// pass
		}

		// args
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

	public List<String> execute(String cmd, Map<String, String> env, File workDirectory) {
		return null;
//		try {
//			Process process = Runtime.getRuntime().exec(cmd);
//			LineNumberReader out = new LineNumberReader(new InputStreamReader(process.getInputStream()));
//			LineNumberReader err = new LineNumberReader(new InputStreamReader(process.getErrorStream()));
//			String line;
//			List<String> lines = new ArrayList<String>();
//			while ((line = out.readLine()) != null) {
//				lines.add(line);
//			}
//			process.exitValue();
//			return lines;
//		} catch (IOException e) {
//			throw new RuntimeException("error when execute cmd: " + cmd, e);
//		}
	}

	public String getHostname() {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			return ip.getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException("error when get hostname", e);
		}
	}

	public String getAddress() {
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			return inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException("error when get address", e);
		}
	}

	public Os getOs() {
		OperatingSystemMXBean oper = ManagementFactory.getOperatingSystemMXBean();
		Os os = new Os();
		os.setName(oper.getName());
		os.setVersion(oper.getVersion());
		os.setArchitecture(oper.getArch());
		os.setAvailableProcessors(oper.getAvailableProcessors());
		os.setSystemLoadAverage(oper.getSystemLoadAverage());
		return os;
	}

	public Memory getMemory() throws SigarException {
		Memory memory = new Memory();
		Mem mem = sigar.getMem();
		memory.setTotal(mem.getTotal());
		memory.setRam(mem.getRam());
		memory.setUsed(mem.getUsed());
		memory.setFree(mem.getFree());
		memory.setActualUsed(mem.getActualUsed());
		memory.setActualFree(mem.getActualFree());
		memory.setUsedPercent(mem.getUsedPercent());
		memory.setFreePercent(mem.getFreePercent());
		return memory;
	}

	public List<Disk> getDisks() throws SigarException {
		List<Disk> disks = new ArrayList<Disk>();

		FileSystem[] fss = sigar.getFileSystemList();
		for (FileSystem fs : fss) {
			Disk disk = new Disk();

			if (isAnyFs(fs.getSysTypeName(), FSS)) {
				disk.setDevName(fs.getDevName());
				disk.setDirName(fs.getDirName());
				disk.setFlags(fs.getFlags());
				disk.setOption(fs.getOptions());
				disk.setSysTypeName(fs.getSysTypeName());
				disk.setType(fs.getType());
				disk.setTypeName(fs.getTypeName());

				disks.add(disk);
			}
		}
		return disks;
	}

	public List<Network> getNetworks() throws SigarException {
		List<Network> networks = new ArrayList<Network>();

		String[] netIfs = sigar.getNetInterfaceList();
		for (String netIf : netIfs) {
			NetInterfaceConfig config = sigar.getNetInterfaceConfig(netIf);
			Network network = new Network();
			network.setAddress(config.getAddress());
			network.setName(config.getName());
			network.setMtu(config.getMtu());
			network.setBroadcast(config.getBroadcast());
			network.setDescription(config.getDescription());
			network.setDistination(config.getDestination());
			network.setFlags(config.getFlags());
			network.setHwaddr(config.getHwaddr());
			network.setMetric(config.getMetric());
			network.setNetmask(config.getNetmask());
			network.setType(config.getType());

			if (!network.getAddress().equals("127.0.0.1") && !network.getAddress().equals("0.0.0.0")) {
				networks.add(network);
			}
		}

		return networks;
	}

	private void getDiskStats() throws SigarException {
		FileSystem[] fss = sigar.getFileSystemList();

		List<DiskStat> list = new ArrayList<DiskStat>();
		for (FileSystem fs : fss) {
			long avail = 0;
			double diskQueue = 0;
			long diskReadBytes = 0;
			long diskReads = 0;
			double diskServiceTime = 0;
			long diskWriteBytes = 0;
			long diskWrites = 0;
			long files = 0;
			long free = 0;
			long freeFiles = 0;
			long total = 0;
			long used = 0;
			double usePercent = 0;

			try {
				FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
				avail = fsu.getAvail();
				diskQueue = fsu.getDiskQueue();
				diskReadBytes = fsu.getDiskReadBytes();
				diskReads = fsu.getDiskReads();
				diskServiceTime = fsu.getDiskServiceTime();
				diskWriteBytes = fsu.getDiskWriteBytes();
				diskWrites = fsu.getDiskWrites();
				files = fsu.getFiles();
				free = fsu.getFree();
				freeFiles = fsu.getFreeFiles();
				total = fsu.getTotal();
				used = fsu.getUsed();
				usePercent = fsu.getUsePercent();
			} catch (Exception e) {
				// pass
			}

			if (isAnyFs(fs.getSysTypeName(), FSS)) {
				DiskStat disk = new DiskStat();
				disk.setDevName(fs.getDevName());
				disk.setDirName(fs.getDirName());
				disk.setAvail(avail);
				disk.setDiskQueue(diskQueue);
				disk.setDiskReadBytes(diskReadBytes);
				disk.setDiskReads(diskReads);
				disk.setDiskServiceTime(diskServiceTime);
				disk.setDiskWriteBytes(diskWriteBytes);
				disk.setDiskWrites(diskWrites);
				disk.setFiles(files);
				disk.setFree(free);
				disk.setFreeFiles(freeFiles);
				disk.setTotal(total);
				disk.setUsed(used);
				disk.setUsePercent(usePercent);

				list.add(disk);
			}
		}

		diskStats.clear();
		diskStats.addAll(list);
	}
	
	private void setupDiskStat(MeterRegistry registry) {
		Gauge.builder("system.disk.avail", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getAvail()).sum()).register(registry);
		Gauge.builder("system.disk.disk.queue", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getDiskQueue()).sum()).register(registry);
		Gauge.builder("system.disk.disk.read.bytes", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getDiskReadBytes()).sum()).register(registry);
		Gauge.builder("system.disk.disk.reads", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getDiskReads()).sum()).register(registry);
		Gauge.builder("system.disk.disk.write.bytes", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getDiskWriteBytes()).sum()).register(registry);
		Gauge.builder("system.disk.disk.writes", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getDiskWrites()).sum()).register(registry);
		Gauge.builder("system.disk.files", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getFiles()).sum()).register(registry);
		Gauge.builder("system.disk.free", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getFree()).sum()).register(registry);
		Gauge.builder("system.disk.free.files", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getFreeFiles()).sum()).register(registry);
		Gauge.builder("system.disk.total", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getTotal()).sum()).register(registry);
		Gauge.builder("system.disk.used", StringUtils.EMPTY, s -> diskStats.stream().mapToDouble(d -> d.getUsed()).sum()).register(registry);
		Gauge.builder("system.disk.use.percent", StringUtils.EMPTY, s -> {
			double total = diskStats.stream().mapToDouble(d -> d.getTotal()).sum();
			double used = diskStats.stream().mapToDouble(d -> d.getUsed()).sum();
			return total > 0 ? used / total * 100 : 0;
		}).register(registry);
	}

	private void getNetworkStats() throws SigarException {
		String[] netIfs = sigar.getNetInterfaceList();
		List<NetworkStat> list = new ArrayList<NetworkStat>();

		for (String name : netIfs) {
			NetInterfaceConfig config = sigar.getNetInterfaceConfig(name);
			NetInterfaceStat stat = sigar.getNetInterfaceStat(name);

			if (!config.getAddress().equals("127.0.0.1") && !config.getAddress().equals("0.0.0.0")) {

				NetworkStat network = new NetworkStat();

				network.setAddress(config.getAddress());
				network.setName(config.getName());
				network.setRxBytes(stat.getRxBytes());
				network.setRxDropped(stat.getRxDropped());
				network.setRxErrors(stat.getRxErrors());
				network.setRxFrame(stat.getRxFrame());
				network.setRxOverruns(stat.getRxOverruns());
				network.setRxPackets(stat.getRxPackets());
				network.setSpeed(stat.getSpeed());
				network.setTxBytes(stat.getTxBytes());
				network.setTxCarrier(stat.getTxCarrier());
				network.setTxCollisions(stat.getTxCollisions());
				network.setTxDropped(stat.getTxDropped());
				network.setTxErrors(stat.getTxErrors());
				network.setTxOverruns(stat.getTxOverruns());
				network.setTxPackets(stat.getTxPackets());

				list.add(network);
			}
		}

		networkStats.clear();
		networkStats.addAll(list);
	}
	
	private void setupNetworkStat(MeterRegistry registry) {
		Gauge.builder("system.network.rx.bytes", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getRxBytes()).sum()).register(registry);
		Gauge.builder("system.network.rx.dropped", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getRxDropped()).sum()).register(registry);
		Gauge.builder("system.network.rx.errors", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getRxErrors()).sum()).register(registry);
		Gauge.builder("system.network.rx.frame", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getRxFrame()).sum()).register(registry);
		Gauge.builder("system.network.rx.overruns", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getRxOverruns()).sum()).register(registry);
		Gauge.builder("system.network.rx.packets", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getRxPackets()).sum()).register(registry);
		Gauge.builder("system.network.speed", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getSpeed()).sum()).register(registry);
		Gauge.builder("system.network.tx.bytes", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getTxBytes()).sum()).register(registry);
		Gauge.builder("system.network.tx.carrier", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getTxCarrier()).sum()).register(registry);
		Gauge.builder("system.network.tx.collisions", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getTxCollisions()).sum()).register(registry);
		Gauge.builder("system.network.tx.dropped", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getTxDropped()).sum()).register(registry);
		Gauge.builder("system.network.tx.errors", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getTxErrors()).sum()).register(registry);
		Gauge.builder("system.network.tx.overruns", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getTxOverruns()).sum()).register(registry);
		Gauge.builder("system.network.tx.packets", StringUtils.EMPTY, s -> networkStats.stream().mapToDouble(n -> n.getTxPackets()).sum()).register(registry);
	}

	private void getMemoryStat() throws SigarException {
		MemoryStat memory = new MemoryStat();
		Mem mem = sigar.getMem();
		memory.setActualFree(mem.getActualFree());
		memory.setActualUsed(mem.getActualUsed());
		memory.setFree(mem.getFree());
		memory.setFreePercent(mem.getFreePercent());
		memory.setRam(mem.getRam());
		memory.setTotal(mem.getTotal());
		memory.setUsed(mem.getUsed());
		memory.setUsedPercent(mem.getUsedPercent());
		memoryStat = memory;
	}

	private void setupMemoryStat(MeterRegistry registry) {
		Gauge.builder("system.memory.actual.free", StringUtils.EMPTY, s -> memoryStat.getActualFree()).register(registry);
		Gauge.builder("system.memory.actual.used", StringUtils.EMPTY, s -> memoryStat.getActualUsed()).register(registry);
		Gauge.builder("system.memory.free", StringUtils.EMPTY, s -> memoryStat.getFree()).register(registry);
		Gauge.builder("system.memory.free.percent", StringUtils.EMPTY, s -> memoryStat.getFreePercent()).register(registry);
		Gauge.builder("system.memory.ram", StringUtils.EMPTY, s -> memoryStat.getRam()).register(registry);
		Gauge.builder("system.memory.total", StringUtils.EMPTY, s -> memoryStat.getTotal()).register(registry);
		Gauge.builder("system.memory.used", StringUtils.EMPTY, s -> memoryStat.getUsed()).register(registry);
		Gauge.builder("system.memory.used.percent", StringUtils.EMPTY, s -> memoryStat.getUsedPercent()).register(registry);
	}

	private void getCpuStat() throws SigarException {
		CpuStat cpu = new CpuStat();
		CpuPerc cpuP = sigar.getCpuPerc();
		cpu.setCombined(cpuP.getCombined());
		cpu.setIdle(cpuP.getCombined());
		cpu.setIrq(cpuP.getIrq());
		cpu.setNice(cpuP.getNice());
		cpu.setSoftIrq(cpuP.getSoftIrq());
		cpu.setStolen(cpuP.getStolen());
		cpu.setSys(cpuP.getSys());
		cpu.setUser(cpuP.getUser());
		cpu.setWait(cpuP.getWait());
		cpuStat = cpu;
	}
	
	private void setupCpuStat(MeterRegistry registry) {
		Gauge.builder("system.cpu.combined", StringUtils.EMPTY, s -> cpuStat.getCombined()).register(registry);
		Gauge.builder("system.cpu.idle", StringUtils.EMPTY, s -> cpuStat.getIdle()).register(registry);
		Gauge.builder("system.cpu.irq", StringUtils.EMPTY, s -> cpuStat.getIrq()).register(registry);
		Gauge.builder("system.cpu.nice", StringUtils.EMPTY, s -> cpuStat.getNice()).register(registry);
		Gauge.builder("system.cpu.soft.irq", StringUtils.EMPTY, s -> cpuStat.getSoftIrq()).register(registry);
		Gauge.builder("system.cpu.stolen", StringUtils.EMPTY, s -> cpuStat.getStolen()).register(registry);
		Gauge.builder("system.cpu.sys", StringUtils.EMPTY, s -> cpuStat.getSys()).register(registry);
		Gauge.builder("system.cpu.user", StringUtils.EMPTY, s -> cpuStat.getUser()).register(registry);
		Gauge.builder("system.cpu.wait", StringUtils.EMPTY, s -> cpuStat.getWait()).register(registry);
	}

	private boolean isAnyFs(String s, String[] searchStrs) {
		for (String con : searchStrs) {
			if (StringUtils.startsWithIgnoreCase(s, con)) {
				return true;
			}
		}
		return false;
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
		if (StringUtils.equals(String.valueOf(p.getPid()), s)) {
			return true;
		}

		if (StringUtils.equals(String.valueOf(p.getPpid()), s)) {
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
