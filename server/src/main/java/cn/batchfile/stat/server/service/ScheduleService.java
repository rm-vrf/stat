package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.batchfile.stat.server.domain.node.Node;

@org.springframework.stereotype.Service
public class ScheduleService {
	private static final Logger LOG = LoggerFactory.getLogger(ScheduleService.class);
	private BlockingQueue<List<Node>> quickQueue = new LinkedBlockingQueue<>(2);
	private BlockingQueue<List<Node>> slowQueue = new LinkedBlockingQueue<>(2);

	@Autowired
	private ContainerService containerService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private MasterService masterService;

	@PostConstruct
	public void init() {
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
			//判断当前节点是不是master
			if (!masterService.isMaster()) {
				return;
			}
			
			//获取所有节点
			List<Node> nodes = null;
			try {
				nodes = new ArrayList<>();//.getNodes();//TODO for debug
			} catch (Exception e) {
				LOG.error("error when get node data", e);
				return;
			}

			//创建快队列和慢队列
			List<Node> quick = new ArrayList<>();
			List<Node> slow = new ArrayList<>();
			
			//遍历所有节点
			refreshNodes(quick, slow, nodes);
			
			//消息入队
			quickQueue.offer(quick);
			slowQueue.offer(slow);
		}, 20, 2, TimeUnit.SECONDS);
		
		Executors.newScheduledThreadPool(2).scheduleWithFixedDelay(() -> {
			//判断当前节点是不是master
			if (!masterService.isMaster()) {
				return;
			}
			
			try {
				//从慢队列中取得消息，同步容器
				List<Node> nodes = slowQueue.take();
				refreshContainers(nodes);
			} catch (Exception e) {
				LOG.error("error when refresh container", e);
			}
		}, 20, 2, TimeUnit.SECONDS);

		Executors.newScheduledThreadPool(4).scheduleWithFixedDelay(() -> {
			//判断当前节点是不是master
			if (!masterService.isMaster()) {
				return;
			}
			
			try {
				//从快队列中取得消息，同步容器
				List<Node> nodes = quickQueue.take();
				refreshContainers(nodes);
			} catch (Exception e) {
				LOG.error("error when refresh container", e);
			}
		}, 20, 2, TimeUnit.SECONDS);
	}
	
	private void refreshNodes(List<Node> quick, List<Node> slow, List<Node> nodes) {
		for (Node n : nodes) {
			try {
				//刷新节点
				Node node = nodeService.refreshNode(n);
				
				//刷新容器，只刷在线的节点
				String newStatus = node.getStatus();
				
				//在线节点入队，准备同步容器
				if (StringUtils.equals(newStatus, Node.STATUS_ONLINE)) {
					if (node.getSlow()) {
						slow.add(node);
					} else {
						quick.add(node);
					}
				}
			} catch (Exception e) {
				LOG.error("error when refresh node: " + n.getId(), e);
			}
		}
	}
	
	private void refreshContainers(List<Node> nodes) {
		for (Node node : nodes) {
			try {
				LOG.debug("refresh container data, node: {}", node.getId());
				containerService.refreshContainer(node);
			} catch (Exception e) {
				LOG.error("error when refresh container, node: " + node.getId(), e);
			}
		}
	}
}
