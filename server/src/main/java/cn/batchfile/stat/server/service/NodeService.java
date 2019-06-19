package cn.batchfile.stat.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.batchfile.stat.domain.node.*;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.batchfile.stat.domain.PaginationList;

@Service
public class NodeService {
	private static final Logger LOG = LoggerFactory.getLogger(NodeService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate = null;

	public String putNode(Node node) {
        String id = node.getId();
        String hostname = node.getHostname();
        String address = node.getAddress();
        String os = JSON.toJSONString(node.getOs());
        String memory = JSON.toJSONString(node.getMemory());
        String networks = JSON.toJSONString(node.getNetworks());
        String disks = JSON.toJSONString(node.getDisks());
        String tags = JSON.toJSONString(node.getTags());
        long timestamp = System.currentTimeMillis();
        String status = node.getStatus();

        String sql = "REPLACE INTO `node` " +
                "(`id`,`hostname`,`address`,`os`,`memory`,`networks`,`disks`,`tags`,`timestamp`,`status`) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        int count = jdbcTemplate.update(sql,
                id, hostname, address, os, memory, networks, disks, tags, timestamp, status);
        LOG.info("put node, id: {}, address: {}, count: {}", id, address, count);

        return id;
	}

	public List<Node> getNodes() {
        String sql = "SELECT * FROM `node` ORDER BY `address`";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        List<Node> nodes = new ArrayList<>();
        for (Map<String, Object> map : list) {
            nodes.add(composeNode(map));
        }
        return nodes;
    }

	public Node getNode(String id) {
	    String sql = "SELECT * FROM `node` WHERE `id`=? LIMIT 1";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, id);
        if (list.size() == 0) {
            return null;
        } else {
            return composeNode(list.get(0));
        }
	}

	public String deleteNode(String id) {
        String sql = "DELETE FROM `node` WHERE `id`=?";
        int count = jdbcTemplate.update(sql, id);
        LOG.info("delete node, id: {}, count: {}", id, count);
        if (count > 0) {
            return id;
        } else {
            return null;
        }
    }

	public List<String> putTags(String id, List<String> tags) {
        String json = JSON.toJSONString(tags);
        String sql = "UPDATE `node` SET `tags`=? WHERE `id`=?";
        int count = jdbcTemplate.update(sql, json, id);
        LOG.info("put tags, id: {}, tags: {}, count: {}", id, json, count);
        return tags;
	}

	public PaginationList<Node> searchNodes(String query, int from, int size) {
	    return searchNodes(query, true, from, size);
	}

    public PaginationList<Node> searchNodes(String query, boolean upOnly, int from, int size) {
	    return new PaginationList<>();
    }

    private Node composeNode(Map<String, Object> map) {
        Object id = map.get("id");
        Object hostname = map.get("hostname");
        Object address = map.get("address");
        Object os = map.get("os");
        Object memory = map.get("memory");
        Object networks = map.get("networks");
        Object disks = map.get("disks");
        Object tags = map.get("tags");
        Object timestamp = map.get("timestamp");
        Object status = map.get("status");

        Node node = new Node();
        node.setId(id == null ? null : id.toString());
        node.setHostname(hostname == null ? null : hostname.toString());
        node.setAddress(address == null ? null : address.toString());
        node.setOs(os == null ? null : JSON.parseObject(os.toString(), Os.class));
        node.setMemory(memory == null ? null : JSON.parseObject(memory.toString(), Memory.class));
        node.setNetworks(networks == null ? null : JSON.parseArray(networks.toString(), Network.class));
        node.setDisks(disks == null ? null : JSON.parseArray(disks.toString(), Disk.class));
        node.setTags(tags == null ? null : JSON.parseArray(tags.toString(), String.class));
        node.setTimestamp(timestamp == null ? null : new Date(Long.valueOf(timestamp.toString())));
        node.setStatus(status == null ? null : status.toString());

        return node;
    }
}
