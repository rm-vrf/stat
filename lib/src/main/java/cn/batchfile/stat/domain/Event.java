package cn.batchfile.stat.domain;

import java.util.Date;

/**
 * 事件
 * @author lane.cn@gmail.com
 *
 */
public class Event {
	private String action;
	private Date time;
	private String address;
	private String hostname;
	private String service;
	private long pid;
	private String message;
	
	/**
	 * 行为
	 * @return 行为
	 */
	public String getAction() {
		return action;
	}
	
	/**
	 * 行为
	 * @param action 行为
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	/**
	 * 时间
	 * @return 时间
	 */
	public Date getTime() {
		return time;
	}
	
	/**
	 * 时间
	 * @param time 时间
	 */
	public void setTime(Date time) {
		this.time = time;
	}
	
	/**
	 * 地址
	 * @return 地址
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * 地址
	 * @param address 地址
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * 主机名
	 * @return 主机名
	 */
	public String getHostname() {
		return hostname;
	}
	
	/**
	 * 主机名
	 * @param hostname 主机名
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	/**
	 * 服务
	 * @return 服务
	 */
	public String getService() {
		return service;
	}
	
	/**
	 * 服务
	 * @param service 服务
	 */
	public void setService(String service) {
		this.service = service;
	}
	
	/**
	 * 进程号
	 * @return 进程号
	 */
	public long getPid() {
		return pid;
	}
	
	/**
	 * 进程号
	 * @param pid 进程号
	 */
	public void setPid(long pid) {
		this.pid = pid;
	}
	
	/**
	 * 消息
	 * @return 消息
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * 消息
	 * @param message 消息
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
}
