package cn.batchfile.stat.server.domain;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

import cn.batchfile.stat.agent.domain.Stack;

public class StackData {
	@JSONField(name="command_id")
	private String commandId;
	private Date time;
	private long pid;
	private int count;
	private List<Stack> stacks;

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<Stack> getStacks() {
		return stacks;
	}

	public void setStacks(List<Stack> stacks) {
		this.stacks = stacks;
	}
}
