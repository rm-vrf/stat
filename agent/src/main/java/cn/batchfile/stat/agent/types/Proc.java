package cn.batchfile.stat.agent.types;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Proc {
	private long pid;
	private long ppid;
	private String app;
	private String name;
	private String user;
	private String group;
	private String workDirectory;
	private String exe;
	private String[] args;
	private Date startTime;
	private List<Integer> ports = new ArrayList<Integer>();

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public long getPpid() {
		return ppid;
	}

	public void setPpid(long ppid) {
		this.ppid = ppid;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	public String getExe() {
		return exe;
	}

	public void setExe(String exe) {
		this.exe = exe;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public List<Integer> getPorts() {
		return ports;
	}

	public void setPorts(List<Integer> ports) {
		this.ports = ports;
	}
}
