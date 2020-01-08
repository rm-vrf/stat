package cn.batchfile.stat.server.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "Master")
@Table(name = "master")
public class MasterTable {

	@Id
	@Column(name = "id", length = 64)
	private String id;
	
	@Column(name = "address", length = 64)
	private String address;
	
	@Column(name = "hostname", length = 64)
	private String hostname;
	
	@Column(name = "start_time")
	private Date startTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
}
