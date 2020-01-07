package cn.batchfile.stat.server.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.batchfile.stat.server.dto.NodeTable;

public interface NodeRepository extends CrudRepository<NodeTable, String> {

	@Query("SELECT n FROM Node n ORDER BY dockerHost")
	Iterable<NodeTable> findMany();
	
	@Query("SELECT n FROM Node n WHERE id=:id")
	Optional<NodeTable> findOne(@Param("id") String id);
}
