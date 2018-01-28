package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.agent.types.Os;
import cn.batchfile.stat.agent.types.Proc;

@Service
public class SysService {
	protected static final Logger log = LoggerFactory.getLogger(SysService.class);
	
	@Value("${ps.cmd:ps -ef {pid}}")
	private String psCmd;

	@Value("${kill.cmd:kill -{signal} {pid}}")
	private String killCmd;
	
	@PostConstruct
	public void init() {
	}
	
	public List<Proc> ps() {
		String cmd = StringUtils.replaceEach(psCmd, 
				new String[] {"{pid}"}, 
				new String[] {StringUtils.EMPTY});
		List<String> lines = exec(cmd);
		List<Proc> ps = composeProcs(lines);
		return ps;
	}
	
	public Proc ps(long pid) {
		String cmd = StringUtils.replaceEach(psCmd, 
				new String[] {"{pid}"}, 
				new String[] {String.valueOf(pid)});
		List<String> lines = exec(cmd);
		List<Proc> ps = composeProcs(lines);
		return ps == null || ps.size() == 0 ? null : ps.get(0);
	}
	
	public void kill(long pid, int signal) {
		String cmd = StringUtils.replaceEach(killCmd, 
				new String[] {"{signal}", "{pid}"}, 
				new String[] {String.valueOf(signal), String.valueOf(pid)});
		
		exec(cmd);
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
		os.setCpu(oper.getAvailableProcessors());
		os.setName(oper.getName());
		os.setVersion(oper.getVersion());
		return os;
	}

	private List<Proc> composeProcs(List<String> lines) {
		//Line: UID   PID  PPID   C STIME   TTY           TIME CMD
		List<Proc> ps = new ArrayList<Proc>();
		for (String line : lines) {
			String[] ary = StringUtils.split(line);
			if (!StringUtils.isNumeric(ary[1])) {
				continue;
			}
			
			Proc p = new Proc();
			String uid = ary[0];
			String pid = ary[1];
			String ppid = ary[2];
			//String c = ary[3];
			String stime = ary[4];
			String tty = ary[5];
			//String time = ary[6];
			List<String> cmd = new ArrayList<String>();
			for (int i = 7; i < ary.length; i ++) {
				cmd.add(ary[i]);
			}
			p.setUid(Long.valueOf(uid));
			p.setPid(Long.valueOf(pid));
			p.setPpid(Long.valueOf(ppid));
			p.setStartTime(stime);
			p.setTty(tty);
			p.setCmd(StringUtils.join(cmd, " "));
			
			ps.add(p);
		}
		return ps;
	}

//	public List<Disk> getDisks() throws SigarException {
//		FileSystem[] fss = sigar.getFileSystemList();
//		List<Disk> list = new ArrayList<Disk>();
//		for (FileSystem fs : fss) {
//			FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
//			Disk disk = new Disk();
//			disk.setDevName(fs.getDevName());
//			disk.setDirName(fs.getDirName());
//			disk.setOptions(fs.getOptions());
//			disk.setSysTypeName(fs.getSysTypeName());
//			disk.setTotal(fsu.getTotal());
//			disk.setType(fs.getType());
//			disk.setTypeName(fs.getTypeName());
//			list.add(disk);
//		}
//		return list;
//	}

//	public Memory getMemory() throws SigarException {
//		Memory memory = new Memory();
//		Mem mem = sigar.getMem();
//		memory.setRam(mem.getRam());
//		memory.setTotal(mem.getTotal());
//	
//		return memory;
//	}

//	public List<Network> getNetworks() throws SigarException {
//		String[] netIfs = sigar.getNetInterfaceList();
//		List<Network> networks = new ArrayList<Network>();
//	
//		for (String name : netIfs) {
//			NetInterfaceConfig config = sigar.getNetInterfaceConfig(name);
//			NetInterfaceStat stat = sigar.getNetInterfaceStat(name);
//			if (stat.getRxBytes() > 0 
//					&& !config.getAddress().equals("127.0.0.1") 
//					&& !config.getAddress().equals("0.0.0.0")) {
//				Network network = new Network();
//				network.setAddress(config.getAddress());
//				network.setBroadcast(config.getBroadcast());
//				network.setDescription(config.getDescription());
//				network.setDestination(config.getDestination());
//				network.setHwaddr(config.getHwaddr());
//				network.setMtu(config.getMtu());
//				network.setName(config.getName());
//				network.setNetmask(config.getNetmask());
//				network.setType(config.getType());
//			
//				networks.add(network);
//			}
//		}
//		return networks;
//	}
}
