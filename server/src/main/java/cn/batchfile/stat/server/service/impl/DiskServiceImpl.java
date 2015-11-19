package cn.batchfile.stat.server.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.server.dao.DiskDao;
import cn.batchfile.stat.server.domain.Disk;
import cn.batchfile.stat.server.domain.DiskData;
import cn.batchfile.stat.server.service.DiskService;

@Service
public class DiskServiceImpl implements DiskService {
	private Map<String, DiskData> disk_data_map = new HashMap<String, DiskData>();

	@Autowired
	private DiskDao diskDao;
	
	@Override
	public void insertDisk(Disk disk) {
		diskDao.insertDisk(disk);
	}

	@Override
	public void insertDiskData(DiskData diskData) {
		calc_offset_value(diskData);
		diskDao.insertDiskData(diskData);
	}
	
	private void calc_offset_value(DiskData diskData) {
		String key = String.format("%s#%s", diskData.getAgentId(), diskData.getDirName());
		DiskData dd = disk_data_map.get(key);
		if (dd != null) {
			long time_span = (diskData.getTime().getTime() - dd.getTime().getTime()) / 1000;
			if (time_span > 0) {
				diskData.setDiskReadsPerSecond(diskData.getDiskReads() >= dd.getDiskReads() ? (diskData.getDiskReads() - dd.getDiskReads()) / time_span : 0);
				diskData.setDiskWritesPerSecond(diskData.getDiskWrites() >= dd.getDiskWrites() ? (diskData.getDiskWrites() - dd.getDiskWrites()) / time_span : 0);
				diskData.setDiskReadBytesPerSecond(diskData.getDiskReadBytes() >= dd.getDiskReadBytes() ? (diskData.getDiskReadBytes() - dd.getDiskReadBytes()) / time_span : 0);
				diskData.setDiskWriteBytesPerSecond(diskData.getDiskWriteBytes() >= dd.getDiskWriteBytes() ? (diskData.getDiskWriteBytes() - dd.getDiskWriteBytes()) / time_span : 0);
			}
		}
		disk_data_map.put(key, diskData);
	}

}
