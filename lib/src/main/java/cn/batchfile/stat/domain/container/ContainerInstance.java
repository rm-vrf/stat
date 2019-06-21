package cn.batchfile.stat.domain.container;

import java.util.Date;

/**
 * 容器实例
 */
public class ContainerInstance {

    private String node;
    private String service;
    private Boolean valid;
    private String id;
    private Long pid;
    private String status;
    private String bundle;
    private String owner;
    private Date createTime;
    private Date updateTime;

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
     * 是否有效
     * @return 是否有效
     */
    public Boolean getValid() {
        return valid;
    }

    /**
     * 是否有效
     * @param valid 是否有效
     */
    public void setValid(Boolean valid) {
        this.valid = valid;
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
    public Long getPid() {
        return pid;
    }

    /**
     * 进程号
     * @param pid 进程号
     */
    public void setPid(Long pid) {
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
}
