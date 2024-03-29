package com.nimai.splan.payload;

import java.util.Set;

public class MigrationDataRequest {



	private String companyName;
	private String bankName;
	private String branchName;
	private String swiftCode;
	
	private Set<EntityEmployeeDto> emplyeeDetails;
	private AddressDetailsDto addressDetails;
	private RegistrationType registrationType;
	private SubscriberType subscriberType;
	

	
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
	public SubscriberType getSubscriberType() {
		return subscriberType;
	}
	public void setSubscriberType(SubscriberType subscriberType) {
		this.subscriberType = subscriberType;
	}
	public Set<EntityEmployeeDto> getEmplyeeDetails() {
		return emplyeeDetails;
	}
	public void setEmplyeeDetails(Set<EntityEmployeeDto> emplyeeDetails) {
		this.emplyeeDetails = emplyeeDetails;
	}
	public AddressDetailsDto getAddressDetails() {
		return addressDetails;
	}
	public void setAddressDetails(AddressDetailsDto addressDetails) {
		this.addressDetails = addressDetails;
	}
	public RegistrationType getRegistrationType() {
		return registrationType;
	}
	public void setRegistrationType(RegistrationType registrationType) {
		this.registrationType = registrationType;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
//	public String getBankName() {
//		return bankName;
//	}
//	public void setBankName(String bankName) {
//		this.bankName = bankName;
//	}
//	public String getBranchName() {
//		return branchName;
//	}
//	public void setBranchName(String branchName) {
//		this.branchName = branchName;
//	}
//	public String getSwiftCode() {
//		return swiftCode;
//	}
//	public void setSwiftCode(String swiftCode) {
//		this.swiftCode = swiftCode;
//	}







}
