package com.nimai.lc.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="temp_quotation")
@NamedStoredProcedureQuery(name = "move_to_quotation_master", procedureName = "move_to_quotation_master", parameters = {
		@StoredProcedureParameter(mode = ParameterMode.IN, name = "inp_quotation_id", type = String.class),
		@StoredProcedureParameter(mode = ParameterMode.IN, name = "inp_transaction_id", type = String.class),
		@StoredProcedureParameter(mode = ParameterMode.IN, name = "inp_userid", type = String.class)})
public class Quotation 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="QUOTATION_ID")
	private Integer quotationId;
	
	@Column(name="TRANSACTION_ID")
	private String transactionId;
	
	@Column(name="USERID")
	private String userId;
	
	@Column(name="BANK_USERID")
	private String bankUserId;
	
	@Column(name="CONFIRMATION_CHARGES")
	private Float confirmationCharges;
	
	@Column(name="CONF_CHGS_ISSUANCE_TO_NEGOT")
	private String confChgsIssuanceToNegot;
	
	@Column(name="CONF_CHGS_ISSUANCE_TO_EXP")
	private String confChgsIssuanceToexp;
	
	@Column(name="CONF_CHGS_ISSUANCE_TO_MATUR")
	private String confChgsIssuanceToMatur;
	
	@Column(name="CONF_CHGS_ISSUANCE_TO_CLAIM_EXP")
	private String confChgsIssuanceToClaimExp;
	
	@Column(name="DISCOUNTING_CHARGES")
	private Float discountingCharges;
	
	@Column(name="REFINANCING_CHARGES")
	private Float refinancingCharges;
	
	@Column(name="BANKER_ACCEPT_CHARGES")
	private Float bankAcceptCharges;
	
	@Column(name="APPLICABLE_BENCHMARK")
	private Float applicableBenchmark;
	
	@Column(name="COMMENTS_BENCHMARK")
	private String commentsBenchmark;
	
	@Column(name="NEGOTIATION_CHARGES_FIXED")
	private Float negotiationChargesFixed;
	
	@Column(name="NEGOTIATION_CHARGES_PERCT")
	private Float negotiationChargesPerct;
	
	@Column(name="DOC_HANDLING_CHARGES")
	private Float docHandlingCharges;
	
	@Column(name="OTHER_CHARGES")
	private Float otherCharges;
	
	@Column(name="CHARGES_TYPE")
	private String chargesType;
	
	@Column(name="MIN_TRANSACTION_CHARGES")
	private Float minTransactionCharges;
	
	@Column(name="INSERTED_DATE")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date insertedDate;
	
	@Column(name="INSERTED_BY")
	private String insertedBy;
	
	@Column(name="MODIFIED_DATE")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date modifiedDate;
	
	@Column(name="MODIFIED_BY")
	private String modifiedBy;

	@Column(name="CONFIRMED_FLAG")
	private Integer confirmedFlag;
	
	@Column(name="TOTAL_QUOTE_VALUE")
	private Float totalQuoteValue;
	
	@Column(name="VALIDITY_DATE")
	private Date validityDate;
	
	@Column(name="TERM_CONDITION_COMMENTS")
	private String termConditionComments;
	
	@Column(name="BANK_NAME")
	private String bankName;
	
	@Column(name="BRANCH_NAME")
	private String branchName;
	
	@Column(name="SWIFT_CODE")
	private String swiftCode;
	
	@Column(name="COUNTRY_NAME")
	private String countryName;
	
	@Column(name="EMAIL_ADDRESS")
	private String emailAddress;
	
	@Column(name="TELEPHONE")
	private String telephone;
	
	@Column(name="MOBILE_NUMBER")
	private String mobileNumber;
	
	@Column(name="FIRST_NAME")
	private String firstName;
	
	@Column(name="LAST_NAME")
	private String lastName;
	
	@Column(name="IS_DELETED")
	private Integer isDeleted;
	
	@Column(name="PARTICIPATION_AMOUNT")
	private Float participationAmount;
	
	@Column(name="PARTICIPATION_COMMISSION")
	private Float participationCommission;
	
	@Column(name="ACCEPTANCE_REASON")
	private String acceptanceReason;
	
	@Column(name="IS_OFFERED")
	private String isOffered;
	
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
