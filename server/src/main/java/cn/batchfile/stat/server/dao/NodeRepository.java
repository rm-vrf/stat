package cn.batchfile.stat.server.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cn.batchfile.stat.server.dto.NodeTable;

public interface NodeRepository extends JpaRepository<NodeTable, String> {

	@Query(value = "SELECT n FROM Node n")
	Page<NodeTable> findMany(Pageable pageable);
	
	@Query(value = "SELECT n FROM Node n WHERE n.status IN (:status)")
	Page<NodeTable> findMany(@Param("status") String[] status, Pageable pageable);
	
	@Query(value = "SELECT n FROM Node n WHERE (:name is null OR name LIKE :name) "
			+ "AND (:publicIp is null OR publicIp LIKE :publicIp) "
			+ "AND (:label is null OR labels LIKE CONCAT('%\"', :label, '\"%'))")
	Page<NodeTable> findMany(@Param("name") String name,
			@Param("publicIp") String publicIp, 
			@Param("label") String label, 
			Pageable pageable);
	
	@Query("SELECT n FROM Node n WHERE n.id=:id")
	Optional<NodeTable> findOne(@Param("id") String id);

}
