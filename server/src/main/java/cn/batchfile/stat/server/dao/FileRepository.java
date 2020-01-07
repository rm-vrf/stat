package cn.batchfile.stat.server.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.batchfile.stat.server.dto.FileTable;

public interface FileRepository extends CrudRepository<FileTable, String> {
	
	@Query("SELECT f FROM File f WHERE f.namespace=:namespace AND f.type='ns'")
	Optional<FileTable> findOne(@Param("namespace") String namespace);
	
	@Query("SELECT f FROM File f WHERE f.namespace=:namespace AND f.name=:name")
	Optional<FileTable> findOne(@Param("namespace") String namespace, @Param("name") String name);
	
	@Query("SELECT f FROM File f WHERE f.type='ns' ORDER BY f.namespace")
	Iterable<FileTable> findMany();
	
	@Modifying
	@Query("DELETE FROM File f WHERE f.namespace=:namespace")
	void deleteMany(@Param("namespace") String namespace);

	@Modifying
	@Query("DELETE FROM File f WHERE f.namespace=:namespace AND f.name=:name")
	void deleteOne(@Param("namespace") String namespace, @Param("name") String name);
	
	@Query("SELECT f FROM File f WHERE f.parent=:parentId ORDER BY f.name")
	Iterable<FileTable> findMany(@Param("parentId") String parentId);
}
