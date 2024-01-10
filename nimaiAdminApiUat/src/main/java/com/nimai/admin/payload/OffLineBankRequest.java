package com.nimai.admin.payload;

import java.util.List;



public class OffLineBankRequest {

	private String userId;
	private String bankName;
	private String bankCountry;
	private String issuanceCountryIntrested;
	private String subscriberType;
	
	private String parentFirstName;
	private String parentLastName;
	private String parentEmailAddress;
	private String parentMobileNo;
	
	
	private String bankType;
	private String minLcValue;
	private String ccy;
	private String firstName;
	private String lastName;
	private String emailId;
	private String mobileNo;
	private List<InterestedCountryBean> interestedCountry;
	private List<BlackListedGoodsBean> blacklistedGoods;
	private List<BeneInterestedCountryBean> beneInterestedCountry;
	private List<AdditionalUserList> additionalUserList;
	private String userMode;
	private String accountSource;
	private String businessType;
	private String createdBy;
	
	private String kycStatus;
	private String paymentStatus;
	private String mrpa; 
	private String modifiedBy;
	
	
	
	
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public String getMrpa() {
		return mrpa;
	}
	public void setMrpa(String mrpa) {
		this.mrpa = mrpa;
	}
	public String getKycStatus() {
		return kycStatus;
	}
	public void setKycStatus(String kycStatus) {
		this.kycStatus = kycStatus;
	}
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getParentFirstName() {
		return parentFirstName;
	}
	public void setParentFirstName(String parentFirstName) {
		this.parentFirstName = parentFirstName;
	}
	public String getParentLastName() {
		return parentLastName;
	}
	public void setParentLastName(String parentLastName) {
		this.parentLastName = parentLastName;
	}
	public String getParentEmailAddress() {
		return parentEmailAddress;
	}
	public void setParentEmailAddress(String parentEmailAddress) {
		this.parentEmailAddress = parentEmailAddress;
	}
	public String getParentMobileNo() {
		return parentMobileNo;
	}
	public void setParentMobileNo(String parentMobileNo) {
		this.parentMobileNo = parentMobileNo;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public String getAccountSource() {
		return accountSource;
	}
	public void setAccountSource(String accountSource) {
		this.accountSource = accountSource;
	}
	public String getBankType() {
		return bankType;
	}
	public void setBankType(String bankType) {
		this.bankType = bankType;
	}
	public String getSubscriberType() {
		return subscriberType;
	}
	public void setSubscriberType(String subscriberType) {
		this.subscriberType = subscriberType;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankCountry() {
		return bankCountry;
	}
	public void setBankCountry(String bankCountry) {
		this.bankCountry = bankCountry;
	}
	public String getIssuanceCountryIntrested() {
		return issuanceCountryIntrested;
	}
	public void setIssuanceCountryIntrested(String issuanceCountryIntrested) {
		this.issuanceCountryIntrested = issuanceCountryIntrested;
	}
	public String getMinLcValue() {
		return minLcValue;
	}
	public void setMinLcValue(String minLcValue) {
		this.minLcValue = minLcValue;
	}
	public String getCcy() {
		return ccy;
	}
	public void setCcy(String ccy) {
		this.ccy = ccy;
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
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public List<InterestedCountryBean> getInterestedCountry() {
		return interestedCountry;
	}
	public void setInterestedCountry(List<InterestedCountryBean> interestedCountry) {
		this.interestedCountry = interestedCountry;
	}
	public List<BlackListedGoodsBean> getBlacklistedGoods() {
		return blacklistedGoods;
	}
	public void setBlacklistedGoods(List<BlackListedGoodsBean> blacklistedGoods) {
		this.blacklistedGoods = blacklistedGoods;
	}
	public List<BeneInterestedCountryBean> getBeneInterestedCountry() {
		return beneInterestedCountry;
	}
	public void setBeneInterestedCountry(List<BeneInterestedCountryBean> beneInterestedCountry) {
		this.beneInterestedCountry = beneInterestedCountry;
	}
	public List<AdditionalUserList> getAdditionalUserList() {
		return additionalUserList;
	}
	public void setAdditionalUserList(List<AdditionalUserList> additionalUserList) {
		this.additionalUserList = additionalUserList;
	}
	public String getUserMode() {
		return userMode;
	}
	public void setUserMode(String userMode) {
		this.userMode = userMode;
	}
	
	
	
	
	
	
}
