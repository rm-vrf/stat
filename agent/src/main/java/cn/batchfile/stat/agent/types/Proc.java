//package cn.batchfile.stat.agent.types;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 进程信息
// * @author lane.cn
// *
// */
//public class Proc {
//	private long pid;
//	private long ppid;
//	private String uid;
//	private String tty;
//	private String startTime;
//	private String cmd;
//	private List<Long> tree = new ArrayList<Long>();
//	private List<Integer> ports = new ArrayList<Integer>();
//	private String app;
//	
//	/**
//	 * 进程ID
//	 * @return 进程ID
//	 */
//	public long getPid() {
//		return pid;
//	}
//
//	/**
//	 * 进程ID
//	 * @param pid 进程ID
//	 */
//	public void setPid(long pid) {
//		this.pid = pid;
//	}
//
//	/**
//	 * 父进程ID
//	 * @return 父进程ID
//	 */
//	public long getPpid() {
//		return ppid;
//	}
//
//	/**
//	 * 父进程ID
//	 * @param ppid 父进程ID
//	 */
//	public void setPpid(long ppid) {
//		this.ppid = ppid;
//	}
//
//	/**
//	 * 子进程ID
//	 * @return 子进程ID
//	 */
//	public List<Long> getTree() {
//		return tree;
//	}
//
//	/**
//	 * 子进程ID
//	 * @param tree 子进程ID
//	 */
//	public void setTree(List<Long> tree) {
//		this.tree = tree;
//	}
//
//	/**
//	 * 应用名称
//	 * @return 应用名称
//	 */
//	public String getApp() {
//		return app;
//	}
//
//	/**
//	 * 应用名称
//	 * @param app 应用名称
//	 */
//	public void setApp(String app) {
//		this.app = app;
//	}
//
//	/**
//	 * 进程所有者的用户名
//	 * @return 进程所有者的用户名
//	 */
//	public String getUid() {
//		return uid;
//	}
//
//	/**
//	 * 进程所有者的用户名
//	 * @param uid 进程所有者的用户名
//	 */
//	public void setUid(String uid) {
//		this.uid = uid;
//	}
//
//	/**
//	 * 与进程相关的终端
//	 * @return 与进程相关的终端
//	 */
//	public String getTty() {
//		return tty;
//	}
//
//	/**
//	 * 与进程相关的终端
//	 * @param tty 与进程相关的终端
//	 */
//	public void setTty(String tty) {
//		this.tty = tty;
//	}
//
//	/**
//	 * 进程启动时间
//	 * @return 进程启动时间
//	 */
//	public String getStartTime() {
//		return startTime;
//	}
//
//	/**
//	 * 进程启动时间
//	 * @param startTime 进程启动时间
//	 */
//	public void setStartTime(String startTime) {
//		this.startTime = startTime;
//	}
//
//	/**
//	 * 进程名，包括参数
//	 * @return 进程名，包括参数
//	 */
//	public String getCmd() {
//		return cmd;
//	}
//
//	/**
//	 * 进程名，包括参数
//	 * @param cmd 进程名，包括参数
//	 */
//	public void setCmd(String cmd) {
//		this.cmd = cmd;
//	}
//
//	/**
//	 * 端口
//	 * @return 端口
//	 */
//	public List<Integer> getPorts() {
//		return ports;
//	}
//
//	/**
//	 * 端口
//	 * @param ports 端口
//	 */
//	public void setPorts(List<Integer> ports) {
//		this.ports = ports;
//	}
//}
