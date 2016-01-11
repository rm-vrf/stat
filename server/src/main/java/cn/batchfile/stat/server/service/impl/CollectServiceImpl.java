package cn.batchfile.stat.server.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.TypeReference;

import cn.batchfile.stat.agent.domain.Cpu;
import cn.batchfile.stat.agent.domain.Everything;
import cn.batchfile.stat.agent.domain.Memory;
import cn.batchfile.stat.agent.domain.Process;
import cn.batchfile.stat.server.domain.CpuData;
import cn.batchfile.stat.server.domain.Disk;
import cn.batchfile.stat.server.domain.DiskData;
import cn.batchfile.stat.server.domain.Gc;
import cn.batchfile.stat.server.domain.MemoryData;
import cn.batchfile.stat.server.domain.Network;
import cn.batchfile.stat.server.domain.NetworkData;
import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.server.domain.NodeData;
import cn.batchfile.stat.server.domain.ProcessData;
import cn.batchfile.stat.server.domain.ProcessInstance;
import cn.batchfile.stat.server.domain.ProcessMonitor;
import cn.batchfile.stat.server.domain.Stack;
import cn.batchfile.stat.server.domain.StackData;
import cn.batchfile.stat.server.service.CollectService;
import cn.batchfile.stat.server.service.ConfigService;
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
import cn.batchfile.stat.util.PathUtils;

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
	
	@Autowired
	private ConfigService configService;
	
	@Override
	public void collectProcess() {
		LOG.info("collect process data, begin");
		List<Node> nodes = nodeService.getEnableNodes();
		List<ProcessMonitor> monitors = processService.getEnabledMonitors();
		for (ProcessMonitor monitor : monitors) {
			int instance_count = 0;
			for (Node node : nodes) {
				LOG.info(String.format("collect process data, monitor: %s, ip: %s", monitor.getName(), node.getAddress()));
				
				//上一次采集的进程列表
				List<ProcessInstance> runningInstances = processService.getInstancesByAgentMonitorStatus(node.getAgentId(), monitor.getName(), "running");
				
				try {
					//获得运行中的进程
					Date now = new Date();
					String uri = String.format("/process?query=%s", PathUtils.encodeUrl(monitor.getQuery()));
					List<Process> ps = get(node, uri, new TypeReference<List<Process>>() {});
					instance_count += ps.size();
	
					//处理每一个采集进程
					for (Process p : ps) {
						ProcessInstance instance = compose_process_instance(p, node, monitor, now, runningInstances);
						if (StringUtils.isEmpty(instance.getInstanceId())) {
							//没有进程ID，新进程
							instance.setInstanceId(UUID.randomUUID().toString().replaceAll("-", ""));
							processService.insertInstance(instance);
							LOG.info(String.format("new process of: %s, on: %s, pid: %s", monitor.getName(), node.getAddress(), p.getPid()));
						}
						
						//进程的运行数据
						ProcessData data = new ProcessData();
						data.setInstanceId(instance.getInstanceId());
						data.setTime(now);
						data.setAgentId(node.getAgentId());
						data.setName(monitor.getName());
						data.setPid(p.getPid());
						data.setCpuPercent(p.getCpuPercent());
						data.setThreads(p.getThreads());
						data.setVsz(p.getVsz());
						data.setRss(p.getRss());
						data.setCpuSys(p.getCpuSys());
						data.setCpuTotal(p.getCpuTotal());
						data.setCpuUser(p.getCpuUser());
						processService.insertData(data);
					}
					
					//设置停止进程的状态, 两次采集的进程号，停止的进程设置stop状态
					for (ProcessInstance runningInstance : runningInstances) {
						if (!pid_exist(runningInstance.getPid(), ps)) {
							processService.updateInstanceStatus(runningInstance.getInstanceId(), "stop");
							LOG.info(String.format("process stop, name: %s, node: %s, pid: %s", monitor.getName(), node.getAddress(), runningInstance.getPid()));
						}
					}
				} catch (Exception e) {
					//pass
				}
			}
			
			//update instance count
			processService.updateMonitorInstanceCount(monitor.getName(), instance_count);
			LOG.info(String.format("instance on all nodes: %s", instance_count));
		}
	}
	
	@Override
	public void collectEverything() {
		LOG.info("collect everything, begin");
		List<Node> nodes = nodeService.getEnableNodes();
		Date now = new Date();
		
		for (Node node : nodes) {
			NodeData nodeData = new NodeData();
			nodeData.setAgentId(node.getAgentId());
			nodeData.setTime(now);
			
			try {
				//fetch everything from node
				LOG.info(String.format("collect everything, ip: %s", node.getAddress()));
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
				collect_cpu_data(node, e.getCpu(), now);
				collect_disk_data(node, e.getDisks(), now);
				collect_memory_data(node, e.getMemory(), now);
				collect_network_data(node, e.getNetworks(), now);
			} catch (Exception e) {
				//set unavail if cannot reach node
				nodeData.setAvailable(0);
			} finally {
				nodeService.insertData(nodeData);
			}
		}
		configService.setDate("collect.time", now);
		LOG.info("collect everything, end");
	}
	
	@Override
	public void collectGc() {
		LOG.debug("start collect gc data");
		List<Gc> gcs = gcService.getRunningGcs();
		for (Gc gc : gcs) {
			try {
				Date now = new Date();
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
					gcService.insertData(gc.getCommandId(), gc.getAgentId(), gc.getPid(), now, out);
				}
			} catch (Exception e) {
				gc.setStatus("stop");
				gcService.updateGcStatus(gc);
			}
			
		}
	}

	@Override
	public void collectStack() {
		LOG.debug("start collect stack data");
		List<Stack> stacks = stackService.getRunningStacks();
		for (Stack stack : stacks) {
			try {
				Date now = new Date();
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
					sd.setTime(now);
					sd.setPid(stack.getPid());
					sd.setAgentId(stack.getAgentId());
					sd.setCount(count);
					sd.setStacks(JsonUtil.encode(list));
					stackService.insertData(sd);
				}
			} catch (Exception e) {
				stack.setStatus("stop");
				stackService.updateStackStatus(stack);
			}
		}
	}
	
	private ProcessInstance compose_process_instance(Process p, Node node, ProcessMonitor monitor, Date time, List<ProcessInstance> instances) {
		ProcessInstance instance = new ProcessInstance();
		instance.setInstanceId(find_instance_id(instances, node.getAgentId(), p.getPid(), "running"));
		instance.setAgentId(node.getAgentId());
		instance.setPid(p.getPid());
		instance.setPpid(p.getPpid());
		instance.setStatus("running");
		instance.setName(p.getName());
		//instance.setDeployment(StringUtils.EMPTY);
		instance.setMonitor(monitor.getName());
		instance.setUser(p.getUser());
		instance.setGroup(p.getGroup());
		instance.setWorkDirectory(p.getWorkDirectory());
		instance.setStartTime(p.getStartTime());
		instance.setCommand(p.getExe());
		instance.setArgs(StringUtils.join(p.getArgs(), " "));
		instance.setJavaArgs(p.getJavaArgs());
		return instance;
	}

	private String find_instance_id(List<ProcessInstance> instances, String agentId, long pid, String status) {
		for (ProcessInstance instance : instances) {
			if (StringUtils.equals(agentId, instance.getAgentId()) 
					&& pid == instance.getPid() 
					&& StringUtils.equals(status, instance.getStatus())) {
				return instance.getInstanceId();
			}
		}
		return null;
	}

	private boolean pid_exist(long pid, List<Process> ps) {
		for (Process p : ps) {
			if (pid == p.getPid()) {
				return true;
			}
		}
		return false;
	}
	
	private void collect_cpu_data(Node node, Cpu cpu, Date time) {
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
		cpuService.insertData(cd);
	}

	private void collect_disk_data(Node node, List<cn.batchfile.stat.agent.domain.Disk> ds, Date time) {
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
			diskService.insertData(diskData);
		}
	}

	private void collect_memory_data(Node node, Memory memory, Date time) {
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
		memoryService.insertData(md);
	}

	private void collect_network_data(Node node, List<cn.batchfile.stat.agent.domain.Network> ns, Date time) {
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
			networkService.insertData(networkData);
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
