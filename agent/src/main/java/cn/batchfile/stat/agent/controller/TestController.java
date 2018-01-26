//package cn.batchfile.stat.agent.controller;
//
//import javax.annotation.Resource;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import cn.batchfile.stat.agent.domain.DatabaseResult;
//import cn.batchfile.stat.agent.domain.HttpResult;
//import cn.batchfile.stat.agent.service.TestService;
//
//@Controller
//public class TestController {
//
//	@Resource(name="testService")
//	private TestService testService;
//	
//	@RequestMapping(value="/http/_test", method=RequestMethod.GET)
//	@ResponseBody
//	public HttpResult testHttp(@RequestParam("url") String url,
//			@RequestParam("method") String method,
//			@RequestParam(value="username", required=false) String username,
//			@RequestParam(value="password", required=false) String password) {
//		
//		return testService.testHttp(url, method, username, password);
//	}
//	
//	@RequestMapping(value="/database/_test", method=RequestMethod.GET)
//	@ResponseBody
//	public DatabaseResult testDatabase(@RequestParam("driver") String driver,
//			@RequestParam("url") String url,
//			@RequestParam(value="username", required=false) String username,
//			@RequestParam(value="password", required=false) String password,
//			@RequestParam("sql") String sql) {
//		
//		return testService.testDatabase(driver, url, username, password, sql);
//	}
//}
