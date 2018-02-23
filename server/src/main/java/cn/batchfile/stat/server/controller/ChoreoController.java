package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.RestResponse;

@RestController
public class ChoreoController extends cn.batchfile.stat.controller.ChoreoController {
	protected static final Logger log = LoggerFactory.getLogger(ChoreoController.class);
	
	@Autowired
	@Override
	public void setChoreoService(cn.batchfile.stat.service.ChoreoService choreoService) {
		super.setChoreoService(choreoService);
	}
	
	@GetMapping("/v1/choreo")
	public List<Choreo> getChoreos(@RequestParam("node") String node) throws IOException {
		return super.getChoreos(node);
	}
	
	@GetMapping("/v1/app/{name}/choreo")
	public Choreo getChoreo(HttpServletResponse response,
			@PathVariable("name") String name) throws IOException {
		
		return super.getChoreo(response, name);
	}
	
	@PutMapping("/v1/app/{name}/choreo")
	public RestResponse<String> putChoreo(HttpServletResponse response, 
			@RequestBody Choreo choreo) throws IOException {
		return super.putChoreo(response, choreo);
	}
	
	@GetMapping("/v1/app/{name}/_scale")
	public int getScale(HttpServletResponse response,
			@PathVariable("name") String name) throws IOException {
		
		return super.getScale(response, name);
	}
	
	@PostMapping("/v1/app/{name}/_scale")
	public RestResponse<Integer> putScale(HttpServletResponse response,
			@PathVariable("name") String name, 
			@RequestParam("num") int scale) throws IOException {
		
		return super.putScale(response, name, scale);
	}
	
	@GetMapping("/v1/app/{name}/_query")
	public String getQuery(HttpServletResponse response,
			@PathVariable("name") String name) throws IOException {
		
		Choreo choreo = super.getChoreo(response, name);
		return choreo.getQuery();
	}
	
	@PostMapping("/v1/app/{name}/_query")
	public RestResponse<String> putQuery(HttpServletResponse response,
			@PathVariable("name") String name, 
			@RequestBody String query) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			Choreo choreo = super.getChoreo(response, name);
			choreo.setQuery(query);
			super.putChoreo(response, choreo);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}

}
