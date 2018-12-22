package cn.batchfile.stat.domain;

import java.util.List;

/**
 * 部署位置
 * @author lane.cn@gmail.com
 *
 */
public class Placement {

	private List<String> constraints;
	private List<String> preferences;
	
	/**
	 * 限制
	 * @return 限制
	 */
	public List<String> getConstraints() {
		return constraints;
	}
	
	/**
	 * 限制
	 * @param constraints 限制
	 */
	public void setConstraints(List<String> constraints) {
		this.constraints = constraints;
	}
	
	/**
	 * 优先
	 * @return 优先
	 */
	public List<String> getPreferences() {
		return preferences;
	}
	
	/**
	 * 优先
	 * @param preferences 优先
	 */
	public void setPreferences(List<String> preferences) {
		this.preferences = preferences;
	}

}
