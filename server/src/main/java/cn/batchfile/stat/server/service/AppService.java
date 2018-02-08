package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.agent.types.App;

@Service
public class AppService {
	protected static final Logger log = LoggerFactory.getLogger(AppService.class);
	private cn.batchfile.stat.agent.service.AppService as;

	@Value("${store.directory}")
	private String storeDirectory;
	
	@PostConstruct
	public void init() throws IOException {
		as = new cn.batchfile.stat.agent.service.AppService();
		as.storeDirectory = storeDirectory;
		as.init();
	}
	
	public List<String> getApps() {
		return as.getApps();
	}
	
	public App getApp(String name) throws IOException {
		return as.getApp(name);
	}
	
	public void putApp(App app) throws UnsupportedEncodingException, IOException {
		as.putApp(app);
	}
	
	public void postApp(App app) throws UnsupportedEncodingException, IOException {
		as.postApp(app);
	}
	
	public void deleteApp(String name) {
		//TODO delete choreo
		as.deleteApp(name);
	}
	
	public void putStart(String app, boolean start) throws IOException {
		as.putStart(app, start);
	}

}
