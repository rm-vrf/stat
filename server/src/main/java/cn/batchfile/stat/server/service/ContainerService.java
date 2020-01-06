//package cn.batchfile.stat.server.service;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import javax.annotation.PostConstruct;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import cn.batchfile.stat.domain.container.Container;
//import cn.batchfile.stat.server.dao.ContainerRepository;
//import cn.batchfile.stat.server.dto.ContainerTable;
//
//@org.springframework.stereotype.Service
//public class ContainerService {
//	
//	private static final Logger LOG = LoggerFactory.getLogger(ContainerService.class);
//	
//	@Autowired
//	private ContainerRepository containerRepository;
//	
////	@PostConstruct
////	public void init() {
////		ContainerTable con = new ContainerTable();
////		con.setId("35f0646aa23f138cc50af76dc3247178bb2c1358005016f40a11fba01dc56cdb");
////		con.setNode("6YIPE2EED2ERGWZ64FOE65LGE3JCZNGF3LS6A6D5X76Z5EZX");
////		con.setNamespace("");
////		con.setService("");
////		con.setName("/frosty_boyd");
////		con.setImage("bobrik/socat");
////		con.setCommand("socat TCP4-LISTEN:2375,fork,reuseaddr UNIX-CONNECT:/var/run/docker.sock");
////		con.setPorts("[]");
////		con.setMounts("[]");
////		con.setState("running");
////		con.setCreateTime(new Date(1577226627000L));
////		con.setStopTime(null);
////		con.setStartTime(new Date(1577226627000L));
////		con.setRequestCpus(0.1F);
////		con.setRequestMemory(1024L);
////		
////		containerRepository.save(con);
////	}
//	
//	public List<Container> getContainersAll() {
//		List<Container> containers = new ArrayList<Container>();
//		Iterable<ContainerTable> it = containerRepository.findAll();
//		it.forEach(i -> {
//			Container container = new Container();
//			container.setId(i.getId());
//			container.setNode(i.getNode());
//			container.setNamespace(i.getNamespace());
//			container.setName(i.getName());
//			container.setImage(i.getImage());
//			container.setCommand(i.getCommand());
//			container.setCreateTime(i.getCreateTime());
//			container.setState(i.getState());
//			containers.add(container);
//		});
//		return containers;
//	}
//	
//	public List<Container> getContainersByNode(String nodeId) {
//		List<Container> containers = new ArrayList<Container>();
//		Iterable<ContainerTable> it = containerRepository.fineByNodeId(nodeId);
//		it.forEach(i -> {
//			Container container = new Container();
//			container.setId(i.getId());
//			container.setNode(i.getNode());
//			container.setNamespace(i.getNamespace());
//			container.setName(i.getName());
//			container.setImage(i.getImage());
//			container.setCommand(i.getCommand());
//			container.setCreateTime(i.getCreateTime());
//			container.setState(i.getState());
//			containers.add(container);
//		});
//		return containers;
//	}
//
//}
