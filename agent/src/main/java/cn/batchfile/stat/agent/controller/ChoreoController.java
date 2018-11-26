//package cn.batchfile.stat.agent.controller;
//
//import java.io.IOException;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.context.request.WebRequest;
//
//import cn.batchfile.stat.domain.Choreo;
//import cn.batchfile.stat.domain.RestResponse;
//
//@RestController
//public class ChoreoController extends cn.batchfile.stat.controller.ChoreoController {
//	protected static final Logger log = LoggerFactory.getLogger(ChoreoController.class);
//	
//	@Autowired
//	@Override
//	public void setChoreoService(cn.batchfile.stat.service.ChoreoService choreoService) {
//		super.setChoreoService(choreoService);
//	}
//	
//	@GetMapping("/v1/app/{name}/choreo")
//	public ResponseEntity<Choreo> getChoreo(WebRequest request,
//			@PathVariable("name") String name) throws IOException {
//		
//		return super.getChoreo(request, name);
//	}
//	
//	@PutMapping("/v1/app/{name}/choreo")
//	public RestResponse<String> putChoreo(HttpServletResponse response, 
//			@RequestBody Choreo choreo) throws IOException {
//		
//		return super.putChoreo(response, choreo);
//	}
//	
//	@GetMapping("/v1/app/{name}/_scale")
//	public ResponseEntity<Integer> getScale(WebRequest request,
//			@PathVariable("name") String name) throws IOException {
//		
//		return super.getScale(request, name);
//	}
//	
//	@PostMapping("/v1/app/{name}/_scale")
//	public RestResponse<Integer> putScale(HttpServletResponse response,
//			@PathVariable("name") String name, 
//			@RequestParam("num") int scale) throws IOException {
//		
//		return super.putScale(response, name, scale);
//	}
//	
//}
