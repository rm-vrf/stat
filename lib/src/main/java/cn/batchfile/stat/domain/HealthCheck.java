package cn.batchfile.stat.domain;

/**
 * 健康检查
 * @author lane.cn@gmail.com
 *
 */
public class HealthCheck {
	
	private Boolean enabled;
	private HttpGet httpGet;
	private Command command;
	private Integer startPeriod;
	private Integer retries;
	private Integer interval;

	/**
	 * 启用
	 * @return 启用
	 */
	public Boolean getEnabled() {
		return enabled;
	}

	/**
	 * 启用
	 * @param enabled 启用
	 */
	public void setEnabled(Boolean enabled) {
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
	public Integer getStartPeriod() {
		return startPeriod;
	}

	/**
	 * 启动时间
	 * @param startPeriod 启动时间
	 */
	public void setStartPeriod(Integer startPeriod) {
		this.startPeriod = startPeriod;
	}

	/**
	 * 尝试次数
	 * @return 尝试次数
	 */
	public Integer getRetries() {
		return retries;
	}

	/**
	 * 尝试次数
	 * @param retries 尝试次数
	 */
	public void setRetries(Integer retries) {
		this.retries = retries;
	}

	/**
	 * 间隔时间
	 * @return 间隔时间
	 */
	public Integer getInterval() {
		return interval;
	}

	/**
	 * 间隔时间
	 * @param interval 间隔时间
	 */
	public void setInterval(Integer interval) {
		this.interval = interval;
	}

}
