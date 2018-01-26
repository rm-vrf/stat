//package cn.batchfile.stat.agent.domain;
//
//import com.alibaba.fastjson.annotation.JSONField;
//
//public class Network {
//	private String name;
//	private String hwaddr;
//	private String type;
//	private String description;
//	private String address;
//	private String destination;
//	private String broadcast;
//	private String netmask;
//	private long flags;
//	private long mtu;
//	private long metric;
//	@JSONField(name="rx_bytes")
//	private long rxBytes;
//	@JSONField(name="rx_packets")
//	private long rxPackets;
//	@JSONField(name="rx_errors")
//	private long rxErrors;
//	@JSONField(name="rx_dropped")
//	private long rxDropped;
//	@JSONField(name="rx_overruns")
//	private long rxOverruns;
//	@JSONField(name="rx_frame")
//	private long rxFrame;
//	@JSONField(name="tx_bytes")
//	private long txBytes;
//	@JSONField(name="tx_packets")
//	private long txPackets;
//	@JSONField(name="tx_errors")
//	private long txErrors;
//	@JSONField(name="tx_dropped")
//	private long txDropped;
//	@JSONField(name="tx_overruns")
//	private long txOverruns;
//	@JSONField(name="tx_collisions")
//	private long txCollisions;
//	@JSONField(name="tx_carrier")
//	private long txCarrier;
//	private long speed;
//	
//	public String getName() {
//		return name;
//	}
//	
//	public void setName(String name) {
//		this.name = name;
//	}
//	
//	public String getHwaddr() {
//		return hwaddr;
//	}
//	
//	public void setHwaddr(String hwaddr) {
//		this.hwaddr = hwaddr;
//	}
//	
//	public String getType() {
//		return type;
//	}
//	
//	public void setType(String type) {
//		this.type = type;
//	}
//	
//	public String getDescription() {
//		return description;
//	}
//	
//	public void setDescription(String description) {
//		this.description = description;
//	}
//	
//	public String getAddress() {
//		return address;
//	}
//	
//	public void setAddress(String address) {
//		this.address = address;
//	}
//	
//	public String getDestination() {
//		return destination;
//	}
//	
//	public void setDestination(String destination) {
//		this.destination = destination;
//	}
//	
//	public String getBroadcast() {
//		return broadcast;
//	}
//	
//	public void setBroadcast(String broadcast) {
//		this.broadcast = broadcast;
//	}
//	
//	public String getNetmask() {
//		return netmask;
//	}
//	
//	public void setNetmask(String netmask) {
//		this.netmask = netmask;
//	}
//	
//	public long getFlags() {
//		return flags;
//	}
//	
//	public void setFlags(long flags) {
//		this.flags = flags;
//	}
//	
//	public long getMtu() {
//		return mtu;
//	}
//	
//	public void setMtu(long mtu) {
//		this.mtu = mtu;
//	}
//	
//	public long getMetric() {
//		return metric;
//	}
//	
//	public void setMetric(long metric) {
//		this.metric = metric;
//	}
//	
//	public long getRxBytes() {
//		return rxBytes;
//	}
//	
//	public void setRxBytes(long rxBytes) {
//		this.rxBytes = rxBytes;
//	}
//	
//	public long getRxPackets() {
//		return rxPackets;
//	}
//	
//	public void setRxPackets(long rxPackets) {
//		this.rxPackets = rxPackets;
//	}
//	
//	public long getRxErrors() {
//		return rxErrors;
//	}
//	
//	public void setRxErrors(long rxErrors) {
//		this.rxErrors = rxErrors;
//	}
//	
//	public long getRxDropped() {
//		return rxDropped;
//	}
//	
//	public void setRxDropped(long rxDropped) {
//		this.rxDropped = rxDropped;
//	}
//	
//	public long getRxOverruns() {
//		return rxOverruns;
//	}
//	
//	public void setRxOverruns(long rxOverruns) {
//		this.rxOverruns = rxOverruns;
//	}
//	
//	public long getRxFrame() {
//		return rxFrame;
//	}
//	
//	public void setRxFrame(long rxFrame) {
//		this.rxFrame = rxFrame;
//	}
//	
//	public long getTxBytes() {
//		return txBytes;
//	}
//	
//	public void setTxBytes(long txBytes) {
//		this.txBytes = txBytes;
//	}
//	
//	public long getTxPackets() {
//		return txPackets;
//	}
//	
//	public void setTxPackets(long txPackets) {
//		this.txPackets = txPackets;
//	}
//	
//	public long getTxErrors() {
//		return txErrors;
//	}
//	
//	public void setTxErrors(long txErrors) {
//		this.txErrors = txErrors;
//	}
//	
//	public long getTxDropped() {
//		return txDropped;
//	}
//	
//	public void setTxDropped(long txDropped) {
//		this.txDropped = txDropped;
//	}
//	
//	public long getTxOverruns() {
//		return txOverruns;
//	}
//	
//	public void setTxOverruns(long txOverruns) {
//		this.txOverruns = txOverruns;
//	}
//	
//	public long getTxCollisions() {
//		return txCollisions;
//	}
//	
//	public void setTxCollisions(long txCollisions) {
//		this.txCollisions = txCollisions;
//	}
//	
//	public long getTxCarrier() {
//		return txCarrier;
//	}
//	
//	public void setTxCarrier(long txCarrier) {
//		this.txCarrier = txCarrier;
//	}
//	
//	public long getSpeed() {
//		return speed;
//	}
//	
//	public void setSpeed(long speed) {
//		this.speed = speed;
//	}
//}
