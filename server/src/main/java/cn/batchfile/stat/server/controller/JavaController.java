package cn.batchfile.stat.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.batchfile.stat.server.service.GcService;
import cn.batchfile.stat.server.service.StackService;

@Controller
public class JavaController {
	
	@Autowired
	private GcService gcService;

	@Autowired
	private StackService stackService;

	@RequestMapping(value="/a/gc", method=RequestMethod.POST)
	@ResponseBody
	public String startGc(@RequestParam("agent_id") String agentId, 
			@RequestParam("pid") long pid,
			@RequestParam(value="name",required=false) String name) {
		
		return gcService.startGc(agentId, pid, name);
	}
	
	@RequestMapping(value="/a/stack", method=RequestMethod.POST)
	@ResponseBody
	public String startStack(@RequestParam("agent_id") String agentId, 
			@RequestParam("pid") long pid,
			@RequestParam(value="name",required=false) String name) {
		
		return stackService.startStack(agentId, pid, name);
	}

}
