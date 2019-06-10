package cn.batchfile.stat.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.RestResponse;
import cn.batchfile.stat.service.AppService;

public abstract class AppController {
	protected static final Logger log = LoggerFactory.getLogger(AppController.class);
	private AppService appService;
	
	public void setAppService(AppService appService) {
		this.appService = appService;
	}

	public ResponseEntity<List<String>> getApps(WebRequest request, String query) {
		long lastModified = appService.getLastModified();
		if (StringUtils.isEmpty(query) && request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<String>>(HttpStatus.NOT_MODIFIED);
		}
		
		List<String> apps = appService.getApps();
		if (StringUtils.isNotEmpty(query)) {
			Iterator<String> iter = apps.iterator();
			while (iter.hasNext()) {
				String s = iter.next();
				if (!StringUtils.containsIgnoreCase(s, query)) {
					iter.remove();
				}
			}
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<String>>(apps, headers, HttpStatus.OK);
	}
	
	public ResponseEntity<App> getApp(WebRequest request, String name) throws IOException {
		long lastModified = appService.getLastModified(name);
		if (lastModified < 0) {
			return new ResponseEntity<App>(HttpStatus.NOT_FOUND);
		}
		
		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<App>(HttpStatus.NOT_MODIFIED);
		}
		
		App app = appService.getApp(name);
		if (app == null) {
			return new ResponseEntity<App>(HttpStatus.NOT_FOUND);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<App>(app, headers, HttpStatus.OK);
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
