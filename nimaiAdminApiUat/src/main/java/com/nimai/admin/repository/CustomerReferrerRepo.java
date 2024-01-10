package com.nimai.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.admin.model.NimaiMCustomer;

public interface CustomerReferrerRepo extends JpaRepository<NimaiMCustomer, String>, JpaSpecificationExecutor<NimaiMCustomer> {

	@Query(value ="SELECT  COUNT(*) AS totalReferences FROM nimai_m_customer nc \r\n" + 
	  		"WHERE nc.account_source=:userid and nc.RM_ID=:rmId and nc.RM_STATUS=:status", nativeQuery = true)
	  int getTotareferenceNewCuRef(@Param("userid") String userid,@Param("rmId") String rmId,@Param("status") String status);
	
	
	 @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc  "
		  		+ "WHERE nc.account_source=:userId AND nc.KYC_STATUS='Approved' and nc.RM_ID=:rmId and nc.RM_STATUS=:status", nativeQuery = true)
		  int getApprovedReferrenceNewCuRef(@Param("userId") String userId,@Param("rmId") String rmId,@Param("status") String status);
	
	  @Query(value = "\r\nSELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc "
		  		+ "WHERE nc.account_source=:userid AND "
		  		+ "nc.KYC_STATUS='Pending' and nc.RM_ID=:rmId and nc.RM_STATUS=:status ", nativeQuery = true)
		  int getpendingReferenceNewCuRef(@Param("userid") String userid,@Param("rmId") String rmId,@Param("status") String status);
	  
	  @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc "
		  		+ "WHERE nc.account_source=:userid AND nc.KYC_STATUS='Rejected' and nc.RM_ID=:rmId and nc.RM_STATUS=:status", nativeQuery = true)
		  int getRejectedReferenceNewCuRef(@Param("userid") String userid,@Param("rmId") String rmId,@Param("status") String status);
	  
	  //@Query(value = "SELECT sum((nsd.SUBSCRIPTION_AMOUNT+nsd.VAS_AMOUNT)-(nsd.DISCOUNT)) FROM nimai_subscription_details nsd \r\n\tWHERE nsd.userid=(:userid) and nsd.PAYMENT_STATUS!='Rejected' AND nsd.PAYMENT_STATUS!='Pending' and nc.RM_ID=:rmId and nc.RM_STATUS=:status", nativeQuery = true)
	  //Integer findTotalEarningCuRef(@Param("userid") String userid,@Param("rmId") String rmId,@Param("status") String status);
	  
	  //By Adil
	  @Query(value = "SELECT sum((nsd.SUBSCRIPTION_AMOUNT+nsd.VAS_AMOUNT)-(nsd.DISCOUNT)) FROM nimai_subscription_details nsd \r\n\tWHERE nsd.userid=(:userid) and nsd.PAYMENT_STATUS!='Rejected' AND nsd.PAYMENT_STATUS!='Pending'", nativeQuery = true)
	  Integer findTotalEarningCuRef(@Param("userid") String userid);
	 
	  @Query(value ="SELECT  COUNT(*) AS totalReferences FROM nimai_m_customer nc \r\n" + 
		  		"WHERE nc.account_source=:userid", nativeQuery = true)
		  int getTotareferenceNewCuRefforSA(@Param("userid") String userid);
	  
	  @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc  "
		  		+ "WHERE nc.account_source=:userId AND nc.KYC_STATUS='Approved'", nativeQuery = true)
		  int getApprovedReferrenceNewCuRefSA(@Param("userId") String userId);
	
	  @Query(value = "\r\nSELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc "
		  		+ "WHERE nc.account_source=:userid AND "
		  		+ "nc.KYC_STATUS='Pending'", nativeQuery = true)
		  int getpendingReferenceNewCuRefSA(@Param("userid") String userid);
	  
	  @Query(value = "SELECT COUNT(nc.USERID) AS approvedReferrence FROM "
		  		+ "nimai_m_customer nc "
		  		+ "WHERE nc.account_source=:userid AND nc.KYC_STATUS='Rejected'", nativeQuery = true)
		  int getRejectedReferenceNewCuRefSA(@Param("userid") String userid);
}
