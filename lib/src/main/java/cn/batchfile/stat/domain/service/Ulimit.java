package cn.batchfile.stat.domain.service;

/**
 * A list of resource limits to set in the container.
 */
public class Ulimit {

    private String name;
    private Integer soft;
    private Integer hard;

    /**
     * Name of ulimit
     * @return Name of ulimit
     */
    public String getName() {
        return name;
    }

    /**
     * Name of ulimit
     * @param name Name of ulimit
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Soft limit
     * @return Soft limit
     */
    public Integer getSoft() {
        return soft;
    }

    /**
     * Soft limit
     * @param soft Soft limit
     */
    public void setSoft(Integer soft) {
        this.soft = soft;
    }

    /**
     * Hard limit
     * @return Hard limit
     */
    public Integer getHard() {
        return hard;
    }

    /**
     * Hard limit
     * @param hard Hard limit
     */
    public void setHard(Integer hard) {
        this.hard = hard;
    }

}
