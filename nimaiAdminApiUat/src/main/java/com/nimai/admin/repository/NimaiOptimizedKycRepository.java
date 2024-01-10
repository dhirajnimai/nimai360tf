package com.nimai.admin.repository;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.admin.model.NimaiKyc;
import com.nimai.admin.model.NimaiOptimizedKyc;

public interface NimaiOptimizedKycRepository extends JpaRepository<NimaiOptimizedKyc, Integer> {

	
	 @Query(value = "SELECT k.id,k.document_Name,k.country,k.kyc_type,k.document_type,k.`comment`,k.kyc_status,k.userId,k.approved_maker FROM nimai_f_kyc k INNER JOIN nimai_m_customer nc ON k.userId=nc.USERID WHERE k.kyc_status='Maker Approved' AND nc.COUNTRY_NAME IN :value",countQuery = "SELECT count(*) FROM nimai_f_kyc k INNER JOIN nimai_m_customer nc ON k.userId=nc.USERID WHERE k.kyc_status='Maker Approved' AND nc.COUNTRY_NAME IN :value", nativeQuery = true)
	 Page<NimaiOptimizedKyc> findMakerApprovedKycByCountries(@Param("value") List<String> value, Pageable paramPageable);
		  
	 @Query(value = "SELECT k.id,k.document_Name,k.country,k.kyc_type,k.document_type,k.`comment`,k.kyc_status,k.userId,k.approved_maker FROM nimai_f_kyc k INNER JOIN nimai_m_customer nc ON k.userId=nc.USERID WHERE k.kyc_status='Maker Approved' AND nc.subscriber_type=:subsType and nc.bank_type=:bankType and nc.COUNTRY_NAME IN :value",countQuery = "SELECT count(*) FROM nimai_f_kyc k INNER JOIN nimai_m_customer nc ON k.userId=nc.USERID WHERE k.kyc_status='Maker Approved' AND nc.subscriber_type=:subsType and nc.bank_type=:bankType and nc.COUNTRY_NAME IN :value", nativeQuery = true)
	 Page<NimaiOptimizedKyc> findMakerApprovedKycByCountriesSubsTypeBankType(@Param("value") List<String> value,@Param("subsType") String subsType,@Param("bankType") String bankType, Pageable paramPageable);
	  
	 @Query(value = "SELECT k.id,k.document_Name,k.country,k.kyc_type,k.document_type,k.`comment`,k.kyc_status,k.userId,k.approved_maker FROM nimai_f_kyc k INNER JOIN nimai_m_customer nc  ON k.userId=nc.USERID WHERE nc.subscriber_type=:subsType and k.kyc_status='Maker Approved' AND nc.COUNTRY_NAME IN :value", countQuery = "SELECT COUNT(*) AS cnt FROM nimai_f_kyc k INNER JOIN nimai_m_customer nc ON k.userId=nc.USERID\r\n WHERE nc.subscriber_type=:subsType  \r\nAND k.kyc_status='Maker Approved' \r\nAND nc.COUNTRY_NAME IN :value", nativeQuery = true)
	 Page<NimaiOptimizedKyc> findCustomerReferrerMakerApprovedKycByCountries(@Param("value") List<String> value,@Param("subsType") String subsType, Pageable paramPageable);

	
	
	
}
