package cn.batchfile.stat.domain.node;

/**
 * 操作系统
 */
public class Os {
    private String name;
    private String version;
    private int cpus;
    private String architecture;

    /**
     * 名称
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 名称
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 版本
     * @return 版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 版本
     * @param version 版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * CPU 核数
     * @return CPU 核数
     */
    public int getCpus() {
        return cpus;
    }

    /**
     * CPU 核数
     * @param cpus CPU 核数
     */
    public void setCpus(int cpus) {
        this.cpus = cpus;
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
}
