package cn.batchfile.stat.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.domain.Deploy;
import cn.batchfile.stat.domain.Service;

/**
 * 服务
 * @author lane.cn@gmail.com
 *
 */
public class ServiceService {

	protected static final Logger log = LoggerFactory.getLogger(ServiceService.class);
	private static final char[] INVALID_CHARS = new char[] {
			'`', '~', '!', '@', '#',
			'$', '%', '^', '&', '*',
			'+', '=', '\t', '|', '\\',
			':', '"', '\'', '<', '>',
			'.', '?', '/', ' '
	};
	private File storeDirectory;
	
	/**
	 * 设置存储目录
	 * @param storeDirectory 存储目录
	 * @throws IOException 异常
	 */
	public void setStoreDirectory(String storeDirectory) throws IOException {
		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
		
		this.storeDirectory = new File(f, "service");
		if (!this.storeDirectory.exists()) {
			FileUtils.forceMkdir(this.storeDirectory);
		}
	}

	/**
	 * 获取修改时间
	 * @return 修改时间
	 */
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

	/**
	 * 获取服务
	 * @return 服务
	 * @throws IOException 异常
	 */
	public List<Service> getServices() throws IOException {
		List<Service> services = new ArrayList<Service>();
		File[] files = storeDirectory.listFiles();
		for (File file : files) {
			if (!StringUtils.startsWith(file.getName(), ".")) {
				String json = FileUtils.readFileToString(file, "UTF-8");
				Service object = JSON.parseObject(json, Service.class);
				object.setName(file.getName());
				services.add(object);
			}
		}
		
		Collections.sort(services, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		return services;
	}

	/**
	 * 获取修改事件
	 * @param name 服务名称
	 * @return 修改时间
	 */
	public long getLastModified(String name) {
		File f = new File(storeDirectory, name);
		if (f.exists()) {
			return f.lastModified();
		} else {
			return -1;
		}
	}
	
	/**
	 * 获取服务
	 * @param name 名称
	 * @return 服务
	 * @throws IOException 异常
	 */
	public Service getService(String name) throws IOException {
		Service s = null;
		File f = new File(storeDirectory, name);
		if (f.exists()) {
			String json = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(json)) {
				s = JSON.parseObject(json, Service.class);
			}
		}
		return s;
	}

	/**
	 * 修改服务
	 * @param service 服务
	 * @throws IOException 异常
	 */
	public void putService(Service service) throws IOException {
		//检查名称中的非法字符
		checkName(service.getName());
		
		//创建存储文件
		String json = JSON.toJSONString(service, SerializerFeature.PrettyFormat);
		File f = new File(storeDirectory, service.getName());
		FileUtils.writeByteArrayToFile(f, json.getBytes("UTF-8"));
	}
	
	/**
	 * 添加服务
	 * @param service 服务
	 * @throws IOException 异常
	 */
	public void postService(Service service) throws IOException {
		//检查重复的文件名
		if (getService(service.getName()) != null) {
			throw new RuntimeException("Duplicated service name: " + service.getName());
		}
		
		//保存数据
		putService(service);
	}
	
	/**
	 * 删除服务
	 * @param name 名称
	 */
	public void deleteService(String name) {
		File f = new File(storeDirectory, name);
		FileUtils.deleteQuietly(f);
	}
	
	/**
	 * 设置副本数
	 * @param name 名称
	 * @param replicas 副本数
	 * @throws IOException 异常
	 */
	public void setReplicas(String name, int replicas) throws IOException {
		Service service = getService(name);
		if (service.getDeploy() == null) {
			service.setDeploy(new Deploy());
		}
		service.getDeploy().setReplicas(replicas);
		
		putService(service);
	}
	
	/**
	 * 检查名称
	 * @param name 名称
	 */
	private void checkName(String name) {
		if (StringUtils.isBlank(name)) {
			throw new RuntimeException("Empty service name");
		}
		
		if (StringUtils.containsAny(name, INVALID_CHARS)) {
			throw new RuntimeException("Invalid char in service name: " + name);
		}
	}
	
}
