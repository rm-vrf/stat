package cn.batchfile.stat.domain.service;

/**
 * 文件
 */
public class File {

    private String source;
    private String destination;
    private String option;

    /**
     * 文件，File 的名称，比如"packages/zipkin-server-2.9.16-exec.jar"
     * @return 文件，File 的名称，比如"packages/zipkin-server-2.9.16-exec.jar"
     */
    public String getSource() {
        return source;
    }

    /**
     * 文件，File 的名称，比如"packages/zipkin-server-2.9.16-exec.jar"
     * @param source 文件，File 的名称，比如"packages/zipkin-server-2.9.16-exec.jar"
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 容器路径
     * @return 容器路径
     */
    public String getDestination() {
        return destination;
    }

    /**
     * 容器路径
     * @param destination 容器路径
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * 选项："--chown=<user>:<group>"
     * @return 选项："--chown=<user>:<group>"
     */
    public String getOption() {
        return option;
    }

    /**
     * 选项："--chown=<user>:<group>"
     * @param option 选项："--chown=<user>:<group>"
     */
    public void setOption(String option) {
        this.option = option;
    }
}
