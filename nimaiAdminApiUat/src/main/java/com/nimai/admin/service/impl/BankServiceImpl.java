package com.nimai.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimai.admin.exception.ResourceNotFoundException;

import com.nimai.admin.model.*;
import com.nimai.admin.model.GenericResponse;
import com.nimai.admin.payload.*;
import com.nimai.admin.repository.*;
import com.nimai.admin.service.BankService;
import com.nimai.admin.specification.CustomerSearchSpecification;
import com.nimai.admin.specification.KycSpecification;
import com.nimai.admin.specification.NimaiSubscriptionVasSpecification;
import com.nimai.admin.specification.QuotationSpecification;
import com.nimai.admin.util.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;
import javax.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BankServiceImpl implements BankService {
	private static Logger logger = LoggerFactory.getLogger(BankServiceImpl.class);

	@Autowired
	OwnerMasterRepository owrepo;

	@Autowired
	MasterSubsPlanRepository sPlanMasterRepo;

	@Autowired
	QuotationRepository quotemasterrepo;
	
	@Autowired
	PhaseTwoTransferFailRepository phaseRepo;

	@Autowired
	SubscriptionDetailsRepository sPlanRepo;
	
	@Autowired
	NimaiOptimizedKycRepository optKycRepo;

	@Autowired
	CustomerRepository repo;

	@Autowired
	SourceDetailsRepository sourceRepo;

	@Autowired
	nimaiSystemConfigRepository configRepo;

	@Autowired
	NimaiLcMasterRepository lcMasterrepo;

	@Autowired
	SubscriptionVasRepository vasRep;

	@Autowired
	QuotationRepository quoteRepo;

	@Autowired
	NimaiKycRepository kycNRepo;

	@Autowired
	KycRepository kycRepo;

	@Autowired
	SubscriptionDetailsRepository planRepo;

	@Autowired
	ReferrerRepository referrerRepo;

	@Autowired
	KycFiledRepository fieldRepo;

	@Autowired
	PostPaidPlanRepository postPaidRepo;

	@Autowired
	NimaiPostpaidSubscriptionDetailsUpdRepo postPaidRepoUpd;

	@Autowired
	CustomerSearchSpecification customerSearchSpecification;

	@Autowired
	NimaiSubscriptionVasSpecification vasSearchSpecification;

	@Autowired
	TransactionsRepository transactionsRepository;

	@Autowired
	DiscountRepository discRepo;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	InterestedCountryRepository icr;
	@Autowired
	BeneInterestedCountryRepository bicr;

	@Autowired
	QuotationSpecification quotationSpecification;

	@Autowired
	KycSpecification kycSpecification;

	@Autowired
	NimaiEmailSchedulerRepository schRepo;


	@Autowired
	ThirdPartyApiIntegration tPartApi;


	@Autowired
	SubscriptionVasRepository vasRepo;

	@Autowired
	PreferredBankRepository preferBankRepo;

	@Autowired
	BankRatingRepository bankratingRepo;

	@Autowired
	MasterRatingsRepository ratingRepo;

	@Autowired
	EntityManagerFactory em;

	@Autowired
	NimaiEmailSchedulerAlertToBanksRepository userDao;

	@Autowired
	NimaiOfflineUserDetailsRepository offLineRepo;

	@Autowired
	SPlanSerialComparator comparatorSplan;

	@Autowired
	NimaiTransactionViewCountRepo viewCountRepo;

	public ResponseEntity<BankDetailsResponse> getBankDetailUserId(String userid) {
		NimaiMCustomer cust = this.repo.findByUserid(userid);
		if (cust != null) {
			BankDetailsResponse response = ModelMapper.mapBankDetails(cust);
			response.setCurrencyCode(cust.getCurrencyCode());
			response.setDesignation(cust.getDesignation());
			System.out.println("Email ID: " + cust.getEmailAddress());
//			if(cust.getRmDetails()==null) {
//				response.setRmDetails("");
//			}else {
//				NimaiMEmployee empDetails
//				response.setRmDetails(cust.get);
//			}
			NimaiMCustomer referredcust = null;
			try {
				if (cust.getIsAssociated() == 1) {
					response.setIsAssociated(cust.getIsAssociated());
					response.setParentUserId(cust.getAccountSource());
					response.setReferredUserId("");
					response.setReferredFirstName("");
					response.setReferredLastName("");
					response.setReferredCompanyName("");
				} else {
					String referredUserId = this.referrerRepo.findReferredUserByEmailId(cust.getEmailAddress());
					System.out.println("Referred User ID: " + referredUserId);
					referredcust = this.repo.findByUserid(referredUserId);
				}
			} catch (Exception e) {
				response.setReferredUserId("");
				response.setReferredFirstName("");
				response.setReferredLastName("");
				response.setReferredCompanyName("");
			}
			if (referredcust != null) {
				response.setReferredUserId(referredcust.getUserid());
				response.setReferredFirstName(referredcust.getFirstName());
				response.setReferredLastName(referredcust.getLastName());
				response.setReferredCompanyName(referredcust.getCompanyName());
			} else {
				response.setReferredUserId("");
				response.setReferredFirstName("");
				response.setReferredLastName("");
				response.setReferredCompanyName("");
			}
			if (response.getRmFirstName() != null) {
				NimaiMEmployee emp = this.employeeRepository.findByEmpCode(response.getRmFirstName());
				response.setRmFirstName(emp.getEmpName());
				response.setRmLastName(emp.getEmpLastName());
				response.setRmDesignation(emp.getDesignation());
				response.setRmDetails(emp.getEmpName() + " " + emp.getEmpLastName());
			}
			if (userid.substring(0, 2).equalsIgnoreCase("BA")) {
				List<NimaiMCustomer> subsidiaryList = this.repo.findAdditionalUserByUserId(userid);
				if (subsidiaryList != null && subsidiaryList.size() != 0) {
					AssociatedAccountsDetails associatedAccountsDetails = null;
					List<AssociatedAccountsDetails> data = new ArrayList<>();
					for (NimaiMCustomer sub : subsidiaryList) {
						AssociatedAccountsDetails subdetails = new AssociatedAccountsDetails();
						subdetails.setUserId(sub.getUserid());
						subdetails.setName(sub.getFirstName());
						subdetails.setStatus(sub.getAccountStatus());
						subdetails.setEmailId(sub.getEmailAddress());
						subdetails.setCountry(sub.getRegisteredCountry());
						subdetails.setLandline(sub.getLandline());
						data.add(subdetails);
					}
					response.setSubsidiary(data);
				}
			} else {
				List<NimaiMCustomer> subsidiaryList = this.repo.findSubsidiaryByUserId(userid);
				if (subsidiaryList != null && subsidiaryList.size() != 0) {
					List<AssociatedAccountsDetails> data = new ArrayList<>();
					for (NimaiMCustomer sub : subsidiaryList) {
						AssociatedAccountsDetails subdetails = new AssociatedAccountsDetails();
						subdetails.setUserId(sub.getUserid());
						subdetails.setName(sub.getFirstName());
						subdetails.setStatus(sub.getAccountStatus());
						subdetails.setEmailId(sub.getEmailAddress());
						subdetails.setCountry(sub.getRegisteredCountry());
						subdetails.setLandline(sub.getLandline());
						data.add(subdetails);
					}
					response.setSubsidiary(data);
				}
			}
			List<NimaiFOwner> owener = this.owrepo.findDataByUserId(userid);
			System.out.println("Owner List: " + owener);
			if (owener != null && owener.size() != 0) {
				List<OwenerBean> data2 = (List<OwenerBean>) owener.stream()
						.filter(d1 -> d1.getUserid().equals(d1.getUserid()))
						.map(owe -> new OwenerBean(owe.getOwnerFname(), owe.getOwnerLname(), owe.getOwnerDesignation()))
						.collect(Collectors.toList());
				response.setOwner(data2);
			}

			List<NimaiOfflineUserDetails> additionUser = this.offLineRepo.findByOfflineUserIdSearch(userid);
			System.out.println("AdditionEmail List: " + additionUser);

			if (additionUser != null && additionUser.size() != 0) {
				List<AdditionalUserList> data = new ArrayList<>();
				for (NimaiOfflineUserDetails sub : additionUser) {
					AdditionalUserList addOfflinedetails = new AdditionalUserList();
					addOfflinedetails.setFirstName(sub.getFirstName());
					addOfflinedetails.setLastName(sub.getLastName());
					addOfflinedetails.setEmailAddress(sub.getEmailAddress());
					addOfflinedetails.setMobileNo(sub.getMobileNo());

					data.add(addOfflinedetails);
				}
				response.setAdditionalEmailList(data);
			} else {
				response.setAdditionalEmailList(null);
			}

			return new ResponseEntity(response, HttpStatus.OK);
		}
		logger.info("No Bank Details exist for given id");
		throw new ResourceNotFoundException("No Bank Details exist for given id");
	}

	public List<QuotationDetailsResponse> getQuotesUserId(String userId) {
		List<NimaiMQuotation> quotes = this.quoteRepo.findByUserid(new NimaiMCustomer(userId));
		List<QuotationDetailsResponse> response = (List<QuotationDetailsResponse>) quotes.stream()
				.map(emp -> new QuotationDetailsResponse(emp.getQuotationId(), emp.getUserid().getUserid(),
						emp.getTransactionId().getTransactionId(), emp.getBankUserid(), emp.getConfirmationCharges(),
						emp.getConfChgsIssuanceToNegot(), emp.getConfChgsIssuanceToMatur(),
						emp.getMinimumTransactionCharges(), emp.getOtherCharges(), emp.getValidityDate(),
						emp.getTotalQuoteValue(), emp.getCurrency(), emp.getQuotationStatus()))
				.collect(Collectors.toList());
		return response;
	}

	public List<KycSDetailResponse> getKycDetailsUserId(NimaiMCustomer userId) {
		List<NimaiFKyc> fkyc = this.kycRepo.findKycByUserid(userId.getUserid());
		List<KycSDetailResponse> kycResp;
		List<KycSDetailResponse> map = null;
		NimaiMCustomer associatedCust = this.repo.getOne(userId.getUserid());
		if (associatedCust.getIsAssociated() == 1) {
			NimaiMCustomer parentUser = this.repo.getOne(associatedCust.getAccountSource());
			associatedCust.setSourceDetails(parentUser.getSourceDetails());
			this.repo.save(associatedCust);
			String cuType = "associateCompany";
			NimaiMEmployee empDetails = employeeRepository.findByEmpCode(associatedCust.getRmId());

			if (empDetails == null) {
				// if(empDetails.getem)
				String rmDetails;

				NimaiMEmployee rmUser = employeeRepository.findByEmpCode(parentUser.getRmDetails());
				if (rmUser == null) {
					rmDetails = "";

				} else {
					rmDetails = rmUser.getEmpName() + " " + rmUser.getEmpLastName() + "-" + rmUser.getEmpCode();
				}

				kycResp = (List<KycSDetailResponse>) fkyc.stream()
						.sorted(Comparator.comparing(NimaiFKyc::getId).reversed())
						.map(kyc -> new KycSDetailResponse(kyc.getId(), kyc.getDocumentName(), kyc.getCountry(),
								parentUser.getSourceDetails(), parentUser.getSourceDetails(), kyc.getKycType(),
								kyc.getDocumentType(), kyc.getComment(), kyc.getKycStatus(), kyc.getCheckerComment(),
								kyc.getEncodedFileContent().substring(kyc.getEncodedFileContent().indexOf("|") + 1,
										kyc.getEncodedFileContent().length()),
								associatedCust.getIsAssociated(), cuType, rmDetails))
						.collect(Collectors.toList());

			} else {
				String rmDetails;

				NimaiMEmployee rmUser = employeeRepository.findByEmpCode(parentUser.getRmDetails());
				if (rmUser == null) {
					rmDetails = "";

				} else {
					rmDetails = rmUser.getEmpName() + " " + rmUser.getEmpLastName() + "-" + rmUser.getEmpCode();
				}
				kycResp = (List<KycSDetailResponse>) fkyc.stream()
						.sorted(Comparator.comparing(NimaiFKyc::getId).reversed())

						.map(kyc -> new KycSDetailResponse(kyc.getId(), kyc.getDocumentName(), kyc.getCountry(),
								parentUser.getSourceDetails(), parentUser.getSourceDetails(), kyc.getKycType(),
								kyc.getDocumentType(), kyc.getComment(), kyc.getKycStatus(), kyc.getCheckerComment(),
								kyc.getEncodedFileContent().substring(kyc.getEncodedFileContent().indexOf("|") + 1,
										kyc.getEncodedFileContent().length()),
								associatedCust.getIsAssociated(), cuType, rmDetails,
								empDetails.getEmpName() == null ? "" : empDetails.getEmpName(),
								empDetails.getEmpLastName() == null ? "" : empDetails.getEmpLastName(),
								empDetails.getDesignation() == null ? "" : empDetails.getDesignation()))
						.collect(Collectors.toList());
			}

		} else {
			String cuType = "parentCompany";
			NimaiMCustomer parentUser = this.repo.getOne(userId.getUserid());
			NimaiMEmployee empDetails = employeeRepository.findByEmpCode(associatedCust.getRmId());
			if (empDetails == null) {
				String rmDetails;

				NimaiMEmployee rmUser = employeeRepository.findByEmpCode(associatedCust.getRmDetails());
				if (rmUser == null) {
					rmDetails = "";

				} else {
					rmDetails = rmUser.getEmpName() + " " + rmUser.getEmpLastName() + "-" + rmUser.getEmpCode();
				}
				kycResp = (List<KycSDetailResponse>) fkyc.stream()
						.sorted(Comparator.comparing(NimaiFKyc::getId).reversed())
						.map(kyc -> new KycSDetailResponse(kyc.getId(), kyc.getDocumentName(), kyc.getCountry(),
								parentUser.getSourceDetails(), parentUser.getSourceDetails(), kyc.getKycType(),
								kyc.getDocumentType(), kyc.getComment(), kyc.getKycStatus(), kyc.getCheckerComment(),
								kyc.getEncodedFileContent().substring(kyc.getEncodedFileContent().indexOf("|") + 1,
										kyc.getEncodedFileContent().length()),
								associatedCust.getIsAssociated(), cuType, rmDetails))

						.collect(Collectors.toList());

			} else {
				String rmDetails;

				NimaiMEmployee rmUser = employeeRepository.findByEmpCode(associatedCust.getRmDetails());
				if (rmUser == null) {
					rmDetails = "";

				} else {
					rmDetails = rmUser.getEmpName() + " " + rmUser.getEmpLastName() + "-" + rmUser.getEmpCode();
				}
				kycResp = (List<KycSDetailResponse>) fkyc.stream()
						.sorted(Comparator.comparing(NimaiFKyc::getId).reversed())
						.map(kyc -> new KycSDetailResponse(kyc.getId(), kyc.getDocumentName(), kyc.getCountry(),
								parentUser.getSourceDetails(), parentUser.getSourceDetails(), kyc.getKycType(),
								kyc.getDocumentType(), kyc.getComment(), kyc.getKycStatus(), kyc.getCheckerComment(),
								kyc.getEncodedFileContent().substring(kyc.getEncodedFileContent().indexOf("|") + 1),
								associatedCust.getIsAssociated(), cuType, rmDetails,
								empDetails == null ? "" : empDetails.getEmpName(),
								empDetails == null ? "" : empDetails.getEmpLastName(),
								empDetails == null ? "" : empDetails.getDesignation()))

						.collect(Collectors.toList());

			}

			// map=
			// kycResp.stream().map(s->s).filter(s->s!=null).collect(Collectors.toList());
		}

		return kycResp;
	}

	public List<PlanOfPaymentDetailsResponse> getPlanOPayDetails(String userId) {
		NimaiMCustomer cust = (NimaiMCustomer) this.repo.getOne(userId);
		List<NimaiSubscriptionDetails> subs = (List<NimaiSubscriptionDetails>) this.planRepo.findByUserid(cust).stream()
				.sorted(Comparator.comparingInt(NimaiSubscriptionDetails::getSplSerialNumber).reversed()).limit(5L)
				.collect(Collectors.toList());
		List<PlanOfPaymentDetailsResponse> data = new ArrayList<>();
		for (NimaiSubscriptionDetails pay : subs) {
			PlanOfPaymentDetailsResponse value = new PlanOfPaymentDetailsResponse();
			value.setSubscriptionId(pay.getSubscriptionId());
			value.setSplSerialNumber(pay.getSplSerialNumber());
			value.setUserid(pay.getUserid().getUserid());
			value.setSubscriptionName(pay.getSubscriptionName());
			value.setSubscriptionAmount(
					(pay.getSubscriptionAmount() != null) ? ("USD " + pay.getSubscriptionAmount()) : "");
			value.setLcCount(pay.getLcCount() + " Trxn Credits");
			value.setSplanStartDate(pay.getSplanStartDate());
			value.setSplanEndDate(pay.getSplanEndDate());
			value.setSubsidiaries(pay.getSubsidiaries() + " Group Companies");
			value.setRelationshipManager(pay.getRelationshipManager() + ", Relationship Manager");
			value.setCustomerSupport(pay.getCustomerSupport() + " Customer Support");
			value.setRemark(pay.getRemark());
			value.setIsVasAppliedWithSPlan(pay.getIsVasApplied());
			if (pay.getStatus().equalsIgnoreCase("ACTIVE")) {
				value.setStatus("Active");
				value.setMakerComment(pay.getMakerComment());
				value.setCheckerComment(pay.getCheckerComment());
			} else {
				value.setStatus(pay.getStatus());
			}
			value.setSubscriptionValidity(pay.getSubscriptionValidity() + " Months Validity");
			value.setDiscount((pay.getDiscount() == null) ? "" : ("USD " + pay.getDiscount()));
			value.setPaymentStatus(pay.getPaymentStatus());
			value.setInsertedDate(pay.getInsertedDate());
			value.setPaymentMode(pay.getPaymentMode());
			List<NimaiSubscriptionVas> subsVas = this.vasRep.findByUserIdAndSubscriptionIdAndSerialNo(userId,
					pay.getSubscriptionId(), pay.getSplSerialNumber().intValue());
			VasDetails vasDetailsVa = new VasDetails();
			List<VasDetails> details = new ArrayList<>();
			if (subsVas == null) {
				vasDetailsVa.setVasPlan("");
				vasDetailsVa.setVasStatus("");
				vasDetailsVa.setVasBenefits("");
				vasDetailsVa.setVasAmount("");
				vasDetailsVa.setTotalAmount("");
				details.add(vasDetailsVa);
			}
			List admins = (List) subsVas.stream().filter(subsVa -> 
			(subsVa.getPaymentSts().equalsIgnoreCase("Pending")
					&& subsVa.getStatus().equalsIgnoreCase("ACTIVE"))).collect(Collectors.toList());
			if (admins.size() >= 1) {
				value.setIsMultipleVasApplied("1");
			} else {
				value.setIsMultipleVasApplied("0");
			}
			for (NimaiSubscriptionVas vasDetailsNew : subsVas) {
				VasDetails vasDetailsValue = new VasDetails();
				System.out.println("subscriptionid" + pay.getSplSerialNumber());
				System.out.println("" + vasDetailsNew.toString());
				if (pay.getStatus().equalsIgnoreCase("ACTIVE") || pay.getStatus().equalsIgnoreCase("INACTIVE")) {
					if ((vasDetailsNew.getStatus().equalsIgnoreCase("Active")
							|| vasDetailsNew.getStatus().equalsIgnoreCase("Inactive")) && pay.getVasAmount() != 0
							&& !vasDetailsNew.getStatus().equalsIgnoreCase("Rejected")) {
						vasDetailsValue.setVasPlan(vasDetailsNew.getPlanName());
						vasDetailsValue.setVasStatus(vasDetailsNew.getStatus());
						vasDetailsValue.setVasBenefits(vasDetailsNew.getDescription1() + ", "
								+ vasDetailsNew.getDescription2() + ", " + vasDetailsNew.getDescription3());
						vasDetailsValue.setVasAmount("USD " + vasDetailsNew.getPricing());
						vasDetailsValue.setTotalAmount("USD "
								+ (pay.getSubscriptionAmount().intValue() + vasDetailsNew.getPricing().floatValue()));
						vasDetailsValue.setVasMakerComment(vasDetailsNew.getMakerComment());
						vasDetailsValue.setVasCheckerComment(vasDetailsNew.getCheckerComment());
						vasDetailsValue.setVasPaymentStatus(vasDetailsNew.getPaymentSts());
						vasDetailsValue.setVasPlanPaymentMode(vasDetailsNew.getMode());
						vasDetailsValue.setVasId(vasDetailsNew.getId());
						if (vasDetailsNew.getsPlanVasFlag() == null) {
							vasDetailsValue.setIsSplanWithVasFlag(2);
						} else if (vasDetailsNew.getsPlanVasFlag().intValue() == 0) {
							value.setIsSplanWithVasFlag(vasDetailsNew.getsPlanVasFlag().intValue());
						} else {
							if (vasDetailsNew.getsPlanVasFlag().intValue() != 1)
								continue;
							vasDetailsValue.setIsSplanWithVasFlag(vasDetailsNew.getsPlanVasFlag().intValue());
						}
					}
					details.add(vasDetailsValue);
				} else {
					if (vasDetailsNew.getStatus().equalsIgnoreCase("Inactive") && pay.getVasAmount() != 0) {
						vasDetailsValue.setVasPlan(vasDetailsNew.getPlanName());
						vasDetailsValue.setVasStatus(vasDetailsNew.getStatus());
						vasDetailsValue.setVasBenefits(vasDetailsNew.getDescription1() + ", "
								+ vasDetailsNew.getDescription2() + ", " + vasDetailsNew.getDescription3());
						vasDetailsValue.setVasAmount("USD " + vasDetailsNew.getPricing());
						vasDetailsValue.setTotalAmount("USD "
								+ (pay.getSubscriptionAmount().intValue() + vasDetailsNew.getPricing().floatValue()));
						vasDetailsValue.setVasMakerComment(vasDetailsNew.getMakerComment());
						vasDetailsValue.setVasCheckerComment(vasDetailsNew.getCheckerComment());
						vasDetailsValue.setVasPaymentStatus(vasDetailsNew.getPaymentSts());
						vasDetailsValue.setVasPlanPaymentMode(vasDetailsNew.getMode());
						vasDetailsValue.setVasId(vasDetailsNew.getId());
						if (vasDetailsNew.getsPlanVasFlag() == null) {
							vasDetailsValue.setIsSplanWithVasFlag(2);
						} else if (vasDetailsNew.getsPlanVasFlag().intValue() == 0) {
							vasDetailsValue.setIsSplanWithVasFlag(vasDetailsNew.getsPlanVasFlag().intValue());
						} else {
							if (vasDetailsNew.getsPlanVasFlag().intValue() != 1)
								continue;
							vasDetailsValue.setIsSplanWithVasFlag(vasDetailsNew.getsPlanVasFlag().intValue());
						}
					}
					details.add(vasDetailsValue);
				}
				value.setVasList(details);
				System.out.println("added list" + value.getVasList().toString());
			}
			if (pay.getUserid() != null) {
				if (pay.getInvoiceId() == null) {
					value.setTransactionId("");
				} else {
					value.setTransactionId(pay.getInvoiceId());
				}
			} else {
				value.setTransactionId("");
			}
			if (pay.getDiscountId() > 0) {
				NimaiMDiscount dis = (NimaiMDiscount) this.discRepo.getOne(Integer.valueOf(pay.getDiscountId()));
				value.setCouponCode(dis.getCouponCode());
			} else {
				value.setCouponCode("");
			}
			value.setTotalAmount("USD " + (pay.getSubscriptionAmount().intValue() + pay.getVasAmount()));
			value.setAmountPaid("USD " + ((pay.getSubscriptionAmount().intValue() + pay.getVasAmount())
					- ((pay.getDiscount() == null) ? 0.0D : pay.getDiscount().doubleValue())));
			data.add(value);
		}
		return data;
	}

	public PagedResponse<?> getSearchBankDetail(SearchRequest request) {
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
		} else if (countryNames != null || request.getCountry() == null) {

		}
		List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
				.collect(Collectors.toList());
		System.out.println("Values BankService: " + value);
		if (request.getRole() != null && request.getRole().equalsIgnoreCase("Bank RM")) {
			request.setLoginUserId(Utility.getUserId());
			request.setRmStatus("Active");
		}
		Page<NimaiMCustomer> bankDetails = null;
		if (request.getRole().equalsIgnoreCase("Bank RM")) {
			String userId = Utility.getUserId();
			System.out.println("===========RM userId" + userId);
			bankDetails = retiriveListOnRMId(userId, request, value, (Pageable) pageRequest);
		} else if (request.getUserId() != null && request.getTxtStatus() == null) {
			System.out.println("************=============1.insdie request.getUserId()============***********"
					+ request.getUserId());
			bankDetails = this.repo.getBankDetailsByUser(request.getUserId(), value, (Pageable) pageRequest);
		} else if (request.getMobileNo() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============2.request.getMobileNo()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByMobile(request.getMobileNo(), value, (Pageable) pageRequest);
		} else if (request.getCountry() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============3.request.getCountry()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByCountr(request.getCountry(), value, (Pageable) pageRequest);
		} else if (request.getBankName() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============4.request.getCompanyName()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByCompanyNa(request.getBankName(), value, (Pageable) pageRequest);
		} else if (request.getEmailId() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============5.request.getEmailId()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByEmail(request.getEmailId(), value, (Pageable) pageRequest);
		} else if (request.getTxtStatus() == null || request.getTxtStatus().equalsIgnoreCase("all")) {
			bankDetails = this.repo.getOnlineAllBankKYC(value, (Pageable) pageRequest);
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
			if (request.getTxtStatus() == null) {
				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
						? collectPlanName(cust.getNimaiSubscriptionDetailsList())
						: "No Active Plan");
				if (response.getPlanOfPayments().isEmpty() || response.getPlanOfPayments() == null)
					response.setPlanOfPayments(
							"Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
			} else if (request.getTxtStatus().equalsIgnoreCase("PaymentPendingUser")) {
				if (cust.getNimaiSubscriptionDetailsList().size() != 0) {
					response.setPlanOfPayments(
							"Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
				} else {
					response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
							? collectPlanName(cust.getNimaiSubscriptionDetailsList())
							: "No Active Plan");
				}
			} else {
				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
						? collectPlanName(cust.getNimaiSubscriptionDetailsList())
						: "No Active Plan");
				if (response.getPlanOfPayments().isEmpty() || response.getPlanOfPayments() == null)
					response.setPlanOfPayments(
							"Latest Inactive_".concat(collectInPlanName(cust.getNimaiSubscriptionDetailsList())));
			}
			response.setTotalQuotes(this.quoteRepo.quoteCout(cust.getUserid()));
			List<NimaiFKyc> kycdetails = this.kycRepo.findByUserid(cust);
			if (kycdetails.size() == 0) {
				response.setKyc("Not Uploaded");
			} else {
				response.setKyc(cust.getKycStatus());
			}
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
			bankDetails = this.repo.getBankDetailsByUserIdRmId(request.getUserId(), rmId, value, pageable);
		} else if (request.getMobileNo() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============2.request.getMobileNo()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByMobileNoRmId(request.getMobileNo(), rmId, value, pageable);
		} else if (request.getCountry() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============3.request.getCountry()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByCountryRmId(request.getCountry(), rmId, value, pageable);
		} else if (request.getBankName() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============4.request.getCompanyName()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByCompanyNameRmId(request.getBankName(), rmId, value, pageable);
		} else if (request.getEmailId() != null && request.getTxtStatus() == null) {
			System.out.println(
					"************=============5.request.getEmailId()============***********" + request.getUserId());
			bankDetails = this.repo.getBankDetailsByEmailIdRmId(request.getEmailId(), rmId, value, pageable);
		} else if (request.getTxtStatus() == null || request.getTxtStatus().equalsIgnoreCase("all")) {
			bankDetails = this.repo.getAllBankKYCRmId(rmId, value, pageable);
			System.out.println("************=============6.request.getTxtStatus() == null============***********"
					+ request.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Pending")) {
			bankDetails = this.repo.getPendingBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============7.request.getTxtStatus().equalsIgnoreCase(\"Pending\") == null============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Approved")) {
			bankDetails = this.repo.getApprovedBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============8.request.getTxtStatus().equalsIgnoreCase(\"Approved\")============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Rejected")) {
			bankDetails = this.repo.getRejectedBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============10.request.getTxtStatus().equalsIgnoreCase(\"Rejected\")============***********"
							+ request

									.getUserId());
		} else if (request.getTxtStatus().equalsIgnoreCase("Not Uploaded")) {
			bankDetails = this.repo.getNotUploadBankKYCRmId(rmId, value, pageable);
			System.out.println(
					"************=============11.request.getTxtStatus().equalsIgnoreCase(\"Not Uploaded\")============***********"
							+ request

									.getUserId());
		}
		return bankDetails;
	}

	public ResponseEntity<?> kycStatusUpdate(KycBDetailResponse data) {
		NimaiFKyc chck = (NimaiFKyc) this.kycRepo.getOne(data.getKycid());
		if (chck.getApprovedMaker().equalsIgnoreCase(Utility.getUserId()))
			return new ResponseEntity(
					new ApiResponse(Boolean.valueOf(false), "You dont have the authority for this operation!!!"),
					HttpStatus.OK);
		logger.debug("updating kyc status-");
		System.out.println("Account detials" + chck.getApprovedBy());
		try {
			NimaiFKyc fkyc = (NimaiFKyc) this.kycRepo.getOne(data.getKycid());
			fkyc.setKycStatus(data.getKycStatus());
			fkyc.setApprovedBy(data.getApproverName());
			fkyc.setApprovedDate(new Date());
			fkyc.setCheckerComment(data.getApprovalReason().concat(" - " + Utility.getUserId()));
			this.kycRepo.save(fkyc);
			NimaiMCustomer customer = (NimaiMCustomer) this.repo.getOne(fkyc.getUserid().getUserid());
			NimaiMEmployee emp = this.employeeRepository.findByEmpCode(customer.getRmId());
			if (data.getKycStatus().equalsIgnoreCase("Rejected")) {
				NimaiEmailScheduler schdata = new NimaiEmailScheduler();
				if (emp != null && customer.getRmStatus().equalsIgnoreCase("Active")) {
					schdata.setrMName(emp.getEmpName());
					schdata.setUserid(customer.getUserid());
					schdata.setrMemailId(emp.getEmpEmail());
					schdata.setSubMobile(emp.getEmpMobile());
					schdata.setSubLandLine(emp.getEmpMobile());
					schdata.setUserName(customer.getFirstName());
					if (customer.getUserid().substring(0, 2).equalsIgnoreCase("RE")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_REFERRER");
						schdata.setDescription1(customer.getBusinessType());
					} else if (customer.getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_BANk");
						schdata.setDescription1(customer.getBankName());
					} else if (customer.getIsAssociated() == 0) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_CUSTOMER");
					} else {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_CUSTOMER_PARENT");
					}
				} else {
					schdata.setrMemailId("nimaitradesupport@360tf.com");
					if (customer.getUserid().substring(0, 2).equalsIgnoreCase("RE")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_REFERRER_Support");
						schdata.setDescription1(customer.getBusinessType());
					} else if (customer.getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_BANk_Support");
						schdata.setDescription1(customer.getBankName());
					} else if (customer.getIsAssociated() == 0) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_CUSTOMER_Support");
					} else {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_PA_CUSTOMER_Support");
					}
				}
				if (customer.getIsAssociated() == 1) {
					System.out.println("Account source in bankserviceImpl" + customer.getAccountSource());
					NimaiMCustomer parentCust = (NimaiMCustomer) this.repo.getOne(customer.getAccountSource());
					System.out.println("Account email in bankserviceimpl parentCust" + parentCust.getEmailAddress());
					schdata.setEmailId(parentCust.getEmailAddress());
					schdata.setEmailId(parentCust.getEmailAddress());
					schdata.setDescription3(customer.getCompanyName());
					schdata.setDescription4(parentCust.getCompanyName());
					schdata.setUserName(parentCust.getFirstName());
					schdata.setDescription2("ONE");
				} else {
					schdata.setEmailId(customer.getEmailAddress());
				}
				schdata.setKycDocName(fkyc.getKycType());
				schdata.setEmailStatus("Pending");
				Calendar cal = Calendar.getInstance();
				Date today = cal.getTime();
				schdata.setInsertedDate(today);
				this.schRepo.save(schdata);
			}
			String kycStatus = "";
			String companyName = "";
			if (customer.getKycStatus() == null) {
				kycStatus = "null";
			} else {
				kycStatus = customer.getKycStatus();
			}
			if (customer.getCompanyName() == null || customer.getCompanyName().isEmpty()) {
				companyName = "NA";
			} else {
				companyName = customer.getCompanyName();
			}
			String kycSts = "Maker Approved";
			List<NimaiKyc> listData = this.kycNRepo.findByUserid(fkyc.getUserid().getUserid());
			for (NimaiKyc kycData : listData)
				System.out.println("===============kyc data id" + kycData.getId());
			Map<String, Set<String>> result = (Map<String, Set<String>>) listData.stream().collect(Collectors
					.groupingBy(NimaiKyc::getKycType, Collectors.mapping(NimaiKyc::getKycStatus, Collectors.toSet())));
			String status = "";
			Set<String> bstat = new HashSet<>();
			Set<String> cstat = new HashSet<>();
			for (Map.Entry<String, Set<String>> entry : result.entrySet()) {
				logger.debug("Key = " + (String) entry.getKey() + ", Value = " + entry.getValue());
				if (((String) entry.getKey()).equalsIgnoreCase("Personal"))
					cstat = entry.getValue();
				if (((String) entry.getKey()).equalsIgnoreCase("Business"))
					bstat = entry.getValue();
				if (((Set) entry.getValue()).contains("Approved") && !status.equals("Pending")) {
					status = "Approved";
					System.out.println("inside approved !pending condition" + entry.getValue());
					continue;
				}
				if (bstat.contains("Rejected") && cstat.contains("Rejected")) {
					status = "Rejected";
					System.out.println("inside Rejected Rejected condition" + entry.getValue());
					continue;
				}
				if (bstat.contains("Rejected") && cstat.contains("Approved")) {
					status = "Rejected";
					System.out.println("inside Rejected Approved condition" + entry.getValue());
					continue;
				}
				if (bstat.contains("Approved") && cstat.contains("Rejected")) {
					status = "Rejected";
					System.out.println("inside Approved Rejected condition" + entry.getValue());
					continue;
				}
				status = "Pending";
				System.out.println("inside else condition" + entry.getValue());
			}
			if (status.equals("Approved")) {
				customer.setKycStatus("Approved");
				customer.setKycApprovaldate(new Date());
				NimaiEmailScheduler schdata2 = new NimaiEmailScheduler();
				schdata2.setUserid(customer.getUserid());
				schdata2.setUserName(customer.getFirstName());
				if (customer.getUserid().substring(0, 2).equalsIgnoreCase("RE")) {
					schdata2.setEvent("KYC_APPROVAL_FROMRMTO_REFERRER");
					schdata2.setDescription1(customer.getCompanyName());
				} else if (customer.getUserid().substring(0, 2).equalsIgnoreCase("BC")
						|| customer.getUserid().substring(0, 2).equalsIgnoreCase("CU")) {
					if (customer.getIsAssociated() == 0) {
						schdata2.setEvent("KYC_APPROVAL_FROMRMTO_CUSTOMER");
					} else {
						schdata2.setEvent("KYC_APPROVAL_FROMRMTO_CUSTOMER_PARENT");
					}
					if (customer.getUserid().substring(0, 2).equalsIgnoreCase("BC")) {
						schdata2.setDescription1(customer.getCompanyName());
					} else {
						schdata2.setDescription1(customer.getCompanyName());
					}
				} else if (customer.getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
					schdata2.setEvent("KYC_APPROVAL_FROMRMTO_BANk");
					schdata2.setDescription1(customer.getBankName());
				}
				if (customer.getIsAssociated() == 1) {
					System.out.println("Account source in bankserviceImpl" + customer.getAccountSource());
					NimaiMCustomer parentCust = (NimaiMCustomer) this.repo.getOne(customer.getAccountSource());
					System.out.println("Account email in bankserviceimpl parentCust" + parentCust.getEmailAddress());
					schdata2.setEmailId(parentCust.getEmailAddress());
					schdata2.setEmailId(parentCust.getEmailAddress());
					schdata2.setDescription3(customer.getCompanyName());
					schdata2.setDescription4(parentCust.getCompanyName());
					schdata2.setUserName(parentCust.getFirstName());
					schdata2.setDescription2("ONE");
					NimaiEmailScheduler accoountActivation = new NimaiEmailScheduler();
					accoountActivation.setSubUserId(customer.getUserid());
					accoountActivation.setUserid(customer.getAccountSource());
					accoountActivation.setEvent("ASSOCIATE_ACTIVATION_ALERT");
					accoountActivation.setEmailStatus("Pending");
					this.schRepo.save(accoountActivation);
				} else {
					schdata2.setEmailId(customer.getEmailAddress());
				}
				schdata2.setKycDocName(fkyc.getKycType());
				schdata2.setEmailStatus("Pending");
				String planStatus = "ACTIVE";
				Calendar cal2 = Calendar.getInstance();
				Date today2 = cal2.getTime();
				schdata2.setInsertedDate(today2);
				this.schRepo.save(schdata2);
				if (!customer.getUserid().substring(0, 2).equalsIgnoreCase("RE")
						&& !customer.getAccountType().equalsIgnoreCase("SUBSIDIARY")
						&& !customer.getAccountType().equalsIgnoreCase("BANKUSER")) {
					Calendar calNew = Calendar.getInstance();
					Date todayDate = calNew.getTime();
					NimaiSubscriptionDetails details = this.planRepo.getplanByUserID(customer.getUserid(), "ACTIVE");
					int noOfdays = 30;
					Integer validityInNumber = Integer.valueOf(details.getSubscriptionValidity());
					int actualEndDaysOfPLan = validityInNumber.intValue() * 30 - 1;
					System.out.println(actualEndDaysOfPLan);
					Calendar calforEndDate = Calendar.getInstance();
					calforEndDate.setTime(today2);
					System.out.println(today2);
					calforEndDate.add(5, actualEndDaysOfPLan);
					Date endDate = calforEndDate.getTime();
					System.out.println(endDate);
					details.setSplanStartDate(today2);
					calforEndDate.add(5, actualEndDaysOfPLan);
					details.setSplanEndDate(endDate);
					this.planRepo.save(details);
				}
				if (customer.getAccountType().equalsIgnoreCase("SUBSIDIARY") && customer.getIsAssociated() != 1) {
					NimaiEmailScheduler schData = new NimaiEmailScheduler();
					schData.setSubUserId(customer.getUserid());
					schData.setUserid(customer.getAccountSource());
					schData.setEvent("SUBSIDIARY_ACTIVATION_ALERT");
					schData.setEmailStatus("Pending");
					this.schRepo.save(schData);
				}
				if (!customer.getAccountSource().equalsIgnoreCase("WEBSITE")
						&& customer.getAccountType().equalsIgnoreCase("REFER") && status.equalsIgnoreCase("Approved")
						&& (customer.getPaymentStatus().equalsIgnoreCase("Approved") || customer.getPaymentStatus().equalsIgnoreCase("Success")) ) {
					NimaiEmailScheduler accountReferEmail = new NimaiEmailScheduler();
					accountReferEmail.setSubUserId(customer.getUserid());
					NimaiSubscriptionDetails details = this.planRepo.getplanByUserID(customer.getUserid(), "ACTIVE");
					accountReferEmail.setSubscriptionAmount(String.valueOf(details.getSubscriptionAmount()));
					if (customer.getCompanyName() == null || customer.getCompanyName().isEmpty()) {
						accountReferEmail.setDescription1("NA");
					} else {
						accountReferEmail.setDescription1(customer.getCompanyName());
					}
					
					NimaiMCustomer accounSourceDetails = (NimaiMCustomer) this.repo.getOne(customer.getAccountSource());
					accountReferEmail.setUserid(customer.getAccountSource());
					accountReferEmail.setUserName(accounSourceDetails.getFirstName());
					accountReferEmail.setEmailId(accounSourceDetails.getEmailAddress());
					accountReferEmail.setEmailStatus("Pending");
					accountReferEmail.setEvent("CUSTOMER_ACCOUNT_REFERRED");
					this.schRepo.save(accountReferEmail);
				}

				String subType="";
				if(customer.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE) && customer.getBankType().equalsIgnoreCase(AppConstants.CUBANKTYPE)) {
					subType=AppConstants.CLIENTBASUBTYPE;
				}


				if(customer.getPaymentStatus().equalsIgnoreCase("Approved") && status.equals("Approved")
						&&	(customer.getSubscriberType().equalsIgnoreCase("CUSTOMER") || subType.equalsIgnoreCase("BANK_CUSTOMER")) ) {

					String resut=postDataProcess(customer);
					System.out.println("===============Third Party Api response"+resut);
				}


			} else if (status.equals("Rejected")) {
				customer.setKycStatus("Rejected");
			} else if (customer.getKycStatus().equalsIgnoreCase("Rejected")) {
				customer.setKycStatus("Rejected");
			} else {
				customer.setKycStatus("Pending");
			}
			if (fkyc.getKycStatus().equalsIgnoreCase("Rejected"))
				customer.setKycStatus("Rejected");
			this.repo.save(customer);
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Kyc Status updated successfully....",status),
					HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Error while updating kyc status - " + e);
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Error while updating kyc status."),
					HttpStatus.BAD_REQUEST);
		}
	}
	
	public String postDataProcess(NimaiMCustomer customer) throws Exception {
		String urLdetails="";
		try {
			System.out.println("===============++++++++++postDataProcess 1");
			String countryCode;
			if(customer.getCountryName()==null || customer.getCountryName().isEmpty() || customer.getCountryName().equalsIgnoreCase(" ")) {
				countryCode=AppConstants.VALUABSENT;
			}else {
				countryCode=repo.findCountryCode(customer.getCountryName());
				if(countryCode==null || countryCode.equalsIgnoreCase(" ")) {
					countryCode=null;
				}
			}

			if(customer.getAccountType().equalsIgnoreCase(AppConstants.SUBACCTYPE))
			{
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
				//String beDetailsUrl="";
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
				//	beDetailsUrl="https://api.app.360tf.trade/user-management/v2/migration/assign/group/company/"+customer.getAccountSource();
				String beUsrApi=tPartApi.postData(requestData, beDetailsUrl);
				urLdetails=beDetailsUrl;

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
				urLdetails=url;
				String actPassword="";
				if(crUsrApi.equalsIgnoreCase(AppConstants.SUCCESSMSG)) {

					String passurl=configRepo.migUrlData(AppConstants.MIGUPDATEPASSUEL);
					//password
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
					urLdetails=passurl;
					if(paUsrApi.equalsIgnoreCase(AppConstants.SUCCESSMSG)) {
						//getMasterToken
						String tokenUrl=configRepo.migUrlData(AppConstants.MIGMASTERTOKENURL);
						MigrationTokenBean bean=new MigrationTokenBean();
						bean.setLoginTypeEnum(AppConstants.ENUMTYPE);
						bean.setUserName(customer.getUserid());
						bean.setPassword(actPassword);

						System.out.println("tokenUrl user URL 1"+tokenUrl);

						MigrationResponse tokenApi=tPartApi.sendHttpGetRequest(bean, tokenUrl);
						
						urLdetails=tokenUrl;
						System.out.println("=========token api"+tokenApi.getAccessToken());
						System.out.println("============token api"+tokenApi.getResponse());
						if(tokenApi.getAccessToken()!=null && tokenApi.getResponse().equalsIgnoreCase("Success")) {

							System.out.println("//PersonaDetails");
							//PersonalDetails
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
							urLdetails=perUsrUrl;
							if(perUsrApi.equalsIgnoreCase(AppConstants.SUCCESSMSG)) {
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

								String beUsrApi=tPartApi.postDataWithToken(requestData, beDetailsUrl,tokenApi.getAccessToken());
								
								urLdetails=beDetailsUrl;
								//String beDetailsUrl="https://api.devapp.360tf.trade/user-management/v2/details/business";
							}
						}
					}
					return AppConstants.SUCCESSMSG;
				}
			}
			phaseTwoTransferFailData(customer,urLdetails);
			return AppConstants.FAILMSG;
		}catch(Exception e) {
			System.out.println("INside catch block==========");
			e.printStackTrace();
			phaseTwoTranferFailData(customer,urLdetails,e.getMessage());
			return AppConstants.FAILMSG;
		}


	}

	public PagedResponse<?> getBankQuoteList(SearchRequest request) {
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc")
						? Sort.by(new String[] { request.getSortBy() }).descending()
						: Sort.by(new String[] { request.getSortBy() }).ascending());
		Page<NimaiMQuotation> quoteDetails = this.quoteRepo
				.findAll(this.quotationSpecification.quotationFilter(request), (Pageable) pageRequest);
		List<QuotationListResponse> responses = quoteDetails.map(quote -> {
			QuotationListResponse response = new QuotationListResponse();
			
			response.setQuotationId(quote.getQuotationId());
			response.setBankUserid(quote.getBankUserid());
			response.setUserId(quote.getUserid().getUserid());
			response.setMobile(quote.getMobileNumber());
			response.setEmail(quote.getEmailAddress());
			response.setBeneficiary(quote.getTransactionId().getBeneName());
			response.setCountry(quote.getTransactionId().getBeneCountry());
			response.setTransactionId(quote.getTransactionId().getTransactionId());
			response.setInsertedDate(quote.getInsertedDate());
			response.setValidityDate(quote.getValidityDate());
			response.setIb(quote.getTransactionId().getLcIssuanceBank());
			response.setAmount(quote.getTransactionId().getLcValue() + "");
			response.setCcy(quote.getTransactionId().getLcCurrency());
			if(quote.getIsOffered()==null || quote.getIsOffered().trim().equalsIgnoreCase("")|| quote.getIsOffered().equalsIgnoreCase("null")) {
				response.setTotalQuoteValue(String.valueOf(quote.getTotalQuoteValue()));
			}else {
				response.setTotalQuoteValue(quote.getTransactionId().getOfferedPrice());
			}

			response.setQuoteCcy(quote.getTransactionId().getLcCurrency());
			response.setRequirement(quote.getTransactionId().getRequirementType());
			response.setTrxnStatus(quote.getQuotationStatus());
			return response;
		}).getContent();
		return new PagedResponse(responses, quoteDetails.getNumber(), quoteDetails.getSize(),
				quoteDetails.getTotalElements(), quoteDetails.getTotalPages(), quoteDetails.isLast());
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

	public ResponseEntity<?> makerKycStatusUpdate(KycBDetailResponse data) {
		logger.debug("updating kyc status - Maker " + data.getApproverName());
		try {
			NimaiEmailScheduler schdata = new NimaiEmailScheduler();
			NimaiFKyc fkyc = (NimaiFKyc) this.kycRepo.getOne(data.getKycid());
			NimaiMCustomer customer = (NimaiMCustomer) this.repo.getOne(fkyc.getUserid().getUserid());
			if (data.getKycStatus().equalsIgnoreCase("Maker Rejected")) {
				NimaiMCustomer cust = (NimaiMCustomer) this.repo.getOne(fkyc.getUserid().getUserid());
				NimaiMEmployee emp = this.employeeRepository.findByEmpCode(cust.getRmId());
				if (emp == null || cust.getRmStatus().equalsIgnoreCase("Pending")) {
					schdata.setrMemailId("nimaitradesupport@360tf.com");
					schdata.setDescription1("ContactPersonDetails");
					if (cust.getUserid().substring(0, 2).equalsIgnoreCase("RE")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_REFERRER_Support");
						schdata.setDescription2(cust.getBusinessType());
					} else if (cust.getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_BANk_Support");
						schdata.setDescription2(cust.getBankName());
					} else if (customer.getIsAssociated() == 0) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_CUSTOMER_Support");
					} else {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_PA_CUSTOMER_Support");
					}
				} else {
					schdata.setrMName(emp.getEmpName());
					schdata.setUserid(cust.getUserid());
					schdata.setrMemailId(emp.getEmpEmail());
					schdata.setSubMobile(emp.getEmpMobile());
					schdata.setSubLandLine(emp.getEmpMobile());
					if (cust.getUserid().substring(0, 2).equalsIgnoreCase("RE")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_REFERRER");
						schdata.setDescription1(cust.getBusinessType());
					} else if (cust.getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_BANk");
						schdata.setDescription1(cust.getBankName());
					} else if (customer.getIsAssociated() == 0) {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_CUSTOMER");
					} else {
						schdata.setEvent("KYC_REJECTION_FROMRMTO_CUSTOMER_PARENT");
					}
				}
				if (customer.getIsAssociated() == 1) {
					System.out.println("Account source in bankserviceImpl" + customer.getAccountSource());
					NimaiMCustomer parentCust = (NimaiMCustomer) this.repo.getOne(customer.getAccountSource());
					System.out.println("Account email in bankserviceimpl parentCust" + parentCust.getEmailAddress());
					schdata.setEmailId(parentCust.getEmailAddress());
					schdata.setEmailId(parentCust.getEmailAddress());
					schdata.setDescription3(customer.getCompanyName());
					schdata.setDescription4(parentCust.getCompanyName());
					schdata.setUserName(parentCust.getFirstName());
					schdata.setDescription2("ONE");
				} else {
					schdata.setUserName(cust.getFirstName());
					schdata.setEmailId(customer.getEmailAddress());
				}
				schdata.setKycDocName(fkyc.getKycType());
				schdata.setEmailStatus("Pending");
				Calendar cal = Calendar.getInstance();
				Date today = cal.getTime();
				schdata.setInsertedDate(today);
				this.schRepo.save(schdata);
				cust.setKycStatus("Rejected");
				this.repo.save(cust);
			}
			fkyc.setComment(data.getComment().concat(" -  " + data.getApproverName()));
			fkyc.setReason(data.getReason());
			fkyc.setKycStatus(data.getKycStatus());
			fkyc.setApprovedMaker(data.getApproverName());
			fkyc.setMakerApprovedDate(new Date());
			String sourceName = " ";
			if (data.getSourceDetails() != null && !data.getSourceDetails().isEmpty()) {
				System.out.println("Inside if condition sourcedetailslist");
				List<SourceDetailsList> detailsList = data.getSourceDetails();
				for (SourceDetailsList listItems : detailsList) {
					System.out.println("Inside if condition sourcedetailslist" + listItems.getSourceDtails());
					sourceName = listItems.getSourceDtails();
					customer.setSourceDetails(listItems.getSourceDtails());
				}
			}
			fkyc.setSourceDetails(sourceName);
			this.kycRepo.save(fkyc);
			String kycStatus = "";
			if (customer.getKycStatus() == null) {
				kycStatus = "null";
			} else {
				kycStatus = customer.getKycStatus();
			}
			List<NimaiKyc> listData = this.kycNRepo.findByUseridDesc(fkyc.getUserid().getUserid());
			System.out.println("=============result in makerKycStatus update==========" + listData.toString());
			Map<String, Set<String>> result = (Map<String, Set<String>>) listData.stream().collect(Collectors
					.groupingBy(NimaiKyc::getKycType, Collectors.mapping(NimaiKyc::getKycStatus, Collectors.toSet())));
			String status = "";
			Set<String> bstat = new HashSet<>();
			Set<String> cstat = new HashSet<>();
			for (Map.Entry<String, Set<String>> entry : result.entrySet()) {
				logger.debug("Key = " + (String) entry.getKey() + ", Value = " + entry.getValue());
				if (((String) entry.getKey()).equalsIgnoreCase("Personal"))
					cstat = entry.getValue();
				if (((String) entry.getKey()).equalsIgnoreCase("Business"))
					bstat = entry.getValue();
				if (((Set) entry.getValue()).contains("Maker Approved") && !status.equals("Pending")) {
					status = "Pending";
					continue;
				}
				if (bstat.contains("Maker Rejected") && cstat.contains("Maker Rejected")) {
					status = "Rejected";
					continue;
				}
				if (bstat.contains("Maker Rejected") && cstat.contains("Maker Approved")) {
					status = "Rejected";
					continue;
				}
				if (bstat.contains("Maker Approved") && cstat.contains("Maker Rejected")) {
					status = "Rejected";
					continue;
				}
				status = "Pending";
			}
			if (status.equals("Approved")) {
				customer.setKycStatus("Approved");
				customer.setKycApprovaldate(new Date());
			} else if (status.equals("Rejected")) {
				customer.setKycStatus("Rejected");
			} else {
				customer.setKycStatus("Pending");
			}
			if (fkyc.getKycStatus().equalsIgnoreCase("Maker Rejected"))
				customer.setKycStatus("Rejected");
			if (customer.getRmDetails() == null || customer.getRmDetails().isEmpty()
					|| customer.getRmDetails().equalsIgnoreCase("")) {
				customer.setRmDetails(data.getRmDetails());
			} else {
				customer.setRmDetails(customer.getRmDetails());
			}

			this.repo.save(customer);
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Kyc Status updated successfully..."),
					HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Error while updating kyc status - " + e);
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(false), "Error while updating kyc status."),
					HttpStatus.BAD_REQUEST);
		}
	}

	public PagedResponse<?> getMakerApprovedKyc(SearchRequest request) {
		//Page<NimaiFKyc> kycDetails;
		Page<NimaiOptimizedKyc> kycDetails;
	
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc")
						? Sort.by(new String[] { request.getSortBy() }).descending()
						: Sort.by(new String[] { request.getSortBy() }).ascending());
		String countryNames = Utility.getUserCountry();
		logger.info("countryNames: " + countryNames);
		if (countryNames != null && countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
			countryNames = "";
			List<String> countryList = this.repo.getCountryList();
			for (String country : countryList)
				countryNames = countryNames + country + ",";
			logger.info("Country List: " + countryNames);
			request.setCountryNames(countryNames);
		} else if (countryNames != null && !countryNames.equalsIgnoreCase("all") && request.getCountry() == null) {
			request.setCountryNames(countryNames);
		} else if (countryNames != null || request.getCountry() == null) {

		}
		List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
				.collect(Collectors.toList());
		logger.info("Values BankService: " + value);
		logger.info("subsType: " + request.getSubscriberType());
		logger.info("bankType: " + request.getBankType());
		try {
			if ((request.getSubscriberType().equalsIgnoreCase("CUSTOMER")
					|| request.getSubscriberType().equalsIgnoreCase("REFERRER")) && request.getBankType() == null) {
				kycDetails = this.optKycRepo.findCustomerReferrerMakerApprovedKycByCountries(value,
						request.getSubscriberType(), (Pageable) pageRequest);
		//		kycDetail = this.kycRepo.findTCustomerReferrerMakerApprovedKycByCountries(value,
				//		request.getSubscriberType(), (Pageable) pageRequest);
			} else if ((request.getSubscriberType().equalsIgnoreCase("BANK")
					&& request.getBankType().equalsIgnoreCase("UNDERWRITER"))
					|| (request.getSubscriberType().equalsIgnoreCase("BANK")
							&& request.getBankType().equalsIgnoreCase("CUSTOMER"))) {
				kycDetails = this.optKycRepo.findMakerApprovedKycByCountriesSubsTypeBankType(value,
						request.getSubscriberType(), request.getBankType(), (Pageable) pageRequest);
//				
				//kycDetail = this.kycRepo.findTMakerApprovedKycByCountriesSubsTypeBankType(value,
				//		request.getSubscriberType(), request.getBankType(), (Pageable) pageRequest);
			} else if ((request.getSubscriberType() == null && request.getBankType() == null)
					|| (request.getSubscriberType().isEmpty() && request.getBankType().isEmpty())
					|| (request.getSubscriberType().equalsIgnoreCase("null")
							&& request.getBankType().equalsIgnoreCase("null"))) {
				logger.info("Getting value by clicking on TAB");
				kycDetails = this.optKycRepo.findMakerApprovedKycByCountries(value, (Pageable) pageRequest);
				//kycDetail = this.kycRepo.findTMakerApprovedKycByCountries(value, (Pageable) pageRequest);
				
			} else {
				logger.info("Getting value by clicking on TAB");
				kycDetails = this.optKycRepo.findMakerApprovedKycByCountries(value, (Pageable) pageRequest);
				//kycDetail = this.kycRepo.findTMakerApprovedKycByCountries(value, (Pageable) pageRequest);
				
			}
		} catch (Exception e) {
			logger.info("=========== In CATCH 2============");
			//kycDetails = this.optKycRepo.findMakerApprovedKycByCountries(value, (Pageable) pageRequest);
			kycDetails = this.optKycRepo.findMakerApprovedKycByCountries(value, (Pageable) pageRequest);
			
		}
		

System.out.println("kyc details size "+kycDetails.getSize());
		List<KycBDetailResponse> responses = kycDetails.map(kyc -> {
			KycBDetailResponse response = new KycBDetailResponse();
			response.setKycid(kyc.getId());
			response.setDocName(kyc.getDocumentName());
			response.setCountry(kyc.getCountry());
			response.setKycType(kyc.getKycType());
			response.setDocType(kyc.getDocumentType());
			response.setReason(kyc.getComment());
			response.setKycStatus(kyc.getKycStatus());
			response.setUserid(kyc.getUserid().getUserid());
			response.setApproverName(kyc.getApprovedMaker());
			response.setRm(kyc.getUserid().getRmId());
			response.setRmStatus(kyc.getUserid().getRmStatus());
			return response;
		}).getContent();
		
		
		return new PagedResponse(responses, kycDetails.getNumber(), kycDetails.getSize(), kycDetails.getTotalElements(),
				kycDetails.getTotalPages(), kycDetails.isLast());
	}



	public ResponseEntity<?> wireTranferStatusUpdate(VasUpdateRequestBody request) {
		NimaiMCustomer customer2 = this.repo.findByUserid(request.getUserId());
		if (customer2.getPaymentApprovedBy() != null
				&& customer2.getPaymentApprovedBy().equalsIgnoreCase(request.getUserId()))
			return new ResponseEntity(
					new ApiResponse(Boolean.valueOf(false), "You dont have the authority for this operation!!!"),
					HttpStatus.OK);
		try {
			if (request.getVasNUmber() == null || request.getVasNUmber().isEmpty()) {
				customer2.setPaymentApprovedBy(Utility.getUserId());
				customer2.setPaymentStatus(request.getStatus());
				customer2.setPaymentDate(new Date());
				this.repo.save(customer2);
				NimaiEmailScheduler schedulerData = new NimaiEmailScheduler();
				NimaiSubscriptionDetails details = (NimaiSubscriptionDetails) this.planRepo
						.getOne(Integer.valueOf(request.getSubcriptionId()));
				if (request.getMakerComment() != null)
					details.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
				if (request.getCheckerComment() != null)
					details.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));
				details.setPaymentStatus(request.getStatus());
				if (details.getVasAmount() != 0) {
					List<NimaiSubscriptionVas> vasUpdate = this.vasRep
							.getVasListDetailsBySplanId(details.getSubscriptionId(), details.getUserid().getUserid());
					for (NimaiSubscriptionVas vasDetails2 : vasUpdate) {
						vasDetails2.setPaymentSts(request.getStatus());
						if (request.getMakerComment() != null)
							vasDetails2.setMakerComment(request.getMakerComment().concat(" -  " + Utility.getUserId()));
						if (request.getCheckerComment() != null)
							vasDetails2.setCheckerComment(
									request.getCheckerComment().concat(" -  " + Utility.getUserId()));
						this.vasRep.save(vasDetails2);
					}
				}
				if (request.getStatus().equalsIgnoreCase("Rejected")) {
					schedulerData.setUserid(customer2.getUserid());
					schedulerData.setUserName(customer2.getFirstName());
					schedulerData.setEmailId(customer2.getEmailAddress());
					schedulerData.setEvent("Cust_Splan_email_Wire_Rejected");
					schedulerData.setSubscriptionId(details.getSubscriptionId());
					schedulerData.setSubscriptionAmount(String.valueOf(details.getSubscriptionAmount()));
					schedulerData.setSubscriptionName(details.getSubscriptionName());
					schedulerData.setSubscriptionValidity(details.getSubscriptionValidity());
					schedulerData.setRelationshipManager(details.getRelationshipManager());
					schedulerData.setCustomerSupport(details.getCustomerSupport());
					schedulerData.setSubscriptionStartDate(details.getSplanStartDate());
					schedulerData.setSubscriptionEndDate(details.getSplanEndDate());
					schedulerData.setEmailStatus("Pending");
					schedulerData.setInsertedDate(new Date());
					this.schRepo.save(schedulerData);
				}
				if (request.getStatus().equalsIgnoreCase("Approved")
						&& details.getUserid().getPaymentStatus().equalsIgnoreCase("Approved")) {
					logger.debug("Inside subscriptionplan approved status");
					logger.debug("subscriptionplan:" + request.getStatus());
					schedulerData.setUserid(customer2.getUserid());
					schedulerData.setUserName(customer2.getFirstName());
					schedulerData.setEmailId(customer2.getEmailAddress());
					schedulerData.setEvent("Cust_Splan_email_Wire");
					schedulerData.setSubscriptionId(details.getSubscriptionId());
					schedulerData.setSubscriptionAmount(String.valueOf(details.getSubscriptionAmount()));
					schedulerData.setSubscriptionName(details.getSubscriptionName());
					schedulerData.setSubscriptionValidity(details.getSubscriptionValidity());
					schedulerData.setRelationshipManager(details.getRelationshipManager());
					schedulerData.setCustomerSupport(details.getCustomerSupport());
					schedulerData.setSubscriptionStartDate(details.getSplanStartDate());
					schedulerData.setSubscriptionEndDate(details.getSplanEndDate());
					Calendar cal = Calendar.getInstance();
					Date today = cal.getTime();
					int noOfdays = 30;
					int validityInNumber = Integer.valueOf(details.getSubscriptionValidity()).intValue();
					int actualEndDaysOfPLan = validityInNumber * 30 - 1;
					logger.debug(String.valueOf(actualEndDaysOfPLan));
					Calendar calforEndDate = Calendar.getInstance();
					calforEndDate.setTime(today);
					calforEndDate.add(5, actualEndDaysOfPLan);
					Date endDate = calforEndDate.getTime();
					details.setSplanStartDate(today);
					calforEndDate.add(5, actualEndDaysOfPLan);
					details.setSplanEndDate(endDate);
					this.planRepo.save(details);
					schedulerData.setEmailStatus("Pending");
					schedulerData.setInsertedDate(new Date());
					this.schRepo.save(schedulerData);
					logger.debug("details to be save of subscription plan" + details.toString());
					
					String subType="";
					if(customer2.getSubscriberType().equalsIgnoreCase(AppConstants.BATYPE) && customer2.getBankType().equalsIgnoreCase("UNDERWRITER")) {
						subType=AppConstants.CLIENTSUBTYPE;
					}else {
						subType=customer2.getSubscriberType();
					}
				
					if (!customer2.getAccountSource().equalsIgnoreCase("WEBSITE") && request.getStatus().equalsIgnoreCase("Approved")
							&& customer2.getAccountType().equalsIgnoreCase("REFER") 
							&& (customer2.getKycStatus().equalsIgnoreCase("Approved")
									|| customer2.getKycStatus().equalsIgnoreCase("SUCCESS"))) {
						NimaiEmailScheduler accountReferEmail = new NimaiEmailScheduler();
						accountReferEmail.setSubUserId(customer2.getUserid());
						NimaiSubscriptionDetails detail = this.planRepo.getplanByUserID(customer2.getUserid(), "ACTIVE");
						accountReferEmail.setSubscriptionAmount(String.valueOf(details.getSubscriptionAmount()));
						if (customer2.getCompanyName() == null || customer2.getCompanyName().isEmpty()) {
							accountReferEmail.setDescription1("NA");
						} else {
							accountReferEmail.setDescription1(customer2.getCompanyName());
						}
						
						NimaiMCustomer accounSourceDetails = (NimaiMCustomer) this.repo.getOne(customer2.getAccountSource());
						accountReferEmail.setUserid(customer2.getAccountSource());
						accountReferEmail.setUserName(accounSourceDetails.getFirstName());
						accountReferEmail.setEmailId(accounSourceDetails.getEmailAddress());
						accountReferEmail.setEmailStatus("Pending");
						accountReferEmail.setEvent("CUSTOMER_ACCOUNT_REFERRED");
						this.schRepo.save(accountReferEmail);
					}
					if(request.getStatus().equalsIgnoreCase("Approved")
							&& details.getUserid().getPaymentStatus().equalsIgnoreCase("Approved")
							&&	(customer2.getSubscriberType().equalsIgnoreCase("CUSTOMER") || subType.equalsIgnoreCase("BANK_CUSTOMER"))	   
							&& (customer2.getKycStatus().equalsIgnoreCase("Approved")
									|| customer2.getKycStatus().equalsIgnoreCase("SUCCESS"))) {
						
						
						String resut=postDataProcess(customer2);
						System.out.println("===============Third Party Api response"+resut);
					}
					
				}
				this.planRepo.save(details);
			} else if (request.getVasNUmber() != null) {
				String vasId = request.getVasNUmber();
				String[] vasIdArray = vasId.split("-");
				for (String str : vasIdArray) {

					System.out.println(str);
					logger.debug("VASID:" + Integer.parseInt(str));
					NimaiSubscriptionVas vasDetails = this.vasRep.getOne(Integer.valueOf(Integer.parseInt(str)));
					NimaiSubscriptionDetails details = this.planRepo.getOne(vasDetails.getsPLanSerialNumber());
					if (!details.getPaymentStatus().equalsIgnoreCase("Approved"))
						updateSPlanAndVasStatus(request, customer2, details);
					logger.debug("vasDetails:" + vasDetails.toString());
					if (vasDetails.getPaymentApprovedBy() != null
							&& vasDetails.getPaymentApprovedBy().equalsIgnoreCase(request.getUserId()))
						return new ResponseEntity(new ApiResponse(Boolean.valueOf(false),
								"You dont have the authority for this operation!!!"), HttpStatus.OK);
					if (request.getVasMakerComment() != null)
						vasDetails.setMakerComment(request.getVasMakerComment().concat(" -  " + Utility.getUserId()));
					if (request.getVasCheckerComment() != null)
						vasDetails
								.setCheckerComment(request.getVasCheckerComment().concat(" -  " + Utility.getUserId()));
					vasDetails.setPaymentApprovalDate(new Date());
					vasDetails.setPaymentSts(request.getStatus());
					vasDetails.setPaymentApprovedBy(Utility.getUserId());
					this.vasRep.save(vasDetails);
					if (request.getStatus().equalsIgnoreCase("Maker Rejected")
							|| request.getStatus().equalsIgnoreCase("Rejected")) {
						String planstatus = "ACTIVE";
						NimaiSubscriptionDetails sDetails = this.planRepo.getplanByUserIDAndSID(vasDetails.getUserId(),
								planstatus, vasDetails.getSubscriptionId());
						Double vasDeductAmt = Double
								.valueOf(sDetails.getGrandAmount().doubleValue() - sDetails.getVasAmount());
						DecimalFormat f = new DecimalFormat("##.00");
						System.out.println(f.format(vasDeductAmt));
						sDetails.setGrandAmount(Double.valueOf(f.format(vasDeductAmt)));
						this.planRepo.save(sDetails);
					}
					NimaiEmailScheduler vaSchData = new NimaiEmailScheduler();
					NimaiMCustomer customer = this.repo.findByUserid(vasDetails.getUserId());
					if (request.getStatus().equalsIgnoreCase("Rejected")) {
						vaSchData.setUserid(vasDetails.getUserId());
						vaSchData.setUserName(customer.getFirstName());
						vaSchData.setDescription1(vasDetails.getPlanName());
						vaSchData.setSubscriptionId(vasDetails.getSubscriptionId());
						vaSchData.setEmailId(customer.getEmailAddress());
						vaSchData.setEmailStatus("Pending");
						vaSchData.setEvent("VAS_PLAN_WIRE_REJECTED");
					}
					if (request.getStatus().equalsIgnoreCase("Approved")) {
						vaSchData.setUserid(vasDetails.getUserId());
						vaSchData.setUserName(customer.getFirstName());
						vaSchData.setEmailId(customer.getEmailAddress());
						vaSchData.setSubscriptionId(vaSchData.getSubscriptionId());
						vaSchData.setSubscriptionName(vasDetails.getPlanName());
						vaSchData.setEmailStatus("Pending");
						vaSchData.setEvent("VAS_PLAN_WIRE_APPROVED");
						vaSchData.setSubscriptionAmount(String.valueOf(vasDetails.getPricing()));
					}
					this.schRepo.save(vaSchData);
				}
			}
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Payment status updated successfully... "),
					HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("Exception in Payment status update :" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Error due to some technical issue"),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private void updateSPlanAndVasStatus(VasUpdateRequestBody request, NimaiMCustomer customer2,
			NimaiSubscriptionDetails details) {
		customer2.setPaymentApprovedBy(Utility.getUserId());
		customer2.setPaymentStatus(request.getStatus());
		customer2.setPaymentDate(new Date());
		this.repo.save(customer2);
		NimaiEmailScheduler schedulerData = new NimaiEmailScheduler();
		if (request.getMakerComment() != null)
			details.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
		if (request.getCheckerComment() != null)
			details.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));
		details.setPaymentStatus(request.getStatus());
		if (request.getStatus().equalsIgnoreCase("Rejected")) {
			schedulerData.setUserid(customer2.getUserid());
			schedulerData.setUserName(customer2.getFirstName());
			schedulerData.setEmailId(customer2.getEmailAddress());
			schedulerData.setEvent("Cust_Splan_email_Wire_Rejected");
			schedulerData.setSubscriptionId(details.getSubscriptionId());
			schedulerData.setSubscriptionAmount(String.valueOf(details.getSubscriptionAmount()));
			schedulerData.setSubscriptionName(details.getSubscriptionName());
			schedulerData.setSubscriptionValidity(details.getSubscriptionValidity());
			schedulerData.setRelationshipManager(details.getRelationshipManager());
			schedulerData.setCustomerSupport(details.getCustomerSupport());
			schedulerData.setSubscriptionStartDate(details.getSplanStartDate());
			schedulerData.setSubscriptionEndDate(details.getSplanEndDate());
			schedulerData.setEmailStatus("Pending");
			schedulerData.setInsertedDate(new Date());
			this.schRepo.save(schedulerData);
		}
		if (request.getStatus().equalsIgnoreCase("Approved")
				&& details.getUserid().getPaymentStatus().equalsIgnoreCase("Approved")) {
			logger.debug("Inside subscriptionplan approved status");
			logger.debug("subscriptionplan:" + request.getStatus());
			schedulerData.setUserid(customer2.getUserid());
			schedulerData.setUserName(customer2.getFirstName());
			schedulerData.setEmailId(customer2.getEmailAddress());
			schedulerData.setEvent("Cust_Splan_email_Wire");
			schedulerData.setSubscriptionId(details.getSubscriptionId());
			schedulerData.setSubscriptionAmount(String.valueOf(details.getSubscriptionAmount()));
			schedulerData.setSubscriptionName(details.getSubscriptionName());
			schedulerData.setSubscriptionValidity(details.getSubscriptionValidity());
			schedulerData.setRelationshipManager(details.getRelationshipManager());
			schedulerData.setCustomerSupport(details.getCustomerSupport());
			schedulerData.setSubscriptionStartDate(details.getSplanStartDate());
			schedulerData.setSubscriptionEndDate(details.getSplanEndDate());
			Calendar cal = Calendar.getInstance();
			Date today = cal.getTime();
			int noOfdays = 30;
			int validityInNumber = Integer.valueOf(details.getSubscriptionValidity()).intValue();
			int actualEndDaysOfPLan = validityInNumber * 30 - 1;
			logger.debug(String.valueOf(actualEndDaysOfPLan));
			Calendar calforEndDate = Calendar.getInstance();
			calforEndDate.setTime(today);
			calforEndDate.add(5, actualEndDaysOfPLan);
			Date endDate = calforEndDate.getTime();
			details.setSplanStartDate(today);
			calforEndDate.add(5, actualEndDaysOfPLan);
			details.setSplanEndDate(endDate);
			this.planRepo.save(details);
			schedulerData.setEmailStatus("Pending");
			schedulerData.setInsertedDate(new Date());
			this.schRepo.save(schedulerData);
			logger.debug("details to be save of subscription plan" + details.toString());
		}
		this.planRepo.save(details);
	}

	private void updateVasDetails(String vasNUmber, VasUpdateRequestBody request) {
		String vasId = request.getVasNUmber();
		String[] vasIdArray = vasId.split("-");
		for (String str : vasIdArray) {
			System.out.println(str);
			logger.debug("VASID:" + Integer.parseInt(str));
			NimaiSubscriptionVas vasDetails = (NimaiSubscriptionVas) this.vasRep
					.getOne(Integer.valueOf(Integer.parseInt(str)));
			logger.debug("vasDetails:" + vasDetails.toString());
			if (request.getVasMakerComment() != null)
				vasDetails.setMakerComment(request.getVasMakerComment().concat(" -  " + Utility.getUserId()));
			if (request.getVasCheckerComment() != null)
				vasDetails.setCheckerComment(request.getVasCheckerComment().concat(" -  " + Utility.getUserId()));
			vasDetails.setPaymentApprovalDate(new Date());
			vasDetails.setPaymentSts(request.getStatus());
			vasDetails.setPaymentApprovedBy(Utility.getUserId());
			this.vasRep.save(vasDetails);
			if (request.getStatus().equalsIgnoreCase("Maker Rejected")
					|| request.getStatus().equalsIgnoreCase("Rejected")) {
				String planstatus = "ACTIVE";
				NimaiSubscriptionDetails sDetails = this.planRepo.getplanByUserIDAndSID(vasDetails.getUserId(),
						planstatus, vasDetails.getSubscriptionId());
				Double vasDeductAmt = Double.valueOf(sDetails.getGrandAmount().doubleValue() - sDetails.getVasAmount());
				DecimalFormat f = new DecimalFormat("##.00");
				System.out.println(f.format(vasDeductAmt));
				sDetails.setGrandAmount(Double.valueOf(f.format(vasDeductAmt)));
				this.planRepo.save(sDetails);
			}
			NimaiEmailScheduler vaSchData = new NimaiEmailScheduler();
			NimaiMCustomer customer = this.repo.findByUserid(vasDetails.getUserId());
			if (request.getStatus().equalsIgnoreCase("Rejected")) {
				vaSchData.setUserid(vasDetails.getUserId());
				vaSchData.setUserName(customer.getFirstName());
				vaSchData.setDescription1(vasDetails.getPlanName());
				vaSchData.setSubscriptionId(vasDetails.getSubscriptionId());
				vaSchData.setEmailId(customer.getEmailAddress());
				vaSchData.setEmailStatus("Pending");
				vaSchData.setEvent("VAS_PLAN_WIRE_REJECTED");
			}
			if (request.getStatus().equalsIgnoreCase("Approved")) {
				vaSchData.setUserid(vasDetails.getUserId());
				vaSchData.setUserName(customer.getFirstName());
				vaSchData.setEmailId(customer.getEmailAddress());
				vaSchData.setSubscriptionId(vaSchData.getSubscriptionId());
				vaSchData.setSubscriptionName(vasDetails.getPlanName());
				vaSchData.setEmailStatus("Pending");
				vaSchData.setEvent("VAS_PLAN_WIRE_APPROVED");
				vaSchData.setSubscriptionAmount(String.valueOf(vasDetails.getPricing()));
			}
			this.schRepo.save(vaSchData);
		}
	}

	@SuppressWarnings("unchecked")
	public PagedResponse<?> getWireTransferList(SearchRequest request) {
		Page<NimaiMCustomer> paymentDetails;
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { "PAYMENT_DATE" }).descending()
						: Sort.by(new String[] { "PAYMENT_DATE" }).ascending());
		String countryNames = Utility.getUserCountry();
		int pageSize = request.getSize();
		if (request.getSubscriberType() != null || request.getBankType() != null) {
			if (request.getCountry() == null || request.getCountry().isEmpty()) {
				request.setCountryNames(countryNames);
				if (request.getCountryNames().equalsIgnoreCase("all")) {
					if (request.getSubscriberType().equalsIgnoreCase("CUSTOMER") && request.getBankType() == null) {
						paymentDetails = this.repo.findAllMakerApprovedPaymentDetailsSubsType(
								request.getSubscriberType(), (Pageable) pageRequest);
					} else {
						paymentDetails = this.repo.findAllMakerApprovedPaymentDetailsSubsTypeBankType(
								request.getSubscriberType(), request.getBankType(), (Pageable) pageRequest);
					}
				} else {
					List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
							.collect(Collectors.toList());
					if (request.getSubscriberType().equalsIgnoreCase("CUSTOMER") && request.getBankType() == null) {
						paymentDetails = this.repo.findMakerApprovedPaymentDetailsSubsType(request.getSubscriberType(),
								value, (Pageable) pageRequest);
					} else {
						paymentDetails = this.repo.findMakerApprovedPaymentDetailsSubsTypeBankType(
								request.getSubscriberType(), request.getBankType(), value, (Pageable) pageRequest);
					}
				}
			} else {
				if (request.getCountry().equalsIgnoreCase("all")) {
					request.setCountryNames(countryNames);
					if (request.getCountryNames().equalsIgnoreCase("all")) {
						paymentDetails = this.repo.findAllMakerApprovedPaymentDetailsSubsTypeBankType(
								request.getSubscriberType(), request.getBankType(), (Pageable) pageRequest);
					} else {
						List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
								.collect(Collectors.toList());
						paymentDetails = this.repo.findMakerApprovedPaymentDetailsSubsTypeBankType(
								request.getSubscriberType(), request.getBankType(), value, (Pageable) pageRequest);
					}
				} else {
					paymentDetails = this.repo.getListByCountryname(request.getCountry(), (Pageable) pageRequest);
				}
				request.setCountryNames(request.getCountry());
			}
		} else if (request.getCountry() == null || request.getCountry().isEmpty()) {
			request.setCountryNames(countryNames);
			if (request.getCountryNames().equalsIgnoreCase("all")) {
				paymentDetails = this.repo.findAllMakerApprovedPaymentDetails((Pageable) pageRequest);
			} else {
				List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
						.collect(Collectors.toList());
				paymentDetails = this.repo.findMakerApprovedPaymentDetails(value, (Pageable) pageRequest);
			}
		} else {
			if (request.getCountry().equalsIgnoreCase("all")) {
				request.setCountryNames(countryNames);
				if (request.getCountryNames().equalsIgnoreCase("all")) {
					paymentDetails = this.repo.findAllMakerApprovedPaymentDetails((Pageable) pageRequest);
				} else {
					List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
							.collect(Collectors.toList());
					paymentDetails = this.repo.findMakerApprovedPaymentDetails(value, (Pageable) pageRequest);
				}
			} else {
				paymentDetails = this.repo.getListByCountryname(request.getCountry(), (Pageable) pageRequest);
			}
			request.setCountryNames(request.getCountry());
		}
		System.out.println("==============countrynames 2" + countryNames);
		System.out.println("==============countrynames 2" + request.getCountryNames());
		System.out.println("payment detials size" + paymentDetails.getSize());
		System.out.println("payment detials size" + paymentDetails.getSize());
		// final BankDetailsResponse response;
		// final NimaiSubscriptionDetails sub;
		// final NimaiSubscriptionVas vasDetails;
		List<BankDetailsResponse> responses = paymentDetails.map(cust -> {
			BankDetailsResponse response = new BankDetailsResponse();
			// sub =
			// cust.getNimaiSubscriptionDetailsList().stream().filter().findFirst().orElse(null);
			NimaiSubscriptionDetails sub = (NimaiSubscriptionDetails) cust.getNimaiSubscriptionDetailsList().stream()
					.filter(e -> e.getStatus().equalsIgnoreCase("Active")).findFirst().orElse(null);

			if (sub != null) {
				response.setUserid(cust.getUserid());
				response.setFirstName(cust.getFirstName());
				response.setLastName(cust.getLastName());
				response.setEmailAddress(cust.getEmailAddress());
				response.setMobileNumber(cust.getMobileNumber());
				response.setRegisteredCountry(cust.getRegisteredCountry());
				response.setStatus(cust.getPaymentStatus());
				response.setPaymentApprovedBy(cust.getPaymentApprovedBy());
				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
						? collectPlanName(cust.getNimaiSubscriptionDetailsList())
						: "No Active Plan");
				response.setPlanId(sub.getSplSerialNumber().intValue());
				response.setMakerComment(sub.getMakerComment());
				response.setCheckerComment(sub.getCheckerComment());
				response.setPaymentMode(sub.getPaymentMode());
				if (sub.getVasAmount() != 0) {
					NimaiSubscriptionVas vasDetails = this.vasRep.getVasDetailsBySplanId(sub.getSubscriptionId(),
							sub.getUserid().getUserid());
					if (vasDetails == null) {
						response.setVasMakerComment((String) null);
						response.setVasCheckerComment((String) null);
						response.setVasStatus((String) null);
					} else {
						response.setVasMakerComment(sub.getMakerComment());
						response.setVasCheckerComment(sub.getCheckerComment());
						response.setVasStatus(vasDetails.getPaymentSts());
					}
				}
			}
			return response;
		}).getContent();
		return new PagedResponse(responses, paymentDetails.getNumber(), paymentDetails.getSize(),
				paymentDetails.getTotalElements(), paymentDetails.getTotalPages(), paymentDetails.isLast());
	}

	public KycBDetailResponse getMakerApprovedKycByKycId(SearchRequest request) {
		NimaiFKyc kyc = (NimaiFKyc) this.kycRepo.getOne(Integer.valueOf(request.getKycId()));
		KycBDetailResponse response = new KycBDetailResponse();
		response.setKycid(kyc.getId());
		response.setDocName(kyc.getDocumentName());
		response.setCountry(kyc.getCountry());
		response.setKycType(kyc.getKycType());
		response.setDocType(kyc.getDocumentType());
		response.setReason(kyc.getReason());
		response.setKycStatus(kyc.getKycStatus());
		response.setEncodedFileContent(
				kyc.getEncodedFileContent().substring(kyc.getEncodedFileContent().indexOf("|") + 1));
		response.setUserid(kyc.getUserid().getUserid());
		response.setApproverName(kyc.getApprovedMaker());
		return response;
	}

	public ResponseEntity<?> checkDuplicateCouponCode(CouponBean request) {
		NimaiMDiscount discountDetails = this.discRepo.getDetailsByCoupon(request.getCouponCode(),
				request.getCountryName(), "ACTIVE", request.getCustomerType());
		if (discountDetails != null)
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Coupon already exist"), HttpStatus.OK);
		return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Success"), HttpStatus.OK);
	}

	public ResponseEntity<?> checkDuplicateSPLan(SPlanBean request) {
		return null;
	}

	public PagedResponse<?> getVasWireTransferList(SearchRequest request) {
		Page<NimaiSubscriptionVas> vasDetails;
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc")
						? Sort.by(new String[] { request.getSortBy() }).descending()
						: Sort.by(new String[] { request.getSortBy() }).ascending());
		String countryNames = Utility.getUserCountry();
		if (request.getCountry() == null || request.getCountry().isEmpty()) {
			request.setCountryNames(countryNames);
			if (request.getCountryNames().equalsIgnoreCase("all")) {
				vasDetails = this.vasRep.getAllVasListByCountryname(countryNames, (Pageable) pageRequest);
			} else {
				List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
						.collect(Collectors.toList());
				vasDetails = this.vasRep.findMakerApprovedVasDetails(value, (Pageable) pageRequest);
			}
		} else {
			if (request.getCountry().equalsIgnoreCase("all")) {
				request.setCountryNames(countryNames);
				if (request.getCountryNames().equalsIgnoreCase("all")) {
					vasDetails = this.vasRep.getAllVasListByCountryname(countryNames, (Pageable) pageRequest);
				} else {
					List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
							.collect(Collectors.toList());
					vasDetails = this.vasRep.findMakerApprovedVasDetails(value, (Pageable) pageRequest);
				}
			} else {
				vasDetails = this.vasRep.getVasListByCountryname(request.getCountry(), (Pageable) pageRequest);
			}
			request.setCountryNames(request.getCountry());
		}
		System.out.println("======================countryNames=======" + countryNames);
		for (NimaiSubscriptionVas vas : vasDetails) {
			if (vas.getPricing() == null)
				vas.setPricing(Float.valueOf(0.0F));
		}
		List<BankDetailsResponse> varesponses = vasDetails.map(vascust -> {
			BankDetailsResponse response = new BankDetailsResponse();
			List<NimaiSubscriptionVas> vasList = this.vasRep.findVasByUserId(vascust.getUserId());
			List<BankDetailsResponse> responseList = new ArrayList<>();
			NimaiSubscriptionVas vasSingle = this.vasRep.getVasDetailsBySplanId(vascust.getSubscriptionId(),
					vascust.getUserId());
			String vasIdResponse = "";
			List<String> vasIdString = new ArrayList<>();
			for (NimaiSubscriptionVas vasNew : vasList)
				vasIdString.add(String.valueOf(vasNew.getId()).concat("-"));
			String[] array = new String[vasIdString.size()];
			vasIdString.toArray(array);
			StringBuilder sb = new StringBuilder();
			for (String str : (String[]) vasIdString.<String>toArray(array)) {
				sb.append(str);
				sb.substring(0, sb.length() - 1);
			}
			StringBuilder vasNumberString = sb;
			response.setUserid(vascust.getUserId());
			System.out.println("user id for nullpointer exception" + vascust.getUserId());
			try {
				NimaiMCustomer customerDetails = this.repo.findByUserid(vascust.getUserId());
				if (customerDetails == null) {
					response.setFirstName("");
					response.setLastName("");
					response.setEmailAddress("");
					response.setMobileNumber("");
				} else {
					response.setFirstName(customerDetails.getFirstName());
					response.setLastName(customerDetails.getLastName());
					response.setEmailAddress(customerDetails.getEmailAddress());
					response.setMobileNumber(customerDetails.getMobileNumber());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			response.setVasMakerComment(vasSingle.getMakerComment());
			response.setVasCheckerComment(vasSingle.getCheckerComment());
			response.setPlanOfPayments((vasList.size() != 0) ? collectVasPlanName(vasList) : "No Active Plan");
			response.setRegisteredCountry(vasSingle.getCountryName());
			response.setPlanId(vasSingle.getId().intValue());
			response.setStatus(vasSingle.getPaymentSts());
			response.setPaymentApprovedBy(vasSingle.getPaymentApprovedBy());
			response.setVasNUmber(vasNumberString.toString());
			responseList.add(response);
			return response;
		}).getContent();
		return new PagedResponse(varesponses, vasDetails.getNumber(), vasDetails.getSize(),
				vasDetails.getTotalElements(), vasDetails.getTotalPages(), vasDetails.isLast());
	}

	static {
		logger = LoggerFactory.getLogger(BankServiceImpl.class);
	}

	@SuppressWarnings("rawtypes")
	public ResponseEntity<?> transactionStatusUpdate(TransactionRequestBody request) {
		NimaiLCMaster lcDetais = (NimaiLCMaster) this.lcMasterrepo.getOne(request.getTransactionId());
		if (lcDetais.getTransactionApprovedBy() != null
				&& lcDetais.getTransactionApprovedBy().equalsIgnoreCase(Utility.getUserId()))
			return new ResponseEntity(
					new ApiResponse(Boolean.valueOf(false), "You dont have the authority for this operation!!!"),
					HttpStatus.OK);
		lcDetais.setTrApprovaldate(new Date());
		if (request.getStatus().equalsIgnoreCase("Approved")) {
			System.out.println("INside approved condition");
			lcDetais.setTrApprovaldate(new Date());
			lcDetais.setTransactionApprovedBy(Utility.getUserId());
			lcDetais.setTransactionStatus("Active");
			getAlleligibleBAnksEmail(lcDetais.getUserId(), request.getTransactionId(), 0, "LC_UPLOAD_ALERT_ToBanks",
					"LC_UPLOAD(DATA)");
		} else {
			lcDetais.setTransactionRejected(Utility.getUserId());
			lcDetais.setTrRejectedDate(new Date());
			lcDetais.setTransactionStatus(request.getStatus());
			try {
				NimaiSubscriptionDetails details = this.sPlanRepo.getplanByUserID(lcDetais.getUserId(), "ACTIVE");
				int count = details.getLcUtilizedCount() - 1;
				details.setLcUtilizedCount(count);
				this.sPlanRepo.save(details);
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity(
						new ApiResponse(Boolean.valueOf(true), "Error while updating Transaction status... "),
						HttpStatus.OK);
			}
		}
		if (request.getMakerComment() != null) {
			lcDetais.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
			lcDetais.setStatusReason(request.getMakerComment());
		}
		if (request.getCheckerComment() != null) {
			lcDetais.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));
			lcDetais.setStatusReason(request.getCheckerComment());
		}
		this.lcMasterrepo.save(lcDetais);
		return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Transaction status updated successfully... "),
				HttpStatus.OK);
	}

	public ResponseEntity<?> makerTransactionStatusUpdate(TransactionRequestBody request) {
		NimaiLCMaster lcDetais = (NimaiLCMaster) this.lcMasterrepo.getOne(request.getTransactionId());
		lcDetais.setTransactionStatus(request.getStatus());
		lcDetais.setTrApprovaldate(new Date());
		if (request.getStatus().equalsIgnoreCase("Maker Approved")) {
			lcDetais.setTrApprovaldate(new Date());
			lcDetais.setTransactionApprovedBy(Utility.getUserId());
		} else {
			lcDetais.setTransactionRejected(Utility.getUserId());
			lcDetais.setTrRejectedDate(new Date());
			try {
				NimaiSubscriptionDetails details = this.sPlanRepo.getplanByUserID(lcDetais.getUserId(), "ACTIVE");
				int count = details.getLcUtilizedCount() - 1;
				details.setLcUtilizedCount(count);
				this.sPlanRepo.save(details);
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity(
						new ApiResponse(Boolean.valueOf(true), "Error while updating Transaction status... "),
						HttpStatus.OK);
			}
		}
		if (request.getMakerComment() != null) {
			lcDetais.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
			lcDetais.setStatusReason(request.getMakerComment());
		}
		if (request.getCheckerComment() != null) {
			lcDetais.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));
			lcDetais.setStatusReason(request.getCheckerComment());
		}
		lcDetais.setTransactionStatus(request.getStatus());
		this.lcMasterrepo.save(lcDetais);
		return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Transaction status updated successfully... "),
				HttpStatus.OK);
	}

	public void getAlleligibleBAnksEmail(String userId, String transactionId, int quoteId, String bankEmailEvent,
			String custEmailEvent) {
		System.out.println("======== Getting eligible bank ========");
		EntityManager entityManager = this.em.createEntityManager();
		try {
			StoredProcedureQuery getBAnksEmail = entityManager.createStoredProcedureQuery("get_eligible_banks",
					new Class[] { NimaiMCustomer.class });
			getBAnksEmail.registerStoredProcedureParameter("inp_customer_userID", String.class, ParameterMode.IN);
			getBAnksEmail.registerStoredProcedureParameter("inp_transaction_ID", String.class, ParameterMode.IN);
			getBAnksEmail.setParameter("inp_customer_userID", userId);
			getBAnksEmail.setParameter("inp_transaction_ID", transactionId);
			getBAnksEmail.execute();
			List<NimaiMCustomer> nimaiCust = getBAnksEmail.getResultList();
			List<EligibleEmailList> emailId = (List<EligibleEmailList>) nimaiCust.stream().map(obj -> {
				EligibleEmailList data = new EligibleEmailList();
				NimaiEmailSchedulerAlertToBanks schedulerEntity = new NimaiEmailSchedulerAlertToBanks();
				Calendar cal = Calendar.getInstance();
				System.out.println("=========Bank list to send emil" + nimaiCust.toString());
				Date insertedDate = cal.getTime();
				schedulerEntity.setInsertedDate(insertedDate);
				schedulerEntity.setCustomerid(userId);
				System.out.println("Userid:" + userId);
				schedulerEntity.setTransactionid(transactionId);
				schedulerEntity.setEmailEvent(bankEmailEvent);
				schedulerEntity.setBanksEmailID(obj.getEmailAddress());
				schedulerEntity.setBankUserid(obj.getUserid());
				schedulerEntity.setBankUserName(obj.getFirstName());
				schedulerEntity.setEmailFlag("Pending");
				this.userDao.save(schedulerEntity);
				data.setEmailList(obj.getEmailAddress());
				return data;
			}).collect(Collectors.toList());
			if (nimaiCust.isEmpty())
				System.out.println("No Banks Eligible");
			Calendar cal = Calendar.getInstance();
			Date insertedDate = cal.getTime();
			NimaiEmailSchedulerAlertToBanks schedulerEntityCust = new NimaiEmailSchedulerAlertToBanks();
			NimaiLCMaster passcodeDetails = this.lcMasterrepo.findSpecificTransactionById(transactionId);
			NimaiMCustomer custDetails = this.lcMasterrepo.getCustomerDetais(passcodeDetails.getBranchUserEmail());
			if (custDetails == null)
				schedulerEntityCust.setPasscodeuserEmail(passcodeDetails.getBranchUserEmail());
			String custUserName = this.lcMasterrepo.getCustomerName(userId);
			String custEmailId = this.lcMasterrepo.getCustomerEmailId(userId);
			schedulerEntityCust.setInsertedDate(insertedDate);
			schedulerEntityCust.setQuotationId(Integer.valueOf(quoteId));
			schedulerEntityCust.setCustomerid(userId);
			schedulerEntityCust.setCustomerUserName((custUserName == null) ? "" : custUserName);
			schedulerEntityCust.setCustomerEmail((custEmailId == null) ? "" : custEmailId);
			schedulerEntityCust.setTransactionid(transactionId);
			schedulerEntityCust.setEmailEvent(custEmailEvent);
			if (nimaiCust.isEmpty()) {
				System.out.println("No Banks Eligible");
				schedulerEntityCust.setTransactionEmailStatusToBanks("Pending");
			}
			schedulerEntityCust.setEmailFlag("Pending");
			this.userDao.save(schedulerEntityCust);
		} catch (Exception e) {
			System.out.println("" + e.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResponseEntity<?> kycFiledSave(KycFiledBean data) {
		GenericResponse response = new GenericResponse();
		// String response;
		NimaiMKycfileds fields;
		Calendar cal2 = Calendar.getInstance();
		Date today2 = cal2.getTime();
		System.out.println("Field ID: " + data.getId());
		System.out.println("Field ID: " + data.getUserId());
		fields = this.fieldRepo.getFidData(data.getUserId());
		if (fields == null) {
			NimaiMKycfileds field = new NimaiMKycfileds();
			field.setInsertedDate(today2);
			field.setInsertedBy(data.getUserId());
			field.setCustTurnover(data.getCustTurnover());
			field.setImportVolume(data.getImportVolume());
			field.setExportVolume(data.getExportVolume());
			field.setYearlyLCVolume(data.getYearlyLCVolume());
			field.setUsedLCIssuance(data.getUsedLCIssuance());
			field.setUserId(data.getUserId());
			this.fieldRepo.save(field);
			response.setMessage("fieldData save successfully... ");
		} else {
			fields = this.fieldRepo.getOne(fields.getId());
			fields.setModifiedDat(today2);
			fields.setModifiedBy(Utility.getUserId());
			response.setMessage("fieldData updated successfully... ");

			fields.setCustTurnover(data.getCustTurnover());
			fields.setImportVolume(data.getImportVolume());
			fields.setExportVolume(data.getExportVolume());
			fields.setYearlyLCVolume(data.getYearlyLCVolume());
			fields.setUsedLCIssuance(data.getUsedLCIssuance());
			fields.setUserId(data.getUserId());
			this.fieldRepo.save(fields);
		}
		// return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), response,
		// fields.getId()), HttpStatus.OK);
		return new ResponseEntity(response, HttpStatus.OK);

	}

	public ResponseEntity<?> kycViewFieldData(KycFiledBean data) {
		GenericResponse response = new GenericResponse();
		KycFiledBean dataResponse = new KycFiledBean();
		String userID = " ";
		if (data.getCustUserId() == null || data.getCustUserId().isEmpty()) {
			userID = data.getUserId();
		} else {
			userID = data.getCustUserId();
		}
		NimaiMKycfileds fields = this.fieldRepo.getFidData(userID);
		if (fields == null) {
			try {
				logger.info("filedl data " + fields.getId());
			} catch (Exception e) {

				response.setMessage("Field Data not available");
			}

		} else {
			logger.info("filedl data " + fields.getId());
			data.setCustTurnover(fields.getCustTurnover());
			data.setImportVolume(fields.getImportVolume());
			data.setExportVolume(fields.getExportVolume());
			data.setYearlyLCVolume(fields.getYearlyLCVolume());
			data.setUsedLCIssuance(fields.getUsedLCIssuance());
			data.setCustUserId(fields.getUserId());
			data.setInsertedBy(fields.getInsertedBy());
			data.setModifiedby(fields.getModifiedBy());
			data.setId(fields.getId());
			response.setData(data);
		}

		return new ResponseEntity(response, HttpStatus.OK);
	}

	public static void main(String[] args) {
		String number = "23_24_26_";
		String[] arr = number.split("_");
		for (String str : arr)
			System.out.println(str);
		System.out.println(arr.toString());
	}

	public List<PreferredBankListResponse> getBankList() {
		List<NimaiMCustomer> cust = this.repo.findBankDetails();
		if (cust.size() != 0 && cust != null)
			return (List<PreferredBankListResponse>) cust.stream()
					.map(res -> new PreferredBankListResponse(res.getUserid(), res.getFirstName(), res.getLastName(),
							res.getEmailAddress(), res.getBankName() + ", " + res.getRegisteredCountry()))

					.collect(Collectors.toList());
		return null;
	}

	public ResponseEntity<?> createOrUpdatePreferredBank(@Valid PreferredBankRequest request) {
		this.preferBankRepo.deletePreferredBank(request.getCustUserId());
		for (int i = 0; i < (request.getBanks()).length; i++) {
			PreferredBankModel preferBank = new PreferredBankModel();
			preferBank.setCustUserId(request.getCustUserId());
			preferBank.setBankUserId(request.getBanks()[i]);
			preferBank.setIsUploaded(Integer.valueOf(0));
			this.preferBankRepo.save(preferBank);
		}
		return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Preferred Bank Added Successfully..."),
				HttpStatus.CREATED);
	}

	public List<PreferredBankListResponse> viewPreferredBanks(@Valid PreferredBankRequest request) {
		List<PreferredBankListResponse> prBankList = new ArrayList<>();
		List<PreferredBankModel> cust = this.preferBankRepo.findBankDetails(request.getCustUserId());
		for (PreferredBankModel pm : cust) {
			PreferredBankListResponse pr = new PreferredBankListResponse();
			NimaiMCustomer custDetails = this.repo.custDetails(pm.getBankUserId());
			if (custDetails == null) {
				pr.setBankName(pm.getBankUserId());
				pr.setCustUserid(pm.getCustUserId());
			} else {
				pr.setUserid(pm.getBankUserId());
				pr.setCustUserid(pm.getCustUserId());
				pr.setBankName(custDetails.getBankName() + ", " + custDetails.getRegisteredCountry());
			}
			prBankList.add(pr);
		}
		return prBankList;
	}

	public ResponseEntity<?> createOrUpdateBankRating(@Valid BankRatingRequest request) {
		BankRatingModel br = this.bankratingRepo.getDetailsByBankUserId(request.getBankUserid());
		if (br == null) {
			BankRatingModel brNew = new BankRatingModel();
			brNew.setBankUserid(request.getBankUserid());
			brNew.setRating(request.getRating());
			this.bankratingRepo.save(brNew);
		} else {
			BankRatingModel brOld = (BankRatingModel) this.bankratingRepo.getOne(br.getId());
			brOld.setBankUserid(request.getBankUserid());
			brOld.setRating(request.getRating());
			this.bankratingRepo.save(brOld);
		}
		return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Bank Rated Successfully..."),
				HttpStatus.CREATED);
	}

	public ResponseEntity<?> viewBankRatingDetails(@Valid BankRatingRequest request) {
		GenericResponse response = new GenericResponse();
		BankRatingModel brm = this.bankratingRepo.getDetailsByBankUserId(request.getBankUserid());
		response.setData(brm);
		return new ResponseEntity(response, HttpStatus.OK);
	}

//	public ResponseEntity<?> createOrUpdateUploadedPreferredBankList(String userId, MultipartFile file)
//			throws IOException {
//		GenericResponse response = new GenericResponse();
//		Integer j = Integer.valueOf(0), result = Integer.valueOf(0);
//		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
//		System.out.println("Extension: " + extension);
//		if (!extension.equalsIgnoreCase("xlsx"))
//			return new ResponseEntity(
//					new ApiResponse(Boolean.valueOf(false), "Invalid file. Kindly Upload correct file"), HttpStatus.OK);
//		Boolean validateEx = Boolean.valueOf(UploadedPreferredBankExcelHelper.validateExcel(file.getInputStream()));
//		if (validateEx.booleanValue()) {
//			List<PreferredBankModel> preferredBankList;
//			System.out.println("Validating excel: " + validateEx);
//			try {
//				preferredBankList = UploadedPreferredBankExcelHelper.extractInfo(userId, file.getInputStream());
//				
//				if(preferredBankList.size()==0 || preferredBankList.isEmpty() ) {
//					response.setMessage("Mandatory fields(Bank name and Country) cannot be Blank");
//					return new ResponseEntity(response, HttpStatus.OK);
//				}
//			} catch (Exception w) {
//				return new ResponseEntity(
//						new ApiResponse(Boolean.valueOf(false), "Parse Error! Could not able to Upload Excel"),
//						HttpStatus.OK);
//			}
//			System.out.println("List of Preferred Bank: " + preferredBankList);
//		
//			List<PreferredBankModel> details = this.preferBankRepo.findAll();
//			System.out.println("Details: " + details);
//			System.out.println("Checking Bank for Duplicate......");
//			List<PreferredBankModel> preferDuplicateList = new ArrayList<>();
//			Set<PreferredBankModel> items = new HashSet<>();
//			Set<PreferredBankModel> duplicates = new HashSet<>();
//			try {
//				for (PreferredBankModel pre : preferredBankList) {
//					Integer integer1 = j, integer2 = j = Integer.valueOf(j.intValue() + 1);
//					System.out.println(userId + " " + pre.getBankName() + " " + pre.getBankCountry());
//					for (Integer i = j; i.intValue() < preferredBankList
//							.size(); integer1 = i, integer2 = i = Integer.valueOf(i.intValue() + 1)) {
//						System.out.println(
//								"=====" + ((PreferredBankModel) preferredBankList.get(i.intValue())).getBankName()
//										+ "====" + pre.getBankName());
//						if (((PreferredBankModel) preferredBankList.get(i.intValue())).getBankName()
//								.equalsIgnoreCase(pre.getBankName())
//								&& ((PreferredBankModel) preferredBankList.get(i.intValue())).getBankCountry()
//										.equalsIgnoreCase(pre.getBankCountry()))
//							result = Integer.valueOf(1);
//					}
//					if (pre.getBankName() == null || pre.getBankName() == "" || pre.getBankCountry() == null
//							|| pre.getBankCountry() == "")
//						result = Integer.valueOf(2);
//				}
//			} catch (NullPointerException ne) {
//				result = Integer.valueOf(2);
//				System.out.println("Exception: " + ne);
//			}
//			if (result.intValue() == 0) {
//				this.preferBankRepo.deleteUploadedPreferredBank(userId);
//				this.preferBankRepo.saveAll(preferredBankList);
//				return new ResponseEntity(response, HttpStatus.OK);
//			}
//			if (result.intValue() == 2)
//				return new ResponseEntity(new ApiResponse(Boolean.valueOf(false),
//						"Mandatory fields(Bank name and Country) cannot be Blank"), HttpStatus.OK);
//			
//			return new ResponseEntity(new ApiResponse(Boolean.valueOf(false),
//					"Duplicate Data available in file. Please check and upload again"), HttpStatus.OK);
//		}
//		return new ResponseEntity(
//				new ApiResponse(Boolean.valueOf(false), "Parse Error! Could not able to Upload Excel"), HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<?> createOrUpdateUploadedPreferredBankList(String userId, MultipartFile file)
			throws IOException {
		// TODO Auto-generated method stub
		GenericResponse response = new GenericResponse();
		List<PreferredBankModel> preferredBankList;
		Integer i, j = 0, result = 0;
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		System.out.println("Extension: " + extension);
		if (!extension.equalsIgnoreCase("xlsx")) {
			return new ResponseEntity<>(new ApiResponse(false, "Invalid file. Kindly Upload correct file"),
					HttpStatus.OK);
		}
		Boolean validateEx = UploadedPreferredBankExcelHelper.validateExcel(file.getInputStream());
		if (validateEx) {
			System.out.println("Validating excel: " + validateEx);
			try {
				preferredBankList = UploadedPreferredBankExcelHelper.extractInfo(userId, file.getInputStream());

				if (preferredBankList.size() == 0 || preferredBankList.isEmpty()) {
					response.setMessage("Mandatory fields(Bank name and Country) cannot be Blank");
					return new ResponseEntity(response, HttpStatus.OK);
				}
			} catch (Exception w) {
				return new ResponseEntity<>(new ApiResponse(false, "Parse Error! Could not able to Upload Excel"),
						HttpStatus.OK);
			}
			System.out.println("List of Preferred Bank: " + preferredBankList);
			List<PreferredBankModel> details = preferBankRepo.findAll();
			System.out.println("Details: " + details);
			System.out.println("Checking Bank for Duplicate......");
			// PreferredBankModel
			// preDuplicate=preferBankRepo.findUploadedBankDetailsForDuplict();
			PreferredBankModel preDuplicateData;
			List<PreferredBankModel> preferDuplicateList = new ArrayList<PreferredBankModel>();
			// System.out.println("Static data: "+preDuplicate);
			Set<PreferredBankModel> items = new HashSet<PreferredBankModel>();
			Set<PreferredBankModel> duplicates = new HashSet<PreferredBankModel>();
			try {
				for (PreferredBankModel pre : preferredBankList) {
					j++;
					System.out.println(userId + " " + pre.getBankName() + " " + pre.getBankCountry());
					for (i = j; i < preferredBankList.size(); i++) {
						System.out
								.println("=====" + preferredBankList.get(i).getBankName() + "====" + pre.getBankName());
						if (preferredBankList.get(i).getBankName().equalsIgnoreCase(pre.getBankName())
								&& preferredBankList.get(i).getBankCountry().equalsIgnoreCase(pre.getBankCountry()))
							result = 1;

					}
					if (pre.getBankName() == null || pre.getBankName() == "" || pre.getBankCountry() == null
							|| pre.getBankCountry() == "")
						result = 2;
				}
			} catch (NullPointerException ne) {
				result = 2;
				System.out.println("Exception: " + ne);
			}
			/*
			 * Set<PreferredBankModel> s= new HashSet<PreferredBankModel>();
			 * s.addAll(preferredBankList); preferredBankList = new
			 * ArrayList<PreferredBankModel>(); preferredBankList.addAll(s);
			 * System.out.println("Set Data: "+preferredBankList);
			 */
			/*
			 * if(!preferDuplicateList.isEmpty()) { result=1; }
			 */
			if (result == 0) {
				preferBankRepo.deleteUploadedPreferredBank(userId);
				preferBankRepo.saveAll(preferredBankList);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else if (result == 2) {
				return new ResponseEntity<>(
						new ApiResponse(false, "Mandatory fields(Bank name and Country) cannot be Blank"),
						HttpStatus.OK);
			} else {
				// response.setData("Duplicate Data was Uploaded.");
				// return new ResponseEntity<>(response, HttpStatus.OK);
				return new ResponseEntity<>(
						new ApiResponse(false, "Duplicate Data available in file. Please check and upload again"),
						HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(new ApiResponse(false, "Parse Error! Could not able to Upload Excel"),
					HttpStatus.OK);
		}
	}

	private static PreferredBankModel getPreferredBankInList(List<PreferredBankModel> list, String bankName,
			String country) {
		return list.stream().filter(t -> t.getBankName().equalsIgnoreCase(bankName))
				.filter(t -> t.getBankCountry().equalsIgnoreCase(country)).findFirst().orElse(null);
	}

	public List<?> viewUploadedPreferredBanks(@Valid PreferredBankRequest request) {
		List<PreferredBankListResponse> prUploadedBankList = new ArrayList<>();
		List<PreferredBankModel> custDet = this.preferBankRepo.findUploadedBankDetails(request.getCustUserId());
		for (PreferredBankModel pum : custDet) {
			PreferredBankListResponse pr1 = new PreferredBankListResponse();
			pr1.setUserid(pum.getBankUserId());
			pr1.setCustUserid(pum.getCustUserId());
			pr1.setBankName(pum.getBankName());
			pr1.setCountry(pum.getBankCountry());
			pr1.setContactPersonName(pum.getBankContactPersonName());
			pr1.setContactPersonContactNo(pum.getBankContactPersonMobNo());
			pr1.setContactPersonEmailId(pum.getBankContactPersonEmailId());
			pr1.setIsUploaded(pum.getIsUploaded());
			prUploadedBankList.add(pr1);
		}
		return prUploadedBankList;
	}

	public List<?> viewMasterRating(String agency) {
		List<MasterRatingPayload> prBankList = new ArrayList<>();
		List<MasterRating> ratingDet = this.ratingRepo.getRatings(agency);
		for (MasterRating pm : ratingDet) {
			MasterRatingPayload pr = new MasterRatingPayload();
			pr.setId(pm.getId());
			pr.setAgency(pm.getAgency());
			pr.setScale(pm.getScale());
			prBankList.add(pr);
		}
		return prBankList;
	}

	public List<?> viewAgency() {
		return this.ratingRepo.getAgency();
	}

	public List<?> getSourceDetailsList() {
		List<NimaiMSource> cust = this.sourceRepo.findAll();
		if (cust.size() != 0 && cust != null)
			return (List) cust.stream()
					.map(res -> new SorceDetailsReponseBean(res.getSourceId(), res.getSourceDetails()))

					.collect(Collectors.toList());
		return null;
	}

	public ResponseEntity<?> saveSourceDetails(SorceDetailsReponseBean data) {
		return null;
	}

	@Override
	public ResponseEntity<?> wireTranferStatusUpdatePostPaid(VasUpdateRequestBody request) {
		NimaiMCustomer customer2 = this.repo.findByUserid(request.getUserId());
		NimaiPostpaidSubscriptionDetailsUpd pstPaidDetails = this.postPaidRepoUpd.getOne(request.getSubcriptionId());
		String userId=" ";
		if(customer2.getAccountType().equalsIgnoreCase("SUBSIDIARY")) {
			userId=customer2.getAccountSource();
		}else {
			userId=request.getUserId();
		}
		List<NimaiSubscriptionVas> vasList = this.vasRep.findVasByUserId(userId);
		System.out.println("Insode checker approval"+vasList.size());
		if (pstPaidDetails.getPaymentApprovedBy() != null
				&& pstPaidDetails.getPaymentApprovedBy().equalsIgnoreCase(request.getUserId()))
			return new ResponseEntity(
					new ApiResponse(Boolean.valueOf(false), "You dont have the authority for this operation!!!"),
					HttpStatus.OK);
		try {
			if (pstPaidDetails.getDueType().equalsIgnoreCase("totalDue")) {
				String vasId = request.getPostPaidIdString();
				String[] vasIdArray = vasId.split("-");
				for (String str : vasIdArray) {
					NimaiPostpaidSubscriptionDetailsUpd pstPaidDetail = this.postPaidRepoUpd.getOne(Integer.valueOf(str));

					if (request.getMakerComment() != null) {
						pstPaidDetail.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
						// details.setMakerComment(request.getMakerComment().concat("_" +
						// Utility.getUserId()));

					} else if (request.getCheckerComment() != null) {
						// details.setCheckerComment(request.getCheckerComment().concat("_" +
						// Utility.getUserId()));

						pstPaidDetail.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));

					}
					pstPaidDetail.setPaymentStatus(request.getStatus());
					pstPaidDetail.setPaymentApprovedBy(Utility.getUserId());
					this.postPaidRepoUpd.save(pstPaidDetail);

				}

			}
			
			NimaiEmailScheduler schedulerData = new NimaiEmailScheduler();
			NimaiSubscriptionDetails details = (NimaiSubscriptionDetails) this.planRepo
					.getOne(pstPaidDetails.getSubscriptionDetailsId());
			Integer lcUCnt=details.getLcUtilizedCount();

			if (request.getMakerComment() != null) {
				pstPaidDetails.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
				details.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
				if (request.getStatus().equalsIgnoreCase("Maker Approved")) {
					System.out.println("inside for maker approved comment");
					if (!details.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")) {
						
						NimaiMCustomer grCoUser = this.repo.findByUserid(pstPaidDetails.getUserId());
						
						if(grCoUser.getAccountType().equalsIgnoreCase("SUBSIDIARY")||grCoUser.getAccountType().equalsIgnoreCase("BANKUSER")) {
							grCoUser.setPaymentApprovedBy(Utility.getUserId());
							grCoUser.setPaymentDate(new Date());
							this.repo.save(grCoUser);
						}else {
							customer2.setPaymentApprovedBy(Utility.getUserId());
							customer2.setPaymentStatus("Maker Approved");
							customer2.setPaymentDate(new Date());
							this.repo.save(customer2);
						}
					}else {
	NimaiMCustomer grCoUser = this.repo.findByUserid(pstPaidDetails.getUserId());	
						if(grCoUser.getAccountType().equalsIgnoreCase("SUBSIDIARY")||grCoUser.getAccountType().equalsIgnoreCase("BANKUSER")) {
							grCoUser.setPaymentApprovedBy(Utility.getUserId());
							grCoUser.setPaymentDate(new Date());
							this.repo.save(grCoUser);
						}else {
							customer2.setPaymentDate(new Date());
							customer2.setPaymentApprovedBy(Utility.getUserId());
							this.repo.save(customer2);
						}
					}
					// List<NimaiSubscriptionVas> vasList =
					// this.vasRep.findVasByUserId(request.getUserId());
					if (vasList.size() > 0) {
						for (NimaiSubscriptionVas vasPlan : vasList) {
							vasPlan.setPaymentSts(request.getStatus());
							vasPlan.setPaymentApprovalDate(new Date());
							vasPlan.setMakerComment(request.getMakerComment().concat("_" + Utility.getUserId()));
							vasRepo.save(vasPlan);
						}
					}
				}
			}

			else if (request.getCheckerComment() != null) {
System.out.println("Insode checker approval");
				List<String> vasIdString = new ArrayList<>();
				if (request.getStatus().equalsIgnoreCase("Approved")) {
					if (!details.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")) {
						customer2.setPaymentApprovedBy(Utility.getUserId());
						customer2.setPaymentStatus("Approved");
						customer2.setPaymentDate(new Date());
						this.repo.save(customer2);
					}else {
						customer2.setPaymentDate(new Date());
						customer2.setPaymentApprovedBy(Utility.getUserId());
						this.repo.save(customer2);
					}
					System.out.println("Insode checker approval"+vasList.size());
					if (vasList.size() > 0) {
						for (NimaiSubscriptionVas vasPlan : vasList) {
							System.out.println("Insode checker approval"+vasPlan.getId());
							vasPlan.setPaymentSts(request.getStatus());
							vasPlan.setPaymentApprovalDate(new Date());
							vasPlan.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));
							vasRepo.save(vasPlan);
						}
					}
				}

				details.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));
				pstPaidDetails.setCheckerComment(request.getCheckerComment().concat("_" + Utility.getUserId()));
			}
			details.setPaymentStatus(request.getStatus());
			pstPaidDetails.setPaymentStatus(request.getStatus());
			pstPaidDetails.setPaymentApprovedBy(Utility.getUserId());
			
			if (request.getStatus().equalsIgnoreCase("Rejected")) {
				schedulerData.setUserid(customer2.getUserid());
				schedulerData.setUserName(customer2.getFirstName());
				schedulerData.setEmailId(customer2.getEmailAddress());
				schedulerData.setEvent("Cust_SplanPost_email_Wire_Rejected");
				schedulerData.setSubscriptionId("POSTPAID_PLAN");
				schedulerData.setSubscriptionAmount(String.valueOf(pstPaidDetails.getTotalPayment()));
				schedulerData.setSubscriptionName("POSTPAID_PLAN");
				schedulerData.setSubscriptionValidity(details.getSubscriptionValidity());
				schedulerData.setRelationshipManager(details.getRelationshipManager());
				schedulerData.setCustomerSupport(details.getCustomerSupport());
				schedulerData.setSubscriptionStartDate(details.getSplanStartDate());
				schedulerData.setSubscriptionEndDate(details.getSplanEndDate());
				schedulerData.setPostPaidId(pstPaidDetails.getPostpaidId());
				schedulerData.setSubsriptionDetailsId(details.getSplSerialNumber());
				schedulerData.setEmailStatus("Pending");
				schedulerData.setInsertedDate(new Date());
				this.schRepo.save(schedulerData);
				System.out.println("going to update..... "+details.getSplSerialNumber());
				details.setDiscountId(0);
				details.setDiscount(0.0D);

				pstPaidDetails.setDueType(null);
				pstPaidDetails.setDisountId(0);
				pstPaidDetails.setDiscountAmnt(0.0D);


				if(!details.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")){
					NimaiMCustomer grCoUser = this.repo.findByUserid(pstPaidDetails.getUserId());	
					if(grCoUser.getAccountType().equalsIgnoreCase("SUBSIDIARY")||grCoUser.getAccountType().equalsIgnoreCase("BANKUSER")) {
						grCoUser.setPaymentApprovedBy(Utility.getUserId());
						grCoUser.setPaymentDate(new Date());
						this.repo.save(grCoUser);
					}else {
						customer2.setPaymentApprovedBy(null);
						customer2.setPaymentStatus("Rejected");
						customer2.setPaymentDate(null);
						this.repo.save(customer2);
					}
				
				}else {
					NimaiMCustomer grCoUser = this.repo.findByUserid(pstPaidDetails.getUserId());	
					if(grCoUser.getAccountType().equalsIgnoreCase("SUBSIDIARY")||grCoUser.getAccountType().equalsIgnoreCase("BANKUSER")) {
						grCoUser.setPaymentApprovedBy(Utility.getUserId());
						grCoUser.setPaymentDate(new Date());
						this.repo.save(grCoUser);
					}else {
						customer2.setPaymentDate(null);
						customer2.setPaymentApprovedBy(null);
						this.repo.save(customer2);
					}
				
				}

			}
			if (request.getStatus().equalsIgnoreCase("Approved")) {
				logger.debug("Inside subscriptionplan approved status");
				logger.debug("subscriptionplan:" + request.getStatus());
				schedulerData.setUserid(customer2.getUserid());
				schedulerData.setUserName(customer2.getFirstName());
				schedulerData.setEmailId(customer2.getEmailAddress());
				schedulerData.setEvent("Cust_SplanPost_email_Wire_Approved");
				schedulerData.setSubscriptionId("POSTPAID_PLAN");
				schedulerData.setSubscriptionAmount(String.valueOf(pstPaidDetails.getTotalPayment()));
				schedulerData.setSubscriptionName("POSTPAID_PLAN");
				schedulerData.setSubscriptionValidity(details.getSubscriptionValidity());
				schedulerData.setRelationshipManager(details.getRelationshipManager());
				schedulerData.setCustomerSupport(details.getCustomerSupport());
				schedulerData.setSubscriptionStartDate(details.getSplanStartDate());
				schedulerData.setSubscriptionEndDate(details.getSplanEndDate());
				schedulerData.setEmailStatus("Pending");
				schedulerData.setPostPaidId(pstPaidDetails.getPostpaidId());
				schedulerData.setSubsriptionDetailsId(details.getSplSerialNumber());
				schedulerData.setInsertedDate(new Date());
				List<String> vasIdString = new ArrayList<>();
				
				int trxnCount=0;
				if (pstPaidDetails.getDueType().equalsIgnoreCase("totalDue")) {
					String vasId = request.getPostPaidIdString();
					String[] vasIdArray = vasId.split("-");
					for (String str : vasIdArray) {
						trxnCount=trxnCount+1;
					}
					}
				else {
					trxnCount=1;
				}
				details.setLcUtilizedCount(lcUCnt-trxnCount);
				schedulerData.setNumberOfTrxn(trxnCount);
				if(!details.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")){
					NimaiMCustomer grCoUser = this.repo.findByUserid(pstPaidDetails.getUserId());	
					if(grCoUser.getAccountType().equalsIgnoreCase("SUBSIDIARY")||grCoUser.getAccountType().equalsIgnoreCase("BANKUSER")) {
						grCoUser.setPaymentApprovedBy(Utility.getUserId());
						grCoUser.setPaymentDate(new Date());
						grCoUser.setPaymentStatus("Approved");
						this.repo.save(grCoUser);
					}else {
						customer2.setPaymentApprovedBy(null);
						customer2.setPaymentStatus("Approved");
						customer2.setPaymentDate(null);
						this.repo.save(customer2);
					}
				
				}else {
					NimaiMCustomer grCoUser = this.repo.findByUserid(pstPaidDetails.getUserId());	
					if(grCoUser.getAccountType().equalsIgnoreCase("SUBSIDIARY")||grCoUser.getAccountType().equalsIgnoreCase("BANKUSER")) {
						grCoUser.setPaymentApprovedBy(Utility.getUserId());
						grCoUser.setPaymentDate(new Date());
						this.repo.save(grCoUser);
					}else {
						customer2.setPaymentDate(null);
						customer2.setPaymentApprovedBy(Utility.getUserId());
						this.repo.save(customer2);
					}
				
				}
				
				int vasAmt=0;
				if (vasList.size() >= 1) {
					System.out.println("Inside vas list for loop");
					float vasSumAmount = 0;
					for (NimaiSubscriptionVas vasPlan : vasList) {
						vasIdString.add(String.valueOf(vasPlan.getId()).concat("-"));
						String[] array = new String[vasIdString.size()];
						vasIdString.toArray(array);
						StringBuilder sb = new StringBuilder();
						for (String str : (String[]) vasIdString.<String>toArray(array)) {
							sb.append(str);
							sb.substring(0, sb.length() - 1);
						}
						StringBuilder vasNumberString = sb;
						schedulerData.setDescription5(vasNumberString.toString());
						vasSumAmount = vasSumAmount + vasPlan.getPricing();
					}
					vasAmt= Math.round(vasSumAmount);
				}
				schedulerData.setVasAmount(vasAmt);
				this.schRepo.save(schedulerData);

				logger.debug("details to be save of subscription plan" + details);
				NimaiTransactionViewCount newViewCount = new
						NimaiTransactionViewCount();
				if(customer2.getAccountType().equalsIgnoreCase("SUBSIDIARY") ||
						customer2.getAccountType().equalsIgnoreCase("BANKUSER")	) {
					newViewCount.setUserId(customer2.getAccountSource());
				}else {
					newViewCount.setUserId(customer2.getUserid());
				}
				
				newViewCount.setAfterAccepted(0);
				newViewCount.setAcceptedFlag(0);
				newViewCount.setBeforeAccepted(0);
				newViewCount.setInsertDate(new Date());
				this.viewCountRepo.save(newViewCount);
				logger.debug("Reset transaction count for userId: " + customer2.getUserid());
			}

			this.postPaidRepoUpd.save(pstPaidDetails);
			
			sPlanRepo.save(details);

//				if(request.getDueType().equalsIgnoreCase("totalDue")) {
//					List<NimaiPostpaidSubscriptionDetails> postPaidPlan = this.postPaidRepo.findByUserId(pstPaidDetails.getUserId());
//					
//				}

			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Payment status updated successfully... "),
					HttpStatus.OK);
		} catch(Exception e) {
			System.out.println("Exception in Payment status update :" + e.getMessage());
			e.printStackTrace();
			
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), 
					"Error due to some technical issue"),HttpStatus.EXPECTATION_FAILED);
		}
		}

	
	
	
	@Override
	public List<PlanOfPaymentDetailsResponse> getPlanOPostPaidPayDetails(String userId) {

		NimaiMCustomer cust = (NimaiMCustomer) this.repo.getOne(userId);
		System.out.println(">>>>>>>>>>> "+cust.getUserid());
		List<NimaiSubscriptionDetails> subs = (List<NimaiSubscriptionDetails>) this.planRepo
				.postPaidPlanFindByUserid(cust.getUserid()).stream()
				.sorted(Comparator.comparingInt(NimaiSubscriptionDetails::getSplSerialNumber).reversed()).limit(5L)
				.collect(Collectors.toList());
		List<PlanOfPaymentDetailsResponse> data = new ArrayList<>();
		for (NimaiSubscriptionDetails pay : subs) {
			PlanOfPaymentDetailsResponse value = new PlanOfPaymentDetailsResponse();
			value.setSubscriptionId(pay.getSubscriptionId());
			value.setSplSerialNumber(pay.getSplSerialNumber());
			value.setUserid(pay.getUserid().getUserid());
			value.setSubscriptionName(pay.getSubscriptionName());
			value.setSubscriptionAmount(
					(pay.getSubscriptionAmount() != null) ? ("USD " + pay.getSubscriptionAmount()) : "");
			value.setLcCount(pay.getLcCount() + " Trxn Credits");
			value.setSplanStartDate(pay.getSplanStartDate());
			value.setSplanEndDate(pay.getSplanEndDate());
			value.setSubsidiaries(pay.getSubsidiaries() + " Subsidiaries");
			value.setRelationshipManager(pay.getRelationshipManager() + ", Relationship Manager");
			value.setCustomerSupport(pay.getCustomerSupport() + " Customer Support");
			value.setRemark(pay.getRemark());
			value.setDiscount(String.valueOf(pay.getDiscount()));
			value.setIsVasAppliedWithSPlan(pay.getIsVasApplied());
			if (pay.getStatus().equalsIgnoreCase("ACTIVE")) {
				value.setStatus("Active");
				value.setMakerComment(pay.getMakerComment());
				value.setCheckerComment(pay.getCheckerComment());
			} else {
				value.setStatus(pay.getStatus());
			}
			value.setSubscriptionValidity(pay.getSubscriptionValidity() + " Months Validity");
			value.setDiscount((pay.getDiscount() == null) ? "" : ("USD " + pay.getDiscount()));
			value.setPaymentStatus(pay.getPaymentStatus());
			value.setInsertedDate(pay.getInsertedDate());
			value.setPaymentMode(pay.getPaymentMode());
//			List<String> userIdList = this.postPaidRepo.findUserIdByAccountSource(userId);
//			System.out.println("userId List ---------> "+userIdList);
//			if(userIdList != null){
//				if(userIdList.isEmpty()){
//					userIdList.add(userId);
//				}
//			}else{
//				userIdList = new ArrayList<>();
//				userIdList.add(userId);
//			}
//			System.out.println("After patch userId list -------> "+userIdList);
			List<NimaiPostpaidSubscriptionDetailsUpd> postPaidPlan = this.postPaidRepoUpd
					.findByUserIdAndSubscriptionIdAndSerialNo(userId, pay.getSplSerialNumber());
			NimaiPostpaidSubscriptionRes res = new NimaiPostpaidSubscriptionRes();
			List<NimaiPostpaidSubscriptionRes> poDetails = new ArrayList<>();
			if (postPaidPlan == null || postPaidPlan.size() == 0) {
				res.setCheckerApprovedBy("");
				res.setMakerApprovedby("");
				res.setInvoiceId("");
				res.setMakerApprovedby("");
				res.setMakerComment("");
				res.setMinDue(0.0);
				res.setDueType(" ");
				res.setTotalDue(0.0);
			}
int discountId=0;
			for (NimaiPostpaidSubscriptionDetailsUpd posModel : postPaidPlan) {
				NimaiPostpaidSubscriptionRes poRes = new NimaiPostpaidSubscriptionRes();
				poRes.setCheckerApprovedBy(posModel.getCheckerApprovalBy());
				poRes.setMakerApprovedby(posModel.getMakerApprovalBy());
				poRes.setCheckerComment(posModel.getCheckerComment());
				poRes.setMakerComment(posModel.getMakerComment());
				poRes.setPostPaidId(posModel.getPostpaidId());
				poRes.setTotalDue((double) Math.round(posModel.getTotalDue()));
				poRes.setMinDue((double) Math.round(posModel.getMinDue()));
				poRes.setTotalPayment((double) Math.round(posModel.getMinDue()));
				poRes.setDueType(posModel.getDueType());
				poRes.setTransactioId(posModel.getTransactionId());
				poRes.setPaymentMode(posModel.getModeOfPayment());
				String status=quotemasterrepo.quotationStatus(posModel.getTransactionId());
				poRes.setQuotationStatus(status);
				poRes.setInvoiceId(posModel.getInvoiceId());
				poRes.setUserId(this.postPaidRepo.findUserIdByPerId(posModel.getPostpaidId()));
				//poRes.setUserId(posModel.getUserId().getUserid().getUserid());
//				if(posModel.getDiscountId()==null || posModel.getDiscountId()==0) {
//					discountId=0;
//				}else {
//					discountId=posModel.getDiscountId();
//				}
				if(posModel.getDisountId()!=null)
					if(posModel.getDisountId()!=0)
						discountId=posModel.getDisountId();
				
				poRes.setPaymentStatus(posModel.getPaymentStatus());
				poDetails.add(poRes);

			}
			value.setPostPaidList(poDetails);
			List<VasDetails> vaDetails = new ArrayList<>();
			List<NimaiSubscriptionVas> vasList = this.vasRep.findVasById(userId);
			if (vasList.size() > 0) {

				for (NimaiSubscriptionVas vasDetails : vasList) {
					VasDetails vdetails = new VasDetails();
					vdetails.setVasAmount(String.valueOf(Math.round(vasDetails.getPricing())));
					vdetails.setVasStatus(vasDetails.getStatus());
					vdetails.setVasPlan(vasDetails.getPlanName());
					vdetails.setVasPaymentStatus(vasDetails.getPaymentSts());
					vdetails.setPaymentMode(vasDetails.getMode());
					vdetails.setVasCheckerComment(vasDetails.getCheckerComment());
					vdetails.setVasMakerComment(vasDetails.getMakerComment());
					vdetails.setVasInvoiceId(vasDetails.getInvoiceId());
					vdetails.setVasInsertedDate(vasDetails.getInsertedDate());
					vdetails.setVasModifiedDate(vasDetails.getModifiedDate());
					vdetails.setVasBenefits(vasDetails.getDescription1()+","
							+vasDetails.getDescription2()+","+vasDetails.getDescription3()+","
							+vasDetails.getDescription4()+","+vasDetails.getDescription5());
					vaDetails.add(vdetails);
				}
				value.setVasList(vaDetails);
			}

			
		
			
			
			if (pay.getUserid() != null) {
				if (pay.getInvoiceId() == null) {
					value.setTransactionId("");
				} else {
					value.setTransactionId(pay.getInvoiceId());
				}
			} else {
				value.setTransactionId("");
			}
			if (discountId > 0) {
				NimaiMDiscount dis = (NimaiMDiscount) this.discRepo.getOne(discountId);
				value.setCouponCode(dis.getCouponCode());
			} else {
				value.setCouponCode("");
			}
			value.setTotalAmount("USD " + (pay.getSubscriptionAmount().intValue() + pay.getVasAmount()));
			value.setAmountPaid("USD " + ((pay.getSubscriptionAmount().intValue() + pay.getVasAmount())
					- ((pay.getDiscount() == null) ? 0.0D : pay.getDiscount().doubleValue())));
			data.add(value);
		}
		return data;
	}

	@Override
	public PagedResponse<?> getvasWirePostPaidTransferList(SearchRequest request) {
		Page<NimaiMCustomer> paymentDetails;
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { "PAYMENT_DATE" }).descending()
						: Sort.by(new String[] { "PAYMENT_DATE" }).ascending());
		String countryNames = Utility.getUserCountry();
		int pageSize = request.getSize();
		if (request.getSubscriberType() != null || request.getBankType() != null) {
			if (request.getCountry() == null || request.getCountry().isEmpty()) {
				request.setCountryNames(countryNames);
				if (request.getCountryNames().equalsIgnoreCase("all")) {
					if (request.getSubscriberType().equalsIgnoreCase("CUSTOMER") && request.getBankType() == null) {
						paymentDetails = this.repo.findAllMakerApprovedPostPaidPaymentDetailsSubsType(
								request.getSubscriberType(), (Pageable) pageRequest);
					} else {
						System.out.println("==============countrynames 10D" + countryNames);
						paymentDetails = this.repo.findAllMakerApprovedPaymentDetailsSubsTypeBankTypeForPostPaid(
								request.getSubscriberType(), request.getBankType(), (Pageable) pageRequest);
					}
				} else {
					System.out.println("==============countrynames 10C" + countryNames);
					List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
							.collect(Collectors.toList());
					if (request.getSubscriberType().equalsIgnoreCase("CUSTOMER") && request.getBankType() == null) {
						paymentDetails = this.repo.findMakerApprovedPoPaymentDetailsSubsType(
								request.getSubscriberType(), value, (Pageable) pageRequest);
						System.out.println("==============countrynames 10B" + countryNames);
					} else {
						System.out.println("==============countrynames 10A" + countryNames);
						paymentDetails = this.repo.findMakerApprovedPaymentDetailsSubsTypeBankTypeForPostPaid(
								request.getSubscriberType(), request.getBankType(), value, (Pageable) pageRequest);
					}
				}
			} else {
				if (request.getCountry().equalsIgnoreCase("all")) {
					request.setCountryNames(countryNames);
					if (request.getCountryNames().equalsIgnoreCase("all")) {
						System.out.println("==============countrynames 1" + countryNames);
						paymentDetails = this.repo.findAllMakerApprovedPoPaymentDetailsSubsTypeBankType(
								request.getSubscriberType(), request.getBankType(), (Pageable) pageRequest);
					} else {
						System.out.println("==============countrynames 2" + countryNames);
						List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
								.collect(Collectors.toList());
						paymentDetails = this.repo.findMakerApprovedPaymentDetailsPoSubsTypeBankType(
								request.getSubscriberType(), value, (Pageable) pageRequest);
					}
				} else {
					System.out.println("==============countrynames 3" + countryNames);
					paymentDetails = this.repo.getPoListByCountryname(request.getCountry(), (Pageable) pageRequest);
				}
				request.setCountryNames(request.getCountry());
			}
		} else if (request.getCountry() == null || request.getCountry().isEmpty()) {
			System.out.println("==============countrynames 4" + countryNames);
			request.setCountryNames(countryNames);
			if (request.getCountryNames().equalsIgnoreCase("all")) {
				System.out.println("==============countrynames 5" + countryNames);
				paymentDetails = this.repo.findAllMakerApprovedPoPaymentDetails(pageRequest);
			} else {
				System.out.println("==============countrynames 6" + countryNames);
				List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
						.collect(Collectors.toList());
				paymentDetails = this.repo.findMakerApprovedPoPaymentDetails(value, (Pageable) pageRequest);
			}
		} else {
			System.out.println("==============countrynames 7" + countryNames);
			if (request.getCountry().equalsIgnoreCase("all")) {
				request.setCountryNames(countryNames);
				if (request.getCountryNames().equalsIgnoreCase("all")) {
					System.out.println("==============countrynames 8" + countryNames);
					paymentDetails = this.repo.findAllMakerApprovedPoPaymentDetails((Pageable) pageRequest);
				} else {
					System.out.println("==============countrynames 9" + countryNames);
					List<String> value = (List<String>) Stream.<String>of(request.getCountryNames().split(",", -1))
							.collect(Collectors.toList());
					paymentDetails = this.repo.findMakerApprovedPoPaymentDetails(value, (Pageable) pageRequest);
				}
			} else {
				System.out.println("==============countrynames 10" + countryNames);
				paymentDetails = this.repo.getListByCountrynameForPostPaid(request.getCountry(), (Pageable) pageRequest);
			}
			request.setCountryNames(request.getCountry());
		}
		System.out.println("==============countrynames 2" + countryNames);
		System.out.println("==============countrynames 2" + request.getCountryNames());
		System.out.println("payment detials size" + paymentDetails.getSize());
		System.out.println("payment detials size" + paymentDetails.getSize());
		// final BankDetailsResponse response;
		// final NimaiSubscriptionDetails sub;
		// final NimaiSubscriptionVas vasDetails;
		List<BankDetailsResponse> responses = paymentDetails.map(cust -> {
			BankDetailsResponse response = new BankDetailsResponse();
			String accType=cust.getAccountType();
			String sPlanPresentUsrId="";
			NimaiSubscriptionDetails sub;
			if(accType.equalsIgnoreCase("SUBSIDIARY")){
				sPlanPresentUsrId=cust.getAccountSource();
				NimaiMCustomer parentDetails=repo.getOne(sPlanPresentUsrId);

				sub = (NimaiSubscriptionDetails) parentDetails.getNimaiSubscriptionDetailsList().stream()
						.filter(e -> e.getStatus().equalsIgnoreCase("Active")).findFirst().orElse(null);

			}else {
				sub = (NimaiSubscriptionDetails) cust.getNimaiSubscriptionDetailsList().stream()
						.filter(e -> e.getStatus().equalsIgnoreCase("Active")).findFirst().orElse(null);


			}

			// sub =
			// cust.getNimaiSubscriptionDetailsList().stream().filter().findFirst().orElse(null);

//			NimaiPostpaidSubscriptionDetails posPaid = (NimaiPostpaidSubscriptionDetails) sub.getNimaiPostPaidDetails()
//					.stream().filter(e -> e.getStatus().equalsIgnoreCase("Active")).findFirst().orElse(null);

			if (sub != null) {
				System.out.println("query for NimaiPostpaidSubscriptionDetailsUpd "+cust.getUserid());
				List<NimaiPostpaidSubscriptionDetailsUpd> postPaidPlan = this.postPaidRepoUpd
						.findByUserIdAndSubscriptionIdAndTotalMaker(cust.getUserid(), "Wire", "Maker Approved",
								"totalDue", "minDue");

				List<String> vasIdString = new ArrayList<>();
				for (NimaiPostpaidSubscriptionDetailsUpd postPaid : postPaidPlan) {

					if (postPaid.getDueType().equalsIgnoreCase("totalDue")) {
						List<NimaiPostpaidSubscriptionDetailsUpd> postPaidPlanDetails = this.postPaidRepoUpd
								.findByUserIdAndPostPaidDetails(cust.getUserid(), "Wire", "Maker Approved");
						for (NimaiPostpaidSubscriptionDetailsUpd postPaidNew : postPaidPlanDetails) {
							vasIdString.add(String.valueOf(postPaidNew.getPostpaidId()).concat("-"));
							String[] array = new String[vasIdString.size()];
							vasIdString.toArray(array);
							StringBuilder sb = new StringBuilder();
							for (String str : (String[]) vasIdString.<String>toArray(array)) {
								sb.append(str);
								sb.substring(0, sb.length() - 1);

							}
							StringBuilder vasNumberString = sb;
							response.setPostPaidIdString(vasNumberString.toString());
						}
					}

					response.setPostPaidId(postPaid.getPostpaidId());

					if (postPaid.getDueType() == null) {
						response.setDueType("NA");
					} else {
						response.setDueType(postPaid.getDueType());
					}
					response.setStatus(postPaid.getPaymentStatus());
				}

				response.setUserid(cust.getUserid());
				response.setFirstName(cust.getFirstName());
				response.setLastName(cust.getLastName());
				response.setEmailAddress(cust.getEmailAddress());
				response.setMobileNumber(cust.getMobileNumber());
				response.setRegisteredCountry(cust.getRegisteredCountry());

				response.setPaymentApprovedBy(cust.getPaymentApprovedBy());
				response.setPlanOfPayments((cust.getNimaiSubscriptionDetailsList().size() != 0)
						? collectPlanName(cust.getNimaiSubscriptionDetailsList())
						: "No Active Plan");
				response.setPlanId(sub.getSplSerialNumber());
				response.setMakerComment(sub.getMakerComment());
				response.setCheckerComment(sub.getCheckerComment());
				response.setPaymentMode(sub.getPaymentMode());
			}
			return response;
		}).getContent();
		return new PagedResponse(responses, paymentDetails.getNumber(), paymentDetails.getSize(),
				paymentDetails.getTotalElements(), paymentDetails.getTotalPages(), paymentDetails.isLast());
	}

	@Override
	public NimaiPostpaidSubscriptionRes getCurrentPlanOPostPaidPayDetails(String userId) {
		// TODO Auto-generated method stub
		// CU55714,CU9184
		String paymentMode = "Wire";
		String paymentStatus = "Pending";
		List<NimaiPostpaidSubscriptionDetailsUpd> postPaidPlan = this.postPaidRepoUpd
				.findByUserIdAndModeAndStatus(userId, paymentMode,paymentStatus);
		NimaiPostpaidSubscriptionRes poRes = new NimaiPostpaidSubscriptionRes();

		if (postPaidPlan.size() == 1) {
			NimaiPostpaidSubscriptionDetailsUpd posModel=postPaidPlan.get(0);
			if(Objects.equals(posModel.getDueType(), "minDue")){
				poRes.setCheckerApprovedBy(posModel.getCheckerApprovalBy());
				poRes.setMakerApprovedby(posModel.getMakerApprovalBy());
				poRes.setCheckerComment(posModel.getCheckerComment());
				poRes.setMakerComment(posModel.getMakerComment());
				poRes.setPostPaidId(posModel.getPostpaidId());
				poRes.setTotalDue(0.0D);
				poRes.setDueType(posModel.getDueType());
				poRes.setTransactioId(posModel.getTransactionId());
				poRes.setPaymentStatus(posModel.getPaymentStatus());
				poRes.setGroupTotalPayment((double) Math.round(posModel.getTotalPayment()));
				float vasAmountTotal=0;
				List<NimaiSubscriptionVas> vasList = this.vasRep.findVasByUserIdAndinvoiceid(userId,posModel.getInvoiceId());
				if (vasList.size() > 0) {
					for (NimaiSubscriptionVas vasDetails : vasList) {
						vasAmountTotal=vasAmountTotal+vasDetails.getPricing();
					}
				}
				Double disAmount=0.0;

				if (posModel.getDisountId() > 0) {
					disAmount=posModel.getDiscountAmnt();
				}
				poRes.setMinDue((posModel.getMinDue() + vasAmountTotal) - ((disAmount == null) ? 0.0D : disAmount));
				poRes.setTotalPayment(poRes.getMinDue());
			}
		} else {
			List<String> vasIdString = new ArrayList<>();
//			Double totalDue=0.0D;
			for (NimaiPostpaidSubscriptionDetailsUpd postPaid : postPaidPlan) {
				System.out.println("Inside minDue condition4:"+postPaid.toString());
				vasIdString.add(String.valueOf(postPaid.getPostpaidId()).concat("-"));
				String[] array = new String[vasIdString.size()];
				vasIdString.toArray(array);
				StringBuilder sb = new StringBuilder();
				for (String str : vasIdString.toArray(array)) {
					sb.append(str);
					sb.substring(0, sb.length() - 1);
				}
				StringBuilder vasNumberString = sb;

				Double totalDue = quotemasterrepo.findSumOfTotalDue(userId);
				if (totalDue == null) {
					poRes.setTotalDue(0.0);
					totalDue=0.0;
				} else {
					poRes.setTotalDue(totalDue);
				}
				poRes.setCheckerApprovedBy(postPaid.getCheckerApprovalBy());
				poRes.setMakerApprovedby(postPaid.getMakerApprovalBy());
				poRes.setPostPaidIdString(vasNumberString.toString());
				poRes.setCheckerComment(postPaid.getCheckerComment());
				poRes.setMakerComment(postPaid.getMakerComment());
				poRes.setPostPaidId(postPaid.getPostpaidId());

				poRes.setMinDue(0.0D);
				poRes.setTotalPayment(postPaid.getTotalPayment());
				poRes.setDueType("totalDue");
				poRes.setTransactioId(postPaid.getTransactionId());
				poRes.setPaymentStatus(postPaid.getPaymentStatus());
				poRes.setGroupTotalPayment(postPaid.getTotalPayment());
				
				float vasAmountTotal=0;
				List<VasDetails> vaDetails = new ArrayList<>();
				List<NimaiSubscriptionVas> vasList = this.vasRep.findVasByUserIdAndinvoiceid(userId,postPaid.getInvoiceId());
				if (vasList.size() > 0) {
					for (NimaiSubscriptionVas vasDetails : vasList) {
						vasAmountTotal=vasAmountTotal+vasDetails.getPricing();
					}
				}
				Double disAmount=0.0;
				
				if (postPaid.getDisountId() > 0) {
					disAmount=postPaid.getDiscountAmnt();
				} 
				poRes.setTotalDue((totalDue + vasAmountTotal) - ((disAmount == null) ? 0.0D : disAmount));
			}
		}
		Double finalMinDue=quotemasterrepo.findMinDueBeforeInitiate(userId);
		Double finalTotalDue = quotemasterrepo.findSumOfTotalDueBeforeInitiate(userId);
		if (finalTotalDue == null) {
			poRes.setFinalTotalPayment(0.0);
		} else {
			poRes.setFinalTotalPayment(finalTotalDue);
		}
		
		if (finalMinDue == null) {
			poRes.setFinalminDue(0.0);
		} else {
			poRes.setFinalminDue(finalMinDue);
		}
		
		return poRes;
	}

	@Override
	public ResponseEntity<?> interestedCountryUpdate(InterestedCountryBean[] request, String userId) {
		try {
			NimaiMCustomer details = this.repo.getOne(userId);
			icr.deleteInterestedCountryUserId(userId);
			for (int i = 0; i < request.length; i++) {
				// InterestedCountryBean intCon = new InterestedCountryBean();
				InterestedCountry ic = new InterestedCountry();
				ic.setCountryName(request[i].getCountriesIntrested());
				ic.setInsertedDate(Calendar.getInstance().getTime());
				ic.setUserId(details);
				ic.setCountryCurrencyId(request[i].getCcid());
				icr.save(ic);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(new ApiResponse(Boolean.valueOf(false), "Unable to Update Interested Country "),
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(new ApiResponse(Boolean.valueOf(true), "Interested Country Updated Successfully..."),
				HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> beneInterestedCountriesUpdate(BeneInterestedCountryBean[] request, String userId) {
		try {
			NimaiMCustomer details = this.repo.getOne(userId);
			bicr.deleteBeneInterestedCountryUserId(userId);
			for (int i = 0; i < request.length; i++) {
				// InterestedCountryBean intCon = new InterestedCountryBean();
				BeneInterestedCountry ic = new BeneInterestedCountry();
				ic.setCountryName(request[i].getCountriesIntrested());
				ic.setInsertedDate(Calendar.getInstance().getTime());
				ic.setUserid(details);
				ic.setCountryCurrencyId(request[i].getCcid());
				bicr.save(ic);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(
					new ApiResponse(Boolean.valueOf(false), "Unable to Update Bene Interested Country "),
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(
				new ApiResponse(Boolean.valueOf(true), "Bene Interested Country Updated Successfully..."),
				HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> addViewCount(TranxViewCount count) {
		try {
			NimaiMCustomer cust = this.repo.getOne(count.getUserId());
			if(cust.getUserid()!=null){
				boolean isPostPaidPlan=false;
				List<NimaiSubscriptionDetails> subscriptionDetailsList = cust.getNimaiSubscriptionDetailsList();
				if(subscriptionDetailsList!=null && !subscriptionDetailsList.isEmpty()){
//					isPostPaidPlan = subscriptionDetailsList.stream().map(
//									NimaiSubscriptionDetails::getSubscriptionName)
//							.anyMatch(e->e.equalsIgnoreCase("POSTPAID"));
					for (NimaiSubscriptionDetails subscriptionDetails:subscriptionDetailsList) {
						System.out.println("subscriptionDetails-------> "+subscriptionDetails.getSubscriptionName());
						System.out.println("subscriptionDetails-------> "+subscriptionDetails.getStatus());
						if(subscriptionDetails.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")){
							if(subscriptionDetails.getStatus().equalsIgnoreCase("ACTIVE")){
								isPostPaidPlan=true;
								break;
							}
						}
					}
					if(isPostPaidPlan){
						NimaiTransactionViewCount prevViewCount = this.viewCountRepo
								.getViewCountById(count.getUserId());
						if(prevViewCount!=null && prevViewCount.getAcceptedFlag()!=null){
							if(prevViewCount.getAcceptedFlag()==0){
								NimaiTransactionViewCount newViewCount = new
										NimaiTransactionViewCount();
								newViewCount.setUserId(cust.getUserid());
								newViewCount.setAfterAccepted(prevViewCount.getAfterAccepted());
								newViewCount.setAcceptedFlag(prevViewCount.getAcceptedFlag());
								newViewCount.setBeforeAccepted(count.getCount()
										+prevViewCount.getBeforeAccepted());
								newViewCount.setInsertDate(new Date());
								this.viewCountRepo.save(newViewCount);
								return new ResponseEntity(
										new ApiResponse(Boolean.TRUE, "New Transaction View Count Added Successfully..."),
										HttpStatus.CREATED);
							} else if (prevViewCount.getAcceptedFlag()==1) {
								NimaiTransactionViewCount newViewCount = new
										NimaiTransactionViewCount();
								newViewCount.setUserId(cust.getUserid());
								newViewCount.setAfterAccepted(count.getCount()+
										prevViewCount.getAfterAccepted());
								newViewCount.setBeforeAccepted(prevViewCount.getBeforeAccepted());
								newViewCount.setAcceptedFlag(prevViewCount.getAcceptedFlag());
								newViewCount.setInsertDate(new Date());
								this.viewCountRepo.save(newViewCount);
								return new ResponseEntity(
										new ApiResponse(Boolean.TRUE, "New Transaction View Count Added Successfully..."),
										HttpStatus.CREATED);
							}
						}else {
							NimaiTransactionViewCount newViewCount = new NimaiTransactionViewCount();
							newViewCount.setUserId(cust.getUserid());
							newViewCount.setBeforeAccepted(count.getCount());
							newViewCount.setAfterAccepted(0);
							newViewCount.setAcceptedFlag(0);
							newViewCount.setInsertDate(new Date());
							this.viewCountRepo.save(newViewCount);
							return new ResponseEntity(
									new ApiResponse(Boolean.TRUE, "New Transaction View Count Added Successfully..."),
									HttpStatus.CREATED);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(
					new ApiResponse(Boolean.FALSE, "Unable to Add New Transaction View Count for userId: "+count.getUserId()),
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(
				new ApiResponse(Boolean.FALSE, "Unable to Add New Transaction View Count for userId: "+count.getUserId()),
				HttpStatus.BAD_REQUEST);

	}

	@Override
	public ResponseEntity<?> viewCount(String userId) {
		List<NimaiTransactionViewCount> viewCount = null;
		NimaiTransactionViewCount lastEntry = null;
		int lastCount = 0;
		try {
			NimaiMCustomer cust = this.repo.getOne(userId);

			if(cust.getUserid()!=null){
				boolean isPostPaidPlan=false;
				List<NimaiSubscriptionDetails> subscriptionDetailsList = cust.getNimaiSubscriptionDetailsList();
				if(subscriptionDetailsList!=null && !subscriptionDetailsList.isEmpty()){
//					boolean isPostPaidPlan = subscriptionDetailsList.stream().map(
//									NimaiSubscriptionDetails::getSubscriptionName)
//							.anyMatch(e->e.equalsIgnoreCase("POSTPAID"));
					for (NimaiSubscriptionDetails subscriptionDetails:subscriptionDetailsList) {
						System.out.println("subscriptionDetails-------> "+subscriptionDetails.getSubscriptionName());
						System.out.println("subscriptionDetails-------> "+subscriptionDetails.getStatus());
						if(subscriptionDetails.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")){
							if(subscriptionDetails.getStatus().equalsIgnoreCase("ACTIVE")){
								isPostPaidPlan=true;
								break;
							}
						}
					}
					if(isPostPaidPlan){
						lastEntry = this.viewCountRepo.getViewCountById(userId);
						viewCount = this.viewCountRepo.getLastTwoRowsById(userId);
						System.out.println("viewCount.size()--> "+viewCount.size());
						System.out.println("viewCount.get(0).getAcceptedFlag()--> "+viewCount.get(0).getAcceptedFlag());
						if(viewCount.size()==2){
							if(viewCount.get(0).getAcceptedFlag() == 0){
								lastCount = viewCount.get(0).getBeforeAccepted()
										- viewCount.get(1).getBeforeAccepted();
							} else if (viewCount.get(0).getAcceptedFlag() == 1) {
								lastCount = viewCount.get(0).getAfterAccepted()
										- viewCount.get(1).getAfterAccepted();
							}
						} else if (viewCount.size()==1) {
							if(viewCount.get(0).getAcceptedFlag() == 0){
								lastCount = viewCount.get(0).getBeforeAccepted();
							} else if (viewCount.get(0).getAcceptedFlag() == 1) {
								lastCount = viewCount.get(0).getAfterAccepted();
							}
						}else {
							lastCount=0;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(
					new ApiResponse(Boolean.FALSE, "Unable To Read From Given UserId: "+userId),
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(
				new ApiResponse(Boolean.TRUE, "Success",lastEntry,String.valueOf(lastCount)),
				HttpStatus.CREATED);
	}


	

	private void phaseTwoTranferFailData(NimaiMCustomer customer, String urLdetails, String message) {
		// TODO Auto-generated method stub
		System.out.println("inside phaseTwoTranferFailData");
		PhaseTwoTransferFail data=new PhaseTwoTransferFail();
		data.setUserid(customer.getUserid());
		data.setBankName(customer.getBankName());
		data.setCompanyName(customer.getCompanyName());
		data.setBankName(customer.getBankName());
		data.setErrorDump(urLdetails +" "+ message);
		data.setFirstName(customer.getFirstName());
		data.setLastName(customer.getLastName());
		data.setEmailAddress(customer.getEmailAddress());
		phaseRepo.save(data);
		
	}

	private void phaseTwoTransferFailData(NimaiMCustomer customer, String urLdetails) {
		// TODO Auto-generated method stub
		System.out.println("inside phaseTwoTranferFailData");
		PhaseTwoTransferFail data=new PhaseTwoTransferFail();
		data.setUserid(customer.getUserid());
		data.setBankName(customer.getBankName());
		data.setCompanyName(customer.getCompanyName());
		data.setBankName(customer.getBankName());
		data.setErrorDump(urLdetails);
		data.setFirstName(customer.getFirstName());
		data.setLastName(customer.getLastName());
		data.setEmailAddress(customer.getEmailAddress());
		phaseRepo.save(data);
		NimaiEmailScheduler scheduler=new NimaiEmailScheduler();
		scheduler.setUserid(customer.getUserid());
		scheduler.setDescription1(urLdetails);
		scheduler.setEmailStatus(AppConstants.PEEMAILSTTS);
		scheduler.setEvent(AppConstants.FAILEVENT);
		schRepo.save(scheduler);
	}

	private void phaseTwoTranferFailData(NimaiMCustomer customer, Exception e) {
		// TODO Auto-generated method stub
		System.out.println("inside phaseTwoTranferFailData");
		PhaseTwoTransferFail data=new PhaseTwoTransferFail();
		data.setUserid(customer.getUserid());
		data.setBankName(customer.getBankName());
		data.setCompanyName(customer.getCompanyName());
		data.setBankName(customer.getBankName());
		data.setErrorDump(e.getMessage());
		data.setFirstName(customer.getFirstName());
		data.setLastName(customer.getLastName());
		data.setEmailAddress(customer.getEmailAddress());
		phaseRepo.save(data);
		NimaiEmailScheduler scheduler=new NimaiEmailScheduler();
		scheduler.setUserid(customer.getUserid());
		scheduler.setDescription1(e.getMessage());
		scheduler.setEmailStatus(AppConstants.PEEMAILSTTS);
		scheduler.setEvent(AppConstants.FAILEVENT);
		schRepo.save(scheduler);
	}


}
