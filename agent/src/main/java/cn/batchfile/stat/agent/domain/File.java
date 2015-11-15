package cn.batchfile.stat.agent.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class File {
	@JSONField(name="free_space")
	private long freeSpace;
	private String name;
	private String parent;
	private String path;
	@JSONField(name="total_space")
	private long totalSpace;
	@JSONField(name="usable_space")
	private long usableSpace;
	private boolean directory;
	private boolean file;
	private boolean hidden;
	@JSONField(name="last_modified")
	private Date lastModified;
	private long length;
	private String uri;
	
	public long getFreeSpace() {
		return freeSpace;
	}
	
	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getParent() {
		return parent;
	}
	
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public long getTotalSpace() {
		return totalSpace;
	}
	
	public void setTotalSpace(long totalSpace) {
		this.totalSpace = totalSpace;
	}
	
	public long getUsableSpace() {
		return usableSpace;
	}
	
	public void setUsableSpace(long usableSpace) {
		this.usableSpace = usableSpace;
	}
	
	public boolean isDirectory() {
		return directory;
	}
	
	public void setDirectory(boolean directory) {
		this.directory = directory;
	}
	
	public boolean isFile() {
		return file;
	}
	
	public void setFile(boolean file) {
		this.file = file;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public long getLength() {
		return length;
	}
	
	public void setLength(long length) {
		this.length = length;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
}
