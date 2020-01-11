package cn.batchfile.stat.server.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.dockerjava.api.model.Container;

import cn.batchfile.stat.server.domain.container.ContainerInstance;
import cn.batchfile.stat.server.domain.node.Node;

@org.springframework.stereotype.Service
public class ScheduleService {
	private static final int ONLINE_POOL_SIZE = 2;
	private static final int OFFLINE_POOL_SIZE = 1;
	private static final int ONLINE_REFRESH_INTERVAL = 5;
	private static final int OFFLINE_REFRESH_INTERVAL = 10;
	private static final int PAGE_SIZE = 50;
	private static final Logger LOG = LoggerFactory.getLogger(ScheduleService.class);

	@Autowired
	private ContainerService containerService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private MasterService masterService;
	
	@Autowired
	private DockerService dockerService;

	@PostConstruct
	public void init() {
		/** 
		 * 分离两种状态的定时器，避免离线节点卡住流程，影响数据同步的性能
		 */
		
		//同步在线节点的定时器
		setupSchedule(new String[] {Node.STATUS_ONLINE, Node.STATUS_CREATED}, 
				ONLINE_POOL_SIZE, ONLINE_REFRESH_INTERVAL);

		//同步离线节点的定时器
		setupSchedule(new String[] {Node.STATUS_OFFLINE, Node.STATUS_UNKNOWN}, 
				OFFLINE_POOL_SIZE, OFFLINE_REFRESH_INTERVAL);
	}
	
	private void setupSchedule(String[] status, int poolSize, int refreshInterval) {
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
			//判断当前节点是不是master
			if (masterService.isMaster()) {
				//同步离线节点和未知节点
				try {
					refreshNodes(status, poolSize);
				} catch (Exception e) {
					LOG.error("error when refresh nodes", e);
				}
			}
		}, 10, refreshInterval, TimeUnit.SECONDS);
	}
	
	private void refreshNodes(String[] status, int poolSize) {
		//遍历节点
		Pageable pageable = PageRequest.of(0, PAGE_SIZE);
		Page<Node> page = nodeService.searchNodes(status, pageable);
		while (!page.isEmpty()) {
			//遍历节点，逐个刷新
			try {
				ExecutorService es = Executors.newFixedThreadPool(poolSize);
				page.getContent().forEach(node -> {
					//创建Runnable对象
					es.submit(() -> {
						refreshNode(node);
					});
				});
				es.shutdown();

				//根据数据量计算合适的超时时间
				int timeout = page.getContent().size() * dockerService.getConnectTimeoutSeconds();
				es.awaitTermination(timeout, TimeUnit.SECONDS);
				LOG.debug("---- finish refresh loop ----");
			} catch (Exception e) {
				LOG.error("error when refresh nodes", e);
			}
			
			//遍历下一页数据
    		if (page.hasNext()) {
    			page = nodeService.getNodes(page.nextPageable());
    		} else {
    			break;
    		}
		}
	}
	
	private void refreshNode(Node node) {
		//从远程接口刷新节点
		Node newNode = nodeService.refreshNode(node);
		
		//在线节点同步容器
		if (StringUtils.equals(newNode.getStatus(), Node.STATUS_ONLINE)) {
			refreshContainers(newNode);
		}
	}
	
	private void refreshContainers(Node node) {
		//从远程节点获取所有的容器，包括所有的状态
		List<Container> containerList = dockerService.listContainers(node.getInfo().getDockerHost(), node.getApiVersion(), true);
		Map<String, Container> remoteContainers = new HashMap<>();
		for (Container container : containerList) {
			remoteContainers.put(container.getId(), container);
		}
		
		//从数据库获取所有容器，遍历更新
		Pageable pageable = PageRequest.of(0, 50);
		Page<ContainerInstance> page = containerService.getContainersByNode(node.getId(), pageable);
		while (!page.isEmpty()) {
			//遍历节点，逐个刷新
			page.getContent().forEach(instance -> {
				try {
					//刷新数据
					LOG.debug("save container instance: {}, node: {}", instance.getId(), node.getInfo().getDockerHost());
					containerService.refreshContainer(instance, remoteContainers.get(instance.getId()), node);
				} catch (Exception e) {
					LOG.error("error when refresh container: {}", instance.getId());
				} finally {
					//刷新后从集合中删除容器
					remoteContainers.remove(instance.getId());
				}
			});
			
			//遍历下一页数据
    		if (page.hasNext()) {
    			page = containerService.getContainersByNode(node.getId(), page.nextPageable());
    		} else {
    			break;
    		}
		}
		
		//遍历完毕之后留在集合中的容器，数据库里缺少的容器，需要添加到数据库
		for (Container container : remoteContainers.values()) {
			try {
				//刷新数据
				LOG.debug("new container, image: {}, node: {}", container.getImage(), node.getInfo().getDockerHost());
				containerService.refreshContainer(null, container, node);
			} catch (Exception e) {
				LOG.error("error when refresh container: {}", container.getId());
			}
		}
	}
}
