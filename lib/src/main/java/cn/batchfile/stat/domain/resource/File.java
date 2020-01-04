package cn.batchfile.stat.domain.resource;

import java.util.Date;

/**
 * 文件
 */
public class File {
    private Boolean directory;
    private String name;
    private Long size;
    private Date timestamp;

    /**
     * 是不是目录
     * @return 是不是目录
     */
    public Boolean isDirectory() {
        return directory;
    }

    /**
     * 是不是目录
     * @param directory 是不是目录
     */
    public void setDirectory(Boolean directory) {
        this.directory = directory;
    }

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
