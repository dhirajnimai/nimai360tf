package com.nimai.admin.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMQuotation;

public interface QuotationRepository
		extends JpaRepository<NimaiMQuotation, Integer>, JpaSpecificationExecutor<NimaiMQuotation> {

	List<NimaiMQuotation> findByUserid(NimaiMCustomer userid);

	List<NimaiMQuotation> findByUseridAndCountryNameIn(NimaiMCustomer userid, List<String> countryName);

	@Query("select count(*) FROM NimaiMQuotation m where m.bankUserid = :userid")
	long quoteCout(@Param("userid") String userid);

//	@Query(value = "SELECT COUNT(*) FROM nimai_m_quotation nmq WHERE (nmq.inserted_date BETWEEN :dateFrom AND :dateTo) and nmq.transaction_id IN(:transactionId)",nativeQuery = true)
//	long quoteCount(@Param("dateFrom")Date dateFrom,@Param("dateTo") Date dateTo,@Param("transactionId") List<String> transactionId);

	
	@Query(value = "SELECT COUNT(*) FROM nimai_m_quotation nmq WHERE nmq.transaction_id IN(:transactionId) and nmq.inserted_date between :dateFrom and :dateTo",nativeQuery = true)
	long quoteCount(@Param("transactionId") List<String> transactionId,String dateFrom,String dateTo);
	
//	@Param("transactionId") List<String> transactionId
//	@Query("select count(*) FROM NimaiMQuotation")
//	long quoteCount();
	
//	 @Query(value = "select SUM(total_due) from nimai_m_quotation INNER JOIN nimai_postpaid_subscription_details ON nimai_m_quotation.transaction_id= nimai_postpaid_subscription_details.txn_id WHERE nimai_postpaid_subscription_details.userid=:userId and (payment_status='Pending' or payment_status='Maker Approved' or payment_status='Approved')", nativeQuery = true)
//	    Double findSumOfTotalDue(String userId);

//	@Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdraw' AND quotation_status != 'FreezePlaced' ) AND userid=:userId and (payment_status='Pending' or payment_status='Maker Approved' or payment_status='Approved')", nativeQuery = true)
//	Double findSumOfTotalDue(String userId);

	@Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdraw' AND quotation_status != 'FreezePlaced' ) AND" +
			"(userid IN(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE=:userId ) OR userid=:userId) " +
			"and payment_status='Pending' and Due_type='totalDue'", nativeQuery = true)
	Double findSumOfTotalDue(String userId);

//	 @Query(value = "select SUM(round(total_due)) from nimai_m_quotation INNER JOIN nimai_postpaid_subscription_details ON nimai_m_quotation.transaction_id= nimai_postpaid_subscription_details.txn_id WHERE nimai_postpaid_subscription_details.userid=:userId and (payment_status='Pending' or payment_status='Maker Approved')", nativeQuery = true)
//	Double findSumOfTotalDueAfterApproval(String userId);

	@Query(value = "select SUM((round(total_due))) AS total from nimai_postpaid_subscription_details WHERE \n" +
			" txn_id IN(SELECT distinct transaction_id FROM nimai_m_quotation WHERE  quotation_status != 'Withdrawn' \n" +
			" AND quotation_status != 'FreezePlaced' ) AND (userid=:userId OR userid IN(SELECT userid FROM \n" +
			" nimai_m_customer nc WHERE nc.account_source=:userId AND nc.account_type!='REFER') OR \n" +
			" userid IN(SELECT account_source FROM nimai_m_customer nc WHERE nc.userid=:userId AND \n" +
			" nc.account_type!='REFER')) and ((Due_type IS NULL AND payment_mode IS NULL) OR \n" +
			" (payment_status='Rejected' AND payment_mode='Wire'))", nativeQuery = true)
	Double findSumOfTotalDueBeforeInitiate(String userId);
	 
	 
//	 @Query(value = "select round(min_due) from nimai_m_quotation INNER JOIN \r\n" +
//	 		" nimai_postpaid_subscription_details ON  \r\n" +
//	 		" nimai_m_quotation.transaction_id= nimai_postpaid_subscription_details.txn_id \r\n" +
//	 		" WHERE nimai_postpaid_subscription_details.userid=:userId AND \r\n" +
//	 		"  (payment_status='Pending' OR \r\n" +
//	 		"payment_status='Maker Approved' ) ORDER BY postpaid_id  LIMIT 1", nativeQuery = true)
//		Double findMinDueAfterApproval(String userId);

	@Query(value = "SELECT round(min_due) from nimai_m_quotation \n" +
			"INNER JOIN nimai_postpaid_subscription_details ON \n" +
			"nimai_m_quotation.transaction_id= nimai_postpaid_subscription_details.txn_id WHERE \n" +
			"(nimai_postpaid_subscription_details.userid=:userId OR nimai_postpaid_subscription_details.userid IN(SELECT nc.USERID FROM nimai_m_customer \n" +
			"nc WHERE nc.ACCOUNT_SOURCE=:userId)) AND \n" +
			"((Due_type IS NULL AND payment_mode IS NULL) OR (payment_status='Rejected' AND payment_mode='Wire')) AND quotation_status !='FreezePlaced' AND quotation_status !='Withdrawn' \n" +
			"ORDER BY inserted_date,modified_date LIMIT 1", nativeQuery = true)
	Double findMinDueBeforeInitiate(String userId);

	@Query ( value = "SELECT COUNT(DISTINCT nq.quotation_id) from nimai_m_quotation nq \r\n"
			+ "WHERE \r\n"
			+ "  (nq.inserted_date BETWEEN :dateFrom AND :dateTo) \r\n"
			+ "   AND nq.transaction_id=:transactionId ", nativeQuery = true)
	int getQuoteCount(@Param("dateFrom")Date dateFrom,@Param("dateTo") Date dateTo,@Param("transactionId")  String transactionId);

	@Query(value = "SELECT nq.quotation_status FROM nimai_m_quotation nq\r\n"
			+ "WHERE nq.transaction_id=:transationId order by nq.quotation_id DESC LIMIT 1", nativeQuery = true)
	String quotationStatus(@Param("transationId") String transationId);


}
