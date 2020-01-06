package cn.batchfile.stat.server.domain.node;

import java.util.List;

import cn.batchfile.stat.server.domain.service.ResourcesControl;

public class Info {
    private String dockerHost;
    private String publicIp;
    private List<String> labels;
    private String os;
    private String architecture;
    private ResourcesControl resources;

	public String getDockerHost() {
		return dockerHost;
	}

	public void setDockerHost(String dockerHost) {
		this.dockerHost = dockerHost;
	}

	public String getPublicIp() {
		return publicIp;
	}

	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
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

	public ResourcesControl getResources() {
		return resources;
	}

	public void setResources(ResourcesControl resources) {
		this.resources = resources;
	}
}
