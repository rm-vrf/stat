package cn.batchfile.stat.server.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.batchfile.stat.server.dao.ContainerRepository;

@org.springframework.stereotype.Service
public class ScheduleService {
	private static final Logger LOG = LoggerFactory.getLogger(ScheduleService.class);

	@Autowired
	private ContainerRepository containerRepository;
	
	@Autowired
	private NodeService nodeService;

	@PostConstruct
	public void init() {
		
	}
}
