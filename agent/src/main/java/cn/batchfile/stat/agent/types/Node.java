package cn.batchfile.stat.agent.types;

public class Node {
	private String id;
	private String hostname;
	private Os os;
	//private Memory memory;
	//private List<Network> networks = new ArrayList<Network>();
	//private List<Disk> disks = new ArrayList<Disk>();
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Os getOs() {
		return os;
	}
	
	public void setOs(Os os) {
		this.os = os;
	}
	
}
