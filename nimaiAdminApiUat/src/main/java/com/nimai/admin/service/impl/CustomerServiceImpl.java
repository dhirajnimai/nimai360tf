package com.nimai.admin.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nimai.admin.model.NimaiEmailScheduler;
import com.nimai.admin.model.NimaiFKyc;
import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMEmployee;
import com.nimai.admin.model.NimaiMLogin;
import com.nimai.admin.model.NimaiMpUserRole;
import com.nimai.admin.model.NimaiSubscriptionDetails;
import com.nimai.admin.payload.ApiResponse;
import com.nimai.admin.payload.AssignRmResponse;
import com.nimai.admin.payload.AssignRmResponseBean;
import com.nimai.admin.payload.CustomerResponse;
import com.nimai.admin.payload.PagedResponse;
import com.nimai.admin.payload.RmRequestBody;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.repository.CustomerRepository;
import com.nimai.admin.repository.EmployeeRepository;
import com.nimai.admin.repository.KycRepository;
import com.nimai.admin.repository.NimaiEmailSchedulerRepository;
import com.nimai.admin.repository.TransactionsRepository;
import com.nimai.admin.repository.UserRepository;
import com.nimai.admin.repository.UserRoleRepository;
import com.nimai.admin.service.CustomerService;
import com.nimai.admin.specification.CustomerSearchSpecification;
import com.nimai.admin.specification.CustomerSpecification;
import com.nimai.admin.util.GenericResponse;
import com.nimai.admin.util.Utility;

@Service
public class CustomerServiceImpl implements CustomerService {

	
	
	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	CustomerSpecification customerSpecification;

	@Autowired
	CustomerSearchSpecification customerSearchSpecification;

	@Autowired
	TransactionsRepository transactionsRepository;

	@Autowired
	UserRoleRepository userRoleRepository;

	@Autowired
	UserRepository loginRepository;

	@Autowired
	EmployeeRepository empRepository;


	  @Autowired
	  CustomerRepository repo;
	
	
	@Autowired
	NimaiEmailSchedulerRepository schRepo;
	
	@Autowired
	KycRepository kycRepo;
	
	
	
	

	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

	@Override
	public PagedResponse<?> getAllCustomer(Map<String, String> data) {

		Pageable pageable = PageRequest.of(Integer.parseInt(data.get("page")), Integer.parseInt(data.get("size")),
				data.get("direction").equalsIgnoreCase("desc") ? Sort.by(data.get("sortBy")).descending()
						: Sort.by(data.get("sortBy")).ascending());
		// --Bashir changes
		Page<NimaiMCustomer> customerList;
		String countryNames = Utility.getUserCountry();
		if (countryNames != null && countryNames.equalsIgnoreCase("all")) {

		} else if (countryNames != null && !countryNames.equalsIgnoreCase("all")) {
			data.put("countryNames", countryNames);
		} else if (countryNames == null) {
			customerList = null;
		}
		if (data.get("status") != null && data.get("status").equalsIgnoreCase("Pending"))
			customerList = customerRepository.findAll(customerSpecification.getGrantRmFilter(data), pageable);
		else
			customerList = customerRepository.findAll(customerSpecification.getFilter(data), pageable);

		List<CustomerResponse> responses = customerList.map(cust -> {
			CustomerResponse response = new CustomerResponse();
			response.setSubscriberType(cust.getSubscriberType().toUpperCase());
			response.setUserid(cust.getUserid());
			response.setFirstName(cust.getFirstName());
			response.setLastName(cust.getLastName());
			response.setEmailAddress(cust.getEmailAddress());
			response.setMobileNumber(cust.getMobileNumber());
			response.setCountryName(cust.getCountryName());
			response.setLandline(cust.getLandline());
			response.setDesignation(cust.getDesignation());
			response.setCompanyName(cust.getCompanyName());
			response.setBusinessType(cust.getBusinessType());
			response.setBankName(cust.getBankName());
			response.setBranchName(cust.getBranchName());
			response.setSwiftCode(cust.getSwiftCode());
			response.setRegisteredCountry(cust.getRegisteredCountry());
			response.setTelephone(cust.getTelephone());
			response.setMinValueofLc(cust.getMinValueofLc());
			response.setRegistrationType(cust.getRegistrationType());
			response.setProvincename(cust.getProvincename());
			response.setAddress1(cust.getAddress1());
			response.setAddress2(cust.getAddress2());
			response.setAddress3(cust.getAddress3());
			response.setCity(cust.getCity());
			response.setPincode(cust.getPincode());
			response.setInsertedDate(cust.getInsertedDate());
			if (cust.getRmStatus() != null && cust.getRmStatus().equalsIgnoreCase("Pending")) {
				NimaiMEmployee employee = employeeRepository.findByEmpCode(cust.getRmId());
				if(employee==null) {
					response.setRelationshipManager("");
					response.setRmFirstName("");
					response.setRmApprovedBy("");
				}else {
					response.setRelationshipManager(cust.getRmId());
					if(employee.getEmpName()==null || employee.getEmpLastName()==null) {
						response.setRmFirstName("");
					}else {
						response.setRmFirstName(employee.getEmpName() + " " + employee.getEmpLastName());
					}
					
					response.setRmApprovedBy(cust.getApprovedBy());
				}
			
			}
			return response;
		}).getContent();

		return new PagedResponse<>(responses, customerList.getNumber(), customerList.getSize(),
				customerList.getTotalElements(), customerList.getTotalPages(), customerList.isLast());
	}

	@Override
	public ResponseEntity<?> updateRelationshipManager(Map<String, String> data) {
		NimaiMCustomer customer = customerRepository.getOne(data.get("userId"));
		if (customer.getApprovedBy() != null && customer.getApprovedBy().equalsIgnoreCase(Utility.getUserId())) {
			return new ResponseEntity<>(new ApiResponse(true, "You dont have the authority for this operation!!!"),
					HttpStatus.OK);
		}
		System.out.println(data);
		logger.info("data");
		NimaiMEmployee rmDetails = empRepository.findByEmpCode(data.get("rm"));
		NimaiEmailScheduler schData = new NimaiEmailScheduler();
		customer.setIsRmassigned(true);
		customer.setRmId(data.get("rm"));
		customer.setRmStatus(data.get("status"));
		customer.setApprovedBy(Utility.getUserId());
		customer.setModifiedDate(Utility.getSysDate());
		customerRepository.save(customer);
		if(customer.getRmStatus().equalsIgnoreCase("Active")) {
			/*
			 * changes done by dhiraj to send the notification to rm
			 */
			schData.setrMName(rmDetails.getEmpName());
			schData.setrMemailId(rmDetails.getEmpEmail());
			schData.setUserid(data.get("userId"));
			schData.setsPLanCountry(customer.getCountryName());
			
			if (data.get("userId").substring(0, 2).equalsIgnoreCase("CU")) {
				schData.setEvent("ASSIGN_NOTIFICATION_TO_RM");
				schData.setUserName(customer.getCompanyName());
			} else if (data.get("userId").substring(0, 2).equalsIgnoreCase("RE")) {
				schData.setEvent("ASSIGN_NOTIFICATION_TO_RM_RE");
				schData.setUserName(customer.getCompanyName());
			} else if (data.get("userId").substring(0, 2).equalsIgnoreCase("BC")) {
				schData.setEvent("ASSIGN_NOTIFICATION_TO_RM_BC");
				schData.setUserName(customer.getCompanyName());
			} else if (data.get("userId").substring(0, 2).equalsIgnoreCase("BA")) {
				schData.setEvent("ASSIGN_NOTIFICATION_TO_RM_BA");
				schData.setUserName(customer.getBankName());
			}

			schData.setEmailStatus("Pending");
			schData.setInsertedDate(Utility.getSysDate());
			schRepo.save(schData);
		}


		return new ResponseEntity<>(
				new ApiResponse(true,
						"Relationship Manager assigned to " + customer.getFirstName() + " " + customer.getLastName()),
				HttpStatus.CREATED);
	}

	@Override
	public Map<String, Map<String, List<AssignRmResponse>>> getRmList() {

		Map<String, Map<String, List<AssignRmResponse>>> value2 = new HashMap<>();
		/**
		 * Old Logic
		 */
//		List<NimaiMEmployee> empList = employeeRepository.findAll();
//		System.out.println(">>> empList >>> " + empList.size());
//		Map<String, List<NimaiMEmployee>> result = empList.stream()
//				.filter(e -> e.getStatus().equalsIgnoreCase("Active")
//						&& e.getNimaiMpUserRoleList().get(0).getRoleId().getRoleShortName().contains("RM"))
//				.collect(Collectors
//						.groupingBy(emp -> emp.getNimaiMpUserRoleList().get(0).getRoleId().getRoleShortName()));
//
//		for (Map.Entry<String, List<NimaiMEmployee>> entry : result.entrySet()) {
//			Map<String, List<AssignRmResponse>> value = new HashMap<String, List<AssignRmResponse>>();
//
//			System.out.println(entry.getKey() + ":" + entry.getValue());
//			for (NimaiMEmployee item : entry.getValue()) {
//				String[] stringArray = item.getCountry().split(",");
//				for (String val : stringArray) {
//					value.computeIfAbsent(val, k -> new ArrayList<AssignRmResponse>()).add(
//							new AssignRmResponse(item.getEmpCode(), item.getEmpName() + " " + item.getEmpLastName()));
//				}
//			}
//			value2.put(entry.getKey().toUpperCase(), value);
//		}

		/**
		 * New Logic
		 */
		List<NimaiMpUserRole> userRole = userRoleRepository.findAll();
		
		Map<String, List<NimaiMpUserRole>> result = userRole.stream()
				.filter(e -> e.getStatus().equalsIgnoreCase("Active") && 
						e.getRoleId().getRoleShortName().contains("RM")
						&& e.getEmpCode().getStatus().equalsIgnoreCase("Active"))
				.collect(Collectors.groupingBy(emp -> emp.getRoleId().getRoleShortName()));

		for (Map.Entry<String, List<NimaiMpUserRole>> entry : result.entrySet()) {
			Map<String, List<AssignRmResponse>> value = new HashMap<String, List<AssignRmResponse>>();
			for (NimaiMpUserRole item : entry.getValue()) {
				String[] stringArray = item.getEmpCode().getCountry().split(",");
				
				
				for (String val : stringArray) {
					value.computeIfAbsent(val, k -> new ArrayList<AssignRmResponse>())
							.add(new AssignRmResponse(item.getEmpCode().getEmpCode(),
									item.getEmpCode().getEmpName() + " " + item.getEmpCode().getEmpLastName()));
				}
			}
			
			
			value2.put(entry.getKey().toUpperCase(), value);
		}

		return value2;
	}

	public String collectInPlanName(List<NimaiSubscriptionDetails> subscriptionList) {
		return subscriptionList.stream().filter(plan -> plan.getStatus().equalsIgnoreCase("INACTIVE")).
				sorted(Comparator.comparingInt(NimaiSubscriptionDetails::getSplSerialNumber).reversed()).findFirst()
				.map(NimaiSubscriptionDetails::getSubscriptionName).get();

	}
	
	@Override
	  public PagedResponse<?> getSearchCustomer(SearchRequest request) {
		PageRequest pageRequest;
		System.out.println(" Search Request " + request);
		Pageable pageable = null;
		request.setSortBy("inserted_date");
		if ((request.getUserId() != null && request.getTxtStatus() == null) || (request
				.getMobileNo() != null && request.getTxtStatus() == null)) {
			request.setSize(1);
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() :
							Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if ((request.getUserId() != null && request.getTxtStatus() == null) || (request
				.getEmailId() != null && request.getTxtStatus() == null)) {
			request.setSize(request.getSize());
			System.out.println("Search by emailid :" + request.getSize());
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() :
							Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if (request.getTxtStatus() == null) {
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ?
							Sort.by(new String[] { request.getSortBy() }).descending() :
							Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {
			request.setSortBy("nfk.INSERTED_DATE");
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ?
							Sort.by(new String[] { request.getSortBy() }).descending() :
							Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
			System.out.println("===============Inside PaymentPendingUser=================");
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ?
							Sort.by(new String[] { request.getSortBy() }).descending() :
							Sort.by(new String[] { request.getSortBy() }).ascending());
			System.out.println("===============Inside PaymentPendingUser=================" + request.getSortBy());
		} else {
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() :
							Sort.by(new String[] { request.getSortBy() }).ascending());
		}
		Page<NimaiMCustomer> customerList = null;
		NimaiMCustomer custDetails = new NimaiMCustomer();
		String countryNames = Utility.getUserCountry();
		System.out.println("countryNames: " + countryNames);
		if (countryNames != null && countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
			countryNames = "";
			List<String> countryList = this.repo.getCountryList();
			for (String country : countryList)
				countryNames = countryNames + country + ",";
			System.out.println("Country List: " + countryNames);
			request.setCountryNames(countryNames);
			System.out.println("1: " + countryNames);
		} else if (countryNames != null && !countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
			request.setCountryNames(countryNames);
			System.out.println("2: " + countryNames);
		} else if (countryNames != null && request.getCountry() != null) {
			request.setCountryNames(request.getCountry());
			System.out.println("3: " + countryNames);
		} else if (countryNames == null && request.getCountry() == null) {
			System.out.println("4: " + countryNames);
		}
		List<String> value = Stream.of(request.getCountryNames().split(",", -1)).collect(Collectors.toList());
		System.out.println("Values BankService: " + value);
		System.out.println("BankType: " + request.getBankType());
		System.out.println("SubscriberType: " + request.getSubscriberType());
		if (request.getRole() != null && request.getRole().equalsIgnoreCase("Customer RM")) {
			request.setLoginUserId(Utility.getUserId());
			request.setRmStatus("Active");
		}
		List<CustomerResponse> responses = new ArrayList<>();
		if (request.getRole().equalsIgnoreCase("Customer RM")) {
			String rmId = Utility.getUserId();
			System.out.println("===========RM userId" + rmId);
			customerList = retiriveListOnRMId(rmId, request, value, (Pageable)pageRequest);
			logger.info("************=============insdie request.getUserId()============***********cu RM" + request
					.getUserId());
			for(NimaiMCustomer cu:customerList) {
				logger.info("************=============insdie request.getUserId()============***********cu RM" + cu
						.getUserid());
			}
		} else if (request.getUserId() != null && request.getTxtStatus() == null) {
			logger.info("************=============Searching Customer By userID no============***********12" + request
					.getUserId());
			customerList = this.customerRepository.getDetailsByUserId(request.getUserId(), value, (Pageable)pageRequest);
		} else if (request.getMobileNo() != null && request.getTxtStatus() == null) {
			System.out.println("========= Searching Customer By Mobile no =========");
			customerList = this.customerRepository.getDetailsByMobileNumber(request.getMobileNo(), value, (Pageable)pageRequest);
		} else if (request.getEmailId() != null && request.getTxtStatus() == null) {
			System.out.println("========= Searching Customer By Email ID =========");
			customerList = this.customerRepository.getDetailsByEmailId(request.getEmailId(), value, (Pageable)pageRequest);
		} else if (request.getCountry() != null && request.getTxtStatus() == null) {
			System.out.println("========= Searching Customer By Country =========");
			customerList = this.customerRepository.getDetailsByCountry(request.getCountry(), value, (Pageable)pageRequest);
		} else if (request.getCompanyName() != null && request.getTxtStatus() == null) {
			System.out.println("========= Searching Customer By Company name =========");
			customerList = this.customerRepository.getDetailsByCompanyName(request.getCompanyName(), value, (Pageable)pageRequest);
		} else if (request.getTxtStatus() == null || request.getTxtStatus().equalsIgnoreCase("all")) {
			System.out.println("========= Searching All Customer =========");
			customerList = this.customerRepository.getAllCustomerKYC(value, (Pageable)pageRequest);
		} else if (request.getTxtStatus().equalsIgnoreCase("Pending") && (request
				.getBankType() == null || request.getBankType().isEmpty()) && (request
				.getSubscriberType() == null || request.getSubscriberType().isEmpty())) {
			System.out.println("========= Searching Pending Customer KYC =========");
			customerList = this.customerRepository.getPendingCustomerKYC(value, (Pageable)pageRequest);
		} else if (request.getTxtStatus().equalsIgnoreCase("Approved")) {
			System.out.println("========= Searching Approved Customer KYC =========");
			customerList = this.customerRepository.getApprovedCustomerKYC(value, (Pageable)pageRequest);
		} else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {
			System.out.println("========= Searching Rejected Customer KYC =========");
			customerList = this.customerRepository.getRejectedCustomerKYC(value, (Pageable)pageRequest);
		} else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
			if (request.getSubscriberType() == null && request.getBankType() == null) {
				customerList = this.customerRepository.getNotUploadCustomerKYC(value, (Pageable)pageRequest);
			} else if (request.getSubscriberType().equals("CUSTOMER") && request.getBankType() == null) {
				customerList = this.customerRepository.getNotUploadForCU(value, (Pageable)pageRequest);
			} else if (request.getSubscriberType().equals("BANK") && request
					.getBankType().equals("CUSTOMER")) {
				System.out.println("inside khkj");
				customerList = this.customerRepository.getNotUploadForBC(value, (Pageable)pageRequest);
			}
			System.out.println("========= Searching Not Uploaded Customer KYC =========");
		} else if ((request.getBankType() == null || request.getBankType().isEmpty()) && request.getSubscriberType().equalsIgnoreCase("Customer")) {
			if (request.getTxtStatus().equalsIgnoreCase("PaymentPending")) {
				System.out.println("========= Searching PaymentPending Customer =========");
				customerList = this.customerRepository.getCuPendingCustomerPayment(value, (Pageable)pageRequest);
			} else if (request.getTxtStatus().equalsIgnoreCase("subexpiry")) {
				customerList = this.customerRepository.getSubscriptionExpiryCU(value, (Pageable)pageRequest);
				System.out.println("************=============Subscription Expiry in 30 days============***********");
			} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
				System.out.println("inside paymetpending user customer " + request.getSortBy());
				customerList = this.customerRepository.getPaymentPendingUserCU(value, (Pageable)pageRequest);
				System.out.println("************=============PaymentPaymentUser getPaymentPendingUserCU============***********" + customerList

						.getSize() + "and :" + customerList.getTotalPages() + "and :" + customerList.getTotalElements() + "country list count:" + value.size());
				for (NimaiMCustomer cust : customerList)
					System.out.println("************=============PaymentPaymentUser getPaymentPendingUserCU============***********" + cust

							.getUserid());
			} else {
				System.out.println("========= Searching Customer =========");
				customerList = this.customerRepository.getCuPendingCustomerKYC(value, (Pageable)pageRequest);
			}
		} else if (request.getBankType().equalsIgnoreCase("Customer") && request.getSubscriberType().equalsIgnoreCase("Bank")) {
			if (request.getTxtStatus().equalsIgnoreCase("PaymentPending")) {
				System.out.println("========= Searching PaymentPending Bank As Customer =========");
				customerList = this.customerRepository.getBCuPendingCustomerPayment(value, (Pageable)pageRequest);
			} else if (request.getTxtStatus().equalsIgnoreCase("subexpiry")) {
				customerList = this.customerRepository.getSubscriptionExpiryBC(value, (Pageable)pageRequest);
				System.out.println("************=============Subscription Expiry in 30 days============***********");
			} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
				System.out.println("inside paymetpending user getPaymentPendingUserBC " + request.getSortBy());
				System.out.println("valur----------------> "+value);
				customerList = this.customerRepository.getPaymentPendingUserBC(value, (Pageable)pageRequest);
				for (NimaiMCustomer cust : customerList)
					System.out.println("************=============PaymentPaymentUser getPaymentPendingUserBC============***********" + cust

							.getUserid());
				System.out.println("************=============PaymentPaymentUser getPaymentPendingUserBC============***********" + request

						.getUserId());
			} else {
				System.out.println("========= Searching Bank As Customer =========");
				customerList = this.customerRepository.getBCuPendingCustomerKYC(value, (Pageable)pageRequest);
			}
		} else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
			if (request.getSubscriberType() == null && request.getBankType() == null) {
				customerList = this.customerRepository.getNotUploadCustomerKYC(value, (Pageable)pageRequest);
			} else if (request.getSubscriberType().equals("CUSTOMER") && request.getBankType() == null) {
				customerList = this.customerRepository.getNotUploadForCU(value, (Pageable)pageRequest);
			} else if (request.getSubscriberType().equals("BANK") && request.getBankType().equals("CUSTOMER")) {
				System.out.println("inside khkj");
				customerList = this.customerRepository.getNotUploadForBC(value, (Pageable)pageRequest);
			}
		}
		System.out.println("ID");
		responses = customerList.map(cust -> {
			CustomerResponse response = new CustomerResponse();
			System.out.println("userId pending paymebts" + cust.getUserid());
			System.out.println("==========================Payment pending customer user request" + cust.toString());
			response.setUserid(cust.getUserid());
			response.setFirstName(cust.getFirstName());
			response.setLastName(cust.getLastName());
			response.setMobileNumber(cust.getMobileNumber());
			response.setEmailAddress(cust.getEmailAddress());
			response.setCountryName(cust.getCountryName());
			response.setCompanyName(cust.getCompanyName());
			if (request.getTxtStatus() == null) {
				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0) ? collectPlanName(cust.getNimaiSubscriptionDetailsList()) : "No Active Plan");
				if (response.getPlanOfPayments().isEmpty() || response.getPlanOfPayments() == null)
					response.setPlanOfPayments("Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
			} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
				if (cust.getNimaiSubscriptionDetailsList().size() != 0) {
					response.setPlanOfPayments("Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
				} else {
					response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0) ? collectPlanName(cust.getNimaiSubscriptionDetailsList()) : "No Active Plan");
				}
			} else {
				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0) ? collectPlanName(cust.getNimaiSubscriptionDetailsList()) : "No Active Plan");
				if (response.getPlanOfPayments().isEmpty() || response.getPlanOfPayments() == null)
					response.setPlanOfPayments("Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
			}
			response.setTotalTxn((cust.getNimaiMmTransactionList() != null) ? cust.getNimaiMmTransactionList().size() : 0L);
			String userId = response.getUserid();
			System.out.println("==============useriD" + userId);
			List<NimaiFKyc> kycdetails = this.kycRepo.findByUserid(cust);
			if (kycdetails.size() == 0) {
				response.setKyc("Not Uploaded");
			} else {
				response.setKyc(cust.getKycStatus());
			}
			response.setRegisteredCountry(cust.getRegisteredCountry());
			return response;
		}).getContent();
		return new PagedResponse(responses, customerList.getNumber(), customerList.getSize(), customerList
				.getTotalElements(), customerList.getTotalPages(), customerList.isLast());
	  }
	
	
	  public String collectPlanName(List<NimaiSubscriptionDetails> subscriptionList) {
		    return ((String)subscriptionList.stream().filter(plan -> plan.getStatus().equalsIgnoreCase("Active"))
		      .sorted(Comparator.comparingInt(NimaiSubscriptionDetails::getSplSerialNumber).reversed())
		      .map(NimaiSubscriptionDetails::getSubscriptionName).collect(Collectors.joining(" "))).toString();
		  }
	

	  private Page<NimaiMCustomer> retiriveListOnRMId(String rmId, SearchRequest request, List<String> value, Pageable pageable) {
	    Page<NimaiMCustomer> customerList = null;
	    if (request.getUserId() != null && request.getTxtStatus() == null) {
	      logger.info("************=============insdie request.getUserId()============***********1" + request
	          .getUserId());
	      customerList = this.customerRepository.getDetailsByUserIdRmId(rmId, request.getUserId(),value,  pageable);
	    } else if (request.getMobileNo() != null && request.getTxtStatus() == null) {
	    	logger.info("************=============insdie request.getUserId()============***********2" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getDetailsByMobileNumberRmId(request.getMobileNo(), rmId, value, pageable);
	    } else if (request.getEmailId() != null && request.getTxtStatus() == null) {
	    	logger.info("************=============insdie request.getUserId()============***********3" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getDetailsByEmailIdRmId(request.getEmailId(), rmId, value, pageable);
	    } else if (request.getCountry() != null && request.getTxtStatus() == null) {
	    	logger.info("************=============insdie request.getUserId()============***********4" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getDetailsByCountryRmId(request.getCountry(), rmId, value, pageable);
	    } else if (request.getCompanyName() != null && request.getTxtStatus() == null) {
	    	logger.info("************=============insdie request.getUserId()============***********5" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getDetailsByCompanyNameRmId(request.getCompanyName(), rmId, value, pageable);
	    } else if (request.getTxtStatus() == null || request.getTxtStatus().equalsIgnoreCase("all")) {
	    	logger.info("************=============insdie request.getUserId()============***********6" + request
	  	          .getUserId());
	      System.out.println(rmId);
	      System.out.println(pageable);
	      try {
	        customerList = this.customerRepository.getAllCustomerKYCRmid(rmId, value, pageable);
	      } catch (Exception e) {
	        e.printStackTrace();
	      } 
	      System.out.println(customerList.toString());
	    } else if (request.getTxtStatus().equalsIgnoreCase("Pending")) {
	    	logger.info("************=============insdie request.getUserId()============***********7" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getPendingCustomerKYCRmId(rmId, value, pageable);
	    } else if (request.getTxtStatus().equalsIgnoreCase("Approved")) {
	    	logger.info("************=============insdie request.getUserId()============***********8" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getApprovedCustomerKYCRmId(rmId, value, pageable);
	    } else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {
	    	logger.info("************=============insdie request.getUserId()============***********9" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getRejectedCustomerKYCRmId(rmId, value, pageable);
	    } else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
	    	logger.info("************=============insdie request.getUserId()============***********10" + request
	  	          .getUserId());
	      customerList = this.customerRepository.getNotUploadCustomerKYCRmId(rmId, value, pageable);
	    } 
	    return customerList;
	  }
	
	
	
	
	
	
	
	
	@Override
	public ResponseEntity<?> removeSubsidiary(Map<String, String> data) {
		logger.debug(" Remove User ID : " + data.get("userId"));
		try {

			NimaiMLogin login = loginRepository.findByUserId(data.get("userId"));
			login.setStatus(data.get("status"));
			login.setLastModifiedBy(data.get("approverName"));
			login.setLastModifiedDate(new Date());
			NimaiMCustomer customer = login.getUserid();
			customer.setAccountStatus(data.get("status"));
			customer.setAccountRemark(data.get("remark"));
			customer.setModifiedDate(new Date());
			login.setUserid(customer);
			loginRepository.save(login);
			return new ResponseEntity<>(
					new ApiResponse(true, "Removed subsidiary successfully ..." + customer.getUserid()),
					HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Error while updating kyc status - " + e.getMessage());
			return new ResponseEntity<>(new ApiResponse(true, "Error while updating kyc status."),
					HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> getRmList(RmRequestBody request) {
		GenericResponse genResponse=new GenericResponse();
		// TODO Auto-generated method stub
	String	country="%"+request.getCountryName()+"%";
	String cuSubsType="";
	String baSubsType="";
	
	if(request.getUserMode()==null) {
		if(request.getSubType().equalsIgnoreCase("Customer")
				|| request.getSubType().equalsIgnoreCase("REFERRER")) {
			request.setSubType("Customer Rm");
		}
		else {
			request.setSubType("Bank Rm");
		}
	}else {
		 cuSubsType="Customer Rm";
		 baSubsType="Bank Rm";
	}

	List<AssignRmResponseBean> responseList=new ArrayList<>();
	try {
		List<Tuple> employeeDetails;
		if(request.getUserMode()==null) {
		if(request.getSubType().equalsIgnoreCase("REFERRER")) {
			logger.info("Inside Referrer condition"+request.getSubType());
			 employeeDetails=employeeRepository.getRefEmployeRepoList(country);
		}else {
			logger.info("Inside Referrer condition"+request.getSubType());
			logger.info("Inside Referrer condition"+country);
			employeeDetails=employeeRepository.getEmployeRepoList(request.getSubType(),country);
		}
		}else {
			employeeDetails=employeeRepository.getEmployeRepoList(cuSubsType,baSubsType,country);
		}
		 
		for(Tuple res:employeeDetails) {
			AssignRmResponseBean response=new AssignRmResponseBean();
			response.setCode((String)res.get("emp_code"));
			response.setValue((String)res.get("emp_first_name")+" "+(String)res.get("emp_last_name"));
			responseList.add(response);	
		}
		genResponse.setList(responseList);
		return new ResponseEntity(genResponse, HttpStatus.OK);
	}catch(Exception e) {
		e.printStackTrace();
		e.getMessage();
		genResponse.setMessage("Data Not found");
		return new ResponseEntity(genResponse, HttpStatus.OK);
	}

		
	}

}
