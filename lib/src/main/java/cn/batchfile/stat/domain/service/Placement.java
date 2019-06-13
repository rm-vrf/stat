package cn.batchfile.stat.domain.service;

import java.util.List;

public class Placement {
    private List<String> constraints;
    private List<String> preferences;

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
}
