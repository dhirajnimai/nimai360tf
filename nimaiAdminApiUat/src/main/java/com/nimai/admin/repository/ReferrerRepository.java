package com.nimai.admin.repository;

import com.nimai.admin.model.NimaiMRefer;
import java.util.Date;
import java.util.List;
import javax.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferrerRepository extends JpaRepository<NimaiMRefer, Integer>, JpaSpecificationExecutor<NimaiMRefer> {
  @Query(value = "select userid from nimai_m_refer where email_address =:emailAddress", nativeQuery = true)
  String findReferredUserByEmailId(@Param("emailAddress") String emailAddress);
  
  @Query(value = "SELECT * from nimai_m_refer nm where   nm.INSERTED_DATE >= (:fromDate) AND nm.INSERTED_DATE <= (:toDate) GROUP BY nm.USERID", nativeQuery = true)
  List<NimaiMRefer> finBydates(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
  
  @Query(value = "SELECT * from nimai_m_refer nm where nm.INSERTED_DATE >= (:fromDate) AND nm.INSERTED_DATE <= (:toDate) AND nm.USERID=(:userId);", nativeQuery = true)
  List<Tuple> finBydatesandUserId(@Param("userId") String userId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
  
  @Query("select re from NimaiMRefer re where re.userid.userid= (:userId)")
  List<NimaiMRefer> findReferByUserId(@Param("userId") String userId);
  
  @Query(value = "SELECT  * FROM nimai_m_customer nc inner JOIN nnimai_m_refer nr ON nr.USERID=nc.ACCOUNT_SOURCE WHERE nr.USERID=:value "
  		+ "AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", 
		  countQuery = "SELECT count(*) FROM nimai_m_customer nc inner JOIN nimai_m_refer nr ON nr.USERID=nc.ACCOUNT_SOURCE WHERE"
		  		+ " nr.USERID=:value AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
  Page<NimaiMRefer> getReferrerDetails(String value, Pageable paramPageable);
  
  @Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on "
  		+ "nr.USERID=nc.ACCOUNT_SOURCE  WHERE nr.USERID=:value AND nc.KYC_STATUS=:kycStatus and nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", 
  		countQuery = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM nimai_m_customer nc inner join nimai_m_refer nr on nr.USERID=nc.ACCOUNT_SOURCE WHERE "
  				+ "nr.USERID=:value AND nc.KYC_STATUS=:kycStatus "
  				+ "and nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
  Page<NimaiMRefer> approvedReferrerDetails(String value, String kycStatus, Pageable paramPageable);
  
@Query(value="select DISTINCT nr.FIRST_NAME,nr.LAST_NAME,\r\n" + 
		"nr.EMAIL_ADDRESS,nr.COUNTRY_NAME,nr.COMPANY_NAME,nr.INSERTED_DATE,\r\n" + 
		"nr.EMAIL_ADDRESS,nr.`STATUS`,nr.ID FROM nimai_m_refer nr where nr.USERID=:userId ",nativeQuery = true)
Page<NimaiMRefer> findAllById(String userId,Pageable pageable);


@Query(value="select DISTINCT nr.FIRST_NAME,nr.LAST_NAME,nr.MOBILE_NO,nr.BRANCH_USER_ID,nr.branch_user_email,nr.REFERERNCEID,nr.USERID,nr.ID"
		+ ",nr.MODIFIED_DATE,nr.INSERTED_BY,nr.MODIFIED_BY,nr.token_expiry_time,nr.token,nr.token_inserted_date,nr.promo_code,\r\n" + 
		"nr.EMAIL_ADDRESS,nr.COUNTRY_NAME,nr.COMPANY_NAME,nr.INSERTED_DATE,\r\n" + 
		"nr.REFER_EMAIL_ID,nr.`STATUS` FROM nimai_m_refer nr where nr.USERID=:userId ",
  		countQuery = "SELECT count(*) FROM nimai_m_refer nr where nr.USERID=:userId",nativeQuery = true)
Page<NimaiMRefer> findAllByIdNew(String userId,Pageable pageable);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
	  Page<NimaiMRefer> getRxilFieoReferrerPendingDetails(String value, Pageable paramPageable);

@Query(value="select * from nimai_m_refer nr where nr.ID=:id",nativeQuery = true)
NimaiMRefer findByRefId(Integer id);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
Page<NimaiMRefer> getReferrerApprovedDetails(String value, Pageable pageable);

@Query(value = "SELECT * FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
		"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
		"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", 
  		countQuery = "SELECT count(*) FROM nimai_m_customer nc inner join nimai_m_refer nr on \r\n" + 
  				"nr.USERID=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND nc.ACCOUNT_SOURCE=:value\r\n" + 
  				"AND nr.EMAIL_ADDRESS=nc.EMAIL_ADDRESS", nativeQuery = true)
Page<NimaiMRefer> getRxilFieoReferrerRejectedDetails(String value, Pageable pageable);

}
