package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

import cn.batchfile.stat.server.dao.ServiceRepository;
import cn.batchfile.stat.server.domain.service.Container;
import cn.batchfile.stat.server.domain.service.HealthCheck;
import cn.batchfile.stat.server.domain.service.Image;
import cn.batchfile.stat.server.domain.service.Service;
import cn.batchfile.stat.server.domain.service.ServiceDeploy;
import cn.batchfile.stat.server.dto.ServiceTable;
import cn.batchfile.stat.server.exception.DuplicateEntryException;

@org.springframework.stereotype.Service
public class ServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    
    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<Service> getServices(String namespace) {
    	LOG.debug("get service list, {}", namespace);
    	Iterable<ServiceTable> sts = serviceRepository.findMany(namespace);
    	List<Service> list = new ArrayList<Service>();
    	sts.forEach(st -> {
    		Service service = compose(st);
    		list.add(service);
    	});
    	LOG.debug("list size: {}", list.size());
    	return list;
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public Service getService(String namespace, String serviceName) {
    	LOG.debug("get service, {}/{}", namespace, serviceName);
    	Optional<ServiceTable> st = serviceRepository.findOne(namespace, serviceName);
    	if (st.isPresent()) {
    		return compose(st.get());
    	} else {
    		return null;
    	}
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Service createService(String namespace, Service service) {
    	LOG.info("create service, {}/{}", namespace, service.getName());
    	Optional<ServiceTable> op = serviceRepository.findOne(namespace, service.getName());
    	if (op.isPresent()) {
    		throw new DuplicateEntryException("service already exist");
    	}

    	ServiceTable st = new ServiceTable();
    	compose(st, service);
    	st.setId(StringUtils.remove(UUID.randomUUID().toString(), '-'));
    	st.setNamespace(namespace);
    	
    	serviceRepository.save(st);
    	LOG.info("created");
        return service;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Service updateService(String namespace, Service service) {
    	LOG.info("update service, {}/{}", namespace, service.getName());
    	Optional<ServiceTable> op = serviceRepository.findOne(namespace, service.getName());
    	if (!op.isPresent()) {
    		throw new DuplicateEntryException("service not exist");
    	}
    	
    	ServiceTable st = op.get();
    	compose(st, service);
    	serviceRepository.save(st);
    	LOG.info("updated");
        return service;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<String> deleteServices(String namespace) {
    	LOG.info("delete all service, {}/*", namespace);
    	Iterable<ServiceTable> sts = serviceRepository.findMany(namespace);
    	List<String> serviceNames = new ArrayList<String>();
    	sts.forEach(st -> {
    		serviceNames.add(st.getName());
    	});
    	serviceRepository.deleteMany(namespace);
    	LOG.info("deleted many: {}", serviceNames);
    	
        return serviceNames;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Service deleteService(String namespace, String serviceName) {
    	LOG.info("delete service, {}/{}", namespace, serviceName);
    	Optional<ServiceTable> st = serviceRepository.findOne(namespace, serviceName);
    	if (!st.isPresent()) {
    		LOG.info("no such servie: {}/{}", namespace, serviceName);
    		return null;
    	} else {
    		serviceRepository.deleteById(st.get().getId());
    		LOG.info("deleted");
    	}
    	
        return compose(st.get());
    }

    private Service compose(ServiceTable serviceTable) {
    	Service service = new Service();
    	service.setContainer(JSON.parseObject(serviceTable.getContainer(), Container.class));
    	service.setDependsOn(JSON.parseArray(serviceTable.getDependsOn(), String.class));
    	service.setDeploy(JSON.parseObject(serviceTable.getDeploy(), ServiceDeploy.class));
    	service.setDomainName(serviceTable.getDomainName());
    	service.setHealthCheck(JSON.parseObject(serviceTable.getHealthCheck(), HealthCheck.class));
    	service.setImage(JSON.parseObject(serviceTable.getImage(), Image.class));
    	service.setName(serviceTable.getName());
    	service.setStateful(serviceTable.getStateful());
    	return service;
    }
    
    private void compose(ServiceTable serviceTable, Service service) {
    	serviceTable.setContainer(JSON.toJSONString(service.getContainer()));
    	serviceTable.setDependsOn(JSON.toJSONString(service.getDependsOn()));
    	serviceTable.setDeploy(JSON.toJSONString(service.getDeploy()));
    	serviceTable.setDomainName(service.getDomainName());
    	serviceTable.setHealthCheck(JSON.toJSONString(service.getHealthCheck()));
    	serviceTable.setImage(JSON.toJSONString(service.getImage()));
    	serviceTable.setName(service.getName());
    	serviceTable.setStateful(service.getStateful());
    }
}
