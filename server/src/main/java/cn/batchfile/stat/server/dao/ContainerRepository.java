package cn.batchfile.stat.server.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.batchfile.stat.server.dto.ContainerTable;

public interface ContainerRepository extends CrudRepository<ContainerTable, String> {
	
	@Query("SELECT c FROM Container c WHERE c.node=:nodeId ORDER BY c.createTime")
	Iterable<ContainerTable> findMany(@Param("nodeId") String nodeId);
	
	@Query("SELECT c FROM Container c WHERE c.namespace=:namespace AND c.service=:serviceName ORDER BY c.createTime")
	Iterable<ContainerTable> findMany(@Param("namespace") String namespace, @Param("serviceName") String serviceName);
	
	@Modifying
	@Query("UPDATE Container c SET c.state=:state WHERE c.node=:node")
	void updateStatus(@Param("node") String node, @Param("state") String state);
}
