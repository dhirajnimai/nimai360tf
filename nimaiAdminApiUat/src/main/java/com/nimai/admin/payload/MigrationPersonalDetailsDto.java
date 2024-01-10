package com.nimai.admin.payload;

import java.util.List;
import java.util.Set;

import com.nimai.admin.model.BeneInterestedCountry;
import com.nimai.admin.model.NimaiFBlkgoods;
import com.nimai.admin.model.NimaiFIntcountry;

public class MigrationPersonalDetailsDto {

    private String firstName;

    private String lastName;
    // @ValidMobileNumber(field = "mobileNumber")
    private String mobileNumber;

    private String country;

    private Set<String> additionalEmails;
    private List<NimaiFIntcountry> issuanceCountryList;
    private List<BeneInterestedCountry> beneficiaryCountryList;
    private List<NimaiFBlkgoods> blklstedGoods;
    private String currency;
    private Double minLCValue;
    private String landLineNumber;

    private String countryExt;
    private String email;
    private String username;
    private String subscriberType;
    private String designation;
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
    public String getMobileNumber() {
        return mobileNumber;
    }
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public Set<String> getAdditionalEmails() {
        return additionalEmails;
    }
    public void setAdditionalEmails(Set<String> additionalEmails) {
        this.additionalEmails = additionalEmails;
    }

    public List<NimaiFIntcountry> getIssuanceCountryList() {
        return issuanceCountryList;
    }
    public void setIssuanceCountryList(List<NimaiFIntcountry> issuanceCountryList) {
        this.issuanceCountryList = issuanceCountryList;
    }
    public List<BeneInterestedCountry> getBeneficiaryCountryList() {
        return beneficiaryCountryList;
    }
    public void setBeneficiaryCountryList(List<BeneInterestedCountry> beneficiaryCountryList) {
        this.beneficiaryCountryList = beneficiaryCountryList;
    }

    public List<NimaiFBlkgoods> getBlklstedGoods() {
        return blklstedGoods;
    }
    public void setBlklstedGoods(List<NimaiFBlkgoods> blklstedGoods) {
        this.blklstedGoods = blklstedGoods;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public Double getMinLCValue() {
        return minLCValue;
    }
    public void setMinLCValue(Double minLCValue) {
        this.minLCValue = minLCValue;
    }
    public String getLandLineNumber() {
        return landLineNumber;
    }
    public void setLandLineNumber(String landLineNumber) {
        this.landLineNumber = landLineNumber;
    }
    public String getCountryExt() {
        return countryExt;
    }
    public void setCountryExt(String countryExt) {
        this.countryExt = countryExt;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getSubscriberType() {
        return subscriberType;
    }
    public void setSubscriberType(String subscriberType) {
        this.subscriberType = subscriberType;
    }
    public String getDesignation() {
        return designation;
    }
    public void setDesignation(String designation) {
        this.designation = designation;
    }



}
