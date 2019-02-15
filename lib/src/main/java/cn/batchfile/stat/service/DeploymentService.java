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

import cn.batchfile.stat.domain.Deployment;

public class DeploymentService {

	protected static final Logger LOG = LoggerFactory.getLogger(DeploymentService.class);
	private File storeDirectory;

	public void setStoreDirectory(String storeDirectory) throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		this.storeDirectory = new File(f, "deployment");
		if (!this.storeDirectory.exists()) {
			FileUtils.forceMkdir(this.storeDirectory);
		}
	}
	
	public long getLastModified() {
		long l = storeDirectory.lastModified();
		for (File f : storeDirectory.listFiles()) {
			if (!StringUtils.startsWith(f.getName(), ".")) {
				if (f.lastModified() > l) {
					l = f.lastModified();
				}
			}
		}
		return l;
	}
	
	public long getLastModified(String servie) {
		File f = new File(storeDirectory, servie);
		if (f.exists()) {
			return f.lastModified();
		} else {
			return -1;
		}
	}

	public List<Deployment> getDeployments() throws IOException {
		List<Deployment> ds = new ArrayList<>();
		File[] files = storeDirectory.listFiles();
		for (File file : files) {
			if (!StringUtils.startsWith(file.getName(), ".")) {
				String json = FileUtils.readFileToString(file, "UTF-8");
				if (StringUtils.isNotEmpty(json)) {
					List<Deployment> list = JSON.parseArray(json, Deployment.class);
					if (list != null) {
						ds.addAll(list);
					}
				}
			}
		}
		return ds;
	}
	
	public List<Deployment> getDeployments(String servie) throws IOException {
		List<Deployment> list = null;
		File f = new File(storeDirectory, servie);
		if (f.exists()) {
			String json = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(json)) {
				list = JSON.parseArray(json, Deployment.class);
			}
		}
		
		if (list == null) {
			list = new ArrayList<>();
		}
		return list;
	}
	
	public void putDeployments(String service, List<Deployment> deployments) throws IOException {
		String json = JSON.toJSONString(deployments, SerializerFeature.PrettyFormat);
		File f = new File(storeDirectory, service);
		FileUtils.writeByteArrayToFile(f, json.getBytes("UTF-8"));
	}
	
	public void deleteDeployment(String servie) {
		File f = new File(storeDirectory, servie);
		FileUtils.deleteQuietly(f);
	}
	
	
}
