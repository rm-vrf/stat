package cn.batchfile.stat.server.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import cn.batchfile.stat.server.domain.Gc;
import cn.batchfile.stat.server.service.GcService;

@Service
public class GcServiceImpl implements GcService {

	@Override
	public String startGc(String agentId, long pid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Gc> getRunningGcs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateGcStatus(Gc gc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertGcData(String out) {
		// TODO Auto-generated method stub

	}

}
