package cn.batchfile.stat.server.service;

import cn.batchfile.stat.domain.Name;
import cn.batchfile.stat.domain.service.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class FileService {
    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
    private java.io.File dataPath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${data.path}")
    public void setDataPath(String dataPath) throws IOException {
        this.dataPath = new java.io.File(dataPath);
        FileUtils.forceMkdir(this.dataPath);
    }

    public String postFile(String namespace, String name, String contentType, long size, InputStream inputStream) throws IOException {
        java.io.File path = new java.io.File(dataPath, namespace);
        FileUtils.forceMkdir(path);

        java.io.File file = new java.io.File(path, name);
        FileUtils.deleteQuietly(file);

        FileUtils.copyInputStreamToFile(inputStream, file);
        LOG.info("upload file: {}/{}, size: {}", namespace, name, size);

        File fileObject = new File();
        fileObject.setNamespace(namespace);
        fileObject.setName(name);
        fileObject.setSize(size);
        fileObject.setContentType(contentType);

        return putFile(fileObject);
    }

    public String putFile(File file) {
        if (StringUtils.isBlank(file.getNamespace())) {
            file.setNamespace(Name.DEFAULT_NAMESPACE);
        }

        String namespace = file.getNamespace();
        String name = file.getName();
        String url = file.getUrl();
        long size = file.getSize();
        String contentType = file.getContentType();
        long timestamp = System.currentTimeMillis();

        String sql = "REPLACE INTO `file` " +
                "(`namespace`,`name`,`url`,`size`,`content_type`,`timestamp`) " +
                "VALUES (?,?,?,?,?,?)";
        int count = jdbcTemplate.update(sql,
                namespace, name, url, size, contentType, timestamp);
        LOG.info("put file: {}/{}, count: {}", namespace, name, count);

        return namespace + "/" + name;
    }

    public List<String> deleteFiles(String namespace) {
        List<File> files = getFiles(namespace);
        List<String> ret = new ArrayList<>();
        for (File file : files) {
            String s = deleteFile(file.getNamespace(), file.getName());
            if (StringUtils.isNotEmpty(s)) {
                LOG.debug("delete file: {}", s);
                ret.add(s);
            }
        }

        java.io.File path = new java.io.File(dataPath, namespace);
        FileUtils.deleteQuietly(path);
        LOG.info("delete path: {}", path);

        return ret;
    }

    public String deleteFile(String namespace, String name) {
        String sql = "DELETE FROM `file` WHERE `namespace`=? AND `name`=?";
        int count = jdbcTemplate.update(sql, namespace, name);

        java.io.File path = new java.io.File(dataPath, namespace);
        java.io.File file = new java.io.File(path, name);
        FileUtils.deleteQuietly(file);

        LOG.info("delete file: {}/{}, count: {}", namespace, name, count);

        if (count == 0) {
            return null;
        } else {
            return namespace + "/" + name;
        }
    }

    public List<File> getFiles() {
        return getFiles(StringUtils.EMPTY);
    }

    public List<File> getFiles(String namespace) {
        List<Map<String, Object>> list = null;
        if (StringUtils.isEmpty(namespace)) {
            String sql = "SELECT * FROM `file` ORDER BY `namespace`, `name`";
            list = jdbcTemplate.queryForList(sql);
        } else {
            String sql = "SELECT * FROM `file` WHERE `namespace`=? ORDER BY `namespace`, `name`";
            list = jdbcTemplate.queryForList(sql, namespace);
        }

        List<File> ret = new ArrayList<>();
        for (Map<String, Object> map : list) {
            ret.add(composeInfo(map));
        }

        return ret;
    }

    public InputStream getStream(String namespace, String name) throws FileNotFoundException {
        java.io.File path = new java.io.File(dataPath, namespace);
        java.io.File file = new java.io.File(path, name);
        return new FileInputStream(file);
    }

    public File getInfo(String namespace, String name) {
        String sql = "SELECT * FROM `file` WHERE `namespace`=? AND `name`=? LIMIT 1";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, namespace, name);
        if (list.size() == 0) {
            return null;
        } else {
            return composeInfo(list.get(0));
        }
    }

    private File composeInfo(Map<String, Object> map) {
        Object namespace = map.get("namespace");
        Object name = map.get("name");
        Object url = map.get("url");
        Object size = map.get("size");
        Object contentType = map.get("content_type");
        Object timestamp = map.get("timestamp");

        File file = new File();
        file.setNamespace(namespace == null ? null : namespace.toString());
        file.setName(name == null ? null : name.toString());
        file.setUrl(url == null ? null : url.toString());
        file.setSize(size == null ? 0 : Long.valueOf(size.toString()));
        file.setContentType(contentType == null ? null : contentType.toString());
        file.setTimestamp(timestamp == null ? null : new Date(Long.valueOf(timestamp.toString())));

        return file;
    }
}
