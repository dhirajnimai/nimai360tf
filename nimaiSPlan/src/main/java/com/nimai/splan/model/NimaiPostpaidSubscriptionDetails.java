package com.nimai.splan.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

@Entity
@Table(name = "NIMAI_POSTPAID_SUBSCRIPTION_DETAILS")
public class NimaiPostpaidSubscriptionDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "POSTPAID_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer postpaidId;

    @JoinColumn(name = "SUBSCRIPTION_DETAILS_ID", referencedColumnName = "SPL_SERIAL_NUMBER")
    @ManyToOne(optional = false)
    private NimaiSubscriptionDetails subscriptionDetailsId ;

    @JoinColumn(name = "USERID", referencedColumnName = "USERID")
    @ManyToOne(optional = false)
    private NimaiSubscriptionDetails userId;

    @Column(name = "TXN_ID")
    private String transactionId ;

    @Column(name = "Due_type")
    private String Due_type ;

    @Column(name = "TOTAL_DUE")
    private Double totalDue;

    @Column(name = "MIN_DUE")
    private Double minDue;

    @Column(name = "PER_TRANSACTION_DUE")
    private Double perTransactionDue;


    @Column(name = "TOTAL_PAYMENT")
    private Double totalPayment;

    @JoinColumn(name = "INVOICE_ID", referencedColumnName = "INVOICE_ID")
    @ManyToOne(optional = false)
    private NimaiSubscriptionDetails invoiceId;

    @Column(name = "PAYMENT_TXN_ID")
    private String paymentTxnId;

    @Column(name = "POSTPAID_START_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date postpaidStartDate;

    @Column(name = "MAKER_APPROVAL_BY")
    private String makerApprovalBy;

    @Column(name = "CHECKER_APPROVAL_BY")
    private String checkerApprovalBy;

    @Column(name = "MAKER_APPROVAL_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date makerApprovalDate;

    @Column(name = "payment_mode")
    private String modeOfPayment;

    @Column(name = "checker_comment")
    private String checkerComment;
    
    @Column(name = "maker_comment")
    private String makerComment;
    
    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_approved_by")
    private String paymentApprovedBy;
    
    @Column(name = "CHECKER_APPROVAL_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date checkerApprovalDate;


    @Column(name = "PAYMENT_COUNTER")
    private String paymentCounter;


    @Column(name = "STATUS")
    private String status;

    @Column(name = "REMARK")
    private String remark;

    @Column(name = "Discount_Id")
    private Integer disountId;

    @Column(name = "Discount_Amount")
    private Double discountAmnt;

    private String email_account;


    
    
    
    


	public Integer getDisountId() {
        return disountId;
    }


    public String getEmail_account() {
        return email_account;
    }

    public void setEmail_account(String email_account) {
        this.email_account = email_account;
    }

    public void setDisountId(Integer disountId) {
        this.disountId = disountId;
    }


    public Double getDiscountAmnt() {
        return discountAmnt;
    }

    public void setDiscountAmnt(Double discountAmnt) {
        this.discountAmnt = discountAmnt;
    }

    public Integer getPostpaidId() {
        return postpaidId;
    }

    public void setPostpaidId(Integer postpaidId) {
        this.postpaidId = postpaidId;
    }

    public NimaiSubscriptionDetails getSubscriptionDetailsId() {
        return subscriptionDetailsId;
    }

    public void setSubscriptionDetailsId(NimaiSubscriptionDetails subscriptionDetailsId) {
        this.subscriptionDetailsId = subscriptionDetailsId;
    }

    public NimaiSubscriptionDetails getUserId() {
        return userId;
    }

    public void setUserId(NimaiSubscriptionDetails userId) {
        this.userId = userId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(Double totalDue) {
        this.totalDue = totalDue;
    }

    public Double getMinDue() {
        return minDue;
    }

    public void setMinDue(Double minDue) {
        this.minDue = minDue;
    }

    public Double getPerTransactionDue() {
        return perTransactionDue;
    }

    public void setPerTransactionDue(Double perTransactionDue) {
        this.perTransactionDue = perTransactionDue;
    }

    public Double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(Double totalPayment) {
        this.totalPayment = totalPayment;
    }

    public NimaiSubscriptionDetails getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(NimaiSubscriptionDetails invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getPaymentTxnId() {
        return paymentTxnId;
    }

    public void setPaymentTxnId(String paymentTxnId) {
        this.paymentTxnId = paymentTxnId;
    }

    public Date getPostpaidStartDate() {
        return postpaidStartDate;
    }

    public void setPostpaidStartDate(Date postpaidStartDate) {
        this.postpaidStartDate = postpaidStartDate;
    }

    public String getMakerApprovalBy() {
        return makerApprovalBy;
    }

    public void setMakerApprovalBy(String makerApprovalBy) {
        this.makerApprovalBy = makerApprovalBy;
    }

    public String getCheckerApprovalBy() {
        return checkerApprovalBy;
    }

    public void setCheckerApprovalBy(String checkerApprovalBy) {
        this.checkerApprovalBy = checkerApprovalBy;
    }

    public Date getMakerApprovalDate() {
        return makerApprovalDate;
    }

    public void setMakerApprovalDate(Date makerApprovalDate) {
        this.makerApprovalDate = makerApprovalDate;
    }

    public Date getCheckerApprovalDate() {
        return checkerApprovalDate;
    }

    public void setCheckerApprovalDate(Date checkerApprovalDate) {
        this.checkerApprovalDate = checkerApprovalDate;
    }

    public String getPaymentCounter() {
        return paymentCounter;
    }

    public String getDue_type() {
        return Due_type;
    }

    public void setDue_type(String due_type) {
        Due_type = due_type;
    }

    public void setPaymentCounter(String paymentCounter) {
        this.paymentCounter = paymentCounter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    
    public String getModeOfPayment() {
		return modeOfPayment;
	}

	public void setModeOfPayment(String modeOfPayment) {
		this.modeOfPayment = modeOfPayment;
	}

	@Override
    public String toString() {
        return "NimaiPostpaidSubscriptionDetails{" +
                "postpaidId=" + postpaidId +
                ", subscriptionDetailsId=" + subscriptionDetailsId +
                ", userId=" + userId +
                ", transactionId='" + transactionId + '\'' +
                ", totalDue=" + totalDue +
                ", minDue=" + minDue +
                ", perTransactionDue=" + perTransactionDue +
                ", totalPayment=" + totalPayment +
                ", invoiceId=" + invoiceId +
                ", paymentTxnId='" + paymentTxnId + '\'' +
                ", postpaidStartDate=" + postpaidStartDate +
                ", makerApprovalBy='" + makerApprovalBy + '\'' +
                ", checkerApprovalBy='" + checkerApprovalBy + '\'' +
                ", makerApprovalDate=" + makerApprovalDate +
                ", checkerApprovalDate=" + checkerApprovalDate +
                ", paymentCounter='" + paymentCounter + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }

	public String getCheckerComment() {
		return checkerComment;
	}

	public void setCheckerComment(String checkerComment) {
		this.checkerComment = checkerComment;
	}

	public String getMakerComment() {
		return makerComment;
	}

	public void setMakerComment(String makerComment) {
		this.makerComment = makerComment;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentApprovedBy() {
		return paymentApprovedBy;
	}

	public void setPaymentApprovedBy(String paymentApprovedBy) {
		this.paymentApprovedBy = paymentApprovedBy;
	}
	
	
}
