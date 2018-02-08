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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.agent.types.App;

@Service
public class AppService {
	protected static final Logger log = LoggerFactory.getLogger(AppService.class);
	private static final char[] INVALID_CHARS = new char[] {
			'`', '~', '!', '@', '#', 
			'$', '%', '^', '&', '*', 
			'+', '=', '\t', '|', '\\', 
			':', '"', '\'', '<', '>', 
			'.', '?', '/', ' '
	};
	private File appDirectory;
	
	@Value("${store.directory}")
	public String storeDirectory;
	
	@Autowired
	private ChoreoService choreoService;
	
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
		//检查名称中的非法字符
		checkAppName(app.getName());
		
		//检查进程名称
		checkProcessName(app.getToProcess());
		
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
		if (choreoService != null) {
			choreoService.deleteChoreo(name);
		}
		
		File f = new File(appDirectory, name);
		FileUtils.deleteQuietly(f);
	}
	
	public void putStart(String app, boolean start) throws IOException {
		App appObject = getApp(app);
		appObject.setStart(start);
		putApp(appObject);
	}
	
	private void checkAppName(String name) {
		if (StringUtils.isBlank(name)) {
			throw new RuntimeException("Empty application name");
		}
		
		if (StringUtils.containsAny(name, INVALID_CHARS)) {
			throw new RuntimeException("Invalid char in application name: " + name);
		}
	}
	
	private void checkProcessName(String toProcess) {
		if (StringUtils.isBlank(toProcess)) {
			throw new RuntimeException("Empty process name");
		}
	}

}
