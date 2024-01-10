package com.nimai.splan.repository;

import com.nimai.splan.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.splan.model.NimaiSubscriptionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
public interface PostpaidSPlanRepository extends JpaRepository<NimaiPostpaidSubscriptionDetails, Integer> {
    // single amount from here
    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId  and n.txn_id=:transactionId",nativeQuery = true)
    NimaiPostpaidSubscriptionDetails findNewUserByUserIdAndTransactionId(String userId,String transactionId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
    		+ "OR n.userid IN(SELECT nc.userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and n.txn_id=:transactionId",nativeQuery = true)
    NimaiPostpaidSubscriptionDetails  findUserByUserIdAndTransactionId(String userId,String transactionId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId AND payment_status='Approved' AND payment_mode='Wire'",nativeQuery = true)
    List<Map<String,String>> findUsersByUserIdAndPaymentStatus(String userId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId AND payment_status='Approved' AND (payment_mode='Wire' or payment_mode='Credit')",nativeQuery = true)
    List<Map<String,String>> findUsersByUserIdAndPaymentStatusForBoth(String userId);
    
    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId AND n.txn_id=:transactionId AND payment_status='Pending'",nativeQuery = true)
    NimaiPostpaidSubscriptionDetails findUsersByUserIdTransactionIdAndPaymentStatus(String userId,String transactionId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
    		+ "OR n.userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "AND (payment_status='Pending' OR payment_status='Maker Approved') AND payment_mode='Wire' and Due_type='totalDue'",nativeQuery = true)
    List<Map<String,String>> findUsersByUserIdAndPaymentStatusPending(String userId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId order by n.postpaid_id desc limit 1",nativeQuery = true)
    NimaiPostpaidSubscriptionDetails findPreviousUserByUserIdAndTransactionId(String userId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId  ORDER BY n.postpaid_id DESC LIMIT 1",nativeQuery = true)
    NimaiPostpaidSubscriptionDetails findLastUserByUserId(String userId);

    @Query(value = "SELECT \r\n" + 
	  		" * FROM nimai_postpaid_subscription_details nsd WHERE \r\n" + 
	  		"(nsd.PAYMENT_STATUS='Approved') AND\r\n" +
	  		" nsd.userid=:userId", nativeQuery = true)
	  List<NimaiPostpaidSubscriptionDetails> getPreviousPostPaidSubscription(String userId);

    @Query(value = "SELECT invoice_id FROM nimai_postpaid_subscription_details " +
            "WHERE postpaid_id=:postpaidId",nativeQuery = true)
    String getInvoiceIdByPostPaidId(Integer postpaidId);

    @Query(value = "SELECT round(min_due) from nimai_postpaid_subscription_details where (userid=:userId "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) and txn_id=:transactionId", nativeQuery = true)
    Double findFirstMinDue(String userId,String transactionId);

    @Query(value = "SELECT payment_status from nimai_postpaid_subscription_details where (userid=:userId "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) and txn_id=:transactionId", nativeQuery = true)
    String findPaymentStatus(String userId,String transactionId);

    @Query(value = "SELECT round(min_due) from nimai_postpaid_subscription_details where userid=:userId and txn_id=:transactionId", nativeQuery = true)
    Double findMinDueByPaymentCounter(String userId,String transactionId);

    @Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdrawn' AND quotation_status != 'FreezePlaced' ) "
    		+ "AND (userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId AND nc.account_type!='REFER') OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId AND nc.account_type!='REFER')) "
    		+ "and (payment_status='Pending' OR payment_status='Maker Approved')", nativeQuery = true)
    Double findSumOfTotalDue(String userId);

    @Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation) "
    		+ "AND (userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId AND nc.account_type!='REFER') OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId AND nc.account_type!='REFER')) "
    		+ "and (payment_status='Pending' OR payment_status='Maker Approved')", nativeQuery = true)
    Double findSumOfTotalDuev2(String userId);
    
    @Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdrawn' AND quotation_status != 'FreezePlaced' ) AND userid=:userId and payment_status='Maker Approved'", nativeQuery = true)
    Double findSumOfTotalDueOfMakerApproved(String userId);
    
    @Query(value = "select COUNT((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdrawn' AND quotation_status != 'FreezePlaced' ) AND userid=:userId and payment_status='Pending'", nativeQuery = true)
    Integer findCountTotalDue(String userId);

    @Query(value = "select COUNT((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdrawn' AND quotation_status != 'FreezePlaced' ) "
    		+ "AND (userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and (payment_status='Pending' OR payment_status='Maker Approved')", nativeQuery = true)
    Integer findCountTotalDueOverall(String userId);
    
    @Query(value = "select COUNT((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation) "
    		+ "AND (userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and (payment_status='Pending' OR payment_status='Maker Approved')", nativeQuery = true)
    Integer findCountTotalDueOverallv2(String userId);
    
    @Query(value = "select COUNT((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdrawn' AND quotation_status != 'FreezePlaced' ) AND userid=:userId and payment_status='Maker Approved'", nativeQuery = true)
    Integer findCountTotalDueMakerApproved(String userId);

    @Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation "
    		+ "WHERE  quotation_status != 'Withdrawn' AND quotation_status != 'FreezePlaced' ) AND "
    		+ "(userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and (payment_status='Rejected' OR payment_status='Pending')", nativeQuery = true)
    Double findSumOfTotalDueRejected(String userId);

    @Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation) "
    		+ "AND (userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and (payment_status='Rejected' OR payment_status='Pending')", nativeQuery = true)
    Double findSumOfTotalDueRejectedv2(String userId);

    @Query(value = "select COUNT((round(total_due))) AS total from nimai_postpaid_subscription_details "
    		+ "WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE "
    		+ "quotation_status != 'Withdrawn' AND quotation_status != 'FreezePlaced' ) AND "
    		+ "(userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and (payment_status='Rejected' OR payment_status='Pending')", nativeQuery = true)
    Integer findCountTotalDueRejected(String userId);
    
    @Query(value = "select COUNT((round(total_due))) AS total from nimai_postpaid_subscription_details "
    		+ "WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation) AND "
    		+ "(userid=:userId OR userid IN(SELECT userid FROM nimai_m_customer nc "
    		+ "WHERE nc.account_source=:userId) OR userid IN(SELECT account_source FROM nimai_m_customer nc "
    		+ "WHERE nc.userid=:userId)) "
    		+ "and (payment_status='Rejected' OR payment_status='Pending')", nativeQuery = true)
    Integer findCountTotalDueRejectedv2(String userId);
    
    
    @Query(value = "SELECT transaction_id from nimai_m_quotation where (userid=:userId OR "
    		+ "userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=(:userId)) "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=(:userId)))"
    		+ "AND quotation_status != 'FreezePlaced' AND quotation_status !='Withdrawn'", nativeQuery = true)
    List findPendingTransactionIdsFromQuotationOnly(String userId);
    
    @Query(value = "SELECT userid from nimai_m_quotation where (userid=:userId OR "
    		+ "userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=(:userId)) "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=(:userId)))"
    		+ "AND quotation_status != 'FreezePlaced' AND quotation_status !='Withdrawn'", nativeQuery = true)
    List findPendingUserIdsFromQuotationOnly(String userId);
    
    @Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id IN(:transactionIds)", nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetails> findPendingTransactionIdsFromQuotation(List<String>transactionIds);
    
    @Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id IN(:transactionIds) and payment_status='Pending'", nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetails> findPendingTransactionIdsFromPostpaid(List<String>transactionIds);

    @Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id =:transactionId and "
    		+ "(userid=:userId OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=(:userId)) "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=(:userId)))"
    		+ "and payment_status='Pending'", nativeQuery = true)
    NimaiPostpaidSubscriptionDetails findPendingTransactionIdsFromQuotationInUnpaidAndOverAll(String userId,String transactionId);

    @Modifying
    @Query(value = "update nimai_postpaid_subscription_details set userid=:userId  where postpaid_id=(:postpaidId)", nativeQuery = true)
    void updateUserIdForSubsidary(String userId,Integer postpaidId);

    @Query(value = "select * from nimai_postpaid_subscription_details WHERE txn_id =:transactionId and userid=:userId", nativeQuery = true)
    NimaiPostpaidSubscriptionDetails findDetailsId(String userId,String transactionId);
    @Query(value = "select DISTINCT txn_id,payment_status,payment_mode,postpaid_id from nimai_postpaid_subscription_details WHERE txn_id =:transactionId and userid=:userId and payment_status='Rejected'", nativeQuery = true)
    NimaiPostpaidSubscriptionDetails findPendingTransactionIdsFromQuotationInUnpaidAndOverAllRejected(String userId,String transactionId);
    
    @Query(value="select DISTINCT(transaction_id),userid from nimai_m_quotation where (userid=:userId \r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId AND nc.ACCOUNT_TYPE!='REFER') \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId AND nc.ACCOUNT_TYPE!='REFER')) AND \r\n"
    		+ "quotation_status !='FreezePlaced' AND quotation_status !='Withdrawn' \r\n"
    		+ "AND transaction_id IN (SELECT txn_id FROM nimai_postpaid_subscription_details WHERE userid=:userId\r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId AND nc.ACCOUNT_TYPE!='REFER') \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId AND nc.ACCOUNT_TYPE!='REFER')\r\n"
    		+ ")\r\n"
    		+ "order by inserted_date,modified_date", nativeQuery = true)
    List findFirstQuotationByDateAndTime(String userId);
    
    
    
    @Query(value="select DISTINCT(transaction_id),userid,inserted_date,modified_date from nimai_m_quotation where (userid=:userId \r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId AND nc.ACCOUNT_TYPE!='REFER') \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId AND nc.ACCOUNT_TYPE!='REFER')) AND \r\n"
    		+ "transaction_id IN (SELECT txn_id FROM nimai_postpaid_subscription_details WHERE userid=:userId\r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId AND nc.ACCOUNT_TYPE!='REFER') \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId AND nc.ACCOUNT_TYPE!='REFER')\r\n"
    		+ ") and (quotation_status='Accepted' or quotation_status='Rejected')\r\n"
    		+ "order by inserted_date,modified_date", nativeQuery = true)
    List findFirstQuotationByDateAndTimev2(String userId);
    
    @Query(value="select DISTINCT(transaction_id),bank_userid from nimai_m_quotation where (userid=:userId \r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId) \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId)) AND \r\n"
    		+ "quotation_status !='FreezePlaced' AND quotation_status !='Withdrawn' \r\n"
    		+ "AND transaction_id IN (SELECT txn_id FROM nimai_postpaid_subscription_details WHERE userid=:userId\r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId) \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId)\r\n"
    		+ ")\r\n"
    		+ "order by inserted_date,modified_date", nativeQuery = true)
    List findFirstQuotationByDateAndTimeForBank(String userId);
    
    @Query(value="select DISTINCT(transaction_id),bank_userid from nimai_m_quotation where (userid=:userId \r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId) \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId)) AND \r\n"
    		+ "transaction_id IN (SELECT txn_id FROM nimai_postpaid_subscription_details WHERE userid=:userId\r\n"
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId) \r\n"
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId)\r\n"
    		+ ")\r\n"
    		+ "order by inserted_date,modified_date", nativeQuery = true)
    List findFirstQuotationByDateAndTimeForBankv2(String userId);
    
    @Query(value="select DISTINCT(transaction_id),bank_userid from nimai_m_quotation WHERE \r\n"
    		+ "((userid=:userId \r\n"
    		+ " OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId) \r\n"
    		+ " OR userid IN(SELECT userid FROM nimai_m_customer nc\r\n"
    		+ "  WHERE nc.account_source=:userId)) OR \r\n"
    		+ "  (bank_userid=:userId OR bank_userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId) \r\n"
    		+ " OR bank_userid IN(SELECT userid FROM nimai_m_customer nc\r\n"
    		+ "  WHERE nc.account_source=:userId)))\r\n"
    		+ "  AND \r\n"
    		+ " quotation_status !='FreezePlaced' AND quotation_status !='Withdrawn' \r\n"
    		+ " AND transaction_id IN (SELECT txn_id FROM nimai_postpaid_subscription_details \r\n"
    		+ " WHERE userid=:userId\r\n"
    		+ " OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=:userId) \r\n"
    		+ " OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=:userId)\r\n"
    		+ " ) order by inserted_date,modified_date", nativeQuery = true)
    List findFirstQuotationByDateAndTimeForBa(String userId);

    @Query(value="select DISTINCT(transaction_id) from nimai_m_quotation where userid=(:userId) AND quotation_status ='FreezePlaced' order by inserted_date ", nativeQuery = true)
    List<String>findFirstQuotationByDateAndTimeAndEqualFreezePlaced(String userId);


    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and payment_status='Rejected'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetails> findDataOfUserByUserIdAsRejected(String userId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and payment_status='Maker Approved'",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetails> findDataOfUserByUserIdMakerApproved(String userId);

    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId and (payment_status='Pending' OR payment_status='Maker Approved')",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetails> findDataOfUserByUserId(String userId);


    @Query(value="select * FROM nimai_postpaid_subscription_details n where n.userid=:userId AND payment_mode='Wire' AND (payment_status='Pending' OR payment_status='Maker Approved') ",nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetails> findDataOfUserByUserIdAndMakerApproved(String userId);

    @Query(value="SELECT transaction_id FROM nimai_m_quotation WHERE userid=:userId AND quotation_status='Withdrawn'",nativeQuery = true)
    List<String> quotationWithdraw(String userId);


    @Query(value="select distinct(txn_id),payment_status,PAYMENT_MODE FROM nimai_postpaid_subscription_details n where (n.userid=:userId "
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=(:userId)) "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=(:userId))) and n.txn_id=:transactionId",nativeQuery = true)
    Object findUsersDetailsByUserIdAndTransactionId(String userId,String transactionId);

    @Query(value="select distinct(txn_id),payment_status FROM nimai_postpaid_subscription_details n where n.userid=:userId "
    		+ "and n.txn_id=:transactionId and (n.PAYMENT_STATUS='Pending' OR n.PAYMENT_STATUS='Maker Approved') "
    		+ "AND n.PAYMENT_MODE='Wire'",nativeQuery = true)
    Object findNewUsersDetailsByUserIdAndTransactionId(String userId,String transactionId);
    
    @Query(value="SELECT transaction_id FROM nimai_mm_transaction WHERE (user_id=:userId "
    		+ "OR user_id=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=(:userId)) "
    		+ "OR user_id IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=(:userId)))",nativeQuery = true)
    List<String> getTxnId(String userId);
    
    @Query(value="SELECT quotation_id FROM nimai_m_quotation WHERE (userid=:userId "
    		+ "OR userid=(SELECT account_source FROM nimai_m_customer nc WHERE nc.USERID=(:userId) and nc.account_type!='REFER') "
    		+ "OR userid IN(SELECT userid FROM nimai_m_customer nc WHERE nc.account_source=(:userId) and nc.account_type!='REFER'))",nativeQuery = true)
    List<Integer> getQuoteId(String userId);

    @Query(value="SELECT user_id FROM nimai_mm_transaction WHERE transaction_id=(:txnId)",nativeQuery = true)
	String getUserId(String txnId);
    
    @Query(value="SELECT transaction_id FROM nimai_m_quotation WHERE quotation_id=(:qId)",nativeQuery = true)
	String getTxnId(Integer qId);
    
    @Query(value="SELECT quotation_status FROM nimai_m_quotation WHERE quotation_id=(:qId)",nativeQuery = true)
	String getQuotationStatus(Integer qId);
    
    @Query(value="SELECT cast(inserted_date as char) FROM nimai_m_quotation WHERE quotation_id=(:qId)",nativeQuery = true)
    String getQuoteInsertedDate(Integer qId);
    
    @Query(value="SELECT cast(modified_date as char) FROM nimai_m_quotation WHERE quotation_id=(:qId)",nativeQuery = true)
    String getQuoteModifiedDate(Integer qId);
    
    @Query(value="SELECT cast(accepted_on as char) FROM nimai_mm_transaction WHERE user_id=(:userId) and transaction_id=(:txnId)",nativeQuery = true)
    String getQuoteAcceptedOn(String userId,String txnId);
    
    @Query(value="select payment_status FROM nimai_postpaid_subscription_details n where n.userid=:userId "
    		+ "and n.txn_id=:transactionId",nativeQuery = true)
    String findPaymentStatusByUserIdAndTransactionId(String userId,String transactionId);
    
    @Query(value="select payment_mode FROM nimai_postpaid_subscription_details n where n.userid=:userId "
    		+ "and n.txn_id=:transactionId",nativeQuery = true)
    String findPaymentModeByUserIdAndTransactionId(String userId,String transactionId);
}

