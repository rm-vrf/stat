package cn.batchfile.stat.domain;

public class DiskStat {
	private String dirName;
	private String devName;
	private long total;
	private long free;
	private long used;
	private long avail;
	private long files;
	private long freeFiles;
	private long diskReads;
	private long diskWrites;
	private long diskReadBytes;
	private long diskWriteBytes;
	private double diskQueue;
	private double diskServiceTime;
	private double usePercent;
	private long diskReadsPerSecond;
	private long diskWritesPerSecond;
	private long diskReadBytesPerSecond;
	private long diskWriteBytesPerSecond;

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

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getFree() {
		return free;
	}

	public void setFree(long free) {
		this.free = free;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public long getAvail() {
		return avail;
	}

	public void setAvail(long avail) {
		this.avail = avail;
	}

	public long getFiles() {
		return files;
	}

	public void setFiles(long files) {
		this.files = files;
	}

	public long getFreeFiles() {
		return freeFiles;
	}

	public void setFreeFiles(long freeFiles) {
		this.freeFiles = freeFiles;
	}

	public long getDiskReads() {
		return diskReads;
	}

	public void setDiskReads(long diskReads) {
		this.diskReads = diskReads;
	}

	public long getDiskWrites() {
		return diskWrites;
	}

	public void setDiskWrites(long diskWrites) {
		this.diskWrites = diskWrites;
	}

	public long getDiskReadBytes() {
		return diskReadBytes;
	}

	public void setDiskReadBytes(long diskReadBytes) {
		this.diskReadBytes = diskReadBytes;
	}

	public long getDiskWriteBytes() {
		return diskWriteBytes;
	}

	public void setDiskWriteBytes(long diskWriteBytes) {
		this.diskWriteBytes = diskWriteBytes;
	}

	public double getDiskQueue() {
		return diskQueue;
	}

	public void setDiskQueue(double diskQueue) {
		this.diskQueue = diskQueue;
	}

	public double getDiskServiceTime() {
		return diskServiceTime;
	}

	public void setDiskServiceTime(double diskServiceTime) {
		this.diskServiceTime = diskServiceTime;
	}

	public double getUsePercent() {
		return usePercent;
	}

	public void setUsePercent(double usePercent) {
		this.usePercent = usePercent;
	}

	public long getDiskReadsPerSecond() {
		return diskReadsPerSecond;
	}

	public void setDiskReadsPerSecond(long diskReadsPerSecond) {
		this.diskReadsPerSecond = diskReadsPerSecond;
	}

	public long getDiskWritesPerSecond() {
		return diskWritesPerSecond;
	}

	public void setDiskWritesPerSecond(long diskWritesPerSecond) {
		this.diskWritesPerSecond = diskWritesPerSecond;
	}

	public long getDiskReadBytesPerSecond() {
		return diskReadBytesPerSecond;
	}

	public void setDiskReadBytesPerSecond(long diskReadBytesPerSecond) {
		this.diskReadBytesPerSecond = diskReadBytesPerSecond;
	}

	public long getDiskWriteBytesPerSecond() {
		return diskWriteBytesPerSecond;
	}

	public void setDiskWriteBytesPerSecond(long diskWriteBytesPerSecond) {
		this.diskWriteBytesPerSecond = diskWriteBytesPerSecond;
	}
}
