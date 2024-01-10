package com.nimai.admin.service.impl;

import java.util.Arrays;


import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nimai.admin.model.BeneInterestedCountry;
import com.nimai.admin.model.GenericResponse;
import com.nimai.admin.model.InterestedCountry;
import com.nimai.admin.model.NimaiFBlkgoods;
import com.nimai.admin.model.NimaiFKyc;
import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMEmployee;
import com.nimai.admin.model.NimaiMLogin;
import com.nimai.admin.model.NimaiMOldCustDetails;
import com.nimai.admin.model.NimaiOfflineUserDetails;
import com.nimai.admin.model.NimaiSubscriptionDetails;
import com.nimai.admin.model.NimaiSubscriptionVas;
import com.nimai.admin.payload.AdditionalUserList;
import com.nimai.admin.payload.ApiResponse;
import com.nimai.admin.payload.BankDetailsResponse;
import com.nimai.admin.payload.BeneInterestedCountryBean;
import com.nimai.admin.payload.BlackListedGoodsBean;
import com.nimai.admin.payload.InterestedCountryBean;
import com.nimai.admin.payload.OffLineBankRequest;
import com.nimai.admin.payload.OfflineUpdateResponse;
import com.nimai.admin.payload.PagedResponse;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.payload.SorceDetailsReponseBean;
import com.nimai.admin.repository.BeneInterestedCountryRepository;
import com.nimai.admin.repository.BlackListedGoodsRepository;
import com.nimai.admin.repository.CustomerRepository;
import com.nimai.admin.repository.InterestedCountryRepository;
import com.nimai.admin.repository.KycRepository;
import com.nimai.admin.repository.NimaiKycRepository;
import com.nimai.admin.repository.NimaiMOldCustRepository;
import com.nimai.admin.repository.NimaiOfflineUserDetailsRepository;
import com.nimai.admin.repository.QuotationRepository;
import com.nimai.admin.repository.UserRepository;
import com.nimai.admin.service.OffLineBankService;
import com.nimai.admin.util.PasswordGenerator;
import com.nimai.admin.util.RegistrationId;
import com.nimai.admin.util.Utility;




@Service
public class OfflineBauServiceImpl implements OffLineBankService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OfflineBauServiceImpl.class);
	
	@Autowired
	CustomerRepository repo;

	
	@Autowired
	RegistrationId userid;
	
	@Autowired
	CustomerRepository customerRepository;
	
	@Autowired
	InterestedCountryRepository icr;

	@Autowired
	BeneInterestedCountryRepository bicr;

	@Autowired
	BlackListedGoodsRepository blgr;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	QuotationRepository quoteRepo;

	@Autowired
	UserRepository loginRepository;
	
	@Autowired
	NimaiOfflineUserDetailsRepository offLineRepo;
	
	@Autowired
	NimaiMOldCustRepository oldCuRepo;
	
	@Autowired
	KycRepository kycRepo;
	
	@Autowired
	PasswordGenerator password;
	
	@Override
	public ResponseEntity<?> createOrUpdateOfflineBanks(OffLineBankRequest request) {
		GenericResponse response=new GenericResponse<>();
	
		NimaiMCustomer customerDetails=new NimaiMCustomer();
		String subscriberType = request.getSubscriberType();
		String bankType = "";
		String userID = "";
		String addUserId="";
		String accountStatus = "PENDING";
		String kycStatus = "PENDING";
		if (request.getBankType() == null)
			bankType = "";
		else
			bankType = request.getBankType().toUpperCase();

		System.out.println(subscriberType + "   " + bankType);
		userID = userid.username(subscriberType, bankType);

		NimaiMCustomer checkUserId = customerRepository.getOne(userID);
		if (checkUserId != null && checkUserId.getUserid().equals(userID)) {
			System.out.println(subscriberType + "   " + bankType);
			userID = userid.newUsername(subscriberType, bankType);
		}
		request.setUserId(userID);
		customerDetails.setUserid(userID);
		customerDetails.setBankType(request.getBankType().toUpperCase());
		customerDetails.setAccountSource(request.getAccountSource());
		customerDetails.setSubscriberType(request.getSubscriberType());
		customerDetails.setBankName(request.getBankName());
		customerDetails.setAccountCreatedDate(Calendar.getInstance().getTime());
		customerDetails.setBusinessType(request.getBusinessType());
		customerDetails.setMinValueofLc(request.getMinLcValue());
		customerDetails.setOffBauStatus("Pending");
		customerDetails.setCurrencyCode(request.getCcy());
		
		//Added By Adil
		customerDetails.setCountryName(request.getBankCountry());
		customerDetails.setRegisteredCountry(request.getBankCountry());
		customerDetails.setKycStatus("Pending");
		customerDetails.setPaymentStatus("Pending");
		customerDetails.setEmailAddress(request.getEmailId());
		customerDetails.setAccountSource("WEBSITE");
		customerDetails.setAccountType("MASTER");
		
		customerDetails.setMrpa(request.getMrpa());
		//END
		NimaiOfflineUserDetails parentOffBauDetails=new NimaiOfflineUserDetails();
		Calendar cal2 = Calendar.getInstance();
		Date today2 = cal2.getTime();
	
		
		customerDetails.setFirstName(request.getFirstName());
		customerDetails.setLastName(request.getLastName());
		customerDetails.setEmailAddress(request.getEmailId());
		customerDetails.setMobileNumber(request.getMobileNo());
		customerDetails.setInsertedDate(today2);
		customerDetails.setIsOfflineBank("OFFLINE");
		customerDetails.setCreatedBy(request.getCreatedBy());
	
		NimaiMCustomer cust=customerRepository.save(customerDetails);
		
		for(AdditionalUserList userList:request.getAdditionalUserList()) {



			System.out.println(subscriberType + "   " + bankType);
			addUserId = userid.username("AdditionalUser", "AdditionalBank");

			NimaiOfflineUserDetails checkAddUserId = offLineRepo.findByOfflineUserId(addUserId);
			if (checkAddUserId != null && checkAddUserId.getAdditionalUserId().equals(addUserId)) {
				System.out.println(subscriberType + "   " + bankType);
				addUserId = userid.newUsername(subscriberType, bankType);
			}
			AdditionalUserList user=new AdditionalUserList();
			NimaiOfflineUserDetails offBauDetails=new NimaiOfflineUserDetails();



			offBauDetails.setAdditionalUserId(addUserId);

			offBauDetails.setParentUserId(userID);
			offBauDetails.setFirstName(userList.getFirstName());
			offBauDetails.setLastName(userList.getLastName());
			offBauDetails.setMobileNo(userList.getMobileNo());
			offBauDetails.setUserid(customerDetails);
			offBauDetails.setIsParent(0);
			offBauDetails.setCreatedBy(request.getCreatedBy());
			offBauDetails.setInsertedDate(today2);
			offBauDetails.setEmailAddress(userList.getEmailAddress());
			offLineRepo.save(offBauDetails);
		}
		for (BlackListedGoodsBean blgBean : request.getBlacklistedGoods()) {

			if (blgBean.getGoods_ID() == null) {
				NimaiFBlkgoods blg = new NimaiFBlkgoods();
				blg.setGoodsName(blgBean.getBlackListGoods());
				blg.setInsertedDate(Calendar.getInstance().getTime());
				blg.setUserid(customerDetails);
				blg.setGoodsMid(blgBean.getGoodsMId());
				saveBlackListedGoods(blg);
			} else {
				updateBlackListedGoods(blgBean);
			}

		}
		for (InterestedCountryBean intCon : request.getInterestedCountry()) {

			if (intCon.getCountryID() == null) {
				InterestedCountry ic = new InterestedCountry();
				ic.setCountryName(intCon.getCountriesIntrested());
				ic.setInsertedDate(Calendar.getInstance().getTime());
				ic.setUserId(customerDetails);
				ic.setCountryCurrencyId(intCon.getCcid());
				saveInterestedCountry(ic);
			} else {
				updateInterestedCountry(intCon);
			}

		}
		
		//Added By Adil
		for (BeneInterestedCountryBean intCon : request.getBeneInterestedCountry()) {

			if (intCon.getCountryID() == null) {
				BeneInterestedCountry ic = new BeneInterestedCountry();
				ic.setCountryName(intCon.getCountriesIntrested());
				ic.setInsertedDate(Calendar.getInstance().getTime());
				ic.setUserid(customerDetails);
				ic.setCountryCurrencyId(intCon.getCcid());
				saveBeneInterestedCountry(ic);
			} else {
				updateBeneInterestedCountry(intCon);
			}

		}
		//END
		
		if (cust != null) { // && customerRegister.getNimaiMLoginList().size() == 0) {

			NimaiMLogin loginEntity = new NimaiMLogin();

			loginEntity.setUserid(cust);
			loginEntity.setPassword(password.getInitialPassword(8));
			loginEntity.setUserType(request.getSubscriberType().toUpperCase());

			loginEntity.setIsActPassed("INACTIVE");
			loginEntity.setFlag("0");
			loginEntity.setStatus("INACTIVE");

			loginEntity.setCreatedDate(Calendar.getInstance().getTime());
			loginEntity.setLastModifiedDate(Calendar.getInstance().getTime());

			// loginEntity.setToken(customerRegister.getUserid());
			// loginEntity.setToken_exp_Date(Calendar.getInstance().getTime());

			loginEntity.setLastActivityTime(Calendar.getInstance().getTime());
			loginEntity.setLastLoginTime(Calendar.getInstance().getTime());

			loginRepository.save(loginEntity);
		}
		
		
		response.setData(customerDetails);
		response.setErrCode("ASA001");
		response.setMessage("Bank Details Save Successfully");
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}



	
	@Override
	public void saveBlackListedGoods(NimaiFBlkgoods blg) {
		// Changes From Sravan
		LOGGER.info("Save Black Listed Goods method is invoked in RegisterServiceImpl class");
		LOGGER.info(" Goods Name " + blg.getGoodsName() + " Goods ID " + blg.getGoodsId() + " GoodsMId "
				+ blg.getGoodsMid() + " Inserted Date " + blg.getInsertedDate() + " Modified Date "
				+ blg.getModifiedDate() + " User Id " + blg.getUserid());
		blgr.save(blg);
	}




	public boolean existsByEmailIdAddUser(OffLineBankRequest request) {

		NimaiOfflineUserDetails checkForDuplicateEmailId=offLineRepo.existsByEmailId(request.getEmailId());
		if(checkForDuplicateEmailId==null) {
			return true;
		}else {
			return false;
		}




	}



	@Override
	public boolean existsByEmIdAddUser(OffLineBankRequest request,String userType) {
		if(request.getEmailId().trim().equals("") || request.getEmailId()==null )
			return true;
		if (userType.equalsIgnoreCase("addPaUser")) {
			NimaiMCustomer checkForDuplicateEmailId = repo.existsByEmailId(request.getEmailId());
			return checkForDuplicateEmailId == null;
		} else {
			NimaiOfflineUserDetails checkForDuplicateEmailId = offLineRepo.existsByEmailId(request.getEmailId());
			return checkForDuplicateEmailId == null;
		}
	}
		@Override
	public void updateBlackListedGoods(BlackListedGoodsBean blgb) {
		// Changes From Sravan
		LOGGER.info("Update Black Listed Goods method is invoked in RegisterServiceImpl class");
		LOGGER.info(" Black List Goods " + blgb.getBlackListGoods() + " Goods ID " + blgb.getGoods_ID() + " Goods MId "
				+ blgb.getGoodsMId());
		NimaiFBlkgoods blg = blgr.getOne(blgb.getGoods_ID());
		if (blg != null) {
			blg.setGoodsName(blgb.getBlackListGoods());
			blg.setInsertedDate(Calendar.getInstance().getTime());
			blg.setGoodsMid(blgb.getGoodsMId());
			blgr.save(blg);
		}

	}
	
	@Override
	public void saveInterestedCountry(InterestedCountry ic) {
		// Changes From Sravan
		LOGGER.info("Save Interested Country method is invoked in RegisterServiceImpl class");
		LOGGER.info(" Country Name " + ic.getCountryName() + " Country Currency Id " + ic.getCountryCurrencyId()
				+ " Country Id " + ic.getCountryID() + " Inserted Date " + ic.getInsertedDate() + " Modified Date "
				+ ic.getModifiedDate() + " User Id " + ic.getUserId());
		icr.save(ic);
	}
	
	@Override
	public void updateInterestedCountry(InterestedCountryBean icb) {
		// Changes From Sravan
		LOGGER.info("Update Interested Country method is invoked in RegisterServiceImpl class");
		LOGGER.info(" Ccid " + icb.getCcid() + " Country Id " + icb.getCountryID());
		InterestedCountry ic = icr.getOne(icb.getCountryID());
		if (ic != null) {
			ic.setCountryName(icb.getCountriesIntrested());
			ic.setInsertedDate(Calendar.getInstance().getTime());
			ic.setCountryCurrencyId(icb.getCcid());
			icr.save(ic);
		}

	}
	
	@Override
	public void saveBeneInterestedCountry(BeneInterestedCountry ic) {
		// Changes From Sravan
		LOGGER.info("Save Interested Country method is invoked in RegisterServiceImpl class");
		LOGGER.info(" Country Name " + ic.getCountryName() + " Country Currency Id " + ic.getCountryCurrencyId()
				+ " Country Id " + ic.getCountryID() + " Inserted Date " + ic.getInsertedDate() + " Modified Date "
				+ ic.getModifiedDate() + " User Id " + ic.getUserid());
		bicr.save(ic);
	}
	
	@Override
	public void updateBeneInterestedCountry(BeneInterestedCountryBean icb) {
		// Changes From Sravan
		LOGGER.info("Update Interested Country method is invoked in RegisterServiceImpl class");
		LOGGER.info(" Ccid " + icb.getCcid() + " Country Id " + icb.getCountryID());
		BeneInterestedCountry ic = bicr.getOne(icb.getCountryID());
		if (ic != null) {
			ic.setCountryName(icb.getCountriesIntrested());
			ic.setInsertedDate(Calendar.getInstance().getTime());
			ic.setCountryCurrencyId(icb.getCcid());
			bicr.save(ic);
		}

	}



@Override
	public PagedResponse<?> getSearchOfflineBankDetail(SearchRequest request) {
		PageRequest pageRequest;
		request.setSubscriberType("BANK");
		request.setBankType("UNDERWRITER");
		Pageable pageable = null;
		request.setSortBy("inserted_date");
		if ((request.getUserId() != null && request.getTxtStatus() == null)
				|| (request.getEmailId() != null && request.getTxtStatus() == null)
				|| (request.getMobileNo() != null && request.getTxtStatus() == null)) {
			request.setSize(1);
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc")
							? Sort.by(new String[] { request.getSortBy() }).descending()
							: Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if (request.getTxtStatus() == null) {
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc")
							? Sort.by(new String[] { request.getSortBy() }).descending()
							: Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if (request.getTxtStatus().equalsIgnoreCase("Pending")) {
			request.setSortBy("nfk.INSERTED_DATE");
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc")
							? Sort.by(new String[] { request.getSortBy() }).descending()
							: Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {
			request.setSortBy("nfk.INSERTED_DATE");
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc")
							? Sort.by(new String[] { request.getSortBy() }).descending()
							: Sort.by(new String[] { request.getSortBy() }).ascending());
		} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
			request.setSortBy("inserted_date");
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc")
							? Sort.by(new String[] { request.getSortBy() }).descending()
							: Sort.by(new String[] { request.getSortBy() }).ascending());
		} else {
			pageRequest = PageRequest.of(request.getPage(), request.getSize(),
					request.getDirection().equalsIgnoreCase("desc")
							? Sort.by(new String[] { request.getSortBy() }).descending()
							: Sort.by(new String[] { request.getSortBy() }).ascending());
		}
		String countryNames = Utility.getUserCountry();
		System.out.println("countryNames: " + countryNames);
		if (countryNames != null && countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
			countryNames = "";
			List<String> countryList = this.repo.getCountryList();
			for (String country : countryList)
				countryNames = countryNames + country + ",";
			System.out.println("Country List: " + countryNames);
			request.setCountryNames(countryNames);
		} else if (countryNames != null && !countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
			request.setCountryNames(countryNames);
		}
	List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
				.collect(Collectors.toList());
		System.out.println("Values BankService: " + value);
		if (request.getRole() != null && (request.getRole().equalsIgnoreCase("Bank RM")
		|| request.getRole().equalsIgnoreCase("Customer RM"))) {
			request.setLoginUserId(Utility.getUserId());
			request.setRmStatus("Active");
		}
		Page<NimaiMCustomer> bankDetails = null;
		if (request.getRole().equalsIgnoreCase("Bank RM")||
				request.getRole().equalsIgnoreCase("Customer RM")) {
			String userId = Utility.getUserId();
			System.out.println("===========RM userId" + userId);
			bankDetails = retiriveListOnRMId(userId, request, value, (Pageable) pageRequest);
		} else if (request.getUserId() != null && request.getTxtStatus() == null) {
			System.out.println("************=============1.insdie request.getUserId()============***********"
					+ request.getUserId());
			bankDetails = this.repo.getOffBankDetailsByUser(request.getUserId(), value, (Pageable) pageRequest);
		} else if (request.getMobileNo() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============2.request.getMobileNo()============***********" + request.getUserId());
			bankDetails = this.repo.getOffBankDetailsByMobile(request.getMobileNo(), value, (Pageable) pageRequest);
		} else if (request.getCountry() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============3.request.getCountry()============***********" + request.getUserId());
			bankDetails = this.repo.getOffBankDetailsByCountr(request.getCountry(), value, (Pageable) pageRequest);
		} else if (request.getBankName() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============4.request.getCompanyName()============***********" + request.getUserId());
			bankDetails = this.repo.getOffBankDetailsByCompanyNa(request.getBankName(), value, (Pageable) pageRequest);
		} else if (request.getEmailId() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============5.request.getEmailId()============***********" + request.getUserId());
			bankDetails = this.repo.getOffBankDetailsByEmail(request.getEmailId(), value, (Pageable) pageRequest);
		} else if (request.getTxtStatus() == null || request.getTxtStatus().equalsIgnoreCase("all")) {


			bankDetails = this.repo.getAllOffBankKYC(value, (Pageable) pageRequest);
			System.out.println("************=============6.request.getTxtStatus() == null============***********"
					+ request.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Pending")) {
			bankDetails = this.repo.getPendingBankKYC(value, (Pageable) pageRequest);
			System.out.println(
					"************=============7.request.getTxtStatus().equalsIgnoreCase(\"Pending\") == null============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPending")) {
			bankDetails = this.repo.getPaymentPendingBank(value, (Pageable) pageRequest);
			System.out.println(
					"************=============7.request.getTxtStatus().equalsIgnoreCase(\"PaymentPending\") ============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("subexpiry")) {
			bankDetails = this.repo.getSubscriptionExpiryBank(value, (Pageable) pageRequest);
			System.out.println("************=============Subscription Expiry in 30 days============***********");
		} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
			bankDetails = this.repo.getPaymentPendingUserBank(value, (Pageable) pageRequest);
			System.out.println(
					"************=============PaymentPaymentUser============***********" + request.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Approved")) {
			bankDetails = this.repo.getApprovedBankKYC(value, (Pageable) pageRequest);
			System.out.println(
					"************=============8.request.getTxtStatus().equalsIgnoreCase(\"Approved\")============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {
			bankDetails = this.repo.getRejectedBankKYC(value, (Pageable) pageRequest);
			System.out.println(
					"************=============10.request.getTxtStatus().equalsIgnoreCase(\"Rejected\")============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
			bankDetails = this.repo.getNotUploadBankKYC(value, (Pageable) pageRequest);
			System.out.println(
					"************=============11.request.getTxtStatus().equalsIgnoreCase(\"Not Uploaded\")============***********"
							+ request

									.getUserId());
		}
		List<BankDetailsResponse> responses = bankDetails.map(cust -> {
			BankDetailsResponse response = new BankDetailsResponse();
			response.setUserid(cust.getUserid());
			response.setFirstName(cust.getFirstName());
			response.setLastName(cust.getLastName());
			response.setEmailAddress(cust.getEmailAddress());
			response.setMobileNumber(cust.getMobileNumber());
			response.setLandline(cust.getLandline());
			response.setCountryName(cust.getCountryName());
			response.setBankName(cust.getBankName());
			response.setMrpa(cust.getMrpa());
			response.setCreatedBy(cust.getCreatedBy());
			response.setOffBaauStatus(cust.getOffBauStatus());
//			if (request.getTxtStatus() == null) {
//				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
//						? collectPlanName(cust.getNimaiSubscriptionDetailsList())
//						: "No Active Plan");
//				if (response.getPlanOfPayments().isEmpty() || response.getPlanOfPayments() == null)
//					response.setPlanOfPayments(
//							"Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
//			} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
//				if (cust.getNimaiSubscriptionDetailsList().size() != 0) {
//					response.setPlanOfPayments(
//							"Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
//				} else {
//					response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
//							? collectPlanName(cust.getNimaiSubscriptionDetailsList())
//							: "No Active Plan");
//				}
//			} else {
//				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
//						? collectPlanName(cust.getNimaiSubscriptionDetailsList())
//						: "No Active Plan");
//				if (response.getPlanOfPayments().isEmpty() || response.getPlanOfPayments() == null)
//					response.setPlanOfPayments(
//							"Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
//			}
			response.setTotalQuotes(this.quoteRepo.quoteCout(cust.getUserid()));
		//	List<NimaiFKyc> kycdetails = this.kycRepo.findByUserid(cust);
//			if (kycdetails.size() == 0) {
//				response.setKyc("Not Uploaded");
//			} else {
//				response.setKyc(cust.getKycStatus());
//			}
			response.setRegisteredCountry(cust.getRegisteredCountry());
			return response;
		}).getContent();
		return new PagedResponse(responses, bankDetails.getNumber(), bankDetails.getSize(),
				bankDetails.getTotalElements(), bankDetails.getTotalPages(), bankDetails.isLast());
	}




	private Page<NimaiMCustomer> retiriveListOnRMId(String rmId, SearchRequest request, List<String> value,
			Pageable pageable) {
		Page<NimaiMCustomer> bankDetails = null;
		if (request.getUserId() != null && request.getTxtStatus() == null) {
			System.out.println("************=============1.insdie request.getUserId()============***********"
					+ request.getUserId());
			bankDetails = this.repo.getOffBankDetailsByUserIdRmId(request.getUserId(), rmId, value, pageable);
		} else if (request.getMobileNo() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============2.request.getMobileNo()============***********" + request.getUserId());
			bankDetails = this.repo.getOffBankDetailsByMobileNoRmId(request.getMobileNo(), rmId, value, pageable);
		} else if (request.getCountry() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============3.request.getCountry()============***********" + request.getUserId());
			bankDetails = this.repo.getoffBankDetailsByCountryRmId(request.getCountry(), rmId, value, pageable);
		} else if (request.getBankName() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============4.request.getCompanyName()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByCompanyNameRmId(request.getBankName(), rmId, value, pageable);
		} else if (request.getEmailId() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============5.request.getEmailId()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByEmailIdRmId(request.getEmailId(), rmId, value, pageable);
		} else if (request.getTxtStatus() == null || request.getTxtStatus().equalsIgnoreCase("all")) {
			bankDetails = this.repo.getAllOffBankKYCRmId(rmId, value, pageable);
			System.out.println("************=============6.request.getTxtStatus() == null============***********"
					+ request.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Pending")) {
			bankDetails = this.repo.getPendingffBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============7.request.getTxtStatus().equalsIgnoreCase(\"Pending\") == null============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Approved")) {
			bankDetails = this.repo.getApprovedOffBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============8.request.getTxtStatus().equalsIgnoreCase(\"Approved\")============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {
			bankDetails = this.repo.getRejectedOffBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============10.request.getTxtStatus().equalsIgnoreCase(\"Rejected\")============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
			bankDetails = this.repo.getNotUploadOffBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============11.request.getTxtStatus().equalsIgnoreCase(\"Not Uploaded\")============***********"
							+ request

									.getUserId());
		}
		return bankDetails;
	}
	
	public String collectPlanName(List<NimaiSubscriptionDetails> subscriptionList) {
		return ((String) subscriptionList.stream().filter(plan -> plan.getStatus().equalsIgnoreCase("Active"))
				.sorted(Comparator.comparingInt(NimaiSubscriptionDetails::getSplSerialNumber).reversed())
				.map(NimaiSubscriptionDetails::getSubscriptionName).collect(Collectors.joining(" "))).toString();
	}

	public String collectVasPlanName(List<NimaiSubscriptionVas> subscriptionList) {
		return ((String) subscriptionList.stream().filter(plan -> plan.getStatus().equalsIgnoreCase("Active"))
				.sorted(Comparator.comparingInt(NimaiSubscriptionVas::getVasId).reversed())
				.map(NimaiSubscriptionVas::getPlanName).collect(Collectors.joining(" "))).toString();
	}

	public String collectInPlanName(List<NimaiSubscriptionDetails> subscriptionList) {
		return subscriptionList.stream().filter(plan -> plan.getStatus().equalsIgnoreCase("INACTIVE"))
				.sorted(Comparator.comparingInt(NimaiSubscriptionDetails::getSplSerialNumber).reversed()).findFirst()
				.map(NimaiSubscriptionDetails::getSubscriptionName).get();
	}




//	@Override
//	public PagedResponse<?> getSearchOfflineBankDetail(SearchRequest request) {
//		// TODO Auto-generated method stub
//		return null;
//	}




	@Override
	public ResponseEntity<?> updateOffBauUserStatus(String userId, String status) {
		NimaiMCustomer bauUser = customerRepository.getOne(userId);
		if(bauUser.getCreatedBy().equalsIgnoreCase(Utility.getUserId())) {
			return new ResponseEntity<>(new ApiResponse(true, "Unauthorized Operation !!!!"),
					HttpStatus.OK);	
		}
		bauUser.setOffBauStatus(status);
		bauUser.setOffBauApprovalDate((Calendar.getInstance().getTime()));
		bauUser.setOffBauApprovedBy(Utility.getUserId());
		bauUser.setRmStatus("Active");
		Optional<NimaiMLogin> bauLogin=userRepository.findByBauUserId(userId);
		customerRepository.save(bauUser);
		LOGGER.info("Employee status updated successfully... " + userId);
		return new ResponseEntity<>(new ApiResponse(true, "Employee status updated successfully..."),
				HttpStatus.CREATED);
	}
	
	@Override
	public boolean existsByEmailId(OffLineBankRequest request) {
		
			NimaiMCustomer checkForDuplicateEmailId=customerRepository.existsByEmailId(request.getEmailId());
			if(checkForDuplicateEmailId==null) {
				return true;
			}else {
				return false;
			}

		
		
		
	}




	@Override
	public ResponseEntity<?> editOffBauUserStatus(OffLineBankRequest request) {
		// TODO Auto-generated method stub
		GenericResponse response=new GenericResponse<>();
		
		Calendar cal2 = Calendar.getInstance();
		Date today2 = cal2.getTime();
		NimaiMCustomer checkUserId = customerRepository.getOne(request.getUserId());
		
		NimaiMOldCustDetails details=new NimaiMOldCustDetails();
		details.setOldFirstName(checkUserId.getFirstName());
		details.setOldastName(checkUserId.getLastName());
		details.setOldEmailId(checkUserId.getEmailAddress());
		details.setOldMobileNumber(checkUserId.getMobileNumber());
		details.setInsertedDate(today2);
		details.setModifiedDate(today2);
		details.setModifiedBy(request.getModifiedBy());
		details.setUserid(checkUserId);
		
		oldCuRepo.save(details);
		
		if(request.getFirstName()==null || request.getFirstName().equalsIgnoreCase(checkUserId.getFirstName())) {
			checkUserId.setFirstName(checkUserId.getFirstName());
		}else {
			checkUserId.setFirstName(request.getFirstName());
		}
		if(request.getLastName()==null || request.getFirstName().equalsIgnoreCase(checkUserId.getLastName())) {
			checkUserId.setLastName(checkUserId.getLastName());
		}else {
			checkUserId.setLastName(request.getLastName());
		}
		if(request.getEmailId()==null || request.getEmailId().equalsIgnoreCase(checkUserId.getEmailAddress())) {
			checkUserId.setEmailAddress(checkUserId.getEmailAddress());
		}else {
			 
			  Boolean checkForDuplicateEmailId=existsByEmailId(request);
			  if(checkForDuplicateEmailId==true) {
				  
				  checkUserId.setEmailAddress(request.getEmailId());
			  }else {
				  
					response.setErrCode("ASA002");
					response.setMessage("Email Id Already exists");
					return new ResponseEntity<Object>(response, HttpStatus.OK);
			  }
		}
		if(request.getMobileNo()==null || request.getMobileNo().equalsIgnoreCase(checkUserId.getMobileNumber())) {
			checkUserId.setMobileNumber(checkUserId.getMobileNumber());
		}else {
			checkUserId.setMobileNumber(request.getMobileNo());
		}
		
		
		checkUserId.setModifiedDate(today2);
	//	checkUserId.setModifiedBy(request.getModifiedBy());
		customerRepository.save(checkUserId);
		
		OfflineUpdateResponse res =new OfflineUpdateResponse();
		res.setId(details.getGoodsId());
		response.setFlag(details.getGoodsId());
		response.setData(res);
		response.setErrCode("ASA001");
		response.setMessage("Personal details updated Successfully");
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}




	
}
