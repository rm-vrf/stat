package cn.batchfile.stat.domain;

/**
 * 控制组
 * @author lane.cn@gmail.com
 *
 */
public class ControlGroup {

	private String cpus;
	private String memory;

	/**
	 * CPU
	 * @return CPU
	 */
	public String getCpus() {
		return cpus;
	}

	/**
	 * CPU
	 * @param cpus CPU
	 */
	public void setCpus(String cpus) {
		this.cpus = cpus;
	}

	/**
	 * 内存
	 * @return 内存
	 */
	public String getMemory() {
		return memory;
	}

	/**
	 * 内存
	 * @param memory 内存
	 */
	public void setMemory(String memory) {
		this.memory = memory;
	}

}
