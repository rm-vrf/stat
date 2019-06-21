package cn.batchfile.stat.server.service;

import cn.batchfile.stat.domain.container.ContainerInstance;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@org.springframework.stereotype.Service
public class ContainerService {
    private static final Logger LOG = LoggerFactory.getLogger(ContainerService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ContainerInstance> getContainersByNode(String node) {
        String sql = "SELECT `id`,`node`,`service`,`valid`,`pid`,`status`,`bundle`,`owner`," +
                "`create_time`,`update_time` FROM `container` " +
                "WHERE `node`=? ORDER BY `create_time`";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, node);
        List<ContainerInstance> containers = new ArrayList<>();
        for (Map<String, Object> map : list) {
            containers.add(composeContainer(map));
        }
        return containers;
    }

    public List<ContainerInstance> getContainersByService(String service) {
        String sql = "SELECT `id`,`node`,`service`,`valid`,`pid`,`status`,`bundle`,`owner`," +
                "`create_time`,`update_time` FROM `container` " +
                "WHERE `service`=? ORDER BY `create_time`";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, service);
        List<ContainerInstance> containers = new ArrayList<>();
        for (Map<String, Object> map : list) {
            containers.add(composeContainer(map));
        }
        return containers;
    }

    public ContainerInstance getContainer(String id) {
        String sql = "SELECT `id`,`node`,`service`,`valid`,`pid`,`status`,`bundle`,`owner`," +
                "`create_time`,`update_time` FROM `container` WHERE `id`=? LIMIT 1";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
        if (list.size() == 0) {
            return null;
        } else {
            return composeContainer(list.get(0));
        }
    }

    public String putContainer(ContainerInstance container) {
        if (StringUtils.isEmpty(container.getId())) {
            container.setId(StringUtils.remove(UUID.randomUUID().toString(), '-'));
        }

        String id = container.getId();
        String node = container.getNode();
        String service = container.getService();
        Boolean valid = container.getValid();
        Long pid = container.getPid();
        String status = container.getStatus();
        String bundle = container.getBundle();
        String owner = container.getOwner();
        long createTime = new Date().getTime();
        long updateTime = createTime;

        String sql = "REPLACE INTO `container` " +
                "(`id`,`node`,`service`,`valid`,`pid`,`status`,`bundle`,`owner`,`create_time`,`update_time`) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        int count = jdbcTemplate.update(sql,
                id, node, service, (valid != null && valid.booleanValue() ? 1 : 0),
                pid, status, bundle, owner, createTime, updateTime);
        LOG.info("put container, id: {}, service: {}, node: {}, count: {}", id, service, node, count);

        return id;
    }

    public String patchContainer(ContainerInstance container) {
        String id = container.getId();
        String node = container.getNode();
        String service = container.getService();
        Boolean valid = container.getValid();
        Long pid = container.getPid();
        String status = container.getStatus();
        String bundle = container.getBundle();
        String owner = container.getOwner();
        long updateTime = new Date().getTime();

        List<Object> args = new ArrayList<>();
        String sql = "UPDATE `container` SET ";
        if (node != null) {
            args.add(node);
            sql += "`node`=?,";
        }
        if (service != null) {
            args.add(service);
            sql += "`service`=?,";
        }
        if (valid != null) {
            args.add(valid != null && valid.booleanValue() ? 1 : 0);
            sql += "`valid`=?,";
        }
        if (pid != null) {
            args.add(pid);
            sql += "`pid`=?,";
        }
        if (status != null) {
            args.add(status);
            sql += "`status`=?,";
        }
        if (bundle != null) {
            args.add(bundle);
            sql += "`bundle`=?,";
        }
        if (owner != null) {
            args.add(owner);
            sql += "`owner`=?,";
        }
        if (args.size() == 0) {
            LOG.info("nothing change");
            return null;
        }

        args.add(updateTime);
        args.add(id);
        sql += "`update_time`=? WHERE `id`=?";

        int count = jdbcTemplate.update(sql, args.toArray());
        if (count > 0) {
            return id;
        } else {
            return null;
        }
    }


    public String getConfig(String id) {
        String sql = "SELECT `config` FROM `container` WHERE `id`=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
        if (list.size() == 0) {
            return null;
        } else {
            Object config = list.get(0).get("config");
            return config == null ? StringUtils.EMPTY : config.toString();
        }
    }

    public String putConfig(String id, String config) {
        String sql = "UPDATE `container` SET `config`=? WHERE `id`=?";
        int count = jdbcTemplate.update(sql, config, id);
        LOG.info("update container config, id: {}, count: {}", id, count);
        if (count > 0) {
            return id;
        } else {
            return null;
        }
    }

    public String deleteContainer(String id) {
        String sql = "DELETE FROM `container` WHERE `id`=?";
        int count = jdbcTemplate.update(sql, id);
        LOG.info("delete container, id: {}, count: {}", id, count);
        if (count > 0) {
            return id;
        } else {
            return null;
        }
    }

    private ContainerInstance composeContainer(Map<String, Object> map) {
        Object id = map.get("id");
        Object node = map.get("node");
        Object service = map.get("service");
        Object valid = map.get("valid");
        Object pid = map.get("pid");
        Object status = map.get("status");
        Object bundle = map.get("bundle");
        Object owner = map.get("owner");
        Object createTime = map.get("create_time");
        Object updateTime = map.get("update_time");

        ContainerInstance container = new ContainerInstance();
        container.setId(id == null ? null : id.toString());
        container.setNode(node == null ? null : node.toString());
        container.setService(service == null ? null : service.toString());
        container.setValid(valid != null && valid.toString().equals("1") ? true : false);
        container.setPid(pid == null ? 0 : Long.valueOf(pid.toString()));
        container.setStatus(status == null ? null : status.toString());
        container.setBundle(bundle == null ? null : bundle.toString());
        container.setOwner(owner == null ? null : owner.toString());
        container.setCreateTime(createTime == null ? null : new Date(Long.valueOf(createTime.toString())));
        container.setUpdateTime(updateTime == null ? null : new Date(Long.valueOf(updateTime.toString())));
        return container;
    }
}
