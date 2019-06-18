package cn.batchfile.stat.domain.service;

import java.util.List;

public class Volume {
    private String namespace;
    private String name;
    private String type;
    private String source;
    private List<String> options;
    private VolumeDeploy deploy;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public VolumeDeploy getDeploy() {
        return deploy;
    }

    public void setDeploy(VolumeDeploy deploy) {
        this.deploy = deploy;
    }
}
