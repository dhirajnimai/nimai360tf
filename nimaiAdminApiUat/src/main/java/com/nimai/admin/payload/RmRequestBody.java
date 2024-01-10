package com.nimai.admin.payload;

public class RmRequestBody {

	private String userId;
	private String countryName;
	private String subType;
	private String userMode;
	
	
	public String getUserMode() {
		return userMode;
	}
	public void setUserMode(String userMode) {
		this.userMode = userMode;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	
	
	
	
	
}
