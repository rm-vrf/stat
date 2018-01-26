package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import cn.batchfile.stat.agent.types.Disk;
import cn.batchfile.stat.agent.types.Memory;
import cn.batchfile.stat.agent.types.Network;
import cn.batchfile.stat.agent.types.Node;
import cn.batchfile.stat.agent.types.Os;

@Service
public class NodeService {
	protected static final Logger log = LoggerFactory.getLogger(NodeService.class); 
	
	private Sigar sigar;
	private Node node;

	@Value("${store.directory}")
	private String storeDirectory;
	
	@PostConstruct
	public void init() throws SigarException, IOException {
		sigar = new Sigar();
		node = new Node();

		node.setId(getId());
		node.setDisks(getDisks());
		node.setMemory(getMemory());
		node.setNetworks(getNetworks());
		node.setOs(getOs());

		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
	}
	
	public Node getNode() {
		return node;
	}
	
	public List<String> getTags() throws IOException {
		List<String> tags = new ArrayList<String>();
		File f = new File(new File(storeDirectory), "tag");
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				tags = JSON.parseArray(s, String.class);
			}
		}
		return tags;
	}
	
	public void putTags(List<String> tags) throws UnsupportedEncodingException, IOException {
		String s = JSON.toJSONString(tags);
		File f = new File(new File(storeDirectory), "tag");
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public Map<String, String> getEnvs() throws IOException {
		Map<String, String> envs = new HashMap<String, String>();
		File f = new File(new File(storeDirectory), "env");
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				envs = JSON.parseObject(s, new TypeReference<Map<String, String>>(){});
			}
		}
		return envs;
	}
	
	public void putEnvs(Map<String, String> envs) throws UnsupportedEncodingException, IOException {
		String s = JSON.toJSONString(envs);
		File f = new File(new File(storeDirectory), "env");
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	private String getId() throws IOException {
		String id = StringUtils.EMPTY;
		
		File f = new File(new File(storeDirectory), "id");
		if (f.exists()) {
			id = FileUtils.readFileToString(f, "UTF-8");
		} else {
			id = StringUtils.remove(UUID.randomUUID().toString(), "-");
			FileUtils.writeByteArrayToFile(f, id.getBytes("UTF-8"));
		}
		
		return id;
	}
	
	private Os getOs() {
		OperatingSystemMXBean oper = ManagementFactory.getOperatingSystemMXBean();
		Os os = new Os();
		os.setArchitecture(oper.getArch());
		os.setCpu(oper.getAvailableProcessors());
		os.setName(oper.getName());
		os.setVersion(oper.getVersion());
		return os;
	}
	
	private List<Disk> getDisks() throws SigarException {
		FileSystem[] fss = sigar.getFileSystemList();
		List<Disk> list = new ArrayList<Disk>();
		for (FileSystem fs : fss) {
			FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
			Disk disk = new Disk();
			disk.setDevName(fs.getDevName());
			disk.setDirName(fs.getDirName());
			disk.setOptions(fs.getOptions());
			disk.setSysTypeName(fs.getSysTypeName());
			disk.setTotal(fsu.getTotal());
			disk.setType(fs.getType());
			disk.setTypeName(fs.getTypeName());
			list.add(disk);
		}
		return list;
	}

	private Memory getMemory() throws SigarException {
		Memory memory = new Memory();
		Mem mem = sigar.getMem();
		memory.setRam(mem.getRam());
		memory.setTotal(mem.getTotal());
	
		return memory;
	}

	private List<Network> getNetworks() throws SigarException {
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
				network.setHwaddr(config.getHwaddr());
				network.setMtu(config.getMtu());
				network.setName(config.getName());
				network.setNetmask(config.getNetmask());
				network.setType(config.getType());
			
				networks.add(network);
			}
		}
		return networks;
	}
}
