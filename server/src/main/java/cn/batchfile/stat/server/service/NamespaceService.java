package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import cn.batchfile.stat.server.dao.FileRepository;
import cn.batchfile.stat.server.dto.FileTable;

@org.springframework.stereotype.Service
public class NamespaceService {
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceService.class);
    
    @Autowired
    private FileRepository fileRepository;
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<String> getNamespaces() {
    	LOG.debug("get all namespace datas");
    	List<String> nss = new ArrayList<String>();
    	
    	Iterable<FileTable> fts = fileRepository.findMany();
    	fts.forEach(ft -> {
    		nss.add(ft.getNamespace());
    	});
    	
    	LOG.debug("namespace count: {}", nss.size());
    	return nss;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public String deleteNamespace(String namespace) {
    	LOG.info("delete namespace: {}", namespace);
    	
    	//TODO 检查是否是空目录，级联删除服务，停止相关的容器
    	
    	String ns = getNamespace(namespace);
    	if (ns != null) {
    		fileRepository.deleteMany(namespace);
    		LOG.info("namespace and related object deleted");
    		return ns;
    	} else {
    		LOG.info("no such namespace");
    		return null;
    	}
    }

}
