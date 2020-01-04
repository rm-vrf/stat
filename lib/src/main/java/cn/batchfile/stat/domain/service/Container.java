package cn.batchfile.stat.domain.service;

import java.util.List;
import java.util.Map;

/**
 * 容器定义
 */
public class Container {

    private List<Port> ports;
    private Logging logging;
    private List<Mount> mounts;
    private List<Host> hosts;
    private List<Ulimit> ulimits;
    private Map<String, String> sysctls;

    /**
     * 端口定义
     * @return 端口定义
     */
    public List<Port> getPorts() {
        return ports;
    }

    /**
     * 端口定义
     * @param ports 端口定义
     */
    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    /**
     * 日志设置
     * @return 日志设置
     */
    public Logging getLogging() {
        return logging;
    }

    /**
     * 日志设置
     * @param logging 日志设置
     */
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    /**
     * 挂载设置
     * @return 挂载设置
     */
    public List<Mount> getMounts() {
        return mounts;
    }

    /**
     * 挂载设置
     * @param mounts 挂载设置
     */
    public void setMounts(List<Mount> mounts) {
        this.mounts = mounts;
    }

    /**
     * 主机名映射
     * @return 主机名映射
     */
    public List<Host> getHosts() {
        return hosts;
    }

    /**
     * 主机名映射
     * @param hosts 主机名映射
     */
    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    /**
     * A list of resource limits to set in the container. For example: {"Name": "nofile", "Soft": 1024, "Hard": 2048}"
     * @return A list of resource limits to set in the container
     */
    public List<Ulimit> getUlimits() {
        return ulimits;
    }

    /**
     * A list of resource limits to set in the container
     * @param ulimits A list of resource limits to set in the container
     */
    public void setUlimits(List<Ulimit> ulimits) {
        this.ulimits = ulimits;
    }

    /**
     * A list of kernel parameters (sysctls) to set in the container. For example: {"net.ipv4.ip_forward": "1"}
     * @return A list of kernel parameters (sysctls) to set in the container
     */
    public Map<String, String> getSysctls() {
        return sysctls;
    }

    /**
     * A list of kernel parameters (sysctls) to set in the container. For example: {"net.ipv4.ip_forward": "1"}
     * @param sysctls A list of kernel parameters (sysctls) to set in the container
     */
    public void setSysctls(Map<String, String> sysctls) {
        this.sysctls = sysctls;
    }

}
