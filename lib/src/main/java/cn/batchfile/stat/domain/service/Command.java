package cn.batchfile.stat.domain.service;

import java.util.List;

/**
 * 健康检查命令
 */
public class Command {
    private List<String> test;

    /**
     * 测试命令
     * @return 测试命令
     */
    public List<String> getTest() {
        return test;
    }

    /**
     * 测试命令
     * @param test 测试命令
     */
    public void setTest(List<String> test) {
        this.test = test;
    }

}
