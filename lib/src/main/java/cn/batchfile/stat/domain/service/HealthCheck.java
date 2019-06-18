package cn.batchfile.stat.domain.service;

public class HealthCheck {
    private boolean enabled;
    private Command command;
    private HttpGet httpGet;
    private int interval;
    private int retries;
    private int startPeriod;
    private int timeout;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public HttpGet getHttpGet() {
        return httpGet;
    }

    public void setHttpGet(HttpGet httpGet) {
        this.httpGet = httpGet;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(int startPeriod) {
        this.startPeriod = startPeriod;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
