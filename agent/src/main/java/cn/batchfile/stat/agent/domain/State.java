package cn.batchfile.stat.agent.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class State {
	@JSONField(name="agent_id")
	private String agentId;
	private String hostname;
	private String address;
	private int port;
	@JSONField(name="start_time")
	private Date startTime;
	
	public String getAgentId() {
		return agentId;
	}
	
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
}
