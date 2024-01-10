package com.nimai.lc.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.lc.entity.OfflineTxnBank;

@Transactional
public interface OfflineTxnBankRepo extends JpaRepository<OfflineTxnBank, Integer>
{
	@Modifying
	@Query(value = "delete from nimai_offline_txn_bank where parent_userid=(:parentUserId) and txnid=(:txnId)", nativeQuery = true)
	void deleteUsingParentID(@Param("parentUserId") String parentUserId,@Param("txnId") String txnId);

	@Query(value = "select * from nimai_offline_txn_bank where parent_userid=(:parentUserId) and txnid=(:txnId)", nativeQuery = true)
	List<OfflineTxnBank> getListOfOfflineTxnUsers(String parentUserId,String txnId);
	
	@Query(value = "select * from nimai_offline_txn_bank where parent_userid=(:userId) and txnid=(:txnId) and emailid=:emailId", nativeQuery = true)
	OfflineTxnBank getOfllineTrxnDetails(@Param("userId")String userId,@Param("txnId")String txnId,@Param("emailId")String emailId);
	
	@Query(value = "select * from nimai_offline_txn_bank where userid=(:userId) and txnid=(:txnId) and emailid=:emailId", nativeQuery = true)
	OfflineTxnBank getOfllineUsrId(@Param("userId")String userId,@Param("txnId")String txnId,@Param("emailId")String emailId);
	

	@Query(value = "select * from nimai_offline_txn_bank where userid=(:userId) and txnid=(:txnId) ", nativeQuery = true)
	OfflineTxnBank getSeOfllineUsrId(@Param("userId")String userId,@Param("txnId")String txnId);
	
	
	@Query(value = "select * from nimai_offline_txn_bank where userid=(:userId)", nativeQuery = true)
	List<OfflineTxnBank> getListOfOfflineTxnUsersByUserId(String userId);
	
	
	@Query(value = "select * from nimai_offline_txn_bank where parent_userid=(:userId)", nativeQuery = true)
	List<OfflineTxnBank> getListOfOfflineTxnUsersByPaUserId(String userId);
	
	@Query(value = "SELECT * FROM nimai_offline_txn_bank WHERE \r\n"
			+ "txnid=(:txnId) AND\r\n"
			+ "(userid IN\r\n"
			+ "(SELECT userid FROM nimai_offline_users WHERE userid IN \r\n"
			+ "(SELECT userid FROM nimai_m_customer WHERE account_source=(:userId))) OR \r\n"
			+ "userid='360tf partner banks')", nativeQuery = true)
	List<OfflineTxnBank> getListOfOfflineTxnUsersByUserIdTxnId(String userId,String txnId);
	
}
