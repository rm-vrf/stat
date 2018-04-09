package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.P;
import cn.batchfile.stat.domain.Proc;
import cn.batchfile.stat.domain.RestResponse;
import cn.batchfile.stat.server.service.ProcService;

@RestController
public class ProcController {

	@Autowired
	private ProcService procService;

	@GetMapping("/v1/proc")
	public List<P> getProcs() {
		List<Proc> ps = procService.getProcs();
		return ps.stream().map(p -> new P(p.getPid(), p.getNode(), p.getApp())).collect(Collectors.toList());
	}
	
	@GetMapping("/v1/app/{name}/proc")
	public List<P> getProcsByApp(@PathVariable("name") String app, @RequestParam(name="query", defaultValue="") String query) {
		List<Proc> ps = procService.getProcsByApp(app, query);
		return ps.stream().map(p -> new P(p.getPid(), p.getNode(), p.getApp())).collect(Collectors.toList());
	}
	
	@GetMapping("/v1/app/{name}/proc/{pid}")
	public Proc getProcByApp(HttpServletResponse response,
			@PathVariable("name") String app, 
			@PathVariable("pid") long pid) throws IOException {
		
		Proc p = procService.getProcByApp(app, pid);
		if (p == null) {
			response.sendError(404);
		}
		return p;
	}
	
	@GetMapping("/v1/node/{id}/proc")
	public List<P> getProcsByNode(@PathVariable("id") String node) {
		List<Proc> ps = procService.getProcsByNode(node);
		return ps.stream().map(p -> new P(p.getPid(), p.getNode(), p.getApp())).collect(Collectors.toList());
	}
	
	@GetMapping("/v1/node/{id}/proc/{pid}")
	public Proc getProcByNode(HttpServletResponse response,
			@PathVariable("id") String node, 
			@PathVariable("pid") long pid) throws IOException {
		
		Proc p = procService.getProcByNode(node, pid);
		if (p == null) {
			response.sendError(404);
		}
		return p;
	}
	
	@PostMapping("/v1/node/{id}/proc/{pid}/_kill")
	public RestResponse<P> killProc(HttpServletResponse response,
			@PathVariable("id") String node,
			@PathVariable("pid") long pid) throws IOException {
		
		RestResponse<P> resp = new RestResponse<P>();
		try {
			Proc p = procService.getProcByNode(node, pid);
			procService.killProcs(node, Arrays.asList(new Long[] {pid}));
			resp.setOk(true);
			resp.setBody(new P(pid, node, p == null ? null : p.getApp()));
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	@GetMapping("/v1/node/{id}/proc/{pid}/_stdout")
	public String[] getSystemOut(@PathVariable("id") String node, 
			@PathVariable("pid") long pid) {
		
		return procService.getSystemOut(node, pid);
	}
	
	@GetMapping("/v1/node/{id}/proc/{pid}/_stderr")
	public String[] getSystemErr(@PathVariable("id") String node, 
			@PathVariable("pid") long pid) {
		
		return procService.getSystemErr(node, pid);
	}
	
	@PostMapping("/v1/proc")
	public RestResponse<String> putProcs(HttpServletResponse response, 
			@RequestBody List<Proc> ps,
			@RequestParam("id") String id) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			procService.putProcs(id, ps);
			resp.setOk(true);
			resp.setBody(id);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
}
