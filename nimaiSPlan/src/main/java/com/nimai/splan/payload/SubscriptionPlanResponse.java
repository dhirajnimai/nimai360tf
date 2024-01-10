package com.nimai.splan.payload;

import java.util.Date;

public class SubscriptionPlanResponse {
  private String userId;
  
  private String subscriptionId;
  
  private String subscriptionName;
  
  private int subscriptionAmount;
  
  private String lcCount;
  
  private String remark;
  
  private String status;
  
  private int subscriptionValidity;
  
  private String subsidiaries;
  
  private String relationshipManager;
  
  private String customerSupport;
  
  private int vasAmount;
  
  private Double discount;
  
  private Double grandAmount;
  
  private int isVasApplied;
  
  private int discountId;
  
  private Date subsStartDate;
  
  private String invoiceId;
  
  private String paymentStatus;
  
  
  
  public String getPaymentStatus() {
	return paymentStatus;
}

public void setPaymentStatus(String paymentStatus) {
	this.paymentStatus = paymentStatus;
}

public String getInvoiceId() {
    return this.invoiceId;
  }
  
  public void setInvoiceId(String invoiceId) {
    this.invoiceId = invoiceId;
  }
  
  public Date getSubsStartDate() {
    return this.subsStartDate;
  }
  
  public void setSubsStartDate(Date subsStartDate) {
    this.subsStartDate = subsStartDate;
  }
  
  public int getDiscountId() {
    return this.discountId;
  }
  
  public void setDiscountId(int discountId) {
    this.discountId = discountId;
  }
  
  public int getIsVasApplied() {
    return this.isVasApplied;
  }
  
  public void setIsVasApplied(int isVasApplied) {
    this.isVasApplied = isVasApplied;
  }
  
  public int getVasAmount() {
    return this.vasAmount;
  }
  
  public void setVasAmount(int vasAmount) {
    this.vasAmount = vasAmount;
  }
  
  public Double getDiscount() {
    return this.discount;
  }
  
  public void setDiscount(Double discount) {
    this.discount = discount;
  }
  
  public Double getGrandAmount() {
    return this.grandAmount;
  }
  
  public void setGrandAmount(Double grandAmount) {
    this.grandAmount = grandAmount;
  }
  
  public int getSubscriptionValidity() {
    return this.subscriptionValidity;
  }
  
  public void setSubscriptionValidity(int subscriptionValidity) {
    this.subscriptionValidity = subscriptionValidity;
  }
  
  public String getSubsidiaries() {
    return this.subsidiaries;
  }
  
  public void setSubsidiaries(String subsidiaries) {
    this.subsidiaries = subsidiaries;
  }
  
  public String getRelationshipManager() {
    return this.relationshipManager;
  }
  
  public void setRelationshipManager(String relationshipManager) {
    this.relationshipManager = relationshipManager;
  }
  
  public String getCustomerSupport() {
    return this.customerSupport;
  }
  
  public void setCustomerSupport(String customerSupport) {
    this.customerSupport = customerSupport;
  }
  
  public String getUserId() {
    return this.userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public String getSubscriptionId() {
    return this.subscriptionId;
  }
  
  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }
  
  public String getSubscriptionName() {
    return this.subscriptionName;
  }
  
  public void setSubscriptionName(String subscriptionName) {
    this.subscriptionName = subscriptionName;
  }
  
  public int getSubscriptionAmount() {
    return this.subscriptionAmount;
  }
  
  public void setSubscriptionAmount(int subscriptionAmount) {
    this.subscriptionAmount = subscriptionAmount;
  }
  
  public String getLcCount() {
    return this.lcCount;
  }
  
  public void setLcCount(String lcCount) {
    this.lcCount = lcCount;
  }
  
  public String getRemark() {
    return this.remark;
  }
  
  public void setRemark(String remark) {
    this.remark = remark;
  }
  
  public String getStatus() {
    return this.status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
}
