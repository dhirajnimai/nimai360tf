package com.nimai.admin.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "PHASE_TWO_TRANSFER_FAIL")
public class PhaseTwoTransferFail {

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "DATA_FAIL_ID")
	private Integer dataFailId;
	
	@Size(max = 20)
	@Column(name = "SUBSCRIBER_TYPE")
	private String subscriberType;

	@Size(min = 1, max = 50)
	@Column(name = "USERID")
	private String userid;
	@Size(max = 20)
	@Column(name = "BANK_TYPE")
	private String bankType;
	@Size(max = 100)
	@Column(name = "FIRST_NAME")
	private String firstName;
	@Size(max = 100)
	@Column(name = "LAST_NAME")
	private String lastName;
	
	@Size(max = 100)
	@Column(name = "EMAIL_ADDRESS")
	private String emailAddress;
	
	@Column(name = "ERROR_DUMP")
	private String errorDump;
	
	@Size(max = 100)
	@Column(name = "COMPANY_NAME")
	private String companyName;
	
	
	@Size(max = 100)
	@Column(name = "BANK_NAME")
	private String bankName;


	public Integer getDataFailId() {
		return dataFailId;
	}


	public void setDataFailId(Integer dataFailId) {
		this.dataFailId = dataFailId;
	}


	public String getSubscriberType() {
		return subscriberType;
	}


	public void setSubscriberType(String subscriberType) {
		this.subscriberType = subscriberType;
	}


	public String getUserid() {
		return userid;
	}


	public void setUserid(String userid) {
		this.userid = userid;
	}


	public String getBankType() {
		return bankType;
	}


	public void setBankType(String bankType) {
		this.bankType = bankType;
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


	public String getEmailAddress() {
		return emailAddress;
	}


	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}


	public String getErrorDump() {
		return errorDump;
	}


	public void setErrorDump(String errorDump) {
		this.errorDump = errorDump;
	}


	public String getCompanyName() {
		return companyName;
	}


	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}


	public String getBankName() {
		return bankName;
	}


	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	
	
	
	
	
	
	
}
