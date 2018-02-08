package cn.batchfile.stat.server.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.agent.types.App;
import cn.batchfile.stat.server.types.Choreo;

@Service
public class ChoreoService {
	protected static final Logger log = LoggerFactory.getLogger(ChoreoService.class);
	private File choreoDirectory;
	
	@Value("${store.directory}")
	private String storeDirectory;
	
	@Autowired
	private AppService appService;

	@PostConstruct
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

	@Scheduled(fixedDelay = 5000)
	public void refresh() throws IOException {
		//TODO
		//遍历应用列表，根据当前的节点健康情况为每一个应用分配运行实例
		for (String appName : appService.getApps()) {
			Choreo choreo = getChoreo(appName);
			List<String> distribution = calcDistribute(appName, choreo);
			putDistribution(appName, distribution);
		}
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

	public void putChoreo(Choreo choreo, boolean keepDistribution) throws IOException {
		//保持原定的分配方式
		if (keepDistribution) {
			Choreo c = getChoreo(choreo.getApp());
			if (c != null) {
				choreo.setDistribution(c.getDistribution());
			}
		}
		
		//存储编排结果
		String s = JSON.toJSONString(choreo, SerializerFeature.PrettyFormat);
		File f = new File(choreoDirectory, choreo.getApp());
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public void deleteChoreo(String name) {
		File f = new File(choreoDirectory, name);
		FileUtils.deleteQuietly(f);
	}
	
	private List<String> calcDistribute(String app, Choreo choreo) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	private void putDistribution(String app, List<String> distribution) throws IOException {
		Choreo choreo = getChoreo(app);
		if (choreo != null) {
			choreo.getDistribution().clear();
			choreo.getDistribution().addAll(distribution);
			putChoreo(choreo, false);
		}
	}
	

}
