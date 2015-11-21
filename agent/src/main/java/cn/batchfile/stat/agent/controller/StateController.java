package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.hyperic.sigar.SigarException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.batchfile.stat.agent.domain.Cpu;
import cn.batchfile.stat.agent.domain.Disk;
import cn.batchfile.stat.agent.domain.Everything;
import cn.batchfile.stat.agent.domain.Memory;
import cn.batchfile.stat.agent.domain.Network;
import cn.batchfile.stat.agent.domain.Os;
import cn.batchfile.stat.agent.domain.State;
import cn.batchfile.stat.agent.service.ProcessService;
import cn.batchfile.stat.agent.service.StateService;

@Controller
public class StateController {
	
	@Resource(name="stateService")
	private StateService stateService;
	
	@Resource(name="processService")
	private ProcessService processService;
	
	@RequestMapping(value="/everything", method=RequestMethod.GET)
	@ResponseBody
	public Everything getEverything() throws SigarException {
		Everything e = new Everything();
		e.setCpu(stateService.getCpu());
		e.setDisks(stateService.getDisks());
		e.setMemory(stateService.getMemory());
		e.setNetworks(stateService.getNetworks());
		e.setOs(stateService.getOs());
		e.setState(stateService.getState());
		return e;
	}
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	@ResponseBody
	public Object index() {
		return getState();
	}
	
	@RequestMapping(value="/state", method=RequestMethod.GET)
	@ResponseBody
	public State getState() {
		return stateService.getState();
	}

	@RequestMapping(value="/os", method=RequestMethod.GET)
	@ResponseBody
	public Os getOs() {
		return stateService.getOs();
	}
	
	@RequestMapping(value="/cpu", method=RequestMethod.GET)
	@ResponseBody
	public Cpu getCpu() throws SigarException {
		return stateService.getCpu();
	}
	
	@RequestMapping(value="/disk", method=RequestMethod.GET)
	@ResponseBody
	public List<Disk> getDisks() throws SigarException {
		return stateService.getDisks();
	}
	
	@RequestMapping(value="/memory", method=RequestMethod.GET)
	@ResponseBody
	public Memory getMemory() throws SigarException {
		return stateService.getMemory();
	}

	@RequestMapping(value="/network", method=RequestMethod.GET)
	@ResponseBody
	public List<Network> getNetworks() throws SigarException {
		return stateService.getNetworks();
	}
	
	@RequestMapping(value="/env", method=RequestMethod.GET)
	@ResponseBody
	public Properties getEnv() throws IOException {
		return CommandLineUtils.getSystemEnvVars();
	}
}
