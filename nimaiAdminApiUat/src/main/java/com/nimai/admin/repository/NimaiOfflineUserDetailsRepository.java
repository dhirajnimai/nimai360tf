package com.nimai.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.admin.model.BeneInterestedCountry;
import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMSubscriptionCountry;
import com.nimai.admin.model.NimaiOfflineUserDetails;

public interface NimaiOfflineUserDetailsRepository 
extends JpaRepository<NimaiOfflineUserDetails, Integer> 
{
	@Query("FROM NimaiOfflineUserDetails nc where nc.userid.userid= :userid")
	List<NimaiOfflineUserDetails> findByOfflineUserIdSearch(@Param("userid") String userid);
	
	
	@Query("FROM NimaiOfflineUserDetails nc where nc.additionalUserId= :userid")
	NimaiOfflineUserDetails findByOfflineUserId(@Param("userid") String userid);




	@Query("FROM NimaiOfflineUserDetails nc where nc.emailAddress= :emailAddress")
	NimaiOfflineUserDetails existsByEmailId(@Param("emailAddress") String emailAddress);
}

