package cn.batchfile.stat.domain.container;

/**
 * 端口
 * @author Administrator
 *
 */
public class Port {

	private String ip;
	private Integer privatePort;
	private Integer publicPort;
	private String type;

	/**
	 * IP
	 * @return IP
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * IP
	 * @param ip IP
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Port on the container
	 * @return Port on the container
	 */
	public Integer getPrivatePort() {
		return privatePort;
	}

	/**
	 * Port on the container
	 * @param privatePort Port on the container
	 */
	public void setPrivatePort(Integer privatePort) {
		this.privatePort = privatePort;
	}

	/**
	 * Port exposed on the host
	 * @return Port exposed on the host
	 */
	public Integer getPublicPort() {
		return publicPort;
	}

	/**
	 * Port exposed on the host
	 * @param publicPort Port exposed on the host
	 */
	public void setPublicPort(Integer publicPort) {
		this.publicPort = publicPort;
	}

	/**
	 * "tcp" "udp" "sctp"
	 * @return"tcp" "udp" "sctp"
	 */
	public String getType() {
		return type;
	}

	/**
	 * "tcp" "udp" "sctp"
	 * @param type "tcp" "udp" "sctp"
	 */
	public void setType(String type) {
		this.type = type;
	}
	
}
