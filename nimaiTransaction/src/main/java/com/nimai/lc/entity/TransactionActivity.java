package com.nimai.lc.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="transaction_activity")
public class TransactionActivity 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;
	
	@Column(name="userid")
	private String userId;
	
	@Column(name="transaction_id")
	private String txnId;
	
	@Column(name="placed_by")
	private String placedBy;
	
	@Column(name="accept_by")
	private String acceptBy;
	
	@Column(name="reject_by")
	private String rejectBy;
	
	@Column(name="reopen_by")
	private String reopenBy;
	
	@Column(name="cancel_by")
	private String cancelBy;
	
	@Column(name="inserted_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date insertedDate;
	
	@Column(name="modified_date")
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
