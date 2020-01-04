package cn.batchfile.stat.domain.service;

import java.util.List;
import java.util.Map;

/**
 * 镜像构建参数
 */
public class Image {

    private String from;
    private List<List<String>> runs;
    private List<String> command;
    private Map<String, String> labels;
    private String expose;
    private Map<String, String> environments;
    private List<File> adds;
    private List<File> copies;
    private List<String> entrypoint;
    private List<String> volumes;
    private String user;
    private String workingDirectory;
    private String stopSignal;
    private HealthCheck healthCheck;

    /**
     * 基础镜像
     * @return 基础镜像
     */
    public String getFrom() {
        return from;
    }

    /**
     * 基础镜像
     * @param from 基础镜像
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * RUN ["/bin/bash", "-c", "echo hello"]
     * @return RUN ["/bin/bash", "-c", "echo hello"]
     */
    public List<List<String>> getRuns() {
        return runs;
    }

    /**
     * RUN ["/bin/bash", "-c", "echo hello"]
     * @param runs RUN ["/bin/bash", "-c", "echo hello"]
     */
    public void setRuns(List<List<String>> runs) {
        this.runs = runs;
    }

    /**
     * CMD ["executable","param1","param2"]
     * @return CMD ["executable","param1","param2"]
     */
    public List<String> getCommand() {
        return command;
    }

    /**
     * CMD ["executable","param1","param2"]
     * @param command CMD ["executable","param1","param2"]
     */
    public void setCommand(List<String> command) {
        this.command = command;
    }

    /**
     * adds metadata to an image
     * @return adds metadata to an image
     */
    public Map<String, String> getLabels() {
        return labels;
    }

    /**
     * adds metadata to an image
     * @param labels adds metadata to an image
     */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * EXPOSE 80/tcp
     * does not actually publish the port.
     * It functions as a type of documentation between the person who builds the image
     * and the person who runs the container, about which ports are intended to be published
     * @return EXPOSE 80/tcp
     */
    public String getExpose() {
        return expose;
    }

    /**
     * EXPOSE 80/tcp
     * does not actually publish the port.
     * It functions as a type of documentation between the person who builds the image
     * and the person who runs the container, about which ports are intended to be published
     * @param expose EXPOSE 80/tcp
     */
    public void setExpose(String expose) {
        this.expose = expose;
    }

    /**
     * sets the environment variable
     * @return sets the environment variable
     */
    public Map<String, String> getEnvironments() {
        return environments;
    }

    /**
     * sets the environment variable
     * @param environments sets the environment variable
     */
    public void setEnvironments(Map<String, String> environments) {
        this.environments = environments;
    }

    /**
     * copies new files or remote file URLs
     * @return copies new files or remote file URLs
     */
    public List<File> getAdds() {
        return adds;
    }

    /**
     * copies new files or remote file URLs
     * @param adds copies new files or remote file URLs
     */
    public void setAdds(List<File> adds) {
        this.adds = adds;
    }

    /**
     * copies new files
     * @return copies new files
     */
    public List<File> getCopies() {
        return copies;
    }

    /**
     * copies new files
     * @param copies copies new files
     */
    public void setCopies(List<File> copies) {
        this.copies = copies;
    }

    /**
     * ENTRYPOINT ["executable", "param1", "param2"]
     * @return ENTRYPOINT ["executable", "param1", "param2"]
     */
    public List<String> getEntrypoint() {
        return entrypoint;
    }

    /**
     * ENTRYPOINT ["executable", "param1", "param2"]
     * @param entrypoint ENTRYPOINT ["executable", "param1", "param2"]
     */
    public void setEntrypoint(List<String> entrypoint) {
        this.entrypoint = entrypoint;
    }

    /**
     * 数据卷
     * @return 数据卷
     */
    public List<String> getVolumes() {
        return volumes;
    }

    /**
     * 数据卷
     * @param volumes 数据卷
     */
    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    /**
     * user[:group]
     * @return user[:group]
     */
    public String getUser() {
        return user;
    }

    /**
     * user[:group]
     * @param user user[:group]
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 工作目录
     * @return 工作目录
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * 工作目录
     * @param workingDirectory 工作目录
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * 停止信号 SIGTERM
     * @return 停止信号 SIGTERM
     */
    public String getStopSignal() {
        return stopSignal;
    }

    /**
     * 停止信号 SIGTERM
     * @param stopSignal 停止信号 SIGTERM
     */
    public void setStopSignal(String stopSignal) {
        this.stopSignal = stopSignal;
    }

    /**
     * 健康检查
     * @return 健康检查
     */
    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    /**
     * 健康检查
     * @param healthCheck 健康检查
     */
    public void setHealthCheck(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

}
