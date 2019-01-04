package cn.batchfile.stat.server.domain;

import java.util.List;
import java.util.Map;

import cn.batchfile.stat.domain.Disk;
import cn.batchfile.stat.domain.Memory;
import cn.batchfile.stat.domain.Network;
import cn.batchfile.stat.domain.Os;

public class Node {
	
    public static final String STATUS_UNKNOWN = "UNKNOWN";
    public static final String STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";
    public static final String STATUS_UP = "UP";
    public static final String STATUS_DOWN = "DOWN";
    public static final String STATUS_OFFLINE = "OFFLINE";
    public static final String STATUS_RESTRICTED = "RESTRICTED";

	private String id;
	private String hostname;
	private String address;
	private String serviceUrl;
	private String status;
	private Os os;
	private Memory memory;
	private List<Network> networks;
	private List<Disk> disks;
	private long diskTotal;
	private List<String> tags;
	private Map<String, Object> metadata;

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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public long getDiskTotal() {
		return diskTotal;
	}

	public void setDiskTotal(long diskTotal) {
		this.diskTotal = diskTotal;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
