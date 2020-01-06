package cn.batchfile.stat.server.domain.service;

/**
 * 资源控制
 */
public class ResourcesControl {
    private Float cpus;
    private String memory;

    /**
     * CPU 线程数
     * @return CPU 线程数
     */
    public Float getCpus() {
        return cpus;
    }

    /**
     * CPU 线程数
     * @param cpus CPU 线程数
     */
    public void setCpus(Float cpus) {
        this.cpus = cpus;
    }

    /**
     * 内存
     * @return 内存
     */
    public String getMemory() {
        return memory;
    }

    /**
     * 内存
     * @param memory 内存
     */
    public void setMemory(String memory) {
        this.memory = memory;
    }

}
