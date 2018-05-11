package cn.batchfile.stat.domain;

import java.util.List;

public class Everything {
	private String id;
	private String hostname;
	private CpuStat cpuStat;
	private OsStat osStat;
	private List<DiskStat> diskStats;
	private MemoryStat memoryStat;
	private List<NetworkStat> networkStats;

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

	public CpuStat getCpuStat() {
		return cpuStat;
	}

	public void setCpuStat(CpuStat cpuStat) {
		this.cpuStat = cpuStat;
	}

	public OsStat getOsStat() {
		return osStat;
	}

	public void setOsStat(OsStat osStat) {
		this.osStat = osStat;
	}

	public List<DiskStat> getDiskStats() {
		return diskStats;
	}

	public void setDiskStats(List<DiskStat> diskStats) {
		this.diskStats = diskStats;
	}

	public MemoryStat getMemoryStat() {
		return memoryStat;
	}

	public void setMemoryStat(MemoryStat memoryStat) {
		this.memoryStat = memoryStat;
	}

	public List<NetworkStat> getNetworkStats() {
		return networkStats;
	}

	public void setNetworkStats(List<NetworkStat> networkStats) {
		this.networkStats = networkStats;
	}
}
