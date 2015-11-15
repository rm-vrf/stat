package cn.batchfile.stat.agent.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.batchfile.stat.agent.domain.File;
import cn.batchfile.stat.agent.service.FileService;

public class FileServiceImpl implements FileService {

	@Override
	public File getFile(String path) throws IOException {
		java.io.File file = new java.io.File(path);
		return compose_file(file);
	}

	@Override
	public List<File> listRoots() throws IOException {
		List<File> list = new ArrayList<File>();
		java.io.File[] files = java.io.File.listRoots();
		for (java.io.File file : files) {
			list.add(compose_file(file));
		}
		return list;
	}

	@Override
	public List<File> listFiles(String path) throws IOException {
		List<File> list = new ArrayList<File>();
		java.io.File[] files = new java.io.File(path).listFiles();
		for (java.io.File file : files) {
			list.add(compose_file(file));
		}
		return list;
	}
	
	private File compose_file(java.io.File file) throws IOException {
		File r = new File();
		r.setDirectory(file.isDirectory());
		r.setFile(file.isFile());
		r.setFreeSpace(file.getFreeSpace());
		r.setHidden(file.isHidden());
		r.setLastModified(new Date(file.lastModified()));
		r.setLength(file.length());
		r.setName(file.getName());
		r.setParent(file.getParent());
		r.setPath(file.getPath());
		r.setTotalSpace(file.getTotalSpace());
		r.setUri(file.toURI().toString());
		r.setUsableSpace(file.getUsableSpace());
		
		return r;
	}

}
