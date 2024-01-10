package com.nimai.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.email.entity.EmailComponentMaster;
import com.nimai.email.entity.NimaiEmailScheduler;

@Repository
@Transactional
public interface NimaiEmailSchedulerRepo extends JpaRepository<NimaiEmailScheduler, Integer>, 
JpaSpecificationExecutor<EmailComponentMaster> {

}
