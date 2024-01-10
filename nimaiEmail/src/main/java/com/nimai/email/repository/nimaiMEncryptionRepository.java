package com.nimai.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.email.entity.NimaiEncryptedDetails;
import com.nimai.email.entity.NimaiToken;


@Repository
@Transactional
public interface nimaiMEncryptionRepository extends JpaRepository<NimaiEncryptedDetails, String> {

}
