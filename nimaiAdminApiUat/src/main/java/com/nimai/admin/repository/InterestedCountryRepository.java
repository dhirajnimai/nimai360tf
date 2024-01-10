package com.nimai.admin.repository;

import java.util.List;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.admin.model.InterestedCountry;
import com.nimai.admin.model.NimaiMCustomer;

@Transactional
public interface InterestedCountryRepository extends JpaRepository<InterestedCountry, Long> {

	List<InterestedCountry> findByUserId(NimaiMCustomer nc);
	
	@Modifying
	@Query("delete from InterestedCountry b where b.userId.userid =:userId")
	void deleteInterestedCountryUserId(@Param("userId") String userId);



}
