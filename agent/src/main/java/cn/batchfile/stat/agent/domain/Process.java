package cn.batchfile.stat.agent.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class Process {
	private long pid;
	private long ppid;
	private String name;
	private String user;
	private String group;
	@JSONField(name="work_directory")
	private String workDirectory;
	private String exe;
	private String[] args;
	@JSONField(name="start_time")
	private Date startTime;
	@JSONField(name="cpu_sys")
	private long cpuSys;
	@JSONField(name="cpu_user")
	private long cpuUser;
	@JSONField(name="cpu_total")
	private long cpuTotal;
	@JSONField(name="cpu_percent")
	private double cpuPercent;
	private long vsz;
	private long rss;
	private long threads;
	
	public long getPid() {
		return pid;
	}
	
	public void setPid(long pid) {
		this.pid = pid;
	}
	
	public long getPpid() {
		return ppid;
	}

	public void setPpid(long ppid) {
		this.ppid = ppid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	public String getExe() {
		return exe;
	}
	
	public void setExe(String exe) {
		this.exe = exe;
	}
	
	public String[] getArgs() {
		return args;
	}
	
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public long getCpuSys() {
		return cpuSys;
	}
	
	public void setCpuSys(long cpuSys) {
		this.cpuSys = cpuSys;
	}
	
	public long getCpuUser() {
		return cpuUser;
	}
	
	public void setCpuUser(long cpuUser) {
		this.cpuUser = cpuUser;
	}
	
	public long getCpuTotal() {
		return cpuTotal;
	}
	
	public void setCpuTotal(long cpuTotal) {
		this.cpuTotal = cpuTotal;
	}
	
	public double getCpuPercent() {
		return cpuPercent;
	}
	
	public void setCpuPercent(double cpuPercent) {
		this.cpuPercent = cpuPercent;
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
	
	public long getThreads() {
		return threads;
	}
	
	public void setThreads(long threads) {
		this.threads = threads;
	}
}
