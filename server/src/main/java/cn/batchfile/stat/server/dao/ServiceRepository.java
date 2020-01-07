package cn.batchfile.stat.server.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.batchfile.stat.server.dto.ServiceTable;

public interface ServiceRepository extends CrudRepository<ServiceTable, String> {

	@Query("SELECT s FROM Service s WHERE s.namespace=:namespace AND s.name=:serviceName")
	Optional<ServiceTable> findOne(@Param("namespace") String namespace, @Param("serviceName") String serviceName);

	@Query("SELECT s FROM Service s WHERE s.namespace=:namespace ORDER BY s.name")
	Iterable<ServiceTable> findMany(@Param("namespace") String namespace);
	
	@Modifying
	@Query("DELETE FROM Service s WHERE s.namespace=:namespace AND s.name=:serviceName")
	void deleteOne(@Param("namespace") String namespace, @Param("serviceName") String serviceName);
	
	@Modifying
	@Query("DELETE FROM Service s WHERE s.namespace=:namespace")
	void deleteMany(@Param("namespace") String namespace);
}
