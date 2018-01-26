package cn.batchfile.stat.agent.types;

public class Os {
	private String name;
	private String version;
	private int cpu;
	private String architecture;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getCpu() {
		return cpu;
	}
	
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	
	public String getArchitecture() {
		return architecture;
	}
	
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}
}
