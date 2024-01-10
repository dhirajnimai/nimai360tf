package com.nimai.splan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.splan.model.NimaiSubscriptionDetails;
import com.nimai.splan.payload.SubscriptionAndPaymentBean;

@Transactional
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<NimaiSubscriptionDetails, Integer> {

	@Query("FROM NimaiSubscriptionDetails n where n.userid.userid = :userId and Status = 'Active'")
	List<NimaiSubscriptionDetails> findAllByUserId(String userId);


	@Query("FROM NimaiSubscriptionDetails n where n.userid.userid = :userId and n.status = 'Active'")
	NimaiSubscriptionDetails findByUserId(String userId);
	
	@Query(value="select * FROM nimai_subscription_details n where (n.userid = :userId "
			+ "OR n.userid IN(SELECT nc.userid FROM nimai_m_customer nc "
			+ "WHERE nc.account_source=:userId AND nc.ACCOUNT_TYPE != 'REFER') OR n.userid IN(SELECT account_source FROM nimai_m_customer nc "
			+ "WHERE nc.userid=:userId AND nc.ACCOUNT_TYPE != 'REFER')) "
			+ "and n.status = 'Active'", nativeQuery = true)
	NimaiSubscriptionDetails findByUserIdForPostPaid(String userId);
	
	  @Query("FROM NimaiSubscriptionDetails n where n.userid.userid = :userId and Status = 'Active' and paymentStatus!='Rejected'")
	  List<NimaiSubscriptionDetails> findAllByUserIdExpReject(String userId);
	  
	  @Query(value = "select * FROM nimai_subscription_details where userid = :userId and Status = 'Inactive' ORDER BY SPL_SERIAL_NUMBER DESC LIMIT 1", nativeQuery = true)
	  List<NimaiSubscriptionDetails> findAllInactivePlanByUserId(String userId);
	
	
	/*@Modifying
	@Query("update NimaiSubscriptionDetails nsd set nsd.isVasApplied=1 where nsd.userid.userid = :userId and nsd.status = 'Active'")
	void updateIsVASApplied(String userId);*/
	
	@Modifying
	@Query(value = "update nimai_subscription_details set is_vas_applied=1 where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updateIsVASApplied(String userId);

	@Query(value = "select *\n" + 
			"   from nimai_subscription_details\n" + 
			"   where date(splan_start_date) = (select min(date(splan_start_date))\n" + 
			"      from nimai_subscription_details\n" + 
			"      where date(splan_start_date) >= date(curdate())\n" + 
			"   ) AND userid=(:userId) and status='INACTIVE' ORDER BY spl_serial_number DESC LIMIT 1", nativeQuery = true)
	NimaiSubscriptionDetails findLatestInactiveSubscriptionByUserId(String userId);
	
	@Query(value = "select *\n" + 
			"   from nimai_subscription_details\n" + 
			"   where userid=(:userId) and status='INACTIVE' ORDER BY spl_serial_number DESC LIMIT 1", nativeQuery = true)
	NimaiSubscriptionDetails findOnlyLatestInactiveSubscriptionByUserId(String userId);

	@Modifying
	@Query(value = "update nimai_subscription_details set discount_id=(:discountId) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updateDiscountId(String userId, Double discountId);

	@Modifying
	@Query(value = "update nimai_subscription_details set discount_id=(:discountId),discount=(:discount) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updateDiscountIdForPostpaid(String userId, Double discountId,Double discount);
	
	@Modifying
	@Query(value = "update nimai_subscription_details set PAYMENT_TXN_ID=(:payTxnId) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updatePaymentTxnId(String userId, String payTxnId);
	
	
	@Modifying
	@Query(value = "update nimai_subscription_details set PAYMENT_TXN_ID=(:payTxnId), invoice_id=(:invoiceId) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updatePaymentTxnIdInvId(String userId, String payTxnId, String invoiceId);
	
	@Modifying
	@Query(value = "update nimai_subscription_details set PAYMENT_TXN_ID=(:payTxnId), invoice_id=(:invoiceId), vas_amount=(:vasAmount), discount_id=(:discId), "
			+ "discount=(:disc), grand_amount=(:grAmt), payment_mode='Wire', payment_status='Pending' where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updatePaymentDetails(String userId, String payTxnId, String invoiceId, Integer vasAmount,Integer discId,Double disc, Double grAmt);
	
	@Modifying
	@Query(value = "update nimai_subscription_details set invoice_id=(:invoiceId) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updateInvId(String userId, String invoiceId);
	
	@Modifying
	@Query(value = "update nimai_subscription_details set PAYMENT_TXN_ID=(:payTxnId), invoice_id=(:invoiceId), grand_amount=:grandgstValue where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updatePaymentTxnIdForWire(String userId, String payTxnId, String invoiceId, String grandgstValue);
	
	@Modifying
	@Query(value = "update nimai_subscription_details set PAYMENT_TXN_ID=(:payTxnId),payment_mode='Credit',payment_status='Approved', invoice_id=(:invoiceId), grand_amount=:grandgstValue, vas_amount=:vasAmt where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updatePaymentTxnIdForWireNew(String userId, String payTxnId, String invoiceId, String grandgstValue,int vasAmt);

	@Modifying
	@Query(value = "update nimai_subscription_details set PAYMENT_TXN_ID=(:payTxnId), grand_amount=(:amt) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updatePaymentTxnIdGrandAmount(String userId, String payTxnId, Double amt);
	
	@Query(value = "SELECT TIMESTAMPDIFF(month, nsd.SPLAN_START_DATE, nsd.SPLAN_END_DATE)\n" + 
			"FROM nimai_subscription_details nsd \n" + 
			"WHERE nsd.userid=(:userId) AND nsd.`STATUS`='ACTIVE'", nativeQuery = true)
	Double findNoOfMonthOfSubscriptionByUserId(String userId);
	
	@Query(value = "SELECT TIMESTAMPDIFF(month, nsd.SPLAN_START_DATE, CURDATE())\n" + 
			"FROM nimai_subscription_details nsd \n" + 
			"WHERE nsd.userid=(:userId) AND nsd.`STATUS`='ACTIVE'", nativeQuery = true)
	Double findDiffInSubscriptionStartAndCurrentByUserId(String userId);

	@Query(value = "update nimai_subscription_details set is_vas_applied=1,vas_amount=(:vasAmount),grand_amount=grand_amount+(:pricing) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updateVASDetailsApplied(String userId, Float vasAmount, Float pricing);

	@Modifying
	@Query(value = "update nimai_subscription_details set is_vas_applied=1,vas_amount=(:vasAmount),grand_amount=grand_amount+(:pricing) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	void updateVASDetailsAppliedWire(String userId, String vasAmount, String pricing);
	
	@Query(value = "select subscription_amount from nimai_m_subscription where subscription_id=:subscriptionId and status='ACTIVE'", nativeQuery = true)
	Integer getSubscriptionAmt(String subscriptionId);

	  @Query(value = "SELECT nsd.SPL_SERIAL_NUMBER from nimai_subscription_details nsd ORDER BY nsd.SPL_SERIAL_NUMBER DESC LIMIT 1", nativeQuery = true)
	  Integer findLastSerialNo();
	//@Modifying
	//@Query(value = "update nimai_subscription_details set status=(:sts) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	//void updateDiscountId(String userId, String sts);

	  @Modifying
	  @Query(value = "update nimai_subscription_details set LC_UTILIZED_COUNT=(:utilizedValue) where userid=(:userId) and status='ACTIVE'", nativeQuery = true)
	  void updateLCUtilzed(String userId, Integer utilizedValue);

	  @Query(value = "select result.INVOICE_ID,result.INSERTED_DATE,result.PAYMENT_STATUS,\nresult.SPL_SERIAL_NUMBER\nfrom\n(SELECT nsd.INVOICE_ID,nsd.INSERTED_DATE,nsd.PAYMENT_STATUS,\nnsd.SPL_SERIAL_NUMBER \n\tFROM nimai_subscription_details nsd\n\twhere\n\t(nsd.STATUS!='Active' or nsd.PAYMENT_STATUS='Rejected') \n\tand nsd.userid=:userId and nsd.SUBSCRIPTION_NAME !='POSTPAID_PLAN' \n\tunion\n\tSELECT nsv.INVOICE_ID,nsv.INSERTED_DATE,nsv.PAYMENT_STATUS,\n\tnsv.SPL_SERIAL_NUMBER \n\tFROM nimai_subscription_vas nsv INNER JOIN nimai_subscription_details nd ON nsv.SPL_SERIAL_NUMBER=nd.SPL_SERIAL_NUMBER\n\twhere\n\t(nsv.SPLAN_VAS_FLAG=0 and nd.SUBSCRIPTION_NAME !='POSTPAID_PLAN' AND (nsv.STATUS!='Active' or nsv.PAYMENT_STATUS='Rejected')) \n\tand nsv.userid=:userId) result\n\torder by result.SPL_SERIAL_NUMBER desc", nativeQuery = true)
	  List getPreviousSubscription(String userId);

	  @Query(value="select n.INVOICE_ID FROM nimai_subscription_details n where n.userid=:userId  and n.status = 'Active'",nativeQuery = true)
	  NimaiSubscriptionDetails findInvoiceByUserId(String userId);

	  @Query(value="select n.PAYMENT_TXN_ID FROM nimai_subscription_details n where n.userid =:userId and n.status ='Active'", nativeQuery = true)
	  NimaiSubscriptionDetails findPaymentTXNIDByUserId(String userId);

	  @Query(value="select n.usd_currency_value FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId\r\n"
	  		+ "AND (n.user_id=:userId OR n.user_id IN (SELECT userid FROM nimai_m_customer WHERE account_source=:userId))",nativeQuery = true)
	  Double findCurrencyValueByUserIdAndTransactionId(String userId,String transactionId);

	  //BAGU,CONF,CODI
	  @Query(value="select n.confirmation_period FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId\r\n"
		  		+ "AND (n.user_id=:userId OR n.user_id IN (SELECT userid FROM nimai_m_customer WHERE account_source=:userId))",nativeQuery = true)
	  Integer findConfirmationPeriodByUserIdAndTransactionId(String userId,String transactionId);
	  
	  
	  @Query(value="select n.confirmation_period FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId",nativeQuery = true)
	  Integer findConfirmationPeriodByUserIdAndTransactionIdBA(String transactionId);
	  
	  
	  @Query(value="select n.usance_days FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId",nativeQuery = true)
	  Integer findConfirmationPeriodByUserIdAndTrxnIdUsanceBA(String transactionId);
	  
	  
	  //Refinance
	  @Query(value="select n.original_tenor_days FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId\r\n"
		  		+ "AND (n.user_id=:userId OR n.user_id IN (SELECT userid FROM nimai_m_customer WHERE account_source=:userId))",nativeQuery = true)
	  Integer findOriginalTenorByUserIdAndTransactionId(String userId,String transactionId);
	  
	  //Banker,AVAL,DISC
	  @Query(value="select n.discounting_period FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId\r\n"
		  		+ "AND (n.user_id=:userId OR n.user_id IN (SELECT userid FROM nimai_m_customer WHERE account_source=:userId))",nativeQuery = true)
	  Integer findDiscountingPeriodByUserIdAndTransactionId(String userId,String transactionId);
	  
	  //Refinance
	  @Query(value="select n.original_tenor_days FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId",nativeQuery = true)
	  Integer findOriginalTenorByUserIdAndTransactionIdBA(String transactionId);
	  
//	  @Query(value="select n.refinancing_period FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId",nativeQuery = true)
//	  Integer findOriginalTenorByUserIdAndTransactionIdBA(String transactionId);
//	  
	  
	  //Banker,AVAL,DISC
	  @Query(value="select n.discounting_period FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId",nativeQuery = true)
	  Integer findDiscountingPeriodByUserIdAndTransactionIdBA(String transactionId);
	  
	  
	  @Query(value="select n.requirement_type FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId\r\n"
		  		+ "AND (n.user_id=:userId OR n.user_id IN (SELECT userid FROM nimai_m_customer WHERE account_source=:userId))",nativeQuery = true)
	  String findProductByUserIdAndTransactionId(String userId,String transactionId);
	  
	  @Query(value="select n.requirement_type FROM nimai_mm_transaction n WHERE n.transaction_id=:transactionId",nativeQuery = true)
	  String findBAProductByUserIdAndTransactionId(String transactionId);
	  
	  
	  @Query(value="select n.branch_user_email FROM nimai_mm_transaction n where n.user_id=:userId  and n.transaction_id=:transactionId",nativeQuery = true)
	  String findEmailByUserIdAndTransactionId(String userId,String transactionId);
	  @Query(value="select n.usd_currency_value FROM nimai_mm_transaction n where n.transaction_id=:transactionId",nativeQuery = true)
	  Double findCurrencyValueByTransactionId(String transactionId);

	  @Query(value="select n.branch_user_email FROM nimai_mm_transaction n where n.transaction_id=:transactionId",nativeQuery = true)
	  String findEmailByTransactionId(String transactionId);

	  @Query(value="select n.user_id FROM nimai_mm_transaction n where n.transaction_id=:transactionId",nativeQuery = true)
	  String findUserIdByTransactionId(String transactionId);

	  @Query(value="select * FROM nimai_subscription_details n where n.userid=(:userId) and n.status='Active' order by n.spl_serial_number desc  limit 1",nativeQuery = true)
	  NimaiSubscriptionDetails findByUserIdAndStatus(String userId);

	  @Modifying
	  @javax.transaction.Transactional
	  @Query(value = "update nimai_subscription_details  set payment_status= 'Pending' WHERE userid=(:userid)",nativeQuery = true)
	  void updatePaymentStatusInSubscription(String userid);


	  @Query(value="SELECT * FROM nimai_subscription_details nsd WHERE \r\n"
		  		+ "	nsd.userid in('CU58125','CU18124','BA17184','BA51108','CU37100','CU16174','BC1177','BC19107','CU9131','BC34108','CU9140','CU39171','CU39171','CU1354','BC19163','CU50141','BA58163','CU16107','CU54158','CU4168','CU44179','CU48138','BC2128','CU51152','CU53158','CU12126','BC8178','CU0153','CU20173') "
		  		+ "and	(nsd.`STATUS`='INACTIVE' OR nsd.LC_COUNT-nsd.LC_UTILIZED_COUNT<=0) AND nsd.SUBSCRIPTION_NAME!='POSTPAID_PLAN'\r\n"
		  		+ "		group BY nsd.userid ORDER BY nsd.SPL_SERIAL_NUMBER",nativeQuery = true)
	  List<NimaiSubscriptionDetails> findListOfUserToConvertToPostpaid();

	  @Modifying
	  @javax.transaction.Transactional
	  @Query(value = "UPDATE nimai_subscription_details SET STATUS='INACTIVE' WHERE SPL_SERIAL_NUMBER=:splId",nativeQuery = true)
	  void updateStatusToInactive(Integer splId);

	  @Modifying
	  @Query(value = "UPDATE nimai_subscription_details nsd SET \r\n"
	  		+ "nsd.LC_UTILIZED_COUNT= \r\n"
	  		+ "case \r\n"
	  		+ "	when (nsd.LC_COUNT-nsd.LC_UTILIZED_COUNT)<1 then  \r\n"
	  		+ "	nsd.LC_UTILIZED_COUNT-1\r\n"
	  		+ "	when (nsd.LC_COUNT-nsd.LC_UTILIZED_COUNT)=1 then \r\n"
	  		+ "	0\r\n"
	  		+ "	else\r\n"
	  		+ "	0\r\n"
	  		+ "end\r\n"
	  		+ "WHERE nsd.userid=:userId AND nsd.`STATUS`='Active'", nativeQuery = true)
	  void updateLCUtilzedPostPaid(String userId);

}
