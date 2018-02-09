package cn.batchfile.stat.server.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChoreoService extends cn.batchfile.stat.service.ChoreoService {
	
	@Value("${store.directory}")
	@Override
	public void setStoreDirectory(String storeDirectory) {
		super.setStoreDirectory(storeDirectory);
	}
	
	@Autowired
	@Override
	public void setAppService(cn.batchfile.stat.service.AppService appService) {
		super.setAppService(appService);
	}

	@PostConstruct
	public void init() throws IOException {
		super.init();
	}

}
