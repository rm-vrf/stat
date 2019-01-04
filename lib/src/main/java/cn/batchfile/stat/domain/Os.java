package cn.batchfile.stat.domain;

public class Os {
	private String name;
	private String architecture;
	private String version;
	private int cpus;
	private double systemLoadAverage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getCpus() {
		return cpus;
	}

	public void setCpus(int cpus) {
		this.cpus = cpus;
	}

	public double getSystemLoadAverage() {
		return systemLoadAverage;
	}

	public void setSystemLoadAverage(double systemLoadAverage) {
		this.systemLoadAverage = systemLoadAverage;
	}

}
