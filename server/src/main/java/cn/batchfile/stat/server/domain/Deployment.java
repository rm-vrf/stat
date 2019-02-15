package cn.batchfile.stat.server.domain;

import cn.batchfile.stat.domain.ControlGroup;

public class Deployment {

	private String service;
	private String node;
	private ControlGroup reservations;
	
	public String getService() {
		return service;
	}
	
	public void setService(String service) {
		this.service = service;
	}
	
	public String getNode() {
		return node;
	}
	
	public void setNode(String node) {
		this.node = node;
	}
	
	public ControlGroup getReservations() {
		return reservations;
	}
	
	public void setReservations(ControlGroup reservations) {
		this.reservations = reservations;
	}

}
