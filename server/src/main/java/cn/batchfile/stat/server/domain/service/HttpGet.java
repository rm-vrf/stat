package cn.batchfile.stat.server.domain.service;

/**
 * HTTP 健康测试
 */
public class HttpGet {
    private Integer portIndex;
    private String protocol;
    private String uri;

    /**
     * 端口序号
     * @return 端口序号
     */
    public Integer getPortIndex() {
        return portIndex;
    }

    /**
     * 端口序号
     * @param portIndex 端口序号
     */
    public void setPortIndex(Integer portIndex) {
        this.portIndex = portIndex;
    }

    /**
     * 协议
     * @return 协议
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 协议
     * @param protocol 协议
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * 访问地址
     * @return 访问地址
     */
    public String getUri() {
        return uri;
    }

    /**
     * 访问地址
     * @param uri 访问地址
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

}
