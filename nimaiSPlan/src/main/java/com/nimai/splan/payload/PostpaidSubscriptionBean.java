package com.nimai.splan.payload;

public class PostpaidSubscriptionBean {

    private String postpaidId;
    private String subscriptionDetailsId;
    private String userId;

    private String transactionId;

    private Double totalDue;
    private Double minDue;
    private Double perTransactionDue;
    private Double totalPayment;
    private String invoiceId;
    private String paymentTxnId;

    private String postpaidStartDate;
    private String makerApprovalBy;
    private String checkerApprovalBy;
    private String makerApprovalDate;
    private String checkerApprovalDate;
    private String paymentCounter;
    private String status;
    private String remark;

    public String getPostpaidId() {
        return postpaidId;
    }

    public void setPostpaidId(String postpaidId) {
        this.postpaidId = postpaidId;
    }

    public String getSubscriptionDetailsId() {
        return subscriptionDetailsId;
    }

    public void setSubscriptionDetailsId(String subscriptionDetailsId) {
        this.subscriptionDetailsId = subscriptionDetailsId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public String getPostpaidStartDate() {
        return postpaidStartDate;
    }

    public void setPostpaidStartDate(String postpaidStartDate) {
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

    public String getMakerApprovalDate() {
        return makerApprovalDate;
    }

    public void setMakerApprovalDate(String makerApprovalDate) {
        this.makerApprovalDate = makerApprovalDate;
    }

    public String getCheckerApprovalDate() {
        return checkerApprovalDate;
    }

    public void setCheckerApprovalDate(String checkerApprovalDate) {
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
        return "postpaidSubscriptionBean{" +
                "postpaidId='" + postpaidId + '\'' +
                ", subscriptionDetailsId='" + subscriptionDetailsId + '\'' +
                ", userId='" + userId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", totalDue=" + totalDue +
                ", minDue=" + minDue +
                ", perTransactionDue=" + perTransactionDue +
                ", totalPayment=" + totalPayment +
                ", invoiceId='" + invoiceId + '\'' +
                ", paymentTxnId='" + paymentTxnId + '\'' +
                ", postpaidStartDate='" + postpaidStartDate + '\'' +
                ", makerApprovalBy='" + makerApprovalBy + '\'' +
                ", checkerApprovalBy='" + checkerApprovalBy + '\'' +
                ", makerApprovalDate='" + makerApprovalDate + '\'' +
                ", checkerApprovalDate='" + checkerApprovalDate + '\'' +
                ", paymentCounter='" + paymentCounter + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
