package cn.batchfile.stat.domain.service;

public class Resources {
    private ResourcesControl limits;
    private ResourcesControl requests;

    public ResourcesControl getLimits() {
        return limits;
    }

    public void setLimits(ResourcesControl limits) {
        this.limits = limits;
    }

    public ResourcesControl getRequests() {
        return requests;
    }

    public void setRequests(ResourcesControl requests) {
        this.requests = requests;
    }
}
