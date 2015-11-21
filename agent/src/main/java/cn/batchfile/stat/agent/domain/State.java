package cn.batchfile.stat.agent.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class State {
	private String hostname;
	private int port;
	@JSONField(name="start_time")
	private Date startTime;
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
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
