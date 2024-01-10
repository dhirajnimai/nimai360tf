package com.nimai.lc.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nimai_offline_txn_bank")
public class OfflineTxnBank 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	private Long id;
	
	@Column(name="parent_userid")
	private String parentUserId;
	
	@Column(name="userid")
	private String userId;
	
	@Column(name="emailid")
	private String emailId;

	@Column(name="txnid")
	private String txnId;
	
	@Column(name="Quotation_Placed_BY")
	private String quPlacedby;
	
	
	@Column(name="Quotation_Status")
	private String  quotationStatus;
	
	@Column(name="Quotation_Accepted_BY")
	private String quAcceptedBy;
	
	@Column(name="Quotation_Rejected_BY")
	private String quRejectedby;

	@Column(name="transaction_placed_user_id")
	private String trPlacedUser;

	
	@Column(name="Quotation_Withdrawn_BY")
	private String withdrawnBy;
	
	
	@Column(name="select_bank_user_id")
	private String seBnkUsrId;
	
	
	
	
	
	public String getWithdrawnBy() {
		return withdrawnBy;
	}

	public void setWithdrawnBy(String withdrawnBy) {
		this.withdrawnBy = withdrawnBy;
	}

	public String getSeBnkUsrId() {
		return seBnkUsrId;
	}

	public void setSeBnkUsrId(String seBnkUsrId) {
		this.seBnkUsrId = seBnkUsrId;
	}

	public String getQuotationStatus() {
		return quotationStatus;
	}

	public void setQuotationStatus(String quotationStatus) {
		this.quotationStatus = quotationStatus;
	}

	public String getTrPlacedUser() {
		return trPlacedUser;
	}

	public void setTrPlacedUser(String trPlacedUser) {
		this.trPlacedUser = trPlacedUser;
	}

	public String getQuPlacedby() {
		return quPlacedby;
	}

	public void setQuPlacedby(String quPlacedby) {
		this.quPlacedby = quPlacedby;
	}

	public String getQuAcceptedBy() {
		return quAcceptedBy;
	}

	public void setQuAcceptedBy(String quAcceptedBy) {
		this.quAcceptedBy = quAcceptedBy;
	}

	public String getQuRejectedby() {
		return quRejectedby;
	}

	public void setQuRejectedby(String quRejectedby) {
		this.quRejectedby = quRejectedby;
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
