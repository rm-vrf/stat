package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.domain.Disk;
import cn.batchfile.stat.domain.Memory;
import cn.batchfile.stat.domain.Network;
import cn.batchfile.stat.domain.Os;
import cn.batchfile.stat.domain.Proc;

@Service
public class SysService {
	protected static final Logger log = LoggerFactory.getLogger(SysService.class);
	private Sigar sigar;
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	@PostConstruct
	public void init() throws Exception {
		//TODO copy lib into data path
		String libPath = System.getProperty("java.library.path");
		log.info("lib path: {}", libPath);
		
		//TODO append path, not change
		File sigarPath = new File(new File(storeDirectory), "sigar");
		System.setProperty("java.library.path", sigarPath.getAbsolutePath());
		
		//set sys_paths to null
		final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		sysPathsField.setAccessible(true);
		sysPathsField.set(null, null);
		
		sigar = new Sigar();
	}
	
	public List<Proc> ps() throws SigarException {
		List<Proc> ps = new ArrayList<Proc>();
		long[] pids = sigar.getProcList();
		for (long pid : pids) {
			Proc p = ps(pid);
			if (p != null) {
				ps.add(p);
			}
		}
		return ps;
	}
	
	public Proc ps(long pid) {
		//ppid
		long ppid = 0;
		try {
			ProcState state = sigar.getProcState(pid);
			ppid = state.getPpid();
		} catch (Exception e) {
			//pass
		}
		
		//start time
		String startTime = StringUtils.EMPTY;
		try {
			ProcCpu cpu = sigar.getProcCpu(pid);
			startTime = ProcService.TIME_FORMAT.get().format(new Date(cpu.getStartTime()));
		} catch (Exception e) {
			//pass
		}
		
		//user
		String uid = StringUtils.EMPTY;
		try {
			ProcCredName credName = sigar.getProcCredName(pid);
			uid = credName.getUser();
		} catch (Exception e) {
			//pass
		}
		
		Proc p = new Proc();
		p.setPid(pid);
		p.setPpid(ppid);
		p.setStartTime(startTime);
		p.setUid(uid);
		
		return p.getPpid() > 0 
				&& StringUtils.isNotEmpty(startTime) 
				&& StringUtils.isNotEmpty(uid) ? p : null;
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
	
	public List<String> exec(String cmd) {
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

	public List<Disk> getDisks() throws SigarException {
		List<Disk> disks = new ArrayList<Disk>();
		
		FileSystem[] fss = sigar.getFileSystemList();
		for (FileSystem fs : fss) {
			Disk disk = new Disk();
			FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
			
			disk.setDevName(fs.getDevName());
			disk.setDirName(fs.getDirName());
			disk.setFlags(fs.getFlags());
			disk.setOption(fs.getOptions());
			disk.setSysTypeName(fs.getSysTypeName());
			disk.setTotal(fsu.getTotal() * 1024);
			disk.setType(fs.getType());
			disk.setTypeName(fs.getTypeName());
			
			if (disk.getTotal() > 0 
					&& containsAny(disk.getSysTypeName(), new String[] {"ext", "apfs", "xfs", "fat", "ntfs"})) {
				disks.add(disk);
			}
		}
		return disks;
	}

	public Memory getMemory() throws SigarException {
		Memory memory = new Memory();
		Mem mem = sigar.getMem();
		memory.setRam(mem.getRam() * 1024 * 1024);
		memory.setTotal(mem.getTotal());
		return memory;
	}

	public List<Network> getNetworks() throws SigarException {
		List<Network> networks = new ArrayList<Network>();
		
		String[] netIfs = sigar.getNetInterfaceList();
		for (String netIf : netIfs) {
			NetInterfaceConfig config = sigar.getNetInterfaceConfig(netIf);
			//NetInterfaceStat stat = sigar.getNetInterfaceStat(netIf);
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
				
			if (!network.getAddress().equals("127.0.0.1") 
						&& !network.getAddress().equals("0.0.0.0")) {
				networks.add(network);
			}
		}
		
		return networks;
	}

	private boolean containsAny(String s, String[] searchStrs) {
		for (String con : searchStrs) {
			if (StringUtils.containsIgnoreCase(s, con)) {
				return true;
			}
		}
		return false;
	}

}
