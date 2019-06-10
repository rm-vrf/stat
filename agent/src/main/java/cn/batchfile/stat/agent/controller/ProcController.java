package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.agent.service.ProcService;
import cn.batchfile.stat.domain.Proc;
import cn.batchfile.stat.domain.RestResponse;

@RestController
public class ProcController {

	@Autowired
	private ProcService procService;
	
	@GetMapping("/v1/proc")
	public ResponseEntity<List<Long>> getProcs(WebRequest request) {
		long lastModified = procService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<Long>>(HttpStatus.NOT_MODIFIED);
		}
		
		List<Long> ps = procService.getProcs();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Long>>(ps, headers, HttpStatus.OK);
	}
	
	@GetMapping("/v1/proc/{pid}")
	public ResponseEntity<Proc> getProc(WebRequest request, 
			@PathVariable("pid") long pid) throws IOException {
		
		long lastModified = procService.getLastModified(pid);
		if (lastModified < 0) {
			return new ResponseEntity<Proc>(HttpStatus.NOT_FOUND);
		}
		
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<Proc>(HttpStatus.NOT_MODIFIED);
		}
		
		Proc p = procService.getProc(pid);
		if (p == null) {
			return new ResponseEntity<Proc>(HttpStatus.NOT_FOUND);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Proc>(p, headers, HttpStatus.OK);
	}
	
	@GetMapping("/v1/app/{app}/proc")
	public ResponseEntity<List<Long>> getProcs(WebRequest request,
			@PathVariable("app") String app) throws IOException {
		
		long lastModified = procService.getLastModified();
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<Long>>(HttpStatus.NOT_MODIFIED);
		}
		
		List<Long> ps = procService.getProcs(app);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Long>>(ps, headers, HttpStatus.OK);
	}
	
	@GetMapping("/v1/proc/{pid}/_stdout")
	public List<String> getSystemOut(@PathVariable("pid") long pid) {
		return procService.getSystemOut(pid);
	}

	@GetMapping("/v1/proc/{pid}/_stderr")
	public List<String> getSystemErr(@PathVariable("pid") long pid) {
		return procService.getSystemErr(pid);
	}
	
	@PostMapping("/v1/proc/{pid}/_kill")
	public RestResponse<Long> killProc(HttpServletResponse response,
			@PathVariable("pid") long pid) throws IOException {

		RestResponse<Long> resp = new RestResponse<Long>();
		try {
			procService.killProcs(Arrays.asList(new Long[] {pid}));
			resp.setOk(true);
			resp.setBody(pid);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	@PostMapping("/v1/proc/_kill")
	public RestResponse<List<Long>> killProcs(HttpServletResponse response, 
			@RequestBody List<Long> pids) throws IOException {
		
		RestResponse<List<Long>> resp = new RestResponse<List<Long>>();
		try {
			procService.killProcs(pids);
			resp.setOk(true);
			resp.setBody(pids);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
}
