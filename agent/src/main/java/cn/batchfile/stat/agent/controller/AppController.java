package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.agent.service.AppService;
import cn.batchfile.stat.agent.types.App;

@RestController
public class AppController {
	protected static final Logger log = LoggerFactory.getLogger(AppController.class);
	
	@Autowired
	private AppService appService;
	
	@RequestMapping(value="/v1/app", method=RequestMethod.GET)
	public List<String> getApps() {
		return appService.getApps();
	}
	
	@RequestMapping(value="/v1/app/{name}", method=RequestMethod.GET)
	public App getApp(@PathVariable("name") String name) throws IOException {
		return appService.getApp(name);
	}
	
	@RequestMapping(value="/v1/app", method=RequestMethod.PUT)
	public void putApp(@RequestBody App app) throws UnsupportedEncodingException, IOException {
		appService.putApp(app);
	}
	
	@RequestMapping(value="/v1/app/{name}", method=RequestMethod.DELETE)
	public void deleteApp(@PathVariable("name") String name) {
		appService.deleteApp(name);
	}

	@RequestMapping(value="/v1/app/{name}/_scale", method=RequestMethod.POST)
	public void putScale(@PathVariable("name") String name, @RequestParam("num") int scale) throws IOException {
		appService.putScale(name, scale);
	}
	
	@RequestMapping(value="/v1/app/{name}/_start", method=RequestMethod.POST)
	public void startApp(@PathVariable("name") String name) throws IOException {
		appService.putStart(name, true);
	}

	@RequestMapping(value="/v1/app/{name}/_stop", method=RequestMethod.POST)
	public void stopApp(@PathVariable("name") String name) throws IOException {
		appService.putStart(name, false);
	}
}
