package cn.batchfile.stat.agent.types;

public class Network {
	private String name;
	private String hwaddr;
	private String type;
	private String description;
	private String address;
	private String destination;
	private String broadcast;
	private String netmask;
	private long mtu;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getHwaddr() {
		return hwaddr;
	}
	
	public void setHwaddr(String hwaddr) {
		this.hwaddr = hwaddr;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public String getBroadcast() {
		return broadcast;
	}
	
	public void setBroadcast(String broadcast) {
		this.broadcast = broadcast;
	}
	
	public String getNetmask() {
		return netmask;
	}
	
	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}
	
	public long getMtu() {
		return mtu;
	}
	
	public void setMtu(long mtu) {
		this.mtu = mtu;
	}
	
}
