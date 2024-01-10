package com.nimai.admin.model;

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

    @Column(name = "TOTAL_DUE")
  
    private Double totalDue;

    @Column(name = "MIN_DUE")
    private Double minDue;

    @Column(name = "PER_TRANSACTION_DUE")
    private Double perTransactionDue;


    @Column(name = "TOTAL_PAYMENT")
    private Double totalPayment;

  
    @Column(name = "invoice_id")
    private String invoiceId;

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

    @Column(name = "CHECKER_APPROVAL_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date checkerApprovalDate;


    @Column(name = "PAYMENT_COUNTER")
    private String paymentCounter;


    @Column(name = "STATUS")
    private String status;

    @Column(name = "REMARK")
    private String remark;
    
    @Column(name = "checker_comment")
    private String checkerComment;
    
    @Column(name = "maker_comment")
    private String makerComment;
    
    @Column(name = "PAYMENT_MODE")
    private String paymentmode;
    
    @Column(name = "PAYMENT_STATUS")
    private String paymentStatus;

    
    @Column(name = "PAYMENT_APPROVED_BY")
    private String paymentApprovedby;
    
    @Column(name="Due_Type")
    private String dueType;
    
    @Column(name="Discount_Id")
    private Integer discountId;
    
    @Column(name="Discount_Amount")
    private Double discountAmount;
    
    
    
    

    public Integer getDiscountId() {
		return discountId;
	}

	public void setDiscountId(Integer discountId) {
		this.discountId = discountId;
	}

	public Double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
	}

	public String getDueType() {
		return dueType;
	}

	public void setDueType(String dueType) {
		this.dueType = dueType;
	}

	public String getPaymentApprovedby() {
		return paymentApprovedby;
	}

	public void setPaymentApprovedby(String paymentApprovedby) {
		this.paymentApprovedby = paymentApprovedby;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
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

	public String getPaymentmode() {
		return paymentmode;
	}

	public void setPaymentmode(String paymentmode) {
		this.paymentmode = paymentmode;
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



    public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
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

	@Override
	public String toString() {
		return "NimaiPostpaidSubscriptionDetails [postpaidId=" + postpaidId + ", subscriptionDetailsId="
				+ subscriptionDetailsId + ", userId=" + userId + ", transactionId=" + transactionId + ", totalDue="
				+ totalDue + ", minDue=" + minDue + ", perTransactionDue=" + perTransactionDue + ", totalPayment="
				+ totalPayment + ", invoiceId=" + invoiceId + ", paymentTxnId=" + paymentTxnId + ", postpaidStartDate="
				+ postpaidStartDate + ", makerApprovalBy=" + makerApprovalBy + ", checkerApprovalBy="
				+ checkerApprovalBy + ", makerApprovalDate=" + makerApprovalDate + ", checkerApprovalDate="
				+ checkerApprovalDate + ", paymentCounter=" + paymentCounter + ", status=" + status + ", remark="
				+ remark + ", checkerComment=" + checkerComment + ", makerComment=" + makerComment + ", paymentmode="
				+ paymentmode + ", paymentStatus=" + paymentStatus + ", paymentApprovedby=" + paymentApprovedby
				+ ", dueType=" + dueType + ", discountId=" + discountId + ", discountAmount=" + discountAmount + "]";
	}


}
