package com.nimai.admin.repository;

import java.util.List;


import com.nimai.admin.model.NimaiPostpaidSubscriptionDetailsUpd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.admin.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.admin.model.NimaiSubscriptionDetails;
import com.nimai.admin.model.NimaiSubscriptionVas;

public interface PostPaidPlanRepository extends JpaRepository<NimaiPostpaidSubscriptionDetails, Integer>, JpaSpecificationExecutor<NimaiSubscriptionDetails> {

	
	  @Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE nd.subscription_details_id=:intValue \r\n" +
	  		" AND (nd.userid=:userId OR nd.userid IN (:userIdList))",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetails> findByUserIdAndSubscriptionIdAndSerialNo(String userId, int intValue,List<String> userIdList);

	  @Query(value = "SELECT USERID FROM nimai_m_customer WHERE ACCOUNT_SOURCE=:accountSource",nativeQuery = true)
	  List<String> findUserIdByAccountSource(String accountSource);

	  @Query(value = "select userid from nimai_postpaid_subscription_details where postpaid_id=:id",nativeQuery = true)
	  String findUserIdByPerId(Integer id);

	@Query(value = "select * from nimai_postpaid_subscription_details where postpaid_id=:id",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetails> findAllByPerId(Integer id);

//	  @Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE \r\n" +
//		  		"nd.userid=:userId and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus",nativeQuery = true)
//	List<NimaiPostpaidSubscriptionDetails> findByUserIdAndSubscriptionIdAndSerialNo(String userId, String paymentMode, String paymentStatus);

//	@Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE (userid \n" +
//			"IN(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE=:userId \n" +
//			"AND nc.ACCOUNT_TYPE='SUBSIDIARY') OR userid=:userId) and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus",nativeQuery = true)
//	List<NimaiPostpaidSubscriptionDetails> findByUserIdAndSubscriptionIdAndSerialNo(String userId, String paymentMode, String paymentStatus);

//	@Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE (userid \n" +
//			"IN(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE='CU9184' \n" +
//			"AND nc.ACCOUNT_TYPE='SUBSIDIARY') OR userid='CU9184')and nd.PAYMENT_MODE='Wire' and nd.PAYMENT_STATUS='Pending'\n",nativeQuery = true)
//	List<NimaiPostpaidSubscriptionDetails> findByUserIdAndSubscriptionIdAndSerialNo(String userId, String paymentMode, String paymentStatus);


	@Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE \r\n" +
		  		"(nd.userid=:userId or nd.userid IN(SELECT nc.userid FROM nimai_m_customer \n" +
			"nc WHERE nc.ACCOUNT_SOURCE=:userId)) and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetails> findByUserIdAndPostPaidDetails(String userId, String paymentMode,String paymentStatus);


	  @Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE \r\n" + 
		  		" nd.userid=:userId and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus and nd.Due_Type=:dueType ",nativeQuery = true)
	NimaiPostpaidSubscriptionDetails findByUserIdAndSubscriptionIdAndTotalDue(String userId, String paymentMode,
			String paymentStatus,String dueType);
 

	
	  @Query(value="SELECT * FROM nimai_postpaid_subscription_details nd WHERE \r\n" + 
		  		" nd.userid=:userId and nd.PAYMENT_MODE=:paymentMode and nd.PAYMENT_STATUS=:paymentStatus and (nd.Due_Type=:dueType or nd.Due_Type=:mDueType ) limit 1 ",nativeQuery = true)
	  List<NimaiPostpaidSubscriptionDetails> findByUserIdAndSubscriptionIdAndTotalMaker(String userId, String paymentMode,
			String paymentStatus,String dueType,String mDueType);


	  @Query(value = "SELECT SUM(b.payment) FROM (SELECT SUM(psd.total_payment) AS payment FROM nimai_postpaid_subscription_details psd \n" +
			  "WHERE psd.subscription_details_id=:subscriptionId \n" +
			  " AND psd.PAYMENT_STATUS='Approved' GROUP BY psd.invoice_id) b",nativeQuery = true)
	Double findTotalPaymentBySubscriptionId(int subscriptionId);

	
}
