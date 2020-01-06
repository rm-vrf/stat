package cn.batchfile.stat.server.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.batchfile.stat.server.dto.ContainerTable;

public interface ContainerRepository extends CrudRepository<ContainerTable, String> {
	
	@Query("SELECT c FROM Container c WHERE c.node=:nodeId")
	Iterable<ContainerTable> fineByNodeId(@Param("nodeId") String nodeId);

}
