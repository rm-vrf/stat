package cn.batchfile.stat.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.server.domain.Stat;
import cn.batchfile.stat.server.service.StatService;

@RestController
public class StatController {
	protected static final Logger log = LoggerFactory.getLogger(StatController.class);
	
	@Autowired
	private StatService statService;
	
	@GetMapping("/v1/stat/node/os")
	public Stat getOsStat(@RequestParam(name="node", defaultValue="") String node, 
			@RequestParam(name="time", defaultValue="30min") String time) {
		
		return statService.getOsStat(node, time);
	}

	@GetMapping("/v1/stat/node/cpu")
	public Stat getCpuStat(@RequestParam(name="node", defaultValue="") String node, 
			@RequestParam(name="time", defaultValue="30min") String time) {
		
		return statService.getCpuStat(node, time);
	}

	@GetMapping("/v1/stat/node/mem")
	public Stat getMemStat(@RequestParam(name="node", defaultValue="") String node, 
			@RequestParam(name="time", defaultValue="30min") String time) {
		
		return statService.getMemStat(node, time);
	}

	@GetMapping("/v1/stat/node/disk")
	public Stat getDiskStat(@RequestParam(name="node", defaultValue="") String node, 
			@RequestParam(name="time", defaultValue="30min") String time) {
		
		return statService.getDiskStat(node, time);
	}

	@GetMapping("/v1/stat/node/net")
	public Stat getNetStat(@RequestParam(name="node", defaultValue="") String node, 
			@RequestParam(name="time", defaultValue="30min") String time) {
		
		return statService.getNetStat(node, time);
	}
}
