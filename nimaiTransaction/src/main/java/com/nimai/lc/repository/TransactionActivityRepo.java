package com.nimai.lc.repository;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nimai.lc.entity.TransactionActivity;

@Transactional
public interface TransactionActivityRepo  extends JpaRepository<TransactionActivity, Integer> {

	@Query(value="SELECT * from transaction_activity where userid=(:userIdByQid) and transaction_id=(:transId)", nativeQuery = true )
	TransactionActivity getDetailByUserIdTxnId(String userIdByQid, String transId);

}
