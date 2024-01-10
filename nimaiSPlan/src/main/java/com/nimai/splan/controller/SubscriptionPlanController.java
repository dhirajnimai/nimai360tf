package com.nimai.splan.controller;

import com.nimai.splan.model.NimaiAdvisory;

import com.nimai.splan.model.NimaiCustomerSubscriptionGrandAmount;
import com.nimai.splan.model.NimaiMCustomer;
import com.nimai.splan.model.NimaiMSubscription;
import com.nimai.splan.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.splan.model.NimaiTransactionViewCount;
import com.nimai.splan.model.OnlinePayment;
import com.nimai.splan.payload.CustomerSubscriptionGrandAmountBean;
import com.nimai.splan.payload.EditPostpaidBean;
import com.nimai.splan.payload.GenericResponse;
import com.nimai.splan.payload.PaypalPaymentRequest;
import com.nimai.splan.payload.SplanRequest;
import com.nimai.splan.payload.SubscriptionAndPaymentBean;
import com.nimai.splan.payload.SubscriptionBean;
import com.nimai.splan.payload.PostpaidSubscriptionBean;
import com.nimai.splan.payload.SubscriptionPaymentBean;
import com.nimai.splan.payload.SubscriptionPlanResponse;
import com.nimai.splan.payload.TransactionPostPaidDetail;
import com.nimai.splan.repository.NimaiPostpaidSubscriptionDetailsUpdRepo;
import com.nimai.splan.repository.OnlinePaymentRepo;
import com.nimai.splan.service.NimaiAdvisoryService;
import com.nimai.splan.service.PaypalService;
import com.nimai.splan.service.SubscriptionPlanService;
import com.nimai.splan.service.ValidateCoupenService;
import com.nimai.splan.utility.Credentials;
import com.nimai.splan.utility.ErrorDescription;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.http.HttpRequest;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.Capture;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersGetRequest;
import com.paypal.orders.PurchaseUnit;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"*"})
@RestController
public class SubscriptionPlanController {
  private static Logger logger = LoggerFactory.getLogger(SubscriptionPlanController.class);
  
  @Autowired
  private SubscriptionPlanService sPlanService;
  
  @Autowired
  private NimaiAdvisoryService advService;
  
  @Autowired
  private ValidateCoupenService couponService;
  
  @Autowired
  OnlinePaymentRepo onlinePaymentRepo;
  
  @Autowired
  NimaiPostpaidSubscriptionDetailsUpdRepo postpaidSPlanRepositoryUpd;
  
  @Value("${payment.redirect.url}")
  private String redirectFromPaymentLink;
  
  public static final String PAYPAL_SUCCESS_URL = "pay/success";
  
  public static final String PAYPAL_CANCEL_URL = "pay/cancel";
  
  @Autowired
  private PaypalService paypalService;
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/saveUserSubscriptionPlan/{userId}/{lcUtilizedCount}"})
  public ResponseEntity<?> saveCustomerSPlan(@PathVariable("userId") String userID, @RequestBody SubscriptionBean subscriptionRequest,@PathVariable("lcUtilizedCount") Integer lcUtilizedCount) {
    GenericResponse response = new GenericResponse();
    String flag = subscriptionRequest.getFlag();
    if ("new".equalsIgnoreCase(flag)) {
      logger.info(" ================ Send saveCustomerSPlan API is Invoked ================:" + userID);
      if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
        System.out.println(subscriptionRequest.toString());
        return this.sPlanService.saveUserSubscriptionPlan(subscriptionRequest, userID);
      } 
      response.setErrCode("ASA014");
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
    logger.info(" ================ renewCustomerSPlan API is Invoked ================:" + userID);
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      System.out.println(subscriptionRequest.toString());
      return this.sPlanService.renewSubscriptionPlanV2(subscriptionRequest, userID,lcUtilizedCount);
    } 
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/saveUserPostPaidSPlan/{userId}/{flagged}"})
  public ResponseEntity<?> savePostPaidSPlan(@PathVariable("userId") String userID, @PathVariable("flagged") String flagged ,@RequestBody SubscriptionBean subscriptionRequest) {
    GenericResponse response = new GenericResponse();
    String flag = subscriptionRequest.getFlag();
    if ("new".equalsIgnoreCase(flag)) {
      logger.info(" ================ Send savePostPaidCustomerOrBank as SPlan================:" + userID);
      if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
        System.out.println(subscriptionRequest.toString());
        return this.sPlanService.saveUserPostPaidSPlan(subscriptionRequest,flagged,userID);
      } 
      response.setErrCode("ASA014");
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
    logger.info(" ================ renewCustomerSPlan API is Invoked ================:" + userID);
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      System.out.println(subscriptionRequest.toString());
      Integer lcUtilizedCount=0;
      return this.sPlanService.renewSubscriptionPlan(subscriptionRequest, userID,lcUtilizedCount);
    } 
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  // This URL is used to call the Service of Postpaid in order to save the details in the
  // @table nimai_postpaid_subscription_details

  /*
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/pushPostpaidSPlanPayment/{userId}/{txnId}/{flag}/{amountField}"})
  public ResponseEntity<?> postpaidSPlanPayment(@PathVariable("userId") String userID, @PathVariable("txnId") String txnId,@PathVariable ("amountField") String amountField, @PathVariable ("flag") String flag,@RequestBody PostpaidSubscriptionBean postpaidSubscriptionRequest) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get the  getPostpaidSPlanPayment as SPlan================:" + userID);
      if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
        System.out.println(postpaidSubscriptionRequest.toString());
        return this.sPlanService.pushPostpaidSPlanPayment(postpaidSubscriptionRequest,userID,txnId,flag,amountField);
      }
      response.setErrCode("ASA014");
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
*/
   /* logger.info(" ================ for transaction API calculation is Invoked ================:" + userID);
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      System.out.println(subscriptionRequest.toString());
      return this.sPlanService.renewSubscriptionPlan(subscriptionRequest, userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);*/
 // }


  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/pushPostpaidSPlanPayment/{userId}/{txnId}"})
  public ResponseEntity<?> postpaidSPlanPayment(@PathVariable("userId") String userID, @PathVariable("txnId") String txnId) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get the  getPostpaidSPlanPayment as SPlan================:" + userID);
      if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
        return this.sPlanService.pushPostpaidSPlanPayment(userID,txnId);
      }
      response.setErrCode("ASA014");
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);

   /* logger.info(" ================ for transaction API calculation is Invoked ================:" + userID);
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      System.out.println(subscriptionRequest.toString());
      return this.sPlanService.renewSubscriptionPlan(subscriptionRequest, userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);*/
  }
  
 
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/pushPostpaidSPlanPayment/{userId}"})
  public ResponseEntity<?> postpaidSPlanPaymentForUnQuotedTxn(@PathVariable("userId") String userID) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Get the  getPostpaidSPlanPayment as SPlan================:" + userID);
    int i;
      if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
    	  /*List<String> txnIds=postpaidSPlanRepositoryUpd.findTransactionIdForUnQuoted(userID);
    	  System.out.println("txnIds: "+txnIds);
    	  System.out.println("Size: "+txnIds.size());
    	  for(i=0;i<txnIds.size();i++)
    	  {
    		  String txnId=txnIds.get(i);
    		  System.out.println("txnId: "+txnId);
    		 // sPlanService.pushPostpaidSPlanPaymentUnQuoted(userID,txnId);
    	  }*/
    	  	response.setStatus("Success");
    	    //response.setErrCode("ASA003");
    	    response.setErrMessage("Plan Purchased Successfully");
    	    return new ResponseEntity(response, HttpStatus.OK);
      }
      response.setErrCode("ASA014");
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);

   /* logger.info(" ================ for transaction API calculation is Invoked ================:" + userID);
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      System.out.println(subscriptionRequest.toString());
      return this.sPlanService.renewSubscriptionPlan(subscriptionRequest, userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);*/
  }

  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/payForpostpaidSPlan/{userId}/{txnId}/{amount}/{discId}/{discAmt}/{mode}"})
  public ResponseEntity<?> payForpostpaidSPlan(@PathVariable("userId") String userID, @PathVariable("txnId") String txnId,  @PathVariable("amount") String amount, 
		  @PathVariable("discId") String discId,@PathVariable("discAmt") String discAmt,@PathVariable("mode") String mode) {
    GenericResponse response = new GenericResponse();
    logger.info(" ================ Pay for Postpaid SPlan================:" + userID);
      if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
        return this.sPlanService.payForPostpaidSubscriptionPlan(userID,txnId,amount,discId,discAmt,mode);
      }
      response.setErrCode("ASA014");
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);

   /* logger.info(" ================ for transaction API calculation is Invoked ================:" + userID);
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      System.out.println(subscriptionRequest.toString());
      return this.sPlanService.renewSubscriptionPlan(subscriptionRequest, userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);*/
  }

  // This URL is used to get the unpaid postpaid amount ( in the QuoteReceived and Dashboard)
  // update @table nimai_postpaid_subscription_details
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/unpaidPostpaidSubscriptionPlan/{userId}/{txnID}"})
  public ResponseEntity<?> unpaidPostpaidSubscriptionPlan(@PathVariable("userId") String userID,@PathVariable("txnID") String txnID) {
    logger.info(" ================ Get the unpaid users record  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      return this.sPlanService.unpaidPostpaidSubscriptionPlan(userID,txnID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/getPostpaidFreezePlacedSubscriptionPlan/{userId}"})
  public ResponseEntity<?> getPostpaidFreezePlacedSubscriptionPlan(@PathVariable("userId") String userID) {
    logger.info(" ================ Get the getPostpaidFreezePlacedSubscriptionPlan users record  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      return this.sPlanService.getPostpaidFreezePlacedSubscriptionPlan(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/overallPostPaid/{userId}"})
  public ResponseEntity<?> overallPostpaidSubscriptionPlan(@PathVariable("userId") String userID) {
    logger.info(" ================ Get the unpaid users record  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      return this.sPlanService.overallPostpaidSubscriptionPlan(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/overallPostPaidBA/{userId}"})
  public ResponseEntity<?> overallPostpaidSubscriptionPlanBA(@PathVariable("userId") String userID) {
    logger.info(" ================ Get the unpaid users record  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      return this.sPlanService.overallPostpaidSubscriptionPlanBA(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  
  
  
  
  
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/getTxnFromPostPaid/{userId}"})
  public ResponseEntity<?> getTxnFromPostPaid(@PathVariable("userId") String userID) {
    logger.info(" ================ Get the txn From postpaid  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      return this.sPlanService.getPostpaidTxnDet(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  
  // This URL is used to get Minimum Due & Total Due and called when we click on click here pay
  // update @table nimai_postpaid_subscription_details
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/getMinAndTotalSubscriptionPlan/{userId}"})
  public ResponseEntity<?> getMinAndTotalSubscriptionPlan(@PathVariable("userId") String userID) {
    logger.info(" ================ getMinAndTotalSubscriptionPlan API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      //System.out.println(subscriptionRequest.toString());
      return this.sPlanService.getMinAndTotalSubscriptionPlan(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  // This URL is used to get Minimum Due & Total Due and called when we click on click here pay
  // update @table nimai_postpaid_subscription_details
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/editPostpaidPlanDetails/{userId}/{txnId}"})
  public ResponseEntity<?> editPostpaidPlanDetails(@PathVariable("userId") String userID, @PathVariable("txnId") String txnId,@RequestBody EditPostpaidBean editBean) {
    logger.info(" ================ editPostpaidPlanDetails API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      //System.out.println(subscriptionRequest.toString());
      return this.sPlanService.editPostpaidPlanDetails(userID,txnId,editBean);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/getApprovedTransactions/{userId}"})
  public ResponseEntity<?> getApprovedTransactions(@PathVariable("userId") String userID) {
    logger.info(" ================ getApprovedTransactions API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      //System.out.println(subscriptionRequest.toString());
      return this.sPlanService.getApprovedTransactions(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }

  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/getPendingTransactions/{userId}"})
  public ResponseEntity<?> getPendingTransactions(@PathVariable("userId") String userID) {
    logger.info(" ================ getPendingTransactions API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      //System.out.println(subscriptionRequest.toString());
      return this.sPlanService.getPendingTransactions(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/renewSubscriptionPlan/{userId}"})
  public ResponseEntity<?> renewCustomerSPlan(@PathVariable("userId") String userID, @RequestBody SubscriptionBean subscriptionRequest) {
    logger.info(" ================ renewCustomerSPlan API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      System.out.println(subscriptionRequest.toString());
      Integer lcUtilizedCount=0;
      return this.sPlanService.renewSubscriptionPlan(subscriptionRequest, userID,lcUtilizedCount);
    } 
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  
  @CrossOrigin({"*"})
  @GetMapping({"/viewSPlanToUser/{userId}"})
  public ResponseEntity<?> viewSPlanToCustomer(@PathVariable("userId") String userId) {
    logger.info(" ================ Send viewSPlanToCustomer API is Invoked ================:" + userId);
    return this.sPlanService.findMSPlanDetails(userId);
  }
  
  @CrossOrigin({"*"})
  @GetMapping({"/list/{userId}"})
  public ResponseEntity<?> findAllSPlanDetailsByUserId(@PathVariable("userId") String userId) {
    logger.info(" ================ Send findAllSPlanDetailsByUserId API is Invoked ================:" + userId);
    return this.sPlanService.findSPlanDetailsByUserId(userId);
  }
  
  @CrossOrigin({"*"})
  @GetMapping({"/getSPlan/{userId}"})
  public ResponseEntity<?> getSPlanByUserId(@PathVariable("userId") String userId) {
    logger.info(" ================ Send getSPlanByUserId API is Invoked ================:" + userId);
    return this.sPlanService.getSPlanByUserId(userId);
  }
  
  @CrossOrigin({"*"})
  @GetMapping({"/getInactiveSPlan/{userId}"})
  public ResponseEntity<?> getInactiveSPlanByUserId(@PathVariable("userId") String userId) {
    logger.info(" ================ Send getInactiveSPlanByUserId API is Invoked ================:" + userId);
    return this.sPlanService.getInactiveSPlanByUserId(userId);
  }
  
  @CrossOrigin({"*"})
  @PostMapping({"/viewCustomerSPlan"})
  public ResponseEntity<?> ViewCustomerSPlans(@RequestBody SplanRequest sPLanRequest) {
    logger.info(" ================ Send ViewCustomerSPlans API is Invoked ================:" + sPLanRequest
        .getUserId());
    GenericResponse response = new GenericResponse();
    if (!sPLanRequest.getUserId().substring(0, 2).equalsIgnoreCase("RE"))
      return this.sPlanService.findCustomerSPlanDetails(sPLanRequest); 
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @GetMapping({"/viewAllCustomerSPlan/{userId}"})
  public ResponseEntity<?> viewAllCustomerSPlans(@PathVariable("userId") String userID) {
    logger.info(" ================ Send ViewAllCustomerSPlans API is Invoked ================:");
    GenericResponse response = new GenericResponse();
    return this.sPlanService.findAllSPlanDetailsForCustomer(userID);
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/viewSubscriptionBySubscriptionId"})
  public ResponseEntity<?> viewSPlanBySPlanId(@RequestBody SubscriptionPlanResponse srb) {
    logger.info(" ================ Send ViewSPlans By SPlanID API is Invoked ================:");
    GenericResponse response = new GenericResponse();
    NimaiMSubscription subData = this.sPlanService.getPlanDetailsBySubscriptionId(srb.getSubscriptionId());
    response.setData(subData);
    response.setStatus("Success");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/initiatePG"})
  public ResponseEntity<?> initiatePG(@RequestBody SubscriptionPaymentBean sPymentRequest) {
    logger.info(" ================ Send Payment Request ================:" + sPymentRequest
            .getUserId());
    GenericResponse response = new GenericResponse();
    Double vasAmt = Double.valueOf(0.0D);
    try {
      Double grandAmt;
      String subsCurrency;
      NimaiCustomerSubscriptionGrandAmount ncsga = this.sPlanService.getCustomerAmount(sPymentRequest.getUserId());
      try {
        NimaiMSubscription subsDet = this.sPlanService.getPlanDetailsBySubscriptionId(sPymentRequest.getMerchantParam2());
        subsCurrency = subsDet.getCurrency();
        Double subsAmt = Double.valueOf(subsDet.getSubscriptionAmount());
        int vasCount = StringUtils.countOccurrencesOf(sPymentRequest.getMerchantParam4(), "-");
        System.out.println("Total VAS: " + vasCount);
        String[] vasSplitted = sPymentRequest.getMerchantParam4().split("-", vasCount + 1);
        for (int i = 0; i < vasCount; i++) {
          System.out.println("Iteration: " + i);
          System.out.println("VAS: " + vasSplitted[i]);
          NimaiAdvisory vasDet = this.advService.getVasDetails(vasSplitted[i]);
          if (vasDet == null) {
            vasAmt = Double.valueOf(0.0D);
          } else {
            vasAmt = Double.valueOf(vasAmt.doubleValue() + vasDet.getPricing().floatValue());
          }
        }
        System.out.println("VAS Amount: " + vasAmt);
        System.out.println("Discount Amt: " + vasSplitted[vasCount]);
        Double discAmt = Double.valueOf(vasSplitted[vasCount]);
        grandAmt = Double.valueOf(subsAmt.doubleValue() + vasAmt.doubleValue() - discAmt.doubleValue());
        System.out.println("Grand Amount: " + grandAmt);
      } catch (Exception e) {
        response.setStatus("Failure");
        response.setData("OOPs! Something went wrong. Please Buy Plan again");
        return new ResponseEntity(response, HttpStatus.OK);
      }
      System.out.println("Amount Calculated: " + grandAmt);
      if (sPymentRequest.getMerchantParam3().equalsIgnoreCase("renew")) {
        if (Double.compare(ncsga.getGrandAmount().doubleValue(), grandAmt.doubleValue()) == 0) {
          Map<String, Object> map = this.sPlanService.initiatePayment(sPymentRequest, ncsga.getGrandAmount(), subsCurrency);
          response.setStatus("Success");
          response.setData(map);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setStatus("Failure");
        response.setData("OOPs! Something went wrong. Please Buy Plan again");
        return new ResponseEntity(response, HttpStatus.OK);
      }
      Map<String, Object> spb = this.sPlanService.initiatePayment(sPymentRequest, grandAmt, subsCurrency);
      response.setStatus("Success");
      response.setData(spb);
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setErrCode("ASA014");
      System.out.println("Error: " + e);
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }


  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/initiatePGForPostpaid"})
  public ResponseEntity<?> initiatePGForPostpaid(@RequestBody SubscriptionPaymentBean sPymentRequest) {
    logger.info(" ================ Send Payment Request For Postpaid ================:" + sPymentRequest
            .getUserId());
    GenericResponse response = new GenericResponse();
    Double vasAmt = Double.valueOf(0.0D);
    try {
      Double grandAmt;
      String subsCurrency;
      System.out.println("Payment request payment userID :"+sPymentRequest.getUserId());
      NimaiCustomerSubscriptionGrandAmount ncsga = this.sPlanService.getCustomerAmount(sPymentRequest.getUserId());
      try {
        NimaiMSubscription subsDet = this.sPlanService.getPlanDetailsBySubscriptionId(sPymentRequest.getMerchantParam2());
        subsCurrency = sPymentRequest.getCurrency();
        //Double subsAmt = Double.valueOf(subsDet.getSubscriptionAmount());

        String[] key =sPymentRequest.getMerchantParam5().split(":");
        String amount="";
        String dueType="";
        if(key != null) {
          dueType = key[0].toString();
          System.out.println("dueType in Paypal:" + dueType);
          amount = key[1].toString();
          System.out.println("amount in Paypal:" + amount);
        }
        int vasCount = StringUtils.countOccurrencesOf(sPymentRequest.getMerchantParam4(), "-");
        System.out.println("Total VAS: " + vasCount);
        String[] vasSplitted = sPymentRequest.getMerchantParam4().split("-", vasCount + 1);
        for (int i = 0; i < vasCount; i++) {
          System.out.println("Iteration: " + i);
          System.out.println("VAS: " + vasSplitted[i]);
          NimaiAdvisory vasDet = this.advService.getVasDetails(vasSplitted[i]);
          if (vasDet == null) {
            vasAmt = Double.valueOf(0.0D);
          } else {
            vasAmt = Double.valueOf(0.0D);
          }
        }
        System.out.println("VAS Amount: " + vasAmt);
        System.out.println("Discount Amt: " + vasSplitted[vasCount]);
        Double discAmt = Double.valueOf(0.0D);//;Double.valueOf(vasSplitted[vasCount]);
        grandAmt = Double.valueOf(Double.valueOf(amount )+ vasAmt.doubleValue() - discAmt.doubleValue());
        System.out.println("Grand Amount: " + grandAmt);
      } catch (Exception e) {
        response.setStatus("Failure");
        response.setData("OOPs! Something went wrong. Please Buy Plan again");
        return new ResponseEntity(response, HttpStatus.OK);
      }
      System.out.println("Amount Calculated: " + grandAmt);

      if (sPymentRequest.getMerchantParam3().equalsIgnoreCase("renew")) {
        if (Double.compare(ncsga.getGrandAmount().doubleValue(), grandAmt.doubleValue()) == 0) {
          Map<String, Object> map = this.sPlanService.initiatePaymentForPostpaid(sPymentRequest, grandAmt, subsCurrency);
          response.setStatus("Success");
          response.setData(map);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        response.setStatus("Failure");
        response.setData("OOPs! Something went wrong. Please Buy Plan again");
        return new ResponseEntity(response, HttpStatus.OK);
      }
      Map<String, Object> spb = this.sPlanService.initiatePaymentForPostpaid(sPymentRequest, grandAmt, subsCurrency);
      response.setStatus("Success");
      response.setData(spb);
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setErrCode("ASA014");
      System.out.println("Error: " + e);
      response.setErrMessage(ErrorDescription.getDescription("ASA014"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }
  }


  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/PGResponse"})
  public ResponseEntity<?> pgResponse(@RequestBody PaypalPaymentRequest ppRequest) throws IOException, ServletException {
    logger.info(" ================ Getting Payent Response ================");
    GenericResponse response = new GenericResponse();
    String userId = ppRequest.getUserId();
    String payerId = ppRequest.getPayerId();
    String orderId = ppRequest.getOrderId();
    try {
      System.out.println("OrderId: " + orderId);
      System.out.println("PayerId: " + payerId);
      if (payerId == null) {
        response.setStatus("Cancelled");
        return new ResponseEntity(response, HttpStatus.OK);
      } 
      try {
        Map<String, Object> spb = this.sPlanService.executePayment(orderId);
        String sts = spb.get("OrderStatus").toString();
        System.out.println("Order Status: " + sts);
        Order order = null;
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        HttpResponse<Order> responseCapture = Credentials.client.execute((HttpRequest)request);
        order = (Order)responseCapture.result();
        System.out.println("Order Capture Status: " + order.status());
        String paymentStatus = ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).status();
        System.out.println("Payment Status: " + paymentStatus);
        System.out.println("Payment Txn Id: " + ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).id());
        logger.info("Order Id: " + orderId);
        logger.info("Payment Txn Id: " + ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).id());
        logger.info("Order Status: " + sts);
        logger.info("Order Capture Status: " + order.status());
        logger.info("Payment Status: " + paymentStatus);
        if (sts.equalsIgnoreCase("Approved") && order.status().equalsIgnoreCase("Completed") && paymentStatus.equalsIgnoreCase("Completed")) {
          logger.info("Order Id: " + orderId);
          logger.info("Payment Txn Id: " + ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).id());
          logger.info("Order Status: " + sts);
          logger.info("Order Capture Status: " + order.status());
          logger.info("Payment Status: " + paymentStatus);
          this.sPlanService.saveData(orderId, "");
          
          response.setStatus("Success");
          response.setData(spb);
          return new ResponseEntity(response, HttpStatus.OK);
        } 
        if (sts.equalsIgnoreCase("Approved")) {
          this.sPlanService.saveData(orderId, "Failed");
          response.setStatus("Failed");
          response.setData("Transaction Failed");
          return new ResponseEntity(response, HttpStatus.OK);
        } 
      } catch (PayPalRESTException e) {
        e.printStackTrace();
        this.sPlanService.saveData(orderId, "Failed");
        response.setStatus("Failed");
        response.setData("Transaction Failed");
        return new ResponseEntity(response, HttpStatus.OK);
      } catch (HttpException he) {
        System.out.println("Exception Decline: " + he.statusCode());
        if (he.statusCode() == 422) {
          Order order1 = null;
          OrdersGetRequest request = new OrdersGetRequest(orderId);
          HttpResponse<Order> responsePaypal = Credentials.client.execute((HttpRequest)request);
          order1 = (Order)responsePaypal.result();
          for (LinkDescription o : order1.links())
            System.out.println("---" + o.rel() + "---" + o.href()); 
          System.out.println("Link: " + ((LinkDescription)order1.links().get(0)).href());
          response.setStatus("Declined");
          response.setData("Declined");
          return new ResponseEntity(response, HttpStatus.OK);
        } 
        this.sPlanService.saveData(orderId, "Failed");
        response.setStatus("Failed");
        response.setData("Transaction Failed");
        return new ResponseEntity(response, HttpStatus.OK);
      } 
    } catch (IllegalArgumentException e) {
      response.setStatus("Cancelled");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    response.setStatus("OK");
    return new ResponseEntity(response, HttpStatus.OK);
  }

  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/PGResponseForPostpaid"})
  public ResponseEntity<?> PGResponseForPostpaid(@RequestBody PaypalPaymentRequest ppRequest) throws IOException, ServletException {
    logger.info(" ================ Getting PGResponseForPostpaid Payment Response ================");
    GenericResponse response = new GenericResponse();
    String userId = ppRequest.getUserId();
    String payerId = ppRequest.getPayerId();
    String orderId = ppRequest.getOrderId();
    try {
      System.out.println("OrderId: " + orderId);
      System.out.println("PayerId: " + payerId);
      if (payerId == null) {
        response.setStatus("Cancelled");
        return new ResponseEntity(response, HttpStatus.OK);
      }
      try {
        Map<String, Object> spb = this.sPlanService.executePaymentPostPaid(orderId);
        String sts = spb.get("OrderStatus").toString();
        System.out.println("Order Status: " + sts);
        Order order = null;
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        HttpResponse<Order> responseCapture = Credentials.client.execute((HttpRequest)request);
        order = (Order)responseCapture.result();
        System.out.println("Order Capture Status: " + order.status());
        String paymentStatus = ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).status();
        System.out.println("Payment Status: " + paymentStatus);
        System.out.println("Payment Txn Id: " + ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).id());
        logger.info("Order Id: " + orderId);
        logger.info("Payment Txn Id: " + ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).id());
        logger.info("Order Status: " + sts);
        logger.info("Order Capture Status: " + order.status());
        logger.info("Payment Status: " + paymentStatus);
        if (sts.equalsIgnoreCase("Approved") && order.status().equalsIgnoreCase("Completed") && paymentStatus.equalsIgnoreCase("Completed")) {
          logger.info("Order Id: " + orderId);
          logger.info("Payment Txn Id: " + ((Capture)((PurchaseUnit)order.purchaseUnits().get(0)).payments().captures().get(0)).id());
          logger.info("Order Status: " + sts);
          logger.info("Order Capture Status: " + order.status());
          logger.info("Payment Status: " + paymentStatus);
          this.sPlanService.saveDataPostpaid(orderId, "");
          response.setStatus("Success");
          response.setData(spb);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        if (sts.equalsIgnoreCase("Approved")) {
          this.sPlanService.saveData(orderId, "Failed");
          response.setStatus("Failed");
          response.setData("Transaction Failed");
          return new ResponseEntity(response, HttpStatus.OK);
        }
      } catch (PayPalRESTException e) {
        e.printStackTrace();
        this.sPlanService.saveData(orderId, "Failed");
        response.setStatus("Failed");
        response.setData("Transaction Failed");
        return new ResponseEntity(response, HttpStatus.OK);
      } catch (HttpException he) {
        System.out.println("Exception Decline: " + he.statusCode());
        if (he.statusCode() == 422) {
          Order order1 = null;
          OrdersGetRequest request = new OrdersGetRequest(orderId);
          HttpResponse<Order> responsePaypal = Credentials.client.execute((HttpRequest)request);
          order1 = (Order)responsePaypal.result();
          for (LinkDescription o : order1.links())
            System.out.println("---" + o.rel() + "---" + o.href());
          System.out.println("Link: " + ((LinkDescription)order1.links().get(0)).href());
          response.setStatus("Declined");
          response.setData("Declined");
          return new ResponseEntity(response, HttpStatus.OK);
        }
        this.sPlanService.saveData(orderId, "Failed");
        response.setStatus("Failed");
        response.setData("Transaction Failed");
        return new ResponseEntity(response, HttpStatus.OK);
      }
    } catch (IllegalArgumentException e) {
      response.setStatus("Cancelled");
      return new ResponseEntity(response, HttpStatus.OK);
    }
    response.setStatus("OK");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/checkPaymentStatus"})
  public ResponseEntity<?> checkPGStatus(@RequestBody SubscriptionPaymentBean sPymentRequest) {
    logger.info(" ================ Check Payment Status ================:" + sPymentRequest
        .getUserId());
    GenericResponse response = new GenericResponse();
    try {
      OnlinePayment spb = this.sPlanService.checkPayment(sPymentRequest);
      if (spb.getStatus().equalsIgnoreCase("Success")) {
        response.setStatus("Success");
        response.setData(spb);
        return new ResponseEntity(response, HttpStatus.OK);
      } 
      response.setStatus("Failure");
      response.setData(spb);
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setErrCode("ASA014");
      System.out.println("Error: " + e);
      response.setErrMessage("Something Went Wrong!");
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/checkSubsidiary"})
  public ResponseEntity<?> saveCustomerSPlan(@RequestBody SubscriptionBean subscriptionRequest) {
    GenericResponse response = new GenericResponse();
    return this.sPlanService.checkForSubsidiary(subscriptionRequest);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/continueBuy"})
  public ResponseEntity<?> saveGrandAmount(@RequestBody CustomerSubscriptionGrandAmountBean subscriptionRequest) {
    Double discAmt;
    NimaiCustomerSubscriptionGrandAmount ncsga;
    System.out.println("======= Continue Buy =======");
    Double vasAmt = Double.valueOf(0.0D);
    GenericResponse response = new GenericResponse();
    NimaiMSubscription subsDet = this.sPlanService.getPlanDetailsBySubscriptionId(subscriptionRequest.getSubscriptionId());
    Double subsAmt = Double.valueOf(subsDet.getSubscriptionAmount());
    if (subscriptionRequest.getDiscountId().doubleValue() == 0.0D) {
      discAmt = Double.valueOf(0.0D);
    } else {
      HashMap<String, Double> discData = this.couponService.discountCalculate(subscriptionRequest.getDiscountId(), subscriptionRequest.getSubscriptionId());
      discAmt = discData.get("discount");
    } 
    if (subscriptionRequest.getVasId().equalsIgnoreCase("0")) {
      vasAmt = Double.valueOf(0.0D);
    } else {
      System.out.println("VAS Purchased: " + subscriptionRequest.getVasId());
      Double vasPrice = Double.valueOf(0.0D);
      int vasCount = StringUtils.countOccurrencesOf(subscriptionRequest.getVasId(), "-");
      System.out.println("Total VAS: " + vasCount + '\001');
      String[] vasSplitted = subscriptionRequest.getVasId().split("-", vasCount + 1);
      for (int i = 0; i < vasCount + 1; i++) {
        System.out.println("Iteration: " + i);
        NimaiAdvisory vasDet = this.advService.getVasDetails(vasSplitted[i]);
        vasPrice = Double.valueOf(vasDet.getPricing().floatValue());
        vasAmt = Double.valueOf(vasAmt.doubleValue() + vasPrice.doubleValue());
      } 
    } 
    Double calculatedAmt = Double.valueOf(subsAmt.doubleValue() + vasAmt.doubleValue() - discAmt.doubleValue());
    if (Double.compare(calculatedAmt.doubleValue(), subscriptionRequest.getGrandAmount().doubleValue()) != 0) {
      response.setStatus("Failure");
      response.setData("OOPs! Something Went Wrong. Please Buy Plan Again.");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    try {
      ncsga = this.sPlanService.getCustomerAmount(subscriptionRequest.getUserId());
      System.out.println("Data: " + ncsga.getDiscountApplied());
    } catch (Exception e) {
      return this.sPlanService.insertGrandAmountData(subscriptionRequest);
    } 
    if (ncsga.getDiscountApplied() == null || ncsga.getVasApplied() == null)
      return this.sPlanService.insertGrandAmountData(subscriptionRequest); 
    if (Double.compare(ncsga.getGrandAmount().doubleValue(), subscriptionRequest.getGrandAmount().doubleValue()) == 0) {
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    response.setStatus("Failure");
    response.setData("OOPs! Something Went Wrong. Please Apply Discount Again");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/verifyPayment"})
  public ResponseEntity<?> verifyPayment(@RequestBody CustomerSubscriptionGrandAmountBean subscriptionRequest) {
    GenericResponse response = new GenericResponse();
    int id = subscriptionRequest.getId().intValue();
    Double amt = subscriptionRequest.getGrandAmount();
    boolean verify = this.sPlanService.checkPaymentData(id, amt);
    if (verify) {
      response.setStatus("Success");
      response.setData("Verified");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    response.setStatus("Failure");
    response.setData("OOPs! Something Went Wrong. Please Apply Discount Again");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/getPreviousPlans/{planType}"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<Object> getCount(@RequestBody NimaiMCustomer nimaicustomer, HttpServletRequest request,@PathVariable("planType") String planType) {
    logger.info("======== Getting previously purchased plan =========");
    GenericResponse response = new GenericResponse();
    try {
      String userId = nimaicustomer.getUserid();
      List<SubscriptionAndPaymentBean> spb = this.sPlanService.getLastPurchasedPlan(userId,planType);
      response.setData(spb);
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/getTransactionOfPendingMarApproved/{userId}"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<Object> getCount(HttpServletRequest request,@PathVariable("userId") String userId) {
    logger.info("======== Get Transaction Of Pending Marker Approved =========");
    GenericResponse response = new GenericResponse();
    try {
      
      String spb = this.sPlanService.getTransactionIdOfPending(userId);
      response.setData(spb);
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/payTotalDue/{userId}/{vasId}/{discId}/{discAmt}/{grAmt}"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<Object> payTotalDues(HttpServletRequest request,@PathVariable("userId") String userId,
		  @PathVariable("vasId") Integer vasId,@PathVariable("discId") Integer discId,@PathVariable("discAmt") Double discAmt,
		  @PathVariable("grAmt") Double grAmt) {
    logger.info("======== Pay Total Dues =========");
    GenericResponse response = new GenericResponse();
    try {
      
      sPlanService.totalDuePayment(userId,vasId,discId,discAmt,grAmt);
      //response.setData(spb);
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/getTransactionPostPaidDetail/{userId}"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<Object> getTransactionPostPaidDetail(HttpServletRequest request,@PathVariable("userId") String userId) {
    logger.info("======== Get Transaction PostPaid Detail =========");
    GenericResponse response = new GenericResponse();
    try {
      
      List<TransactionPostPaidDetail> spb = this.sPlanService.getTransactionPostPaidDetail(userId);
      response.setData(spb);
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/overallPostPaidv2/{userId}"})
  public ResponseEntity<?> overallPostpaidSubscriptionPlanv2(@PathVariable("userId") String userID) {
    logger.info(" ================ Get the unpaid users record  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    if (!userID.substring(0, 2).equalsIgnoreCase("RE")) {
      return this.sPlanService.overallPostpaidSubscriptionPlanv2(userID);
    }
    response.setErrCode("ASA014");
    response.setErrMessage(ErrorDescription.getDescription("ASA014"));
    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/getViewCount/{userId}"})
  public ResponseEntity<?> getViewCount(@PathVariable("userId") String userID) {
    logger.info(" ================ Get the viewCount  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    try
    {
    	NimaiTransactionViewCount ntvc=sPlanService.getViewCountByUserId(userID);
    	response.setData(ntvc);
        response.setStatus("Success");
        return new ResponseEntity(response, HttpStatus.OK);
    }
    catch(Exception e)
    {
    	response.setErrCode("ASA014");
    	response.setErrMessage("No Data");
    	return new ResponseEntity(response, HttpStatus.OK);
  }
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping({"/updateViewCount/{userId}"})
  public ResponseEntity<?> updateViewCountAfterQuoteAccept(@PathVariable("userId") String userID) {
    logger.info(" ================ update viewCount  API is Invoked ================:" + userID);
    GenericResponse response = new GenericResponse();
    try
    {
    	sPlanService.updateViewCountByUserId(userID);
    	response.setData("Flag Updated Sccessfully");
        response.setStatus("Success");
        return new ResponseEntity(response, HttpStatus.OK);
    }
    catch(Exception e)
    {
    	response.setErrCode("ASA014");
    	response.setErrMessage("No Data");
    	return new ResponseEntity(response, HttpStatus.OK);
  }
  }
}
