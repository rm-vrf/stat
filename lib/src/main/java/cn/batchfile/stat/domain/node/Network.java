package cn.batchfile.stat.domain.node;

/**
 * 网络
 */
public class Network {
    private String name;
    private String displayName;
    private String type;
    private String address;
    private String hostname;
    private boolean loopbackAddress;
    private boolean multicastAddress;
    private boolean siteLocalAddress;

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
     * 显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 显示名称
     * @param displayName 显示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 类型
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 类型
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 地址
     * @return 地址
     */
    public String getAddress() {
        return address;
    }

    /**
     * 地址
     * @param address 地址
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 机器名
     * @return 机器名
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * 机器名
     * @param hostname 机器名
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Utility routine to check if the InetAddress is a loopback address.
     * @return a {@code boolean} indicating if the InetAddress is
     * a loopback address; or false otherwise.
     */
    public boolean isLoopbackAddress() {
        return loopbackAddress;
    }

    /**
     * Utility routine to check if the InetAddress is a loopback address.
     * @param loopbackAddress a {@code boolean} indicating if the InetAddress is
     * a loopback address; or false otherwise.
     */
    public void setLoopbackAddress(boolean loopbackAddress) {
        this.loopbackAddress = loopbackAddress;
    }

    /**
     * Utility routine to check if the InetAddress is an IP multicast address.
     * @return a {@code boolean} indicating if the InetAddress is
     * an IP multicast address
     */
    public boolean isMulticastAddress() {
        return multicastAddress;
    }

    /**
     * Utility routine to check if the InetAddress is an IP multicast address.
     * @param multicastAddress a {@code boolean} indicating if the InetAddress is
     *                         an IP multicast address
     */
    public void setMulticastAddress(boolean multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    /**
     * Utility routine to check if the InetAddress is a site local address.
     * @return a {@code boolean} indicating if the InetAddress is
     * a site local address; or false if address is not a site local unicast address.
     */
    public boolean isSiteLocalAddress() {
        return siteLocalAddress;
    }

    /**
     * Utility routine to check if the InetAddress is a site local address.
     * @param siteLocalAddress a {@code boolean} indicating if the InetAddress is
     * a site local address; or false if address is not a site local unicast address.
     */
    public void setSiteLocalAddress(boolean siteLocalAddress) {
        this.siteLocalAddress = siteLocalAddress;
    }
}
