package com.nimai.admin.payload;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ReportCustomerTransaction {
  private String user_ID;
  private String user_Type;
  private String landline_no;
  private String mobile;
  private String email;
//  @SerializedName("Trxn_Placed_Date_Time")
  private String Trxn_Placed_Date_Time;

  private String trxn_ID;
  
  private String applicant;
  
  private String applicant_country;
  
  private String beneficiary;
  
  private String beneficiary_Country;
  
  private String client_Contact ;
  
  private String client_Email_ID ;
  
  private String beneficiary_Bank_Country;
  
  private String beneficiary_Bank_Swift_Code;
  
  private String beneficiary_Bank_Name;
  
  private String issuing_Bank;
  
  private String issuing_Bank_Branch;
  
  private String issuing_Bank_Swift_Code;
  
  private String issuing_Bank_Country;
  
  private String requirement;
  
  private Double amount;
  
  private String ccy;
  private Double usd_currency_value;
  
  private String issuing_Date;
  
  private String lsd;
  
  private String negotiation_Date;
  
  private String goods;
  
  private Integer usance_Period;
  
  private Integer payment_Terms_With_Seller;
  
  private String lc_Maturity_Date;
  
//  private String lc_Number;
  
//  private String ben_trxn_bank_Name;
  
//  private String ben_trxn_swiftCode;
  
//  private String ben_trxn_Country;
  
  private String port_of_Loading;
  
  private String port_of_Loading_Country;
  
  private String port_of_Discharge;
  
  private String port_of_Discharge_Country;
  
  private String charges_are_on;
  
  private String transaction_Validity;
  
//  private Integer count_Quotes_Recd;
  
//  private String quote_Status;
//  private Double value;
  private String last_bene_bank_country;
  private Integer quotation_received;
  private String quotation_accepted;
  private String transaction_status;
//  private String quote_bank_name;
//
//  public String getQuote_bank_name() {
//    return quote_bank_name;
//  }
//
//  public void setQuote_bank_name(String quote_bank_name) {
//    this.quote_bank_name = quote_bank_name;
//  }

//  public Double getValue() {
//    return value;
//  }
//
//  public void setValue(Double value) {
//    this.value = value;
//  }


  public String getTransaction_status() {
    return transaction_status;
  }

  public void setTransaction_status(String transaction_status) {
    this.transaction_status = transaction_status;
  }

  public Double getUsd_currency_value() {
    return usd_currency_value;
  }

  public void setUsd_currency_value(Double usd_currency_value) {
    this.usd_currency_value = usd_currency_value;
  }

  public String getLast_bene_bank_country() {
    return last_bene_bank_country;
  }

  public void setLast_bene_bank_country(String last_bene_bank_country) {
    this.last_bene_bank_country = last_bene_bank_country;
  }

  public Integer getQuotation_received() {
    return quotation_received;
  }

  public void setQuotation_received(Integer quotation_received) {
    this.quotation_received = quotation_received;
  }

  public String getQuotation_accepted() {
    return quotation_accepted;
  }

  public void setQuotation_accepted(String quotation_accepted) {
    this.quotation_accepted = quotation_accepted;
  }

//  public String getQuote_Status() {
//    return quote_Status;
//  }

//  public void setQuote_Status(String quote_Status) {
//    this.quote_Status = quote_Status;
//  }
  //  public String getQuotes_Accepted() {
//	return quotes_Accepted;
//}
//
//public void setQuotes_Accepted(String quotes_Accepted) {
//	this.quotes_Accepted = quotes_Accepted;
//}

private String applicant_Contact;
  
  private String applicant_Email_ID;
  
  
  public String getLandline_no() {
    return this.landline_no;
  }
  
  public void setLandline_no(String landline_no) {
    this.landline_no = landline_no;
  }

  public String getApplicant_Contact() {
    return applicant_Contact;
  }

  public void setApplicant_Contact(String applicant_Contact) {
    this.applicant_Contact = applicant_Contact;
  }

  public String getApplicant_Email_ID() {
    return applicant_Email_ID;
  }

  public void setApplicant_Email_ID(String applicant_Email_ID) {
    this.applicant_Email_ID = applicant_Email_ID;
  }

  public String getUser_ID() {
    return this.user_ID;
  }
  
  public void setUser_ID(String user_ID) {
    this.user_ID = user_ID;
  }
  
  public String getUser_Type() {
    return this.user_Type;
  }
  
  public void setUser_Type(String user_Type) {
    this.user_Type = user_Type;
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
  
  public String getTrxn_Placed_Date_Time() {
	return Trxn_Placed_Date_Time;
}

public void setTrxn_Placed_Date_Time(String date_And_Time) {
	this.Trxn_Placed_Date_Time = date_And_Time;
}

public void setEmail(String email) {
    this.email = email;
  }
  
  public String getTrxn_ID() {
    return this.trxn_ID;
  }
  
  public void setTrxn_ID(String trxn_ID) {
    this.trxn_ID = trxn_ID;
  }
  
  public String getApplicant() {
    return this.applicant;
  }
  
  public void setApplicant(String applicant) {
    this.applicant = applicant;
  }
  
  public String getApplicant_country() {
    return this.applicant_country;
  }
  
  public void setApplicant_country(String applicant_country) {
    this.applicant_country = applicant_country;
  }
  
  public String getBeneficiary() {
    return this.beneficiary;
  }
  
  public void setBeneficiary(String beneficiary) {
    this.beneficiary = beneficiary;
  }
  
  public String getBeneficiary_Country() {
    return this.beneficiary_Country;
  }
  
  public void setBeneficiary_Country(String beneficiary_Country) {
    this.beneficiary_Country = beneficiary_Country;
  }
  
  public String getIssuing_Bank_Branch() {
	return issuing_Bank_Branch;
}

public void setIssuing_Bank_Branch(String issuing_Bank_Branch) {
	this.issuing_Bank_Branch = issuing_Bank_Branch;
}

  public String getClient_Contact() {
    return client_Contact;
  }

  public void setClient_Contact(String client_Contact) {
    this.client_Contact = client_Contact;
  }

  public String getClient_Email_ID() {
    return client_Email_ID;
  }

  public void setClient_Email_ID(String client_Email_ID) {
    this.client_Email_ID = client_Email_ID;
  }

  public String getBeneficiary_Bank_Country() {
	return beneficiary_Bank_Country;
}

public void setBeneficiary_Bank_Country(String beneficiary_Bank_Country) {
	this.beneficiary_Bank_Country = beneficiary_Bank_Country;
}
  
  public String getBeneficiary_Bank_Swift_Code() {
	return beneficiary_Bank_Swift_Code;
}

public void setBeneficiary_Bank_Swift_Code(String beneficiary_Bank_Swift_Code) {
	this.beneficiary_Bank_Swift_Code = beneficiary_Bank_Swift_Code;
}
  
  public String getBeneficiary_Bank_Name() {
	return beneficiary_Bank_Name;
}

public void setBeneficiary_Bank_Name(String beneficiary_Bank_Name) {
	this.beneficiary_Bank_Name = beneficiary_Bank_Name;
}
  
  public String getIssuing_Bank() {
	return issuing_Bank;
}

public void setIssuing_Bank(String issuing_Bank) {
	this.issuing_Bank = issuing_Bank;
}
  
  public String getIssuing_Bank_Swift_Code() {
	return issuing_Bank_Swift_Code;
}

public void setIssuing_Bank_Swift_Code(String issuing_Bank_Swift_Code) {
	this.issuing_Bank_Swift_Code = issuing_Bank_Swift_Code;
}
  
  public String getIssuing_Bank_Country() {
	return issuing_Bank_Country;
}

public void setIssuing_Bank_Country(String issuing_Bank_Country) {
	this.issuing_Bank_Country = issuing_Bank_Country;
}
  
  public Double getAmount() {
    return this.amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }
  
  public String getCcy() {
    return this.ccy;
  }

  public String getRequirement() {
    return requirement;
  }

  public void setRequirement(String requirement) {
    this.requirement = requirement;
  }

  public void setCcy(String ccy) {
    this.ccy = ccy;
  }
  
  public String getIssuing_Date() {
	return issuing_Date;
}

public void setIssuing_Date(String issuing_Date) {
	this.issuing_Date = issuing_Date;
}
  
  public String getLsd() {
	return lsd;
}

public void setLsd(String lsd) {
	this.lsd = lsd;
}
  
  public String getNegotiation_Date() {
	return negotiation_Date;
}

public void setNegotiation_Date(String negotiation_Date) {
	this.negotiation_Date = negotiation_Date;
}

public String getGoods() {
    return this.goods;
  }
  
  public void setGoods(String goods) {
    this.goods = goods;
  }

  public Integer getUsance_Period() {
    return usance_Period;
  }

  public void setUsance_Period(Integer usance_Period) {
    this.usance_Period = usance_Period;
  }

  public Integer getPayment_Terms_With_Seller() {
    return payment_Terms_With_Seller;
  }

  public void setPayment_Terms_With_Seller(Integer payment_Terms_With_Seller) {
    this.payment_Terms_With_Seller = payment_Terms_With_Seller;
  }

  public String getLc_Maturity_Date() {
    return this.lc_Maturity_Date;
  }
  
  public void setLc_Maturity_Date(String lc_Maturity_Date) {
    this.lc_Maturity_Date = lc_Maturity_Date;
  }
  
//  public String getLc_Number() {
//    return this.lc_Number;
//  }
//
//  public void setLc_Number(String lc_Number) {
//    this.lc_Number = lc_Number;
//  }
  
//  public String getBen_trxn_bank_Name() {
//    return this.ben_trxn_bank_Name;
//  }
//
//  public void setBen_trxn_bank_Name(String ben_trxn_bank_Name) {
//    this.ben_trxn_bank_Name = ben_trxn_bank_Name;
//  }
//
//  public String getBen_trxn_swiftCode() {
//    return this.ben_trxn_swiftCode;
//  }
//
//  public void setBen_trxn_swiftCode(String ben_trxn_swiftCode) {
//    this.ben_trxn_swiftCode = ben_trxn_swiftCode;
//  }
//
//  public String getBen_trxn_Country() {
//    return this.ben_trxn_Country;
//  }
//
//  public void setBen_trxn_Country(String ben_trxn_Country) {
//    this.ben_trxn_Country = ben_trxn_Country;
//  }
//
  public String getPort_of_Loading_Country() {
	return port_of_Loading_Country;
}

public void setPort_of_Loading_Country(String port_of_Loading_Country) {
	this.port_of_Loading_Country = port_of_Loading_Country;
}

public String getPort_of_Loading() {
    return this.port_of_Loading;
  }
  
  public void setPort_of_Loading(String port_of_Loading) {
    this.port_of_Loading = port_of_Loading;
  }
  
  public String getPort_of_Discharge() {
    return this.port_of_Discharge;
  }
  
  public void setPort_of_Discharge(String port_of_Discharge) {
    this.port_of_Discharge = port_of_Discharge;
  }
  
  public String getPort_of_Discharge_Country() {
	return port_of_Discharge_Country;
}

public void setPort_of_Discharge_Country(String port_of_Discharge_Country) {
	this.port_of_Discharge_Country = port_of_Discharge_Country;
}

public String getCharges_are_on() {
    return this.charges_are_on;
  }
  
  public void setCharges_are_on(String charges_are_on) {
    this.charges_are_on = charges_are_on;
  }
  
  public String getTransaction_Validity() {
	return transaction_Validity;
}

public void setTransaction_Validity(String transaction_Validity) {
	this.transaction_Validity = transaction_Validity;
}

//  public Integer getCount_Quotes_Recd() {
//    return count_Quotes_Recd;
//  }

//  public void setCount_Quotes_Recd(Integer count_Quotes_Recd) {
//    this.count_Quotes_Recd = count_Quotes_Recd;
//  }

  //public Integer getQuotes_Received() {
//    return this.quotes_Received;
//  }
//
//  public void setQuotes_Received(Integer quotes_Received) {
//    this.quotes_Received = quotes_Received;
//  }
}
