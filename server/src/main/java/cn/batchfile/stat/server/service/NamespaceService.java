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

    /**
     * 创建命名空间
     * @param namespace 命名空间
     * @return 命名空间
     */
    public String createNamespace(String namespace) {

        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace;
        String servicePath = path + ZookeeperService.SERVICE_PATH;
        String[] ary = new String[] {path, servicePath};

        for (String s : ary) {
            if (!zookeeperService.exist(s)) {
            	zookeeperService.createPersistent(s);
            }
        }

        return namespace;
    }

    /**
     * 获取所有的命名空间
     * @return 命名空间列表
     */
    public List<String> getNamespaces() {
        List<String> list = zookeeperService.getChildren(ZookeeperService.NAMESPACE_PATH);
        return list;
    }

    /**
     * 删除命名空间
     * @param namespace 命名空间
     * @return 命名空间
     */
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
