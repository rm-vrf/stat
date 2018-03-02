package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.domain.RestResponse;
import cn.batchfile.stat.server.service.EventService;

@RestController
public class EventController {
	protected static final Logger log = LoggerFactory.getLogger(EventController.class);
	
	@Autowired
	private EventService eventService;
	
	@PostMapping("/v1/event")
	public RestResponse<Integer> postEvent(HttpServletResponse response,
			@RequestBody List<Event> events) throws IOException {
		
		RestResponse<Integer> resp = new RestResponse<Integer>();
		try {
			eventService.postEvents(events);
			resp.setBody(events.size());
			resp.setOk(true);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
	
	@GetMapping("/v1/event/_count")
	public int countEvents() {
		return i ++;
	}
	int i = 0;
	
	@GetMapping("/v1/event/_search")
	public PaginationList<Event> searchEvents() {

		return new PaginationList<Event>(0, new ArrayList<Event>());
	}
	
	@PostMapping("/v1/event/_lastTime")
	public RestResponse<Long> postLastTime(HttpServletResponse response,
			@RequestParam("timestamp") long timestamp) throws IOException {
		
		RestResponse<Long> resp = new RestResponse<Long>();
		try {
			resp.setBody(timestamp);
			resp.setOk(true);
		} catch (Exception e) {
			resp.setOk(false);
			resp.setMessage(e.getMessage());
			response.sendError(500, e.getMessage());
		}
		return resp;
	}
}
