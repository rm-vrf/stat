//package cn.batchfile.stat.server.controller;
//
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//
//import cn.batchfile.stat.domain.container.Container;
//import cn.batchfile.stat.server.service.ContainerService;
//
//@RestController
//public class ContainerController {
//	private static final Logger LOG = LoggerFactory.getLogger(ContainerController.class);
//	
//	@Autowired
//	private ContainerService containerService;
//	
//	@GetMapping("/api/container")
//	public List<Container> getContainersAll() {
//		return containerService.getContainersAll();
//	}
//	
//	@GetMapping("/api/node/{nodeId}/container")
//	public List<Container> getContainersByNode(@PathVariable("nodeId") String nodeId) {
//		return containerService.getContainersByNode(nodeId);
//	}
//}
