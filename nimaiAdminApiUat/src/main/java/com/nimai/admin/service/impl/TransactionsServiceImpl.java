package com.nimai.admin.service.impl;

import com.nimai.admin.model.NimaiMCustomer;

import com.nimai.admin.model.NimaiMEmployee;
import com.nimai.admin.model.NimaiMQuotation;
import com.nimai.admin.model.NimaiMmTransaction;
import com.nimai.admin.payload.PagedResponse;
import com.nimai.admin.payload.QuotationDetailsResponse;
import com.nimai.admin.payload.QuotationListResponse;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.payload.TransactionDetails;
import com.nimai.admin.payload.TransactionSearchResponse;
import com.nimai.admin.repository.CurrencyRepository;
import com.nimai.admin.repository.CustomerRepository;
import com.nimai.admin.repository.EmployeeRepository;
import com.nimai.admin.repository.QuotationRepository;
import com.nimai.admin.repository.TransactionsRepository;
import com.nimai.admin.service.TransactionsService;
import com.nimai.admin.specification.QuotationSpecification;
import com.nimai.admin.specification.SecondaryTrSpecification;
import com.nimai.admin.specification.TransactionTempSpecification;
import com.nimai.admin.specification.TransactionsSpecification;
import com.nimai.admin.util.ModelMapper;
import com.nimai.admin.util.Utility;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TransactionsServiceImpl implements TransactionsService {
	@Autowired
	TransactionsRepository transactionsRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	TransactionsSpecification transactionsSpecification;

	@Autowired
	SecondaryTrSpecification secTransactionsSpecification;

	@Autowired
	TransactionTempSpecification transactionTempSpecification;

	@Autowired
	QuotationRepository quotationRepository;

	@Autowired
	QuotationSpecification quotationSpecification;

	@Autowired
	EmployeeRepository employeeRepository;

	private static final Logger logger = LoggerFactory.getLogger(TransactionsServiceImpl.class);

	public PagedResponse<?> getAllTransaction(SearchRequest request) {
		Page<NimaiMmTransaction> transactionList;
		Specification<NimaiMmTransaction> spec;
		System.out.println("================_+++++++++++Login userId" + Utility.getUserId());
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc")
						? Sort.by(request.getSortBy()).descending()
						: Sort.by(request.getSortBy()).ascending());
		String countryNames = Utility.getUserCountry();
		if (request.getCountry() == null) {
			System.out.println("============inside firstcondiiton" + countryNames);
			if (countryNames != null && countryNames.equalsIgnoreCase("all")) {
				System.out.println("============inside Seconcondiiton" + countryNames);
			} else if (countryNames != null && !countryNames.equalsIgnoreCase("all")) {
				System.out.println("============inside Thirddiiton" + countryNames);
				request.setCountryNames(countryNames);
			}
		} else if (request.getCountry() == null && countryNames == null) {
			System.out.println("============inside fourthdiiton" + countryNames);
			transactionList = null;
		}
		System.out.println("final countries inside method" + countryNames);
		System.out.println("Role selected: " + request.getRole());
		if (request.getDateFrom() == null && request.getDateTo() == null) {
			request.setDateFrom(LocalDate.now().minusDays(760L).toString());
			request.setDateTo(LocalDate.now().toString());
			logger.info("Fromdate minus 760 days in getCustomerRevenue " + LocalDate.now().minusDays(760L).toString());
			logger.info("Todate getCustomerRevenue" + LocalDate.now().toString());
		}
		System.out.println("Date from-------> "+request.getDateFrom());
		System.out.println("Date to-------> "+request.getDateTo());
		String dateTo = Utility.getDateTimeInFormat(request.getDateTo());
		System.out.println("Updated date----------> "+dateTo );
		if (request.getRole().equalsIgnoreCase("Customer RM") || request.getRole().equalsIgnoreCase("Bank RM")) {
			System.out.println("In CustomerRM / BankRM block");
			
			request.setCountryNames("All");
			if (request.getDateFrom() == null && request.getDateTo() == null) {
				request.setDateFrom(LocalDate.now().minusDays(760L).toString());
				request.setDateTo(LocalDate.now().toString());
				logger.info("Fromdate minus 760 days in getCustomerRevenue " + LocalDate.now().minusDays(760L).toString());
				logger.info("Todate getCustomerRevenue" + LocalDate.now().toString());
			}
			System.out.println("============inside customer/bankRM block: " + countryNames);
			System.out.println("============inside customer/bankRM block: " + request.getCountry());
			
			spec = this.transactionTempSpecification.getFilter(request);
			transactionList = this.transactionsRepository.findAll(spec,
					(Pageable) pageRequest);
		} else {
			System.out.println("In CustomerRM / BankRM else block");
			System.out.println("============inside customer/bankRM else block: " + countryNames);
			System.out.println("============inside customer/bankRM block: " + request.getCountry());
//			if (request.getEmailId() == null) {
//				request.setEmailId(request.getEmailId());
//			}
			if (request.getEmailId() != null) {
				logger.info("final email inside method3" + request.getEmailId());
				NimaiMCustomer custDetailsById = this.customerRepository.getUserByEmailId(request.getEmailId());
				request.setUserId(custDetailsById.getUserid());
				request.setEmailId(null);
				request.setUserBranchEmail(null);
				logger.info("userbranch email" + custDetailsById.getUserid());
			} else if (request.getMobileNo() != null) {
				logger.info("final request.getMobileNo inside method3" + request.getMobileNo());
				NimaiMCustomer custDetailsById = this.customerRepository.getUserByMobileNumber(request.getMobileNo());
				request.setUserId(custDetailsById.getUserid());
				request.setEmailId(null);
				request.setUserBranchEmail(null);
				request.setMobileNo(null);
				logger.info("userbranch email" + custDetailsById.getUserid());
			} else if (request.getCompanyName() != null) {
				logger.info("final request.request.getCompanyName inside method3" + request.getCompanyName());
				NimaiMCustomer custDetailsById = this.customerRepository.getUserByCompanyName(request.getMobileNo());
				if(custDetailsById==null) {
					logger.info("userbranch email" + request.getUserId());
					request.setUserId(request.getUserId());
				}else {
					logger.info("userbranch email" + custDetailsById.getUserid());
					request.setUserId(custDetailsById.getUserid());
				}
			
				request.setEmailId(null);
				request.setUserBranchEmail(null);
				
			}
			if(request.getUserId()==null || request.getUserId().isEmpty()) {
				request.setSubscriberType("BANK");
				request.setBankType("UNDERWRITER");
			}
			else if(request.getUserId().substring(0, 2).equalsIgnoreCase("CU")) {
				request.setSubscriberType("CUSTOMER");
				request.setBankType(null);
			}else if(request.getUserId().substring(0, 2).equalsIgnoreCase("BC")) {
				request.setSubscriberType("BANK");
				request.setBankType(null);
			}else {
				request.setSubscriberType("BANK");
				request.setBankType("UNDERWRITER");
			}
			System.out.println(request.getTxtStatus());
			System.out.println(request.getBankType());
			
			spec = this.transactionsSpecification.getFilter(request);
		
			transactionList = this.transactionsRepository.findAll(spec,pageRequest);
		}
		Map<String, String> countList = new HashMap<>();
		long count=0;
		Double totalTransaction = 0.0D;
		Double quoteReceived = 0.0D;
		Double quoteAccepted = 0.0D;
		if(transactionList.getSize()>0){
			List<String> transactionIdList = this.transactionsRepository
					.findAll(spec).stream().map(NimaiMmTransaction::getTransactionId)
					.collect(Collectors.toList());
			if(transactionIdList.size()>0){
				System.out.println("list of transaction ----> "+transactionIdList.get(0));
				count = this.quotationRepository.quoteCount(transactionIdList,
						request.getDateFrom(), dateTo);
				System.out.println("quotation count----------> " + count);
				countList.put("quotation", String.valueOf(count));
				totalTransaction = this.transactionsRepository
						.gettotalQuoteClosed(transactionIdList,request.getDateFrom(),dateTo);
				System.out.println("totalTransaction sum----------> " + totalTransaction);
				countList.put("totalTransaction",  totalTransaction==null?"0.0":String.valueOf(totalTransaction));
				quoteReceived = this.transactionsRepository
						.gettotalQuoteReceived(transactionIdList, request.getDateFrom(), dateTo);
				System.out.println("quoteReceived sum----------> " + quoteReceived);
				countList.put("quoteReceived", quoteReceived==null?"0.0":String.valueOf(quoteReceived));
				quoteAccepted = this.transactionsRepository
						.gettotalQuoteAccepted(transactionIdList, request.getDateFrom(), dateTo);
				System.out.println("quoteAccepted sum----------> " + quoteAccepted);
				countList.put("quoteAccepted", quoteAccepted==null?"0.0":String.valueOf(quoteAccepted));
			}else {
				countList.put("quotation", String.valueOf(count));
				countList.put("totalTransaction", String.valueOf(totalTransaction));
				countList.put("quoteReceived", String.valueOf(quoteReceived));
				countList.put("quoteAccepted", String.valueOf(quoteAccepted));
			}
		}else {
			countList.put("quotation", String.valueOf(count));
			countList.put("totalTransaction", String.valueOf(totalTransaction));
			countList.put("quoteReceived", String.valueOf(quoteReceived));
			countList.put("quoteAccepted", String.valueOf(quoteAccepted));
		}
		List<TransactionSearchResponse> responses = transactionList
				.map(cust -> ModelMapper.mapTransactionToResponse(cust,request)).getContent();
		return new PagedResponse(responses, transactionList.getNumber(), transactionList.getSize(),
				transactionList.getTotalElements(), transactionList.getTotalPages(), transactionList.isLast(), countList);
	}
	
	

	public PagedResponse<?> getmakerApprovedTransaction(SearchRequest request) {
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc")
						? Sort.by(new String[] { request.getSortBy() }).descending()
						: Sort.by(new String[] { request.getSortBy() }).ascending());
		String countryNames = Utility.getUserCountry();
		if (request.getCountry() == null) {
			if (countryNames == null || !countryNames.equalsIgnoreCase("all"))
				if (countryNames != null && !countryNames.equalsIgnoreCase("all"))
					request.setCountryNames(countryNames);
		} else if (request.getCountry() == null && countryNames == null) {
			Object object = null;
		}
		Page<NimaiMmTransaction> transactionList = this.transactionsRepository
				.findAll(this.transactionsSpecification.getFilter(request), (Pageable) pageRequest);
		
		
		List<TransactionSearchResponse> responses = transactionList
				.map(cust -> ModelMapper.mapMakerAppTransactionToResponse(cust)).getContent();
		return new PagedResponse(responses, transactionList.getNumber(), transactionList.getSize(),
				transactionList.getTotalElements(), transactionList.getTotalPages(), transactionList.isLast());
	}

	public List<String> userIdSearch(String userId) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = (List<String>) Stream.<String>of(val.split(",")).collect(Collectors.toList());
			return this.customerRepository.userIdSearchByCountry(userId.toLowerCase(), list);
		}
		return this.customerRepository.userIdSearch(userId.toLowerCase());
	}

	public List<String> emailIdSearch(String emailId) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = (List<String>) Stream.<String>of(val.split(",")).collect(Collectors.toList());
			return this.customerRepository.emailIdSearchByCountry(emailId.toLowerCase(), list);
		}
		return this.customerRepository.emailIdSearch(emailId.toLowerCase());
	}

	public List<String> mobileNumberSearch(String mobileNo) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = (List<String>) Stream.<String>of(val.split(",")).collect(Collectors.toList());
			return this.customerRepository.mobileNumberSearchByCountry(mobileNo.toLowerCase(), list);
		}
		return this.customerRepository.mobileNumberSearch(mobileNo.toLowerCase());
	}

	public List<String> companyNameSearch(String companyName) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> list = (List<String>) Stream.<String>of(val.split(",")).collect(Collectors.toList());
			return this.customerRepository.companyNameSearchByCountry(companyName.toLowerCase(), list);
		}
		return this.customerRepository.companyNameSearch(companyName.toLowerCase());
	}

	public List<String> countrySearch(String country) {
		String val = Utility.getUserCountry();
		if (!val.equalsIgnoreCase("All")) {
			List<String> nameList = (List<String>) Stream.<String>of(val.split(",")).collect(Collectors.toList());
			return (List<String>) nameList.stream().filter(x -> x.toLowerCase().contains(country.toLowerCase()))
					.collect(Collectors.toList());
		}
		return this.currencyRepository.countrySearch(country);
	}

	public ResponseEntity<TransactionDetails> getTransactionById(String transactionId) {
		try {
			System.out.println("details getTransactionById search TransactionsServiceImpl:: " + transactionId);
			NimaiMmTransaction trasaction = (NimaiMmTransaction) this.transactionsRepository.getOne(transactionId);
			NimaiMCustomer customer = (NimaiMCustomer) this.customerRepository
					.getOne(trasaction.getUserId().getUserid());
			TransactionDetails employeeListResponse = ModelMapper.mapTransactionDetails(trasaction, customer);
			if (employeeListResponse.getRmFirstName() != null) {
				NimaiMEmployee emp = this.employeeRepository.findByEmpCode(employeeListResponse.getRmFirstName());
				employeeListResponse.setRmFirstName(emp.getEmpName());
				employeeListResponse.setRmLastName(emp.getEmpLastName());
				employeeListResponse.setRmDesignation(emp.getDesignation());
			}
			return new ResponseEntity(employeeListResponse, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
	}

	public PagedResponse<?> getQuotesDetails(SearchRequest request) {

		// ==========Seondary marker implementation==========1.OfferPrice and isOffered
		// added newly

		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc")
						? Sort.by(new String[] { request.getSortBy() }).descending()
						: Sort.by(new String[] { request.getSortBy() }).ascending());
		Page<NimaiMQuotation> quotationList = this.quotationRepository
				.findAll(this.quotationSpecification.getFilter(request), (Pageable) pageRequest);
		List<QuotationListResponse> responses = (List<QuotationListResponse>) quotationList.stream()
				.map(quote -> new QuotationListResponse(quote.getQuotationId(),
						quote.getTransactionId().getTransactionId(),
						this.customerRepository.findBankName(quote.getBankUserid()), String.valueOf(quote.getTotalQuoteValue()),
						quote.getValidityDate(), quote.getQuotationStatus(), quote.getIsOffered(),
						quote.getTransactionId().getOfferedPrice(), quote.getTransactionId().getLcCurrency(),
				quote.getInsertedDate()))
				.collect(Collectors.toList());
		return new PagedResponse(responses, quotationList.getNumber(), quotationList.getSize(),
				quotationList.getTotalElements(), quotationList.getTotalPages(), quotationList.isLast());
	}

	public ResponseEntity<QuotationDetailsResponse> getQuotesDetailsById(Integer quotationId) {
		
		NimaiMQuotation quotation = (NimaiMQuotation) this.quotationRepository.getOne(quotationId);
		if (quotation != null) {
			QuotationDetailsResponse response = new QuotationDetailsResponse();
			response.setUserid(quotation.getUserid().getUserid());
			response.setQuotationId(quotation.getQuotationId());
			response.setBankUserid(this.customerRepository.findBankName(quotation.getBankUserid()));
			response.setCurrency(quotation.getTransactionId().getLcCurrency());
			response.setQuotationStatus(quotation.getQuotationStatus());
			response.setConfirmationCharges(quotation.getConfirmationCharges());
			response.setConfChgsIssuanceToNegot((quotation.getConfChgsIssuanceToNegot() != null)
					? quotation.getConfChgsIssuanceToNegot().toUpperCase()
					: "");
			response.setConfChgsIssuanceToMatur((quotation.getConfChgsIssuanceToMatur() != null)
					? quotation.getConfChgsIssuanceToMatur().toUpperCase()
					: "");
			response.setConfChgsIssuanceToClaimExp(quotation.getConfChgsIssToClaimExp());
			response.setConfChgsIssuanceToexp(quotation.getConfChgsIssuanceToExp());
			response.setTermCondition(quotation.getTermsConditions());
			if (quotation.getApplicableBenchmark() != null)
				response.setApplicableBenchmark(quotation.getApplicableBenchmark());
			if (quotation.getCommentsBenchmark() != null)
				response.setCommentsBenchmark(quotation.getCommentsBenchmark());
			if (quotation.getDiscountingCharges() != null)
				response.setDiscountingChargesPA(quotation.getDiscountingCharges());
			if (quotation.getRefinancingCharges() != null)
				response.setRefinancingCharges(quotation.getRefinancingCharges());
			if (quotation.getBankerAcceptCharges() != null)
				response.setBankerAcceptCharges(quotation.getBankerAcceptCharges());
			if (quotation.getNegotiationChargesFixed() != null)
				response.setNegotiationChargesFixed(quotation.getNegotiationChargesFixed());
			if (quotation.getNegotiationChargesPerct() != null)
				response.setNegotiationChargesPerct(quotation.getNegotiationChargesPerct());
			if (quotation.getDocHandlingCharges() != null)
				response.setDocHandlingCharges(quotation.getDocHandlingCharges());
			if (quotation.getOtherCharges() != null)
				response.setOtherCharges(quotation.getOtherCharges());
			if (quotation.getChargesType() != null)
				response.setSpecifyTypeOfCharges(quotation.getChargesType());
			if (quotation.getMinTransactionCharges() != null)
				response.setMinimumTransactionCharges(quotation.getMinTransactionCharges());
			response.setTotalQuoteValue(quotation.getTotalQuoteValue());
			response.setValidityDate(quotation.getValidityDate());
			response.setTransactionId(quotation.getTransactionId().getTransactionId());
		
			response.setIb(quotation.getTransactionId().getLcIssuanceBank());
			response.setAmount(quotation.getTransactionId().getLcValue() + "");
			
//			if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Confirmation")) {
//				response.setTanor(quotation.getTransactionId().getConfirmationPeriod());
//			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Discounting")) {
//				response.setTanor(quotation.getTransactionId().getDiscountingPeriod());
//			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Refinance")) {
//				response.setTanor(quotation.getTransactionId().getRefinancingPeriod());
//			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("ConfirmAndDiscount")) {
//				response.setTanor(quotation.getTransactionId().getConfirmationPeriod());
//			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Banker")) {
//				response.setTanor(quotation.getTransactionId().getDiscountingPeriod());
//			} else {
//				response.setTanor("");
//			}
			
			
			
			if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Confirmation")) {
				if(quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
					response.setTanor(String.valueOf(quotation.getTransactionId().getUsanceDays()));
				}else {
					response.setTanor(quotation.getTransactionId().getConfirmationPeriod());
				}
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Discounting")) {
				if(quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
					response.setTanor(String.valueOf(quotation.getTransactionId().getUsanceDays()));
				}else {
					response.setTanor(quotation.getTransactionId().getDiscountingPeriod());
				}
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Refinance")) {
				if(quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
					response.setTanor(String.valueOf(quotation.getTransactionId().getUsanceDays()));
				}else {
					//response.set;(quotation.getTransactionId().getRefinancingPeriod());
					response.setTanor(String.valueOf(quotation.getTransactionId().getOriginalTenorDays()));
				}
				
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("ConfirmAndDiscount")
					&& (quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("CU")
							|| quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("BC"))) {
				response.setTanor(quotation.getTransactionId().getConfirmationPeriod());
			}
			else if(quotation.getTransactionId().getRequirementType().equalsIgnoreCase("ConfirmAndDiscount") &&
					quotation.getTransactionId().getUserId().getUserid().equalsIgnoreCase("BA")) {
				response.setTanor(String.valueOf(quotation.getTransactionId().getUsanceDays()));
			}else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Banker")) {
				response.setTanor(quotation.getTransactionId().getDiscountingPeriod());	
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("TradeLoan")) {
				response.setTanor(String.valueOf(quotation.getTransactionId().getUsanceDays()));	
			} 
			else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("BillAvalisation") && 
					(quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("CU")
							|| quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("BC"))) {
				response.setTanor(String.valueOf(quotation.getTransactionId().getDiscountingPeriod()));
			} 	else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("BillAvalisation") && 
					(quotation.getTransactionId().getUserId().getUserid().substring(0, 2).equalsIgnoreCase("BA"))) {
				response.setTanor(String.valueOf(quotation.getTransactionId().getUsanceDays()));
			} 
			else {
				response.setTanor("");
			}
			
			String requirement = null;
			if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("ConfirmAndDiscount")) {
				requirement = "Confirmation and Discounting";
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Banker")) {
				requirement = "Banker's Acceptance";
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Refinance")) {
				requirement = "Refinancing";
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Discounting")) {
				requirement = "Discounting";
			} else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("Confirmation")) {
				requirement = "Confirmation";
			}
			else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("TradeLoan")) {
				requirement = "Trade Loan";
			}
			else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("BillAvalisation")) {
				requirement = "Avalisation";
			}	else if (quotation.getTransactionId().getRequirementType().equalsIgnoreCase("BankGuarantee")) {
				requirement = "Bank Guarantee";
			}
			
			response.setRequirementType(requirement);
			
			
			// secondary MArket transation new field Added
			if (quotation.getParticipationCommission() == null) {
				response.setParticipationCommision(0);
			} else {
				response.setParticipationCommision(quotation.getParticipationCommission());
			}

			if (quotation.getTransactionId().getParticipationBasis() == null) {
				response.setParticipationBasis(" ");
			} else {
				response.setParticipationBasis(quotation.getTransactionId().getParticipationBasis());
			}
			response.setTenor(quotation.getTransactionId().getUsanceDays());
			if (quotation.getDiscountingCharges() == null) {
				response.setDiscountingCharges(0);
			} else {
				response.setDiscountingCharges(quotation.getDiscountingCharges());
			}
			if(quotation.getTransactionId().getSecTransactionType()==null) {
				response.setSecondaryTrType(" ");
			}else {
				response.setSecondaryTrType(quotation.getTransactionId().getSecTransactionType());
			}

			response.setObligorBank(quotation.getTransactionId().getLcIssuanceBank());
			response.setDistributingBank(quotation.getUserid().getBankName());

			if (quotation.getParticipationAmount() == null) {
				response.setParticipationAmount((float) 0);
			} else {
				response.setParticipationAmount(quotation.getParticipationAmount());
			}

			if (quotation.getTransactionId().getOfferedPrice() == null) {
				response.setOfferPrice(" ");
			} else {
				response.setOfferPrice(quotation.getTransactionId().getOfferedPrice());
			}
			response.setIsOffered(quotation.getIsOffered());
			response.setBankQuoteValidity(quotation.getValidityDate());
			response.setOtherTermsCondition(quotation.getTransactionId().getOtherCondition());

			return new ResponseEntity(response, HttpStatus.OK);
		}
		return null;
	}

	public List<String> customerUserIdSearch(String userId) {
		String val = Utility.getUserCountry();
		if (userId.substring(0, 2).equalsIgnoreCase("BA")) {
			if (!val.equalsIgnoreCase("All")) {
				List<String> list = (List<String>) Stream.<String>of(val.split(",")).collect(Collectors.toList());
				return this.customerRepository.userIdSearchByCountry(userId.toLowerCase(), list);
			}
			return this.customerRepository.userIdSearchForBaU(userId.toLowerCase());

		} else {
			if (!val.equalsIgnoreCase("All")) {
				List<String> list = (List<String>) Stream.<String>of(val.split(",")).collect(Collectors.toList());
				return this.customerRepository.userIdSearchByCountry(userId.toLowerCase(), list);
			}
			return this.customerRepository.userIdSearchForCustomer(userId.toLowerCase());
		}

	}

	public PagedResponse<?> getsecondaryTransaction(SearchRequest request) {
		Page<NimaiMmTransaction> transactionList;
		System.out.println("================_+++++++++++Login userId" + Utility.getUserId());
		PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(),
				request.getDirection().equalsIgnoreCase("desc")
						? Sort.by(new String[] { request.getSortBy() }).descending()
						: Sort.by(new String[] { request.getSortBy() }).ascending());
		String countryNames = Utility.getUserCountry();
		if (request.getCountry() == null) {
			logger.info("============inside firstcondiiton" + countryNames);
			if (countryNames != null && countryNames.equalsIgnoreCase("all")) {
				logger.info("============inside Seconcondiiton" + countryNames);
			} else if (countryNames != null && !countryNames.equalsIgnoreCase("all")) {
				logger.info("============inside Thirddiiton" + countryNames);
				request.setCountryNames(countryNames);
			}
		} else if (request.getCountry() == null && countryNames == null) {
			logger.info("============inside fourthdiiton" + countryNames);
			transactionList = null;
		}
		logger.info("final countries inside method" + countryNames);
		logger.info("Role selected: " + request.getRole());
		if (request.getRole().equalsIgnoreCase("Customer RM") || request.getRole().equalsIgnoreCase("Bank RM")) {
			logger.info("In CustomerRM / BankRM block");
			request.setCountryNames("All");
			logger.info("============inside customer/bankRM block: " + countryNames);
			logger.info("============inside customer/bankRM block: " + request.getCountry());
			transactionList = this.transactionsRepository.findAll(this.transactionTempSpecification.getFilter(request),
					(Pageable) pageRequest);
		} else {
			logger.info("In CustomerRM / BankRM else block");
			logger.info("============inside customer/bankRM else block: " + countryNames);
			logger.info("============inside customer/bankRM block: " + request.getCountry());

			if (request.getMobileNo() != null) {
				logger.info("final request.getMobileNo inside method3" + request.getEmailId());
				NimaiMCustomer custDetailsById = this.customerRepository.getUserByMobileNumber(request.getMobileNo());

				request.setUserId(custDetailsById.getUserid());
				request.setEmailId(null);
				request.setUserBranchEmail(null);
				request.setMobileNo(null);
				logger.info("userbranch email" + custDetailsById.getUserid());

			} else if (request.getEmailId() != null) {
				logger.info("final email inside method3" + request.getEmailId());
				NimaiMCustomer custDetailsById = this.customerRepository.getUserByEmailId(request.getEmailId());

				request.setUserId(custDetailsById.getUserid());
				request.setEmailId(null);
				request.setUserBranchEmail(null);
				logger.info("userbranch email" + custDetailsById.getUserid());
			} else if (request.getCompanyName() != null) {
				logger.info("final request.request.getCompanyName inside method3" + request.getCompanyName());
				NimaiMCustomer custDetailsById = this.customerRepository.getUserByCompanyName(request.getMobileNo());
				request.setUserId(custDetailsById.getUserid());
				request.setEmailId(null);
				request.setUserBranchEmail(null);
				request.setCompanyName(null);
				logger.info("userbranch email" + custDetailsById.getUserid());
			}
			if(request.getUserId()==null || request.getUserId().isEmpty()) {
				request.setSubscriberType("BANK");
				request.setBankType("UNDERWRITER");
			}
			else if(request.getUserId().substring(0, 2).equalsIgnoreCase("CU")) {
				request.setSubscriberType("CUSTOMER");
				request.setBankType(null);
			}else if(request.getUserId().substring(0, 2).equalsIgnoreCase("BC")) {
				request.setSubscriberType("BANK");
				request.setBankType("CUSTOMER");
			}else {
				request.setSubscriberType("BANK");
				request.setBankType("UNDERWRITER");
			}
		
			transactionList = this.transactionsRepository.findAll(this.secTransactionsSpecification.getFilter(request),
					(Pageable) pageRequest);
		}
		List<TransactionSearchResponse> responses = transactionList
				.map(cust -> ModelMapper.mapTransactionToResponse(cust,request)).getContent();
		return new PagedResponse(responses, transactionList.getNumber(), transactionList.getSize(),
				transactionList.getTotalElements(), transactionList.getTotalPages(), transactionList.isLast());
	}
}
