package cn.batchfile.stat.agent.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.ChoreoService;
import cn.batchfile.stat.agent.types.Choreo;
import cn.batchfile.stat.agent.types.RestResponse;

@RestController
public class ChoreoController {
	protected static final Logger log = LoggerFactory.getLogger(ChoreoController.class);
	
	@Autowired
	private ChoreoService choreoService;
	
	@GetMapping("/v1/app/{name}/_scale")
	public int getScale(HttpServletResponse response,
			@PathVariable("name") String name) throws IOException {
		
		Choreo choreo = choreoService.getChoreo(name);
		if (choreo == null) {
			response.setStatus(404);
		}
		return choreo.getScale();
	}
	
	@PostMapping("/v1/app/{name}/_scale")
	public RestResponse<Integer> putScale(HttpServletResponse response,
			@PathVariable("name") String name, 
			@RequestParam("num") int scale) throws IOException {
		
		RestResponse<Integer> resp = new RestResponse<Integer>();
		try {
			choreoService.putScale(name, scale);
			resp.setOk(true);
			resp.setBody(scale);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
}
