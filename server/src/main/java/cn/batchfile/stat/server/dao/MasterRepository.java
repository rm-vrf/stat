package cn.batchfile.stat.server.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import cn.batchfile.stat.server.dto.MasterTable;

public interface MasterRepository extends CrudRepository<MasterTable, String> {

	@Query("SELECT m FROM Master m ORDER BY m.startTime, m.id")
	Iterable<MasterTable> findMany();
	
//	@Modifying
//	@Query("UPDATE Master m SET pingTime=current_date() WHERE m.serviceId=:serviceId")
//	void updatePingTime(@Param("serviceId") String serviceId);
//	
//	@Modifying
//	@Query(value = "INSERT INTO master (service_id, ip, hostname, start_time, ping_time) VALUES (?1, ?2, ?3, sysdate(), sysdate())", nativeQuery = true)
//	void insertOne(@Param("serviceId") String serviceId, @Param("ip") String ip, @Param("hostname") String hostname);
	
}
