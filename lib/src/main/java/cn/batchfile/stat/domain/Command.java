package cn.batchfile.stat.domain;

/**
 * 命令健康检查
 * @author lane.cn@gmail.com
 *
 */
public class Command {

	private String test;
	private String check;

	/**
	 * 测试命令
	 * @return 测试命令
	 */
	public String getTest() {
		return test;
	}

	/**
	 * 测试命令
	 * @param test 测试命令
	 */
	public void setTest(String test) {
		this.test = test;
	}

	/**
	 * 检查内容
	 * @return 检查内容
	 */
	public String getCheck() {
		return check;
	}

	/**
	 * 检查内容
	 * @param check 检查内容
	 */
	public void setCheck(String check) {
		this.check = check;
	}

}
