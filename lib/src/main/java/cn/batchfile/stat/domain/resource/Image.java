package cn.batchfile.stat.domain.resource;

import java.util.Date;

/**
 * 镜像
 */
public class Image {

    private String name;
    private String tag;
    private String os;
    private String architecture;
    private Long size;
    private Date timestamp;

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
     * 标签
     * @return 标签
     */
    public String getTag() {
        return tag;
    }

    /**
     * 标签
     * @param tag 标签
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 操作系统
     * @return 操作系统
     */
    public String getOs() {
        return os;
    }

    /**
     * 操作系统
     * @param os 操作系统
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * 架构
     * @return 架构
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * 架构
     * @param architecture 架构
     */
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * 体积
     * @return 体积
     */
    public Long getSize() {
        return size;
    }

    /**
     * 体积
     * @param size 体积
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * 时间戳
     * @return 时间戳
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 时间戳
     * @param timestamp 时间戳
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
