package com.nimai.lc.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.nimai.lc.entity.NimaiOfflineUser;



@Repository
@Transactional
public interface NimaiOfflineDetailsRepository extends JpaRepository<NimaiOfflineUser, Integer> {

	@Query(value="SELECT * from nimai_offline_users nc where nc.email_address=:emailId",nativeQuery = true)
	NimaiOfflineUser existsByEmailId(@Param("emailId") String emailId);
	
	
	
	@Query(value="SELECT * from nimai_offline_users nc where nc.additional_user_id=:userId",nativeQuery = true)
	NimaiOfflineUser existsByUserId(@Param("userId") String userId);
	
}
