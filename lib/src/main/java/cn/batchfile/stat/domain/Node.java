package cn.batchfile.stat.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Node {
	private String id;
	private String hostname;
	private String agentAddress;
	private Os os;
	private Memory memory;
	private List<Network> networks = new ArrayList<Network>();
	private List<Disk> disks = new ArrayList<Disk>();
	private List<String> tags = new ArrayList<String>();
	private Date timestamp;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getHostname() {
		return hostname;
	}

	public String getAgentAddress() {
		return agentAddress;
	}

	public void setAgentAddress(String agentAddress) {
		this.agentAddress = agentAddress;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Os getOs() {
		return os;
	}
	
	public void setOs(Os os) {
		this.os = os;
	}

	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	public List<Network> getNetworks() {
		return networks;
	}

	public void setNetworks(List<Network> networks) {
		this.networks = networks;
	}

	public List<Disk> getDisks() {
		return disks;
	}

	public void setDisks(List<Disk> disks) {
		this.disks = disks;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
