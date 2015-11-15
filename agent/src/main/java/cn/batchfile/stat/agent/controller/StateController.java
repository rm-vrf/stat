package cn.batchfile.stat.agent.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
