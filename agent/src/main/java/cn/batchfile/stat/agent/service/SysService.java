package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.agent.types.Disk;
import cn.batchfile.stat.agent.types.Memory;
import cn.batchfile.stat.agent.types.Network;
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
		os.setCpus(oper.getAvailableProcessors());
		os.setName(oper.getName());
		os.setVersion(oper.getVersion());
		return os;
	}

	private List<Proc> composeProcs(List<String> lines) {
		//Line: UID   PID  PPID   C STIME   TTY           TIME CMD
		List<Proc> ps = new ArrayList<Proc>();
		for (String line : lines) {
			String[] ary = StringUtils.split(line);
			if (ary == null || ary.length < 2 || !StringUtils.isNumeric(ary[1])) {
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
			p.setUid(uid);
			p.setPid(Long.valueOf(pid));
			p.setPpid(Long.valueOf(ppid));
			p.setStartTime(stime);
			p.setTty(tty);
			p.setCmd(StringUtils.join(cmd, " "));
			
			ps.add(p);
		}
		return ps;
	}

	public List<Disk> getDisks() {
		List<Disk> list = new ArrayList<Disk>();
		File[] roots = File.listRoots();
		for (File root : roots) {
			Disk disk = new Disk();
			disk.setDirName(root.getAbsolutePath());
			disk.setTotal(root.getTotalSpace());
			list.add(disk);
		}
		return list;
	}

	public Memory getMemory() {
		Memory memory = new Memory();
		try {
			List<String> lines = exec("free -k");
			for (String line : lines) {
				String[] ary = StringUtils.split(line, " ");
				if (ary == null || ary.length < 2 || !StringUtils.isNumeric(ary[1])) {
					continue;
				}
				
				if (StringUtils.equalsIgnoreCase(ary[0], "mem:")) {
					memory.setRam(Long.valueOf(ary[1]) * 1024);
					memory.setTotal(memory.getRam());
					break;
				}
			}
		} catch (Exception e) {
			log.error("error when get memeory info", e);
		}
		return memory;
	}

	public List<Network> getNetworks() {
		List<Network> networks = new ArrayList<Network>();
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			Enumeration<InetAddress> addresses;
			while (en.hasMoreElements()) {
				NetworkInterface networkinterface = en.nextElement();
				addresses = networkinterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement(); 
					
					Network network = new Network();
					network.setName(networkinterface.getName());
					network.setPointToPoint(networkinterface.isPointToPoint());
					network.setIndex(networkinterface.getIndex());
					network.setUp(networkinterface.isUp());
					network.setMtu(networkinterface.getMTU());
					network.setVirtual(networkinterface.isVirtual());
					network.setAddress(address.getHostAddress());
					network.setSiteLocal(address.isSiteLocalAddress());
					network.setLoopback(address.isLoopbackAddress());
					network.setLinkLocal(address.isLinkLocalAddress());
					network.setAnyLocal(address.isAnyLocalAddress());
					network.setMulticast(address.isMulticastAddress());
					
					networks.add(network);
				}
			}
		} catch (Exception e) {
			log.error("error when get network info", e);
		}
		return networks;
	}
}
