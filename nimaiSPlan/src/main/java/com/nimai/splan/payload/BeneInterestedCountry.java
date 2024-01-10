package com.nimai.splan.payload;

import java.util.Date;

public class BeneInterestedCountry {



	private Long countryID;

	private static final long serialVersionUID = 1L;

	private String countryName;


	private Date insertedDate;

	private Date modifiedDate;

	
//	@Column(name = "COUNTRY_CURID")
//	private Integer countryCurrencyId;
	

	private String countryCurrencyId;
	
	
//	public Integer getCountryCurrencyId() {
//		return countryCurrencyId;
//	}
//
//	public void setCountryCurrencyId(Integer countryCurrencyId) {
//		this.countryCurrencyId = countryCurrencyId;
//	}
	
	



	public String getCountryCurrencyId() {
		return countryCurrencyId;
	}

	public void setCountryCurrencyId(String countryCurrencyId) {
		this.countryCurrencyId = countryCurrencyId;
	}

	public Long getCountryID() {
		return countryID;
	}

	public void setCountryID(Long countryID) {
		this.countryID = countryID;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public Date getInsertedDate() {
		return insertedDate;
	}

	public void setInsertedDate(Date insertedDate) {
		this.insertedDate = insertedDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}



	
}
