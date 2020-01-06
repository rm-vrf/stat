package cn.batchfile.stat.server.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "Service")
@Table(name = "service")
public class ServiceTable {

	@Id
	@Column(length = 64)
	private String id;
	
	@Column(length = 64)
    private String namespace;
	
	@Column(length = 64)
    private String name;
	
	@Column(name = "domain_name", length = 255)
    private String domainName;
	
	@Column
    private Boolean stateful;
	
	@Column(length = 1024)
    private String image;
	
	@Column(length = 1024)
    private String container;
	
	@Column(length = 1024)
    private String deploy;
	
	@Column(name = "depends_on", length = 1024)
    private String dependsOn;
	
	@Column(name = "health_check", length = 1024)
    private String healthCheck;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public Boolean getStateful() {
		return stateful;
	}

	public void setStateful(Boolean stateful) {
		this.stateful = stateful;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getDeploy() {
		return deploy;
	}

	public void setDeploy(String deploy) {
		this.deploy = deploy;
	}

	public String getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}

	public String getHealthCheck() {
		return healthCheck;
	}

	public void setHealthCheck(String healthCheck) {
		this.healthCheck = healthCheck;
	}
}
