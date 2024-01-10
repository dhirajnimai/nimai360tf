package com.nimai.splan.controller;

import com.nimai.splan.model.NimaiAdvisory;
import com.nimai.splan.model.NimaiCustomerSubscriptionGrandAmount;
import com.nimai.splan.model.NimaiSubscriptionVas;
import com.nimai.splan.payload.CustomerSubscriptionGrandAmountBean;
import com.nimai.splan.payload.GenericResponse;
import com.nimai.splan.payload.NimaiAfterVasSubscriptionBean;
import com.nimai.splan.payload.NimaiSubscriptionVasBean;
import com.nimai.splan.repository.NimaiAdvisoryRepo;
import com.nimai.splan.repository.SubscriptionPlanRepository;
import com.nimai.splan.service.NimaiAdvisoryService;
import com.nimai.splan.service.SubscriptionPlanService;
import com.nimai.splan.utility.ErrorDescription;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"*"})
@RestController
public class AdvisoryController {
  private static final String randomString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  
  @Autowired
  NimaiAdvisoryService advisoryService;
  
  @Autowired
  private SubscriptionPlanService sPlanService;
  
  @Autowired
  SubscriptionPlanRepository splanDetRepo;
  
  @Autowired
  NimaiAdvisoryRepo nimaiAdvisoryRepo;
  
  GenericResponse response = new GenericResponse();
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/viewAdvisory"}, produces = {"application/json"}, method = {RequestMethod.GET})
  public ResponseEntity<?> viewAdvisory() {
    List<NimaiAdvisory> nadvisory = this.advisoryService.viewAdvisory();
    if (nadvisory.isEmpty()) {
      this.response.setErrMessage("No Records Found");
      this.response.setStatus("Failure");
      return new ResponseEntity(this.response, HttpStatus.OK);
    } 
    this.response.setData(nadvisory);
    this.response.setStatus("Success");
    return new ResponseEntity(this.response, HttpStatus.OK);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping(value = {"/getAdvisoryListByCountry/{userId}/{type}"}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> getAdvisoryByCountryName(@RequestBody NimaiAdvisory nimaiAdvisory, @PathVariable("userId") String userID, @PathVariable("type") String type) {
    GenericResponse response = new GenericResponse();
    try {
      String country_name = nimaiAdvisory.getCountry_name();
      List<NimaiAdvisory> outdata1= null;

      if(type.equalsIgnoreCase("postpaid")){
        outdata1 = this.advisoryService.viewAdvisoryByType();

      }else {
        outdata1 = this.advisoryService.viewAdvisoryByCountry(country_name, userID);
      }

      if (!outdata1.isEmpty()) {
          response.setData(outdata1);
          response.setStatus("Success");
          return new ResponseEntity(response, HttpStatus.OK);
      }



      response.setData(null);
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
      } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      System.out.println("" + e);
      response.setErrMessage(ErrorDescription.getDescription("EXE000"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping(value = {"/addVAS"}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> addVAS(@RequestBody NimaiSubscriptionVasBean nimaiSubsciptionVas) {
    GenericResponse response = new GenericResponse();
    String userId = nimaiSubsciptionVas.getUserId();
    String subscriptionId = nimaiSubsciptionVas.getSubscriptionId();
    String mode = nimaiSubsciptionVas.getMode();
    int isSplanWithVasFlag = nimaiSubsciptionVas.getIsSplanWithVasFlag().intValue();
    String vasPurchased = nimaiSubsciptionVas.getVasPurchased();
    try {
      int vasCount = StringUtils.countOccurrencesOf(vasPurchased, "-");
      Integer totalVAS = Integer.valueOf(vasCount);
      System.out.println("Total VAS: " + totalVAS);
      this.advisoryService.inactiveVASStatus(userId);
      String[] vasSplitted = vasPurchased.split("-", totalVAS.intValue());
      for (int i = 0; i < totalVAS.intValue(); i++) {
        System.out.println("vasSplitted: " + vasSplitted[i]);
        if (i == totalVAS.intValue() - 1)
          vasSplitted[i] = vasSplitted[i].replace("-", ""); 
        int vasId = Integer.valueOf(vasSplitted[i]).intValue();
        System.out.println("VAS ID: " + vasId);
        this.advisoryService.addVasDetails(userId, subscriptionId, Integer.valueOf(vasId), mode, isSplanWithVasFlag);
      } 
      this.advisoryService.getLastSerialNoAndUpdate(userId, mode);
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      response.setStatus("Failure");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping(value = {"/getVASByUserId"}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> getVASByUserId(@RequestBody NimaiSubscriptionVas nimaiSubsciptionVas) {
    GenericResponse response = new GenericResponse();
    List<NimaiSubscriptionVas> vasDetails = null;
    vasDetails = this.advisoryService.getActiveVASByUserId(nimaiSubsciptionVas.getUserId());
    if (vasDetails.isEmpty()) {
      response.setErrMessage("No Records Found");
      response.setStatus("Failure");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    response.setData(vasDetails);
    response.setStatus("Success");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping(value = {"/addVASAfterSubscription"}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> addVASAfterSubscription(@RequestBody NimaiAfterVasSubscriptionBean nimaiSubsciptionVas) {
    GenericResponse response = new GenericResponse();
    String userId = nimaiSubsciptionVas.getUserId();
    String vasId = nimaiSubsciptionVas.getVasId();
    String subscriptionId = nimaiSubsciptionVas.getSubscriptionId();
    String mode = nimaiSubsciptionVas.getMode();
    String paymentTxnId = "", invoiceId = "";
    if (mode.equalsIgnoreCase("Wire")) {
      paymentTxnId = generatePaymentTtransactionID(15);
      invoiceId = generatePaymentTtransactionID(10);
    } else {
      paymentTxnId = nimaiSubsciptionVas.getPaymentTxnId();
      invoiceId = nimaiSubsciptionVas.getInvoiceId();
    } 
    try {
      if(subscriptionId.equalsIgnoreCase("Postpaid")) {
        this.advisoryService.activeVASStatus(userId);
      }else{
        this.advisoryService.inactiveVASStatus(userId);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e);
      response.setStatus("Failure");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
    Float pricing = Float.valueOf(0.0F);
    this.advisoryService.addVasDetailsAfterSubscription(userId, subscriptionId, vasId, mode, pricing, paymentTxnId, invoiceId);
    response.setErrMessage("VAS Plan Purchased Successfully");
    response.setStatus("Success");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping(value = {"/getFinalVASAmount"}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> getFinalVASAmount(@RequestBody NimaiSubscriptionVas nimaiSubsciptionVas) {
    GenericResponse response = new GenericResponse();
    String userId = nimaiSubsciptionVas.getUserId();
    int vasId = nimaiSubsciptionVas.getVasId().intValue();
    try {
      Float vasAmount = this.advisoryService.getVASAmount(userId, Integer.valueOf(vasId));
      response.setData(vasAmount);
      response.setStatus("Success");
      return new ResponseEntity(response, HttpStatus.OK);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      response.setStatus("Failure");
      return new ResponseEntity(response, HttpStatus.OK);
    } 
  }
  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping(value = {"/addVASToGrand"}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> addVASToGrand(@RequestBody CustomerSubscriptionGrandAmountBean nimaiSubsciptionVas) {
    GenericResponse response = new GenericResponse();
    String userId = nimaiSubsciptionVas.getUserId();
    Double grandAmount = nimaiSubsciptionVas.getGrandAmount();
    this.advisoryService.addGrandVasDetails(userId, grandAmount);
    response.setStatus("Success");
    return new ResponseEntity(response, HttpStatus.OK);
  }

  
  @CrossOrigin(origins = {"*"}, allowedHeaders = {"*"})
  @PostMapping(value = {"/removeVASFromGrand"}, consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> removeVASFromGrand(@RequestBody CustomerSubscriptionGrandAmountBean nimaiSubsciptionVas) {
    GenericResponse response = new GenericResponse();
    String userId = nimaiSubsciptionVas.getUserId();
    NimaiCustomerSubscriptionGrandAmount ncsga = this.advisoryService.getCustomerVASAmount(userId);
    this.advisoryService.removeGrandVasDetails(ncsga.getId());
    response.setStatus("Success");
    return new ResponseEntity(response, HttpStatus.OK);
  }
  
  public static String generatePaymentTtransactionID(int count) {
    StringBuilder sb = new StringBuilder();
    while (count-- != 0) {
      int character = (int)(Math.random() * "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".length());
      sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(character));
    } 
    return sb.toString();
  }
}
