package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Proc;
import cn.batchfile.stat.domain.RestResponse;
import cn.batchfile.stat.server.service.ProcService;

@RestController
public class ProcController {

	@Autowired
	private ProcService procService;

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
