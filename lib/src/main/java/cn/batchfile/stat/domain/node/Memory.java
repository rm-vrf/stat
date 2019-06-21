package cn.batchfile.stat.domain.node;

/**
 * 内存
 */
public class Memory {
    private Long total;
    private Long ram;

    /**
     * 总内存
     * @return 总内存
     */
    public Long getTotal() {
        return total;
    }

    /**
     * 总内存
     * @param total 总内存
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * RAM
     * @return RAM
     */
    public Long getRam() {
        return ram;
    }

    /**
     * RAM
     * @param ram RAM
     */
    public void setRam(Long ram) {
        this.ram = ram;
    }
}
