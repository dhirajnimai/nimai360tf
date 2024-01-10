package com.nimai.splan.service;

import com.nimai.splan.model.NimaiAdvisory;
import com.nimai.splan.model.NimaiCustomerSubscriptionGrandAmount;
import com.nimai.splan.model.NimaiEmailScheduler;
import com.nimai.splan.model.NimaiSubscriptionDetails;
import com.nimai.splan.model.NimaiSubscriptionVas;
import com.nimai.splan.repository.NimaiAdvisoryRepo;
import com.nimai.splan.repository.NimaiCustomerGrandAmountRepo;
import com.nimai.splan.repository.NimaiEmailSchedulerRepository;
import com.nimai.splan.repository.NimaiMCustomerRepository;
import com.nimai.splan.repository.NimaiMSPlanRepository;
import com.nimai.splan.repository.NimaiSubscriptionVasRepo;
import com.nimai.splan.repository.SubscriptionPlanRepository;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NimaiAdvisoryServiceImpl implements NimaiAdvisoryService {
  @Autowired
  NimaiMCustomerRepository userRepository;
  
  @Autowired
  NimaiAdvisoryRepo nimaiAdvisoryRepo;
  
  @Autowired
  NimaiMSPlanRepository subscriptionRepo;
  
  @Autowired
  SubscriptionPlanRepository splanDetRepo;
  
  @Autowired
  NimaiCustomerGrandAmountRepo nimaiCustomerGrandAmtRepository;
  
  @Autowired
  NimaiSubscriptionVasRepo nsvRepo;
  
  @Autowired
  NimaiEmailSchedulerRepository emailDetailsRepository;
  
  public List<NimaiAdvisory> viewAdvisory() {
    return this.nimaiAdvisoryRepo.findAll();
  }
  
  public List<NimaiAdvisory> viewAdvisoryByCountry(String country_name, String userId) {
    List<NimaiAdvisory> vasList = null;
    if (userId.substring(0, 2).equalsIgnoreCase("CU"))
      vasList = this.nimaiAdvisoryRepo.findByCountryName(country_name, "Customer");
    if (userId.substring(0, 2).equalsIgnoreCase("BC"))
      vasList = this.nimaiAdvisoryRepo.findByCountryName(country_name, "Bank As Customer"); 
    return vasList;
  }



  public List<NimaiAdvisory> viewAdvisoryByType() {
    List<NimaiAdvisory> vasListPostpaid = this.nimaiAdvisoryRepo.findByType();
    return vasListPostpaid;
  }

  
  public String getSubscriptionIdForActive(String userId) {
    String obtainedSubscriptionId = "";
    try {
      NimaiSubscriptionDetails nmd = this.splanDetRepo.findByUserId(userId);
      obtainedSubscriptionId = nmd.getSubscriptionId();
    } catch (Exception e) {
      System.out.println("No Active Subscription Available");
      obtainedSubscriptionId = "";
    } 
    return obtainedSubscriptionId;
  }
  
  public void addVasDetails(String userId, String subscriptionId, Integer vasId, String mode, int isSplanWithvasFlag) {
    NimaiAdvisory vasAdvisory = this.nimaiAdvisoryRepo.getDataByVasId(vasId.intValue());
    NimaiSubscriptionVas nsv = new NimaiSubscriptionVas();
    nsv.setUserId(userId);
    nsv.setSubscriptionId(subscriptionId);
    nsv.setVasId(vasId);
    nsv.setCountryName(vasAdvisory.getCountry_name());
    nsv.setPlanName(vasAdvisory.getPlan_name());
    nsv.setDescription_1(vasAdvisory.getDescription_1());
    nsv.setDescription_2(vasAdvisory.getDescription_2());
    nsv.setDescription_3(vasAdvisory.getDescription_3());
    nsv.setDescription_4(vasAdvisory.getDescription_4());
    nsv.setDescription_5(vasAdvisory.getDescription_5());
    nsv.setCurrency(vasAdvisory.getCurrency());
    nsv.setPricing(vasAdvisory.getPricing());
    nsv.setIsSplanWithVasFlag(isSplanWithvasFlag);
    nsv.setStatus("Active");
    nsv.setInsertedBy(userId);
    nsv.setInsertedDate(new Date());
    nsv.setModifiedBy(userId);
    nsv.setModifiedDate(new Date());
    if (mode.equalsIgnoreCase("Wire")) {
      nsv.setPaymentSts("Pending");
      nsv.setMode("Wire");
    } else {
      nsv.setPaymentSts("Approved");
      nsv.setMode("Credit");
    } 
    this.nsvRepo.save(nsv);
    this.splanDetRepo.updateIsVASApplied(userId);
  }
  
  public void addVasDetailsAfterSubscription(String userId, String subscriptionId, String vasIdString, String mode, Float pricing, String paymentTxnId, String invoiceId) {
    Float vasFinalAmount = null;
    int vasCount = StringUtils.countOccurrencesOf(vasIdString, "-");
    System.out.println("Total VAS: " + vasCount);
    String[] vasSplitted = vasIdString.split("-", vasCount + 1);
    for (int i = 0; i < vasCount; i++) {
      System.out.println("Iteration: " + i);
      int vasId = Integer.valueOf(vasSplitted[i]).intValue();
      System.out.println("VASID: " + vasId);
      NimaiAdvisory vasAdvisory = this.nimaiAdvisoryRepo.getDataByVasId(vasId);
      NimaiSubscriptionDetails nsd = this.splanDetRepo.findByUserId(userId);
      try {
        Double subscriptionTotalMonth = this.splanDetRepo.findNoOfMonthOfSubscriptionByUserId(userId);
        System.out.println("Subscription of Month: " + subscriptionTotalMonth);
        Double differenceInSubsStartAndCurrent = this.splanDetRepo.findDiffInSubscriptionStartAndCurrentByUserId(userId);
        System.out.println("Subscription of Month: " + differenceInSubsStartAndCurrent);
        Double halfSubscriptionTotalMonth = Double.valueOf(subscriptionTotalMonth.doubleValue() / 2.0D);
        System.out.println("Half of SubscriptionTotalMonth: " + halfSubscriptionTotalMonth);
        System.out.println("VAS Price: " + vasAdvisory.getPricing());
        if (differenceInSubsStartAndCurrent.doubleValue() > halfSubscriptionTotalMonth.doubleValue()) {
          vasFinalAmount = Float.valueOf(vasAdvisory.getPricing().floatValue() / 2.0F);
        } else {
          vasFinalAmount = vasAdvisory.getPricing();
        } 
      } catch (Exception e) {
        Double subscriptionTotalMonth = Double.valueOf(0.0D);
        Double differenceInSubsStartAndCurrent = Double.valueOf(0.0D);
        vasFinalAmount = Float.valueOf(0.0F);
      } 
      Double gstValue = Double.valueOf(this.nimaiAdvisoryRepo.getGSTValue().doubleValue() / 100.0D);
      Double planPriceGST = Double.valueOf(vasFinalAmount.floatValue() + vasFinalAmount.floatValue() * gstValue.doubleValue());
      System.out.println("gstValue: " + gstValue);
      System.out.println("planPriceGST: " + planPriceGST);
      String finalPrice = String.format("%.2f", new Object[] { planPriceGST });
      String vasfinalPrice = String.format("%.2f", new Object[] { vasFinalAmount });
      System.out.println("Final Amount: " + vasFinalAmount);
      NimaiSubscriptionVas nsv = new NimaiSubscriptionVas();
      nsv.setUserId(userId);
      nsv.setSubscriptionId(subscriptionId);
      nsv.setVasId(Integer.valueOf(vasId));
      nsv.setCountryName(vasAdvisory.getCountry_name());
      nsv.setPlanName(vasAdvisory.getPlan_name());
      nsv.setDescription_1(vasAdvisory.getDescription_1());
      nsv.setDescription_2(vasAdvisory.getDescription_2());
      nsv.setDescription_3(vasAdvisory.getDescription_3());
      nsv.setDescription_4(vasAdvisory.getDescription_4());
      nsv.setDescription_5(vasAdvisory.getDescription_5());
      nsv.setCurrency(vasAdvisory.getCurrency());
      nsv.setSplSerialNumber(nsd.getsPlSerialNUmber());
      nsv.setStatus("Active");
      nsv.setMode(mode);
      nsv.setPaymentTxnId(paymentTxnId);
      nsv.setInvoiceId(invoiceId);
      if (mode.equalsIgnoreCase("Wire")) {
        nsv.setPricing(Float.valueOf(finalPrice));
        nsv.setPaymentSts("Pending");
      } else {
        nsv.setPricing(Float.valueOf(finalPrice));
        nsv.setPaymentSts("Approved");
      } 
      nsv.setInsertedBy(userId);
      nsv.setInsertedDate(new Date());
      nsv.setModifiedBy(userId);
      nsv.setModifiedDate(new Date());
      this.nsvRepo.save(nsv);
      if (mode.equalsIgnoreCase("Wire")) {
    	  if(!subscriptionId.equalsIgnoreCase("Postpaid"))
    	  {
    		  this.splanDetRepo.updateVASDetailsAppliedWire(userId, vasfinalPrice, finalPrice);
    		  this.userRepository.updatePaymentTxnId("Wire", invoiceId, userId);
    	  }
      } else {
        this.splanDetRepo.updateVASDetailsApplied(userId, vasFinalAmount, Float.valueOf(finalPrice));
        this.userRepository.updatePaymentTxnId("Credit", invoiceId, userId);
      } 
      NimaiEmailScheduler schedularData = new NimaiEmailScheduler();
      schedularData.setUserid(nsd.getUserid().getUserid());
      schedularData.setDescription1(vasAdvisory.getDescription_1());
      schedularData.setDescription2(vasAdvisory.getDescription_2());
      schedularData.setDescription3(vasAdvisory.getDescription_3());
      schedularData.setDescription4(vasAdvisory.getDescription_4());
      schedularData.setDescription5(vasAdvisory.getDescription_5());
      schedularData.setSubscriptionName(vasAdvisory.getPlan_name());
      schedularData.setEmailId(nsd.getUserid().getEmailAddress());
      schedularData.setSubscriptionAmount(String.valueOf(vasFinalAmount));
      schedularData.setEvent("VAS_ADDED");
      schedularData.setEmailStatus("Pending");
      schedularData.setSubscriptionId(nsd.getSubscriptionId());
      this.emailDetailsRepository.save(schedularData);
    } 
  }
  
  public void inactiveVASStatus(String userId) {
    List<NimaiSubscriptionVas> vasEntity = this.nsvRepo.findAllByUserId(userId);
    if (!vasEntity.isEmpty())
      for (NimaiSubscriptionVas vas : vasEntity) {
        vas.setStatus("Inactive");
        this.nsvRepo.save(vas);
      }  
  }

  public void activeVASStatus(String userId) {
    List<NimaiSubscriptionVas> vasEntity = this.nsvRepo.findAllByUserId(userId);
    if (!vasEntity.isEmpty())
      for (NimaiSubscriptionVas vas : vasEntity) {
        vas.setStatus("Active");
        this.nsvRepo.save(vas);
      }
  }
  
  public List<NimaiSubscriptionVas> getActiveVASByUserId(String userId) {
    return this.nsvRepo.findActiveVASByUserId(userId);
  }


  public NimaiAdvisory getVasDetails(String string) {
    return this.nimaiAdvisoryRepo.getVASDetByVasId(Integer.valueOf(string).intValue());
  }
  
  public Float getVASAmount(String userId, Integer vasId) {
    Float vasFinalAmount = null;
    NimaiAdvisory vasAdvisory = this.nimaiAdvisoryRepo.getDataByVasId(vasId.intValue());
    NimaiSubscriptionDetails nsd = this.splanDetRepo.findByUserId(userId);
    try {
      Double subscriptionTotalMonth = this.splanDetRepo.findNoOfMonthOfSubscriptionByUserId(userId);
      System.out.println("Subscription of Month: " + subscriptionTotalMonth);
      Double differenceInSubsStartAndCurrent = this.splanDetRepo.findDiffInSubscriptionStartAndCurrentByUserId(userId);
      System.out.println("Subscription of Month: " + differenceInSubsStartAndCurrent);
      Double halfSubscriptionTotalMonth = Double.valueOf(subscriptionTotalMonth.doubleValue() / 2.0D);
      System.out.println("Half of SubscriptionTotalMonth: " + halfSubscriptionTotalMonth);
      System.out.println("VAS Price: " + vasAdvisory.getPricing());
      if (differenceInSubsStartAndCurrent.doubleValue() > halfSubscriptionTotalMonth.doubleValue()) {
        vasFinalAmount = Float.valueOf(vasAdvisory.getPricing().floatValue() / 2.0F);
      } else {
        vasFinalAmount = vasAdvisory.getPricing();
      } 
    } catch (Exception e) {
      Double subscriptionTotalMonth = Double.valueOf(0.0D);
      Double differenceInSubsStartAndCurrent = Double.valueOf(0.0D);
      vasFinalAmount = Float.valueOf(0.0F);
    } 
    System.out.println("Final Amount: " + vasFinalAmount);
    return vasFinalAmount;
  }
  
  public void addGrandVasDetails(String userId, Double grandAmount) {
    NimaiCustomerSubscriptionGrandAmount ncsgm = new NimaiCustomerSubscriptionGrandAmount();
    ncsgm.setUserId(userId);
    ncsgm.setGrandAmount(grandAmount);
    ncsgm.setVasApplied("Yes");
    ncsgm.setInsertedDate(new Date());
    this.nimaiCustomerGrandAmtRepository.save(ncsgm);
  }
  
  public void removeGrandVasDetails(Integer id) {
    this.nimaiCustomerGrandAmtRepository.deleteById(id);
  }
  
  public NimaiCustomerSubscriptionGrandAmount getCustomerVASAmount(String userId) {
    NimaiCustomerSubscriptionGrandAmount userDet = this.nimaiCustomerGrandAmtRepository.getVASDetByUserId(userId);
    return userDet;
  }
  
  @Override
  public void getLastSerialNoAndUpdate(String userId, String mode) {
    Integer serialNo = this.splanDetRepo.findLastSerialNo();
    System.out.println("SerialNo: " + serialNo);
    if (mode.equalsIgnoreCase("Wire")) {
      this.nimaiAdvisoryRepo.updateSplSerialNo(userId, serialNo);
    } else {
      this.nimaiAdvisoryRepo.updateSplSerialNo(userId, serialNo);
    } 
  }

@Override
public void addVasDetailsAfterSubscription(String userId, String subscriptionId, Integer vasId, String mode,
		Float pricing, String paymentTxnId, String invoiceId) {
	// TODO Auto-generated method stub
	
}
}
