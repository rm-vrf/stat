package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import cn.batchfile.stat.domain.resource.File;
import cn.batchfile.stat.server.dao.FileDao;
import cn.batchfile.stat.server.dao.impl.LocalFileDao;

@org.springframework.stereotype.Service
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
    private FileDao fileDao;

    @Value("${data.path}")
    public void setDataPath(String dataPath) throws IOException {
    	String root = dataPath + "/file";
        if (StringUtils.startsWith(root, "file://")) {
        	LOG.info("create LocalFileDao, root path: {}", root);
        	String path = StringUtils.substringAfter(root, "file://");
        	fileDao = new LocalFileDao();
            fileDao.setRoot(path);
        } else if (StringUtils.startsWith(root, "hdfs://")) {
        	LOG.info("create HdfsFileDao, root path: {}", root);
        	//String path = StringUtils.substringAfter(root, "hdfs://");
        	//fileDao = new HdfsFileDao();
        	//fileDao.setRoot(path);
        }
    }
    
    /**
     * 获取文件信息
     * @param namespace 命名空间
     * @param name 名称
     * @return 文件信息
     */
    public File getFile(String namespace, String name) {
    	String directory = StringUtils.isEmpty(name) ? namespace : namespace + "/" + name;
    	String s = fileDao.get(directory);
    	if (StringUtils.isEmpty(s)) {
    		LOG.debug("file not exist: {}/{}", namespace, name);
    		return null;
    	}
    	
    	String[] ary = StringUtils.split(s, '\t');
		File f = new File();
		f.setDirectory(StringUtils.equals(ary[0], "d"));
		f.setSize(Long.valueOf(ary[1]));
		f.setTimestamp(new Date(Long.valueOf(ary[2])));
		f.setName(ary[3]);
		
		return f;
    }
    
    /**
     * 创建目录
     * @param namespace 命名空间
     * @param name 目录名称
     * @return 文件对象
     * @throws IOException 异常
     */
    public File createDirectory(String namespace, String name) throws IOException {
    	String directory = StringUtils.isEmpty(name) ? namespace : namespace + "/" + name;
    	File file = getFile(namespace, name);
    	
    	if (file != null) {
    		LOG.info("file already exist: {}", directory);
    		return null;
    	}
    	
    	boolean success = fileDao.mkdir(directory);
    	if (success) {
    		file = new File();
    		file.setDirectory(true);
    		file.setName(directory);
    		file.setSize(0L);
    		file.setTimestamp(new Date());
    		return file;
    	} else {
    		throw new RuntimeException("Error when create directory " + directory);
    	}
    }

    /**
     * 列出目录中的文件
     * @param namespace 命名空间
     * @param name 目录名称
     * @return 文件列表
     */
    public List<File> listFiles(String namespace, String name) {
    	String directory = StringUtils.isEmpty(name) ? namespace : namespace + "/" + name;
    	File file = getFile(namespace, name);
    	if (file == null || !file.isDirectory()) {
    		return null;
    	}
    	
    	List<String> list = fileDao.ls(directory);
    	LOG.debug("list dir, count: {}", list.size());
    	
    	List<File> files = new ArrayList<File>();
    	for (String s : list) {
    		String[] ary = StringUtils.split(s, '\t');
    		
    		File f = new File();
    		f.setDirectory(StringUtils.equals(ary[0], "d"));
    		f.setSize(Long.valueOf(ary[1]));
    		f.setTimestamp(new Date(Long.valueOf(ary[2])));
    		String n = StringUtils.isEmpty(name) ? ary[3] : name + "/" + ary[3];
    		f.setName(n);
    		
    		files.add(f);
    	}
    	return files;
    }

    /**
     * 删除文件
     * @param namespace 命名空间
     * @param name 名称
     * @return 文件对象
     */
    public File deleteFile(String namespace, String name) {
    	File file = getFile(namespace, name);
    	if (file == null) {
    		LOG.info("file not exist, {}/{}", namespace, name);
    		return null;
    	}
    	
    	boolean success = fileDao.rm(namespace + "/" + name);
    	if (success) {
    		return file; 
    	} else {
    		throw new RuntimeException("Error when delete file, " + namespace + "/" + name);
    	}
    }

    /**
     * 更改文件名称
     * @param namespace 命名空间
     * @param name 文件名称
     * @param target 新名称（不含目录）
     * @return 更改后的文件
     * @throws IOException 异常
     */
    public File moveFile(String namespace, String name, String target) throws IOException {
    	File file = getFile(namespace, name);
    	if (file == null) {
    		LOG.info("file not exist, {}/{}", namespace, name);
    		return null;
    	}
    	
    	String newName = target;
    	if (StringUtils.contains(name, '/')) {
    		newName = StringUtils.substringBeforeLast(name, "/") + "/" + target;
    	}
    	LOG.info("new file: {}", newName);
    	
    	boolean success = fileDao.mv(namespace + "/" + name, namespace + "/" + newName);
    	if (success) {
    		file.setName(newName);
    		return file;
    	} else {
    		throw new RuntimeException("Error when move file, " + namespace + "/" + name);
    	}
    }
    
    /**
     * 创建文件
     * @param namespace 命名空间
     * @param name 名称
     * @param size 大小
     * @param inputStream 流
     * @return 文件对象
     * @throws IOException 异常
     */
    public File createFile(String namespace, String name, long size, InputStream inputStream) throws IOException {
    	boolean success = fileDao.write(namespace + "/" + name, inputStream);
    	LOG.info("create file: {}", success);
    	
    	if (success) {
	    	File file = new File();
	    	file.setName(name);
	    	file.setSize(size);
	    	file.setTimestamp(new Date());
	    	file.setDirectory(false);
	    	return file;
    	} else {
    		throw new RuntimeException("Error when create file: " + namespace + "/" + name);
    	}
    }
    
    /**
     * 得到文件内容
     * @param namespace 命名空间
     * @param name 名称
     * @return 流
     * @throws IOException 异常
     */
    public InputStream getStream(String namespace, String name) throws IOException {
    	InputStream stream = fileDao.read(namespace + "/" + name);
    	return stream;
    }

}
