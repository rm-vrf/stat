package cn.batchfile.stat.domain.service;

/**
 * 资源
 */
public class Resources {

    private ResourcesControl limits;
    private ResourcesControl requests;

    /**
     * 限制
     * @return 限制
     */
    public ResourcesControl getLimits() {
        return limits;
    }

    /**
     * 限制
     * @param limits 限制
     */
    public void setLimits(ResourcesControl limits) {
        this.limits = limits;
    }

    /**
     * 申请
     * @return 申请
     */
    public ResourcesControl getRequests() {
        return requests;
    }

    /**
     * 申请
     * @param requests 申请
     */
    public void setRequests(ResourcesControl requests) {
        this.requests = requests;
    }

}
