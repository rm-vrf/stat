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
import cn.batchfile.stat.agent.types.Choreo;

@Service
public class AppService {
	protected static final Logger log = LoggerFactory.getLogger(AppService.class);
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	private File appDirectory;
	private File choreoDirectory;
	
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
		
		choreoDirectory = new File(f, "choreo");
		if (!choreoDirectory.exists()) {
			FileUtils.forceMkdir(choreoDirectory);
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
		//检查名称中的非法字符
		checkAppName(app.getName());
		
		//创建App文件
		String s = JSON.toJSONString(app, SerializerFeature.PrettyFormat);
		File f = new File(appDirectory, app.getName());
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public void postApp(App app) throws UnsupportedEncodingException, IOException {
		//检查重复的文件名
		App a = getApp(app.getName());
		if (a != null) {
			throw new RuntimeException("Duplicated application name: " + app.getName());
		}

		//保存数据
		putApp(app);
	}
	
	public void deleteApp(String name) {
		File f = new File(appDirectory, name);
		FileUtils.deleteQuietly(f);
		
		File f2 = new File(choreoDirectory, name);
		FileUtils.deleteQuietly(f2);
	}
	
	public void putScale(String app, int scale) throws IOException {
		Choreo choreo = getChoreo(app);
		choreo.setScale(scale);
		putChoreo(choreo);
	}
	
	public void putStart(String app, boolean start) throws IOException {
		Choreo choreo = getChoreo(app);
		choreo.setStart(start);
		putChoreo(choreo);
	}
	
	public Choreo getChoreo(String app) throws IOException {
		Choreo choreo = null;
		File f = new File(choreoDirectory, app);
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				choreo = JSON.parseObject(s, Choreo.class);
			}
		}

		//如果没有值，给一个默认值
		if (choreo == null) {
			App appObject = getApp(app);
			if (appObject != null) {
				choreo = new Choreo();
				choreo.setApp(app);
				choreo.setScale(0);
				choreo.setStart(false);
			}
		}
		return choreo;
	}
	
	private void putChoreo(Choreo choreo) throws IOException {
		String s = JSON.toJSONString(choreo, SerializerFeature.PrettyFormat);
		File f = new File(choreoDirectory, choreo.getApp());
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
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
