package cn.batchfile.stat.domain.service;

public class ServiceDeploy {
    public static final String MODE_REPLICARED = "replicated";
    public static final String MODE_GLOBAL = "global";

    private String mode;
    private int replicas;
    private Placement placement;
    private Resources resources;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }
}
