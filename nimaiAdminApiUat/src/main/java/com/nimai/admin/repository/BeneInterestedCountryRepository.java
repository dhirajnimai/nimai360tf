package com.nimai.admin.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.admin.model.BeneInterestedCountry;
import com.nimai.admin.model.InterestedCountry;
import com.nimai.admin.model.NimaiMCustomer;

@Transactional
public interface BeneInterestedCountryRepository extends JpaRepository<BeneInterestedCountry, Long> {

	//List<BeneInterestedCountry> findByUserId(NimaiMCustomer nc);
	
	@Modifying
	@Query("delete from BeneInterestedCountry b where b.userid.userid =:userId")
	void deleteBeneInterestedCountryUserId(@Param("userId") String userId);

	
	


}
