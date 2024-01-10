package com.nimai.admin.repository;

import com.nimai.admin.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.admin.model.NimaiPostpaidSubscriptionDetailsUpd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NimaiPostpaidSubscriptionDetailsUpdRepo extends JpaRepository<NimaiPostpaidSubscriptionDetailsUpd, Integer>{


	@Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE (userid \n" +
			"IN(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE=:userId)\n" +
			"OR userid=:userId)and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetailsUpd> findByUserIdAndModeAndStatus(String userId, String paymentMode, String paymentStatus);


	@Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE (userid \n" +
			"IN(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE=:userId)\n" +
			"OR userid=:userId)and nd.SUBSCRIPTION_DETAILS_ID=:subscriptionId",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetailsUpd> findByUserIdAndSubscriptionIdAndSerialNo(String userId, int subscriptionId);


	@Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id IN(:transactionIds) and userid in(:userIds) and (payment_status='Pending' or payment_status='Rejected')", nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findPendingTransactionIdsFromPostpaid(List<String>transactionIds, List<String>userIds);

	@Modifying
	@Query(value = "update nimai_postpaid_subscription_details set min_due=(:finalMinDue), total_due=(:finalTotalDue), per_transaction_due=(:perTxn) where userid=(:userId) and txn_id=(:txnId)", nativeQuery = true)
	void updateMinDueTotalDue(String userId,String txnId,String finalMinDue, String finalTotalDue, String perTxn);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and payment_status='Rejected'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdAsRejected(String userId);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and (payment_status='Pending' OR payment_status='Maker Approved')",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserId(String userId);
	
	@Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and payment_status='Maker Approved'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetailsUpd> findDataOfUserByUserIdMakerApproved(String userId);
	
	@Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id =:transactionId and userid=:userId and payment_status='Rejected'", nativeQuery = true)
    NimaiPostpaidSubscriptionDetailsUpd findPendingTransactionIdsFromQuotationInUnpaidAndOverAllRejected(String userId,String transactionId);
    
	@Query(value="select * FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
    		+ "OR n.userid IN(SELECT nc.userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and n.txn_id=:transactionId",nativeQuery = true)
    NimaiPostpaidSubscriptionDetailsUpd  findUserByUserIdAndTransactionId(String userId,String transactionId);

	@Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE \r\n" +
			" nd.userid=:userId and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus and (nd.Due_Type=:dueType or nd.Due_Type=:mDueType ) limit 1 ",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetailsUpd> findByUserIdAndSubscriptionIdAndTotalMaker(String userId, String paymentMode,
																				String paymentStatus,String dueType,String mDueType);

	@Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE \r\n" +
			"(nd.userid=:userId or nd.userid IN(SELECT nc.userid FROM nimai_m_customer \n" +
			"nc WHERE nc.ACCOUNT_SOURCE=:userId)) and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetailsUpd> findByUserIdAndPostPaidDetails(String userId, String paymentMode,String paymentStatus);

}
