package com.nimai.splan.service;

import com.nimai.splan.NimaiSPlanApplication;
import com.nimai.splan.model.NimaiCustomerSubscriptionGrandAmount;
import com.nimai.splan.model.NimaiMCustomer;
import com.nimai.splan.model.NimaiMMCoupen;
import com.nimai.splan.payload.GenericResponse;
import com.nimai.splan.repository.NimaiCustomerGrandAmountRepo;
import com.nimai.splan.repository.NimaiMCustomerRepository;
import com.nimai.splan.repository.NimaiMMCoupenRepo;
import com.nimai.splan.repository.SubscriptionPlanRepository;

import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ValidateCoupenServiceImpl implements ValidateCoupenService {
  private static final Logger logger = LoggerFactory.getLogger(NimaiSPlanApplication.class);
  
  @Autowired
  EntityManagerFactory emFactory;
  
  @Autowired
  NimaiCustomerGrandAmountRepo nimaiCustomerGrandAmtRepository;
  
  @Autowired
  NimaiMMCoupenRepo nimaimmRepo;
  
  @Autowired
  NimaiMCustomerRepository userRepository;
  
  @Autowired
  SubscriptionPlanRepository subscriptionDetailsRepository;
  
  public HashMap<String, String> validateCoupen(String coupenId, String countryName, String subscriptionPlan, String coupenfor) {
    EntityManager entityManager = this.emFactory.createEntityManager();
    StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("VALIDATE_COUPEN", new Class[] { NimaiMMCoupen.class });
    storedProcedureQuery.registerStoredProcedureParameter("inp_coupen_id", String.class, ParameterMode.IN);
    storedProcedureQuery.registerStoredProcedureParameter("inp_country_name", String.class, ParameterMode.IN);
    storedProcedureQuery.registerStoredProcedureParameter("inp_subsciption_plan", String.class, ParameterMode.IN);
    storedProcedureQuery.registerStoredProcedureParameter("inp_user_type", String.class, ParameterMode.IN);
    storedProcedureQuery.registerStoredProcedureParameter("out_coupen_status", String.class, ParameterMode.OUT);
    storedProcedureQuery.registerStoredProcedureParameter("out_total_amount", float.class, ParameterMode.OUT);
    storedProcedureQuery.setParameter("inp_coupen_id", coupenId);
    storedProcedureQuery.setParameter("inp_country_name", countryName);
    storedProcedureQuery.setParameter("inp_subsciption_plan", subscriptionPlan);
    storedProcedureQuery.setParameter("inp_user_type", coupenfor);
    storedProcedureQuery.execute();
    String out_coupen_status = (String)storedProcedureQuery.getOutputParameterValue("out_coupen_status");
    Float out_total_amount = (Float)storedProcedureQuery.getOutputParameterValue("out_total_amount");
    HashMap<String, String> outputdata = new HashMap<>();
    outputdata.put("coupenstatus", out_coupen_status);
    outputdata.put("totalamount", out_total_amount.toString());
    return outputdata;
  }
  
  public ResponseEntity<?> applyForCoupen(String userId, String subscriptionId, String subscriptionName, String coupenCode, Integer subscriptionAmount) {
    GenericResponse response = new GenericResponse();
    String coupenFor = null;
    if (userId.substring(0, 2).equalsIgnoreCase("BA"))
      coupenFor = "Bank"; 
    if (userId.substring(0, 2).equalsIgnoreCase("BC"))
      coupenFor = "Bank as Customer"; 
    if (userId.substring(0, 2).equalsIgnoreCase("CU"))
      coupenFor = "Customer"; 
    try {
      Optional<NimaiMCustomer> custDet = this.userRepository.findByUserId(userId);
      String businessCountry = ((NimaiMCustomer)custDet.get()).getRegistredCountry();
      System.out.println("Business Country: " + businessCountry);
      String promoCode = "", emailID = "";
      String couponType = "";
      Integer leadID = this.nimaimmRepo.getLeadId(userId);
      if (leadID == null)
        leadID = Integer.valueOf(0); 
      logger.info("Lead ID: " + leadID);
      System.out.println("Lead ID: " + leadID);
      emailID = this.nimaimmRepo.getEmailId(userId);
      promoCode = this.nimaimmRepo.getPromoCode(emailID);
      if (promoCode == null)
        promoCode = ""; 
      System.out.println("emailID: " + emailID);
      System.out.println("promoCode: " + promoCode);
      if (leadID.intValue() > 0 && !promoCode.equalsIgnoreCase("")) {
        System.out.println("In lead section of coupon");
        couponType = this.nimaimmRepo.getCouponTypeByCoupenCode(coupenCode);
        System.out.println("CouponType: " + couponType);
      } else {
        System.out.println("In ELSE --- couponType");
        couponType = this.nimaimmRepo.getCouponTypeByCoupenCodeSubscriptionNameStatusAndConsumption(coupenCode, subscriptionName, coupenFor, businessCountry);
      } 
      System.out.println("Coupon Type: " + couponType);
      logger.info("Coupon Type: " + couponType);
      logger.info("Coupon For: " + coupenFor);
      logger.info("Coupon Code: " + coupenCode);
      logger.info("Subscription Name: " + subscriptionName);
      logger.info("Subscription Id: " + subscriptionId);
      int couponCount = this.nimaimmRepo.getCountForValidCoupon(coupenCode);
      System.out.println("Coupon count :" + couponCount);
      if (couponType.equalsIgnoreCase("nc") && couponCount > 0)
        return proceedForDiscountProcess(userId, subscriptionId, coupenCode, businessCountry, subscriptionName, coupenFor, subscriptionAmount, leadID); 
      if (couponType.equalsIgnoreCase("pc") && couponCount > 0) {
        List validUser = this.nimaimmRepo.getDataByUserIdAndStatus(userId);
        if (validUser.size() == 0) {
          response.setStatus("Failure");
          response.setErrCode("Coupon is Invalid");
          return new ResponseEntity(response, HttpStatus.OK);
        } 
        return proceedForDiscountProcess(userId, subscriptionId, coupenCode, businessCountry, subscriptionName, coupenFor, subscriptionAmount, Integer.valueOf(0));
      } 
      response.setStatus("Failure");
      response.setErrCode("Coupon is Invalid");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("Coupon is Invalid");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
  }

  public ResponseEntity<?> applyForCoupenForPostpaid(String userId, String subscriptionId, String subscriptionName, String coupenCode, Integer subscriptionAmount) {
    GenericResponse response = new GenericResponse();
    String coupenFor = null;
    if (userId.substring(0, 2).equalsIgnoreCase("BA"))
      coupenFor = "Bank";
    if (userId.substring(0, 2).equalsIgnoreCase("BC"))
      coupenFor = "Bank as Customer";
    if (userId.substring(0, 2).equalsIgnoreCase("CU"))
      coupenFor = "Customer";
    try {
      Optional<NimaiMCustomer> custDet = this.userRepository.findByUserId(userId);
     /* String businessCountry = ((NimaiMCustomer)custDet.get()).getRegistredCountry();
      System.out.println("Business Country: " + businessCountry);*/
      String promoCode = "", emailID = "";
      String couponType = "";
      Integer leadID = this.nimaimmRepo.getLeadId(userId);
      if (leadID == null)
        leadID = Integer.valueOf(0);
      logger.info("Lead ID: " + leadID);
      System.out.println("Lead ID: " + leadID);
      emailID = this.nimaimmRepo.getEmailId(userId);
      promoCode = this.nimaimmRepo.getPromoCode(emailID);
      if (promoCode == null)
        promoCode = "";
      System.out.println("emailID: " + emailID);
      System.out.println("promoCode: " + promoCode);
      if (leadID.intValue() > 0 && !promoCode.equalsIgnoreCase("")) {
        System.out.println("In lead section of coupon");
        couponType = this.nimaimmRepo.getCouponTypeByCoupenCode(coupenCode);
        System.out.println("CouponType: " + couponType);
      } else {
        System.out.println("In ELSE --- couponType");
        couponType = this.nimaimmRepo.getCouponTypeByCoupenCodeSubscriptionNameStatusAndConsumptionPostpaid(coupenCode, subscriptionName, coupenFor);
      }
      System.out.println("Coupon Type: " + couponType);
      logger.info("Coupon Type: " + couponType);
      logger.info("Coupon For: " + coupenFor);
      logger.info("Coupon Code: " + coupenCode);
      logger.info("Subscription Id: " + subscriptionId);
      logger.info("Checking Coupon Code is empty or not : " + Objects.isNull(coupenCode));
      if(Objects.isNull(coupenCode) || coupenCode.isEmpty()){
        this.subscriptionDetailsRepository.updateDiscountIdForPostpaid(userId, 0.0,0.0);
      }
      int couponCount= this.nimaimmRepo.getCountForValidCoupon(coupenCode);
      System.out.println("Coupon count :" + couponCount);
      if (couponType.equalsIgnoreCase("nc") && couponCount > 0)
        return proceedForDiscountProcessPostpaid(userId, subscriptionId, coupenCode,subscriptionName, coupenFor, subscriptionAmount, leadID);
      if (couponType.equalsIgnoreCase("pc") && couponCount > 0) {
        List validUser = this.nimaimmRepo.getDataByUserIdAndStatus(userId);
        if (validUser.size() == 0) {
          response.setStatus("Failure");
          response.setErrCode("Coupon is Invalid");
          return new ResponseEntity(response, HttpStatus.OK);
        }
        return proceedForDiscountProcessPostpaid(userId, subscriptionId, coupenCode,subscriptionName, coupenFor, subscriptionAmount, Integer.valueOf(0));
      }
      response.setStatus("Failure");
      response.setErrCode("Coupon is Invalid");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("Coupon is Invalid");
      return new ResponseEntity(response, HttpStatus.OK);
    }
  }

  public ResponseEntity<?> removeFromCoupen(String userId, int discountId, NimaiCustomerSubscriptionGrandAmount ncsga) {
    GenericResponse response = new GenericResponse();
    try {
      this.nimaimmRepo.decrementConsumption(discountId);
      this.nimaiCustomerGrandAmtRepository.deleteById(ncsga.getId());
      response.setStatus("Coupon Removed Successfully");
      response.setData("0");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("Unable to remove Coupon");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
  }
  
  private ResponseEntity<?> proceedForDiscountProcess(String userId, String subscriptionId, String coupenCode, String businessCountry, String subscriptionName, String coupenFor, Integer subscriptionAmount, Integer leadID) {
    Double discountId;
    String discountType;
    GenericResponse response = new GenericResponse();
    Date today = new Date();
    String emailID = "", promoCode = "";
    emailID = this.nimaimmRepo.getEmailId(userId);
    promoCode = this.nimaimmRepo.getPromoCode(emailID);
    if (promoCode == null)
      promoCode = ""; 
    if (leadID.intValue() > 0 && !promoCode.equalsIgnoreCase("")) {
      discountType = this.nimaimmRepo.getDiscountTypeByCoupenCodeStatusAndConsumption(coupenCode);
      discountId = this.nimaimmRepo.getDiscountIdByCouponCode(coupenCode);
    } else {
      discountType = this.nimaimmRepo.getDiscountTypeByCoupenCodeSubscriptionNameStatusAndConsumption(coupenCode, subscriptionName, coupenFor, businessCountry);
      discountId = this.nimaimmRepo.getDiscountId(coupenCode, businessCountry, subscriptionName, coupenFor);
    } 
    System.out.println("Current Date: " + today);
    System.out.println("Discount Type: " + discountType);
    Integer subsAmount = this.subscriptionDetailsRepository.getSubscriptionAmt(subscriptionId);
    if (discountType.equalsIgnoreCase("Fixed")) {
      Double discAmount = this.nimaimmRepo.getDiscAmountByDiscId(discountId);
      Double discountedAmount = this.nimaimmRepo.getAmountByDiscId(discountId);
      Double finalAmount = Double.valueOf(subscriptionAmount.intValue() - discountedAmount.doubleValue());
      if (finalAmount.doubleValue() >= 0.0D) {
        System.out.println("Original Diff: " + (subsAmount.intValue() - discAmount.doubleValue()));
        logger.info("Original Diff: " + (subsAmount.intValue() - discAmount.doubleValue()));
        System.out.println("final Diff: " + finalAmount);
        logger.info("final Diff: " + finalAmount);
        logger.info("Updating Consumption for discount id: " + discountId);
        this.nimaimmRepo.updateConsumption(discountId);
        NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
        ncsgm.setUserId(userId);
        ncsgm.setGrandAmount(finalAmount);
        ncsgm.setDiscountApplied("Yes");
        ncsgm.setInsertedDate(new Date());
        this.nimaiCustomerGrandAmtRepository.save(ncsgm);
        HashMap<String, Double> data = new HashMap<>();
        data.put("discountId", discountId);
        data.put("discount", discountedAmount);
        data.put("grandAmount", finalAmount);
        this.subscriptionDetailsRepository.updateDiscountId(userId, discountId);
        response.setStatus("Coupon Applied Successfully");
        response.setData(data);
        return new ResponseEntity(response, HttpStatus.OK);
      } 
      response.setStatus("Failure");
      response.setData("0");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    if (discountType.equalsIgnoreCase("Percentage") || discountType.equalsIgnoreCase("%")) {
      Double discountPercentage = this.nimaimmRepo.getDiscPercByDiscountId(discountId);
      Double maxDiscount = this.nimaimmRepo.getMaxDiscByDiscountId(discountId);
      System.out.println("MaxDiscount is="+maxDiscount);
      System.out.println("discountPercentage is="+discountPercentage);
      System.out.println("discountId is="+discountId);
      Double subsValue = Double.valueOf(subscriptionAmount.intValue() * discountPercentage.doubleValue() / 100.0D);
      System.out.println("subsValue is="+subsValue);
      if (subsValue.doubleValue() <= maxDiscount.doubleValue()) {

        Double double_ = Double.valueOf(subscriptionAmount.intValue() - subsValue.doubleValue());
        System.out.println("Where Subsvalue smaller than maxDiscount="+ double_);
        if (double_.doubleValue() >= 0.0D) {
          logger.info("Updating Consumption for discount id: " + discountId);
          this.nimaimmRepo.updateConsumption(discountId);
          NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
          ncsgm.setUserId(userId);
          ncsgm.setGrandAmount(double_);
          ncsgm.setDiscountApplied("Yes");
          ncsgm.setInsertedDate(new Date());
          this.nimaiCustomerGrandAmtRepository.save(ncsgm);
          HashMap<String, Double> data1 = new HashMap<>();
          data1.put("discountId", discountId);
          data1.put("discount", subsValue);
          data1.put("grandAmount", double_);
          response.setStatus("Coupon Applied Successfully");
          response.setData(data1);
          return new ResponseEntity(response, HttpStatus.OK);
        } 
        logger.info("final amount is -ve");
        response.setStatus("Failure");
        response.setData("0");
        return new ResponseEntity(response, HttpStatus.OK);
      } 
      Double finalAmount = Double.valueOf(subscriptionAmount.intValue() - maxDiscount.doubleValue());
      System.out.println("Where Final Amount="+ finalAmount);
      if (finalAmount.doubleValue() >= 0.0D) {
        logger.info("Updating Consumption for discount id: " + discountId);
        this.nimaimmRepo.updateConsumption(discountId);
        NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
        ncsgm.setUserId(userId);
        ncsgm.setGrandAmount(finalAmount);
        ncsgm.setDiscountApplied("Yes");
        ncsgm.setInsertedDate(new Date());
        this.nimaiCustomerGrandAmtRepository.save(ncsgm);
        HashMap<String, Double> data1 = new HashMap<>();
        data1.put("discountId", discountId);
        data1.put("discount", maxDiscount);
        data1.put("grandAmount", finalAmount);
        response.setStatus("Coupon Applied Successfully");
        response.setData(data1);
        return new ResponseEntity(response, HttpStatus.OK);
      } 
      logger.info("final amount is -ve");
      response.setStatus("Failure");
      response.setData("0");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    response.setStatus("Failure");
    response.setData("0");
    return new ResponseEntity(response, HttpStatus.OK);
  }

  private ResponseEntity<?> proceedForDiscountProcessPostpaid(String userId, String subscriptionId, String coupenCode, String subscriptionName, String coupenFor, Integer subscriptionAmount, Integer leadID) {
    Double discountId;
    String discountType;
    GenericResponse response = new GenericResponse();
    Date today = new Date();
    String emailID = "", promoCode = "";
    emailID = this.nimaimmRepo.getEmailId(userId);
    promoCode = this.nimaimmRepo.getPromoCode(emailID);
    if (promoCode == null)
      promoCode = "";
    if (leadID.intValue() > 0 && !promoCode.equalsIgnoreCase("")) {
      discountType = this.nimaimmRepo.getDiscountTypeByCoupenCodeStatusAndConsumption(coupenCode);
      discountId = this.nimaimmRepo.getDiscountIdByCouponCode(coupenCode);
    } else {
      discountType = this.nimaimmRepo.getDiscountTypeByCoupenCodeSubscriptionNameStatusAndConsumptionPostpaid(coupenCode, subscriptionName, coupenFor);
      discountId = this.nimaimmRepo.getDiscountIdPostpaid(coupenCode, subscriptionName, coupenFor);
    }
    System.out.println("Current Date: " + today);
    System.out.println("Discount Type: " + discountType);
    Integer subsAmount = this.subscriptionDetailsRepository.getSubscriptionAmt(subscriptionId);
   /* if (discountType.equalsIgnoreCase("Fixed")) {
      Double discAmount = this.nimaimmRepo.getDiscAmountByDiscId(discountId);
      Double discountedAmount = this.nimaimmRepo.getAmountByDiscId(discountId);
      Double finalAmount = Double.valueOf(subscriptionAmount.intValue() - discountedAmount.doubleValue());
      if (finalAmount.doubleValue() >= 0.0D) {
        System.out.println("Original Diff: " + (subsAmount.intValue() - discAmount.doubleValue()));
        logger.info("Original Diff: " + (subsAmount.intValue() - discAmount.doubleValue()));
        System.out.println("final Diff: " + finalAmount);
        logger.info("final Diff: " + finalAmount);
        logger.info("Updating Consumption for discount id: " + discountId);
        this.nimaimmRepo.updateConsumption(discountId);
        NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
        ncsgm.setUserId(userId);
        ncsgm.setGrandAmount(finalAmount);
        ncsgm.setDiscountApplied("Yes");
        ncsgm.setInsertedDate(new Date());
        this.nimaiCustomerGrandAmtRepository.save(ncsgm);
        HashMap<String, Double> data = new HashMap<>();
        data.put("discountId", discountId);
        data.put("discount", discountedAmount);
        data.put("grandAmount", finalAmount);
        this.subscriptionDetailsRepository.updateDiscountId(userId, discountId);
        response.setStatus("Coupon Applied Successfully");
        response.setData(data);
        return new ResponseEntity(response, HttpStatus.OK);
      }
      response.setStatus("Failure");
      response.setData("0");
      return new ResponseEntity(response, HttpStatus.OK);
    }*/
    if (discountType.equalsIgnoreCase("Percentage") || discountType.equalsIgnoreCase("%")) {
      Double discountPercentage = this.nimaimmRepo.getDiscPercByDiscountId(discountId);
      Double maxDiscount = this.nimaimmRepo.getMaxDiscByDiscountId(discountId);
      Double subsValue = Double.valueOf(subscriptionAmount.intValue() * discountPercentage.doubleValue() / 100.0D);
      if (maxDiscount.doubleValue() > 0) {
        if (subsValue.doubleValue() <= maxDiscount.doubleValue()) {
          Double double_ = Double.valueOf(subscriptionAmount.intValue() - subsValue.doubleValue());
          if (double_.doubleValue() >= 0.0D) {
            logger.info("Updating Consumption for discount id: " + discountId);
            this.nimaimmRepo.updateConsumption(discountId);
            NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
            ncsgm.setUserId(userId);
            ncsgm.setGrandAmount(double_);
            ncsgm.setDiscountApplied("Yes");
            ncsgm.setInsertedDate(new Date());
            this.nimaiCustomerGrandAmtRepository.save(ncsgm);
            HashMap<String, Double> data1 = new HashMap<>();
            data1.put("discountId", discountId);
            data1.put("discount", subsValue);
            data1.put("grandAmount", double_);
            if (coupenCode.isEmpty()){
              this.subscriptionDetailsRepository.updateDiscountIdForPostpaid(userId, 0.0, 0.0);
            }else{
              this.subscriptionDetailsRepository.updateDiscountIdForPostpaid(userId, discountId, subsValue);
            }
            response.setStatus("Coupon Applied Successfully");
            response.setData(data1);
            return new ResponseEntity(response, HttpStatus.OK);
          }
          logger.info("final amount is -ve");
          response.setStatus("Failure");
          response.setData("0");
          return new ResponseEntity(response, HttpStatus.OK);
        } else {
          Double double_ = Double.valueOf(subscriptionAmount.intValue() - maxDiscount.doubleValue());
          if (double_.doubleValue() >= 0.0D) {
            logger.info("Updating Consumption for discount id: " + discountId);
            this.nimaimmRepo.updateConsumption(discountId);
            NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
            ncsgm.setUserId(userId);
            ncsgm.setGrandAmount(double_);
            ncsgm.setDiscountApplied("Yes");
            ncsgm.setInsertedDate(new Date());
            this.nimaiCustomerGrandAmtRepository.save(ncsgm);
            HashMap<String, Double> data1 = new HashMap<>();
            data1.put("discountId", discountId);
            data1.put("discount", maxDiscount);
            data1.put("grandAmount", double_);
            this.subscriptionDetailsRepository.updateDiscountIdForPostpaid(userId, discountId,subsValue);
            response.setStatus("Coupon Applied Successfully");
            response.setData(data1);
            return new ResponseEntity(response, HttpStatus.OK);
          }
          logger.info("final amount is -ve");
          response.setStatus("Failure");
          response.setData("0");
          return new ResponseEntity(response, HttpStatus.OK);
        }
      } else {
        Double double_ = Double.valueOf(subscriptionAmount.intValue() - subsValue.doubleValue());
        if (double_.doubleValue() >= 0.0D) {
          logger.info("Updating Consumption for discount id: " + discountId);
          this.nimaimmRepo.updateConsumption(discountId);
          NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
          ncsgm.setUserId(userId);
          ncsgm.setGrandAmount(double_);
          ncsgm.setDiscountApplied("Yes");
          ncsgm.setInsertedDate(new Date());
          this.nimaiCustomerGrandAmtRepository.save(ncsgm);
          HashMap<String, Double> data1 = new HashMap<>();
          data1.put("discountId", discountId);
          data1.put("discount", subsValue);
          data1.put("grandAmount", double_);
          this.subscriptionDetailsRepository.updateDiscountIdForPostpaid(userId, discountId,subsValue);
          response.setStatus("Coupon Applied Successfully");
          response.setData(data1);
          return new ResponseEntity(response, HttpStatus.OK);
        }
        logger.info("final amount is -ve");
        response.setStatus("Failure");
        response.setData("0");
        return new ResponseEntity(response, HttpStatus.OK);
      }

    }
    logger.info("final amount is -ve");
    response.setStatus("Failure");
    response.setData("0");
    return new ResponseEntity(response, HttpStatus.OK);


  }

  public HashMap<String, Double> discountCalculate(Double discountId, String subscriptionId) {
    logger.info("Discount Calculation");
    GenericResponse response = new GenericResponse();
    Date today = new Date();
    String discountType = this.nimaimmRepo.getDiscountTypeByDiscountId(discountId);
    System.out.println("Current Date: " + today);
    System.out.println("Discount Type: " + discountType);
    logger.info("Discount Type: " + discountType);
    logger.info("discount id: " + discountId);
    Integer subsAmount = this.subscriptionDetailsRepository.getSubscriptionAmt(subscriptionId);
    logger.info("Subscription Amount: " + subsAmount);
    if (discountType.equalsIgnoreCase("Fixed")) {
      Double discAmount = this.nimaimmRepo.getDiscAmountByDiscId(discountId);
      Double discountedAmount = this.nimaimmRepo.getAmountByDiscountId(discountId);
      Double finalAmount = Double.valueOf(subsAmount.intValue() - discountedAmount.doubleValue());
      logger.info("Discount Amount: " + discAmount);
      logger.info("Discounted Amount: " + discountedAmount);
      logger.info("Final Amount: " + finalAmount);
      if (finalAmount.doubleValue() >= 0.0D) {
        HashMap<String, Double> hashMap = new HashMap<>();
        hashMap.put("discountId", discountId);
        hashMap.put("discount", discountedAmount);
        hashMap.put("grandAmount", finalAmount);
        return hashMap;
      } 
      HashMap<String, Double> data = new HashMap<>();
      data.put("discountId", discountId);
      data.put("discount", Double.valueOf(0.0D));
      data.put("grandAmount", finalAmount);
      return data;
    } 
    if (discountType.equalsIgnoreCase("Percentage") || discountType.equalsIgnoreCase("%")) {
      Double discountPercentage = this.nimaimmRepo.getDiscPercByDiscountId(discountId);
      Double maxDiscount = this.nimaimmRepo.getMaxDiscByDiscountId(discountId);
      Double subsValue = Double.valueOf(subsAmount.intValue() * discountPercentage.doubleValue() / 100.0D);
      logger.info("Discount Percentage: " + discountPercentage);
      logger.info("Max Discount: " + maxDiscount);
      logger.info("subsValue: " + subsValue);
      if (subsValue.doubleValue() <= maxDiscount.doubleValue()) {
        Double double_ = Double.valueOf(subsAmount.intValue() - subsValue.doubleValue());
        logger.info("Final Amount: " + double_);
        if (double_.doubleValue() >= 0.0D) {
          HashMap<String, Double> hashMap2 = new HashMap<>();
          hashMap2.put("discountId", discountId);
          hashMap2.put("discount", subsValue);
          hashMap2.put("grandAmount", double_);
          return hashMap2;
        } 
        HashMap<String, Double> hashMap1 = new HashMap<>();
        hashMap1.put("discountId", discountId);
        hashMap1.put("discount", Double.valueOf(0.0D));
        hashMap1.put("grandAmount", double_);
        return hashMap1;
      } 
      Double finalAmount = Double.valueOf(subsAmount.intValue() - maxDiscount.doubleValue());
      logger.info("Final Amount: " + finalAmount);
      if (finalAmount.doubleValue() >= 0.0D) {
        HashMap<String, Double> hashMap1 = new HashMap<>();
        hashMap1.put("discountId", discountId);
        hashMap1.put("discount", maxDiscount);
        hashMap1.put("grandAmount", finalAmount);
        return hashMap1;
      } 
      HashMap<String, Double> hashMap = new HashMap<>();
      hashMap.put("discountId", discountId);
      hashMap.put("discount", Double.valueOf(0.0D));
      hashMap.put("grandAmount", finalAmount);
      return hashMap;
    } 
    HashMap<String, Double> data1 = new HashMap<>();
    data1.put("discountId", discountId);
    data1.put("discount", Double.valueOf(0.0D));
    data1.put("grandAmount", Double.valueOf(0.0D));
    return data1;
  }
}
