package cn.batchfile.stat.server.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.ZkClient;

import cn.batchfile.stat.server.util.YamlUtils;

@org.springframework.stereotype.Service
public class ZookeeperService {
    public static final String NAMESPACE_PATH = "/namespace";
    public static final String SERVICE_PATH = "/service";
    public static final String CONTAINER_OF_SERVICE_PATH = "/container-of-service";
    public static final String CONTAINER_OF_NODE_PATH = "/container-of-node";
    
    public static final String MASTER_PATH = "/master";
    public static final String NODE_PATH = "/node";
    
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperService.class);
    private ZkClient zkClient;
    private AtomicBoolean master = new AtomicBoolean(false);
    
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
        String[] ary = new String[] {root, root + MASTER_PATH, root + NODE_PATH};
        for (String s : ary) {
            if (!zkClient.exists(s)) {
                zkClient.createPersistent(s);
            }
        }

        //create temp node after /master
        String path = MASTER_PATH + "/stat-hub-";
        String data = getData();
        String self = createEphemeralSequential(path, data);

        List<String> list = getChildren(MASTER_PATH, (n, l) -> {
            setMaster(self, l);
        });
        setMaster(self, list);
    }

    @PreDestroy
    public void destroy() {
        zkClient.close();
    }
    
    public boolean isMaster() {
        return master.get();
    }

    public boolean exist(String path) {
        String p = root + path;
        return zkClient.exists(p);
    }

    public List<String> getChildren(String path) {
        return getChildren(path, null);
    }

    public List<String> getChildren(String path, IZkChildListener listener) {
        String p = root + path;
        if (!zkClient.exists(p)) {
            return null;
        }

        List<String> list = zkClient.getChildren(p);
        if (listener != null) {
            zkClient.subscribeChildChanges(p, listener);
        }
        return list;
    }

    public String getData(String path) {
        return getData(path, null);
    }

    public String getData(String path, IZkDataListener listener) {
        String p = root + path;
        byte[] bytes = zkClient.readData(p);
        if (listener != null) {
            zkClient.subscribeDataChanges(p, listener);
        }
        return new String(bytes, Charset.forName("UTF-8"));
    }

    public void updateData(String path, String data) {
        String p = root + path;
        zkClient.writeData(p, data.getBytes(Charset.forName("UTF-8")));
    }

    public void createPersistent(String path) {
        String p = root + path;
        zkClient.createPersistent(p, true);
    }

    public void createPersistent(String path, String data) {
        String p = root + path;
        zkClient.createPersistent(p, data.getBytes(Charset.forName("UTF-8")));
    }

    public void deleteNode(String path) {
        String p = root + path;
        zkClient.deleteRecursive(p);
    }

    private String createEphemeralSequential(String path, String data) {
        String p = root + path;
        String node = zkClient.createEphemeralSequential(p, data.getBytes(Charset.forName("UTF-8")));
        if (StringUtils.contains(node, "/")) {
            return StringUtils.substringAfterLast(node, "/");
        } else {
            return node;
        }
    }

    private void setMaster(String self, List<String> nodes) {
        Collections.sort(nodes);
        master.set(nodes.size() > 0 && nodes.get(0).equals(self));
        LOG.info("master elected: {}", master.get());
    }

    private String getData() {
        Map<String, Object> map = new HashMap<>();
        map.put("address", getAddress());
        map.put("hostname", getHostname());
        map.put("startTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss.SSSZ").format(new Date()));
        return YamlUtils.toString(map);
    }

    private String getAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LOG.error("unable to get current IP " + e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    private String getHostname() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String name = inetAddress.getHostName();
            return name;
        } catch (UnknownHostException e) {
            return StringUtils.EMPTY;
        }
    }

}
