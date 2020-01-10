package cn.batchfile.stat.server.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cn.batchfile.stat.server.dto.ContainerTable;

public interface ContainerRepository extends JpaRepository<ContainerTable, String> {
	
	@Query("SELECT c FROM Container c WHERE c.node=:nodeId")
	Page<ContainerTable> findMany(@Param("nodeId") String nodeId, Pageable pageable);
	
	@Query("SELECT c FROM Container c WHERE c.namespace=:namespace AND c.service=:serviceName")
	Page<ContainerTable> findMany(@Param("namespace") String namespace, 
			@Param("serviceName") String serviceName, 
			Pageable pageable);
	
	@Modifying
	@Query("UPDATE Container c SET c.state=:state WHERE c.node=:node")
	void updateStatus(@Param("node") String node, @Param("state") String state);
	
	@Modifying
	@Query("DELETE Container c WHERE c.node=:node")
	void deleteMany(@Param("node") String node);
}
