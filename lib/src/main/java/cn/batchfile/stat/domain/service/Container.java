package cn.batchfile.stat.domain.service;

import java.util.List;
import java.util.Map;

public class Container {
    private String image;
    private Boolean readonly;
    private List<Add> adds;
    private List<List<String>> runs;
    private User user;
    private String workDirectory;
    private List<String> command;
    private Integer stopSignal;
    private Integer stopGracePeriod;
    private String hostname;
    private Map<String, String> environments;
    private List<Mount> mounts;
    private List<Port> ports;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public List<Add> getAdds() {
        return adds;
    }

    public void setAdds(List<Add> adds) {
        this.adds = adds;
    }

    public List<List<String>> getRuns() {
        return runs;
    }

    public void setRuns(List<List<String>> runs) {
        this.runs = runs;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getWorkDirectory() {
        return workDirectory;
    }

    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public Integer getStopSignal() {
        return stopSignal;
    }

    public void setStopSignal(Integer stopSignal) {
        this.stopSignal = stopSignal;
    }

    public Integer getStopGracePeriod() {
        return stopGracePeriod;
    }

    public void setStopGracePeriod(Integer stopGracePeriod) {
        this.stopGracePeriod = stopGracePeriod;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Map<String, String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Map<String, String> environments) {
        this.environments = environments;
    }

    public List<Mount> getMounts() {
        return mounts;
    }

    public void setMounts(List<Mount> mounts) {
        this.mounts = mounts;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }
}
