package com.nimai.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nimai.email.entity.NimaiMOldCustDetails;


@Repository
public interface NimaiMOldCustRepository
extends JpaRepository<NimaiMOldCustDetails, Integer>{

	@Query(value="select * from nimai_m_old_customer_details ns where ns.old_email_address=:emailId",nativeQuery = true)
	NimaiMOldCustDetails finByEmailId(String emailId);
}
