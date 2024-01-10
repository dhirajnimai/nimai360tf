package com.nimai.lc.repository;

import com.nimai.lc.entity.NimaiLC;
import com.nimai.lc.entity.NimaiLCTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface LCTempRepository extends JpaRepository<NimaiLCTemp, String> {
}