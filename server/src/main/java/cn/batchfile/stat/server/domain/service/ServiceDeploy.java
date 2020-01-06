package cn.batchfile.stat.server.domain.service;

/**
 * 服务部署定义
 */
public class ServiceDeploy {
    public static final String MODE_REPLICARED = "replicated";
    public static final String MODE_GLOBAL = "global";

    private String mode = MODE_REPLICARED;
    private Integer replicas;
    private Placement placement;
    private Resources resources;

    /**
     * 部署模式
     * @return 部署模式
     */
    public String getMode() {
        return mode;
    }

    /**
     * 部署模式
     * @param mode 部署模式
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 副本数
     * @return 副本数
     */
    public Integer getReplicas() {
        return replicas;
    }

    /**
     * 副本数
     * @param replicas 副本数
     */
    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    /**
     * 部署位置
     * @return 部署位置
     */
    public Placement getPlacement() {
        return placement;
    }

    /**
     * 部署位置
     * @param placement 部署位置
     */
    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    /**
     * 资源
     * @return 资源
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * 资源
     * @param resources 资源
     */
    public void setResources(Resources resources) {
        this.resources = resources;
    }

}
