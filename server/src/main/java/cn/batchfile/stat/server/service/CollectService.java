package cn.batchfile.stat.server.service;

public interface CollectService {

	void collectCpuData();
	
	void collectDiskData();
	
	void collectGcData();
	
	void collectMemoryData();
	
	void collectNetworkData();
	
	void collectNodeData();
	
	void collectProcessData();
	
	void collectStackData();
}
