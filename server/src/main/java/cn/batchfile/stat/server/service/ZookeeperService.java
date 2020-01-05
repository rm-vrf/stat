package cn.batchfile.stat.server.service;

import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.ZkClient;

import cn.batchfile.stat.server.util.ChildListener;
import cn.batchfile.stat.server.util.DataListener;

@org.springframework.stereotype.Service
public class ZookeeperService {
    public static final String NAMESPACE_PATH = "/namespace";
    public static final String SERVICE_PATH = "/service";
    public static final String MASTER_PATH = "/master";
    public static final String NODE_PATH = "/node";
    public static final String CONTAINER_PATH = "/container";
    
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperService.class);
    private ZkClient zkClient;
    
    @Value("${zookeeper.servers}")
    private String zookeeperServers;

    @Value("${zookeeper.session.timeout}")
    private int sessionTimeout;

    @Value("${zookeeper.connection.timeout}")
    private int connectionTimeout;

    @Value("${zookeeper.root}")
    private String root;

    @PostConstruct
    public void init() {
        zkClient = new ZkClient(zookeeperServers, sessionTimeout, connectionTimeout);

        //create nodes
        String[] ary = new String[] {root, root + MASTER_PATH, root + NODE_PATH, root + CONTAINER_PATH};
        for (String s : ary) {
            if (!zkClient.exists(s)) {
                zkClient.createPersistent(s);
                LOG.info("create node: {}", s);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        zkClient.close();
    }
    
    /**
     * 路径是否存在
     * @param path 路径
     * @return 是否存在
     */
    public boolean exist(String path) {
        String p = root + path;
        return zkClient.exists(p);
    }

    /**
     * 获取子节点
     * @param path 路径
     * @return 子节点名称
     */
    public List<String> getChildren(String path) {
        return getChildren(path, null);
    }

    /**
     * 获取子节点并监听
     * @param path 路径
     * @param listener 监听器
     * @return 子节点名称
     */
    public List<String> getChildren(String path, ChildListener listener) {
        String p = root + path;
        if (!zkClient.exists(p)) {
            return null;
        }

        List<String> list = zkClient.getChildren(p);
        if (listener != null) {
            zkClient.subscribeChildChanges(p, new IZkChildListener() {
				
				@Override
				public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
					listener.handleChildChange(parentPath, currentChildren);
				}
			});
        }
        return list;
    }

    /**
     * 获取节点的数据
     * @param path 路径
     * @return 数据
     */
    public String getData(String path) {
        return getData(path, null);
    }

    /**
     * 获取节点的数据并监听
     * @param path 路径
     * @param listener 监听器
     * @return 数据
     */
    public String getData(String path, DataListener listener) {
        String p = root + path;
        byte[] bytes = zkClient.readData(p);
        if (listener != null) {
            zkClient.subscribeDataChanges(p, new IZkDataListener() {
				
				@Override
				public void handleDataDeleted(String dataPath) throws Exception {
					listener.handleDataChange(true, dataPath, null);
				}
				
				@Override
				public void handleDataChange(String dataPath, byte[] data) throws Exception {
					listener.handleDataChange(false, dataPath, new String(data, Charset.forName("UTF-8")));
				}
			});
        }
        return new String(bytes, Charset.forName("UTF-8"));
    }

    /**
     * 更新数据
     * @param path 路径
     * @param data 数据
     */
    public void updateData(String path, String data) {
        String p = root + path;
        zkClient.writeData(p, data.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * 创建持久节点
     * @param path 路径
     */
    public void createPersistent(String path) {
        String p = root + path;
        zkClient.createPersistent(p, true);
    }

    /**
     * 创建持久节点
     * @param path 路径
     * @param data 数据
     */
    public void createPersistent(String path, String data) {
        String p = root + path;
        zkClient.createPersistent(p, data.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * 删除节点
     * @param path 路径
     */
    public void deleteNode(String path) {
        String p = root + path;
        zkClient.deleteRecursive(p);
    }

    /**
     * 创建临时节点
     * @param path 节点
     * @param data 数据
     * @return 节点
     */
    public String createEphemeralSequential(String path, String data) {
        String p = root + path;
        String node = zkClient.createEphemeralSequential(p, data.getBytes(Charset.forName("UTF-8")));
        if (StringUtils.contains(node, "/")) {
            return StringUtils.substringAfterLast(node, "/");
        } else {
            return node;
        }
    }

}
