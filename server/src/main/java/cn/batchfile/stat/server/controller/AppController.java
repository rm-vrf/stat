//package cn.batchfile.stat.server.controller;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.List;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.context.request.WebRequest;
//
//import cn.batchfile.stat.domain.App;
//import cn.batchfile.stat.domain.RestResponse;
//
//@RestController
//public class AppController extends cn.batchfile.stat.controller.AppController {
//	protected static final Logger log = LoggerFactory.getLogger(AppController.class);
//	
//	@Autowired
//	@Override
//	public void setAppService(cn.batchfile.stat.service.AppService appService) {
//		super.setAppService(appService);
//	}
//	
//	@GetMapping("/v1/app")
//	public ResponseEntity<List<String>> getApps(WebRequest request,
//			@RequestParam(name="query", defaultValue="") String query) {
//		
//		return super.getApps(request, query);
//	}
//	
//	@GetMapping("/v1/app/{name}")
//	public ResponseEntity<App> getApp(WebRequest request, 
//			@PathVariable("name") String name) throws IOException {
//		
//		return super.getApp(request, name);
//	}
//	
//	@PostMapping("/v1/app")
//	public RestResponse<String> postApp(HttpServletResponse response,
//			@RequestBody App app) throws UnsupportedEncodingException, IOException {
//		
//		return super.postApp(response, app);
//	}
//	
//	@PutMapping("/v1/app/{name}")
//	public RestResponse<String> putApp(HttpServletResponse response,
//			@PathVariable("name") String name, 
//			@RequestBody App app) throws UnsupportedEncodingException, IOException {
//		
//		return super.putApp(response, name, app);
//	}
//	
//	@DeleteMapping("/v1/app/{name}")
//	public RestResponse<String> deleteApp(HttpServletResponse response,
//			@PathVariable("name") String name) throws IOException {
//		
//		return super.deleteApp(response, name);
//	}
//	
//	@PostMapping("/v1/app/{name}/_start")
//	public RestResponse<String> startApp(HttpServletResponse response,
//			@PathVariable("name") String name) throws IOException {
//
//		return super.startApp(response, name);
//	}
//
//	@PostMapping("/v1/app/{name}/_stop")
//	public RestResponse<String> stopApp(HttpServletResponse response,
//			@PathVariable("name") String name) throws IOException {
//
//		return super.stopApp(response, name);
//	}
//
//}
