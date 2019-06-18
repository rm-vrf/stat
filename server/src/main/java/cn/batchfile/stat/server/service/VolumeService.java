package cn.batchfile.stat.server.service;

import cn.batchfile.stat.domain.Name;
import cn.batchfile.stat.domain.service.Volume;
import cn.batchfile.stat.domain.service.VolumeDeploy;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class VolumeService {
    private static final Logger LOG = LoggerFactory.getLogger(VolumeService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Volume> getVolumes() {
        return getVolumes(StringUtils.EMPTY);
    }

    public List<Volume> getVolumes(String namespace) {
        List<Map<String, Object>> list = null;
        if (StringUtils.isEmpty(namespace)) {
            String sql = "SELECT * FROM `volume` ORDER BY `namespace`, `name`";
            list = jdbcTemplate.queryForList(sql);
        } else {
            String sql = "SELECT * FROM `volume` WHERE `namespace`=? ORDER BY `namespace`, `name`";
            list = jdbcTemplate.queryForList(sql, namespace);
        }

        List<Volume> ret = new ArrayList<>();
        for (Map<String, Object> map : list) {
            ret.add(composeVolume(map));
        }

        return ret;
    }

    public Volume getVolume(String namespace, String name) {
        String sql = "SELECT * FROM `volume` WHERE `namespace`=? AND `name`=? LIMIT 1";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, namespace, name);
        if (list.size() == 0) {
            return null;
        } else {
            return composeVolume(list.get(0));
        }
    }

    public String putVolume(Volume volume) {
        if (StringUtils.isEmpty(volume.getNamespace())) {
            volume.setNamespace(Name.DEFAULT_NAMESPACE);
        }

        String namespace = volume.getNamespace();
        String name = volume.getName();
        String type = volume.getType();
        String source = volume.getSource();
        String options = JSON.toJSONString(volume.getOptions());
        String deploy = JSON.toJSONString(volume.getDeploy());

        String sql = "REPLACE INTO `volume` " +
                "(`namespace`,`name`,`type`,`source`,`options`,`deploy`) " +
                "VALUES (?,?,?,?,?,?)";
        int count = jdbcTemplate.update(sql,
                namespace, name, type, source, options, deploy);
        LOG.info("put volume: {}/{}, count: {}", namespace, name, count);

        return namespace + "/" + name;
    }

    public List<String> deleteVolumes(String namespace) {
        List<Volume> volumes = getVolumes(namespace);
        List<String> ret = new ArrayList<>();
        for (Volume volume : volumes) {
            String s = deleteVolume(volume.getNamespace(), volume.getName());
            if (StringUtils.isNotEmpty(s)) {
                LOG.debug("delete volume: {}", s);
                ret.add(s);
            }
        }
        return ret;
    }

    public String deleteVolume(String namespace, String name) {
        String sql = "DELETE FROM `volume` WHERE `namespace`=? AND `name`=?";
        int count = jdbcTemplate.update(sql, namespace, name);
        LOG.info("delete volume: {}/{}, count: {}", namespace, name, count);
        if (count == 0) {
            return null;
        } else {
            return namespace + "/" + name;
        }
    }

    private Volume composeVolume(Map<String, Object> map) {
        Object namespace = map.get("namespace");
        Object name = map.get("name");
        Object type = map.get("type");
        Object source = map.get("source");
        Object options = map.get("options");
        Object deploy = map.get("deploy");

        Volume volume = new Volume();
        volume.setNamespace(namespace == null ? null : namespace.toString());
        volume.setName(name == null ? null : name.toString());
        volume.setType(type == null ? null : type.toString());
        volume.setSource(source == null ? null : source.toString());
        volume.setOptions(options == null ? null : JSON.parseArray(options.toString(), String.class));
        volume.setDeploy(deploy == null ? null : JSON.parseObject(deploy.toString(), VolumeDeploy.class));

        return volume;
    }
}
