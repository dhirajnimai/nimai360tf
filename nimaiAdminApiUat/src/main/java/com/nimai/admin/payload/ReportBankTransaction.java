package com.nimai.admin.payload;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class ReportBankTransaction {
  private String user_ID;
  private String customer_UserId;
  private String landline_no;
  private String mobile;
  private String email;
//  private String quote_Placed;
  private String date_And_Time;
  private String underwriting_Bank;
  private String branch_Name;
  private String Underwriting_Bank_Country;
  private String transaction_Id;
  private String requirement;
  private String iB;
  private String lc_issuance_branch_name;
  private Double value;
  private String ccy;
  private Double usd_currency_value;
  private Integer usance_Period;
  private Integer original_tenor_days;
  private Float applicable_benchmark;
  private Float confirmation_charges_p_a;
  private Float discounting_charges_p_a;
  private Float refinancing_charges_p_a;
  private Float banker_accept_charges_p_a;
  private String confirmation_charges_from_date_of_issuance_till_negotiation_date;
  private String confirmation_charges_from_date_of_issuance_till_maturity_date;
  private Float negotiation_charges_in_percentage;
  private Float negotiation_charges_in_fixed;
  private Float other_Charges;
  private Float min_Trxn_Charges;
  private String Bank_Quote_validity;
  private String Transaction_Validity;
  private String Total_Quote_Value;
  private String quote_Status;
  private String term_Condition;
  private String payment_Terms;
  public Integer getOriginal_tenor_days() {
    return original_tenor_days;
  }

  public void setOriginal_tenor_days(Integer original_tenor_days) {
    this.original_tenor_days = original_tenor_days;
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  public Double getUsd_currency_value() {
    return usd_currency_value;
  }

  public void setUsd_currency_value(Double usd_currency_value) {
    this.usd_currency_value = usd_currency_value;
  }

  public String getPayment_Terms() {
    return payment_Terms;
  }

  public void setPayment_Terms(String payment_Terms) {
    this.payment_Terms = payment_Terms;
  }

  public String getTerm_Condition() {
    return term_Condition;
  }

  public void setTerm_Condition(String term_Condition) {
    this.term_Condition = term_Condition;
  }

  public String getQuote_Status() {
    return quote_Status;
  }

  public void setQuote_Status(String quote_Status) {
    this.quote_Status = quote_Status;
  }

  public String getDate_And_Time() {
    return date_And_Time;
  }

  public void setDate_And_Time(String date_And_Time) {
    this.date_And_Time = date_And_Time;
  }

  public String getTransaction_Validity() {
    return this.Transaction_Validity;
  }
  
public String getTotal_Quote_Value() {
	return Total_Quote_Value;
}

public void setTotal_Quote_Value(String total_Quote_Value) {
	Total_Quote_Value = total_Quote_Value;
}

public void setTransaction_Validity(String transaction_Validity) {
    this.Transaction_Validity = transaction_Validity;
  }
  
  public String getUnderwriting_Bank_Country() {
    return this.Underwriting_Bank_Country;
  }
  
  public void setUnderwriting_Bank_Country(String underwriting_Bank_Country) {
    this.Underwriting_Bank_Country = underwriting_Bank_Country;
  }
  
  public String getUnderwriting_Bank() {
    return this.underwriting_Bank;
  }
  
  public void setUnderwriting_Bank(String underwriting_Bank) {
    this.underwriting_Bank = underwriting_Bank;
  }
  
  public String getLc_issuance_branch_name() {
    return this.lc_issuance_branch_name;
  }
  
  public void setLc_issuance_branch_name(String lc_issuance_branch_name) {
    this.lc_issuance_branch_name = lc_issuance_branch_name;
  }
  
  public String getCustomer_UserId() {
    return this.customer_UserId;
  }
  
  public void setCustomer_UserId(String customer_UserId) {
    this.customer_UserId = customer_UserId;
  }
  
  public String getLandline_no() {
    return this.landline_no;
  }
  
  public void setLandline_no(String landline_no) {
    this.landline_no = landline_no;
  }
  
  public Float getApplicable_benchmark() {
    return this.applicable_benchmark;
  }
  
  public void setApplicable_benchmark(Float applicable_benchmark) {
    this.applicable_benchmark = applicable_benchmark;
  }
  
  public Float getNegotiation_charges_in_percentage() {
    return this.negotiation_charges_in_percentage;
  }
  
  public void setNegotiation_charges_in_percentage(Float negotiation_charges_in_percentage) {
    this.negotiation_charges_in_percentage = negotiation_charges_in_percentage;
  }
  
  public Float getNegotiation_charges_in_fixed() {
    return this.negotiation_charges_in_fixed;
  }
  
  public void setNegotiation_charges_in_fixed(Float negotiation_charges_in_fixed) {
    this.negotiation_charges_in_fixed = negotiation_charges_in_fixed;
  }
  

  
//  public String getDate_And_Time() {
//	return date_And_Time;
//}
//
//public void setDate_And_Time(String date_And_Time) {
//	this.date_And_Time = date_And_Time;
//}

public String getUser_ID() {
    return this.user_ID;
  }
  
  public void setUser_ID(String user_ID) {
    this.user_ID = user_ID;
  }
  
  public String getMobile() {
    return this.mobile;
  }
  
  public void setMobile(String mobile) {
    this.mobile = mobile;
  }
  
  public String getEmail() {
    return this.email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getBranch_Name() {
    return this.branch_Name;
  }
  
  public void setBranch_Name(String branch_Name) {
    this.branch_Name = branch_Name;
  }
  
  public String getTransaction_Id() {
    return this.transaction_Id;
  }
  
  public void setTransaction_Id(String transaction_Id) {
    this.transaction_Id = transaction_Id;
  }
  
  public String getRequirement() {
    return this.requirement;
  }
  
  public void setRequirement(String requirement) {
    this.requirement = requirement;
  }
  
  public String getiB() {
    return this.iB;
  }
  
  public void setiB(String iB) {
    this.iB = iB;
  }

  public String getCcy() {
    return this.ccy;
  }
  
  public void setCcy(String ccy) {
    this.ccy = ccy;
  }

  public Integer getUsance_Period() {
    return usance_Period;
  }

  public void setUsance_Period(Integer usance_Period) {
    this.usance_Period = usance_Period;
  }

//  public String getUsance() {
//    return this.usance;
//  }
//
//  public void setUsance(String usance) {
//    this.usance = usance;
//  }
  
  public Float getConfirmation_charges_p_a() {
    return this.confirmation_charges_p_a;
  }
  
  public void setConfirmation_charges_p_a(Float confirmation_charges_p_a) {
    this.confirmation_charges_p_a = confirmation_charges_p_a;
  }
  
  public String getConfirmation_charges_from_date_of_issuance_till_negotiation_date() {
    return this.confirmation_charges_from_date_of_issuance_till_negotiation_date;
  }
  
  public void setConfirmation_charges_from_date_of_issuance_till_negotiation_date(String confirmation_charges_from_date_of_issuance_till_negotiation_date) {
    this.confirmation_charges_from_date_of_issuance_till_negotiation_date = confirmation_charges_from_date_of_issuance_till_negotiation_date;
  }
  
  public String getConfirmation_charges_from_date_of_issuance_till_maturity_date() {
    return this.confirmation_charges_from_date_of_issuance_till_maturity_date;
  }
  
  public void setConfirmation_charges_from_date_of_issuance_till_maturity_date(String confirmation_charges_from_date_of_issuance_till_maturity_date) {
    this.confirmation_charges_from_date_of_issuance_till_maturity_date = confirmation_charges_from_date_of_issuance_till_maturity_date;
  }
  
  public Float getOther_Charges() {
    return this.other_Charges;
  }
  
  public void setOther_Charges(Float other_Charges) {
    this.other_Charges = other_Charges;
  }
  
  public Float getMin_Trxn_Charges() {
    return this.min_Trxn_Charges;
  }
  
  public void setMin_Trxn_Charges(Float min_Trxn_Charges) {
    this.min_Trxn_Charges = min_Trxn_Charges;
  }
  
  public String getBank_Quote_validity() {
    return this.Bank_Quote_validity;
  }
  
  public void setBank_Quote_validity(String bank_Quote_validity) {
    this.Bank_Quote_validity = bank_Quote_validity;
  }
  
  public Float getDiscounting_charges_p_a() {
    return this.discounting_charges_p_a;
  }
  
  public void setDiscounting_charges_p_a(Float discounting_charges_p_a) {
    this.discounting_charges_p_a = discounting_charges_p_a;
  }
  
  public Float getRefinancing_charges_p_a() {
    return this.refinancing_charges_p_a;
  }
  
  public void setRefinancing_charges_p_a(Float refinancing_charges_p_a) {
    this.refinancing_charges_p_a = refinancing_charges_p_a;
  }
  
  public Float getBanker_accept_charges_p_a() {
    return this.banker_accept_charges_p_a;
  }
  
  public void setBanker_accept_charges_p_a(Float banker_accept_charges_p_a) {
    this.banker_accept_charges_p_a = banker_accept_charges_p_a;
  }
}
