package com.nimai.lc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.nimai.lc.entity.*;

@Repository
@Transactional
public interface NimaiMBranchRepository
extends JpaRepository<NimaiMBranch, Integer>, JpaSpecificationExecutor<NimaiMBranch>{

	@Query(value="From NimaiMBranch b WHERE userid=:userId and emailId!=:placedByEmail and emailId!=:maUserEmail order by b.insertTime desc")
	List<NimaiMBranch> getBranchUserDetails(@Param("userId")String userId,@Param("placedByEmail")String placedByEmail,@Param("maUserEmail")String maUserEmail);

	
	
}
