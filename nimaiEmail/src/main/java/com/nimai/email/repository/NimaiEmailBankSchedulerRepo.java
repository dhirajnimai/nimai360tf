package com.nimai.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.email.entity.EmailComponentMaster;
import com.nimai.email.entity.NimaiEmailScheduler;
import com.nimai.email.entity.NimaiEmailSchedulerAlertToBanks;

@Repository
@Transactional
public interface NimaiEmailBankSchedulerRepo extends JpaRepository<NimaiEmailSchedulerAlertToBanks, Integer>, 
JpaSpecificationExecutor<EmailComponentMaster> {

}
