package com.nimai.lc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.lc.entity.NimaiClient;
import com.nimai.lc.entity.QuotationMaster;
import com.nimai.lc.entity.SavingInput;

@Repository
@Transactional
public interface QuotationMasterRepository extends JpaRepository<QuotationMaster, Integer>
{
	@Query(value="SELECT * from get_all_quotation where userid=(:userId) and quotation_status=(:status)", nativeQuery = true )
	List<QuotationMaster> findAllQuotation(@Param("userId") String userId,@Param("status") String status);
	
	@Query(value="SELECT transaction_id from get_all_quotation where quotation_id=(:qId)", nativeQuery = true )
	String findTransactionIdByQid(@Param("qId") Integer qId);
	
	@Query(value = "SELECT transaction_id,total_quote_value,quotation_status,inserted_date,modified_date from get_all_quotation where bank_userid=(:userid)", nativeQuery = true)
	List findQuotationByBankUserId(@Param("userid") String userid);
	
	@Query(value = "SELECT transaction_id from nimai_m_quotation where quotation_id=(:qId)", nativeQuery = true)
    String findTransactionIdByQuoteid(@Param("qId") final Integer qId);
	
	@Query(value="SELECT * from get_all_quotation where userid=(:userId)", nativeQuery = true )
	List<QuotationMaster> findAllQuotationByUserId(@Param("userId") String userId);
	
	/*@Query(value="SELECT qu.* from get_all_quotation qu WHERE qu.bank_userid IN\r\n" + 
			"(SELECT cpb.bank_userid FROM customer_preferred_banks cpb WHERE cpb.cust_userid=qu.userid)\r\n" + 
			"AND qu.userid=(:userId)  \r\n" + 
			"		and qu.transaction_id=(:transactionId) \r\n" + 
			"		and (qu.validity_date>=curdate() and  \r\n" + 
			"		(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn') \r\n" + 
			"		OR qu.quotation_status IS NULL))	\r\n" + 
			"		union\r\n" + 
			"SELECT qu.* from get_all_quotation qu\r\n" + 
			"where qu.userid=(:userId)  \r\n" + 
			"		and qu.transaction_id=(:transactionId) \r\n" + 
			"		and (qu.validity_date>=curdate() and  \r\n" + 
			"		(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn') \r\n" + 
			"		OR qu.quotation_status IS NULL))", nativeQuery = true )*/
	@Query(value="SELECT qu.* from get_all_quotation qu\r\n" + 
			"where qu.userid=(:userId)  \r\n" + 
			"		and qu.transaction_id=(:transactionId) \r\n" + 
			"		and (qu.validity_date>=curdate() and  \r\n" + 
			"		(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn') \r\n" + 
			"		OR qu.quotation_status IS NULL)) group by qu.bank_userid order by qu.total_quote_value;", nativeQuery = true )
	List<QuotationMaster> findAllQuotationByUserIdAndTransactionId(@Param("userId") String userId,@Param("transactionId") String transactionId);

	@Query(value="SELECT qu.* from get_all_quotation qu\r\n" + 
			"where qu.userid=(:userId)  \r\n" + 
			"		and qu.transaction_id=(:transactionId) \r\n" + 
			"		and (qu.validity_date>=curdate() and  \r\n" + 
			"		(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn') \r\n" + 
			"		OR qu.quotation_status IS NULL)) group by qu.bank_userid order by qu.total_quote_value;", nativeQuery = true )
	List<QuotationMaster> findAllQuotationByUserIdAndTransactionIdExpPreferred(@Param("userId") String userId,@Param("transactionId") String transactionId);

	
	@Query(value="SELECT qu.* from get_all_quotation qu\r\n" + 
			"INNER JOIN customer_preferred_banks cpb\r\n" + 
			"where qu.quotation_id=:quoteId \r\n" + 
			"and qu.transaction_id=:transactionId \r\n" + 
			"and (qu.validity_date>=curdate() and \r\n" + 
			"(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn') \r\n" + 
			"OR qu.quotation_status IS NULL))\r\n" + 
			"group by qu.bank_userid\r\n" + 
			"order by cpb.id;", nativeQuery = true )
	List<QuotationMaster> findAllQuotationByQuoteIdAndTransactionId(@Param("quoteId") Integer quoteId,@Param("transactionId") String transactionId);

	@Query(value="SELECT qu.* from get_all_quotation qu\r\n" + 
			"where qu.quotation_id=:quoteId\r\n" + 
			"and qu.transaction_id=:transactionId\r\n" + 
			"and (qu.validity_date>=curdate() and\r\n" + 
			"(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn')\r\n" + 
			"OR qu.quotation_status IS NULL))\r\n" + 
			"group by qu.bank_userid;", nativeQuery = true )
	List<QuotationMaster> findAllQuotationByQuoteIdAndTransactionIdExpPreferred(@Param("quoteId") Integer quoteId,@Param("transactionId") String transactionId);

	@Query(value="SELECT cpb.* FROM customer_preferred_banks cpb WHERE cpb.cust_userid=:userId", nativeQuery = true )
	List findPreferredBank(@Param("userId") String userId);

	
	@Query(value="SELECT * from get_all_quotation where userid=(:userId) and transaction_id=(:transactionId) and (quotation_status like '%Placed' OR quotation_status like 'Rejected')", nativeQuery = true )
	List<QuotationMaster> findAllReplacedQuotationByUserIdAndTransactionId(@Param("userId") String userId,@Param("transactionId") String transactionId);
	
	@Query(value="SELECT * from get_all_quotation where userid IN (:userId) and transaction_id=(:transactionId) and (quotation_status like '%Placed' OR quotation_status like 'Rejected')", nativeQuery = true )
	List<QuotationMaster> findAllReplacedQuotationByUserIdsAndTransactionId(@Param("userId") List<String> userId,@Param("transactionId") String transactionId);

	@Query(value="SELECT * from get_all_quotation where userid=(:userId) and transaction_id=(:transactionId) and quotation_status=(:status)", nativeQuery = true )
	List<QuotationMaster> findQuotationByUserIdAndTransactionIdStatus(@Param("userId") String userId,@Param("transactionId") String transactionId,@Param("status") String status);
	
	@Query(value="SELECT * from get_all_quotation where userid IN (:userId) and transaction_id=(:transactionId) and quotation_status=(:status)", nativeQuery = true )
	List<QuotationMaster> findQuotationByUserIdsAndTransactionIdStatus(@Param("userId") List<String> userId,@Param("transactionId") String transactionId,@Param("status") String status);
	
	@Query(value="SELECT * from nimai_m_quotation where userid IN (:userId) and transaction_id=(:transactionId) and quotation_status=(:status)", nativeQuery = true )
	List<QuotationMaster> findNewQuotationByUserIdsAndTransactionIdStatus(@Param("userId") List<String> userId,@Param("transactionId") String transactionId,@Param("status") String status);
	
	@Query(value="SELECT * from nimai_m_quotation where quotation_id=(:quotationId) and userid=(:userId) and transaction_id=(:transactionId) and quotation_status=(:status)", nativeQuery = true )
	List<QuotationMaster> findSecQuotationByUserIdAndTransactionIdStatus(@Param("quotationId") Integer quotationId,@Param("userId") String userId,@Param("transactionId") String transactionId,@Param("status") String status);
	
	
	@Query(value="SELECT * from get_all_quotation where quotation_id=(:quotationId)", nativeQuery = true )
	List<QuotationMaster> findAllQuotationByQuotationId(Integer quotationId);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='Rejected',modified_date=now(),rejected_by=(:rejectedBy),rejected_reason=(:rejectedReason)  where quotation_id=(:quotationId)", nativeQuery = true)
	void updateQuotationStatusToReject(Integer quotationId,String rejectedBy,String rejectedReason);
	
	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='Accepted' where quotation_id=(:quotationId)", nativeQuery = true)
	void updateQuotationStatusToAccept(Integer quotationId);
	
	@Modifying
	@Query(value= "update nimai_m_quotation SET quotation_status=\r\n" + 
			"case when quotation_status='FreezePlaced' then \r\n" + 
			"'FreezeExpired'\r\n" + 
			"ELSE\r\n" + 
			"'Expired'\r\n" + 
			"end,expired_on=NOW() where transaction_id=(:transId) and quotation_id!=(:quotationId) and quotation_status!='Withdrawn'", nativeQuery = true)
	void updateQuotationStatusToExpired(String transId,Integer quotationId);

	@Query(value="SELECT * from get_all_quotation where bank_userid=(:bankUserId)", nativeQuery = true )
	List<QuotationMaster> findAllQuotationBybankUserId(String bankUserId);

	
	@Query(value="SELECT DISTINCT t.lc_currency  from nimai_mm_transaction t  WHERE t.user_id=(:bankUserId)", nativeQuery = true )
	List<String> findSavingsByUserId(String bankUserId);
	
	@Query(value="select SUM(s.savings) FROM nimai_m_transaction_savings s WHERE s.userid=(:bankUserId)", nativeQuery = true )
	List<String> findTotalSavings(String bankUserId);

	@Query(value="select SUM(s.savings) FROM nimai_m_transaction_savings s ,nimai_mm_transaction t WHERE s.transaction_id=t.transaction_id\r\n" + 
			"AND t.lc_currency=(:ccy) AND s.userid=(:bankUserId)", nativeQuery = true )
	List<String> findTotalSavingsByUserId(String ccy,String bankUserId);

	
	/*@Query(value="SELECT * from get_trans_quote_for_bank where quotation_placed=(:quotationPlaced) and transaction_status=(:transactionStatus) and bank_userid=(:bankUserId)", nativeQuery = true )
	List findTransQuotationBybankUserIdAndStatus(String bankUserId,String quotationPlaced,String transactionStatus);
	*/
	@Query(value="SELECT * from get_trans_quote_for_bank where quotation_status like (%:quotationStatus%) and bank_userid=(:bankUserId)", nativeQuery = true )
	List findTransQuotationBybankUserIdAndStatus(String bankUserId,String quotationStatus);
	
	@Query(value="SELECT * from get_trans_quote_for_bank where quotation_id=(:qid)", nativeQuery = true )
	List findTransQuotationByqId(int qid);
	
	
	@Query(value="SELECT first_name,last_name,email_address,mobile_number,country_name,bank_name,branch_name,swift_code,telephone from nimai_m_customer where userid=(:bankUserId)", nativeQuery = true )
	List findBankDetailsBybankUserId(String bankUserId);

	@Query(value="SELECT bank_userid from nimai_m_quotation where quotation_id=(:quotationId) ", nativeQuery = true )
	String findBankUserIdByQuotationId(Integer quotationId);

	@Query(value= "select * from nimai_m_quotation where quotation_status='Accepted' and transaction_id=(:transId) and userid=(:userId)", nativeQuery = true)
	List<QuotationMaster> findAcceptedTransByTransIdUserId(String transId, String userId);

	@Query(value= "select * from nimai_m_quotation where quotation_status='Accepted' and transaction_id=(:transId)", nativeQuery = true)
	List<QuotationMaster> findAcceptedTransByTransId(String transId);
	
	@Query(value="SELECT quotation_id from nimai_m_quotation where transaction_id=(:transactionId) and quotation_status='Rejected' ORDER BY quotation_id DESC LIMIT 1", nativeQuery = true )
	Integer findRejectedQuotationByTransId(String transactionId);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='Placed' where transaction_id=(:transId) and quotation_id=(:qid)", nativeQuery = true)
	void updateQuotationToActiveByQid(Integer qid,String transId);
	
	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status=\r\n" + 
			"case when quotation_status='FreezeExpired' then \r\n" + 
			"'FreezeRePlaced'\r\n" + 
			"else\r\n" + 
			"'RePlaced' \r\n" + 
			"end where transaction_id=(:transId) and quotation_id=(:qid)", nativeQuery = true)
	void updateQuotationToRePlacedByQid(Integer qid,String transId);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='Placed' where transaction_id=(:transId) and quotation_status='Expired'", nativeQuery = true)
	void updateQuotationToRePlacedByTransId(String transId);
	
	@Query(value= "select * from nimai_m_quotation where (quotation_status!='Rejected' or quotation_status is null) and transaction_id=(:transId)", nativeQuery = true)
	List<QuotationMaster> findValidityDateAndQidByTransId(String transId);
	
	@Query(value= "select * from nimai_m_quotation where (quotation_status like 'Accepted') and transaction_id=(:transId)", nativeQuery = true)
	List<QuotationMaster> findAcceptedQuotationByTransId(String transId);
	
	@Query(value= "select * from nimai_m_quotation where quotation_id=(:qId)", nativeQuery = true)
	QuotationMaster findQuotationByQId(Integer qId);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status=null where transaction_id=(:transId) and quotation_id=(:qid)", nativeQuery = true)
	void updateQuotationForNewRequestByQid(Integer qid, String transId);

	@Modifying
	@Query(value= "update nimai_m_quotation SET quotation_status=\r\n" + 
			"case when quotation_status='FreezePlaced' then \r\n" + 
			"'FreezeExpired'\r\n" + 
			"ELSE\r\n" + 
			"'Expired'\r\n" + 
			"end,expired_on=NOW() where transaction_id=(:transId) and quotation_id!=(:quotationId) and quotation_status!='Withdrawn'", nativeQuery = true)
	void updateQuotationStatusToExpiredExceptRejectedStatus(String transId, Integer quotationId);

	@Query(value= "select count(*) from nimai_m_quotation where bank_userid=(:bankUserId)", nativeQuery = true)
	Integer getQuotesCount(String bankUserId);

	@Query(value= "select goods_type from nimai_mm_transaction where transaction_id=(:transId)", nativeQuery = true)
	String getGoodsByTransactionId(String transId);
	
	@Query(value= "SELECT sec_transaction_type FROM nimai_mm_transaction WHERE transaction_id=(:transId)", nativeQuery = true)
	String getSecTxnType(String transId);
	
	@Query(value= "SELECT lc_value FROM nimai_mm_transaction WHERE transaction_id=(:transId)", nativeQuery = true)
	Double getTransactionValue(String transId);
	
	@Query(value= "SELECT count(transaction_id) FROM nimai_mm_transaction WHERE transaction_id IN\r\n" + 
			"(SELECT transaction_id FROM nimai_m_quotation where bank_userid=(:bankUserId)) \r\n" + 
			"and goods_type=(:goodsType) GROUP BY goods_type", nativeQuery = true)
	Integer getGoodsCount(String goodsType,String bankUserId);
	
	@Query(value= "SELECT offered_price FROM nimai_mm_transaction WHERE transaction_id=(:transId)", nativeQuery = true)
	String getOfferedPrice(String transId);

	@Query(value= "select total_quote_value from nimai_m_quotation where transaction_id=(:transactionId) and quotation_status='Accepted'", nativeQuery = true)
	Float getAcceptedQuoteValue(String transactionId);
	
	@Query(value= "select total_quote_value from nimai_m_quotation where transaction_id=(:transactionId) and quotation_id=(:qId) and quotation_status='Accepted'", nativeQuery = true)
	Float getAcceptedQuoteValueByTransIdQuoteId(String transactionId, Integer qId);

	@Query(value= "select * from nimai_m_quotation where transaction_id=(:transactionId) and quotation_status='Accepted'", nativeQuery = true)
	QuotationMaster getAcceptedQuoteByTransactionId(String transactionId);

	@Query("SELECT qm FROM QuotationMaster qm WHERE qm.quotationId= (:quotationId)")
	QuotationMaster getDetailsByQuoteId(@Param("quotationId") int quotationId);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='ExpPlaced' where quotation_id=(:quotationId)", nativeQuery = true)
	void updateQuotationStatusToExpPlacedForQid(Integer quotationId);

	@Query(value= "select quotation_status from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	String getStatusAfterReopen(Integer qid);

	@Query(value= "select validity_date from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	String getValidityDate(Integer qid);

	@Query(value= "select bank_userid from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	String getBankUserId(int qid);
	
	@Query(value= "select userid from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	String getCuUserId(int qid);

	@Query(value= "select transaction_id from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	String getTransactionId(Integer qid);

	@Query(value= "select userid from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	String getUserIdByQid(Integer qid);

	@Query(value= "select total_quote_value from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	Double getQuoteAmount(Integer qid);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='FreezePlaced' where transaction_id=(:tid) and quotation_status NOT IN ('Rejected','Expired','ExpPlaced') OR quotation_status IS NULL", nativeQuery = true)
	void updateQuotationToFreezePlaced(String tid);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='Placed' where transaction_id=(:tid) and bank_userid=(:bankUserId) and quotation_status IN ('FreezePlaced','FreezeRePlaced')", nativeQuery = true)
	void updateQuotationToPlaced(String tid,String bankUserId);

	@Query("FROM QuotationMaster r where r.quotationId = :qid")
	QuotationMaster getById(@Param("qid")Integer qid);

	@Query(value= "select quotation_status from nimai_m_quotation where quotation_id=(:qid)", nativeQuery = true)
	String getStatus(Integer qid);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='FreezeExpired',expired_on=now() where transaction_id=(:transId) and quotation_id!=(:qid)", nativeQuery = true)
	void updateQuotationStatusFreezeToExpiredExceptRejectedStatus(String transId, Integer qid);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='FreezeExpired',expired_on=now() where transaction_id=(:transId) and quotation_id!=(:quotationId)", nativeQuery = true)
	void updateQuotationStatusFreezeToExpired(String transId, Integer quotationId);

	@Query(value= "select quotation_id from nimai_m_quotation where transaction_id=(:transactionId) and userid=(:userId) and quotation_status=(:status)", nativeQuery = true)
	Integer getQuotationId(String transactionId, String userId, String status);
	
	@Query(value= "select quotation_id from nimai_m_quotation where transaction_id=(:transactionId) and userid IN (:userId) and quotation_status=(:status)", nativeQuery = true)
	Integer getQuotationIdByTxnIdUserIdsStatus(String transactionId, List<String> userId, String status);

	@Query(value= "select quotation_id from nimai_m_quotation where transaction_id=(:transactionId) and userid=(:userId)", nativeQuery = true)
	Integer getQuotationId(String transactionId, String userId);
	
	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='Expired',expired_on=now() where transaction_id=(:transactionId) and userid=(:userId) and quotation_status!='Rejected'", nativeQuery = true)
	void updateQuotationStatusForCancelToExpired(String transactionId, String userId);

	@Modifying
	@Query(value= "update nimai_m_quotation set quotation_status='Withdrawn' where quotation_id=(:qid) ", nativeQuery = true)
	void updateStatusToWithdrawn(Integer qid);

	@Modifying
	@Query(value= "UPDATE nimai_subscription_details nsd SET\r\n" + 
			"nsd.LC_UTILIZED_COUNT=\r\n" + 
			"case \r\n" + 
			"when nsd.LC_COUNT>nsd.LC_UTILIZED_COUNT then \r\n" + 
			"nsd.LC_UTILIZED_COUNT-1\r\n" + 
			"when nsd.LC_COUNT=nsd.LC_UTILIZED_COUNT then\r\n" + 
			"nsd.LC_UTILIZED_COUNT"
			+ " when nsd.LC_COUNT<nsd.LC_UTILIZED_COUNT then\n"
			+ "nsd.LC_UTILIZED_COUNT-1\r\n" + 
			"end\r\n" + 
			"WHERE nsd.userid=(:userId) AND nsd.`STATUS`='Active'", nativeQuery = true)
	void refundCredit(String userId);
	
	@Modifying
	@Query(value= "UPDATE nimai_subscription_details nsd SET\r\n" + 
			"nsd.LC_UTILIZED_COUNT=\r\n" + 
			"case \r\n" + 
			"when nsd.LC_COUNT>nsd.LC_UTILIZED_COUNT then \r\n" + 
			"nsd.LC_UTILIZED_COUNT-1\r\n" + 
			"when nsd.LC_COUNT=nsd.LC_UTILIZED_COUNT then\r\n" + 
			"nsd.LC_UTILIZED_COUNT-1"
			+ " when nsd.LC_COUNT<nsd.LC_UTILIZED_COUNT then\n"
			+ "nsd.LC_UTILIZED_COUNT-1\r\n" + 
			"end\r\n" + 
			"WHERE nsd.userid=(:userId) AND nsd.`STATUS`='Active'", nativeQuery = true)
	void refundCreditWithdraw(String userId);

	/*@Modifying
	@Query(value= "UPDATE nimai_subscription_details nsd SET\r\n" + 
			"nsd.LC_UTILIZED_COUNT=\r\n" + 
			"case \r\n" + 
			"when nsd.LC_UTILIZED_COUNT=0 then \r\n" + 
			"1\r\n" + 
			"when nsd.LC_COUNT>nsd.LC_UTILIZED_COUNT then\r\n" + 
			"nsd.LC_UTILIZED_COUNT+1\r\n" + 
			"end\r\n" + 
			"WHERE nsd.userid=(:userId) AND nsd.`STATUS`='Active'", nativeQuery = true)
	void updateLCUtilizedByUserId(String userId);*/
	
	@Modifying
	@Query(value= "UPDATE nimai_subscription_details nsd SET\r\n" + 
			"nsd.LC_UTILIZED_COUNT=nsd.LC_UTILIZED_COUNT+1 WHERE nsd.userid=(:userId) AND nsd.`STATUS`='Active'", nativeQuery = true)
	void updateLCUtilizedByUserId(String userId);
	
	@Query(value = "SELECT userid from nimai_m_customer where account_source=(:userId) or userid=(:userId)", nativeQuery = true)
	List<NimaiClient> getAdditionalUserList(String userId);
	
	@Query(value="SELECT * from get_trans_quote_for_bank where (user_id like 'CU%' or user_id like 'BC%') and quotation_status like (%:status%) and bank_userid IN (:addUserList)", nativeQuery = true )
	List findTransQuoteDetByBankUserIdListAndStatus(List addUserList,
			@Param("status") String status);
	
	@Query(value="SELECT * from get_trans_quote_for_bank where user_id like 'BA%' and quotation_status like (%:status%) and bank_userid IN (:addUserList)", nativeQuery = true )
	List findSecTransQuoteDetByBankUserIdListAndStatus(List addUserList,
			@Param("status") String status);
	
	@Query(
	      value = "SELECT qu.* from get_all_quotation qu\r\nwhere qu.userid=(:userId)  \r\n\t\tand qu.transaction_id=(:transactionId) \r\n\t\tand ((qu.validity_date>=curdate() or qu.validity_date<curdate()) and  \r\n\t\t(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn') \r\n\t\tOR qu.quotation_status IS NULL)) group by qu.bank_userid order by qu.total_quote_value;",
	      nativeQuery = true
	)
	List<QuotationMaster> findAllSecQuotationByUserIdAndTransactionId(@Param("userId") String userId, @Param("transactionId") String transactionId);


	@Query(value = "select * from nimai_m_quotation nmq where nmq.transaction_id=(:transactionId) and quotation_status like '%Placed' and \r\n" + 
			"(nmq.bank_userid=(:bankUserId) or nmq.bank_userid in\r\n" + 
			"(select nmc.USERID from nimai_m_customer nmc where nmc.ACCOUNT_SOURCE=(:bankUserId))"
			+ "or nmq.bank_userid in (select nmc.ACCOUNT_SOURCE from nimai_m_customer nmc where nmc.USERID=(:bankUserId)))", nativeQuery = true)
	List<QuotationMaster> getDataToCheckQuotationPlaced(String transactionId, String bankUserId);

	@Modifying
	@Query(value= "UPDATE nimai_subscription_details nsd SET\r\n" + 
			"nsd.LC_UTILIZED_COUNT=\r\n" + 
			"case \r\n" + 
			"when nsd.LC_UTILIZED_COUNT=0 then \r\n" + 
			"nsd.LC_UTILIZED_COUNT\r\n" + 
			"when nsd.LC_COUNT>nsd.LC_UTILIZED_COUNT then\r\n" + 
			"nsd.LC_UTILIZED_COUNT+1\r\n" + 
			"end\r\n" + 
			"WHERE nsd.userid=(:userId) AND nsd.`STATUS`='Active'", nativeQuery = true)
	void updateLCUtilizedByUserIdAfter4Reopen(String userId);

	@Query(value="SELECT * from get_all_quotation where quotation_id=(:quotationId) and quotation_status='Accepted'", nativeQuery = true )
	QuotationMaster findQuotationByAcceptedQuotationId(Integer quotationId);

	@Query(value="select cpb.bank_userid from customer_preferred_banks cpb\r\n" + 
			"where cpb.cust_userid=:userId and cpb.bank_userid=:bankUserId", nativeQuery = true )
	String getPreferredBank(String userId, String bankUserId);

	@Query(value="select br.rating from bank_rating br where br.bank_userid=:bankUserId", nativeQuery = true )
	String getRating(String bankUserId);

	@Modifying
	@Query(value= "UPDATE nimai_m_quotation set acceptance_reason=(:acceptanceReason) where quotation_id=(:quotationId)", nativeQuery = true)
	void updateQuotationAcceptanceReason(Integer quotationId, String acceptanceReason);

	

	@Query(value= "SELECT * FROM nimai_m_quotation nq WHERE nq.transaction_id=:transactionId \n"
			+ " ORDER BY nq.inserted_date DESC LIMIT 1", nativeQuery = true)
	QuotationMaster findTransactionDetDisById(String transactionId);
	
	
	@Modifying
	@Query(
	      value = "update nimai_m_quotation SET quotation_status=\r\ncase when quotation_status='FreezePlaced' then \r\n'FreezeExpired'\r\nELSE\r\n'Expired'\r\nend,expired_on=NOW() where transaction_id=(:transId) and quotation_id!=(:quotationId) and quotation_status NOT IN ('Withdrawn','Accepted')",
	      nativeQuery = true
	)
	void updateSecQuotationStatusToExpired(String transId, Integer quotationId);

	@Query(
	    value = "SELECT qu.* from get_all_quotation qu\r\nwhere qu.userid=(:userId)  \r\n\t\tand qu.transaction_id=(:transactionId) \r\n\t\tand ((qu.validity_date>=curdate() or qu.validity_date<curdate()) and  \r\n\t\t(qu.quotation_status NOT IN ('Rejected','Expired','ExpPlaced','Withdrawn') \r\n\t\tOR qu.quotation_status IS NULL)) group by qu.bank_userid order by qu.total_quote_value;",
	    nativeQuery = true
	)
	List<QuotationMaster> findAllSecQuotationByUserIdAndTransactionIdExpPreferred(@Param("userId") String userId, @Param("transactionId") String transactionId);

	@Query(value= "SELECT * FROM nimai_m_quotation nq WHERE nq.transaction_id=:transactionId and nq.userid=:userId "
			+ "and nq.quotation_status like '%Placed%'", nativeQuery = true)
	List<QuotationMaster> getQuotationDetailsForWithdrawl(String transactionId, String userId);

	
	//@Query(value = "insert into nimai_m_savings_input values (:lcCountry,:lcCurrency,:updatedAnnualAssetValue,:updatedNetRevenue)", nativeQuery = true)
	//void saveDetailsToSavingInput(String lcCountry, String lcCurrency, Double updatedAnnualAssetValue,
	//		Double updatedNetRevenue);
}
