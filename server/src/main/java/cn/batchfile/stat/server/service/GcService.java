package cn.batchfile.stat.server.service;

import java.util.List;

import cn.batchfile.stat.server.domain.Gc;

public interface GcService {

	String startGc(String agentId, long pid);

	List<Gc> getRunningGcs();

	void updateGcStatus(Gc gc);

	void insertGcData(String out);

}
