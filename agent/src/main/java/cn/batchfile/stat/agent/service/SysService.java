//package cn.batchfile.stat.agent.service;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.LineNumberReader;
//import java.lang.management.ManagementFactory;
//import java.lang.management.OperatingSystemMXBean;
//import java.lang.reflect.Field;
//import java.net.InetAddress;
//import java.net.ServerSocket;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import javax.annotation.PostConstruct;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.StringUtils;
//import org.hyperic.sigar.CpuPerc;
//import org.hyperic.sigar.FileSystem;
//import org.hyperic.sigar.FileSystemUsage;
//import org.hyperic.sigar.Mem;
//import org.hyperic.sigar.NetInterfaceConfig;
//import org.hyperic.sigar.NetInterfaceStat;
//import org.hyperic.sigar.ProcCpu;
//import org.hyperic.sigar.ProcCredName;
//import org.hyperic.sigar.ProcExe;
//import org.hyperic.sigar.ProcMem;
//import org.hyperic.sigar.ProcState;
//import org.hyperic.sigar.Sigar;
//import org.hyperic.sigar.SigarException;
//import org.hyperic.sigar.SigarNotImplementedException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import cn.batchfile.stat.domain.CpuStat;
//import cn.batchfile.stat.domain.Disk;
//import cn.batchfile.stat.domain.DiskStat;
//import cn.batchfile.stat.domain.Everything;
//import cn.batchfile.stat.domain.Memory;
//import cn.batchfile.stat.domain.MemoryStat;
//import cn.batchfile.stat.domain.Network;
//import cn.batchfile.stat.domain.NetworkStat;
//import cn.batchfile.stat.domain.Os;
//import cn.batchfile.stat.domain.OsStat;
//import cn.batchfile.stat.domain.Proc;
//import cn.batchfile.stat.domain.ProcStat;
//
//@Service
//public class SysService {
//	protected static final Logger log = LoggerFactory.getLogger(SysService.class);
//	private static String[] FSS = new String[] {
//			"btrfs", "f2fs", "ext3", "ext4", "hfs", "jfs", "nilfs", 
//			"reiser4", "reiserfs", "udf", "xfs", "zfs", 
//			"ntfs", "refs", "exfat", "fat", "vfat", 
//			"apfs",
//			"adbfs", "encfs", "fuseiso", "gitfs", "gocryptfs", "sshfs", "vdfuse", "bfuse", "xmlfs", 
//			"aufs", "ecryptfs", "mergerfs", "mhddfs", "overlayfs", "unionfs", "squashfs", 
//			"ceph", "glusterfs", "go-ipfs", "moosefs", "openafs", "orangefs", "sheepdog", "tahoe-lafs"};
//	private Sigar sigar;
//	private List<DiskStat> diskStats = new ArrayList<DiskStat>();
//	private List<NetworkStat> networkStats = new ArrayList<NetworkStat>();
//	
//	@Value("${master.address:}")
//	private String masterAddress;
//	
//	@Value("${store.directory}")
//	private String storeDirectory;
//	
//	@Autowired
//	private RestTemplate restTemplate;
//	
//	@PostConstruct
//	public void init() throws Exception {
//		//copy lib into data path
//		copyResources();
//
//		//change lib path
//		String libPath = System.getProperty("java.library.path");
//		File sigarPath = new File(new File(storeDirectory), "sigar");
//		if (StringUtils.isEmpty(libPath)) {
//			libPath = sigarPath.getAbsolutePath();
//		} else {
//			libPath = String.format("%s%s%s", sigarPath.getAbsolutePath(), File.pathSeparatorChar, libPath);
//		}
//		log.info("lib path: {}", libPath);
//		System.setProperty("java.library.path", libPath);
//		
//		//set sys_paths to null
//		final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
//		sysPathsField.setAccessible(true);
//		sysPathsField.set(null, null);
//
//		//init sigar object
//		sigar = new Sigar();
//		
//		//start timer
//		ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
//		es.scheduleAtFixedRate(() -> {
//			try {
//				reportStat();
//			} catch (Exception e) {
//				//pass
//			}
//		}, 0, 60, TimeUnit.SECONDS);
//	}
//	
//	private void copyResources() throws IOException {
//		File store = new File(storeDirectory);
//		if (!store.exists()) {
//			FileUtils.forceMkdir(store);
//		}
//		File sigarPath = new File(store, "sigar");
//		if (!sigarPath.exists()) {
//			FileUtils.forceMkdir(sigarPath);
//		}
//		
//		log.info("cp lib file to {}", sigarPath.getAbsolutePath());
//		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
//		Resource[] resources = resourcePatternResolver.getResources("sigar/*");
//		
//		for (Resource resource : resources) {
//			log.info("find so file: {}, [{}]", resource.toString(), resource.getClass().getName());
//			
//			//get file name
//			String path = getPathOfResource(resource);
//			File file = new File(path);
//			String fileName = file.getName();
//			
//			//write file
//			File dist = new File(sigarPath, fileName);
//			if (!dist.exists()) {
//				//get file content
//				byte[] content = getContentOfResource(resource);
//				log.info("get so: {}, length: {}", fileName, content.length);
//				
//				FileUtils.writeByteArrayToFile(dist, content);
//			}
//		}
//	}
//	
//	private String getPathOfResource(Resource resource) {
//		if (resource instanceof ClassPathResource) {
//			return ((ClassPathResource) resource).getPath();
//		} else if (resource instanceof FileSystemResource) {
//			return ((FileSystemResource) resource).getPath();
//		} else {
//			throw new RuntimeException("error when get resouece path");
//		}
//	}
//	
//	public List<Proc> ps() throws SigarException {
//		List<Proc> ps = new ArrayList<Proc>();
//		long[] pids = sigar.getProcList();
//		for (long pid : pids) {
//			Proc p = ps(pid);
//			if (p != null) {
//				ps.add(p);
//			}
//		}
//		return ps;
//	}
//	
//	public Proc ps(long pid) {
//		//ppid
//		long ppid = 0;
//		try {
//			ProcState state = sigar.getProcState(pid);
//			ppid = state.getPpid();
//		} catch (Exception e) {
//			//pass
//		}
//		
//		//start time
//		String startTime = StringUtils.EMPTY;
//		try {
//			ProcCpu cpu = sigar.getProcCpu(pid);
//			startTime = ProcService.TIME_FORMAT.get().format(new Date(cpu.getStartTime()));
//		} catch (Exception e) {
//			//pass
//		}
//		
//		//user
//		String uid = StringUtils.EMPTY;
//		try {
//			ProcCredName credName = sigar.getProcCredName(pid);
//			uid = credName.getUser();
//		} catch (Exception e) {
//			//pass
//		}
//		
//		Proc p = new Proc();
//		p.setPid(pid);
//		p.setPpid(ppid);
//		p.setStartTime(startTime);
//		p.setUid(uid);
//		
//		return p.getPpid() > 0 
//				&& StringUtils.isNotEmpty(startTime) 
//				&& StringUtils.isNotEmpty(uid) ? p : null;
//	}
//	
//	public void kill(long pid, int signal) throws SigarException {
//		sigar.kill(pid, signal);
//	}
//
//	public int randomPort() {
//		ServerSocket socket = null;
//		try {
//			socket = new ServerSocket(0);
//			int port = socket.getLocalPort();
//			return port;
//		} catch (IOException e) {
//			throw new RuntimeException("error when get random port", e);
//		} finally {
//			IOUtils.closeQuietly(socket);
//		}
//	}
//	
//	public List<String> exec(String cmd) {
//		try {
//			Process process = Runtime.getRuntime().exec(cmd);
//			LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()));
//			String line;
//			List<String> lines = new ArrayList<String>();
//			while ((line = br.readLine()) != null) {
//				lines.add(line);
//			}
//			return lines;
//		} catch (IOException e) {
//			throw new RuntimeException("error when execute cmd: " + cmd, e);
//		}
//	}
//
//	public String getHostname() {
//		try {
//			InetAddress ip = InetAddress.getLocalHost();
//			return ip.getHostName();
//		} catch (UnknownHostException e) {
//			throw new RuntimeException("error when get hostname", e);
//		}
//	}
//
//	public Os getOs() {
//		OperatingSystemMXBean oper = ManagementFactory.getOperatingSystemMXBean();
//		Os os = new Os();
//		os.setArchitecture(oper.getArch());
//		os.setCpus(oper.getAvailableProcessors());
//		os.setName(oper.getName());
//		os.setVersion(oper.getVersion());
//		return os;
//	}
//
//	public List<Disk> getDisks() throws SigarException {
//		List<Disk> disks = new ArrayList<Disk>();
//		
//		FileSystem[] fss = sigar.getFileSystemList();
//		for (FileSystem fs : fss) {
//			Disk disk = new Disk();
//			
//			long total = 0;
//			try {
//				FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
//				total = fsu.getTotal() * 1024;
//			} catch (Exception e) {
//				//pass
//			}
//			
//			if (isAnyFs(fs.getSysTypeName(), FSS)) {
//				disk.setDevName(fs.getDevName());
//				disk.setDirName(fs.getDirName());
//				disk.setFlags(fs.getFlags());
//				disk.setOption(fs.getOptions());
//				disk.setSysTypeName(fs.getSysTypeName());
//				disk.setTotal(total);
//				disk.setType(fs.getType());
//				disk.setTypeName(fs.getTypeName());
//			
//				disks.add(disk);
//			}
//		}
//		return disks;
//	}
//
//	public Memory getMemory() throws SigarException {
//		Memory memory = new Memory();
//		Mem mem = sigar.getMem();
//		memory.setRam(mem.getRam() * 1024 * 1024);
//		memory.setTotal(mem.getTotal());
//		return memory;
//	}
//
//	public List<Network> getNetworks() throws SigarException {
//		List<Network> networks = new ArrayList<Network>();
//		
//		String[] netIfs = sigar.getNetInterfaceList();
//		for (String netIf : netIfs) {
//			NetInterfaceConfig config = sigar.getNetInterfaceConfig(netIf);
//			Network network = new Network();
//			network.setAddress(config.getAddress());
//			network.setName(config.getName());
//			network.setMtu(config.getMtu());
//			network.setBroadcast(config.getBroadcast());
//			network.setDescription(config.getDescription());
//			network.setDistination(config.getDestination());
//			network.setFlags(config.getFlags());
//			network.setHwaddr(config.getHwaddr());
//			network.setMetric(config.getMetric());
//			network.setNetmask(config.getNetmask());
//			network.setType(config.getType());
//				
//			if (!network.getAddress().equals("127.0.0.1") 
//						&& !network.getAddress().equals("0.0.0.0")) {
//				networks.add(network);
//			}
//		}
//		
//		return networks;
//	}
//
//	private boolean isAnyFs(String s, String[] searchStrs) {
//		for (String con : searchStrs) {
//			if (StringUtils.startsWithIgnoreCase(s, con)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private byte[] getContentOfResource(Resource resource) throws IOException {
//		if (resource instanceof ClassPathResource) {
//			String path = ((ClassPathResource) resource).getPath();
//			InputStream stream = getClass().getClassLoader().getResource(path).openStream();
//			try {
//				return IOUtils.toByteArray(stream);
//			} finally {
//				IOUtils.closeQuietly(stream);
//			}
//		} else if (resource instanceof FileSystemResource) {
//			String path = ((FileSystemResource) resource).getPath();
//			File file = new File(path);
//			return FileUtils.readFileToByteArray(file);
//		} else {
//			throw new RuntimeException("error when get resouece content");
//		}
//	}
//
//	private void reportStat() throws IOException, SigarException {
//		if (StringUtils.isEmpty(masterAddress)) {
//			return;
//		}
//		
//		Everything everything = new Everything();
//		everything.setId(getId());
//		everything.setHostname(getHostname());
//		
//		if (StringUtils.isEmpty(everything.getId())) {
//			return;
//		}
//		
//		everything.setCpuStat(getCpuStat());
//		everything.setOsStat(getOsStat());
//		everything.setDiskStats(getDiskStats());
//		everything.setMemoryStat(getMemoryStat());
//		everything.setNetworkStats(getNetworkStats());
//		everything.setProcStats(getProcStats());
//
//		String url = String.format("%s/v1/everything", masterAddress);
//		restTemplate.put(url, everything);
//	}
//
//	private List<NetworkStat> getNetworkStats() throws SigarException {
//		String[] netIfs = sigar.getNetInterfaceList();
//		List<NetworkStat> networks = new ArrayList<NetworkStat>();
//		
//		for (String name : netIfs) {
//			NetInterfaceConfig config = sigar.getNetInterfaceConfig(name);
//			NetInterfaceStat stat = sigar.getNetInterfaceStat(name);
//			
//			if (!config.getAddress().equals("127.0.0.1") 
//					&& !config.getAddress().equals("0.0.0.0")) {
//				
//				NetworkStat network = new NetworkStat();
//				
//				network.setAddress(config.getAddress());
//				network.setName(config.getName());
//				network.setRxBytes(stat.getRxBytes());
//				network.setRxDropped(stat.getRxDropped());
//				network.setRxErrors(stat.getRxErrors());
//				network.setRxFrame(stat.getRxFrame());
//				network.setRxOverruns(stat.getRxOverruns());
//				network.setRxPackets(stat.getRxPackets());
//				network.setSpeed(stat.getSpeed());
//				network.setTxBytes(stat.getTxBytes());
//				network.setTxCarrier(stat.getTxCarrier());
//				network.setTxCollisions(stat.getTxCollisions());
//				network.setTxDropped(stat.getTxDropped());
//				network.setTxErrors(stat.getTxErrors());
//				network.setTxOverruns(stat.getTxOverruns());
//				network.setTxPackets(stat.getTxPackets());
//				
//				List<NetworkStat> ns = networkStats.stream()
//						.filter(net -> net.getName().equals(network.getName()) && net.getAddress().equals(network.getAddress()))
//						.collect(Collectors.toList());
//				
//				if (ns.size() > 0) {
//					network.setRxBytesPerSecond((network.getRxBytes() - ns.get(0).getRxBytes()) / 60);
//					network.setRxPacketsPerSecond((network.getRxPackets() - ns.get(0).getRxPackets()) / 60);
//					network.setRxErrorsPerSecond((network.getRxErrors() - ns.get(0).getRxErrors()) / 60);
//					network.setRxDroppedPerSecond((network.getRxDropped() - ns.get(0).getRxDropped()) / 60);
//					network.setRxOverrunsPerSecond((network.getRxOverruns() - ns.get(0).getRxOverruns()) / 60);
//					network.setRxFramePerSecond((network.getRxFrame() - ns.get(0).getRxFrame()) / 60);
//					network.setTxBytesPerSecond((network.getTxBytes() - ns.get(0).getTxBytes()) / 60);
//					network.setTxPacketsPerSecond((network.getTxPackets() - ns.get(0).getTxPackets()) / 60);
//					network.setTxErrorsPerSecond((network.getTxErrors() - ns.get(0).getTxErrors()) / 60);
//					network.setTxDroppedPerSecond((network.getTxDropped() - ns.get(0).getTxDropped()) / 60);
//					network.setTxOverrunsPerSecond((network.getTxOverruns() - ns.get(0).getTxOverruns()) / 60);
//					network.setTxCollisionsPerSecond((network.getTxCollisions() - ns.get(0).getTxCollisions()) / 60);
//					network.setTxCarrierPerSecond((network.getTxCarrier() - ns.get(0).getTxCarrier()) / 60);
//				}
//				
//				networks.add(network);
//			}
//		}
//		
//		networkStats.clear();
//		networkStats.addAll(networks);
//		
//		return networks;
//	}
//
//	private MemoryStat getMemoryStat() throws SigarException {
//		MemoryStat memory = new MemoryStat();
//		Mem mem = sigar.getMem();
//		memory.setActualFree(mem.getActualFree());
//		memory.setActualUsed(mem.getActualUsed());
//		memory.setFree(mem.getFree());
//		memory.setFreePercent(mem.getFreePercent());
//		memory.setRam(mem.getRam());
//		memory.setTotal(mem.getTotal());
//		memory.setUsed(mem.getUsed());
//		memory.setUsedPercent(mem.getUsedPercent());
//		return memory;
//	}
//
//	private List<DiskStat> getDiskStats() throws SigarException {
//		FileSystem[] fss = sigar.getFileSystemList();
//		
//		List<DiskStat> list = new ArrayList<DiskStat>();
//		for (FileSystem fs : fss) {
//			long avail = 0;
//			double diskQueue = 0;
//			long diskReadBytes = 0;
//			long diskReads = 0;
//			double diskServiceTime = 0;
//			long diskWriteBytes = 0;
//			long diskWrites = 0;
//			long files = 0;
//			long free = 0;
//			long freeFiles = 0;
//			long total = 0;
//			long used = 0;
//			double usePercent = 0;
//			
//			try {
//				FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
//				avail = fsu.getAvail();
//				diskQueue = fsu.getDiskQueue();
//				diskReadBytes = fsu.getDiskReadBytes();
//				diskReads = fsu.getDiskReads();
//				diskServiceTime = fsu.getDiskServiceTime();
//				diskWriteBytes = fsu.getDiskWriteBytes();
//				diskWrites = fsu.getDiskWrites();
//				files = fsu.getFiles();
//				free = fsu.getFree();
//				freeFiles = fsu.getFreeFiles();
//				total = fsu.getTotal();
//				used = fsu.getUsed();
//				usePercent = fsu.getUsePercent();
//			} catch (Exception e) {
//				//pass
//			}
//			
//			if (isAnyFs(fs.getSysTypeName(), FSS)) {
//				DiskStat disk = new DiskStat();
//				disk.setDevName(fs.getDevName());
//				disk.setDirName(fs.getDirName());
//				disk.setAvail(avail);
//				disk.setDiskQueue(diskQueue);
//				disk.setDiskReadBytes(diskReadBytes);
//				disk.setDiskReads(diskReads);
//				disk.setDiskServiceTime(diskServiceTime);
//				disk.setDiskWriteBytes(diskWriteBytes);
//				disk.setDiskWrites(diskWrites);
//				disk.setFiles(files);
//				disk.setFree(free);
//				disk.setFreeFiles(freeFiles);
//				disk.setTotal(total);
//				disk.setUsed(used);
//				disk.setUsePercent(usePercent);
//				
//				List<DiskStat> ds = diskStats.stream()
//						.filter(d -> StringUtils.equals(disk.getDevName(), d.getDevName()) && StringUtils.equals(disk.getDirName(), d.getDirName()))
//						.collect(Collectors.toList());
//				
//				if (ds.size() > 0) {
//					disk.setDiskReadsPerSecond((disk.getDiskReads() - ds.get(0).getDiskReads()) / 60);
//					disk.setDiskWritesPerSecond((disk.getDiskWrites() - ds.get(0).getDiskWrites()) / 60);
//					disk.setDiskReadBytesPerSecond((disk.getDiskReadBytes() - ds.get(0).getDiskReadBytes()) / 60);
//					disk.setDiskWriteBytesPerSecond((disk.getDiskWriteBytes() - ds.get(0).getDiskWriteBytes()) / 60);
//				}
//				
//				list.add(disk);
//			}
//		}
//		
//		diskStats.clear();
//		diskStats.addAll(list);
//		
//		return list;
//	}
//
//	private OsStat getOsStat() {
//		OperatingSystemMXBean oper = ManagementFactory.getOperatingSystemMXBean();
//		OsStat os = new OsStat();
//		os.setCpus(oper.getAvailableProcessors());
//		os.setLoad(oper.getSystemLoadAverage());
//		return os;
//	}
//
//	private CpuStat getCpuStat() throws SigarException {
//		CpuStat cpu = new CpuStat();
//		CpuPerc cpuP = sigar.getCpuPerc();
//		cpu.setCombined(cpuP.getCombined());
//		cpu.setIdle(cpuP.getCombined());
//		cpu.setIrq(cpuP.getIrq());
//		cpu.setNice(cpuP.getNice());
//		cpu.setSoftIrq(cpuP.getSoftIrq());
//		cpu.setStolen(cpuP.getStolen());
//		cpu.setSys(cpuP.getSys());
//		cpu.setUser(cpuP.getUser());
//		cpu.setWait(cpuP.getWait());
//		return cpu;
//	}
//	
//	private List<ProcStat> getProcStats() throws SigarException {
//		List<ProcStat> ps = new ArrayList<ProcStat>();
//		File f = new File(storeDirectory);
//		if (!f.exists()) {
//			return ps;
//		}
//		File procDirectory = new File(f, "proc");
//		if (!procDirectory.exists()) {
//			return ps;
//		}
//		String[] files = procDirectory.list();
//		
//		for (String file : files) {
//			if (!StringUtils.startsWith(file, ".") && StringUtils.isNumeric(file)) {
//				Long pid = Long.valueOf(file);
//				ProcStat p = composeProcStat(pid);
//				if (p != null) {
//					ps.add(p);
//				}
//			}
//		}
//		
//		return ps;
//	}
//
//	private ProcStat composeProcStat(Long pid) throws SigarException {
//		ProcStat process = new ProcStat();
//		process.setPid(pid);
//		
//		try {
//			String[] args = sigar.getProcArgs(pid);
//			process.setArgs(args);
//		} catch (SigarNotImplementedException ex) {}
//		
//		try {
//			ProcCpu cpu = sigar.getProcCpu(pid);
//			process.setCpuPercent(cpu.getPercent());
//			process.setCpuSys(cpu.getSys());
//			process.setCpuTotal(cpu.getTotal());
//			process.setCpuUser(cpu.getUser());
//		} catch (SigarNotImplementedException ex) {}
//
//		try {
//			ProcExe exe = sigar.getProcExe(pid);
//			process.setExe(exe.getName());
//		} catch (SigarNotImplementedException ex) {}
//
//		try {
//			ProcMem mem = sigar.getProcMem(pid);
//			process.setVsz(mem.getSize());
//			process.setRss(mem.getResident());
//		} catch (SigarNotImplementedException ex) {}
//
//		try {
//			ProcState state = sigar.getProcState(pid);
//			process.setThreads(state.getThreads());
//			process.setPpid(state.getPpid());
//			process.setName(state.getName());
//		} catch (SigarNotImplementedException ex) {}
//
//		return process;
//	}
//
//	private String getId() throws IOException {
//		String id = StringUtils.EMPTY;
//		
//		File f = new File(new File(storeDirectory), "id");
//		if (f.exists()) {
//			id = FileUtils.readFileToString(f, "UTF-8");
//		} else {
//			id = StringUtils.remove(UUID.randomUUID().toString(), "-");
//			FileUtils.writeByteArrayToFile(f, id.getBytes("UTF-8"));
//		}
//		
//		return id;
//	}
//}
