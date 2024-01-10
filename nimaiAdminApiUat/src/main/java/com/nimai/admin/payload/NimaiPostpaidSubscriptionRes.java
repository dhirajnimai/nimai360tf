package com.nimai.admin.payload;

public class NimaiPostpaidSubscriptionRes {

	private int postPaidId;
	private String makerComment;
	private String checkerComment;
	private String paymentStatus;
	private Double minDue;
	private Double totalDue;
	private Double totalPayment;
	private String transactioId;
	private String invoiceId;
	private String checkerApprovedBy;
	private String makerApprovedby;
	private String dueType;
	private String postPaidIdString;
    private String paymentMode;
    private String posBenefits;
    private Double finalTotalPayment;
    private Double finalminDue;
	private Double groupTotalPayment;
	private String userId;
	private String quotationStatus;
	
	

	public String getQuotationStatus() {
		return quotationStatus;
	}

	public void setQuotationStatus(String quotationStatus) {
		this.quotationStatus = quotationStatus;
	}

	public Double getGroupTotalPayment() {
		return groupTotalPayment;
	}

	public void setGroupTotalPayment(Double groupTotalPayment) {
		this.groupTotalPayment = groupTotalPayment;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Double getFinalminDue() {
		return finalminDue;
	}
	public void setFinalminDue(Double finalminDue) {
		this.finalminDue = finalminDue;
	}
	public Double getFinalTotalPayment() {
		return finalTotalPayment;
	}
	public void setFinalTotalPayment(Double finalTotalPayment) {
		this.finalTotalPayment = finalTotalPayment;
	}
	public String getPosBenefits() {
		return posBenefits;
	}
	public void setPosBenefits(String posBenefits) {
		this.posBenefits = posBenefits;
	}
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	public String getPostPaidIdString() {
		return postPaidIdString;
	}
	public void setPostPaidIdString(String postPaidIdString) {
		this.postPaidIdString = postPaidIdString;
	}
	public String getDueType() {
		return dueType;
	}
	public void setDueType(String dueType) {
		this.dueType = dueType;
	}
	public int getPostPaidId() {
		return postPaidId;
	}
	public void setPostPaidId(int postPaidId) {
		this.postPaidId = postPaidId;
	}
	public String getMakerComment() {
		return makerComment;
	}
	public void setMakerComment(String makerComment) {
		this.makerComment = makerComment;
	}
	public String getCheckerComment() {
		return checkerComment;
	}
	public void setCheckerComment(String checkerComment) {
		this.checkerComment = checkerComment;
	}
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
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
	public Double getTotalPayment() {
		return totalPayment;
	}
	public void setTotalPayment(Double totalPayment) {
		this.totalPayment = totalPayment;
	}
	public String getTransactioId() {
		return transactioId;
	}
	public void setTransactioId(String transactioId) {
		this.transactioId = transactioId;
	}
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public String getCheckerApprovedBy() {
		return checkerApprovedBy;
	}
	public void setCheckerApprovedBy(String checkerApprovedBy) {
		this.checkerApprovedBy = checkerApprovedBy;
	}
	public String getMakerApprovedby() {
		return makerApprovedby;
	}
	public void setMakerApprovedby(String makerApprovedby) {
		this.makerApprovedby = makerApprovedby;
	}
	
	
	
	
	
	
}
