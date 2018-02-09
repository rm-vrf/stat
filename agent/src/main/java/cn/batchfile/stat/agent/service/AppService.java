package cn.batchfile.stat.agent.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppService extends cn.batchfile.stat.service.AppService {
	
	@Value("${store.directory}")
	@Override
	public void setStoreDirectory(String storeDirectory) {
		super.setStoreDirectory(storeDirectory);
	}
	
	@Autowired
	@Override
	public void setChoreoService(cn.batchfile.stat.service.ChoreoService choreoService) {
		super.setChoreoService(choreoService);
	}
	
	@PostConstruct
	public void init() throws IOException {
		super.init();
	}

}
