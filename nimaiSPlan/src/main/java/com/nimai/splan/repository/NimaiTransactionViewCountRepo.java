package com.nimai.splan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nimai.splan.model.NimaiTransactionViewCount;

public interface NimaiTransactionViewCountRepo extends JpaRepository<NimaiTransactionViewCount, Long>
{

	@Query(value="SELECT * FROM nimai_trnx_view_count where user_id=(:userId) ORDER BY id DESC LIMIT 1",nativeQuery = true)
    NimaiTransactionViewCount getviewCountByUserId(String userId);
}