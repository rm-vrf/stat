package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.ProcService;
import cn.batchfile.stat.agent.types.Proc;
import cn.batchfile.stat.agent.types.RestResponse;

@RestController
public class ProcController {

	@Autowired
	private ProcService procService;
	
	@RequestMapping(value="/v1/proc", method=RequestMethod.GET)
	public List<Long> getProcs() {
		return procService.getProcs();
	}
	
	@RequestMapping(value="/v1/proc/{pid}", method=RequestMethod.GET)
	public Proc getProc(@PathVariable("pid") long pid) throws IOException {
		return procService.getProc(pid);
	}
	
	@RequestMapping(value="/v1/app/{app}/proc", method=RequestMethod.GET)
	public List<Long> getProcs(@PathVariable("app") String app) throws IOException {
		return procService.getProcs(app);
	}
	
	@RequestMapping(value="/v1/proc/{pid}/_stdout", method=RequestMethod.GET)
	public List<String> getSystemOut(@PathVariable("pid") long pid) {
		return procService.getSystemOut(pid);
	}

	@RequestMapping(value="/v1/proc/{pid}/_stderr", method=RequestMethod.GET)
	public List<String> getSystemErr(@PathVariable("pid") long pid) {
		return procService.getSystemErr(pid);
	}
	
	@RequestMapping(value="/v1/proc/{pid}/_kill", method=RequestMethod.POST)
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
			response.setStatus(500);
		}
		return resp;
	}
	
	@RequestMapping(value="/v1/proc/_kill", method=RequestMethod.POST)
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
			response.setStatus(500);
		}
		return resp;
	}
	
}
