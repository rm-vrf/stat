package cn.batchfile.stat.domain;

public class Res {
	private float cpus;
	private long mem;
	private long disk;

	public float getCpus() {
		return cpus;
	}

	public void setCpus(float cpus) {
		this.cpus = cpus;
	}

	public long getMem() {
		return mem;
	}

	public void setMem(long mem) {
		this.mem = mem;
	}

	public long getDisk() {
		return disk;
	}

	public void setDisk(long disk) {
		this.disk = disk;
	}
}
