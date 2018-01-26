package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

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

	@RequestMapping(value="/v1/app/{name}/scale", method=RequestMethod.GET)
	public int getScale(@PathVariable("name") String name) throws IOException {
		return appService.getScale(name);
	}

	@RequestMapping(value="/v1/app/{name}/scale", method=RequestMethod.PUT)
	public void putScale(@PathVariable("name") String name, @RequestParam("scale") int scale) throws UnsupportedEncodingException, IOException {
		appService.putScale(name, scale);
	}
}
