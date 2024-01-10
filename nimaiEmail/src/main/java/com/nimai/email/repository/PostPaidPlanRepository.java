package com.nimai.email.repository;


import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.email.entity.NimaiPostpaidSubscriptionDetails;




public interface PostPaidPlanRepository extends JpaRepository<NimaiPostpaidSubscriptionDetails, Integer> {

	@Query(value="select * from nimai_postpaid_subscription_details ns where ns.invoice_id=:invoiceId and " +
			"ns.userid IN(:userId)",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetails> getDetailsByInvoiceId(List<String> userId,String invoiceId);

	@Query(value="SELECT userid from nimai_postpaid_subscription_details WHERE invoice_id=:invoiceId",nativeQuery = true)
	List<String> getGroupOfUserByInvoiceId(String invoiceId);
	
	@Query(value="select * from nimai_postpaid_subscription_details ns where " +
			"(ns.userid=:userId or ns.userid IN(SELECT nc.USERID FROM nimai_m_customer \n" +
			"nc WHERE nc.ACCOUNT_SOURCE=:userId)) and ns.invoice_id=:invoiceId",nativeQuery = true)
	List<NimaiPostpaidSubscriptionDetails> getDetailsByUserId(String userId,String invoiceId);

}
