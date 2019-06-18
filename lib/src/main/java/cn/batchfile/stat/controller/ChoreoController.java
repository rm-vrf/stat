//package cn.batchfile.stat.controller;
//
//import java.io.IOException;
//import java.util.List;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.context.request.WebRequest;
//
//import cn.batchfile.stat.domain.Choreo;
//import cn.batchfile.stat.domain.RestResponse;
//import cn.batchfile.stat.service.ChoreoService;
//
//public abstract class ChoreoController {
//	protected static final Logger log = LoggerFactory.getLogger(ChoreoController.class);
//	private ChoreoService choreoService;
//
//	public void setChoreoService(ChoreoService choreoService) {
//		this.choreoService = choreoService;
//	}
//
//	public ResponseEntity<Choreo> getChoreo(WebRequest request,
//			String name) throws IOException {
//
//		long lastModified = choreoService.getLastModified(name);
//		if (lastModified < 0) {
//			return new ResponseEntity<Choreo>(HttpStatus.NOT_FOUND);
//		}
//
//		if (request.checkNotModified(lastModified)) {
//			return new ResponseEntity<Choreo>(HttpStatus.NOT_MODIFIED);
//		}
//
//		Choreo choreo = choreoService.getChoreo(name);
//		if (choreo == null) {
//			return new ResponseEntity<Choreo>(HttpStatus.NOT_FOUND);
//		}
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.setLastModified(lastModified);
//		headers.setCacheControl("no-cache");
//		return new ResponseEntity<Choreo>(choreo, headers, HttpStatus.OK);
//	}
//
//	public ResponseEntity<List<Choreo>> getChoreos(WebRequest request, String node) throws IOException {
//		long lastModified = choreoService.getLastModified();
//		if (request.checkNotModified(lastModified)) {
//			return new ResponseEntity<List<Choreo>>(HttpStatus.NOT_MODIFIED);
//		}
//
//		List<Choreo> choreos = choreoService.getChoreos(node);
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.setLastModified(lastModified);
//		headers.setCacheControl("no-cache");
//		return new ResponseEntity<List<Choreo>>(choreos, headers, HttpStatus.OK);
//	}
//
//	public RestResponse<String> putChoreo(HttpServletResponse response, Choreo choreo) throws IOException {
//		RestResponse<String> resp = new RestResponse<String>();
//		try {
//			choreoService.putChoreo(choreo);
//			resp.setOk(true);
//			resp.setBody(choreo.getApp());
//		} catch (Exception e) {
//			resp.setOk(false);
//			resp.setMessage(e.getMessage());
//			response.sendError(500, e.getMessage());
//		}
//		return resp;
//	}
//
//	public ResponseEntity<Integer> getScale(WebRequest request,
//			String name) throws IOException {
//
//		long lastModified = choreoService.getLastModified(name);
//		if (lastModified < 0) {
//			return new ResponseEntity<Integer>(HttpStatus.NOT_FOUND);
//		}
//
//		if (request.checkNotModified(lastModified)) {
//			return new ResponseEntity<Integer>(HttpStatus.NOT_MODIFIED);
//		}
//
//		Choreo choreo = choreoService.getChoreo(name);
//		if (choreo == null) {
//			return new ResponseEntity<Integer>(HttpStatus.NOT_FOUND);
//		}
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.setLastModified(lastModified);
//		headers.setCacheControl("no-cache");
//		return new ResponseEntity<Integer>(choreo.getScale(), headers, HttpStatus.OK);
//	}
//
//	public RestResponse<Integer> putScale(HttpServletResponse response,
//			String name,
//			int scale) throws IOException {
//
//		RestResponse<Integer> resp = new RestResponse<Integer>();
//		try {
//			choreoService.putScale(name, scale);
//			resp.setOk(true);
//			resp.setBody(scale);
//		} catch (Exception e) {
//			resp.setOk(false);
//			resp.setMessage(e.getMessage());
//			response.sendError(500, e.getMessage());
//		}
//		return resp;
//	}
//
//	public RestResponse<String> putQuery(HttpServletResponse response,
//			String name,
//			String query) throws IOException {
//
//		RestResponse<String> resp = new RestResponse<String>();
//		try {
//			Choreo ch = choreoService.getChoreo(name);
//			ch.setQuery(query);
//			choreoService.putChoreo(ch);
//			resp.setOk(true);
//			resp.setBody(query);
//		} catch (Exception e) {
//			resp.setOk(false);
//			resp.setMessage(e.getMessage());
//			response.sendError(500, e.getMessage());
//		}
//		return resp;
//	}
//
//	public ResponseEntity<String> getQuery(WebRequest request,
//			String name) throws IOException {
//
//		long lastModified = choreoService.getLastModified(name);
//		if (lastModified < 0) {
//			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
//		}
//
//		if (request.checkNotModified(lastModified)) {
//			return new ResponseEntity<String>(HttpStatus.NOT_MODIFIED);
//		}
//
//		Choreo choreo = choreoService.getChoreo(name);
//		if (choreo == null) {
//			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
//		}
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.setLastModified(lastModified);
//		headers.setCacheControl("no-cache");
//		return new ResponseEntity<String>(choreo.getQuery(), headers, HttpStatus.OK);
//	}
//}
