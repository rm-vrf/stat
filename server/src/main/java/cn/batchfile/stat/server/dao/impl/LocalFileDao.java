package cn.batchfile.stat.server.dao.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.batchfile.stat.server.dao.FileDao;

public class LocalFileDao implements FileDao {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileDao.class);
    private File root;

    @Override
    public void setRoot(String root) throws IOException {
        this.root = new File(root);
        if (!this.root.exists()) {
        	FileUtils.forceMkdir(this.root);
        }
    }

    @Override
    public List<String> ls(String file) {
        File dir = new File(root, file);
        if (!dir.exists()) {
            return null;
        }

        File[] files = dir.listFiles(f -> !f.getName().startsWith("."));

        List<String> list = new ArrayList<>();
        for (java.io.File f : files) {
        	String d = f.isDirectory() ? "d" : "f";
        	long size = f.length();
        	long timestamp = f.lastModified();
        	String name = f.getName();
        	String s = String.format("%s\t%s\t%s\t%s", d, size, timestamp, name);
        	list.add(s);
        }
        return list;
    }
    
    @Override
    public boolean mkdir(String dir) throws IOException {
    	File file = new File(root, dir);
    	FileUtils.forceMkdir(file);
    	return true;
    }

    @Override
    public InputStream read(String file) throws IOException {
        File fileObject = new File(root, file);
        return new BufferedInputStream(new FileInputStream(fileObject));
    }

    @Override
    public String get(String file) {
    	File f = new File(root, file);
    	if (!f.exists()) {
    		return null;
    	}
    	
    	String d = f.isDirectory() ? "d" : "f";
    	long size = f.length();
    	long timestamp = f.lastModified();
    	String name = f.getName();
    	String s = String.format("%s\t%s\t%s\t%s", d, size, timestamp, name);
    	return s;
    }
    
    @Override
    public boolean mv(String source, String target) throws IOException {
    	File sourceFile = new File(root, source);
    	File targetFile = new File(root, target);
    	if (!sourceFile.exists()) {
    		throw new RuntimeException("Source file does not exist");
    	}
    	
    	if (targetFile.exists()) {
    		throw new RuntimeException("Target file already exist");
    	}
    	
    	if (sourceFile.isDirectory() != targetFile.isDirectory()) {
    		throw new RuntimeException("Source file and target file must be same type");
    	}
    	
    	if (sourceFile.isDirectory()) {
    		FileUtils.moveDirectory(sourceFile, targetFile);
    	} else {
    		FileUtils.moveFile(sourceFile, targetFile);
    	}
    	
    	return true;
    }

    @Override
    public boolean write(String file, InputStream inputStream) throws IOException {
    	LOG.info("write file: {}", file);
        File f = new File(root, file);
        if (!f.exists()) {
        	f.createNewFile();
        }
        
        OutputStream outputStream = new FileOutputStream(f);
        try {
        	IOUtils.copyLarge(inputStream, outputStream);
        } finally {
        	try {
        		outputStream.close();
        	} catch (Exception e) {}
        }
        return true;
    }

    @Override
    public boolean rm(String file) {
    	File f = new File(root, file);
    	return FileUtils.deleteQuietly(f);
    }

}
