package com.nimai.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.admin.model.NimaiMRefer;

public interface ReferrerSaLeadRepo  extends JpaRepository<NimaiMRefer, Integer>, JpaSpecificationExecutor<NimaiMRefer> {
	

@Query(value="select DISTINCT nr.FIRST_NAME,nr.LAST_NAME,nr.MOBILE_NO,nr.BRANCH_USER_ID,nr.branch_user_email,nr.REFERERNCEID,nr.USERID,nr.ID, nr.MODIFIED_DATE,nr.INSERTED_BY,nr.MODIFIED_BY,nr.token_expiry_time,nr.token,nr.token_inserted_date,nr.promo_code,nr.EMAIL_ADDRESS,nr.COUNTRY_NAME,nr.COMPANY_NAME,nr.INSERTED_DATE,nr.REFER_EMAIL_ID,nr.`STATUS` FROM nimai_m_customer nc inner join  nimai_m_refer nr where nr.USERID=:userId and nc.RM_ID=:rmId and nc.RM_STATUS='Active' AND nc.EMAIL_ADDRESS=nr.EMAIL_ADDRESS",
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join  nimai_m_refer nr where nr.USERID=:userId and nc.RM_ID=:rmId and nc.RM_STATUS='Active' AND nc.EMAIL_ADDRESS=nr.EMAIL_ADDRESS",nativeQuery = true)
Page<NimaiMRefer> findAllByIdNewRefSaLead(@Param("userId") String userId,Pageable pageable,@Param("rmId") String rmId);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS and nc.RM_ID=:rmId and nc.RM_STATUS=:status ", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS and nc.RM_ID=:rmId and nc.RM_STATUS=:status", nativeQuery = true)
	  Page<NimaiMRefer> getRxilFieoReferrerPendingDetailsRefSaLead(@Param("value")String value, Pageable paramPageable,@Param("rmId") String rmId,@Param("status") String status);

@Query(value="select * from nimai_m_refer nr where nr.ID=:id",nativeQuery = true)
NimaiMRefer findByRefId(Integer id);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS and nc.RM_ID=:rmId and nc.RM_STATUS=:status", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS and nc.RM_ID=:rmId and nc.RM_STATUS=:status", nativeQuery = true)
Page<NimaiMRefer> getReferrerApprovedDetailsRefSaLead(@Param("value")String value, Pageable pageable,@Param("rmId") String rmId,@Param("status") String status);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS",
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
Page<NimaiMRefer> getReferrerApprovedDetailsRefSaLeadSA(@Param("value")String value, Pageable pageable);


@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS and nc.RM_ID=:rmId and nc.RM_STATUS=:status", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS and nc.RM_ID=:rmId and nc.RM_STATUS=:status", nativeQuery = true)
Page<NimaiMRefer> getRxilFieoReferrerRejectedDetailsRefSaLead(@Param("value")String value, Pageable pageable,@Param("rmId") String rmId,@Param("status") String status);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
Page<NimaiMRefer> getRxilFieoReferrerRejectedDetailsRefSaLeadSA(@Param("value")String value, Pageable pageable);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
	  Page<NimaiMRefer> getRxilFieoReferrerPendingDetailsRefSaLeadSA(@Param("value")String value, Pageable paramPageable);

@Query(value="select DISTINCT nr.FIRST_NAME,nr.LAST_NAME,nr.MOBILE_NO,nr.BRANCH_USER_ID,nr.branch_user_email,nr.REFERERNCEID,nr.USERID,nr.ID ,nr.MODIFIED_DATE,nr.INSERTED_BY,nr.MODIFIED_BY,nr.token_expiry_time,nr.token,nr.token_inserted_date,nr.promo_code,nr.EMAIL_ADDRESS,nr.COUNTRY_NAME,nr.COMPANY_NAME,nr.INSERTED_DATE, nr.REFER_EMAIL_ID,nr.`STATUS` FROM nimai_m_customer nc inner join nimai_m_refer nr where nr.USERID=:userId AND nc.EMAIL_ADDRESS=nr.EMAIL_ADDRESS",
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr where nr.USERID=:userId AND nc.EMAIL_ADDRESS=nr.EMAIL_ADDRESS",nativeQuery = true)
Page<NimaiMRefer> findAllByIdNewRefSaLeadWithoutRm(String userId, Pageable pageable);


}
