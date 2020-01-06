package cn.batchfile.stat.server.domain.container;

import java.util.Date;
import java.util.List;

import cn.batchfile.stat.server.domain.service.Resources;

public class ContainerInstance {
	private String id;
	private String node;
	private String namespace;
	private String service;
	private List<String> domainNames;
	private String ip;
	private String name;
	private String image;
	private String command;
	private List<PortInstance> ports;
	private List<MountInstance> mounts;
	private Resources resources;
	private String state;
	private String description;
	private Date createTime;
	private Date startTime;
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

	public List<String> getDomainNames() {
		return domainNames;
	}

	public void setDomainNames(List<String> domainNames) {
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

	public List<PortInstance> getPorts() {
		return ports;
	}

	public void setPorts(List<PortInstance> ports) {
		this.ports = ports;
	}

	public List<MountInstance> getMounts() {
		return mounts;
	}

	public void setMounts(List<MountInstance> mounts) {
		this.mounts = mounts;
	}

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources resources) {
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
