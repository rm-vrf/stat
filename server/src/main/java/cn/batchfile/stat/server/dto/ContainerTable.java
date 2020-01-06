package cn.batchfile.stat.server.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "Container")
@Table(name = "container")
public class ContainerTable {

	@Id
	@Column(length = 64)
	private String id;
	
	@Column(length = 64)
	private String node;
	
	@Column(length = 64)
	private String namespace;
	
	@Column(length = 64)
	private String service;
	
	@Column(name = "domain_names", length = 1024)
	private String domainNames;
	
	@Column(length = 32)
	private String ip;
	
	@Column(length = 64)
	private String name;
	
	@Column(length = 64)
	private String image;
	
	@Column(length = 255)
	private String command;
	
	@Column(length = 1024)
	private String ports;
	
	@Column(length = 1024)
	private String mounts;
	
	@Column(length = 1024)
	private String resources;
	
	@Column(length = 16)
	private String state;
	
	@Column(length = 64)
	private String description;
	
	@Column(name = "create_time")
	private Date createTime;
	
	@Column(name = "start_time")
	private Date startTime;
	
	@Column(name = "stop_time")
	private Date stopTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getDomainNames() {
		return domainNames;
	}

	public void setDomainNames(String domainNames) {
		this.domainNames = domainNames;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getPorts() {
		return ports;
	}

	public void setPorts(String ports) {
		this.ports = ports;
	}

	public String getMounts() {
		return mounts;
	}

	public void setMounts(String mounts) {
		this.mounts = mounts;
	}

	public String getResources() {
		return resources;
	}

	public void setResources(String resources) {
		this.resources = resources;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getStopTime() {
		return stopTime;
	}

	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
}
