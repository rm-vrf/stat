package cn.batchfile.stat.server.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.TypeReference;

import cn.batchfile.stat.agent.domain.Cpu;
import cn.batchfile.stat.agent.domain.Everything;
import cn.batchfile.stat.agent.domain.Memory;
import cn.batchfile.stat.server.domain.CpuData;
import cn.batchfile.stat.server.domain.Disk;
import cn.batchfile.stat.server.domain.DiskData;
import cn.batchfile.stat.server.domain.Gc;
import cn.batchfile.stat.server.domain.MemoryData;
import cn.batchfile.stat.server.domain.Network;
import cn.batchfile.stat.server.domain.NetworkData;
import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.server.domain.NodeData;
import cn.batchfile.stat.server.domain.ProcessInstance;
import cn.batchfile.stat.server.domain.Stack;
import cn.batchfile.stat.server.domain.StackData;
import cn.batchfile.stat.server.service.CollectService;
import cn.batchfile.stat.server.service.CpuService;
import cn.batchfile.stat.server.service.DiskService;
import cn.batchfile.stat.server.service.GcService;
import cn.batchfile.stat.server.service.MemoryService;
import cn.batchfile.stat.server.service.NetworkService;
import cn.batchfile.stat.server.service.NodeService;
import cn.batchfile.stat.server.service.ProcessService;
import cn.batchfile.stat.server.service.StackService;
import cn.batchfile.stat.util.HttpClient;
import cn.batchfile.stat.util.JsonUtil;

public class CollectServiceImpl implements CollectService {
	private static final Logger LOG = Logger.getLogger(CollectServiceImpl.class);
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private NetworkService networkService;
	
	@Autowired
	private CpuService cpuService;
	
	@Autowired
	private DiskService diskService;

	@Autowired
	private MemoryService memoryService;
	
	@Autowired
	private ProcessService processService;
	
	@Autowired
	private StackService stackService;

	@Autowired
	private GcService gcService;
	
	@Override
	public void collectEverything() {
		LOG.debug("collect everything");
		List<Node> nodes = nodeService.getNodes();
		for (Node node : nodes) {
			NodeData nodeData = new NodeData();
			nodeData.setAgentId(node.getAgentId());
			nodeData.setTime(new Date());
			
			try {
				//fetch everything from node
				Date now = new Date();
				Everything e = get(node, "/everything", Everything.class);
				
				nodeData.setLoad(e.getOs().getLoad());
				nodeData.setAvailable(1);
				
				//update node
				node.setArchitecture(e.getOs().getArchitecture());
				node.setCpu(e.getOs().getCpu());
				node.setHostname(e.getState().getHostname());
				node.setOsName(e.getOs().getName());
				node.setOsVersion(e.getOs().getVersion());
				nodeService.updateNode(node);
				
				//collect everything else
				collect_process_data(node, e.getProcesses(), now);
				collect_cpu_data(node, e.getCpu(), now);
				collect_disk_data(node, e.getDisks(), now);
				collect_memory_data(node, e.getMemory(), now);
				collect_network_data(node, e.getNetworks(), now);
			} catch (Exception e) {
				//set unavail if cannot reach node
				nodeData.setAvailable(0);
			} finally {
				nodeService.insertNodeData(nodeData);
			}
		}
	}
	
	@Override
	public void collectGcData() {
		LOG.debug("start collect gc data");
		List<Gc> gcs = gcService.getRunningGcs();
		for (Gc gc : gcs) {
			try {
				String uri = String.format("/command/%s/_consume", gc.getCommandId());
				Node node = nodeService.getNode(gc.getAgentId());
				String out = get(node, uri);
				if (StringUtils.startsWith(out, "\"")) {
					out = JsonUtil.decode(out, String.class);
				}
				if (StringUtils.isEmpty(out) 
						&& new Date().getTime() - gc.getBeginTime().getTime() > 10000) {
					gc.setStatus("stop");
					gcService.updateGcStatus(gc);
				} else {
					gcService.insertGcData(gc.getCommandId(), gc.getAgentId(), gc.getPid(), out);
				}
			} catch (Exception e) {
				gc.setStatus("stop");
				gcService.updateGcStatus(gc);
			}
			
		}
	}

	@Override
	public void collectStackData() {
		LOG.debug("start collect stack data");
		List<Stack> stacks = stackService.getRunningStacks();
		for (Stack stack : stacks) {
			try {
				String uri = String.format("/process/%s/stack", stack.getPid());
				Node node = nodeService.getNode(stack.getAgentId());
				List<cn.batchfile.stat.agent.domain.Stack> list = get(node, uri, new TypeReference<List<cn.batchfile.stat.agent.domain.Stack>>(){});
				int count = list == null ? 0 : list.size();
				if (count == 0) {
					stack.setStatus("stop");
					stackService.updateStackStatus(stack);
				} else {
					StackData sd = new StackData();
					sd.setCommandId(stack.getCommandId());
					sd.setTime(new Date());
					sd.setPid(stack.getPid());
					sd.setAgentId(stack.getAgentId());
					sd.setCount(count);
					sd.setStacks(JsonUtil.encode(list));
					stackService.insertStackData(sd);
				}
			} catch (Exception e) {
				stack.setStatus("stop");
				stackService.updateStackStatus(stack);
			}
		}
	}
	
	public void collect_process_data(Node node, List<cn.batchfile.stat.agent.domain.Process> ps, Date time) {
		List<cn.batchfile.stat.server.domain.Process> process_list = processService.getProcessByAgentId(node.getAgentId());
		for (cn.batchfile.stat.server.domain.Process process : process_list) {
			List<ProcessInstance> instances = processService.getRunningInstance(ps, process, time);
			process.setRunningInstance(instances.size());
			processService.updateRunningInstance(process);
		}
	}

	public void collect_cpu_data(Node node, Cpu cpu, Date time) {
		CpuData cd = new CpuData();
		cd.setAgentId(node.getAgentId());
		cd.setTime(time);
		cd.setCombined(cpu.getCombined());
		cd.setIdle(cpu.getIdle());
		cd.setIrq(cpu.getIrq());
		cd.setNice(cpu.getNice());
		cd.setSoftIrq(cpu.getSoftIrq());
		cd.setStolen(cpu.getStolen());
		cd.setSys(cpu.getSys());
		cd.setUser(cpu.getUser());
		cd.setWait(cpu.getWait());
		cpuService.insertCpuData(cd);
	}

	public void collect_disk_data(Node node, List<cn.batchfile.stat.agent.domain.Disk> ds, Date time) {
		for (cn.batchfile.stat.agent.domain.Disk d : ds) {
			Disk disk = new Disk();
			disk.setAgentId(node.getAgentId());
			disk.setDirName(d.getDirName());
			disk.setDevName(d.getDevName());
			disk.setType(d.getType());
			disk.setTypeName(d.getTypeName());
			disk.setSysTypeName(d.getSysTypeName());
			disk.setOptions(d.getOptions());
			disk.setFlags(d.getFlags());
			diskService.insertDisk(disk);
			
			DiskData diskData = new DiskData();
			diskData.setAgentId(node.getAgentId());
			diskData.setDirName(d.getDirName());
			diskData.setTime(time);
			diskData.setTotal(d.getTotal());
			diskData.setFree(d.getFree());
			diskData.setUsed(d.getUsed());
			diskData.setAvail(d.getAvail());
			diskData.setFiles(d.getFiles());
			diskData.setFreeFiles(d.getFreeFiles());
			diskData.setDiskReads(d.getDiskReads());
			diskData.setDiskWrites(d.getDiskWrites());
			diskData.setDiskReadBytes(d.getDiskReadBytes());
			diskData.setDiskWriteBytes(d.getDiskWriteBytes());
			diskData.setDiskQueue(d.getDiskQueue());
			diskData.setDiskServiceTime(d.getDiskServiceTime());
			diskData.setUsePercent(d.getUsePercent());
			diskService.insertDiskData(diskData);
		}
	}

	public void collect_memory_data(Node node, Memory memory, Date time) {
		MemoryData md = new MemoryData();
		md.setAgentId(node.getAgentId());
		md.setTime(time);
		md.setActualFree(memory.getActualFree());
		md.setActualUsed(memory.getActualUsed());
		md.setFree(memory.getFree());
		md.setFreePercent(memory.getFreePercent());
		md.setRam(memory.getRam());
		md.setTotal(memory.getTotal());
		md.setUsed(memory.getUsed());
		md.setUsedPercent(memory.getUsedPercent());
		memoryService.insertMemoryData(md);
	}

	public void collect_network_data(Node node, List<cn.batchfile.stat.agent.domain.Network> ns, Date time) {
		for (cn.batchfile.stat.agent.domain.Network n : ns) {
			Network network = new Network();
			network.setAgentId(node.getAgentId());
			network.setAddress(n.getAddress());
			network.setBroadcast(n.getBroadcast());
			network.setDescription(n.getDescription());
			network.setDestination(n.getDestination());
			network.setFlags(n.getFlags());
			network.setHwaddr(n.getHwaddr());
			network.setMetric(n.getMetric());
			network.setMtu(n.getMtu());
			network.setName(n.getName());
			network.setNetmask(n.getNetmask());
			network.setType(n.getType());
			networkService.insertNetwork(network);
			
			NetworkData networkData = new NetworkData();
			networkData.setAgentId(node.getAgentId());
			networkData.setAddress(n.getAddress());
			networkData.setTime(time);
			networkData.setRxBytes(n.getRxBytes());
			networkData.setRxDropped(n.getRxDropped());
			networkData.setRxErrors(n.getRxErrors());
			networkData.setRxFrame(n.getRxFrame());
			networkData.setRxOverruns(n.getRxOverruns());
			networkData.setRxPackets(n.getRxPackets());
			networkData.setSpeed(n.getSpeed());
			networkData.setTxBytes(n.getTxBytes());
			networkData.setTxCarrier(n.getTxCarrier());
			networkData.setTxCollisions(n.getTxCollisions());
			networkData.setTxDropped(n.getTxDropped());
			networkData.setTxErrors(n.getTxErrors());
			networkData.setTxOverruns(n.getTxOverruns());
			networkData.setTxPackets(n.getTxPackets());
			networkService.insertNetworkData(networkData);
		}
	}

	private <T> T get(Node node, String uri, Class<T> clazz) {
		String json = get(node, uri);
		if (StringUtils.isEmpty(json)) {
			return null;
		} else {
			return JsonUtil.decode(json, clazz);
		}
	}
	
	private <T> T get(Node node, String uri, TypeReference<T> typeRef) {
		String json = get(node, uri);
		if (StringUtils.isEmpty(json)) {
			return null;
		} else {
			return JsonUtil.decode(json, typeRef);
		}
	}
	
	private String get(Node node, String uri) {
		String url = String.format("%s://%s:%s%s", node.getSchema(), node.getAddress(), node.getPort(), uri);
		HttpClient hc = new HttpClient();
		hc.setContentType("application/json");
		hc.setCharset("utf-8");
		hc.setConnectionTimeout(1000);
		hc.setReadTimeout(10000);
		return hc.get(url);
	}

}
