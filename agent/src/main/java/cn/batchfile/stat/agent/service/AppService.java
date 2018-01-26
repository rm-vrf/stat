package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.agent.types.App;

@Service
public class AppService {
	protected static final Logger log = LoggerFactory.getLogger(AppService.class);
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	private File appDirectory;
	
	@PostConstruct
	public void init() throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		appDirectory = new File(f, "app");
		if (!appDirectory.exists()) {
			FileUtils.forceMkdir(appDirectory);
		}
	}
	
	public List<String> getApps() {
		List<String> apps = new ArrayList<String>();
		String[] files = appDirectory.list();
		for (String file : files) {
			if (StringUtils.startsWith(file, ".")) {
				continue;
			}
			apps.add(file);
		}
		return apps;
	}
	
	public App getApp(String name) throws IOException {
		App app = null;
		File f = new File(appDirectory, name);
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				app = JSON.parseObject(s, App.class);
			}
		}
		return app;
	}
	
	public void putApp(App app) throws UnsupportedEncodingException, IOException {
		String s = JSON.toJSONString(app);
		File f = new File(appDirectory, app.getName());
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public void deleteApp(String name) {
		File f = new File(appDirectory, name);
		FileUtils.deleteQuietly(f);
	}
	
	public int getScale(String name) throws IOException {
		App app = getApp(name);
		return app.getScale();
	}
	
	public void putScale(String name, int scale) throws UnsupportedEncodingException, IOException {
		App app = getApp(name);
		app.setScale(scale);
		putApp(app);
	}
}
