package cn.batchfile.stat.server.domain;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public class Node {
	@JSONField(name="agent_id")
	private String agentId;
	private String schema;
	private String address;
	private int port;
	private String hostname;
	private String description;
	@JSONField(name="os_name")
	private String osName;
	@JSONField(name="os_version")
	private String osVersion;
	private int cpu;
	private String architecture;
	@JSONField(name="id_rsa")
	private String idRsa;
	@JSONField(name="id_rsa_pub")
	private String idRsaPub;
	@JSONField(name="create_time")
	private Date createTime;
	
	public String getAgentId() {
		return agentId;
	}
	
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getOsName() {
		return osName;
	}
	
	public void setOsName(String osName) {
		this.osName = osName;
	}
	
	public String getOsVersion() {
		return osVersion;
	}
	
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	
	public int getCpu() {
		return cpu;
	}
	
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	
	public String getArchitecture() {
		return architecture;
	}
	
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}
	
	public String getIdRsa() {
		return idRsa;
	}
	
	public void setIdRsa(String idRsa) {
		this.idRsa = idRsa;
	}
	
	public String getIdRsaPub() {
		return idRsaPub;
	}
	
	public void setIdRsaPub(String idRsaPub) {
		this.idRsaPub = idRsaPub;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
