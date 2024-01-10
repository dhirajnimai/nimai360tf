package com.nimai.admin.service.impl;

import com.nimai.admin.model.NimaiLCMaster;


import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMDiscount;
import com.nimai.admin.model.NimaiMRefer;
import com.nimai.admin.model.NimaiMmTransaction;
import com.nimai.admin.model.NimaiSubscriptionDetails;
import com.nimai.admin.payload.ReportBankRmPerformance;
import com.nimai.admin.payload.ReportBankRmUwPerformance;
import com.nimai.admin.payload.ReportBankTransaction;
import com.nimai.admin.payload.ReportCountryWise;
import com.nimai.admin.payload.ReportCustomerRmPerformance;
import com.nimai.admin.payload.ReportCustomerTransaction;
import com.nimai.admin.payload.ReportDiscountCoupon;
import com.nimai.admin.payload.ReportNewUserStatus;
import com.nimai.admin.payload.ReportPaymentAndSubscription;
import com.nimai.admin.payload.ReportProductRequirement;
import com.nimai.admin.payload.ReportReferrer;
import com.nimai.admin.payload.ReportUserSubscriptionRenewal;
import com.nimai.admin.payload.ReportsTxnExpiry;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.property.GenericExcelWriter;
import com.nimai.admin.repository.*;
import com.nimai.admin.service.ReportService;
import com.nimai.admin.specification.CustomerSearchSpecification;
import com.nimai.admin.specification.DiscountSpecification;
import com.nimai.admin.specification.TransactionsSpecification;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.text.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;

import com.nimai.admin.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {
  @Autowired
  CustomerRepository customerRepository;
  @Autowired
  SubscriptionVasRepository subscriptionVasRepo;
  
  @Autowired
  EmployeeRepository employeeRepository;
  
  @Autowired
  EntityManagerFactory em;
  
  @Autowired
  TransactionsRepository transactionRepository;
  
  @Autowired
  TransactionsSpecification transactionSpecification;
  
  @Autowired
  CustomerSearchSpecification customerSearchSpecification;
  
  @Autowired
  SubscriptionDetailsRepository subsDetailsRepo;
  
  @Autowired
  DiscountRepository discountRepo;
  
  @Autowired
  nimaiSystemConfigRepository nimaiSystemRepo;
  
  @Autowired
  DiscountSpecification discSpecification;
  
  @Autowired
  SubscriptionDetailsRepository sPLanRepo;
  
  @Autowired
  ReferrerRepository referRepo;
  
  @Autowired
  QuoteCalTimingDetailsRepository quoTimeRepo;
  
  Date date = null;
  
   DecimalFormat df = new DecimalFormat("0.00");

  public ByteArrayInputStream getAllBankTransactionDetails(SearchRequest request, String fileName) {
    List<Tuple> bankProjection;
    List<ReportBankTransaction> transaction = new ArrayList<>();
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      bankProjection = this.customerRepository.getAllTransactionDetailsUserId(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), request.getUserId());
    } else {
      bankProjection = this.customerRepository.getAllTransactionDetails(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    }
    NimaiMCustomer custDetails = new NimaiMCustomer();
//    System.out.println(">>>>>>>>>>> "+bankProjection.toString());
    for (Tuple details : bankProjection) {
      ReportBankTransaction bank = new ReportBankTransaction();
      custDetails = this.customerRepository.getOne((String) details.get("bank_userid"));
//      System.out.println(details.toString());
//      System.out.println(custDetails);

      bank.setUser_ID((String)details.get("bank_userid"));
      bank.setCustomer_UserId((String)details.get("Customer_User_Id"));
      bank.setMobile(((String)details.get("mobile_number") != null) ? (String)details.get("mobile_number") : "");
      bank.setLandline_no((custDetails.getLandline() != null) ? custDetails.getLandline() : "");
      bank.setEmail(((String)details.get("email_address") != null) ? (String)details.get("email_address") : "");
//      bank.setDate_3_Time(((java.util.Date)details.get("inserted_date") != null) ? (java.util.Date)details
//Prateek          .get("inserted_date") : this.date);
      bank.setDate_And_Time(details.get("inserted_date").toString());
//      bank.setDate_And_Time(details.get("inserted_date").toString().substring(0, 10));
      bank.setUnderwriting_Bank(((String)details.get("Underwriting_Bank") != null) ? (String)details.get("Underwriting_Bank") : "");
      bank.setBranch_Name(((String)details.get("branch_name") != null) ? (String)details.get("branch_name") : "");
      bank.setUnderwriting_Bank_Country(((String)details.get("Underwriting_Bank_Country") != null) ? (String)details.get("Underwriting_Bank_Country") : "");
      bank.setTransaction_Id(
          ((String)details.get("transaction_id") != null) ? (String)details.get("transaction_id") : "");
      if (details.get("requirement_type").equals("ConfirmAndDiscount")) {
        bank.setRequirement("Confirmation and Discounting");
      } else if (details.get("requirement_type").equals("Banker")) {
        bank.setRequirement("Banker's Acceptance");
      }else if (details.get("requirement_type").equals("BillAvalisation")) {
        bank.setRequirement("Avalisation");
      } else if (details.get("requirement_type").equals("Refinance")) {
        bank.setRequirement("Refinancing");
        bank.setOriginal_tenor_days((details.get("original_tenor_days")!=null)
                && (!Objects.equals(details.get("original_tenor_days").toString(),""))?
                (Integer)details.get("original_tenor_days"):0);
      } else {
        bank.setRequirement(
            (details.get("requirement_type") != null) ?
                    details.get("requirement_type").toString()
                            .replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2") : "");
      }
//      bank.setUsd_currency_value((details.get("usd_currency_value")!=null)?
//              (!Objects.equals(details.get("usd_currency_value").toString(),""))?
//                      Double.parseDouble(
//                              String.format("%.2s",details.get("usd_currency_value")))
//                      :0.0D:0.0D);

      bank.setUsd_currency_value((details.get("usd_currency_value")!=null)
              && (!Objects.equals(details.get("usd_currency_value").toString(),""))?
              (Double) details.get("usd_currency_value") :0.0D);

      bank.setValue((details.get("lc_value")!=null)
              && (!Objects.equals(details.get("lc_value").toString(),""))?
              (Double) details.get("lc_value"):0.0D);
      bank.setiB(
          (details.get("lc_issuance_bank") != null) ? (String)details.get("lc_issuance_bank") : "");
      bank.setLc_issuance_branch_name(
          (details.get("lc_issuance_branch") != null) ? (String)details.get("lc_issuance_branch") : "");
      bank.setCcy((details.get("lc_currency") != null) ?
              (String)details.get("lc_currency") : "");
//      if (details.get("requirement_type").equals("Banker") || details
//        .get("requirement_type").equals("Discounting")) {
//        bank.setUsance_Period(
//            ((String)details.get("discounting_period") != null) ? (String)details.get("discounting_period") : "");
//      } else if (details.get("requirement_type").equals("Refinance")) {
//        bank.setUsance_Period(
//            ((String)details.get("refinancing_period") != null) ? (String)details.get("refinancing_period") : "");
//      } else if(details.get("requirement_type").equals("Confirmation")){
//        bank.setUsance_Period((details.get("confirmation_period") != null) ?
//                Integer.parseInt((String) details.get("confirmation_period")): 0);
//      }

//      bank.setUsance_Period(
//          Integer.valueOf(((Integer)details.get("original_tenor_days") != null) ? ((Integer)details.get("original_tenor_days")).intValue() : 0));
     
//     bank.setApplicable_benchmark(
//             (details.get("applicable_benchmark") != null)
//                     && (!Objects.equals(details.get("applicable_benchmark").toString(),"")) ?
//                     Float.parseFloat(String.format() details.get("applicable_benchmark")
//                             .toString()) : 0.0F);
      bank.setApplicable_benchmark(Utility.getDecimalFormat
              (details.get("applicable_benchmark")));

 //     bank.setApplicable_benchmark(
 //             Float.valueOf(((Float)details.get("applicable_benchmark") != null) ? (Float.valueOf(df.format((Float)details.get("applicable_benchmark")))).floatValue() : 0.0F));
      
//      System.out.println("Prateek : " + bank.getApplicable_benchmark()+" | "+bank.getUser_ID());
      
//      System.out.println("Pratt : " + df.format(bank.getApplicable_benchmark())); 
//		float app_ben_f=Float.valueOf(df.format(bank.getApplicable_benchmark()));
//		System.out.println("Prateek2 : " + app_ben_f);
//		bank.setApplicable_benchmark(app_ben_f);
//		System.out.println("Prateek1 : " + bank.getApplicable_benchmark());
//      System.out.println("Pratt : " + ((Float)details.get("applicable_benchmark")));
//      offered_price
 //    bank.setTotal_Quote_Value(((String)details.get("Total_Quote_Value") != null) ? (String)details.get("Total_Quote_Value") : "");
     if(details.get("IS_OFFERED")!= null && details.get("IS_OFFERED").equals("Yes")){
       bank.setTotal_Quote_Value(
               (details.get("offered_price") != null)
               && (!Objects.equals(details.get("offered_price").toString(),""))
                       ?details.get("offered_price").toString() : "0.0");
     }else {
       bank.setTotal_Quote_Value(
               (details.get("Total_Quote_Value") != null)
                       && (!Objects.equals(details.get("Total_Quote_Value").toString(), ""))
                       ? details.get("Total_Quote_Value").toString() : "0.0");
     }
     System.out.println("Prateek : " + bank.getTotal_Quote_Value()+" | "+bank.getUser_ID());
     
//      bank.setConfirmation_charges_p_a(
//              (details.get("confirmation_charges") != null)
//                      && (!Objects.equals(details.get("confirmation_charges").toString(), ""))
//                      ? Float.parseFloat(df.format(details.get("confirmation_charges"))) : 0.0F);
//      bank.setDiscounting_charges_p_a(
//              (details.get("discounting_charges") != null)
//                      && (!Objects.equals(details.get("discounting_charges").toString(), ""))
//                      ? Float.parseFloat(df.format(details.get("discounting_charges"))) : 0.0F);
//      bank.setRefinancing_charges_p_a(
//              (details.get("refinancing_charges") != null)
//                      && (!Objects.equals(details.get("refinancing_charges").toString(), ""))
//                      ? Float.parseFloat(df.format(details.get("refinancing_charges"))) : 0.0F);
//      bank.setBanker_accept_charges_p_a(
//              (details.get("banker_accept_charges") != null) ?
//                      Float.parseFloat(df.format(details.get("banker_accept_charges"))) : 0.0F);
      System.out.println("confirmation_charges ---> "+details.get("confirmation_charges"));
      System.out.println("confirmation_charges ---> "+Utility.getDecimalFormat(details.get("confirmation_charges")));
      bank.setConfirmation_charges_p_a
              (Utility.getDecimalFormat(details.get("confirmation_charges")));
      bank.setDiscounting_charges_p_a(
              Utility.getDecimalFormat(details.get("discounting_charges")));
      bank.setRefinancing_charges_p_a(
              Utility.getDecimalFormat(details.get("refinancing_charges")));
      bank.setBanker_accept_charges_p_a(
              Utility.getDecimalFormat(details.get("banker_accept_charges")));

      bank.setConfirmation_charges_from_date_of_issuance_till_negotiation_date(
          (String.valueOf(details.get("conf_chgs_issuance_to_negot")) != null) ? String.valueOf(details.get("conf_chgs_issuance_to_negot")) : "0");
      bank.setConfirmation_charges_from_date_of_issuance_till_maturity_date(
          (String.valueOf(details.get("conf_chgs_issuance_to_matur")) != null) ? String.valueOf(details.get("conf_chgs_issuance_to_matur")) : "0");
      bank.setNegotiation_charges_in_fixed(
              Utility.getDecimalFormat(details.get("negotiation_charges_fixed")));
      bank.setNegotiation_charges_in_percentage(
              Utility.getDecimalFormat(details.get("negotiation_charges_perct")));
      bank.setOther_Charges(
              Utility.getDecimalFormat(details.get("other_charges")));
      bank.setMin_Trxn_Charges(
              Utility.getDecimalFormat(details.get("min_transaction_charges")));
      bank.setBank_Quote_validity(details.get("Quote_Validity_date").toString().substring(0, 10));
      bank.setTransaction_Validity(details.get("Transaction_Validity").toString().substring(0, 10));
      bank.setQuote_Status(details.get("quotation_status")!=null?
              details.get("quotation_status").toString():null);
      bank.setTerm_Condition(details.get("term_condition_comments")!=null?
              details.get("term_condition_comments").toString():null);
      bank.setPayment_Terms(details.get("payment_terms")!=null?
              details.get("payment_terms").toString():null);
      Object requirement_type = details.get("requirement_type");
      if ("Confirmation".equals(requirement_type) || 
              "Confirmation and Discounting".equals(requirement_type) ||
              "Bank Guarantee".equals(requirement_type)) {

        bank.setUsance_Period((details.get("confirmation_period") != null)
                && (!Objects.equals(details.get("confirmation_period").toString(), "")) ?
                Integer.parseInt(details.get("confirmation_period").toString()) : 0);
      } else if ("Avalisation".equals(requirement_type) ||
              "Banker's Acceptance".equals(requirement_type) ||
              "Discounting".equals(requirement_type)) {
        bank.setUsance_Period((details.get("discounting_period") != null)
                && (!Objects.equals(details.get("discounting_period").toString(), "")) ?
                Integer.parseInt(details.get("discounting_period").toString()) : 0);
      } else if ("Refinancing".equals(requirement_type)) {
        bank.setUsance_Period((details.get("refinancing_period") != null)
                && (!Objects.equals(details.get("refinancing_period").toString(), "")) ?
                Integer.parseInt(details.get("refinancing_period").toString()) : 0);
      }else {
        bank.setUsance_Period(0);
      }

      transaction.add(bank);
    }
    return GenericExcelWriter.writeToExcel(fileName, transaction);
  }
  
  public ByteArrayInputStream getTransactioReports(SearchRequest request, String filename) {
    List<Tuple> tupleTxn;
    List<ReportsTxnExpiry> trans = new ArrayList<>();
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      tupleTxn = this.transactionRepository.getAllTransDetailsUserId(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), request.getUserId());
    } else {
      tupleTxn = this.transactionRepository.getAllTransDetails(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } 
    for (Tuple t : tupleTxn) {
      ReportsTxnExpiry repTran = new ReportsTxnExpiry();
      repTran.setTrxn_ID((String)t.get("txnId"));
      repTran.setDate_Time(
          ((java.util.Date)t.get("date_time") != null) ? (java.util.Date)t.get("date_time") : this.date);
      repTran.setApplicant(((String)t.get("applicant") != null) ? (String)t.get("applicant") : "");
      repTran.setA_Country(((String)t.get("a_country") != null) ? (String)t.get("a_country") : "");
      repTran.setBeneficiary(((String)t.get("beneficiary") != null) ? (String)t.get("beneficiary") : "");
      repTran.setB_country(((String)t.get("b_country") != null) ? (String)t.get("b_country") : "");
      repTran.setRequirement(((String)t.get("requirement") != null) ? (String)t.get("requirement") : "");
      repTran.setAmount(Long.valueOf((Long.valueOf(((Double)t.get("amount")).longValue()) != null) ? 
            Long.valueOf(((Double)t.get("amount")).longValue()).longValue() : 0L));
      repTran.setCcy(((String)t.get("ccy") != null) ? (String)t.get("ccy") : "");
      repTran.setCustomer(((String)t.get("customer") != null) ? (String)t.get("customer") : "");
      
      if((String)t.get("validity")==null) {
    	  repTran.setValidity(" ");
      }else {
    	  repTran.setValidity((((String)t.get("validity")).replace("-", "/").replaceAll(" 00:00:00", "") != null) ? (String)t
    	          .get("validity") : "");
      }
     
      
      if (((BigInteger)t.get("expires_in"))==null) {
          repTran.setExpired_in(" ");
        }
      else if (((BigInteger)t.get("expires_in")).intValue() < 0) {
        repTran.setExpired_in("expired");
      } else if (((BigInteger)t.get("expires_in")).intValue() == 0) {
        repTran.setExpired_in("Expiring Today");
      } else {
        repTran.setExpired_in(t.get("expires_in").toString() + " days");
      } 
      repTran.setRm(((String)t.get("rm") != null) ? (String)t.get("rm") : "");
      trans.add(repTran);
    } 
    return GenericExcelWriter.writeToExcel(filename, trans);
  }
  
  public ByteArrayInputStream getNewUserReport(SearchRequest request, String filename) {
    List<Tuple> tuple;
    List<ReportNewUserStatus> newUser = new ArrayList<>();
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      tuple = this.customerRepository.getNewUserIdReports(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), request.getUserId());
    } else {
      tuple = this.customerRepository.getNewUserReports(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } 
    for (Tuple details : tuple) {
      ReportNewUserStatus newReport = new ReportNewUserStatus();
      newReport.setUser_ID((String)details.get("userid"));
      newReport.setUser_Type(
          ((String)details.get("customer_type") != null) ? (String)details.get("customer_type") : "");
      newReport.setCustomer$Bank$Bank_as_Customer_Name(
          ((String)details.get("customer_Name") != null) ? (String)details.get("customer_Name") : "");
      newReport.setSubscription(
          ((String)details.get("subs_plan") != null) ? (String)details.get("subs_plan") : "");
      if (details.get("vas") != null)
        if (details.get("vas").equals(Integer.valueOf(0))) {
          newReport.setvAS("No");
        } else if (details.get("vas").equals(Integer.valueOf(1))) {
          newReport.setvAS("Yes");
        }  
      newReport.setFees_Paid_$USD$(Double.valueOf(((Double)details.get("fee") != null) ? ((Double)details.get("fee")).doubleValue() : 0.0D));
      newReport.setPayment_Mode(
          ((String)details.get("mode_of_payment") != null) ? (String)details.get("mode_of_payment") : "");
      newReport.setPayment_Status(
          ((String)details.get("payment_status") != null) ? (String)details.get("payment_status") : "");
      newReport.setkYC_Status(
          ((String)details.get("kyc_status") != null) ? (String)details.get("kyc_status") : "");
      newReport.setRm(((String)details.get("rm") != null) ? (String)details.get("rm") : "");
      newUser.add(newReport);
    } 
    return GenericExcelWriter.writeToExcel(filename, newUser);
  }
  
  public ByteArrayInputStream getUSubsRenewal(SearchRequest request, String filename) {
    List<Tuple> reports;
    List<ReportUserSubscriptionRenewal> renewal = new ArrayList<>();
    System.out.println("----------------" + request.getDateFrom());
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      reports = this.subsDetailsRepo.getUserSubsRenewalUserId(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(0L)), request.getUserId());
    } else {
      reports = this.subsDetailsRepo.getUserSubsRenewal(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } 
    for (Tuple response : reports) {
      ReportUserSubscriptionRenewal responses = new ReportUserSubscriptionRenewal();
      responses.setUser_ID(((String)response.get("userid") != null) ? (String)response.get("userid") : "");
      responses.setUser_Type(
          ((String)response.get("subscriber_type") != null) ? (String)response.get("subscriber_type") : "");
      responses.setCustomer$Bank$Bank_as_Customer_Name(
          ((String)response.get("company_name") != null) ? (String)response.get("company_name") : "");
      responses.setSubscription(
          ((String)response.get("subscription_name") != null) ? (String)response.get("subscription_name") : "");
      if (((Integer)response.get("is_vas_applied")).intValue() == 0) {
        responses.setVas("No");
      } else if (((Integer)response.get("is_vas_applied")).intValue() == 1) {
        responses.setVas("Yes");
      } 
      responses.setFees_Paid(Integer.valueOf(((Integer)response.get("subscription_amount") != null) ? ((Integer)response
            .get("subscription_amount")).intValue() : 0));
      responses.setActivation_Date(
          ((Date)response.get("splan_start_date") != null) ? (Date)response.get("splan_start_date") : this.date);
      responses.setExpiry_Date(
          ((Date)response.get("splan_end_date") != null) ? (Date)response.get("splan_end_date") : this.date);
      responses.setCredits_Available(
          Double.valueOf(((Double)response.get("credits_available") != null) ? ((Double)response.get("credits_available")).doubleValue() : 0.0D));
      responses.setSubsidiaries$Additional(
          ((String)response.get("subsidiaries") != null) ? (String)response.get("subsidiaries") : "");
      renewal.add(responses);
    } 
    return GenericExcelWriter.writeToExcel(filename, renewal);
  }
  
  public ByteArrayInputStream getDiscountReport(SearchRequest request, String filename) {
    List<NimaiMDiscount> mDiscount = this.discountRepo.findAll(this.discSpecification.getFilter(request));
    List<ReportDiscountCoupon> disCoupon = new ArrayList<>();
    for (NimaiMDiscount disc : mDiscount) {
      ReportDiscountCoupon response = new ReportDiscountCoupon();
      response.setDiscount_Type((disc.getDiscountType() != null) ? disc.getDiscountType() : "");
      response.setDiscount_amount(Double.valueOf((Double.valueOf(disc.getAmount()) != null) ? disc.getAmount() : 0.0D));
      response.setCcy((disc.getCurrency() != null) ? disc.getCurrency() : "");
      response.setDiscount_2(Double.valueOf((Double.valueOf(disc.getDiscountPercentage()) != null) ? disc.getDiscountPercentage() : 0.0D));
      response.setMax_Discount(Double.valueOf((Double.valueOf(disc.getMaxDiscount()) != null) ? disc.getMaxDiscount() : 0.0D));
      response.setCcy((disc.getCurrency() != null) ? disc.getCurrency() : "");
      response.setCoupon_For((disc.getCouponFor() != null) ? disc.getCouponFor() : "");
      response.setSubscripton_Plan((disc.getSubscriptionPlan() != null) ? disc.getSubscriptionPlan() : "");
      response.setCountry((disc.getCountry() != null) ? disc.getCountry() : "");
      response.setQuantity(Integer.valueOf((disc.getQuantity() != null) ? disc.getQuantity().intValue() : 0));
      response.setStart_Date((disc.getStartDate() != null) ? disc.getStartDate() : this.date);
      response.setEnd_Date((disc.getEndDate() != null) ? disc.getEndDate() : this.date);
      response.setCoupon_Code((disc.getCouponCode() != null) ? disc.getCouponCode() : "");
      response.setConsumed(Integer.valueOf((disc.getConsumedCoupons() != null) ? disc.getConsumedCoupons().intValue() : 0));
      disCoupon.add(response);
    } 
    return GenericExcelWriter.writeToExcel(filename, disCoupon);
  }
  
  public ByteArrayInputStream getProdReqReport(SearchRequest request, String filename) {
    List<Tuple> reqReport = this.transactionRepository.getProductReqRep(Date.valueOf(request.getDateFrom()), 
        Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    List<ReportProductRequirement> product = new ArrayList<>();
    for (Tuple tuple : reqReport) {
      ReportProductRequirement response = new ReportProductRequirement();
      response.setCountry(
          ((String)tuple.get("lc_issuance_country") != null) ? (String)tuple.get("lc_issuance_country") : "");
      response.setCcy(((String)tuple.get("lc_currency") != null) ? (String)tuple.get("lc_currency") : "");
      response.setProduct_Type(
          ((String)tuple.get("requirement_type") != null) ? (String)tuple.get("requirement_type") : "");
      response.setTxn_Placed(Integer.valueOf(((BigInteger)tuple.get("txn_Placed")).intValue()));
      if (tuple.get("lc_value") == null) {
        response.setCumulative_LC_Value(Double.valueOf(0.0D));
      } else {
        response.setCumulative_LC_Value((Double)tuple.get("lc_value"));
      } 
      response.setQuotes_Received(Integer.valueOf(((BigInteger)tuple.get("total_quotes")).intValue()));
      response.setCumulative_Quote_Value(
          Double.valueOf(((Double)tuple.get("quote_value") != null) ? ((Double)tuple.get("quote_value")).doubleValue() : 0.0D));
      product.add(response);
    } 
    return GenericExcelWriter.writeToExcel(filename, product);
  }
  
  public ByteArrayInputStream getReffReport(SearchRequest request, String filename) throws ParseException {
    DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
    java.util.Date fromDate = formatter.parse(request.getDateFrom());
    java.util.Date toDate = formatter.parse(request.getDateTo());
    List<ReportReferrer> referrer = new ArrayList<>();
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      NimaiMCustomer referrDetails = (NimaiMCustomer)this.customerRepository.getOne(request.getUserId());
      ReportReferrer response = new ReportReferrer();
      response.setCountry(referrDetails.getCountryName());
      response.setReferrer_userID(referrDetails.getUserid());
      response.setFirst_Name(referrDetails.getFirstName());
      response.setLast_Name(referrDetails.getLastName());
      response.setReferrerEmailId(referrDetails.getEmailAddress());
      response.setRm(referrDetails.getRmId());
      response.setReferrer_Company(referrDetails.getCompanyName());
      Integer approvedReference = Integer.valueOf(this.customerRepository.getApprovedReferrence(request.getUserId()));
      if (approvedReference.equals(null)) {
        response.setApproved_References(0);
      } else {
        response.setApproved_References(approvedReference.intValue());
      } 
      try {
        Integer totalReference = Integer.valueOf(this.customerRepository.getTotareference(request.getUserId()));
        if (totalReference.equals(null)) {
          response.setTotal_References(0);
        } else {
          response.setTotal_References(totalReference.intValue());
        } 
      } catch (Exception e) {
        e.printStackTrace();
      } 
      try {
        Integer pendingReference = Integer.valueOf(this.customerRepository.getpendingReference(request.getUserId()));
        if (pendingReference.equals(null)) {
          response.setPending_References(0);
        } else {
          response.setPending_References(pendingReference.intValue());
        } 
      } catch (Exception e) {
        e.printStackTrace();
      } 
      try {
        Integer rejectedReference = Integer.valueOf(this.customerRepository.getRejectedReference(request.getUserId()));
        if (rejectedReference.equals(null)) {
          response.setRejected_References(0);
        } else {
          response.setRejected_References(rejectedReference.intValue());
        } 
      } catch (Exception e) {
        e.printStackTrace();
      } 
      String accountStatus = checkAccStatus(referrDetails);
      if (accountStatus == null) {
        response.setAccount_status("INACTIVE");
        response.setEarning(0.0D);
      } else if (accountStatus.equalsIgnoreCase("ACTIVE")) {
        response.setAccount_status("ACTIVE");
        try {
          String loginId = Utility.getUserId();
          Double value=0.0D;
          if(Objects.equals(request.getRole(), "Customer RM")){
            System.out.println("request.getUserId()-------> "+request.getUserId()+" loginId ------> "+loginId);
            value = this.customerRepository.getEarningByCustRm(request.getUserId(),loginId);
          }else {
            System.out.println("ref.getUserid()-------> "+request.getUserId());
            value = this.customerRepository.getEarning(request.getUserId());
          }
//          List<Tuple> custList = this.customerRepository.getEarning(request.getUserId(),loginId);
//          Double vasAmount = this.subscriptionVasRepo.getVasAmount(custList.stream().map(e->e.get("USERID").toString()).collect(Collectors.toList()));
//          Double value = Utility.getReferEarnings(custList,"all",vasAmount);
          Float referEarning = Float.valueOf(this.nimaiSystemRepo.earningPercentage());
          Float actualREarning = Float.valueOf(referEarning.floatValue() / 100.0F);
          Float earning = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format(value.doubleValue() * actualREarning.floatValue())));
          if (earning.equals(null)) {
            response.setEarning(0.0D);
          } else {
            response.setEarning(earning.floatValue());
          } 
        } catch (Exception e) {
          e.printStackTrace();
        } 
      } else {
        response.setAccount_status("INACTIVE");
        response.setEarning(0.0D);
      } 
      response.setCcy(referrDetails.getCurrencyCode());
      response.setRm(referrDetails.getRmId());
      referrer.add(response);
    } else {
      DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      List<NimaiMRefer> referList = this.referRepo.finBydates(request.getDateFrom(), request.getDateTo());
      System.out.println(request.getDateFrom());
      System.out.println(request.getDateTo());
      System.out.println("Inside referList else conditions" + referList.toString());
      for (NimaiMRefer cust : referList) {
        System.out.println("Inside else for loop referList else conditions" + cust.toString());
        NimaiMCustomer referrDetails = (NimaiMCustomer)this.customerRepository.getOne(cust.getUserid().getUserid());
        ReportReferrer response = new ReportReferrer();
        response.setCountry(cust.getCountryName());
        response.setReferrer_userID(cust.getUserid().getUserid());
        response.setFirst_Name(cust.getFirstName());
        response.setLast_Name(cust.getLastName());
        response.setReferrerEmailId(cust.getUserid().getEmailAddress());
        System.out.println("===================company Name" + referrDetails.getCompanyName());
        response.setReferrer_Company(referrDetails.getCompanyName());
        Integer approvedReference = Integer.valueOf(this.customerRepository.getApprovedReferrence(cust.getUserid().getUserid()));
        if (approvedReference.equals(null)) {
          response.setApproved_References(0);
        } else {
          response.setApproved_References(approvedReference.intValue());
        } 
        try {
          Integer totalReference = Integer.valueOf(this.customerRepository.getTotareference(cust.getUserid().getUserid()));
          if (totalReference.equals(null)) {
            response.setTotal_References(0);
          } else {
            response.setTotal_References(totalReference.intValue());
          } 
        } catch (Exception e) {
          e.printStackTrace();
          continue;
        } 
        try {
          Integer pendingReference = Integer.valueOf(this.customerRepository.getpendingReference(cust.getUserid().getUserid()));
          if (pendingReference.equals(null)) {
            response.setPending_References(0);
          } else {
            response.setPending_References(pendingReference.intValue());
          } 
        } catch (Exception e) {
          e.printStackTrace();
          continue;
        } 
        try {
          Integer rejectedReference = Integer.valueOf(this.customerRepository.getRejectedReference(cust.getUserid().getUserid()));
          if (rejectedReference.equals(null)) {
            response.setRejected_References(0);
          } else {
            response.setRejected_References(rejectedReference.intValue());
          } 
        } catch (Exception e) {
          e.printStackTrace();
          continue;
        } 
        String accountStatus = checkAccStatus(referrDetails);
        if (accountStatus == null) {
          response.setAccount_status("INACTIVE");
          response.setEarning(0.0D);
        } else if (accountStatus.equalsIgnoreCase("ACTIVE")) {
          response.setAccount_status("ACTIVE");
          try {
            String loginId = Utility.getUserId();
            Double value=0.0D;
            if(Objects.equals(request.getRole(), "Customer RM")){
              System.out.println("cust.getUserid().getUserid()-------> "+cust.getUserid().getUserid()+" loginId ------> "+loginId);
              value = this.customerRepository.getEarningByCustRm(cust.getUserid().getUserid(),loginId);
            }else {
              System.out.println("ref.getUserid()-------> "+cust.getUserid().getUserid());
              value = this.customerRepository.getEarning(cust.getUserid().getUserid());
            }
//            List<Tuple> custList = this.customerRepository.getEarning(cust.getUserid().getUserid(),loginId);
//            Double vasAmount = this.subscriptionVasRepo.getVasAmount(custList.stream().map(e->e.get("USERID").toString()).collect(Collectors.toList()));
//            Double value = Utility.getReferEarnings(custList,"all",vasAmount);
            Float referEarning = Float.valueOf(this.nimaiSystemRepo.earningPercentage());
            Float actualREarning = Float.valueOf(referEarning.floatValue() / 100.0F);
            Float earning = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format(value.doubleValue() * actualREarning.floatValue())));
            if (earning.equals(null)) {
              response.setEarning(0.0D);
            } else {
              response.setEarning(earning.floatValue());
            } 
          } catch (Exception e) {
            e.printStackTrace();
            continue;
          } 
        } else {
          response.setAccount_status("INACTIVE");
          response.setEarning(0.0D);
        } 
        response.setCcy(cust.getUserid().getCurrencyCode());
        response.setRm(cust.getUserid().getRmId());
        referrer.add(response);
      } 
    } 
    return GenericExcelWriter.writeToExcel(filename, referrer);
  }
  
  public String checkAccStatus(NimaiMCustomer custDetails) {
    String accountStatus = "";
    if (custDetails.getUserid().substring(0, 2).equalsIgnoreCase("BA") || custDetails
      .getUserid().substring(0, 2).equalsIgnoreCase("BC") || custDetails
      .getUserid().substring(0, 2).equalsIgnoreCase("CU")) {
      if (custDetails.getPaymentStatus() == null || custDetails.getKycStatus() == null) {
        accountStatus = "INACTIVE";
      } else if (custDetails.getPaymentStatus().equalsIgnoreCase("Approved") && custDetails
        .getKycStatus().equalsIgnoreCase("Approved")) {
        accountStatus = "ACTIVE";
      } else {
        accountStatus = "INACTIVE";
      } 
    } else if (custDetails.getKycStatus() == null) {
      accountStatus = "INACTIVE";
    } else if (custDetails.getKycStatus().equalsIgnoreCase("Approved")) {
      accountStatus = "ACTIVE";
    } else {
      accountStatus = "INACTIVE";
    } 
    return accountStatus;
  }
  
  public ByteArrayInputStream getCountryWiseReport(SearchRequest request, String filename) {
    List<NimaiMmTransaction> transaction = this.transactionRepository.findCountryWiseDataByDates(request.getDateFrom(), request
        .getDateTo());
    System.out.println(transaction.size());
    List<ReportCountryWise> country = new ArrayList<>();
    for (NimaiMmTransaction details : transaction) {
      ReportCountryWise countReport = new ReportCountryWise();
      Integer txnCount = this.transactionRepository.getTxnCount(details.getLcIssuanceCountry(), details
          .getLcCurrency());
      if (txnCount == null) {
        countReport.setTrxn_Count(Integer.valueOf(0));
      } else {
        countReport.setTrxn_Count(txnCount);
      } 
      Double getcumulativeTxnAMount = this.transactionRepository.getcumulativeTxnAMount(details.getLcIssuanceCountry(), details
          .getLcCurrency());
      if (getcumulativeTxnAMount == null) {
        countReport.setCumulative_Trxn_Amount("0");
      } else {
        String cumulativeTxnAMount = (new BigDecimal(getcumulativeTxnAMount.doubleValue())).toEngineeringString();
        countReport.setCumulative_Trxn_Amount(cumulativeTxnAMount);
      } 
      Integer gettxnAccepted = this.transactionRepository.gettxnAccepted(details.getLcIssuanceCountry(), details
          .getLcCurrency());
      if (gettxnAccepted == null) {
        countReport.setTrxn_Accepted(Integer.valueOf(0));
      } else {
        countReport.setTrxn_Accepted(gettxnAccepted);
      } 
      Integer gettxnRejected = this.transactionRepository.gettxnRejected(details.getLcIssuanceCountry(), details
          .getLcCurrency());
      if (gettxnRejected == null) {
        countReport.setTrxn_Rejected(Integer.valueOf(0));
      } else {
        countReport.setTrxn_Rejected(gettxnRejected);
      } 
      Integer gettxnExpired = this.transactionRepository.gettxnExpired(details.getLcIssuanceCountry(), details
          .getLcCurrency());
      if (gettxnExpired == null) {
        countReport.setTrxn_Expired(Integer.valueOf(0));
      } else {
        countReport.setTrxn_Expired(gettxnExpired);
      } 
      Integer gettxnclosedTxn = this.transactionRepository.gettxnclosedTxn(details.getLcIssuanceCountry(), details
          .getLcCurrency());
      if (gettxnclosedTxn == null) {
        countReport.setTrxn_Closed(Integer.valueOf(0));
      } else {
        countReport.setTrxn_Closed(gettxnclosedTxn);
      } 
      countReport.setCountry1of_registration0(details.getLcIssuanceCountry());
      countReport.setCcy(details.getLcCurrency());
      country.add(countReport);
    } 
    return GenericExcelWriter.writeToExcel(filename, country);
  }
  
  public ByteArrayInputStream getPaymentSubscriptionReport(SearchRequest request, String filename) throws ParseException {
    List<Tuple> tuple;
    List<ReportPaymentAndSubscription> payReport = new ArrayList<>();
    DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
    java.util.Date fromDate = formatter.parse(request.getDateFrom());
    java.util.Date toDate = formatter.parse(request.getDateTo());
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      tuple = this.customerRepository.getPaymentSubUserIdReport(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), request.getUserId());
    } else {
      tuple = this.customerRepository.getPaymentSubReport(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    }
    System.out.println("$$$$$$$$$$$ "+ Arrays.toString(tuple.toArray()));
    for (Tuple report : tuple) {
      ReportPaymentAndSubscription response = new ReportPaymentAndSubscription();
      response.setUser_ID(((String)report.get("userid") != null) ? (String)report.get("userid") : "");
      response.setUser_Type(
          ((String)report.get("subscriber_type") != null) ? (String)report.get("subscriber_type") : "");
      response.setOrganization(
          ((String)report.get("company_name") != null) ? (String)report.get("company_name") : "");
      response.setMobile(
          ((String)report.get("mobile_number") != null) ? (String)report.get("mobile_number") : "");
      response.setLandline(((String)report.get("landline") != null) ? (String)report.get("landline") : "");
      response.setCountry(((String)report.get("country_name") != null) ? (String)report.get("country_name") : "");
      response.setEmail(((String)report.get("email_address") != null) ? (String)report.get("email_address") : "");
      response.setFirst_Name(((String)report.get("first_name") != null) ? (String)report.get("first_name") : "");
      response.setLast_Name(((String)report.get("last_name") != null) ? (String)report.get("last_name") : "");
      response.setPayment_ID(
          ((String)report.get("PAYMENT_TXN_ID") != null) ? (String)report.get("PAYMENT_TXN_ID") : "");
      response.setInvoice_number(
          ((String)report.get("INVOICE_NUMBER") != null) ? (String)report.get("INVOICE_NUMBER") : "");
      response.setPlan(
          ((String)report.get("subscription_name") != null) ? (String)report.get("subscription_name") : "");
      response.setSubscription_amount((Double.valueOf(((Integer)report.get("subscription_amount")).intValue()) != null) ? 
          Double.valueOf(((Integer)report.get("subscription_amount")).intValue()).doubleValue() : 0.0D);
      response.setSubscription_status(
          ((String)report.get("subscription_status") != null) ? (String)report.get("subscription_status") : "");
//      if (((Integer)report.get("is_vas_applied")).intValue() == 0) {
//        response.setVas("No");
//      } else if (((Integer)report.get("is_vas_applied")).intValue() == 1) {
//        response.setVas("Yes");
//      }

      if (report.get("VAS_PRICING") == null) {
        response.setVas_pricing(0.0D);
      } else {
        response.setVas_pricing((Double.valueOf(((Float)report.get("VAS_PRICING")).floatValue()) != null) ? 
            Double.valueOf(((Float)report.get("VAS_PRICING")).floatValue()).doubleValue() : 0.0D);
      } 
      response.setVas_status(
          ((String)report.get("vas_status") != null) ? (String)report.get("vas_status") : "");
     // response.setDiscount_id((report.get("DISCOUNT_ID") != null) ? report.get("DISCOUNT_ID").toString() : "0");
      response.setCoupon_Code((report.get("COUPON_CODE") != null) ? report.get("COUPON_CODE").toString() : "NA");
      response.setCoupon_Discount(
          ((Double)report.get("DISCOUNT") != null) ? ((Double)report.get("DISCOUNT")).doubleValue() : 0.0D);
//      if (report.get("DISCOUNT_ID") != "0" && report.get("DISCOUNT_ID") != null && 
//        !report.get("DISCOUNT_ID").equals(Integer.valueOf(0)))
        //response.setDiscount_Ccy("USD"); 
      //response.setCcy(((String)report.get("currency_code") != null) ? (String)report.get("currency_code") : "");
      response.setMode_of_Payment(
          ((String)report.get("mode_of_payment") != null) ? (String)report.get("mode_of_payment") : "");
      response.setDate_3_Time(
				(java.util.Date) report.get("inserted_date") != null ? (java.util.Date) report.get("inserted_date")
						: date);

      response.setPlan_activation_Date(
          ((Date)report.get("splan_start_date") != null) ? (Date)report.get("splan_start_date") : this.date);
      response.setPlan_expiry_Date(
          ((Date)report.get("splan_end_date") != null) ? (Date)report.get("splan_end_date") : this.date);
      
      if(report.get("NET_FEE")==null) {
    	  response.setNet_fee_IN_USD(0.0D);
      }else {
    	  response.setNet_fee_IN_USD((Double.valueOf(((Double)report.get("NET_FEE")).doubleValue()) != null) ? 
    	          Double.valueOf(((Double)report.get("NET_FEE")).doubleValue()).doubleValue() : 0.0D);
    	        
      }
      
      response.setMaker_Comment(
              (report.get("maker_comment") != null) ? (String)report.get("maker_comment") : "");
      response.setChecker_Comment(
              (report.get("checker_comment") != null) ? (String)report.get("checker_comment") : "");
      response.setPayment_Status(
              (report.get("PAYMENT_STATUS")!=null)?report.get("PAYMENT_STATUS").toString():"");
      
      
      payReport.add(response);
    } 
    return GenericExcelWriter.writeToExcel(filename, payReport);
  }
  
  private List<ReportPaymentAndSubscription> processCUstomerDetailsPayment(List<NimaiSubscriptionDetails> customerList, SearchRequest request) {
    List<ReportPaymentAndSubscription> payReport = new ArrayList<>();
    for (NimaiSubscriptionDetails report : customerList) {
      System.out.println("Process ========================" + report.toString());
      ReportPaymentAndSubscription response = new ReportPaymentAndSubscription();
      if (report.getUserid() == null) {
        response.setUser_ID("");
        response.setUser_Type("");
        response.setOrganization("");
        response.setMobile("");
        response.setLandline("");
        response.setCountry("");
        response.setEmail("");
        response.setFirst_Name("");
        response.setLast_Name("");
      } else {
        response.setUser_ID(
            (report.getUserid().getUserid() != null) ? report.getUserid().getUserid() : "");
        response.setUser_Type((report.getUserid().getSubscriberType() != null) ? report
            .getUserid().getSubscriberType() : "");
        response.setOrganization((report.getUserid().getCompanyName() != null) ? report
            .getUserid().getCompanyName() : "");
        response.setMobile((report.getUserid().getMobileNumber() != null) ? report
            .getUserid().getMobileNumber() : "");
        response.setLandline(
            (report.getUserid().getLandline() != null) ? report.getUserid().getLandline() : "");
        response.setCountry((report.getUserid().getCountryName() != null) ? report
            .getUserid().getCountryName() : "");
        response.setEmail(
            (report.getUserid().getEmailAddress() != null) ? report.getUserid().getEmailAddress() : "");
        response.setFirst_Name(
            (report.getUserid().getFirstName() != null) ? report.getUserid().getFirstName() : "");
        response.setLast_Name(
            (report.getUserid().getLastName() != null) ? report.getUserid().getLastName() : "");
      } 
      response.setPlan(
          (report.getSubscriptionName() != null) ? report.getSubscriptionName() : "");
//      if (Integer.valueOf(report.getIsVasApplied()).intValue() == 0) {
//        response.setVas("No");
//      } else if (Integer.valueOf(report.getIsVasApplied()).intValue() == 1) {
//        response.setVas("Yes");
//      } 
      response.setCoupon_Code("");
      response.setCoupon_Discount(0.0D);
     // response.setDiscount_Ccy("");
      if (report.getSubscriptionAmount() == null) {
        response.setNet_fee_IN_USD(0.0D);
      } else {
        response.setNet_fee_IN_USD((Double.valueOf(report.getSubscriptionAmount().intValue()) != null) ? 
            Double.valueOf(report.getSubscriptionAmount().intValue()).doubleValue() : 0.0D);
      } 
      response.setDate_3_Time(
          (report.getInsertedDate() != null) ? (Date) report.getInsertedDate() : this.date);
      response.setPayment_ID("");
      response.setPlan_activation_Date(
          ((Date)report.getSplanStartDate() != null) ? (Date) report.getSplanStartDate() : this.date);
      response.setPlan_expiry_Date(
          ((Date)report.getSplanEndDate() != null) ? (Date) report.getSplanEndDate() : this.date);
      payReport.add(response);
    } 
    return payReport;
  }
  
  public ByteArrayInputStream getCustRmPerfReport(SearchRequest request, String filename) {
    List<Tuple> performanceList;
    List<ReportCustomerRmPerformance> custRm = new ArrayList<>();
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      performanceList = this.employeeRepository.getCustRmReportByEmpCode(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), request.getUserId());
    } else {
      performanceList = this.employeeRepository.getCustRmReport(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } 
    for (Tuple report : performanceList) {
      BigInteger b1 = BigInteger.valueOf(0L);
      BigDecimal b2 = BigDecimal.valueOf(0L);
      ReportCustomerRmPerformance response = new ReportCustomerRmPerformance();
      response.setCountry(((String)report.get("country") != null) ? (String)report.get("country") : "");
      response.setFirst_Name(
          ((String)report.get("emp_first_name") != null) ? (String)report.get("emp_first_name") : "");
      response.setLast_Name(
          ((String)report.get("emp_last_name") != null) ? (String)report.get("emp_last_name") : "");
      if (report.get("customer_accounts") != null) {
        BigInteger bi1 = (BigInteger)report.get("customer_accounts");
        int ccCount = bi1.intValue();
        response.setCustomer_accounts(Integer.valueOf(ccCount));
      } else {
        response.setCustomer_accounts(Integer.valueOf(0));
      } 
      if (report.get("trxn_count") != null) {
        BigInteger bi2 = (BigInteger)report.get("trxn_count");
        int ccAmtCount = bi2.intValue();
        response.setTrxn_Count(Integer.valueOf(ccAmtCount));
      } else {
        response.setTrxn_Count(Integer.valueOf(0));
      } 
      response.setCumulative_LC_Amount(
          Double.valueOf(((Double)report.get("cumulative_LC_Amount") != null) ? ((Double)report.get("cumulative_LC_Amount")).doubleValue() : 0.0D));
      if (report.get("trxn_Accepted") != null) {
        BigDecimal bd1 = (BigDecimal)report.get("trxn_Accepted");
        double acceptedCount = bd1.doubleValue();
        response.setTrxn_Accepted(Double.valueOf(acceptedCount));
      } else {
        response.setTrxn_Accepted(Double.valueOf(0.0D));
      } 
      if (report.get("trxn_Rejected") != null) {
        BigDecimal bd2 = (BigDecimal)report.get("trxn_Rejected");
        double rejectedCount = bd2.doubleValue();
        response.setTrxn_Rejected(Double.valueOf(rejectedCount));
      } else {
        response.setTrxn_Rejected(Double.valueOf(0.0D));
      } 
      if (report.get("trxn_Expired") != null) {
        BigDecimal bd3 = (BigDecimal)report.get("trxn_Expired");
        double expiredCount = bd3.doubleValue();
        response.setTrxn_Expired(Double.valueOf(expiredCount));
      } else {
        response.setTrxn_Expired(Double.valueOf(0.0D));
      } 
      custRm.add(response);
      System.out.println(response);
    } 
    System.out.println(custRm);
    return GenericExcelWriter.writeToExcel(filename, custRm);
  }
  
  public ByteArrayInputStream getBankRmPerfReport(SearchRequest request, String filename) {
    List<Tuple> performanceList;
    List<ReportBankRmPerformance> bankRm = new ArrayList<>();
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      performanceList = this.employeeRepository.getBankRmReportByEmpCode(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), request.getUserId());
    } else {
      performanceList = this.employeeRepository.getBankRmReport(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } 
    for (Tuple report : performanceList) {
      BigInteger b1 = BigInteger.valueOf(0L);
      BigDecimal b2 = BigDecimal.valueOf(0L);
      ReportBankRmPerformance response = new ReportBankRmPerformance();
      response.setCountry(((String)report.get("country") != null) ? (String)report.get("country") : "");
      response.setFirst_Name(
          ((String)report.get("emp_first_name") != null) ? (String)report.get("emp_first_name") : "");
      response.setLast_Name(
          ((String)report.get("emp_last_name") != null) ? (String)report.get("emp_last_name") : "");
      if (report.get("customer_accounts") != null) {
        BigInteger bi1 = (BigInteger)report.get("customer_accounts");
        int ccCount = bi1.intValue();
        response.setBank_As_Customer_accounts(Integer.valueOf(ccCount));
      } else {
        response.setBank_As_Customer_accounts(Integer.valueOf(0));
      } 
      if (report.get("trxn_count") != null) {
        BigInteger bi2 = (BigInteger)report.get("trxn_count");
        int ccAmtCount = bi2.intValue();
        response.setTrxn_Count(Integer.valueOf(ccAmtCount));
      } else {
        response.setTrxn_Count(Integer.valueOf(0));
      } 
      response.setCumulative_LC_Amount(
          Double.valueOf(((Double)report.get("cumulative_LC_Amount") != null) ? ((Double)report.get("cumulative_LC_Amount")).doubleValue() : 0.0D));
      if (report.get("trxn_Accepted") != null) {
        BigDecimal bd1 = (BigDecimal)report.get("trxn_Accepted");
        double acceptedCount = bd1.doubleValue();
        response.setTrxn_Accepted(Double.valueOf(acceptedCount));
      } else {
        response.setTrxn_Accepted(Double.valueOf(0.0D));
      } 
      if (report.get("trxn_Rejected") != null) {
        BigDecimal bd2 = (BigDecimal)report.get("trxn_Rejected");
        double rejectedCount = bd2.doubleValue();
        response.setTrxn_Rejected(Double.valueOf(rejectedCount));
      } else {
        response.setTrxn_Rejected(Double.valueOf(0.0D));
      } 
      if (report.get("trxn_Expired") != null) {
        BigDecimal bd3 = (BigDecimal)report.get("trxn_Expired");
        double expiredCount = bd3.doubleValue();
        response.setTrxn_Expired(Double.valueOf(expiredCount));
      } else {
        response.setTrxn_Expired(Double.valueOf(0.0D));
      } 
      bankRm.add(response);
      System.out.println(response);
    } 
    System.out.println(bankRm);
    return GenericExcelWriter.writeToExcel(filename, bankRm);
  }
  
  public ByteArrayInputStream getBankRmPerfUwReport(SearchRequest request, String filename) {
    List<Tuple> reports;
    List<ReportBankRmUwPerformance> uwPerfReport = new ArrayList<>();
    if (request.getUserId() != null && !request.getUserId().equals("")) {
      reports = this.employeeRepository.getBankRmUwReportbyEmpCode(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), request.getUserId());
    } else {
      reports = this.employeeRepository.getBankRmUwReport(request.getDateFrom(), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } 
    for (Tuple report : reports) {
      ReportBankRmUwPerformance response = new ReportBankRmUwPerformance();
      BigInteger b1 = BigInteger.valueOf(0L);
      BigDecimal b2 = BigDecimal.valueOf(0L);
      response.setCountry(((String)report.get("country") != null) ? (String)report.get("country") : "");
      response.setFirst_Name(
          ((String)report.get("emp_first_name") != null) ? (String)report.get("emp_first_name") : "");
      response.setLast_Name(
          ((String)report.get("emp_last_name") != null) ? (String)report.get("emp_last_name") : "");
      if (report.get("customer_accounts") != null) {
        BigInteger bi11 = (BigInteger)report.get("customer_accounts");
        int ccCount = bi11.intValue();
        response.setBank_Accounts(Integer.valueOf(ccCount));
      } else {
        response.setBank_Accounts(Integer.valueOf(0));
      } 
      if (report.get("trxn_count") != null) {
        BigInteger bi12 = (BigInteger)report.get("trxn_count");
        int ccAmtCount = bi12.intValue();
        response.setQuote_Count(Integer.valueOf(ccAmtCount));
      } else {
        response.setQuote_Count(Integer.valueOf(0));
      } 
      response.setCumulative_Quote_Amount(
          Double.valueOf(((Double)report.get("cumulative_LC_Amount") != null) ? ((Double)report.get("cumulative_LC_Amount")).doubleValue() : 0.0D));
      if (report.get("trxn_Accepted") != null) {
        BigDecimal bd1 = (BigDecimal)report.get("trxn_Accepted");
        double acceptedCount = bd1.doubleValue();
        response.setAccepted_Quote(Double.valueOf(acceptedCount));
      } else {
        response.setAccepted_Quote(Double.valueOf(0.0D));
      } 
      if (report.get("trxn_Rejected") != null) {
        BigDecimal bd2 = (BigDecimal)report.get("trxn_Rejected");
        double rejectedCount = bd2.doubleValue();
        response.setRejected_Quote(Double.valueOf(rejectedCount));
      } else {
        response.setRejected_Quote(Double.valueOf(0.0D));
      } 
      if (report.get("trxn_Expired") != null) {
        BigDecimal bd3 = (BigDecimal)report.get("trxn_Expired");
        double expiredCount = bd3.doubleValue();
        response.setExpired_Quote(Double.valueOf(expiredCount));
      } else {
        response.setExpired_Quote(Double.valueOf(0.0D));
      } 
      uwPerfReport.add(response);
    } 
    return GenericExcelWriter.writeToExcel(filename, uwPerfReport);
  }
  
  public ByteArrayInputStream getAllCustomerTransactionDetails(SearchRequest request, String filename) {
    List<ReportCustomerTransaction> custTransaction = new ArrayList<>();
    NimaiMCustomer custDetails = new NimaiMCustomer();
    List<Tuple> reports = this.customerRepository.getCustomerTransactionReportByDates(request.getDateFrom(), request
        .getDateTo());
    for (Tuple report : reports) {
      ReportCustomerTransaction response = new ReportCustomerTransaction();
      response.setUser_ID(((String)report.get("user_id") != null) ? (String)report.get("user_id") : "");
      try {
        custDetails = (NimaiMCustomer)this.customerRepository.getOne((String)report.get("user_id"));
        response.setUser_Type((custDetails.getSubscriberType() != null) ? custDetails.getSubscriberType() : "");
        response.setMobile((custDetails.getMobileNumber() != null) ? custDetails.getMobileNumber() : "");
        response.setEmail((custDetails.getEmailAddress() != null) ? custDetails.getEmailAddress() : "");
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Unfind userId===:" + (String)report.get("user_id"));
        continue;
      } 
      
//	  response.setDate_3_Time( ((Date)report.get("inserted_date") != null) ?
//		  (Date)report.get("inserted_date") : this.date);

      response.setTrxn_Placed_Date_Time(report.get("inserted_date").toString());
      
      response.setTrxn_ID(
          ((String)report.get("transaction_id") != null) ? (String)report.get("transaction_id") : "");
      response.setApplicant(
          ((String)report.get("applicant_name") != null) ? (String)report.get("applicant_name") : "");
      response.setApplicant_country(
          ((String)report.get("applicant_country") != null) ? (String)report.get("applicant_country") : "");
      response.setApplicant_Contact(((String)report.get("applicant_contact_person") != null) ? (String)report
          .get("applicant_contact_person") : "");
      response.setApplicant_Email_ID(((String)report.get("applicant_contact_person_email") != null) ? (String)report
          .get("applicant_contact_person_email") : "");
      response.setBeneficiary(((String)report.get("bene_name") != null) ? (String)report.get("bene_name") : "");
      response.setBeneficiary_Country(
          ((String)report.get("bene_country") != null) ? (String)report.get("bene_country") : "");
      response.setClient_Contact(
          ((String)report.get("bene_contact_person") != null) ? (String)report.get("bene_contact_person") : "");
      response.setClient_Email_ID(((String)report.get("bene_contact_person_email") != null) ? (String)report
          .get("bene_contact_person_email") : "");
      
      
//    response.setBank_Country(
//          ((String)report.get("bene_bank_country") != null) ? (String)report.get("bene_bank_country") : "");
//Prateek// 
      
      response.setBeneficiary_Bank_Country(
          ((String)report.get("bene_bank_country") != null) ? (String)report.get("bene_bank_country") : "");
//      response.setBank_swift_Code(
//Prateek          ((String)report.get("bene_swift_code") != null) ? (String)report.get("bene_swift_code") : "");
      
      response.setBeneficiary_Bank_Swift_Code(
              ((String)report.get("bene_swift_code") != null) ? (String)report.get("bene_swift_code") : "");
      
//      response.setBank_Name(
// Prateek        ((String)report.get("bene_bank_name") != null) ? (String)report.get("bene_bank_name") : "");
      
      response.setBeneficiary_Bank_Name(
              ((String)report.get("bene_bank_name") != null) ? (String)report.get("bene_bank_name") : "");
      
//      response.setiB(
//Prateek          ((String)report.get("lc_issuance_bank") != null) ? (String)report.get("lc_issuance_bank") : "");
      
      response.setIssuing_Bank(
              ((String)report.get("lc_issuance_bank") != null) ? (String)report.get("lc_issuance_bank") : "");
      
//      response.setBranch(
//Prateek         ((String)report.get("lc_issuance_branch") != null) ? (String)report.get("lc_issuance_branch") : "");
      
        response.setIssuing_Bank_Branch(
              ((String)report.get("lc_issuance_branch") != null) ? (String)report.get("lc_issuance_branch") : "");
      
//Prateek      response.setSwift_Code(((String)report.get("swift_code") != null) ? (String)report.get("swift_code") : "");
      
       response.setIssuing_Bank_Swift_Code(((String)report.get("swift_code") != null) ? (String)report.get("swift_code") : "");      
      
//      response.setCountry(
//Prateek          ((String)report.get("lc_issuance_country") != null) ? (String)report.get("lc_issuance_country") : "");
      
        response.setIssuing_Bank_Country(
                   ((String)report.get("lc_issuance_country") != null) ? (String)report.get("lc_issuance_country") : "");
//      response.setRequirement(
//              ((String)report.get("requirement_type") != null) ? (String)report.get("requirement_type") : "");

      if (report.get("requirement_type").equals("ConfirmAndDiscount")) {
        response.setRequirement("Confirmation and Discounting");
      } else if (report.get("requirement_type").equals("Banker")) {
        response.setRequirement("Banker's Acceptance");
      } else if (report.get("requirement_type").equals("Refinance")) {
        response.setRequirement("Refinancing");
      } else {
        response.setRequirement(
                (report.get("requirement_type") != null) ?
                        report.get("requirement_type").toString()
                                .replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2") : "");
      }

      response.setAmount(Double.valueOf(((Double)report.get("lc_value") != null) ? ((Double)report.get("lc_value")).doubleValue() : 0.0D));
      response.setCcy(((String)report.get("lc_currency") != null) ? (String)report.get("lc_currency") : "");
      
//      response.setIssuing_Date(((Date)report.get("lc_issuing_date") != null) ? (Date)report
//Prateek          .get("lc_issuing_date") : this.date);
      
      response.setIssuing_Date(report.get("lc_issuing_date").toString().substring(0, 10));
      
//      response.setLsd(((Date)report.get("last_shipment_date") != null) ? (Date)report
//Prateek          .get("last_shipment_date") : this.date);
      
//      response.setLsd(report.get("last_shipment_date").toString().substring(0, 10));
      
      response.setLsd((report.get("last_shipment_date") != null) ? report.get("last_shipment_date").toString().substring(0, 10) : "");
      
//      response.setNegotiation_Date(((Date)report.get("negotiation_date") != null) ? (Date)report
//Prateek          .get("negotiation_date") : this.date);
      
      response.setNegotiation_Date((report.get("negotiation_date") != null) ? report.get("negotiation_date").toString().substring(0, 10) : "");
      
      response.setGoods(((String)report.get("goods_type") != null) ? (String)report.get("goods_type") : "");
//      if (report.get("requirement_type").equals("Banker") || report
//        .get("requirement_type").equals("Discounting")) {
//        response.setUsance_Period(
//            ((String)report.get("discounting_period") != null) ? (String)report.get("discounting_period") : "");
//      } else if (report.get("requirement_type").equals("Refinance")) {
//        response.setUsance_Period(
//            ((String)report.get("refinancing_period") != null) ? (String)report.get("refinancing_period") : "");
//      } else {
//        response.setUsance_Period(
//            ((String)report.get("confirmation_period") != null) ? (String)report.get("confirmation_period") : "");
//      }
      Object requirement_type = report.get("requirement_type");
      if ("Confirmation".equals(requirement_type) ||
              "Confirmation and Discounting".equals(requirement_type) ||
              "Bank Guarantee".equals(requirement_type)) {
        response.setUsance_Period(report.get("confirmation_period") != null ?
                Integer.parseInt(report.get("confirmation_period").toString()) : 0);
      } else if ("Avalisation".equals(requirement_type) ||
              "Banker's Acceptance".equals(requirement_type) ||
              "Discounting".equals(requirement_type)) {
        response.setUsance_Period(report.get("discounting_period") != null ?
                Integer.parseInt(report.get("discounting_period").toString()) : 0);
      } else if ("Refinancing".equals(requirement_type)) {
        response.setUsance_Period(report.get("refinancing_period") != null ?
                Integer.parseInt(report.get("refinancing_period").toString()) : 0);
      }else {
        response.setUsance_Period(0);
      }
      response.setLc_Maturity_Date((report.get("lc_maturity_date") != null)
              ? report
          .get("lc_maturity_date").toString() : "");
//      response.setLc_Number(((String)report.get("lc_number") != null) ? (String)report.get("lc_number") : "");
//      response.setBen_trxn_bank_Name(
//          ((String)report.get("last_bene_bank") != null) ? (String)report.get("last_bene_bank") : "");
//      response.setBen_trxn_swiftCode(
//          ((String)report.get("last_bene_swift_code") != null) ? (String)report.get("last_bene_swift_code") : "");
//      response.setBen_trxn_Country(
//          ((String)report.get("last_bank_country") != null) ? (String)report.get("last_bank_country") : "");
      response.setPort_of_Loading(
          ((String)report.get("loading_port") != null) ? (String)report.get("loading_port") : "");
      
//      response.setPort_Country(
//Prateek          ((String)report.get("loading_country") != null) ? (String)report.get("loading_country") : "");
      
      response.setPort_of_Loading_Country(
              ((String)report.get("loading_country") != null) ? (String)report.get("loading_country") : "");
      
      response.setPort_of_Discharge(
          ((String)report.get("discharge_port") != null) ? (String)report.get("discharge_port") : "");
      
//      response.setDischarge_Country(
//Prateek          ((String)report.get("discharge_country") != null) ? (String)report.get("discharge_country") : "");
      
      response.setPort_of_Discharge_Country(
              ((String)report.get("discharge_country") != null) ? (String)report.get("discharge_country") : "");
      
      response.setCharges_are_on(
          ((String)report.get("charges_type") != null) ? (String)report.get("charges_type") : "");
      
//Prateek      response.setValidity((report.get("validity") != null) ? (String)report.get("validity") : "");
      
      response.setTransaction_Validity((report.get("validity") != null) ? (String)report.get("validity") : "");
      
//      response.setCount_Quotes_Recd(
//          Integer.valueOf(((Integer)report.get("quotation_received") != null) ? ((Integer)report.get("quotation_received")).intValue() : 0));
      
//      response.setQuote_Status(
//              ((String)report.get("quotation_accepted") != null) ? (String)report.get("quotation_accepted") : "");
      
      custTransaction.add(response);
    } 
    return GenericExcelWriter.writeToExcel(filename, custTransaction);
  }
  
  public ByteArrayInputStream getAllCustomerTransactionDetailsByUserId(SearchRequest request, String filename) throws ParseException {
    DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
    java.util.Date fromDate = formatter.parse(request.getDateFrom());
    java.util.Date toDate = formatter.parse(request.getDateTo());
    try {
      List<Tuple> cuTrDetails;
      if (request.getUserId() != null && !request.getUserId().equals("")) {
        cuTrDetails = this.transactionRepository.findByUsrIdDates(request.getUserId(), request.getDateFrom(), 
            Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
      } else {
        cuTrDetails = this.transactionRepository.findByDates(request.getDateFrom(), 
            Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
      } 
      NimaiMCustomer custDetails = new NimaiMCustomer();
      List<ReportCustomerTransaction> custTransaction = new ArrayList<>();
      for (Tuple report : cuTrDetails) {
        ReportCustomerTransaction response = new ReportCustomerTransaction();
        response.setUser_ID(((String)report.get("user_id") != null) ? (String)report.get("user_id") : "");
        try {
          custDetails = (NimaiMCustomer)this.customerRepository.getOne((String)report.get("user_id"));
          if (custDetails.getSubscriberType() != null)
            if (custDetails.getUserid().startsWith("BC")) {
              response.setUser_Type("BANK AS A CUSTOMER");
            } else {
              response.setUser_Type(custDetails.getSubscriberType());
            }  
          response.setLandline_no((custDetails.getLandline() != null) ? custDetails.getLandline() : "");
          response.setMobile((custDetails.getMobileNumber() != null) ? custDetails.getMobileNumber() : "");
          response.setEmail((custDetails.getEmailAddress() != null) ? custDetails.getEmailAddress() : "");
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Unfind userId===:" + (String)report.get("user_id"));
          continue;
        } 
//        response.setDate_3_Time(((java.util.Date)report.get("inserted_date") != null) ? (java.util.Date)report
 // Prateek           .get("inserted_date") : this.date);

//        response.setValue(report.get("lc_value") !=null ?
//                (Double)(report.get("lc_value")): 0.0D);

        String transactionId=report.get("transaction_id")!=null?
                report.get("transaction_id").toString():null;
        if(transactionId!=null){
          int quotationReceived = this.transactionRepository.getCountByTransactionId(transactionId);
          response.setQuotation_received(quotationReceived);
        }

//        response.setQuotation_received(report.get("quotation_received")!=null?
//                (Integer) (report.get("quotation_received")):0);
        if(report.get("transaction_status")==null) {
          response.setQuotation_accepted("");
        }else {
          if(report.get("transaction_status").toString().equalsIgnoreCase("Accepted"))
            response.setQuotation_accepted("Yes");
          else
            response.setQuotation_accepted("No");
        }
//        response.setQuotation_accepted(report.get("quotation_accepted")!=null?
//                (String) report.get("quotation_accepted"):"");
//        String quoteBankName=null;
//        if(report.get("transaction_id")!=null){
//          System.out.println("@@@@@@@@@@ "+report.get("transaction_id"));
//          quoteBankName = this.transactionRepository.findQuoteBankName(
//                  (String)report.get("transaction_id"));
//        }
//        response.setQuote_bank_name(quoteBankName!=null?quoteBankName:"");
        response.setTrxn_Placed_Date_Time(
                report.get("inserted_date").toString());
        
        response.setTrxn_ID(
            ((String)report.get("transaction_id") != null) ? (String)report.get("transaction_id") : "");
        response.setApplicant(
            ((String)report.get("applicant_name") != null) ? (String)report.get("applicant_name") : "");
        response.setApplicant_country(
            ((String)report.get("applicant_country") != null) ? (String)report.get("applicant_country") : "");
        response.setApplicant_Contact(((String)report.get("applicant_contact_person") != null) ? (String)report
            .get("applicant_contact_person") : "");
        response.setApplicant_Email_ID(((String)report.get("applicant_contact_person_email") != null) ? (String)report
            .get("applicant_contact_person_email") : "");
        response.setBeneficiary(
            ((String)report.get("bene_name") != null) ? (String)report.get("bene_name") : "");
        response.setBeneficiary_Country(
            ((String)report.get("bene_country") != null) ? (String)report.get("bene_country") : "");
        response.setClient_Contact(
            ((String)report.get("bene_contact_person") != null) ? (String)report.get("bene_contact_person") : "");
        response.setClient_Email_ID(((String)report.get("bene_contact_person_email") != null) ? (String)report
            .get("bene_contact_person_email") : "");
//        response.setBank_Country(
 // Prateek          ((String)report.get("bene_bank_country") != null) ? (String)report.get("bene_bank_country") : "");
        
        response.setBeneficiary_Bank_Country(
               ((String)report.get("bene_bank_country") != null) ? (String)report.get("bene_bank_country") : "");
        
//        response.setBank_swift_Code(
//Prateek            ((String)report.get("bene_swift_code") != null) ? (String)report.get("bene_swift_code") : "");
        
        response.setBeneficiary_Bank_Swift_Code(
                ((String)report.get("bene_swift_code") != null) ? (String)report.get("bene_swift_code") : "");
        
//        response.setBank_Name(
//Prateek            ((String)report.get("bene_bank_name") != null) ? (String)report.get("bene_bank_name") : "");
        
        response.setBeneficiary_Bank_Name(
                    ((String)report.get("bene_bank_name") != null) ? (String)report.get("bene_bank_name") : "");
        
//        response.setiB(
//Prateek            ((String)report.get("lc_issuance_bank") != null) ? (String)report.get("lc_issuance_bank") : "");
        
        response.setIssuing_Bank(
                ((String)report.get("lc_issuance_bank") != null) ? (String)report.get("lc_issuance_bank") : "");
        
//        response.setBranch(
//Prateek            ((String)report.get("lc_issuance_branch") != null) ? (String)report.get("lc_issuance_branch") : "");
        
          response.setIssuing_Bank_Branch(
              ((String)report.get("lc_issuance_branch") != null) ? (String)report.get("lc_issuance_branch") : "");
        
//        response.setSwift_Code(
//Prateek            ((String)report.get("swift_code") != null) ? (String)report.get("swift_code") : "");
        
        response.setIssuing_Bank_Swift_Code(
            ((String)report.get("swift_code") != null) ? (String)report.get("swift_code") : "");       
        
//        response.setCountry(
//Prateek            ((String)report.get("lc_issuance_country") != null) ? (String)report.get("lc_issuance_country") : "");
        
          response.setIssuing_Bank_Country(
                     ((String)report.get("lc_issuance_country") != null) ? (String)report.get("lc_issuance_country") : "");       

//        response.setRequirement(
//                ((String)report.get("requirement_type") != null) ? (String)report.get("requirement_type") : "");
        if (report.get("requirement_type").equals("ConfirmAndDiscount")) {
          response.setRequirement("Confirmation and Discounting");
        } else if (report.get("requirement_type").equals("Banker")) {
          response.setRequirement("Banker's Acceptance");
        }else if (report.get("requirement_type").equals("BillAvalisation")) {
          response.setRequirement("Avalisation");
        } else if (report.get("requirement_type").equals("Refinance")) {
          response.setRequirement("Refinancing");
          response.setLast_bene_bank_country(report.get("last_bank_country")!= null?
                  (String) report.get("last_bank_country"):"");
          response.setLc_Maturity_Date((report.get("lc_maturity_date") != null)
                  ?report
                  .get("lc_maturity_date").toString().split(" ")[0] : "");
        } else {
          response.setRequirement(
                  (report.get("requirement_type") != null) ?
                          report.get("requirement_type").toString()
                                  .replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2") : "");
        }
        response.setAmount(((Double) report.get("lc_value") != null)
                && (!Objects.equals(report.get("lc_value").toString(),""))
                ? (Double) report.get("lc_value") : 0.0D);
        response.setCcy(((String)report.get("lc_currency") != null) ? (String)report.get("lc_currency") : "");
        
//        response.setIssuing_Date(((java.util.Date)report.get("lc_issuing_date") != null) ? (java.util.Date)report
//Prateek            .get("lc_issuing_date") : this.date);
        
        response.setIssuing_Date((report.get("lc_issuing_date")!=null)?
                report.get("lc_issuing_date").toString().substring(0, 10):"");
        
//        response.setLsd(((java.util.Date)report.get("last_shipment_date") != null) ? (java.util.Date)report
//Prateek            .get("last_shipment_date") : this.date);
        
//        response.setLsd(report.get("last_shipment_date").toString().substring(0, 10));
        response.setLsd((report.get("last_shipment_date") != null) ?
                report.get("last_shipment_date").toString().substring(0, 10) : "");
        
//        response.setNegotiation_Date(((java.util.Date)report.get("negotiation_date") != null) ? (java.util.Date)report
//Prateek            .get("negotiation_date") : this.date);
        
        response.setNegotiation_Date((report.get("negotiation_date") != null) ? report.get("negotiation_date").toString().substring(0, 10) : "");
        
        response.setGoods(((String)report.get("goods_type") != null) ? (String)report.get("goods_type") : "");
        Object requirement_type = report.get("requirement_type");
        if ("Confirmation".equals(requirement_type) ||
                "ConfirmAndDiscount".equals(requirement_type) ||
                "BankGuarantee".equals(requirement_type)) {
//          System.out.println(">>>>>>>> "+report.get("confirmation_period"));
          if(report.get("confirmation_period") == null)
            response.setUsance_Period(0);
          else {
            if(report.get("confirmation_period").toString().equalsIgnoreCase(""))
              response.setUsance_Period(0);
            else
              response.setUsance_Period(Integer.parseInt(report.get("confirmation_period").toString()));
          }
         // response.setUsance_Period(report.get("confirmation_period") != null ?
           //       Integer.parseInt(report.get("confirmation_period").toString()) : 0);
        } else if ("BillAvalisation".equals(requirement_type) ||
                "Banker".equals(requirement_type) ||
                "Discounting".equals(requirement_type)) {
          if(report.get("discounting_period") == null)
            response.setUsance_Period(0);
          else {
            if(report.get("discounting_period").toString().equalsIgnoreCase(""))
              response.setUsance_Period(0);
            else
              response.setUsance_Period(Integer.parseInt(report.get("discounting_period").toString()));
          }
//          response.setUsance_Period(report.get("discounting_period") != null ?
//                  Integer.parseInt(report.get("discounting_period").toString()) : 0);
        } else if ("Refinance".equals(requirement_type)) {
          if(report.get("refinancing_period") == null)
            response.setUsance_Period(0);
          else {
            if(report.get("refinancing_period").toString().equalsIgnoreCase(""))
              response.setUsance_Period(0);
            else
              response.setUsance_Period(Integer.parseInt(report.get("refinancing_period").toString()));
          }
//          response.setUsance_Period(report.get("refinancing_period") != null ?
//                  Integer.parseInt(report.get("refinancing_period").toString()) : 0);
        }else {
          response.setUsance_Period(0);
        }
        System.out.println("&&&&&&&&&&&&&&& "+report.get("requirement_type"));
        System.out.println("%%%%%%%%%%%%% "+response.getUsance_Period());
//        if (report.get("requirement_type")==null) {
//                response.setUsance_Period("");
//              }
//        else if (report.get("requirement_type").equals("Banker") || report
//          .get("requirement_type").equals("Discounting")) {
//          response.setUsance_Period(((String)report.get("discounting_period") != null) ? (String)report
//              .get("discounting_period") : "");
//        } else if (report.get("requirement_type").equals("Refinance")) {
//          response.setUsance_Period(((String)report.get("refinancing_period") != null) ? (String)report
//              .get("refinancing_period") : "");
//        } else {
//          response.setUsance_Period(((String)report.get("confirmation_period") != null) ? (String)report
//              .get("confirmation_period") : "");
//        }
        response.setPayment_Terms_With_Seller((report.get("original_tenor_days") != null)
                && (!Objects.equals(report.get("original_tenor_days").toString(),""))
                ? (Integer) report.get("original_tenor_days") : 0);

        System.out.println("##### "+response.getTrxn_ID());
        System.out.println(">>>>>>>>> "+(report.get("lc_maturity_date")));
        System.out.println("<<<<<<<<<<<< "+response.getLc_Maturity_Date());
//        response.setLc_Number(((String)report.get("lc_number") != null) ? (String)report.get("lc_number") : "");
//        response.setBen_trxn_bank_Name(
//            ((String)report.get("last_bene_bank") != null) ? (String)report.get("last_bene_bank") : "");
//        response.setBen_trxn_swiftCode(((String)report.get("last_bene_swift_code") != null) ? (String)report
//            .get("last_bene_swift_code") : "");
//        response.setBen_trxn_Country(
//            ((String)report.get("last_bank_country") != null) ? (String)report.get("last_bank_country") : "");
        response.setPort_of_Loading(
            ((String)report.get("loading_port") != null) ? (String)report.get("loading_port") : "");
        
//        response.setPort_Country(
//Prateek            ((String)report.get("loading_country") != null) ? (String)report.get("loading_country") : "");
        
        response.setPort_of_Loading_Country(
                ((String)report.get("loading_country") != null) ? (String)report.get("loading_country") : "");
        
        response.setPort_of_Discharge(
            ((String)report.get("discharge_port") != null) ? (String)report.get("discharge_port") : "");
        
//        response.setDischarge_Country(
//Prateek            ((String)report.get("discharge_country") != null) ? (String)report.get("discharge_country") : "");
        
        response.setPort_of_Discharge_Country(
                ((String)report.get("discharge_country") != null) ? (String)report.get("discharge_country") : "");
        
        response.setCharges_are_on(
            ((String)report.get("charges_type") != null) ? (String)report.get("charges_type") : "");
        
//Prateek        response.setValidity((report.get("validity") != null) ? (String)report.get("validity") : "");
        
        response.setTransaction_Validity((report.get("validity") != null) ? (String)report.get("validity") : "");
//        response.setUsd_currency_value((report.get("usd_currency_value") !=null)? report.get("usd_currency_value").toString()  :"");
        response.setUsd_currency_value((report.get("usd_currency_value")!=null)
                && (!Objects.equals(report.get("usd_currency_value").toString(),""))?
                (Double) report.get("usd_currency_value") :0.0D);
        response.setTransaction_status((report.get("transaction_status") != null) ?
                (String)report.get("transaction_status") : "");
//        response.setCount_Quotes_Recd(
//            Integer.valueOf(((Integer)report.get("quotation_received") != null) ? ((Integer)report.get("quotation_received")).intValue() : 0));
 
//Prateek (Added new column Quotes_Accepted)       

//        response.setQuote_Status(
//                ((String)report.get("quotation_accepted") != null) ? (String)report.get("quotation_accepted") : "");
        System.out.println("######################## "+response.getUsd_currency_value());
        custTransaction.add(response);
      } 
      return GenericExcelWriter.writeToExcel(filename, custTransaction);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public HashMap<String, Integer> calculateQuote(Integer quotationId, String transId, String tableType) {
    EntityManager entityManager = this.em.createEntityManager();
    try {
      StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("quote_calculation", new Class[] { NimaiLCMaster.class });
      storedProcedure.registerStoredProcedureParameter("inp_quotation_id", Integer.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("inp_transaction_id", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("inp_table_type", String.class, ParameterMode.IN);
      storedProcedure.registerStoredProcedureParameter("negoDays", Integer.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("expDays", Integer.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("matDays", Integer.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("expClaimDays", Integer.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("confChgsNegot", Float.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("confChgsMatur", Float.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("confChgsExp", Float.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("confChgsClaimExp", Float.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("sumOfQuote", Integer.class, ParameterMode.OUT);
      storedProcedure.registerStoredProcedureParameter("totalQuote", Integer.class, ParameterMode.OUT);
      storedProcedure.setParameter("inp_quotation_id", quotationId);
      storedProcedure.setParameter("inp_transaction_id", transId);
      storedProcedure.setParameter("inp_table_type", tableType);
      storedProcedure.execute();
      int negoDays = ((Integer)storedProcedure.getOutputParameterValue("negoDays")).intValue();
      int expDays = ((Integer)storedProcedure.getOutputParameterValue("expDays")).intValue();
      int matDays = ((Integer)storedProcedure.getOutputParameterValue("matDays")).intValue();
      int expClaimDays = ((Integer)storedProcedure.getOutputParameterValue("expClaimDays")).intValue();
      float confChgsNegot = ((Float)storedProcedure.getOutputParameterValue("confChgsNegot")).floatValue();
      float confChgsMatur = ((Float)storedProcedure.getOutputParameterValue("confChgsMatur")).floatValue();
      float confChgsExp = ((Float)storedProcedure.getOutputParameterValue("confChgsExp")).floatValue();
      float confChgsClaimExp = ((Float)storedProcedure.getOutputParameterValue("confChgsClaimExp")).floatValue();
      int sumOfQuote = ((Integer)storedProcedure.getOutputParameterValue("sumOfQuote")).intValue();
      int totalQuote = ((Integer)storedProcedure.getOutputParameterValue("totalQuote")).intValue();
      System.out.println(negoDays + " " + expDays + " " + matDays + " " + sumOfQuote + " " + totalQuote);
      HashMap<Object, Object> outputData = new HashMap<>();
      outputData.put("negotiationDays", Integer.valueOf(negoDays));
      outputData.put("expiryDays", Integer.valueOf(expDays));
      outputData.put("maturityDays", Integer.valueOf(matDays));
      outputData.put("confChgsNegot", Float.valueOf(confChgsNegot));
      outputData.put("confChgsMatur", Float.valueOf(confChgsMatur));
      outputData.put("sumOfQuote", Integer.valueOf(sumOfQuote));
      outputData.put("TotalQuote", Integer.valueOf(totalQuote));
      return (HashMap)outputData;
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      entityManager.close();
    } 
    return null;
  }
}
