package cn.batchfile.stat.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.server.domain.system.Master;
import cn.batchfile.stat.server.service.MasterService;

@RestController
public class MasterController {
	private static final Logger LOG = LoggerFactory.getLogger(MasterController.class);
	
	@Autowired
	private MasterService masterService;
	
	@GetMapping("/api/ping")
	public String ping() {
		LOG.debug("ping me");
		return "ok";
	}
	
	@GetMapping("/api/master")
	public ResponseEntity<List<Master>> getMasters() {
		List<Master> list = masterService.getMasters();
		return new ResponseEntity<List<Master>>(list, HttpStatus.OK);
	}
}
