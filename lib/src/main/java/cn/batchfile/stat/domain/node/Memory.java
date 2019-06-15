package cn.batchfile.stat.domain.node;

/**
 * 内存
 */
public class Memory {
    private long total;
    private long ram;

    /**
     * 总内存
     * @return 总内存
     */
    public long getTotal() {
        return total;
    }

    /**
     * 总内存
     * @param total 总内存
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * RAM
     * @return RAM
     */
    public long getRam() {
        return ram;
    }

    /**
     * RAM
     * @param ram RAM
     */
    public void setRam(long ram) {
        this.ram = ram;
    }
}
