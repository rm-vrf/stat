package cn.batchfile.stat.domain.node;

import java.util.Date;
import java.util.List;

/**
 * 节点
 */
public class Node {
    public static final String STATUS_ONLINE = "ONLINE";
    public static final String STATUS_OFFLINE = "OFFLINE";
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    private String id;
    private String hostname;
    private String address;
    private Os os;
    private Memory memory;
    private List<Network> networks;
    private List<Disk> disks;
    private List<String> tags;
    private Date timestamp;
    private String status;

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
    public String getHostname() {
        return hostname;
    }

    /**
     * 机器名
     * @param hostname 机器名
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * 地址
     * @return 地址
     */
    public String getAddress() {
        return address;
    }

    /**
     * 地址
     * @param address 地址
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 操作系统
     * @return 操作系统
     */
    public Os getOs() {
        return os;
    }

    /**
     * 操作系统
     * @param os 操作系统
     */
    public void setOs(Os os) {
        this.os = os;
    }

    /**
     * 内存
     * @return 内存
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * 内存
     * @param memory 内存
     */
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    /**
     * 网络
     * @return 网络
     */
    public List<Network> getNetworks() {
        return networks;
    }

    /**
     * 网络
     * @param networks 网络
     */
    public void setNetworks(List<Network> networks) {
        this.networks = networks;
    }

    /**
     * 网络
     * @return 网络
     */
    public List<Disk> getDisks() {
        return disks;
    }

    /**
     * 磁盘
     * @param disks 磁盘
     */
    public void setDisks(List<Disk> disks) {
        this.disks = disks;
    }

    /**
     * 标签
     * @return 标签
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * 标签
     * @param tags 标签
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * 时间戳
     * @return 时间戳
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 时间戳
     * @param timestamp 时间戳
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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
}
