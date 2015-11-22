package cn.batchfile.stat.server.dao;

import cn.batchfile.stat.server.domain.Network;
import cn.batchfile.stat.server.domain.NetworkData;

public interface NetworkDao {

	void insertNetwork(Network network);
	
	void insertData(NetworkData networkData);
}
