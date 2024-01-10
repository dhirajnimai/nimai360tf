package com.nimai.lc.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.lc.entity.AdditionalUserList;




@Repository
@Transactional
public interface AdditionalOfflineUser extends JpaRepository<AdditionalUserList, Integer>
{

	@Query(value = "SELECT * FROM nimai_offline_users WHERE parent_userid=(:userId)", nativeQuery = true)
	List<AdditionalUserList> getListOfOfflineUsers(String userId);
	
	
	
	
	
	
	@Modifying
	@Query(value= "delete from nimai_offline_users where userid=:userId", nativeQuery = true)
	void deleteAdditionalUser(String userId);
}
