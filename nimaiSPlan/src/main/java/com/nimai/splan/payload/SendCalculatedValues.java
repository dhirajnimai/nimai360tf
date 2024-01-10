package com.nimai.splan.payload;

public class SendCalculatedValues {
    private Double minDue;
    private Double totalDue;
    private Double perTxnDue;
    private Double totalPayment;
    private Integer paymentCounter;
    private String transactionId;
    private String userId;
    private Integer transCounter;


    private String payment_mode;

    private String payment_status;

    
    
    public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getTransCounter() {
        return transCounter;
    }

    public void setTransCounter(Integer transCounter) {
        this.transCounter = transCounter;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getMinDue() {
        return minDue;
    }

    public void setMinDue(Double minDue) {
        this.minDue = minDue;
    }

    public Double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(Double totalDue) {
        this.totalDue = totalDue;
    }

    public Double getPerTxnDue() {
        return perTxnDue;
    }

    public void setPerTxnDue(Double perTxnDue) {
        this.perTxnDue = perTxnDue;
    }

    public Double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(Double totalPayment) {
        this.totalPayment = totalPayment;
    }

    public Integer getPaymentCounter() {
        return paymentCounter;
    }

    public void setPaymentCounter(Integer paymentCounter) {
        this.paymentCounter = paymentCounter;
    }

    public String getPayment_mode() {
        return payment_mode;
    }

    public void setPayment_mode(String payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }
}
