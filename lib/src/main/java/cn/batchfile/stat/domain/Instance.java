package cn.batchfile.stat.domain;

import java.util.List;

/**
 * 服务实例
 * @author lane.cn@gmail.com
 *
 */
public class Instance {

	private String service;
	private Long pid;
	private Long ppid;
	private String uid;
	private String startTime;
	private String command;
	private List<Long> children;
	private List<Integer> ports;
	private String container;

	/**
	 * 服务
	 * @return 服务
	 */
	public String getService() {
		return service;
	}

	/**
	 * 服务
	 * @param service 服务
	 */
	public void setService(String service) {
		this.service = service;
	}

	/**
	 * 进程ID
	 * @return 进程ID
	 */
	public Long getPid() {
		return pid;
	}

	/**
	 * 进程ID
	 * @param pid 进程ID
	 */
	public void setPid(Long pid) {
		this.pid = pid;
	}

	/**
	 * 父进程ID
	 * @return 父进程ID
	 */
	public Long getPpid() {
		return ppid;
	}

	/**
	 * 父进程ID
	 * @param ppid 父进程ID
	 */
	public void setPpid(Long ppid) {
		this.ppid = ppid;
	}

	/**
	 * 用户ID
	 * @return 用户ID
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * 用户ID
	 * @param uid 用户ID
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * 启动时间
	 * @return 启动时间
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * 启动时间
	 * @param startTime 启动时间
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * 命令行
	 * @return 命令行
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * 命令行
	 * @param command 命令行
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * 子进程
	 * @return 子进程
	 */
	public List<Long> getChildren() {
		return children;
	}

	/**
	 * 子进程
	 * @param children 子进程
	 */
	public void setChildren(List<Long> children) {
		this.children = children;
	}

	/**
	 * 端口
	 * @return 端口
	 */
	public List<Integer> getPorts() {
		return ports;
	}

	/**
	 * 端口
	 * @param ports 端口
	 */
	public void setPorts(List<Integer> ports) {
		this.ports = ports;
	}

	/**
	 * 容器
	 * @return 容器
	 */
	public String getContainer() {
		return container;
	}

	/**
	 * 容器
	 * @param container 容器
	 */
	public void setContainer(String container) {
		this.container = container;
	}

}
