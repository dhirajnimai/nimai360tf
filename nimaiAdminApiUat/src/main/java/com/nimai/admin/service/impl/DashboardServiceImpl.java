package com.nimai.admin.service.impl;

import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMmTransaction;
import com.nimai.admin.model.NimaiSubscriptionDetails;
import com.nimai.admin.payload.CustomerStatResponse;
import com.nimai.admin.payload.DashCountryAnalysisResponse;
import com.nimai.admin.payload.DashNewUserStat;
import com.nimai.admin.payload.DashTransStat;
import com.nimai.admin.payload.DiscardResponse;
import com.nimai.admin.payload.PagedResponse;
import com.nimai.admin.payload.PendingRequestBean;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.payload.SubscriptionRenewalResponse;
import com.nimai.admin.repository.CustomerRepository;
import com.nimai.admin.repository.EmployeeRepository;
import com.nimai.admin.repository.SubscriptionDetailsRepository;
import com.nimai.admin.repository.TransactionsRepository;
import com.nimai.admin.service.DashboardService;
import com.nimai.admin.specification.CustomerSearchSpecification;
import com.nimai.admin.specification.SubscriptionDetailsSpecification;
import com.nimai.admin.specification.TransactionDashboardSpecification;
import com.nimai.admin.util.Utility;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
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
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
  private static Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
  
  @Autowired
  CustomerRepository custRepo;
  
  @Autowired
  CustomerSearchSpecification custSpecs;
  
  @Autowired
  CustomerRepository repo;
  
  @Autowired
  EmployeeRepository empRepo;
  
  @Autowired
  SubscriptionDetailsRepository subsRepo;
  
  @Autowired
  SubscriptionDetailsSpecification subSpecification;
  
  @Autowired
  TransactionDashboardSpecification transactionsSpecification;
  
  @Autowired
  TransactionsRepository transaRepo;
  
  public int getConfAwaited() {
    System.out.println(Utility.getUserCountry());
    return this.custRepo.getDashboardCount(1, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getpayApproval() {
    return this.custRepo.getDashboardCount(2, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getAssignRmCount() {
    return this.custRepo.getDashboardCount(3, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getPendingAssignRmCount() {
    return this.custRepo.getDashboardCount(4, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getUserCount() {
    return this.custRepo.getDashboardCount(5, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getKycApprovalCount() {
    return this.custRepo.getDashboardCount(6, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getGrantKycCount() {
    return this.custRepo.getDashboardCount(7, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getPendingKycCount(SearchRequest request) {
    if (request.getSubscriberType().equalsIgnoreCase("all"))
      return this.custRepo.getDashboardCount(8, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry()); 
    if (!request.getSubscriberType().equalsIgnoreCase("all") && request.getBankType() == null)
      return this.custRepo.getDashboardCount(8, request.getSubscriberType(), "", "", Integer.valueOf(1), "", "", "A", 
          Utility.getUserCountry()); 
    return this.custRepo.getDashboardCount(8, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "A", 
        Utility.getUserCountry());
  }
  
  public int getBankAndCustPendingKycCount(SearchRequest request) {
    if (request.getBankType() == null)
      return this.custRepo.getDashboardCount(25, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "A", 
          Utility.getUserId()); 
    return this.custRepo.getDashboardCount(25, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "A", 
        Utility.getUserId());
  }
  
  public int getSubsExpCount(Optional<SearchRequest> requests) {
    SearchRequest request = requests.get();
    System.out.println(request.getSubscriberType());
    if (request.getSubscriberType().equalsIgnoreCase("all"))
      return this.custRepo.getDashboardCount(9, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry()); 
    if (!request.getSubscriberType().equalsIgnoreCase("all") && request.getBankType() == null)
      return this.custRepo.getDashboardCount(9, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "A", 
          Utility.getUserCountry()); 
    return this.custRepo.getDashboardCount(10, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", 
        Utility.getUserCountry());
  }
  
  public int getPayPendingCount(SearchRequest request) {
    System.out.println(Utility.getUserCountry());
    if (request.getSubscriberType().equalsIgnoreCase("all"))
      return this.custRepo.getDashboardCount(11, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry()); 
    if (!request.getSubscriberType().equalsIgnoreCase("All") && request.getBankType() == null)
      return this.custRepo.getDashboardCount(11, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "A", 
          Utility.getUserCountry()); 
    return this.custRepo.getDashboardCount(12, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", 
        Utility.getUserCountry());
  }
  
  public int getCustBankPayPendingCount(SearchRequest request) {
    return this.custRepo.getDashboardCount(24, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "", Utility.getUserId());
  }
  
  public int getSubsGrantCount() {
    return this.custRepo.getDashboardCount(13, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getVasGrantCount() {
    return this.custRepo.getDashboardCount(14, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public int getDiscountCouponCount() {
    return this.custRepo.getDashboardCount(15, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
  }
  
  public Map<String, Object> getCustomerRevenue(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
      logger.info("Fromdate minus 30 days in getCustomerRevenue " + LocalDate.now().minusDays(30L).toString());
      logger.info("Todate getCustomerRevenue" + LocalDate.now().toString());
    } 
    logger.info("Fromdate minus in getCustomerRevenue from request " + request.getDateFrom());
    logger.info("Todate getCustomerRevenue from request" + request.getDateTo());
    logger.info("User countries in getCustomerRevenue" + 
        Utility.getUserCountry() + "_userId:_" + Utility.getUserId());
    return this.subsRepo.getRevenues(1, Date.valueOf(request.getDateFrom()), 
        Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserCountry());
  }
  
  public Object totalQuoteReceived(SearchRequest request) {
    Double amount = Double.valueOf(0.0D);
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
      logger.info("Fromdate minus 30 days in getCustomerRevenue " + LocalDate.now().minusDays(30L).toString());
      logger.info("Todate getCustomerRevenue" + LocalDate.now().toString());
    } 
    logger.info("Fromdate minus in getCustomerRevenue from request " + request.getDateFrom());
    logger.info("Todate getCustomerRevenue from request" + request.getDateTo());
    logger.info("User countries in getCustomerRevenue" + 
        Utility.getUserCountry() + "_userId:_" + Utility.getUserId());
    if (Utility.getUserCountry().equalsIgnoreCase("ALL")) {
      amount = this.subsRepo.gettotalQuoteReceived(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)));
    } else {
      amount = this.subsRepo.gettotalQuoteReceivedCountryNames(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserCountry());
    } 
    if (amount == null)
      amount = Double.valueOf(0.0D); 
    Double amountInUsd = Double.valueOf(Double.parseDouble((new DecimalFormat("##.##")).format(amount)));
    return amountInUsd;
  }
  
  public Map<String, Object> getBankCustomerRevenue(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
      logger.info("Fromdate minus 30 days in getBankCustomerRevenue " + LocalDate.now().minusDays(30L).toString());
      logger.info("Todate getBankCustomerRevenue" + LocalDate.now().toString());
    } 
    logger.info("Fromdate minus in getBankCustomerRevenue from request " + request.getDateFrom());
    logger.info("Todate getBankCustomerRevenue from request" + request.getDateTo());
    logger.info("User countries in getBankCustomerRevenue" + Utility.getUserCountry() + "_userId:_" + 
        Utility.getUserId());
    return this.subsRepo.getRevenues(2, Date.valueOf(request.getDateFrom()), 
        Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserCountry());
  }
  
  public Map<String, Object> getBankUwRevenue(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
      logger.info("Fromdate minus 30 days in getBankUwRevenue " + LocalDate.now().minusDays(30L).toString());
      logger.info("Todate getBankUwRevenue" + LocalDate.now().toString());
    } 
    logger.info("Fromdate minus in getBankUwRevenue from request " + request.getDateFrom());
    logger.info("Todate getBankUwRevenue from request" + request.getDateTo());
    logger.info("User countries in getBankUwRevenue" + 
        Utility.getUserCountry() + "_userId:_" + Utility.getUserId());
    return this.subsRepo.getRevenues(3, Date.valueOf(request.getDateFrom()), 
        Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserCountry());
  }
  
  public List<DashCountryAnalysisResponse> getCountryAnalysis() {
    List<Tuple> responses;
//    if (Utility.getUserCountry().equalsIgnoreCase("all")) {
//      responses = this.custRepo.getCountryDetailsAnalysis();
//    } else {
//      List<String> value = (List<String>)Stream.<String>of(Utility.getUserCountry().split(",", -1)).collect(Collectors.toList());
//      responses = this.custRepo.getCountryFilteredDetails(value);
//    } 
//    List<DashCountryAnalysisResponse> resp = (List<DashCountryAnalysisResponse>)responses.stream().map(res -> {
//          DashCountryAnalysisResponse analysis = new DashCountryAnalysisResponse();
//          analysis.setCountryName(((String)res.get("country_name") != null) ? (String)res.get("country_name") : "");
//          analysis.setTotalCustomers(Integer.valueOf((Integer.valueOf(((BigInteger)res.get("total_customers")).intValue()) != null) ? Integer.valueOf(((BigInteger)res.get("total_customers")).intValue()).intValue() : 0));
//          analysis.setTotalUnderwriters(Integer.valueOf((Integer.valueOf(((BigInteger)res.get("total_underwriter")).intValue()) != null) ? Integer.valueOf(((BigInteger)res.get("total_underwriter")).intValue()).intValue() : 0));
//          analysis.setTotalTrxn(Integer.valueOf((Integer.valueOf(((BigInteger)res.get("total_trxn")).intValue()) != null) ? Integer.valueOf(((BigInteger)res.get("total_trxn")).intValue()).intValue() : 0));
//          analysis.setCumulativeLcValue(Double.valueOf(((Double)res.get("cumulative_lc_amount") != null) ? ((Double)res.get("cumulative_lc_amount")).doubleValue() : 0.0D));
//          return analysis;
//        }).collect(Collectors.toList());
    return null;
  }
  
  public List<DashNewUserStat> getNewUserStat(SearchRequest request) {
    List<Tuple> userStat;
    if (request.getBankType() == null) {
      if (Utility.getUserCountry().equalsIgnoreCase("all")) {
        userStat = this.custRepo.getDashboardUserStat(request.getDateFrom());
      } else {
        userStat = this.custRepo.getDashboardCountryrUserStat(request.getDateFrom(), Utility.getUserCountry());
      } 
    } else if (Utility.getUserCountry().equalsIgnoreCase("all")) {
      userStat = this.custRepo.getDashboardBankStat(request.getDateFrom(), request.getBankType());
    } else {
      userStat = this.custRepo.getDashboardBankCountryStat(request.getDateFrom(), request.getBankType(), 
          Utility.getUserCountry());
    } 
    List<DashNewUserStat> resp = (List<DashNewUserStat>)userStat.stream().map(res -> {
          DashNewUserStat response = new DashNewUserStat();
          response.setMonth((String)res.get("month"));
          response.setCustomers(Integer.valueOf(((BigInteger)res.get("customers")).intValue()));
          response.setSubs_rate(Double.valueOf(((BigDecimal)res.get("subscription_rate")).doubleValue()).doubleValue());
          return response;
        }).collect(Collectors.toList());
    return resp;
  }
  
  public List<DashNewUserStat> getActiveUserStat(SearchRequest request) {
    List<Tuple> activeUserStat;
    if (request.getBankType() == null) {
      if (Utility.getUserCountry().equalsIgnoreCase("All")) {
        activeUserStat = this.subsRepo.getDashboardActiveCustStat(request.getDateFrom());
      } else {
        activeUserStat = this.subsRepo.getDashboardActiveCustCountryStat(request.getDateFrom(), 
            Utility.getUserCountry());
      } 
    } else if (Utility.getUserCountry().equalsIgnoreCase("all")) {
      activeUserStat = this.subsRepo.getDashboardActiveBankStat(request.getBankType(), request.getDateFrom());
    } else {
      activeUserStat = this.subsRepo.getDashboardActiveBankCountryStat(request.getBankType(), request
          .getDateFrom(), Utility.getUserCountry());
    } 
    List<DashNewUserStat> resp = (List<DashNewUserStat>)activeUserStat.stream().map(res -> {
          DashNewUserStat response = new DashNewUserStat();
          response.setMonth((String)res.get("month"));
          response.setCustomers(Integer.valueOf(((BigInteger)res.get("customers")).intValue()));
          return response;
        }).collect(Collectors.toList());
    return resp;
  }
  
  public List<DashTransStat> getTransactionStat(SearchRequest request) {
    List<Tuple> graph;
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    if (request.getBankType() == null)
      request.setBankType(""); 
    if (Utility.getUserCountry().equalsIgnoreCase("all")) {
      graph = this.transaRepo.getGenCumulativeTrxn(Date.valueOf(LocalDate.parse(request.getDateFrom())), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } else {
      graph = this.transaRepo.getGenCumulativeCountryTrxn(Date.valueOf(LocalDate.parse(request.getDateFrom())), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), Utility.getUserCountry());
    } 
    List<DashTransStat> responses = (List<DashTransStat>)graph.stream().map(res -> {
          DashTransStat response = new DashTransStat();
          response.setMonth((String)res.get("month"));
          response.setDay((Integer)res.get("weekDay"));
          response.setTrxn_count(Integer.valueOf(((BigInteger)res.get("trxn_count")).intValue()));
          response.setCumulative_amount(((Double)res.get("cumulative_amount")).longValue());
          return response;
        }).collect(Collectors.toList());
    return responses;
  }
  
  public List<Map<String, Object>> getQuotesStat(SearchRequest request) {
    List<Tuple> quotesList;
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    if (Utility.getUserCountry().equalsIgnoreCase("all")) {
      quotesList = this.transaRepo.getAvgQuote(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)));
    } else {
      quotesList = this.transaRepo.getAvgCountryQuote(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), 
          Utility.getUserCountry());
    } 
    List<Map<String, Object>> qList = (List<Map<String, Object>>)quotesList.stream().map(res -> {
          Map<String, Object> response = new HashMap<>();
          response.put("month", ((String)res.get("month") != null) ? res.get("month") : "");
          response.put("transaction_count", res.get("transaction_count"));
          response.put("day", res.get("day"));
          response.put("total_quotes", res.get("total_quotes"));
          return response;
        }).collect(Collectors.toList());
    return qList;
  }
  
  public int getOverallCustomers(SearchRequest request) {
    if (request.getSubscriberType().equalsIgnoreCase("Referrer"))
      return this.custRepo.getDashboardCount(27, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "", 
          Utility.getUserCountry()); 
    return this.custRepo.getDashboardCount(16, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "", 
        Utility.getUserCountry());
  }
  
  public int getOverallCustomersDate(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    return this.custRepo.getDashboardCount(16, request.getSubscriberType(), "", "", Integer.valueOf(0), request.getDateFrom(), request
        .getDateTo(), "A", Utility.getUserId());
  }
  
  public int getOverallBankUw(SearchRequest request) {
	  
    return this.custRepo.getDashboardCount(17, "Bank", request.getBankType(), "", Integer.valueOf(0), "", "", "", 
        Utility.getUserCountry());
  }
  
  public int getOverallAutoBankUw(SearchRequest request) {
	  
	    return this.custRepo.getDashboardCount(41, "Bank", request.getBankType(), "", Integer.valueOf(0), "", "", "", 
	        Utility.getUserCountry());
	  }
  
  public int getCustomerStatusTransc(SearchRequest request) {
    logger.info("Coutry names" + Utility.getUserCountry());
    if (Utility.getUserCountry().equalsIgnoreCase("All")) {
      logger.info("Country all country" + Utility.getUserCountry());
      return this.custRepo.getDashboardCount(18, "", "", request.getStatus(), Integer.valueOf(7), "", "", "", Utility.getUserCountry());
    } 
    List<String> value = (List<String>)Stream.<String>of(Utility.getUserCountry().split(",", -1)).collect(Collectors.toList());
    if (request.getStatus().equalsIgnoreCase("Expired")) {
      logger.info("Country Expired condition country" + Utility.getUserCountry());
      return this.custRepo.getDashboardCountByCountryWise(value);
    } 
    logger.info("Country Rejected condition country" + Utility.getUserCountry());
    return this.custRepo.getDashboardRejectedCountByCountryWise(value);
  }
  
  public int getCustTran(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    if (request.getBankType() == null)
      return this.custRepo.getDashboardCount(22, request.getSubscriberType(), "", "", Integer.valueOf(0), request.getDateFrom(), request
          .getDateTo(), "A", Utility.getUserId()); 
    return this.custRepo.getDashboardCount(22, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), request
        .getDateFrom(), request.getDateTo(), "B", Utility.getUserId());
  }
  
  public int getCustTrxn(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    return this.custRepo.getDashboardCount(18, "", "", request.getStatus(), Integer.valueOf(0), request.getDateFrom(), request
        .getDateTo(), "", Utility.getUserId());
  }
  
  public int getCustActiveTrxnCount() {
    return this.custRepo.getDashboardCount(19, "Customer", "", "", Integer.valueOf(0), "", "", "", Utility.getUserId());
  }
  
  public int getCustTrxnEXp(SearchRequest request) {
    if (request.getBankType() != null)
      return this.custRepo.getDashboardCount(20, request.getSubscriberType(), "Customer", "", Integer.valueOf(3), "", "", "A", 
          Utility.getUserId()); 
    return this.custRepo.getDashboardCount(20, request.getSubscriberType(), "", "", Integer.valueOf(3), "", "", "", 
        Utility.getUserId());
  }
  
  public int getSubsExpBankAndCustCount(SearchRequest request) {
    if (request.getBankType() == null)
      return this.custRepo.getDashboardCount(26, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "A", 
          Utility.getUserId()); 
    return this.custRepo.getDashboardCount(26, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", 
        Utility.getUserId());
  }
  
  public int getCustTrxnBank(Optional<SearchRequest> request) {
    if (((SearchRequest)request.get()).getDateFrom() == null && ((SearchRequest)request.get()).getDateTo() == null) {
      ((SearchRequest)request.get()).setDateFrom(LocalDate.now().minusDays(30L).toString());
      ((SearchRequest)request.get()).setDateTo(LocalDate.now().toString());
    } 
    return this.custRepo.getDashboardCount(18, ((SearchRequest)request.get()).getSubscriberType(), ((SearchRequest)request.get()).getBankType(), ((SearchRequest)request
        .get()).getStatus(), Integer.valueOf(0), ((SearchRequest)request.get()).getDateFrom(), ((SearchRequest)request.get()).getDateTo(), "A", 
        Utility.getUserId());
  }
  
  public int getTotalCustomers(Optional<SearchRequest> request) {
    if (((SearchRequest)request.get()).getDateFrom() == null && ((SearchRequest)request.get()).getDateTo() == null) {
      ((SearchRequest)request.get()).setDateFrom(LocalDate.now().minusDays(30L).toString());
      ((SearchRequest)request.get()).setDateTo(LocalDate.now().toString());
    } 
    return this.custRepo.getDashboardCount(17, "Bank", ((SearchRequest)request.get()).getBankType(), "", Integer.valueOf(0), ((SearchRequest)request.get()).getDateFrom(), ((SearchRequest)request
        .get()).getDateTo(), "A", Utility.getUserId());
  }
  
  public int getBankAs(SearchRequest request) {
    return this.custRepo.getDashboardCount(17, "Bank", request.getBankType(), "", Integer.valueOf(1), "", "", "A", Utility.getUserId());
  }
  
	@Override
	public int getBankQuotesCount(Optional<SearchRequest> request) {

		if (request.get().getDateFrom() == null && request.get().getDateTo() == null) {
			request.get().setDateFrom(LocalDate.now().minusDays(30).toString());
			request.get().setDateTo(LocalDate.now().toString());
		}
		if (request.get().getStatus() != null & request.get().getSubscriberType().equalsIgnoreCase("Customer")) {
			return custRepo.getDashboardCount(21, request.get().getSubscriberType(), "", request.get().getStatus(), 0,
					request.get().getDateFrom(), request.get().getDateTo(), "A", Utility.getUserId());
		} else if (request.get().getStatus() != null & request.get().getSubscriberType().equalsIgnoreCase("Bank")) {
			return custRepo.getDashboardCount(21, request.get().getSubscriberType(), "", request.get().getStatus(), 0,
					request.get().getDateFrom(), request.get().getDateTo(), "B", Utility.getUserId());
		} else {
			return custRepo.getDashboardCount(21, request.get().getSubscriberType(), "", "", 0,
					request.get().getDateFrom(), request.get().getDateTo(), "", Utility.getUserId());
		}
	}
  
  public PagedResponse<?> getCustDiscardO(Optional<SearchRequest> request) {
    if (((SearchRequest)request.get()).getDateFrom() == null)
      ((SearchRequest)request.get()).setDateFrom("7"); 
    Integer duration = Integer.valueOf(Integer.parseInt(((SearchRequest)request.get()).getDateFrom()) + 1);
    SearchRequest requests = request.get();
    requests.setDateTo(LocalDate.parse(LocalDate.now().toString()).plusDays(duration.intValue()).toString());
    requests.setDateFrom(LocalDate.now().toString());
    PageRequest pageRequest = PageRequest.of(requests.getPage(), requests.getSize(), 
        requests.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { requests.getSortBy() }).descending() : 
        Sort.by(new String[] { requests.getSortBy() }).ascending());
    Page<NimaiMmTransaction> transList = this.transaRepo.findAll(this.transactionsSpecification.getFilter(requests), (Pageable)pageRequest);
    List<DiscardResponse> resp = (List<DiscardResponse>)transList.stream().map(res -> {
          Period period = Period.between(LocalDate.parse(LocalDate.now().toString()), LocalDate.parse(res.getValidity().substring(0, 10)));
          DiscardResponse response = new DiscardResponse();
          response.setTrxnId(res.getTransactionId());
          response.setCustomer(res.getApplicantName());
          response.setExpiresIn(String.valueOf(period.getDays()) + " days");
          return response;
        }).collect(Collectors.toList());
    return new PagedResponse(resp, transList.getNumber(), transList.getSize(), transList.getTotalElements(), transList
        .getTotalPages(), transList.isLast());
  }
  
  public PagedResponse<?> getCustStat(SearchRequest requests) {
    PageRequest pageRequest = PageRequest.of(requests.getPage(), requests.getSize(), 
        requests.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { requests.getSortBy() }).descending() : 
        Sort.by(new String[] { requests.getSortBy() }).ascending());
    Page<NimaiMCustomer> custStatList = this.custRepo.findAll(this.custSpecs.getFilter(requests), (Pageable)pageRequest);
    List<CustomerStatResponse> responses = (List<CustomerStatResponse>)custStatList.stream().map(res -> {
          CustomerStatResponse response = new CustomerStatResponse();
          response.setUserid(res.getUserid());
          response.setCustomer(res.getCompanyName());
          response.setPayment(res.getModeOfPayment());
          response.setKyc(res.getKycStatus());
          return response;
        }).collect(Collectors.toList());
    return new PagedResponse(responses, custStatList.getNumber(), custStatList.getSize(), custStatList
        .getTotalElements(), custStatList.getTotalPages(), custStatList.isLast());
  }
  
  public PagedResponse<?> getSubStat(SearchRequest request) {
    if (request.getDateFrom() == null)
      request.setDateFrom("7"); 
    Integer duration = Integer.valueOf(Integer.parseInt(request.getDateFrom()) + 1);
    request.setDateTo(LocalDate.parse(LocalDate.now().toString()).plusDays(duration.intValue()).toString());
    request.setDateFrom(LocalDate.now().toString());
    PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), 
        request.getDirection().equalsIgnoreCase("desc") ? Sort.by(new String[] { request.getSortBy() }).descending() : 
        Sort.by(new String[] { request.getSortBy() }).ascending());
    Page<NimaiSubscriptionDetails> subsDetails = this.subsRepo.findAll(this.subSpecification.getFilter(request), (Pageable)pageRequest);
    List<SubscriptionRenewalResponse> response = (List<SubscriptionRenewalResponse>)subsDetails.stream().map(res -> {
          long exp = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(res.getSplanEndDate().toString()));
          SubscriptionRenewalResponse resp = new SubscriptionRenewalResponse();
          resp.setUserid(res.getUserid().getUserid());
          resp.setCustomer(res.getUserid().getCompanyName());
          resp.setExpiryDate(res.getSplanEndDate());
          if (exp <= 0L) {
            resp.setExpiresIn("Expired");
          } else {
            resp.setExpiresIn(exp + " days");
          } 
          return resp;
        }).collect(Collectors.toList());
    return new PagedResponse(response, subsDetails.getNumber(), subsDetails.getSize(), subsDetails
        .getTotalElements(), subsDetails.getTotalPages(), subsDetails.isLast());
  }
  
  public List<DashTransStat> getCustTransactionStat(SearchRequest request) {
		if (request.getDateFrom() == null && request.getDateTo() == null) {
			request.setDateFrom(LocalDate.now().minusDays(30).toString());
			request.setDateTo(LocalDate.now().toString());
		}
		List<Tuple> graph;
		if (request.getSubscriberType().equalsIgnoreCase("Bank")) {
			graph = transaRepo.getCumulativeBankTrxn(request.getSubscriberType(),
					Date.valueOf(LocalDate.parse(request.getDateFrom())),
					Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserId());
		}
		graph = transaRepo.getCumulativeCustTrxn(request.getSubscriberType(),
				Date.valueOf(LocalDate.parse(request.getDateFrom())),
				Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserId());
		System.out.println(graph.isEmpty());
		List<DashTransStat> responses = graph.stream().map(res -> {
			DashTransStat response = new DashTransStat();
			response.setMonth((String) res.get("month"));
			response.setDay((Integer) res.get("weekDay"));
			response.setTrxn_count((Integer) ((BigInteger) res.get("trxn_count")).intValue());
			response.setCumulative_amount((long) ((Double) res.get("cumulative_amount")).longValue());
			return response;
		}).collect(Collectors.toList());
		
    return responses;
  }
  
  public int getBankAwaitedQuotes() {
    return this.custRepo.getDashboardCount(23, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserId());
  }
  
  public List<Map<String, Object>> getBankAvgStat(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    List<Tuple> quotesList = this.transaRepo.getAvgBankQuote(Date.valueOf(request.getDateFrom()), 
        Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), Utility.getUserId());
    List<Map<String, Object>> qList = (List<Map<String, Object>>)quotesList.stream().map(res -> {
          Map<String, Object> response = new HashMap<>();
          response.put("month", ((String)res.get("month") != null) ? res.get("month") : "");
          response.put("transaction_count", res.get("transaction_count"));
          response.put("day", res.get("day"));
          response.put("total_quotes", res.get("total_quotes"));
          return response;
        }).collect(Collectors.toList());
    return qList;
  }
  
  public List<Map<String, Object>> getTransactionComp(SearchRequest request) {
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    List<Tuple> compList = this.transaRepo.getCustTransComparision(Date.valueOf(request.getDateFrom()), 
        Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1L)), Utility.getUserId());
    List<Map<String, Object>> qList = (List<Map<String, Object>>)compList.stream().map(res -> {
          Map<String, Object> response = new WeakHashMap<>();
          response.put("month", ((String)res.get("Month") != null) ? res.get("Month") : Integer.valueOf(0));
          response.put("day", Integer.valueOf(((Integer)res.get("day") != null) ? ((Integer)res.get("day")).intValue() : 0));
          response.put("Confirmation", ((BigInteger)res.get("Confirmation") != null) ? res.get("Confirmation") : Integer.valueOf(0));
          response.put("Discounting", ((BigInteger)res.get("Discounting") != null) ? res.get("Discounting") : Integer.valueOf(0));
          response.put("Refinance", ((BigInteger)res.get("Refinance") != null) ? res.get("Refinance") : Integer.valueOf(0));
          response.put("ConfirmAndDiscount", ((BigInteger)res.get("Confirm And Discount") != null) ? res.get("Confirm And Discount") : Integer.valueOf(0));
          response.put("Banker", ((BigInteger)res.get("Banker") != null) ? res.get("Banker") : Integer.valueOf(0));
          return response;
        }).collect(Collectors.toList());
    return qList;
  }
  
  public int getReferrers(SearchRequest request) {
    System.out.println("======IN REFERRER========");
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      System.out.println("=== Searching Default referrer data ====");
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    } 
    System.out.println("==== Searching Referrer Data from " + request.getDateFrom() + " to " + request.getDateTo());
    return this.custRepo.getDashboardCount(38, request.getSubscriberType(), Utility.getUserId(), "", 
        Integer.valueOf(0), request.getDateFrom(), request.getDateTo(), "", 
        Utility.getUserCountry());
  }
  
  public List<DashCountryAnalysisResponse> getLatestCountryAnalysis(SearchRequest request) {
    if(request.getDateTo()==null || request.getDateFrom()==null){
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
    }
    System.out.println("setDateFrom "+request.getDateFrom());
    System.out.println("setDateTo "+request.getDateTo());
    List<Tuple> responses;
    if (Utility.getUserCountry().equalsIgnoreCase("all")) {
      responses = this.custRepo.getCountryDetailsAnalysis(Date.valueOf(request.getDateFrom()),Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)));
    } else {
      List<String> value = (List<String>)Stream.<String>of(Utility.getUserCountry().split(",", -1)).collect(Collectors.toList());
      responses = this.custRepo.getCountryFilteredDetails(value,Date.valueOf(request.getDateFrom()),Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)));
    }
    List<DashCountryAnalysisResponse> resp = (List<DashCountryAnalysisResponse>)responses.stream().map(res -> {
          DashCountryAnalysisResponse analysis = new DashCountryAnalysisResponse();
          analysis.setCountryName(((String)res.get("REGISTERED_COUNTRY") != null) ? (String)res.get("REGISTERED_COUNTRY") : "");
          analysis.setTotalCustomers(Integer.valueOf((Integer.valueOf(((BigInteger)res.get("total_customers")).intValue()) != null) ? Integer.valueOf(((BigInteger)res.get("total_customers")).intValue()).intValue() : 0));
          analysis.setTotalUnderwriters(Integer.valueOf((Integer.valueOf(((BigInteger)res.get("total_underwriter")).intValue()) != null) ? Integer.valueOf(((BigInteger)res.get("total_underwriter")).intValue()).intValue() : 0));
          analysis.setTotalTrxn(Integer.valueOf((Integer.valueOf(((BigInteger)res.get("total_trxn")).intValue()) != null) ? Integer.valueOf(((BigInteger)res.get("total_trxn")).intValue()).intValue() : 0));
          analysis.setCumulativeLcValue(Double.valueOf(((Double)res.get("cumulative_lc_amount") != null) ? ((Double)res.get("cumulative_lc_amount")).doubleValue() : 0.0D));
          return analysis;
        }).collect(Collectors.toList());
    return resp;
  }
  
  public Object totalQuoteAccepted(SearchRequest request) {
    Double amount = Double.valueOf(0.0D);
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
      logger.info("Fromdate minus 30 days in getCustomerRevenue " + LocalDate.now().minusDays(30L).toString());
      logger.info("Todate getCustomerRevenue" + LocalDate.now().toString());
    } 
    logger.info("Fromdate minus in getCustomerRevenue from request " + request.getDateFrom());
    logger.info("Todate getCustomerRevenue from request" + request.getDateTo());
    logger.info("User countries in getCustomerRevenue" + 
        Utility.getUserCountry() + "_userId:_" + Utility.getUserId());
    if (Utility.getUserCountry().equalsIgnoreCase("ALL")) {
      amount = this.subsRepo.gettotalQuoteAccepted(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)));
    } else {
      amount = this.subsRepo.gettotalQuoteAcceptedCountryNames(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserCountry());
    } 
    if (amount == null)
      amount = Double.valueOf(0.0D); 
    Double amountInUsd = Double.valueOf(Double.parseDouble((new DecimalFormat("##.##")).format(amount)));
    return amountInUsd;
  }
  
  public Object totalQuoteClosed(SearchRequest request) {
	 
    Double amount = 0.0D;
    if (request.getDateFrom() == null && request.getDateTo() == null) {
      request.setDateFrom(LocalDate.now().minusDays(30L).toString());
      request.setDateTo(LocalDate.now().toString());
      logger.info("Fromdate minus 30 days in getCustomerRevenue " + LocalDate.now().minusDays(30L).toString());
      logger.info("Todate getCustomerRevenue" + LocalDate.now().toString());
    } 
    logger.info("Fromdate minus in getCustomerRevenue from request " + request.getDateFrom());
    logger.info("Todate getCustomerRevenue from request" + request.getDateTo());
    logger.info("User countries in getCustomerRevenue" + 
        Utility.getUserCountry() + "_userId:_" + Utility.getUserId());
    if (Utility.getUserCountry().equalsIgnoreCase("ALL")) {
      amount = this.subsRepo.gettotalQuoteClosed(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)));
    } else {
      amount = this.subsRepo.gettotalQuoteClosedCountryNames(Date.valueOf(request.getDateFrom()), 
          Date.valueOf(LocalDate.parse(request.getDateTo()).plusDays(1)), Utility.getUserCountry());
    } 
    if (amount == null)
      amount = 0.0D;
    return Double.parseDouble((new DecimalFormat("##.##")).format(amount));
  }
  
  public PagedResponse<?> getPendingWireTfList(SearchRequest request) {
    return null;
  }
  
  public PendingRequestBean getPendingRequests(SearchRequest request) {
    PendingRequestBean response = new PendingRequestBean();
    String countryNames = Utility.getUserCountry();
    String rmId = Utility.getUserId();
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
    } else if (countryNames != null && request.getCountry() != null) {
      request.setCountryNames(request.getCountry());
    } else if (countryNames != null || request.getCountry() == null) {
    
    } 
    List<String> value = (List<String>)Stream.<String>of(request.getCountryNames().split(",", -1)).collect(Collectors.toList());
    System.out.println("Values BankService: " + value);
    if ((request.getBankType() == null || request.getBankType().isEmpty() || request
      .getRole().equalsIgnoreCase("Management") || request
      .getRole().equalsIgnoreCase("Ops Admin")) && request
      .getSubscriberType().equalsIgnoreCase("All")) {
      System.out.println("Inside All ");
      response = getAllData(countryNames, request, response, value, rmId);
    } else if ((request.getBankType() == null || request.getBankType().isEmpty()) && request.getSubscriberType().equalsIgnoreCase("Customer")) {
      System.out.println("Inside the  customer ");
      response = getCustomerData(countryNames, request, response, value, rmId);
    } else if ((request.getBankType() == null || request.getBankType().isEmpty()) && request.getSubscriberType().equalsIgnoreCase("Referrer")) {
      System.out.println("Inside the referrer ");
      response = getReferrerdata(countryNames, request, value, response);
    } else if (request.getBankType().equalsIgnoreCase("Customer") && request
      .getSubscriberType().equalsIgnoreCase("Bank")) {
      System.out.println("Inside the bank as customer ");
      response = getBankAsCustomerdata(countryNames, request, response, value, rmId);
    } else if (request.getBankType().equalsIgnoreCase("Underwriter") && request
      .getSubscriberType().equalsIgnoreCase("Bank")) {
      System.out.println("Inside the bank as Underwriter ");
      response = getBankAsUnderwriterdata(countryNames, request, value, response);
    } 
    return response;
  }
  
  private PendingRequestBean getReferrerdata(String countryNames, SearchRequest request, List<String> value, PendingRequestBean response) {
    long pendingKyc, kycApproval = this.custRepo.getRefPendingAllKYC(value);
    long grntkycApproval = this.custRepo.getRefCustGrantKYC(value);
    long paymentApprovalList = 0L;
    long grantRm = this.custRepo.getDashboardCount(4, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantPayment = 0L;
    long assignRm = this.custRepo.getDashboardCount(3, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantUser = this.custRepo.getDashboardCount(5, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long subExp30Days = this.custRepo.getDashboardCount(9, "Referrer", "", "", Integer.valueOf(0), "", "", "A", Utility.getUserCountry());
    if (Utility.getUserCountry().equalsIgnoreCase("All")) {
      pendingKyc = this.custRepo.getRefpendingKycNullNew();
    } else {
      pendingKyc = this.custRepo.getRefpendingKycCountryWiseNullNew(value);
    } 
    long paymentPending = 0L;
    response.setPaymentApproval(paymentApprovalList);
    response.setGrantPayment(grantPayment);
    response.setAssignRm(assignRm);
    response.setGrantRM(grantRm);
    response.setGrantUser(grantUser);
    response.setKycApproval(kycApproval);
    response.setGrantKyc(grntkycApproval);
    response.setSubPlanExpiring30Days(subExp30Days);
    response.setKycPendingUser(pendingKyc);
    response.setPaymentPendingUser(paymentPending);
    return response;
  }
  
  private PendingRequestBean getBankAsUnderwriterdata(String countryNames, SearchRequest request, List<String> value, PendingRequestBean response) {
    long pendingKyc, kycApproval = this.custRepo.getBankAsUnderPendingAllKYC(value);
    long grntkycApproval = this.custRepo.getBankAsUnderGrantKYC(value);
    long paymentApprovalList = this.custRepo.getDashboardCount(30, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantRm = this.custRepo.getDashboardCount(4, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantPayment = this.custRepo.getDashboardCount(33, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long assignRm = this.custRepo.getDashboardCount(3, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantUser = this.custRepo.getDashboardCount(5, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    this.custRepo.getDashboardCount(12, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", 
        Utility.getUserCountry());
    long subExp30Days = this.custRepo.getDashboardCount(10, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    if (Utility.getUserCountry().equalsIgnoreCase("All")) {
      pendingKyc = this.custRepo.getBankAsUndependingKycNullNew();
    } else {
      pendingKyc = this.custRepo.getCountryBankAsUndependingKycNullNew(value);
    } 
    long paymentPending = this.custRepo.getDashboardCountBank(request.getSubscriberType(), request.getBankType(), value);
    response.setPaymentApproval(paymentApprovalList);
    response.setGrantPayment(grantPayment);
    response.setAssignRm(assignRm);
    response.setGrantRM(grantRm);
    response.setGrantUser(grantUser);
    response.setKycApproval(kycApproval);
    response.setGrantKyc(grntkycApproval);
    response.setSubPlanExpiring30Days(subExp30Days);
    response.setKycPendingUser(pendingKyc);
    response.setPaymentPendingUser(paymentPending);
    return response;
  }
  
  private PendingRequestBean getBankAsCustomerdata(String countryNames, SearchRequest request, PendingRequestBean response, List<String> value, String rmId) {
    long pendingKyc, kycApproval = this.custRepo.getBankAsCustPendingAllKYC(value);
    long grntkycApproval = this.custRepo.getBankAsCustGrantKYC(value);
    long paymentApprovalList = this.custRepo.getDashboardCount(29, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantRm = this.custRepo.getDashboardCount(4, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantPayment = this.custRepo.getDashboardCount(32, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long assignRm = this.custRepo.getDashboardCount(3, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantUser = this.custRepo.getDashboardCount(5, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long subExp30Days = this.custRepo.getDashboardCount(10, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    if (Utility.getUserCountry().equalsIgnoreCase("All")) {
      pendingKyc = this.custRepo.getBankAsCustpendingKycNullNew();
    } else {
      pendingKyc = this.custRepo.getCountryBankAsCustpendingKycNullNew(value);
    } 
    long paymentPending = this.custRepo.getDashboardCountBank(request.getSubscriberType(), request.getBankType(), value);
    System.out.println("Bank as customer data values" + paymentPending);
    response.setPaymentApproval(paymentApprovalList);
    response.setGrantPayment(grantPayment);
    response.setAssignRm(assignRm);
    response.setGrantRM(grantRm);
    response.setGrantUser(grantUser);
    response.setKycApproval(kycApproval);
    response.setGrantKyc(grntkycApproval);
    response.setSubPlanExpiring30Days(subExp30Days);
    response.setKycPendingUser(pendingKyc);
    response.setPaymentPendingUser(paymentPending);
    return response;
  }
  
  private PendingRequestBean getCustomerData(String countryNames, SearchRequest request, PendingRequestBean response, List<String> value, String rmId) {
    long pendingKyc, kycApproval = this.custRepo.getCustPendingAllKYC(value);
    long grntkycApproval = this.custRepo.getCustGrantKYC(value);
    long paymentApprovalList = this.custRepo.getDashboardCount(28, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantRm = this.custRepo.getDashboardCount(4, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantPayment = this.custRepo.getDashboardCount(31, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long assignRm = this.custRepo.getDashboardCount(3, request.getSubscriberType(), request.getBankType(), "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantUser = this.custRepo.getDashboardCount(5, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long subExp30Days = this.custRepo.getDashboardCount(9, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "A", Utility.getUserCountry());
    if (Utility.getUserCountry().equalsIgnoreCase("All")) {
      pendingKyc = this.custRepo.getCustpendingKycNullNew();
    } else {
      System.out.println("Countrynames inside the conditions" + countryNames);
      pendingKyc = this.custRepo.getCountryCustpendingKycNullNew(value);
    } 
    long paymentPending = this.custRepo.getDashboardCountCustomer(request.getSubscriberType(), value);
    response.setPaymentApproval(paymentApprovalList);
    response.setGrantPayment(grantPayment);
    response.setAssignRm(assignRm);
    response.setGrantRM(grantRm);
    response.setGrantUser(grantUser);
    response.setKycApproval(kycApproval);
    response.setGrantKyc(grntkycApproval);
    response.setSubPlanExpiring30Days(subExp30Days);
    response.setKycPendingUser(pendingKyc);
    response.setPaymentPendingUser(paymentPending);
    return response;
  }
  
  private PendingRequestBean getAllData(String countryNames, SearchRequest request, PendingRequestBean response, List<String> value, String rmId) {
    long pendingKyc;
    System.out.println("===========Inside  all condition=========");
    long kycApproval = this.custRepo.getPendingAllKYC(value);
    long grntkycApproval = this.custRepo.getGrantKYC(value);
    long paymentApprovalList = this.custRepo.getDashboardCount(1, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantRm = this.custRepo.getDashboardCount(37, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantPayment = this.custRepo.getDashboardCount(2, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long assignRm = this.custRepo.getDashboardCount(36, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long grantUser = this.custRepo.getDashboardCount(5, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    long subExp30Days = this.custRepo.getDashboardCount(9, "", "", "", Integer.valueOf(0), "", "", "", Utility.getUserCountry());
    if (Utility.getUserCountry().equalsIgnoreCase("All")) {
      pendingKyc = this.custRepo.getpendingKycNullNew();
    } else {
      pendingKyc = this.custRepo.getCountrypendingKycNullNew(value);
    } 
    long paymentPending = this.custRepo.getDashboardCountByQuery(value);
    response.setPaymentApproval(paymentApprovalList);
    response.setGrantPayment(grantPayment);
    response.setAssignRm(assignRm);
    response.setGrantRM(grantRm);
    response.setGrantUser(grantUser);
    response.setKycApproval(kycApproval);
    response.setGrantKyc(grntkycApproval);
    response.setSubPlanExpiring30Days(subExp30Days);
    response.setKycPendingUser(pendingKyc);
    response.setPaymentPendingUser(paymentPending);
    return response;
  }
  
  public int getOverallReferrer(SearchRequest request) {
    return this.custRepo.getDashboardCount(39, request.getSubscriberType(), "", "", Integer.valueOf(0), "", "", "", 
        Utility.getUserCountry());
  }


}
