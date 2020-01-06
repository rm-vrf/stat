package cn.batchfile.stat.server.domain.node;

import cn.batchfile.stat.server.domain.service.Resources;

public class Node {
    public static final String STATUS_ONLINE = "ONLINE";
    public static final String STATUS_OFFLINE = "OFFLINE";
    public static final String STATUS_UNKNOWN = "UNKNOWN";
    
    private String id;
    private String name;
    private Info info;
    private String apiVersion;
    private String engineVersion;
    private Containers containers;
    private Integer images;
    private String status;
    private String description;
    private Boolean slow;
    private Resources resources;

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

	public Info getInfo() {
		return info;
	}

	public void setInfo(Info info) {
		this.info = info;
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

	public Containers getContainers() {
		return containers;
	}

	public void setContainers(Containers containers) {
		this.containers = containers;
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

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

}
