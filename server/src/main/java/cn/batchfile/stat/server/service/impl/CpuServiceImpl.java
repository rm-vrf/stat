package cn.batchfile.stat.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.server.dao.CpuDao;
import cn.batchfile.stat.server.domain.CpuData;
import cn.batchfile.stat.server.service.CpuService;

@Service
public class CpuServiceImpl implements CpuService {

	@Autowired
	private CpuDao cpuDao;
	
	@Override
	public void insertData(CpuData cpuData) {
		cpuDao.insertData(cpuData);
	}
}
