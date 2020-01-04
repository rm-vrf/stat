package cn.batchfile.stat.domain.service;

/**
 * 健康检查
 */
public class HealthCheck {

    public Boolean enabled;
    public String interval;
    public String timeout;
    public String startPeriod;
    public Integer retries;
    public Command command;
    public HttpGet httpGet;

    /**
     * 有效标志
     * @return 有效标志
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * 有效标志
     * @param enabled 有效标志
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 检查间隔时间
     * @return 检查间隔时间
     */
    public String getInterval() {
        return interval;
    }

    /**
     * 检查间隔时间
     * @param interval 检查间隔时间
     */
    public void setInterval(String interval) {
        this.interval = interval;
    }

    /**
     * 超时时间
     * @return 超时时间
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * 超时时间
     * @param timeout 超时时间
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * 启动时间
     * @return 启动时间
     */
    public String getStartPeriod() {
        return startPeriod;
    }

    /**
     * 启动时间
     * @param startPeriod 启动时间
     */
    public void setStartPeriod(String startPeriod) {
        this.startPeriod = startPeriod;
    }

    /**
     * 重试次数
     * @return 重试次数
     */
    public Integer getRetries() {
        return retries;
    }

    /**
     * 重试次数
     * @param retries 重试次数
     */
    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    /**
     * 测试命令
     * @return 测试命令
     */
    public Command getCommand() {
        return command;
    }

    /**
     * 测试命令
     * @param command 测试命令
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * HTTP 健康测试
     * @return HTTP 健康测试
     */
    public HttpGet getHttpGet() {
        return httpGet;
    }

    /**
     * HTTP 健康测试
     * @param httpGet HTTP 健康测试
     */
    public void setHttpGet(HttpGet httpGet) {
        this.httpGet = httpGet;
    }

}
