package cn.batchfile.stat.domain;

public class P {
	private long pid;
	private String node;

	public P() {
		//pass
	}
	
	public P(long pid, String node) {
		this.pid = pid;
		this.node = node;
	}
	
	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
}
