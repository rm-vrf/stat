package cn.batchfile.stat.agent.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class Process {
	private String user;
	private long pid;
	private String type;
	@JSONField(name="cpu_percent")
	private double cpuPercent;
	@JSONField(name="memory_percent")
	private double memoryPercent;
	private long vsz;
	private long rss;
	private String tt;
	private String stat;
	private String started;
	private String time;
	private String command;
	@JSONField(name="main_class")
	private String mainClass;
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public long getPid() {
		return pid;
	}
	
	public void setPid(long pid) {
		this.pid = pid;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public double getCpuPercent() {
		return cpuPercent;
	}
	
	public void setCpuPercent(double cpuPercent) {
		this.cpuPercent = cpuPercent;
	}
	
	public double getMemoryPercent() {
		return memoryPercent;
	}
	
	public void setMemoryPercent(double memoryPercent) {
		this.memoryPercent = memoryPercent;
	}
	
	public long getVsz() {
		return vsz;
	}
	
	public void setVsz(long vsz) {
		this.vsz = vsz;
	}
	
	public long getRss() {
		return rss;
	}
	
	public void setRss(long rss) {
		this.rss = rss;
	}
	
	public String getTt() {
		return tt;
	}
	
	public void setTt(String tt) {
		this.tt = tt;
	}
	
	public String getStat() {
		return stat;
	}
	
	public void setStat(String stat) {
		this.stat = stat;
	}
	
	public String getStarted() {
		return started;
	}
	
	public void setStarted(String started) {
		this.started = started;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
}
