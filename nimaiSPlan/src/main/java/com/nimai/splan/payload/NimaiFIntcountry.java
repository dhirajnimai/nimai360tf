package com.nimai.splan.payload;
import java.util.Date;

public class NimaiFIntcountry {

    private static final long serialVersionUID = 1L;
  
    private Long countryId;
  

    private String countryName;

    private Date insertedDate;


    private Date modifiedDate;

    private String countryCurid;
 

    public NimaiFIntcountry() {
    }

    public NimaiFIntcountry(Long countryId) {
        this.countryId = countryId;
    }

    public NimaiFIntcountry(Long countryId, String countryCurid) {
        this.countryId = countryId;
        this.countryCurid = countryCurid;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
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

    public String getCountryCurid() {
        return countryCurid;
    }

    public void setCountryCurid(String countryCurid) {
        this.countryCurid = countryCurid;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (countryId != null ? countryId.hashCode() : 0);
        return hash;
    }


}
