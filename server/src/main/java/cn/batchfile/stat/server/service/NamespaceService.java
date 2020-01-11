package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import cn.batchfile.stat.server.dao.FileRepository;
import cn.batchfile.stat.server.dto.FileTable;

@org.springframework.stereotype.Service
public class NamespaceService {
	private static final String DEFAULT_NAMESPACE = "default";
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceService.class);
    
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private ServiceService serviceService;
    
    @PostConstruct
    public void init() {
    	//如果库是空的，创建default命名空间
    	Pageable pageable = PageRequest.of(0, 1);
    	Page<String> page = getNamespaces(pageable);
    	if (page.getTotalElements() == 0) {
    		createNamespace(DEFAULT_NAMESPACE);
    	}
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public String getNamespace(String namespace) {
    	LOG.debug("get namespace: {}", namespace);
    	Optional<FileTable> ft = fileRepository.findOne(namespace);
    	if (ft.isPresent()) {
    		return ft.get().getNamespace();
    	} else {
    		LOG.debug("namespace not found");
    		return null;
    	}
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public String createNamespace(String namespace) {
    	LOG.info("create namespace: {}", namespace);
    	
    	String ns = getNamespace(namespace);
    	if (ns != null) {
    		LOG.info("duplicated namespace: {}", namespace);
    		return null;
    	}
    	
    	FileTable ft = new FileTable();
    	ft.setId(StringUtils.remove(UUID.randomUUID().toString(), '-'));
    	ft.setNamespace(namespace);
    	ft.setSize(0L);
    	ft.setTimestamp(new Date());
    	ft.setType(FileTable.TYPE_NAMESPACE);
    	
    	fileRepository.save(ft);
    	LOG.info("namespace created");
        return namespace;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public Page<String> getNamespaces(Pageable pageable) {
    	LOG.debug("get all namespace datas");
    	List<String> list = new ArrayList<String>();
    	
    	Page<FileTable> page = fileRepository.findMany(pageable);
    	page.getContent().forEach(ft -> {
    		list.add(ft.getNamespace());
    	});
    	
    	LOG.debug("namespace count: total: {}, content: {}",  page.getTotalElements(), page.getContent().size());
    	return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public String deleteNamespace(String namespace) {
    	LOG.info("delete namespace: {}", namespace);
    	
    	//删除命名空间
    	String ns = getNamespace(namespace);
    	if (ns != null) {
        	//级联删除服务，停止相关的容器
        	List<String> svcs = serviceService.deleteServices(namespace);
        	LOG.info("delete service: {}", svcs);
        	
    		fileRepository.deleteMany(namespace);
    		LOG.info("namespace and related object deleted");
    		return ns;
    	} else {
    		LOG.info("no such namespace");
    		return null;
    	}
    }
}
