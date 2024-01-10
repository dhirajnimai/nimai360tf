package com.nimai.lc.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class TransactionActivityBean {

	private Integer id;
	private String userId;
	private String txnId;
	private String placedBy;
	private String acceptBy;
	private String rejectBy;
	private String reopenBy;
	private String cancelBy;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date insertedDate;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date modifiedDate;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getPlacedBy() {
		return placedBy;
	}
	public void setPlacedBy(String placedBy) {
		this.placedBy = placedBy;
	}
	public String getAcceptBy() {
		return acceptBy;
	}
	public void setAcceptBy(String acceptBy) {
		this.acceptBy = acceptBy;
	}
	public String getRejectBy() {
		return rejectBy;
	}
	public void setRejectBy(String rejectBy) {
		this.rejectBy = rejectBy;
	}
	public String getReopenBy() {
		return reopenBy;
	}
	public void setReopenBy(String reopenBy) {
		this.reopenBy = reopenBy;
	}
	public String getCancelBy() {
		return cancelBy;
	}
	public void setCancelBy(String cancelBy) {
		this.cancelBy = cancelBy;
	}
	public Date getInsertedDate() {
		return insertedDate;
	}
	public void setInsertedDate(Date insertedDate) {
		this.insertedDate = insertedDate;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	
		
		
}
