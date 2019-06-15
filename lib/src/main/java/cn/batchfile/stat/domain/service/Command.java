package cn.batchfile.stat.domain.service;

/**
 * 健康检查命令
 */
public class Command {
    private String test;

    /**
     * 测试命令
     * @return 测试命令
     */
    public String getTest() {
        return test;
    }

    /**
     * 测试命令
     * @param test 测试命令
     */
    public void setTest(String test) {
        this.test = test;
    }
}
