package cn.batchfile.stat.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.RestResponse;
import cn.batchfile.stat.service.ChoreoService;

public abstract class ChoreoController {
	protected static final Logger log = LoggerFactory.getLogger(ChoreoController.class);
	private ChoreoService choreoService;

	public void setChoreoService(ChoreoService choreoService) {
		this.choreoService = choreoService;
	}

	public Choreo getChoreo(HttpServletResponse response,
			String name) throws IOException {
		
		Choreo choreo = choreoService.getChoreo(name);
		if (choreo == null) {
			response.setStatus(404);
		}
		return choreo;
	}
	
	public RestResponse<String> putChoreo(HttpServletResponse response, Choreo choreo) throws IOException {
		RestResponse<String> resp = new RestResponse<String>();
		try {
			choreoService.putChoreo(choreo);
			resp.setOk(true);
			resp.setBody(choreo.getApp());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	public int getScale(HttpServletResponse response,
			String name) throws IOException {
		
		Choreo choreo = choreoService.getChoreo(name);
		if (choreo == null) {
			response.setStatus(404);
		}
		return choreo.getScale();
	}
	
	public RestResponse<Integer> putScale(HttpServletResponse response,
			String name, 
			int scale) throws IOException {
		
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
