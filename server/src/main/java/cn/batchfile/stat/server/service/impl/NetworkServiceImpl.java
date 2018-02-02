//package cn.batchfile.stat.server.service.impl;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import cn.batchfile.stat.server.dao.NetworkDao;
//import cn.batchfile.stat.server.domain.Network;
//import cn.batchfile.stat.server.domain.NetworkData;
//import cn.batchfile.stat.server.service.NetworkService;
//
//@Service
//public class NetworkServiceImpl implements NetworkService {
//	private Map<String, NetworkData> network_data_map = new HashMap<String, NetworkData>();
//	@Autowired
//	private NetworkDao networkDao;
//	
//	@Override
//	public void insertNetwork(Network network) {
//		networkDao.insertNetwork(network);
//	}
//
//	@Override
//	public void insertData(NetworkData networkData) {
//		calc_offset_value(networkData);
//		networkDao.insertData(networkData);
//	}
//
//	private void calc_offset_value(NetworkData networkData) {
//		String key = String.format("%s#%s", networkData.getAgentId(), networkData.getAddress());
//		NetworkData nd = network_data_map.get(key);
//		if (nd != null) {
//			long time_span = (networkData.getTime().getTime() - nd.getTime().getTime()) / 1000;
//			if (time_span > 0) {
//				networkData.setRxBytesPerSecond(networkData.getRxBytes() >= nd.getRxBytes() ? (networkData.getRxBytes() - nd.getRxBytes()) / time_span : 0);
//				networkData.setRxDroppedPerSecond(networkData.getRxDropped() >= nd.getRxDropped() ? (networkData.getRxDropped() - nd.getRxDropped()) / time_span : 0);
//				networkData.setRxErrorsPerSecond(networkData.getRxErrors() >= nd.getRxErrors() ? (networkData.getRxErrors() - nd.getRxErrors()) / time_span : 0);
//				networkData.setRxFramePerSecond(networkData.getRxFrame() >= nd.getRxFrame() ? (networkData.getRxFrame() - nd.getRxFrame()) / time_span : 0);
//				networkData.setRxOverrunsPerSecond(networkData.getRxOverruns() >= nd.getRxOverruns() ? (networkData.getRxOverruns() - nd.getRxOverruns()) / time_span : 0);
//				networkData.setRxPacketsPerSecond(networkData.getRxPackets() >= nd.getRxPackets() ? (networkData.getRxPackets() - nd.getRxPackets()) / time_span : 0);
//				networkData.setTxBytesPerSecond(networkData.getTxBytes() >= nd.getTxBytes() ? (networkData.getTxBytes() - nd.getTxBytes()) / time_span : 0);
//				networkData.setTxCarrierPerSecond(networkData.getTxCarrier() >= nd.getTxCarrier() ? (networkData.getTxCarrier() - nd.getTxCarrier()) / time_span : 0);
//				networkData.setTxDroppedPerSecond(networkData.getTxDropped() >= nd.getTxDropped() ? (networkData.getTxDropped() - nd.getTxDropped()) / time_span : 0);
//				networkData.setTxErrorsPerSecond(networkData.getTxErrors() >= nd.getTxErrors() ? (networkData.getTxErrors() - nd.getTxErrors()) / time_span : 0);
//				networkData.setTxOverrunsPerSecond(networkData.getTxOverruns() >= nd.getTxOverruns() ? (networkData.getTxOverruns() - nd.getTxOverruns()) / time_span : 0);
//				networkData.setTxPacketsPerSecond(networkData.getTxPackets() >= nd.getTxPackets() ? (networkData.getTxPackets() - nd.getTxPackets()) / time_span : 0);
//			}
//		}
//		network_data_map.put(key, networkData);
//	}
//}
