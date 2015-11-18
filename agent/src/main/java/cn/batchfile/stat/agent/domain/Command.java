package cn.batchfile.stat.agent.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class Command {
	private String id;
	private String command;
	@JSONField(name="start_time")
	private Date startTime;
	@JSONField(name="stop_time")
	private Date stopTime;
	private boolean background;
	private String status;
	@JSONField(name="read_time")
	private Date readTime;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public Date getStopTime() {
		return stopTime;
	}
	
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
	
	public boolean isBackground() {
		return background;
	}
	
	public void setBackground(boolean background) {
		this.background = background;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getReadTime() {
		return readTime;
	}
	
	public void setReadTime(Date readTime) {
		this.readTime = readTime;
	}
}
