package cn.batchfile.stat.server.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 本地文件存储操作
 * @author Administrator
 *
 */
public interface FileDao {

	/**
	 * 设置存储根目录
	 * @param root 根目录
	 * @throws IOException 异常
	 */
    void setRoot(String root) throws IOException;
    
    /**
     * 创建目录
     * @param dir 目录
     * @return 成功
     * @throws IOException 异常
     */
    boolean mkdir(String dir) throws IOException;
    
    /**
     * List directory
     * @param dir directory
     * @return list of string, ["d	size	timestamp	name"]
     */
    List<String> ls(String dir);

    /**
     * 读文件内容
     * @param file 文件
     * @return 文件流
     * @throws IOException 异常
     */
    InputStream read(String file) throws IOException;

    /**
     * 读文件信息
     * @param file 文件
     * @return 信息
     */
    String get(String file);

    /**
     * 移动文件
     * @param source 源
     * @param target 目标
     * @return 成功
     * @throws IOException 异常
     */
    boolean mv(String source, String target) throws IOException;

    /**
     * 写文件内容
     * @param file 文件
     * @param inputStream 流
     * @return 成功
     * @throws IOException 异常
     */
    boolean write(String file, InputStream inputStream) throws IOException;

    /**
     * 删除文件
     * @param file 文件
     * @return 成功
     */
    boolean rm(String file);

}
