package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.domain.Event;
import cn.batchfile.stat.domain.PaginationList;
import cn.batchfile.stat.server.service.EventService;

@RestController
public class EventController {
	protected static final Logger log = LoggerFactory.getLogger(EventController.class);
	private static final int SIZE = 256;
	
	@Autowired
	private EventService eventService;
	
	@GetMapping("/v1/event/count")
	public ResponseEntity<Long> eventsCount() throws IOException {
		long count = eventService.searchEvent(eventService.getTimestamp(), 0).getTotal();
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Long>(count, headers, HttpStatus.OK);
	}
	
	@GetMapping("/v1/event/_search")
	public ResponseEntity<PaginationList<Event>> searchEvents(
			@RequestParam(name="timestamp", defaultValue="0") long timestamp) {
		
		PaginationList<Event> events = eventService.searchEvent(new Date(timestamp), SIZE);
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<PaginationList<Event>>(events, headers, HttpStatus.OK);
	}
	
	@PostMapping("/v1/event/lastTime")
	public ResponseEntity<Long> postLastTime(@RequestParam("timestamp") long timestamp) throws IOException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		
		try {
			eventService.setTimestamp(new Date(timestamp));
			return new ResponseEntity<Long>(timestamp, headers, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Long>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
