package com.nimai.admin.service.impl;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nimai.admin.model.*;
import com.nimai.admin.repository.*;
import com.nimai.admin.util.ExcelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.nimai.admin.payload.CustomerResponse;
import com.nimai.admin.payload.PagedResponse;
import com.nimai.admin.payload.RefBean;
import com.nimai.admin.payload.ReferrerBean;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.service.RefererService;
import com.nimai.admin.specification.CustomerReferSearchSpecification;
import com.nimai.admin.specification.CustomerSearchSpecification;
import com.nimai.admin.specification.ReferrerSpecification;
import com.nimai.admin.util.Utility;

import javax.persistence.Tuple;

@Service
public class RefererServiceImpl implements RefererService {
	@Autowired
	ReferrerRepository referRepository;

	@Value("${RXIL.USER}")
	private String rxilUser;
	
	@Value("${REFER.USER}")
	private String saUser;
	
	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	CustomerSearchSpecification customerSearchSpecification;
	
	@Autowired
	CustomerReferSearchSpecification customerReferSearchSpecification;

	@Autowired
	ReferrerSpecification referSpecification;

	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	ReferrerSaLeadRepo referSaLeadRepo;
	
	@Autowired
	CustomerReferrerRepo custRefSaLeadRepo;
	
	  @Autowired
	  SubscriptionDetailsRepository subRepo;
	
	  @Autowired
	  nimaiSystemConfigRepository systemConfig;
	  
	  @Autowired
	  ReferralSalLeadRepository referralSaLeadRepo;
	  
	  
	  @Autowired
		ReferralLeadsRepo referralRepo;
	@Autowired
	PostPaidPlanRepository postPaidRepo;

//	@Override
//	  public PagedResponse<?> getRefDetails(SearchRequest request) {
//	    Page<NimaiMCustomer> referDetails;
//	    request.setSubscriberType("REFERRER");
//	    if (request.getRole() != null && (request.getRole().equalsIgnoreCase("Bank RM") || request
//	      .getRole().equalsIgnoreCase("Customer RM"))) {
//	      request.setLoginUserId(Utility.getUserId());
//	      request.setRmStatus("Active");
//	    } 
//	    String countryNames = Utility.getUserCountry();
//	    System.out.println("countryNames: " + countryNames);
//	    if (countryNames != null && countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
//	      countryNames = "";
//	      List<String> countryList = this.customerRepository.getCountryList();
//	      for (String country : countryList) {
//	        countryNames = countryNames + country + ",";
//	        request.setCountryNames(countryNames);
//	      } 
//	      System.out.println("Country List: " + countryNames);
//	      request.setCountryNames(countryNames);
//	    } else if (countryNames != null && !countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
//	      request.setCountryNames(countryNames);
//	    } else if (countryNames != null || request.getCountry() == null) {
//	    
//	    } 
//	    List<String> countryValue = (List<String>)Stream.<String>of(request.getCountryNames().split(",", -1)).collect(Collectors.toList());
//	    System.out.println("countryValue: " + countryValue);
//	    System.out.println("REquest--Txn Status: " + request.getTxtStatus());
//	    try {
//	      if (request.getTxtStatus().equalsIgnoreCase("null") || request.getTxtStatus() == null) {
//	        System.out.println("======= creating pageable (txn sts=null)=======");
//	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
//	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
//	            Sort.by(new String[] { request.getSortBy() }).ascending());
//	        System.out.println("======= Getting Referrer Data =======");
//	        referDetails = this.customerRepository.findAll(this.customerSearchSpecification.getFilter(request), (Pageable)pageRequest);
//	      } else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
//	        System.out.println("======= creating pageable (txn sts=not uploaded)=======");
//	        request.setSortBy("inserted_date");
//	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
//	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
//	            Sort.by(new String[] { request.getSortBy() }).ascending());
//	        System.out.println("======= Getting Not Uploaded KYC of Referrer =======");
//	        referDetails = this.customerRepository.getNotUploadForRE(countryValue, (Pageable)pageRequest);
//	      } else if (request.getTxtStatus().equalsIgnoreCase("Pending")) {
//	        System.out.println("======= creating pageable (txn sts=pending)=======");
//	        request.setSortBy("inserted_date");
//	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
//	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
//	            Sort.by(new String[] { request.getSortBy() }).ascending());
//	        System.out.println("======= Getting Not Uploaded KYC of Referrer =======");
//	        referDetails = this.customerRepository.getPendingForRE(countryValue, (Pageable)pageRequest);
//	      } else {
//	        System.out.println("======= creating pageable else (txn sts=null)=======");
//	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
//	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
//	            Sort.by(new String[] { request.getSortBy() }).ascending());
//	        System.out.println("======= Getting Referrer Data =======");
//	        referDetails = this.customerRepository.findAll(this.customerSearchSpecification.getFilter(request), (Pageable)pageRequest);
//	      } 
//	    } catch (Exception e) {
//	      System.out.println("In Exception block -- Referrer");
//	      System.out.println("======= creating pageable else (txn sts=null)=======");
//	      PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
//	          request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
//	          Sort.by(new String[] { request.getSortBy() }).ascending());
//	      System.out.println("======= Getting Referrer Data =======");
//	      referDetails = this.customerRepository.findAll(this.customerSearchSpecification.getFilter(request), (Pageable)pageRequest);
//	    } 
//	    List<CustomerResponse> responses = referDetails.map(ref -> {
//	          CustomerResponse response = new CustomerResponse();
//	          response.setUserid(ref.getUserid());
//	          System.out.println("==========================referreer userId====================" + ref.getUserid());
//	          response.setFirstName(ref.getFirstName());
//	          response.setLastName(ref.getLastName());
//	          response.setEmailAddress(ref.getEmailAddress());
//	          response.setMobileNumber(ref.getMobileNumber());
//	          response.setCountryName(ref.getCountryName());
//	          response.setLandline(ref.getLandline());
//	          response.setDesignation(ref.getDesignation());
//	          response.setCompanyName(ref.getCompanyName());
//	          if (ref.getKycStatus() == null) {
//	            response.setKyc("Not Uploaded");
//	          } else if (ref.getKycStatus().equals("PENDING")) {
//	            response.setKyc("Not Uploaded");
//	          } else {
//	            response.setKyc(ref.getKycStatus());
//	          } 
//	          List<NimaiMRefer> refer = this.referRepository.findReferByUserId(ref.getUserid());
//	          RefBean referBean = new RefBean();
//	          List<ReferrerBean> rfb = new ArrayList<>();
//	          Float totalEarning = Float.valueOf(0.0F);
//	          for (NimaiMRefer rf : refer) {
//	            NimaiMCustomer customer = new NimaiMCustomer();
//	            ReferrerBean rb = new ReferrerBean();
//	            try {
//	              customer = this.customerRepository.getUserIdByEmailId(rf.getEmailAddress());
//	            } catch (Exception e) {
//	              e.printStackTrace();
//	              System.out.println("Customer id which is not present :" + rf.getEmailAddress());
//	              continue;
//	            } 
//	            List<NimaiSubscriptionDetails> details = new ArrayList<>();
//	            
//	            try {
//	              details = this.subRepo.finSplanByReferId(customer.getUserid());
//	              Float referEarning = Float.valueOf(this.systemConfig.earningPercentage());
//	              Float actualREarning = Float.valueOf(referEarning.floatValue() / 100.0F);
//	              Float userTotalEarning = Float.valueOf(0.0F);
//	              if (details == null) {
//	                userTotalEarning = Float.valueOf(0.0F);
//	              } else if (details.size() == 1) {
//	                if ((customer.getPaymentStatus().equalsIgnoreCase("Approved") || customer.getPaymentStatus().equalsIgnoreCase("Success")) && customer.getKycStatus().equalsIgnoreCase("Approved")) {
//	                  Integer totalEarn = this.customerRepository.findTotalEarning(customer.getUserid());
//	                  if (totalEarn == null) {
//	                    userTotalEarning = Float.valueOf(0.0F);
//	                  } else {
//	                    Float value = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format((totalEarn.intValue() * actualREarning.floatValue()))));
//	                    userTotalEarning = value;
//	                  } 
//	                } else {
//	                  userTotalEarning = Float.valueOf(0.0F);
//	                } 
//	              } else {
//	                Integer totalEarn = this.customerRepository.findTotalEarning(customer.getUserid());
//	                if (totalEarn == null) {
//	                  userTotalEarning = Float.valueOf(0.0F);
//	                } else {
//	                  Float value = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format((totalEarn.intValue() * actualREarning.floatValue()))));
//	                  userTotalEarning = value;
//	                } 
//	              } 
//	              rb.setUserWiseTotalEarning(userTotalEarning.floatValue());
//	              rb.setUserId(customer.getUserid());
//	              rfb.add(rb);
//	            } catch (Exception e) {
//	              e.printStackTrace();
//	            } 
//	          } 
//	          referBean.setRfb(rfb);
//	          Integer approvedReference = Integer.valueOf(this.customerRepository.getApprovedReferrenceNew(ref.getUserid()));
//	          if (approvedReference.equals(null)) {
//	            response.setApprovedReference(0L);
//	          } else {
//	            response.setApprovedReference(approvedReference.intValue());
//	          } 
//	          try {
//	            Integer totalReference = Integer.valueOf(this.customerRepository.getTotareference(ref.getUserid()));
//	            if (totalReference.equals(null)) {
//	              response.setTotalReference(0);
//	            } else {
//	              response.setTotalReference(totalReference.intValue());
//	            } 
//	          } catch (Exception e) {
//	            e.printStackTrace();
//	          } 
//	          try {
//	            Integer pendingReference = Integer.valueOf(this.customerRepository.getpendingReference(ref.getUserid()));
//	            if (pendingReference.equals(null)) {
//	              response.setPendingReference(0L);
//	            } else {
//	              response.setPendingReference(pendingReference.intValue());
//	            } 
//	          } catch (Exception e) {
//	            e.printStackTrace();
//	          } 
//	          try {
//	            Integer rejectedReference = Integer.valueOf(this.customerRepository.getRejectedReference(ref.getUserid()));
//	            if (rejectedReference.equals(null)) {
//	              response.setRejectedReference(0L);
//	            } else {
//	              response.setRejectedReference(rejectedReference.intValue());
//	            } 
//	          } catch (Exception e) {
//	            e.printStackTrace();
//	          } 
//	          NimaiMCustomer referrDetails = (NimaiMCustomer)this.customerRepository.getOne(ref.getUserid());
//	          String accountStatus = checkAccStatus(referrDetails);
//	          if (accountStatus == null) {
//	            response.setEarning(String.valueOf(0));
//	          } else if (accountStatus.equalsIgnoreCase("ACTIVE")) {
//	            try {
//	              Double value = this.customerRepository.getEarning(request.getUserId());
//	              if (value == null || value.equals(null)) {
//	                value = Double.valueOf(0.0D);
//	              } else {
//	                value = value;
//	              } 
//	              Float referEarning = Float.valueOf(this.systemConfig.earningPercentage());
//	              Float actualREarning = Float.valueOf(referEarning.floatValue() / 100.0F);
//	              Float earning = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format(value.doubleValue() * actualREarning.floatValue())));
//	              if (earning.equals(null)) {
//	                response.setEarning(String.valueOf(0));
//	              } else {
//	                response.setEarning(String.valueOf(earning));
//	              } 
//	            } catch (Exception e) {
//	              e.printStackTrace();
//	            } 
//	          } else {
//	            response.setEarning(String.valueOf(0));
//	          } 
//	          response.setInsertedDate(ref.getInsertedDate());
//	          response.setInsertedDate(ref.getInsertedDate());
//	          return response;
//	        }).getContent();
//	    return new PagedResponse(responses, referDetails.getNumber(), referDetails.getSize(), referDetails
//	        .getTotalElements(), referDetails.getTotalPages(), referDetails.isLast());
//	  }
//	  
	@Override
	  public PagedResponse<?> getRefDetails(SearchRequest request) {
	    Page<NimaiMCustomer> referDetails;
	    request.setSubscriberType("REFERRER");
//		List<Tuple> custList1 = this.customerRepository.getEarningForSingleRefer("CU49111","CLOVER");
//		Double vasAmount1 = this.subscriptionVasRepo.getActiveVasAmount("CU49111");
//		Double value1 = Utility.getReferEarnings(custList1,"userid",vasAmount1);
//		System.out.println("Earnings for referrer as per rule rxilUser-----> "+value1);
	    if (request.getRole() != null && (request.getRole().equalsIgnoreCase("Bank RM") || request
	      .getRole().equalsIgnoreCase("Customer RM"))) {
	      request.setLoginUserId(Utility.getUserId());
	      request.setRmStatus("Active");
	    } 
	    String countryNames = Utility.getUserCountry();
	    System.out.println("countryNames: " + countryNames);
	    if (countryNames != null && countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
	      countryNames = "";
	      List<String> countryList = this.customerRepository.getCountryList();
	      for (String country : countryList) {
	        countryNames = countryNames + country + ",";
	        request.setCountryNames(countryNames);
	      } 
	      System.out.println("Country List: " + countryNames);
	      request.setCountryNames(countryNames);
	    } else if (countryNames != null && !countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
	      request.setCountryNames(countryNames);
	    } else if (countryNames != null || request.getCountry() == null) {
	    
	    } 
	    List<String> countryValue = (List<String>)Stream.<String>of(request.getCountryNames().split(",", -1)).collect(Collectors.toList());
	    System.out.println("countryValue: " + countryValue);
	    System.out.println("REquest--Txn Status: " + request.getTxtStatus());
	    try {
	      if (request.getTxtStatus() == null || request.getTxtStatus().equalsIgnoreCase("null") ) {
	        System.out.println("======= creating pageable (txn sts=null)=======");
	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
	            Sort.by(new String[] { request.getSortBy() }).ascending());
	        System.out.println("======= Getting Referrer Data =======");
	        referDetails = this.customerRepository.findAll(this.customerSearchSpecification.getFilter(request), (Pageable)pageRequest);
	      } else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
	        System.out.println("======= creating pageable (txn sts=not uploaded)=======");
	        request.setSortBy("inserted_date");
	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
	            Sort.by(new String[] { request.getSortBy() }).ascending());
	        System.out.println("======= Getting Not Uploaded KYC of Referrer =======");
	        referDetails = this.customerRepository.getNotUploadForRE(countryValue, (Pageable)pageRequest);
	      } else if (request.getTxtStatus().equalsIgnoreCase("Pending")) {
	        System.out.println("======= creating pageable (txn sts=pending)=======");
	        request.setSortBy("inserted_date");
	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
	            Sort.by(new String[] { request.getSortBy() }).ascending());
	        System.out.println("======= Getting Not Uploaded KYC of Referrer =======");
	        referDetails = this.customerRepository.getPendingForRE(countryValue, (Pageable)pageRequest);
	      } else {
	        System.out.println("======= creating pageable else (txn sts=null)=======");
	        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
	            request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
	            Sort.by(new String[] { request.getSortBy() }).ascending());
	        System.out.println("======= Getting Referrer Data =======");
	        referDetails = this.customerRepository.findAll(this.customerSearchSpecification.getFilter(request), (Pageable)pageRequest);
	      } 
	    } catch (Exception e) {
	      System.out.println("In Exception block -- Referrer");
	      System.out.println("======= creating pageable else (txn sts=null)=======");
	      PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
	          request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
	          Sort.by(new String[] { request.getSortBy() }).ascending());
	      System.out.println("======= Getting Referrer Data =======");
	      referDetails = this.customerRepository.findAll(this.customerSearchSpecification.getFilter(request), (Pageable)pageRequest);
	  
	    } 
	    
	    
	    
	    
	    List<CustomerResponse> responses = referDetails.map(ref -> {
	          CustomerResponse response = new CustomerResponse();
	          response.setUserid(ref.getUserid());
	          System.out.println("==========================referreer userId====================" + ref.getUserid());
	          response.setFirstName(ref.getFirstName());
	          response.setLastName(ref.getLastName());
	          response.setEmailAddress(ref.getEmailAddress());
	          response.setMobileNumber(ref.getMobileNumber());
	          response.setCountryName(ref.getCountryName());
	          response.setLandline(ref.getLandline());
	          response.setDesignation(ref.getDesignation());
	          response.setCompanyName(ref.getCompanyName());
	          if (ref.getKycStatus() == null) {
	            response.setKyc("Not Uploaded");
	          } else if (ref.getKycStatus().equals("PENDING")) {
	            response.setKyc("Not Uploaded");
	          } else {
	            response.setKyc(ref.getKycStatus());
	          } 
	          List<NimaiMRefer> refer = this.referRepository.findReferByUserId(ref.getUserid());
	          RefBean referBean = new RefBean();
	          List<ReferrerBean> rfb = new ArrayList<>();
	          Float totalEarning = Float.valueOf(0.0F);
	          for (NimaiMRefer rf : refer) {
	            NimaiMCustomer customer = new NimaiMCustomer();
	            ReferrerBean rb = new ReferrerBean();
	            try {
	              customer = this.customerRepository.getUserIdByEmailId(rf.getEmailAddress());
	            } catch (Exception e) {
	              e.printStackTrace();
	              System.out.println("Customer id which is not present :" + rf.getEmailAddress());
	              continue;
	            } 
	            List<NimaiSubscriptionDetails> details = new ArrayList<>();
	            
	            try {
	              details = this.subRepo.finSplanByReferId(customer.getUserid());
	              Float referEarning = Float.valueOf(this.systemConfig.earningPercentage());
	              Float actualREarning = Float.valueOf(referEarning.floatValue() / 100.0F);
	              Float userTotalEarning = Float.valueOf(0.0F);
	              if (details == null) {
	                userTotalEarning = Float.valueOf(0.0F);
	              } else if (details.size() == 1) {
	                if ((customer.getPaymentStatus().equalsIgnoreCase("Approved") || customer.getPaymentStatus().equalsIgnoreCase("Success")) && customer.getKycStatus().equalsIgnoreCase("Approved")) {
	                  //Integer totalEarn = this.customerRepository.findTotalEarning(customer.getUserid());
	                  Integer totalEarn = this.custRefSaLeadRepo.findTotalEarningCuRef(customer.getUserid());
	                  if (totalEarn == null) {
	                    userTotalEarning = Float.valueOf(0.0F);
	                  } else {
	                    Float value = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format((totalEarn.intValue() * actualREarning.floatValue()))));
	                    userTotalEarning = value;
	                  } 
	                } else {
	                  userTotalEarning = Float.valueOf(0.0F);
	                } 
	              } else {
	              //  Integer totalEarn = this.customerRepository.findTotalEarning(customer.getUserid());
	                Integer totalEarn = this.custRefSaLeadRepo.findTotalEarningCuRef(customer.getUserid());
	                if (totalEarn == null) {
	                  userTotalEarning = Float.valueOf(0.0F);
	                } else {
	                  Float value = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format((totalEarn.intValue() * actualREarning.floatValue()))));
	                  userTotalEarning = value;
	                } 
	              } 
	              rb.setUserWiseTotalEarning(userTotalEarning.floatValue());
	              rb.setUserId(customer.getUserid());
	              rfb.add(rb);
	            } catch (Exception e) {
	              e.printStackTrace();
	            } 
	          } 
	          referBean.setRfb(rfb);
	       //   Integer approvedReference = Integer.valueOf(this.customerRepository.getApprovedReferrenceNew(ref.getUserid()));
	          Integer approvedReference = 0;
	          if(request.getRole().equalsIgnoreCase("Management"))
        	  {
	        	  if(ref.getUserid().equalsIgnoreCase(saUser))
	        		  approvedReference = Integer.valueOf(this.custRefSaLeadRepo.getApprovedReferrenceNewCuRefSA(ref.getUserid()));
	        	  else
	        		  approvedReference = Integer.valueOf(this.custRefSaLeadRepo.getApprovedReferrenceNewCuRefSA(ref.getUserid()));
        	  }
	          else
	          {
	        	  approvedReference = Integer.valueOf(this.custRefSaLeadRepo.getApprovedReferrenceNewCuRef(ref.getUserid(),Utility.getUserId(),"Active"));
	          }
	          if (approvedReference.equals(null)) {
	            response.setApprovedReference(0L);
	          } else {
	            response.setApprovedReference(approvedReference.intValue());
	          } 
	          try {
	        	  System.out.println("Refer By: "+ref.getUserid());
	        	  Integer totalReference=0;
	        	  
	        	  
	        	  if(ref.getUserid().equalsIgnoreCase(rxilUser) || ref.getUserid().equalsIgnoreCase("RE32221"))
	        	  {
	        		  totalReference = Integer.valueOf(this.customerRepository.getTotareferenceForRxilFieoReferrer(ref.getUserid()));
	        	  }
	        	  else if(request.getRole().equalsIgnoreCase("Management"))
	        	  {
		        	  if(ref.getUserid().equalsIgnoreCase(saUser))
		        		  totalReference = Integer.valueOf(this.custRefSaLeadRepo.getTotareferenceNewCuRefforSA(ref.getUserid()));
		        	  else
		        		  totalReference = Integer.valueOf(this.custRefSaLeadRepo.getTotareferenceNewCuRefforSA(ref.getUserid()));
	        	  }
	        	  else
	        	  {
	        		  //totalReference = Integer.valueOf(this.customerRepository.getTotareferenceNew(ref.getUserid()));
	        		  totalReference = Integer.valueOf(this.custRefSaLeadRepo.getTotareferenceNewCuRef(ref.getUserid(),Utility.getUserId(),"Active"));
	        	  }
	            if (totalReference.equals(null)) {
	              response.setTotalReference(0);
	            } else {
	              response.setTotalReference(totalReference.intValue());
	            } 
	          } catch (Exception e) {
	            e.printStackTrace();
	          } 
	          try {
	            //Integer pendingReference = Integer.valueOf(this.customerRepository.getpendingReferenceNew(ref.getUserid()));
	            Integer pendingReference = 0;
	            if(request.getRole().equalsIgnoreCase("Management"))
	        	{
	            	if(ref.getUserid().equalsIgnoreCase(saUser))
	            		pendingReference = Integer.valueOf(this.custRefSaLeadRepo.getpendingReferenceNewCuRefSA(ref.getUserid()));
		        	else
		        		pendingReference = Integer.valueOf(this.custRefSaLeadRepo.getpendingReferenceNewCuRefSA(ref.getUserid()));
	        	}
		        else
		        {		
		        	pendingReference = Integer.valueOf(this.custRefSaLeadRepo.getpendingReferenceNewCuRef(ref.getUserid(),Utility.getUserId(),"Active"));
		        }
	            if (pendingReference.equals(null)) {
	              response.setPendingReference(0L);
	            } else {
	              response.setPendingReference(pendingReference.intValue());
	            } 
	          } catch (Exception e) {
	            e.printStackTrace();
	          } 
	          try {
	           // Integer rejectedReference = Integer.valueOf(this.customerRepository.getRejectedReferenceNew(ref.getUserid()));
	            Integer rejectedReference = 0;
	            
	            if(request.getRole().equalsIgnoreCase("Management"))
	        	{
	            	if(ref.getUserid().equalsIgnoreCase(saUser))
	            		rejectedReference = Integer.valueOf(this.custRefSaLeadRepo.getRejectedReferenceNewCuRefSA(ref.getUserid()));
		        	else
		        		rejectedReference = Integer.valueOf(this.custRefSaLeadRepo.getRejectedReferenceNewCuRefSA(ref.getUserid()));
	        	}
		        else
		        {	
		        	rejectedReference = Integer.valueOf(this.custRefSaLeadRepo.getRejectedReferenceNewCuRef(ref.getUserid(),Utility.getUserId(),"Active"));
		        }
	            if (rejectedReference.equals(null)) {
	              response.setRejectedReference(0L);
	            } else {
	              response.setRejectedReference(rejectedReference.intValue());
	            } 
	          } catch (Exception e) {
	            e.printStackTrace();
	          } 
	          NimaiMCustomer referrDetails = (NimaiMCustomer)this.customerRepository.getOne(ref.getUserid());
	          String accountStatus = checkAccStatus(referrDetails);
	          if (accountStatus == null) {
	            response.setEarning(String.valueOf(0));
	          } else if (accountStatus.equalsIgnoreCase("ACTIVE")) {
	            try {
					String loginId = Utility.getUserId();
					Double value=0.0D;
					if(Objects.equals(request.getRole(), "Customer RM")){
						System.out.println("ref.getUserid()-------> "+ref.getUserid()+" loginId ------> "+loginId);
						value = this.customerRepository.getEarningByCustRm(ref.getUserid(),loginId);
					}else{
						System.out.println("ref.getUserid()-------> "+ref.getUserid());
						value = this.customerRepository.getEarning(ref.getUserid());
					}
//					List<Tuple> custList = this.customerRepository.getEarning(ref.getUserid(),loginId);
//					Double vasAmount = this.subscriptionVasRepo.getVasAmount(custList.stream().map(e->e.get("USERID").toString()).collect(Collectors.toList()));
//					Double value = Utility.getReferEarnings(custList,"all",vasAmount);
					System.out.println("Earnings for referrer as per rule-----> "+value);
	              if (value == null || value.equals(null)) {
	                value = Double.valueOf(0.0D);
	              } else {
	                value = value;
	              } 
	              Float referEarning = Float.valueOf(this.systemConfig.earningPercentage());
	              Float actualREarning = referEarning / 100.0F;
	              Float earning = Float.parseFloat((new DecimalFormat("##.##")).format(value.doubleValue() * actualREarning.floatValue()));
	              if (earning.equals(null)) {
	                response.setEarning(String.valueOf(0));
	              } else {
	                response.setEarning(String.valueOf(earning));
	              } 
	            } catch (Exception e) {
	              e.printStackTrace();
	            } 
	          } else {
	            response.setEarning(String.valueOf(0));
	          } 
	          response.setInsertedDate(ref.getInsertedDate());
	          response.setInsertedDate(ref.getInsertedDate());
	          return response;
	        }).getContent();
	    return new PagedResponse(responses, referDetails.getNumber(), referDetails.getSize(), referDetails
	        .getTotalElements(), referDetails.getTotalPages(), referDetails.isLast());
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
	  
	@Override
	public PagedResponse<?> getRefDetails1(SearchRequest request) {
		request.setSubscriberType("REFERRER");
		Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc") ? Sort.by(request.getSortBy()).descending()
						: Sort.by(request.getSortBy()).ascending());

		Page<NimaiMCustomer> referDetails;
		String countryNames = Utility.getUserCountry();
		if (countryNames != null && countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {

		} else if (countryNames != null && !countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
			request.setCountryNames(countryNames);
		} else if (countryNames == null && request.getCountry() == null) {
			referDetails = null;
		}

		referDetails = customerRepository.findAll(customerSearchSpecification.getFilter(request), pageable);

		List<CustomerResponse> responses = referDetails.map(ref -> {
			CustomerResponse response = new CustomerResponse();
			response.setUserid(ref.getUserid());
			response.setFirstName(ref.getFirstName());
			response.setLastName(ref.getLastName());
			response.setEmailAddress(ref.getEmailAddress());
			response.setMobileNumber(ref.getMobileNumber());
			response.setCountryName(ref.getCountryName());
			response.setLandline(ref.getLandline());
			response.setDesignation(ref.getDesignation());
			response.setCompanyName(ref.getCompanyName());
			response.setKyc(ref.getKycStatus());
			response.setTotalReference(ref.getNimaiMReferList().size());
			int approve = 0;
			int reject = 0;
			for (int i = 0; i < ref.getNimaiMReferList().size(); i++) {
				NimaiMCustomer data = customerRepository
						.findByEmailAddress(ref.getNimaiMReferList().get(i).getEmailAddress().toLowerCase());
				if (data != null) {
					if (data.getKycStatus() != null && data.getKycStatus().equalsIgnoreCase("Approved")) {
						approve = approve + 1;
					} else if (data.getKycStatus() != null && data.getKycStatus().equalsIgnoreCase("rejected")) {
						reject = reject + 1;
					}
				}
			}

			response.setApprovedReference(approve);
			response.setRejectedReference(reject);
			response.setPendingReference(ref.getNimaiMReferList().size() - approve - reject);
			response.setInsertedDate(ref.getInsertedDate());

			response.setInsertedDate(ref.getInsertedDate());
			return response;
		}).getContent();

		return new PagedResponse<>(responses, referDetails.getNumber(), referDetails.getSize(),
				referDetails.getTotalElements(), referDetails.getTotalPages(), referDetails.isLast());

	}



	@Override
	public List<String> userIdSearch(String userId, String data) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = Stream.of(val.split(",")).collect(Collectors.toList());
			return customerRepository.userIdDataSearchByCountry(userId.toLowerCase(), data, list);
		} else {
			return customerRepository.userIdDataSearch(userId.toLowerCase(), data);
		}
	}

	@Override
	public List<String> emailIdSearch(String emailId) {
//			String data) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = Stream.of(val.split(",")).collect(Collectors.toList());
			// return customerRepository.emailIdDataSearchByCountry(emailId.toLowerCase(),
			// data, list);
			return customerRepository.emailIdDataSearchByCountry(emailId.toLowerCase(), list);
		} else {
//			return customerRepository.emailIdDataSearch(emailId.toLowerCase(), data);
			return customerRepository.emailIdDataSearch(emailId.toLowerCase());
		}
	}

	@Override
	public List<String> mobileNumberSearch(String mobileNo) {
//			, String data) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = Stream.of(val.split(",")).collect(Collectors.toList());
			return customerRepository.mobileNumberDataSearchByCountry(mobileNo.toLowerCase(), list);
			// return
			// customerRepository.mobileNumberDataSearchByCountry(mobileNo.toLowerCase(),
			// data, list);
		} else {
			return customerRepository.mobileNumberDataSearch(mobileNo.toLowerCase());
			// return customerRepository.mobileNumberDataSearch(mobileNo.toLowerCase(),
			// data);
		}
	}

	@Override
	public List<String> companyNameSearch(String companyName) {
//			, String data) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = Stream.of(val.split(",")).collect(Collectors.toList());
			return customerRepository.companyNameDataSearchByCountry(companyName.toLowerCase(), list);
			// return
			// customerRepository.companyNameDataSearchByCountry(companyName.toLowerCase(),
			// data, list);
		} else {
			return customerRepository.companyNameDataSearch(companyName.toLowerCase());
			// return customerRepository.companyNameDataSearch(companyName.toLowerCase(),
			// data);
		}
	}

//	@Override
//	public PagedResponse<?> getAllReferDetails(SearchRequest request) {
//		List<CustomerResponse> responses = null;
//		Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
//				request.getDirection().equalsIgnoreCase("desc") ? Sort.by(request.getSortBy()).descending()
//						: Sort.by(request.getSortBy()).ascending());
//
////		Page<NimaiMRefer> referDetails = referRepository.findAll(referSpecification.getFilter(request), pageable);
////		
//		
//Page<NimaiMRefer> referDetails = referRepository.findAllById(pageable);
//		
//		
//		
//		if (request.getTxtStatus().equalsIgnoreCase("all")) {
//			responses = referDetails.map(ref -> {
//				CustomerResponse response = new CustomerResponse();
//				response.setFirstName(ref.getFirstName());
//				response.setLastName(ref.getLastName());
//				response.setEmailAddress(ref.getEmailAddress());
//				response.setMobileNumber(ref.getMobileNo());
//				response.setCountryName(ref.getCountryName());
//				response.setCompanyName(ref.getCompanyName());
//				response.setInsertedDate(ref.getInsertedDate());
//				response.setAccountStatus(ref.getStatus());
//				response.setReferId(ref.getId());
//				return response;
//			}).getContent();
//		} else if (request.getTxtStatus().equalsIgnoreCase("approved")
//				|| request.getTxtStatus().equalsIgnoreCase("rejected")) {
//			responses = referDetails.map(ref -> {
//				String customer = customerRepository.findKycByEmailAddress(ref.getEmailAddress().toLowerCase());
//				if (customer != null && customer.equalsIgnoreCase(request.getTxtStatus())) {
//					CustomerResponse response = new CustomerResponse();
//					response.setFirstName(ref.getFirstName());
//					response.setLastName(ref.getLastName());
//					response.setEmailAddress(ref.getEmailAddress());
//					response.setMobileNumber(ref.getMobileNo());
//					response.setCountryName(ref.getCountryName());
//					response.setCompanyName(ref.getCompanyName());
//					response.setInsertedDate(ref.getInsertedDate());
//					response.setAccountStatus(ref.getStatus());
//					response.setReferId(ref.getId());
//					return response;
//				}
//				return null;
//			}).toList();
//		} else if (request.getTxtStatus().equalsIgnoreCase("pending")) {
//			responses = referDetails.map(ref -> {
//				String customer = customerRepository.findKycByEmailAddress(ref.getEmailAddress().toLowerCase());
//				if (customer == null || customer.equalsIgnoreCase(request.getTxtStatus())) {
//					CustomerResponse response = new CustomerResponse();
//					response.setFirstName(ref.getFirstName());
//					response.setLastName(ref.getLastName());
//					response.setEmailAddress(ref.getEmailAddress());
//					response.setMobileNumber(ref.getMobileNo());
//					response.setCountryName(ref.getCountryName());
//					response.setCompanyName(ref.getCompanyName());
//					response.setInsertedDate(ref.getInsertedDate());
//					response.setAccountStatus(ref.getStatus());
//					response.setReferId(ref.getId());
//					return response;
//				}
//				return null;
//			}).toList();
//		}
//
//		return new PagedResponse<>(responses, referDetails.getNumber(), referDetails.getSize(),
//				referDetails.getTotalElements(), referDetails.getTotalPages(), referDetails.isLast());
//	}
	
	
	
	public PagedResponse<?> getAllReferDetails(SearchRequest request) {
		List<CustomerResponse> responses = null;
	
		
		Page<NimaiMRefer> referDetails = null;
		Page<ReferralLeads> referralLeads = null;

		try	{
			if(request.getLeadStatus()!=null && request.getLeadEmail()!=null){
				NimaiMCustomer customer = this.customerRepository.findByEmailAddress(request.getLeadEmail());
				System.out.println("cust---------> "+customer.getUserid());
				customer.setLeadStatus(request.getLeadStatus());
				customer.setLeadComment(request.getLeadRemark());
				this.customerRepository.save(customer);
//			this.customerRepository.updateLeadStatusById(request.getReferId(),request.getLeadStatus(),
//					request.getLeadRemark());
			}
		}catch (Exception ex){
			System.out.println("unable to set lead_status value ");
		}
		
		if(request.getUserId().equalsIgnoreCase(rxilUser))
		{
			request.setSortBy("INSERTED_DATE");
			Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ? Sort.by(request.getSortBy()).descending()
							: Sort.by(request.getSortBy()).ascending());
			System.out.println("===== In FIEO/RXIL referrer =====");
			
			if (request.getTxtStatus().equalsIgnoreCase("all")) {
				//referralLeads=referralRepo.getRxilFIEOReferrerDetails(rxilUser, pageable);
				referralLeads=referralSaLeadRepo.getRxilFIEOReferrerDetails(rxilUser, pageable,Utility.getUserId(),"Active");
				responses = referralLeads.map(ref -> {
					NimaiMCustomer custDetails=customerRepository.findCustomersByLeadId(ref.getLeadId());
					CustomerResponse response = new CustomerResponse();
					if(custDetails==null) {
						response.setFirstName("");
						response.setLastName("");
						response.setEmailAddress( "");
						response.setMobileNumber( "");
						response.setCountryName("");
						response.setCompanyName("");
						//response.setInsertedDate(custDetails.getInsertedDate()==null ? "":custDetails.getInsertedDate());
						response.setAccountStatus( "");
						response.setReferId(0);
						//response.setMembershipStatus(ref.getMembershipStatus());
					}else {
						response.setFirstName(custDetails.getFirstName()==null ? "":custDetails.getFirstName());
						response.setLastName(custDetails.getLastName()==null ? "":custDetails.getLastName());
						response.setEmailAddress(custDetails.getEmailAddress()==null ? "":custDetails.getEmailAddress());
						response.setMobileNumber(custDetails.getMobileNumber()==null ? "":custDetails.getMobileNumber());
						response.setCountryName(custDetails.getCountryName()==null ? "":custDetails.getCountryName());
						response.setCompanyName(custDetails.getCompanyName()==null ? "":custDetails.getCompanyName());
						response.setInsertedDate(custDetails.getInsertedDate());
						response.setAccountStatus(custDetails.getAccountStatus()==null ? "": custDetails.getAccountStatus());
						response.setReferId(ref.getLeadId());
						response.setMembershipStatus(ref.getMembershipStatus());
						response.setIsActPassed(!custDetails.getNimaiMLoginList().isEmpty()?custDetails.getNimaiMLoginList()
								.get(0).getIsActPassed():"");
						response.setLeadStatus(custDetails.getLeadStatus());
						response.setLeadRemark(custDetails.getLeadComment());
					}
				
					return response;
				}).getContent();
				System.out.println("firstname 10"+responses);
				return new PagedResponse<>(responses, referralLeads.getNumber(), referralLeads.getSize(),
						referralLeads.getTotalElements(), referralLeads.getTotalPages(), referralLeads.isLast());
			}
			else if (request.getTxtStatus().equalsIgnoreCase("pending"))
			{
				
				//referralLeads=referralRepo.getRxilFieoReferrerPendingDetails(rxilUser, pageable);
				referralLeads=referralSaLeadRepo.getRxilFieoReferrerPendingDetails(rxilUser, pageable,Utility.getUserId(),"Active");
				responses = referralLeads.map(ref -> {
					NimaiMCustomer custDetails=customerRepository.findCustomersByLeadId(ref.getLeadId());
					CustomerResponse response = new CustomerResponse();
				
					response.setFirstName(custDetails.getFirstName());
					
					response.setLastName(custDetails.getLastName());
					
					response.setEmailAddress(custDetails.getEmailAddress());
					
					response.setMobileNumber(custDetails.getMobileNumber());
					
					response.setCountryName(custDetails.getCountryName());
					
					response.setCompanyName(custDetails.getCompanyName());
					
					response.setInsertedDate(ref.getInsertedDate());
					
					response.setAccountStatus(custDetails.getAccountStatus());
					response.setMembershipStatus(ref.getMembershipStatus());
					
					response.setReferId(ref.getLeadId());
					response.setIsActPassed(!custDetails.getNimaiMLoginList().isEmpty()?custDetails.getNimaiMLoginList()
							.get(0).getIsActPassed():"");
					response.setLeadStatus(custDetails.getLeadStatus());
					response.setLeadRemark(custDetails.getLeadComment());
					return response;
				}).getContent();
	
			}
			else if (request.getTxtStatus().equalsIgnoreCase("Approved")) {

//referralLeads=referralRepo.getRxilFieoReferrerApprovedDetails(rxilUser, pageable);

referralLeads=referralSaLeadRepo.getRxilFieoReferrerApprovedDetails(rxilUser, pageable,Utility.getUserId(),"Active");
			responses = referralLeads.map(ref -> {
				NimaiMCustomer custDetails=customerRepository.findCustomersByLeadId(ref.getLeadId());
				CustomerResponse response = new CustomerResponse();
			
				response.setFirstName(custDetails.getFirstName());
				
				response.setLastName(custDetails.getLastName());
				
				response.setEmailAddress(custDetails.getEmailAddress());
				response.setCountryName(custDetails.getCountryName());
				response.setCompanyName(custDetails.getCompanyName());
				
				response.setInsertedDate(custDetails.getInsertedDate());
				response.setAccountStatus(custDetails.getAccountStatus());
				response.setMobileNumber(custDetails.getMobileNumber());
				response.setMembershipStatus(ref.getMembershipStatus());
				
				response.setReferId(ref.getLeadId());
				response.setIsActPassed(!custDetails.getNimaiMLoginList().isEmpty()?custDetails.getNimaiMLoginList()
						.get(0).getIsActPassed():"");
				response.setLeadStatus(custDetails.getLeadStatus());
				response.setLeadRemark(custDetails.getLeadComment());
				return response;
			}).getContent();

			}
			else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {

				//referralLeads=referralRepo.getRxilFieoReferrerRejectedDetails(rxilUser, pageable);
				referralLeads=referralSaLeadRepo.getRxilFieoReferrerRejectedDetails(rxilUser, pageable,Utility.getUserId(),"Active");
				responses = referralLeads.map(ref -> {
					NimaiMCustomer custDetails=customerRepository.findCustomersByLeadId(ref.getLeadId());
					CustomerResponse response = new CustomerResponse();
				
					response.setFirstName(custDetails.getFirstName());
					
					response.setLastName(custDetails.getLastName());
					
					response.setEmailAddress(custDetails.getEmailAddress());
					
					response.setMembershipStatus(ref.getMembershipStatus());
					
					response.setCountryName(custDetails.getCountryName());
					
					response.setCompanyName(custDetails.getCompanyName());
					
					response.setInsertedDate(custDetails.getInsertedDate());
					
					response.setAccountStatus(custDetails.getAccountStatus());
					response.setMobileNumber(custDetails.getMobileNumber());
					
					response.setReferId(ref.getLeadId());
					response.setIsActPassed(!custDetails.getNimaiMLoginList().isEmpty()?custDetails.getNimaiMLoginList()
							.get(0).getIsActPassed():"");
					response.setLeadStatus(custDetails.getLeadStatus());
					response.setLeadRemark(custDetails.getLeadComment());
					return response;
				}).getContent();

				}
				
			
			return new PagedResponse<>(responses, referralLeads.getNumber(), referralLeads.getSize(),
					referralLeads.getTotalElements(), referralLeads.getTotalPages(), referralLeads.isLast());
		
		}
		else
		{
			request.setSortBy("INSERTED_DATE");
			String loginUser=Utility.getUserId();
			String Status="Active";
			Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ? Sort.by(request.getSortBy()).descending()
							: Sort.by(request.getSortBy()).ascending());
			System.out.println("===== In other referrer =====");
			System.out.println("===== In other referrer ====="+Status);
//referDetails = referRepository.findAll(referSpecification.getFilter(request), pageable);
			//referDetails = referRepository.findAllByIdNew(request.getUserId(),pageable);

			 if(request.getRole().equalsIgnoreCase("Management"))
	        	{
				 referDetails = referSaLeadRepo.findAllByIdNewRefSaLeadWithoutRm(request.getUserId(),pageable);
	        	}
			 else
			 {
				 System.out.println("request.getUserId()--> "+request.getUserId()+" loginUser-> "+loginUser);
			referDetails = referSaLeadRepo.findAllByIdNewRefSaLead(request.getUserId(),pageable,loginUser);
			 }
		if (request.getTxtStatus().equalsIgnoreCase("all")) {
			responses = referDetails.map(ref -> {
				NimaiMCustomer cust = customerRepository.existsByEmailId(ref.getEmailAddress());
				CustomerResponse response = new CustomerResponse();
				response.setFirstName(ref.getFirstName());
				response.setLastName(ref.getLastName());
				response.setEmailAddress(ref.getEmailAddress());
				response.setMobileNumber(ref.getMobileNo());
				response.setCountryName(ref.getCountryName());
				response.setCompanyName(ref.getCompanyName());
				response.setInsertedDate(ref.getInsertedDate());
				response.setAccountStatus(ref.getStatus());
				response.setReferId(ref.getId());
				response.setIsActPassed(!cust.getNimaiMLoginList().isEmpty()?cust.getNimaiMLoginList()
						.get(0).getIsActPassed():"");
				response.setLeadStatus(cust.getLeadStatus());
				response.setLeadRemark(cust.getLeadComment());
				System.out.println("Lead status value---------> "+response.getLeadStatus());
				return response;
			}).getContent();
		} else if (request.getTxtStatus().equalsIgnoreCase("approved")
				) {
			//referDetails=referRepository.getReferrerApprovedDetails(request.getUserId(), pageable);
			
			if(request.getRole().equalsIgnoreCase("Management"))
        	{
            	if(request.getUserId().equalsIgnoreCase(saUser))
            		referDetails=referSaLeadRepo.getReferrerApprovedDetailsRefSaLeadSA(request.getUserId(), pageable);
	        	else
	        		referDetails=referSaLeadRepo.getReferrerApprovedDetailsRefSaLeadSA(request.getUserId(), pageable);
        	}
			else
			{
				referDetails=referSaLeadRepo.getReferrerApprovedDetailsRefSaLead(request.getUserId(), pageable,loginUser,Status);
			}
			responses = referDetails.map(ref -> {
				String customer = customerRepository.findKycByEmailAddress(ref.getEmailAddress().toLowerCase());
				NimaiMCustomer cust = customerRepository.existsByEmailId(ref.getEmailAddress());
				if (customer != null && customer.equalsIgnoreCase(request.getTxtStatus())) {
					CustomerResponse response = new CustomerResponse();
					response.setFirstName(ref.getFirstName());
					response.setLastName(ref.getLastName());
					response.setEmailAddress(ref.getEmailAddress());
					response.setMobileNumber(ref.getMobileNo());
					response.setCountryName(ref.getCountryName());
					response.setCompanyName(ref.getCompanyName());
					response.setInsertedDate(ref.getInsertedDate());
					response.setAccountStatus(ref.getStatus());
					response.setReferId(ref.getId());
					response.setIsActPassed(!cust.getNimaiMLoginList().isEmpty()?cust.getNimaiMLoginList()
							.get(0).getIsActPassed():"");
					response.setLeadStatus(cust.getLeadStatus());
					response.setLeadRemark(cust.getLeadComment());
					return response;
				}
				return null;
			}).toList();
		} else if ( request.getTxtStatus().equalsIgnoreCase("rejected")) {
			//referDetails=referRepository.getRxilFieoReferrerRejectedDetails(request.getUserId(), pageable);
			
			if(request.getRole().equalsIgnoreCase("Management"))
        	{
            	if(request.getUserId().equalsIgnoreCase(saUser))
            		referDetails=referSaLeadRepo.getRxilFieoReferrerRejectedDetailsRefSaLeadSA(request.getUserId(), pageable);
	        	else
	        		referDetails=referSaLeadRepo.getRxilFieoReferrerRejectedDetailsRefSaLeadSA(request.getUserId(), pageable);
        	}
			else
			{
				referDetails=referSaLeadRepo.getRxilFieoReferrerRejectedDetailsRefSaLead(request.getUserId(), pageable,loginUser,Status);			
			}
			responses = referDetails.map(ref -> {
				NimaiMCustomer cust = customerRepository.existsByEmailId(ref.getEmailAddress());
				String customer = customerRepository.findKycByEmailAddress(ref.getEmailAddress().toLowerCase());
				if (customer != null && customer.equalsIgnoreCase(request.getTxtStatus())) {
					CustomerResponse response = new CustomerResponse();
					response.setFirstName(ref.getFirstName());
					response.setLastName(ref.getLastName());
					response.setEmailAddress(ref.getEmailAddress());
					response.setMobileNumber(ref.getMobileNo());
					response.setCountryName(ref.getCountryName());
					response.setCompanyName(ref.getCompanyName());
					response.setInsertedDate(ref.getInsertedDate());
					response.setAccountStatus(ref.getStatus());
					response.setReferId(ref.getId());
					response.setIsActPassed(!cust.getNimaiMLoginList().isEmpty()?cust.getNimaiMLoginList()
							.get(0).getIsActPassed():"");
					response.setLeadStatus(cust.getLeadStatus());
					response.setLeadRemark(cust.getLeadComment());
					return response;
				}
				return null;
			}).toList();
		} else if (request.getTxtStatus().equalsIgnoreCase("pending")) {
			//referDetails=referRepository.getRxilFieoReferrerPendingDetails(request.getUserId(), pageable);
			
			if(request.getRole().equalsIgnoreCase("Management"))
        	{
            	if(request.getUserId().equalsIgnoreCase(saUser))
            		referDetails=referSaLeadRepo.getRxilFieoReferrerPendingDetailsRefSaLeadSA(request.getUserId(), pageable);
	        	else
	        		referDetails=referSaLeadRepo.getRxilFieoReferrerPendingDetailsRefSaLeadSA(request.getUserId(), pageable);
        	}
			else
			{
				referDetails=referSaLeadRepo.getRxilFieoReferrerPendingDetailsRefSaLead(request.getUserId(), pageable,loginUser,Status);			
			}
				responses = referDetails.map(ref -> {
					NimaiMCustomer cust = customerRepository.existsByEmailId(ref.getEmailAddress());
				String customer = customerRepository.findKycByEmailAddress(ref.getEmailAddress().toLowerCase());
				if (customer == null || customer.equalsIgnoreCase(request.getTxtStatus())) {
					CustomerResponse response = new CustomerResponse();
					response.setFirstName(ref.getFirstName());
					response.setLastName(ref.getLastName());
					response.setEmailAddress(ref.getEmailAddress());
					response.setMobileNumber(ref.getMobileNo());
					response.setCountryName(ref.getCountryName());
					response.setCompanyName(ref.getCompanyName());
					response.setInsertedDate(ref.getInsertedDate());
					response.setAccountStatus(ref.getStatus());
					response.setReferId(ref.getId());
					response.setIsActPassed(!cust.getNimaiMLoginList().isEmpty()?cust.getNimaiMLoginList()
							.get(0).getIsActPassed():"");
					response.setLeadStatus(cust.getLeadStatus());
					response.setLeadRemark(cust.getLeadComment());
					return response;
				}
				return null;
			}).toList();
		}
		return new PagedResponse<>(responses, referDetails.getNumber(), referDetails.getSize(),
				referDetails.getTotalElements(), referDetails.getTotalPages(), referDetails.isLast());
		}
		
	}


//	
//	public PagedResponse<?> getAllReferDetails(SearchRequest request) {
//		List<CustomerResponse> responses = null;
//	
//		
//		Page<NimaiMRefer> referDetails = null;
//		Page<ReferralLeads> referralLeads;
//		
//		if(request.getUserId().equalsIgnoreCase("RE54153"))
//		{
//			request.setSortBy("INSERTED_DATE");
//			Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
//					request.getDirection().equalsIgnoreCase("desc") ? Sort.by(request.getSortBy()).descending()
//							: Sort.by(request.getSortBy()).ascending());
//			System.out.println("===== In FIEO/RXIL referrer =====");
//			referralLeads=referralRepo.getRxilFIEOReferrerDetails("RE54153", pageable);
//			if (request.getTxtStatus().equalsIgnoreCase("all")) {
//				responses = referralLeads.map(ref -> {
//					CustomerResponse response = new CustomerResponse();
//					response.setFirstName(ref.getFirstName());
//					response.setLastName(ref.getLastName());
//					response.setEmailAddress(ref.getEmailId());
//					response.setMobileNumber(ref.getMobileNo());
//					response.setCountryName(ref.getCountry());
//					response.setCompanyName(ref.getOrgName());
//					response.setInsertedDate(ref.getInsertedDate());
//					response.setAccountStatus("");
//					response.setReferId(0);
//					return response;
//				}).getContent();
//			}
//			return new PagedResponse<>(responses, referralLeads.getNumber(), referralLeads.getSize(),
//					referralLeads.getTotalElements(), referralLeads.getTotalPages(), referralLeads.isLast());
//		}
//		else
//		{
//			request.setSortBy("INSERTED_DATE");
//			Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
//					request.getDirection().equalsIgnoreCase("desc") ? Sort.by(request.getSortBy()).descending()
//							: Sort.by(request.getSortBy()).ascending());
//			System.out.println("===== In other referrer =====");
//		//referDetails = referRepository.findAll(referSpecification.getFilter(request), pageable);
//			referDetails = referRepository.findAllById(request.getUserId(),pageable);
//		if (request.getTxtStatus().equalsIgnoreCase("all")) {
//			responses = referDetails.map(ref -> {
//				CustomerResponse response = new CustomerResponse();
//				response.setFirstName(ref.getFirstName());
//				response.setLastName(ref.getLastName());
//				response.setEmailAddress(ref.getEmailAddress());
//				response.setMobileNumber(ref.getMobileNo());
//				response.setCountryName(ref.getCountryName());
//				response.setCompanyName(ref.getCompanyName());
//				response.setInsertedDate(ref.getInsertedDate());
//				response.setAccountStatus(ref.getStatus());
//				response.setReferId(ref.getId());
//				return response;
//			}).getContent();
//		} else if (request.getTxtStatus().equalsIgnoreCase("approved")
//				|| request.getTxtStatus().equalsIgnoreCase("rejected")) {
//			responses = referDetails.map(ref -> {
//				String customer = customerRepository.findKycByEmailAddress(ref.getEmailAddress().toLowerCase());
//				if (customer != null && customer.equalsIgnoreCase(request.getTxtStatus())) {
//					CustomerResponse response = new CustomerResponse();
//					response.setFirstName(ref.getFirstName());
//					response.setLastName(ref.getLastName());
//					response.setEmailAddress(ref.getEmailAddress());
//					response.setMobileNumber(ref.getMobileNo());
//					response.setCountryName(ref.getCountryName());
//					response.setCompanyName(ref.getCompanyName());
//					response.setInsertedDate(ref.getInsertedDate());
//					response.setAccountStatus(ref.getStatus());
//					response.setReferId(ref.getId());
//					return response;
//				}
//				return null;
//			}).toList();
//		} else if (request.getTxtStatus().equalsIgnoreCase("pending")) {
//			responses = referDetails.map(ref -> {
//				String customer = customerRepository.findKycByEmailAddress(ref.getEmailAddress().toLowerCase());
//				if (customer == null || customer.equalsIgnoreCase(request.getTxtStatus())) {
//					CustomerResponse response = new CustomerResponse();
//					response.setFirstName(ref.getFirstName());
//					response.setLastName(ref.getLastName());
//					response.setEmailAddress(ref.getEmailAddress());
//					response.setMobileNumber(ref.getMobileNo());
//					response.setCountryName(ref.getCountryName());
//					response.setCompanyName(ref.getCompanyName());
//					response.setInsertedDate(ref.getInsertedDate());
//					response.setAccountStatus(ref.getStatus());
//					response.setReferId(ref.getId());
//					return response;
//				}
//				return null;
//			}).toList();
//		}
//		return new PagedResponse<>(responses, referDetails.getNumber(), referDetails.getSize(),
//				referDetails.getTotalElements(), referDetails.getTotalPages(), referDetails.isLast());
//		}
//		
//	}
//

	@Override
	public ResponseEntity<CustomerResponse> getReferrerById(Integer id) {
		NimaiMRefer refer = referRepository.findByRefId(id);
		
if(refer==null || refer.getUserid().getUserid().equalsIgnoreCase(rxilUser)) {
	ReferralLeads leads=referralRepo.getOne(id);
	NimaiMCustomer customer = customerRepository.findCustomersByLeadId(leads.getLeadId());
	if(leads!=null) {
		CustomerResponse response = new CustomerResponse();
		response.setFirstName(customer.getFirstName());
		response.setLastName(customer.getLastName());
		response.setEmailAddress(customer.getEmailAddress());
		response.setMobileNumber(customer.getMobileNumber());
		response.setCompanyName(customer.getCompanyName());
		response.setCountryName(customer.getCountryName());
		response.setInsertedDate(customer.getInsertedDate());
	
	if (customer != null) {
		response.setUserid(customer.getUserid());
		response.setSignUpDate(customer.getInsertedDate());
		response.setAccountStatus(customer.getAccountStatus());
		response.setSubscriberType(customer.getSubscriberType());
		response.setLandline(customer.getLandline());
		for (NimaiSubscriptionDetails custSubDetails:customer.getNimaiSubscriptionDetailsList()) {
			if(Objects.equals(custSubDetails.getStatus(), "ACTIVE")){
				response.setSubscriptionValidity(custSubDetails.getSubscriptionValidity());
				response.setPlanName(custSubDetails.getSubscriptionName());
				response.setPlanAmount(Objects.equals(custSubDetails.getSubscriptionName(), "POSTPAID_PLAN") ?
						this.postPaidRepo.findTotalPaymentBySubscriptionId(custSubDetails.getSplSerialNumber()):
						custSubDetails.getGrandAmount().intValue());
				response.setUserWiseTotalEarning(String.valueOf(0));
				String loginId = Utility.getUserId();
				System.out.println("userid---> "+customer.getUserid());
				Double value = this.customerRepository.getEarningForSingleRefer(customer.getUserid());
//					List<Tuple> custList = this.customerRepository.getEarningForSingleRefer(customer.getUserid(),loginId);
//					Double vasAmount = this.subscriptionVasRepo.getActiveVasAmount(customer.getUserid());
//					Double value = Utility.getReferEarnings(custList,"userid",vasAmount);
				System.out.println("Earnings for referrer as per rule rxilUser-----> "+value);
				if (value == null || value.equals(null)) {
					value = Double.valueOf(0.0D);
				} else {
					value = value;
				}
				Float referEarning = Float.valueOf(this.systemConfig.earningPercentage());
				Float actualREarning = referEarning / 100.0F;
				Float earning = Float.parseFloat((new DecimalFormat("##.##")).format(value.doubleValue() * actualREarning.floatValue()));
				if (earning.equals(null)) {
					response.setUserWiseTotalEarning(String.valueOf(0));
				} else {
					response.setUserWiseTotalEarning(String.valueOf(earning));
				}
			}
		}
		if (customer.getRmId() != null) {
			NimaiMEmployee emp = employeeRepository.findByEmpCode(customer.getRmId());
			response.setRmFirstName(emp.getEmpName());
			response.setRmLastName(emp.getEmpLastName());
			response.setRmDesignation(emp.getDesignation());
		}
	}
	return new ResponseEntity<CustomerResponse>(response, HttpStatus.OK);
	}
	
}else {
	NimaiMCustomer customer = customerRepository.findByEmailAddress(refer.getEmailAddress().toLowerCase());

	if (refer != null) {
		CustomerResponse response = new CustomerResponse();
		response.setFirstName(refer.getFirstName());
		response.setLastName(refer.getLastName());
		response.setEmailAddress(refer.getEmailAddress());
		response.setMobileNumber(refer.getMobileNo());
		response.setCompanyName(refer.getCompanyName());
		response.setCountryName(refer.getCountryName());
		response.setInsertedDate(refer.getInsertedDate());

		if (customer != null) {
			response.setUserid(customer.getUserid());
			response.setSignUpDate(customer.getInsertedDate());
			response.setAccountStatus(customer.getAccountStatus());
			response.setSubscriberType(customer.getSubscriberType());
			response.setLandline(customer.getLandline());
			for (NimaiSubscriptionDetails custSubDetails:customer.getNimaiSubscriptionDetailsList()) {
				if(Objects.equals(custSubDetails.getStatus(), "ACTIVE")){
					response.setSubscriptionValidity(custSubDetails.getSubscriptionValidity());
					response.setPlanName(custSubDetails.getSubscriptionName());
					System.out.println(""+Utility.getUserId());
					if(Objects.equals(custSubDetails.getSubscriptionName(), "POSTPAID_PLAN")){
						System.out.println("custSubDetails.getSplSerialNumber()--------> "+custSubDetails.getSplSerialNumber());
						Double d = this.postPaidRepo
								.findTotalPaymentBySubscriptionId(custSubDetails.getSplSerialNumber());
						if(d ==null){
							d = 0.0D;
						}
						response.setPlanAmount(d);
					}else {
						System.out.println("custSubDetails.getGrandAmount()-----------> "+custSubDetails.getGrandAmount());
						response.setPlanAmount(custSubDetails.getGrandAmount());
					}
//					response.setPlanAmount(Objects.equals(custSubDetails.getSubscriptionName(), "POSTPAID_PLAN") ?
//							this.postPaidRepo.findTotalPaymentBySubscriptionId(custSubDetails.getSplSerialNumber()):
//							custSubDetails.getGrandAmount());
					response.setUserWiseTotalEarning(String.valueOf(0));
					String loginId = Utility.getUserId();
					System.out.println("userid---> "+customer.getUserid());
					Double value = this.customerRepository.getEarningForSingleRefer(customer.getUserid());
//						List<Tuple> custList = this.customerRepository.getEarningForSingleRefer(customer.getUserid(),loginId);
//						Double vasAmount = this.subscriptionVasRepo.getActiveVasAmount(customer.getUserid());
//						Double value = Utility.getReferEarnings(custList,"userid",vasAmount);
					System.out.println("Earnings for referrer as per rule-----> "+value);
					if (value == null || value.equals(null)) {
						value = Double.valueOf(0.0D);
					} else {
						value = value;
					}
					Float referEarning = Float.valueOf(this.systemConfig.earningPercentage());
					Float actualREarning = referEarning / 100.0F;
					Float earning = Float.parseFloat((new DecimalFormat("##.##")).format(value.doubleValue() * actualREarning.floatValue()));
					System.out.println("Earnings for referrer as per rule--------> "+earning);
					if (earning.equals(null)) {
						response.setUserWiseTotalEarning(String.valueOf(0));
					} else {
						response.setUserWiseTotalEarning(String.valueOf(earning));
					}
				}
			}
			if (customer.getRmId() != null) {
				NimaiMEmployee emp = employeeRepository.findByEmpCode(customer.getRmId());
				response.setRmFirstName(emp.getEmpName());
				response.setRmLastName(emp.getEmpLastName());
				response.setRmDesignation(emp.getDesignation());
			}
		}
		return new ResponseEntity<CustomerResponse>(response, HttpStatus.OK);
	}
}
		return null;
	}



	@Override
	public List<String> bankNameSearch(String bankName) {
//			, String data) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = Stream.of(val.split(",")).collect(Collectors.toList());
			// return customerRepository.bankNameDataSearchByCountry(bankName.toLowerCase(),
			// data, list);
			return customerRepository.bankNameDataSearchByCountry(bankName.toLowerCase(), list);
		} else {
			return customerRepository.bankNameDataSearch(bankName.toLowerCase());
			// return customerRepository.bankNameDataSearch(bankName.toLowerCase(), data);
		}
	}

}
