package com.nimai.lc.service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.nimai.lc.bean.BankDetailsBean;
import com.nimai.lc.bean.NimaiLCMasterBean;
import com.nimai.lc.bean.QuotationBean;
import com.nimai.lc.bean.QuotationMasterBean;
import com.nimai.lc.bean.TransactionQuotationBean;
import com.nimai.lc.entity.NimaiClient;
import com.nimai.lc.entity.NimaiLCMaster;
import com.nimai.lc.entity.OfflineTxnBank;
import com.nimai.lc.entity.Quotation;
import com.nimai.lc.entity.QuotationMaster;

public interface QuotationService 
{
	public Integer saveQuotationdetails(QuotationBean quotationbean);
	public QuotationBean confirmQuotationdetails(QuotationBean quotationbean);
	public HashMap<String, Integer> calculateQuote(Integer quotationId,String transId,String tableType);
	public void quotePlace(String transId);
	public int getQuotationdetailsToCount(String transId);
	public List<Quotation> getAllDraftQuotationDetails(String userId);
	public List<QuotationMaster> getQuotationDetailByUserIdAndStatus(String userId,String status);
	public List<QuotationMaster> getAllQuotationDetails(String userId);
	public void updateDraftQuotationDet(QuotationBean quotationbean);
	public void moveQuoteToHistory(Integer quotationId,String transId, String userId);
	public void updateQuotationMasterDetails(QuotationMasterBean quotationbean) throws ParseException;
	public Quotation getSpecificDraftQuotationDetail(Integer quotationId);
	public List<QuotationMasterBean> getQuotationDetailByUserIdAndTransactionId(String userId, String transactionId);
	public List<QuotationMaster> getQuotationDetailByQuotationId(Integer quotationId);
	//public void updateQuotationForReject(Integer quotationId,String userId, String statusReason);
	public List<QuotationMaster> getQuotationDetailByBankUserId(String bankUserId);
	public List<String> getSavingsByUserId(String bankUserId);
	public List<String> getTotalSavings(String bankUserId);
	public List<String> getTotalSavingsUserId(String ccy,String bankUserId);
	public void updateQuotationForAccept(Integer quotationId,String transId, String userId, NimaiLCMaster transDetails,QuotationBean quotationbean);
	//public List<TransactionQuotationBean> getTransactionQuotationDetailByBankUserIdAndStatus(String bankUserId, String quotationPlaced, String transactionStatus) throws ParseException;
	public List<TransactionQuotationBean> getTransactionQuotationDetailByBankUserIdAndStatus(String bankUserId,String quotationStatus) throws ParseException;
	public List<TransactionQuotationBean> getTransactionQuotationDetailByQId(int qId) throws NumberFormatException,ParseException;	
	public List<Quotation> getAllDraftQuotationDetailsByBankUserId(String bankUserId);
	public HashMap<String, Integer> calculateQuote(Integer quotationId, String transactionId);
	public String findBankUserIdByQuotationId(Integer quotationId);
	public HashMap<String, String> getBankDetailsByBankUserId(String bankUserId);
	public List<BankDetailsBean> getBankDet(String bankUserId);
	public List<TransactionQuotationBean> getAllDraftTransQuotationDetailsByBankUserId(String bankUserId) throws NumberFormatException, ParseException;
	public Quotation findDraftQuotation(String transId, String userId, String bankUserId);
	public int findQuotationId(String transId, String userId, String bankUserId);
	public List<QuotationMaster> getDetailsOfAcceptedTrans(String transId, String userId);
	public Integer getRejectedQuotationByTransactionId(String transactionId);
	public void updateQuotationStatusForReopenToRePlaced(Integer qid, String transactionId) throws ParseException;
	public List<QuotationMaster> getQuotationDetailByUserIdAndTransactionIdStatus(String userId, String transactionId,String status);
	public QuotationMaster getQuotationOfAcceptedQuote(String transactionId);
	public void sendMailToBank(QuotationBean quotationBean, String string);
	public Quotation getDraftQuotationDetails(Integer quotationId);
	public void deleteDraftQuotation(Integer quotationId);
	public String getTransactionId(Integer quotationId);
	public String getUserId(Integer quotationId);
	public Double getQuoteValueByQid(Integer quotationId);
	public void updateQuotationStatusForFreezeToPlaced(String transactionId, String bankUserId);
	public Integer getQuotationIdByTransIdUserId(String transactionId, String userId, String status);
	public String getBankUserIdByQId(Integer quotationId);
	public boolean withdrawQuoteByQid(QuotationBean quotationbean, String obtainUserId, QuotationMaster qm);
	public String calculateSavingPercent(String transId, Integer quotationId, String userId);
	public boolean checkDataForSaving(String lcCountry, String lcCurrency);
	public List<QuotationMaster> checkQuotationPlacedOrNot(String transactionId, String bankUserId);
	List<QuotationMaster> getDetailsOfAcceptedTrans(String transId);
	public QuotationMaster getQuotationDetailByAcceptedQuotationId(Integer quotationId);
	List<QuotationMasterBean> getQuotationDetailByQuotationIdUserIdAndTransactionId(Integer quoteId, String userId,
			String transactionId);
	public List<TransactionQuotationBean> getAllSecondaryDraftTransQuotationDetailsByBankUserId(String bankUserId) throws NumberFormatException, ParseException;
	public int updateSecQuotationForAccept(Integer quotationId, String transId, String userId,
			NimaiLCMaster transDetails, Integer flag, NimaiClient obtainUserId);
	List<TransactionQuotationBean> getSecTransactionQuotationDetailByBankUserIdAndStatus(String bankUserId,
			String quotationStatus) throws NumberFormatException, ParseException;
	public void updateAcceptanceReason(Integer quotationId,String acceptanceReason);
	Integer saveQuotationdetailsForOffered(QuotationBean quotationbean);
	void updateDraftQuotationDetForOffered(QuotationBean quotationbean);
	List<QuotationMaster> getNewQuotationDetailByUserIdAndTransactionIdStatus(String userId, String transactionId,
			String status);
	List<QuotationMaster> getSecQuotationDetailByUserIdAndTransactionIdStatus(Integer quoteId, String userId,
			String transactionId, String status);
	public void updateSecQuotationForReject(Integer quotationId, String userId, String statusReason,NimaiLCMasterBean nimailcmasterbean);
	void updateQuotationForReject(Integer quotationId, String userId, String transId, String statusReason,NimaiLCMasterBean nimailcmasterbean);
	public List<OfflineTxnBank> getAllOffTrxnByUserId(String userId);
	public void updateOffSecQuotationForAccept(QuotationBean quotationbean);
	
}
