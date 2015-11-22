package cn.batchfile.stat.server.dao;

import cn.batchfile.stat.server.domain.Disk;
import cn.batchfile.stat.server.domain.DiskData;

public interface DiskDao {

	void insertDisk(Disk disk);
	
	void insertData(DiskData diskData);
}
