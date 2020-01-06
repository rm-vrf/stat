package cn.batchfile.stat.server.domain.service;

/**
 * 部署位置
 */
public class Placement {
    private String constraint;
    private String preference;

    /**
     * 限制位置
     * @return 限制位置
     */
    public String getConstraint() {
        return constraint;
    }

    /**
     * 限制位置
     * @param constraint 限制位置
     */
    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    /**
     * 优先位置
     * @return 优先位置
     */
    public String getPreference() {
        return preference;
    }

    /**
     * 优先位置
     * @param preference 优先位置
     */
    public void setPreference(String preference) {
        this.preference = preference;
    }

}
