package cn.batchfile.stat.domain;

import org.apache.commons.lang.StringUtils;

public class Name {
    public static final String DEFAULT_NAMESPACE = "default";

    private String namespace;
    private String name;

    public Name(String namespace, String name) {
        this.namespace = StringUtils.isEmpty(namespace) ? DEFAULT_NAMESPACE : namespace;
        this.name = name;
    }

    public Name(String name) {
        if (StringUtils.contains(name, '/')) {
            String[] ary = StringUtils.split(name, '/');
            String namespace = ary[0];
            this.namespace = StringUtils.isEmpty(namespace) ? DEFAULT_NAMESPACE : namespace;
            this.name = ary[1];
        } else {
            this.namespace = DEFAULT_NAMESPACE;
            this.name = name;
        }
    }

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

    @Override
    public String toString() {
        return String.format("%s/%s", namespace, name);
    }
}
