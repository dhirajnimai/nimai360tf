package com.nimai.email.bean;

public class BranchUserRequest {
	private String emailId;
	private String userId;
	private String event;
	private String branchId;
	private String employeeName;
	private int encryptionLength;
	private String userType;
	private String userMode;
	
	
	
	
	
	
	
	
	
	
	


	public String getUserMode() {
		return userMode;
	}
	public void setUserMode(String userMode) {
		this.userMode = userMode;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public int getEncryptionLength() {
		return encryptionLength;
	}
	public void setEncryptionLength(int encryptionLength) {
		this.encryptionLength = encryptionLength;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	/**
	 * @return the branchId
	 */
	public String getBranchId() {
		return branchId;
	}
	/**
	 * @param branchId the branchId to set
	 */
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}
	/**
	 * @return the emailId
	 */
	public String getEmailId() {
		return emailId;
	}
	/**
	 * @param emailId the emailId to set
	 */
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}
	/**
	 * @param event the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}
	@Override
	public String toString() {
		return "BranchUserRequest [emailId=" + emailId + ", userId=" + userId + ", event=" + event + ", branchId="
				+ branchId + "]";
	}
	
	
}
