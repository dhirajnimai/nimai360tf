package com.nimai.email.repository;

import java.util.Date;

import java.util.List;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nimai.email.entity.NimaiMEmployee;



@Repository
public interface EmployeeRepository
		extends JpaRepository<NimaiMEmployee, Integer>, JpaSpecificationExecutor<NimaiMEmployee> {


	@Query("FROM NimaiMEmployee r where r.empCode = :empCode")
	NimaiMEmployee findByEmpCode(@Param("empCode") String empCode);

	

}
