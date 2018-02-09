package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.ProcService;
import cn.batchfile.stat.domain.Proc;
import cn.batchfile.stat.domain.RestResponse;

@RestController
public class ProcController {

	@Autowired
	private ProcService procService;
	
	@GetMapping("/v1/proc")
	public List<Long> getProcs() {
		return procService.getProcs();
	}
	
	@GetMapping("/v1/proc/{pid}")
	public Proc getProc(@PathVariable("pid") long pid) throws IOException {
		return procService.getProc(pid);
	}
	
	@GetMapping("/v1/app/{app}/proc")
	public List<Long> getProcs(@PathVariable("app") String app) throws IOException {
		return procService.getProcs(app);
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
