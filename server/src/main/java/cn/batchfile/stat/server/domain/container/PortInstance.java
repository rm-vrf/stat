package cn.batchfile.stat.server.domain.container;

public class PortInstance {
	private String ip;
	private Integer privatePort;
	private Integer publicPort;
	private String type;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPrivatePort() {
		return privatePort;
	}

	public void setPrivatePort(Integer privatePort) {
		this.privatePort = privatePort;
	}

	public Integer getPublicPort() {
		return publicPort;
	}

	public void setPublicPort(Integer publicPort) {
		this.publicPort = publicPort;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
