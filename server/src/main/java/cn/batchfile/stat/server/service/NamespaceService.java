package cn.batchfile.stat.server.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class NamespaceService {
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceService.class);

    @Autowired
    private ZookeeperService zookeeperService;

    @PostConstruct
    public void init() {
        if (!zookeeperService.exist(ZookeeperService.NAMESPACE_PATH)) {
        	zookeeperService.createPersistent(ZookeeperService.NAMESPACE_PATH);
        }
    }

    public String createNamespace(String namespace) {

        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace;
        String servicePath = path + ZookeeperService.SERVICE_PATH;
        //String nodePath = path + ZookeeperUtils.NODE_PATH;
        String containerOfServicePath = path + ZookeeperService.CONTAINER_OF_SERVICE_PATH;
        String containerOfNodePath = path + ZookeeperService.CONTAINER_OF_NODE_PATH;
        String[] ary = new String[] {path, servicePath, containerOfNodePath, containerOfServicePath};

        for (String s : ary) {
            if (!zookeeperService.exist(s)) {
            	zookeeperService.createPersistent(s);
            }
        }

        return namespace;
    }

    public List<String> getNamespaces() {
        List<String> list = zookeeperService.getChildren(ZookeeperService.NAMESPACE_PATH);
        return list;
    }

    public String deleteNamespace(String namespace) {
        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace;
        if (zookeeperService.exist(path)) {
        	zookeeperService.deleteNode(path);
            return namespace;
        } else {
            LOG.debug("namespace not exists");
            return null;
        }
    }

}
