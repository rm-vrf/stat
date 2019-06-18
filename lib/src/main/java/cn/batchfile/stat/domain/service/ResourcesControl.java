package cn.batchfile.stat.domain.service;

public class ResourcesControl {
    private float cpus;
    private String memory;
    private String storage;

    public float getCpus() {
        return cpus;
    }

    public void setCpus(float cpus) {
        this.cpus = cpus;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
