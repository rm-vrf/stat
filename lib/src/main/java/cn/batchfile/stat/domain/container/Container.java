package cn.batchfile.stat.domain.container;

import java.util.Date;

/**
 * 容器实例
 */
public class Container {
    public static final String STATUS_INIT = "INIT";
    public static final String STATUS_CREATING = "CREATING";
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_STOPPED = "STOPPED";

    private String node;
    private String service;
    private String image;
    private String id;
    private long pid;
    private String status;
    private String bundle;
    private String owner;
    private Date createTime;
    private Date updateTime;
    private String config;

    /**
     * 所在节点
     * @return 节点编号
     */
    public String getNode() {
        return node;
    }

    /**
     * 所在节点
     * @param node 节点编号
     */
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * 服务名称
     * @return 服务名称
     */
    public String getService() {
        return service;
    }

    /**
     * 服务名称
     * @param service 服务名称
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * 镜像，例如："python:2.7"
     * @return 镜像
     */
    public String getImage() {
        return image;
    }

    /**
     * 镜像，例如："python:2.7"
     * @param image 镜像
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * 编号
     * @return 编号
     */
    public String getId() {
        return id;
    }

    /**
     * 编号
     * @param id 编号
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 进程号
     * @return 进程号
     */
    public long getPid() {
        return pid;
    }

    /**
     * 进程号
     * @param pid 进程号
     */
    public void setPid(long pid) {
        this.pid = pid;
    }

    /**
     * 容器状态
     * @return 容器状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 容器状态
     * @param status 容器状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 容器位置
     * @return 容器位置
     */
    public String getBundle() {
        return bundle;
    }

    /**
     * 容器位置
     * @param bundle 容器位置
     */
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    /**
     * 容器创建人
     * @return 容器创建人
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 容器创建人
     * @param owner 容器创建人
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 创建时间
     * @return 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 变更时间
     * @return 变更时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 变更时间
     * @param updateTime 变更时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 配置
     * @return 配置
     */
    public String getConfig() {
        return config;
    }

    /**
     * 配置
     * @param config 配置
     */
    public void setConfig(String config) {
        this.config = config;
    }
}
