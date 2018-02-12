package cn.batchfile.stat.agent.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.batchfile.stat.domain.App;
import cn.batchfile.stat.domain.Choreo;
import cn.batchfile.stat.domain.Node;
import cn.batchfile.stat.domain.Proc;
import cn.batchfile.stat.domain.RestResponse;

@Service
public class ShakehandService {
	protected static final Logger log = LoggerFactory.getLogger(ShakehandService.class);
	
	@Value("${agent.address:}")
	private String agentAddress;
	
	@Value("${master.address:}")
	private String masterAddress;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ProcService procService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private AppService appService;
	
	@Autowired
	private ChoreoService choreoService;
	
	@Scheduled(fixedDelay = 5000)
	public void shakehand() throws IOException {
		if (StringUtils.isEmpty(masterAddress)) {
			return;
		}
		
		//提交节点状态
		submitNode();
		
		//提交进程信息
		submitProcs();
		
		//同步应用信息
		refreshApps();
	}
	
	private void refreshApps() throws UnsupportedEncodingException, IOException {
		List<String> existNames = appService.getApps();
		
		//抓应用数据
		String url = String.format("%s/v1/app", masterAddress);
		String[] ary = restTemplate.getForObject(url, String[].class);
		List<String> appNames = Arrays.asList(ary);
		
		//遍历应用数据，更新应用数据
		for (Object appName : appNames) {
			url = String.format("%s/v1/app/%s", masterAddress, appName);
			App app = restTemplate.getForObject(url, App.class);
			if (existNames.contains(appName)) {
				appService.putApp(app);
			} else {
				appService.postApp(app);
			}
		}
		
		//更新编排数据
		url = String.format("%s/v1/choreo?node=%s", masterAddress, nodeService.getNode().getId());
		Choreo[] choreos = restTemplate.getForObject(url, Choreo[].class);
		for (Choreo choreo : choreos) {
			choreoService.putScale(choreo.getApp(), choreo.getScale());
		}
		
		//删除多余的应用
		for (String existName : existNames) {
			if (!appNames.contains(existName)) {
				appService.deleteApp(existName);
			}
		}
	}
	
	private void submitNode() throws IOException {
		Node node = nodeService.getNode();
		String url = String.format("%s/v1/shakehand/node", masterAddress);
		RestResponse<?> resp = restTemplate.postForObject(url, node, RestResponse.class);
		log.debug("submit node, resp: {}", resp.isOk());
	}
	
	private void submitProcs() throws IOException {
		Node node = nodeService.getNode();
		List<Proc> ps = new ArrayList<Proc>();
		List<Long> pids = procService.getProcs();
		for (Long pid : pids) {
			Proc p = procService.getProc(pid);
			if (p != null) {
				ps.add(p);
			}
		}

		String url = String.format("%s/v1/shakehand/proc?id=%s", masterAddress, 
				new URLEncoder().encode(node.getId(), Charset.forName("UTF-8")));
		
		RestResponse<?> resp = restTemplate.postForObject(url, ps, RestResponse.class);
		log.debug("submit ps, resp: {}", resp.isOk());
	}
}
