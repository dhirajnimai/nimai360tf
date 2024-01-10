package com.nimai.splan.controller;

import com.nimai.splan.NimaiSPlanApplication;
import com.nimai.splan.model.NimaiCustomerSubscriptionGrandAmount;
import com.nimai.splan.model.NimaiMMCoupen;
import com.nimai.splan.model.NimaiMSubscription;
import com.nimai.splan.payload.GenericResponse;
import com.nimai.splan.payload.SubscriptionBean;
import com.nimai.splan.repository.NimaiMMCoupenRepo;
import com.nimai.splan.service.SubscriptionPlanService;
import com.nimai.splan.service.ValidateCoupenService;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"*"})
@RestController
public class ValidateCoupenController {
  private static final Logger logger = LoggerFactory.getLogger(NimaiSPlanApplication.class);
  
  @Value("${fieo.vaspromocode}")
  private String fieoVASPromocode;
  
  @Autowired
  private SubscriptionPlanService sPlanService;
  
  @Autowired
  ValidateCoupenService validatecoupenService;
  
  @Autowired
  NimaiMMCoupenRepo nimaimmRepo;
  
  @RequestMapping(value = {"/validateCoupen/{coupenId}/{countryName}/{subscriptionPlan}/{coupenfor}"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<Object> validateCoupen(@PathVariable("coupenId") String coupenId, @PathVariable("countryName") String countryName, @PathVariable("subscriptionPlan") String subscriptionPlan, @PathVariable("coupenfor") String coupenfor) {
    GenericResponse response = new GenericResponse();
    NimaiMMCoupen nmcoupen = new NimaiMMCoupen();
    HashMap<String, String> outdata = this.validatecoupenService.validateCoupen(coupenId, countryName, subscriptionPlan, coupenfor);
    response.setData(outdata);
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/applyCoupon"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<?> applyCoupon(@RequestBody SubscriptionBean subscriptionRequest) {
    logger.info("=========== Applying Coupon ===========");
    GenericResponse response = new GenericResponse();
    String subscriptionName = subscriptionRequest.getSubscriptionName();
    String userId = subscriptionRequest.getUserId();
    String coupenCode = subscriptionRequest.getCoupenCode();
    Integer subscriptionAmount = Integer.valueOf(subscriptionRequest.getSubscriptionAmount());
    String subscriptionId = subscriptionRequest.getSubscriptionId();
    if(subscriptionId.equalsIgnoreCase("postpaid")){
       return this.validatecoupenService.applyForCoupenForPostpaid(userId, subscriptionId, subscriptionName, coupenCode, subscriptionAmount);
    }else {
      NimaiMSubscription subsDetail = this.sPlanService.getPlanDetailsBySubscriptionId(subscriptionId);
      if (subscriptionAmount.intValue() == subsDetail.getSubscriptionAmount())
        return this.validatecoupenService.applyForCoupen(userId, subscriptionId, subscriptionName, coupenCode, subscriptionAmount);
    }
    response.setStatus("Failure");
    response.setErrCode("OOPs! Something Went Wrong.");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/removeCoupon"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<?> removeCoupon(@RequestBody SubscriptionBean subscriptionRequest) {
    logger.info("=========== Removing Coupon ===========");
    int discountId = subscriptionRequest.getDiscountId();
    String userId = subscriptionRequest.getUserId();
    System.out.println("Discount Id:" + discountId);
    NimaiCustomerSubscriptionGrandAmount ncsga = this.sPlanService.getCustomerAmount(subscriptionRequest.getUserId());
    return this.validatecoupenService.removeFromCoupen(userId, discountId, ncsga);
  }
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/applyCouponAfterVASBuy"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<?> applyCouponAfterVASBuy(@RequestBody SubscriptionBean subscriptionRequest) {
    logger.info("=========== Applying Coupon ===========");
    GenericResponse response = new GenericResponse();
    String subscriptionName = subscriptionRequest.getSubscriptionName();
    String userId = subscriptionRequest.getUserId();
    Integer subscriptionAmount = Integer.valueOf(subscriptionRequest.getSubscriptionAmount());
    String subscriptionId = subscriptionRequest.getSubscriptionId();
    NimaiMSubscription subsDetail = this.sPlanService.getPlanDetailsBySubscriptionId(subscriptionId);
    if (subscriptionAmount.intValue() == subsDetail.getSubscriptionAmount()) {
      Double discountId = this.nimaimmRepo.getDiscountIdByCouponCode(this.fieoVASPromocode);
      HashMap<String, Double> amount = this.validatecoupenService.discountCalculate(discountId, subscriptionId);
      response.setData(amount);
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    response.setStatus("Failure");
    response.setErrCode("OOPs! Something Went Wrong.");
    return new ResponseEntity(response, HttpStatus.OK);
  }
}
