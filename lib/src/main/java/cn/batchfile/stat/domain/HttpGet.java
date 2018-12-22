package cn.batchfile.stat.domain;

/**
 * HTTP 健康检查
 * @author  lane.cn@gmail.com
 *
 */
public class HttpGet {

	public static final String PROTOCAL_HTTP = "http";
	public static final String PROTOCAL_HTTPS = "https";
	
	private String protocol;
	private String uri;
	private int portIndex;
	private int timeout;

	/**
	 * 协议
	 * @return 协议
	 */
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * 协议
	 * @param protocol 协议
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * 检查地址
	 * @return 检查地址
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * 检查地址
	 * @param uri 检查地址
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * 端口序号
	 * @return 端口序号
	 */
	public int getPortIndex() {
		return portIndex;
	}
	
	/**
	 * 端口序号
	 * @param portIndex 端口序号
	 */
	public void setPortIndex(int portIndex) {
		this.portIndex = portIndex;
	}

	/**
	 * 超时时间
	 * @return 超时时间
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * 超时时间
	 * @param timeout 超时时间
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}
