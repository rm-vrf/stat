package cn.batchfile.stat.agent.types;

public class Disk {
	private String dirName;
	private String devName;
	private String typeName;
	private String sysTypeName;
	private String options;
	private int type;
	//private long flags;
	private long total;

	public String getDirName() {
		return dirName;
	}
	
	public void setDirName(String dirName) {
		this.dirName = dirName;
	}
	
	public String getDevName() {
		return devName;
	}
	
	public void setDevName(String devName) {
		this.devName = devName;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public String getSysTypeName() {
		return sysTypeName;
	}
	
	public void setSysTypeName(String sysTypeName) {
		this.sysTypeName = sysTypeName;
	}
	
	public String getOptions() {
		return options;
	}
	
	public void setOptions(String options) {
		this.options = options;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public long getTotal() {
		return total;
	}
	
	public void setTotal(long total) {
		this.total = total;
	}
}
