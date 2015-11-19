package cn.batchfile.stat.server.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.TypeReference;

import cn.batchfile.stat.agent.domain.Cpu;
import cn.batchfile.stat.agent.domain.Memory;
import cn.batchfile.stat.agent.domain.Network;
import cn.batchfile.stat.agent.domain.Os;
import cn.batchfile.stat.agent.domain.Process;
import cn.batchfile.stat.agent.domain.State;
import cn.batchfile.stat.server.domain.CpuData;
import cn.batchfile.stat.server.domain.Disk;
import cn.batchfile.stat.server.domain.DiskData;
import cn.batchfile.stat.server.domain.Gc;
import cn.batchfile.stat.server.domain.MemoryData;
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
	public void collectProcessData() {
		LOG.debug("start collect process data");
		List<Node> nodes = nodeService.getNodes();
		for (Node node : nodes) {
			try {
				List<Process> ps = get(node, "/process", new TypeReference<List<Process>>() {});
				List<cn.batchfile.stat.server.domain.Process> process_list = processService.getProcessByAgentId(node.getAgentId());
				for (cn.batchfile.stat.server.domain.Process process : process_list) {
					List<ProcessInstance> instances = processService.getRunningInstance(ps, process);
					process.setRunningInstance(instances.size());
					processService.updateRunningInstance(process);
				}
			} catch (Exception e) {
				//pass
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
				String out = get(node, uri, String.class);
				if (StringUtils.isEmpty(out) 
						&& new Date().getTime() - gc.getBeginTime().getTime() > 10000) {
					gc.setStatus("stop");
					gcService.updateGcStatus(gc);
				} else {
					gcService.insertGcData(gc.getCommandId(), gc.getAgentId(), gc.getPid(), out);
				}
			} catch (Exception e) {
				//pass
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
					sd.setStacks(list);
					stack.insertStackData();
				}
			} catch (Exception e) {
				//pass
			}
		}
	}
	
	@Override
	public void collectCpuData() {
		LOG.debug("start collect cpu data");
		List<Node> nodes = nodeService.getNodes();
		for (Node node : nodes) {
			try {
				Cpu cpu = get(node, "/cpu", Cpu.class);
				CpuData cd = new CpuData();
				cd.setAgentId(node.getAgentId());
				cd.setTime(new Date());
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
			} catch (Exception e) {
				//pass
			}
		}
	}

	@Override
	public void collectNodeData() {
		LOG.debug("collect node data");
		List<Node> nodes = nodeService.getNodes();
		for (Node node : nodes) {
			NodeData nd = new NodeData();
			nd.setAgentId(node.getAgentId());
			nd.setTime(new Date());
			try {
				State state = get(node, "/state", State.class);
				Os os = get(node, "/os", Os.class);
				
				node.setArchitecture(os.getArchitecture());
				node.setCpu(os.getCpu());
				node.setHostname(state.getHostname());
				node.setOsName(os.getName());
				node.setOsVersion(os.getVersion());

				nd.setLoad(os.getLoad());
				nd.setAvailable(1);

				nodeService.updateNode(node);
			} catch (Exception e) {
				nd.setAvailable(0);
			} finally {
				nodeService.insertNodeData(nd);
			}
		}
	}

	@Override
	public void collectDiskData() {
		LOG.debug("collect disk data");
		List<Node> nodes = nodeService.getNodes();
		for (Node node : nodes) {
			try {
				Disk disk = new Disk();
				DiskData diskData = new DiskData();
				List<cn.batchfile.stat.agent.domain.Disk> ds = get(node, "/disk", new TypeReference<List<cn.batchfile.stat.agent.domain.Disk>>() {});
				for (cn.batchfile.stat.agent.domain.Disk d : ds) {
					disk.setAgentId(node.getAgentId());
					disk.setDirName(d.getDirName());
					disk.setDevName(d.getDevName());
					disk.setType(d.getType());
					disk.setTypeName(d.getTypeName());
					disk.setSysTypeName(d.getSysTypeName());
					disk.setOptions(d.getOptions());
					disk.setFlags(d.getFlags());
					
					diskData.setAgentId(node.getAgentId());
					diskData.setDirName(d.getDirName());
					diskData.setTime(new Date());
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
					
					diskService.insertDisk(disk);
					diskService.insertDiskData(diskData);
				}
			} catch (Exception e) {
				//pass
			}
		}
	}

	@Override
	public void collectMemoryData() {
		LOG.debug("start collect memory data");
		List<Node> nodes = nodeService.getNodes();
		for (Node node : nodes) {
			try {
				MemoryData md = new MemoryData();
				Memory m = get(node, "/memory", Memory.class);
				md.setAgentId(node.getAgentId());
				md.setTime(new Date());
				md.setActualFree(m.getActualFree());
				md.setActualUsed(m.getActualUsed());
				md.setFree(m.getFree());
				md.setFreePercent(m.getFreePercent());
				md.setRam(m.getRam());
				md.setTotal(m.getTotal());
				md.setUsed(m.getUsed());
				md.setUsedPercent(m.getUsedPercent());
				
				memoryService.insertMemoryData(md);
			} catch (Exception e) {
				//pass
			}
		}
	}

	@Override
	public void collectNetworkData() {
		LOG.debug("start collect network data");
		List<Node> nodes = nodeService.getNodes();
		for (Node node : nodes) {
			try {
				cn.batchfile.stat.server.domain.Network network = new cn.batchfile.stat.server.domain.Network();
				NetworkData networkData = new NetworkData();
				
				List<Network> ns = get(node, "/network", new TypeReference<List<Network>>() {});
				for (Network n : ns) {
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
					
					networkData.setAgentId(node.getAgentId());
					networkData.setAddress(n.getAddress());
					networkData.setTime(new Date());
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
					
					networkService.insertNetwork(network);
					networkService.insertNetworkData(networkData);
				}
			} catch (Exception e) {
				//pass
			}
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
