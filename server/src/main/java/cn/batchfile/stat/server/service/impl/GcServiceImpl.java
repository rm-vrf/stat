package cn.batchfile.stat.server.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.server.dao.GcDao;
import cn.batchfile.stat.server.dao.ProcessDao;
import cn.batchfile.stat.server.domain.Gc;
import cn.batchfile.stat.server.domain.GcData;
import cn.batchfile.stat.server.domain.Node;
import cn.batchfile.stat.server.domain.ProcessInstance;
import cn.batchfile.stat.server.service.GcService;
import cn.batchfile.stat.server.service.NodeService;
import cn.batchfile.stat.util.HttpClient;
import cn.batchfile.stat.util.JsonUtil;

@Service
public class GcServiceImpl implements GcService {
	private static final int INTETRVAL = 5000;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ProcessDao processDao;
	
	@Autowired
	private GcDao gcDao;

	@Override
	public String startGc(String agentId, long pid, String name) {
		if (StringUtils.isEmpty(name)) {
			List<ProcessInstance> pis = processDao.getInstancesByAgentPidStatus(agentId, pid, "running");
			if (pis != null && pis.size() > 0) {
				ProcessInstance pi = pis.get(0);
				name = StringUtils.isEmpty(pi.getDeploymentName()) ? pi.getMonitorName() : pi.getDeploymentName();
			}
		}
		
		Node node = nodeService.getNode(agentId);

		//send gc message
		String cmd = String.format("jstat -gc %s %s", pid, INTETRVAL);
		String uri = String.format("%s://%s:%s/command/_start?cmd=%s",
				node.getSchema(), node.getAddress(), node.getPort(), StringUtils.replace(cmd, " ", "%20"));
		HttpClient hc = new HttpClient();
		hc.setConnectionTimeout(10000);
		hc.setReadTimeout(20000);
		hc.setContentType("application/json");
		hc.setCharset("utf-8");
		String command_id = hc.post(uri, null);
		if (StringUtils.startsWith(command_id, "\"")) {
			command_id = JsonUtil.decode(command_id, String.class);
		}
		
		//save gc job
		Gc gc = new Gc();
		gc.setAgentId(agentId);
		gc.setBeginTime(new Date());
		gc.setCommandId(command_id);
		gc.setName(name);
		gc.setPid(pid);
		gc.setStatus("running");
		
		gcDao.insertGc(gc);
		
		return command_id;
	}

	@Override
	public List<Gc> getRunningGcs() {
		return gcDao.getRunningGcs();
	}

	@Override
	public void updateGcStatus(Gc gc) {
		gcDao.updateGcStatus(gc);
	}

	@Override
	public void insertData(String commandId, String agentId, long pid, Date time, String out) {
		out = StringUtils.remove(out, '\r');
		String[] lines = StringUtils.split(out, '\n');
		if (lines == null || lines.length == 0) {
			return;
		}
		
		int offset = lines.length - 1;
		long t = time.getTime() - (offset * INTETRVAL);
		for (String line : lines) {
			if (!StringUtils.contains(line, "FGCT")) {
				String[] ary = StringUtils.split(line, ' ');
				if (ary != null && ary.length == 17) {
					GcData gd = new GcData();
					gd.setAgentId(agentId);
					gd.setCommandId(commandId);
					gd.setPid(pid);
					gd.setTime(new Date(t));
					gd.setS0c(Double.valueOf(ary[0]));
					gd.setS1c(Double.valueOf(ary[1]));
					gd.setS0u(Double.valueOf(ary[2]));
					gd.setS1u(Double.valueOf(ary[3]));
					gd.setEc(Double.valueOf(ary[4]));
					gd.setEu(Double.valueOf(ary[5]));
					gd.setOc(Double.valueOf(ary[6]));
					gd.setOu(Double.valueOf(ary[7]));
					gd.setMc(Double.valueOf(ary[8]));
					gd.setMu(Double.valueOf(ary[9]));
					gd.setCcsc(Double.valueOf(ary[10]));
					gd.setCcsu(Double.valueOf(ary[11]));
					gd.setYgc(Integer.valueOf(ary[12]));
					gd.setYgct(Double.valueOf(ary[13]));
					gd.setFgc(Integer.valueOf(ary[14]));
					gd.setFgct(Double.valueOf(ary[15]));
					gd.setGct(Double.valueOf(ary[16]));
					gcDao.insertData(gd);
				}
			}
			t += INTETRVAL;
		}
	}

}
