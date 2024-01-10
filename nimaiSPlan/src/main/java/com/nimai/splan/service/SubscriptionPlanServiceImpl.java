package com.nimai.splan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimai.splan.model.*;
import com.nimai.splan.payload.*;
import com.nimai.splan.repository.*;
import com.nimai.splan.utility.AppConstants;
import com.nimai.splan.utility.Credentials;
import com.nimai.splan.utility.ErrorDescription;
import com.nimai.splan.utility.ModelMapper;
import com.nimai.splan.utility.SPlanUniqueNumber;
import com.nimai.splan.utility.SubscriptionPlanValidation;
import com.nimai.splan.utility.ThirdPartyApiIntegration;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Capture;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Money;
import com.paypal.orders.Name;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.OrdersGetRequest;
import com.paypal.orders.Payer;
import com.paypal.orders.PaymentMethod;
import com.paypal.orders.PurchaseUnit;
import com.paypal.orders.PurchaseUnitRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
  private static Logger logger = LoggerFactory.getLogger(SubscriptionPlanServiceImpl.class);

  private static final String randomString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  @Autowired
  SubscriptionPlanValidation validationDao;

  @Autowired
  NimaiMSPlanRepository subscriptionRepo;


	@Autowired
	ThirdPartyApiIntegration tPartApi;
  
  @Autowired
	NimaiSystemConfigRepository configRepo;
  
  @Autowired
  PostpaidSPlanRepository postpaidSPlanRepository;
  
  @Autowired
  NimaiPostpaidSubscriptionDetailsUpdRepo postpaidSPlanRepositoryUpd;

  @Autowired
  NimaiMCustomerRepository userRepository;

  @Autowired
  private NimaiAdvisoryService advService;

  @Autowired
  SubscriptionPlanRepository subscriptionDetailsRepository;

  @Autowired
  NimaiAdvisoryRepo advRepo;

  @Autowired
  NimaiEmailSchedulerRepository emailDetailsRepository;

  @Autowired
  NimaiCustomerGrandAmountRepo nimaiCustomerGrandAmtRepository;

  @Autowired
  OnlinePaymentRepo onlinePaymentRepo;

  @Autowired
  NimaiSubscriptionVasRepo nimaiSubscriptionVasRepo;

  @Autowired
  NimaiTransactionViewCountRepo viewCountRepo;
  
  @Autowired
  EntityManagerFactory em;

  @Autowired
  NimaiMSPlanRepository masterSPlanRepo;

  /*@Autowired
  postpaidSubscriptionBean PostpaidSubscriptionBean;*/

  @Value("${payment.ccavenue.workingkey}")
  private String paymentWorkingKey;

  @Value("${paypal.mode}")
  private String mode;

  @Value("${paypal.client.app}")
  private String clientId;

  @Value("${paypal.client.secret}")
  private String clientSecret;

  @Value("${configuredValue}")
  private String configuredValue;

  @Value("${fixedRate}")
  private String fixedRate;

  @Value("${payment.redirect.url}")
  private String redirectFromPaymentLink;

  String[] ccavenueParameterNames = new String[]{
          "merchant_id", "order_id", "currency", "amount", "redirect_url", "cancel_url", "language", "billing_name", "billing_address", "billing_city",
          "billing_state", "billing_zip", "billing_country", "billing_tel", "billing_email", "delivery_name", "delivery_address", "delivery_city", "delivery_state", "delivery_zip",
          "delivery_country", "delivery_tel", "merchant_param1", "merchant_param2", "merchant_param3", "merchant_param4", "merchant_param5", "promo_code", "customer_identifier"};

  Double minDue = 0.0;
  Double totalDue = 0.0;
  Double LcValue = 0.0;
  Double totalPayment = 0.0;
  Double perTransactionDue = 0.0;
  Double calPerTransactions = 0.0;
  int transactionCounter = 0;

  public ResponseEntity<?> saveUserSubscriptionPlan(SubscriptionBean subscriptionRequest, String userId) {
    GenericResponse response = new GenericResponse();
    String paymentTrId = "";
    logger.info(" ================ Send saveUserSubscriptionPlan method Invoked ================");
    try {
      if (subscriptionRequest.getSubscriptionId() != null) {
        Optional<NimaiMCustomer> mCustomer = this.userRepository.findByUserId(userId);
        if (mCustomer.isPresent()) {
          List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllByUserId(userId);
          if (!subscriptionEntity.isEmpty())
            for (NimaiSubscriptionDetails plan : subscriptionEntity) {
              plan.setStatus("Inactive");
              this.subscriptionDetailsRepository.save(plan);
            }
          NimaiSubscriptionDetails subScriptionDetails = new NimaiSubscriptionDetails();
          NimaiEmailScheduler schedularData = new NimaiEmailScheduler();
          subScriptionDetails.setSubscriptionName(subscriptionRequest.getSubscriptionName());
          subScriptionDetails.setUserid(mCustomer.get());
          subScriptionDetails.setSubscriptionValidity(subscriptionRequest.getSubscriptionValidity());
          subScriptionDetails.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          subScriptionDetails.setRemark(subscriptionRequest.getRemark());
          subScriptionDetails.setSubscriptionAmount(subscriptionRequest.getSubscriptionAmount());
          subScriptionDetails.setlCount(subscriptionRequest.getLcCount());
          subScriptionDetails.setSubsidiaries(subscriptionRequest.getSubsidiaries());
          subScriptionDetails.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          subScriptionDetails.setCustomerSupport(subscriptionRequest.getCustomerSupport());
          subScriptionDetails.setIsVasApplied(subscriptionRequest.getIsVasApplied());
          subScriptionDetails.setVasAmount(subscriptionRequest.getVasAmount());
          subScriptionDetails.setDiscountId(subscriptionRequest.getDiscountId());
          subScriptionDetails.setDiscount(subscriptionRequest.getDiscount());
          subScriptionDetails.setGrandAmount(subscriptionRequest.getGrandAmount());
          subScriptionDetails.setInsertedBy(((NimaiMCustomer) mCustomer.get()).getFirstName());
          subScriptionDetails.setsPLanCountry(((NimaiMCustomer) mCustomer.get()).getAddress3());
          subScriptionDetails.setInsertedDate(new Date());
          String customerType = subscriptionRequest.getSubscriptionId().substring(0, 2);
          if (customerType.equalsIgnoreCase("BA")) {
            subScriptionDetails.setCustomerType("Bank");
          } else {
            subScriptionDetails.setCustomerType("Customer");
          }
          SPlanUniqueNumber endDate = new SPlanUniqueNumber();
          int year = endDate.getNoOfyears(subScriptionDetails.getSubscriptionValidity());
          int month = endDate.getNoOfMonths(subScriptionDetails.getSubscriptionValidity());
          System.out.println(year);
          System.out.println(month);
          subScriptionDetails.setStatus("ACTIVE");
          Calendar cal = Calendar.getInstance();
          Date today = cal.getTime();
          cal.add(1, year);
          cal.add(2, month);
          Date sPlanEndDate = cal.getTime();
          subScriptionDetails.setSubscriptionStartDate(today);
          subScriptionDetails.setSubscriptionEndDate(sPlanEndDate);
          subScriptionDetails.setRenewalEmailStatus("Pending");
          if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
            subScriptionDetails.setPaymentMode("Wire");
            subScriptionDetails.setPaymentStatus("Pending");
          } else {
            subScriptionDetails.setPaymentMode("Credit");
            subScriptionDetails.setPaymentStatus("Approved");
          }
          NimaiSubscriptionDetails subScription = (NimaiSubscriptionDetails) this.subscriptionDetailsRepository.save(subScriptionDetails);
          if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
            this.userRepository.updatePaymentStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            String invoiceId = generatePaymentTtransactionID(10);
            paymentTrId = generatePaymentTtransactionID(15);
            this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
            this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                    .get()).getUserid());
            Double gstValue = Double.valueOf(this.subscriptionRepo.getGSTValue().doubleValue() / 100.0D);
            Double planPriceGST = Double.valueOf(subScription.getGrandAmount().doubleValue() + subScription.getGrandAmount().doubleValue() * gstValue.doubleValue());
            System.out.println("gstValue: " + gstValue);
            System.out.println("planPriceGST: " + planPriceGST);
            String finalPrice = String.format("%.2f", new Object[]{planPriceGST});
            this.subscriptionDetailsRepository.updatePaymentTxnIdForWire(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId, invoiceId, finalPrice);
          } else {
            this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                    .get()).getUserid());
            this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            this.userRepository.updatePaymentStatusForCredit(((NimaiMCustomer) mCustomer.get()).getUserid());
            OnlinePayment paymentDet = this.onlinePaymentRepo.getDetailsByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
            if (subscriptionRequest.getGrandAmount().doubleValue() == 0.0D) {
              String invoiceId = generatePaymentTtransactionID(10);
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
              this.subscriptionDetailsRepository.updateInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
            } else {
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getInvoiceId());
              this.subscriptionDetailsRepository.updatePaymentTxnIdInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getOrderId(), paymentDet.getInvoiceId());
            }
          }
          schedularData.setUserid(((NimaiMCustomer) mCustomer.get()).getUserid());
          String sPlanValidity = Integer.toString(subscriptionRequest.getSubscriptionValidity());
          String sPlanAmount = Integer.toString(subscriptionRequest.getSubscriptionAmount());
          schedularData.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          schedularData.setCustomerSupport(subscriptionRequest.getCustomerSupport());
          schedularData.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          schedularData.setSubscriptionAmount(sPlanAmount);
          if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("BA")) {
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(((NimaiMCustomer) mCustomer.get()).getEmailAddress());
          } else if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("CU") || subscriptionRequest
                  .getUserId().substring(0, 2).equalsIgnoreCase("BC")) {
            String emailId = "";
            if (subscriptionRequest.getEmailID() != null) {
              emailId = subscriptionRequest.getEmailID() + "," + ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            } else {
              emailId = ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            }
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(emailId);
          }
          schedularData.setSubscriptionStartDate(today);
          schedularData.setSubscriptionEndDate(sPlanEndDate);
          schedularData.setSubscriptionName(subscriptionRequest.getSubscriptionName());
          schedularData.setSubscriptionValidity(sPlanValidity);
          schedularData.setEmailStatus("pending");
          schedularData.setEvent("Cust_Splan_email");
          schedularData.setInsertedDate(today);
          NimaiEmailScheduler emailData = (NimaiEmailScheduler) this.emailDetailsRepository.save(schedularData);
          response.setErrCode("ASA001");
          response.setData(paymentTrId);
          response.setErrMessage(ErrorDescription.getDescription("ASA001"));
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setStatus("Failure");
        response.setErrCode("ASA003");
        response.setErrMessage(ErrorDescription.getDescription("ASA003"));
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
      response.setStatus("Failure");
      response.setErrCode("ASA009");
      response.setErrMessage(ErrorDescription.getDescription("ASA009"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }

  public ResponseEntity<?> renewSubscriptionPlan(SubscriptionBean subscriptionRequest, String userId,Integer lcUtilizedCount) {
    GenericResponse response = new GenericResponse();
    String paymentTrId = "";
    logger.info(" ================ renewSubscriptionPlan method Invoked ================");
    Calendar cal = Calendar.getInstance();
    Date today = cal.getTime();
    int addOnCredit = 0, utilzedLcCount = 0, days = 0;
    NimaiSubscriptionDetails inactiveSubscriptionEntity=new NimaiSubscriptionDetails();
    try {
      if (subscriptionRequest.getSubscriptionId() != null) {
        Optional<NimaiMCustomer> mCustomer = this.userRepository.findByUserId(userId);
        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
        if (mCustomer.isPresent()) {
          List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllByUserId(userId);
          if (!subscriptionEntity.isEmpty()) {
            for (NimaiSubscriptionDetails plan : subscriptionEntity) {
              if (plan.getSubsidiaryUtilizedCount() >
                      Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue())
                if (userId.substring(0, 2).equalsIgnoreCase("CU")) {
                  response.setStatus("Failure");
                  response.setErrMessage("You had already Active Subsidiary. Kindly select appropriate Plan.");
                  return new ResponseEntity(response, HttpStatus.OK);
                }
              if ((plan.getSubscriptionEndDate().after(today) || plan
                      .getSubscriptionEndDate().compareTo(today) <= 0) &&
                      Integer.valueOf(plan.getlCount()).intValue() - plan.getLcUtilizedCount() > 0) {
                if (plan.getSubscriptionEndDate().compareTo(today) <= 0) {
                  days = (int) ((plan.getSubscriptionEndDate().getTime() - today.getTime()) / 86400000L);
                } else {
                  days = (int) ((plan.getSubscriptionEndDate().getTime() - today.getTime()) / 86400000L) + 1;
                }
                if (!plan.getPaymentStatus().equalsIgnoreCase("Rejected")) {
                  addOnCredit = Integer.valueOf(plan.getlCount()).intValue() - plan.getLcUtilizedCount();
                } else {
                  addOnCredit = 0;
                }
                System.out.println("addOnCredit:" + addOnCredit);
              }
              plan.setStatus("Inactive");
              this.subscriptionDetailsRepository.save(plan);
              int utilizedCount=plan.getLcUtilizedCount();
              int obtainlcCount=0,derivedCount,finalUtilized;
              System.out.println("utilizedCount: "+utilizedCount);
              if(plan.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN"))
              {
            	  
            	  
            	  System.out.println("Renewing from postpaid....");
            	  obtainlcCount=Integer.valueOf(subscriptionRequest.getLcCount()).intValue();
            	  System.out.println("subscriptionRequest.getLcCount()).intValue(): "+obtainlcCount);
            	  derivedCount=Integer.valueOf(plan.getlCount())-utilizedCount;
            	  System.out.println("derivedCount: "+derivedCount);
            	  finalUtilized=obtainlcCount+(derivedCount);
            	  if(utilizedCount==0)
            	  {
            		  System.out.println("Previous utilized count is 0");
            		  utilzedLcCount=0;
            	  }
            	  else
            	  {
            		  System.out.println("Previous utilized count is not 0");
            		  utilzedLcCount=finalUtilized;
            	  }
            	  subscriptionRequest.setLcCount(""+obtainlcCount);
            	  //subscriptionRequest.
            	  System.out.println("finalUtilized: "+finalUtilized);
            	  System.out.println("utilzedLcCount: "+utilzedLcCount);
            	  System.out.println("subscriptionRequest.setLcCount(): "+subscriptionRequest.getLcCount());
              }
            }
          } else {
             inactiveSubscriptionEntity = this.subscriptionDetailsRepository.findOnlyLatestInactiveSubscriptionByUserId(userId);
            int noOfDays = (int) ((today.getTime() - inactiveSubscriptionEntity.getSubscriptionEndDate().getTime()) / 86400000L);
            System.out.println("Diff between exp and current date: " + noOfDays);
            if (inactiveSubscriptionEntity.getSubsidiaryUtilizedCount() >=
                    Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue() && userId.substring(0, 2).equalsIgnoreCase("CU")) {
              response.setStatus("Failure");
              response.setErrMessage("You had already Active Subsidiary. Kindly select appropriate Plan.");
              return new ResponseEntity(response, HttpStatus.OK);
            }
            if (noOfDays < 60 && Integer.valueOf(inactiveSubscriptionEntity.getlCount()).intValue() - inactiveSubscriptionEntity
                    .getLcUtilizedCount() > 0)
              if (!inactiveSubscriptionEntity.getPaymentStatus().equalsIgnoreCase("Rejected")) {
                addOnCredit = Integer.valueOf(inactiveSubscriptionEntity.getlCount()).intValue() - inactiveSubscriptionEntity.getLcUtilizedCount();
              } else {
                addOnCredit = 0;
              }
          }
          System.out.println("AddOnCredit: " + addOnCredit);
          System.out.println("UtilizedLcCount: " + utilzedLcCount);
          
          
          
          NimaiSubscriptionDetails subScriptionDetails = new NimaiSubscriptionDetails();
          NimaiEmailScheduler schedularData = new NimaiEmailScheduler();
          subScriptionDetails.setSubscriptionName(subscriptionRequest.getSubscriptionName());
          subScriptionDetails.setUserid(mCustomer.get());
          subScriptionDetails.setSubscriptionValidity(subscriptionRequest.getSubscriptionValidity());
          subScriptionDetails.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          subScriptionDetails.setRemark(subscriptionRequest.getRemark());
          subScriptionDetails.setSubscriptionAmount(subscriptionRequest.getSubscriptionAmount());
          subScriptionDetails
                  .setlCount(String.valueOf(Integer.valueOf(subscriptionRequest.getLcCount()).intValue() + addOnCredit));
        
          subScriptionDetails.setLcUtilizedCount(utilzedLcCount);
          
          subScriptionDetails.setSubsidiaries(subscriptionRequest.getSubsidiaries());
          subScriptionDetails.setIsVasApplied(subscriptionRequest.getIsVasApplied());
          subScriptionDetails.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          subScriptionDetails.setVasAmount(subscriptionRequest.getVasAmount());
          subScriptionDetails.setDiscountId(subscriptionRequest.getDiscountId());
          subScriptionDetails.setDiscount(subscriptionRequest.getDiscount());
          System.out.println("Grand Amount: " + subscriptionRequest.getGrandAmount());
          Double toBeTruncated = new Double(subscriptionRequest.getGrandAmount().doubleValue());
          Double truncatedDouble = Double.valueOf(BigDecimal.valueOf(toBeTruncated.doubleValue())
                  .setScale(2, RoundingMode.HALF_UP)
                  .doubleValue());
          subScriptionDetails.setGrandAmount(truncatedDouble);
          subScriptionDetails.setCustomerSupport(subscriptionRequest.getCustomerSupport());
          subScriptionDetails.setInsertedBy(((NimaiMCustomer) mCustomer.get()).getFirstName());
          subScriptionDetails.setsPLanCountry(((NimaiMCustomer) mCustomer.get()).getAddress3());
          subScriptionDetails.setInsertedDate(new Date());
          NimaiSubscriptionDetails inactiveSubscriptionEntity2 = this.subscriptionDetailsRepository.findOnlyLatestInactiveSubscriptionByUserId(userId);
          System.out.println("" + inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount());
          System.out.println("" + subscriptionRequest.getSubsidiaries());
          if (inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount() == Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue()) {
            subScriptionDetails.setSubsidiaryUtilizedCount(inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount());
          } else if (inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount() == 0) {
            subScriptionDetails.setSubsidiaryUtilizedCount(0);
          } else {
            subScriptionDetails.setSubsidiaryUtilizedCount(Integer.valueOf(inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount()).intValue());
          }
          String customerType = subscriptionRequest.getSubscriptionId().substring(0, 2);
          if (customerType.equalsIgnoreCase("BA")) {
            subScriptionDetails.setCustomerType("Bank");
          } else {
            subScriptionDetails.setCustomerType("Customer");
          }
          SPlanUniqueNumber endDate = new SPlanUniqueNumber();
          int year = endDate.getNoOfyears(subScriptionDetails.getSubscriptionValidity());
          int month = endDate.getNoOfMonths(subScriptionDetails.getSubscriptionValidity());
          System.out.println(year);
          System.out.println(month);
          subScriptionDetails.setStatus("ACTIVE");
          cal.add(5, days);
          cal.add(1, year);
          cal.add(2, month);
          Date sPlanEndDate = cal.getTime();
          subScriptionDetails.setSubscriptionStartDate(today);
          subScriptionDetails.setSubscriptionEndDate(sPlanEndDate);
          subScriptionDetails.setRenewalEmailStatus("Pending");
          System.out.println("Current Date: " + today);
          if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
            subScriptionDetails.setPaymentMode("Wire");
            subScriptionDetails.setPaymentStatus("Pending");
          } else {
            subScriptionDetails.setPaymentMode("Credit");
            subScriptionDetails.setPaymentStatus("Approved");
          }
          NimaiSubscriptionDetails subScription = (NimaiSubscriptionDetails) this.subscriptionDetailsRepository.save(subScriptionDetails);
          System.out.println("Grand Amount after save: " + subScription.getGrandAmount());
          if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
            this.advService.inactiveVASStatus(userId);
            this.userRepository.updatePaymentStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            //String invoiceId = generatePaymentTtransactionID(10);
            
            String invoiceId = generatePaymentTtransactionID(10);
            paymentTrId = generatePaymentTtransactionID(15);
            //paymentTrId = generatePaymentTtransactionID(15);
            
            this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
            this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                    .get()).getUserid());
            this.subscriptionDetailsRepository.updatePaymentTxnIdInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId, invoiceId);
            Double gstValue = Double.valueOf(this.subscriptionRepo.getGSTValue().doubleValue() / 100.0D);
            Double planPriceGST = Double.valueOf(subScription.getGrandAmount().doubleValue() + subScription.getGrandAmount().doubleValue() * gstValue.doubleValue());
            System.out.println("gstValue: " + gstValue);
            System.out.println("planPriceGST: " + planPriceGST);
            String finalPrice = String.format("%.2f", new Object[]{planPriceGST});
            this.subscriptionDetailsRepository.updatePaymentTxnIdForWire(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId, invoiceId, finalPrice);
            if (Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue() < Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue()) {
              utilzedLcCount = Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue() - Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue();
              this.subscriptionDetailsRepository.updateLCUtilzed(((NimaiMCustomer) mCustomer.get()).getUserid(), Integer.valueOf(utilzedLcCount));
            }
          } else {
            this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                    .get()).getUserid());
            this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            this.userRepository.updatePaymentStatusForCredit(((NimaiMCustomer) mCustomer.get()).getUserid());
            OnlinePayment paymentDet = this.onlinePaymentRepo.getDetailsByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
            if (subscriptionRequest.getGrandAmount().doubleValue() == 0.0D) {
              String invoiceId = generatePaymentTtransactionID(10);
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
              this.subscriptionDetailsRepository.updateInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
            } else {
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getInvoiceId());
              this.subscriptionDetailsRepository.updatePaymentTxnIdInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getOrderId(), paymentDet.getInvoiceId());
            }
            if (Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue() < Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue()) {
              utilzedLcCount = Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue() - Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue();
              this.subscriptionDetailsRepository.updateLCUtilzed(((NimaiMCustomer) mCustomer.get()).getUserid(), Integer.valueOf(utilzedLcCount));
            }
          }
          schedularData.setUserid(((NimaiMCustomer) mCustomer.get()).getUserid());
          String sPlanValidity = Integer.toString(subscriptionRequest.getSubscriptionValidity());
          String sPlanAmount = Integer.toString(subscriptionRequest.getSubscriptionAmount());
          schedularData.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          schedularData.setCustomerSupport(subscriptionRequest.getCustomerSupport());
          schedularData.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          schedularData.setSubscriptionAmount(sPlanAmount);
          if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("BA")) {
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(((NimaiMCustomer) mCustomer.get()).getEmailAddress());
          } else if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("CU") || subscriptionRequest
                  .getUserId().substring(0, 2).equalsIgnoreCase("BC")) {
            String emailId = "";
            if (subscriptionRequest.getEmailID() != null) {
              emailId = subscriptionRequest.getEmailID() + "," + ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            } else {
              emailId = ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            }
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(emailId);
          }
          schedularData.setSubscriptionEndDate(sPlanEndDate);
          schedularData.setSubscriptionStartDate(today);
          schedularData.setSubscriptionName(subscriptionRequest.getSubscriptionName());
          schedularData.setSubscriptionValidity(sPlanValidity);
          schedularData.setEmailStatus("pending");
          schedularData.setEvent("Cust_Splan_email");
          schedularData.setInsertedDate(today);
          NimaiEmailScheduler emailData = (NimaiEmailScheduler) this.emailDetailsRepository.save(schedularData);
          response.setErrCode("ASA001");
          response.setErrMessage("Subscription Plan Renewed Successfully.");
          response.setData(paymentTrId);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setStatus("Failure");
        response.setErrCode("ASA003");
        response.setErrMessage(ErrorDescription.getDescription("ASA003"));
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
      response.setStatus("Failure");
      response.setErrCode("ASA009");
      response.setErrMessage(ErrorDescription.getDescription("ASA009"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }

  public ResponseEntity<?> findSPlanDetailsByUserId(String userId) {
    logger.info(" ================ Send findSPlanDetailsByUserId method Invoked ================");
    GenericResponse response = new GenericResponse();
    try {
      List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllByUserId(userId);
      if (subscriptionEntity.isEmpty()) {
        response.setStatus("Failure");
        response.setErrCode("ASA002");
        response.setErrMessage(ErrorDescription.getDescription("ASA002"));
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
      List<SubscriptionPlanResponse> subscriptionBean = new ArrayList<>();
      for (NimaiSubscriptionDetails temp : subscriptionEntity) {
        SubscriptionPlanResponse responseBean = new SubscriptionPlanResponse();
        responseBean.setSubscriptionAmount(temp.getSubscriptionAmount());
        responseBean.setSubscriptionName(temp.getSubscriptionName());
        responseBean.setSubscriptionId(temp.getSubscriptionId());
        responseBean.setSubscriptionValidity(temp.getSubscriptionValidity());
        responseBean.setLcCount(temp.getlCount());
        responseBean.setRemark(temp.getRemark());
        responseBean.setUserId(temp.getUserid().getUserid());
        responseBean.setStatus(temp.getStatus());
        responseBean.setSubsidiaries(temp.getSubsidiaries());
        responseBean.setRelationshipManager(temp.getRelationshipManager());
        responseBean.setCustomerSupport(temp.getCustomerSupport());
        responseBean.setIsVasApplied(temp.getIsVasApplied());
        responseBean.setVasAmount(temp.getVasAmount());
        responseBean.setDiscount(temp.getDiscount());
        responseBean.setDiscountId(temp.getDiscountId());
        responseBean.setGrandAmount(temp.getGrandAmount());
        subscriptionBean.add(responseBean);
        response.setData(subscriptionBean);
      }
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }

  private Date getDate(String pattern) throws ParseException {
    return (new SimpleDateFormat(pattern)).parse((new SimpleDateFormat(pattern)).format(new Date()));
  }

  public ResponseEntity<?> getSPlanByUserId(String userId) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ getSPlanByUserId method Invoked ================");
    try {
      List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllByUserId(userId);
      if (subscriptionEntity.isEmpty()) {
        System.out.println("INside first if condtion");
        response.setStatus("Failure");
        response.setErrCode("ASA002");
        response.setErrMessage(ErrorDescription.getDescription("ASA002"));
        return new ResponseEntity(response, HttpStatus.OK);
      }
      System.out.println("INside second else condition");
      List<SubscriptionPlanResponse> subscriptionBean = new ArrayList<>();
      Iterator<NimaiSubscriptionDetails> iterator = subscriptionEntity.iterator();
      if (iterator.hasNext()) {
        NimaiSubscriptionDetails temp = iterator.next();
        System.out.println("INside for condition in loop");
        SubscriptionPlanResponse responseBean = new SubscriptionPlanResponse();
        System.out.println("temp status" + temp.getStatus());
        System.out.println("Inside for loop flag" + temp.getFlag());
        if (temp.getStatus().equalsIgnoreCase("ACTIVE") && temp.getFlag() == 0) {
          System.out.println("INside second if condition");
          responseBean.setSubscriptionAmount(temp.getSubscriptionAmount());
          responseBean.setSubscriptionName(temp.getSubscriptionName());
          responseBean.setSubscriptionId(temp.getSubscriptionId());
          responseBean.setSubscriptionValidity(temp.getSubscriptionValidity());
          responseBean.setLcCount(temp.getlCount());
          responseBean.setRemark(temp.getRemark());
          responseBean.setUserId(temp.getUserid().getUserid());
          responseBean.setStatus(temp.getStatus());
          responseBean.setSubsidiaries(temp.getSubsidiaries());
          responseBean.setRelationshipManager(temp.getRelationshipManager());
          responseBean.setCustomerSupport(temp.getCustomerSupport());
          responseBean.setIsVasApplied(temp.getIsVasApplied());
          responseBean.setVasAmount(temp.getVasAmount());
          responseBean.setDiscountId(temp.getDiscountId());
          responseBean.setDiscount(temp.getDiscount());
          responseBean.setGrandAmount(temp.getGrandAmount());
          responseBean.setSubsStartDate(temp.getInsertedDate());
          responseBean.setInvoiceId(temp.getInvoiceId());
          responseBean.setPaymentStatus(temp.getPaymentStatus());
          subscriptionBean.add(responseBean);
          response.setData(subscriptionBean);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        System.out.println("inside second else condtion");
        response.setStatus("Failure");
        response.setErrCode("ASA008");
        response.setErrMessage(ErrorDescription.getDescription("ASA008"));
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  public ResponseEntity<?> getInactiveSPlanByUserId(String userId) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ getInactiveSPlanByUserId method Invoked ================");
    try {
      List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllInactivePlanByUserId(userId);
      if (subscriptionEntity.isEmpty()) {
        response.setStatus("Failure");
        response.setErrCode("ASA002");
        response.setErrMessage(ErrorDescription.getDescription("ASA002"));
        return new ResponseEntity(response, HttpStatus.OK);
      }
      List<SubscriptionPlanResponse> subscriptionBean = new ArrayList<>();
      Iterator<NimaiSubscriptionDetails> iterator = subscriptionEntity.iterator();
      if (iterator.hasNext()) {
        NimaiSubscriptionDetails temp = iterator.next();
        SubscriptionPlanResponse responseBean = new SubscriptionPlanResponse();
        if (temp.getStatus().equalsIgnoreCase("INACTIVE") && temp.getFlag() == 0) {
          responseBean.setSubscriptionAmount(temp.getSubscriptionAmount());
          responseBean.setSubscriptionName(temp.getSubscriptionName());
          responseBean.setSubscriptionId(temp.getSubscriptionId());
          responseBean.setSubscriptionValidity(temp.getSubscriptionValidity());
          responseBean.setLcCount(temp.getlCount());
          responseBean.setRemark(temp.getRemark());
          responseBean.setUserId(temp.getUserid().getUserid());
          responseBean.setStatus(temp.getStatus());
          responseBean.setSubsidiaries(temp.getSubsidiaries());
          responseBean.setRelationshipManager(temp.getRelationshipManager());
          responseBean.setCustomerSupport(temp.getCustomerSupport());
          responseBean.setIsVasApplied(temp.getIsVasApplied());
          responseBean.setVasAmount(temp.getVasAmount());
          responseBean.setDiscountId(temp.getDiscountId());
          responseBean.setDiscount(temp.getDiscount());
          responseBean.setGrandAmount(temp.getGrandAmount());
          responseBean.setSubsStartDate(temp.getInsertedDate());
          responseBean.setInvoiceId(temp.getInvoiceId());
          subscriptionBean.add(responseBean);
          response.setData(subscriptionBean);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setStatus("Failure");
        response.setErrCode("ASA008");
        response.setErrMessage(ErrorDescription.getDescription("ASA008"));
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  public ResponseEntity<?> findMSPlanDetails(String userId) {
    GenericResponse response = new GenericResponse();
    logger.info("======findMSplanDetails method invoked===========");
    try {
      Optional<NimaiMCustomer> user = this.userRepository.findById(userId);
      if (user.isPresent()) {
        List<NimaiSubscriptionDetails> sPlanEntity = this.subscriptionDetailsRepository.findAllByUserId(userId);
        if (!sPlanEntity.isEmpty()) {
          for (NimaiSubscriptionDetails temp : sPlanEntity) {
            if (temp.getStatus().equalsIgnoreCase("Active")) {
              SubscriptionPlanResponse responseBean = new SubscriptionPlanResponse();
              responseBean.setSubscriptionAmount(temp.getSubscriptionAmount());
              responseBean.setSubscriptionName(temp.getSubscriptionName());
              responseBean.setSubscriptionId(temp.getSubscriptionId());
              responseBean.setSubscriptionValidity(temp.getSubscriptionValidity());
              responseBean.setLcCount(temp.getlCount());
              responseBean.setRemark(temp.getRemark());
              responseBean.setUserId(temp.getUserid().getUserid());
              responseBean.setStatus(temp.getStatus());
              responseBean.setSubsidiaries(temp.getSubsidiaries());
              responseBean.setRelationshipManager(temp.getRelationshipManager());
              responseBean.setCustomerSupport(temp.getCustomerSupport());
              responseBean.setIsVasApplied(temp.getIsVasApplied());
              responseBean.setVasAmount(temp.getVasAmount());
              responseBean.setDiscountId(temp.getDiscountId());
              responseBean.setDiscount(temp.getDiscount());
              responseBean.setGrandAmount(temp.getGrandAmount());
              response.setData(responseBean);
              continue;
            }
            response.setErrMessage("SubscriptionPlan is not Activated on this userId");
          }
        } else {
          List<SubscriptionPlanResponse> subscriptionBean = new ArrayList<>();
          List<NimaiMSubscription> subscriptionEntity = this.masterSPlanRepo.findAll();
          for (NimaiMSubscription mSPLan : subscriptionEntity) {
            SubscriptionPlanResponse responseBean = new SubscriptionPlanResponse();
            responseBean.setSubscriptionAmount(mSPLan.getSubscriptionAmount());
            responseBean.setSubscriptionName(mSPLan.getSubscriptionName());
            responseBean.setSubscriptionId(mSPLan.getSubscriptionId());
            responseBean.setSubscriptionValidity(mSPLan.getSubscriptionValidity());
            responseBean.setLcCount(mSPLan.getlCount());
            responseBean.setRemark(mSPLan.getRemark());
            responseBean.setStatus(mSPLan.getStatus());
            responseBean.setSubsidiaries(mSPLan.getSubsidiaries());
            responseBean.setRelationshipManager(mSPLan.getRelationshipManager());
            responseBean.setCustomerSupport(mSPLan.getCustomerSupport());
            subscriptionBean.add(responseBean);
            response.setData(subscriptionBean);
          }
        }
      } else {
        response.setErrMessage("Invalid UserId");
      }
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
    }
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  public SPlanResponseBean sPlanMasterlist(NimaiMCustomer user) {
    logger.info("======SPlanResponseBean method invoked===========");
    SPlanResponseBean sPlanResponseBean = new SPlanResponseBean();
    String customerType = user.getUserid().substring(0, 2);
    logger.info("CountryName:" + user.getCountryName());
    if (customerType.equalsIgnoreCase("Cu")) {
      String str = "Customer";
      List<NimaiMSubscription> custSPlanList = this.masterSPlanRepo.findByCountry(str, user.getCountryName());
      System.out.println(custSPlanList.toString());
      if (!custSPlanList.isEmpty()) {
        List<customerSPlansResponse> custSubscriptionBean = ModelMapper.mapCustSplanListToSBeanRsponse(custSPlanList);
        sPlanResponseBean.setCustomerSplans(custSubscriptionBean);
        return sPlanResponseBean;
      }
      return null;
    }
    String custType = "Bank";
    List<NimaiMSubscription> banksSPlanList = this.masterSPlanRepo.findByCountry(custType, user.getCountryName());
    if (!banksSPlanList.isEmpty()) {
      List<banksSplansReponse> banksubscriptionBean = ModelMapper.mapBankSplanListToSBeanRsponse(banksSPlanList);
      sPlanResponseBean.setBanksSplans(banksubscriptionBean);
      return sPlanResponseBean;
    }
    return null;
  }

  public ResponseEntity<?> findCustomerSPlanDetails(SplanRequest sPlanRequest) {
    GenericResponse response = new GenericResponse();
    logger.info("======findCustomerSPlanDetails method invoked===========");
    try {
      Optional<NimaiMCustomer> user = this.userRepository.findById(sPlanRequest.getUserId());
      SPlanResponseBean sPlanResponseBean = new SPlanResponseBean();
      if (user.isPresent()) {
        String customerType = ((NimaiMCustomer) user.get()).getUserid().substring(0, 2);
        logger.info("CountryName:" + ((NimaiMCustomer) user.get()).getRegistredCountry());
        if (customerType.equalsIgnoreCase("CU")) {
          List<NimaiMSubscription> custSPlanList = this.masterSPlanRepo.findByCountry("Customer", ((NimaiMCustomer) user
                  .get()).getRegistredCountry());
          System.out.println(custSPlanList.toString());
          if (!custSPlanList.isEmpty()) {
            List<customerSPlansResponse> custSubscriptionBean = ModelMapper.mapCustSplanListToSBeanRsponse(custSPlanList);
            sPlanResponseBean.setCustomerSplans(custSubscriptionBean);
          } else {
            sPlanResponseBean = null;
          }
        } else if (customerType.equalsIgnoreCase("BC")) {
          List<NimaiMSubscription> custSPlanList = this.masterSPlanRepo.findByCountry("Bank As Customer", ((NimaiMCustomer) user
                  .get()).getRegistredCountry());
          System.out.println(custSPlanList.toString());
          if (!custSPlanList.isEmpty()) {
            List<customerSPlansResponse> custSubscriptionBean = ModelMapper.mapCustSplanListToSBeanRsponse(custSPlanList);
            sPlanResponseBean.setCustomerSplans(custSubscriptionBean);
          } else {
            sPlanResponseBean = null;
          }
        } else {
          List<NimaiMSubscription> banksSPlanList = this.masterSPlanRepo.findByCountry("Bank", ((NimaiMCustomer) user
                  .get()).getRegistredCountry());
          if (!banksSPlanList.isEmpty()) {
            List<banksSplansReponse> banksubscriptionBean = ModelMapper.mapBankSplanListToSBeanRsponse(banksSPlanList);
            sPlanResponseBean.setBanksSplans(banksubscriptionBean);
          } else {
            sPlanResponseBean = null;
          }
        }
        if (sPlanResponseBean != null) {
          response.setData(sPlanResponseBean);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setErrCode("ASA012");
        response.setErrMessage(ErrorDescription.getDescription("ASA012"));
        return new ResponseEntity(response, HttpStatus.OK);
      }
      response.setErrCode("ASA003");
      response.setErrMessage(ErrorDescription.getDescription("ASA003"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      response.setErrMessage("No entity Found");
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }

  public static String generatePaymentTtransactionID(int count) {
    StringBuilder sb = new StringBuilder();
    while (count-- != 0) {
      int character = (int) (Math.random() * "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".length());
      sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(character));
    }
    return sb.toString();
  }

  public Map<String, Object> initiatePayment(SubscriptionPaymentBean sPymentRequest, Double grndAmt, final String subsCurency) throws PayPalRESTException {
    Double itemGst, amountWithGST, disc;
    NimaiCustomerSubscriptionGrandAmount nc = getCustomerAmount(sPymentRequest.getUserId());
    String userId = sPymentRequest.getUserId();
    String merchantId = sPymentRequest.getMerchantId();
    String orderId = sPymentRequest.getOrderId();
    Double amount = grndAmt;
    String currency = sPymentRequest.getCurrency();
    String redirectURL = sPymentRequest.getRedirectURL();
    String cancelURL = sPymentRequest.getCancelURL();
    String merchantParam1 = sPymentRequest.getMerchantParam1();
    String merchantParam2 = sPymentRequest.getMerchantParam2();
    final String merchantParam3 = sPymentRequest.getMerchantParam3();
    String merchantParam4 = sPymentRequest.getMerchantParam4();
    final String merchantParam5 = sPymentRequest.getMerchantParam5();
    String merchantParam6 = sPymentRequest.getMerchantParam6();
    Optional<NimaiMCustomer> mCustomer = this.userRepository.findByUserId(merchantParam1);
    String productDescription = "merchantParam1=" + merchantParam1 + ",merchantParam2=" + merchantParam2 + ",merchantParam3=" + merchantParam3 + ",merchantParam4=" + merchantParam4 + ",merchantParam5=" + merchantParam5 + ",merchantParam6=" + merchantParam6;
    System.out.println("Product description: " + productDescription);
    Map<String, Object> response = new HashMap<>();
    int vasCount = StringUtils.countOccurrencesOf(merchantParam4, "-");
    System.out.println("Total VAS: " + vasCount);
    String[] vasSplitted = merchantParam4.split("-", vasCount + 1);
    Double vasAmount = null;
    Double vasFinalAmount = Double.valueOf(0.0D);
    try {
      for (int i = 0; i < vasCount; i++) {
        System.out.println("Iteration: " + i);
        System.out.println("VAS: " + vasSplitted[i]);
        vasAmount = this.advRepo.findPricingByVASId(Integer.valueOf(vasSplitted[i]));
        if (vasAmount == null) {
          vasFinalAmount = Double.valueOf(0.0D);
        } else {
          vasFinalAmount = Double.valueOf(vasFinalAmount.doubleValue() + vasAmount.doubleValue());
        }
      }
    } catch (Exception e) {
      vasAmount = Double.valueOf(0.0D);
    }
    final Double vasAmt = vasFinalAmount;
    Order order = null;
    OrderRequest orderRequest = new OrderRequest();
    orderRequest.checkoutPaymentIntent("CAPTURE");
    ApplicationContext ac = new ApplicationContext();
    if (merchantParam1.substring(0, 2).equalsIgnoreCase("BA")) {
      ac.cancelUrl(this.redirectFromPaymentLink + "#/bcst/dsb/subscription");
      ac.returnUrl(this.redirectFromPaymentLink + "#/bcst/dsb/subscription");
    } else {
      ac.cancelUrl(this.redirectFromPaymentLink + "#/cst/dsb/subscription");
      ac.returnUrl(this.redirectFromPaymentLink + "#/cst/dsb/subscription");
    }
    ac.shippingPreference("NO_SHIPPING");

    ac.userAction("PAY_NOW");
    PaymentMethod pm = new PaymentMethod();
    pm.payerSelected("PAYPAL");
    pm.payeePreferred("IMMEDIATE_PAYMENT_REQUIRED");
    ac.paymentMethod(pm);
    orderRequest.applicationContext(ac);
    System.out.println("Initiating Payer");
    Payer payer = new Payer();
    Name name = new Name();
    name.givenName(((NimaiMCustomer) mCustomer.get()).getFirstName());
    name.surname(((NimaiMCustomer) mCustomer.get()).getLastName());

    payer.name(name);
    orderRequest.payer(payer);
    //orderRequest.processingInstruction(productDescription);

    System.out.println("Subscription Amount:" + merchantParam5);
    System.out.println("VAS Amount:" + String.format("%.2f", new Object[]{vasAmt}));
    Double gst = Double.valueOf(this.subscriptionRepo.getGSTValue().doubleValue() / 100.0D);
    System.out.println("GST Value from DB: " + gst);
    if (merchantParam3.equalsIgnoreCase("renew-vas")) {
      itemGst = Double.valueOf(vasAmt.doubleValue() * gst.doubleValue());
      amountWithGST = Double.valueOf(vasAmt.doubleValue() + itemGst.doubleValue());
    } else {
      itemGst = Double.valueOf(amount.doubleValue() * gst.doubleValue());
      amountWithGST = Double.valueOf(amount.doubleValue() + itemGst.doubleValue());
    }
    if (!vasSplitted[vasCount].equalsIgnoreCase("0")) {
      disc = Double.valueOf(vasSplitted[vasCount]);
    } else {
      disc = Double.valueOf(0.0D);
    }
    Double subVas = Double.valueOf(Double.valueOf(merchantParam5).doubleValue() + vasAmt.doubleValue());
    System.out.println("Subsc + VAS: " + subVas);
    System.out.println("Discount: " + disc);
    System.out.println("Amount without GST: " + amount);
    System.out.println("GST: " + itemGst);
    System.out.println("Amount with GST: " + String.format("%.2f", new Object[]{amountWithGST}));
    System.out.println("Amount: " + amount);
    String invoiceId = generatePaymentTtransactionID(10);
    List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
    PurchaseUnitRequest purchaseUnitRequest = null;
    if (merchantParam3.equalsIgnoreCase("renew") || merchantParam3.equalsIgnoreCase("new")) {
      System.out.println("Its renew/new");
      purchaseUnitRequest = (new PurchaseUnitRequest())
              .referenceId(productDescription)
              .invoiceId(invoiceId)
              .amountWithBreakdown((new AmountWithBreakdown())
                      .currencyCode(subsCurency).
                      value("" + String.format("%.2f", new Object[]{amountWithGST})).
                      amountBreakdown((new AmountBreakdown()).
                              itemTotal((new Money()).currencyCode(subsCurency).value("" + subVas)).discount((new Money()).currencyCode(subsCurency).value("" + String.format("%.2f", new Object[]{disc}))).taxTotal((new Money()).currencyCode(subsCurency).value("" + String.format("%.2f", new Object[]{itemGst}))))).items(new ArrayList<com.paypal.orders.Item>() {

              });
    } else {
      purchaseUnitRequest = (new PurchaseUnitRequest()).referenceId(merchantParam1 + "-" + merchantParam2 + "-" + merchantParam3 + "-" + merchantParam6).invoiceId(invoiceId).amountWithBreakdown((new AmountWithBreakdown()).currencyCode(subsCurency).value("" + String.format("%.2f", new Object[]{amountWithGST})).amountBreakdown((new AmountBreakdown()).itemTotal((new Money()).currencyCode(subsCurency).value("" + String.format("%.2f", new Object[]{vasAmt}))).discount((new Money()).currencyCode(subsCurency).value("" + String.format("%.2f", new Object[]{disc}))).taxTotal((new Money()).currencyCode(subsCurency).value("" + String.format("%.2f", new Object[]{itemGst}))))).items(new ArrayList<com.paypal.orders.Item>() {

      });
    }
    orderRequest.purchaseUnits(Arrays.asList(purchaseUnitRequest));
    System.out.println("orderRequest: " + orderRequest.purchaseUnits());
    //purchaseUnitRequests.add(purchaseUnitRequest);
    //orderRequest.purchaseUnits(purchaseUnitRequests);
    OrdersCreateRequest request = (new OrdersCreateRequest()).requestBody(orderRequest);
    try {
      HttpResponse<Order> OrderResponse = Credentials.client.execute((HttpRequest) request);
      order = (Order) OrderResponse.result();
      System.out.println("Order ID: " + order.id());
      String redirectLink = "";
      for (LinkDescription o : order.links()) {
        System.out.println("---" + o.href());
        if (o.rel().equalsIgnoreCase("approve")) {
          redirectLink = o.href();
          break;
        }
      }
      response.put("status", "success");
      response.put("redirect_url", redirectLink);
    } catch (IOException ioe) {
      if (ioe instanceof HttpException) {
        HttpException he = (HttpException) ioe;
        System.out.println(he.getMessage());
        he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
      }
    }
    return response;
  }



  public Map<String, Object> initiatePaymentForPostpaid(SubscriptionPaymentBean sPaymentRequest, Double grandAmt, String subsCurrency) throws PayPalRESTException {
    Double itemGst, amountWithGST, disc;
    NimaiCustomerSubscriptionGrandAmount nc = getCustomerAmount(sPaymentRequest.getUserId());
    String userId = sPaymentRequest.getUserId();
    String merchantId = sPaymentRequest.getMerchantId();
    String orderId = sPaymentRequest.getOrderId();
    Double amount = grandAmt;
    String currency = sPaymentRequest.getCurrency();
    String redirectURL = sPaymentRequest.getRedirectURL();
    String cancelURL = sPaymentRequest.getCancelURL();
    String merchantParam1 = sPaymentRequest.getMerchantParam1();
    String merchantParam2 = sPaymentRequest.getMerchantParam2();
    final String merchantParam3 = sPaymentRequest.getMerchantParam3();
    String merchantParam4 = sPaymentRequest.getMerchantParam4();
    String merchantParam5 = sPaymentRequest.getMerchantParam5();
    String [] keys = merchantParam5.split(":");
    String key = keys[0].toString();
    String merchantParam6 = sPaymentRequest.getMerchantParam6();
    Optional<NimaiMCustomer> mCustomer = this.userRepository.findByUserId(merchantParam1);
    String productDescription = "merchantParam1=" + merchantParam1 + ",merchantParam2=" + merchantParam2 + ",merchantParam3=" + merchantParam3 + ",merchantParam4=" + merchantParam4 + ",merchantParam5=" + merchantParam5 + ",merchantParam6=" + merchantParam6;
   
    System.out.println("Product description: " + productDescription);
    Map<String, Object> response = new HashMap<>();
    int vasCount = StringUtils.countOccurrencesOf(merchantParam4, "-");
    System.out.println("Total VAS: " + vasCount);
    String[] vasSplitted = merchantParam4.split("-", vasCount + 1);
    Double vasAmount = null;
    Double vasFinalAmount = Double.valueOf(0.0D);
    try {
      for (int i = 0; i < vasCount; i++) {
        System.out.println("Iteration: " + i);
        System.out.println("VAS: " + vasSplitted[i]);
        vasAmount = this.advRepo.findPricingByVASId(Integer.valueOf(vasSplitted[i]));
        if (vasAmount == null) {
          vasFinalAmount = Double.valueOf(0.0D);
        } else {
          vasFinalAmount = Double.valueOf(0.0D);
        }
      }
    } catch (Exception e) {
      vasAmount = Double.valueOf(0.0D);
    }
    final Double vasAmt = vasFinalAmount;
    Order order = null;
    OrderRequest orderRequest = new OrderRequest();
    orderRequest.checkoutPaymentIntent("CAPTURE");
    ApplicationContext ac = new ApplicationContext();
    if (merchantParam1.substring(0, 2).equalsIgnoreCase("BA")) {
      ac.cancelUrl(this.redirectFromPaymentLink + "#/bcst/dsb/subscription");
      ac.returnUrl(this.redirectFromPaymentLink + "#/bcst/dsb/subscription");
    } else {
      ac.cancelUrl(this.redirectFromPaymentLink + "#/cst/dsb/subscription");
      ac.returnUrl(this.redirectFromPaymentLink + "#/cst/dsb/subscription");
    }
    ac.shippingPreference("NO_SHIPPING");
    ac.userAction("PAY_NOW");
    PaymentMethod pm = new PaymentMethod();
    pm.payerSelected("PAYPAL");
    pm.payeePreferred("IMMEDIATE_PAYMENT_REQUIRED");
    ac.paymentMethod(pm);
    orderRequest.applicationContext(ac);
    Payer payer = new Payer();
    Name name = new Name();
    name.givenName(((NimaiMCustomer) mCustomer.get()).getFirstName());
    name.surname(((NimaiMCustomer) mCustomer.get()).getLastName());
    payer.name(name);
    payer.email(((NimaiMCustomer) mCustomer.get()).getEmailAddress());
    orderRequest.payer(payer);
    System.out.println("Subscription Amount:" + merchantParam5);
    System.out.println("VAS Amount:" + String.format("%.2f", new Object[]{vasAmt}));
    Double gst = Double.valueOf(this.subscriptionRepo.getGSTValue().doubleValue() / 100.0D);
    System.out.println("GST Value from DB: " + gst);
    if (merchantParam3.equalsIgnoreCase("renew-vas")) {
      itemGst = Double.valueOf(vasAmt.doubleValue() * gst.doubleValue());
      amountWithGST = Double.valueOf(vasAmt.doubleValue() + itemGst.doubleValue());
    } else {
      itemGst = Double.valueOf(amount.doubleValue() * gst.doubleValue());
      amountWithGST = Double.valueOf(amount.doubleValue() + itemGst.doubleValue());
    }
    if (!vasSplitted[vasCount].equalsIgnoreCase("0")) {
      //disc = Double.valueOf(vasSplitted[vasCount]);
    	disc = Double.valueOf(0.0D);
    } else {
      disc = Double.valueOf(0.0D);
    }
    Double subVas = Double.valueOf(Double.valueOf(amountWithGST).doubleValue() + vasAmt.doubleValue());
    System.out.println("Subsc + VAS: " + subVas);
    System.out.println("Discount: " + disc);
    System.out.println("Amount without GST: " + amount);
    System.out.println("GST: " + itemGst);
    System.out.println("Amount with GST: " + String.format("%.2f", new Object[]{amountWithGST}));
    System.out.println("Amount: " + amount);
    /*System.out.println("Logs at initiatePaymentForPostpaid ::" );
    String transactionId =  sPaymentRequest.getTransactionId();
    System.out.println("User Id = "+userId);
    NimaiPostpaidSubscriptionDetails nimaiPostpaidSubscriptionDetails = this.postpaidSPlanRepository.findUserByUserIdAndTransactionId(userId,transactionId);
    System.out.println("Logs at initiatePaymentForPostpaid ::"+ nimaiPostpaidSubscriptionDetails.getTransactionId() );
    NimaiSubscriptionDetails subscriptionDetails = this.subscriptionDetailsRepository.findByUserId(userId);
    System.out.println("Logs at  initiatePaymentForPostpaid subscriptionDetails ::"+ subscriptionDetails.getSubscriptionName());
    NimaiPostpaidSubscriptionDetails npsp = postpaidSPlanRepository.getOne(nimaiPostpaidSubscriptionDetails.getPostpaidId());
    System.out.println("Logs at npsp ::"+ npsp.getTotalPayment());
    List<NimaiSubscriptionVas> vasList = null;
    vasList = this.nimaiSubscriptionVasRepo.findActiveVASByUserId(userId);
    String[] newKeyValueDetails = null;
    String amountFieldGetKey = "";
    newKeyValueDetails = merchantParam5.split(":");
    Double finalPayment= 0.0;
    if(Objects.isNull(nimaiPostpaidSubscriptionDetails.getModeOfPayment())){
      npsp.setModeOfPayment("Credit");
      npsp.setStatus("ACTIVE");
      npsp.setPaymentStatus("Pending");
      postpaidSPlanRepository.save(npsp);
    }//else if(nimaiPostpaidSubscriptionDetails.getModeOfPayment().equalsIgnoreCase("Credit") &&  (nimaiPostpaidSubscriptionDetails.getPaymentStatus().equalsIgnoreCase("Pending") || (nimaiPostpaidSubscriptionDetails.getPaymentStatus().equalsIgnoreCase("Maker Approved")))){
     // response.put("reject","reject");
     // return response;
    //}
    vasAmount=0.0;
    System.out.println("Logs at initiatePaymentForPostpaid npsp setPaymentStatus ::"+ npsp.getPaymentStatus());
    double payment = 0.0;
    Double discount=0.0;
    String invoiceId = generatePaymentTtransactionID(10);
    String paymentTrId = generatePaymentTtransactionID(15);
   //if (npsp.getModeOfPayment().equalsIgnoreCase("Credit") && (npsp.getPaymentStatus().equalsIgnoreCase("Pending") || npsp.getPaymentStatus().equalsIgnoreCase("Maker Approved"))) {
      for (NimaiSubscriptionVas vasPlan : vasList) {
        vasAmount=vasAmount+vasPlan.getPricing();
        System.out.println("Vas Amount ="+ vasAmount);
      }
      System.out.println("Value is as new  initiatePaymentForPostpaid ::" + newKeyValueDetails);
      amountFieldGetKey = newKeyValueDetails[0].toString();
      System.out.println("Dues Type is initiatePaymentForPostpaid ::" + amountFieldGetKey);
      System.out.println("Value is initiatePaymentForPostpaid ::" + newKeyValueDetails);
      amountFieldGetKey = newKeyValueDetails[0].toString();
      System.out.println("Dues is initiatePaymentForPostpaid ::" + amountFieldGetKey);
      if (amountFieldGetKey.equalsIgnoreCase("minDue")) {
        payment = this.postpaidSPlanRepository.findMinDueByPaymentCounter(userId, transactionId);
        try {
          discount=subscriptionDetails.getDiscount();
          vasAmount= Double.valueOf(subscriptionDetails.getVasAmount());
        }catch (Exception ex){
          ex.printStackTrace();
        }
        finalPayment = payment-discount+vasAmount;
        System.out.println("payFor API1 : finalPayment"+ finalPayment);
        npsp.setDue_type(amountFieldGetKey);
        System.out.println("Discount Id is initiatePaymentForPostpaid ::" + subscriptionDetails.getDiscountId());
        System.out.println("Discount Amount is initiatePaymentForPostpaid ::" + subscriptionDetails.getDiscount());
        try {
          npsp.setDisountId(subscriptionDetails.getDiscountId());
          npsp.setDiscountAmnt(subscriptionDetails.getDiscount());
          npsp.setTotalPayment(finalPayment);
          subscriptionDetails.setInvoiceId(invoiceId);
          npsp.setInvoiceId(subscriptionDetails);
          npsp.setPaymentTxnId(paymentTrId);
          postpaidSPlanRepository.save(npsp);
          subscriptionDetails.setGrandAmount(finalPayment);
          subscriptionDetailsRepository.save(subscriptionDetails);
        }catch (Exception ex){
          ex.printStackTrace();
        }
      } else {
        List transactionIdsFromQuotation = this.postpaidSPlanRepository.findPendingTransactionIdsFromQuotationOnly(userId);
        List<NimaiPostpaidSubscriptionDetails> nn = this.postpaidSPlanRepository.findPendingTransactionIdsFromQuotation(transactionIdsFromQuotation);
        payment = this.postpaidSPlanRepository.findSumOfTotalDue(userId);
        try {
          discount = subscriptionDetails.getDiscount();
          vasAmount = Double.valueOf(subscriptionDetails.getVasAmount());
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        finalPayment = payment - discount + vasAmount;
        System.out.println("payFor API2 : finalPayment" + finalPayment);
        for (NimaiPostpaidSubscriptionDetails nv : nn) {
          if (Objects.isNull(nv.getDue_type())) {
            nv.setDue_type(amountFieldGetKey);
            nv.setModeOfPayment("Credit");
            System.out.println("Discount id is :" + subscriptionDetails.getDiscountId());
            try {
              nv.setDisountId(subscriptionDetails.getDiscountId());
              nv.setPaymentStatus("Pending");
              nv.setDiscountAmnt(subscriptionDetails.getDiscount());
              nv.setTotalPayment(finalPayment);
              subscriptionDetails.setInvoiceId(invoiceId);
              System.out.println("Invoice Id:"+ subscriptionDetails.getInvoiceId());
              nv.setInvoiceId(subscriptionDetails);
              nv.setPaymentTxnId(paymentTrId);
              postpaidSPlanRepository.save(npsp);
              subscriptionDetails.setGrandAmount(finalPayment);
              subscriptionDetailsRepository.save(subscriptionDetails);
            } catch (Exception ex) {
              ex.printStackTrace();
            }

          }

        }
      }
   // }
    System.out.println("Details:" + userId + "::" + paymentTrId + "::" + invoiceId + "::" + finalPayment);
    subscriptionDetailsRepository.updatePaymentTxnIdForWire(userId, paymentTrId, invoiceId, finalPayment.toString());
    userRepository.updatePaymentTransactionId(userId, invoiceId);
    userRepository.updatePaymentMode(npsp.getModeOfPayment(), userId);
    nimaiSubscriptionVasRepo.updatePaymentTransactionId(userId,invoiceId);
    */
    //Above Surabhi Code to save data without payment completion
   String invoiceId = generatePaymentTtransactionID(10);
    String paymentTrId = generatePaymentTtransactionID(15);
    /*  String transactionId =  sPaymentRequest.getTransactionId();
    System.out.println("User Id = "+userId);
    NimaiPostpaidSubscriptionDetails npsp = postpaidSPlanRepository.findUsersByUserIdTransactionIdAndPaymentStatus(userId,transactionId);
    NimaiSubscriptionDetails nsd = subscriptionDetailsRepository.findByUserId(userId);
    nsd.setInvoiceId(invoiceId);
    nsd.setPaymentMode("Credit");
    nsd.setPaymentStatus("Approved");
    nsd.setGrandAmount(grandAmt);
    subscriptionDetailsRepository.save(nsd);
    npsp.setInvoiceId(nsd);
    npsp.setPaymentTxnId(paymentTrId);
    npsp.setPaymentStatus("Approved");
    npsp.setTotalPayment(grandAmt);
    npsp.setModeOfPayment("Credit");
    npsp.setDue_type(key);
    postpaidSPlanRepository.save(npsp);
    System.out.println("Details:"+userId+"::"+paymentTrId+"::"+ invoiceId+"::"+ npsp.getTotalPayment().toString());
    subscriptionDetailsRepository.updatePaymentTxnIdForWire(userId, paymentTrId, invoiceId, npsp.getTotalPayment().toString());
    userRepository.updatePaymentTransactionId(userId, invoiceId);
    userRepository.updatePaymentMode(npsp.getModeOfPayment(), userId);*/

    List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
    PurchaseUnitRequest purchaseUnitRequest = null;
    
    if (merchantParam3.equalsIgnoreCase("renew") || merchantParam3.equalsIgnoreCase("new")) {
    	System.out.println("Its new/renew - POSTPAID");
      purchaseUnitRequest = (new PurchaseUnitRequest()).
              referenceId
                      (productDescription).invoiceId(invoiceId).amountWithBreakdown((new AmountWithBreakdown()).currencyCode(subsCurrency).
                      value("" + String.format("%.2f", new Object[]{amountWithGST})));
                      //.
                      //amountBreakdown((new AmountBreakdown()).
                      //        itemTotal((new Money()).currencyCode(subsCurrency).value("" + subVas)).discount((new Money()).currencyCode(subsCurrency).value("" + String.format("%.2f", new Object[]{disc}))).taxTotal((new Money()).currencyCode(subsCurrency).value("" + String.format("%.2f", new Object[]{itemGst}))))).items(new ArrayList<com.paypal.orders.Item>() {

             // });
    } else {
      purchaseUnitRequest = (new PurchaseUnitRequest()).referenceId(merchantParam1 + "-" + merchantParam2 + "-" + merchantParam3 + "-" + merchantParam6).invoiceId(invoiceId).amountWithBreakdown((new AmountWithBreakdown()).currencyCode(subsCurrency).value("" + String.format("%.2f", new Object[]{amountWithGST})).amountBreakdown((new AmountBreakdown()).itemTotal((new Money()).currencyCode(subsCurrency).value("" + String.format("%.2f", new Object[]{vasAmt}))).discount((new Money()).currencyCode(subsCurrency).value("" + String.format("%.2f", new Object[]{disc}))).taxTotal((new Money()).currencyCode(subsCurrency).value("" + String.format("%.2f", new Object[]{itemGst}))))).items(new ArrayList<com.paypal.orders.Item>() {

      });
    }

    orderRequest.purchaseUnits(Arrays.asList(purchaseUnitRequest));
    System.out.println("orderRequest: " + orderRequest.purchaseUnits());
    //purchaseUnitRequests.add(purchaseUnitRequest);
    //orderRequest.purchaseUnits(purchaseUnitRequests);
    OrdersCreateRequest request = (new OrdersCreateRequest()).requestBody(orderRequest);
    try {
      HttpResponse<Order> OrderResponse = Credentials.client.execute((HttpRequest) request);
      order = (Order) OrderResponse.result();
      System.out.println("Order ID: " + order.id());
      String redirectLink = "";
      for (LinkDescription o : order.links()) {
        System.out.println("---" + o.href());
        if (o.rel().equalsIgnoreCase("approve")) {
          redirectLink = o.href();
          break;
        }
      }
      response.put("status", "success");
      response.put("redirect_url", redirectLink);
    } catch (IOException ioe) {
      if (ioe instanceof HttpException) {
        HttpException he = (HttpException) ioe;
        System.out.println(he.getMessage());
        he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
      }
    }
    return response;
  }

  public Map<String, Object> executePaymentPostPaid(String orderId) throws PayPalRESTException {
    System.out.println("In executePayment of PostPaid..");
    System.out.println("");
    Map<String, Object> responseData = new HashMap<>();
    Order order = null;
    OrdersGetRequest request = new OrdersGetRequest(orderId);
    try {
      HttpResponse<Order> responsePaypal = Credentials.client.execute((HttpRequest) request);
      order = (Order) responsePaypal.result();
      System.out.println("Details: " + (order.purchaseUnits().get(0)).referenceId());
      //System.out.println("Production Information: "+order.payer().payerId());
      //System.out.println("Details: " + ((PurchaseUnit) order.purchaseUnits().get(0)).items().size());
      //System.out.println("Purchase Unit Size at Details: " + ((PurchaseUnit) order.purchaseUnits().get(0)).items().size());

      String[] planSplit = ((PurchaseUnit) order.purchaseUnits().get(0)).referenceId().split(",", 6);
      System.out.println("Length: " + planSplit.length);
      System.out.println("planSplit[0] :" + planSplit[0]);
      System.out.println("planSplit[1] :" + planSplit[1]);
      System.out.println("planSplit[2] :" + planSplit[2]);
      System.out.println("planSplit[3] :" + planSplit[3]);
      System.out.println("planSplit[4] :" + planSplit[4]);
      System.out.println("planSplit[5] :" + planSplit[5]);
      String discId = "";
      if (planSplit.length == 6) {
    	  System.out.println("Assigning discId");
        discId = planSplit[5].substring(planSplit[5].indexOf("=")+1);
        System.out.println("discId: "+discId);  
      }
      System.out.println("discId: "+discId);
       /* if (planSplit.length == 5) {
          discId = planSplit[4];
          System.out.println("planSplit[4] :" + planSplit[4]);
        }*/
      //Commented on 28-11-2022
      /*String[] planSplit = ((PurchaseUnit) order.purchaseUnits().get(0)).referenceId().split("-", 5);
      System.out.println("Length: " + planSplit.length);
      System.out.println("planSplit[0] :" + planSplit[0]);
      System.out.println("planSplit[1] :" + planSplit[1]);
      System.out.println("planSplit[2] :" + planSplit[2]);
      String discId = "";
      if (planSplit.length == 4) {
        discId = planSplit[3];
        System.out.println("planSplit[3] :" + planSplit[3]);
      }
      if (planSplit.length == 5) {
        discId = planSplit[4];
        System.out.println("planSplit[4] :" + planSplit[4]);
      }*/
      String vasPrice = "0", discountPrice = "";
      try
      {
    	  String valueOfVAS=planSplit[3].substring(planSplit[3].indexOf("=")+1);
    	  System.out.println("valueOfVAS: "+valueOfVAS);
    	  
    	  if(valueOfVAS.contains("-"))
    	  {
    		  System.out.println("- is present");
    		  Double finalPrice=0d;
    		  System.out.println("VAS plan is present -");
    		  int vasCount = StringUtils.countOccurrencesOf(valueOfVAS, "-");
  	        System.out.println("Total VAS: " + vasCount);
  	        String[] vasSplitted =valueOfVAS.split("-", vasCount + 1);
  	        for (int i = 0; i < vasCount; i++) {
  	        	System.out.println("vasSplitted: "+vasSplitted[i]);
  	        	Double price=advRepo.findPricingByVASId(Integer.valueOf(vasSplitted[i]));
  	        	finalPrice=finalPrice+price;
  	        }
  	        vasPrice=""+finalPrice;
  	        System.out.println("final VAS Price: "+vasPrice);
    	  }
    	  else if(Integer.valueOf(valueOfVAS)==0)
    	  {
    		  System.out.println("No VAS Plan");
    		  vasPrice="0";
    	  }
    	  
    	  else
    	  {
    		  int vasCount = StringUtils.countOccurrencesOf(valueOfVAS, "-");
    		  if(vasCount>0)
    		  {
    			  System.out.println("VAS Plan is present");
    			  vasPrice=""+advRepo.findPricingByVASId(Integer.valueOf(planSplit[3].substring(planSplit[3].indexOf("=")+1,planSplit[3].indexOf("-"))));
    	  
    		  }
    		  else
    		  {
    			  System.out.println("VAS is not present as vasCount==0");
    			  vasPrice="0";
    		  }
    	  }
    	if(vasPrice==null || vasPrice.equalsIgnoreCase("null"))
        {
        	System.out.println("vasPrice is null");
        	vasPrice="0";
        }
      }
      catch(NullPointerException ne)
      {
        vasPrice="0";
      }
      //25-11-2022 Test
      /*System.out.println("Purchase Unit Size: " + ((PurchaseUnit) order.purchaseUnits().get(0)).items().size());
      if (((PurchaseUnit) order.purchaseUnits().get(0)).items().size() == 2) {
        vasPrice = ((com.paypal.orders.Item) ((PurchaseUnit) order.purchaseUnits().get(0)).items().get(1)).unitAmount().value();
      } else {
        vasPrice = "0";
      }*/
      System.out.println("VAS Price: " + vasPrice);
      try {
        if (((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().amountBreakdown().discount().value().equalsIgnoreCase("0") || ((PurchaseUnit) order
                .purchaseUnits().get(0)).amountWithBreakdown().amountBreakdown().discount().value() == null) {
          discountPrice = "0";
        } else {
          discountPrice = ((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().amountBreakdown().discount().value();
        }
      } catch (NullPointerException e) {
        discountPrice = "0";
      }
      String merchantP1 = planSplit[0].substring(planSplit[0].indexOf("=")+1);
      String merchantP2 = planSplit[1].substring(planSplit[1].indexOf("=")+1);
      String merchantP3 = planSplit[2].substring(planSplit[2].indexOf("=")+1);
      String merchantP4 = vasPrice + "-" + discountPrice;
      System.out.println("assigning amount to merchantP5");
      String merchantP5="";
      NimaiMSubscription subsDetail=null;
      String planType=merchantP2.substring(merchantP2.indexOf("=")+1,merchantP2.indexOf("-"));
      System.out.println("merchantP1: "+merchantP1);
      System.out.println("merchantP2: "+merchantP2);
      System.out.println("merchantP3: "+merchantP3);
      System.out.println("merchantP4: "+merchantP4);
      System.out.println("planType: "+planType);
      if(planType.equalsIgnoreCase("postpaid"))
      {
    	  System.out.println("Its postpaid plan");
    	  merchantP5 = planSplit[4].substring(planSplit[4].indexOf(":")+1);
    	  subsDetail=new NimaiMSubscription();
    	  subsDetail.setCustomerSupport("12*7");
    	  subsDetail.setlCount("1");
    	  subsDetail.setSubscriptionName("POSTPAID_PLAN");
    	  subsDetail.setRelationshipManager("Yes");
      }
      else
      {
    	  System.out.println("Its prepaid plan");
    	  merchantP5 = planSplit[4].substring(planSplit[4].indexOf("=")+1);//((com.paypal.orders.Item) ((PurchaseUnit) order.purchaseUnits().get(0)).items().get(0)).unitAmount().value();
    	  subsDetail = getPlanDetailsBySubscriptionId(merchantP2);

      }
      System.out.println("Merchant Param= " + merchantP1 + "--" + merchantP2 + "--" + merchantP3 + "--" + merchantP4 + "--" + merchantP5);
      //System.out.println("Amount with GST --> execute= " + ((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().value());
      
      Double finalAmount;

      finalAmount=(Double.valueOf(merchantP5)+Double.valueOf(vasPrice))-Double.valueOf(discountPrice);
      System.out.println("final Amount: "+finalAmount);

      responseData.put("subscriptionId", merchantP2);
      responseData.put("orderId", orderId);
      responseData.put("custSupport", subsDetail.getCustomerSupport());
      responseData.put("lcCount", subsDetail.getlCount());
      responseData.put("relManager", subsDetail.getRelationshipManager());
      responseData.put("subsAmount", finalAmount);// + ((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().value());
      responseData.put("subsName", subsDetail.getSubscriptionName());
      responseData.put("subsValidity", String.valueOf(subsDetail.getSubscriptionValidity()));
      responseData.put("subsidiaries", subsDetail.getSubsidiaries());
      responseData.put("userId", merchantP1);
      responseData.put("subsflag", merchantP3);
      responseData.put("actualAmt", merchantP5);
      responseData.put("paymentMode", "Credit");
      responseData.put("userId", merchantP1);
      responseData.put("discAmount", discountPrice);
      responseData.put("discId", discId);
      responseData.put("vasAmount", vasPrice);
      responseData.put("OrderStatus", order.status());
      responseData.put("invoiceId", ((PurchaseUnit) order.purchaseUnits().get(0)).invoiceId());
    } catch (IOException ioe) {
      if (ioe instanceof HttpException) {
        System.out.println("Issue On Server Side");
        HttpException he = (HttpException) ioe;
        System.out.println(he.getMessage());
        he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
      }
    }
    return responseData;
  }
  
  public Map<String, Object> executePayment(String orderId) throws PayPalRESTException {
	    System.out.println("In executePayment..");
	    System.out.println("");
	    Map<String, Object> responseData = new HashMap<>();
	    Order order = null;
	    OrdersGetRequest request = new OrdersGetRequest(orderId);
	    try {
	      HttpResponse<Order> responsePaypal = Credentials.client.execute((HttpRequest) request);
	      order = (Order) responsePaypal.result();
	      System.out.println("Details: " + (order.purchaseUnits().get(0)).referenceId());
	      //System.out.println("Production Information: "+order.payer().payerId());
	      //System.out.println("Details: " + ((PurchaseUnit) order.purchaseUnits().get(0)).items().size());
	      //System.out.println("Purchase Unit Size at Details: " + ((PurchaseUnit) order.purchaseUnits().get(0)).items().size());

	      String[] planSplit = ((PurchaseUnit) order.purchaseUnits().get(0)).referenceId().split(",", 6);
	      System.out.println("Length: " + planSplit.length);
	      System.out.println("planSplit[0] :" + planSplit[0]);
	      System.out.println("planSplit[1] :" + planSplit[1]);
	      System.out.println("planSplit[2] :" + planSplit[2]);
	      System.out.println("planSplit[3] :" + planSplit[3]);
	      System.out.println("planSplit[4] :" + planSplit[4]);
	      System.out.println("planSplit[5] :" + planSplit[5]);
	      String discId = "";
	      if (planSplit.length == 6) {
	        discId = planSplit[5].substring(planSplit[5].indexOf("=")+1);
	      }
	       /* if (planSplit.length == 5) {
	          discId = planSplit[4];
	          System.out.println("planSplit[4] :" + planSplit[4]);
	        }*/
	      //Commented on 28-11-2022
	      /*String[] planSplit = ((PurchaseUnit) order.purchaseUnits().get(0)).referenceId().split("-", 5);
	      System.out.println("Length: " + planSplit.length);
	      System.out.println("planSplit[0] :" + planSplit[0]);
	      System.out.println("planSplit[1] :" + planSplit[1]);
	      System.out.println("planSplit[2] :" + planSplit[2]);
	      String discId = "";
	      if (planSplit.length == 4) {
	        discId = planSplit[3];
	        System.out.println("planSplit[3] :" + planSplit[3]);
	      }
	      if (planSplit.length == 5) {
	        discId = planSplit[4];
	        System.out.println("planSplit[4] :" + planSplit[4]);
	      }*/
	      String vasPrice = "0", discountPrice = "";
	      try
	      {
	        vasPrice=""+advRepo.findPricingByVASId(Integer.valueOf(planSplit[3].substring(planSplit[3].indexOf("=")+1,planSplit[3].indexOf("-"))));
	        if(vasPrice==null || vasPrice.equalsIgnoreCase("null"))
	        {
	        	System.out.println("vasPrice is null");
	        	vasPrice="0";
	        }
	      }
	      catch(NullPointerException ne)
	      {
	        vasPrice="0";
	      }
	      //25-11-2022 Test
	      /*System.out.println("Purchase Unit Size: " + ((PurchaseUnit) order.purchaseUnits().get(0)).items().size());
	      if (((PurchaseUnit) order.purchaseUnits().get(0)).items().size() == 2) {
	        vasPrice = ((com.paypal.orders.Item) ((PurchaseUnit) order.purchaseUnits().get(0)).items().get(1)).unitAmount().value();
	      } else {
	        vasPrice = "0";
	      }*/
	      System.out.println("VAS Price: " + vasPrice);
	      try {
	        if (((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().amountBreakdown().discount().value().equalsIgnoreCase("0") || ((PurchaseUnit) order
	                .purchaseUnits().get(0)).amountWithBreakdown().amountBreakdown().discount().value() == null) {
	          discountPrice = "0";
	        } else {
	          discountPrice = ((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().amountBreakdown().discount().value();
	        }
	      } catch (NullPointerException e) {
	        discountPrice = "0";
	      }
	      String merchantP1 = planSplit[0].substring(planSplit[0].indexOf("=")+1);
	      String merchantP2 = planSplit[1].substring(planSplit[1].indexOf("=")+1);
	      String merchantP3 = planSplit[2].substring(planSplit[2].indexOf("=")+1);
	      String merchantP4 = vasPrice + "-" + discountPrice;
	      System.out.println("assigning amount to merchantP5");
	      String merchantP5="";
	      NimaiMSubscription subsDetail=null;
	      //String planType=//merchantP2.substring(merchantP2.indexOf("=")+1,merchantP2.indexOf("-"));
	      System.out.println("merchantP1: "+merchantP1);
	      System.out.println("merchantP2: "+merchantP2);
	      System.out.println("merchantP3: "+merchantP3);
	      System.out.println("merchantP4: "+merchantP4);
	      //System.out.println("planType: "+planType);
	      if(merchantP2.contains("postpaid"))
	      {
	    	  System.out.println("Its postpaid plan");
	    	  merchantP5 = planSplit[4].substring(planSplit[4].indexOf(":")+1);
	    	  subsDetail=new NimaiMSubscription();
	    	  subsDetail.setCustomerSupport("12*7");
	    	  subsDetail.setlCount("1");
	    	  subsDetail.setSubscriptionName("POSTPAID_PLAN");
	    	  subsDetail.setRelationshipManager("Yes");
	      }
	      else
	      {
	    	  System.out.println("Its prepaid plan");
	    	  merchantP5 = planSplit[4].substring(planSplit[4].indexOf("=")+1);//((com.paypal.orders.Item) ((PurchaseUnit) order.purchaseUnits().get(0)).items().get(0)).unitAmount().value();
	    	  subsDetail = getPlanDetailsBySubscriptionId(merchantP2);

	      }
	      System.out.println("Merchant Param= " + merchantP1 + "--" + merchantP2 + "--" + merchantP3 + "--" + merchantP4 + "--" + merchantP5);
	      //System.out.println("Amount with GST --> execute= " + ((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().value());
	      
	      Double finalAmount;

	      finalAmount=(Double.valueOf(merchantP5)+Double.valueOf(vasPrice))-Double.valueOf(discountPrice);
	      System.out.println("final Amount: "+finalAmount);

	      responseData.put("subscriptionId", merchantP2);
	      responseData.put("orderId", orderId);
	      responseData.put("custSupport", subsDetail.getCustomerSupport());
	      responseData.put("lcCount", subsDetail.getlCount());
	      responseData.put("relManager", subsDetail.getRelationshipManager());
	      responseData.put("subsAmount", finalAmount);// + ((PurchaseUnit) order.purchaseUnits().get(0)).amountWithBreakdown().value());
	      responseData.put("subsName", subsDetail.getSubscriptionName());
	      responseData.put("subsValidity", String.valueOf(subsDetail.getSubscriptionValidity()));
	      responseData.put("subsidiaries", subsDetail.getSubsidiaries());
	      responseData.put("userId", merchantP1);
	      responseData.put("subsflag", merchantP3);
	      responseData.put("actualAmt", merchantP5);
	      responseData.put("paymentMode", "Credit");
	      responseData.put("userId", merchantP1);
	      responseData.put("discAmount", discountPrice);
	      responseData.put("discId", discId);
	      responseData.put("vasAmount", vasPrice);
	      responseData.put("OrderStatus", order.status());
	      responseData.put("invoiceId", ((PurchaseUnit) order.purchaseUnits().get(0)).invoiceId());
	    } catch (IOException ioe) {
	      if (ioe instanceof HttpException) {
	        System.out.println("Issue On Server Side");
	        HttpException he = (HttpException) ioe;
	        System.out.println(he.getMessage());
	        he.headers().forEach(x -> System.out.println(x + " :" + he.headers().header(x)));
	      }
	    }
	    return responseData;
	  }

  private String mapParameterNameAndValues(String[] ccavenueParameterNames2, String[] requestValues) {
    String ccaRequest = "", pname = "", pvalue = "";
    for (int i = 0; i < ccavenueParameterNames2.length; i++) {
      if (ccavenueParameterNames2[i].equalsIgnoreCase("merchant_id")) {
        pname = "" + ccavenueParameterNames2[0];
        pvalue = requestValues[0];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("order_id")) {
        pname = "" + ccavenueParameterNames2[1];
        pvalue = requestValues[1];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("currency")) {
        pname = "" + ccavenueParameterNames2[2];
        pvalue = requestValues[2];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("amount")) {
        pname = "" + ccavenueParameterNames2[3];
        pvalue = requestValues[3];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("redirect_url")) {
        pname = "" + ccavenueParameterNames2[4];
        pvalue = requestValues[4];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("cancel_url")) {
        pname = "" + ccavenueParameterNames2[5];
        pvalue = requestValues[5];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("merchant_param1")) {
        pname = "" + ccavenueParameterNames2[22];
        pvalue = requestValues[6];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("merchant_param2")) {
        pname = "" + ccavenueParameterNames2[23];
        pvalue = requestValues[7];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("merchant_param3")) {
        pname = "" + ccavenueParameterNames2[24];
        pvalue = requestValues[8];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("merchant_param4")) {
        pname = "" + ccavenueParameterNames2[25];
        pvalue = requestValues[9];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
      if (ccavenueParameterNames2[i].equalsIgnoreCase("merchant_param5")) {
        pname = "" + ccavenueParameterNames2[26];
        pvalue = requestValues[10];
        ccaRequest = ccaRequest + pname + "=" + pvalue + "&";
      }
    }
    System.out.println("ccaRequest==" + ccaRequest);
    return ccaRequest;
  }

  public OnlinePayment checkPayment(SubscriptionPaymentBean sPymentRequest) {
    OnlinePayment op = this.onlinePaymentRepo.getDetailsByUserId(sPymentRequest.getUserId());
    System.out.println("Data of User: " + sPymentRequest.getUserId());
    System.out.println("Data: " + op);
    return op;
  }

  public NimaiMSubscription getPlanDetailsBySubscriptionId(String string) {
    return this.subscriptionRepo.findDetailBySubscriptionId(string);
  }

  public ResponseEntity<?> findAllSPlanDetailsForCustomer(String userId) {
    GenericResponse response = new GenericResponse();
    logger.info("======findCustomerSPlanDetails method invoked===========");
    try {
      SPlanResponseBean sPlanResponseBean = new SPlanResponseBean();
      String countryName = this.masterSPlanRepo.getBusinessCountry(userId);
      logger.info("======findCustomerSPlanDetails method invoked for country:===========" + countryName);
      List<NimaiMSubscription> custSPlanList = this.masterSPlanRepo.findByCustomerType("Customer", countryName);
      System.out.println(custSPlanList.toString());
      if (!custSPlanList.isEmpty()) {
        List<customerSPlansResponse> custSubscriptionBean = ModelMapper.mapCustSplanListToSBeanRsponse(custSPlanList);
        sPlanResponseBean.setCustomerSplans(custSubscriptionBean);
      } else {
        sPlanResponseBean = null;
      }
      if (sPlanResponseBean != null) {
        response.setData(sPlanResponseBean);
        return new ResponseEntity(response, HttpStatus.OK);
      }
      response.setErrCode("ASA012");
      response.setErrMessage(ErrorDescription.getDescription("ASA012"));
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setErrMessage("No entity Found");
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }

  public ResponseEntity<?> checkForSubsidiary(SubscriptionBean subscriptionRequest) {
    GenericResponse response = new GenericResponse();
    String userId = subscriptionRequest.getUserId();
    Calendar cal = Calendar.getInstance();
    Date today = cal.getTime();
    int addOnCredit = 0, days = 0;
    try {
      if (subscriptionRequest.getSubscriptionId() != null) {
        Optional<NimaiMCustomer> mCustomer = this.userRepository.findByUserId(userId);
        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
        if (mCustomer.isPresent()) {
          List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllByUserId(userId);
          if (!subscriptionEntity.isEmpty()) {
            for (NimaiSubscriptionDetails plan : subscriptionEntity) {
              if (plan.getSubsidiaryUtilizedCount() >
                      Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue())
                if (userId.substring(0, 2).equalsIgnoreCase("CU")) {
                  response.setStatus("Failure");
                  response.setErrMessage("You had already Active Subsidiary. Kindly select appropriate Plan.");
                  return new ResponseEntity(response, HttpStatus.OK);
                }
              if ((plan.getSubscriptionEndDate().after(today) || plan
                      .getSubscriptionEndDate().compareTo(today) <= 0) &&
                      Integer.valueOf(plan.getlCount()).intValue() - plan.getLcUtilizedCount() > 0) {
                if (plan.getSubscriptionEndDate().compareTo(today) <= 0) {
                  days = (int) ((plan.getSubscriptionEndDate().getTime() - today.getTime()) / 86400000L);
                } else {
                  days = (int) ((plan.getSubscriptionEndDate().getTime() - today.getTime()) / 86400000L) + 1;
                }
                addOnCredit = Integer.valueOf(plan.getlCount()).intValue() - plan.getLcUtilizedCount();
                System.out.println("addOnCredit:" + addOnCredit);
              }
            }
          } else {
            NimaiSubscriptionDetails inactiveSubscriptionEntity = this.subscriptionDetailsRepository.findOnlyLatestInactiveSubscriptionByUserId(userId);
            if (inactiveSubscriptionEntity == null) {
              response.setStatus("Success");
              return new ResponseEntity(response, HttpStatus.OK);
            }
            int noOfDays = (int) ((today.getTime() - inactiveSubscriptionEntity.getSubscriptionEndDate().getTime()) / 86400000L);
            System.out.println("Diff between exp and current date: " + noOfDays);
            if (inactiveSubscriptionEntity.getSubsidiaryUtilizedCount() >=
                    Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue() && userId.substring(0, 2).equalsIgnoreCase("CU")) {
              response.setStatus("Failure");
              response.setErrMessage("You had already Active Subsidiary. Kindly select appropriate Plan.");
              return new ResponseEntity(response, HttpStatus.OK);
            }
            if (noOfDays < 60 && Integer.valueOf(inactiveSubscriptionEntity.getlCount()).intValue() - inactiveSubscriptionEntity
                    .getLcUtilizedCount() > 0)
              addOnCredit = Integer.valueOf(inactiveSubscriptionEntity.getlCount()).intValue() - inactiveSubscriptionEntity.getLcUtilizedCount();
          }
        } else {
          response.setStatus("Failure");
          response.setErrCode("ASA003");
          response.setErrMessage(ErrorDescription.getDescription("ASA003"));
          return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }
      } else {
        response.setStatus("Failure");
        response.setErrCode("ASA009");
        response.setErrMessage(ErrorDescription.getDescription("ASA009"));
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
    response.setStatus("Success");
    return new ResponseEntity(response, HttpStatus.OK);
  }

  public ResponseEntity<?> insertGrandAmountData(CustomerSubscriptionGrandAmountBean subscriptionRequest) {
    GenericResponse response = new GenericResponse();
    NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
    ncsgm.setUserId(subscriptionRequest.getUserId());
    ncsgm.setGrandAmount(subscriptionRequest.getGrandAmount());
    ncsgm.setDiscountApplied("");
    ncsgm.setInsertedDate(new Date());
    this.nimaiCustomerGrandAmtRepository.save(ncsgm);
    response.setStatus("Success");
    response.setData(ncsgm.getId());
    return new ResponseEntity(response, HttpStatus.OK);
  }

  public NimaiCustomerSubscriptionGrandAmount getCustomerAmount(String userId) {
    NimaiCustomerSubscriptionGrandAmount userDet = this.nimaiCustomerGrandAmtRepository.getDetByUserId(userId);
    return userDet;
  }

  public boolean checkPaymentData(int id, Double amt) {
    NimaiCustomerSubscriptionGrandAmount userDet = this.nimaiCustomerGrandAmtRepository.getDetByIdAndAmt(id, amt);
    if (userDet != null)
      return true;
    return false;
  }

  public Map<String, Object> completePayment(Payment payment, String paymentId) {
    String[] planSplit = ((Transaction) payment.getTransactions().get(0)).getCustom().split("-", 5);
    System.out.println("Length: " + planSplit.length);
    System.out.println("planSplit[0] :" + planSplit[0]);
    System.out.println("planSplit[1] :" + planSplit[1]);
    System.out.println("planSplit[2] :" + planSplit[2]);
    String discId = "";
    if (planSplit.length == 4) {
      discId = planSplit[3];
      System.out.println("planSplit[3] :" + planSplit[3]);
    }
    if (planSplit.length == 5) {
      discId = planSplit[4];
      System.out.println("planSplit[4] :" + planSplit[4]);
    }
    String vasPrice = "", discountPrice = "";
    if (((Transaction) payment.getTransactions().get(0)).getItemList().getItems().size() == 2) {
      if (((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(1)).getName().equalsIgnoreCase("VAS")) {
        vasPrice = ((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(1)).getPrice();
      } else {
        vasPrice = "0";
      }
      if (((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(1)).getName().equalsIgnoreCase("Discount")) {
        discountPrice = ((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(1)).getPrice().substring(1);
      } else {
        discountPrice = "0";
      }
    }
    if (((Transaction) payment.getTransactions().get(0)).getItemList().getItems().size() == 3) {
      if (((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(1)).getName().equalsIgnoreCase("VAS")) {
        vasPrice = ((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(1)).getPrice();
      } else {
        vasPrice = "0";
      }
      if (((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(2)).getName().equalsIgnoreCase("Discount")) {
        discountPrice = ((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(2)).getPrice().substring(1);
      } else {
        discountPrice = "0";
      }
    }
    String merchantP1 = planSplit[0];
    String merchantP2 = planSplit[1];
    String merchantP3 = planSplit[2];
    String merchantP4 = vasPrice + "-" + discountPrice;
    String merchantP5 = ((Item) ((Transaction) payment.getTransactions().get(0)).getItemList().getItems().get(0)).getPrice();
    NimaiMSubscription subsDetail = getPlanDetailsBySubscriptionId(merchantP2);
    Map<String, Object> response = new HashMap<>();
    response.put("subscriptionId", merchantP2);
    response.put("orderId", paymentId.substring(paymentId.lastIndexOf("-") + 1));
    response.put("custSupport", subsDetail.getCustomerSupport());
    response.put("lcCount", subsDetail.getlCount());
    response.put("relManager", subsDetail.getRelationshipManager());
    response.put("subsAmount", "" + ((Transaction) payment.getTransactions().get(0)).getAmount().getTotal());
    response.put("subsName", subsDetail.getSubscriptionName());
    response.put("subsValidity", String.valueOf(subsDetail.getSubscriptionValidity()));
    response.put("subsidiaries", subsDetail.getSubsidiaries());
    response.put("userId", merchantP1);
    response.put("subsflag", merchantP3);
    response.put("actualAmt", merchantP5);
    response.put("paymentMode", "Credit");
    response.put("userId", merchantP1);
    response.put("discAmount", discountPrice);
    response.put("discId", discId);
    response.put("vasAmount", vasPrice);
    response.put("actualAmt", merchantP5);
    return response;
  }

  public void saveData(String orderId, String sts) throws IOException {
    Order order1 = null;
    OrdersGetRequest request = new OrdersGetRequest(orderId);
    HttpResponse<Order> responsePaypal = Credentials.client.execute((HttpRequest) request);
    order1 = (Order) responsePaypal.result();
    System.out.println("Details: " + ((PurchaseUnit) order1.purchaseUnits().get(0)).referenceId());

    String[] planSplit = ((PurchaseUnit) order1.purchaseUnits().get(0)).referenceId().split(",", 6);
    System.out.println("Length: " + planSplit.length);
    System.out.println("planSplit[0] :" + planSplit[0]);
    System.out.println("planSplit[1] :" + planSplit[1]);
    System.out.println("planSplit[2] :" + planSplit[2]);
    System.out.println("planSplit [3] :" + planSplit[3]);
    System.out.println("planSplit[4] :" + planSplit[4]);
    System.out.println("planSplit[5] :" + planSplit[5]);
    //29NOV2022
    /*String[] planSplit = ((PurchaseUnit) order1.purchaseUnits().get(0)).referenceId().split("-", 5);
    System.out.println("Length: " + planSplit.length);
    System.out.println("planSplit[0] :" + planSplit[0]);
    System.out.println("planSplit[1] :" + planSplit[1]);
    System.out.println("planSplit[2] :" + planSplit[2]);*/

    String merchantP1 = planSplit[0].substring(planSplit[0].indexOf("=") + 1);
    OnlinePayment op = new OnlinePayment();
    op.setUserId(merchantP1);
    op.setOrderId(orderId);
    op.setAmount(Double.valueOf(((PurchaseUnit) order1.purchaseUnits().get(0)).amountWithBreakdown().value()));
    op.setCurrency("USD");
    op.setInvoiceId(((PurchaseUnit) order1.purchaseUnits().get(0)).invoiceId());
    op.setTransactionId(((Capture) ((PurchaseUnit) order1.purchaseUnits().get(0)).payments().captures().get(0)).id());
    if (sts.equalsIgnoreCase("Failed")) {
      op.setStatus("Failed");
    } else {
      op.setStatus("Approved");
    }
    op.setInsertedBy(merchantP1);
    Date now = new Date();
    op.setInsertedDate(now);
    op.setModifiedBy(merchantP1);
    this.onlinePaymentRepo.save(op);
    if (op.getStatus().equalsIgnoreCase("Approved"))
      this.userRepository.updatePaymentStatus("Approved", op.getOrderId(), op.getUserId());
    NimaiMCustomer customer = this.userRepository.findCustomerDetailsByUserId(op.getUserId());
   
    String subType="";
	 if(customer.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE) && customer.getBankType().equalsIgnoreCase(AppConstants.CUBANKTYPE)) {
			subType=AppConstants.CLIENTBASUBTYPE;
		}
    if(customer.getPaymentStatus().equalsIgnoreCase("Approved") && customer.getKycStatus().equalsIgnoreCase("Approved")
			&&	(customer.getSubscriberType().equalsIgnoreCase("CUSTOMER") || subType.equalsIgnoreCase("BANK_CUSTOMER")) ) {
      String resut;
	try {
		resut = postDataProcess(customer);
		System.out.println("===============Third Party Api response"+resut);
	} catch (Exception e) {
		
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
			}
  }

  
  
  public String postDataProcess(NimaiMCustomer customer) throws Exception {
		try {
			 System.out.println("===============++++++++++postDataProcess 1");
		String countryCode;
		if(customer.getCountryName()==null || customer.getCountryName().isEmpty() || customer.getCountryName().equalsIgnoreCase(" ")) {
			countryCode=AppConstants.VALUABSENT;
		}else {
			countryCode=userRepository.findCountryCode(customer.getCountryName());
			if(countryCode==null || countryCode.equalsIgnoreCase(" ")) {
				countryCode=null;
			}
		}
		
		 if(customer.getAccountType().equalsIgnoreCase(AppConstants.SUBACCTYPE))
		   {
			 //Group of company API
			 System.out.println("===============++++++++++postDataProcess SUBSIDIARY 2");
	  String regType="";
		if(customer.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE)) {
			regType=" ";
		}else {
			if(customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYPEBOTH) ||
					customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTBOTH)) {
				regType=AppConstants.ClIENTREGEXPIMP;
			}else if(customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYIMP)||
					customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYPEIMP)) {
				regType=AppConstants.CLIENTREGIMP;
				
				
			}else if(customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYEXP)
					|| customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYEXPORTER)) {
				regType=AppConstants.CLIENTREGEXP;
			}
		}
		String subType="";
		if(customer.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE) && customer.getBankType().equalsIgnoreCase(AppConstants.BANKTYPE)) {
			subType=AppConstants.CLIENTSUBTYPE;
		}else {
			subType=customer.getSubscriberType();
		}
		String  beDetailsUrl=configRepo.migUrlData(AppConstants.MIGGCURL);
		System.out.println("Migration group of company URL 1"+beDetailsUrl);
			MigrationDataRequestGC requestData=new MigrationDataRequestGC();
			AddressDetailsDto aDeto=new AddressDetailsDto();
			aDeto.setAddress1(customer.getAddress1());
			aDeto.setAddress2(customer.getAddress2());
			aDeto.setAddress3(customer.getAddress3());
			aDeto.setCity(customer.getCity());
			aDeto.setPincode(customer.getPincode());
			aDeto.setProvince(customer.getProvincename());
			aDeto.setRegisteredCountry(customer.getRegisteredCountry());
			aDeto.setTelephone(customer.getTelephone());
			requestData.setAddressDetails(aDeto);
			requestData.setRegistrationType(RegistrationType.valueOf(regType));
			requestData.setSubscriberType(SubscriberType.valueOf(subType));
			requestData.setBankName(customer.getBankName());
			requestData.setCompanyName(customer.getCompanyName());
			requestData.setBranchName(customer.getBranchName());
		//requestData.setDesignation(customer.getDesignation());
			requestData.setSwiftCode(customer.getSwiftCode());
			  ObjectMapper objectMapper = new ObjectMapper();
			    String data = objectMapper
			          .writerWithDefaultPrettyPrinter()
			          .writeValueAsString(requestData);
			    System.out.println("===============++++++++++postDataProcess SUBSIDIARY 3"+data);
		beDetailsUrl= beDetailsUrl+customer.getAccountSource();
			String beUsrApi=tPartApi.postData(requestData, beDetailsUrl);
	
}else {
	//Create user
	String url=configRepo.migUrlData(AppConstants.MIGCREATEUSRURL);
	System.out.println("Cretae user URL 1"+url);
			MigrationUserCreationBean mUsrBean=new MigrationUserCreationBean();
			mUsrBean.setCountry(customer.getCountryName());
			mUsrBean.setUsername(customer.getUserid());
			mUsrBean.setTermsAndPolicyVersion("1.0");
			mUsrBean.setEmail(customer.getEmailAddress());
			mUsrBean.setFirstName(customer.getFirstName());
			mUsrBean.setLastName(customer.getLastName());
			mUsrBean.setLandLineNumber(customer.getLandline());
			mUsrBean.setSubscriberType(customer.getSubscriberType());
			mUsrBean.setMobileNo(customer.getMobileNumber());
			mUsrBean.setCountryExt((countryCode)!=null ? countryCode :"");
		    
		    String crUsrApi=tPartApi.postData(mUsrBean, url);

	String actPassword="";
		if(crUsrApi.equalsIgnoreCase(AppConstants.SUCCESSMSG)) {
			
			String passurl=configRepo.migUrlData(AppConstants.MIGUPDATEPASSUEL);
			  //update password url
			MigrationPasswordBean passBean=new MigrationPasswordBean();
		List<NimaiMLogin> loginList=customer.getNimaiMLoginList();
		loginList.stream().filter(x -> x.getUserid().getUserid().equalsIgnoreCase(customer.getUserid())).findFirst();
		
		for(NimaiMLogin list:loginList) {
			System.out.println(" //password"+loginList.size());
			passBean.setPassword(list.getPassword());
			passBean.setConfirmPassword(list.getPassword());
			passBean.setEmail(customer.getEmailAddress());
			actPassword=list.getPassword();
		}
		System.out.println("update passsword user URL 1"+passurl);
			      String paUsrApi=tPartApi.updatepostData(passBean, passurl);
			    	  if(paUsrApi.equalsIgnoreCase(AppConstants.SUCCESSMSG)) {
				    	  //getMasterToken url
			    		  String tokenUrl=configRepo.migUrlData(AppConstants.MIGMASTERTOKENURL);
				    	  MigrationTokenBean bean=new MigrationTokenBean();
				    	  bean.setLoginTypeEnum(AppConstants.ENUMTYPE);
				    	  bean.setUserName(customer.getUserid());
				    	  bean.setPassword(actPassword);

				    		System.out.println("tokenUrl user URL 1"+tokenUrl);
				    	
				    	   MigrationResponse tokenApi=tPartApi.sendHttpGetRequest(bean, tokenUrl);
				  		      System.out.println("=========token api"+tokenApi.getAccessToken());
				  		      System.out.println("============token api"+tokenApi.getResponse());
				    	  if(tokenApi.getAccessToken()!=null && tokenApi.getResponse().equalsIgnoreCase("Success")) {
				    		  
				    		  System.out.println("//PersonaDetails");
				    		  //PersonalDetails url
				        	  String perUsrUrl=configRepo.migUrlData(AppConstants.MIGPERDETAILSURL);
				    		  MigrationPersonalDetailsDto personalDetails=new MigrationPersonalDetailsDto();
				    		  if((customer.getEmailAddress1()==null || customer.getEmailAddress1().length()==0 || customer.getEmailAddress1().equalsIgnoreCase(""))
				    				  && (customer.getEmailAddress2()==null || customer.getEmailAddress2().length()==0 || customer.getEmailAddress2().equalsIgnoreCase(""))
				    				  && (customer.getEmailAddress3()==null || customer.getEmailAddress3().length()==0 || customer.getEmailAddress3().equalsIgnoreCase("")))
				    		  {
				    			  Set<String> emptySet = Collections.emptySet();
				    			  personalDetails.setAdditionalEmails(emptySet);
				    		  }else {
				    			  Set<String>emailSets = new HashSet<String>();
				    			  emailSets.add(customer.getEmailAddress1());
				    			  emailSets.add(customer.getEmailAddress2());
				    			  emailSets.add(customer.getEmailAddress3());
				    			  personalDetails.setAdditionalEmails(emailSets);
				    		  }
//				    		  personalDetails.setBeneficiaryCountryList(customer.getNimaiFBeneIntcountryList());
//				    		  personalDetails.setBlklstedGoods(customer.getNimaiFBlkgoodsList());
				    		  personalDetails.setCountry(customer.getCountryName());
				    		  personalDetails.setCountryExt("91");
				    		  personalDetails.setCurrency(customer.getCurrencyCode());
				    		  personalDetails.setDesignation(customer.getDesignation());
				    		  personalDetails.setEmail(customer.getEmailAddress());
				    		  personalDetails.setFirstName(customer.getFirstName());
				    		  personalDetails.setLandLineNumber(customer.getLandline());
				    		  personalDetails.setLastName(customer.getLastName());
				    		  if(customer.getMinValueofLc()==null || customer.getMinValueofLc().isEmpty()
				    				  || customer.getMinValueofLc().equalsIgnoreCase("")) {
				    			  personalDetails.setMinLCValue(0.0);
				    		  }else {
				    			  personalDetails.setMinLCValue(Double.parseDouble(customer.getMinValueofLc()));
				    		  }
				    		  
				    		  personalDetails.setMobileNumber(customer.getMobileNumber());
				    		  personalDetails.setSubscriberType(customer.getSubscriberType());
				    		  personalDetails.setUsername(customer.getUserid());
				    			
				    			System.out.println("personal details user URL 1"+perUsrUrl);
				    			
				    			String perUsrApi=tPartApi.postDataWithToken(personalDetails, perUsrUrl,tokenApi.getAccessToken());
				    			
								if(perUsrApi.equalsIgnoreCase(AppConstants.SUCCESSMSG)) {
									//Business details url
									  System.out.println("//Business Details");
									  String beDetailsUrl=configRepo.migUrlData(AppConstants.MIGBUSINDETAILSURL);
									String regType="";
									if(customer.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE)) {
										regType=" ";
									}else {
										if(customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTBOTH) ||
												customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYPEBOTH)) {
											regType=AppConstants.ClIENTREGEXPIMP;
											
											
										}else if(customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYIMP)||
												customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYPEIMP)) {
											regType=AppConstants.CLIENTREGIMP;
											
											
										}else if(customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYEXP)
												|| customer.getRegistrationType().equalsIgnoreCase(AppConstants.REGTYEXPORTER)) {
											regType=AppConstants.CLIENTREGEXP;
										}
									}
								
									String subType="";
									if(customer.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE) && customer.getBankType().equalsIgnoreCase("UNDERWRITER")) {
										subType=AppConstants.CLIENTSUBTYPE;
									}else {
										subType=customer.getSubscriberType();
									}
						
										MigrationDataRequest requestData=new MigrationDataRequest();
										AddressDetailsDto aDeto=new AddressDetailsDto();
										aDeto.setAddress1(customer.getAddress1());
										aDeto.setAddress2(customer.getAddress2());
										aDeto.setAddress3(customer.getAddress3());
										aDeto.setCity(customer.getCity());
										aDeto.setPincode(customer.getPincode());
										aDeto.setProvince(customer.getProvincename());
										aDeto.setRegisteredCountry(customer.getRegisteredCountry());
										aDeto.setTelephone(customer.getTelephone());
										requestData.setAddressDetails(aDeto);
										  Set<EntityEmployeeDto> emptySet = Collections.emptySet();
										  Set<EntityEmployeeDto> empDet = new HashSet<EntityEmployeeDto>();
										  EntityEmployeeDto empDto=new EntityEmployeeDto();
										  empDto.setFirstName(customer.getFirstName());
										  empDto.setLastName(customer.getLastName());
										  empDto.setDesignation(customer.getLastName());
										  empDet.add(empDto);
										  requestData.setEmplyeeDetails(empDet);
										  if(customer.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE)) {
											  requestData.setRegistrationType(null);
										  }else {
											  requestData.setRegistrationType(RegistrationType.valueOf(regType));
										  }
											requestData.setSubscriberType(SubscriberType.valueOf(subType));
										requestData.setBankName(customer.getBankName());
										requestData.setCompanyName(customer.getCompanyName());
										requestData.setBranchName(customer.getBranchName());
									//requestData.setDesignation(customer.getDesignation());
										requestData.setSwiftCode(customer.getSwiftCode());
										System.out.println("Business details url"+beDetailsUrl);
										 ObjectMapper objectMapper = new ObjectMapper();
										    String data = objectMapper
										          .writerWithDefaultPrettyPrinter()
										          .writeValueAsString(requestData);
										    System.out.println("===============++++++++++json String postDataWithToken"+requestData);
										 String beUsrApi=tPartApi.postDataWithToken(requestData, beDetailsUrl,tokenApi.getAccessToken());
									//String beDetailsUrl="https://api.devapp.360tf.trade/user-management/v2/details/business";
								}
				    	  }  
			    	  }
			      return AppConstants.SUCCESSMSG;
		}
}
	return AppConstants.FAILMSG;
		}catch(Exception e) {
			e.printStackTrace();
			 return AppConstants.FAILMSG;
		}
	
	    
	}
  
  public void saveDataPostpaid(String orderId, String sts) throws IOException {
	    Order order1 = null;
	    OrdersGetRequest request = new OrdersGetRequest(orderId);
	    HttpResponse<Order> responsePaypal = Credentials.client.execute((HttpRequest) request);
	    order1 = (Order) responsePaypal.result();
	    System.out.println("Details: " + ((PurchaseUnit) order1.purchaseUnits().get(0)).referenceId());

	    String[] planSplit = ((PurchaseUnit) order1.purchaseUnits().get(0)).referenceId().split(",", 6);
	    System.out.println("Length: " + planSplit.length);
	    System.out.println("planSplit[0] :" + planSplit[0]);
	    System.out.println("planSplit[1] :" + planSplit[1]);
	    System.out.println("planSplit[2] :" + planSplit[2]);
	    System.out.println("planSplit [3] :" + planSplit[3]);
	    System.out.println("planSplit[4] :" + planSplit[4]);
	    System.out.println("planSplit[5] :" + planSplit[5]);
	    //29NOV2022
	    /*String[] planSplit = ((PurchaseUnit) order1.purchaseUnits().get(0)).referenceId().split("-", 5);
	    System.out.println("Length: " + planSplit.length);
	    System.out.println("planSplit[0] :" + planSplit[0]);
	    System.out.println("planSplit[1] :" + planSplit[1]);
	    System.out.println("planSplit[2] :" + planSplit[2]);*/

	    String merchantP1 = planSplit[0].substring(planSplit[0].indexOf("=") + 1);
	    OnlinePayment op = new OnlinePayment();
	    op.setUserId(merchantP1);
	    op.setOrderId(orderId);
	    op.setAmount(Double.valueOf(((PurchaseUnit) order1.purchaseUnits().get(0)).amountWithBreakdown().value()));
	    op.setCurrency("USD");
	    op.setInvoiceId(((PurchaseUnit) order1.purchaseUnits().get(0)).invoiceId());
	    op.setTransactionId(((Capture) ((PurchaseUnit) order1.purchaseUnits().get(0)).payments().captures().get(0)).id());
	    if (sts.equalsIgnoreCase("Failed")) {
	      op.setStatus("Failed");
	    } else {
	      op.setStatus("Approved");
	    }
	    op.setInsertedBy(merchantP1);
	    Date now = new Date();
	    op.setInsertedDate(now);
	    op.setModifiedBy(merchantP1);
	    this.onlinePaymentRepo.save(op);
	    if (op.getStatus().equalsIgnoreCase("Approved"))
	      this.userRepository.updatePaymentStatus("Approved", op.getOrderId(), op.getUserId());
	    
	    //Logic to update data related to postpaid
	    
	    System.out.println("Logs at initiatePaymentForPostpaid ::" );
	    System.out.println(planSplit[1].substring(planSplit[1].indexOf("-")+1));
	    String tid=planSplit[1].substring(planSplit[1].indexOf("-")+1);
	    System.out.println("tid: "+tid);
	    System.out.println(planSplit[0].substring(planSplit[0].indexOf("=")+1));
	    String userid=planSplit[0].substring(planSplit[0].indexOf("=")+1);
	    System.out.println("userId: "+userid);
	    String transactionId =  tid;
	    NimaiPostpaidSubscriptionDetails nimaiPostpaidSubscriptionDetails = this.postpaidSPlanRepository.findUserByUserIdAndTransactionId(userid,transactionId);
	    System.out.println("Logs at initiatePaymentForPostpaid ::"+ nimaiPostpaidSubscriptionDetails.getTransactionId() );
	    NimaiSubscriptionDetails subscriptionDetails = this.subscriptionDetailsRepository.findByUserId(userid);
	    System.out.println("Logs at  initiatePaymentForPostpaid subscriptionDetails ::"+ subscriptionDetails.getSubscriptionName());
	    NimaiPostpaidSubscriptionDetails npsp = postpaidSPlanRepository.getOne(nimaiPostpaidSubscriptionDetails.getPostpaidId());
	    System.out.println("Logs at npsp ::"+ npsp.getTotalPayment());
	    List<NimaiSubscriptionVas> vasList = null;
	    vasList = this.nimaiSubscriptionVasRepo.findActiveVASByUserId(userid);
	    String[] newKeyValueDetails = null;
	    String amountFieldGetKey = "";
	    newKeyValueDetails = planSplit[4].split(":");
	    System.out.println("newKeyValueDetails: "+newKeyValueDetails);
	    String type=planSplit[4].substring(planSplit[4].indexOf("=")+1, planSplit[4].indexOf(":"));
	    System.out.println("----DueType: "+type);
	    
	    String str=planSplit[4];
		String str1=str.substring(str.indexOf("=")+1);
		String str2=str1.substring(str1.indexOf(":")+1);
		System.out.println(""+str1);
		System.out.println("New Amount: "+str2);
		
	    Double finalPayment= 0.0;
	    if(Objects.isNull(nimaiPostpaidSubscriptionDetails.getModeOfPayment())){
	      npsp.setModeOfPayment("Credit");
	      npsp.setStatus("ACTIVE");
	      npsp.setPaymentStatus("Pending");
	      postpaidSPlanRepository.save(npsp);
	    }//else if(nimaiPostpaidSubscriptionDetails.getModeOfPayment().equalsIgnoreCase("Credit") &&  (nimaiPostpaidSubscriptionDetails.getPaymentStatus().equalsIgnoreCase("Pending") || (nimaiPostpaidSubscriptionDetails.getPaymentStatus().equalsIgnoreCase("Maker Approved")))){
	     // response.put("reject","reject");
	     // return response;
	    //}
	    Double vasAmount=0.0;
	    System.out.println("Logs at initiatePaymentForPostpaid npsp setPaymentStatus ::"+ npsp.getPaymentStatus());
	    double payment = 0.0;
	    Double discount=0.0;
	    String invoiceId = generatePaymentTtransactionID(10);
	    String paymentTrId = generatePaymentTtransactionID(15);
	   //if (npsp.getModeOfPayment().equalsIgnoreCase("Credit") && (npsp.getPaymentStatus().equalsIgnoreCase("Pending") || npsp.getPaymentStatus().equalsIgnoreCase("Maker Approved"))) {
	      for (NimaiSubscriptionVas vasPlan : vasList) {
	        vasAmount=vasAmount+vasPlan.getPricing();
	        System.out.println("Vas Amount ="+ vasAmount);
	      }
	      String dueType=amountFieldGetKey.substring(amountFieldGetKey.indexOf("=")+1);
	      System.out.println("Value is as new  initiatePaymentForPostpaid ::" + newKeyValueDetails);
	      amountFieldGetKey = newKeyValueDetails[0].toString();
	      System.out.println("Dues Type is initiatePaymentForPostpaid ::" + dueType);
	      System.out.println("Value is initiatePaymentForPostpaid ::" + newKeyValueDetails);
	      amountFieldGetKey = newKeyValueDetails[0].toString();
	      System.out.println("Dues is initiatePaymentForPostpaid ::" + dueType);
	      if (type.equalsIgnoreCase("minDue")) 
	      {
	    	  System.out.println("It's minDue");
	        payment = this.postpaidSPlanRepository.findMinDueByPaymentCounter(userid, transactionId);
	        try {
	          discount=subscriptionDetails.getDiscount();
	          vasAmount= Double.valueOf(subscriptionDetails.getVasAmount());
	        }catch (Exception ex){
	          ex.printStackTrace();
	        }
	        finalPayment = payment-discount+vasAmount;
	        System.out.println("payFor API1 : finalPayment"+ finalPayment);
	        npsp.setDue_type(type);
	        npsp.setPaymentStatus("Approved");
	        System.out.println("Discount Id is initiatePaymentForPostpaid ::" + subscriptionDetails.getDiscountId());
	        System.out.println("Discount Amount is initiatePaymentForPostpaid ::" + subscriptionDetails.getDiscount());
	        try {
	        	NimaiSubscriptionDetails nsdUpd=subscriptionDetailsRepository.getOne(subscriptionDetails.getsPlSerialNUmber());
	          npsp.setDisountId(subscriptionDetails.getDiscountId());
	          npsp.setDiscountAmnt(subscriptionDetails.getDiscount());
	          npsp.setTotalPayment(finalPayment);
	          nsdUpd.setInvoiceId(invoiceId);
	          npsp.setInvoiceId(subscriptionDetails);
	          npsp.setPaymentTxnId(paymentTrId);
	          nsdUpd.setPaymentStatus("Approved");
	          nsdUpd.setPaymentMode("Credit");
	          postpaidSPlanRepository.save(npsp);
	          //nsdUpd.setVasAmount(Integer.valueOf(vasAmount));
	          nsdUpd.setGrandAmount(npsp.getTotalPayment());
	          subscriptionDetailsRepository.save(nsdUpd);
	          System.out.println("Updating credit of: "+subscriptionDetails.getUserid().getUserid());
	          subscriptionDetailsRepository.updateLCUtilzedPostPaid(subscriptionDetails.getUserid().getUserid());
	        }catch (Exception ex){
	          ex.printStackTrace();
	        }
	      } else {
	    	  System.out.println("It's total due");
	        List transactionIdsFromQuotation = this.postpaidSPlanRepository.findPendingTransactionIdsFromQuotationOnly(userid);
	        List<NimaiPostpaidSubscriptionDetails> nn = this.postpaidSPlanRepository.findPendingTransactionIdsFromQuotation(transactionIdsFromQuotation);
	        payment = this.postpaidSPlanRepository.findSumOfTotalDue(userid);
	        try {
	          discount = subscriptionDetails.getDiscount();
	          vasAmount = Double.valueOf(subscriptionDetails.getVasAmount());
	        } catch (Exception ex) {
	          ex.printStackTrace();
	        }
	        finalPayment = payment - discount + vasAmount;
	        System.out.println("payFor API2 : finalPayment" + finalPayment);
	        for (NimaiPostpaidSubscriptionDetails nv : nn) {
	          if (Objects.isNull(nv.getDue_type())) {
	            nv.setDue_type(type);
	            nv.setModeOfPayment("Credit");
	            nv.setPaymentStatus("Approved");
	            System.out.println("Discount id is :" + subscriptionDetails.getDiscountId());
	            try {
	            	NimaiSubscriptionDetails nsdUpd=subscriptionDetailsRepository.getOne(subscriptionDetails.getsPlSerialNUmber());
	              nv.setDisountId(subscriptionDetails.getDiscountId());
	              //nv.setPaymentStatus("Pending");
	              nv.setDiscountAmnt(subscriptionDetails.getDiscount());
	              nv.setTotalPayment(Double.valueOf(str2));
	              nsdUpd.setInvoiceId(invoiceId);
	              System.out.println("Invoice Id:"+ nsdUpd.getInvoiceId());
	              nv.setInvoiceId(nsdUpd);
	              nv.setPaymentTxnId(paymentTrId);
	              postpaidSPlanRepository.save(npsp);
	              nsdUpd.setVasAmount(vasAmount.intValue());
	              nsdUpd.setGrandAmount(npsp.getTotalPayment());
	              subscriptionDetailsRepository.save(nsdUpd);
	            } catch (Exception ex) {
	              ex.printStackTrace();
	            }

	          }
	          System.out.println("Updating credit of: "+subscriptionDetails.getUserid().getUserid());
	          subscriptionDetailsRepository.updateLCUtilzedPostPaid(subscriptionDetails.getUserid().getUserid());
	        }
	      }
	   // }
	    System.out.println("Details:" + userid + "::" + paymentTrId + "::" + invoiceId + "::" + finalPayment + "::"+vasAmount);
	    int vas=vasAmount.intValue();
	    System.out.println("vasAmount converted value: "+vas);
	    subscriptionDetailsRepository.updatePaymentTxnIdForWire(userid, paymentTrId, invoiceId, Double.valueOf(str2).toString());
	    userRepository.updatePaymentTransactionId(userid, invoiceId);
	    userRepository.updatePaymentMode(npsp.getModeOfPayment(), userid);
	    nimaiSubscriptionVasRepo.updatePaymentTransactionId(userid,invoiceId);
	    NimaiTransactionViewCount newViewCount = new
				NimaiTransactionViewCount();
		newViewCount.setUserId(userid);
		newViewCount.setAfterAccepted(0);
		newViewCount.setAcceptedFlag(0);
		newViewCount.setBeforeAccepted(0);
		newViewCount.setInsertDate(new Date());
		this.viewCountRepo.save(newViewCount);
	    
	  }

  
  public List<SubscriptionAndPaymentBean> getLastPurchasedPlan(String userId,String planType) throws ParseException {
	    List<SubscriptionAndPaymentBean> sp = new ArrayList<>();
	    DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    try {
	    	   	
	    	if(planType.equalsIgnoreCase("PostPaid")) {
	    		List<NimaiPostpaidSubscriptionDetails> ppl = this.postpaidSPlanRepository.getPreviousPostPaidSubscription(userId);
	    	     System.out.println("========size 1"+ppl.size());
	    	      for (NimaiPostpaidSubscriptionDetails d : ppl) {
	    	          System.out.println("============= " + d);
	    	          SubscriptionAndPaymentBean s = new SubscriptionAndPaymentBean();
                    s.setInvoiceId(this.postpaidSPlanRepository.getInvoiceIdByPostPaidId(d.getPostpaidId()));
                    s.setInsertedDate(d.getPostpaidStartDate());
	    	          s.setPaymentStatus(d.getPaymentStatus());
	    	          s.setSplSerialNo(d.getPostpaidId());
	    	          sp.add(s);
	    	        }
	    	}else {
	    		List<Object[]>  spb = this.subscriptionDetailsRepository.getPreviousSubscription(userId);
//	    	    	System.out.println("list: " + spb.toString());
	    	        for (Object[] result : spb) {
	    	          System.out.println("============= " + Arrays.toString(result));
	    	          SubscriptionAndPaymentBean s = new SubscriptionAndPaymentBean();
	    	          s.setInvoiceId(String.valueOf(result[0]));
	    	          s.setInsertedDate((java.util.Date)result[1]);
	    	          s.setPaymentStatus(String.valueOf(result[2]));
	    	          s.setSplSerialNo((Integer) result[3]);
	    	          sp.add(s);
	    	        } 
	    	}

	    } catch (Exception e) {
	      System.out.println(e);
	    }
	    return sp;
	  }

  @Override
  public ResponseEntity<?> saveUserPostPaidSPlan(SubscriptionBean subscriptionRequest, String flagged, String userID) {
    GenericResponse response = new GenericResponse();
    String paymentTrId = "";
    logger.info(" ================ Send saveUserPostPaidSPlan method Invoked ================");
    try {
      if (subscriptionRequest.getSubscriptionId() != null) {
        Optional<NimaiMCustomer> mCustomer = this.userRepository.findByUserId(userID);
        Optional<NimaiMCustomer> subCustomer = this.userRepository.findByUserId(userID);
        if (mCustomer.isPresent()) {
          List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllByUserId(userID);
          if (!subscriptionEntity.isEmpty())
            for (NimaiSubscriptionDetails plan : subscriptionEntity) {
              plan.setStatus("ACTIVE");
              this.subscriptionDetailsRepository.save(plan);

            }
          NimaiSubscriptionDetails subScriptionDetails = new NimaiSubscriptionDetails();
          NimaiEmailScheduler schedularData = new NimaiEmailScheduler();
          subScriptionDetails.setSubscriptionName("POSTPAID_PLAN");
          subScriptionDetails.setUserid(mCustomer.get());
          subScriptionDetails.setSubscriptionValidity(subscriptionRequest.getSubscriptionValidity());
          subScriptionDetails.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          subScriptionDetails.setRemark(subscriptionRequest.getRemark());
          subScriptionDetails.setSubscriptionAmount(subscriptionRequest.getSubscriptionAmount());
          subScriptionDetails.setlCount(subscriptionRequest.getLcCount());
          subScriptionDetails.setSubsidiaries(subscriptionRequest.getSubsidiaries());
          subScriptionDetails.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          subScriptionDetails.setCustomerSupport(subscriptionRequest.getCustomerSupport());
          subScriptionDetails.setIsVasApplied(subscriptionRequest.getIsVasApplied());
          subScriptionDetails.setVasAmount(subscriptionRequest.getVasAmount());
          subScriptionDetails.setDiscountId(subscriptionRequest.getDiscountId());
          subScriptionDetails.setDiscount(subscriptionRequest.getDiscount());
          subScriptionDetails.setGrandAmount(subscriptionRequest.getGrandAmount());
          subScriptionDetails.setInsertedBy(((NimaiMCustomer) mCustomer.get()).getFirstName());
          subScriptionDetails.setsPLanCountry(((NimaiMCustomer) mCustomer.get()).getAddress3());
          subScriptionDetails.setInsertedDate(new Date());
          subScriptionDetails.setPaymentStatus("Pending");
          String customerType = subscriptionRequest.getUserId().substring(0, 2);
          if (customerType.equalsIgnoreCase("BA")) {
            subScriptionDetails.setCustomerType("Bank");
          } else {
            subScriptionDetails.setCustomerType("Customer");
          }
          SPlanUniqueNumber endDate = new SPlanUniqueNumber();
          int year = endDate.getNoOfyears(subScriptionDetails.getSubscriptionValidity());
          int month = endDate.getNoOfMonths(subScriptionDetails.getSubscriptionValidity());
          System.out.println(year);
          System.out.println(month);
          subScriptionDetails.setStatus("ACTIVE");
          Calendar cal = Calendar.getInstance();
          Date today = cal.getTime();
          cal.add(1, year);
          cal.add(2, month);
          Date sPlanEndDate = cal.getTime();
          subScriptionDetails.setSubscriptionStartDate(today);
          subScriptionDetails.setSubscriptionEndDate(sPlanEndDate);
          subScriptionDetails.setRenewalEmailStatus("Pending");
          NimaiSubscriptionDetails subScription=null;
         if(userID.equalsIgnoreCase(subScriptionDetails.getUserid().getUserid())) {
        	 System.out.println("userID matches: "+userID);
        	 System.out.println("subScriptionDetails.getUserid().getUserid(): "+subScriptionDetails.getUserid().getUserid());
              subScription = (NimaiSubscriptionDetails) this.subscriptionDetailsRepository.save(subScriptionDetails);
         }else{
        	 System.out.println("userID  doen't matches: "+userID);
        	 System.out.println("subScriptionDetails.getUserid().getUserid(): "+subScriptionDetails.getUserid().getUserid());
            subScription= this.subscriptionDetailsRepository.findByUserIdAndStatus(userID);
         }
          // For the postpaid model in
          if(flagged.equalsIgnoreCase("postpaid")){
        	  System.out.println("Inside PostPaid block");
            if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
            	System.out.println("Inside Wire block");
              this.userRepository.updatePostPaidPaymentStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
              this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
              String invoiceId = generatePaymentTtransactionID(10);
              paymentTrId = generatePaymentTtransactionID(15);
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId);
              this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                      .get()).getUserid());
              //Double gstValue = Double.valueOf(this.subscriptionRepo.getGSTValue().doubleValue() / 100.0D);
              //Double planPriceGST = Double.valueOf(subScription.getGrandAmount().doubleValue() + subScription.getGrandAmount().doubleValue() * gstValue.doubleValue());
              //System.out.println("gstValue: " + gstValue);
              //System.out.println("planPriceGST: " + planPriceGST);
              String finalPrice = String.format("%.2f", new Object[]{subScription.getGrandAmount().doubleValue()});
              //this.subscriptionDetailsRepository.updatePaymentTxnIdForWire(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId, invoiceId, finalPrice);
            } else {
              this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                      .get()).getUserid());
              this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
              this.userRepository.updatePaymentStatusForCredit(((NimaiMCustomer) mCustomer.get()).getUserid());
              OnlinePayment paymentDet = this.onlinePaymentRepo.getDetailsByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
              if (subscriptionRequest.getGrandAmount().doubleValue() == 0.0D) {
                String invoiceId = generatePaymentTtransactionID(10);
                this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId);
                this.subscriptionDetailsRepository.updateInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
              } else {
                this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getInvoiceId());
                this.subscriptionDetailsRepository.updatePaymentTxnIdInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getOrderId(), paymentDet.getInvoiceId());
              }
            }
          }else {
            if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
              this.userRepository.updatePaymentStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
              this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
              String invoiceId = generatePaymentTtransactionID(10);
              paymentTrId = generatePaymentTtransactionID(15);
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId);
              this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                      .get()).getUserid());
              //Double gstValue = Double.valueOf(this.subscriptionRepo.getGSTValue().doubleValue() / 100.0D);
              //Double planPriceGST = Double.valueOf(subScription.getGrandAmount().doubleValue() + subScription.getGrandAmount().doubleValue() * gstValue.doubleValue());
              //System.out.println("gstValue: " + gstValue);
              //System.out.println("planPriceGST: " + planPriceGST);
              String finalPrice = String.format("%.2f", new Object[]{subScription.getGrandAmount().doubleValue()});
              this.subscriptionDetailsRepository.updatePaymentTxnIdForWire(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId, invoiceId, finalPrice);
            } else {
              this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                      .get()).getUserid());
              this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
              this.userRepository.updatePaymentStatusForCredit(((NimaiMCustomer) mCustomer.get()).getUserid());
              OnlinePayment paymentDet = this.onlinePaymentRepo.getDetailsByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
              if (subscriptionRequest.getGrandAmount().doubleValue() == 0.0D) {
                String invoiceId = generatePaymentTtransactionID(10);
                this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId);
                this.subscriptionDetailsRepository.updateInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
              } else {
                this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getInvoiceId());
                this.subscriptionDetailsRepository.updatePaymentTxnIdInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getOrderId(), paymentDet.getInvoiceId());
              }
            }
          }// Finish payment area

          schedularData.setUserid(((NimaiMCustomer) mCustomer.get()).getUserid());
          String sPlanValidity = Integer.toString(subscriptionRequest.getSubscriptionValidity());
          String sPlanAmount = Integer.toString(subscriptionRequest.getSubscriptionAmount());
          schedularData.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          schedularData.setCustomerSupport(subscriptionRequest.getCustomerSupport());
         // schedularData.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          schedularData.setSubscriptionAmount(sPlanAmount);
          if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("BA")) {
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(((NimaiMCustomer) mCustomer.get()).getEmailAddress());
          } else if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("CU") || subscriptionRequest
                  .getUserId().substring(0, 2).equalsIgnoreCase("BC")) {
            String emailId = "";
            if (subscriptionRequest.getEmailID() != null) {
              emailId = subscriptionRequest.getEmailID() + "," + ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            } else {
              emailId = ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            }
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(emailId);
          }
          schedularData.setSubscriptionStartDate(today);
         // schedularData.setSubscriptionEndDate(sPlanEndDate);
          schedularData.setSubscriptionName(subscriptionRequest.getSubscriptionName());
          schedularData.setSubscriptionValidity(sPlanValidity);
          schedularData.setEmailStatus("pending");
          schedularData.setEvent("Cust_Splan_email_PostPaid");
          schedularData.setInsertedDate(today);
          NimaiEmailScheduler emailData = (NimaiEmailScheduler) this.emailDetailsRepository.save(schedularData);
          response.setData("Data successfully saved in the table of Subscription Detail ");
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setStatus("Success");
        //response.setErrCode("ASA003");
        response.setErrMessage("Plan Purchased Successfully");
        return new ResponseEntity(response, HttpStatus.OK);
      }
      response.setStatus("Failure");
      response.setErrCode("ASA009");
      response.setErrMessage(ErrorDescription.getDescription("ASA009"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<?> pushPostpaidSPlanPayment(PostpaidSubscriptionBean postpaidSubscriptionRequest, String userID, String txnId, String flag, String amountField) {
    return null;
  }


  // Calculation of the minDue and the totalDue
  public List<Object> calculationAsUserPresent(Double UsdAmount, String userID, String txnId, NimaiPostpaidSubscriptionDetailsUpd nimaiPostpaidSubscriptionDetails, NimaiSubscriptionDetails subscriptionDetails) {

	  System.out.println("----- In calculationAsUserPresent");
	  System.out.println("UsdAmount: "+UsdAmount);
	  System.out.println("userID: "+userID);
	  System.out.println("txnId: "+txnId);
	  System.out.println("nimaiPostpaidSubscriptionDetails: "+nimaiPostpaidSubscriptionDetails);
	  System.out.println("subscriptionDetails: "+subscriptionDetails);
      UsdAmount = subscriptionDetailsRepository.findCurrencyValueByUserIdAndTransactionId(userID, txnId);
      Integer usance;
      String product=" ";
      if(userID.substring(0, 2).equalsIgnoreCase("BA")) {
   	   product = subscriptionDetailsRepository.findBAProductByUserIdAndTransactionId(txnId);	 
     }else {
    	  product = subscriptionDetailsRepository.findProductByUserIdAndTransactionId(userID, txnId);
          
     }
      
      System.out.println("Product: "+product);
      switch(product)
	  {
	      case "ConfirmAndDiscount":
	    	  System.out.println("Getting usance for ConfirmAndDiscount");
	    	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionId(userID, txnId);
	    	  break;
	      case "Discounting":
	    	  System.out.println("Getting usance for Discount");
	    	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionId(userID, txnId);
	    	  break;
	      case "Confirmation":
	    	  System.out.println("Getting usance for Confirmation");
	    	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionId(userID, txnId);
	    	  break;
	      case "Refinance":
	    	  System.out.println("Getting usance for Confirmation");
	    	  usance=subscriptionDetailsRepository.findOriginalTenorByUserIdAndTransactionId(userID, txnId);
	    	  break;
	      case "Banker":
	    	  System.out.println("Getting usance for Confirmation");
	    	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionId(userID, txnId);
	    	  break;
	      case "BankGuarantee":
	    	  System.out.println("Getting usance for Confirmation");
	    	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionId(userID, txnId);
	    	  break;
	      case "BillAvalisation":
	    	  System.out.println("Getting usance for Confirmation");
	    	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionId(userID, txnId);
	    	  break;
	      default:
	    	  usance=0;
      }
      System.out.println("Usance: "+usance);
      LcValue = (((UsdAmount/360)*usance) * Double.parseDouble(fixedRate))/100;
      if (LcValue < Double.parseDouble(configuredValue)) {
        perTransactionDue = Double.parseDouble(configuredValue);
      } else {
        perTransactionDue = LcValue;
      }
      minDue=perTransactionDue;
      totalDue= perTransactionDue;
      transactionCounter++;


    System.out.println("Min Value in UserPresent is ="+minDue);
    if (nimaiPostpaidSubscriptionDetails != null){
      //per transaction due
     // nimaiPostpaidSubscriptionDetails = new NimaiPostpaidSubscriptionDetails();
    }else{

      //per transaction due
    }
   // nimaiPostpaidSubscriptionDetails = new NimaiPostpaidSubscriptionDetails();
    nimaiPostpaidSubscriptionDetails.setMinDue(minDue);
    nimaiPostpaidSubscriptionDetails.setTotalDue(totalDue);
    //per transaction due
    nimaiPostpaidSubscriptionDetails.setPerTransactionDue(perTransactionDue);
    nimaiPostpaidSubscriptionDetails.setTotalPayment(totalPayment);
    nimaiPostpaidSubscriptionDetails.setRemark(subscriptionDetails.getRemark());
    nimaiPostpaidSubscriptionDetails.setPaymentCounter("" + transactionCounter);
    nimaiPostpaidSubscriptionDetails.setPostpaidStartDate(new Date());
    nimaiPostpaidSubscriptionDetails.setStatus(subscriptionDetails.getStatus());
//    nimaiPostpaidSubscriptionDetails.setInvoiceId(subscriptionDetails);
//    nimaiPostpaidSubscriptionDetails.setSubscriptionDetailsId(subscriptionDetails);
//    nimaiPostpaidSubscriptionDetails.setUserId(subscriptionDetails);
   /* if (!txnId.equalsIgnoreCase(nimaiPostpaidSubscriptionDetails.getTransactionId())) {
      nimaiPostpaidSubscriptionDetails.setTransactionId(txnId);
      this.postpaidSPlanRepository.save(nimaiPostpaidSubscriptionDetails);
    } else {
      String val = "Duplicate transactions are not being entered in table ";
      return Arrays.asList(val);
    }*/
   // Double totalDueNew= postpaidSPlanRepository.findSumOfTotalDue(userID);
   // Integer count = postpaidSPlanRepository.findCountTotalDue(userID);
    return Arrays.asList("minDue:"+minDue, "totalDue:"+totalDue, "TotalPayment:"+totalPayment, "perTxnDue:"+perTransactionDue);
  }


  // This method is used to update the unpaidPostpaidSubscriptionPlan
  @Override
  public ResponseEntity<?> unpaidPostpaidSubscriptionPlan(String userID,String txnID) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get Users unpaidPostpaidSPlanPayment method Invoked ================");
    PostpaidSubscriptionBean PostpaidSubscriptionBean = new PostpaidSubscriptionBean();
    SendCalculatedValues send=null;
    try{
      int flag=0;
      String status="";
      String newUsers="";
      List QuotationCount = null;
      String app ="";
      String customerType = this.userRepository.getAccountTypeByUserId(userID);
      if(customerType.equalsIgnoreCase("SUBSIDIARY")) {
        newUsers = this.userRepository.getAccountSourceByUserId(customerType);
        List<NimaiPostpaidSubscriptionDetails> nimPostpaidSubscriptionDetails = this.postpaidSPlanRepository.findDataOfUserByUserIdAndMakerApproved(newUsers);
        if (nimPostpaidSubscriptionDetails.size() > 0) {
          status = "Reject";
          response.setStatus(status);
          return new ResponseEntity(response, HttpStatus.OK);
        } else {
        	System.out.println("else block 1");
          QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
        }
      }else {
    	  System.out.println("else block 2");
        QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
      }
      System.out.println("The Quotation Count:" + QuotationCount);
      String resultUserId="";
      String transactId="";
      String payment1= "";
      String payment2="";
      String tt= "";
      for (final Object txnDet : QuotationCount) {
        resultUserId = (((Object[]) txnDet)[1] == null) ? null : ((Object[]) txnDet)[1].toString();
        transactId = (((Object[]) txnDet)[0] == null) ? null : ((Object[]) txnDet)[0].toString();
        System.out.println("transactionID: "+transactId);
        System.out.println("resultUserId: "+resultUserId);
        System.out.println("userID: "+userID);
        Object transFromPostpaid = this.postpaidSPlanRepository.findUsersDetailsByUserIdAndTransactionId(resultUserId, transactId);
        System.out.println("((Object[]) transFromPostpaid)[0].toString(): "+((Object[]) transFromPostpaid)[0].toString());
        System.out.println("((Object[]) transFromPostpaid)[1].toString(): "+((Object[]) transFromPostpaid)[1].toString());
        
        tt=(((Object[]) transFromPostpaid)[0] == null) ? null : ((Object[]) transFromPostpaid)[0].toString();
        payment1 =(((Object[]) transFromPostpaid)[1] == null) ? null : ((Object[]) transFromPostpaid)[1].toString();
        if (transactId.equalsIgnoreCase(tt) && payment1.equalsIgnoreCase("Pending") || payment1.equalsIgnoreCase("Rejected")){
          flag = 1;
          break;
        }
      }

      if(flag==1){
        send=payCalc(resultUserId,transactId);
      }
      System.out.println("resultUserId:"+resultUserId);
      System.out.println("transactId:"+transactId);
      System.out.println("Details as="+send);

    }
    catch (Exception e) {
        response.setErrMessage("No entity Found");
        response.setData("firstEntry");
        e.printStackTrace();
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
      response.setData(send);
      response.setErrMessage(ErrorDescription.getDescription("ASA001"));
      return new ResponseEntity(response, HttpStatus.OK);

  }

  @Override
  public ResponseEntity<?> getPostpaidFreezePlacedSubscriptionPlan(String userID) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get Users getPostpaidFreezePlacedSubscriptionPlan method Invoked ================");
    PostpaidSubscriptionBean PostpaidSubscriptionBean = new PostpaidSubscriptionBean();
    SendCalculatedValues send = null;
    List<String> QuotationCount;
    try {
      int flag = 0;
      QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTimeAndEqualFreezePlaced(userID);
      System.out.println("The Quotation Count in getPostpaidFreezePlacedSubscriptionPlan:" + QuotationCount);

    } catch (Exception e) {
      response.setErrMessage("No entity Found");
      e.printStackTrace();
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
    response.setData(QuotationCount);
    response.setErrMessage(ErrorDescription.getDescription("ASA001"));
    return new ResponseEntity(response, HttpStatus.OK);

  }

  @Override
  public ResponseEntity<?> getMinAndTotalSubscriptionPlan(String userID) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get Users getMinAndTotalSubscriptionPlan Invoked ================");
    PostpaidSubscriptionBean PostpaidSubscriptionBean = new PostpaidSubscriptionBean();
    SendCalculatedValues send=null;
    try{
      int flag=0;
      List QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
      System.out.println("The Quotation Count:" + QuotationCount);
      String resultUserId="";
      String transactId="";
      for (final Object txnDet : QuotationCount) {
        resultUserId = (((Object[]) txnDet)[2] == null) ? null : ((Object[]) txnDet)[2].toString();
        transactId = (((Object[]) txnDet)[1] == null) ? null : ((Object[]) txnDet)[1].toString();
        NimaiPostpaidSubscriptionDetails transFromPostpaid =this.postpaidSPlanRepository.findUserByUserIdAndTransactionId(userID,transactId);
        if(transactId.equalsIgnoreCase(transFromPostpaid.getTransactionId()) && transFromPostpaid.getPaymentStatus().equalsIgnoreCase("Pending")){
          flag=1;
          break;
        }
      }
      if(flag==1){
       // send=payCalc(userID,transactId);
      }
      System.out.println("resultUserId: "+resultUserId);
      System.out.println("transactId: "+transactId);

    } catch (Exception e) {
      response.setErrMessage("No entity Found");
      e.printStackTrace();
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }

    response.setData(send);
    response.setErrMessage(ErrorDescription.getDescription("ASA001"));
    return new ResponseEntity(response, HttpStatus.OK);
  }

  // in order to update or edit the details in the postpaid model
  @Override
  public ResponseEntity<?> editPostpaidPlanDetails(String userID,String txnID,EditPostpaidBean editBean) {
    GenericResponse response = new GenericResponse();
    List<Object> countDetails = null;
    Double UsdAmount=0.0;
   
    System.out.println("Get users in editPostpaid planDetails........");
    logger.info(" ================ Get Users editPostpaidPlanDetails Invoked in service implementation class ================");

    NimaiSubscriptionDetails subscriptionDetails=this.subscriptionDetailsRepository.findByUserIdAndStatus(userID);
    System.out.println("Get subscription details ......."+ subscriptionDetails);
    if(subscriptionDetails.getStatus().equalsIgnoreCase("INACTIVE")){
      subscriptionDetails.setStatus("ACTIVE");
      subscriptionDetailsRepository.save(subscriptionDetails);
    }
    userID=editBean.getUserId();
    txnID=editBean.getTxnid();
    //NimaiPostpaidSubscriptionDetails PostpaidSubscriptionDetails = this.postpaidSPlanRepository.findUserByUserIdAndTransactionId(userID,txnID);
    NimaiPostpaidSubscriptionDetailsUpd PostpaidSubscriptionDetails = this.postpaidSPlanRepositoryUpd.findUserByUserIdAndTransactionId(userID,txnID);
    
    
    
    System.out.println("Get edit details  ......."+ PostpaidSubscriptionDetails);
    PostpaidSubscriptionBean PostpaidSubscriptionBean = new PostpaidSubscriptionBean();
    try {

        //postpaidSPlanRepository.editPostpaidPlanDetails(userID,txnID,PostpaidSubscriptionDetails.getMinDue(),PostpaidSubscriptionDetails.getPerTransactionDue(),PostpaidSubscriptionDetails.getTotalDue());
        logger.info("======editPostpaidPlanDetails method invoked for userId:===========" + PostpaidSubscriptionDetails.getUserId()+"and Transaction Id :"+  PostpaidSubscriptionDetails.getTransactionId());
        PostpaidSubscriptionDetails =postpaidSPlanRepositoryUpd.getOne(PostpaidSubscriptionDetails.getPostpaidId());
      
        System.out.println("Get Inside details  ......."+ PostpaidSubscriptionDetails);
       
        countDetails=calculationAsUserPresent(UsdAmount, userID, txnID, PostpaidSubscriptionDetails,subscriptionDetails);
        System.out.println("Get After Calculation details  ......."+ countDetails);
        System.out.println("countDetails.get(0): "+countDetails.get(0));
        System.out.println("countDetails.get(1): "+countDetails.get(1));
        String minDue=(String) countDetails.get(0);
        String totDue=(String) countDetails.get(1);
        String perTxn=(String) countDetails.get(3);
        
        String finalMinDue=minDue.substring(minDue.indexOf(":")+1);
        String finalTotalDue=totDue.substring(totDue.indexOf(":")+1);
        String perTxnDue=perTxn.substring(perTxn.indexOf(":")+1);
        userID=editBean.getUserId();
        txnID=editBean.getTxnid();
        PostpaidSubscriptionDetails.setMinDue(Double.valueOf(finalMinDue));
        PostpaidSubscriptionDetails.setTotalDue(Double.valueOf(finalTotalDue));
        PostpaidSubscriptionDetails.setPerTransactionDue(Double.valueOf(perTxnDue));
        postpaidSPlanRepositoryUpd.save(PostpaidSubscriptionDetails);
      //  PostpaidSubscriptionDetails.ser
       // postpaidSPlanRepositoryUpd.updateMinDueTotalDue(userID,txnID,finalMinDue,finalTotalDue,perTxnDue);
       
        PostpaidSubscriptionBean=getPostpaidBean(PostpaidSubscriptionBean,PostpaidSubscriptionDetails,userID,subscriptionDetails);
        System.out.println("Inserting the details on bean   ......."+ PostpaidSubscriptionBean);
        
        //postpaidSPlanRepositoryUpd.updateMinDueTotalDue();
      //  PostpaidSubscriptionDetails.setMinDue();


    } catch (Exception e) {
      e.getStackTrace();
      response.setErrMessage("No entity Found");
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }

    response.setData(countDetails);
    response.setStatus("Success");
    response.setErrMessage("Succesfully Data Updated..");
    return new ResponseEntity(response, HttpStatus.OK);
  }

  public PostpaidSubscriptionBean getPostpaidBean(PostpaidSubscriptionBean PostpaidSubscriptionBean, NimaiPostpaidSubscriptionDetailsUpd PostpaidSubscriptionDetails,String userID, NimaiSubscriptionDetails subscriptionDetails) {

    try {
      logger.info("======getPostpaidBean method invoked for userId:===========" + PostpaidSubscriptionDetails.getUserId());
      System.out.println("getPostpaidBean:" + PostpaidSubscriptionDetails.getUserId());
      System.out.println("getPostpaidBean method subscription detail id:" + subscriptionDetails.getsPlSerialNUmber().toString());
      PostpaidSubscriptionBean.setSubscriptionDetailsId(subscriptionDetails.getsPlSerialNUmber().toString());
      System.out.println("getPostpaidBean method PostpaidId:" + PostpaidSubscriptionDetails.getPostpaidId().toString());
      PostpaidSubscriptionBean.setPostpaidId(PostpaidSubscriptionDetails.getPostpaidId().toString());
      System.out.println("getPostpaidBean method getInvoiceId:" + PostpaidSubscriptionDetails.getInvoiceId() == null ? "10100AB" : PostpaidSubscriptionDetails.getInvoiceId());
      PostpaidSubscriptionBean.setInvoiceId(PostpaidSubscriptionDetails.getInvoiceId() == null ? "10100AB" : PostpaidSubscriptionDetails.getInvoiceId().toString());
      System.out.println("getPostpaidBean method getStatus:" + PostpaidSubscriptionDetails.getStatus());
      PostpaidSubscriptionBean.setStatus(PostpaidSubscriptionDetails.getStatus());
      if (PostpaidSubscriptionDetails.getRemark() == null) {
        PostpaidSubscriptionBean.setRemark("");
        System.out.println("getPostpaidBean method setRemark in if:" + PostpaidSubscriptionDetails.getRemark());
      } else {
        PostpaidSubscriptionBean.setRemark(PostpaidSubscriptionDetails.getRemark());
        System.out.println("getPostpaidBean method setRemark in else:" + PostpaidSubscriptionDetails.getRemark());
      }
      PostpaidSubscriptionBean.setCheckerApprovalBy(PostpaidSubscriptionDetails.getCheckerApprovalBy());
      System.out.println("getPostpaidBean method setCheckerApprovalBy :" + PostpaidSubscriptionDetails.getCheckerApprovalBy());
      PostpaidSubscriptionBean.setMakerApprovalBy(PostpaidSubscriptionDetails.getMakerApprovalBy());
      System.out.println("getPostpaidBean method setMakerApprovalBy :" + PostpaidSubscriptionDetails.getMakerApprovalBy());
      if (PostpaidSubscriptionDetails.getCheckerApprovalDate() == null) {
        PostpaidSubscriptionBean.setCheckerApprovalDate("");
      } else {
        PostpaidSubscriptionBean.setCheckerApprovalDate(PostpaidSubscriptionDetails.getCheckerApprovalDate().toString());
      }

      if (PostpaidSubscriptionDetails.getMakerApprovalDate() == null) {
        PostpaidSubscriptionBean.setMakerApprovalDate("");
      } else {
        PostpaidSubscriptionBean.setMakerApprovalDate(PostpaidSubscriptionDetails.getMakerApprovalDate().toString());
      }
      PostpaidSubscriptionBean.setMinDue(PostpaidSubscriptionDetails.getMinDue());
      System.out.println("getPostpaidBean method getMinDue :" + PostpaidSubscriptionDetails.getMinDue());
      PostpaidSubscriptionBean.setPaymentCounter(PostpaidSubscriptionDetails.getPaymentCounter());
      System.out.println("getPostpaidBean method getPaymentCounter :" + PostpaidSubscriptionDetails.getPaymentCounter());
      PostpaidSubscriptionBean.setPaymentTxnId(PostpaidSubscriptionDetails.getPaymentTxnId());
      System.out.println("getPostpaidBean method getPaymentTxnId :" + PostpaidSubscriptionDetails.getPaymentTxnId());
      PostpaidSubscriptionBean.setPerTransactionDue(PostpaidSubscriptionDetails.getPerTransactionDue());
      System.out.println("getPostpaidBean method getPerTransactionDue :" + PostpaidSubscriptionDetails.getPerTransactionDue());
      PostpaidSubscriptionBean.setPostpaidStartDate(PostpaidSubscriptionDetails.getPostpaidStartDate().toString());
      System.out.println("getPostpaidBean method getPostpaidStartDate :" + PostpaidSubscriptionDetails.getPostpaidStartDate());
      PostpaidSubscriptionBean.setTotalDue(PostpaidSubscriptionDetails.getTotalDue());
      System.out.println("getPostpaidBean method getTotalDue :" + PostpaidSubscriptionDetails.getTotalDue());
      PostpaidSubscriptionBean.setTotalPayment(PostpaidSubscriptionDetails.getTotalPayment());
      System.out.println("getPostpaidBean method getTotalPayment :" + PostpaidSubscriptionDetails.getTotalPayment());
      PostpaidSubscriptionBean.setTransactionId(PostpaidSubscriptionDetails.getTransactionId());
      System.out.println("getPostpaidBean method getTransactionId :" + PostpaidSubscriptionDetails.getTransactionId());
      PostpaidSubscriptionBean.setSubscriptionDetailsId(subscriptionDetails.getsPlSerialNUmber().toString());
      System.out.println("getPostpaidBean method setSubscriptionDetailsId :" + subscriptionDetails.getsPlSerialNUmber().toString());
      PostpaidSubscriptionBean.setUserId(userID);
      System.out.println("getPostpaidBean method setUserId :" + userID);
      System.out.println("PostpaidSubscriptionBean: Min Due " + PostpaidSubscriptionBean.getMinDue());
      System.out.println("PostpaidSubscriptionBean: Total Due " + PostpaidSubscriptionBean.getTotalDue());

    }catch(Exception e){
      e.printStackTrace();
    }
    return PostpaidSubscriptionBean;
  }

@Override
public ResponseEntity<?> pushPostpaidSPlanPayment(String userID, String txnId) {
	// TODO Auto-generated method stub
    System.out.println("Inside pushPostpaidSPlanPayment----------");
	GenericResponse response = new GenericResponse();
    NimaiMCustomer mCustomer = userRepository.findCustomerDetailsByUserId(userID);
    if(!Objects.isNull(mCustomer)) {
      //mCustomer.setUserid(userID);
      String customersType = this.userRepository.getAccountTypeByUserId(userID);
      System.out.println("pushPostpaidSPlanPayment CustomerType:" + customersType);
      NimaiSubscriptionDetails nsd = new NimaiSubscriptionDetails();
      if (customersType.equalsIgnoreCase("MASTER")) {
        System.out.println("calculationAsUserPresent nimaiCustomer userId MASTER :" + mCustomer.getUserid());
        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(mCustomer.getUserid());
        CalculationValue cv = calculateDues(userID, txnId);
        Double UsdAmount = 0.0;
        NimaiPostpaidSubscriptionDetails npsd = new NimaiPostpaidSubscriptionDetails();
        npsd.setMinDue(cv.getMinDue());
        npsd.setPerTransactionDue(cv.getPerTxnDue());
        npsd.setPaymentCounter(cv.getPaymentCounter().toString());
        npsd.setTotalDue(cv.getTotalDue());
        npsd.setTotalPayment(cv.getTotalPayment());
        npsd.setPostpaidStartDate(new Date());
        npsd.setStatus("ACTIVE");
        npsd.setPaymentStatus("Pending");
        npsd.setUserId(details);
        npsd.setEmail_account(cv.getEmail());
        npsd.setTransactionId(txnId);
        npsd.setSubscriptionDetailsId(details);
        npsd.setTotalPayment(0.0);
        postpaidSPlanRepository.save(npsd);
      } else if (customersType.equalsIgnoreCase("SUBSIDIARY")) {
        String subsidary = mCustomer.getUserid();
        System.out.println("calculationAsUserPresent nimaiCustomer userId SUBSIDIARY:" + subsidary);
        String sourceUserID = this.userRepository.getAccountSourceByUserId(subsidary);
        System.out.println("calculationAsUserPresent nimaiCustomer userId SOURCE:" + sourceUserID);
        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(sourceUserID);
        CalculationValue cv = calculateDues(subsidary, txnId);
        Double UsdAmount = 0.0;
        NimaiPostpaidSubscriptionDetails npsd = new NimaiPostpaidSubscriptionDetails();
        npsd.setMinDue(cv.getMinDue());
        npsd.setPerTransactionDue(cv.getPerTxnDue());
        npsd.setPaymentCounter(cv.getPaymentCounter().toString());
        npsd.setTotalDue(cv.getTotalDue());
        npsd.setTotalPayment(cv.getTotalPayment());
        npsd.setPostpaidStartDate(new Date());
        npsd.setStatus("ACTIVE");
        npsd.setPaymentStatus("Pending");
        npsd.setEmail_account(cv.getEmail());
        //details.setUserid(mCustomer);
        System.out.println("Details id need to be set " + sourceUserID);
       // NimaiSubscriptionDetails nimaiSubscriptionDetails = this.postpaidSPlanRepository.updateDetailsId(subsidary,txnId);
       // npsd.setUserId(nimaiSubscriptionDetails);
        npsd.setTransactionId(txnId);
        npsd.setSubscriptionDetailsId(details);
        npsd.setTotalPayment(0.0);
        postpaidSPlanRepository.save(npsd);
        postpaidSPlanRepository.getOne(npsd.getPostpaidId());
        postpaidSPlanRepository.updateUserIdForSubsidary(subsidary,npsd.getPostpaidId());
      }else if (customersType.equalsIgnoreCase("REFER")) {
        System.out.println("calculationAsUserPresent nimaiCustomer userId REFER:" + mCustomer.getUserid());
      //  String sourceUserID = this.userRepository.getAccountSourceByUserId(mCustomer.getUserid());
       // String newUserID = this.userRepository.get(mCustomer.getUserid());
        System.out.println("calculationAsUserPresent nimaiCustomer userId SOURCE:" + userID);
        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(userID);
        CalculationValue cv = calculateDues( mCustomer.getUserid(), txnId);
        Double UsdAmount = 0.0;
        NimaiPostpaidSubscriptionDetails npsd = new NimaiPostpaidSubscriptionDetails();
        npsd.setMinDue(cv.getMinDue());
        npsd.setPerTransactionDue(cv.getPerTxnDue());
        npsd.setPaymentCounter(cv.getPaymentCounter().toString());
        npsd.setTotalDue(cv.getTotalDue());
        npsd.setTotalPayment(cv.getTotalPayment());
        npsd.setPostpaidStartDate(new Date());
        npsd.setStatus("ACTIVE");
        npsd.setPaymentStatus("Pending");
        npsd.setEmail_account(cv.getEmail());
        details.setUserid(mCustomer);
        npsd.setUserId(details);
        npsd.setTransactionId(txnId);
        npsd.setSubscriptionDetailsId(details);
        npsd.setTotalPayment(0.0);
        postpaidSPlanRepository.save(npsd);

      }
    }

    response.setStatus("Success");
    //response.setErrCode("ASA003");
    response.setErrMessage("Plan Purchased Successfully");
    return new ResponseEntity(response, HttpStatus.OK);

	//UsdAmount = subscriptionDetailsRepository.findCurrencyValueByUserIdAndTransactionId(userID, txnId);
}


//@Scheduled(fixedRate = 60*60*1000)
@Scheduled(fixedRate = 10000)
@Override
public void pushPostpaidSPlanPaymentUnQuoted() {
	// TODO Auto-generated method stub
	int i;
	List<String> listOfUseridForTxn = new ArrayList<>();
	String userID="";
    System.out.println("Inside pushPostpaidSPlanPayment----------");
    
    List<NimaiSubscriptionDetails> convertPostpaidList=subscriptionDetailsRepository.findListOfUserToConvertToPostpaid();
    System.out.println("convertPostpaidList: "+convertPostpaidList);
    
    for(NimaiSubscriptionDetails nsd:convertPostpaidList)
    {
    	System.out.println("for userId: "+nsd.getUserid().getUserid());
    	Integer cnt=postpaidSPlanRepositoryUpd.findCountOfPostPaidPlan(nsd.getUserid().getUserid());
    	System.out.println("Count: "+cnt);
    	
    	if(cnt==0)
    	{
    		subscriptionDetailsRepository.updateStatusToInactive(nsd.getsPlSerialNUmber());
    	
    	NimaiSubscriptionDetails nsdDet=new NimaiSubscriptionDetails();
    	nsdDet.setSubscriptionId("");
    	nsdDet.setSubscriptionName("POSTPAID_PLAN");
    	nsdDet.setSubscriptionAmount(0);
    	nsdDet.setlCount("1");
    	nsdDet.setSubscriptionStartDate(new Date());
    	nsdDet.setSubscriptionEndDate(new Date());
    	nsdDet.setUserid(nsd.getUserid());
    	nsdDet.setFlag(0);
    	nsdDet.setSubsidiaries("0");
    	nsdDet.setRelationshipManager("YES");
    	nsdDet.setCustomerSupport("12*7");
    	nsdDet.setStatus("ACTIVE");
    	nsdDet.setSubscriptionValidity(0);
    	nsdDet.setLcUtilizedCount(0);
    	nsdDet.setSubsidiaryUtilizedCount(0);
    	nsdDet.setIsVasApplied(0);
    	nsdDet.setVasAmount(0);
    	nsdDet.setDiscountId(0);
    	nsdDet.setDiscount(0d);
    	nsdDet.setGrandAmount(0d);
    	nsdDet.setKycCount(0);
    	nsdDet.setInsertedBy("Auto");
    	nsdDet.setCustomerType(nsd.getCustomerType());
    	nsdDet.setsPLanCountry(nsd.getsPLanCountry());
    	nsdDet.setInsertedDate(new Date());
    	nsdDet.setRenewalEmailStatus("Pending");
    	
    	subscriptionDetailsRepository.save(nsdDet);
    	System.out.println("Data Saved...............");
    	System.out.println("Adding userid to list: "+nsd.getUserid().getUserid());
    	listOfUseridForTxn.add(nsd.getUserid().getUserid());
    	}
    }
    System.out.println("List of UserID whose txn to be moved to postpaid table: "+listOfUseridForTxn);
    
    //List<String> userIds=postpaidSPlanRepositoryUpd.findUserIdForUnQuoted();
    
    //System.out.println("userIds: "+userIds);
    List<String> finalIds;
    List<String> subsiIds=postpaidSPlanRepositoryUpd.findSubsidiary(listOfUseridForTxn);
    for(int j=0;j<subsiIds.size();j++)
    {
    	listOfUseridForTxn.add(subsiIds.get(j));
    }
    System.out.println("List of UserID after subsidiary addition: "+listOfUseridForTxn);
    List<String> txnIds=postpaidSPlanRepositoryUpd.findTransactionIdForUnQuoted(listOfUseridForTxn);
    System.out.println("txnIds: "+txnIds);
	System.out.println("Size: "+txnIds.size());
	for(i=0;i<txnIds.size();i++)
	  {
		  String txnId=txnIds.get(i);
		  userID=postpaidSPlanRepositoryUpd.findUserIdFromTxnId(txnId);
		  GenericResponse response = new GenericResponse();
		  List<NimaiPostpaidSubscriptionDetailsUpd> listOfData=postpaidSPlanRepositoryUpd.findDataOfUserByUserIdTxnId(userID,txnId);
		  System.out.println("listOfData.size(): "+listOfData.size());
		  String resultId;
		  if(listOfData.size()==0)
		  {
			  NimaiMCustomer mCustomer = userRepository.findCustomerDetailsByUserId(userID);
			  if(!Objects.isNull(mCustomer)) {
		      //mCustomer.setUserid(userID);
		      String customersType = this.userRepository.getAccountTypeByUserId(userID);
		      System.out.println("pushPostpaidSPlanPayment CustomerType:" + customersType);
		      NimaiSubscriptionDetails nsd = new NimaiSubscriptionDetails();
		      if (customersType.equalsIgnoreCase("MASTER")) {
		    	  System.out.println(userID+" is master");
		    	  resultId=userID;
		      }
		      else
		      {
		      String obtainUserId = mCustomer.getUserid();
		        System.out.println("calculationAsUserPresent nimaiCustomer userId SUBSIDIARY:" + obtainUserId);
		        String sourceUserID = this.userRepository.getAccountSourceByUserId(obtainUserId);
		        System.out.println("calculationAsUserPresent nimaiCustomer userId MASTER :" + obtainUserId);
		        resultId=sourceUserID;
		      }
		        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(resultId);
		        CalculationValue cv = calculateDues(userID, txnId);
		        Double UsdAmount = 0.0;
		        NimaiPostpaidSubscriptionDetailsUpd npsd = new NimaiPostpaidSubscriptionDetailsUpd();
		        npsd.setMinDue(cv.getMinDue());
		        npsd.setPerTransactionDue(cv.getPerTxnDue());
		        npsd.setPaymentCounter(cv.getPaymentCounter().toString());
		        npsd.setTotalDue(cv.getTotalDue());
		        npsd.setTotalPayment(cv.getTotalPayment());
		        npsd.setPostpaidStartDate(new Date());
		        npsd.setStatus("ACTIVE");
		        npsd.setPaymentStatus("Pending");
		        //npsd.setUserId(details);
		        npsd.setEmail_account(cv.getEmail());
		        npsd.setTransactionId(txnId);
		        npsd.setUserId(userID);
		        npsd.setSubscriptionDetailsId(details.getsPlSerialNUmber());
		        npsd.setTotalPayment(0.0);
		        postpaidSPlanRepositoryUpd.save(npsd);
		      /*} else if (customersType.equalsIgnoreCase("SUBSIDIARY")) {
		        String subsidary = mCustomer.getUserid();
		        System.out.println("calculationAsUserPresent nimaiCustomer userId SUBSIDIARY:" + subsidary);
		        String sourceUserID = this.userRepository.getAccountSourceByUserId(subsidary);
		        System.out.println("calculationAsUserPresent nimaiCustomer userId SOURCE:" + sourceUserID);
		        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(sourceUserID);
		        CalculationValue cv = calculateDues(subsidary, txnId);
		        Double UsdAmount = 0.0;
		        NimaiPostpaidSubscriptionDetails npsd = new NimaiPostpaidSubscriptionDetails();
		        npsd.setMinDue(cv.getMinDue());
		        npsd.setPerTransactionDue(cv.getPerTxnDue());
		        npsd.setPaymentCounter(cv.getPaymentCounter().toString());
		        npsd.setTotalDue(cv.getTotalDue());
		        npsd.setTotalPayment(cv.getTotalPayment());
		        npsd.setPostpaidStartDate(new Date());
		        npsd.setStatus("ACTIVE");
		        npsd.setPaymentStatus("Pending");
		        npsd.setEmail_account(cv.getEmail());
		        //details.setUserid(mCustomer);
		        System.out.println("Details id need to be set " + sourceUserID);
		       // NimaiSubscriptionDetails nimaiSubscriptionDetails = this.postpaidSPlanRepository.updateDetailsId(subsidary,txnId);
		       // npsd.setUserId(nimaiSubscriptionDetails);
		        npsd.setTransactionId(txnId);
		        npsd.setSubscriptionDetailsId(details);
		        npsd.setTotalPayment(0.0);
		        postpaidSPlanRepository.save(npsd);
		        postpaidSPlanRepository.getOne(npsd.getPostpaidId());
		        postpaidSPlanRepository.updateUserIdForSubsidary(subsidary,npsd.getPostpaidId());
		      }else if (customersType.equalsIgnoreCase("REFER")) {
		        System.out.println("calculationAsUserPresent nimaiCustomer userId REFER:" + mCustomer.getUserid());
		      //  String sourceUserID = this.userRepository.getAccountSourceByUserId(mCustomer.getUserid());
		       // String newUserID = this.userRepository.get(mCustomer.getUserid());
		        System.out.println("calculationAsUserPresent nimaiCustomer userId SOURCE:" + userID);
		        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(userID);
		        CalculationValue cv = calculateDues( mCustomer.getUserid(), txnId);
		        Double UsdAmount = 0.0;
		        NimaiPostpaidSubscriptionDetails npsd = new NimaiPostpaidSubscriptionDetails();
		        npsd.setMinDue(cv.getMinDue());
		        npsd.setPerTransactionDue(cv.getPerTxnDue());
		        npsd.setPaymentCounter(cv.getPaymentCounter().toString());
		        npsd.setTotalDue(cv.getTotalDue());
		        npsd.setTotalPayment(cv.getTotalPayment());
		        npsd.setPostpaidStartDate(new Date());
		        npsd.setStatus("ACTIVE");
		        npsd.setPaymentStatus("Pending");
		        npsd.setEmail_account(cv.getEmail());
		        details.setUserid(mCustomer);
		        npsd.setUserId(details);
		        npsd.setTransactionId(txnId);
		        npsd.setSubscriptionDetailsId(details);
		        npsd.setTotalPayment(0.0);
		        postpaidSPlanRepository.save(npsd);
		
		      }*/
		   }
		  }
	  }
	//UsdAmount = subscriptionDetailsRepository.findCurrencyValueByUserIdAndTransactionId(userID, txnId);
}



private CalculationValue calculateDues(String userID, String txnId) {
  // TODO Auto-generated method stub
  System.out.println("====== Calculating Dues =======");
  String email ="";
  CalculationValue cal=new CalculationValue();
  Double UsdAmount=0.0,LcValue=0.0;
  if(userID.substring(0,2).equalsIgnoreCase("BA")){
    UsdAmount = subscriptionDetailsRepository.findCurrencyValueByTransactionId(txnId);
    email= subscriptionDetailsRepository.findEmailByTransactionId(txnId);
  }else {
    UsdAmount = subscriptionDetailsRepository.findCurrencyValueByUserIdAndTransactionId(userID, txnId);
    email= subscriptionDetailsRepository.findEmailByUserIdAndTransactionId(userID,txnId);
  }
  System.out.println("USDAMOUNT:"+UsdAmount);
  System.out.println("fixedRate:"+fixedRate);
  Integer usance;
  String product=" ";
  if(userID.substring(0, 2).equalsIgnoreCase("BA")) {
	   product = subscriptionDetailsRepository.findBAProductByUserIdAndTransactionId(txnId);	 
	  
	   if(txnId.substring(0, 2).equalsIgnoreCase("BA")) {
		   usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTrxnIdUsanceBA(txnId);
	   }else {
		   switch(product)
		   {
		       case "ConfirmAndDiscount":
		     	  System.out.println("Getting usance for ConfirmAndDiscount");
		     	  
		     	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionIdBA(txnId);
		     	  
		     	  break;
		       case "Discounting":
		     	  System.out.println("Getting usance for Discount");
		     	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionIdBA(txnId);
		     	  break;
		       case "Confirmation":
		     	  System.out.println("Getting usance for Confirmation");
		     	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionIdBA(txnId);
		     	  break;
		       case "BillAvalisation":
		     	  System.out.println("Getting usance for BillAvalisation");
		     	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionIdBA(txnId);
		     	  break;
		       case "BankGuarantee":
		     	  System.out.println("Getting usance for BankGuarantee");
		     	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionIdBA(txnId);
		     	  break;
		       case "Banker":
		     	  System.out.println("Getting usance for Banker");
		     	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionIdBA(txnId);
		     	  break;
		       case "Refinance":
		     	  System.out.println("Getting usance for Refinance");
		     	  usance=subscriptionDetailsRepository.findOriginalTenorByUserIdAndTransactionIdBA(txnId);
		     	  break;
		       default:
		     	  usance=0;
		   }
	   }
  }else {
	   product = subscriptionDetailsRepository.findProductByUserIdAndTransactionId(userID, txnId);	  
	   switch(product)
	   {
	       case "ConfirmAndDiscount":
	     	  System.out.println("Getting usance for ConfirmAndDiscount");
	     	  
	     	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionId(userID, txnId);
	     	  break;
	       case "Discounting":
	     	  System.out.println("Getting usance for Discount");
	     	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionId(userID, txnId);
	     	  break;
	       case "Confirmation":
	     	  System.out.println("Getting usance for Confirmation");
	     	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionId(userID, txnId);
	     	  break;
	       case "BillAvalisation":
	     	  System.out.println("Getting usance for BillAvalisation");
	     	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionId(userID, txnId);
	     	  break;
	       case "BankGuarantee":
	     	  System.out.println("Getting usance for BankGuarantee");
	     	  usance=subscriptionDetailsRepository.findConfirmationPeriodByUserIdAndTransactionId(userID, txnId);
	     	  break;
	       case "Banker":
	     	  System.out.println("Getting usance for Banker");
	     	  usance=subscriptionDetailsRepository.findDiscountingPeriodByUserIdAndTransactionId(userID, txnId);
	     	  break;
	       case "Refinance":
	     	  System.out.println("Getting usance for Refinance");
	     	  usance=subscriptionDetailsRepository.findOriginalTenorByUserIdAndTransactionId(userID, txnId);
	     	  break;
	       default:
	     	  usance=0;
	   }
  }
 
  
  System.out.println("Usance: "+usance);
  //LcValue = (UsdAmount * Double.parseDouble(fixedRate))/100;
  LcValue = (((UsdAmount/360)*usance) * Double.parseDouble(fixedRate))/100;
  System.out.println("LcValue: "+LcValue);
  if (LcValue < Double.parseDouble(configuredValue)) {
    perTransactionDue = Double.parseDouble(configuredValue);
  } else {
    perTransactionDue = LcValue;
  }
  minDue=perTransactionDue;
  totalDue=perTransactionDue;
  transactionCounter++;
  cal.setMinDue(minDue);
  cal.setTotalDue(totalDue);
  cal.setPerTxnDue(perTransactionDue);
  cal.setTotalPayment(totalPayment);
  cal.setPaymentCounter(transactionCounter);
  cal.setEmail(email);
  return cal;
}



public SendCalculatedValues payCalc(String userID, String txnId) {
    System.out.println("====== Send Calculated Values =======");
    SendCalculatedValues send= new SendCalculatedValues();
    //For Rejected Payment Scenerio
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetailsRejected = postpaidSPlanRepositoryUpd.findDataOfUserByUserIdAsRejected(userID);
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetails =  postpaidSPlanRepositoryUpd.findDataOfUserByUserId(userID);
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetailsMakerApproved =  postpaidSPlanRepositoryUpd.findDataOfUserByUserIdMakerApproved(userID);
    int size=nimaiPostpaidSubscriptionDetails.size();
    String payment_status=null;
    String payment_mode=null;
    String user_id=null;
    List<String> transId= new ArrayList<>();
    System.out.println("Total Details: "+size);
    int transCounter=0;

  if(nimaiPostpaidSubscriptionDetailsMakerApproved.size()>0){
	  System.out.println("Maker Aprroved ............");
    for (NimaiPostpaidSubscriptionDetailsUpd nmm : nimaiPostpaidSubscriptionDetailsMakerApproved) {
      txnId =nmm.getTransactionId();
      payment_mode=nmm.getModeOfPayment();
      payment_status=nmm.getPaymentStatus();
      user_id=nmm.getUserId();
      send.setPayment_status(payment_status);
      send.setPayment_mode(payment_mode);
      break;
    }
  }else if(nimaiPostpaidSubscriptionDetailsRejected.size()>0){
	  System.out.println("In nimaiPostpaidSubscriptionDetailsRejected.size()>0 else if");
      for (NimaiPostpaidSubscriptionDetailsUpd nrsd : nimaiPostpaidSubscriptionDetailsRejected) {
        minDue = this.postpaidSPlanRepository.findFirstMinDue(userID,txnId);
        System.out.println("min Due:"+minDue);
        System.out.println("Details:"+nrsd.getTotalPayment() +":" +nrsd.getPaymentStatus()+":"+ nrsd.getStatus());
        if (nrsd.getTotalPayment() > 0) {
          System.out.println("Total Payment:"+nrsd.getTotalPayment());
          minDue = this.postpaidSPlanRepository.findFirstMinDue(userID,txnId);
          totalDue = this.postpaidSPlanRepository.findSumOfTotalDueRejected(userID);
          transCounter = this.postpaidSPlanRepository.findCountTotalDueRejected(userID);
          NimaiPostpaidSubscriptionDetailsUpd  nn = postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromQuotationInUnpaidAndOverAllRejected(userID,txnId);
          payment_status = nn.getPaymentStatus();
          payment_mode = nn.getModeOfPayment();
          user_id=nn.getUserId();
          if (payment_status.equalsIgnoreCase("Rejected")) {
            System.out.println("In Payment Status:" + payment_status + " Payment Mode:" + payment_mode);
            send.setPayment_status(payment_status);
            send.setPayment_mode(payment_mode);
            send.setTransCounter(transCounter);
            send.setUserId(user_id);
          }
          break;
        }
      }
     }else if(size>0) {
    	 System.out.println("In size>0 else");
    	 System.out.println("txnId: "+txnId);
    	 System.out.println("userID: "+userID);
      for (NimaiPostpaidSubscriptionDetailsUpd npsd : nimaiPostpaidSubscriptionDetails) {
          minDue = this.postpaidSPlanRepository.findFirstMinDue(userID, txnId);
          totalDue = this.postpaidSPlanRepository.findSumOfTotalDue(userID);
          transCounter = this.postpaidSPlanRepository.findCountTotalDueOverall(userID);
          System.out.println("min Due:" + minDue);
          System.out.println("Details:" + npsd.getTotalPayment() + ":" + npsd.getPaymentStatus() + ":" + npsd.getStatus());
          payment_status=npsd.getPaymentStatus();
          payment_mode=npsd.getModeOfPayment();
          user_id=npsd.getUserId();
          if (npsd.getTotalPayment() == 0) {
            System.out.println("Total Payment:" + npsd.getTotalPayment());
            minDue = this.postpaidSPlanRepository.findFirstMinDue(userID, txnId);
            totalDue = this.postpaidSPlanRepository.findSumOfTotalDue(userID);
            transCounter = this.postpaidSPlanRepository.findCountTotalDueOverall(userID);
            NimaiPostpaidSubscriptionDetailsUpd nn = this.postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromQuotationInUnpaidAndOverAll(userID, txnId);
            payment_status = nn.getPaymentStatus();
            payment_mode = nn.getModeOfPayment();
            user_id=nn.getUserId();
            if (payment_status.equalsIgnoreCase("Pending")) {
              System.out.println("In Payment Status:" + payment_status + " Payment Mode:" + payment_mode);
              send.setPayment_status(payment_status);
              send.setPayment_mode(payment_mode);
              send.setTransCounter(transCounter);
              send.setUserId(user_id);
            }
            break;
          }
      }
    }
     else
     {
    	 System.out.println("Its 0");
     }
    System.out.println("please enter"+send);
    
    send.setMinDue(minDue);
    send.setTotalDue(totalDue);
    send.setTransactionId(txnId);
    
    send.setTransCounter(transCounter);
    send.setPayment_status(payment_status);
    send.setPayment_mode(payment_mode);
    send.setUserId(user_id);
    return send  ;
  }

public SendCalculatedValues payCalcOverall(String userID, String txnId) {
    System.out.println("====== Send Overall Calculated Values =======");
    SendCalculatedValues send= new SendCalculatedValues();
    //For Rejected Payment Scenerio
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetailsRejected = postpaidSPlanRepositoryUpd.findDataOfUserByUserIdAsRejectedOverall(userID);
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetails =  postpaidSPlanRepositoryUpd.findDataOfUserByUserIdOverall(userID);
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetailsMakerApproved =  postpaidSPlanRepositoryUpd.findDataOfUserByUserIdMakerApprovedOverall(userID);
    int size=nimaiPostpaidSubscriptionDetails.size();
    String payment_status=null;
    String payment_mode=null;
    String user_id=null;
    List<String> transId= new ArrayList<>();
    System.out.println("Total Details: "+size);
    int transCounter=0;

  if(nimaiPostpaidSubscriptionDetailsMakerApproved.size()>0){
    for (NimaiPostpaidSubscriptionDetailsUpd nmm : nimaiPostpaidSubscriptionDetailsMakerApproved) {
      txnId =nmm.getTransactionId();
      payment_mode=nmm.getModeOfPayment();
      payment_status=nmm.getPaymentStatus();
      send.setPayment_status(payment_status);
      send.setPayment_mode(payment_mode);
      send.setUserId(nmm.getUserId());
      break;
    }
  }else if(nimaiPostpaidSubscriptionDetailsRejected.size()>0){
	  System.out.println("In nimaiPostpaidSubscriptionDetailsRejected.size()>0 else if");
      for (NimaiPostpaidSubscriptionDetailsUpd nrsd : nimaiPostpaidSubscriptionDetailsRejected) {
        minDue = this.postpaidSPlanRepository.findFirstMinDue(userID,txnId);
        System.out.println("min Due:"+minDue);
        System.out.println("Details:"+nrsd.getTotalPayment() +":" +nrsd.getPaymentStatus()+":"+ nrsd.getStatus());
        if (nrsd.getTotalPayment() > 0) {
          System.out.println("Total Payment:"+nrsd.getTotalPayment());
          minDue = this.postpaidSPlanRepository.findFirstMinDue(userID,txnId);
          totalDue = this.postpaidSPlanRepository.findSumOfTotalDueRejected(userID);
          transCounter = this.postpaidSPlanRepository.findCountTotalDueRejected(userID);
          NimaiPostpaidSubscriptionDetailsUpd  nn = postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromQuotationInUnpaidAndOverAllRejected(userID,txnId);
          payment_status = nn.getPaymentStatus();
          payment_mode = nn.getModeOfPayment();
          user_id=nn.getUserId();
          if (payment_status.equalsIgnoreCase("Rejected")) {
            System.out.println("In Payment Status:" + payment_status + " Payment Mode:" + payment_mode);
            send.setPayment_status(payment_status);
            send.setPayment_mode(payment_mode);
            send.setTransCounter(transCounter);
            send.setUserId(user_id);
          }
          break;
        }
      }
     }else if(size>0) {
    	 System.out.println("In size>0 else");
    	 System.out.println("txnId: "+txnId);
    	 System.out.println("userID: "+userID);
      for (NimaiPostpaidSubscriptionDetailsUpd npsd : nimaiPostpaidSubscriptionDetails) {
          minDue = this.postpaidSPlanRepository.findFirstMinDue(userID, txnId);
          totalDue = this.postpaidSPlanRepository.findSumOfTotalDue(userID);
          transCounter = this.postpaidSPlanRepository.findCountTotalDueOverall(userID);
          System.out.println("min Due: " + minDue);
          System.out.println("total Due: "+totalDue);
          System.out.println("Details:" + npsd.getTotalPayment() + ":" + npsd.getPaymentStatus() + ":" + npsd.getStatus());
          payment_status=npsd.getPaymentStatus();
          payment_mode=npsd.getModeOfPayment();
          user_id=npsd.getUserId();
          if (npsd.getTotalPayment() == 0) {
            System.out.println("Total Payment:" + npsd.getTotalPayment());
            minDue = this.postpaidSPlanRepository.findFirstMinDue(userID, txnId);
            totalDue = this.postpaidSPlanRepository.findSumOfTotalDue(userID);
            transCounter = this.postpaidSPlanRepository.findCountTotalDueOverall(userID);
            NimaiPostpaidSubscriptionDetailsUpd nn = this.postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromQuotationInUnpaidAndOverAll(userID, txnId);
            payment_status = nn.getPaymentStatus();
            payment_mode = nn.getModeOfPayment();
            user_id=nn.getUserId();
            if (payment_status.equalsIgnoreCase("Pending")) {
              System.out.println("In Payment Status:" + payment_status + " Payment Mode:" + payment_mode);
              send.setPayment_status(payment_status);
              send.setPayment_mode(payment_mode);
              send.setTransCounter(transCounter);
              send.setUserId(user_id);
            }
            break;
          }
      }
    }
     else
     {
    	 System.out.println("Its 0");
     }
    System.out.println("please enter"+send);
    System.out.println("userId: "+user_id);
    send.setMinDue(minDue);
    send.setTotalDue(totalDue);
    send.setTransactionId(txnId);
    
    send.setTransCounter(transCounter);
    send.setPayment_status(payment_status);
    send.setPayment_mode(payment_mode);
    send.setUserId(user_id);
    return send  ;
  }

  @Override
  public ResponseEntity<?> getApprovedTransactions(String userID){
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get Users getApprovedTransactions method Invoked ================");
    List<Map<String,String>> approvedTransactions= null;
    try {
      approvedTransactions = this.postpaidSPlanRepository.findUsersByUserIdAndPaymentStatusForBoth(userID);
     // approvedTransactions.stream().map()

    } catch (Exception e) {
      response.setErrMessage("No entity Found");
      e.printStackTrace();
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
    response.setData(approvedTransactions);
    // response.setData("------------");
    // response.setData(approvedTransactions);
    response.setErrMessage(ErrorDescription.getDescription("ASA001"));
    return new ResponseEntity(response, HttpStatus.OK);
  }



  @Override
  public ResponseEntity<?> getPendingTransactions(String userID){
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get Users getPendingTransactions method Invoked ================");
    List<Map<String,String>> pendingTransactions= null;
    try {
      pendingTransactions = this.postpaidSPlanRepository.findUsersByUserIdAndPaymentStatusPending(userID);

    } catch (Exception e) {
      response.setErrMessage("No entity Found");
      e.printStackTrace();
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
    response.setData(pendingTransactions);
    // response.setData("------------");
    // response.setData(approvedTransactions);
    response.setErrMessage(ErrorDescription.getDescription("ASA001"));
    return new ResponseEntity(response, HttpStatus.OK);
  }
  @Override
  public ResponseEntity<?> payForPostpaidSubscriptionPlan(String userID, String txnId, String amount, String discId, String discAmt, String mode) {
    // TODO Auto-generated method stub
    System.out.println("Logs at payForPostpaidSubscriptionPlan ::" );
    GenericResponse response = new GenericResponse();
    NimaiPostpaidSubscriptionDetailsUpd nimaiPostpaidSubscriptionDetails = postpaidSPlanRepositoryUpd.findUserByUserIdAndTransactionId(userID, txnId);
    System.out.println("Logs at nimaiPostpaidSubscriptionDetails ::"+ nimaiPostpaidSubscriptionDetails.getTransactionId() );
    NimaiSubscriptionDetails subscriptionDetails = this.subscriptionDetailsRepository.findByUserIdForPostPaid(userID);
    System.out.println("Logs at subscriptionDetails ::"+ subscriptionDetails.getSubscriptionName());
    System.out.println("Logs at subscriptionDetails ::"+ subscriptionDetails.getUserid());
    NimaiPostpaidSubscriptionDetailsUpd npsp = postpaidSPlanRepositoryUpd.getOne(nimaiPostpaidSubscriptionDetails.getPostpaidId());
    System.out.println("Logs at npsp ::"+ npsp.getTotalPayment());
    List<NimaiSubscriptionVas> vasList = null;
    String vaParentUserId="";
    
    NimaiMCustomer customerDetails=userRepository.getOne(npsp.getUserId());
    
    if(customerDetails.getAccountType().equalsIgnoreCase("SUBSIDIARY")) {
    	vaParentUserId=customerDetails.getAccountSource();
    	System.out.println("Logs at npsp 1::"+ vaParentUserId);
    }else {
    	vaParentUserId=userID;
    	System.out.println("Logs at npsp 2::"+ vaParentUserId);
    }
    vasList = this.nimaiSubscriptionVasRepo.findActiveVASByUserId(vaParentUserId);
    String[] newKeyValueDetails = null;
    String amountFieldGetKey = "";
    float vasAmount =0;
    newKeyValueDetails = amount.split(":");
    amount = amount.split(":")[1];
    Double finalPayment= 0.0;
    if(Objects.isNull(nimaiPostpaidSubscriptionDetails.getModeOfPayment())){
      npsp.setModeOfPayment("Wire");
      npsp.setStatus("ACTIVE");
      npsp.setPaymentStatus("Pending");
    }else if(nimaiPostpaidSubscriptionDetails.getModeOfPayment().equalsIgnoreCase("Wire") &&  nimaiPostpaidSubscriptionDetails.getPaymentStatus().equalsIgnoreCase("Pending")){
      response.setStatus("Reject");
      return  new ResponseEntity(response, HttpStatus.OK);
    }else
    {
    	System.out.println("In else");
    	npsp.setModeOfPayment("Wire");
        npsp.setStatus("ACTIVE");
        npsp.setPaymentStatus("Pending");
    }
    System.out.println("Logs at npsp setPaymentStatus ::"+ npsp.getPaymentStatus());
    double payment = 0.0;
    Double discount=0.0;
    String invoiceId = generatePaymentTtransactionID(10);
    String paymentTrId = generatePaymentTtransactionID(15);
    if (npsp.getModeOfPayment().equalsIgnoreCase("Wire") && npsp.getPaymentStatus().equalsIgnoreCase("Pending")) {
      for (NimaiSubscriptionVas vasPlan : vasList) {
        vasAmount=vasAmount+vasPlan.getPricing();
        System.out.println("Vas Amount ="+ vasAmount);
      }
      System.out.println("Value is as new  payForPostpaidSubscriptionPlan ::" + newKeyValueDetails);
      amountFieldGetKey = newKeyValueDetails[0].toString();
      System.out.println("Dues Type is payForPostpaidSubscriptionPlan ::" + amountFieldGetKey);
      System.out.println("Value is payForPostpaidSubscriptionPlan ::" + newKeyValueDetails);
      amountFieldGetKey = newKeyValueDetails[0].toString();
      System.out.println("Dues is payForPostpaidSubscriptionPlan ::" + amountFieldGetKey);
      if (amountFieldGetKey.equalsIgnoreCase("minDue")) {
      payment = this.postpaidSPlanRepository.findMinDueByPaymentCounter(userID, txnId);
      try {
        discount=subscriptionDetails.getDiscount();
        vasAmount=subscriptionDetails.getVasAmount();
      }catch (Exception ex){
        ex.printStackTrace();
      }
      finalPayment = payment-discount+vasAmount;
      System.out.println("payFor API1 : finalPayment"+ finalPayment);
      npsp.setDueType(amountFieldGetKey);
      System.out.println("Discount Id is payForPostpaidSubscriptionPlan ::" + subscriptionDetails.getDiscountId());
      System.out.println("Discount Amount is payForPostpaidSubscriptionPlan ::" + subscriptionDetails.getDiscount());
      try {
        npsp.setDisountId(subscriptionDetails.getDiscountId());
        npsp.setDiscountAmnt(subscriptionDetails.getDiscount());
        npsp.setTotalPayment(Double.valueOf(amount));
        npsp.setDisountId(Integer.valueOf(discId));
        npsp.setDiscountAmnt(Double.valueOf(discAmt));
        subscriptionDetails.setInvoiceId(invoiceId);
        
        npsp.setInvoiceId(invoiceId);
        npsp.setPaymentTxnId(paymentTrId);
        postpaidSPlanRepositoryUpd.save(npsp);
        
        subscriptionDetails.setDiscountId(Integer.valueOf(discId));
        subscriptionDetails.setDiscount(Double.valueOf(discAmt));
        subscriptionDetails.setGrandAmount(npsp.getTotalPayment());
        subscriptionDetails.setPaymentStatus(npsp.getPaymentStatus());
        subscriptionDetailsRepository.save(subscriptionDetails);
      }catch (Exception ex){
        ex.printStackTrace();
      }
    } else {
    	System.out.println("totalDue");
        List transactionIdsFromQuotation = this.postpaidSPlanRepository.findPendingTransactionIdsFromQuotationOnly(userID);
        System.out.println("transactionIdsFromQuotation: "+transactionIdsFromQuotation);
        List<NimaiPostpaidSubscriptionDetailsUpd> nn = this.postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromQuotation(transactionIdsFromQuotation);
        payment = this.postpaidSPlanRepository.findSumOfTotalDue(userID);
        try {
          discount = subscriptionDetails.getDiscount();
          vasAmount = subscriptionDetails.getVasAmount();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        finalPayment = payment - discount + vasAmount;
        System.out.println("payFor API2 : finalPayment" + finalPayment);
        for (NimaiPostpaidSubscriptionDetailsUpd nv : nn) {
          if (Objects.isNull(nv.getDueType())) {
            nv.setDueType(amountFieldGetKey);
            nv.setModeOfPayment("Wire");
            System.out.println("Discount id is :" + subscriptionDetails.getDiscountId());
            try {
              nv.setDisountId(subscriptionDetails.getDiscountId());
              nv.setPaymentStatus("Pending");
              nv.setDiscountAmnt(subscriptionDetails.getDiscount());
              nv.setTotalPayment(Double.valueOf(amount));
              subscriptionDetails.setInvoiceId(invoiceId);
              System.out.println("Invoice Id:"+ subscriptionDetails.getInvoiceId());
              nv.setInvoiceId(invoiceId);
              nv.setPaymentTxnId(paymentTrId);
              postpaidSPlanRepositoryUpd.save(npsp);
              subscriptionDetails.setGrandAmount(npsp.getTotalPayment());
              subscriptionDetailsRepository.save(subscriptionDetails);
            } catch (Exception ex) {
              ex.printStackTrace();
            }

          }

        }
      }
    }
    System.out.println("Details:" + userID + "::" + paymentTrId + "::" + invoiceId + "::" + finalPayment);
   
    
    subscriptionDetailsRepository.updatePaymentTxnIdForWire(userID, paymentTrId, invoiceId, subscriptionDetails.getGrandAmount().toString());
    userRepository.updatePaymentTransactionId(userID, invoiceId);
    userRepository.updatePaymentMode(npsp.getModeOfPayment(), userID);
    
    
  
    System.out.println("vaParentUserId:" + vaParentUserId + "::" + paymentTrId + "::" + invoiceId + "::" + finalPayment);
    
    
    
    nimaiSubscriptionVasRepo.updatePaymentTransactionIdVAS(vaParentUserId,invoiceId);

    System.out.println("For the Scheduler Part...");
    NimaiEmailScheduler schedularData = new NimaiEmailScheduler();
    List<String> vasIdString = new ArrayList<>();
    int trnCount = 0;
    System.out.println("For the Scheduler Part nimaiPostpaidSubscriptionDetails ..."+ nimaiPostpaidSubscriptionDetails.getDueType());
    if (nimaiPostpaidSubscriptionDetails.getDueType().equalsIgnoreCase("totalDue")) {
      vasList = this.nimaiSubscriptionVasRepo.findActiveVASByUserId(userID);
      if(vasList.size()>0) {
        System.out.println("For the Scheduler Part VasList ..." + vasList);
        for (NimaiSubscriptionVas vas : vasList) {
          trnCount = trnCount + 1;
        }
      }
    }else{
      trnCount = 1;
    }
    schedularData.setNumberOfTrxn(trnCount);
    vasList = this.nimaiSubscriptionVasRepo.findActiveVASByUserId(userID);
    System.out.println("For the Scheduler Part VasList on size ..."+ vasList.size());
    if (vasList.size() > 1) {
      for (NimaiSubscriptionVas vasPlan : vasList) {
        vasIdString.add((String.valueOf(vasPlan.getId()).concat("-")));
        String[] array = new String[vasIdString.size()];
        vasIdString.toArray(array);
        StringBuilder sb = new StringBuilder();
        for (String str : (String[]) vasIdString.<String>toArray(array)) {
          sb.append(str);
          sb.substring(0, sb.length() - 1);
        }
        StringBuilder vasNumberString = sb;
        System.out.println("For the Scheduler Part vasNumberString ..."+ vasNumberString);
        schedularData.setDescription5(vasNumberString.toString());
        vasAmount = vasAmount + vasPlan.getPricing();
        System.out.println("For the Scheduler Part vasAmount ..."+ vasAmount);
      }
      schedularData.setVasAmount(Math.round(vasAmount));
      // subscriptionDetails.setVasAmount(Math.round(vasAmount));
    }
    response.setStatus("Success");
    response.setErrMessage("Plan Purchased Successfully");
    return new ResponseEntity(response, HttpStatus.OK);
}

@Override
public ResponseEntity<?> overallPostpaidSubscriptionPlan(String userID) {
  GenericResponse response = new GenericResponse();
  logger.info(" ================ Get Users overallPostpaidSubscriptionPlan method Invoked ================");
  PostpaidSubscriptionBean PostpaidSubscriptionBean = new PostpaidSubscriptionBean();

  SendCalculatedValues send = null;
  try {
    int flag = 0;
    List QuotationCount;// = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
    if(userID.substring(0, 2).equalsIgnoreCase("BA"))
    {
    	System.out.println("Getting details by bank id");
    	QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTimeForBank(userID);
    }
    else
    {
    	System.out.println("Getting details by customer id");
    	QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
    }
    
    System.out.println("The Quotation Count:" + QuotationCount);
    String resultUserId = "";
    String transactId = "";
    String payment1 = "";
    String payment2 = "";
    String tt = "";
    for (final Object txnDet : QuotationCount) {
      resultUserId = (((Object[]) txnDet)[1] == null) ? null : ((Object[]) txnDet)[1].toString();
      transactId = (((Object[]) txnDet)[0] == null) ? null : ((Object[]) txnDet)[0].toString();
      System.out.println("---resultUserId: "+resultUserId);
      System.out.println("---transactId: "+transactId);
      Object transFromPostpaid = this.postpaidSPlanRepository.findUsersDetailsByUserIdAndTransactionId(userID, transactId);
      tt = (((Object[]) transFromPostpaid)[0] == null) ? null : ((Object[]) transFromPostpaid)[0].toString();
      payment1 = (((Object[]) transFromPostpaid)[1] == null) ? null : ((Object[]) transFromPostpaid)[1].toString();
      if (transactId.equalsIgnoreCase(tt) && payment1.equalsIgnoreCase("Pending") || payment1.equalsIgnoreCase("Rejected")) {
        flag = 1;
        break;
      }
    }

    if (flag == 1) {
      send = payCalcOverall(userID,transactId);
    }
    System.out.println("resultUserId: " + resultUserId);
    System.out.println("transactId: " + transactId);
    System.out.println("Details as=" + send);

  } catch (Exception e) {
    response.setErrMessage("No entity Found");
    response.setData("firstEntry");
    e.printStackTrace();
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  response.setData(send);
  response.setErrMessage(ErrorDescription.getDescription("ASA001"));
  return new ResponseEntity(response, HttpStatus.OK);
}

@Override
public String getTransactionIdOfPending(String userId) {
	// TODO Auto-generated method stub
	List QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userId);
    System.out.println("The Quotation Count:" + QuotationCount);
    
    String resultUserId = "";
    String transactId = "";
    String tt="";
    String payment1="";
    for (Object txnDet : QuotationCount) {
        resultUserId = (((Object[]) txnDet)[1] == null) ? null : ((Object[]) txnDet)[1].toString();
        transactId = (((Object[]) txnDet)[0] == null) ? null : ((Object[]) txnDet)[0].toString();
        Object transFromPostpaid = this.postpaidSPlanRepository.findNewUsersDetailsByUserIdAndTransactionId(resultUserId, transactId);
        tt = (((Object[]) transFromPostpaid)[0] == null) ? null : ((Object[]) transFromPostpaid)[0].toString();
        payment1 = (((Object[]) transFromPostpaid)[1] == null) ? null : ((Object[]) transFromPostpaid)[1].toString();
        if (transactId.equalsIgnoreCase(tt) && (payment1.equalsIgnoreCase("Pending") || payment1.equalsIgnoreCase("Maker Approved"))) {
      
          break;
        }
        
      }
    return transactId;
}

@Override
public void totalDuePayment(String userId, Integer vasId, Integer discId, Double discAmt,Double grAmt) {
	// TODO Auto-generated method stub
	List transactionIdsFromQuotation = this.postpaidSPlanRepository.findPendingTransactionIdsFromQuotationOnly(userId);
	List userIdsFromQuotation = this.postpaidSPlanRepository.findPendingUserIdsFromQuotationOnly(userId);
	System.out.println("transactionIdsFromQuotation: "+transactionIdsFromQuotation);
	System.out.println("userIdsFromQuotation: "+userIdsFromQuotation);
	try
    {
		List<NimaiPostpaidSubscriptionDetailsUpd> nn = this.postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromPostpaid(transactionIdsFromQuotation,userIdsFromQuotation);
		String invoiceId = generatePaymentTtransactionID(10);
		String paymentTrId = generatePaymentTtransactionID(15);
    
    	System.out.println("Updating payment Details...");
	    
	    //NimaiSubscriptionDetails nsd1=subscriptionDetailsRepository.findByUserId(userId);
	    int i;
	    for(NimaiPostpaidSubscriptionDetailsUpd npsd:nn)
		{
	    	NimaiPostpaidSubscriptionDetailsUpd postpaidData=postpaidSPlanRepositoryUpd.getOne(npsd.getPostpaidId());
	    	postpaidData.setPaymentStatus("Pending");
	    	postpaidData.setModeOfPayment("Wire");
	    	postpaidData.setDueType("totalDue");
	    	postpaidData.setDisountId(discId);
	    	postpaidData.setTotalPayment(grAmt);
	    	postpaidData.setDiscountAmnt(discAmt);
	    	postpaidData.setPaymentTxnId(paymentTrId);
	    	postpaidData.setInvoiceId(invoiceId);
	    	postpaidSPlanRepositoryUpd.save(postpaidData);
		}
	    System.out.println("VASId: "+vasId);
	    System.out.println("discId: "+discId);
	    System.out.println("discAmt: "+discAmt);
	    subscriptionDetailsRepository.updatePaymentDetails(userId, paymentTrId, invoiceId, vasId, discId, discAmt, grAmt);
	    //this.userRepository.updatePaymentStatus(userId);
        //this.userRepository.updatePlanPurchasedStatus(userId);
        //String invoiceId = generatePaymentTtransactionID(10);
        
        this.userRepository.updatePaymentTransactionId(userId, invoiceId);
        this.userRepository.updatePaymentMode("Wire", userId);
        this.nimaiSubscriptionVasRepo.updatePaymentTransactionIdVAS(userId,invoiceId);
    }
    catch(Exception e)
    {
    	System.out.println("Exception: "+e);
    }
}

@Override
public ResponseEntity<?> getPostpaidTxnDet(String userID) {
	// TODO Auto-generated method stub
	List<NimaiPostpaidSubscriptionDetailsUpd> txnList=postpaidSPlanRepositoryUpd.getpostpaidTxnDeatils(userID);
	
	GenericResponse response = new GenericResponse();
	
	response.setData(txnList);
	response.setStatus("Success");
	
	return new ResponseEntity(response, HttpStatus.OK);
}

@Override
public ResponseEntity<?> overallPostpaidSubscriptionPlanBA(String userID) {
	  GenericResponse response = new GenericResponse();
	  logger.info(" ================ Get Users overallPostpaidSubscriptionPlan method Invoked ================");
	  PostpaidSubscriptionBean PostpaidSubscriptionBean = new PostpaidSubscriptionBean();

	  SendCalculatedValues send = null;
	  try {
	    int flag = 0;
	    List QuotationCount;// = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
	    if(userID.substring(0, 2).equalsIgnoreCase("BA"))
	    {
	    	System.out.println("Getting details by bank id");
	    	QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTimeForBa(userID);
	    }
	    else
	    {
	    	System.out.println("Getting details by customer id");
	    	QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
	    }
	    
	    System.out.println("The Quotation Count:" + QuotationCount);
	    String resultUserId = "";
	    String transactId = "";
	    String payment1 = "";
	    String payment2 = "";
	    String tt = "";
	    
	    for (final Object txnDet : QuotationCount) {
	      resultUserId = (((Object[]) txnDet)[1] == null) ? null : ((Object[]) txnDet)[1].toString();
	      transactId = (((Object[]) txnDet)[0] == null) ? null : ((Object[]) txnDet)[0].toString();
	      System.out.println("---resultUserId: "+resultUserId);
	      System.out.println("---transactId: "+transactId);
	      Object transFromPostpaid = this.postpaidSPlanRepository.findUsersDetailsByUserIdAndTransactionId(userID, transactId);
	      tt = (((Object[]) transFromPostpaid)[0] == null) ? null : ((Object[]) transFromPostpaid)[0].toString();
	      payment1 = (((Object[]) transFromPostpaid)[1] == null) ? null : ((Object[]) transFromPostpaid)[1].toString();
	      
	      if (transactId.equalsIgnoreCase(tt) && 
	    		  payment1.equalsIgnoreCase("Pending") || payment1.equalsIgnoreCase("Rejected")) {
	      
	    	  flag = 1;
	        break;
	      }
	    }

	    if (flag == 1) {
	      send = payCalcOverall(userID,transactId);
	    }
	    System.out.println("resultUserId: " + resultUserId);
	    System.out.println("transactId: " + transactId);
	    System.out.println("Details as=" + send);

	  } catch (Exception e) {
	    response.setErrMessage("No entity Found");
	    response.setData("firstEntry");
	    e.printStackTrace();
	    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
	  }
	  response.setData(send);
	  response.setErrMessage(ErrorDescription.getDescription("ASA001"));
	  return new ResponseEntity(response, HttpStatus.OK);
}

@Override
public List<TransactionPostPaidDetail> getTransactionPostPaidDetail(String userId) {
	// TODO Auto-generated method stub
	List<TransactionPostPaidDetail> tpd=new ArrayList<>();
	try
	{
		List<Integer> quoteId=postpaidSPlanRepository.getQuoteId(userId);
		System.out.println("quoteId: "+quoteId);
		for(Integer qId:quoteId)
		{
			
			String txnId=postpaidSPlanRepository.getTxnId(qId);
			
			String uId=postpaidSPlanRepository.getUserId(txnId);
			
			String quoteStatus=postpaidSPlanRepository.getQuotationStatus(qId);
			
			String paymentStatus=postpaidSPlanRepository.findPaymentStatusByUserIdAndTransactionId(uId, txnId);
			
			String paymentMode=postpaidSPlanRepository.findPaymentModeByUserIdAndTransactionId(uId, txnId);
			
			String insDate=postpaidSPlanRepository.getQuoteInsertedDate(qId);
			
			String modDate=postpaidSPlanRepository.getQuoteModifiedDate(qId);
			
			String acceptedDate=postpaidSPlanRepository.getQuoteAcceptedOn(uId, txnId);
			
			System.out.println("txnId: "+txnId);
			System.out.println("uId: "+uId);
			System.out.println("quoteStatus: "+quoteStatus);
			System.out.println("paymentStatus: "+paymentStatus);
			System.out.println("paymentMode: "+paymentMode);
			System.out.println("insDate: "+insDate);
			System.out.println("modDate: "+modDate);
			System.out.println("acceptedDate: "+acceptedDate);
			
			TransactionPostPaidDetail td=new TransactionPostPaidDetail();
			td.setUserId(uId);
			td.setTransactionId(txnId);
			td.setQuotationStatus(quoteStatus);
			td.setPaymentMode(paymentMode);
			td.setPaymentStatus(paymentStatus);
			td.setInsertedDate(insDate);
			td.setModifiedDate(modDate);
			td.setAcceptedOn(acceptedDate);
			tpd.add(td);
		}
		/*List<String> transId=postpaidSPlanRepository.getTxnId(userId);
		System.out.println("transId: "+transId);
		for(String txnId:transId)
		{
			
			String uId=postpaidSPlanRepository.getUserId(txnId);
			
			String quoteStatus=postpaidSPlanRepository.getQuotationStatus(txnId, uId);
			
			String paymentStatus=postpaidSPlanRepository.findPaymentStatusByUserIdAndTransactionId(uId, txnId);
			
			String paymentMode=postpaidSPlanRepository.findPaymentModeByUserIdAndTransactionId(uId, txnId);
			
			String insDate=postpaidSPlanRepository.getQuoteInsertedDate(uId, txnId);
			
			String modDate=postpaidSPlanRepository.getQuoteModifiedDate(uId, txnId);
			
			String acceptedDate=postpaidSPlanRepository.getQuoteAcceptedOn(uId, txnId);
			
			System.out.println("txnId: "+txnId);
			System.out.println("uId: "+uId);
			System.out.println("quoteStatus: "+quoteStatus);
			System.out.println("paymentStatus: "+paymentStatus);
			System.out.println("paymentMode: "+paymentMode);
			System.out.println("insDate: "+insDate);
			System.out.println("modDate: "+modDate);
			System.out.println("acceptedDate: "+acceptedDate);
			
			TransactionPostPaidDetail td=new TransactionPostPaidDetail();
			td.setUserId(uId);
			td.setTransactionId(txnId);
			td.setQuotationStatus(quoteStatus);
			td.setPaymentMode(paymentMode);
			td.setPaymentStatus(paymentStatus);
			td.setInsertedDate(insDate);
			td.setModifiedDate(modDate);
			td.setAcceptedOn(acceptedDate);
			tpd.add(td);
		}*/
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	return tpd;
}

@Override
public ResponseEntity<?> overallPostpaidSubscriptionPlanv2(String userID) {
  GenericResponse response = new GenericResponse();
  logger.info(" ================ Get v2 overallPostpaidSubscriptionPlan method Invoked ================");
  PostpaidSubscriptionBean PostpaidSubscriptionBean = new PostpaidSubscriptionBean();

  SendCalculatedValues send = null;
  try {
    int flag = 0;
    List QuotationCount;// = this.postpaidSPlanRepository.findFirstQuotationByDateAndTime(userID);
    if(userID.substring(0, 2).equalsIgnoreCase("BA"))
    {
    	System.out.println("Getting details by bank id");
    	QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTimeForBankv2(userID);
    }
    else
    {
    	System.out.println("Getting details by customer id");
    	QuotationCount = this.postpaidSPlanRepository.findFirstQuotationByDateAndTimev2(userID);
    }
    
    System.out.println("The Quotation Count:" + QuotationCount);
    String resultUserId = "";
    String transactId = "";
    String payment1 = "";
    String payment2 = "";
    String tt = "";
    String paymentMode="";
   List<ListPostPaidPayment> details=new ArrayList<>();
    
    for (final Object txnDet : QuotationCount) {
      resultUserId = (((Object[]) txnDet)[1] == null) ? null : ((Object[]) txnDet)[1].toString();
      transactId = (((Object[]) txnDet)[0] == null) ? null : ((Object[]) txnDet)[0].toString();
      System.out.println("---resultUserId: "+resultUserId);
      System.out.println("---transactId: "+transactId);
      Object transFromPostpaid = this.postpaidSPlanRepository.findUsersDetailsByUserIdAndTransactionId(userID, transactId);
      tt = (((Object[]) transFromPostpaid)[0] == null) ? null : ((Object[]) transFromPostpaid)[0].toString();
      payment1 = (((Object[]) transFromPostpaid)[1] == null) ? null : ((Object[]) transFromPostpaid)[1].toString();
      paymentMode = (((Object[]) transFromPostpaid)[2] == null) ? "NA" : ((Object[]) transFromPostpaid)[2].toString();
      System.out.println("---resultUserId1: "+resultUserId);
      System.out.println("---transactId1: "+transactId);
      System.out.println("---payment1: "+payment1);
      System.out.println("---paymentMode1: "+paymentMode);
      ListPostPaidPayment poDetails=new ListPostPaidPayment();
    poDetails.setResultUserId(resultUserId);
    poDetails.setTransactId(transactId);
    poDetails.setPayment1(payment1);
    poDetails.setPaymentMode(paymentMode);
    poDetails.setTt(tt);
    details.add(poDetails);
     
    }
    List<ListPostPaidPayment> makerApprovedList = details
    		  .stream()
    		  .filter(c -> c.getPaymentMode().equalsIgnoreCase("Wire") && c.getPayment1().equalsIgnoreCase("Maker Approved"))
    		  .collect(Collectors.toList());

    List<ListPostPaidPayment> pendingApprovedList = details
    		  .stream()
    		  .filter(c -> c.getPaymentMode().equalsIgnoreCase("Wire") && c.getPayment1().equalsIgnoreCase("Pending"))
    		  .collect(Collectors.toList());
    System.out.println("Inside makeraprrovedList"+makerApprovedList.size());
    System.out.println("Inside pendingApprovedList"+pendingApprovedList.size());
    if(makerApprovedList.size()>0) {
    	for(ListPostPaidPayment makeList:makerApprovedList) {
    		System.out.println("Inside makeraprrovedList"+makerApprovedList.size());
    		if( makeList.getTransactId().equalsIgnoreCase(makeList.getTt())) {
    			send = payCalcOverallv2(makeList.getResultUserId(),makeList.getTransactId());
        		System.out.println("resultUserId: " + makeList.getResultUserId());
        	    System.out.println("transactId: " + makeList.getTransactId());
        	    System.out.println("Details as=" + send);
    		}
    		
    	}
    }
    else if (pendingApprovedList.size()>0){
    	for(ListPostPaidPayment makeList:pendingApprovedList) {
    		System.out.println("Inside pendingApprovedList"+makerApprovedList.size());
    		if( makeList.getTransactId().equalsIgnoreCase(makeList.getTt())) {
    			send = payCalcOverallv2(makeList.getResultUserId(),makeList.getTransactId());
        		System.out.println("resultUserId: " + makeList.getResultUserId());
        	    System.out.println("transactId: " + makeList.getTransactId());
        	    System.out.println("Details as=" + send);
    		}
    		
    	}
    }else {
    	  for (final Object txnDet : QuotationCount) {
    		  System.out.println("Inside else new condition");
    	      resultUserId = (((Object[]) txnDet)[1] == null) ? null : ((Object[]) txnDet)[1].toString();
    	      transactId = (((Object[]) txnDet)[0] == null) ? null : ((Object[]) txnDet)[0].toString();
    	      System.out.println("---resultUserId: "+resultUserId);
    	      System.out.println("---transactId: "+transactId);
    	      Object transFromPostpaid = this.postpaidSPlanRepository.findUsersDetailsByUserIdAndTransactionId(userID, transactId);
    	      tt = (((Object[]) transFromPostpaid)[0] == null) ? null : ((Object[]) transFromPostpaid)[0].toString();
    	      payment1 = (((Object[]) transFromPostpaid)[1] == null) ? null : ((Object[]) transFromPostpaid)[1].toString();
    	      paymentMode = (((Object[]) transFromPostpaid)[2] == null) ? "NA" : ((Object[]) transFromPostpaid)[2].toString();
    	     
    	      if(transactId.equalsIgnoreCase(tt) && payment1.equalsIgnoreCase("Pending") || payment1.equalsIgnoreCase("Rejected")) {
    			   flag = 1;
    		        break;
    		   }
    	     
    	    }
    	   if (flag == 1) {
    		      send = payCalcOverallv2(userID,transactId);
    		    }
    		    System.out.println("resultUserId: " + resultUserId);
    		    System.out.println("transactId: " + transactId);
    		    System.out.println("Details as=" + send);
    	  
    }
    
    
 

  } catch (Exception e) {
    response.setErrMessage("No entity Found");
    response.setData("firstEntry");
    e.printStackTrace();
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  response.setData(send);
  response.setErrMessage(ErrorDescription.getDescription("ASA001"));
  return new ResponseEntity(response, HttpStatus.OK);
}

public SendCalculatedValues payCalcOverallv2(String userID, String txnId) {
    System.out.println("====== Send Overall Calculated Values =======");
    SendCalculatedValues send= new SendCalculatedValues();
    //For Rejected Payment Scenerio
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetailsRejected = postpaidSPlanRepositoryUpd.findDataOfUserByUserIdAsRejectedOverall(userID);
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetails =  postpaidSPlanRepositoryUpd.findDataOfUserByUserIdOverall(userID);
    List<NimaiPostpaidSubscriptionDetailsUpd> nimaiPostpaidSubscriptionDetailsMakerApproved =  postpaidSPlanRepositoryUpd.findDataOfUserByUserIdMakerApprovedOverall(userID);
    int size=nimaiPostpaidSubscriptionDetails.size();
    String payment_status=null;
    String payment_mode=null;
    String user_id=null;
    List<String> transId= new ArrayList<>();
    System.out.println("Total Details: "+size);
    int transCounter=0;

  if(nimaiPostpaidSubscriptionDetailsMakerApproved.size()>0){
    for (NimaiPostpaidSubscriptionDetailsUpd nmm : nimaiPostpaidSubscriptionDetailsMakerApproved) {
      txnId =nmm.getTransactionId();
      payment_mode=nmm.getModeOfPayment();
      payment_status=nmm.getPaymentStatus();
      send.setPayment_status(payment_status);
      send.setPayment_mode(payment_mode);
      send.setUserId(nmm.getUserId());
      break;
    }
  }else if(nimaiPostpaidSubscriptionDetailsRejected.size()>0){
	  System.out.println("In nimaiPostpaidSubscriptionDetailsRejected.size()>0 else if");
      for (NimaiPostpaidSubscriptionDetailsUpd nrsd : nimaiPostpaidSubscriptionDetailsRejected) {
        minDue = this.postpaidSPlanRepository.findFirstMinDue(userID,txnId);
        System.out.println("min Due:"+minDue);
        System.out.println("Details:"+nrsd.getTotalPayment() +":" +nrsd.getPaymentStatus()+":"+ nrsd.getStatus());
        if (nrsd.getTotalPayment() > 0) {
          System.out.println("Total Payment:"+nrsd.getTotalPayment());
          minDue = this.postpaidSPlanRepository.findFirstMinDue(userID,txnId);
          totalDue = this.postpaidSPlanRepository.findSumOfTotalDueRejectedv2(userID);
          transCounter = this.postpaidSPlanRepository.findCountTotalDueRejectedv2(userID);
          NimaiPostpaidSubscriptionDetailsUpd  nn = postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromQuotationInUnpaidAndOverAllRejected(userID,txnId);
          payment_status = nn.getPaymentStatus();
          payment_mode = nn.getModeOfPayment();
          user_id=nn.getUserId();
          if (payment_status.equalsIgnoreCase("Rejected")) {
            System.out.println("In Payment Status:" + payment_status + " Payment Mode:" + payment_mode);
            send.setPayment_status(payment_status);
            send.setPayment_mode(payment_mode);
            send.setTransCounter(transCounter);
            send.setUserId(user_id);
          }
          break;
        }
      }
     }else if(size>0) {
    	 System.out.println("In size>0 else");
    	 System.out.println("txnId: "+txnId);
    	 System.out.println("userID: "+userID);
      for (NimaiPostpaidSubscriptionDetailsUpd npsd : nimaiPostpaidSubscriptionDetails) {
          minDue = this.postpaidSPlanRepository.findFirstMinDue(userID, txnId);
          totalDue = this.postpaidSPlanRepository.findSumOfTotalDuev2(userID);
          transCounter = this.postpaidSPlanRepository.findCountTotalDueOverallv2(userID);
          System.out.println("min Due: " + minDue);
          System.out.println("total Due: "+totalDue);
          System.out.println("Details:" + npsd.getTotalPayment() + ":" + npsd.getPaymentStatus() + ":" + npsd.getStatus());
          payment_status=npsd.getPaymentStatus();
          payment_mode=npsd.getModeOfPayment();
          user_id=npsd.getUserId();
          if (npsd.getTotalPayment() == 0) {
            System.out.println("Total Payment:" + npsd.getTotalPayment());
            minDue = this.postpaidSPlanRepository.findFirstMinDue(userID, txnId);
            totalDue = this.postpaidSPlanRepository.findSumOfTotalDuev2(userID);
            transCounter = this.postpaidSPlanRepository.findCountTotalDueOverallv2(userID);
            NimaiPostpaidSubscriptionDetailsUpd nn = this.postpaidSPlanRepositoryUpd.findPendingTransactionIdsFromQuotationInUnpaidAndOverAll(userID, txnId);
            payment_status = nn.getPaymentStatus();
            payment_mode = nn.getModeOfPayment();
            user_id=nn.getUserId();
            if (payment_status.equalsIgnoreCase("Pending")) {
              System.out.println("In Payment Status:" + payment_status + " Payment Mode:" + payment_mode);
              send.setPayment_status(payment_status);
              send.setPayment_mode(payment_mode);
              send.setTransCounter(transCounter);
              send.setUserId(user_id);
            }
            break;
          }
      }
    }
     else
     {
    	 System.out.println("Its 0");
     }
    System.out.println("please enter"+send);
    System.out.println("userId: "+user_id);
    send.setMinDue(minDue);
    send.setTotalDue(totalDue);
    send.setTransactionId(txnId);
    
    send.setTransCounter(transCounter);
    send.setPayment_status(payment_status);
    send.setPayment_mode(payment_mode);
    send.setUserId(user_id);
    return send  ;
  }

@Override
public NimaiTransactionViewCount getViewCountByUserId(String userID) {
	// TODO Auto-generated method stub
	NimaiTransactionViewCount vc=viewCountRepo.getviewCountByUserId(userID);
	
	return vc;
}

@Override
public void updateViewCountByUserId(String userID) {
	// TODO Auto-generated method stub
	NimaiTransactionViewCount vc=viewCountRepo.getviewCountByUserId(userID);
	System.out.println("vc: "+vc);
	if(vc!=null)
	{
		NimaiTransactionViewCount vcUpdate=viewCountRepo.getOne(vc.getID());
		NimaiTransactionViewCount vcNewWithOld=new NimaiTransactionViewCount();
		vcNewWithOld.setAfterAccepted(vcUpdate.getAfterAccepted());
		vcNewWithOld.setBeforeAccepted(vcUpdate.getBeforeAccepted());
		vcNewWithOld.setUserId(userID);
		vcNewWithOld.setInsertDate(new Date());
		vcNewWithOld.setAcceptedFlag(1);
		viewCountRepo.save(vcNewWithOld);
	}
	if(vc==null)
	{
		System.out.println("No Data Found");
		NimaiTransactionViewCount vcNew=new NimaiTransactionViewCount();
		vcNew.setAcceptedFlag(1);
		vcNew.setAfterAccepted(0);
		vcNew.setBeforeAccepted(0);
		vcNew.setUserId(userID);
		vcNew.setInsertDate(new Date());
		viewCountRepo.save(vcNew);
	}
}

@Override
public ResponseEntity<?> renewSubscriptionPlanV2(SubscriptionBean subscriptionRequest, String userId,
		Integer lcUtilizedCount) {
    GenericResponse response = new GenericResponse();
    String paymentTrId = "";
    logger.info(" ================ renewSubscriptionPlan method Invoked ================");
    Calendar cal = Calendar.getInstance();
    Date today = cal.getTime();
    int addOnCredit = 0, utilzedLcCount = 0, days = 0;
    NimaiSubscriptionDetails inactiveSubscriptionEntity=new NimaiSubscriptionDetails();
    try {
      if (subscriptionRequest.getSubscriptionId() != null) {
        Optional<NimaiMCustomer> mCustomer = this.userRepository.findByUserId(userId);
        NimaiSubscriptionDetails details = this.subscriptionDetailsRepository.findByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
        if (mCustomer.isPresent()) {
          List<NimaiSubscriptionDetails> subscriptionEntity = this.subscriptionDetailsRepository.findAllByUserId(userId);
          if (!subscriptionEntity.isEmpty()) {
            for (NimaiSubscriptionDetails plan : subscriptionEntity) {
              if (plan.getSubsidiaryUtilizedCount() >
                      Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue())
                if (userId.substring(0, 2).equalsIgnoreCase("CU")) {
                  response.setStatus("Failure");
                  response.setErrMessage("You had already Active Subsidiary. Kindly select appropriate Plan.");
                  return new ResponseEntity(response, HttpStatus.OK);
                }
              if ((plan.getSubscriptionEndDate().after(today) || plan
                      .getSubscriptionEndDate().compareTo(today) <= 0) &&
                      Integer.valueOf(plan.getlCount()).intValue() - plan.getLcUtilizedCount() > 0) {
                if (plan.getSubscriptionEndDate().compareTo(today) <= 0) {
                  days = (int) ((plan.getSubscriptionEndDate().getTime() - today.getTime()) / 86400000L);
                } else {
                  days = (int) ((plan.getSubscriptionEndDate().getTime() - today.getTime()) / 86400000L) + 1;
                }
                if (!plan.getPaymentStatus().equalsIgnoreCase("Rejected")) {
                  addOnCredit = Integer.valueOf(plan.getlCount()).intValue() - plan.getLcUtilizedCount();
                } else {
                  addOnCredit = 0;
                }
                System.out.println("addOnCredit:" + addOnCredit);
              }
              plan.setStatus("Inactive");
              this.subscriptionDetailsRepository.save(plan);
              int utilizedCount=plan.getLcUtilizedCount();
              int obtainlcCount=0,derivedCount,finalUtilized;
              System.out.println("utilizedCount: "+utilizedCount);
              if(plan.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN"))
              {
            	  
            	
            	  

            	  System.out.println("Renewing from postpaid....1");
            	  System.out.println("Renewing from postpaid....2"+lcUtilizedCount);
            	  obtainlcCount=Integer.valueOf(subscriptionRequest.getLcCount()).intValue();
            	  System.out.println("subscriptionRequest.getLcCount()).intValue(): "+obtainlcCount);
            	  derivedCount=Integer.valueOf(plan.getlCount())-utilizedCount;
            	  System.out.println("derivedCount: "+derivedCount);
            	  finalUtilized=obtainlcCount+(derivedCount);
//            	  if(utilizedCount==0)
//            	  {
//            		  System.out.println("Previous utilized count is 0");
//            		  utilzedLcCount=0;
//            	  }else 
            	  if(subscriptionRequest.getFlag().equalsIgnoreCase("renew")
            			  && lcUtilizedCount > 0) {
            		  System.out.println("Postpaid to prepaid"+lcUtilizedCount);
            		  utilzedLcCount = lcUtilizedCount;
             	 }
            	  else
            	  {
            		  System.out.println("Previous utilized count is not 0");
            		  utilzedLcCount=finalUtilized;
            	  }
            	  subscriptionRequest.setLcCount(""+obtainlcCount);
            	  //subscriptionRequest.
            	  System.out.println("finalUtilized: "+finalUtilized);
            	  System.out.println("utilzedLcCount: "+utilzedLcCount);
            	  System.out.println("subscriptionRequest.setLcCount(): "+subscriptionRequest.getLcCount());
              }
            }
          } else {
             inactiveSubscriptionEntity = this.subscriptionDetailsRepository.findOnlyLatestInactiveSubscriptionByUserId(userId);
            int noOfDays = (int) ((today.getTime() - inactiveSubscriptionEntity.getSubscriptionEndDate().getTime()) / 86400000L);
            System.out.println("Diff between exp and current date: " + noOfDays);
            if (inactiveSubscriptionEntity.getSubsidiaryUtilizedCount() >=
                    Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue() && userId.substring(0, 2).equalsIgnoreCase("CU")) {
              response.setStatus("Failure");
              response.setErrMessage("You had already Active Subsidiary. Kindly select appropriate Plan.");
              return new ResponseEntity(response, HttpStatus.OK);
            }
            if (noOfDays < 60 && Integer.valueOf(inactiveSubscriptionEntity.getlCount()).intValue() - inactiveSubscriptionEntity
                    .getLcUtilizedCount() > 0)
              if (!inactiveSubscriptionEntity.getPaymentStatus().equalsIgnoreCase("Rejected")) {
                addOnCredit = Integer.valueOf(inactiveSubscriptionEntity.getlCount()).intValue() - inactiveSubscriptionEntity.getLcUtilizedCount();
              } else {
                addOnCredit = 0;
              }
          }
          System.out.println("AddOnCredit: " + addOnCredit);
          System.out.println("UtilizedLcCount: " + utilzedLcCount);
          
          
          
          NimaiSubscriptionDetails subScriptionDetails = new NimaiSubscriptionDetails();
          NimaiEmailScheduler schedularData = new NimaiEmailScheduler();
          subScriptionDetails.setSubscriptionName(subscriptionRequest.getSubscriptionName());
          subScriptionDetails.setUserid(mCustomer.get());
          subScriptionDetails.setSubscriptionValidity(subscriptionRequest.getSubscriptionValidity());
          subScriptionDetails.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          subScriptionDetails.setRemark(subscriptionRequest.getRemark());
          subScriptionDetails.setSubscriptionAmount(subscriptionRequest.getSubscriptionAmount());
          subScriptionDetails
                  .setlCount(String.valueOf(Integer.valueOf(subscriptionRequest.getLcCount()).intValue() + addOnCredit));
          subScriptionDetails.setLcUtilizedCount(utilzedLcCount);
          
          
          subScriptionDetails.setSubsidiaries(subscriptionRequest.getSubsidiaries());
          subScriptionDetails.setIsVasApplied(subscriptionRequest.getIsVasApplied());
          subScriptionDetails.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          subScriptionDetails.setVasAmount(subscriptionRequest.getVasAmount());
          subScriptionDetails.setDiscountId(subscriptionRequest.getDiscountId());
          subScriptionDetails.setDiscount(subscriptionRequest.getDiscount());
          System.out.println("Grand Amount: " + subscriptionRequest.getGrandAmount());
          Double toBeTruncated = new Double(subscriptionRequest.getGrandAmount().doubleValue());
          Double truncatedDouble = Double.valueOf(BigDecimal.valueOf(toBeTruncated.doubleValue())
                  .setScale(2, RoundingMode.HALF_UP)
                  .doubleValue());
          subScriptionDetails.setGrandAmount(truncatedDouble);
          subScriptionDetails.setCustomerSupport(subscriptionRequest.getCustomerSupport());
          subScriptionDetails.setInsertedBy(((NimaiMCustomer) mCustomer.get()).getFirstName());
          subScriptionDetails.setsPLanCountry(((NimaiMCustomer) mCustomer.get()).getAddress3());
          subScriptionDetails.setInsertedDate(new Date());
          NimaiSubscriptionDetails inactiveSubscriptionEntity2 = this.subscriptionDetailsRepository.findOnlyLatestInactiveSubscriptionByUserId(userId);
          System.out.println("" + inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount());
          System.out.println("" + subscriptionRequest.getSubsidiaries());
          if (inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount() == Integer.valueOf(subscriptionRequest.getSubsidiaries()).intValue()) {
            subScriptionDetails.setSubsidiaryUtilizedCount(inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount());
          } else if (inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount() == 0) {
            subScriptionDetails.setSubsidiaryUtilizedCount(0);
          } else {
            subScriptionDetails.setSubsidiaryUtilizedCount(Integer.valueOf(inactiveSubscriptionEntity2.getSubsidiaryUtilizedCount()).intValue());
          }
          String customerType = subscriptionRequest.getSubscriptionId().substring(0, 2);
          if (customerType.equalsIgnoreCase("BA")) {
            subScriptionDetails.setCustomerType("Bank");
          } else {
            subScriptionDetails.setCustomerType("Customer");
          }
          SPlanUniqueNumber endDate = new SPlanUniqueNumber();
          int year = endDate.getNoOfyears(subScriptionDetails.getSubscriptionValidity());
          int month = endDate.getNoOfMonths(subScriptionDetails.getSubscriptionValidity());
          System.out.println(year);
          System.out.println(month);
          subScriptionDetails.setStatus("ACTIVE");
          cal.add(5, days);
          cal.add(1, year);
          cal.add(2, month);
          Date sPlanEndDate = cal.getTime();
          subScriptionDetails.setSubscriptionStartDate(today);
          subScriptionDetails.setSubscriptionEndDate(sPlanEndDate);
          subScriptionDetails.setRenewalEmailStatus("Pending");
          System.out.println("Current Date: " + today);
          if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
            subScriptionDetails.setPaymentMode("Wire");
            subScriptionDetails.setPaymentStatus("Pending");
          } else {
            subScriptionDetails.setPaymentMode("Credit");
            subScriptionDetails.setPaymentStatus("Approved");
          }
          NimaiSubscriptionDetails subScription = (NimaiSubscriptionDetails) this.subscriptionDetailsRepository.save(subScriptionDetails);
          System.out.println("Grand Amount after save: " + subScription.getGrandAmount());
          if (subscriptionRequest.getModeOfPayment().equalsIgnoreCase("Wire")) {
            this.advService.inactiveVASStatus(userId);
            this.userRepository.updatePaymentStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            //String invoiceId = generatePaymentTtransactionID(10);
            
            String invoiceId = generatePaymentTtransactionID(10);
            paymentTrId = generatePaymentTtransactionID(15);
            //paymentTrId = generatePaymentTtransactionID(15);
            
            this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
            this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                    .get()).getUserid());
            this.subscriptionDetailsRepository.updatePaymentTxnIdInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId, invoiceId);
            Double gstValue = Double.valueOf(this.subscriptionRepo.getGSTValue().doubleValue() / 100.0D);
            Double planPriceGST = Double.valueOf(subScription.getGrandAmount().doubleValue() + subScription.getGrandAmount().doubleValue() * gstValue.doubleValue());
            System.out.println("gstValue: " + gstValue);
            System.out.println("planPriceGST: " + planPriceGST);
            String finalPrice = String.format("%.2f", new Object[]{planPriceGST});
            this.subscriptionDetailsRepository.updatePaymentTxnIdForWire(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentTrId, invoiceId, finalPrice);
            if (Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue() < Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue()) {
              utilzedLcCount = Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue() - Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue();
              this.subscriptionDetailsRepository.updateLCUtilzed(((NimaiMCustomer) mCustomer.get()).getUserid(), Integer.valueOf(utilzedLcCount));
            }
          } else {
            this.userRepository.updatePaymentMode(subscriptionRequest.getModeOfPayment(), ((NimaiMCustomer) mCustomer
                    .get()).getUserid());
            this.userRepository.updatePlanPurchasedStatus(((NimaiMCustomer) mCustomer.get()).getUserid());
            this.userRepository.updatePaymentStatusForCredit(((NimaiMCustomer) mCustomer.get()).getUserid());
            OnlinePayment paymentDet = this.onlinePaymentRepo.getDetailsByUserId(((NimaiMCustomer) mCustomer.get()).getUserid());
            if (subscriptionRequest.getGrandAmount().doubleValue() == 0.0D) {
              String invoiceId = generatePaymentTtransactionID(10);
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
              this.subscriptionDetailsRepository.updateInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), invoiceId);
            } else {
              this.userRepository.updatePaymentTransactionId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getInvoiceId());
              this.subscriptionDetailsRepository.updatePaymentTxnIdInvId(((NimaiMCustomer) mCustomer.get()).getUserid(), paymentDet.getOrderId(), paymentDet.getInvoiceId());
            }
            if (Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue() < Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue()) {
              utilzedLcCount = Integer.valueOf(inactiveSubscriptionEntity2.getLcUtilizedCount()).intValue() - Integer.valueOf(inactiveSubscriptionEntity2.getlCount()).intValue();
              this.subscriptionDetailsRepository.updateLCUtilzed(((NimaiMCustomer) mCustomer.get()).getUserid(), Integer.valueOf(utilzedLcCount));
            }
          }
          schedularData.setUserid(((NimaiMCustomer) mCustomer.get()).getUserid());
          String sPlanValidity = Integer.toString(subscriptionRequest.getSubscriptionValidity());
          String sPlanAmount = Integer.toString(subscriptionRequest.getSubscriptionAmount());
          schedularData.setSubscriptionId(subscriptionRequest.getSubscriptionId());
          schedularData.setCustomerSupport(subscriptionRequest.getCustomerSupport());
          schedularData.setRelationshipManager(subscriptionRequest.getRelationshipManager());
          schedularData.setSubscriptionAmount(sPlanAmount);
          if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("BA")) {
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(((NimaiMCustomer) mCustomer.get()).getEmailAddress());
          } else if (subscriptionRequest.getUserId().substring(0, 2).equalsIgnoreCase("CU") || subscriptionRequest
                  .getUserId().substring(0, 2).equalsIgnoreCase("BC")) {
            String emailId = "";
            if (subscriptionRequest.getEmailID() != null) {
              emailId = subscriptionRequest.getEmailID() + "," + ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            } else {
              emailId = ((NimaiMCustomer) mCustomer.get()).getEmailAddress();
            }
            schedularData.setUserName(((NimaiMCustomer) mCustomer.get()).getFirstName());
            schedularData.setEmailId(emailId);
          }
          schedularData.setSubscriptionEndDate(sPlanEndDate);
          schedularData.setSubscriptionStartDate(today);
          schedularData.setSubscriptionName(subscriptionRequest.getSubscriptionName());
          schedularData.setSubscriptionValidity(sPlanValidity);
          schedularData.setEmailStatus("pending");
          schedularData.setEvent("Cust_Splan_email");
          schedularData.setInsertedDate(today);
          NimaiEmailScheduler emailData = (NimaiEmailScheduler) this.emailDetailsRepository.save(schedularData);
          response.setErrCode("ASA001");
          response.setErrMessage("Subscription Plan Renewed Successfully.");
          response.setData(paymentTrId);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setStatus("Failure");
        response.setErrCode("ASA003");
        response.setErrMessage(ErrorDescription.getDescription("ASA003"));
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
      }
      response.setStatus("Failure");
      response.setErrCode("ASA009");
      response.setErrMessage(ErrorDescription.getDescription("ASA009"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000") + e.getMessage());
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }

}





