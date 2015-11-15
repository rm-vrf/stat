package cn.batchfile.stat.agent.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.batchfile.stat.agent.service.CommandService;

@Controller
public class CommandController {

	@Resource(name="commandService")
	private CommandService commandService;
	
	@RequestMapping(value="/command/_execute", method=RequestMethod.POST)
	@ResponseBody
	public String execute(@RequestParam("cmd") String cmd) {
		return commandService.execute(cmd);
	}
	
	@RequestMapping(value="/command/_start", method=RequestMethod.POST)
	@ResponseBody
	public String start(@RequestParam("cmd") String cmd) {
		return commandService.start(cmd);
	}
	
	@RequestMapping(value="/command/{id}/_consume", method=RequestMethod.GET)
	@ResponseBody
	public String consume(@PathVariable("id") String id) {
		return commandService.consume(id);
	}

	@RequestMapping(value="/command/{id}", method=RequestMethod.DELETE)
	@ResponseBody
	public void terminate(@PathVariable("id") String id) {
		commandService.terminate(id);
	}
	
	@RequestMapping(value="/command/{id}/state", method=RequestMethod.GET)
	@ResponseBody
	public String state(@PathVariable("id") String id) {
		return commandService.getState(id);
	}
}
