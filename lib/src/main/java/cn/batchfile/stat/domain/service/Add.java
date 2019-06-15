package cn.batchfile.stat.domain.service;

/**
 * 向镜像中添加文件的描述对象。
 * 文件可以设置成一个引用，比如"default/zipkin-server-exec.jar"，"default"命名空间可以省略；
 * 也可以设置成一个地址。
 * 生产镜像时将文件复制到 path 位置。
 */
public class Add {
    private String file;
    private String url;
    private String path;

    /**
     * 文件，File 的名称，比如"default/zipkin-server-exec.jar"，"default"命名空间可以省略
     * @return 文件
     */
    public String getFile() {
        return file;
    }

    /**
     * 文件，File 的名称，比如"default/zipkin-server-exec.jar"，"default"命名空间可以省略
     * @param file 文件
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * 地址，支持 HTTP/HTTPS 协议
     * @return 地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 地址，支持 HTTP/HTTPS 协议
     * @param url 地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 容器路径
     * @return 容器路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 容器路径
     * @param path 容器路径
     */
    public void setPath(String path) {
        this.path = path;
    }
}
