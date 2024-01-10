package com.nimai.lc.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.lc.entity.NimaiClient;

@Transactional
public interface NimaiClientRepository extends JpaRepository<NimaiClient, String>
{
	@Query("SELECT nc FROM NimaiClient nc WHERE nc.userid= (:userId) or (nc.accountSource= (:userId) and nc.accountType='SUBSIDIARY')")
	List<NimaiClient> findCreditTransactionByUserId(@Param("userId") String userId);
	
	@Query("SELECT nc FROM NimaiClient nc WHERE nc.userid= (:userId)")
	List<NimaiClient> findCreditTransactionByOnlyUserId(@Param("userId") String userId);
	
	@Query("SELECT nc FROM NimaiClient nc WHERE nc.userid= (:userId)")
	NimaiClient findCreditTransactionByUserIdForPasscode(@Param("userId") String userId);
	
	@Query("SELECT nc FROM NimaiClient nc WHERE nc.companyName= (:subsidiaryName) and nc.accountSource= (:userId) and nc.accountType='SUBSIDIARY'")
	List<NimaiClient> findCreditTransactionByUserIdSubsidiary(@Param("userId") String userId,@Param("subsidiaryName") String subsidiaryName);
	
	@Query("SELECT nc FROM NimaiClient nc WHERE nc.userid= (:userId) or nc.accountSource= (:userId) and nc.accountType='BANKUSER'")
	List<NimaiClient> findCreditTransactionByBankUserId(@Param("userId") String userId);

	@Query("SELECT nc.companyName FROM NimaiClient nc WHERE nc.userid= (:userId)")
	String findCompanyNameByUserId(String userId);

//	@Query(value="select * from nimai_m_customer\r\n" + 
//			"nc where nc.USERID!=userId and \r\n" + 
//			"nc.Bank_type='UNDERWRITER' and nc.KYC_STATUS='Approved'\r\n" + 
//			"AND (nc.PAYMENT_STATUS='Approved' or nc.PAYMENT_STATUS='Success')",nativeQuery = true)
//	List<NimaiClient> getAllElBank(String userId);
	
	
	@Query(value="SELECT * FROM nimai_m_customer nc WHERE nc.USERID != (:userId) and nc.USERID like 'BA%' and ((nc.KYC_STATUS='Approved' and (nc.PAYMENT_STATUS='Approved' or nc.PAYMENT_STATUS='Success')) or (nc.OFF_BAU_STATUS='Approved'))",nativeQuery = true)
	List<NimaiClient> getAllElBank(@Param("userId") String userId);

	@Query("SELECT nc FROM NimaiClient nc WHERE nc.userMode='OFFLINE' and offBauStatus='Approved'")
	List<NimaiClient> getApprovedOfflineBank();

	@Query("SELECT nc FROM NimaiClient nc WHERE nc.userid like 'BA%' and nc.userid!=(:userID)")
	List<NimaiClient> getBankListForSec(@Param("userID") String userID);

	@Query("SELECT nc.userMode FROM NimaiClient nc WHERE nc.emailAddress= (:emailId)")
	String findUserMode(String emailId);
	
	@Query("select nc.userid from NimaiClient nc")
	List<String> getAllUserIds();
	
	@Query(value = "select * from nimai_m_customer where EMAIL_ADDRESS=(:emailId)", nativeQuery = true)
	NimaiClient getCuDtlsByEmail(String emailId);
	
	@Query(value = "select * from nimai_m_customer where userid=(:userId)", nativeQuery = true)
	NimaiClient getCustomerDetails(String userId);
	
	@Query(value = "select email_address from nimai_m_customer where userid=(:userId)", nativeQuery = true)
	String getCustomerEmailId(String userId);
	

}
