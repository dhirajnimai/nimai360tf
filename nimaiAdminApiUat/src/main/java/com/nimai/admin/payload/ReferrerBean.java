package com.nimai.admin.payload;

public class ReferrerBean {
  private String userId;
  
  private float userWiseTotalEarning;
  
  private float totalEarning;
  
  public String getUserId() {
    return this.userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public float getUserWiseTotalEarning() {
    return this.userWiseTotalEarning;
  }
  
  public void setUserWiseTotalEarning(float userWiseTotalEarning) {
    this.userWiseTotalEarning = userWiseTotalEarning;
  }
  
  public float getTotalEarning() {
    return this.totalEarning;
  }
  
  public void setTotalEarning(float totalEarning) {
    this.totalEarning = totalEarning;
  }
}
