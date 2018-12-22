package cn.batchfile.stat.domain;

import java.util.Map;

/**
 * 日志
 * @author lane.cn@gmail.com
 *
 */
public class Logging {
	
	public static final String DRIVER_FILE = "file";

	private String driver;
	private Map<String, String> options;

	/**
	 * 日志驱动
	 * @return 日志驱动
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * 日志驱动
	 * @param driver 日志驱动
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * 选项
	 * @return 选项
	 */
	public Map<String, String> getOptions() {
		return options;
	}

	/**
	 * 选项
	 * @param options 选项
	 */
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

}
