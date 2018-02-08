package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.types.RestResponse;
import cn.batchfile.stat.server.service.ChoreoService;
import cn.batchfile.stat.server.types.Choreo;

@RestController
public class ChoreoController {
	protected static final Logger log = LoggerFactory.getLogger(ChoreoController.class);
	
	@Autowired
	private ChoreoService choreoService;
	
	@GetMapping("/v1/app/{name}/choreo")
	public Choreo getChoreo(HttpServletResponse response,
			@PathVariable("name") String name) throws IOException {
		
		Choreo choreo = choreoService.getChoreo(name);
		if (choreo == null) {
			response.setStatus(404);
		}
		return choreo;
	}

	@PutMapping("/v1/app/{name}/choreo")
	public RestResponse<String> putChoreo(HttpServletResponse response,
			@PathVariable("name") String name,
			@RequestBody Choreo choreo) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			choreo.setApp(name);
			choreo.getDistribution().clear();
			choreoService.putChoreo(choreo, true);
			resp.setOk(true);
			resp.setBody(choreo.getApp());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		
		return resp;
	}
	
	@GetMapping("/v1/choreo")
	public List<Choreo> getDistribution(HttpServletResponse response,
			@RequestParam("node") String node) {

		//TODO
		return new ArrayList<Choreo>();
	}
}
