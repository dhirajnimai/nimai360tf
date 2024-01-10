package com.nimai.email.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.email.entity.NimaiLCMaster;


@Repository
@Transactional
public interface LCMasterRepository extends JpaRepository<NimaiLCMaster, String> {

	
	
	@Query(value="select nmt.transaction_id,nmt.lc_currency,nmt.lc_issuance_bank,nmt.lc_value from nimai_mm_transaction nmt \n"
			+ "inner join nimai_m_customer nc on nc.USERID=nmt.user_id \n" + 
			" where nmt.transaction_status='Active' and nc.bank_type='UNDERWRITER'\n" + 
			"AND (nmt.user_id!=(:inp_userid) and nmt.user_id NOT IN \n" + 
			"(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE=(:inp_userid))\n" + 
			"and nmt.user_id NOT IN \n" + 
			"(SELECT nc.ACCOUNT_SOURCE FROM nimai_m_customer nc WHERE nc.USERID=(:inp_userid)))\n" + 
			"and nmt.transaction_id NOT IN\n" + 
			"(select qu.transaction_id from nimai_m_quotation qu\n" + 
			"WHERE (qu.bank_userid=(:inp_userid) OR qu.bank_userid IN \n" + 
			"(SELECT nc.USERID FROM nimai_m_customer nc WHERE nc.ACCOUNT_SOURCE=(:inp_userid))\n" + 
			"OR qu.bank_userid IN \n" + 
			"(SELECT nc.ACCOUNT_SOURCE FROM nimai_m_customer nc WHERE nc.USERID=(:inp_userid)))\n" + 
			"AND \n" + 
			"(qu.quotation_status='Placed' OR qu.quotation_status='Accepted'  \n" + 
			"OR qu.quotation_status='Rejected' OR qu.quotation_status='ExpPlaced' \n" + 
			"OR qu.quotation_status='RePlaced' OR qu.quotation_status like 'Freeze%' \n" + 
			"or qu.quotation_status='Withdrawn')) order by nmt.inserted_date DESC limit 5;", nativeQuery = true )
	List findSecondaryTxnForBank(String inp_userid);

	@Query(value="select tr.transaction_id,tr.lc_currency,tr.lc_issuance_bank,tr.lc_value  from nimai_mm_transaction tr,\r\n" +  
			"		nimai_m_customer cu\r\n" + 
			"		where (tr.user_id not like 'BA%') AND (tr.lc_issuance_country IN \r\n" + 
			"		(select country_name from nimai_f_intcountry where userid=:inp_user_id)\r\n" + 
			"		or tr.bene_country in\r\n" + 
			"		(select country_name from nimai_f_beneintcountry where userid=:inp_user_id))\r\n" + 
			"		and tr.goods_type NOT IN \r\n" + 
			"		(select goods_name from nimai_f_blkgoods where userid=:inp_user_id)\r\n" + 
			"		and cu.USERID=:inp_user_id AND \r\n" + 
			"		case WHEN cu.CURRENCY_CODE=tr.lc_currency then \r\n" + 
			"		cu.MIN_VALUEOF_LC<=tr.lc_value\r\n" + 
			"		WHEN cu.CURRENCY_CODE!=tr.lc_currency then \r\n" + 
			"		cu.MIN_VALUEOF_LC>=0 end\r\n" + 
			"		and tr.transaction_status='Active' AND tr.transaction_id NOT IN\r\n" + 
			"		(select qu.transaction_id from nimai_m_quotation qu\r\n" + 
			"		where (qu.bank_userid=:inp_user_id \r\n" + 
			"		OR\r\n" + 
			"		qu.bank_userid IN (select userid from nimai_m_customer where account_source=:inp_user_id)\r\n" + 
			"		or\r\n" + 
			"		qu.bank_userid IN (select ACCOUNT_SOURCE from nimai_m_customer where userid=:inp_user_id))\r\n" + 
			"		AND (qu.quotation_status='Placed' \r\n" + 
			"		OR qu.quotation_status='Rejected' OR qu.quotation_status='ExpPlaced' \r\n" + 
			"		OR qu.quotation_status='RePlaced' OR qu.quotation_status like 'Freeze%' or qu.quotation_status='Withdrawn')) order by tr.inserted_date desc limit 5; \r\n" + 
			"", nativeQuery = true)
	List findPrimaryTxnForBank(String inp_user_id);

	
	
	
}
