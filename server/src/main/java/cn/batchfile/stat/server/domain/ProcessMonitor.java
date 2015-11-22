package cn.batchfile.stat.server.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class ProcessMonitor {
	private String name;
	private String description;
	private String query;
	@JSONField(name="instance_count")
	private int instanceCount;
	private boolean enabled;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public int getInstanceCount() {
		return instanceCount;
	}
	
	public void setInstanceCount(int instanceCount) {
		this.instanceCount = instanceCount;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
