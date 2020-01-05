package cn.batchfile.stat.domain.node;

import java.util.List;

/**
 * 节点
 */
public class Node {
    public static final String STATUS_ONLINE = "ONLINE";
    public static final String STATUS_OFFLINE = "OFFLINE";
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    private String id;
    private String name;
    private String dockerHost;
    private String publicIp;
    private String apiVersion;
    private String engineVersion;
    private String os;
    private String architecture;
    private List<Integer> containers;
    private Integer images;
    private Integer cpus;
    private Long memory;
    private List<String> labels;
    private String status;
    private Boolean slow;

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
     * 机器名
     * @return 机器名
     */
    public String getName() {
        return name;
    }

    /**
     * 机器名
     * @param name 机器名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Docker API 地址
     * @return Docker API 地址
     */
    public String getDockerHost() {
        return dockerHost;
    }

    /**
     * Docker API 地址
     * @param dockerHost Docker API 地址
     */
    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    /**
     * 公开 IP
     * @return 公开 IP
     */
    public String getPublicIp() {
        return publicIp;
    }

    /**
     * 公开 IP
     * @param publicIp 公开 IP
     */
    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    /**
     * API 版本
     * @return API 版本
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * API 版本
     * @param apiVersion API 版本
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Docker 引擎版本
     * @return Docker 引擎版本
     */
    public String getEngineVersion() {
        return engineVersion;
    }

    /**
     * Docker 引擎版本
     * @param engineVersion Docker 引擎版本
     */
    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * 操作系统
     * @return 操作系统
     */
    public String getOs() {
        return os;
    }

    /**
     * 操作系统
     * @param os 操作系统
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * 架构
     * @return 架构
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * 架构
     * @param architecture 架构
     */
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * 容器数量（total, running, paused, stopped）
     * @return 容器数量
     */
    public List<Integer> getContainers() {
        return containers;
    }

    /**
     * 容器数量（total, running, paused, stopped）
     * @param containers 容器数量
     */
    public void setContainers(List<Integer> containers) {
        this.containers = containers;
    }

    /**
     * 镜像数量
     * @return 镜像数量
     */
    public Integer getImages() {
        return images;
    }

    /**
     * 镜像数量
     * @param images 镜像数量
     */
    public void setImages(Integer images) {
        this.images = images;
    }

    /**
     * CPU 线程数
     * @return CPU 线程数
     */
    public Integer getCpus() {
        return cpus;
    }

    /**
     * CPU 线程数
     * @param cpus CPU 线程数
     */
    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    /**
     * 内存数量
     * @return 内存数量
     */
    public Long getMemory() {
        return memory;
    }

    /**
     * 内存数量
     * @param memory 内存数量
     */
    public void setMemory(Long memory) {
        this.memory = memory;
    }

    /**
     * 标签
     * @return 标签
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     * 标签
     * @param labels 标签
     */
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    /**
     * 状态
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 状态
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 慢节点
     * @return 慢节点
     */
    public Boolean getSlow() {
        return slow;
    }

    /**
     * 慢节点
     * @param slow 慢节点
     */
    public void setSlow(Boolean slow) {
        this.slow = slow;
    }

}
