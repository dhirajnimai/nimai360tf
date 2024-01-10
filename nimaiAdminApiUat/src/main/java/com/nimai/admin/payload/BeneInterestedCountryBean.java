package com.nimai.admin.payload;

public class BeneInterestedCountryBean {
	private Long countryID;
	private String countriesIntrested;
	private String ccid;
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Long getCountryID() {
		return countryID;
	}
	public void setCountryID(Long countryID) {
		this.countryID = countryID;
	}
	public String getCountriesIntrested() {
		return countriesIntrested;
	}
	public void setCountriesIntrested(String countriesIntrested) {
		this.countriesIntrested = countriesIntrested;
	}
	public String getCcid() {
		return ccid;
	}
	public void setCcid(String ccid) {
		this.ccid = ccid;
	}
	
	
	
}
