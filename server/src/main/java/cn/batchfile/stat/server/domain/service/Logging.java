package cn.batchfile.stat.server.domain.service;

import java.util.Map;

/**
 * The logging configuration for this container
 */
public class Logging {
    private String type;
    private Map<String, String> options;

    /**
     * Valid value: "json-file" "syslog" "journald" "gelf" "fluentd" "awslogs" "splunk" "etwlogs" "none"
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Valid value: "json-file" "syslog" "journald" "gelf" "fluentd" "awslogs" "splunk" "etwlogs" "none"
     * @param type type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * config
     * @return config
     */
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * config
     * @param options config
     */
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
