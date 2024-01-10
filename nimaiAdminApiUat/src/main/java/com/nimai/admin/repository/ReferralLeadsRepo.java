package com.nimai.admin.repository;

import com.nimai.admin.model.NimaiMRefer;

import com.nimai.admin.model.ReferralLeads;

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
public interface ReferralLeadsRepo extends JpaRepository<ReferralLeads, Integer>, JpaSpecificationExecutor<ReferralLeads> {
  


	  @Query(value = "SELECT * FROM referral_leads rl WHERE rl.refer_by=:value ", 
				  countQuery = "SELECT count(*) FROM referral_leads rl WHERE rl.refer_by=:value", nativeQuery = true)
	  Page<ReferralLeads> getRxilFIEOReferrerDetails(String value, Pageable paramPageable);
	
	/*@Query(value="SELECT * FROM nimai_m_customer nc inner join referral_leads nr on \r\n" + 
	  		"nr.refer_by=nc.ACCOUNT_SOURCE WHERE \r\n" + 
	  		" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",
	  		countQuery = "SELECT COUNT(nc.USERID) FROM nimai_m_customer nc inner join\r\n" + 
	  				"		 referral_leads nr on \r\n" + 
	  				"nr.refer_by=nc.ACCOUNT_SOURCE WHERE \r\n" + 
	  				" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",nativeQuery = true)
	Page<ReferralLeads> getRxilFIEOReferrerDetails(String value, Pageable pageable);
*/
	  @Query(value="SELECT * FROM nimai_m_customer nc inner join referral_leads nr on \r\n" + 
	  		"nr.refer_by=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND\r\n" + 
	  		" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",
	  		countQuery = "SELECT COUNT(nc.USERID) FROM nimai_m_customer nc inner join\r\n" + 
	  				"		 referral_leads nr on \r\n" + 
	  				"nr.refer_by=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Pending' AND\r\n" + 
	  				" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",nativeQuery = true)
	Page<ReferralLeads> getRxilFieoReferrerPendingDetails(String value, Pageable pageable);
	  
	  @Query(value="SELECT * FROM nimai_m_customer nc inner join referral_leads nr on \r\n" + 
		  		"nr.refer_by=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND\r\n" + 
		  		" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",
		  		countQuery = "SELECT COUNT(nc.USERID) FROM nimai_m_customer nc inner join\r\n" + 
		  				"		 referral_leads nr on \r\n" + 
		  				"nr.refer_by=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Approved' AND\r\n" + 
		  				" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",nativeQuery = true)
		Page<ReferralLeads> getRxilFieoReferrerApprovedDetails(String value, Pageable pageable);
	  
	  @Query(value="SELECT * FROM nimai_m_customer nc inner join referral_leads nr on \r\n" + 
		  		"nr.refer_by=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND\r\n" + 
		  		" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",
		  		countQuery = "SELECT COUNT(nc.USERID) FROM nimai_m_customer nc inner join\r\n" + 
		  				"		 referral_leads nr on \r\n" + 
		  				"nr.refer_by=nc.ACCOUNT_SOURCE WHERE nc.KYC_STATUS='Rejected' AND\r\n" + 
		  				" nc.ACCOUNT_SOURCE=:value and nc.LEAD_ID=nr.lead_id",nativeQuery = true)
		Page<ReferralLeads> getRxilFieoReferrerRejectedDetails(String value, Pageable pageable);

}
