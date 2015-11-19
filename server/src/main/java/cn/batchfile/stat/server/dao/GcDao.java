package cn.batchfile.stat.server.dao;

import java.util.List;

import cn.batchfile.stat.server.domain.Gc;
import cn.batchfile.stat.server.domain.GcData;

public interface GcDao {

	void insertGc(Gc gc);

	List<Gc> getRunningGcs();

	void updateGcStatus(Gc gc);
	
	void insertGcData(GcData gcData);
}
