//package cn.batchfile.stat.server.controller;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import cn.batchfile.stat.domain.Everything;
//import cn.batchfile.stat.server.service.SysService;
//
//@RestController
//public class SysController {
//	protected static final Logger log = LoggerFactory.getLogger(SysController.class);
//
//	@Autowired
//	private SysService sysService;
//
//	@PutMapping("/v1/everything")
//	public void putEverything(@RequestBody Everything everything) {
//		sysService.putEverything(everything);
//	}
//}
