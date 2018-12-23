package cn.batchfile.stat.agent.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.agent.service.EventService;
import cn.batchfile.stat.domain.Event;

@RestController
public class EventController {

	@Autowired
	private EventService eventService;
	
	@GetMapping("/api/v2/event")
	public ResponseEntity<List<Event>> getHealthCheck(WebRequest request) {
		
		List<Event> events = eventService.getEvents();
		HttpHeaders headers = new HttpHeaders();
		//headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Event>>(events, headers, HttpStatus.OK);
	}
	
}
