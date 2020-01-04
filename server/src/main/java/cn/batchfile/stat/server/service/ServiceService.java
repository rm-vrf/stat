package cn.batchfile.stat.server.service;

import cn.batchfile.stat.domain.service.Service;
import cn.batchfile.stat.server.util.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);

    @Autowired
    private ZookeeperService zookeeperService;

    public List<Service> getServices(String namespace) {
        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace + ZookeeperService.SERVICE_PATH;
        if (!zookeeperService.exist(path)) {
            return null;
        }

        List<String> serviceNames = zookeeperService.getChildren(path);
        List<Service> services = new ArrayList<>();
        for (String serviceName : serviceNames) {
            Service service = getService(namespace, serviceName);
            services.add(service);
        }

        return services;
    }

    public Service getService(String namespace, String serviceName) {
        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace + ZookeeperService.SERVICE_PATH + "/" + serviceName;
        if (!zookeeperService.exist(path)) {
            return null;
        }

        String yaml = zookeeperService.getData(path);
        Service service = YamlUtils.toObject(yaml, Service.class);
        return service;
    }

    public Service createService(String namespace, Service service) {
    	LOG.info("create service, {}/{}", namespace, service.getName());
        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace;
        if (!zookeeperService.exist(path)) {
        	throw new RuntimeException("Namespace does not exist. namespace: " + namespace);
        }
        
        path += ZookeeperService.SERVICE_PATH + "/" + service.getName();
        if (zookeeperService.exist(path)) {
            throw new RuntimeException("Service has already existed, name: " + namespace + "/" + service.getName());
        }

        String yaml = YamlUtils.toString(service);
        zookeeperService.createPersistent(path, yaml);
        return service;
    }

    public Service updateService(String namespace, Service service) {
    	LOG.info("update service, {}/{}", namespace, service.getName());
        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace + ZookeeperService.SERVICE_PATH + "/" + service.getName();
        if (!zookeeperService.exist(path)) {
            throw new RuntimeException("Service does not exist, name: " + service.getName());
        }

        String yaml = YamlUtils.toString(service);
        zookeeperService.updateData(path, yaml);
        return service;
    }

    public List<String> deleteServices(String namespace) {
    	LOG.info("delete all service, {}/*", namespace);
        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace + ZookeeperService.SERVICE_PATH;
        if (!zookeeperService.exist(path)) {
            return null;
        }

        List<String> serviceNames = zookeeperService.getChildren(path);
        for (String serviceName : serviceNames) {
        	zookeeperService.deleteNode(path + "/" + serviceName);
        }

        return serviceNames;
    }

    public Service deleteService(String namespace, String serivceName) {
    	LOG.info("delete service, {}/{}", namespace, serivceName);
        Service service = getService(namespace, serivceName);
        if (service == null) {
            return null;
        }

        String path = ZookeeperService.NAMESPACE_PATH + "/" + namespace + ZookeeperService.SERVICE_PATH + "/" + serivceName;
        zookeeperService.deleteNode(path);
        return service;
    }

}
