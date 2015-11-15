package cn.batchfile.stat.agent.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class Disk {
	@JSONField(name="dir_name")
	private String dirName;
	@JSONField(name="dev_name")
	private String devName;
	@JSONField(name="type_name")
	private String typeName;
	@JSONField(name="sys_type_name")
	private String sysTypeName;
	private String options;
	private int type;
	private long flags;
	private long total;
	private long free;
	private long used;
	private long avail;
	private long files;
	@JSONField(name="free_files")
	private long freeFiles;
	@JSONField(name="disk_reads")
	private long diskReads;
	@JSONField(name="disk_writes")
	private long diskWrites;
	@JSONField(name="disk_read_bytes")
	private long diskReadBytes;
	@JSONField(name="disk_write_bytes")
	private long diskWriteBytes;
	@JSONField(name="disk_queue")
	private double diskQueue;
	@JSONField(name="disk_service_time")
	private double diskServiceTime;
	@JSONField(name="use_percent")
	private double usePercent;
	
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
	
	public long getFlags() {
		return flags;
	}
	
	public void setFlags(long flags) {
		this.flags = flags;
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
}
