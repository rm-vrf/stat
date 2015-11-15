package cn.batchfile.stat.agent.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class Memory {
	private long total;
	private long ram;
	private long used;
	private long free;
	@JSONField(name="actual_used")
	private long actualUsed;
	@JSONField(name="actual_free")
	private long actualFree;
	@JSONField(name="used_percent")
	private double usedPercent;
	@JSONField(name="free_percent")
	private double freePercent;
	
	public long getTotal() {
		return total;
	}
	
	public void setTotal(long total) {
		this.total = total;
	}
	
	public long getRam() {
		return ram;
	}
	
	public void setRam(long ram) {
		this.ram = ram;
	}
	
	public long getUsed() {
		return used;
	}
	
	public void setUsed(long used) {
		this.used = used;
	}
	
	public long getFree() {
		return free;
	}
	
	public void setFree(long free) {
		this.free = free;
	}
	
	public long getActualUsed() {
		return actualUsed;
	}
	
	public void setActualUsed(long actualUsed) {
		this.actualUsed = actualUsed;
	}
	
	public long getActualFree() {
		return actualFree;
	}
	
	public void setActualFree(long actualFree) {
		this.actualFree = actualFree;
	}
	
	public double getUsedPercent() {
		return usedPercent;
	}
	
	public void setUsedPercent(double usedPercent) {
		this.usedPercent = usedPercent;
	}
	
	public double getFreePercent() {
		return freePercent;
	}
	
	public void setFreePercent(double freePercent) {
		this.freePercent = freePercent;
	}
}
