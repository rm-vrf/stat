package cn.batchfile.stat.domain;

import java.util.List;

/**
 * 服务实例
 * @author lane.cn@gmail.com
 *
 */
public class Instance {
	
	public static final String STATUS_UP = "UP";
	public static final String STATUS_STOP = "STOP";
	public static final String STATUS_KILL = "KILL";

	private String service;
	private String workDirectory;
	private String status; 
	private long pid;
	private long ppid;
	private String uid;
	private String startTime;
	private String command;
	private List<Long> children;
	private List<Integer> ports;
	private List<String> labels;
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
	 * 工作目录
	 * @return 工作目录
	 */
	public String getWorkDirectory() {
		return workDirectory;
	}

	/**
	 * 工作目录
	 * @param workDirectory 工作目录
	 */
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	/**
	 * 状态
	 * @return 状态
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 状态
	 * @param status 状态
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 进程ID
	 * @return 进程ID
	 */
	public long getPid() {
		return pid;
	}

	/**
	 * 进程ID
	 * @param pid 进程ID
	 */
	public void setPid(long pid) {
		this.pid = pid;
	}

	/**
	 * 父进程ID
	 * @return 父进程ID
	 */
	public long getPpid() {
		return ppid;
	}

	/**
	 * 父进程ID
	 * @param ppid 父进程ID
	 */
	public void setPpid(long ppid) {
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
	 * 标签
	 * @return 标签
	 */
	public List<String> getLabels() {
		return labels;
	}

	/**
	 * 标签
	 * @param labels 标签
	 */
	public void setLabels(List<String> labels) {
		this.labels = labels;
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
