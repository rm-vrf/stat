package cn.batchfile.stat.server.domain.service;

/**
 * 端口定义
 */
public class Port {
    private Integer containerPort;
    private Integer publicPort;

    /**
     * 容器端口
     * @return 容器端口
     */
    public Integer getContainerPort() {
        return containerPort;
    }

    /**
     * 容器端口
     * @param containerPort 容器端口
     */
    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    /**
     * 公开端口
     * @return 公开端口
     */
    public Integer getPublicPort() {
        return publicPort;
    }

    /**
     * 公开端口
     * @param publicPort 公开端口
     */
    public void setPublicPort(Integer publicPort) {
        this.publicPort = publicPort;
    }

}
