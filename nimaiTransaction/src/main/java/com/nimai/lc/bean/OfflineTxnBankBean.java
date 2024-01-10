package com.nimai.lc.bean;

public class OfflineTxnBankBean 
{
	private Long id;
	private String parentUserId;
	private String userId;
	private String emailId;
	private String txnId;
	private String quotationStatus;
	
	
	
	
	
	public String getQuotationStatus() {
		return quotationStatus;
	}
	public void setQuotationStatus(String quotationStatus) {
		this.quotationStatus = quotationStatus;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getParentUserId() {
		return parentUserId;
	}
	public void setParentUserId(String parentUserId) {
		this.parentUserId = parentUserId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	
	
	
}
