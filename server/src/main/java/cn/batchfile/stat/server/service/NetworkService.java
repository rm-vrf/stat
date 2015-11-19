package cn.batchfile.stat.server.service;

import cn.batchfile.stat.server.domain.Network;
import cn.batchfile.stat.server.domain.NetworkData;

public interface NetworkService {

	void insertNetwork(Network network);
	
	void insertNetworkData(NetworkData networkData);
}
