package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import cn.batchfile.stat.domain.node.Node;
import cn.batchfile.stat.server.util.YamlUtils;

@org.springframework.stereotype.Service
public class NodeService {

	private static final Logger LOG = LoggerFactory.getLogger(NodeService.class);
	
    @Autowired
    private ZookeeperService zookeeperService;

    /**
     * 获取节点数量
     * @return 数量
     */
    public int getNodeCount() {
    	String path = ZookeeperService.NODE_PATH;
    	if (!zookeeperService.exist(path)) {
            return 0;
        }
    	
    	List<String> ids = zookeeperService.getChildren(path);
    	LOG.debug("ids count: {}", ids.size());
    	return ids.size();
    }
    
    /**
     * 得到所有节点
     * @return 所有节点
     */
    public List<Node> getNodes() {
    	String path = ZookeeperService.NODE_PATH;
    	if (!zookeeperService.exist(path)) {
            return null;
        }
    	
    	List<String> ids = zookeeperService.getChildren(path);
    	LOG.debug("ids count: {}", ids.size());
    	
    	List<Node> nodes = new ArrayList<Node>();
    	for (String id : ids) {
    		Node node = getNode(id);
    		if (node != null) {
    			nodes.add(node);
    		}
    	}
    	return nodes;
    }

    /**
     * 获取节点
     * @param id 编号
     * @return 节点
     */
    public Node getNode(String id) {
    	String path = ZookeeperService.NODE_PATH + "/" + id;
        if (!zookeeperService.exist(path)) {
            return null;
        }

        String yaml = zookeeperService.getData(path);
        Node node = YamlUtils.toObject(yaml, Node.class);
        return node;
    }
    
    /**
     * 创建节点
     * @param node 节点
     * @return 节点
     */
    public Node createNode(Node node) {
    	LOG.info("create node, {}", node.getId());
    	String path = ZookeeperService.NODE_PATH + "/" + node.getId();
    	if (zookeeperService.exist(path)) {
    		throw new RuntimeException("Node has already existed, id: " + node.getId());
    	}
    	
        String yaml = YamlUtils.toString(node);
        zookeeperService.createPersistent(path, yaml);
        return node;
    }

    /**
     * 更新节点
     * @param node 节点
     * @return 节点
     */
    public Node updateNode(Node node) {
    	LOG.info("update node, {}", node.getId());
    	String path = ZookeeperService.NODE_PATH + "/" + node.getId();
    	if (zookeeperService.exist(path)) {
    		return null;
    	}
    	
        String yaml = YamlUtils.toString(node);
        zookeeperService.updateData(path, yaml);
        return node;
    }
    
    /**
     * 删除节点
     * @param id 编号
     * @return 节点
     */
    public Node deleteNode(String id) {
    	LOG.info("delete node, {}", id);
    	Node node = getNode(id);
    	if (node == null) {
    		return null;
    	}
    	
    	String path = ZookeeperService.NODE_PATH + "/" + node.getId();
    	zookeeperService.deleteNode(path);
    	return node;
    }
    
    /**
     * 导入节点
     * @param dockerHost 主机地址
     * @param publicIp 公开IP
     * @return 节点
     */
    public Node importNode(String dockerHost, String publicIp) {
    	
    	DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
    			.withDockerHost("tcp://" + dockerHost).build();
    	DockerClient docker = DockerClientBuilder.getInstance(config).build();
    	Version version = docker.versionCmd().exec();
    	LOG.info("get version, kernel: {}, api: {}", version.getKernelVersion());
    	
    	long begin = System.currentTimeMillis();
    	Info info = docker.infoCmd().exec();
    	long end = System.currentTimeMillis();
    	LOG.info("get info, name: {}, id: {}", info.getName(), info.getId());
    	
    	//set data
    	Node node = new Node();
    	node.setDockerHost(dockerHost);
    	node.setPublicIp(publicIp);
    	
    	//get version
    	node.setApiVersion(version.getApiVersion());
    	node.setEngineVersion(version.getVersion());
    	
    	//get dynamic data
    	node.setId(StringUtils.remove(info.getId(), ':'));
    	node.setName(info.getName());
    	node.setOs(info.getOsType());
    	node.setArchitecture(info.getArchitecture());
    	node.setContainers(Arrays.asList(new Integer[] {
    			info.getContainers(), 
    			info.getContainersRunning(), 
    			info.getContainersPaused(), 
    			info.getContainersStopped()}));
    	node.setImages(info.getImages());
    	node.setCpus(info.getNCPU());
    	node.setMemory(info.getMemTotal());
    	node.setLabels(Arrays.asList(info.getLabels()));
    	node.setStatus(Node.STATUS_ONLINE);
    	node.setSlow(end - begin > 1000);

    	// create node data
    	return createNode(node);
    }
	
}
