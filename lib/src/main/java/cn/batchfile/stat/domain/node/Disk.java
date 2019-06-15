package cn.batchfile.stat.domain.node;

/**
 * 磁盘
 */
public class Disk {
    private String dirName;
    private long total;

    /**
     * 路径
     * @return 路径
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * 路径
     * @param dirName 路径
     */
    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * 总容量
     * @return 总容量
     */
    public long getTotal() {
        return total;
    }

    /**
     * 总容量
     * @param total 总容量
     */
    public void setTotal(long total) {
        this.total = total;
    }
}
