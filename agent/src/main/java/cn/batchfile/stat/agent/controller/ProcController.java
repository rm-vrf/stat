package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.ProcService;
import cn.batchfile.stat.agent.types.Proc;

@RestController
public class ProcController {

	@Autowired
	private ProcService procService;
	
	@RequestMapping(value="/v1/proc", method=RequestMethod.GET)
	public List<Long> getProcs() {
		return procService.getProcs();
	}
	
	@RequestMapping(value="/v1/proc/{pid}", method=RequestMethod.GET)
	public Proc getProc(@PathVariable("pid") long pid) throws IOException {
		return procService.getProc(pid);
	}
	
	@RequestMapping(value="/v1/app/{app}/proc", method=RequestMethod.GET)
	public List<Long> getProcs(@PathVariable("app") String app) throws IOException {
		return procService.getProcs(app);
	}
	
	@RequestMapping(value="/v1/proc/{pid}/stdout", method=RequestMethod.GET)
	public List<String> getSystemOut(@PathVariable("pid") long pid) {
		return procService.getSystemOut(pid);
	}

	@RequestMapping(value="/v1/proc/{pid}/stderr", method=RequestMethod.GET)
	public List<String> getSystemErr(@PathVariable("pid") long pid) {
		return procService.getSystemErr(pid);
	}

}
