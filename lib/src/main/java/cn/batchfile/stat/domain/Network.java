package cn.batchfile.stat.domain;

public class Network {
	private String address;
	private String name;
	private long mtu;
	private String broadcast;
	private String description;
	private String distination;
	private long flags;
	private String hwaddr;
	private long metric;
	private String netmask;
	private String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMtu() {
		return mtu;
	}

	public void setMtu(long mtu) {
		this.mtu = mtu;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(String broadcast) {
		this.broadcast = broadcast;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDistination() {
		return distination;
	}

	public void setDistination(String distination) {
		this.distination = distination;
	}

	public long getFlags() {
		return flags;
	}

	public void setFlags(long flags) {
		this.flags = flags;
	}

	public String getHwaddr() {
		return hwaddr;
	}

	public void setHwaddr(String hwaddr) {
		this.hwaddr = hwaddr;
	}

	public long getMetric() {
		return metric;
	}

	public void setMetric(long metric) {
		this.metric = metric;
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
