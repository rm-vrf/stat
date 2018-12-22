package cn.batchfile.stat.domain;

/**
 * 进程信息
 * @author lane.cn@gmail.com 
 *
 */
public class Process_ {

	private Long pid;
	private Long ppid;
	private String uid;
	private String gid;
	private String time;
	private String cwd;
	private String cmd;
	private String[] args;

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
	 * 组ID
	 * @return 组ID
	 */
	public String getGid() {
		return gid;
	}

	/**
	 * 组ID
	 * @param gid 组ID
	 */
	public void setGid(String gid) {
		this.gid = gid;
	}
	
	/**
	 * 工作目录
	 * @return 工作目录
	 */
	public String getCwd() {
		return cwd;
	}

	/**
	 * 工作目录
	 * @param cwd 工作目录
	 */
	public void setCwd(String cwd) {
		this.cwd = cwd;
	}

	/**
	 * 参数
	 * @return 参数
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * 参数
	 * @param args 参数
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}

	/**
	 * 开始时间
	 * @return 开始时间
	 */
	public String getTime() {
		return time;
	}

	/**
	 * 开始时间
	 * @param time 开始时间
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * 启动命令
	 * @return 启动命令
	 */
	public String getCmd() {
		return cmd;
	}

	/**
	 * 启动命令
	 * @param cmd 启动命令
	 */
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

}
