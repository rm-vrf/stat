package cn.batchfile.stat.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.Choreo;

public abstract class ChoreoService {
	protected static final Logger log = LoggerFactory.getLogger(ChoreoService.class);
	private File choreoDirectory;
	private String storeDirectory;
	private AppService appService;
	
	public void setStoreDirectory(String storeDirectory) {
		this.storeDirectory = storeDirectory;
	}

	public void setAppService(AppService appService) {
		this.appService = appService;
	}

	public void init() throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		choreoDirectory = new File(f, "choreo");
		if (!choreoDirectory.exists()) {
			FileUtils.forceMkdir(choreoDirectory);
		}
	}
	
	public long getLastModified() {
		long l = choreoDirectory.lastModified();
		for (File f : choreoDirectory.listFiles()) {
			if (StringUtils.startsWith(f.getName(), ".")) {
				continue;
			}
			if (f.lastModified() > l) {
				l = f.lastModified();
			}
		}
		return l;
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
			App appObject = appService.getApp(app);
			if (appObject != null) {
				choreo = new Choreo();
				choreo.setApp(app);
				choreo.setScale(0);
				choreo.setQuery("*");
			}
		}
		return choreo;
	}
	
	public long getLastModified(String name) {
		File f = new File(choreoDirectory, name);
		if (f.exists()) {
			return f.lastModified();
		} else {
			return 0;
		}
	}
	
	public List<Choreo> getChoreos() throws IOException {
		List<Choreo> chs = new ArrayList<Choreo>();
		File[] files = choreoDirectory.listFiles();
		for (File file : files) {
			if (StringUtils.startsWith(file.getName(), ".")) {
				continue;
			}
			
			String s = FileUtils.readFileToString(file, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				Choreo ch = JSON.parseObject(s, Choreo.class);
				chs.add(ch);
			}
		}
		return chs;
	}
	
	public List<Choreo> getChoreos(String node) throws IOException {
		//获取原始列表
		List<Choreo> chs = new ArrayList<Choreo>();
		File[] files = choreoDirectory.listFiles();
		for (File file : files) {
			if (StringUtils.startsWith(file.getName(), ".")) {
				continue;
			}
			
			String s = FileUtils.readFileToString(file, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				Choreo ch = JSON.parseObject(s, Choreo.class);
				chs.add(ch);
			}
		}
		
		//按照节点过滤实例数量
		for (Choreo ch : chs) {
			int scale = 0;
			if (ch.getDist() != null) {
				for (String nodeId : ch.getDist()) {
					if (StringUtils.equals(nodeId, node)) {
						scale ++;
					}
				}
			}
			ch.setScale(scale);
			ch.getDist().clear();
		}
		
		return chs;
	}
	
	public void putChoreo(Choreo choreo) throws IOException {
		String s = JSON.toJSONString(choreo, SerializerFeature.PrettyFormat);
		File f = new File(choreoDirectory, choreo.getApp());
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public int getScale(String app) throws IOException {
		return getChoreo(app).getScale();
	}

	public void putScale(String app, int scale) throws IOException {
		Choreo choreo = getChoreo(app);
		choreo.setScale(scale);
		putChoreo(choreo);
	}
	
	public List<String> getDist(String app) throws IOException {
		return getChoreo(app).getDist();
	}

	public void putDist(String app, List<String> distribution) throws IOException {
		Choreo choreo = getChoreo(app);
		choreo.setDist(distribution);
		putChoreo(choreo);
	}
	
	public void deleteChoreo(String name) {
		File f = new File(choreoDirectory, name);
		FileUtils.deleteQuietly(f);
	}

}
