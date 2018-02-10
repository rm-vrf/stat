package cn.batchfile.stat.domain;

public class Network {
	private String name;
	//private boolean pointToPoint;
	//private int index;
	//private boolean up;
	private int mtu;
	//private boolean virtual;
	private String address;
	private boolean siteLocal;
	//private boolean loopback;
	//private boolean linkLocal;
	//private boolean anyLocal;
	//private boolean multicast;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public boolean isPointToPoint() {
//		return pointToPoint;
//	}
//
//	public void setPointToPoint(boolean pointToPoint) {
//		this.pointToPoint = pointToPoint;
//	}
//
//	public int getIndex() {
//		return index;
//	}
//
//	public void setIndex(int index) {
//		this.index = index;
//	}
//
//	public boolean isUp() {
//		return up;
//	}
//
//	public void setUp(boolean up) {
//		this.up = up;
//	}

	public int getMtu() {
		return mtu;
	}

	public void setMtu(int mtu) {
		this.mtu = mtu;
	}

//	public boolean isVirtual() {
//		return virtual;
//	}
//
//	public void setVirtual(boolean virtual) {
//		this.virtual = virtual;
//	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isSiteLocal() {
		return siteLocal;
	}

	public void setSiteLocal(boolean siteLocal) {
		this.siteLocal = siteLocal;
	}

//	public boolean isLoopback() {
//		return loopback;
//	}
//
//	public void setLoopback(boolean loopback) {
//		this.loopback = loopback;
//	}
//
//	public boolean isLinkLocal() {
//		return linkLocal;
//	}
//
//	public void setLinkLocal(boolean linkLocal) {
//		this.linkLocal = linkLocal;
//	}
//
//	public boolean isAnyLocal() {
//		return anyLocal;
//	}
//
//	public void setAnyLocal(boolean anyLocal) {
//		this.anyLocal = anyLocal;
//	}
//
//	public boolean isMulticast() {
//		return multicast;
//	}
//
//	public void setMulticast(boolean multicast) {
//		this.multicast = multicast;
//	}
}
