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
	private long threads;
	private long vsz;
	private long rss;
	@JSONField(name="cpu_sys")
	private long cpuSys;
	@JSONField(name="cpu_user")
	private long cpuUser;
	@JSONField(name="cpu_total")
	private long cpuTotal;

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
}
