package cn.batchfile.stat.domain.service;

/**
 * 主机名配置
 */
public class Host {

    private String name;
    private String ip;

    /**
     * 名称
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 名称
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 地址
     * @return 地址
     */
    public String getIp() {
        return ip;
    }

    /**
     * 地址
     * @param ip 地址
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

}
