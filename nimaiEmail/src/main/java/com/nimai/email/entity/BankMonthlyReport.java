package com.nimai.email.entity;

import java.util.Date;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="monthly_ba_report")
public class BankMonthlyReport {

	
	
	@Id
	@Basic(optional = false)
	@NotNull
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="ba_report_id")
	private int baReportId;
	
		
		@Column(name="user_id")
		private String userId;
		
		@Column(name="transaction_Received")
		private int transactionReceived;
		
		@Column(name="quote_Placed")
		private int quotePlaced;
		
		@Column(name="acceptedQuotes")
		private int acceptedQuotes;
		
		@Column(name="rejectdQuotes")
		private int rejectdQuotes;
		
		@Column(name="expired_Quotes")
		private int expired_Quotes;
		
		@Column(name="quotes_Withdrawn")
		private int quotesWithdrawn;
		
		@Column(name="credit_Remaining")
		private int creditRemaining;
		
		@Column(name="subsidiary_Slot")
		private int subsidiarySlot;
			
		@Column(name="email_status")
		private String emailstatus;
		
		@Column(name="inserted_date")
		private Date insertedDate;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public int getTransactionReceived() {
			return transactionReceived;
		}

		public void setTransactionReceived(int transactionReceived) {
			this.transactionReceived = transactionReceived;
		}

		public int getQuotePlaced() {
			return quotePlaced;
		}

		public void setQuotePlaced(int quotePlaced) {
			this.quotePlaced = quotePlaced;
		}

		public int getAcceptedQuotes() {
			return acceptedQuotes;
		}

		public void setAcceptedQuotes(int acceptedQuotes) {
			this.acceptedQuotes = acceptedQuotes;
		}

		public int getRejectdQuotes() {
			return rejectdQuotes;
		}

		public void setRejectdQuotes(int rejectdQuotes) {
			this.rejectdQuotes = rejectdQuotes;
		}

		public int getExpired_Quotes() {
			return expired_Quotes;
		}

		public void setExpired_Quotes(int expired_Quotes) {
			this.expired_Quotes = expired_Quotes;
		}

		public int getQuotesWithdrawn() {
			return quotesWithdrawn;
		}

		public void setQuotesWithdrawn(int quotesWithdrawn) {
			this.quotesWithdrawn = quotesWithdrawn;
		}

		public int getCreditRemaining() {
			return creditRemaining;
		}

		public void setCreditRemaining(int creditRemaining) {
			this.creditRemaining = creditRemaining;
		}

		public int getSubsidiarySlot() {
			return subsidiarySlot;
		}

		public void setSubsidiarySlot(int subsidiarySlot) {
			this.subsidiarySlot = subsidiarySlot;
		}

		public String getEmailstatus() {
			return emailstatus;
		}

		public void setEmailstatus(String emailstatus) {
			this.emailstatus = emailstatus;
		}

		public Date getInsertedDate() {
			return insertedDate;
		}

		public void setInsertedDate(Date insertedDate) {
			this.insertedDate = insertedDate;
		}

		
		
		
		
		public int getBaReportId() {
			return baReportId;
		}

		public void setBaReportId(int baReportId) {
			this.baReportId = baReportId;
		}

		@Override
		public String toString() {
			return "BankMonthlyReport [userId=" + userId + ", transactionReceived=" + transactionReceived
					+ ", quotePlaced=" + quotePlaced + ", acceptedQuotes=" + acceptedQuotes + ", rejectdQuotes="
					+ rejectdQuotes + ", expired_Quotes=" + expired_Quotes + ", quotesWithdrawn=" + quotesWithdrawn
					+ ", creditRemaining=" + creditRemaining + ", subsidiarySlot=" + subsidiarySlot + ", emailstatus="
					+ emailstatus + ", insertedDate=" + insertedDate + "]";
		}
		
		
		
}
