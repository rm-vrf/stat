package cn.batchfile.stat.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.RestResponse;
import cn.batchfile.stat.service.AppService;

public abstract class AppController {
	protected static final Logger log = LoggerFactory.getLogger(AppController.class);
	private AppService appService;
	
	public void setAppService(AppService appService) {
		this.appService = appService;
	}

	public List<String> getApps() {
		return appService.getApps();
	}
	
	public App getApp(HttpServletResponse response, String name) throws IOException {
		App app = appService.getApp(name);
		if (app == null) {
			response.setStatus(404);
		} 
		return app;
	}
	
	public RestResponse<String> putApp(HttpServletResponse response,
			String name, 
			App app) throws UnsupportedEncodingException, IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			app.setName(name);
			appService.putApp(app);
			resp.setOk(true);
			resp.setBody(app.getName());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	public RestResponse<String> postApp(HttpServletResponse response,
			App app) throws UnsupportedEncodingException, IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			appService.postApp(app);
			resp.setOk(true);
			resp.setBody(app.getName());
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	public RestResponse<String> deleteApp(HttpServletResponse response,
			String name) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			appService.deleteApp(name);
			resp.setOk(true);
			resp.setBody(name);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	public RestResponse<String> startApp(HttpServletResponse response,
			String name) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			appService.start(name);
			resp.setOk(true);
			resp.setBody(name);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}

	public RestResponse<String> stopApp(HttpServletResponse response,
			String name) throws IOException {
		
		RestResponse<String> resp = new RestResponse<String>();
		try {
			appService.stop(name);
			resp.setOk(true);
			resp.setBody(name);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}

}
