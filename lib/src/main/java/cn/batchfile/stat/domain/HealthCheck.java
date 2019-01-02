package cn.batchfile.stat.domain;

/**
 * 健康检查
 * @author lane.cn@gmail.com
 *
 */
public class HealthCheck {
	
	private boolean enabled;
	private HttpGet httpGet;
	private Command command;
	private int startPeriod;
	private int retries;
	private int interval;
	private int timeout;

	/**
	 * 启用
	 * @return 启用
	 */
	public boolean getEnabled() {
		return enabled;
	}

	/**
	 * 启用
	 * @param enabled 启用
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * HTTP 检查
	 * @return HTTP 检查
	 */
	public HttpGet getHttpGet() {
		return httpGet;
	}

	/**
	 * HTTP 检查
	 * @param httpGet HTTP 检查
	 */
	public void setHttpGet(HttpGet httpGet) {
		this.httpGet = httpGet;
	}

	/**
	 * 命令检查
	 * @return 命令检查
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 命令检查
	 * @param command 命令检查
	 */
	public void setCommand(Command command) {
		this.command = command;
	}

	/**
	 * 启动时间
	 * @return 启动时间
	 */
	public int getStartPeriod() {
		return startPeriod;
	}

	/**
	 * 启动时间
	 * @param startPeriod 启动时间
	 */
	public void setStartPeriod(int startPeriod) {
		this.startPeriod = startPeriod;
	}

	/**
	 * 尝试次数
	 * @return 尝试次数
	 */
	public int getRetries() {
		return retries;
	}

	/**
	 * 尝试次数
	 * @param retries 尝试次数
	 */
	public void setRetries(int retries) {
		this.retries = retries;
	}

	/**
	 * 间隔时间
	 * @return 间隔时间
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * 间隔时间
	 * @param interval 间隔时间
	 */
	public void setInterval(int interval) {
		this.interval = interval;
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
