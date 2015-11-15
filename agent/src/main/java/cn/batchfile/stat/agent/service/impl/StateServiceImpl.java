package cn.batchfile.stat.agent.service.impl;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import cn.batchfile.stat.agent.Main;
import cn.batchfile.stat.agent.domain.Cpu;
import cn.batchfile.stat.agent.domain.Disk;
import cn.batchfile.stat.agent.domain.Memory;
import cn.batchfile.stat.agent.domain.Network;
import cn.batchfile.stat.agent.domain.Os;
import cn.batchfile.stat.agent.domain.State;
import cn.batchfile.stat.agent.service.StateService;

public class StateServiceImpl implements StateService {
	private State state;
	
	public void init() throws IOException {
		state = new State();
		state.setAddress(StringUtils.isEmpty(Main.address) ? get_address() : Main.address);
		state.setAgentId(Main.read_config().getProperty("agent.id").toString());
		state.setHostname(get_host());
		state.setPort(Main.port);
		state.setStartTime(new Date());
	}
	
	@Override
	public State getState() {
		return state;
	}

	@Override
	public Os getOs() {
		OperatingSystemMXBean oper = ManagementFactory.getOperatingSystemMXBean();
		Os os = new Os();
		os.setArchitecture(oper.getArch());
		os.setCpu(oper.getAvailableProcessors());
		os.setLoad(oper.getSystemLoadAverage());
		os.setName(oper.getName());
		os.setVersion(oper.getVersion());
		return os;
	}
	
	@Override
	public Cpu getCpu() throws SigarException {
		add_library_path("./sigar");
		Sigar sigar = new Sigar();
		Cpu cpu = new Cpu();
		
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
		
		return cpu;
	}
	
	@Override
	public List<Disk> getDisks() throws SigarException {
		add_library_path("./sigar");
		Sigar sigar = new Sigar();
		FileSystem[] fss = sigar.getFileSystemList();
		
		List<Disk> list = new ArrayList<Disk>();
		for (FileSystem fs : fss) {
			FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
			if (fs.getType() == 2) {
				Disk disk = new Disk();
				disk.setAvail(fsu.getAvail());
				disk.setDevName(fs.getDevName());
				disk.setDirName(fs.getDirName());
				disk.setDiskQueue(fsu.getDiskQueue());
				disk.setDiskReadBytes(fsu.getDiskReadBytes());
				disk.setDiskReads(fsu.getDiskReads());
				disk.setDiskServiceTime(fsu.getDiskServiceTime());
				disk.setDiskWriteBytes(fsu.getDiskWriteBytes());
				disk.setDiskWrites(fsu.getDiskWrites());
				disk.setFiles(fsu.getFiles());
				disk.setFlags(fs.getFlags());
				disk.setFree(fsu.getFree());
				disk.setFreeFiles(fsu.getFreeFiles());
				disk.setOptions(fs.getOptions());
				disk.setSysTypeName(fs.getSysTypeName());
				disk.setTotal(fsu.getTotal());
				disk.setType(fs.getType());
				disk.setTypeName(fs.getTypeName());
				disk.setUsed(fsu.getUsed());
				disk.setUsePercent(fsu.getUsePercent());
				list.add(disk);
			}
		}
		return list;
	}
	
	@Override
	public Memory getMemory() throws SigarException {
		add_library_path("./sigar");
		Sigar sigar = new Sigar();

		Memory memory = new Memory();
		Mem mem = sigar.getMem();
		memory.setActualFree(mem.getActualFree());
		memory.setActualUsed(mem.getActualUsed());
		memory.setFree(mem.getFree());
		memory.setFreePercent(mem.getFreePercent());
		memory.setRam(mem.getRam());
		memory.setTotal(mem.getTotal());
		memory.setUsed(mem.getUsed());
		memory.setUsedPercent(mem.getUsedPercent());
		
		return memory;
	}
	
	@Override
	public List<Network> getNetworks() throws SigarException {
		add_library_path("./sigar");
		Sigar sigar = new Sigar();
		
		String[] netIfs = sigar.getNetInterfaceList();
		List<Network> networks = new ArrayList<Network>();
		
		for (String name : netIfs) {
			NetInterfaceConfig config = sigar.getNetInterfaceConfig(name);
			NetInterfaceStat stat = sigar.getNetInterfaceStat(name);
			if (stat.getRxBytes() > 0 
					&& !config.getAddress().equals("127.0.0.1") 
					&& !config.getAddress().equals("0.0.0.0")) {
				Network network = new Network();
				network.setAddress(config.getAddress());
				network.setBroadcast(config.getBroadcast());
				network.setDescription(config.getDescription());
				network.setDestination(config.getDestination());
				network.setFlags(config.getFlags());
				network.setHwaddr(config.getHwaddr());
				network.setMetric(config.getMetric());
				network.setMtu(config.getMtu());
				network.setName(config.getName());
				network.setNetmask(config.getNetmask());
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
				network.setType(config.getType());
				
				networks.add(network);
			}
		}
		return networks;
	}
	
	private String get_host() throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getLocalHost();
		String host = inetAddress.getHostName();
		return host;
	}
	
	private String get_address() throws SocketException {
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
			Enumeration<InetAddress> nias = ni.getInetAddresses();
			while (nias.hasMoreElements()) {
				InetAddress ia = (InetAddress) nias.nextElement();
				if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()) {
					String address = ia.getHostAddress();
					if (StringUtils.isNotEmpty(address)) {
						return address;
					}
				}
			}
		}
		return null;
	}

	private void add_library_path(String path) {
		System.setProperty("java.library.path", path);
		String vm = System.getProperty("java.vm.name");
		if (StringUtils.containsIgnoreCase(vm, "openjdk") 
				|| StringUtils.containsIgnoreCase(vm, "hotspot")) {
			try {
				//检查usr_field变量
				Field usr_field = ClassLoader.class.getDeclaredField("usr_paths");
				usr_field.setAccessible(true);
				String[] usr_paths = (String[]) usr_field.get(null);
				for (int i = 0; i < usr_paths.length; i++) {
					if (path.equals(usr_paths[i])) {
						return;
					}
				}
				
				//检查sys_field变量
				Field sys_field = ClassLoader.class.getDeclaredField("sys_paths");
				sys_field.setAccessible(true);
				String[] sys_paths = (String[]) sys_field.get(null);
				for (int i = 0; i < sys_paths.length; i++) {
					if (path.equals(sys_paths[i])) {
						return;
					}
				}
				
				//设置usr_paths，加上新变量
				String[] tmp = new String[usr_paths.length + 1];
				System.arraycopy(usr_paths, 0, tmp, 0, usr_paths.length);
				tmp[usr_paths.length] = path;
				usr_field.set(null, tmp);
				
				//sys_fields设置为空，jdk会自动初始化这个变量
				sys_field.set(null, null);
			} catch (IllegalAccessException e) {
				//do nothing
			} catch (NoSuchFieldException e) {
				//do nothing
			}
		}
	}
}
