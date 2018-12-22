package cn.batchfile.stat.domain;

/**
 * 部署产物
 * @author lane.cn@gmail.com
 *
 */
public class Artifact {
	
	public static final String AUTHORIZATION_BASIC = "BASIC";

	private String uri;
	private String path;
	private String authorization;
	private String userName;
	private String password;

	/**
	 * 下载地址
	 * @return 下载地址
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * 下载地址
	 * @param uri 下载地址
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * 存储地址
	 * @return 存储地址
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 存储地址
	 * @param path 存储地址
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * 授权方式
	 * @return 授权方式
	 */
	public String getAuthorization() {
		return authorization;
	}

	/**
	 * 授权方式
	 * @param authorization 授权方式
	 */
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	/**
	 * 用户名
	 * @return 用户名
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 用户名
	 * @param userName 用户名
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * 密码
	 * @return 密码
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 密码
	 * @param password 密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
}
