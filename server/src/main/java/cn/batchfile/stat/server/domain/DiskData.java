//package cn.batchfile.stat.server.domain;
//
//import java.util.Date;
//
//import com.alibaba.fastjson.annotation.JSONField;
//
//public class DiskData {
//	@JSONField(name="agent_id")
//	private String agentId;
//	@JSONField(name="dir_name")
//	private String dirName;
//	private Date time;
//	private long total;
//	private long free;
//	private long used;
//	private long avail;
//	private long files;
//	@JSONField(name="free_files")
//	private long freeFiles;
//	@JSONField(name="disk_reads")
//	private long diskReads;
//	@JSONField(name="disk_reads_per_second")
//	private long diskReadsPerSecond;
//	@JSONField(name="disk_writes")
//	private long diskWrites;
//	@JSONField(name="disk_writes_per_second")
//	private long diskWritesPerSecond;
//	@JSONField(name="disk_read_bytes")
//	private long diskReadBytes;
//	@JSONField(name="disk_read_bytes_per_second")
//	private long diskReadBytesPerSecond;
//	@JSONField(name="disk_write_bytes")
//	private long diskWriteBytes;
//	@JSONField(name="disk_write_bytes_per_second")
//	private long diskWriteBytesPerSecond;
//	@JSONField(name="disk_queue")
//	private double diskQueue;
//	@JSONField(name="disk_service_time")
//	private double diskServiceTime;
//	@JSONField(name="use_percent")
//	private double usePercent;
//
//	public String getAgentId() {
//		return agentId;
//	}
//
//	public void setAgentId(String agentId) {
//		this.agentId = agentId;
//	}
//
//	public String getDirName() {
//		return dirName;
//	}
//
//	public void setDirName(String dirName) {
//		this.dirName = dirName;
//	}
//
//	public Date getTime() {
//		return time;
//	}
//
//	public void setTime(Date time) {
//		this.time = time;
//	}
//
//	public long getTotal() {
//		return total;
//	}
//
//	public void setTotal(long total) {
//		this.total = total;
//	}
//
//	public long getFree() {
//		return free;
//	}
//
//	public void setFree(long free) {
//		this.free = free;
//	}
//
//	public long getUsed() {
//		return used;
//	}
//
//	public void setUsed(long used) {
//		this.used = used;
//	}
//
//	public long getAvail() {
//		return avail;
//	}
//
//	public void setAvail(long avail) {
//		this.avail = avail;
//	}
//
//	public long getFiles() {
//		return files;
//	}
//
//	public void setFiles(long files) {
//		this.files = files;
//	}
//
//	public long getFreeFiles() {
//		return freeFiles;
//	}
//
//	public void setFreeFiles(long freeFiles) {
//		this.freeFiles = freeFiles;
//	}
//
//	public long getDiskReads() {
//		return diskReads;
//	}
//
//	public void setDiskReads(long diskReads) {
//		this.diskReads = diskReads;
//	}
//
//	public long getDiskReadsPerSecond() {
//		return diskReadsPerSecond;
//	}
//
//	public void setDiskReadsPerSecond(long diskReadsPerSecond) {
//		this.diskReadsPerSecond = diskReadsPerSecond;
//	}
//
//	public long getDiskWrites() {
//		return diskWrites;
//	}
//
//	public void setDiskWrites(long diskWrites) {
//		this.diskWrites = diskWrites;
//	}
//
//	public long getDiskWritesPerSecond() {
//		return diskWritesPerSecond;
//	}
//
//	public void setDiskWritesPerSecond(long diskWritesPerSecond) {
//		this.diskWritesPerSecond = diskWritesPerSecond;
//	}
//
//	public long getDiskReadBytes() {
//		return diskReadBytes;
//	}
//
//	public void setDiskReadBytes(long diskReadBytes) {
//		this.diskReadBytes = diskReadBytes;
//	}
//
//	public long getDiskReadBytesPerSecond() {
//		return diskReadBytesPerSecond;
//	}
//
//	public void setDiskReadBytesPerSecond(long diskReadBytesPerSecond) {
//		this.diskReadBytesPerSecond = diskReadBytesPerSecond;
//	}
//
//	public long getDiskWriteBytes() {
//		return diskWriteBytes;
//	}
//
//	public void setDiskWriteBytes(long diskWriteBytes) {
//		this.diskWriteBytes = diskWriteBytes;
//	}
//
//	public long getDiskWriteBytesPerSecond() {
//		return diskWriteBytesPerSecond;
//	}
//
//	public void setDiskWriteBytesPerSecond(long diskWriteBytesPerSecond) {
//		this.diskWriteBytesPerSecond = diskWriteBytesPerSecond;
//	}
//
//	public double getDiskQueue() {
//		return diskQueue;
//	}
//
//	public void setDiskQueue(double diskQueue) {
//		this.diskQueue = diskQueue;
//	}
//
//	public double getDiskServiceTime() {
//		return diskServiceTime;
//	}
//
//	public void setDiskServiceTime(double diskServiceTime) {
//		this.diskServiceTime = diskServiceTime;
//	}
//
//	public double getUsePercent() {
//		return usePercent;
//	}
//
//	public void setUsePercent(double usePercent) {
//		this.usePercent = usePercent;
//	}
//}
