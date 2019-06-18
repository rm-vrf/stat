package cn.batchfile.stat.server.service;

import cn.batchfile.stat.domain.Name;
import cn.batchfile.stat.domain.service.*;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Service> getServices() {
        return getServices(StringUtils.EMPTY);
    }

    public List<Service> getServices(String namespace) {
        List<Map<String, Object>> list = null;
        if (StringUtils.isEmpty(namespace)) {
            String sql = "SELECT * FROM `service` ORDER BY `namespace`, `name`";
            list = jdbcTemplate.queryForList(sql);
        } else {
            String sql = "SELECT * FROM `service` WHERE `namespace`=? ORDER BY `namespace`, `name`";
            list = jdbcTemplate.queryForList(sql, namespace);
        }

        List<Service> ret = new ArrayList<>();
        for (Map<String, Object> map : list) {
            ret.add(compsoseService(map));
        }

        return ret;
    }

    public Service getService(String namespace, String name) {
        String sql = "SELECT * FROM `service` WHERE `namespace`=? AND `name`=? LIMIT 1";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, namespace, name);
        if (list.size() == 0) {
            return null;
        } else {
            return compsoseService(list.get(0));
        }
    }

    public String putService(Service service) {
        if (StringUtils.isBlank(service.getNamespace())) {
            service.setNamespace(Name.DEFAULT_NAMESPACE);
        }

        String namespace = service.getNamespace();
        String name = service.getName();
        int stateful = service.isStateful() ? 1 : 0;
        String domainName = service.getDomainName();
        String container = JSON.toJSONString(service.getContainer());
        String deploy = JSON.toJSONString(service.getDeploy());
        String healthCheck = JSON.toJSONString(service.getHealthCheck());
        String logging = JSON.toJSONString(service.getLogging());
        String dependsOn = JSON.toJSONString(service.getDependsOn());

        String sql = "REPLACE INTO `service` " +
                "(`namespace`,`name`,`stateful`,`domain_name`,`container`," +
                "`deploy`,`health_check`,`logging`,`depends_on`) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        int count = jdbcTemplate.update(sql,
                namespace, name, stateful, domainName, container, deploy, healthCheck, logging, dependsOn);
        LOG.info("put service: {}/{}, count: {}", namespace, name, count);

        return namespace + "/" + name;
    }

    public List<String> deleteServices(String namespace) {
        List<Service> services = getServices(namespace);
        List<String> ret = new ArrayList<>();
        for (Service service : services) {
            String s = deleteService(service.getNamespace(), service.getName());
            if (StringUtils.isNotEmpty(s)) {
                LOG.debug("delete service: {}", s);
                ret.add(s);
            }
        }
        return ret;
    }

    public String deleteService(String namespace, String name) {
        String sql = "DELETE FROM `service` WHERE `namespace`=? AND `name`=?";
        int count = jdbcTemplate.update(sql, namespace, name);
        LOG.info("delete service: {}/{}, count: {}", namespace, name, count);
        if (count == 0) {
            return null;
        } else {
            return namespace + "/" + name;
        }
    }

    private Service compsoseService(Map<String, Object> map) {
        Object namespace = map.get("namespace");
        Object name = map.get("name");
        Object stateful = map.get("stateful");
        Object domainName = map.get("domain_name");
        Object container = map.get("container");
        Object deploy = map.get("deploy");
        Object healthCheck = map.get("health_check");
        Object logging = map.get("logging");
        Object dependsOn = map.get("depends_on");

        Service service = new Service();
        service.setNamespace(namespace == null ? null : namespace.toString());
        service.setName(name == null ? null : name.toString());
        service.setStateful(stateful == null ? false : stateful.toString().equals("1"));
        service.setDomainName(domainName == null ? null : domainName.toString());
        service.setContainer(container == null ? null : JSON.parseObject(container.toString(), Container.class));
        service.setDeploy(deploy == null ? null : JSON.parseObject(deploy.toString(), ServiceDeploy.class));
        service.setHealthCheck(healthCheck == null ? null : JSON.parseObject(healthCheck.toString(), HealthCheck.class));
        service.setLogging(logging == null ? null : JSON.parseObject(logging.toString(), Logging.class));
        service.setDependsOn(dependsOn == null ? null : JSON.parseArray(dependsOn.toString(), String.class));

        return service;
    }
}
