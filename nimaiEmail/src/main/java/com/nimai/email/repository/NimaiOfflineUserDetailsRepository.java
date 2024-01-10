package com.nimai.email.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nimai.email.entity.NimaiEncryptedDetails;
import com.nimai.email.entity.NimaiOfflineUserDetails;

@Repository
@Transactional
public interface NimaiOfflineUserDetailsRepository extends JpaRepository<NimaiOfflineUserDetails, String> {
	
	@Query(value="SELECT * \r\n"
			+ " FROM nimai_offline_users nc where nc.userid= :userid AND nc.additional_user_id  \r\n"
			+ " LIKE '%AD%'",nativeQuery = true)
	List<NimaiOfflineUserDetails> findByOfflineUserIdSearch(@Param("userid") String userid);
	
	
	@Query(value="SELECT * from nimai_offline_users nc where nc.email_address=:emailId",nativeQuery = true)
	NimaiOfflineUserDetails existsByEmailId(@Param("emailId") String emailId);
	
	
	
	@Query("FROM NimaiOfflineUserDetails nc where nc.additionalUserId= :userid")
	NimaiOfflineUserDetails findByOfflineUserId(@Param("userid") String userid);
}
