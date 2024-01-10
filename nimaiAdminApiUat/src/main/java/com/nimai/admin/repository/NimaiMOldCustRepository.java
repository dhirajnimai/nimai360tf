package com.nimai.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nimai.admin.model.NimaiMOldCustDetails;
import com.nimai.admin.model.NimaiOfflineUserDetails;

public interface NimaiMOldCustRepository
extends JpaRepository<NimaiMOldCustDetails, Integer> {

}
