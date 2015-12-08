package cn.batchfile.stat.server.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class ProcessData {
	@JSONField(name="instance_id")
	private String instanceId;
	private Date time;
	@JSONField(name="agent_id")
	private String agentId;
	private String name;
	private long pid;
	@JSONField(name="cpu_percent")
	private double cpuPercent;
	@JSONField(name="memory_percent")
	private double memoryPercent;
	private long vsz;
	private long rss;
	private String tt;
	private String stat;
	@JSONField(name="cpu_time")
	private long cpuTime;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
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

	public long getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}
}
