package com.nimai.admin.repository;

import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMmTransaction;
import com.nimai.admin.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.admin.model.NimaiSubscriptionDetails;
import java.util.Date;
import java.util.List;
import javax.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<NimaiMCustomer, String>, JpaSpecificationExecutor<NimaiMCustomer> {
  @Query("SELECT userid FROM NimaiMCustomer where lower(userid) like %:userid%")
  List<String> userIdSearch(@Param("userid") String userid);
  
  @Query("SELECT emailAddress FROM NimaiMCustomer where lower(emailAddress) like %:emailAddress%")
  List<String> emailIdSearch(@Param("emailAddress") String emailAddress);
  
  @Query("SELECT mobileNumber FROM NimaiMCustomer where lower(mobileNumber) like %:mobileNumber%")
  List<String> mobileNumberSearch(@Param("mobileNumber") String mobileNumber);
  
  @Query("SELECT companyName FROM NimaiMCustomer where lower(companyName) like %:companyName%")
  List<String> companyNameSearch(@Param("companyName") String companyName);
  
  @Query("SELECT userid FROM NimaiMCustomer where emailAddress like %:emailAddress% or mobileNumber like %:mobileNumber% or countryName like %:companyName%")
  List<String> getUserIdOnEmailMobileNumberCompanyName(@Param("emailAddress") String emailAddress, @Param("mobileNumber") String mobileNumber, @Param("companyName") String companyName);
  
  NimaiMCustomer findByUserid(String paramString);
  
  @Query(value = " SELECT s.SPL_SERIAL_NUMBER, m.userid,m.subscriber_type,m.company_name,\r\n" + 
  		" m.mobile_number,\r\n" + 
  		" m.landline,m.country_name,m.email_address,\r\n" + 
  		"m.first_name,m.last_name,s.PAYMENT_TXN_ID,s.INVOICE_ID \r\n" + 
  		"AS INVOICE_NUMBER,s.subscription_name,s.`STATUS` AS subscription_status,\r\n" + 
  		"nvs.pricing AS VAS_PRICING,nvs.`status` AS \r\n" + 
  		"vas_status,s.subscription_amount,\r\n" + 
  		"m.mode_of_payment,s.inserted_date,\r\n" + 
  		" s.splan_start_date,s.splan_end_date, nmd.COUPON_CODE,s.DISCOUNT,\r\n" + 
  		" (s.SUBSCRIPTION_AMOUNT + s.VAS_AMOUNT - s.DISCOUNT) AS NET_FEE,\r\n" + 
  		" s.maker_comment,s.checker_comment,\r\n" + 
  		"s.PAYMENT_STATUS from nimai_subscription_details s left join\r\n" + 
  		"nimai_m_customer m  ON (s.userid=m.USERID) left JOIN \r\n" + 
  		"nimai_subscription_vas nvs ON (nvs.SPL_SERIAL_NUMBER=s.SPL_SERIAL_NUMBER)\r\n" + 
  		"left JOIN nimai_m_discount nmd\r\n" + 
  		"ON (s.DISCOUNT_ID=nmd.DISCOUNT_ID OR s.DISCOUNT_ID!=nmd.DISCOUNT_ID)\r\n" + 
  		"WHERE s.userid= :userid and s.INSERTED_DATE BETWEEN :dateFrom AND :dateTo \r\n" + 
  		"GROUP BY s.invoice_id\r\n" + 
  		"", nativeQuery = true)
  List<Tuple> getPaymentSubUserIdReport(@Param("dateFrom") String dateFrom, @Param("dateTo") Date paramDate, @Param("userid") String userid);
  
  NimaiMCustomer findByUseridAndCountryNameIn(String paramString, List<String> paramList);
  
  @Query("SELECT bankName FROM NimaiMCustomer where USERID =:userid")
  String findBankName(@Param("userid") String userid);
  
  @Query(value = "SELECT * FROM nimai_m_customer c where c.USERID =:userid", nativeQuery = true)
  NimaiMCustomer custDetails(@Param("userid") String userid);
  
  @Query("SELECT userid FROM NimaiMCustomer where lower(userid) "
  		+ "like %:userid% and userid like %:data%")
  List<String> userIdDataSearch(@Param("userid") String userid, @Param("data") String data);
  
  @Query("SELECT emailAddress FROM NimaiMCustomer where lower(emailAddress) like %:emailAddress%")
  List<String> emailIdDataSearch(@Param("emailAddress") String emailAddress);
  
  @Query("SELECT mobileNumber FROM NimaiMCustomer where lower(mobileNumber) like %:mobileNumber%")
  List<String> mobileNumberDataSearch(@Param("mobileNumber") String mobileNumber);
  
  @Query("SELECT companyName FROM NimaiMCustomer where lower(companyName) like %:companyName%")
  List<String> companyNameDataSearch(@Param("companyName") String companyName);
  
  @Query("from NimaiMCustomer where lower(emailAddress) =:emailAddress")
  NimaiMCustomer findByEmailAddress(@Param("emailAddress") String emailAddress);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc "
  		+ "WHERE lower(nc.userid)  like 'BA%' and nc.kyc_status='Approved' AND "
  		+ " case when nc.account_type='MASTER' then"
  		+ " nc.userid IN (SELECT nsd.userid from nimai_subscription_details nsd "
  		+ "WHERE nsd.status='Active')\r\n AND nc.payment_status='Approved'"
  		+ " when nc.account_type='BANKUSER' then"
  		+ " nc.account_source IN (SELECT nsd.userid from nimai_subscription_details nsd "
  		+ "WHERE nsd.status='Active')"
  		+ " AND nc.payment_status='Approved'"
  		+ " end order by nc.BANK_NAME ASC", nativeQuery = true)
  List<NimaiMCustomer> findBankDetails();
  
  @Query("select kycStatus from NimaiMCustomer where lower(emailAddress) =:emailAddress")
  String findKycByEmailAddress(@Param("emailAddress") String emailAddress);
  
  @Query("SELECT bankName FROM NimaiMCustomer where lower(bankName) like %:bankName%")
  List<String> bankNameDataSearch(@Param("bankName") String bankName);
  
  @Query(value = "select c.userid,c.first_name,c.last_name,c.company_name,"
  		+ "c.country_name,sd.subscription_name,sd.is_vas_applied,"
  		+ "sd.status,sd.splan_start_date,sd.splan_end_date,"
  		+ "(sd.lc_count-sd.lc_utilized_count) as"
  		+ " bal_count from nimai_m_customer c LEFT join nimai_subscription_details sd "
  		+ "on c.userid=sd.userid where c.country_name= :country_name "
  		+ "and c.subscriber_type= :subscriber_type "
  		+ "and c.BANK_TYPE= :bankType", nativeQuery = true)
  List<Object[]> getByCountryAndCustType(@Param("country_name") String countryName, @Param("subscriber_type") String paramString2, @Param("bankType") String paramString3);
  
  @Query(value = "SELECT q.bank_userid,q.userid AS "
	  		+ "Customer_User_Id,q.mobile_number,m.LANDLINE,q.email_address,"
	  		+ "q.inserted_date,q.bank_name as Underwriting_Bank,"
	  		+ "q.branch_name,q.country_name as Underwriting_Bank_Country,"
	  		+ "q.transaction_id,nm.lc_value,nm.lc_currency,nm.lc_issuance_bank,"
	  		+ "nm.lc_issuance_branch,nm.lc_issuance_country,nm.payment_terms,nm.usd_currency_value,"
	  		+ "nm.lc_issuing_date,nm.original_tenor_days,nm.requirement_type,"
	  		+ "nm.confirmation_period,nm.discounting_period,nm.refinancing_period,"
	  		+ "nm.validity as Transaction_Validity,q.applicable_benchmark,q.Total_Quote_Value,"
	  		+ "q.confirmation_charges,q.discounting_charges,q.IS_OFFERED,"
	  		+ "q.refinancing_charges,q.banker_accept_charges,nm.offered_price,"
	  		+ "q.conf_chgs_issuance_to_negot,q.conf_chgs_issuance_to_matur,"
	  		+ "q.negotiation_charges_fixed,q.negotiation_charges_perct,q.term_condition_comments,"
	  		+ "q.other_charges,q.min_transaction_charges,q.quotation_status,"
	  		+ "q.validity_date as Quote_Validity_date from nimai_m_customer m inner join "
	  		+ "nimai_m_quotation q on m.userid=q.bank_userid INNER JOIN "
	  		+ "nimai_mm_transaction nm ON q.transaction_id=nm.transaction_id"
	  		+ " WHERE q.inserted_date BETWEEN :startDate AND :endDate   "
	  		+ "ORDER BY q.inserted_date desc", nativeQuery = true)
	  List<Tuple> getAllTransactionDetails(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	  
	  @Query(value = "SELECT q.bank_userid,q.userid AS "
	  		+ "Customer_User_Id,q.mobile_number,m.LANDLINE,q.email_address,"
	  		+ "q.inserted_date,q.bank_name as Underwriting_Bank,"
	  		+ "q.branch_name,q.country_name as Underwriting_Bank_Country,"
	  		+ "q.transaction_id,nm.lc_value,nm.lc_currency,nm.lc_issuance_bank,"
	  		+ "nm.lc_issuance_branch,nm.lc_issuance_country,nm.usd_currency_value,"
	  		+ "nm.lc_issuing_date,nm.original_tenor_days,nm.payment_terms,"
	  		+ "nm.requirement_type,nm.confirmation_period,q.IS_OFFERED,"
	  		+ "nm.discounting_period,nm.refinancing_period,"
	  		+ "nm.validity as Transaction_Validity,nm.offered_price,"
	  		+ "q.applicable_benchmark,q.confirmation_charges,q.quotation_status,"
	  		+ "q.discounting_charges,q.refinancing_charges,q.banker_accept_charges,"
	  		+ "q.conf_chgs_issuance_to_negot,q.conf_chgs_issuance_to_matur,"
	  		+ "q.negotiation_charges_fixed,q.negotiation_charges_perct,"
	  		+ "q.other_charges,q.min_transaction_charges,q.term_condition_comments,"
	  		+ "q.validity_date as Quote_Validity_date "
	  		+ "from nimai_m_customer m inner join nimai_m_quotation q "
	  		+ "on m.userid=q.bank_userid\r\nINNER JOIN nimai_mm_transaction nm "
	  		+ "ON q.transaction_id=nm.transaction_id"
	  		+ " WHERE q.inserted_date BETWEEN (:startDate) AND (:endDate) "
	  		+ "AND q.bank_userid=(:userid)   ORDER BY q.inserted_date desc", nativeQuery = true)
	  List<Tuple> getAllTransactionDetailsUserId(@Param("startDate") String startDate, @Param("endDate") Date endDate, @Param("userid") String userid);
	  
  
    
  @Query(value = "select m.userid as userid,m.subscriber_type as customer_type,m.company_name as "
  		+ "customer_name,s.subscription_name as subs_plan,s.is_vas_applied as vas,"
  		+ "s.GRAND_AMOUNT as fee,m.currency_code as ccy,m.mode_of_payment,"
  		+ "m.payment_status,m.kyc_status,m.rm_id as rm from nimai_m_customer m left "
  		+ "join nimai_subscription_details s on m.USERID=s.userid where m.KYC_STATUS='Pending' "
  		+ "and m.inserted_date between :startDate and :endDate group by m.USERID ", nativeQuery = true)
  List<Tuple> getNewUserReports(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
  
  @Query(value = "select m.userid as userid,m.subscriber_type as customer_type,m.company_name as "
  		+ "customer_name,s.subscription_name as subs_plan,s.is_vas_applied as vas,"
  		+ "s.GRAND_AMOUNT as fee,m.currency_code as ccy,m.mode_of_payment,m.payment_status,"
  		+ "m.kyc_status,m.rm_id as rm from nimai_m_customer m left join "
  		+ "nimai_subscription_details s on m.USERID=s.userid where m.KYC_STATUS='Pending' "
  		+ "and m.INSERTED_DATE between :startDate and :endDate and m.USERID= :userid group by "
  		+ "m.USERID", nativeQuery = true)
  List<Tuple> getNewUserIdReports(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("userid") String userid);
  
//  @Query(value = " select m.userid,m.subscriber_type,m.company_name,m.mobile_number,"
//  		+ " m.landline,m.country_name,m.email_address,"
//  		+ "m.first_name,m.last_name,s.PAYMENT_TXN_ID,s.INVOICE_ID "
//  		+ "AS INVOICE_NUMBER,s.subscription_name,s.`STATUS` AS subscription_status,"
//  		+ "s.is_vas_applied,nvs.pricing AS VAS_PRICING,nvs.`status` AS "
//  		+ "vas_status,s.subscription_amount,"
//  		+ " m.currency_code,m.mode_of_payment,s.inserted_date,"
//  		+ " s.splan_start_date,s.splan_end_date,s.DISCOUNT_ID,nmd.COUPON_CODE,s.DISCOUNT,"
//  		+ "s.GRAND_AMOUNT AS FEE_PAID from nimai_subscription_details s left join "
//  		+ "nimai_m_customer m  on s.userid=m.USERID left JOIN "
//  		+ "nimai_subscription_vas nvs ON nvs.userid=s.userid left JOIN nimai_m_discount nmd "
//  		+ "ON s.DISCOUNT_ID=nmd.DISCOUNT_ID  where s.INSERTED_DATE "
//  		+ "between :dateFrom and :dateTo GROUP BY s.invoice_id", nativeQuery = true)
//  List<Tuple> getPaymentSubReport(@Param("dateFrom") String dateFrom, @Param("dateTo") Date dateTo);
//  
  
  @Query(value = "SELECT s.SPL_SERIAL_NUMBER, m.userid,m.subscriber_type,m.company_name,\r\n" + 
  		" m.mobile_number,\r\n" + 
  		" m.landline,m.country_name,m.email_address,\r\n" + 
  		"m.first_name,m.last_name,s.PAYMENT_TXN_ID,s.INVOICE_ID \r\n" + 
  		"AS INVOICE_NUMBER,s.subscription_name,s.`STATUS` AS subscription_status,\r\n" + 
  		"nvs.pricing AS VAS_PRICING,nvs.`status` AS \r\n" + 
  		"vas_status,s.subscription_amount,\r\n" + 
  		"m.currency_code,m.mode_of_payment,s.inserted_date,\r\n" + 
  		" s.splan_start_date,s.splan_end_date, nmd.COUPON_CODE,s.DISCOUNT,\r\n" + 
  		" (s.SUBSCRIPTION_AMOUNT + s.VAS_AMOUNT - s.DISCOUNT) AS NET_FEE,\r\n" + 
  		" s.maker_comment,s.checker_comment,\r\n" + 
  		"s.PAYMENT_STATUS from nimai_subscription_details s left join\r\n" + 
  		"nimai_m_customer m  ON (s.userid=m.USERID) left JOIN \r\n" + 
  		"nimai_subscription_vas nvs ON (nvs.SPL_SERIAL_NUMBER=s.SPL_SERIAL_NUMBER)\r\n" + 
  		"left JOIN nimai_m_discount nmd\r\n" + 
  		"ON (s.DISCOUNT_ID=nmd.DISCOUNT_ID OR s.DISCOUNT_ID!=nmd.DISCOUNT_ID) where s.INSERTED_DATE BETWEEN :dateFrom AND :dateTo \r\n" + 
  		"GROUP BY s.invoice_id", nativeQuery = true)
	  List<Tuple> getPaymentSubReport(@Param("dateFrom") String dateFrom, @Param("dateTo") Date dateTo);
	  
  @Query(value = "SELECT d.DISCOUNT_ID,d.AMOUNT,d.CURRENCY FROM nimai_m_discount d WHERE d.DISCOUNT_ID= :discountid", nativeQuery = true)
  List<Tuple> getPaymentSubDiscount(@Param("discountid") String discountid);
  
  @Query(value = "select m.userid,m.subscriber_type,m.company_name,m.mobile_number,m.landline,m.country_name,m.email_address"
  		+ ",m.first_name,m.last_name,s.subscription_name,s.is_vas_applied,s.subscription_amount,"
  		+ "m.currency_code,m.mode_of_payment,s.inserted_date,s.splan_start_date,s.splan_end_date"
  		+ " from nimai_subscription_details s inner join nimai_m_customer m on s.userid=m.USERID "
  		+ "where s.userid= :userid and s.INSERTED_DATE between :dateFrom and :dateTo", nativeQuery = true)
  List<Tuple> b(@Param("dateFrom") String paramString1, @Param("dateTo") Date dateTo, @Param("userid") String userid);
  
  @Query(value = "select m.userid,m.subscriber_type,m.mobile_number,m.email_address,m.inserted_date,t.transaction_id,t.applicant_name,"
  		+ "t.applicant_country,t.bene_name,"
  		+ "t.bene_country,t.bene_contact_person,t.bene_contact_person_email,t.bene_bank_country,"
  		+ "t.bene_swift_code,t.bene_bank_name,"
  		+ "t.lc_issuance_bank,t.lc_issuance_branch,t.swift_code,t.lc_issuance_country,"
  		+ "t.requirement_type,"
  		+ "t.lc_value,t.lc_currency,t.lc_issuing_date,t.last_shipment_date,"
  		+ "t.negotiation_date,t.goods_type,t.usance_days,"
  		+ "t.start_date,t.confirmation_period,t.original_tenor_days,"
  		+ "t.refinancing_period,t.lc_maturity_date,t.last_bene_bank,"
  		+ "t.last_bene_swift_code,t.last_bank_country,t.loading_port,"
  		+ "t.loading_country,t.discharge_port,t.discharge_country,"
  		+ "t.charges_type,t.validity,t.quotation_received from nimai_m_customer m left join  "
  		+ "nimai_mm_transaction t on m.USERID=t.user_id group by m.USERID,t.transaction_id;", nativeQuery = true)
  List<Tuple> getCustomerTransactionReport();
  
  @Query(value = "SELECT * from nimai_mm_transaction nm where  nm.inserted_date >= (:fromDate) AND  "
  		+ "nm.inserted_date  <= (:toDate);", nativeQuery = true)
  List<Tuple> getCustomerTransactionReportByDates(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
  
//  @Query(value = "\n(select c.REGISTERED_COUNTRY AS REGISTERED_COUNTRY,\n\t(select count(m.subscriber_type)from nimai_m_customer m\n\twhere   m.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY    \n\tAND \n\t(m.SUBSCRIBER_TYPE='Customer' OR m.SUBSCRIBER_TYPE='Referrer' OR(m.SUBSCRIBER_TYPE='Bank' \n\tand m.BANK_TYPE='Customer')) and\n\tm.KYC_STATUS='Approved' AND (m.PAYMENT_STATUS='Approved' OR m.PAYMENT_STATUS='Success'))as total_customers,\n\t(select count(u.subscriber_type)from nimai_m_customer u\n\twhere u.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY AND u.SUBSCRIBER_TYPE='Bank' and\n\tu.BANK_TYPE='UNDERWRITER' and (u.PAYMENT_STATUS='Approved' OR u.PAYMENT_STATUS='Success')\n\tAND u.KYC_STATUS='Approved') \n\tas total_underwriter,\n\t(select count(t.transaction_id) from nimai_mm_transaction t  \n\twhere t.lc_issuance_country=c.REGISTERED_COUNTRY\t\n\t) as total_trxn,\n\t(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr \n\twhere tr.lc_issuance_country=c.REGISTERED_COUNTRY\n\t) as cumulative_lc_amount \n\tFROM \n\t     nimai_m_customer c where c.KYC_STATUS='Approved' AND (c.PAYMENT_STATUS='Approved' OR c.PAYMENT_STATUS='Success')  \n\tgroup by c.REGISTERED_COUNTRY order by c.REGISTERED_COUNTRY) \n\tUNION\n\t(SELECT nmtt.lc_issuance_country AS REGISTERED_COUNTRY,0,0,\n\t(select COUNT(t11.transaction_id) FROM nimai_mm_transaction t11 \n\tWHERE t11.lc_issuance_country=nmtt.lc_issuance_country) as total_trxn\n\t,\n\t(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr \n\twhere tr.lc_issuance_country=nmtt.lc_issuance_country) as cumulative_lc_amount\n\tFROM nimai_mm_transaction nmtt \n\twhere nmtt.lc_issuance_country NOT in\n\t(SELECT nmcc.registered_country FROM nimai_m_customer nmcc\n\twhere\n\tnmcc.KYC_STATUS='Approved' AND (nmcc.PAYMENT_STATUS='Approved' \n\tOR nmcc.PAYMENT_STATUS='Success'))  \n\t\n\t#WHERE nmc.REGISTERED_COUNTRY!=nmtt.lc_issuance_country\n\tGROUP BY nmtt.lc_issuance_country)\n\tORDER BY REGISTERED_COUNTRY", nativeQuery = true)
//  List<Tuple> getCountryDetailsAnalysis();

	@Query(value = "(select c.REGISTERED_COUNTRY AS REGISTERED_COUNTRY,\n" +
			"(select count(m.subscriber_type)from nimai_m_customer \n" +
			"m where m.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY AND (m.SUBSCRIBER_TYPE='Customer' OR m.SUBSCRIBER_TYPE='Referrer' \n" +
			"OR(m.SUBSCRIBER_TYPE='Bank' and m.BANK_TYPE='Customer')) and m.KYC_STATUS='Approved' AND (m.inserted_date BETWEEN :fromDate AND :toDate ) \n" +
			"AND (m.PAYMENT_STATUS='Approved' OR m.PAYMENT_STATUS='Success'))as total_customers,\n" +
			"(select count(u.subscriber_type)from nimai_m_customer u where u.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY \n" +
			"AND u.SUBSCRIBER_TYPE='Bank' and u.BANK_TYPE='UNDERWRITER' and (u.PAYMENT_STATUS='Approved' OR \n" +
			"u.PAYMENT_STATUS='Success') AND u.KYC_STATUS='Approved' AND (u.inserted_date BETWEEN :fromDate AND :toDate)) as total_underwriter,\n" +
			"(select count(t.transaction_id) \n" +
			"from nimai_mm_transaction t  where t.lc_issuance_country=c.REGISTERED_COUNTRY AND (t.inserted_date BETWEEN :fromDate AND :toDate)) as total_trxn,\n" +
			"(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr WHERE \n" +
			"tr.lc_issuance_country=c.REGISTERED_COUNTRY AND (tr.inserted_date BETWEEN :fromDate AND :toDate)) as cumulative_lc_amount FROM nimai_m_customer c WHERE \n" +
			"c.KYC_STATUS='Approved' AND (c.PAYMENT_STATUS='Approved' OR c.PAYMENT_STATUS='Success')  group BY \n" +
			"c.REGISTERED_COUNTRY order by c.REGISTERED_COUNTRY) \n" +
			" UNION \n" +
			"  (SELECT nmtt.lc_issuance_country AS \n" +
			"REGISTERED_COUNTRY,0,0,(select COUNT(t11.transaction_id) FROM nimai_mm_transaction t11 WHERE \n" +
			"t11.lc_issuance_country=nmtt.lc_issuance_country AND (t11.inserted_date BETWEEN :fromDate AND :toDate)) as total_trxn,\n" +
			"(select COALESCE(SUM(tr.usd_currency_value),0) \n" +
			"from nimai_mm_transaction tr where tr.lc_issuance_country=nmtt.lc_issuance_country AND (tr.inserted_date BETWEEN :fromDate AND :toDate) ) as cumulative_lc_amount \n" +
			"FROM nimai_mm_transaction nmtt where nmtt.lc_issuance_country NOT in(SELECT nmcc.registered_country \n" +
			"FROM nimai_m_customer nmcc where nmcc.KYC_STATUS='Approved' AND (nmcc.PAYMENT_STATUS='Approved' OR \n" +
			"nmcc.PAYMENT_STATUS='Success')) GROUP BY nmtt.lc_issuance_country) ORDER BY REGISTERED_COUNTRY",nativeQuery = true)
	List<Tuple> getCountryDetailsAnalysis(@Param("fromDate") java.sql.Date fromDate, @Param("toDate") java.sql.Date toDate);
  
//  @Query(value = "(select c.REGISTERED_COUNTRY AS REGISTERED_COUNTRY,\n\t(select count(m.subscriber_type)from nimai_m_customer m\n\twhere   m.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY    \n\tAND \n\t(m.SUBSCRIBER_TYPE='Customer' OR m.SUBSCRIBER_TYPE='Referrer' OR(m.SUBSCRIBER_TYPE='Bank' \n\tand m.BANK_TYPE='Customer')) and\n\tm.KYC_STATUS='Approved' AND (m.PAYMENT_STATUS='Approved' OR m.PAYMENT_STATUS='Success'))as total_customers,\n\t(select count(u.subscriber_type)from nimai_m_customer u\n\twhere u.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY AND u.SUBSCRIBER_TYPE='Bank' and\n\tu.BANK_TYPE='UNDERWRITER' and (u.PAYMENT_STATUS='Approved' OR u.PAYMENT_STATUS='Success')\n\tAND u.KYC_STATUS='Approved') \n\tas total_underwriter,\n\t(select count(t.transaction_id) from nimai_mm_transaction t  \n\twhere t.lc_issuance_country=c.REGISTERED_COUNTRY\t\n\t) as total_trxn,\n\t(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr \n\twhere tr.lc_issuance_country=c.REGISTERED_COUNTRY\n\t) as cumulative_lc_amount \n\tFROM \n\t     nimai_m_customer c where c.REGISTERED_COUNTRY IN (:userCountry)\n\tAND c.KYC_STATUS='Approved' AND (c.PAYMENT_STATUS='Approved' OR c.PAYMENT_STATUS='Success')  \n\tgroup by c.REGISTERED_COUNTRY order by c.REGISTERED_COUNTRY) \n\tUNION\n\t(SELECT nmtt.lc_issuance_country AS REGISTERED_COUNTRY,0,0,\n\t(select COUNT(t11.transaction_id) FROM nimai_mm_transaction t11 \n\tWHERE t11.lc_issuance_country=nmtt.lc_issuance_country) as total_trxn\n\t,\n\t(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr \n\twhere tr.lc_issuance_country=nmtt.lc_issuance_country) as cumulative_lc_amount\n\tFROM nimai_mm_transaction nmtt \n\twhere nmtt.lc_issuance_country NOT in\n\t(SELECT nmcc.registered_country FROM nimai_m_customer nmcc\n\twhere\n\tnmcc.KYC_STATUS='Approved' AND (nmcc.PAYMENT_STATUS='Approved' \n\tOR nmcc.PAYMENT_STATUS='Success'))  \n\t\n\t#WHERE nmc.REGISTERED_COUNTRY!=nmtt.lc_issuance_country\n\tGROUP BY nmtt.lc_issuance_country)\n\tORDER BY REGISTERED_COUNTRY", nativeQuery = true)
//  List<Tuple> getCountryFilteredDetails(@Param("userCountry") List<String> userCountry);
//  
//  @Query(value = "(select c.REGISTERED_COUNTRY AS REGISTERED_COUNTRY,\n\t(select count(m.subscriber_type)from nimai_m_customer m\n\twhere   m.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY    \n\tAND \n\t(m.SUBSCRIBER_TYPE='Customer' OR m.SUBSCRIBER_TYPE='Referrer' OR(m.SUBSCRIBER_TYPE='Bank' \n\tand m.BANK_TYPE='Customer')) and\n\tm.KYC_STATUS='Approved' AND (m.PAYMENT_STATUS='Approved' OR m.PAYMENT_STATUS='Success'))as total_customers,\n\t(select count(u.subscriber_type)from nimai_m_customer u\n\twhere u.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY AND u.SUBSCRIBER_TYPE='Bank' and\n\tu.BANK_TYPE='UNDERWRITER' and (u.PAYMENT_STATUS='Approved' OR u.PAYMENT_STATUS='Success')\n\tAND u.KYC_STATUS='Approved') \n\tas total_underwriter,\n\t(select count(t.transaction_id) from nimai_mm_transaction t  \n\twhere t.lc_issuance_country=c.REGISTERED_COUNTRY\t\n\t) as total_trxn,\n\t(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr \n\twhere tr.lc_issuance_country=c.REGISTERED_COUNTRY\n\t) as cumulative_lc_amount \n\tFROM \n\t     nimai_m_customer c where c.REGISTERED_COUNTRY IN (:userCountry) \n\tgroup by c.REGISTERED_COUNTRY order by c.REGISTERED_COUNTRY) \n\tUNION\n\t(SELECT nmtt.lc_issuance_country AS REGISTERED_COUNTRY,0,0,\n\t(select COUNT(t11.transaction_id) FROM nimai_mm_transaction t11 \n\tWHERE t11.lc_issuance_country=nmtt.lc_issuance_country) as total_trxn\n\t,\n\t(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr \n\twhere tr.lc_issuance_country=nmtt.lc_issuance_country) as cumulative_lc_amount\n\tFROM nimai_mm_transaction nmtt \n\twhere nmtt.lc_issuance_country NOT in\n\t(SELECT nmcc.registered_country FROM nimai_m_customer nmcc\n\twhere\n\tnmcc.KYC_STATUS='Approved' AND (nmcc.PAYMENT_STATUS='Approved' \n\tOR nmcc.PAYMENT_STATUS='Success'))  \n\t\n\t#WHERE nmc.REGISTERED_COUNTRY!=nmtt.lc_issuance_country\n\tGROUP BY nmtt.lc_issuance_country)\n\tORDER BY REGISTERED_COUNTRY", nativeQuery = true)
//  List<Tuple> getCountryFilteredDetails(@Param("userCountry") List<String> userCountry);

	@Query(value = "(select c.REGISTERED_COUNTRY AS REGISTERED_COUNTRY,\n" +
			"(select count(m.subscriber_type)from nimai_m_customer \n" +
			"m where m.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY AND (m.SUBSCRIBER_TYPE='Customer' OR m.SUBSCRIBER_TYPE='Referrer' \n" +
			"OR(m.SUBSCRIBER_TYPE='Bank' and m.BANK_TYPE='Customer')) and m.KYC_STATUS='Approved' AND (m.inserted_date BETWEEN :fromDate AND :toDate) \n" +
			"AND (m.PAYMENT_STATUS='Approved' OR m.PAYMENT_STATUS='Success'))as total_customers,\n" +
			"(select count(u.subscriber_type)from nimai_m_customer u where u.REGISTERED_COUNTRY=c.REGISTERED_COUNTRY \n" +
			"AND u.SUBSCRIBER_TYPE='Bank' and u.BANK_TYPE='UNDERWRITER' and (u.PAYMENT_STATUS='Approved' OR \n" +
			"u.PAYMENT_STATUS='Success') AND u.KYC_STATUS='Approved' AND (u.inserted_date BETWEEN :fromDate AND :toDate)) as total_underwriter,\n" +
			"(select count(t.transaction_id) \n" +
			"from nimai_mm_transaction t  where t.lc_issuance_country=c.REGISTERED_COUNTRY AND (t.inserted_date BETWEEN :fromDate AND :toDate)) as total_trxn,\n" +
			"(select COALESCE(SUM(tr.usd_currency_value),0) from nimai_mm_transaction tr WHERE \n" +
			"tr.lc_issuance_country=c.REGISTERED_COUNTRY AND (tr.inserted_date BETWEEN :fromDate AND :toDate)) as cumulative_lc_amount FROM nimai_m_customer c WHERE \n" +
			"c.KYC_STATUS='Approved' AND (c.PAYMENT_STATUS='Approved' OR c.PAYMENT_STATUS='Success') AND c.REGISTERED_COUNTRY IN (:userCountry)  group BY \n" +
			"c.REGISTERED_COUNTRY order by c.REGISTERED_COUNTRY) \n" +
			" UNION \n" +
			"  (SELECT nmtt.lc_issuance_country AS \n" +
			"REGISTERED_COUNTRY,0,0,(select COUNT(t11.transaction_id) FROM nimai_mm_transaction t11 WHERE \n" +
			"t11.lc_issuance_country=nmtt.lc_issuance_country AND (t11.inserted_date BETWEEN :fromDate AND :toDate)) as total_trxn,\n" +
			"(select COALESCE(SUM(tr.usd_currency_value),0) \n" +
			"from nimai_mm_transaction tr where tr.lc_issuance_country=nmtt.lc_issuance_country AND (tr.inserted_date BETWEEN :fromDate AND :toDate) ) as cumulative_lc_amount \n" +
			"FROM nimai_mm_transaction nmtt where nmtt.lc_issuance_country NOT in(SELECT nmcc.registered_country \n" +
			"FROM nimai_m_customer nmcc where nmcc.KYC_STATUS='Approved' AND (nmcc.PAYMENT_STATUS='Approved' OR \n" +
			"nmcc.PAYMENT_STATUS='Success')) GROUP BY nmtt.lc_issuance_country)ORDER BY REGISTERED_COUNTRY",nativeQuery = true)
	List<Tuple> getCountryFilteredDetails(@Param("userCountry") List<String> userCountry, @Param("fromDate") java.sql.Date fromDate, @Param("toDate") java.sql.Date toDate);
  
 
  @Procedure(name = "dashboardCount", outputParameterName = "result")
  int getDashboardCount(@Param("query_no") int queryNo, @Param("subscriberType") String subscriberType, @Param("bankType") String bankType, @Param("status") String status, @Param("exp_day") Integer expday, @Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo, @Param("cases") String cases, @Param("countryNames") String countryNames);
  
  @Query(value = "select MONTHNAME(m.INSERTED_DATE) as month,count(m.userid) as customers,"
  		+ "COALESCE((select COALESCE(sum(su.subscription_amount),0) "
  		+ "from nimai_subscription_details su inner join nimai_m_customer mn "
  		+ "  on su.userid=mn.USERID where mn.SUBSCRIBER_TYPE=m.SUBSCRIBER_TYPE "
  		+ " and MONTHNAME(mn.INSERTED_DATE)=MONTHNAME(m.INSERTED_DATE))/(select count(c.userid) "
  		+ "from nimai_m_customer c where c.is_splanpurchased='1' "
  		+ "  and c.SUBSCRIBER_TYPE=m.SUBSCRIBER_TYPE"
  		+ "  and MONTHNAME(c.INSERTED_DATE)=MONTHNAME(m.INSERTED_DATE)),0) "
  		+ "as subscription_rate"
  		+ " from nimai_m_customer m where YEAR(m.INSERTED_DATE)= :year "
  		+ "and m.subscriber_type='Customer' "
  		+ "group by MONTHNAME(m.INSERTED_DATE) "
  		+ "ORDER BY DATE_FORMAT(m.INSERTED_DATE,\"%m-%d\") asc; ", nativeQuery = true)
  List<Tuple> getDashboardUserStat(@Param("year") String year);
  
  @Query(value = "select MONTHNAME(m.INSERTED_DATE) as month,count(m.userid) as customers,"
  		+ "COALESCE((select sum(su.subscription_amount) from nimai_subscription_details su "
  		+ "inner join nimai_m_customer mn "
  		+ "on su.userid=mn.USERID where  mn.SUBSCRIBER_TYPE=m.SUBSCRIBER_TYPE "
  		+ "and MONTHNAME(mn.INSERTED_DATE)=MONTHNAME(m.INSERTED_DATE))/(select count(c.userid) "
  		+ "from nimai_m_customer c where c.is_splanpurchased='1' and "
  		+ "c.SUBSCRIBER_TYPE=m.SUBSCRIBER_TYPE "
  		+ "and MONTHNAME(c.INSERTED_DATE)=MONTHNAME(m.INSERTED_DATE)),0) as "
  		+ "subscription_rate "
  		+ "from nimai_m_customer m where YEAR(m.INSERTED_DATE)= :year and "
  		+ "m.subscriber_type='Customer' "
  		+ "and FIND_IN_SET(m.country_name,:userCountry) "
  		+ "group by MONTHNAME(m.INSERTED_DATE) "
  		+ "ORDER BY DATE_FORMAT(m.INSERTED_DATE, \"%m-%d\") asc; ", nativeQuery = true)
  List<Tuple> getDashboardCountryrUserStat(@Param("year") String year, @Param("userCountry") String userCountry);
  
  @Query(value = " select MONTHNAME(m.INSERTED_DATE) as month,count(m.userid) as customers,"
  		+ "COALESCE((select sum(su.subscription_amount) from "
  		+ "nimai_subscription_details su inner join nimai_m_customer mn "
  		+ "on su.userid=mn.USERID where  mn.SUBSCRIBER_TYPE=m.subscriber_type and "
  		+ "mn.BANK_TYPE=m.bank_type and MONTHNAME(mn.INSERTED_DATE) "
  		+ "like MONTHNAME(m.INSERTED_DATE) )/(select count(c.userid) "
  		+ "from nimai_m_customer c where c.is_splanpurchased='1' and c.SUBSCRIBER_TYPE=m.SUBSCRIBER_TYPE "
  		+ "and c.BANK_TYPE= 'Customer' and MONTHNAME(c.INSERTED_DATE)=MONTHNAME(m.INSERTED_DATE)),0) "
  		+ "as subscription_rate from nimai_m_customer m where YEAR(m.INSERTED_DATE)= :year "
  		+ "and m.subscriber_type='Bank' and m.bank_type=:bankType group by MONTHNAME(m.INSERTED_DATE)"
  		+ "ORDER BY DATE_FORMAT(m.INSERTED_DATE,\"%m-%d\") asc;", nativeQuery = true)
  List<Tuple> getDashboardBankStat(@Param("year") String year, @Param("bankType") String bankType);
  
  @Query(value = "select MONTHNAME(m.INSERTED_DATE) as month,count(m.userid) as customers,"
  		+ "COALESCE((select sum(su.subscription_amount) from nimai_subscription_details su "
  		+ "inner join nimai_m_customer mn on su.userid=mn.USERID where  "
  		+ "mn.SUBSCRIBER_TYPE=m.subscriber_type and mn.BANK_TYPE=m.bank_type and "
  		+ "MONTHNAME(mn.INSERTED_DATE) like MONTHNAME(m.INSERTED_DATE) )/(select count(c.userid) "
  		+ "from nimai_m_customer c where c.is_splanpurchased='1' and "
  		+ "c.SUBSCRIBER_TYPE=m.SUBSCRIBER_TYPE and c.BANK_TYPE= m.bank_type "
  		+ "and MONTHNAME(c.INSERTED_DATE)=MONTHNAME(m.INSERTED_DATE)),0) "
  		+ "as subscription_rate\r\nfrom nimai_m_customer m where YEAR(m.INSERTED_DATE)=:year "
  		+ "and m.subscriber_type='Bank' and m.bank_type= :bankType "
  		+ "and FIND_IN_SET(m.country_name, :userCountry)"
  		+ " group by MONTHNAME(m.INSERTED_DATE) ORDER BY "
  		+ "DATE_FORMAT(m.INSERTED_DATE,\"%m-%d\") asc; ", nativeQuery = true)
  List<Tuple> getDashboardBankCountryStat(@Param("year") String year, @Param("bankType") String bankType, @Param("userCountry") String userCountry);
  
  @Query("SELECT userid FROM NimaiMCustomer where lower(userid) like %:userid% and countryName IN (:names)")
  List<String> userIdSearchByCountry(@Param("userid") String userid, @Param("names") List<String> names);
  
  @Query("SELECT emailAddress FROM NimaiMCustomer where lower(emailAddress) like %:emailAddress% and countryName IN (:names)")
  List<String> emailIdSearchByCountry(@Param("emailAddress") String emailAddress, @Param("names") List<String> names);
  
  @Query("SELECT mobileNumber FROM NimaiMCustomer where lower(mobileNumber) like %:mobileNumber% and countryName IN (:names)")
  List<String> mobileNumberSearchByCountry(@Param("mobileNumber") String mobileNumber, @Param("names") List<String> names);
  
  @Query("SELECT companyName FROM NimaiMCustomer where lower(companyName) like %:companyName% and countryName IN (:names)")
  List<String> companyNameSearchByCountry(@Param("companyName") String companyName, @Param("names") List<String> names);
  
  @Query("SELECT userid FROM NimaiMCustomer where lower(userid) like %:userid% and userid like %:data% and countryName IN (:names)")
  List<String> userIdDataSearchByCountry(@Param("userid") String userid, @Param("data") String data, @Param("names") List<String> names);
  
  @Query("SELECT emailAddress FROM NimaiMCustomer where lower(emailAddress) like %:emailAddress% and countryName IN (:names)")
  List<String> emailIdDataSearchByCountry(@Param("emailAddress") String emailAddress, @Param("names") List<String> names);
  
  @Query("SELECT mobileNumber FROM NimaiMCustomer where lower(mobileNumber) like %:mobileNumber%  and countryName IN (:names)")
  List<String> mobileNumberDataSearchByCountry(@Param("mobileNumber") String mobileNumber, @Param("names") List<String> names);
  
  @Query("SELECT companyName FROM NimaiMCustomer where lower(companyName) like %:companyName% and countryName IN (:names)")
  List<String> companyNameDataSearchByCountry(@Param("companyName") String companyName, @Param("names") List<String> names);
  
  @Query("SELECT bankName FROM NimaiMCustomer where lower(bankName) like %:bankName% and countryName IN (:names)")
  List<String> bankNameDataSearchByCountry(@Param("bankName") String bankName, @Param("names") List<String> names);
  
  @Query("FROM NimaiMCustomer where accountSource =:userid and accountType = 'SUBSIDIARY' ")
  List<NimaiMCustomer> findSubsidiaryByUserId(@Param("userid") String userid);
  
  @Query(value = "select nl.COUNTRY_NAME from nimai_lookup_countries nl", nativeQuery = true)
  List<String> getCountryList();
  
  @Query(value = "select nl.country from nimai_m_currency nl", nativeQuery = true)
  List<String> getRoleCountryList();
  
  
  
  
  
  //=========================================
  
  @Query("SELECT userid FROM NimaiMCustomer where lower(userid) "
  		+ "like %:userid% and (userid like 'CU%' or userid like 'BC%')")
  List<String> userIdSearchForCustomer(@Param("userid") String userid);
  
  @Query("SELECT userid FROM NimaiMCustomer where lower(userid) "
	  		+ "like %:userid% and ( userid like 'BA%')")
	  List<String> userIdSearchForBaU(@Param("userid") String userid);
  
  @Query(value = "select c.country_name,(select count(m.subscriber_type)from nimai_m_customer m where m.country_name=c.country_name)-((select count(u.subscriber_type)from nimai_m_customer u where u.country_name=c.country_name and u.BANK_TYPE='UNDERWRITER'))-(select count(n.subscriber_type)from nimai_m_customer n where n.country_name=c.country_name and n.SUBSCRIBER_TYPE='Referrer')as total_customers,\r\n(select count(u.subscriber_type)from nimai_m_customer u where u.country_name=c.country_name and u.BANK_TYPE='UNDERWRITER') as total_underwriter,\r\n(select count(t.transaction_id) from nimai_mm_transaction t where t.lc_issuance_country=c.country_name) as total_trxn,\r\n(select COALESCE(SUM(tr.lc_value),0) from nimai_mm_transaction tr where tr.lc_issuance_country=c.country_name) as cumulative_lc_amount\r\nfrom nimai_m_customer c group by c.country_name order by c.country_name limit :limitRow offset :offsetData", nativeQuery = true)
  List<Tuple> getCountryDetailsAnalysis(@Param("limitRow") int limitRow, @Param("offsetData") int offsetData);
  
  @Query(value = "select c.country_name,(select count(m.subscriber_type)from nimai_m_customer m where m.country_name=c.country_name)-((select count(u.subscriber_type)from nimai_m_customer u where u.country_name=c.country_name and u.BANK_TYPE='UNDERWRITER'))-(select count(n.subscriber_type)from nimai_m_customer n where n.country_name=c.country_name and n.SUBSCRIBER_TYPE='Referrer')as total_customers,\r\n\t\t\t(select count(u.subscriber_type)from nimai_m_customer u where u.country_name=c.country_name and u.BANK_TYPE='UNDERWRITER') as total_underwriter,\r\n\t\t\t(select count(t.transaction_id) from nimai_mm_transaction t where t.lc_issuance_country=c.country_name) as total_trxn,\r\n\t\t\t(select COALESCE(SUM(tr.lc_value),0) from nimai_mm_transaction tr where tr.lc_issuance_country=c.country_name) as cumulative_lc_amount\r\n\t\t\tfrom nimai_m_customer c where FIND_IN_SET(c.country_name, :userCountry) group by c.country_name order by c.country_name limit :limitRow offset :offsetData", nativeQuery = true)
  List<Tuple> getCountryFilteredDetails(@Param("userCountry") String paramString, @Param("limitRow") int paramInt1, @Param("offsetData") int paramInt2);
  
  @Query("FROM NimaiMCustomer where accountSource =:userid and accountType = 'BANKUSER' ")
  List<NimaiMCustomer> findAdditionalUserByUserId(@Param("userid") String userid);
  
  @Query(value = "SELECT * from nimai_mm_transaction nm where    "
  		+ "nm.inserted_date >= (:fromDate) AND     "
  		+ " nm.inserted_date   <= (:toDate) AND nm.user_id=(:userId);", nativeQuery = true)
  List<NimaiMmTransaction> findByUsrIdDates(@Param("userId") String userId, @Param("fromDate") String fromDate, @Param("toDate") String toDate);
  
  @Query(value = "SELECT * from nimai_mm_transaction nm where  "
  		+ "nm.inserted_date >= (:fromDate) AND\n       "
  		+ " nm.inserted_date   <= (:toDate)", nativeQuery = true)
  List<NimaiMmTransaction> getcuTrDetailsByDates(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
  
  @Query(value = "SELECT * from nimai_subscription_details nm where   "
  		+ " nm.inserted_date >= (:fromDate) AND       "
  		+ "nm.inserted_date   <= (:toDate);", nativeQuery = true)
  List<NimaiSubscriptionDetails> getCustomerDetails(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
  
  @Query(value = "from NimaiSubscriptionDetails nm where "
  		+ "nm.inserted_date >= (:fromDate) AND   "
  		+ "  nm.inserted_date   <= (:toDate);", nativeQuery = true)
  List<NimaiSubscriptionDetails> getCustomerDetail(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
  
  @Query(value = "from nimai_subscription_details nm where             "
  		+ "nm.inserted_date >= (:fromDate) AND      "
  		+ " nm.inserted_date   <= (:toDate) AND nm.userid=(:userId)", nativeQuery = true)
  List<NimaiSubscriptionDetails> getCustomerDetailsByUserId(@Param("fromDate") String fromDate, @Param("toDate") String toDate, @Param("userId") String userId);
  
  @Query(value = "SELECT * from nimai_m_customer m where\n           "
  		+ " m.INSERTED_DATE >= (:fromDate) AND     "
  		+ " m.INSERTED_DATE   <= (:toDate) AND "
  		+ "  m.ACCOUNT_SOURCE!='WEBSITE' ;", nativeQuery = true)
  List<NimaiMCustomer> findByDates(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
  
  
//======================New ReferrenceCode
  
  @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
	  		+ "nimai_m_customer nc  "
	  		+ "WHERE nc.USERID=:userId AND nc.KYC_STATUS='Approved' ", nativeQuery = true)
	  int getApprovedReferrence(@Param("userId") String userId);
	  
	  @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc  "
		  		+ "WHERE nc.account_source=:userId AND nc.KYC_STATUS='Approved' ", nativeQuery = true)
		  int getApprovedReferrenceNew(@Param("userId") String userId);
	  
	  @Query(value ="SELECT  COUNT(*) AS totalReferences FROM nimai_m_customer nc \r\n" + 
	  		"WHERE nc.USERID=:userid", nativeQuery = true)
	  int getTotareference(@Param("userid") String userid);
	  
	  @Query(value ="SELECT  COUNT(*) AS totalReferences FROM nimai_m_customer nc \r\n" + 
		  		"WHERE nc.account_source=:userid", nativeQuery = true)
		  int getTotareferenceNew(@Param("userid") String userid);
	  
	  @Query(value ="SELECT COUNT(*) AS totalReferences FROM referral_leads nc \r\n" + 
		  		"WHERE nc.refer_by=:referid", nativeQuery = true)
		  int getTotareferenceForRxilFieoReferrer(@Param("referid") String referid);
	  
	  @Query(value = "\r\nSELECT COUNT(nc.USERID) AS approvedReferrence FROM "
	  		+ "nimai_m_customer nc "
	  		+ "WHERE nc.USERID=:userid AND "
	  		+ "nc.KYC_STATUS='Pending' ", nativeQuery = true)
	  int getpendingReference(@Param("userid") String userid);
	  
	  @Query(value = "\r\nSELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc "
		  		+ "WHERE nc.account_source=:userid AND "
		  		+ "nc.KYC_STATUS='Pending' ", nativeQuery = true)
		  int getpendingReferenceNew(@Param("userid") String userid);
	  
	  @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
	  		+ "nimai_m_customer nc "
	  		+ "WHERE nc.USERID=:userid AND nc.KYC_STATUS='Rejected' ", nativeQuery = true)
	  int getRejectedReference(@Param("userid") String userid);
	  
	  @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc "
		  		+ "WHERE nc.account_source=:userid AND nc.KYC_STATUS='Rejected' ", nativeQuery = true)
		  int getRejectedReferenceNew(@Param("userid") String userid);
	  
  
  @Query(value = "SELECT SUM(a.amount) AS earn FROM (SELECT nc.USERID,CASE WHEN nd.SUBSCRIPTION_NAME='POSTPAID_PLAN' \n" +
		  "THEN psd.total_payment ELSE nd.GRAND_AMOUNT END AS amount,\n" +
		  "CASE WHEN nd.SUBSCRIPTION_NAME='POSTPAID_PLAN' THEN psd.invoice_id ELSE nd.INVOICE_ID END AS invoice FROM\n" +
		  " nimai_m_customer nc INNER JOIN nimai_subscription_details nd ON nd.userid=nc.USERID \n" +
		  " left  JOIN\tnimai_postpaid_subscription_details psd ON psd.subscription_details_id=nd.SPL_SERIAL_NUMBER  \n" +
		  " WHERE (nc.ACCOUNT_SOURCE=:userid OR nc.ACCOUNT_SOURCE IN(SELECT nc.USERID FROM nimai_m_customer nc \n" +
		  " WHERE nc.ACCOUNT_SOURCE=:userid))AND nc.RM_ID=:rmId AND nc.RM_STATUS='Active' AND \n" +
		  "  (psd.PAYMENT_STATUS='Approved' OR psd.PAYMENT_STATUS IS NULL) AND (nd.PAYMENT_STATUS='Approved' OR psd.PAYMENT_STATUS='Approved')\n" +
		  "  GROUP BY invoice) a", nativeQuery = true)
  Double getEarningByCustRm(@Param("userid") String userid,@Param("rmId")String rmId);

	@Query(value = "SELECT SUM(a.amount) AS earn FROM (SELECT nc.USERID,CASE WHEN nd.SUBSCRIPTION_NAME='POSTPAID_PLAN' \n" +
			"THEN psd.total_payment ELSE nd.GRAND_AMOUNT END AS amount,\n" +
			"CASE WHEN nd.SUBSCRIPTION_NAME='POSTPAID_PLAN' THEN psd.invoice_id ELSE nd.INVOICE_ID END AS invoice FROM\n" +
			" nimai_m_customer nc INNER JOIN nimai_subscription_details nd ON nd.userid=nc.USERID \n" +
			" left  JOIN\tnimai_postpaid_subscription_details psd ON psd.subscription_details_id=nd.SPL_SERIAL_NUMBER  \n" +
			" WHERE (nc.ACCOUNT_SOURCE=:userid OR nc.ACCOUNT_SOURCE IN(SELECT nc.USERID FROM nimai_m_customer nc \n" +
			" WHERE nc.ACCOUNT_SOURCE=:userid))AND \n" +
			"  (psd.PAYMENT_STATUS='Approved' OR psd.PAYMENT_STATUS IS NULL) AND (nd.PAYMENT_STATUS='Approved' OR psd.PAYMENT_STATUS='Approved')\n" +
			"  GROUP BY invoice) a", nativeQuery = true)
	Double getEarning(@Param("userid") String userid);

//	@Query(value = " SELECT (nd.SUBSCRIPTION_AMOUNT+nd.VAS_AMOUNT)-(nd.DISCOUNT) "
//			+ "AS Earning FROM nimai_m_customer nc INNER JOIN nimai_subscription_details nd "
//			+ "ON nd.userid=nc.USERID   WHERE nc.USERID=(:userid);", nativeQuery = true)
//	Double getEarningForSingleRefer(@Param("userid") String userid);

	@Query(value = "SELECT sum(amount) AS earn FROM ( SELECT nc.USERID,CASE WHEN nd.SUBSCRIPTION_NAME='POSTPAID_PLAN' \n" +
			"THEN psd.total_payment ELSE nd.GRAND_AMOUNT END AS amount,psd.invoice_id FROM\n" +
			" nimai_m_customer nc INNER JOIN nimai_subscription_details nd ON nd.userid=nc.USERID \n" +
			" left  JOIN\tnimai_postpaid_subscription_details psd ON psd.subscription_details_id=nd.SPL_SERIAL_NUMBER\n" +
			" WHERE nc.USERID IN(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.USERID=:userid OR\tnc.ACCOUNT_SOURCE=:userid)\n" +
			" AND nd.`STATUS`='Active' AND \n" +
			" (psd.PAYMENT_STATUS='Approved' OR psd.PAYMENT_STATUS IS NULL) AND (nd.PAYMENT_STATUS='Approved' OR psd.PAYMENT_STATUS='Approved') \n" +
			" GROUP BY psd.invoice_id) AS a", nativeQuery = true)
	Double getEarningForSingleRefer(@Param("userid") String userid);
  
  @Query(value = "SELECT * from nimai_m_customer nc INNER JOIN nimai_subscription_details nd "
  		+ "ON nc.USERID=nd.userid  where nc.REGISTERED_COUNTRY In(:country) "
  		+ "and nc.MODE_OF_PAYMENT='Wire' and nc.SUBSCRIBER_TYPE!='REFERRER'  "
  		+ "and nc.PAYMENT_STATUS='Maker Approved'\tand nd.`STATUS`='ACTIVE' AND "
  		+ "nc.USERID=nd.userid AND nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN'", countQuery = "SELECT count(*) from nimai_m_customer"
  				+ " nc INNER JOIN nimai_subscription_details nd  "
  				+ " ON nc.USERID=nd.userid   where nc.REGISTERED_COUNTRY In(:country)  "
  				+ "and nc.MODE_OF_PAYMENT='Wire' and nc.SUBSCRIBER_TYPE!='REFERRER'  "
  				+ "and nc.PAYMENT_STATUS='Maker Approved'"
  				+ "and nd.`STATUS`='ACTIVE' AND nc.USERID=nd.userid AND nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN'", nativeQuery = true)
  Page<NimaiMCustomer> getListByCountryname(@Param("country") String country, Pageable paramPageable);

	@Query(value = "SELECT * from nimai_m_customer nc INNER JOIN nimai_subscription_details nd "
			+ "ON nc.USERID=nd.userid  where nc.REGISTERED_COUNTRY In(:country) "
			+ "and nc.MODE_OF_PAYMENT='Wire' and nc.SUBSCRIBER_TYPE!='REFERRER'  "
			+ "and nc.PAYMENT_STATUS='Maker Approved'\tand nd.`STATUS`='ACTIVE' AND "
			+ "nc.USERID=nd.userid AND nc.ACCOUNT_TYPE='MASTER' ", countQuery = "SELECT count(*) from nimai_m_customer"
			+ " nc INNER JOIN nimai_subscription_details nd  "
			+ " ON nc.USERID=nd.userid   where nc.REGISTERED_COUNTRY In(:country)  "
			+ "and nc.MODE_OF_PAYMENT='Wire' and nc.SUBSCRIBER_TYPE!='REFERRER'  "
			+ "and nc.PAYMENT_STATUS='Maker Approved'"
			+ "and nd.`STATUS`='ACTIVE' AND nc.USERID=nd.userid AND nc.ACCOUNT_TYPE='MASTER'", nativeQuery = true)
	Page<NimaiMCustomer> getListByCountrynameForPostPaid(@Param("country") String country, Pageable paramPageable);

  
  @Query(value = "SELECT * from nimai_m_customer nc INNER JOIN nimai_subscription_details nd "
	  		+ "ON nc.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds"
	  		+ " ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id"
	  		+ "  where nc.REGISTERED_COUNTRY In(:country) "
	  		+ "and nds.PAYMENT_MODE='Wire' and nc.SUBSCRIBER_TYPE!='REFERRER'  "
	  		+ "and nds.PAYMENT_STATUS='Maker Approved' \tand nds.`status`='ACTIVE' AND "
	  		+ "nc.USERID=nd.userid AND (nc.ACCOUNT_TYPE='MASTER' or k.ACCOUNT_TYPE='SUBSIDIARY' or k.ACCOUNT_TYPE='REFER')", countQuery = "SELECT count(*) from nimai_m_customer"
	  				+ " nc INNER JOIN nimai_subscription_details nd  "
	  				+ " ON nc.USERID=nd.userid   "
	  				+ " inner JOIN nimai_postpaid_subscription_details nds "
	  				+ " ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id "
	  				+ " where nc.REGISTERED_COUNTRY In(:country)  "
	  				+ " and nds.PAYMENT_MODE='Wire' and nc.SUBSCRIBER_TYPE!='REFERRER'  "
	  				+ " and nds.PAYMENT_STATUS='Maker Approved'"
	  				+ " and nds.`status`='ACTIVE' AND nc.USERID=nd.userid AND (nc.ACCOUNT_TYPE='MASTER' or k.ACCOUNT_TYPE='SUBSIDIARY' or k.ACCOUNT_TYPE='REFER')", nativeQuery = true)
	  Page<NimaiMCustomer> getPoListByCountryname(@Param("country") String country, Pageable paramPageable);
	  
  
  
  
  
  
  
  

  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid  where k.PAYMENT_STATUS='Maker Approved' and k.MODE_OF_PAYMENT='Wire' and k.SUBSCRIBER_TYPE!='REFERRER' and nd.`STATUS`='ACTIVE' and k.REGISTERED_COUNTRY IN (:countries) AND nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN' ", nativeQuery = true)
  Page<NimaiMCustomer> findMakerApprovedPaymentDetails(@Param("countries") List<String> countries, Pageable paramPageable);
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid where k.PAYMENT_STATUS='Maker Approved' and k.MODE_OF_PAYMENT='Wire' and nd.`STATUS`='ACTIVE' and k.SUBSCRIBER_TYPE!='REFERRER' AND nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN'  ", nativeQuery = true)
  Page<NimaiMCustomer> findAllMakerApprovedPaymentDetails(Pageable paramPageable);
//  
//  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and  k.SUBSCRIBER_TYPE!='REFERRER' ", nativeQuery = true)
// Page<NimaiMCustomer> findAllMakerApprovedPoPaymentDetails( Pageable paramPageable);
// 
//  
  @Query(value = "select * FROM nimai_m_customer k INNER join  nimai_postpaid_subscription_details nds \r\n" +
  		"	 ON k.USERID=nds.userid where \r\n" +
  		"	 nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and \r\n" +
  		"	 nds.`status`='ACTIVE' AND (nds.Due_Type='minDue' OR nds.Due_Type='totalDue') AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER' or k.ACCOUNT_TYPE='SUBSIDIARY')  GROUP BY nds.userid ",
		  countQuery = "select count(*) FROM nimai_m_customer k INNER join nimai_postpaid_subscription_details nds \r\n"

		  + " ON k.USERID=nds.userid where"

		  + " nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and "

		  + " nds.`status`='ACTIVE' AND (nds.Due_Type='minDue' OR nds.Due_Type='totalDue') AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER' or k.ACCOUNT_TYPE='SUBSIDIARY') GROUP BY nds.userid ", nativeQuery = true)
 Page<NimaiMCustomer> findAllMakerApprovedPoPaymentDetails( Pageable paramPageable);
  
  
//  @Query(value = "select * FROM nimai_postpaid_subscription_details nds \r\n" + 
//  		"	  where \r\n" + 
//  		"	 nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and \r\n" + 
//  		"	 nds.`status`='ACTIVE' AND nds.Due_Type='minDue' OR nds.Due_Type='totalDue' ", nativeQuery = true)
//  Page<NimaiPostpaidSubscriptionDetails> findAllMakerApprovedPoPaymentDetail( Pageable paramPageable);
  
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and  k.SUBSCRIBER_TYPE!='REFERRER' and k.REGISTERED_COUNTRY IN (:countries) AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER' or k.ACCOUNT_TYPE='SUBSIDIARY') ", nativeQuery = true)
 Page<NimaiMCustomer> findMakerApprovedPoPaymentDetails(@Param("countries") List<String> countries, Pageable paramPageable);
 
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id  where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.BANK_TYPE=:bankType  and k.REGISTERED_COUNTRY IN (:countries) AND nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN'", nativeQuery = true)
  Page<NimaiMCustomer> findMakerApprovedPaymentDetailsSubsTypeBankType(String subsType, String bankType, @Param("countries") List<String> countries, Pageable paramPageable);

	@Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id  where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.BANK_TYPE=:bankType  and k.REGISTERED_COUNTRY IN (:countries) AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER')", nativeQuery = true)
	Page<NimaiMCustomer> findMakerApprovedPaymentDetailsSubsTypeBankTypeForPostPaid(String subsType, String bankType, @Param("countries") List<String> countries, Pageable paramPageable);


	@Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.REGISTERED_COUNTRY IN (:countries) AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER' or k.ACCOUNT_TYPE='SUBSIDIARY')", nativeQuery = true)
  Page<NimaiMCustomer> findMakerApprovedPaymentDetailsPoSubsTypeBankType(String subsType,@Param("countries") List<String> value, Pageable paramPageable);
  
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid where k.PAYMENT_STATUS='Maker Approved' and k.MODE_OF_PAYMENT='Wire' and nd.`STATUS`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.BANK_TYPE=:bankType and k.REGISTERED_COUNTRY IN (:countries)", nativeQuery = true)
  Page<NimaiMCustomer> findMakerApprovedPoPaymentDetailsSubsTypeBankType(String subsType, String bankType,@Param("countries") List<String> countries, Pageable paramPageable);
  
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid  where k.PAYMENT_STATUS='Maker Approved' and k.MODE_OF_PAYMENT='Wire' and k.SUBSCRIBER_TYPE=:subsType and nd.`STATUS`='ACTIVE' and k.REGISTERED_COUNTRY IN (:countries) AND nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN' ", nativeQuery = true)
  Page<NimaiMCustomer> findMakerApprovedPaymentDetailsSubsType(String subsType, @Param("countries") List<String> value, Pageable paramPageable);
  
  
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.REGISTERED_COUNTRY IN (:countries) AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER')", nativeQuery = true)
  Page<NimaiMCustomer> findMakerApprovedPoPaymentDetailsSubsType(String subsType,@Param("countries") List<String> value, Pageable paramPageable);
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid where k.PAYMENT_STATUS='Maker Approved' and k.MODE_OF_PAYMENT='Wire' and nd.`STATUS`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.BANK_TYPE=:bankType and nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN'", nativeQuery = true)
  Page<NimaiMCustomer> findAllMakerApprovedPaymentDetailsSubsTypeBankType(String subsType, String bankType, Pageable paramPageable);


  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid where k.PAYMENT_STATUS='Maker Approved' and k.MODE_OF_PAYMENT='Wire' and nd.`STATUS`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.BANK_TYPE=:bankType AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER')", nativeQuery = true)
  Page<NimaiMCustomer> findAllMakerApprovedPaymentDetailsSubsTypeBankTypeForPostPaid(String subsType, String bankType, Pageable paramPageable);


  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.BANK_TYPE=:bankType AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER' or k.ACCOUNT_TYPE='SUBSIDIARY')", nativeQuery = true)
  Page<NimaiMCustomer> findAllMakerApprovedPoPaymentDetailsSubsTypeBankType(String subsType, String bankType, Pageable paramPageable);
  
  
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid where k.PAYMENT_STATUS='Maker Approved' and k.MODE_OF_PAYMENT='Wire' and nd.`STATUS`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and nd.SUBSCRIPTION_NAME!='POSTPAID_PLAN'", nativeQuery = true)
  Page<NimaiMCustomer> findAllMakerApprovedPaymentDetailsSubsType(String subsType, Pageable paramPageable);
  
  
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType AND (k.ACCOUNT_TYPE='MASTER' OR k.ACCOUNT_TYPE='REFER' or k.ACCOUNT_TYPE='SUBSIDIARY')", nativeQuery = true)
  Page<NimaiMCustomer> findAllMakerApprovedPostPaidPaymentDetailsSubsType(String subsType, Pageable paramPageable);
  
  @Query(value = "select * from nimai_m_customer k INNER JOIN nimai_subscription_details nd ON k.USERID=nd.userid inner JOIN nimai_postpaid_subscription_details nds ON nd.SPL_SERIAL_NUMBER=nds.subscription_details_id where nds.PAYMENT_STATUS='Maker Approved' and nds.PAYMENT_MODE='Wire' and nds.`status`='ACTIVE' and k.SUBSCRIBER_TYPE=:subsType and k.BANK_TYPE=:bankType", nativeQuery = true)
  Page<NimaiMCustomer> findAllMakerApprovedPostPaidPaymentDetailsBankType(String subsType, Pageable paramPageable);
  
  
  @Query(value = "\tselect distinct count(t.transaction_id)  from nimai_mm_transaction t inner join nimai_m_customer c \non c.USERID=t.user_id\nwhere  t.transaction_status='Expired' and t.validity between DATE_SUB(NOW(), INTERVAL 7 DAY) and NOW() \nAND c.REGISTERED_COUNTRY IN (:value)\nand (c.SUBSCRIBER_TYPE='Customer' or c.BANK_TYPE='Customer');", nativeQuery = true)
  int getDashboardCountByCountryWise(List<String> value);
  
  @Query(value = "select distinct count(t.transaction_id)  from nimai_mm_transaction t inner join nimai_m_customer c \non c.USERID=t.user_id\nwhere  t.transaction_status='Rejected' and t.inserted_date between DATE_SUB(NOW(), INTERVAL 7 DAY) and NOW() \nAND c.REGISTERED_COUNTRY IN (:value)\nand (c.SUBSCRIBER_TYPE='Customer' or c.BANK_TYPE='Customer');", nativeQuery = true)
  int getDashboardRejectedCountByCountryWise(List<String> value);
  
  @Query("from NimaiMCustomer nc where nc.accountSource=:userid")
  List<NimaiMCustomer> findReferListByReferrerUsrId(@Param("userid") String userid);
  
  @Query(value = "select * from nimai_m_customer where email_address=(:emailAddress) and account_type='Refer'", nativeQuery = true)
  NimaiMCustomer getUserIdByEmailId(@Param("emailAddress") String emailAddress);
  
  @Query(value = "select * from nimai_m_customer where email_address=(:emailAddress) ", nativeQuery = true)
  NimaiMCustomer getUserByEmailId(@Param("emailAddress") String emailAddress);
  
  
  @Query(value = "select * from nimai_m_customer where MOBILE_NUMBER=(:mobileNUmber) ", nativeQuery = true)
  NimaiMCustomer getUserByMobileNumber(@Param("mobileNUmber") String mobileNUmber);
  
  
  @Query(value = "select * from nimai_m_customer where BANK_NAME=:compnyName ", nativeQuery = true)
  NimaiMCustomer getUserByCompanyName(@Param("compnyName") String compnyName);
  
  
  
  @Query(value = "SELECT sum((nsd.SUBSCRIPTION_AMOUNT+nsd.VAS_AMOUNT)-(nsd.DISCOUNT)) FROM nimai_subscription_details nsd \r\n\tWHERE nsd.userid=(:userid) and nsd.PAYMENT_STATUS!='Rejected' AND nsd.PAYMENT_STATUS!='Pending'", nativeQuery = true)
  Integer findTotalEarning(@Param("userid") String userid);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getAllCustomerKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' AND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' AND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getPendingCustomerKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' AND (nc.SUBSCRIBER_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' AND (nc.SUBSCRIBER_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getCuPendingCustomerKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc WHERE (nc.MODE_OF_PAYMENT='Wire' and nc.payment_status='Pending') AND (nc.SUBSCRIBER_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc WHERE (nc.MODE_OF_PAYMENT='Wire' and nc.payment_status='Pending') AND (nc.SUBSCRIBER_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getCuPendingCustomerPayment(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Approved' AND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Approved' AND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getApprovedCustomerKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nc.KYC_STATUS='Rejected' AND \n(nc.SUBSCRIBER_TYPE='CUSTOMER' or nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Rejected' AND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getRejectedCustomerKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc WHERE nc.USERID NOT IN \n\t\t\t(SELECT nfk.userId from nimai_f_kyc nfk) \n\t\t\tand nc.REGISTERED_COUNTRY IN :value AND ((nc.SUBSCRIBER_TYPE='CUSTOMER' or nc.SUBSCRIBER_TYPE='BANK') AND (nc.BANK_TYPE='CUSTOMER' or\n\t\t\tnc.BANK_TYPE=''))", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) as cnt FROM nimai_m_customer nc WHERE nc.USERID NOT IN (SELECT nfk.userId from nimai_f_kyc nfk) \nAND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getNotUploadCustomerKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')\nAND nc.USERID= :userId and nc.REGISTERED_COUNTRY IN :value", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByUserId(String userId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nc.EMAIL_ADDRESS=:emailId OR (nc.account_source=(select nmcc.USERID FROM nimai_m_customer nmcc\nWHERE nmcc.EMAIL_ADDRESS=:emailId)\nAND nc.is_associated=1) and (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')\n and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByEmailId(String emailId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')\nAND nc.MOBILE_NUMBER= :mobileNumber and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByMobileNumber(String mobileNumber, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')\nAND nc.COUNTRY_NAME= :country and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByCountry(String country, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')\nAND nc.COMPANY_NAME= :companyName and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByCompanyName(String companyName, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getOnlineAllBankKYC(List<String> value, Pageable paramPageable);
  
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='Offline' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='Offline' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getOfflineAllBankKYC(List<String> value, Pageable paramPageable);
  
  
  @Query(value = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' \n and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getPendingAllKYC(List<String> value);
  
  @Query(value = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' and nc.SUBSCRIBER_TYPE='Customer' \nand nc.REGISTERED_COUNTRY IN :value  GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getCustPendingAllKYC(List<String> value);
  
  @Query(value = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' and nc.SUBSCRIBER_TYPE='Bank' and nc.BANK_TYPE='Customer' \n and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getBankAsCustPendingAllKYC(List<String> value);
  
  @Query(value = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nfk.kyc_status='Pending' AND nc.KYC_STATUS='Pending') and nc.SUBSCRIBER_TYPE='Bank' and nc.BANK_TYPE='Underwriter' \n and nc.REGISTERED_COUNTRY IN :value  GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getBankAsUnderPendingAllKYC(List<String> value);
  
  @Query(value = "(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Maker Approved' and nc.REGISTERED_COUNTRY IN :value)\n", nativeQuery = true)
  long getGrantKYC(List<String> value);
  
  @Query(value = "(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId \n  WHERE nfk.kyc_status='Maker Approved' and nc.SUBSCRIBER_TYPE='Bank' and nc.BANK_TYPE='Customer'and nc.REGISTERED_COUNTRY IN :value) \n", nativeQuery = true)
  long getBankAsCustGrantKYC(List<String> value);
  
  @Query(value = "(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Maker Approved' and nc.SUBSCRIBER_TYPE='Bank' and nc.BANK_TYPE='Underwriter' and nc.REGISTERED_COUNTRY IN :value)\n", nativeQuery = true)
  long getBankAsUnderGrantKYC(List<String> value);
  
  @Query(value = "(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Maker Approved' and nc.SUBSCRIBER_TYPE='Customer'and nc.REGISTERED_COUNTRY IN :value)\n", nativeQuery = true)
  long getCustGrantKYC(List<String> value);
  
  @Query(value = "\tSELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\n\t\t\t\tWHERE (nfk.KYC_STATUS='Pending' AND nc.KYC_STATUS='Pending')  AND \n\t\t\t\t(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value\n\t\t\tGROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nfk.kyc_status='Pending' AND nc.KYC_STATUS='Pending') AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getPendingBankKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "\tSELECT * FROM nimai_m_customer nc \n\t\t\t\tWHERE (nc.PAYMENT_STATUS='Pending' and nc.MODE_OF_PAYMENT='Wire')  AND \n\t\t\t\t(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value\n\t\t\tGROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc \nWHERE (nc.PAYMENT_STATUS='Pending' and nc.MODE_OF_PAYMENT='Wire') AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getPaymentPendingBank(List<String> value, Pageable paramPageable);
  
  @Query(value = "\r\n   SELECT c.USERID,c.FIRST_NAME,c.source_details,c.LAST_NAME,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,\r\n\t \t\t c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,\r\n\t \t\t c.LANDLINE,c.DESIGNATION,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,c.BUSINESS_TYPE,\r\n\t \t\t c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE, \r\n\t \t\t c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,\r\n\t \t\t c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT, \r\n\t \t\t c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n\t \t\t c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n\t \t\t c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,\r\n\t \t\t c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n\t \t\t c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from\r\n "
  		+ "((SELECT   c.USERID,c.FIRST_NAME,c.LAST_NAME,c.source_details,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,\r\n\t \t\t c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,\r\n\t \t\t c.LANDLINE,c.DESIGNATION,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,c.BUSINESS_TYPE,\r\n\t \t\t c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE, \r\n\t \t\t c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,\r\n\t \t\t c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT, \r\n\t \t\t c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n\t \t\t c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n\t \t\t c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,\r\n\t \t\t c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n\t \t\t c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from nimai_m_customer c where \r\n c.REGISTERED_COUNTRY IN :value AND \r\n (c.USERID not in (select s.userid from nimai_subscription_details s) )\r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Underwriter')\r\n  union\r\n  "
  		+ "(SELECT   c.USERID,c.FIRST_NAME,c.LAST_NAME,c.source_details,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,\r\n\t \t\t c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,\r\n\t \t\t c.LANDLINE,c.DESIGNATION,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,c.BUSINESS_TYPE,\r\n\t \t\t c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE, \r\n\t \t\t c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,\r\n\t \t\t c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT, \r\n\t \t\t c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n\t \t\t c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n\t \t\t c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,\r\n\t \t\t c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n\t \t\t c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from nimai_m_customer c INNER JOIN nimai_subscription_details nd \r\n  ON  c.USERID=nd.userid\r\n\t  where nd.`STATUS`='INACTIVE' and\r\n\t c.REGISTERED_COUNTRY IN :value \r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Underwriter'))c", countQuery = " \r\n SELECT (t1.cnt1+t2.cnt2) AS totalcount from\r\n ((SELECT  COUNT(*) AS cnt1 from nimai_m_customer c where \r\n c.REGISTERED_COUNTRY IN :value AND \r\n (c.USERID not in (select s.userid from nimai_subscription_details s) )\r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Underwriter'))t1,\r\n  (SELECT COUNT(distinct nd.userid) AS cnt2 from nimai_m_customer c INNER JOIN nimai_subscription_details nd \r\n  ON  c.USERID=nd.userid\r\n  where nd.`STATUS`='INACTIVE' and\r\n c.REGISTERED_COUNTRY IN :value  \r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Underwriter')t2", nativeQuery = true)
  Page<NimaiMCustomer> getPaymentPendingUserBank(List<String> value, Pageable paramPageable);
  
  @Query(value = "\r\n SELECT c.USERID,c.FIRST_NAME,c.source_details,c.LAST_NAME,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,\r\n c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,\r\n c.LANDLINE,c.DESIGNATION,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,c.BUSINESS_TYPE,\r\n c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE,\r\n c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,\r\n c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT,\r\n c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,\r\n c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from\r\n "
  		+ "((SELECT c.USERID,c.FIRST_NAME,c.LAST_NAME,c.source_details,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,\r\n c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,\r\n c.LANDLINE,c.DESIGNATION,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,c.BUSINESS_TYPE,\r\n c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE,\r\n c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,\r\n c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT,\r\n c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,\r\n c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from nimai_m_customer c where \r\n c.REGISTERED_COUNTRY IN :value AND \r\n (c.USERID not in (select s.userid from nimai_subscription_details s) )\r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer')\r\n  "
  		+ "union\r\n  (SELECT  c.USERID,c.FIRST_NAME,c.source_details,c.LAST_NAME,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,\r\n c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,\r\n c.LANDLINE,c.DESIGNATION,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,c.BUSINESS_TYPE,\r\n c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE,\r\n c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,\r\n c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT,\r\n c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,\r\n c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from nimai_m_customer c INNER JOIN nimai_subscription_details nd \r\n  ON  c.USERID=nd.userid\r\n\t  where nd.`STATUS`='INACTIVE' and\r\n\t c.REGISTERED_COUNTRY IN :value  \r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer'))c", countQuery = " \r\n SELECT (t1.cnt1+t2.cnt2) AS totalcount from\r\n ((SELECT  COUNT(c.*) AS cnt1 from nimai_m_customer c where \r\n c.REGISTERED_COUNTRY IN :value AND \r\n (c.USERID not in (select s.userid from nimai_subscription_details s) )\r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer'))t1,\r\n  (SELECT COUNT(distinct nd.userid) AS cnt2 from nimai_m_customer c INNER JOIN nimai_subscription_details nd \r\n  ON  c.USERID=nd.userid\r\n  where nd.`STATUS`='INACTIVE' and\r\n c.REGISTERED_COUNTRY IN :value \r\n  and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer')t2 ", nativeQuery = true)
  Page<NimaiMCustomer> getPaymentPendingUserBC(List<String> value, Pageable paramPageable);
  
  @Query(value = " SELECT c.USERID,c.FIRST_NAME,c.LAST_NAME,c.source_details,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,\r\n c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,"
  		+ "c.INSERTED_DATE,\r\n c.LANDLINE,c.DESIGNATION,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,c.BUSINESS_TYPE,\r\n c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,"
  		+ "c.REGISTRATION_TYPE,\r\n c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,\r\n c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT,\r\n c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n "
  		+ "c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,\r\n c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,"
  		+ "c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from\r\n "
  		+ "((SELECT c.USERID,c.FIRST_NAME,c.LAST_NAME,c.source_details,c.MOBILE_NUMBER,c.EMAIL_ADDRESS,"
  		+ "\r\n c.COUNTRY_NAME,c.COMPANY_NAME,c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,"
  		+ "\r\n c.LANDLINE,c.DESIGNATION,c.BUSINESS_TYPE,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,"
  		+ "\r\n c.BRANCH_NAME,c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE,"
  		+ "\r\n c.PROVINCENAME,c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,"
  		+ "\r\n c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,"
  		+ "c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT,\r\n c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,\r\n "
  		+ "c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,\r\n c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,"
  		+ "\r\n c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,\r\n c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,"
  		+ "c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment "
  		+ "from nimai_m_customer c where \r\n "
  		+ "c.REGISTERED_COUNTRY IN :value AND "
  		+ "\r\n (c.USERID not in (select s.userid from nimai_subscription_details s) )"
  		+ "\r\n  and c.SUBSCRIBER_TYPE='CUSTOMER' and c.ACCOUNT_TYPE!='SUBSIDIARY')\r\n  "
  		+ "union\r\n  (SELECT c.USERID,c.FIRST_NAME,c.source_details,c.LAST_NAME,c.MOBILE_NUMBER,"
  		+ "c.EMAIL_ADDRESS,\r\n c.COUNTRY_NAME,c.COMPANY_NAME,"
  		+ "c.KYC_STATUS,c.REGISTERED_COUNTRY,c.INSERTED_DATE,\r\n c.LANDLINE,c.DESIGNATION,"
  		+ "c.BUSINESS_TYPE,c.SUBSCRIBER_TYPE,c.BANK_NAME,c.BANK_TYPE,\r\n c.BRANCH_NAME,"
  		+ "c.SWIFT_CODE,c.TELEPHONE,c.MIN_VALUEOF_LC,c.REGISTRATION_TYPE,\r\n c.PROVINCENAME,"
  		+ "c.ADDRESS1,c.ADDRESS2,c.ADDRESS3,c.CITY,c.PINCODE,c.IS_REGISTER,"
  		+ "\r\n c.IS_RMASSIGNED,c.RM_ID,c.RM_STATUS,c.IS_BDETAILSFILLED,c.IS_SPLANPURCHASED,c.MODE_OF_PAYMENT,"
  		+ "\r\n c.PAYMENT_STATUS,c.PAYMENT_TRANS_ID,"
  		+ "\r\n c.PAYMENT_DATE,c.PAYMENT_APPROVED_BY,c.KYC_APPROVALDATE,"
  		+ "\r\n c.MODIFIED_DATE,c.ACCOUNT_TYPE,c.ACCOUNT_SOURCE,c.ACCOUNT_CREATED_DATE,c.ACCOUNT_STATUS,"
  		+ "\r\n c.ACCOUNT_REMARK,c.CURRENCY_CODE,c.EMAIL_ADDRESS1,c.EMAIL_ADDRESS2,c.EMAIL_ADDRESS3,"
  		+ "\r\n c.OTHERS,c.TC_INSERTED_DATE,c.TC_FLAG,c.CUSTOMER_IP_ADDRESS,c.LEAD_ID,c.APPROVED_BY,"
  		+ "c.IS_ASSOCIATED,c.MRPA,c.OFF_BAU_APPROVAL_DATE,c.modifiedby,c.OFF_BAU_APPROVED_BY,c.off_bau_created_by,c.user_mode,c.off_bau_status,c.rm_details,c.lead_status,c.lead_comment from nimai_m_customer c INNER JOIN nimai_subscription_details nd \r\n  "
  		+ "ON  c.USERID=nd.userid\r\n\t  where nd.`STATUS`='INACTIVE' "
  		+ " and\r\n\t c.REGISTERED_COUNTRY IN :value  \r\n  and c.SUBSCRIBER_TYPE='CUSTOMER' AND "
  		+ "c.ACCOUNT_TYPE!='SUBSIDIARY'))c", 
  		countQuery = " SELECT (t1.cnt1+t2.cnt2) AS totalcount  "
  		+ "from\r\n ((SELECT  COUNT(c.*) AS cnt1 from nimai_m_customer c where "
  		+ "\r\n c.REGISTERED_COUNTRY IN :value AND \r\n "
  		+ "(c.USERID not in (select s.userid from nimai_subscription_details s) )\r\n  "
  		+ " and c.SUBSCRIBER_TYPE='CUSTOMER' AND c.ACCOUNT_TYPE!='SUBSIDIARY'))t1,\r\n  "
  		+ "(SELECT COUNT(distinct nd.userid) AS cnt2 from nimai_m_customer c "
  		+ "INNER JOIN nimai_subscription_details nd \r\n  ON  c.USERID=nd.userid\r\n  "
  		+ "where nd.`STATUS`='INACTIVE' and\r\n c.REGISTERED_COUNTRY IN :value  "
  		+ "\r\n  and c.SUBSCRIBER_TYPE='CUSTOMER' AND c.ACCOUNT_TYPE!='SUBSIDIARY')t2 ", nativeQuery = true)
  Page<NimaiMCustomer> getPaymentPendingUserCU(List<String> value, Pageable paramPageable);
  
  @Query(value = "\tselect * from nimai_m_customer c\nwhere c.subscriber_type='BANK' and c.bank_type='UNDERWRITER' \nand c.USERID in (select s.userid from nimai_subscription_details s \nwhere s.status='Active' and s.SPLAN_END_DATE between now() \nand DATE_ADD(NOW(), INTERVAL 30 DAY)) and c.REGISTERED_COUNTRY IN :value", countQuery = "select count(*) as cnt from nimai_m_customer c\nwhere c.subscriber_type='BANK' and c.bank_type='UNDERWRITER' \nand c.REGISTERED_COUNTRY IN :value and c.USERID in (select s.userid from nimai_subscription_details s \nwhere s.status='Active' and s.SPLAN_END_DATE between now() \nand DATE_ADD(NOW(), INTERVAL 30 DAY))", nativeQuery = true)
  Page<NimaiMCustomer> getSubscriptionExpiryBank(List<String> value, Pageable paramPageable);
  
  @Query(value = "\tselect c.* from nimai_m_customer c\nwhere c.subscriber_type='BANK' and c.bank_type='CUSTOMER' \nand c.USERID in (select s.userid from nimai_subscription_details s \nwhere s.status='Active' and s.SPLAN_END_DATE between now() \nand DATE_ADD(NOW(), INTERVAL 30 DAY)) and c.REGISTERED_COUNTRY IN :value", countQuery = "select count(c.*) as cnt from nimai_m_customer c\nwhere c.subscriber_type='BANK' and c.bank_type='CUSTOMER' \nand c.REGISTERED_COUNTRY IN :value and c.USERID in (select s.userid from nimai_subscription_details s \nwhere s.status='Active' and s.SPLAN_END_DATE between now() \nand DATE_ADD(NOW(), INTERVAL 30 DAY))", nativeQuery = true)
  Page<NimaiMCustomer> getSubscriptionExpiryBC(List<String> value, Pageable paramPageable);
  
  @Query(value = "\tselect c.* from nimai_m_customer c\nwhere c.subscriber_type='CUSTOMER' \nand c.USERID in (select s.userid from nimai_subscription_details s \nwhere s.status='Active' and s.SPLAN_END_DATE between now() \nand DATE_ADD(NOW(), INTERVAL 30 DAY)) and c.REGISTERED_COUNTRY IN :value", countQuery = "select count(c.*) as cnt from nimai_m_customer c\nwhere c.subscriber_type='CUSTOMER' \nand c.REGISTERED_COUNTRY IN :value and c.USERID in (select s.userid from nimai_subscription_details s \nwhere s.status='Active' and s.SPLAN_END_DATE between now() \nand DATE_ADD(NOW(), INTERVAL 30 DAY))", nativeQuery = true)
  Page<NimaiMCustomer> getSubscriptionExpiryCU(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.KYC_STATUS='Approved' AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Approved' AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getApprovedBankKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nc.KYC_STATUS='Rejected' AND \n(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Rejected' AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getRejectedBankKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE nc.USERID NOT IN \n\t\t\t(SELECT nfk.userId from nimai_f_kyc nfk)\n\t\t\tAND nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER' and nc.REGISTERED_COUNTRY IN :value", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) as cnt FROM nimai_m_customer nc WHERE nc.USERID NOT IN (SELECT nfk.userId from nimai_f_kyc nfk) \nAND nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getNotUploadBankKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId \nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.USERID= :userId and  nc.REGISTERED_COUNTRY IN :value ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByUserId(String userId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.EMAIL_ADDRESS= :emailId and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByEmailId(String emailId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.MOBILE_NUMBER= :mobileNUmber and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByMobileNo(String mobileNUmber, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.COUNTRY_NAME= :country and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByCountry(String country, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.BANK_NAME= :companyName and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByCompanyName(String companyName, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')\nAND nc.USERID= :userId and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value and nc.USERID= :userId and nc.RM_ID= :rmId and nc.RM_STATUS='Active'  \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByUserIdRmId(String rmId,String userId, List<String> value, Pageable paramPageable);
  
  
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId \nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.USERID= :userId and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.USERID= :userId and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByUser(String userId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.EMAIL_ADDRESS= :emailId and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.EMAIL_ADDRESS= :emailId and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByEmail(String emailId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.MOBILE_NUMBER= :mobileNUmber and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.MOBILE_NUMBER= :mobileNUmber and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByMobile(String mobileNUmber, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.COUNTRY_NAME= :country and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.COUNTRY_NAME= :country and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByCountr(String country, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.BANK_NAME= :companyName and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.BANK_NAME= :companyName and IFNULL (nc.user_mode,'' ) !='Offline' and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByCompanyNa(String companyName, List<String> value, Pageable paramPageable);
  
//===============offline===========================
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId \nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.USERID= :userId and nc.user_mode='Offline' and nc.REGISTERED_COUNTRY IN :value ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getOffBankDetailsByUser(String userId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.EMAIL_ADDRESS= :emailId and nc.user_mode='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getOffBankDetailsByEmail(String emailId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.MOBILE_NUMBER= :mobileNUmber and nc.user_mode='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getOffBankDetailsByMobile(String mobileNUmber, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.COUNTRY_NAME= :country and nc.user_mode='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getOffBankDetailsByCountr(String country, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')\nAND nc.BANK_NAME= :companyName and nc.user_mode='Offline' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getOffBankDetailsByCompanyNa(String companyName, List<String> value, Pageable paramPageable);

  
  

	  
	   
  
  
  
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')"
  		+ "AND nc.MOBILE_NUMBER= :mobileNumber and nc.RM_ID= :rmId and nc.RM_STATUS='Active'"
  		+ " and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ",
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer "
  				+ "nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and"
  				+ " nc.REGISTERED_COUNTRY IN :value AND nc.MOBILE_NUMBER= :mobileNumber "
  				+ "and nc.RM_ID= :rmId and nc.RM_STATUS='Active' "
  				+ "GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByMobileNumberRmId
  (@Param("mobileNumber")String mobileNumber, String rmId,@Param("value") List<String> value, Pageable paramPageable);
  
  
  
  
  
  
  
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')"
  		+ "AND nc.EMAIL_ADDRESS= :emailId and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
  		+ "nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", 
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc "
  				+ "left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') AND "
  				+ "nc.EMAIL_ADDRESS= :emailId and nc.RM_ID= :rmId and nc.RM_STATUS='Active'"
  				+ " and nc.REGISTERED_COUNTRY IN :value "
  				+ "GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByEmailIdRmId
  (@Param("emailId")String emailId,@Param("rmId") String rmId, List<String> value, Pageable paramPageable);
  
  
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR "
  		+ "nc.BANK_TYPE='CUSTOMER') AND nc.COUNTRY_NAME= :country and "
  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID ", 
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) "
  				+ "AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  				+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR "
  				+ "nc.BANK_TYPE='CUSTOMER') AND nc.COUNTRY_NAME= :country  and "
  				+ "nc.REGISTERED_COUNTRY IN :value and nc.RM_ID= :rmId and "
  				+ "nc.RM_STATUS='Active' GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByCountryRmId
  (@Param("country")String country,@Param("rmId") String rmId, List<String> value, Pageable paramPageable);
  
  
  
  
  
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR "
  		+ "nc.BANK_TYPE='CUSTOMER') AND nc.COMPANY_NAME= :companyName and "
  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value"
  		+ " GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from "
  				+ "(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc "
  				+ "left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') "
  				+ "and nc.COMPANY_NAME= :companyName and nc.REGISTERED_COUNTRY IN :value "
  				+ "and nc.RM_ID= :rmId and nc.RM_STATUS='Active' GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getDetailsByCompanyNameRmId(@Param("companyName")String companyName,@Param("rmId") String paramString2,@Param("value") List<String> paramList, Pageable paramPageable);
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  		+ "WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and "
  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID", 
  		countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS "
  				+ "cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and"
  				+ " nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
  				+ "nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getAllCustomerKYCRmid(@Param("rmId") String rmId,@Param("value") List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId "
  		+ "WHERE nfk.kyc_status='Pending' AND "
  		+ "(nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER')"
  		+ " and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and"
  		+ " nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' AND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getPendingCustomerKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE nfk.kyc_status='Approved' AND "
  		+ "(nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') "
  		+ "and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID ", 
  		countQuery = "SELECT COUNT(cnt) from "
  				+ "(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk "
  				+ "ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Approved' AND "
  				+ "(nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') "
  				+ "and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and"
  				+ " nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getApprovedCustomerKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId\nWHERE nc.KYC_STATUS='Rejected' AND "
  		+ "(nc.SUBSCRIBER_TYPE='CUSTOMER' or nc.BANK_TYPE='CUSTOMER')  and nc.RM_ID= :rmId "
  		+ "and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Rejected' AND (nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER') and nc.RM_ID= :rmId and nc.RM_STATUS='Active'and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getRejectedCustomerKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc WHERE nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.USERID NOT IN "
  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
  		+ "AND ((nc.SUBSCRIBER_TYPE='CUSTOMER' or nc.SUBSCRIBER_TYPE='BANK') "
  		+ "AND (nc.BANK_TYPE='CUSTOMER' or nc.BANK_TYPE='')) and "
  		+ "nc.REGISTERED_COUNTRY IN :value",
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) as cnt "
  				+ "FROM nimai_m_customer nc WHERE  nc.RM_ID= :rmId "
  				+ "and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value and "
  				+ "nc.USERID NOT IN (SELECT nfk.userId from nimai_f_kyc nfk) "
  				+ "AND nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='CUSTOMER' "
  				+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getNotUploadCustomerKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
  		+ "AND nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER'and "
  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) as cnt FROM nimai_m_customer nc WHERE nc.USERID NOT IN (SELECT nfk.userId from nimai_f_kyc nfk) \nAND nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER'and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getNotUploadBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  
  
  
  
  //=====================================
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' OR nc.BANK_TYPE='UNDERWRITER')\nAND nc.USERID= :userId and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' OR nc.BANK_TYPE='UNDERWRITER') and nc.REGISTERED_COUNTRY IN :value and nc.USERID= :userId and nc.RM_ID= :rmId and nc.RM_STATUS='Active'  \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByUserIdRmId(String userId,String rmId, List<String> value, Pageable paramPageable);
  
  
  
//  
//  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk "
//  		+ "ON nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='BANK' "
//  		+ "AND nc.BANK_TYPE='UNDERWRITER') AND nc.USERID= :userId and nc.RM_ID= :rmId and "
//  		+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value",
//  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM "
//  				+ "nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
//  				+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') AND "
//  				+ "nc.USERID= :userId and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
//  				+ "nc.REGISTERED_COUNTRY IN :value) GROUP BY nc.USERID) as cnt", nativeQuery = true)
//  Page<NimaiMCustomer> getBankDetailsByUserIdRmId(String userId, String rmId, List<String> value, Pageable paramPageable);
//  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId "
  		+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
  		+ "AND nc.MOBILE_NUMBER= :mobileNUmber and nc.RM_ID= :rmId and nc.RM_STATUS='Active' "
  		+ "and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", 
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer "
  				+ "nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') AND "
  				+ "nc.MOBILE_NUMBER= :mobileNUmberand nc.RM_ID= :rmId and nc.RM_STATUS='Active' "
  				+ "and nc.REGISTERED_COUNTRY IN :value) GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByMobileNoRmId(String mobileNUmber, String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')"
  		+ "AND nc.COUNTRY_NAME= :country and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
  		+ "nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", 
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM "
  				+ "nimai_m_customer nc left join nimai_f_kyc nfk ON "
  				+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
  				+ "AND nc.COUNTRY_NAME= :countryand nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
  				+ "nc.REGISTERED_COUNTRY IN :value) GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByCountryRmId(String country, String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  		+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
  		+ "AND nc.BANK_NAME= :companyName and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value"
  		+ " GROUP BY nc.USERID", 
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc "
  				+ "left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
  				+ "AND nc.BANK_NAME= :companyNameand nc.RM_ID= :rmId and "
  				+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value) "
  				+ "GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByCompanyNameRmId(String companyName, String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='BANK' "
  		+ "AND nc.BANK_TYPE='UNDERWRITER') "
  		+ "AND nc.EMAIL_ADDRESS= :emailId and nc.RM_ID= :rmId and "
  		+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID",
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer "
  				+ "nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
  				+ "AND nc.EMAIL_ADDRESS= :emailId and nc.RM_ID= :rmId and nc.RM_STATUS='Active' "
  				+ "and nc.REGISTERED_COUNTRY IN :value) GROUP BY nc.USERID) as cnt ", nativeQuery = true)
  Page<NimaiMCustomer> getBankDetailsByEmailIdRmId(String emailId, String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId "
  		+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')and "
  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID", 
  		countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) "
  				+ "AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  				+ "nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND "
  				+ "nc.BANK_TYPE='UNDERWRITER')and nc.RM_ID= :rmId and nc.RM_STATUS='Active' "
  				+ "and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getAllBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = " SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE (nfk.KYC_STATUS='Pending' AND "
  		+ "nc.KYC_STATUS='Pending')  AND "
  		+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.RM_ID= :rmId "
  		+ "and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID", 
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc"
  				+ " left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  				+ "WHERE nfk.kyc_status='Pending' AND "
  				+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
  				+ "and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
  				+ "nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getPendingBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE nfk.KYC_STATUS='Approved' "
  		+ "AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')and "
  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
  		+ "nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ",
  		countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM "
  				+ "nimai_m_customer nc left join nimai_f_kyc nfk ON "
  				+ "nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Approved' "
  				+ "AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
  				+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getApprovedBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) AND "
  		+ "nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER' and "
  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from "
  				+ "(SELECT COUNT(*) as cnt FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
  				+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
  				+ "AND nc.SUBSCRIBER_TYPE='BANK' AND "
  				+ "nc.BANK_TYPE='UNDERWRITER'and nc.RM_ID= :rmId and"
  				+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
  				+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getRejectedBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE  nc.RM_ID= :rmId and "
  		+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", nativeQuery = true)
  List<NimaiMCustomer> getCountryWiseRmData(List<String> value, String rmId);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE  "
  		+ " nc.RM_ID= :rmId and (nc.RM_STATUS='Pending' or nc.RM_STATUS='Active')"
  		+ " and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID", nativeQuery = true)
  List<NimaiMCustomer> getCountryWiseRmPendingdata(List<String> value, String rmId);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE  "
  		+ " nc.RM_ID= :rmId and (nc.RM_STATUS='Pending' or nc.RM_STATUS='Active') and "
  		+ "nc.REGISTERED_COUNTRY IN :value and nc.SUBSCRIBER_TYPE='Customer' "
  		+ "GROUP BY nc.USERID", nativeQuery = true)
  List<NimaiMCustomer> getCustCountryWiseRmPendingdata(List<String> value, String rmId);
  
  @Query(value = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc"
  		+ " left join nimai_f_kyc nfk ON nc.USERID=nfk.userId WHERE nfk.kyc_status='Pending'"
  		+ " and nc.RM_STATUS='Active' and nc.RM_ID= :rmId and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getPendingAllKYCRmWise(List<String> value, String rmId);
  
  @Query(value = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc"
  		+ " left join nimai_f_kyc nfk ON nc.USERID=nfk.userId WHERE "
  		+ "nfk.kyc_status='Pending' and nc.RM_STATUS='Active' and nc.RM_ID= :rmId and "
  		+ "nc.REGISTERED_COUNTRY IN :value and nc.SUBSCRIBER_TYPE='Customer' "
  		+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getPendingCustKYCRmWise(List<String> value, String rmId);
  
  @Query(value = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc "
  		+ "left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
  		+ "WHERE nfk.kyc_status='Maker Approved' and "
  		+ "nc.RM_STATUS='Active' and nc.RM_ID= :rmId and nc.REGISTERED_COUNTRY IN :value "
  		+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getGrantKYCRmWise(List<String> value, String rmId);
  
  @Query(value = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM "
  		+ "nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId WHERE nfk.kyc_status='Maker Approved' and "
  		+ "nc.RM_STATUS='Active' and nc.RM_ID= :rmId and "
  		+ "nc.REGISTERED_COUNTRY IN :value and nc.SUBSCRIBER_TYPE='Customer' GROUP BY nc.USERID) "
  		+ "as cnt", nativeQuery = true)
  long getGrantCustKYCRmWise(List<String> value, String rmId);
  
  @Query(value = "\tSELECT COUNT(cnt) from\r\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc WHERE nc.KYC_STATUS='Pending' and nc.USERID IN\r\n (SELECT nfk.userId from nimai_f_kyc nfk)\r\n  and nc.REGISTERED_COUNTRY IN :value \r\n  AND nc.SUBSCRIBER_TYPE='REFERRER'  GROUP BY nc.USERID) as cnt", nativeQuery = true)
  long getRefPendingAllKYC(List<String> value);
  
  @Query(value = "(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Maker Approved' and nc.SUBSCRIBER_TYPE='Referrer' and nc.REGISTERED_COUNTRY IN :value)\n", nativeQuery = true)
  long getRefCustGrantKYC(List<String> value);
  
  @Query(value = " select count(c.userid) from nimai_m_customer c \nwhere c.REGISTERED_COUNTRY IN (select COUNTRY_NAME from nimai_lookup_countries) and c.USERID not in (select nk.userid from nimai_f_kyc nk) ", nativeQuery = true)
  long getpendingKycNullNew();
  
  @Query(value = " select count(c.userid)  from nimai_m_customer c\n   where (c.KYC_STATUS='Pending' OR c.KYC_STATUS IS NULL) and  c.SUBSCRIBER_TYPE='Customer'", nativeQuery = true)
  long getCustpendingKycNull();
  
  @Query(value = " select count(c.userid) from nimai_m_customer c \n\twhere c.REGISTERED_COUNTRY IN (select COUNTRY_NAME from nimai_lookup_countries)  and c.USERID not in (select nk.userid from nimai_f_kyc nk) and c.SUBSCRIBER_TYPE='Customer'", nativeQuery = true)
  long getCustpendingKycNullNew();
  
  @Query(value = " select count(c.userid)  from nimai_m_customer c\n   where (c.KYC_STATUS='Pending' OR c.KYC_STATUS IS NULL) and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer'", nativeQuery = true)
  long getBankAsCustpendingKycNull();
  
  @Query(value = " select count(c.userid) from nimai_m_customer c \n\t\twhere c.REGISTERED_COUNTRY IN (select COUNTRY_NAME from nimai_lookup_countries)  and c.USERID not in (select nk.userid from nimai_f_kyc nk)and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer'", nativeQuery = true)
  long getBankAsCustpendingKycNullNew();
  
  @Query(value = " select count(c.userid)  from nimai_m_customer c\n   where (c.KYC_STATUS='Pending' OR c.KYC_STATUS IS NULL) and  c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Underwriter'", nativeQuery = true)
  long getBankAsUndependingKycNull();
  
  @Query(value = " select count(c.userid) from nimai_m_customer c \n\twhere c.REGISTERED_COUNTRY IN (select COUNTRY_NAME from nimai_lookup_countries)  and c.USERID not in (select nk.userid from nimai_f_kyc nk) and  c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Underwriter'", nativeQuery = true)
  long getBankAsUndependingKycNullNew();
  
  @Query(value = " select count(c.userid)  from nimai_m_customer c\n   where (c.KYC_STATUS='Pending' OR c.KYC_STATUS IS NULL) and  c.SUBSCRIBER_TYPE='Referrer'", nativeQuery = true)
  long getRefpendingKycNull();
  
  @Query(value = " select count(c.userid)  from nimai_m_customer c \nwhere c.REGISTERED_COUNTRY IN (select COUNTRY_NAME from nimai_lookup_countries)  and c.USERID not in (select nk.userid from nimai_f_kyc nk) \nand  c.SUBSCRIBER_TYPE='Referrer'", nativeQuery = true)
  long getRefpendingKycNullNew();
  
  @Query(value = " select count(c.userid)  from nimai_m_customer c \nwhere c.COUNTRY_NAME IN :value and c.USERID not in (select nk.userid from nimai_f_kyc nk) \nand  c.SUBSCRIBER_TYPE='Referrer'", nativeQuery = true)
  long getRefpendingKycCountryWiseNullNew(List<String> value);
  
  @Query(value = " select count(c.userid) from nimai_m_customer c\n   where c.USERID not in (select nk.userid from nimai_f_kyc nk) and c.COUNTRY_NAME IN :value", nativeQuery = true)
  long getCountrypendingKycNullNew(List<String> value);
  
  @Query(value = " select count(c.userid) from nimai_m_customer c\n   where (c.KYC_STATUS='Pending' OR c.KYC_STATUS IS NULL) and  c.SUBSCRIBER_TYPE='Customer'and  c.COUNTRY_NAME IN :value", nativeQuery = true)
  long getCountryCustpendingKycNull(List<String> value);
  
  @Query(value = " select count(c.userid) from nimai_m_customer c \n\t\twhere c.USERID not in (select nk.userid from nimai_f_kyc nk) and  c.SUBSCRIBER_TYPE='Customer'and  c.COUNTRY_NAME IN :value", nativeQuery = true)
  long getCountryCustpendingKycNullNew(List<String> value);
  
  @Query(value = " select count(c.userid) from nimai_m_customer c\n   where (c.KYC_STATUS='Pending' OR c.KYC_STATUS IS NULL) and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer'and c.COUNTRY_NAME IN :value", nativeQuery = true)
  long getCountryBankAsCustpendingKycNull(List<String> value);
  
  @Query(value = " select count(c.userid) from nimai_m_customer c \n\twhere c.USERID not in (select nk.userid from nimai_f_kyc nk) and c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Customer'and c.COUNTRY_NAME IN :value", nativeQuery = true)
  long getCountryBankAsCustpendingKycNullNew(List<String> value);
  
  @Query(value = " select count(c.userid) "
  		+ " from nimai_m_customer c"
  		+ "   where (c.KYC_STATUS='Pending' OR c.KYC_STATUS IS NULL) and "
  		+ " c.SUBSCRIBER_TYPE='Bank' and c.bank_type='Underwriter'and c.COUNTRY_NAME IN :value", nativeQuery = true)
  long getCountryBankAsUndependingKycNull(List<String> value);
  
  @Query(value = " select count(c.userid) from nimai_m_customer c "
  		+ "where c.USERID not in (select nk.userid from nimai_f_kyc nk) and  "
  		+ "c.SUBSCRIBER_TYPE='Bank' and "
  		+ "c.bank_type='Underwriter'and c.REGISTERED_COUNTRY IN :value", nativeQuery = true)
  long getCountryBankAsUndependingKycNullNew(List<String> value);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
  		+ "nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' AND "
  		+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='CUSTOMER') "
  		+ "and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID ", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Pending' AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getBCuPendingCustomerKYC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc WHERE "
  		+ "(nc.MODE_OF_PAYMENT='Wire' and nc.payment_status='Pending') AND "
  		+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='CUSTOMER') and "
  		+ "nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID ", 
  		countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc "
  				+ "WHERE (nc.MODE_OF_PAYMENT='Wire' and nc.payment_status='Pending') AND "
  				+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='CUSTOMER') and "
  				+ "nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getBCuPendingCustomerPayment(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
  		+ "and nc.REGISTERED_COUNTRY IN :value AND "
  		+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='CUSTOMER')", 
  		countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) as cnt FROM nimai_m_customer nc WHERE nc.USERID NOT IN (SELECT nfk.userId from nimai_f_kyc nfk) \nAND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='CUSTOMER') and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getNotUploadForBC(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT nc.* FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
  		+ "and nc.REGISTERED_COUNTRY IN :value AND "
  		+ "(nc.SUBSCRIBER_TYPE='CUSTOMER' AND nc.BANK_TYPE='')", 
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) as cnt "
  				+ "FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
  				+ "(SELECT nfk.userId from nimai_f_kyc nfk) AND "
  				+ "nc.SUBSCRIBER_TYPE!='REFERRER' AND "
  				+ "(nc.SUBSCRIBER_TYPE='CUSTOMER' OR nc.BANK_TYPE='') "
  				+ "and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getNotUploadForCU(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
  		+ "and nc.REGISTERED_COUNTRY IN :value AND "
  		+ "(nc.SUBSCRIBER_TYPE='REFERRER' AND nc.BANK_TYPE='')",
  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) "
  				+ "as cnt FROM nimai_m_customer nc WHERE "
  				+ "nc.USERID NOT IN (SELECT nfk.userId from nimai_f_kyc nfk) "
  				+ "AND (nc.SUBSCRIBER_TYPE='REFERRER' AND nc.BANK_TYPE='') "
  				+ "and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getNotUploadForRE(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc WHERE nc.KYC_STATUS='Pending' "
  		+ "and nc.USERID IN (SELECT nfk.userId from nimai_f_kyc nfk) "
  		+ "and nc.REGISTERED_COUNTRY IN :value AND "
  		+ "(nc.SUBSCRIBER_TYPE='REFERRER' AND nc.BANK_TYPE='')", 
  		countQuery = " SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc"
  				+ "WHERE nc.KYC_STATUS='Pending' and nc.USERID IN (SELECT nfk.userId "
  				+ "from nimai_f_kyc nfk) and nc.REGISTERED_COUNTRY IN :value "
  				+ "AND nc.SUBSCRIBER_TYPE='REFERRER' GROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getPendingForRE(List<String> value, Pageable paramPageable);
  
  @Query(value = "SELECT (t1.cnt1+t2.cnt2) AS totalcount  from "
  		+ "((SELECT  COUNT(*) AS cnt1 from nimai_m_customer c where "
  		+ " c.REGISTERED_COUNTRY IN :value AND  (c.USERID not in "
  		+ "(select s.userid from nimai_subscription_details s) )"
  		+ "  and c.SUBSCRIBER_TYPE!='REFERRER' AND c.ACCOUNT_TYPE!='SUBSIDIARY'  ))t1,"
  		+ "  (SELECT COUNT(distinct nd.userid) AS cnt2 from nimai_m_customer c "
  		+ "  INNER JOIN nimai_subscription_details nd   ON  c.USERID=nd.userid"
  		+ "  where nd.`STATUS`='INACTIVE' and c.REGISTERED_COUNTRY IN :value  "
  		+ "  and c.SUBSCRIBER_TYPE!='REFERRER' AND c.ACCOUNT_TYPE!='SUBSIDIARY')t2 ", nativeQuery = true)
  long getDashboardCountByQuery(List<String> value);
  
  @Query(value = "SELECT (t1.cnt1+t2.cnt2) AS totalcount from"
  		+ " ((SELECT  COUNT(*) AS cnt1 from nimai_m_customer c where "
  		+ " c.REGISTERED_COUNTRY IN :value AND "
  		+ " (c.USERID not in (select s.userid from nimai_subscription_details s) )"
  		+ "  and c.SUBSCRIBER_TYPE=:subscriberType AND c.ACCOUNT_TYPE!='SUBSIDIARY'))t1,"
  		+ "  (SELECT COUNT(distinct nd.userid) AS cnt2 from nimai_m_customer c"
  		+ " INNER JOIN nimai_subscription_details nd "
  		+ " ON  c.USERID=nd.userid  where nd.`STATUS`='INACTIVE' and"
  		+ " c.REGISTERED_COUNTRY IN :value "
  		+ "  and c.SUBSCRIBER_TYPE=:subscriberType AND c.ACCOUNT_TYPE!='SUBSIDIARY')t2 ", nativeQuery = true)
  long getDashboardCountCustomer(String subscriberType, List<String> value);
  
  @Query(value = "  SELECT (t1.cnt1+t2.cnt2) AS totalcount from"
  		+ " ((SELECT  COUNT(*) AS cnt1 from nimai_m_customer c where "
  		+ " c.REGISTERED_COUNTRY IN :value AND"
  		+ "  (c.USERID not in (select s.userid from nimai_subscription_details s) )"
  		+ "  and c.SUBSCRIBER_TYPE=:subscriberType and c.bank_type=:bankName))t1,"
  		+ "  (SELECT COUNT(distinct nd.userid) AS cnt2 from nimai_m_customer c "
  		+ "INNER JOIN nimai_subscription_details nd   ON  c.USERID=nd.userid"
  		+ " where nd.`STATUS`='INACTIVE' and c.REGISTERED_COUNTRY IN :value  "
  		+ "  and c.SUBSCRIBER_TYPE=:subscriberType and c.bank_type=:bankName)t2 ", nativeQuery = true)
  long getDashboardCountBank(String subscriberType, String bankName, List<String> value);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk "
	  		+ "ON nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='BANK' "
	  		+ "AND nc.BANK_TYPE='UNDERWRITER') AND nc.USERID= :userId and nc.RM_ID= :rmId and nc.user_mode='OFFLINE' "
	  		+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value",
	  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM "
	  				+ "nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
	  				+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') AND "
	  				+ "nc.USERID= :userId and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
	  				+ "nc.REGISTERED_COUNTRY IN :value) GROUP BY nc.USERID) as cnt", nativeQuery = true)
	  Page<NimaiMCustomer> getOffBankDetailsByUserIdRmId(String userId, String rmId, List<String> value, Pageable paramPageable);
	  

@Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  		+ "nc.USERID=nfk.userId "
	  		+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
	  		+ "AND nc.MOBILE_NUMBER= :mobileNUmber and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.user_mode='OFFLINE' "
	  		+ "and nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", 
	  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer "
	  				+ "nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
	  				+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') AND  nc.user_mode='OFFLINE' "
	  				+ "nc.MOBILE_NUMBER= :mobileNUmberand nc.RM_ID= :rmId and nc.RM_STATUS='Active' "
	  				+ "and nc.REGISTERED_COUNTRY IN :value) GROUP BY nc.USERID) as cnt ", nativeQuery = true)
	  Page<NimaiMCustomer> getOffBankDetailsByMobileNoRmId(String mobileNUmber, String rmId, List<String> value, Pageable paramPageable);
	  
@Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  		+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='OFFLINE' "
	  		+ " AND nc.COUNTRY_NAME= :country and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
	  		+ "nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ", 
	  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM "
	  				+ "nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  				+ "nc.USERID=nfk.userId WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='OFFLINE' "
	  				+ "AND nc.COUNTRY_NAME= :countryand nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
	  				+ "nc.REGISTERED_COUNTRY IN :value) GROUP BY nc.USERID) as cnt ", nativeQuery = true)
	  Page<NimaiMCustomer> getoffBankDetailsByCountryRmId(String country, String rmId, List<String> value, Pageable paramPageable);
	  

@Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
	  		+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
	  		+ "AND nc.BANK_NAME= :companyName and nc.RM_ID= :rmId and nc.RM_STATUS='Active'  and nc.user_mode='OFFLINE' "
	  		+ " and nc.REGISTERED_COUNTRY IN :value"
	  		+ " GROUP BY nc.USERID", 
	  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc "
	  				+ "left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
	  				+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER')  and nc.user_mode='OFFLINE' "
	  				+ "AND nc.BANK_NAME= :companyNameand nc.RM_ID= :rmId and "
	  				+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value) "
	  				+ "GROUP BY nc.USERID) as cnt ", nativeQuery = true)
	  Page<NimaiMCustomer> getoffBankDetailsByCompanyNameRmId(String companyName, String rmId, List<String> value, Pageable paramPageable);
	  

@Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  		+ "nc.USERID=nfk.userId "
	  		+ "WHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='OFFLINE' and "
	  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
	  		+ "GROUP BY nc.USERID", 
	  		countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) "
	  				+ "AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  				+ "nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND "
	  				+ "nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='OFFLINE' and nc.RM_ID= :rmId and nc.RM_STATUS='Active' "
	  				+ "and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
	  Page<NimaiMCustomer> getAllOffBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
	  
@Query(value = " SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  		+ "nc.USERID=nfk.userId WHERE (nfk.KYC_STATUS='Pending' AND "
	  		+ "nc.KYC_STATUS='Pending') and nc.user_mode='OFFLINE'  AND "
	  		+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.RM_ID= :rmId "
	  		+ "and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
	  		+ "GROUP BY nc.USERID", 
	  		countQuery = "SELECT COUNT(cnt) from (SELECT COUNT(*) AS cnt FROM nimai_m_customer nc"
	  				+ " left join nimai_f_kyc nfk ON nc.USERID=nfk.userId "
	  				+ "WHERE nfk.kyc_status='Pending' AND "
	  				+ "(nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='OFFLINE' "
	  				+ "and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
	  				+ "nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID) as cnt", nativeQuery = true)
	  Page<NimaiMCustomer> getPendingffBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
	  

@Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  		+ "nc.USERID=nfk.userId WHERE nfk.KYC_STATUS='Approved' and nc.user_mode='OFFLINE' "
	  		+ "AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and "
	  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and "
	  		+ "nc.REGISTERED_COUNTRY IN :value GROUP BY nc.USERID ",
	  		countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM "
	  				+ "nimai_m_customer nc left join nimai_f_kyc nfk ON "
	  				+ "nc.USERID=nfk.userId\nWHERE nfk.kyc_status='Approved' and nc.user_mode='OFFLINE' "
	  				+ "AND (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') "
	  				+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
	  Page<NimaiMCustomer> getApprovedOffBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
	  
@Query(value = "SELECT * FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
	  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) AND "
	  		+ "nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER' and nc.user_mode='OFFLINE' and "
	  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
	  		+ "GROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from "
	  				+ "(SELECT COUNT(*) as cnt FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
	  				+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
	  				+ "AND nc.SUBSCRIBER_TYPE='BANK' and nc.user_mode='OFFLINE' AND "
	  				+ "nc.BANK_TYPE='UNDERWRITER'and nc.RM_ID= :rmId and"
	  				+ "nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value "
	  				+ "GROUP BY nc.USERID) as cnt", nativeQuery = true)
	  Page<NimaiMCustomer> getRejectedOffBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
	  
@Query(value = "SELECT * FROM nimai_m_customer nc WHERE nc.USERID NOT IN "
	  		+ "(SELECT nfk.userId from nimai_f_kyc nfk) "
	  		+ "AND nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER' and nc.user_mode='OFFLINE' and "
	  		+ "nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) as cnt FROM nimai_m_customer nc WHERE nc.USERID NOT IN (SELECT nfk.userId from nimai_f_kyc nfk) \nAND nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER'and nc.RM_ID= :rmId and nc.RM_STATUS='Active' and nc.REGISTERED_COUNTRY IN :value\nGROUP BY nc.USERID) as cnt", nativeQuery = true)
	  Page<NimaiMCustomer> getNotUploadOffBankKYCRmId(String rmId, List<String> value, Pageable paramPageable);
	
  @Query(value = "SELECT * FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='OFFLINE' and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID", countQuery = "SELECT COUNT(cnt) from\n(SELECT COUNT(*) AS cnt FROM nimai_m_customer nc left join nimai_f_kyc nfk ON nc.USERID=nfk.userId\nWHERE (nc.SUBSCRIBER_TYPE='BANK' AND nc.BANK_TYPE='UNDERWRITER') and nc.user_mode='OFFLINE' and nc.REGISTERED_COUNTRY IN :value \nGROUP BY nc.USERID) as cnt", nativeQuery = true)
  Page<NimaiMCustomer> getAllOffBankKYC(List<String> value, Pageable paramPageable);

  @Query(value= "select * from nimai_m_customer nc where nc.email_address=:emailId",nativeQuery = true)
NimaiMCustomer existsByEmailId(String emailId);

  @Query(value= "select * from nimai_m_customer nc where nc.LEAD_ID=:leadId",nativeQuery = true)
NimaiMCustomer findCustomersByLeadId(Integer leadId);

	@Query(value= "SELECT nc.code FROM nimai_m_currency nc WHERE nc.country=:countryName",nativeQuery = true)
	String findCountryCode(@Param("countryName")String countryName);

//	@Query(value = " update nimai_m_customer set lead_status=:leadStatus AND lead_comment=:leadComment WHERE USERID=:userId")
//	void updateLeadStatusById(@Param("userId")String userId,@Param("leadStatus")String leadStatus,@Param("leadComment")String leadComment);
}
