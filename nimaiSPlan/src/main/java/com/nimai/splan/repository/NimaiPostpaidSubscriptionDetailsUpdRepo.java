package com.nimai.splan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.nimai.splan.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.splan.model.NimaiPostpaidSubscriptionDetailsUpd;
import com.nimai.splan.model.NimaiSubscriptionDetails;

import javax.transaction.Transactional;

public interface NimaiPostpaidSubscriptionDetailsUpdRepo extends JpaRepository<NimaiPostpaidSubscriptionDetailsUpd, Integer>{

	@Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id IN(:transactionIds)", nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetailsUpd> findPendingTransactionIdsFromQuotation(List<String>transactionIds);

	@Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id IN(:transactionIds) and userid in(:userIds) and (payment_status='Pending' or payment_status='Rejected')", nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findPendingTransactionIdsFromPostpaid(List<String>transactionIds,List<String>userIds);
	@Transactional
	@Modifying
	@Query(value = "update nimai_postpaid_subscription_details set min_due=(:finalMinDue), total_due=(:finalTotalDue), per_transaction_due=(:perTxn) where userid=(:userId) and txn_id=(:txnId)", nativeQuery = true)
	void updateMinDueTotalDue(String userId,String txnId,String finalMinDue, String finalTotalDue, String perTxn);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and payment_status='Rejected'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdAsRejected(String userId);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and (payment_status='Pending' OR payment_status='Maker Approved')",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserId(String userId);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and payment_status='Maker Approved'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdMakerApproved(String userId);
	
	@Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id =:transactionId and "
    		+ "(userid=:userId OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=(:userId)) "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=(:userId)))"
    		+ "and payment_status='Pending'", nativeQuery = true)
    NimaiPostpaidSubscriptionDetailsUpd findPendingTransactionIdsFromQuotationInUnpaidAndOverAll(String userId,String transactionId);

	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
			+ "OR n.userid IN(SELECT nc.userid FROM nimai_m_customer nc "
			+ "WHERE nc.account_source=:userId) OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
			+ "WHERE nc.userid=:userId)) and payment_status='Rejected'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdAsRejectedOverall(String userId);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
			+ "OR n.userid IN(SELECT nc.userid FROM nimai_m_customer nc "
			+ "WHERE nc.account_source=:userId) OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
			+ "WHERE nc.userid=:userId)) "
			+ "and (payment_status='Pending' OR payment_status='Maker Approved')",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdOverall(String userId);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
			+ "OR n.userid IN(SELECT nc.userid FROM nimai_m_customer nc "
			+ "WHERE nc.account_source=:userId) OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
			+ "WHERE nc.userid=:userId)) "
			+ "and payment_status='Maker Approved'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdMakerApprovedOverall(String userId);
	
	@Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id =:transactionId and userid=:userId and payment_status='Rejected'", nativeQuery = true)
    NimaiPostpaidSubscriptionDetailsUpd findPendingTransactionIdsFromQuotationInUnpaidAndOverAllRejected(String userId,String transactionId);
    
	@Query(value="select * FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
    		+ "OR n.userid IN(SELECT nc.userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and n.txn_id=:transactionId",nativeQuery = true)
    NimaiPostpaidSubscriptionDetailsUpd  findUserByUserIdAndTransactionId(String userId,String transactionId);
	
	
	@Query(value = "SELECT distinct nmt.user_id FROM nimai_mm_transaction nmt WHERE nmt.transaction_id NOT IN (SELECT nmq.transaction_id FROM \r\n"
			+ "nimai_m_quotation nmq)", nativeQuery = true)
	List<String> findUserIdForUnQuoted();
	

	@Query(value = "SELECT nmt.transaction_id FROM nimai_mm_transaction nmt WHERE nmt.transaction_id NOT IN (SELECT nmq.transaction_id FROM \r\n"
			+ "nimai_m_quotation nmq) and nmt.user_id IN (:userId)", nativeQuery = true)
	List<String> findTransactionIdForUnQuoted(String userId);
	
	@Query(value = "SELECT nmt.transaction_id FROM nimai_mm_transaction nmt WHERE nmt.transaction_id NOT IN (SELECT nmq.transaction_id FROM \r\n"
			+ "nimai_m_quotation nmq) and nmt.user_id IN (:userId)", nativeQuery = true)
	List<String> findTransactionIdForUnQuoted(List<String> userId);
	
	@Query(value = "SELECT userid FROM nimai_m_customer WHERE account_type!='REFER' and "
			+ "(account_source IN (:userId))", nativeQuery = true)
	List<String> findSubsidiary(List<String> userId);
	
	@Query(value = "SELECT nmt.user_id FROM nimai_mm_transaction nmt WHERE nmt.transaction_id=:txnId", nativeQuery = true)
	String findUserIdFromTxnId(String txnId);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and n.txn_id=:txnId",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdTxnId(String userId,String txnId);
	
	@Query(value = "SELECT COUNT(*) FROM nimai_subscription_details WHERE SUBSCRIPTION_NAME='POSTPAID_PLAN' AND \r\n"
			+ "		userid=:userId AND `STATUS`='ACTIVE' ", nativeQuery = true)
	Integer findCountOfPostPaidPlan(String userId);

	@Query(value = "SELECT * FROM nimai_postpaid_subscription_details npsd WHERE npsd.userid=:userID OR\r\n"
			+ " npsd.userid IN(SELECT userid FROM nimai_m_customer nc\r\n"
			+ " WHERE nc.ACCOUNT_SOURCE =:userID) OR npsd.userid\r\n"
			+ " IN (SELECT account_source \r\n"
			+ " FROM nimai_m_customer nc WHERE nc.USERID=:userID And\r\n"
			+ " nc.account_type!='REFER' OR\r\n"
			+ " npsd.userid IN(SELECT userid FROM nimai_m_customer nc\r\n"
			+ " WHERE nc.account_source=:userID and nc.account_type!='REFER'))", nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetailsUpd> getpostpaidTxnDeatils(String userID);
	
	 @Query(value="select * FROM nimai_subscription_details n where n.userid=(:userId) and n.status='Active' order by n.spl_serial_number desc  limit 1",nativeQuery = true)
	 NimaiPostpaidSubscriptionDetailsUpd findByUserIdAndStatus(String userId);

}
