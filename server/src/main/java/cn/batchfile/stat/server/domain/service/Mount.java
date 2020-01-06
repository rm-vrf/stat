package cn.batchfile.stat.server.domain.service;

import java.util.List;

/**
 * 挂载设置
 */
public class Mount {
    private String source;
    private String target;
    private String type;
    private Boolean readOnly;
    private List<String> options;

    /**
     * Mount source (e.g. a volume name, a host path).
     * @return Mount source (e.g. a volume name, a host path).
     */
    public String getSource() {
        return source;
    }

    /**
     * Mount source (e.g. a volume name, a host path).
     * @param source Mount source (e.g. a volume name, a host path).
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Container path.
     * @return Container path.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Container path.
     * @param target Container path.
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * The mount type. Available types: "bind" "volume" "tmpfs" "npipe"
     * @return The mount type. Available types: "bind" "volume" "tmpfs" "npipe"
     */
    public String getType() {
        return type;
    }

    /**
     * The mount type. Available types: "bind" "volume" "tmpfs" "npipe"
     * @param type The mount type. Available types: "bind" "volume" "tmpfs" "npipe"
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Whether the mount should be read-only.
     * @return Whether the mount should be read-only.
     */
    public Boolean getReadOnly() {
        return readOnly;
    }

    /**
     * Whether the mount should be read-only.
     * @param readOnly Whether the mount should be read-only.
     */
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Optional configuration
     * @return Optional configuration
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Optional configuration
     * @param options Optional configuration
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }

}
