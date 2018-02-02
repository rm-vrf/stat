//package cn.batchfile.stat.server.domain;
//
//import java.util.Date;
//
//import com.alibaba.fastjson.annotation.JSONField;
//
//public class NetworkData {
//	@JSONField(name="agent_id")
//	private String agentId;
//	private String address;
//	private Date time;
//	@JSONField(name="rx_bytes")
//	private long rxBytes;
//	@JSONField(name="rx_bytes_per_second")
//	private long rxBytesPerSecond;
//	@JSONField(name="rx_packets")
//	private long rxPackets;
//	@JSONField(name="rx_packets_per_second")
//	private long rxPacketsPerSecond;
//	@JSONField(name="rx_errors")
//	private long rxErrors;
//	@JSONField(name="rx_errors_per_second")
//	private long rxErrorsPerSecond;
//	@JSONField(name="rx_dropped")
//	private long rxDropped;
//	@JSONField(name="rx_dropped_per_second")
//	private long rxDroppedPerSecond;
//	@JSONField(name="rx_overruns")
//	private long rxOverruns;
//	@JSONField(name="rx_overruns_per_second")
//	private long rxOverrunsPerSecond;
//	@JSONField(name="rx_frame")
//	private long rxFrame;
//	@JSONField(name="rx_frame_per_second")
//	private long rxFramePerSecond;
//	@JSONField(name="tx_bytes")
//	private long txBytes;
//	@JSONField(name="tx_bytes_per_second")
//	private long txBytesPerSecond;
//	@JSONField(name="tx_packets")
//	private long txPackets;
//	@JSONField(name="tx_packets_per_second")
//	private long txPacketsPerSecond;
//	@JSONField(name="tx_errors")
//	private long txErrors;
//	@JSONField(name="tx_errors_per_second")
//	private long txErrorsPerSecond;
//	@JSONField(name="tx_dropped")
//	private long txDropped;
//	@JSONField(name="tx_dropped_per_second")
//	private long txDroppedPerSecond;
//	@JSONField(name="tx_overruns")
//	private long txOverruns;
//	@JSONField(name="tx_overruns_per_second")
//	private long txOverrunsPerSecond;
//	@JSONField(name="tx_collisions")
//	private long txCollisions;
//	@JSONField(name="tx_carrier")
//	private long txCarrier;
//	@JSONField(name="tx_carrier_per_second")
//	private long txCarrierPerSecond;
//	private long speed;
//
//	public String getAgentId() {
//		return agentId;
//	}
//
//	public void setAgentId(String agentId) {
//		this.agentId = agentId;
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
//	public Date getTime() {
//		return time;
//	}
//
//	public void setTime(Date time) {
//		this.time = time;
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
//	public long getRxBytesPerSecond() {
//		return rxBytesPerSecond;
//	}
//
//	public void setRxBytesPerSecond(long rxBytesPerSecond) {
//		this.rxBytesPerSecond = rxBytesPerSecond;
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
//	public long getRxPacketsPerSecond() {
//		return rxPacketsPerSecond;
//	}
//
//	public void setRxPacketsPerSecond(long rxPacketsPerSecond) {
//		this.rxPacketsPerSecond = rxPacketsPerSecond;
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
//	public long getRxErrorsPerSecond() {
//		return rxErrorsPerSecond;
//	}
//
//	public void setRxErrorsPerSecond(long rxErrorsPerSecond) {
//		this.rxErrorsPerSecond = rxErrorsPerSecond;
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
//	public long getRxDroppedPerSecond() {
//		return rxDroppedPerSecond;
//	}
//
//	public void setRxDroppedPerSecond(long rxDroppedPerSecond) {
//		this.rxDroppedPerSecond = rxDroppedPerSecond;
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
//	public long getRxOverrunsPerSecond() {
//		return rxOverrunsPerSecond;
//	}
//
//	public void setRxOverrunsPerSecond(long rxOverrunsPerSecond) {
//		this.rxOverrunsPerSecond = rxOverrunsPerSecond;
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
//	public long getRxFramePerSecond() {
//		return rxFramePerSecond;
//	}
//
//	public void setRxFramePerSecond(long rxFramePerSecond) {
//		this.rxFramePerSecond = rxFramePerSecond;
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
//	public long getTxBytesPerSecond() {
//		return txBytesPerSecond;
//	}
//
//	public void setTxBytesPerSecond(long txBytesPerSecond) {
//		this.txBytesPerSecond = txBytesPerSecond;
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
//	public long getTxPacketsPerSecond() {
//		return txPacketsPerSecond;
//	}
//
//	public void setTxPacketsPerSecond(long txPacketsPerSecond) {
//		this.txPacketsPerSecond = txPacketsPerSecond;
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
//	public long getTxErrorsPerSecond() {
//		return txErrorsPerSecond;
//	}
//
//	public void setTxErrorsPerSecond(long txErrorsPerSecond) {
//		this.txErrorsPerSecond = txErrorsPerSecond;
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
//	public long getTxDroppedPerSecond() {
//		return txDroppedPerSecond;
//	}
//
//	public void setTxDroppedPerSecond(long txDroppedPerSecond) {
//		this.txDroppedPerSecond = txDroppedPerSecond;
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
//	public long getTxOverrunsPerSecond() {
//		return txOverrunsPerSecond;
//	}
//
//	public void setTxOverrunsPerSecond(long txOverrunsPerSecond) {
//		this.txOverrunsPerSecond = txOverrunsPerSecond;
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
//	public long getTxCarrierPerSecond() {
//		return txCarrierPerSecond;
//	}
//
//	public void setTxCarrierPerSecond(long txCarrierPerSecond) {
//		this.txCarrierPerSecond = txCarrierPerSecond;
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
