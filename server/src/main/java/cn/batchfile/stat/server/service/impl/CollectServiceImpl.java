package cn.batchfile.stat.server.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.TypeReference;

import cn.batchfile.stat.agent.domain.Network;
import cn.batchfile.stat.agent.domain.Os;
import cn.batchfile.stat.agent.domain.State;
import cn.batchfile.stat.server.domain.NetworkData;
import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.server.domain.NodeData;
import cn.batchfile.stat.server.service.CollectService;
import cn.batchfile.stat.server.service.NetworkService;
import cn.batchfile.stat.server.service.NodeService;
import cn.batchfile.stat.util.HttpClient;
import cn.batchfile.stat.util.JsonUtil;

public class CollectServiceImpl implements CollectService {
	private static final Logger LOG = Logger.getLogger(CollectServiceImpl.class);
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private NetworkService networkService;

	@Override
	public void collectCpuData() {
		LOG.debug("start collect cpu data");
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

	@Override
	public void collectGcData() {
		LOG.debug("start collect gc data");
	}

	@Override
	public void collectMemoryData() {
		LOG.debug("start collect memory data");
	}

	@Override
	public void collectNetworkData() {
		LOG.debug("start collect network data");
	}

	@Override
	public void collectProcessData() {
		LOG.debug("start collect process data");
	}

	@Override
	public void collectStackData() {
		LOG.debug("start collect stack data");
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
