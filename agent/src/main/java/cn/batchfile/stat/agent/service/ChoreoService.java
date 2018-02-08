package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;

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
import cn.batchfile.stat.agent.types.Choreo;

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
			}
		}
		return choreo;
	}
	
	private void putChoreo(Choreo choreo) throws IOException {
		String s = JSON.toJSONString(choreo, SerializerFeature.PrettyFormat);
		File f = new File(choreoDirectory, choreo.getApp());
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}

	public void putScale(String app, int scale) throws IOException {
		Choreo choreo = getChoreo(app);
		choreo.setScale(scale);
		putChoreo(choreo);
	}
	
	public void deleteChoreo(String name) {
		File f = new File(choreoDirectory, name);
		FileUtils.deleteQuietly(f);
	}
}
