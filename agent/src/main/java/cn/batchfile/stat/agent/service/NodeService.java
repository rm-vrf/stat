package cn.batchfile.stat.agent.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.batchfile.stat.agent.types.Node;

@Service
public class NodeService {
	protected static final Logger log = LoggerFactory.getLogger(NodeService.class); 
	
	private Node node;

	@Value("${store.directory}")
	private String storeDirectory;
	
	@Autowired
	private SysService sysService;
	
	@PostConstruct
	public void init() throws IOException {
		node = new Node();
		node.setId(getId());
		node.setHostname(sysService.getHostname());
		node.setOs(sysService.getOs());
		//node.setDisks(sysService.getDisks());
		//node.setMemory(sysService.getMemory());
		//node.setNetworks(sysService.getNetworks());

		File f = new File(storeDirectory);
		if (!f.exists()) {
			FileUtils.forceMkdir(f);
		}
	}
	
	public Node getNode() {
		return node;
	}
	
	public List<String> getTags() throws IOException {
		List<String> tags = new ArrayList<String>();
		File f = new File(new File(storeDirectory), "tag");
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				tags = JSON.parseArray(s, String.class);
			}
		}
		return tags;
	}
	
	public void putTags(List<String> tags) throws UnsupportedEncodingException, IOException {
		String s = JSON.toJSONString(tags, SerializerFeature.PrettyFormat);
		File f = new File(new File(storeDirectory), "tag");
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	public Map<String, String> getEnvs() throws IOException {
		Map<String, String> envs = new HashMap<String, String>();
		File f = new File(new File(storeDirectory), "env");
		if (f.exists()) {
			String s = FileUtils.readFileToString(f, "UTF-8");
			if (StringUtils.isNotEmpty(s)) {
				envs = JSON.parseObject(s, new TypeReference<Map<String, String>>(){});
			}
		}
		return envs;
	}
	
	public void putEnvs(Map<String, String> envs) throws UnsupportedEncodingException, IOException {
		String s = JSON.toJSONString(envs, SerializerFeature.PrettyFormat);
		File f = new File(new File(storeDirectory), "env");
		FileUtils.writeByteArrayToFile(f, s.getBytes("UTF-8"));
	}
	
	private String getId() throws IOException {
		String id = StringUtils.EMPTY;
		
		File f = new File(new File(storeDirectory), "id");
		if (f.exists()) {
			id = FileUtils.readFileToString(f, "UTF-8");
		} else {
			id = StringUtils.remove(UUID.randomUUID().toString(), "-");
			FileUtils.writeByteArrayToFile(f, id.getBytes("UTF-8"));
		}
		
		return id;
	}
}
