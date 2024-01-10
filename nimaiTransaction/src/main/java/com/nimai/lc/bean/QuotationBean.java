package com.nimai.lc.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonFormat;

public class QuotationBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Integer quotationId;
	private String transactionId;
	private String userId;
	private String bankUserId;
	private String userType;
	private String addUserId;
	private String addEmail;
	private Float confirmationCharges;
	private String confChgsIssuanceToNegot;
	private String confChgsIssuanceToexp;
	private String confChgsIssuanceToMatur;
	private String confChgsIssuanceToClaimExp;
	private Float discountingCharges;
    private Float refinancingCharges;
	private Float bankAcceptCharges;
	private Float applicableBenchmark;
	private String commentsBenchmark;
	private Float negotiationChargesFixed;
	private Float negotiationChargesPerct;
	private Float docHandlingCharges;
	private Float otherCharges;
	private String chargesType;
	private Float minTransactionCharges;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date insertedDate;
	private String insertedBy;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date modifiedDate;
	private String modifiedBy;
	private Integer confirmedFlag;
	private Float totalQuoteValue;
	private Date validityDate;
	private String termConditionComments;
	private String bankName;
	private String branchName;
	private String swiftCode;
	private String countryName;
	private String emailAddress;
	private String telephone;
	private String mobileNumber;
	private String firstName;
	private String lastName;
	private String quotationStatus;
	private Integer isDeleted;
	private Float participationAmount;
	private Float participationCommission;
	private String acceptanceReason;
	private String isOffered;
	private Long offLineTrxnId;
	
	
	
	
	
	
	
	


	public Long getOffLineTrxnId() {
		return offLineTrxnId;
	}

	public void setOffLineTrxnId(Long offLineTrxnId) {
		this.offLineTrxnId = offLineTrxnId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getAddUserId() {
		return addUserId;
	}

	public void setAddUserId(String addUserId) {
		this.addUserId = addUserId;
	}

	public String getAddEmail() {
		return addEmail;
	}

	public void setAddEmail(String addEmail) {
		this.addEmail = addEmail;
	}

	public String getIsOffered() {
		return isOffered;
	}

	public void setIsOffered(String isOffered) {
		this.isOffered = isOffered;
	}

	public String getAcceptanceReason() {
		return acceptanceReason;
	}

	public void setAcceptanceReason(String acceptanceReason) {
		this.acceptanceReason = acceptanceReason;
	}
	
	public Float getParticipationAmount() {
		return participationAmount;
	}
	public void setParticipationAmount(Float participationAmount) {
		this.participationAmount = participationAmount;
	}
	public Float getParticipationCommission() {
		return participationCommission;
	}
	public void setParticipationCommission(Float participationCommission) {
		this.participationCommission = participationCommission;
	}
	
	public String getConfChgsIssuanceToClaimExp() {
		return confChgsIssuanceToClaimExp;
	}
	public void setConfChgsIssuanceToClaimExp(String confChgsIssuanceToClaimExp) {
		this.confChgsIssuanceToClaimExp = confChgsIssuanceToClaimExp;
	}
	public Integer getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}
	public String getTermConditionComments() {
		return termConditionComments;
	}
	public void setTermConditionComments(String termConditionComments) {
		this.termConditionComments = termConditionComments;
	}
	public String getQuotationStatus() {
		return quotationStatus;
	}
	public void setQuotationStatus(String quotationStatus) {
		this.quotationStatus = quotationStatus;
	}
	public Float getTotalQuoteValue() {
		return totalQuoteValue;
	}
	public void setTotalQuoteValue(Float totalQuoteValue) {
		this.totalQuoteValue = totalQuoteValue;
	}
	public Date getValidityDate() {
		return validityDate;
	}
	public void setValidityDate(Date validityDate) {
		this.validityDate = validityDate;
	}
	public Integer getQuotationId() {
		return quotationId;
	}
	public void setQuotationId(Integer quotationId) {
		this.quotationId = quotationId;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
	public String getBankUserId() {
		return bankUserId;
	}
	public void setBankUserId(String bankUserId) {
		this.bankUserId = bankUserId;
	}
	public Float getConfirmationCharges() {
		return confirmationCharges;
	}
	public void setConfirmationCharges(Float confirmationCharges) {
		this.confirmationCharges = confirmationCharges;
	}
	public String getConfChgsIssuanceToNegot() {
		return confChgsIssuanceToNegot;
	}
	public void setConfChgsIssuanceToNegot(String confChgsIssuanceToNegot) {
		this.confChgsIssuanceToNegot = confChgsIssuanceToNegot;
	}
	public String getConfChgsIssuanceToexp() {
		return confChgsIssuanceToexp;
	}
	public void setConfChgsIssuanceToexp(String confChgsIssuanceToexp) {
		this.confChgsIssuanceToexp = confChgsIssuanceToexp;
	}
	public String getConfChgsIssuanceToMatur() {
		return confChgsIssuanceToMatur;
	}
	public void setConfChgsIssuanceToMatur(String confChgsIssuanceToMatur) {
		this.confChgsIssuanceToMatur = confChgsIssuanceToMatur;
	}
	public Float getDiscountingCharges() {
		return discountingCharges;
	}
	public void setDiscountingCharges(Float discountingCharges) {
		this.discountingCharges = discountingCharges;
	}
	
	public Float getRefinancingCharges() {
		return refinancingCharges;
	}
	public void setRefinancingCharges(Float refinancingCharges) {
		this.refinancingCharges = refinancingCharges;
	}
	public Float getBankAcceptCharges() {
		return bankAcceptCharges;
	}
	public void setBankAcceptCharges(Float bankAcceptCharges) {
		this.bankAcceptCharges = bankAcceptCharges;
	}
	public Float getApplicableBenchmark() {
		return applicableBenchmark;
	}
	public void setApplicableBenchmark(Float applicableBenchmark) {
		this.applicableBenchmark = applicableBenchmark;
	}
	public String getCommentsBenchmark() {
		return commentsBenchmark;
	}
	public void setCommentsBenchmark(String commentsBenchmark) {
		this.commentsBenchmark = commentsBenchmark;
	}
	public Float getNegotiationChargesFixed() {
		return negotiationChargesFixed;
	}
	public void setNegotiationChargesFixed(Float negotiationChargesFixed) {
		this.negotiationChargesFixed = negotiationChargesFixed;
	}
	public Float getNegotiationChargesPerct() {
		return negotiationChargesPerct;
	}
	public void setNegotiationChargesPerct(Float negotiationChargesPerct) {
		this.negotiationChargesPerct = negotiationChargesPerct;
	}
	public Float getDocHandlingCharges() {
		return docHandlingCharges;
	}
	public void setDocHandlingCharges(Float docHandlingCharges) {
		this.docHandlingCharges = docHandlingCharges;
	}
	public Float getOtherCharges() {
		return otherCharges;
	}
	public void setOtherCharges(Float otherCharges) {
		this.otherCharges = otherCharges;
	}
	public String getChargesType() {
		return chargesType;
	}
	public void setChargesType(String chargesType) {
		this.chargesType = chargesType;
	}
	public Float getMinTransactionCharges() {
		return minTransactionCharges;
	}
	public void setMinTransactionCharges(Float minTransactionCharges) {
		this.minTransactionCharges = minTransactionCharges;
	}
	public Date getInsertedDate() {
		return insertedDate;
	}
	public void setInsertedDate(Date insertedDate) {
		this.insertedDate = insertedDate;
	}
	public String getInsertedBy() {
		return insertedBy;
	}
	public void setInsertedBy(String insertedBy) {
		this.insertedBy = insertedBy;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Integer getConfirmedFlag() {
		return confirmedFlag;
	}
	public void setConfirmedFlag(Integer confirmedFlag) {
		this.confirmedFlag = confirmedFlag;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getSwiftCode() {
		return swiftCode;
	}
	public void setSwiftCode(String swiftCode) {
		this.swiftCode = swiftCode;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	
}
