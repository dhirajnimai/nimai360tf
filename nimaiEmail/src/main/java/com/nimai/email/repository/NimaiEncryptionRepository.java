package com.nimai.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.email.entity.EmailComponentMaster;
import com.nimai.email.entity.NimaiEncryptedDetails;
import com.nimai.email.entity.NimaiMEmployee;

public interface NimaiEncryptionRepository
extends JpaRepository<NimaiEncryptedDetails, String>, 
JpaSpecificationExecutor<NimaiEncryptedDetails>{
	
	@Query("from NimaiEncryptedDetails n where n.userId = :userId ")
	NimaiEncryptedDetails findByEncDetails(@Param("userId") String userId);
	

}
