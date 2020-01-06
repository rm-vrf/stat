package cn.batchfile.stat.server.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "Node")
@Table(name = "node")
public class NodeTable {

	@Id
	@Column(length = 64)
	private String id;
	
	@Column(length = 64)
	private String name;
	
	@Column(name = "docker_host", length = 32)
    private String dockerHost;
	
	@Column(name = "public_ip", length = 32)
    private String publicIp;
	
	@Column(length = 128)
	private String labels;
	
	@Column(length = 64)
    private String os;
	
	@Column(length = 64)
    private String architecture;
	
	@Column(name = "api_version", length = 16)
    private String apiVersion;
	
	@Column(name = "engine_version", length = 16)
    private String engineVersion;
	
	@Column(name = "containers_total")
	private Integer containersTotal;
	
	@Column(name = "containers_running")
	private Integer containersRunning;
	
	@Column(name = "containers_paused")
	private Integer containersPaused;
	
	@Column(name = "containers_stopped")
	private Integer containersStopped;
	
	@Column
	private Integer images;
	
	@Column(length = 32)
	private String status;
	
	@Column(length = 64)
	private String description;
	
	@Column
	private Boolean slow;
	
	@Column(name = "total_cpus")
	private Float totalCpus;
	
	@Column(name = "total_memory", length = 16)
	private String totalMemory;
	
	@Column(name = "limit_cpus")
	private Float limitCpus;
	
	@Column(name = "limit_memory", length = 16)
	private String limitMemory;
	
	@Column(name = "request_cpus")
	private Float requestCpus;

	@Column(name = "request_memory", length = 16)
	private String requestMemory;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDockerHost() {
		return dockerHost;
	}

	public void setDockerHost(String dockerHost) {
		this.dockerHost = dockerHost;
	}

	public String getPublicIp() {
		return publicIp;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getEngineVersion() {
		return engineVersion;
	}

	public void setEngineVersion(String engineVersion) {
		this.engineVersion = engineVersion;
	}

	public Integer getContainersTotal() {
		return containersTotal;
	}

	public void setContainersTotal(Integer containersTotal) {
		this.containersTotal = containersTotal;
	}

	public Integer getContainersRunning() {
		return containersRunning;
	}

	public void setContainersRunning(Integer containersRunning) {
		this.containersRunning = containersRunning;
	}

	public Integer getContainersPaused() {
		return containersPaused;
	}

	public void setContainersPaused(Integer containersPaused) {
		this.containersPaused = containersPaused;
	}

	public Integer getContainersStopped() {
		return containersStopped;
	}

	public void setContainersStopped(Integer containersStopped) {
		this.containersStopped = containersStopped;
	}

	public Integer getImages() {
		return images;
	}

	public void setImages(Integer images) {
		this.images = images;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getSlow() {
		return slow;
	}

	public void setSlow(Boolean slow) {
		this.slow = slow;
	}

	public Float getTotalCpus() {
		return totalCpus;
	}

	public void setTotalCpus(Float totalCpus) {
		this.totalCpus = totalCpus;
	}

	public String getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(String totalMemory) {
		this.totalMemory = totalMemory;
	}

	public Float getLimitCpus() {
		return limitCpus;
	}

	public void setLimitCpus(Float limitCpus) {
		this.limitCpus = limitCpus;
	}

	public String getLimitMemory() {
		return limitMemory;
	}

	public void setLimitMemory(String limitMemory) {
		this.limitMemory = limitMemory;
	}

	public Float getRequestCpus() {
		return requestCpus;
	}

	public void setRequestCpus(Float requestCpus) {
		this.requestCpus = requestCpus;
	}

	public String getRequestMemory() {
		return requestMemory;
	}

	public void setRequestMemory(String requestMemory) {
		this.requestMemory = requestMemory;
	}
}
