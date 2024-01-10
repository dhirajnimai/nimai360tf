package com.nimai.email.repository;

import com.nimai.email.entity.NimaiPostpaidSubscriptionDetails;
import com.nimai.email.entity.NimaiSubscriptionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface PostpaidSubscriptionDetailsRepository extends JpaRepository<NimaiPostpaidSubscriptionDetails, Integer>, JpaSpecificationExecutor<NimaiPostpaidSubscriptionDetails> {
 
	
	
	@Query(value = "SELECT *  from nimai_postpaid_subscription_details n where n.userid=:userId", nativeQuery = true)
    NimaiPostpaidSubscriptionDetails getSplanPostpaidBankInvoiceDetails(@Param("userId") String paramString1);

    //subscriber type bank hoga NimaiMCustomer
    @Query(value = "SELECT *  from nimai_postpaid_subscription_details psd inner join nimai_m_customer ON \r\n"
    		+ "psd.userid=  nimai_m_customer.userid WHERE \r\n"
    		+ "nimai_m_customer.subscriber_type='BANK' AND psd.subscription_details_id \r\n"
    		+ "IN(SELECT sd.SPL_SERIAL_NUMBER FROM nimai_subscription_details sd WHERE \r\n"
    		+ "sd.SPLAN_END_DATE<=DATE(NOW()+INTERVAL 30 DAY)) GROUP BY nimai_m_customer.userid", nativeQuery = true)
    List<NimaiPostpaidSubscriptionDetails> getListSplanPostpaidBankInvoiceDetails();

    @Query(value="SELECT  sum(total_due) from nimai_postpaid_subscription_details INNER JOIN  nimai_m_quotation  ON nimai_m_quotation.transaction_id= nimai_postpaid_subscription_details.txn_id WHERE nimai_postpaid_subscription_details.userid=:userId", nativeQuery = true)
    Double getTotalDueByQuotation(String userId);

    @Query(value="SELECT  min_due from nimai_postpaid_subscription_details  INNER JOIN  nimai_m_quotation  ON nimai_m_quotation.transaction_id= nimai_postpaid_subscription_details.txn_id WHERE nimai_postpaid_subscription_details.userid=:userId ORDER BY postpaid_id LIMIT 1", nativeQuery = true)
    Double getMinDueByQuotation(String userId);
    
    @Query(value="SELECT userid from nimai_postpaid_subscription_details WHERE invoice_id=:invoiceId",nativeQuery = true)
	List<String> getGroupOfUserByInvoiceId(String invoiceId);

}
