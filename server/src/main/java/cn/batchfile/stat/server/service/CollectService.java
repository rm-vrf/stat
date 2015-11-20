package cn.batchfile.stat.server.service;

public interface CollectService {
	
	void collectEverything();

	void collectGcData();
	
	void collectStackData();
}
