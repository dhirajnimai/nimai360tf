package com.nimai.email.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.email.entity.NimaiClient;
import com.nimai.email.entity.NimaiMOldCustDetails;


@Repository
@Transactional
public interface NimaiMCustomerRepo
extends JpaRepository<NimaiClient, String>, JpaSpecificationExecutor<NimaiClient>{
	
	
	
	@Query(value="SELECT * FROM nimai_m_customer nc WHERE nc.USERID=:userId",nativeQuery = true)
	NimaiClient finByUserId(String userId);
	
	
	
	@Query(value = "SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE=:userId",nativeQuery = true)
	List<String> findGrpIdsByUserId(String userId);

}
