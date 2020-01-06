package cn.batchfile.stat.server.domain.service;

import java.util.List;

/**
 * 服务定义
 */
public class Service {
    private String name;
    private String domainName;
    private Boolean stateful;
    private Image image;
    private Container container;
    private ServiceDeploy deploy;
    private List<String> dependsOn;
    private HealthCheck healthCheck;

    /**
     * 服务名称
     * @return 服务名称
     */
    public String getName() {
        return name;
    }

    /**
     * 服务名称
     * @param name 服务名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 域名
     * @return 域名
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * 域名
     * @param domainName 域名
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * 有状态服务
     * @return 有状态服务
     */
    public Boolean getStateful() {
        return stateful;
    }

    /**
     * 有状态服务
     * @param stateful 有状态服务
     */
    public void setStateful(Boolean stateful) {
        this.stateful = stateful;
    }

    /**
     * 镜像
     * @return 镜像
     */
    public Image getImage() {
        return image;
    }

    /**
     * 镜像
     * @param image 镜像
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * 容器定义
     * @return 容器定义
     */
    public Container getContainer() {
        return container;
    }

    /**
     * 容器定义
     * @param container 容器定义
     */
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * 部署定义
     * @return 部署定义
     */
    public ServiceDeploy getDeploy() {
        return deploy;
    }

    /**
     * 部署定义
     * @param deploy 部署定义
     */
    public void setDeploy(ServiceDeploy deploy) {
        this.deploy = deploy;
    }

    /**
     * 依赖服务
     * @return 依赖服务
     */
    public List<String> getDependsOn() {
        return dependsOn;
    }

    /**
     * 依赖服务
     * @param dependsOn 依赖服务
     */
    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * 健康检查
     * @return 健康检查
     */
    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    /**
     * 健康检查
     * @param healthCheck 健康检查
     */
    public void setHealthCheck(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

}
