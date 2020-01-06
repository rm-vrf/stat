package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import cn.batchfile.stat.server.dao.FileDao;
import cn.batchfile.stat.server.dao.FileRepository;
import cn.batchfile.stat.server.dao.impl.LocalFileDao;
import cn.batchfile.stat.server.domain.resource.FileInstance;
import cn.batchfile.stat.server.dto.FileTable;
import cn.batchfile.stat.server.exception.DuplicateEntryException;
import cn.batchfile.stat.server.exception.NotFoundException;

@org.springframework.stereotype.Service
public class FileService {
    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
    private FileDao fileDao;
    
    @Autowired
    private FileRepository fileRepository;

    @Value("${data.path}")
    public void setDataPath(String dataPath) throws IOException {
    	String root = dataPath + "/file";
        if (StringUtils.startsWith(root, "file://")) {
        	LOG.info("create LocalFileDao, root path: {}", root);
        	String path = StringUtils.substringAfter(root, "file://");
        	fileDao = new LocalFileDao();
            fileDao.setRoot(path);
        } else if (StringUtils.startsWith(root, "hdfs://")) {
        	//LOG.info("create HdfsFileDao, root path: {}", root);
        	//String path = StringUtils.substringAfter(root, "hdfs://");
        	//fileDao = new HdfsFileDao();
        	//fileDao.setRoot(path);
        }
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public FileInstance getFile(String namespace, String name) {
    	LOG.debug("get file: {}/{}", namespace, name);
    	Optional<FileTable> ft = StringUtils.isEmpty(name) ? fileRepository.findOne(namespace) : fileRepository.findOne(namespace, name);
    	if (ft.isPresent()) {
    		FileInstance fi = compose(ft.get());
    		return fi;
    	} else {
    		LOG.debug("file not found");
    		return null;
    	}
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public FileInstance createDirectory(String namespace, String name) throws IOException {
    	LOG.info("create directory, {}/{}", namespace, name);
    	Optional<FileTable> ns = fileRepository.findOne(namespace);
    	if (!ns.isPresent()) {
    		LOG.error("namespace not exist");
    		throw new NotFoundException("namespace not exist");
    	}
    	
    	Optional<FileTable> dir = fileRepository.findOne(namespace, name);
    	if (dir.isPresent() && !StringUtils.equals(dir.get().getType(), FileTable.TYPE_FILE)) {
    		LOG.info("directory already exist");
    		return null;
    	}
    	
    	String[] ary = StringUtils.split(name, '/');
    	String n = StringUtils.EMPTY;
    	FileTable parent = ns.get();
    	for (String s : ary) {
    		if (StringUtils.isEmpty(n)) {
    			n = s;
    		} else {
    			n += "/" + s;
    		}
    		LOG.info("create direcotry: {}/{}, parent: {}/{}", namespace, n, parent.getNamespace(), parent.getName());
    		parent = getOrCreateDirectory(namespace, n, parent);
    	}
    	
    	return compose(parent);
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<FileInstance> listFiles(String namespace, String name) {
    	LOG.debug("list dir: {}/{}", namespace, name);
    	Optional<FileTable> ft = StringUtils.isEmpty(name) ? fileRepository.findOne(namespace) : fileRepository.findOne(namespace, name);
    	if (!ft.isPresent()) {
    		return null;
    	}
    	
    	List<FileInstance> list = new ArrayList<FileInstance>();
    	Iterable<FileTable> iter = fileRepository.findMany(ft.get().getId());
    	iter.forEach(i -> {
    		FileInstance fi = compose(i);
    		list.add(fi);
    	});
    	
    	LOG.debug("list dir, count: {}", list.size());
    	return list;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public FileInstance deleteFile(String namespace, String name) {
    	LOG.info("delete file: {}/{}", namespace, name);
    	Optional<FileTable> ft = fileRepository.findOne(namespace, name);
    	if (!ft.isPresent()) {
    		LOG.info("file not exist");
    		return null;
    	}
    	
    	if (StringUtils.equals(ft.get().getType(), FileTable.TYPE_DIRECTORY)) {
    		LOG.info("delete children");
    		deleteChildren(ft.get().getId());
    	} else {
    		LOG.info("delete file");
    		fileRepository.deleteById(ft.get().getId());
    	}

    	LOG.info("deleted");
    	return compose(ft.get());
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public FileInstance createFile(String namespace, String name, long size, InputStream inputStream) throws IOException {
    	LOG.info("upload file: {}/{}", namespace, name);
    	String dir = StringUtils.contains(name, '/') ? StringUtils.substringBeforeLast(name, "/") : null;
    	Optional<FileTable> d = StringUtils.isEmpty(dir) ? fileRepository.findOne(namespace) : fileRepository.findOne(namespace, dir);

    	if (!d.isPresent()) {
    		LOG.error("directory not exist: {}", dir);
    		throw new NotFoundException("directory not exist");
    	}
    	
    	String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
    	String id = StringUtils.remove(UUID.randomUUID().toString(), '-');
    	String file = date + "/" + id;
    	
    	FileTable ft = new FileTable();
    	ft.setFile(file);
    	ft.setId(id);
    	ft.setName(name);
    	ft.setNamespace(namespace);
    	ft.setParent(d.get().getId());
    	ft.setSize(size);
    	ft.setTimestamp(new Date());
    	ft.setType(FileTable.TYPE_FILE);
    	
    	LOG.info("insert data");
    	fileRepository.save(ft);
    	
    	String s = fileDao.get(date);
    	if (s == null) {
    		LOG.info("create directory: {}", date);
    		fileDao.mkdir(date);
    	}
    	
    	boolean success = fileDao.write(file, inputStream);
    	LOG.info("write file: {}", success);
    	
    	if (success) {
    		return compose(ft);
    	} else {
    		throw new RuntimeException("Error when create file: " + namespace + "/" + name);
    	}
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public InputStream getStream(String namespace, String name) throws IOException {
    	Optional<FileTable> ft = fileRepository.findOne(namespace, name);
    	if (!ft.isPresent()) {
    		return null;
    	}
    	
    	String file = ft.get().getFile();
    	InputStream stream = fileDao.read(file);
    	return stream;
    }

    private FileInstance compose(FileTable fileTable) {
    	FileInstance fi = new FileInstance();
    	fi.setDirectory(!StringUtils.equals(fileTable.getType(), FileTable.TYPE_FILE));
    	fi.setName(fileTable.getName());
    	fi.setSize(fileTable.getSize());
    	fi.setTimestamp(fileTable.getTimestamp());
    	return fi;
    }
    
    private FileTable getOrCreateDirectory(String namespace, String name, FileTable parent) {
    	Optional<FileTable> ft = StringUtils.isEmpty(name) ? fileRepository.findOne(namespace) : fileRepository.findOne(namespace, name);
    	if (ft.isPresent()) {
    		if (StringUtils.equals(ft.get().getType(), FileTable.TYPE_FILE)) {
        		LOG.error("file already exist, cannot create dirctory with same name: {}/{}", namespace, ft.get().getName());
        		throw new DuplicateEntryException("file already exist, cannot create dirctory");
    		} else {
        		LOG.info("direcotry already exist, return it");
        		return ft.get();
    		}
    	} else {
    		LOG.info("create directory: {}/{}", namespace, name);
    		FileTable fileTable = new FileTable();
    		fileTable.setId(StringUtils.remove(UUID.randomUUID().toString(), '-'));
    		fileTable.setName(name);
    		fileTable.setNamespace(namespace);
    		fileTable.setParent(parent.getId());
    		fileTable.setSize(0L);
    		fileTable.setTimestamp(new Date());
    		fileTable.setType(FileTable.TYPE_DIRECTORY);
    		
    		fileRepository.save(fileTable);
    		LOG.info("created");
    		return fileTable;
    	}
    }

    private void deleteChildren(String id) {
    	Iterable<FileTable> iter = fileRepository.findMany(id);
    	iter.forEach(i -> {
    		if (StringUtils.equals(i.getType(), FileTable.TYPE_DIRECTORY)) {
    			deleteChildren(i.getId());
    		}
        	LOG.info("delete: {}/{}", i.getNamespace(), i.getName());
        	fileRepository.deleteById(i.getId());
    	});
    	fileRepository.deleteById(id);
    }
}
