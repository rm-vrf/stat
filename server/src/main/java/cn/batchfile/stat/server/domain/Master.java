package cn.batchfile.stat.server.domain;

/**
 * 主节点
 * @author Administrator
 *
 */
public class Master {

	private String address;
	private String hostname;
	private String startTime;
	
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
	 * 机器名
	 * @return 机器名
	 */
	public String getHostname() {
		return hostname;
	}
	
	/**
	 * 机器名
	 * @param hostname 机器名
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	/**
	 * 启动时间
	 * @return 启动时间
	 */
	public String getStartTime() {
		return startTime;
	}
	
	/**
	 * 启动时间
	 * @param startTime 启动时间
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
}
