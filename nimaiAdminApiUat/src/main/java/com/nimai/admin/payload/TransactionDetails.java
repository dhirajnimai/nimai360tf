package com.nimai.admin.payload;

import java.util.Date;

public class TransactionDetails {
	
	private String transactionId;
	private String issuingBank;
	private String branch;
	private String swiftCode;
	private String lcCountry;
	private String requirement;
	private Double amount;
	private Date issuingDate;
	private Date lastShipmentDate;
	private Date negotiationDate;
	private String goods;
	private String usansce; 
	private String paymentTerms;
	private String remarks;
	private String applicant;
	private String applicantCountry;
	private String beneficiary;
	private String beneficiaryCountry;
	private String beneficiaryBank;
	private String beneficiarySwiftCode;
	private String beneficiaryBankCountry;
	private String portofLoading;
	private String portofDischarge;
	private String chargesAreOn;
	private String validity;
	private String proForma;
	
	private String userID;
	private String firstName;
	private String lastName;
	private String emailID;
	private String mobileNo;
	private String custCountry;
	private String registrationType;
	private String companyName;
	private String addressCountry;
	private String stateProvince;
	private String city;	
	private String pin;
	private String addressline1;
	private String addressline2;
	private String addressline3;
	private String telephone;	
	private String rmFirstName;
	private String rmLastName;
	private String rmDesignation;
	
	private String applicantContactPerson;
	private String applicantContactPersonEmail; 
	private String beneContactPerson;
	private String beneContactPersonEmail;
	
	private String commisionScheme;
	private String isEsgCompliant;
	private String benchMark;
	private String anyotherCondition;
	private String currency;
	private String applicableLaw;
	private String transactionType;
	private Double minParticipationAmnt;
	private Double retentionAmnt;
	
	
	private String bankName;
	private String branchName;
	private String participationBasis;
	private String secTransactionType;
	private Date targetBookingdate;
	private Date transactionMaturityDate;
	private Integer tenor;
	private String customerQuoteValidityDate;
	private String offeredPrice;
	private String anyOtherCondition;
	
	
	private Date billMatureDate;
	private String billType;
	
	private String portLoadingCountry;
	private String portDischargeCountry;
	
	private Date bgExpiryDate;
	private Date claimExpiryDate;
	private String bgType;
	private String lcNumber;
	private String lastBeneBank;
	private String lastBeneSwiftCode;
	private String lastBankCountry;

	private String tenorFile;


	public String getTenorFile() {
		return tenorFile;
	}

	public void setTenorFile(String tenorFile) {
		this.tenorFile = tenorFile;
	}

	public String getLcNumber() {
		return lcNumber;
	}

	public void setLcNumber(String lcNumber) {
		this.lcNumber = lcNumber;
	}

	public String getLastBeneBank() {
		return lastBeneBank;
	}

	public void setLastBeneBank(String lastBeneBank) {
		this.lastBeneBank = lastBeneBank;
	}

	public String getLastBeneSwiftCode() {
		return lastBeneSwiftCode;
	}

	public void setLastBeneSwiftCode(String lastBeneSwiftCode) {
		this.lastBeneSwiftCode = lastBeneSwiftCode;
	}

	public String getLastBankCountry() {
		return lastBankCountry;
	}

	public void setLastBankCountry(String lastBankCountry) {
		this.lastBankCountry = lastBankCountry;
	}

	public String getBeneficiaryCountry() {
		return beneficiaryCountry;
	}

	public void setBeneficiaryCountry(String beneficiaryCountry) {
		this.beneficiaryCountry = beneficiaryCountry;
	}

	public Date getBgExpiryDate() {
		return bgExpiryDate;
	}
	public void setBgExpiryDate(Date bgExpiryDate) {
		this.bgExpiryDate = bgExpiryDate;
	}
	public Date getClaimExpiryDate() {
		return claimExpiryDate;
	}
	public void setClaimExpiryDate(Date claimExpiryDate) {
		this.claimExpiryDate = claimExpiryDate;
	}
	public String getBgType() {
		return bgType;
	}
	public void setBgType(String bgType) {
		this.bgType = bgType;
	}
	public String getPortLoadingCountry() {
		return portLoadingCountry;
	}
	public void setPortLoadingCountry(String portLoadingCountry) {
		this.portLoadingCountry = portLoadingCountry;
	}
	public String getPortDischargeCountry() {
		return portDischargeCountry;
	}
	public void setPortDischargeCountry(String portDischargeCountry) {
		this.portDischargeCountry = portDischargeCountry;
	}
	public Date getBillMatureDate() {
		return billMatureDate;
	}
	public void setBillMatureDate(Date billMatureDate) {
		this.billMatureDate = billMatureDate;
	}
	public String getBillType() {
		return billType;
	}
	public void setBillType(String billType) {
		this.billType = billType;
	}
	public Date getTargetBookingdate() {
		return targetBookingdate;
	}
	public void setTargetBookingdate(Date targetBookingdate) {
		this.targetBookingdate = targetBookingdate;
	}

	public Date getTransactionMaturityDate() {
		return transactionMaturityDate;
	}
	public void setTransactionMaturityDate(Date transactionMaturityDate) {
		this.transactionMaturityDate = transactionMaturityDate;
	}
	public Integer getTenor() {
		return tenor;
	}
	public void setTenor(Integer tenor) {
		this.tenor = tenor;
	}
	
	public String getCustomerQuoteValidityDate() {
		return customerQuoteValidityDate;
	}
	public void setCustomerQuoteValidityDate(String customerQuoteValidityDate) {
		this.customerQuoteValidityDate = customerQuoteValidityDate;
	}
	public String getOfferedPrice() {
		return offeredPrice;
	}
	public void setOfferedPrice(String offeredPrice) {
		this.offeredPrice = offeredPrice;
	}
	public String getAnyOtherCondition() {
		return anyOtherCondition;
	}
	public void setAnyOtherCondition(String anyOtherCondition) {
		this.anyOtherCondition = anyOtherCondition;
	}
	public String getParticipationBasis() {
		return participationBasis;
	}
	public void setParticipationBasis(String participationBasis) {
		this.participationBasis = participationBasis;
	}
	public String getSecTransactionType() {
		return secTransactionType;
	}
	public void setSecTransactionType(String secTransactionType) {
		this.secTransactionType = secTransactionType;
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
	public void setRetentionAmnt(Double retentionAmnt) {
		this.retentionAmnt = retentionAmnt;
	}
	public String getCommisionScheme() {
		return commisionScheme;
	}
	public void setCommisionScheme(String commisionScheme) {
		this.commisionScheme = commisionScheme;
	}
	public String getIsEsgCompliant() {
		return isEsgCompliant;
	}
	public void setIsEsgCompliant(String isEsgCompliant) {
		this.isEsgCompliant = isEsgCompliant;
	}
	public String getBenchMark() {
		return benchMark;
	}
	public void setBenchMark(String benchMark) {
		this.benchMark = benchMark;
	}
	public String getAnyotherCondition() {
		return anyotherCondition;
	}
	public void setAnyotherCondition(String anyotherCondition) {
		this.anyotherCondition = anyotherCondition;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getApplicableLaw() {
		return applicableLaw;
	}
	public void setApplicableLaw(String applicableLaw) {
		this.applicableLaw = applicableLaw;
	}
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	
	public Double getMinParticipationAmnt() {
		return minParticipationAmnt;
	}
	public void setMinParticipationAmnt(Double minParticipationAmnt) {
		this.minParticipationAmnt = minParticipationAmnt;
	}
	public Double getRetentionAmnt() {
		return retentionAmnt;
	}
	public String getIssuingBank() {
		return issuingBank;
	}
	public void setIssuingBank(String issuingBank) {
		this.issuingBank = issuingBank;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getSwiftCode() {
		return swiftCode;
	}
	public void setSwiftCode(String swiftCode) {
		this.swiftCode = swiftCode;
	}
	public String getLcCountry() {
		return lcCountry;
	}
	public void setLcCountry(String lcCountry) {
		this.lcCountry = lcCountry;
	}
	public String getRequirement() {
		return requirement;
	}
	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}

	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Date getIssuingDate() {
		return issuingDate;
	}
	public void setIssuingDate(Date issuingDate) {
		this.issuingDate = issuingDate;
	}
	public Date getLastShipmentDate() {
		return lastShipmentDate;
	}
	public void setLastShipmentDate(Date lastShipmentDate) {
		this.lastShipmentDate = lastShipmentDate;
	}
	public Date getNegotiationDate() {
		return negotiationDate;
	}
	public void setNegotiationDate(Date negotiationDate) {
		this.negotiationDate = negotiationDate;
	}
	public String getGoods() {
		return goods;
	}
	public void setGoods(String goods) {
		this.goods = goods;
	}

	public String getUsansce() {
		return usansce;
	}
	public void setUsansce(String usansce) {
		this.usansce = usansce;
	}
	public String getPaymentTerms() {
		return paymentTerms;
	}
	public void setPaymentTerms(String paymentTerms) {
		this.paymentTerms = paymentTerms;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getApplicant() {
		return applicant;
	}
	public void setApplicant(String applicant) {
		this.applicant = applicant;
	}
	public String getApplicantCountry() {
		return applicantCountry;
	}
	public void setApplicantCountry(String applicantCountry) {
		this.applicantCountry = applicantCountry;
	}
	public String getBeneficiary() {
		return beneficiary;
	}
	public void setBeneficiary(String beneficiary) {
		this.beneficiary = beneficiary;
	}
	public String getBeneficiaryBank() {
		return beneficiaryBank;
	}
	public void setBeneficiaryBank(String beneficiaryBank) {
		this.beneficiaryBank = beneficiaryBank;
	}
	public String getBeneficiarySwiftCode() {
		return beneficiarySwiftCode;
	}
	public void setBeneficiarySwiftCode(String beneficiarySwiftCode) {
		this.beneficiarySwiftCode = beneficiarySwiftCode;
	}
	public String getBeneficiaryBankCountry() {
		return beneficiaryBankCountry;
	}
	public void setBeneficiaryBankCountry(String beneficiaryBankCountry) {
		this.beneficiaryBankCountry = beneficiaryBankCountry;
	}
	public String getPortofLoading() {
		return portofLoading;
	}
	public void setPortofLoading(String portofLoading) {
		this.portofLoading = portofLoading;
	}
	public String getPortofDischarge() {
		return portofDischarge;
	}
	public void setPortofDischarge(String portofDischarge) {
		this.portofDischarge = portofDischarge;
	}
	public String getChargesAreOn() {
		return chargesAreOn;
	}
	public void setChargesAreOn(String chargesAreOn) {
		this.chargesAreOn = chargesAreOn;
	}
	public String getValidity() {
		return validity;
	}
	public void setValidity(String validity) {
		this.validity = validity;
	}
	public String getProForma() {
		return proForma;
	}
	public void setProForma(String proForma) {
		this.proForma = proForma;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
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
	public String getEmailID() {
		return emailID;
	}
	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public String getCustCountry() {
		return custCountry;
	}
	public void setCustCountry(String custCountry) {
		this.custCountry = custCountry;
	}
	public String getRegistrationType() {
		return registrationType;
	}
	public void setRegistrationType(String registrationType) {
		this.registrationType = registrationType;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getAddressCountry() {
		return addressCountry;
	}
	public void setAddressCountry(String addressCountry) {
		this.addressCountry = addressCountry;
	}
	public String getStateProvince() {
		return stateProvince;
	}
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public String getAddressline1() {
		return addressline1;
	}
	public void setAddressline1(String addressline1) {
		this.addressline1 = addressline1;
	}
	public String getAddressline2() {
		return addressline2;
	}
	public void setAddressline2(String addressline2) {
		this.addressline2 = addressline2;
	}
	public String getAddressline3() {
		return addressline3;
	}
	public void setAddressline3(String addressline3) {
		this.addressline3 = addressline3;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getRmFirstName() {
		return rmFirstName;
	}
	public void setRmFirstName(String rmFirstName) {
		this.rmFirstName = rmFirstName;
	}
	public String getRmLastName() {
		return rmLastName;
	}
	public void setRmLastName(String rmLastName) {
		this.rmLastName = rmLastName;
	}
	public String getRmDesignation() {
		return rmDesignation;
	}
	public void setRmDesignation(String rmDesignation) {
		this.rmDesignation = rmDesignation;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	
	 public String getApplicantContactPerson() {
		return applicantContactPerson;
	}
	public void setApplicantContactPerson(String applicantContactPerson) {
		this.applicantContactPerson = applicantContactPerson;
	}
	public String getApplicantContactPersonEmail() {
		return applicantContactPersonEmail;
	}
	public void setApplicantContactPersonEmail(String applicantContactPersonEmail) {
		this.applicantContactPersonEmail = applicantContactPersonEmail;
	}
	public String getBeneContactPerson() {
		return beneContactPerson;
	}
	public void setBeneContactPerson(String beneContactPerson) {
		this.beneContactPerson = beneContactPerson;
	}
	public String getBeneContactPersonEmail() {
		return beneContactPersonEmail;
	}
	public void setBeneContactPersonEmail(String beneContactPersonEmail) {
		this.beneContactPersonEmail = beneContactPersonEmail;
	}
	@Override
	public String toString() {
		return "TransactionDetails [transactionId=" + transactionId + ", issuingBank=" + issuingBank + ", branch="
				+ branch + ", swiftCode=" + swiftCode + ", lcCountry=" + lcCountry + ", requirement=" + requirement
				+ ", amount=" + amount + ", issuingDate=" + issuingDate + ", lastShipmentDate=" + lastShipmentDate
				+ ", negotiationDate=" + negotiationDate + ", goods=" + goods + ", usansce=" + usansce
				+ ", paymentTerms=" + paymentTerms + ", remarks=" + remarks + ", applicant=" + applicant
				+ ", applicantCountry=" + applicantCountry + ", beneficiary=" + beneficiary + ", beneficiaryBank="
				+ beneficiaryBank + ", beneficiarySwiftCode=" + beneficiarySwiftCode + ", beneficiaryBankCountry="
				+ beneficiaryBankCountry + ", portofLoading=" + portofLoading + ", portofDischarge=" + portofDischarge
				+ ", chargesAreOn=" + chargesAreOn + ", validity=" + validity + ", proForma=" + proForma + ", userID="
				+ userID + ", firstName=" + firstName + ", lastName=" + lastName + ", emailID=" + emailID
				+ ", mobileNo=" + mobileNo + ", custCountry=" + custCountry + ", registrationType=" + registrationType
				+ ", companyName=" + companyName + ", addressCountry=" + addressCountry + ", stateProvince="
				+ stateProvince + ", city=" + city + ", pin=" + pin + ", addressline1=" + addressline1
				+ ", addressline2=" + addressline2 + ", addressline3=" + addressline3 + ", telephone=" + telephone
				+ ", rmFirstName=" + rmFirstName + ", rmLastName=" + rmLastName + ", rmDesignation=" + rmDesignation
				+ ", applicantContactPerson=" + applicantContactPerson + ", applicantContactPersonEmail="
				+ applicantContactPersonEmail + ", beneContactPerson=" + beneContactPerson + ", beneContactPersonEmail="
				+ beneContactPersonEmail + ", commisionScheme=" + commisionScheme + ", isEsgCompliant=" + isEsgCompliant
				+ ", benchMark=" + benchMark + ", anyotherCondition=" + anyotherCondition + ", currency=" + currency
				+ ", applicableLaw=" + applicableLaw + ", transactionType=" + transactionType
				+ ", minParticipationAmnt=" + minParticipationAmnt + ", retentionAmnt=" + retentionAmnt + ", bankName="
				+ bankName + ", branchName=" + branchName + ", participationBasis=" + participationBasis
				+ ", secTransactionType=" + secTransactionType + ", targetBookingdate=" + targetBookingdate
				+ ", transactionMaturityDate=" + transactionMaturityDate + ", tenor=" + tenor
				+ ", customerQuoteValidityDate=" + customerQuoteValidityDate + ", offeredPrice=" + offeredPrice
				+ ", anyOtherCondition=" + anyOtherCondition + ", billMatureDate=" + billMatureDate + ", billType="
				+ billType + ", portLoadingCountry=" + portLoadingCountry + ", portDischargeCountry="
				+ portDischargeCountry + ", bgExpiryDate=" + bgExpiryDate + ", claimExpiryDate=" + claimExpiryDate
				+ ", bgType=" + bgType + "]";
	} 
	
		

}
