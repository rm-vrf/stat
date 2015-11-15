package cn.batchfile.stat.agent.controller;

import java.util.List;

import javax.annotation.Resource;

import org.hyperic.sigar.SigarException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.batchfile.stat.agent.domain.Cpu;
import cn.batchfile.stat.agent.domain.Disk;
import cn.batchfile.stat.agent.domain.Memory;
import cn.batchfile.stat.agent.domain.Network;
import cn.batchfile.stat.agent.domain.Os;
import cn.batchfile.stat.agent.domain.State;
import cn.batchfile.stat.agent.service.StateService;

@Controller
public class StateController {
	
	@Resource(name="stateService")
	private StateService stateService;
	
	@RequestMapping("/")
	@ResponseBody
	public Object index() {
		return getState();
	}
	
	@RequestMapping("/state")
	@ResponseBody
	public State getState() {
		return stateService.getState();
	}

	@RequestMapping("/os")
	@ResponseBody
	public Os getOs() {
		return stateService.getOs();
	}
	
	@RequestMapping("/cpu")
	@ResponseBody
	public Cpu getCpu() throws SigarException {
		return stateService.getCpu();
	}
	
	@RequestMapping("/disk")
	@ResponseBody
	public List<Disk> getDisks() throws SigarException {
		return stateService.getDisks();
	}
	
	@RequestMapping("/memory")
	@ResponseBody
	public Memory getMemory() throws SigarException {
		return stateService.getMemory();
	}

	@RequestMapping("/network")
	@ResponseBody
	public List<Network> getNetworks() throws SigarException {
		return stateService.getNetworks();
	}
}
