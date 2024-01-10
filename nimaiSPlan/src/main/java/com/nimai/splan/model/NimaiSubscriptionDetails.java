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
@Table(name = "NIMAI_SUBSCRIPTION_DETAILS")
public class NimaiSubscriptionDetails implements Serializable {
  private static final long serialVersionUID = 1L;
  
  @Id
  @Column(name = "SPL_SERIAL_NUMBER")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer sPlSerialNUmber;
  
  @Column(name = "SUBSCRIPTION_ID")
  private String subscriptionId;
  
  @Column(name = "SUBSCRIPTION_NAME")
  private String subscriptionName;
  
  @Column(name = "SUBSCRIPTION_AMOUNT")
  private int subscriptionAmount;
  
  @Column(name = "LC_COUNT")
  private String lCount;
  
  @Column(name = "REMARK")
  private String remark;
  
  @Column(name = "STATUS")
  private String status;
  
  @Column(name = "SUBSCRIPTION_VALIDITY")
  private int subscriptionValidity;
  
  @Column(name = "INSERTED_BY")
  private String insertedBy;
  
  @Column(name = "MODIFIED_BY")
  private String modifiedBy;
  
  @Column(name = "SPLAN_START_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date subscriptionStartDate;
  
  @Column(name = "SPLAN_END_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date subscriptionEndDate;
  
  @Column(name = "SUBSIDIARIES")
  private String subsidiaries;
  
  @Column(name = "RELATIONSHIP_MANAGER")
  private String relationshipManager;
  
  @Column(name = "CUSTOMER_SUPPORT")
  private String customerSupport;
  
  @Column(name = "FLAG")
  private int flag;
  
  @JoinColumn(name = "USERID", referencedColumnName = "USERID")
  @ManyToOne(optional = false)
  private NimaiMCustomer userid;
  
  @Column(name = "CUSTOMER_TYPE")
  private String customerType;
  
  @Column(name = "COUNTRY_NAME")
  private String sPLanCountry;
  
  @Column(name = "LC_UTILIZED_COUNT")
  private int lcUtilizedCount;
  
  @Column(name = "SUBSIDIARIES_UTILIZED_COUNT")
  private int subsidiaryUtilizedCount;
  
  @Column(name = "IS_VAS_APPLIED")
  private int isVasApplied;
  
  @Column(name = "VAS_AMOUNT")
  private int vasAmount;
  
  @Column(name = "GRAND_AMOUNT")
  private Double grandAmount;
  
  @Column(name = "KYC_COUNT")
  private int kycCount;
  
  @Column(name = "DISCOUNT")
  private Double discount;
  
  @Column(name = "DISCOUNT_ID")
  private int discountId;
  
  @Column(name = "RENEWAL_EMAIL_STATUS")
  private String renewalEmailStatus;
  
  @Column(name = "INSERTED_DATE")
  private Date insertedDate;
  
  @Column(name = "PAYMENT_MODE")
  private String paymentMode;
  
  @Column(name = "PAYMENT_STATUS")
  private String paymentStatus;
  
  @Column(name = "INVOICE_ID")
  private String invoiceId;
  
  public String getInvoiceId() {
    return this.invoiceId;
  }
  
  public void setInvoiceId(String invoiceId) {
    this.invoiceId = invoiceId;
  }
  
  public String getPaymentMode() {
    return this.paymentMode;
  }
  
  public void setPaymentMode(String paymentMode) {
    this.paymentMode = paymentMode;
  }
  
  public String getPaymentStatus() {
    return this.paymentStatus;
  }
  
  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }
  
  public Date getInsertedDate() {
    return this.insertedDate;
  }
  
  public void setInsertedDate(Date insertedDate) {
    this.insertedDate = insertedDate;
  }
  
  public String getRenewalEmailStatus() {
    return this.renewalEmailStatus;
  }
  
  public void setRenewalEmailStatus(String renewalEmailStatus) {
    this.renewalEmailStatus = renewalEmailStatus;
  }
  
  public int getDiscountId() {
    return this.discountId;
  }
  
  public void setDiscountId(int discountId) {
    this.discountId = discountId;
  }
  
  public Double getDiscount() {
    return this.discount;
  }
  
  public void setDiscount(Double discount) {
    this.discount = discount;
  }
  
  public int getLcUtilizedCount() {
    return this.lcUtilizedCount;
  }
  
  public void setLcUtilizedCount(int lcUtilizedCount) {
    this.lcUtilizedCount = lcUtilizedCount;
  }
  
  public int getSubsidiaryUtilizedCount() {
    return this.subsidiaryUtilizedCount;
  }
  
  public void setSubsidiaryUtilizedCount(int subsidiaryUtilizedCount) {
    this.subsidiaryUtilizedCount = subsidiaryUtilizedCount;
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
  
  public Double getGrandAmount() {
    return this.grandAmount;
  }
  
  public void setGrandAmount(Double grandAmount) {
    this.grandAmount = grandAmount;
  }
  
  public int getKycCount() {
    return this.kycCount;
  }
  
  public void setKycCount(int kycCount) {
    this.kycCount = kycCount;
  }
  
  public String getsPLanCountry() {
    return this.sPLanCountry;
  }
  
  public void setsPLanCountry(String sPLanCountry) {
    this.sPLanCountry = sPLanCountry;
  }
  
  public String getCustomerType() {
    return this.customerType;
  }
  
  public void setCustomerType(String customerType) {
    this.customerType = customerType;
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
  
  public int getFlag() {
    return this.flag;
  }
  
  public void setFlag(int flag) {
    this.flag = flag;
  }
  
  public Date getSubscriptionEndDate() {
    return this.subscriptionEndDate;
  }
  
  public void setSubscriptionEndDate(Date subscriptionEndDate) {
    this.subscriptionEndDate = subscriptionEndDate;
  }
  
  public Date getSubscriptionStartDate() {
    return this.subscriptionStartDate;
  }
  
  public void setSubscriptionStartDate(Date subscriptionStartDate) {
    this.subscriptionStartDate = subscriptionStartDate;
  }
  
  public Integer getsPlSerialNUmber() {
    return this.sPlSerialNUmber;
  }
  
  public void setsPlSerialNUmber(Integer sPlSerialNUmber) {
    this.sPlSerialNUmber = sPlSerialNUmber;
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
  
  public String getlCount() {
    return this.lCount;
  }
  
  public void setlCount(String lCount) {
    this.lCount = lCount;
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
  
  public String getInsertedBy() {
    return this.insertedBy;
  }
  
  public void setInsertedBy(String insertedBy) {
    this.insertedBy = insertedBy;
  }
  
  public String getModifiedBy() {
    return this.modifiedBy;
  }
  
  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }
  
  public NimaiMCustomer getUserid() {
    return this.userid;
  }
  
  public void setUserid(NimaiMCustomer userid) {
    this.userid = userid;
  }
}
