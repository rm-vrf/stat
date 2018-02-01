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
import com.alibaba.fastjson.serializer.SerializerFeature;

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
		//check name
		checkAppName(app.getName());
		
		String s = JSON.toJSONString(app, SerializerFeature.PrettyFormat);
		File f = new File(appDirectory, app.getName());
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public void postApp(App app) throws UnsupportedEncodingException, IOException {
		//check name
		App a = getApp(app.getName());
		if (a != null) {
			throw new RuntimeException("Duplicated application name");
		}

		//put data
		putApp(app);
	}
	
	public void deleteApp(String name) {
		File f = new File(appDirectory, name);
		FileUtils.deleteQuietly(f);
	}
	
	public void putScale(String name, int scale) throws IOException {
		App app = getApp(name);
		app.setScale(scale);
		putApp(app);
	}
	
	public void putStart(String name, boolean start) throws IOException {
		App app = getApp(name);
		app.setStart(start);
		putApp(app);
	}

	private void checkAppName(String name) {
		for (int i = 0; i < name.length(); i ++) {
			char c = name.charAt(i);
			if (!validChar(c)) {
				throw new RuntimeException("Invalid char in application name: " + c);
			}
		}
	}

	private boolean validChar(char c) {
		if (c >= '0' && c <= '9') {
			return true;
		}
		
		if (c >= 'a' && c <= 'z') {
			return true;
		}
		
		if (c >= 'A' && c <= 'Z') {
			return true;
		}
		
		if (c == '_' || c == '-') {
			return true;
		}
		
		return false;
	}
}
