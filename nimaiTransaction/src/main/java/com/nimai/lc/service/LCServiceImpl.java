package com.nimai.lc.service;



import com.nimai.lc.bean.BankDetailsBean;
import com.nimai.lc.utility.ErrorDescription;
import org.springframework.http.HttpStatus;
import com.nimai.lc.entity.NimaiSubscriptionDetails;
import com.nimai.lc.entity.OfflineTxnBank;
import com.nimai.lc.payload.GenericResponse;
import org.springframework.http.ResponseEntity;
import com.nimai.lc.entity.Goods;
import com.nimai.lc.bean.NimaiCustomerBean;
import com.nimai.lc.entity.NimaiLCPort;
import com.nimai.lc.entity.NimaiLCTemp;
import com.nimai.lc.entity.NimaiOfflineUser;

import java.text.ParseException;
import com.nimai.lc.bean.CustomerTransactionBean;
import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.nimai.lc.entity.NimaiEmailSchedulerAlertToBanks;
import com.nimai.lc.bean.EligibleEmailList;
import com.nimai.lc.bean.EligibleEmailBeanResponse;
import java.util.Collection;
import java.util.ArrayList;
import com.nimai.lc.utility.ModelMapperUtil;
import com.nimai.lc.utility.RegistrationId;
import com.nimai.lc.entity.NimaiClient;
import com.nimai.lc.entity.QuotationMaster;
import com.nimai.lc.entity.TransactionActivity;

import org.json.JSONException;
import java.io.IOException;
import com.nimai.lc.utility.AESUtil;
import com.nimai.lc.utility.AppConstants;
import com.nimai.lc.bean.QuotationBean;
import com.nimai.lc.bean.SelectBankUser;

import java.util.Date;
import com.nimai.lc.bean.NimaiLCMasterBean;
import com.nimai.lc.bean.OfflineTxnBankBean;

import java.text.DateFormat;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Random;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import com.nimai.lc.entity.NewRequestEntity;
import com.nimai.lc.entity.NimaiLCMaster;
import com.nimai.lc.entity.AdditionalUserList;
import com.nimai.lc.entity.Countrycurrency;
import java.util.List;
import com.nimai.lc.entity.NimaiLC;
import com.nimai.lc.bean.NimaiLCBean;
import org.springframework.beans.factory.annotation.Value;
import javax.persistence.EntityManagerFactory;
import com.nimai.lc.repository.TransactionSavingRepo;
import com.nimai.lc.repository.NimaiClientRepository;
import com.nimai.lc.repository.GoodsRepository;
import com.nimai.lc.repository.AdditionalOfflineUser;
import com.nimai.lc.repository.CountryRepository;
import com.nimai.lc.repository.CountrycurrencyRepository;
import com.nimai.lc.repository.LCPortRepository;
import com.nimai.lc.repository.LCGoodsRepository;
import com.nimai.lc.repository.LCCountryRepository;
import com.nimai.lc.repository.QuotationMasterRepository;
import com.nimai.lc.repository.QuotationRepository;
import com.nimai.lc.repository.TransactionActivityRepo;
import com.nimai.lc.repository.LCMasterRepository;
import com.nimai.lc.repository.LCRepository;
import com.nimai.lc.repository.LCTempRepository;
import com.nimai.lc.repository.NimaiEmailSchedulerAlertToBanksRepository;
import com.nimai.lc.repository.NimaiOfflineDetailsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import com.nimai.lc.repository.NimaiSystemConfigRepository;
import com.nimai.lc.repository.OfflineTxnBankRepo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nimai.lc.entity.*;
import com.nimai.lc.repository.*;

@Service
public class LCServiceImpl implements LCService
{
    @Autowired
    NimaiSystemConfigRepository systemConfig;
    
    @Autowired
    NimaiOfflineDetailsRepository offDetailsRepo;
    @Autowired
    NimaiEmailSchedulerAlertToBanksRepository userDao;
    @Autowired
    LCRepository lcrepo;
    @Autowired
    LCMasterRepository lcmasterrepo;
    @Autowired
    QuotationRepository quoterepo;
    @Autowired
    QuotationMasterRepository quotemasterrepo;
    @Autowired
    LCCountryRepository lccountryrepo;
    @Autowired
    LCGoodsRepository lcgoodsrepo;
    @Autowired
    LCPortRepository lcportrepo;
    @Autowired
    CountrycurrencyRepository countryrepo;
    @Autowired
    CountryRepository countryRepo;
    @Autowired
    GoodsRepository goodsRepo;
    @Autowired
    NimaiClientRepository customerRepo;
    @Autowired
    TransactionSavingRepo trSavingRepo;
    @Autowired
    QuotationService quotationService;
    @Autowired
    CurrencyConverterService currencyService;
    @Autowired
	RegistrationId userid;
    @Autowired
	AdditionalOfflineUser offlineRepo;
    @Autowired
	OfflineTxnBankRepo offlineTxnBankRepo;
    @Autowired
    EntityManagerFactory em;
	@Autowired
	NimaiOfflineDetailsRepository offUsrRepo;
     @Autowired
    NimaiMBranchRepository brRepo;
	@Autowired
    LCTempRepository lctemprepo;
    
    private String creditBoundary;
    
    @Autowired
    TransactionActivityRepo trActivity;
    
    
    @Value("${postpaid.credit.boundary}")
    private String postpaidCreditBoundary;
    
    @Value("${prepaid.credit.boundary}")
    private String prepaidCreditBoundary;
    
    public void saveLCdetails(final NimaiLCBean nimailcbean, final String tid) {
        final NimaiLC nimailc = new NimaiLC();
        System.out.println("transaction id= " + tid);
        nimailc.setTransactionId(tid);
        nimailc.setUserId(nimailcbean.getUserId());
        nimailc.setRequirementType(nimailcbean.getRequirementType());
        nimailc.setlCIssuanceBank(nimailcbean.getlCIssuanceBank());
        nimailc.setlCIssuanceBranch(nimailcbean.getlCIssuanceBranch());
        nimailc.setSwiftCode(nimailcbean.getSwiftCode());
        nimailc.setlCIssuanceCountry(nimailcbean.getlCIssuanceCountry());
        nimailc.setlCIssuingDate(nimailcbean.getlCIssuingDate());
        nimailc.setlCExpiryDate(nimailcbean.getlCExpiryDate());
        nimailc.setClaimExpiryDate(nimailcbean.getClaimExpiryDate());
        nimailc.setBgType(nimailcbean.getBgType());
        nimailc.setlCValue(nimailcbean.getlCValue());
        nimailc.setlCCurrency(nimailcbean.getlCCurrency());
        nimailc.setLastShipmentDate(nimailcbean.getLastShipmentDate());
        nimailc.setNegotiationDate(nimailcbean.getNegotiationDate());
        nimailc.setPaymentPeriod(nimailcbean.getPaymentPeriod());
        nimailc.setPaymentTerms(nimailcbean.getPaymentTerms());
        nimailc.setTenorEndDate(nimailcbean.getTenorEndDate());
        nimailc.setUserType(nimailcbean.getUserType());
        nimailc.setApplicantName(nimailcbean.getApplicantName());
        nimailc.setApplicantCountry(nimailcbean.getApplicantCountry());
        nimailc.setApplicantContactPerson(nimailcbean.getApplicantContactPerson());
        nimailc.setApplicantContactPersonEmail(nimailcbean.getApplicantContactPersonEmail());
        nimailc.setBeneName(nimailcbean.getBeneName());
        nimailc.setBeneBankCountry(nimailcbean.getBeneBankCountry());
        nimailc.setBeneContactPerson(nimailcbean.getBeneContactPerson());
        nimailc.setBeneContactPersonEmail(nimailcbean.getBeneContactPersonEmail());
        nimailc.setBeneBankName(nimailcbean.getBeneBankName());
        nimailc.setBeneSwiftCode(nimailcbean.getBeneSwiftCode());
        nimailc.setBeneCountry(nimailcbean.getBeneCountry());
        nimailc.setLoadingCountry(nimailcbean.getLoadingCountry());
        nimailc.setLoadingPort(nimailcbean.getLoadingPort());
        nimailc.setDischargeCountry(nimailcbean.getDischargeCountry());
        nimailc.setDischargePort(nimailcbean.getDischargePort());
        nimailc.setChargesType(nimailcbean.getChargesType());
        nimailc.setValidity(nimailcbean.getValidity());
        nimailc.setInsertedDate(nimailcbean.getInsertedDate());
        nimailc.setInsertedBy(nimailcbean.getInsertedBy());
        nimailc.setModifiedDate(nimailcbean.getModifiedDate());
        nimailc.setModifiedBy(nimailcbean.getModifiedBy());
        nimailc.setTransactionflag(nimailcbean.getTransactionFlag());
        nimailc.setTransactionStatus(nimailcbean.getTransactionStatus());
        nimailc.setBranchUserId(nimailcbean.getBranchUserId());
        nimailc.setBranchUserEmail(nimailcbean.getBranchUserEmail());
        nimailc.setGoodsType(nimailcbean.getGoodsType());
        nimailc.setUsanceDays(nimailcbean.getUsanceDays());
        nimailc.setStartDate(nimailcbean.getStartDate());
        nimailc.setEndDate(nimailcbean.getEndDate());
        nimailc.setOriginalTenorDays(nimailcbean.getOriginalTenorDays());
        nimailc.setRefinancingPeriod(nimailcbean.getRefinancingPeriod());
        nimailc.setLcMaturityDate(nimailcbean.getLcMaturityDate());
        nimailc.setLcNumber(nimailcbean.getLcNumber());
        nimailc.setLastBeneBank(nimailcbean.getLastBeneBank());
        nimailc.setLastBeneSwiftCode(nimailcbean.getLastBeneSwiftCode());
        nimailc.setLastBankCountry(nimailcbean.getLastBankCountry());
        nimailc.setRemarks(nimailcbean.getRemarks());
        nimailc.setDiscountingPeriod(nimailcbean.getDiscountingPeriod());
        nimailc.setConfirmationPeriod(nimailcbean.getConfirmationPeriod());
        nimailc.setFinancingPeriod(nimailcbean.getFinancingPeriod());
        nimailc.setLcProForma(nimailcbean.getLcProForma());
        nimailc.setTenorFile(nimailcbean.getTenorFile());
        nimailc.setIsESGComplaint(nimailcbean.getIsESGComplaint());
        nimailc.setBillType(nimailcbean.getBillType());
        nimailc.setSecTransactionType(nimailcbean.getSecTransactionType());
        nimailc.setApplicableLaw(nimailcbean.getApplicableLaw());
        nimailc.setCommissionScheme(nimailcbean.getCommissionScheme());
        nimailc.setMinParticipationAmt(nimailcbean.getMinParticipationAmt());
        nimailc.setRetentionAmt(nimailcbean.getRetentionAmt());
        nimailc.setBenchmark(nimailcbean.getBenchmark());
        nimailc.setOtherCondition(nimailcbean.getOtherCondition());
        nimailc.setOfferedPrice(nimailcbean.getOfferedPrice());
        nimailc.setParticipationBasis(nimailcbean.getParticipationBasis());
        lcrepo.save(nimailc);
    }
    
    public List<Countrycurrency> getCountry() {
        List<Countrycurrency> list = null;
        try {
            list = (List<Countrycurrency>)this.countryrepo.findAll();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }
    
    public List<NimaiLC> getAllDraftTransactionDetails(final String userId, final String branchEmailId) {
        if (userId.substring(0, 2).equalsIgnoreCase("BC")) {
            return (List<NimaiLC>)this.lcrepo.findAllDraftTransactionByBranchEmailId(branchEmailId);
        }
        System.out.println("UserID: " + userId);
        final List<String> userids = (List<String>)this.lcrepo.getUserIds(userId);
        System.out.println("List of userID: " + userids);
        return (List<NimaiLC>)this.lcrepo.findAllDraftTransactionByUserIdBranchEmailId((List)userids, branchEmailId);
    }
    
    public List<NimaiLCMaster> getAllTransactionDetails() {
        final List<NimaiLCMaster> allTransactionList = (List<NimaiLCMaster>)this.lcmasterrepo.findAllTransaction();
        return allTransactionList;
    }
    
    public NimaiLCMaster getSpecificTransactionDetail(final String transactionId) {
        return this.lcmasterrepo.findSpecificTransactionById(transactionId);
    }
    
    public NimaiLC getSpecificDraftTransactionDetail(final String transactionId) {
        return this.lcrepo.findSpecificDraftTransaction(transactionId);
    }
    
    public List<NimaiLCMaster> getAllTransactionDetailsByStatus(final String status) {
        return (List<NimaiLCMaster>)this.lcmasterrepo.findAllTransactionByStatus(status);
    }
    
    public List<NimaiLCMaster> getTransactionDetailByUserId(final String userId, final String branchEmailId) {
        if (!userId.substring(0, 2).equalsIgnoreCase("BC")) {
            return (List<NimaiLCMaster>)this.lcmasterrepo.findByTransactionByUserId(userId);
        }
        if (branchEmailId.equals(this.lcmasterrepo.getEmailAddress(userId))) {
            System.out.println("Bank as a customer");
            return (List<NimaiLCMaster>)this.lcmasterrepo.findByTransactionByUserId(userId);
        }
        return (List<NimaiLCMaster>)this.lcmasterrepo.findByTransactionByUserIdAndBranchEmail(userId, branchEmailId);
    }
    
    public List<NimaiLCMaster> getTransactionDetailByUserIdAndStatus(final String userId, final String status, final String branchEmailId) {
        if (status.equalsIgnoreCase("Accepted")) {
            if (!userId.substring(0, 2).equalsIgnoreCase("BC")) {
                return (List<NimaiLCMaster>)this.lcmasterrepo.findTransactionByUserIdAndAcceptedClosedStatus(userId);
            }
            if (branchEmailId.equals(this.lcmasterrepo.getEmailAddress(userId))) {
                System.out.println("Bank as a customer");
                return (List<NimaiLCMaster>)this.lcmasterrepo.findTransactionByUserIdAndAcceptedClosedStatus(userId);
            }
            return (List<NimaiLCMaster>)this.lcmasterrepo.findTransactionByUserIdAndAcceptedClosedStatusBranchEmail(userId, branchEmailId);
        }
        else {
            if (!userId.substring(0, 2).equalsIgnoreCase("BC")) {
                return (List<NimaiLCMaster>)this.lcmasterrepo.findByTransactionByUserIdAndStatus(userId, status);
            }
            if (branchEmailId.equals(this.lcmasterrepo.getEmailAddress(userId))) {
                System.out.println("Bank as a customer");
                return (List<NimaiLCMaster>)this.lcmasterrepo.findByTransactionByUserIdAndStatus(userId, status);
            }
            return (List<NimaiLCMaster>)this.lcmasterrepo.findByTransactionByUserIdStatusBranchEmail(userId, status, branchEmailId);
        }
    }
    
    public List<NewRequestEntity> getAllTransactionForBank(final String userid, final String req) {
        System.out.println("ViewBy: " + req);
        final EntityManager entityManager = this.em.createEntityManager();
        try {
            final StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("get_transaction_for_bank", new Class[] { NewRequestEntity.class });
            storedProcedureQuery.registerStoredProcedureParameter("user_id", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.registerStoredProcedureParameter("view_by", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.setParameter("user_id", (Object)userid);
            storedProcedureQuery.setParameter("view_by", (Object)req);
            storedProcedureQuery.execute();
            final List<NewRequestEntity> list = (List<NewRequestEntity>)storedProcedureQuery.getResultList();
            return list;
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            entityManager.close();
        }
        return null;
    }
    
    public List<NimaiLCMaster> getAllTransactionForBankSec(final String userid,NimaiLCBean nimailcbean) {
    	
    	List<NimaiLCMaster> lcList=new ArrayList<>();
    	List<NimaiLCMaster> lcList1=new ArrayList<>();
    	lcList1=lcmasterrepo.findSecondaryTxnForBank(userid);

		System.out.println("offTrxnBank to check the transaction Id 1 Pauser"+userid);
		System.out.println("offTrxnBank to check the transaction Id"+userid);
    	if(nimailcbean.getUserType().equalsIgnoreCase("paUser")) {
    		for(NimaiLCMaster mstr:lcList1) {
    			try {
    				OfflineTxnBank repo=offlineTxnBankRepo.getOfllineUsrId("360tf partner banks",mstr.getTransactionId(),"360tf partner banks");
        			System.out.println("offTrxnBank to check the transaction Id 1 Pauser"+mstr.getTransactionId());	
        			if( repo.getUserId().equalsIgnoreCase("360tf partner banks")){
        				NimaiLCMaster paBanks=new NimaiLCMaster();
        				paBanks.setTransactionId(mstr.getTransactionId());
        			        paBanks.setUserId(mstr.getUserId());
        			        paBanks.setRequirementType(mstr.getRequirementType());
        			        paBanks.setlCIssuanceBank(mstr.getlCIssuanceBank());
        			        paBanks.setlCIssuanceBranch(mstr.getlCIssuanceBranch());
        			        paBanks.setSwiftCode(mstr.getSwiftCode());
        			        paBanks.setlCIssuanceCountry(mstr.getlCIssuanceCountry());
        			        paBanks.setlCIssuingDate(mstr.getlCIssuingDate());
        			        paBanks.setlCExpiryDate(mstr.getlCExpiryDate());
        			        paBanks.setClaimExpiryDate(mstr.getClaimExpiryDate());
        			        paBanks.setBgType(mstr.getBgType());
        			        paBanks.setlCValue(mstr.getlCValue());
        			        paBanks.setlCCurrency(mstr.getlCCurrency());
        			        paBanks.setLastShipmentDate(mstr.getLastShipmentDate());
        			        paBanks.setNegotiationDate(mstr.getNegotiationDate());
        			        paBanks.setPaymentPeriod(mstr.getPaymentPeriod());
        			        paBanks.setPaymentTerms(mstr.getPaymentTerms());
        			        paBanks.setTenorEndDate(mstr.getTenorEndDate());
        			        paBanks.setUserType(mstr.getUserType());
        			        paBanks.setApplicantName(mstr.getApplicantName());
        			        paBanks.setApplicantCountry(mstr.getApplicantCountry());
        			        paBanks.setApplicantContactPerson(mstr.getApplicantContactPerson());
        			        paBanks.setApplicantContactPersonEmail(mstr.getApplicantContactPersonEmail());
        			        paBanks.setBeneName(mstr.getBeneName());
        			        paBanks.setBeneBankCountry(mstr.getBeneBankCountry());
        			        paBanks.setBeneContactPerson(mstr.getBeneContactPerson());
        			        paBanks.setBeneContactPersonEmail(mstr.getBeneContactPersonEmail());
        			        paBanks.setBeneBankName(mstr.getBeneBankName());
        			        paBanks.setBeneSwiftCode(mstr.getBeneSwiftCode());
        			        paBanks.setBeneCountry(mstr.getBeneCountry());
        			        paBanks.setLoadingCountry(mstr.getLoadingCountry());
        			        paBanks.setLoadingPort(mstr.getLoadingPort());
        			        paBanks.setDischargeCountry(mstr.getDischargeCountry());
        			        paBanks.setDischargePort(mstr.getDischargePort());
        			        paBanks.setChargesType(mstr.getChargesType());
        			        paBanks.setValidity(mstr.getValidity());
        			        paBanks.setInsertedDate(mstr.getInsertedDate());
        			        paBanks.setInsertedBy(mstr.getInsertedBy());
        			        paBanks.setModifiedDate(mstr.getModifiedDate());
        			        paBanks.setModifiedBy(mstr.getModifiedBy());
        			        paBanks.setTransactionStatus(mstr.getTransactionStatus());
        			        paBanks.setBranchUserId(mstr.getBranchUserId());
        			        paBanks.setBranchUserEmail(mstr.getBranchUserEmail());
        			        paBanks.setGoodsType(mstr.getGoodsType());
        			        paBanks.setUsanceDays(mstr.getUsanceDays());
        			        paBanks.setStartDate(mstr.getStartDate());
        			        paBanks.setEndDate(mstr.getEndDate());
        			        paBanks.setOriginalTenorDays(mstr.getOriginalTenorDays());
        			        paBanks.setRefinancingPeriod(mstr.getRefinancingPeriod());
        			        paBanks.setLcMaturityDate(mstr.getLcMaturityDate());
        			        paBanks.setLcNumber(mstr.getLcNumber());
        			        paBanks.setLastBeneBank(mstr.getLastBeneBank());
        			        paBanks.setLastBeneSwiftCode(mstr.getLastBeneSwiftCode());
        			        paBanks.setLastBankCountry(mstr.getLastBankCountry());
        			        paBanks.setRemarks(mstr.getRemarks());
        			        paBanks.setDiscountingPeriod(mstr.getDiscountingPeriod());
        			        paBanks.setConfirmationPeriod(mstr.getConfirmationPeriod());
        			        paBanks.setFinancingPeriod(mstr.getFinancingPeriod());
        			        paBanks.setLcProForma(mstr.getLcProForma());
        			        paBanks.setTenorFile(mstr.getTenorFile());
        			        paBanks.setIsESGComplaint(mstr.getIsESGComplaint());
        			        paBanks.setBillType(mstr.getBillType());
        			        paBanks.setSecTransactionType(mstr.getSecTransactionType());
        			        paBanks.setApplicableLaw(mstr.getApplicableLaw());
        			        paBanks.setCommissionScheme(mstr.getCommissionScheme());
        			        paBanks.setMinParticipationAmt(mstr.getMinParticipationAmt());
        			        paBanks.setRetentionAmt(mstr.getRetentionAmt());
        			        paBanks.setBenchmark(mstr.getBenchmark());
        			        paBanks.setOtherCondition(mstr.getOtherCondition());
        			        paBanks.setOfferedPrice(mstr.getOfferedPrice());
        			        paBanks.setParticipationBasis(mstr.getParticipationBasis());
        				lcList.add(paBanks);
        			}
    			}catch(Exception e) {
    				System.out.println("Exception offTrxnBank to check the transaction Id 1 Pauser"+mstr.getTransactionId());
        			continue;
    			}
    		}	
    			}else if(nimailcbean.getUserType().equalsIgnoreCase("seUser") ) {
    				
    			return	lcList1;
    	}
    	
    	return	lcList;
        //return (List<NimaiLCMaster>)this.lcmasterrepo.findSecondaryTxnForBank(userid);
        
        
    }
    
    public String generateSerialNo() {
        final Random rand = new Random();
        final int ranInt = rand.nextInt(1000) + 1000;
        return String.valueOf(ranInt);
    }
    
    public String generateYear() {
        final DateFormat df = new SimpleDateFormat("YY");
        final StringBuilder yearbuilder = new StringBuilder();
        yearbuilder.append((Calendar.getInstance().get(5) < 10) ? ("0" + Calendar.getInstance().get(5)) : Integer.valueOf(Calendar.getInstance().get(5)));
        yearbuilder.append((Calendar.getInstance().get(2) + 1 < 10) ? ("0" + (Calendar.getInstance().get(2) + 1)) : Integer.valueOf(Calendar.getInstance().get(2) + 1));
        yearbuilder.append(df.format(Calendar.getInstance().getTime()));
        final String year = yearbuilder.toString();
        return year;
    }
    
    public String generateCountryCode(final String countryName) {
        return this.countryRepo.getCountryCode(countryName);
    }
    
    public String generateSubscriberType(final String userid) {
        return this.lcrepo.getSubscriberType(userid);
    }
    
    public String generateTransactionType(final String transType) {
        String str = "";
        switch (transType) {
            case "Confirmation": {
                str = "CONF";
                break;
            }
            case "ConfirmAndDiscount": {
                str = "CODI";
                break;
            }
            case "Discounting": {
                str = "DISC";
                break;
            }
            case "Refinance": {
                str = "REFI";
                break;
            }
            case "Refinancing": {
                str = "REFI";
                break;
            }
            case "Banker": {
                str = "BAAC";
                break;
            }
            case "BankGuarantee": {
                str = "BAGU";
                break;
            }
            case "BillAvalisation": {
                str = "AVAL";
                break;
            }
            case "TradeLoan": {
                str = "TRLO";
                break;
            }
        }
        return str;
    }
    
    public String confirmLCDet(final String transId, final String userId) {
        System.out.println("------ IN confirmLCDet method --------");
        System.out.println("transId: " + transId);
        System.out.println("userId: " + userId);
        final EntityManager entityManager = this.em.createEntityManager();
        try {
            final StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("move_to_master", new Class[] { NimaiLC.class });
            storedProcedureQuery.registerStoredProcedureParameter("inp_transaction_id", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.registerStoredProcedureParameter("inp_userid", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.registerStoredProcedureParameter("validation_message", (Class)String.class, ParameterMode.OUT);
            storedProcedureQuery.setParameter("inp_transaction_id", (Object)transId);
            storedProcedureQuery.setParameter("inp_userid", (Object)userId);
            storedProcedureQuery.execute();
            final String message = (String)storedProcedureQuery.getOutputParameterValue("validation_message");
            System.out.println("Status: " + message);
            return message;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            entityManager.close();
        }
        return null;
    }
    
    public void cloneLCDetail(final String oldTransId, final String newTransId) {
        final EntityManager entityManager = this.em.createEntityManager();
        try {
            final StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("clone_transaction", new Class[] { NimaiLC.class });
            storedProcedureQuery.registerStoredProcedureParameter("inp_transaction_id", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.registerStoredProcedureParameter("updated_transaction_id", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.setParameter("inp_transaction_id", (Object)oldTransId);
            storedProcedureQuery.setParameter("updated_transaction_id", (Object)newTransId);
            storedProcedureQuery.execute();
        }
        catch (Exception ex) {}
        finally {
            entityManager.close();
        }
    }
    
    public NimaiLCMaster checkTransaction(final String transId) {
        return this.lcmasterrepo.findSpecificTransactionById(transId);
    }
    
    public NimaiLC findByTransactionIdToConfirm(final String transId) {
        return this.lcrepo.findTransactionIdToConfirm(transId);
    }
    
    public NimaiLC findByTransactionUserIdToConfirm(final String transId, final String userId) {
        return this.lcrepo.findTransactionUserIdToConfirm(transId, userId);
    }
    
    public void moveToHistory(final String transId, final String userId) {
        final EntityManager entityManager = this.em.createEntityManager();
        try {
            final StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("move_to_historytbl", new Class[] { NimaiLCMaster.class });
            storedProcedureQuery.registerStoredProcedureParameter("inp_transaction_id", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.registerStoredProcedureParameter("inp_userid", (Class)String.class, ParameterMode.IN);
            storedProcedureQuery.setParameter("inp_transaction_id", (Object)transId);
            storedProcedureQuery.setParameter("inp_userid", (Object)userId);
            storedProcedureQuery.execute();
        }
        catch (Exception ex) {}
        finally {
            entityManager.close();
        }
    }
    
    public void saveLCMasterdetails(final NimaiLCMasterBean nimailcbean, final String tid) {
        int quoteReceived;
        try {
            quoteReceived = this.lcmasterrepo.getTotalQuoteReceived(tid);
        }
        catch (Exception e) {
            quoteReceived = 0;
        }
        System.out.println("Quote Received: " + quoteReceived);
        if (quoteReceived >= 0) {
            this.quotemasterrepo.updateQuotationToFreezePlaced(tid);
        }
        final NimaiLCMaster nimailc = new NimaiLCMaster();
        System.out.println("transaction id= " + tid);
        nimailc.setTransactionId(tid);
        nimailc.setUserId(nimailcbean.getUserId());
        nimailc.setRequirementType(nimailcbean.getRequirementType());
        nimailc.setlCIssuanceBank(nimailcbean.getlCIssuanceBank());
        nimailc.setlCIssuanceBranch(nimailcbean.getlCIssuanceBranch());
        nimailc.setSwiftCode(nimailcbean.getSwiftCode());
        nimailc.setlCIssuanceCountry(nimailcbean.getlCIssuanceCountry());
        nimailc.setlCIssuingDate(nimailcbean.getlCIssuingDate());
        nimailc.setlCExpiryDate(nimailcbean.getlCExpiryDate());
        nimailc.setClaimExpiryDate(nimailcbean.getClaimExpiryDate());
        nimailc.setBgType(nimailcbean.getBgType());
        nimailc.setlCValue(nimailcbean.getlCValue());
        nimailc.setlCCurrency(nimailcbean.getlCCurrency());
        nimailc.setLastShipmentDate(nimailcbean.getLastShipmentDate());
        nimailc.setNegotiationDate(nimailcbean.getNegotiationDate());
        nimailc.setPaymentPeriod(nimailcbean.getPaymentPeriod());
        nimailc.setPaymentTerms(nimailcbean.getPaymentTerms());
        nimailc.setTenorEndDate(nimailcbean.getTenorEndDate());
        nimailc.setUserType(nimailcbean.getUserType());
        nimailc.setApplicantName(nimailcbean.getApplicantName());
        nimailc.setApplicantCountry(nimailcbean.getApplicantCountry());
        nimailc.setApplicantContactPerson(nimailcbean.getApplicantContactPerson());
        nimailc.setApplicantContactPersonEmail(nimailcbean.getApplicantContactPersonEmail());
        nimailc.setBeneName(nimailcbean.getBeneName());
        nimailc.setBeneBankCountry(nimailcbean.getBeneBankCountry());
        nimailc.setBeneContactPerson(nimailcbean.getBeneContactPerson());
        nimailc.setBeneContactPersonEmail(nimailcbean.getBeneContactPersonEmail());
        nimailc.setBeneBankName(nimailcbean.getBeneBankName());
        nimailc.setBeneSwiftCode(nimailcbean.getBeneSwiftCode());
        nimailc.setBeneCountry(nimailcbean.getBeneCountry());
        nimailc.setLoadingCountry(nimailcbean.getLoadingCountry());
        nimailc.setLoadingPort(nimailcbean.getLoadingPort());
        nimailc.setDischargeCountry(nimailcbean.getDischargeCountry());
        nimailc.setDischargePort(nimailcbean.getDischargePort());
        nimailc.setChargesType(nimailcbean.getChargesType());
        nimailc.setValidity(nimailcbean.getValidity());
        final Date now = new Date();
        nimailc.setInsertedDate(nimailcbean.getInsertedDate());
        nimailc.setInsertedBy(nimailcbean.getInsertedBy());
        nimailc.setModifiedDate(nimailcbean.getModifiedDate());
        nimailc.setModifiedBy(nimailcbean.getModifiedBy());
        nimailc.setTransactionflag(nimailcbean.getTransactionFlag());
        nimailc.setTransactionStatus("Active");
        nimailc.setBranchUserId(nimailcbean.getBranchUserId());
        nimailc.setBranchUserEmail(nimailcbean.getBranchUserEmail());
        nimailc.setGoodsType(nimailcbean.getGoodsType());
        nimailc.setUsanceDays(nimailcbean.getUsanceDays());
        nimailc.setStartDate(nimailcbean.getStartDate());
        nimailc.setEndDate(nimailcbean.getEndDate());
        nimailc.setOriginalTenorDays(nimailcbean.getOriginalTenorDays());
        nimailc.setRefinancingPeriod(nimailcbean.getRefinancingPeriod());
        nimailc.setLcMaturityDate(nimailcbean.getLcMaturityDate());
        nimailc.setLcNumber(nimailcbean.getLcNumber());
        nimailc.setLastBeneBank(nimailcbean.getLastBeneBank());
        nimailc.setLastBeneSwiftCode(nimailcbean.getLastBeneSwiftCode());
        nimailc.setLastBankCountry(nimailcbean.getLastBankCountry());
        nimailc.setRemarks(nimailcbean.getRemarks());
        nimailc.setDiscountingPeriod(nimailcbean.getDiscountingPeriod());
        nimailc.setConfirmationPeriod(nimailcbean.getConfirmationPeriod());
        nimailc.setFinancingPeriod(nimailcbean.getFinancingPeriod());
        nimailc.setLcProForma(nimailcbean.getLcProForma());
        nimailc.setTenorFile(nimailcbean.getTenorFile());
        nimailc.setQuotationReceived(nimailcbean.getQuotationReceived());
        nimailc.setIsESGComplaint(nimailcbean.getIsESGComplaint());
        nimailc.setBillType(nimailcbean.getBillType());
        nimailc.setSecTransactionType(nimailcbean.getSecTransactionType());
        nimailc.setApplicableLaw(nimailcbean.getApplicableLaw());
        nimailc.setCommissionScheme(nimailcbean.getCommissionScheme());
        nimailc.setMinParticipationAmt(nimailcbean.getMinParticipationAmt());
        nimailc.setRetentionAmt(nimailcbean.getRetentionAmt());
        nimailc.setBenchmark(nimailcbean.getBenchmark());
        nimailc.setOtherCondition(nimailcbean.getOtherCondition());
        nimailc.setOfferedPrice(nimailcbean.getOfferedPrice());
        nimailc.setParticipationBasis(nimailcbean.getParticipationBasis());
        lcmasterrepo.save(nimailc);
        this.quoterepo.deleteQuoteByTrasanctionId(nimailc.getTransactionId(), nimailc.getUserId());
        final NimaiLCMaster drafDet = this.lcmasterrepo.findByTransactionIdUserId(nimailc.getTransactionId(), nimailc.getUserId());
        final QuotationBean bean = new QuotationBean();
        final NimaiLCMaster nlc = this.convertAndUpdateStatus(drafDet);
        if (!nlc.getTransactionStatus().equalsIgnoreCase("Pending")) {
            this.getAlleligibleBAnksEmail(nlc.getUserId(), nlc.getTransactionId(), 0, "LC_UPDATE_ALERT_ToBanks", "LC_UPDATE(DATA)", bean,"",nimailcbean);
        }
    }
    
    private NimaiLCMaster convertAndUpdateStatus(final NimaiLCMaster drafDet) {
        NimaiLCMaster lcDetails = null;
        String currency = drafDet.getlCCurrency();
        final Double lcValue = drafDet.getlCValue();
        if (currency.equalsIgnoreCase("euro")) {
            currency = "EUR";
        }
        final String appId = this.systemConfig.findappID();
        System.out.println("=======AppId: " + appId);
        final String currencyConversionUrl = this.systemConfig.finCurrencyConverionUrl();
        System.out.println("=======currencyConversionUrl: " + currencyConversionUrl);
        final AESUtil util = new AESUtil();
        final String decryPtId = util.decrypt(appId);
        System.out.println("=======decryPtId: "+decryPtId);
        final Double value = Double.valueOf(this.systemConfig.find5mnAmount());
        if (lcValue >= value && currency.equalsIgnoreCase("USD")) {
            lcDetails =lcmasterrepo.getOne(drafDet.getTransactionId());
            if (!drafDet.getTransactionId().substring(0, 2).equalsIgnoreCase("BA")) {
                lcDetails.setTransactionStatus("Pending");
            }
            else {
                lcDetails.setTransactionStatus("Active");
            }
            lcDetails.setUsdValue(lcDetails.getlCValue());
            lcmasterrepo.save(lcDetails);
        }
        else if (!currency.equalsIgnoreCase("USD")) {
            System.out.println("Currency is not USD");
            Double rates = 0.0;
            Double usdConversionlcValue = 0.0;
            try {
                final CurrencyConverterService currencyService = this.currencyService;
                rates = CurrencyConverterService.sendHttpGetRequest(currency, "USD", decryPtId, currencyConversionUrl);
                usdConversionlcValue = lcValue / rates;
                System.out.println("Rates: " + rates);
            }
            catch (IOException e) {
                lcDetails = lcmasterrepo.getOne(drafDet.getTransactionId());
                if (!drafDet.getTransactionId().substring(0, 2).equalsIgnoreCase("BA")) {
                    lcDetails.setTransactionStatus("Pending");
                }
                else {
                    lcDetails.setTransactionStatus("Active");
                }
                lcDetails.setUsdValue(usdConversionlcValue);
                lcmasterrepo.save(lcDetails);
                e.printStackTrace();
            }
            catch (JSONException e2) {
                lcDetails = lcmasterrepo.getOne(drafDet.getTransactionId());
                if (!drafDet.getTransactionId().substring(0, 2).equalsIgnoreCase("BA")) {
                    lcDetails.setTransactionStatus("Pending");
                }
                else {
                    lcDetails.setTransactionStatus("Active");
                }
                lcDetails.setUsdValue(usdConversionlcValue);
                lcmasterrepo.save(lcDetails);
                e2.printStackTrace();
            }
            if (rates == null) {
                lcDetails = lcmasterrepo.getOne(drafDet.getTransactionId());
                if (!drafDet.getTransactionId().substring(0, 2).equalsIgnoreCase("BA")) {
                    lcDetails.setTransactionStatus("Pending");
                }
                else {
                    lcDetails.setTransactionStatus("Active");
                }
                lcDetails.setUsdValue(usdConversionlcValue);
                lcmasterrepo.save(lcDetails);
            }
            else {
                System.out.println("Rates is not null");
                System.out.println("usdConversionlcValue=" + usdConversionlcValue);
                System.out.println("value=" + value);
                if (usdConversionlcValue >= value) {
                    System.out.println("Converted value is greater/= value");
                    lcDetails = lcmasterrepo.getOne(drafDet.getTransactionId());
                    if (!drafDet.getTransactionId().substring(0, 2).equalsIgnoreCase("BA")) {
                        lcDetails.setTransactionStatus("Pending");
                    }
                    else {
                        lcDetails.setTransactionStatus("Active");
                    }
                    lcDetails.setUsdValue(usdConversionlcValue);
                    lcmasterrepo.save(lcDetails);
                }
                else {
                    System.out.println("Converted value is smaller value");
                    lcDetails = lcmasterrepo.getOne(drafDet.getTransactionId());
                    lcDetails.setUsdValue(usdConversionlcValue);
                    lcDetails.setTransactionStatus("Active");
                    lcmasterrepo.save(lcDetails);
                }
            }
        }
        else {
            System.out.println("Converted value is smaller value");
            lcDetails = (NimaiLCMaster)this.lcmasterrepo.getOne(drafDet.getTransactionId());
            lcDetails.setUsdValue(lcDetails.getlCValue());
            lcDetails.setTransactionStatus("Active");
            lcmasterrepo.save(lcDetails);
        }
        return lcDetails;
    }
    
    public void updateDraftLCdetails(final NimaiLCBean nimailcbean, final String newtid) {
        String tid = nimailcbean.getTransactionId();
        NimaiLC nimailc = this.lcrepo.getOne(tid);
        System.out.println("transaction id= " + tid);
        nimailc.setUserId(nimailcbean.getUserId());
        nimailc.setRequirementType(nimailcbean.getRequirementType());
        nimailc.setlCIssuanceBank(nimailcbean.getlCIssuanceBank());
        nimailc.setlCIssuanceBranch(nimailcbean.getlCIssuanceBranch());
        nimailc.setSwiftCode(nimailcbean.getSwiftCode());
        nimailc.setlCIssuanceCountry(nimailcbean.getlCIssuanceCountry());
        nimailc.setlCIssuingDate(nimailcbean.getlCIssuingDate());
        nimailc.setlCExpiryDate(nimailcbean.getlCExpiryDate());
        nimailc.setClaimExpiryDate(nimailcbean.getClaimExpiryDate());
        nimailc.setBgType(nimailcbean.getBgType());
        nimailc.setlCValue(nimailcbean.getlCValue());
        nimailc.setlCCurrency(nimailcbean.getlCCurrency());
        nimailc.setLastShipmentDate(nimailcbean.getLastShipmentDate());
        nimailc.setNegotiationDate(nimailcbean.getNegotiationDate());
        nimailc.setPaymentPeriod(nimailcbean.getPaymentPeriod());
        nimailc.setPaymentTerms(nimailcbean.getPaymentTerms());
        nimailc.setTenorEndDate(nimailcbean.getTenorEndDate());
        nimailc.setUserType(nimailcbean.getUserType());
        nimailc.setApplicantName(nimailcbean.getApplicantName());
        nimailc.setApplicantCountry(nimailcbean.getApplicantCountry());
        nimailc.setApplicantContactPerson(nimailcbean.getApplicantContactPerson());
        nimailc.setApplicantContactPersonEmail(nimailcbean.getApplicantContactPersonEmail());
        nimailc.setBeneName(nimailcbean.getBeneName());
        nimailc.setBeneBankCountry(nimailcbean.getBeneBankCountry());
        nimailc.setBeneContactPerson(nimailcbean.getBeneContactPerson());
        nimailc.setBeneContactPersonEmail(nimailcbean.getBeneContactPersonEmail());
        nimailc.setBeneBankName(nimailcbean.getBeneBankName());
        nimailc.setBeneSwiftCode(nimailcbean.getBeneSwiftCode());
        nimailc.setBeneCountry(nimailcbean.getBeneCountry());
        nimailc.setLoadingCountry(nimailcbean.getLoadingCountry());
        nimailc.setLoadingPort(nimailcbean.getLoadingPort());
        nimailc.setDischargeCountry(nimailcbean.getDischargeCountry());
        nimailc.setDischargePort(nimailcbean.getDischargePort());
        nimailc.setChargesType(nimailcbean.getChargesType());
        nimailc.setValidity(nimailcbean.getValidity());
        final Date now = new Date();
        nimailc.setInsertedDate(nimailcbean.getInsertedDate());
        nimailc.setInsertedBy(nimailcbean.getInsertedBy());
        nimailc.setModifiedDate(now);
        nimailc.setModifiedBy(nimailcbean.getModifiedBy());
        nimailc.setTransactionflag(nimailcbean.getTransactionFlag());
        nimailc.setTransactionStatus(nimailcbean.getTransactionStatus());
        nimailc.setBranchUserId(nimailcbean.getBranchUserId());
        nimailc.setBranchUserEmail(nimailcbean.getBranchUserEmail());
        nimailc.setGoodsType(nimailcbean.getGoodsType());
        nimailc.setUsanceDays(nimailcbean.getUsanceDays());
        nimailc.setStartDate(nimailcbean.getStartDate());
        nimailc.setEndDate(nimailcbean.getEndDate());
        nimailc.setOriginalTenorDays(nimailcbean.getOriginalTenorDays());
        nimailc.setRefinancingPeriod(nimailcbean.getRefinancingPeriod());
        nimailc.setLcMaturityDate(nimailcbean.getLcMaturityDate());
        nimailc.setLcNumber(nimailcbean.getLcNumber());
        nimailc.setLastBeneBank(nimailcbean.getLastBeneBank());
        nimailc.setLastBeneSwiftCode(nimailcbean.getLastBeneSwiftCode());
        nimailc.setLastBankCountry(nimailcbean.getLastBankCountry());
        nimailc.setRemarks(nimailcbean.getRemarks());
        nimailc.setDiscountingPeriod(nimailcbean.getDiscountingPeriod());
        nimailc.setConfirmationPeriod(nimailcbean.getConfirmationPeriod());
        nimailc.setFinancingPeriod(nimailcbean.getFinancingPeriod());
        nimailc.setLcProForma(nimailcbean.getLcProForma());
        nimailc.setTenorFile(nimailcbean.getTenorFile());
        nimailc.setIsESGComplaint(nimailcbean.getIsESGComplaint());
        nimailc.setBillType(nimailcbean.getBillType());
        nimailc.setSecTransactionType(nimailcbean.getSecTransactionType());
        nimailc.setApplicableLaw(nimailcbean.getApplicableLaw());
        nimailc.setCommissionScheme(nimailcbean.getCommissionScheme());
        nimailc.setMinParticipationAmt(nimailcbean.getMinParticipationAmt());
        nimailc.setRetentionAmt(nimailcbean.getRetentionAmt());
        nimailc.setBenchmark(nimailcbean.getBenchmark());
        nimailc.setOtherCondition(nimailcbean.getOtherCondition());
        nimailc.setOfferedPrice(nimailcbean.getOfferedPrice());
        nimailc.setParticipationBasis(nimailcbean.getParticipationBasis());
        lcrepo.save(nimailc);
        this.lcrepo.updateTransactionIdByNew(nimailc.getTransactionId(), newtid);
    }
    
    public Integer getLcCount(final String userId) {
        Integer lccount;
        try {
            lccount = this.lcrepo.findLCCount(userId);
        }
        catch (NullPointerException ne) {
            lccount = 0;
        }
        return lccount;
    }
    
    public Integer getUtilizedLcCount(final String userId) {
        Integer lcutilized;
        try {
            lcutilized = this.lcrepo.findUtilzedLCCount(userId);
        }
        catch (NullPointerException ne) {
            lcutilized = 0;
        }
        return lcutilized;
    }
    
    public NimaiLCMaster getTransactionForAcceptCheck(final String transId) {
        return this.lcmasterrepo.getTransactionByTransIdTrStatusAndQuoteStatus(transId);
    }
    
    public void updateTransactionStatusToActive(final String transactionId, final String userId) {
        final List<String> userids = (List<String>)this.lcrepo.getUserIdsWithSubsidiary(userId);
        this.lcmasterrepo.updateTransactionStatusToActive(transactionId, (List)userids);
    }
    
    public String checkMasterForSubsidiary(final String userId) {
        String checkForSubsidiary = "";
        String checkForAdditionalUser = "";
        if (userId.substring(0, 2).equalsIgnoreCase("CU")) {
            checkForSubsidiary = this.lcmasterrepo.getAccountType(userId);
        }
        else {
            checkForAdditionalUser = this.lcmasterrepo.getAccountType(userId);
        }
        if (checkForSubsidiary.equalsIgnoreCase("subsidiary") || checkForAdditionalUser.equalsIgnoreCase("bankuser")) {
            System.out.println("===== Getting Master User ====");
            final String masterUserId = this.lcmasterrepo.findMasterForSubsidiary(userId);
            System.out.println("User is Subsidiary of Master User: " + masterUserId);
            return masterUserId;
        }
        System.out.println(userId + " is Master User");
        return userId;
    }
    
    public Integer getNoOfBanksAgainstCountry(final String countryName) {
        return this.lcrepo.getBanksCountForCountry(countryName);
    }
    
    public void updateTransactionForClosed(final String transactionId, final String userId, final String reason) {
        this.lcmasterrepo.updateTransactionStatusToClosed(transactionId, userId, reason);
    }
    
    public List<NimaiLC> getDraftTransactionDetails(final String transactionId) {
        return (List<NimaiLC>)this.lcrepo.findDraftTransactionByTransactionId(transactionId);
    }
    
    public void deleteDraftTransaction(final String transactionId) {
        this.lcrepo.deleteDraftTransaction(transactionId);
    }
    	
    
    public void getAlleligibleBAnksEmail(final String userId, final String transactionId, final int quoteId, final String bankEmailEvent, final String custEmailEvent, final QuotationBean quotationbean,String route,NimaiLCMasterBean nimailcmasterbean) {
        if ("LC_REOPENING_ALERT_ToBanks".equals(bankEmailEvent)) {
            System.out.println("========== LC_REOPENING_ALERT_ToBanks ==========");
            final List<String> userids = (List<String>)this.lcrepo.getUserIdsWithSubsidiary(userId);
            System.out.println("userids: " + userids);
            final List<QuotationMaster> qmList = (List<QuotationMaster>)this.quotemasterrepo.findAllReplacedQuotationByUserIdsAndTransactionId((List)userids, transactionId);
            System.out.println("QmList: " + qmList);
            for (final QuotationMaster qm : qmList) {
                System.out.println("======== Getting eligible bank LC_REOPENING_ALERT_ToBanks========");
                final EntityManager entityManager = this.em.createEntityManager();
                try {
                    final StoredProcedureQuery getBAnksEmail = entityManager.createStoredProcedureQuery("get_eligible_banks", new Class[] { NimaiClient.class });
                    getBAnksEmail.registerStoredProcedureParameter("inp_customer_userID", (Class)String.class, ParameterMode.IN);
                    getBAnksEmail.registerStoredProcedureParameter("inp_transaction_ID", (Class)String.class, ParameterMode.IN);
                    getBAnksEmail.setParameter("inp_customer_userID", (Object)userId);
                    getBAnksEmail.setParameter("inp_transaction_ID", (Object)transactionId);
                    getBAnksEmail.execute();
                    final ModelMapperUtil modelMapper = new ModelMapperUtil();
                    List<NimaiClient> nimaiCust = new ArrayList<NimaiClient>();
                    List<NimaiClient> onlineCust = new ArrayList<NimaiClient>();
                    List<NimaiClient> offlineCust = new ArrayList<NimaiClient>();
                    if (userId.substring(0, 2).equalsIgnoreCase("BA")) {
                        System.out.println("=====Getting all eleigible banks in BAAU txn=====");
                        nimaiCust = (List<NimaiClient>)this.customerRepo.getAllElBank(userId);
                        offlineCust = (List<NimaiClient>)this.customerRepo.getApprovedOfflineBank();
                        nimaiCust.addAll(0, nimaiCust);
                        nimaiCust.addAll(0, offlineCust);
                    }
                    else {
                        System.out.println("=====Getting all eleigible banks in CU/BC txn=====");
                        onlineCust = (List<NimaiClient>)getBAnksEmail.getResultList();
                        offlineCust = (List<NimaiClient>)this.customerRepo.getApprovedOfflineBank();
                        System.out.println("List of offline banks: " + offlineCust);
                        nimaiCust.addAll(0, onlineCust);
                        nimaiCust.addAll(0, offlineCust);
                        System.out.println("Final Customer List: " + nimaiCust);
                    }
                    System.out.println("UserID: " + userId);
                    System.out.println("TransactionID: " + transactionId);
                    System.out.println("UserID: " + userId);
                    System.out.println("TransactionID: " + transactionId);
                    EligibleEmailBeanResponse responseBean = new EligibleEmailBeanResponse();
                    QuotationMaster quotationMaster = new QuotationMaster();
                    List<EligibleEmailList> emailId = nimaiCust.stream().map(obj -> {
                    	EligibleEmailList data = new EligibleEmailList();
    					NimaiEmailSchedulerAlertToBanks schedulerEntityNew = new NimaiEmailSchedulerAlertToBanks();
    					Calendar cal1 = Calendar.getInstance();
    					Date insertedDateNew = cal1.getTime();
                        schedulerEntityNew.setInsertedDate(insertedDateNew);
                        schedulerEntityNew.setCustomerid(userId);
                        System.out.println("Userid:" + userId);
                        schedulerEntityNew.setTransactionid(transactionId);
                        schedulerEntityNew.setQuotationId(qm.getQuotationId());
                        schedulerEntityNew.setAdditionalUserId(obj.getUserid());
                        schedulerEntityNew.setEmailEvent(bankEmailEvent);
                        schedulerEntityNew.setBanksEmailID(obj.getEmailAddress());
                        schedulerEntityNew.setBankUserid(obj.getUserid());
                        schedulerEntityNew.setBankUserName(obj.getFirstName());
                        System.out.println("userID from getelibible bank" + obj.getUserid());
                        System.out.println("userID from get elibible from qm list" + quotationMaster.getBankUserId());
                        if (obj.getUserid().equalsIgnoreCase(quotationMaster.getBankUserId()) || obj.getUserid() == quotationMaster.getBankUserId()) {
                            System.out.println("inside first condition 12if" + obj.getUserid() + "" + quotationMaster.getBankUserId());
                            schedulerEntityNew.setEmailFlag("Rejected_Quote");
                        }
                        else {
                            System.out.println("inside first else condition" + obj.getUserid() + "" + quotationMaster.getBankUserId());
                            schedulerEntityNew.setEmailFlag(AppConstants.PENEMAILFLG);
                        }
                        schedulerEntityNew.setEmailCount(Integer.valueOf(0));
                        userDao.save(schedulerEntityNew);
                        data.setEmailList(obj.getEmailAddress());
                        return data;
                    }).collect(Collectors.toList());
                    if (!nimaiCust.isEmpty()) {
                        continue;
                    }
                    System.out.println("No Banks Eligible");
                }
                catch (Exception e) {
                    System.out.println("" + e.getMessage());
                }
                finally {
                    entityManager.close();
                }
            }
        }
        else if ("QUOTE_REJECTION".equals(bankEmailEvent)) {
            final String bankUserId = this.quotemasterrepo.getBankUserId(quoteId);
            System.out.println("bankUserId: " + bankUserId);
            final NimaiClient bankUserData = this.userDao.getCustDetailsByUserId(bankUserId);
            final NimaiEmailSchedulerAlertToBanks schedulerEntity = new NimaiEmailSchedulerAlertToBanks();
            final Calendar cal2 = Calendar.getInstance();
            final Date insertedDate = cal2.getTime();
            schedulerEntity.setInsertedDate(insertedDate);
            schedulerEntity.setQuotationId(Integer.valueOf(quoteId));
            schedulerEntity.setTransactionid(transactionId);
            schedulerEntity.setBankUserid(bankUserId);
            schedulerEntity.setAdditionalUserId(bankUserId);
            try {
                final Double saving = this.trSavingRepo.getSavingsByTransId(transactionId);
                System.out.println("Saving: " + saving);
                if(saving==null || String.valueOf(saving).equalsIgnoreCase("null")) {
                	schedulerEntity.setTrsavings("0");
                }else {
                	schedulerEntity.setTrsavings(String.valueOf(saving));
                }  
            }
            catch (Exception e2) {
            	schedulerEntity.setTrsavings("0");
            }
            schedulerEntity.setAdditionalUserId(bankUserId);  
            schedulerEntity.setActivityBy(nimailcmasterbean.getUserType());
            schedulerEntity.setActivityEmail(nimailcmasterbean.getAddEmail());
            schedulerEntity.setActivityUserId(nimailcmasterbean.getAddUserId());
            schedulerEntity.setBankUserName(bankUserData.getFirstName());
            schedulerEntity.setBanksEmailID(bankUserData.getEmailAddress());
            schedulerEntity.setEmailFlag(AppConstants.PENEMAILFLG);
            schedulerEntity.setEmailEvent(bankEmailEvent);
            final String custUserName = this.lcmasterrepo.getCustomerName(userId);
            final String custEmailId = this.lcmasterrepo.getCustomerEmailId(userId);
            schedulerEntity.setQuotationId(Integer.valueOf(quoteId));
            schedulerEntity.setCustomerid(userId);
            schedulerEntity.setCustomerUserName((custUserName == null) ? "" : custUserName);
            schedulerEntity.setCustomerEmail((custEmailId == null) ? "" : custEmailId);
            schedulerEntity.setTransactionid(transactionId);
            schedulerEntity.setEmailCount(Integer.valueOf(0));
            userDao.save(schedulerEntity);
            final NimaiEmailSchedulerAlertToBanks schedulerEntityCu = new NimaiEmailSchedulerAlertToBanks();
            schedulerEntityCu.setInsertedDate(insertedDate);
            schedulerEntityCu.setQuotationId(Integer.valueOf(quoteId));
            schedulerEntityCu.setTransactionid(transactionId);
            schedulerEntityCu.setBankUserid(bankUserId);
            schedulerEntityCu.setBankUserName(bankUserData.getFirstName());
            schedulerEntityCu.setBanksEmailID(bankUserData.getEmailAddress());
     
            
            try {
                final Double saving = this.trSavingRepo.getSavingsByTransId(transactionId);
                System.out.println("Saving: " + saving);
                if(saving==null || String.valueOf(saving).equalsIgnoreCase("null")) {
                	schedulerEntityCu.setTrsavings("0");
                }else {
                	schedulerEntityCu.setTrsavings(String.valueOf(saving));
                }
               
            }
            catch (Exception e2) {
            	schedulerEntityCu.setTrsavings("0");
            }
            schedulerEntityCu.setAdditionalUserId(bankUserId);
            schedulerEntityCu.setEmailFlag(AppConstants.PENEMAILFLG);
            schedulerEntityCu.setEmailEvent(AppConstants.QUOTEREJCT);
            schedulerEntityCu.setQuotationId(Integer.valueOf(quoteId));
            String emailEvent=AppConstants.QUOTEREJCT;
            schedulerEntityCu.setCustomerid(userId);
            System.out.println("userId: " + userId);
           NimaiClient parentCusData = this.userDao.getCustDetailsByUserId(quotationbean.getUserId());
            if (userId != quotationbean.getUserId() || !userId.equalsIgnoreCase(quotationbean.getUserId())) {
                System.out.println("Not equal");
                System.out.println("QuotationBean userid: " + quotationbean.getUserId());
                schedulerEntityCu.setParentUserId(quotationbean.getUserId());
                schedulerEntityCu.setPasscodeuserEmail(quotationbean.getEmailAddress());
            }
            String access="";
            if(parentCusData.getAccess()==null || parentCusData.getAccess().isEmpty() || parentCusData.getAccess().equalsIgnoreCase("")) {
            	access = AppConstants.BLANK;
            }else {
            	access=parentCusData.getAccess();
            }
            System.out.println(AppConstants.ACCESSFLG+access);
            //Passcode Accessbility Logic
            NimaiClient custDetails=this.lcmasterrepo.getCustomerDetais(quotationbean.getEmailAddress());
        	
            String trPlacedBy="";
            if (custDetails == null) {
            	System.out.println("CustomerEmailID PASSCODEUSER: " + quotationbean.getEmailAddress());
                trPlacedBy=AppConstants.PASSCODEUSER;
                if((transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC") )
                		&& access.equalsIgnoreCase(AppConstants.ACCESSFLG)) {
                    List<NimaiEmailSchedulerAlertToBanks>  paUsrQuoteList=passQuoteUserList( schedulerEntity.getTrsavings(), quoteId,transactionId,  bankUserId,  bankUserData,  userId,  quotationbean, custUserName, nimailcmasterbean,trPlacedBy,emailEvent);
                    userDao.saveAll(paUsrQuoteList.stream().collect(Collectors.toList()));	
                }
            }else {
                   System.out.println("CustomerEmailID MASTERUSER: " + quotationbean.getEmailAddress());
                   trPlacedBy=AppConstants.MASTERUSER;
                   if((transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
                		   && access.equalsIgnoreCase(AppConstants.ACCESSFLG)) {
                	    List<NimaiEmailSchedulerAlertToBanks>  paUsrQuoteList=passQuoteUserList( schedulerEntity.getTrsavings(), quoteId,transactionId,  bankUserId,  bankUserData,  userId,  quotationbean, custUserName, nimailcmasterbean,trPlacedBy,emailEvent);
                        userDao.saveAll(paUsrQuoteList.stream().collect(Collectors.toList()));	
                    
                   }
            }
            schedulerEntityCu.setCustomerUserName((custUserName == null) ? "" : custUserName);
            schedulerEntityCu.setCustomerEmail((quotationbean.getEmailAddress() == null) ? "" : quotationbean.getEmailAddress());
            schedulerEntityCu.setPasscodeuserEmail(quotationbean.getEmailAddress());
            schedulerEntityCu.setTransactionid(transactionId);
            schedulerEntityCu.setEmailCount(Integer.valueOf(0));
            userDao.save(schedulerEntityCu);
        }
        else if ("QUOTE_ACCEPT".equals(bankEmailEvent)) {
            String bankUserId = this.quotemasterrepo.getBankUserId(quoteId);
            NimaiClient bankUserData = this.userDao.getCustDetailsByUserId(bankUserId);
            NimaiEmailSchedulerAlertToBanks schedulerEntity = new NimaiEmailSchedulerAlertToBanks();
            Calendar cal2 = Calendar.getInstance();
            final Date insertedDate = cal2.getTime();
            schedulerEntity.setInsertedDate(insertedDate);
            try {
                final Double saving = this.trSavingRepo.getSavingsByTransId(transactionId);
                System.out.println("Saving: " + saving);
                if(saving==null || String.valueOf(saving).equalsIgnoreCase("null")) {
                	schedulerEntity.setTrsavings(String.valueOf("0"));
                }else {
                	schedulerEntity.setTrsavings(String.valueOf(saving));
                }
            }
            catch (Exception e2) {
            	schedulerEntity.setTrsavings("0");
            }
            schedulerEntity.setQuotationId(Integer.valueOf(quoteId));
            schedulerEntity.setTransactionid(transactionId);
            schedulerEntity.setBankUserid(bankUserId);
            schedulerEntity.setAdditionalUserId(bankUserId);
            schedulerEntity.setAdditionalUserId(bankUserId);  
            schedulerEntity.setActivityBy(quotationbean.getUserType());
            schedulerEntity.setActivityEmail(quotationbean.getAddEmail());
            schedulerEntity.setActivityUserId(quotationbean.getAddUserId());
            schedulerEntity.setBankUserName(bankUserData.getFirstName());
            schedulerEntity.setBanksEmailID(bankUserData.getEmailAddress());
            schedulerEntity.setEmailFlag(AppConstants.PENEMAILFLG);
            schedulerEntity.setEmailEvent(bankEmailEvent);
            String custUserName = this.lcmasterrepo.getCustomerName(userId);
            String custEmailId = this.lcmasterrepo.getCustomerEmailId(userId);
            schedulerEntity.setCustomerid(userId);
            schedulerEntity.setCustomerUserName((custUserName == null) ? "" : custUserName);
            schedulerEntity.setCustomerEmail((custEmailId == null) ? "" : custEmailId);
            schedulerEntity.setTransactionid(transactionId);
            schedulerEntity.setEmailCount(Integer.valueOf(0));
            userDao.save(schedulerEntity);
            final NimaiEmailSchedulerAlertToBanks schedulerEntityCUst = new NimaiEmailSchedulerAlertToBanks();
            schedulerEntityCUst.setInsertedDate(insertedDate);
            try {
                final Double saving = this.trSavingRepo.getSavingsByTransId(transactionId);
                System.out.println("Saving: " + saving);
                if(saving==null || String.valueOf(saving).equalsIgnoreCase("null")) {
                	 schedulerEntityCUst.setTrsavings(String.valueOf("0"));
                }else {
                	schedulerEntityCUst.setTrsavings(String.valueOf(saving));
                }
               
            }
            catch (Exception e2) {
                schedulerEntityCUst.setTrsavings("0");
            }
            schedulerEntityCUst.setQuotationId(Integer.valueOf(quoteId));
            schedulerEntityCUst.setTransactionid(transactionId);
            schedulerEntityCUst.setBankUserid(bankUserId);
            schedulerEntity.setAdditionalUserId(bankUserId);
            schedulerEntityCUst.setBankUserName(bankUserData.getFirstName());
            schedulerEntityCUst.setBanksEmailID(bankUserData.getEmailAddress());
            schedulerEntityCUst.setEmailFlag(AppConstants.PENEMAILFLG);
            schedulerEntityCUst.setEmailEvent(AppConstants.QUOTEACCPT);
            schedulerEntityCUst.setCustomerid(userId);
            String emailEvent=AppConstants.QUOTEACCPT;
            final NimaiClient parentCusData = this.userDao.getCustDetailsByUserId(quotationbean.getUserId());
            if (userId != quotationbean.getUserId() || !userId.equalsIgnoreCase(quotationbean.getUserId())) {
                schedulerEntityCUst.setParentUserId(quotationbean.getUserId());
                //schedulerEntityCUst.setPasscodeuserEmail(parentCusData.getEmailAddress());
                schedulerEntityCUst.setPasscodeuserEmail(quotationbean.getEmailAddress());
            }
            String access="";
            if(parentCusData.getAccess()==null || parentCusData.getAccess().isEmpty() || parentCusData.getAccess().equalsIgnoreCase("")) {
            	access = AppConstants.BLANK;
            }else {
            	access=parentCusData.getAccess();
            }
            schedulerEntityCUst.setCustomerUserName((custUserName == null) ? "" : custUserName);
            schedulerEntityCUst.setCustomerEmail((quotationbean.getEmailAddress()== null) ? "" : quotationbean.getEmailAddress());
            //Passcode Accessbility Logic
            NimaiClient custDetails=this.lcmasterrepo.getCustomerDetais(quotationbean.getEmailAddress());
        	
            String trPlacedBy="";
            if (custDetails == null) {
            	System.out.println("CustomerEmailID PASSCODEUSER: " + quotationbean.getEmailAddress());
                trPlacedBy=AppConstants.PASSCODEUSER;
                if((transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
                		&& access.equalsIgnoreCase(AppConstants.ACCESSFLG)) {
                    List<NimaiEmailSchedulerAlertToBanks>  paUsrQuoteList=passQuoteUserList( schedulerEntity.getTrsavings(), quoteId,transactionId,  bankUserId,  bankUserData,  userId,  quotationbean, custUserName, nimailcmasterbean,trPlacedBy,emailEvent);
                    userDao.saveAll(paUsrQuoteList.stream().collect(Collectors.toList()));	
                }
            }else {
                   System.out.println("CustomerEmailID MASTERUSER: " + quotationbean.getEmailAddress());
                   trPlacedBy=AppConstants.MASTERUSER;
                   if((transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
                		   && access.equalsIgnoreCase(AppConstants.ACCESSFLG)) {
                	    List<NimaiEmailSchedulerAlertToBanks>  paUsrQuoteList=passQuoteUserList( schedulerEntity.getTrsavings(), quoteId,transactionId,  bankUserId,  bankUserData,  userId,  quotationbean, custUserName, nimailcmasterbean,trPlacedBy,emailEvent);
                        userDao.saveAll(paUsrQuoteList.stream().collect(Collectors.toList()));	
                    
                   }
            }
            schedulerEntityCUst.setTransactionid(transactionId);
            schedulerEntityCUst.setEmailCount(Integer.valueOf(0));
            userDao.save(schedulerEntityCUst);
            final NimaiEmailSchedulerAlertToBanks winningQuote = new NimaiEmailSchedulerAlertToBanks();
            winningQuote.setTransactionid(transactionId);
            winningQuote.setInsertedDate(insertedDate);
            winningQuote.setQuotationId(Integer.valueOf(quoteId));
            winningQuote.setTransactionid(transactionId);
            winningQuote.setBankUserid(bankUserId);
            winningQuote.setBankUserName(bankUserData.getFirstName());
            winningQuote.setBanksEmailID(bankUserData.getEmailAddress());
            winningQuote.setCustomerid(userId);
            winningQuote.setCustomerUserName((custUserName == null) ? "" : custUserName);
            winningQuote.setCustomerEmail((custEmailId == null) ? "" : custEmailId);
            winningQuote.setEmailEvent("Winning_Quote_Data");
            winningQuote.setEmailFlag("Pending");
            winningQuote.setEmailCount(Integer.valueOf(0));
            userDao.save(winningQuote);
        }
        else if ("QUOTE_PLACE_ALERT_ToBanks".equals(bankEmailEvent) || "QUOTE_ACCEPT".equals(bankEmailEvent)) {
            String bankUserId = this.quotemasterrepo.getBankUserId(quoteId);
            NimaiClient bankUserData = this.userDao.getCustDetailsByUserId(bankUserId);
            NimaiEmailSchedulerAlertToBanks schedulerEntity = new NimaiEmailSchedulerAlertToBanks();
            Calendar cal2 = Calendar.getInstance();
            Date insertedDate = cal2.getTime();
            schedulerEntity.setInsertedDate(insertedDate);
            schedulerEntity.setQuotationId(Integer.valueOf(quoteId));
            schedulerEntity.setTransactionid(transactionId);
            schedulerEntity.setCustomerid(userId);
            schedulerEntity.setBankUserid(bankUserId);
            schedulerEntity.setAdditionalUserId(bankUserId);  
            schedulerEntity.setActivityBy(quotationbean.getUserType());
            schedulerEntity.setActivityEmail(quotationbean.getAddEmail());
            schedulerEntity.setActivityUserId(quotationbean.getAddUserId());
            schedulerEntity.setBankUserName(bankUserData.getFirstName());
            schedulerEntity.setBanksEmailID(bankUserData.getEmailAddress());
            schedulerEntity.setEmailFlag(AppConstants.PENEMAILFLG);
            schedulerEntity.setEmailEvent(bankEmailEvent);
            schedulerEntity.setEmailCount(Integer.valueOf(0));
            userDao.save(schedulerEntity);
            final NimaiEmailSchedulerAlertToBanks schedulerEntityCust = new NimaiEmailSchedulerAlertToBanks();
            String cuUserId = this.quotemasterrepo.getCuUserId(quoteId);
            NimaiClient custData = this.userDao.getCustDetailsByUserId(cuUserId);
            final String custUserName2 = this.lcmasterrepo.getCustomerName(cuUserId);
            final String custEmailId2 = this.lcmasterrepo.getCustomerEmailId(cuUserId);
            schedulerEntityCust.setInsertedDate(insertedDate);
            schedulerEntityCust.setQuotationId(Integer.valueOf(quoteId));
            schedulerEntityCust.setCustomerid(cuUserId);
            schedulerEntityCust.setCustomerUserName((custUserName2 == null) ? "" : custUserName2);
            schedulerEntityCust.setCustomerEmail((custEmailId2 == null) ? "" : custEmailId2);
            schedulerEntityCust.setTransactionid(transactionId);
            schedulerEntityCust.setEmailEvent(custEmailEvent);
            
            schedulerEntityCust.setEmailCount(Integer.valueOf(0));
            schedulerEntityCust.setEmailFlag("Pending");
            NimaiClient custDetails=this.customerRepo.getCustomerDetails(cuUserId);
            String trPlacedBy="";
            	System.out.println("CustomerEmailID PASSCODEUSER: " + quotationbean.getEmailAddress());
                trPlacedBy=AppConstants.MASTERUSER;
                String access="";
                if(custDetails.getAccess()==null || custDetails.getAccess().isEmpty() || custDetails.getAccess().equalsIgnoreCase("")) {
                	access = AppConstants.BLANK;
                }else {
                	access=custDetails.getAccess();
                }
                System.out.println("Access details"+access);
                if((transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
                		&& access.equalsIgnoreCase(AppConstants.ACCESSFLG)) {
                	System.out.println("A"
                			+ "Inside Trxn accessbility"+transactionId);
                	quotationbean.setEmailAddress(custEmailId2);
                	quotationbean.setUserId(cuUserId);
                	
                    List<NimaiEmailSchedulerAlertToBanks>  paUsrQuoteList=passQuoteUserList( schedulerEntity.getTrsavings(), quoteId,transactionId,  bankUserId,  bankUserData,  cuUserId,  quotationbean, custUserName2, nimailcmasterbean,trPlacedBy,custEmailEvent);
                    userDao.saveAll(paUsrQuoteList.stream().collect(Collectors.toList()));	
                }
            
                schedulerEntityCust.setPasscodeuserEmail(custEmailId2);
            userDao.save(schedulerEntityCust);
        }
        else {
            System.out.println("======== Getting eligible bank ========");
            EntityManager entityManager2 = this.em.createEntityManager();
            try {
                
                final ModelMapperUtil modelMapper2 = new ModelMapperUtil();
                List<NimaiClient> nimaiCust2 = new ArrayList<NimaiClient>();
                List<NimaiClient> onlineCust2 = new ArrayList<NimaiClient>();
                List<NimaiClient> offlineCust2 = new ArrayList<NimaiClient>();
                System.out.println("userId: "+userId);
                System.out.println("route: "+route);
                if (userId.substring(0, 2).equalsIgnoreCase("BA")) {
                    System.out.println("=====Getting all eleigible banks in BAAU txn=====");
                    List<OfflineTxnBank> offlineDetList=offlineTxnBankRepo.getListOfOfflineTxnUsers(userId, transactionId);
                    System.out.println("List of offlineDetList: "+offlineDetList);
                    for(OfflineTxnBank otb:offlineDetList)
                    {
                    	if(otb.getUserId().equalsIgnoreCase("360tf partner banks"))
                    	{
                    		System.out.println("It's 360tf partner bank");
                    		onlineCust2 = (List<NimaiClient>)this.customerRepo.getAllElBank(userId);
                    	}
                    	else
                    	{
                    		
                    		System.out.println("user  ofTrxnBank================="+otb.getId());
                    		System.out.println("Its non partner bank");
                    		NimaiClient custList=customerRepo.findCreditTransactionByUserIdForPasscode(otb.getUserId());
                    		System.out.println("user  ofTrxnBank================="+custList.getUserid());
                    		NimaiClient custList1=new NimaiClient();
                    		
                    		NimaiOfflineUser userDetails=offDetailsRepo.existsByEmailId(otb.getEmailId());
                    		System.out.println("user  ofUserDetails================="+userDetails.getOfflineUId());
                    		if(userDetails==null) {
                    			custList1.setAddress1("No_ADD_USER");
                    		}else {
                    			custList1.setAddress1(userDetails.getAdditionalUserId());
                    		}
                    		custList1.setUserid(custList.getUserid());
                    		custList1.setEmailAddress(otb.getEmailId());
                    		custList1.setFirstName(custList.getFirstName());
                    		custList1.setLastName(custList.getLastName());
                    		custList1.setAccountSource(custList.getAccountSource());
                    		System.out.println("custList userId: "+custList.getUserid());
                    		//custList.setEmailAddress(otb.getEmailId());
                    		System.out.println("custList.setEmailAddress: "+custList.getEmailAddress());
                    		offlineCust2.add(custList1);
                    	}
                    }
                    nimaiCust2.addAll(0, onlineCust2);
                    nimaiCust2.addAll(0, offlineCust2);
                }
                else {
                	StoredProcedureQuery getBAnksEmail2 = entityManager2.createStoredProcedureQuery("get_eligible_banks", new Class[] { NimaiClient.class });
                    getBAnksEmail2.registerStoredProcedureParameter("inp_customer_userID", String.class, ParameterMode.IN);
                    getBAnksEmail2.registerStoredProcedureParameter("inp_transaction_ID", String.class, ParameterMode.IN);
                    getBAnksEmail2.setParameter("inp_customer_userID", (Object)userId);
                    getBAnksEmail2.setParameter("inp_transaction_ID", (Object)transactionId);
                    getBAnksEmail2.execute();
                    System.out.println("=====Getting all eleigible banks in CU/BC txn=====");
                    onlineCust2 = (List<NimaiClient>)getBAnksEmail2.getResultList();
                    //offlineCust2 = (List<NimaiClient>)this.customerRepo.getApprovedOfflineBank();
                    System.out.println("List of offline banks: " + offlineCust2);
                    nimaiCust2.addAll(0, onlineCust2);
                    //nimaiCust2.addAll(0, offlineCust2);
                    System.out.println("Final Customer List: " + nimaiCust2);
                }
                System.out.println("UserID: " + userId);
                System.out.println("TransactionID: " + transactionId);
                EligibleEmailBeanResponse responseBean2 = new EligibleEmailBeanResponse();
               List<NimaiEmailSchedulerAlertToBanks> bankEmailData=new ArrayList<>();
                List<EligibleEmailList> emailId2 = nimaiCust2.stream().map(obj -> {
                	EligibleEmailList data2 = new EligibleEmailList();
        			NimaiEmailSchedulerAlertToBanks schedulerEntity2 = new NimaiEmailSchedulerAlertToBanks();
        			Calendar cal = Calendar.getInstance();
        			Date insertedDate = cal.getTime();
                    schedulerEntity2.setInsertedDate(insertedDate);
                    schedulerEntity2.setCustomerid(userId);
                    System.out.println("Userid:" + userId);
                    schedulerEntity2.setTransactionid(transactionId);
                    schedulerEntity2.setEmailEvent(bankEmailEvent);
                    schedulerEntity2.setBanksEmailID(obj.getEmailAddress());
                    schedulerEntity2.setBankUserid(obj.getUserid());
                    schedulerEntity2.setAdditionalUserId(obj.getUserid());
                    schedulerEntity2.setTrScheduledId(obj.getAddress1());
                    schedulerEntity2.setBankUserName(obj.getFirstName());
                    schedulerEntity2.setEmailFlag("Pending");
                    schedulerEntity2.setEmailCount(Integer.valueOf(0));
                    bankEmailData.add(schedulerEntity2);
                   // userDao.save(schedulerEntity2);
                    data2.setEmailList(obj.getEmailAddress());
                    return data2;
                }).collect(Collectors.toList());
                
                userDao.saveAll(bankEmailData);
                if (nimaiCust2.isEmpty()) {
                    System.out.println("No Banks Eligible");
                }
                System.out.println("1. UserID: " + userId);
                System.out.println("2. TransactionID: " + transactionId);
                System.out.println("Bank Details: " + nimaiCust2);
                final Calendar cal4 = Calendar.getInstance();
                final Date insertedDate3 = cal4.getTime();
                final NimaiEmailSchedulerAlertToBanks schedulerEntityCust2 = new NimaiEmailSchedulerAlertToBanks();
                final NimaiLCMaster passcodeDetails = this.lcmasterrepo.findSpecificTransactionById(transactionId);
                System.out.println("Customer PasscodeDetails: " + passcodeDetails);
                NimaiClient custDetails =null; 
                if(transactionId.substring(0, 2).equalsIgnoreCase("BA"))
                {
                	System.out.println("Its sec txn");
                	custDetails=this.lcmasterrepo.getCustomerDetails(passcodeDetails.getUserId());
                }
                else
                {
                	System.out.println("Its primary txn");
                	custDetails=this.customerRepo.getCuDtlsByEmail(passcodeDetails.getBranchUserEmail());
                }
                String trPlacedBy="";
                if (custDetails == null) {
                	   final String custEmailId3 = this.customerRepo.getCustomerEmailId(userId);
                	   String access="";
                	   try {
                		   NimaiClient   custDetl=this.customerRepo.getCustomerDetails(passcodeDetails.getUserId()); 
                		   if(custDetl.getAccess()==null || custDetl.getAccess().isEmpty() || custDetl.getAccess().equalsIgnoreCase("")) {
                           	access = AppConstants.BLANK;
                           }else {
                           	access=custDetl.getAccess();
                           }
                	   }catch(Exception e) {
                		   access = AppConstants.BLANK;
                	   }
                	   System.out.println("Customer Details not found");
                    schedulerEntityCust2.setPasscodeuserEmail(passcodeDetails.getBranchUserEmail());
                    schedulerEntityCust2.setCustomerEmail((custEmailId3 == null) ? "" : custEmailId3);
                    trPlacedBy=AppConstants.PASSCODEUSER;
                    if((transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
                    		&&  access.equalsIgnoreCase(AppConstants.ACCESSFLG)) {
                        List<NimaiEmailSchedulerAlertToBanks>  paUsrList=pssUserList( userId, transactionId,  quoteId,  bankEmailEvent,  custEmailEvent,  quotationbean, route, nimailcmasterbean,passcodeDetails,trPlacedBy,passcodeDetails.getBranchUserEmail());
                        userDao.saveAll(paUsrList.stream().collect(Collectors.toList()));	
                    }
                }else {
                	   final String custEmailId3 = this.customerRepo.getCustomerEmailId(userId);
                	   String access="";
                	   try {
                		   NimaiClient custDetl=this.customerRepo.getCustomerDetails(passcodeDetails.getUserId()); 
                		   if(custDetl.getAccess()==null || custDetl.getAccess().isEmpty() || custDetl.getAccess().equalsIgnoreCase("")) {
                           	access = AppConstants.BLANK;
                           }else {
                           	access=custDetl.getAccess();
                           }
                	   }catch(Exception e) {
                		   access = AppConstants.BLANK;
                	   }
                       System.out.println("CustomerEmailID: " + custEmailId3);
                       schedulerEntityCust2.setCustomerEmail((custEmailId3 == null) ? "" : custEmailId3);
                       trPlacedBy=AppConstants.MASTERUSER;
                       if((transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
                    		   && access.equalsIgnoreCase(AppConstants.ACCESSFLG)) {
                           List<NimaiEmailSchedulerAlertToBanks>  paUsrList=pssUserList( userId, transactionId,  quoteId,  bankEmailEvent,  custEmailEvent,  quotationbean, route, nimailcmasterbean,passcodeDetails,trPlacedBy,passcodeDetails.getBranchUserEmail());
                           userDao.saveAll(paUsrList.stream().collect(Collectors.toList()));
                       }
                }
                final String custUserName3 = this.lcmasterrepo.getCustomerName(userId);
                System.out.println("CustomerUserName: " + custUserName3);
                schedulerEntityCust2.setInsertedDate(insertedDate3);
                schedulerEntityCust2.setQuotationId(Integer.valueOf(quoteId));
                System.out.println("QuoteID: " + quoteId);
                schedulerEntityCust2.setCustomerid(userId);
                schedulerEntityCust2.setCustomerUserName((custUserName3 == null) ? "" : custUserName3);
                schedulerEntityCust2.setTransactionid(transactionId);
                schedulerEntityCust2.setEmailEvent(custEmailEvent);
                schedulerEntityCust2.setEmailCount(Integer.valueOf(0));
                if (nimaiCust2.isEmpty()) {
                    System.out.println("No Banks Eligible");
                    schedulerEntityCust2.setTransactionEmailStatusToBanks("Pending");
                }
                schedulerEntityCust2.setEmailFlag("Pending");
                userDao.save(schedulerEntityCust2);
            }
            catch (Exception e2) {
                System.out.println("" + e2.getMessage());
            }
            finally {
                entityManager2.close();
            }
        }
    }
    
     List<NimaiEmailSchedulerAlertToBanks> passQuoteUserList(String trsavings, int quoteId, String transactionId,
			String bankUserId, NimaiClient bankUserData, String userId, QuotationBean quotationbean,
			String custUserName, NimaiLCMasterBean nimailcmasterbean, String trPlacedBy, String emailEvent) {
		// TODO Auto-generated method stub
    	 System.out.println("A"
     			+ "Inside Trxn passQuoteUserList"+transactionId);
    	 final NimaiClient parentCuData = this.userDao.getCustDetailsByUserId(quotationbean.getUserId());
    		List<NimaiEmailSchedulerAlertToBanks>  paUsrList=new ArrayList<>();
        	//final String custEmailId3 = this.lcmasterrepo.getCustomerEmailId(userId);
    		System.out.println("userId"+userId);
    		System.out.println("userId"+quotationbean.getEmailAddress());
    		System.out.println("userId"+userId);
    		
        	List<NimaiMBranch> branchDetails = brRepo
    				.getBranchUserDetails(userId,quotationbean.getEmailAddress(),parentCuData.getEmailAddress());
       	 System.out.println("A"
      			+ "Inside Trxn passQuoteUserList"+branchDetails.size());
        	for(NimaiMBranch brDetails: branchDetails ) {
        		System.out.println(branchDetails.toString());
    	 NimaiEmailSchedulerAlertToBanks schedulerEntityCUst = new NimaiEmailSchedulerAlertToBanks();
         Calendar cal2 = Calendar.getInstance();
         final Date insertedDate = cal2.getTime();
    	 schedulerEntityCUst.setInsertedDate(insertedDate);
         try {
             final Double saving = this.trSavingRepo.getSavingsByTransId(transactionId);
             System.out.println("Saving: " + saving);
             if(saving==null || String.valueOf(saving).equalsIgnoreCase("null")) {
             	 schedulerEntityCUst.setTrsavings(String.valueOf("0"));
             }else {
             	schedulerEntityCUst.setTrsavings(String.valueOf(saving));
             }
            
         }
         catch (Exception e2) {
             schedulerEntityCUst.setTrsavings("0");
         }
         schedulerEntityCUst.setQuotationId(Integer.valueOf(quoteId));
         schedulerEntityCUst.setTransactionid(transactionId);
         schedulerEntityCUst.setBankUserid(bankUserId);
         schedulerEntityCUst.setBankUserName(bankUserData.getFirstName());
         schedulerEntityCUst.setBanksEmailID(bankUserData.getEmailAddress());
         schedulerEntityCUst.setEmailFlag(AppConstants.PENEMAILFLG);
         schedulerEntityCUst.setEmailEvent(emailEvent);
         if(trPlacedBy.equalsIgnoreCase(AppConstants.PASSCODEUSER)) {
        	 schedulerEntityCUst.setReason("quote placed by passUser");
         }else {
        	 schedulerEntityCUst.setReason("quote placed by maUser");
         }
         schedulerEntityCUst.setCustomerid(userId);
         if (userId != quotationbean.getUserId() || !userId.equalsIgnoreCase(quotationbean.getUserId())) {
             final NimaiClient parentCusData = this.userDao.getCustDetailsByUserId(quotationbean.getUserId());
             schedulerEntityCUst.setParentUserId(quotationbean.getUserId());
             schedulerEntityCUst.setPasscodeuserEmail(brDetails.getEmailId());
         }
         schedulerEntityCUst.setCustomerUserName((custUserName == null) ? "" : custUserName);
         NimaiClient custDetails=this.lcmasterrepo.getCustomerDetais(quotationbean.getEmailAddress());
         schedulerEntityCUst.setPasscodeuserEmail(brDetails.getEmailId());
         schedulerEntityCUst.setCustomerEmail((brDetails.getEmailId()== null) ? "" : brDetails.getEmailId());
         schedulerEntityCUst.setTransactionid(transactionId);
         schedulerEntityCUst.setEmailCount(Integer.valueOf(0));
         paUsrList.add(schedulerEntityCUst);
        	}
        	if(trPlacedBy.equalsIgnoreCase(AppConstants.PASSCODEUSER)) {
        		 NimaiEmailSchedulerAlertToBanks schedulerEntityCUst = new NimaiEmailSchedulerAlertToBanks();
                 Calendar cal2 = Calendar.getInstance();
                 final Date insertedDate = cal2.getTime();
            	 schedulerEntityCUst.setInsertedDate(insertedDate);
                 try {
                     final Double saving = this.trSavingRepo.getSavingsByTransId(transactionId);
                     System.out.println("Saving: " + saving);
                     if(saving==null || String.valueOf(saving).equalsIgnoreCase("null")) {
                     	 schedulerEntityCUst.setTrsavings(String.valueOf("0"));
                     }else {
                     	schedulerEntityCUst.setTrsavings(String.valueOf(saving));
                     }
                 }
                 catch (Exception e2) {
                     schedulerEntityCUst.setTrsavings("0");
                 }
                 schedulerEntityCUst.setQuotationId(Integer.valueOf(quoteId));
                 schedulerEntityCUst.setTransactionid(transactionId);
                 schedulerEntityCUst.setBankUserid(bankUserId);
                 if(trPlacedBy.equalsIgnoreCase(AppConstants.PASSCODEUSER)) {
                	 schedulerEntityCUst.setReason("quote placed by passUser");
                 }else {
                	 schedulerEntityCUst.setReason("quote placed by maUser");
                 }
                 schedulerEntityCUst.setBankUserName(bankUserData.getFirstName());
                 schedulerEntityCUst.setBanksEmailID(bankUserData.getEmailAddress());
                 schedulerEntityCUst.setEmailFlag(AppConstants.PENEMAILFLG);
                 schedulerEntityCUst.setEmailEvent(emailEvent);
                 schedulerEntityCUst.setCustomerid(userId);
                 final NimaiClient parentCusData = this.userDao.getCustDetailsByUserId(quotationbean.getUserId());
                 schedulerEntityCUst.setParentUserId(quotationbean.getUserId());
                 schedulerEntityCUst.setPasscodeuserEmail(parentCusData.getEmailAddress());
                 schedulerEntityCUst.setCustomerEmail((parentCusData.getEmailAddress()== null) ? "" : parentCusData.getEmailAddress());
                 schedulerEntityCUst.setCustomerUserName((custUserName == null) ? "" : custUserName);
                 NimaiClient custDetails=this.lcmasterrepo.getCustomerDetais(quotationbean.getEmailAddress());
                 schedulerEntityCUst.setTransactionid(transactionId);
                 schedulerEntityCUst.setEmailCount(Integer.valueOf(0));
                 paUsrList.add(schedulerEntityCUst);
        	}
   
		return paUsrList;
	}

	List<NimaiEmailSchedulerAlertToBanks> pssUserList(final String userId, final String transactionId, final int quoteId, final String bankEmailEvent, final String custEmailEvent, final QuotationBean quotationbean,String route,NimaiLCMasterBean nimailcmasterbean,NimaiLCMaster passcodeDetails,String trPlacedBy,String placedByEmail){
    		List<NimaiEmailSchedulerAlertToBanks>  paUsrList=new ArrayList<>();
    	final String custEmailId3 = this.lcmasterrepo.getCustomerEmailId(userId);
    	List<NimaiMBranch> branchDetails = brRepo
				.getBranchUserDetails(userId,placedByEmail,custEmailId3);
    	for(NimaiMBranch brDetails: branchDetails ) {
    		final Calendar cal4 = Calendar.getInstance();
        	final String custUserName3 = this.lcmasterrepo.getCustomerName(userId);
        	  final Date insertedDate3 = cal4.getTime();
        	NimaiEmailSchedulerAlertToBanks schedulerEntityCust2=new NimaiEmailSchedulerAlertToBanks();
        	  schedulerEntityCust2.setInsertedDate(insertedDate3);
              schedulerEntityCust2.setQuotationId(Integer.valueOf(quoteId));
              System.out.println("QuoteID: " + quoteId);
              schedulerEntityCust2.setCustomerid(userId);
              schedulerEntityCust2.setCustomerUserName((custUserName3 == null) ? "" : custUserName3);
              schedulerEntityCust2.setPasscodeuserEmail(brDetails.getEmailId());
              schedulerEntityCust2.setTransactionid(transactionId);
              schedulerEntityCust2.setEmailStatus(AppConstants.PENEMAILFLG);
              schedulerEntityCust2.setEmailEvent(custEmailEvent);
              schedulerEntityCust2.setEmailCount(Integer.valueOf(0));
              paUsrList.add(schedulerEntityCust2);
    	}
    	return paUsrList;
    }
    
List<NimaiEmailSchedulerAlertToBanks> pssUserQuoteList(final String userId, final String transactionId, final int quoteId, final String bankEmailEvent, final String custEmailEvent, final QuotationBean quotationbean,String route,NimaiLCMasterBean nimailcmasterbean,NimaiLCMaster passcodeDetails,String trPlacedBy,String placedByEmail){
    	
    	List<NimaiEmailSchedulerAlertToBanks>  paUsrList=new ArrayList<>();
    	final String custEmailId3 = this.lcmasterrepo.getCustomerEmailId(userId);
    	List<NimaiMBranch> branchDetails = brRepo
				.getBranchUserDetails(userId,placedByEmail,custEmailId3);
    	for(NimaiMBranch brDetails: branchDetails ) {
    		final Calendar cal4 = Calendar.getInstance();
        	final String custUserName3 = this.lcmasterrepo.getCustomerName(userId);
        	  final Date insertedDate3 = cal4.getTime();
        	NimaiEmailSchedulerAlertToBanks schedulerEntityCust2=new NimaiEmailSchedulerAlertToBanks();
        	  schedulerEntityCust2.setInsertedDate(insertedDate3);
              schedulerEntityCust2.setQuotationId(Integer.valueOf(quoteId));
              System.out.println("QuoteID: " + quoteId);
              schedulerEntityCust2.setCustomerid(userId);
              schedulerEntityCust2.setCustomerUserName((custUserName3 == null) ? "" : custUserName3);
              schedulerEntityCust2.setPasscodeuserEmail(brDetails.getEmailId());
              schedulerEntityCust2.setTransactionid(transactionId);
              schedulerEntityCust2.setEmailStatus(AppConstants.PENEMAILFLG);
              schedulerEntityCust2.setEmailEvent(custEmailEvent);
              schedulerEntityCust2.setEmailCount(Integer.valueOf(0));
              paUsrList.add(schedulerEntityCust2);
    	}
     	
    	return paUsrList;
    }
    
    
    
    public List<CustomerTransactionBean> getTransactionForCustomerByUserIdAndStatus(final String userId, final String status, final String branchEmailId) throws ParseException {
        if (status.equalsIgnoreCase("Accepted")) {
            if (userId == null || userId == "") {
                System.out.println("UserId: " + userId);
                List<CustomerTransactionBean> details;
                if (status.equalsIgnoreCase("Pending")) {
                    details = lcmasterrepo.findPendingTransactionForCustByUserIdAndAcceptedClosedStatusBranchEmailOnly(branchEmailId);
                }
                else {
                    details = lcmasterrepo.findTransactionForCustByUserIdAndAcceptedClosedStatusBranchEmailOnly(branchEmailId);
                }
                final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                return finalList;
            }
            if (userId.substring(0, 2).equalsIgnoreCase("BC")) {
                if (branchEmailId.equals(this.lcmasterrepo.getEmailAddress(userId))) {
                    System.out.println("Bank as a customer");
                    List<CustomerTransactionBean> details;
                    if (status.equalsIgnoreCase("Pending")) {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdAndAcceptedClosedStatus(userId);
                    }
                    else {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdAndAcceptedClosedStatus(userId);
                    }
                    final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                    return finalList;
                }
                List<CustomerTransactionBean> details;
                if (status.equalsIgnoreCase("Pending")) {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdAndAcceptedClosedStatusBranchEmail(userId, branchEmailId);
                }
                else {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdAndAcceptedClosedStatusBranchEmail(userId, branchEmailId);
                }
                final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                return finalList;
            }
            else {
                if (userId == null && userId == "") {
                    List<CustomerTransactionBean> details;
                    if (status.equalsIgnoreCase("Pending")) {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdAndAcceptedClosedStatusBranchEmailOnly(branchEmailId);
                    }
                    else {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdAndAcceptedClosedStatusBranchEmailOnly(branchEmailId);
                    }
                    final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                    return finalList;
                }
                if (userId.substring(0, 2).equalsIgnoreCase("Al")) {
                    System.out.println("Removing All from userid: " + userId.replaceFirst("All", ""));
                    List<CustomerTransactionBean> details;
                    if (status.equalsIgnoreCase("Pending")) {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdSubsIdAndAcceptedClosedStatus(userId.replaceFirst("All", ""));
                    }
                    else {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdSubsIdAndAcceptedClosedStatus(userId.replaceFirst("All", ""));
                    }
                    final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                    return finalList;
                }
                List<CustomerTransactionBean> details;
                if (status.equalsIgnoreCase("Pending")) {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdAndAcceptedClosedStatus(userId);
                }
                else {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdAndAcceptedClosedStatus(userId);
                }
                final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                return finalList;
            }
        }
        else {
            if (userId == null || userId == "") {
                System.out.println("UserId: " + userId);
                List<CustomerTransactionBean> details;
                if (status.equalsIgnoreCase("Active")) {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByStatusBranchEmail(status, branchEmailId);
                }
                else {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByStatusBranchEmail(status, branchEmailId);
                }
                final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                return finalList;
            }
            if (userId.substring(0, 2).equalsIgnoreCase("BC")) {
                if (branchEmailId.equals(this.lcmasterrepo.getEmailAddress(userId)) || branchEmailId.equalsIgnoreCase("All")) {
                    System.out.println("Bank as a customer");
                    List<CustomerTransactionBean> details;
                    if (status.equalsIgnoreCase("Active")) {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdAndStatus(userId, status);
                    }
                    else {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdAndStatus(userId, status);
                    }
                    final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                    return finalList;
                }
                List<CustomerTransactionBean> details;
                if (status.equalsIgnoreCase("Active")) {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdStatusBranchEmail(userId, status, branchEmailId);
                }
                else {
                    details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdStatusBranchEmail(userId, status, branchEmailId);
                }
                final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                return finalList;
            }
            else {
                if (!userId.substring(0, 2).equalsIgnoreCase("Al")) {
                    System.out.println("Removing All from userid: " + userId.replaceFirst("All", ""));
                    List<CustomerTransactionBean> details;
                    if (status.equalsIgnoreCase("Active")) {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdAndStatusExpAll(userId.replaceFirst("All", ""), status);
                    }
                    else {
                        System.out.println("======In else if if else=====");
                        final List<String> userids = (List<String>)this.lcrepo.getUserIds(userId.replaceFirst("All", ""));
                        System.out.println("UserdIDs: " + userids);
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdsAndStatusExpAll((List)userids, status);
                    }
                    final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                    return finalList;
                }
                if (userId.substring(0, 2).equalsIgnoreCase("Al")) {
                    System.out.println("Removing All from userid: " + userId.replaceFirst("All", ""));
                    List<CustomerTransactionBean> details;
                    if (status.equalsIgnoreCase("Active")) {
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdAndStatus(userId.replaceFirst("All", ""), status);
                    }
                    else {
                        System.out.println("======In else if if else 2=====");
                        details = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdAndStatus(userId.replaceFirst("All", ""), status);
                    }
                    final List<CustomerTransactionBean> finalList = this.mapListToCustomerTransactionBean(details);
                    return finalList;
                }
                try {
                    final List<NimaiClient> listOfSubsidiaries = (List<NimaiClient>)this.lcmasterrepo.getSubsidiaryList(userId.replaceFirst("All", ""));
                    final List<String> subsidiaryList = new ArrayList<String>();
                    System.out.println("List Of Subsidiaries: ");
                    List<CustomerTransactionBean> details2 = null;
                    final List<CustomerTransactionBean> finalDetails = null;
                    for (final NimaiClient nc : listOfSubsidiaries) {
                        final String user = nc.getUserid();
                        subsidiaryList.add(user);
                    }
                    if (status.equalsIgnoreCase("Active")) {
                        details2 = (List<CustomerTransactionBean>)this.lcmasterrepo.findPendingTransactionForCustByUserIdListAndStatus((List)subsidiaryList, status);
                    }
                    else {
                        details2 = (List<CustomerTransactionBean>)this.lcmasterrepo.findTransactionForCustByUserIdListAndStatus((List)subsidiaryList, status);
                    }
                    final List<CustomerTransactionBean> finalList2 = this.mapListToCustomerTransactionBean(details2);
                    System.out.println("" + finalList2);
                    return finalList2;
                }
                catch (Exception e) {
                    System.out.println("No Subsdiary Selected");
                    return null;
                }
            }
        }
    }
    
    public int getSpecificDraftTransactionDetailForDuplicate(final String userId, final String transactionId) {
        int conditionMatch;
        try {
            final NimaiLC draftLC = this.lcrepo.findSpecificDraftTransaction(transactionId);
            final String applicantName = draftLC.getApplicantName();
            final Double lcValue = draftLC.getlCValue();
            final String lcCurrency = draftLC.getlCCurrency();
            final String issuanceBank = draftLC.getlCIssuanceBank();
            final String confirmationPeriod = draftLC.getConfirmationPeriod();
            final String goodsType = draftLC.getGoodsType();
            final String requirementType = draftLC.getRequirementType();
            System.out.println("" + applicantName + lcValue + lcCurrency + issuanceBank + confirmationPeriod + goodsType + requirementType);
            conditionMatch = this.lcmasterrepo.getConditionValue(userId, applicantName, lcValue, lcCurrency, issuanceBank, confirmationPeriod, goodsType, requirementType);
            System.out.println("Condition Matches atleast: " + conditionMatch);
        }
        catch (Exception e) {
            conditionMatch = 0;
            return 0;
        }
        return conditionMatch;
    }
    
    private List<CustomerTransactionBean> mapListToCustomerTransactionBean(final List<CustomerTransactionBean> details) throws ParseException {
        final List<CustomerTransactionBean> list1 = new ArrayList<CustomerTransactionBean>();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (final Object objA : details) {
            final CustomerTransactionBean responseBean = new CustomerTransactionBean();
            responseBean.setTransactionId((((Object[])objA)[0] == null) ? "null" : ((Object[])objA)[0].toString());
            responseBean.setUserId((((Object[])objA)[1] == null) ? "null" : ((Object[])objA)[1].toString());
            responseBean.setRequirementType((((Object[])objA)[2] == null) ? "null" : ((Object[])objA)[2].toString());
            responseBean.setlCIssuanceBank((((Object[])objA)[3] == null) ? "null" : ((Object[])objA)[3].toString());
            responseBean.setlCValue(Double.valueOf((((Object[])objA)[4] == null) ? 0.0 : Double.valueOf(((Object[])objA)[4].toString())));
            responseBean.setGoodsType((((Object[])objA)[5] == null) ? "null" : ((Object[])objA)[5].toString());
            responseBean.setApplicantName((((Object[])objA)[6] == null) ? "null" : ((Object[])objA)[6].toString());
            responseBean.setBeneName((((Object[])objA)[7] == null) ? "null" : ((Object[])objA)[7].toString());
            responseBean.setQuotationReceived(Integer.valueOf((((Object[])objA)[8] == null) ? 0 : Integer.valueOf(((Object[])objA)[8].toString())));
            responseBean.setInsertedDate((((Object[])objA)[9] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])objA)[9].toString()));
            responseBean.setValidity((((Object[])objA)[10] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])objA)[10].toString()));
            responseBean.setAcceptedOn((((Object[])objA)[11] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])objA)[11].toString()));
            responseBean.setTransactionStatus((((Object[])objA)[12] == null) ? "null" : ((Object[])objA)[12].toString());
            responseBean.setRejectedOn((((Object[])objA)[13] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])objA)[13].toString()));
            responseBean.setlCCurrency((((Object[])objA)[14] == null) ? "null" : ((Object[])objA)[14].toString());
            responseBean.setStatusReason((((Object[])objA)[15] == null) ? "null" : ((Object[])objA)[15].toString());
            list1.add(responseBean);
        }
        return list1;
    }
    
    public void updateQuotationReceivedForValidityDateExp(final String userId) {
        this.lcmasterrepo.updateQuotationReceivedCountForQuoteExpValidity(userId);
    }
    
    public String getLCIssuingCountryByTransId(final String transId) {
        return this.lcmasterrepo.getIssuingCountry(transId);
    }
    
    public String getLCCurrencyByTransId(final String transId) {
        return this.lcmasterrepo.getCurrency(transId);
    }
    
    public Integer getLCTenorDays(final String transId, final String userId) {
        Integer tenor = 0;
        try {
            final String productType = this.lcmasterrepo.getProductTypeByTransId(transId);
            System.out.println("Product Type: " + productType);
            if (!userId.substring(0, 2).equalsIgnoreCase("BA")) {
                final String s = productType;
                switch (s) {
                    case "Confirmation": {
                        tenor = Integer.valueOf(this.lcmasterrepo.getConfirmationPeriod(transId));
                        break;
                    }
                    case "ConfirmAndDiscount": {
                        tenor = Integer.valueOf(this.lcmasterrepo.getConfirmationPeriod(transId));
                        break;
                    }
                    case "Discounting": {
                        tenor = Integer.valueOf(this.lcmasterrepo.getDiscountingPeriod(transId));
                        break;
                    }
                    case "Banker": {
                        tenor = Integer.valueOf(this.lcmasterrepo.getDiscountingPeriod(transId));
                        break;
                    }
                    case "Refinance": {
                        tenor = Integer.valueOf(this.lcmasterrepo.getRefinancingPeriod(transId));
                        break;
                    }
                    case "BankGuarantee": {
                        tenor = Integer.valueOf(this.lcmasterrepo.getConfirmationPeriod(transId));
                        break;
                    }
                    case "BillAvalisation": {
                        tenor = Integer.valueOf(this.lcmasterrepo.getDiscountingPeriod(transId));
                        break;
                    }
                }
            }
            else {
                tenor = this.lcmasterrepo.getUsanceDays(transId);
            }
        }
        catch (Exception e) {
            System.out.println("Exception while getting tenor days: " + e);
            tenor = 0;
        }
        return tenor;
    }
    
    public Double getLCValue(final String transId) {
        Double lcValue = 0.0;
        try {
            lcValue = this.lcmasterrepo.getLCValueByTransId(transId);
        }
        catch (Exception e) {
            System.out.println("Exception while getting lcValue: " + e);
            lcValue = 0.0;
        }
        return lcValue;
    }
    
    public Double getAvgAmountForCountryFromAdmin(final String lcCountry, final String lcCurrency) {
        return this.lcmasterrepo.getAvgAmouunt(lcCountry, lcCurrency);
    }
    
    public Double getAnnualAssetValue(final String lcCountry, final String lcCurrency) {
        return this.lcmasterrepo.getAnnualAsset(lcCountry, lcCurrency);
    }
    
    public Double getNetRevenueLC(final String lcCountry, final String lcCurrency) {
        return this.lcmasterrepo.getNetRevenue(lcCountry, lcCurrency);
    }
    
    public List<NimaiLCPort> getPortListByCountry(final String countryName) {
        return (List<NimaiLCPort>)this.lcportrepo.getPort(countryName);
    }
    
    public NimaiLCMaster getAcceptedorExpiredTransaction(final String transactionId, final String userId) {
        return this.lcmasterrepo.getAcceptedORExpiredTrans(transactionId, userId);
    }
    
    public void updateTransactionForCancel(final String transactionId, final String userId) {
        this.lcmasterrepo.updateTransactionStatusToCancel(transactionId, userId);
        this.quotemasterrepo.updateQuotationStatusForCancelToExpired(transactionId, userId);
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId) throws ParseException {
        List<NimaiClient> custData = null;
        if (userId.substring(0, 3).equalsIgnoreCase("All")) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId.replaceFirst("All", ""));
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByOnlyUserId(userId);
        }
        final List<NimaiCustomerBean> custTxnList = new ArrayList<NimaiCustomerBean>();
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findTransactionByUserId(nc.getUserid());
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                
                String transactionId="";
                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                final String lcCountry = (((Object[])txnDet)[6] == null) ? null : ((Object[])txnDet)[6].toString();
                ncb.setEmailAddress((((Object[])txnDet)[7] == null) ? null : ((Object[])txnDet)[7].toString());
                
                
                int creditUsedValue;
                if(transactionId==null) {
                	creditUsedValue=0;
                }else {
                	try {
                		
                		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
                		if(masterLc==null) {
                			creditUsedValue=0;
                		}else {
                			creditUsedValue=1;
                		}
                    	
                	}catch(Exception e) {
                		creditUsedValue=0;
                		continue;
                	}
                	
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    String rejectionCount = "";
                    try {
                        rejectionCount = this.lcmasterrepo.findRejectionCount(ncb.getTransactionId());
                        if (Integer.valueOf(rejectionCount) <= 3) {
                            ncb.setCreditUsed(Integer.valueOf(0));
                        }
                        else {
                            ncb.setCreditUsed(Integer.valueOf(1));
                        }
                    }
                    catch (Exception e) {
                        ncb.setCreditUsed(Integer.valueOf(0));
                    }
                }
                System.out.println("Getting quote value and saving for: " + ncb.getUserid() + " " + ncb.getTransactionId());
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e2) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public Date getValidityDate(final String transId, final String userId) throws ParseException {
        final Date vd = new Date();
        final NimaiLC draftDet = this.lcrepo.findByTransactionIdUserId(transId, userId);
        System.out.println("Validity Date: " + draftDet.getValidity());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return draftDet.getValidity();
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final Date fromDate) throws ParseException {
        List<NimaiClient> custData = null;
        if (userId.substring(0, 3).equalsIgnoreCase("All")) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId.replaceFirst("All", ""));
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByOnlyUserId(userId);
        }
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findTransactionByUserIdStartDate(nc.getUserid(), fromDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                
                String transactionId="";
                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                final String lcCountry = (((Object[])txnDet)[6] == null) ? null : ((Object[])txnDet)[6].toString();
                ncb.setEmailAddress((((Object[])txnDet)[7] == null) ? null : ((Object[])txnDet)[7].toString());
                
                
                int creditUsedValue;
                if(transactionId==null) {
                	creditUsedValue=0;
                }else {
                	try {
                		
                		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
                		if(masterLc==null) {
                			creditUsedValue=0;
                		}else {
                			creditUsedValue=1;
                		}
                    	
                	}catch(Exception e) {
                		creditUsedValue=0;
                		continue;
                	}
                	
                }
                
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    String rejectionCount = "";
                    try {
                        rejectionCount = this.lcmasterrepo.findRejectionCount(ncb.getTransactionId());
                        if (Integer.valueOf(rejectionCount) <= 3) {
                            ncb.setCreditUsed(Integer.valueOf(0));
                        }
                        else {
                            ncb.setCreditUsed(Integer.valueOf(1));
                        }
                    }
                    catch (Exception e) {
                        ncb.setCreditUsed(Integer.valueOf(0));
                    }
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e2) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final Date fromDate, final String subsidiaryName) throws ParseException {
        List<NimaiClient> custData;
        if (subsidiaryName.equalsIgnoreCase("All")) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId);
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserIdSubsidiary(userId, subsidiaryName);
        }
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findTransactionByUserIdStartDate(nc.getUserid(), fromDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                String transactionId="";
                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setCompanyName(companyName);
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                final String lcCountry = (((Object[])txnDet)[6] == null) ? null : ((Object[])txnDet)[6].toString();
                ncb.setEmailAddress((((Object[])txnDet)[7] == null) ? null : ((Object[])txnDet)[7].toString());
               
                
            

                	 int creditUsedValue;
                                if(transactionId==null) {
                                	creditUsedValue=0;
                                }else {
                                	try {
                                		
                                		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
                                		if(masterLc==null) {
                                			creditUsedValue=0;
                                		}else {
                                			creditUsedValue=1;
                                		}
                                    	
                                	}catch(Exception e) {
                                		creditUsedValue=0;
                                		continue;
                                	}
                                	
                                }
                
     
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    String rejectionCount = "";
                    try {
                        rejectionCount = this.lcmasterrepo.findRejectionCount(ncb.getTransactionId());
                        if (Integer.valueOf(rejectionCount) <= 3) {
                            ncb.setCreditUsed(Integer.valueOf(0));
                        }
                        else {
                            ncb.setCreditUsed(Integer.valueOf(1));
                        }
                    }
                    catch (Exception e) {
                        ncb.setCreditUsed(Integer.valueOf(0));
                    }
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e2) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final Date fromDate, final Date toDate) throws ParseException {
        List<NimaiClient> custData = null;
        if (userId.substring(0, 3).equalsIgnoreCase("All")) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId.replaceFirst("All", ""));
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByOnlyUserId(userId);
        }
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findTransactionByUserIdStartDateEndDate(nc.getUserid(), fromDate, toDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                
                
                String transactionId="";
                                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                final String lcCountry = (((Object[])txnDet)[6] == null) ? null : ((Object[])txnDet)[6].toString();
                ncb.setEmailAddress((((Object[])txnDet)[7] == null) ? null : ((Object[])txnDet)[7].toString());
               
           	 int creditUsedValue;
             if(transactionId==null) {
             	creditUsedValue=0;
             }else {
             	try {
             		
             		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
             		if(masterLc==null) {
             			creditUsedValue=0;
             		}else {
             			creditUsedValue=1;
             		}
                 	
             	}catch(Exception e) {
             		creditUsedValue=0;
             		continue;
             	}
             	
             }
                
                
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    String rejectionCount = "";
                    try {
                        rejectionCount = this.lcmasterrepo.findRejectionCount(ncb.getTransactionId());
                        if (Integer.valueOf(rejectionCount) <= 3) {
                            ncb.setCreditUsed(Integer.valueOf(0));
                        }
                        else {
                            ncb.setCreditUsed(Integer.valueOf(1));
                        }
                    }
                    catch (Exception e) {
                        ncb.setCreditUsed(Integer.valueOf(0));
                    }
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e2) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByBankUserId(final String userId) throws ParseException {
        List<NimaiClient> custData = null;
        if (userId.substring(0, 3).equalsIgnoreCase("All")) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByBankUserId(userId.replaceFirst("All", ""));
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByOnlyUserId(userId);
        }
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findQuotationByBankUserId(nc.getUserid());
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType(this.lcmasterrepo.getProductTypeByTransId(ncb.getTransactionId()));
                ncb.setCcy(this.lcmasterrepo.getCurrency(ncb.getTransactionId()));
                ncb.setTxnInsertedDate((((Object[])txnDet)[1] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[1].toString()));
                ncb.setTxnDate((((Object[])txnDet)[2] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[2].toString()));
                if (((Object[])txnDet)[3].toString().equalsIgnoreCase("Placed") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("RePlaced") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("ExpPlaced") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("FreezePlaced")) {
                    ncb.setTransactionStatus("Placed");
                }
                else {
                    ncb.setTransactionStatus((((Object[])txnDet)[3] == null) ? null : ((Object[])txnDet)[3].toString());
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Placed") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("RePlaced") || ncb.getTransactionStatus().equalsIgnoreCase("ExpPlaced") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(Integer.valueOf(1));
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected") || ncb.getTransactionStatus().equalsIgnoreCase("Withdrawn")) {
                    ncb.setCreditUsed(Integer.valueOf(0));
                }
                System.out.println("Getting quote value and saving for: " + ncb.getUserid() + " " + ncb.getTransactionId());
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByBankUserId(final String userId, final Date fromDate) throws ParseException {
        final List<NimaiClient> custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByBankUserId(userId);
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findQuotationByBankUserIdStartDate(nc.getUserid(), fromDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType(this.lcmasterrepo.getProductTypeByTransId(ncb.getTransactionId()));
                ncb.setCcy(this.lcmasterrepo.getCurrency(ncb.getTransactionId()));
                ncb.setTxnInsertedDate((((Object[])txnDet)[1] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[1].toString()));
                ncb.setTxnDate((((Object[])txnDet)[2] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[2].toString()));
                if (((Object[])txnDet)[3].toString().equalsIgnoreCase("Placed") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("RePlaced") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("ExpPlaced") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("FreezePlaced")) {
                    ncb.setTransactionStatus("Placed");
                }
                else {
                    ncb.setTransactionStatus((((Object[])txnDet)[3] == null) ? null : ((Object[])txnDet)[3].toString());
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Placed") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("RePlaced") || ncb.getTransactionStatus().equalsIgnoreCase("ExpPlaced") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(Integer.valueOf(1));
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb.setCreditUsed(Integer.valueOf(0));
                }
                System.out.println("Getting quote value and saving for: " + ncb.getUserid() + " " + ncb.getTransactionId());
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByBankUserId(final String userId, final Date fromDate, final Date toDate) throws ParseException {
        final List<NimaiClient> custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByBankUserId(userId);
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findQuotationByBankUserIdStartDateEndDate(nc.getUserid(), fromDate, toDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType(this.lcmasterrepo.getProductTypeByTransId(ncb.getTransactionId()));
                ncb.setCcy(this.lcmasterrepo.getCurrency(ncb.getTransactionId()));
                ncb.setTxnInsertedDate((((Object[])txnDet)[1] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[1].toString()));
                ncb.setTxnDate((((Object[])txnDet)[2] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[2].toString()));
                if (((Object[])txnDet)[3].toString().equalsIgnoreCase("Placed") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("RePlaced") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("ExpPlaced") || ((Object[])txnDet)[3].toString().equalsIgnoreCase("FreezePlaced")) {
                    ncb.setTransactionStatus("Placed");
                }
                else {
                    ncb.setTransactionStatus((((Object[])txnDet)[3] == null) ? null : ((Object[])txnDet)[3].toString());
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Placed") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("RePlaced") || ncb.getTransactionStatus().equalsIgnoreCase("ExpPlaced") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(Integer.valueOf(1));
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb.setCreditUsed(Integer.valueOf(0));
                }
                System.out.println("Getting quote value and saving for: " + ncb.getUserid() + " " + ncb.getTransactionId());
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public List<Goods> getGoods() {
        List<Goods> list = null;
        try {
            list = (List<Goods>)this.goodsRepo.findAll();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }
    
    public String getAccountType(final String userid) {
        return this.lcmasterrepo.getAccountTypeByUserId(userid);
    }
    
    public String getAccountSource(final String userid) {
        return this.lcmasterrepo.getAccountSourceByUserId(userid);
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final String subsidiaryName) throws ParseException {
        List<NimaiClient> custData;
        if (subsidiaryName.equalsIgnoreCase("All")) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId);
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserIdSubsidiary(userId, subsidiaryName);
        }
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findTransactionByUserId(nc.getUserid());
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setCompanyName(companyName);
                
                String transactionId="";
                                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                final String lcCountry = (((Object[])txnDet)[6] == null) ? null : ((Object[])txnDet)[6].toString();
               
                
                
           	 int creditUsedValue;
             if(transactionId==null) {
             	creditUsedValue=0;
             }else {
             	try {
             		
             		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
             		if(masterLc==null) {
             			creditUsedValue=0;
             		}else {
             			creditUsedValue=1;
             		}
                 	
             	}catch(Exception e) {
             		creditUsedValue=0;
             		continue;
             	}
             	
             }
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Closed") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    String rejectionCount = "";
                    try {
                        rejectionCount = this.lcmasterrepo.findRejectionCount(ncb.getTransactionId());
                        if (Integer.valueOf(rejectionCount) <= 3) {
                            ncb.setCreditUsed(Integer.valueOf(0));
                        }
                        else {
                            ncb.setCreditUsed(Integer.valueOf(1));
                        }
                    }
                    catch (Exception e) {
                        ncb.setCreditUsed(Integer.valueOf(0));
                    }
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e2) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final Date fromDate, final Date toDate, final String subsidiaryName) throws ParseException {
        List<NimaiClient> custData;
        if (subsidiaryName.equalsIgnoreCase("All")) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId);
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserIdSubsidiary(userId, subsidiaryName);
        }
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        for (final NimaiClient nc : custData) {
            final List nlm = this.lcmasterrepo.findTransactionByUserIdStartDateEndDate(nc.getUserid(), fromDate, toDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setCompanyName(companyName);
                
                String transactionId="";
                                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                final String lcCountry = (((Object[])txnDet)[6] == null) ? null : ((Object[])txnDet)[6].toString();
              
                int creditUsedValue;
                if(transactionId==null) {
                	creditUsedValue=0;
                }else {
                	try {
                		
                		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
                		if(masterLc==null) {
                			creditUsedValue=0;
                		}else {
                			creditUsedValue=1;
                		}
                    	
                	}catch(Exception e) {
                		creditUsedValue=0;
                		continue;
                	}
                	
                }
                
                
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb.setCreditUsed(Integer.valueOf(0));
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
        }
        return custFinalList;
    }
    
    public void updateReopenCounter(final String transactionId) {
        this.lcmasterrepo.updateCounterAfterReopen(transactionId);
    }
    
    public Integer getReopenCounter(final String transactionId) {
        try {
            final Integer ctr = Integer.valueOf(this.lcmasterrepo.getReopenCtr(transactionId));
            if (ctr == null || ctr == 0) {
                return 0;
            }
            return ctr;
        }
        catch (Exception e) {
            System.out.println(e);
            return 0;
        }
    }
    
    public void updateLCUtilized(final String userId) {
        this.quotemasterrepo.updateLCUtilizedByUserId(userId);
    }
    
    public void insertDataForSavingInput(final String lcCountry, final String lcCurrency) {
    }
    
    public void updateLCUtilizedReopen4Times(final String userId) {
        this.quotemasterrepo.updateLCUtilizedByUserIdAfter4Reopen(userId);
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final String subsidiaryName, final String passcodeUser) throws ParseException {
        List<NimaiClient> custData;
        if ((subsidiaryName.equalsIgnoreCase("All") || subsidiaryName.equalsIgnoreCase("")) && (passcodeUser.equalsIgnoreCase("All") || passcodeUser.equalsIgnoreCase(""))) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId);
        }
        else if (!passcodeUser.equalsIgnoreCase("") && !passcodeUser.equalsIgnoreCase("All")) {
            custData = null;
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserIdSubsidiary(userId, subsidiaryName);
        }
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        if (custData == null) {
            final NimaiClient nc = this.customerRepo.findCreditTransactionByUserIdForPasscode(userId);
            final List nlm = this.lcmasterrepo.findTransactionByBranchEmailId(passcodeUser);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setCompanyName(companyName);
                String transactionId="";
                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                ncb.setPasscodeUser((((Object[])txnDet)[7] == null) ? null : ((Object[])txnDet)[7].toString());

           	 int creditUsedValue;
                           if(transactionId==null) {
                           	creditUsedValue=0;
                           }else {
                           	try {
                           		
                           		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
                           		if(masterLc==null) {
                           			creditUsedValue=0;
                           		}else {
                           			creditUsedValue=1;
                           		}
                               	
                           	}catch(Exception e) {
                           		creditUsedValue=0;
                           		continue;
                           	}
                           	
                           }
          
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb.setCreditUsed(Integer.valueOf(0));
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
            return custFinalList;
        }
        for (final NimaiClient nc2 : custData) {
            List nlm2;
            if (passcodeUser.equalsIgnoreCase("all")) {
                nlm2 = this.lcmasterrepo.findTransactionByUserId(nc2.getUserid());
            }
            else {
                nlm2 = this.lcmasterrepo.findTransactionByBranchEmailId(passcodeUser);
            }
            for (final Object txnDet2 : nlm2) {
                final NimaiCustomerBean ncb2 = (NimaiCustomerBean)modelMapper.map((Object)nc2, (Class)NimaiCustomerBean.class);
                ncb2.setCompanyName(companyName);
                ncb2.setTransactionId((((Object[])txnDet2)[0] == null) ? null : ((Object[])txnDet2)[0].toString());
                ncb2.setTransactionType((((Object[])txnDet2)[1] == null) ? null : ((Object[])txnDet2)[1].toString());
                ncb2.setCcy((((Object[])txnDet2)[2] == null) ? null : ((Object[])txnDet2)[2].toString());
                ncb2.setTxnInsertedDate((((Object[])txnDet2)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet2)[3].toString()));
                ncb2.setTxnDate((((Object[])txnDet2)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet2)[4].toString()));
                ncb2.setTransactionStatus((((Object[])txnDet2)[5] == null) ? null : ((Object[])txnDet2)[5].toString());
                ncb2.setPasscodeUser((((Object[])txnDet2)[7] == null) ? null : ((Object[])txnDet2)[7].toString());
                if (ncb2.getTransactionStatus().equalsIgnoreCase("Active") || ncb2.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb2.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb2.setCreditUsed(Integer.valueOf(1));
                }
                if (ncb2.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb2.setCreditUsed(Integer.valueOf(0));
                }
                try {
                    final Double saving2 = this.trSavingRepo.getSavingsByTransId(ncb2.getTransactionId());
                    System.out.println("Saving: " + saving2);
                    ncb2.setSavings(saving2);
                }
                catch (Exception e2) {
                    ncb2.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb2);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final Date fromDate, final String subsidiaryName, final String passcodeUser) throws ParseException {
        List<NimaiClient> custData;
        if ((subsidiaryName.equalsIgnoreCase("All") || subsidiaryName.equalsIgnoreCase("")) && (passcodeUser.equalsIgnoreCase("All") || passcodeUser.equalsIgnoreCase(""))) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId);
        }
        else if (!passcodeUser.equalsIgnoreCase("") && !passcodeUser.equalsIgnoreCase("All")) {
            custData = null;
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserIdSubsidiary(userId, subsidiaryName);
        }
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        if (custData == null) {
            final NimaiClient nc = this.customerRepo.findCreditTransactionByUserIdForPasscode(userId);
            final List nlm = this.lcmasterrepo.findTransactionByBranchEmailIdStartDate(nc.getUserid(), passcodeUser, fromDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setCompanyName(companyName);
                
                String transactionId="";
                                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                ncb.setPasscodeUser((((Object[])txnDet)[7] == null) ? null : ((Object[])txnDet)[7].toString());
              
                int creditUsedValue;
                if(transactionId==null) {
                	creditUsedValue=0;
                }else {
                	try {
                		
                		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
                		if(masterLc==null) {
                			creditUsedValue=0;
                		}else {
                			creditUsedValue=1;
                		}
                    	
                	}catch(Exception e) {
                		creditUsedValue=0;
                		continue;
                	}
                	
                }
                
                
                
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(creditUsedValue);
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb.setCreditUsed(Integer.valueOf(0));
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
            return custFinalList;
        }
        for (final NimaiClient nc2 : custData) {
            List nlm;
            if (passcodeUser.equalsIgnoreCase("all")) {
                nlm = this.lcmasterrepo.findTransactionByUserIdStartDate(nc2.getUserid(), fromDate);
            }
            else {
                nlm = this.lcmasterrepo.findTransactionByBranchEmailIdStartDate(nc2.getUserid(), passcodeUser, fromDate);
            }
            for (final Object txnDet2 : nlm) {
                final NimaiCustomerBean ncb2 = (NimaiCustomerBean)modelMapper.map((Object)nc2, (Class)NimaiCustomerBean.class);
                ncb2.setCompanyName(companyName);
                ncb2.setTransactionId((((Object[])txnDet2)[0] == null) ? null : ((Object[])txnDet2)[0].toString());
                ncb2.setTransactionType((((Object[])txnDet2)[1] == null) ? null : ((Object[])txnDet2)[1].toString());
                ncb2.setCcy((((Object[])txnDet2)[2] == null) ? null : ((Object[])txnDet2)[2].toString());
                ncb2.setTxnInsertedDate((((Object[])txnDet2)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet2)[3].toString()));
                ncb2.setTxnDate((((Object[])txnDet2)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet2)[4].toString()));
                ncb2.setTransactionStatus((((Object[])txnDet2)[5] == null) ? null : ((Object[])txnDet2)[5].toString());
                final String lcCountry = (((Object[])txnDet2)[6] == null) ? null : ((Object[])txnDet2)[6].toString();
                if (ncb2.getTransactionStatus().equalsIgnoreCase("Active") || ncb2.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb2.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb2.setCreditUsed(Integer.valueOf(1));
                }
                if (ncb2.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb2.setCreditUsed(Integer.valueOf(0));
                }
                try {
                    final Double saving2 = this.trSavingRepo.getSavingsByTransId(ncb2.getTransactionId());
                    System.out.println("Saving: " + saving2);
                    ncb2.setSavings(saving2);
                }
                catch (Exception e2) {
                    ncb2.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb2);
            }
        }
        return custFinalList;
    }
    
    public List<NimaiCustomerBean> getCreditTxnForCustomerByUserId(final String userId, final Date fromDate, final Date toDate, final String subsidiaryName, final String passcodeUser) throws ParseException {
        List<NimaiClient> custData;
        if ((subsidiaryName.equalsIgnoreCase("All") || subsidiaryName.equalsIgnoreCase("")) && (passcodeUser.equalsIgnoreCase("All") || passcodeUser.equalsIgnoreCase(""))) {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserId(userId);
        }
        else if (!passcodeUser.equalsIgnoreCase("") && !passcodeUser.equalsIgnoreCase("All")) {
            custData = null;
        }
        else {
            custData = (List<NimaiClient>)this.customerRepo.findCreditTransactionByUserIdSubsidiary(userId, subsidiaryName);
        }
        final String companyName = this.customerRepo.findCompanyNameByUserId(userId);
        final List<NimaiCustomerBean> custFinalList = new ArrayList<NimaiCustomerBean>();
        final ModelMapperUtil modelMapper = new ModelMapperUtil();
        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final int i = 0;
        if (custData == null) {
            final NimaiClient nc = this.customerRepo.findCreditTransactionByUserIdForPasscode(userId);
            final List nlm = this.lcmasterrepo.findTransactionByBranchEmailIdStartDateEndDate(nc.getUserid(), passcodeUser, fromDate, toDate);
            for (final Object txnDet : nlm) {
                final NimaiCustomerBean ncb = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb.setCompanyName(companyName);
                String transactionId="";
                transactionId=((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionId((((Object[])txnDet)[0] == null) ? null : ((Object[])txnDet)[0].toString());
                ncb.setTransactionType((((Object[])txnDet)[1] == null) ? null : ((Object[])txnDet)[1].toString());
                ncb.setCcy((((Object[])txnDet)[2] == null) ? null : ((Object[])txnDet)[2].toString());
                ncb.setTxnInsertedDate((((Object[])txnDet)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[3].toString()));
                ncb.setTxnDate((((Object[])txnDet)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet)[4].toString()));
                ncb.setTransactionStatus((((Object[])txnDet)[5] == null) ? null : ((Object[])txnDet)[5].toString());
                ncb.setPasscodeUser((((Object[])txnDet)[7] == null) ? null : ((Object[])txnDet)[7].toString());
              
                
           	 int creditUsedValue;
             if(transactionId==null) {
             	creditUsedValue=0;
             }else {
             	try {
             		
             		QuotationMaster masterLc=quotemasterrepo.findTransactionDetDisById(transactionId);
             		if(masterLc==null) {
             			creditUsedValue=0;
             		}else {
             			creditUsedValue=1;
             		}
                 	
             	}catch(Exception e) {
             		creditUsedValue=0;
             		continue;
             	}
             	
             }
                
                
                if (ncb.getTransactionStatus().equalsIgnoreCase("Active") || ncb.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb.setCreditUsed(Integer.valueOf(1));
                }
                if (ncb.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb.setCreditUsed(Integer.valueOf(0));
                }
                try {
                    final Double saving = this.trSavingRepo.getSavingsByTransId(ncb.getTransactionId());
                    System.out.println("Saving: " + saving);
                    ncb.setSavings(saving);
                }
                catch (Exception e) {
                    ncb.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb);
            }
            return custFinalList;
        }
        final Iterator<NimaiClient> iterator2 = custData.iterator();
        while (iterator2.hasNext()) {
            final NimaiClient nc = iterator2.next();
            List nlm2;
            if (passcodeUser.equalsIgnoreCase("all")) {
                nlm2 = this.lcmasterrepo.findTransactionByUserIdStartDateEndDate(nc.getUserid(), fromDate, toDate);
            }
            else {
                nlm2 = this.lcmasterrepo.findTransactionByBranchEmailIdStartDateEndDate(nc.getUserid(), passcodeUser, fromDate, toDate);
            }
            for (final Object txnDet2 : nlm2) {
                final NimaiCustomerBean ncb2 = (NimaiCustomerBean)modelMapper.map((Object)nc, (Class)NimaiCustomerBean.class);
                ncb2.setCompanyName(companyName);
                ncb2.setTransactionId((((Object[])txnDet2)[0] == null) ? null : ((Object[])txnDet2)[0].toString());
                ncb2.setTransactionType((((Object[])txnDet2)[1] == null) ? null : ((Object[])txnDet2)[1].toString());
                ncb2.setCcy((((Object[])txnDet2)[2] == null) ? null : ((Object[])txnDet2)[2].toString());
                ncb2.setTxnInsertedDate((((Object[])txnDet2)[3] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet2)[3].toString()));
                ncb2.setTxnDate((((Object[])txnDet2)[4] == null) ? new Date(0L) : simpleDateFormat.parse(((Object[])txnDet2)[4].toString()));
                ncb2.setTransactionStatus((((Object[])txnDet2)[5] == null) ? null : ((Object[])txnDet2)[5].toString());
                final String lcCountry = (((Object[])txnDet2)[6] == null) ? null : ((Object[])txnDet2)[6].toString();
                if (ncb2.getTransactionStatus().equalsIgnoreCase("Active") || ncb2.getTransactionStatus().equalsIgnoreCase("Accepted") || ncb2.getTransactionStatus().equalsIgnoreCase("Expired")) {
                    ncb2.setCreditUsed(Integer.valueOf(1));
                }
                if (ncb2.getTransactionStatus().equalsIgnoreCase("Rejected")) {
                    ncb2.setCreditUsed(Integer.valueOf(0));
                }
                try {
                    final Double saving2 = this.trSavingRepo.getSavingsByTransId(ncb2.getTransactionId());
                    System.out.println("Saving: " + saving2);
                    ncb2.setSavings(saving2);
                }
                catch (Exception e2) {
                    ncb2.setSavings(Double.valueOf(0.0));
                }
                custFinalList.add(ncb2);
            }
        }
        return custFinalList;
    }
    
    public NimaiClient checkMasterSubsidiary(final String accountType, final String userId, final NimaiClient userDetails) {
        final String checkForSubsidiary = "";
        final String checkForAdditionalUser = "";
        if (userDetails.getAccountType().equalsIgnoreCase("subsidiary") || userDetails.getAccountType().equalsIgnoreCase("bankuser")) {
            System.out.println("===== Getting Master User ====");
            final NimaiClient masterUserId = customerRepo.getOne(userDetails.getAccountSource());
            System.out.println("User is Subsidiary of Master User: " + masterUserId);
            return masterUserId;
        }
        System.out.println(userId + " is Master User");
        return userDetails;
    }
    
    public ResponseEntity<Object> saveTempLc(final NimaiClient subscriptionDettails, final NimaiLCBean nimailc) {
        final GenericResponse response = new GenericResponse();
        NimaiLCMasterBean nimailcMasterbean=new NimaiLCMasterBean();
        Integer lcCount = 0;
        Integer utilizedLcCount = 0;
        if (!subscriptionDettails.getSubscriptionDettails().get(0).getStatus().equalsIgnoreCase("inactive")) {
            lcCount = Integer.valueOf(subscriptionDettails.getSubscriptionDettails().get(0).getlCount());
            utilizedLcCount = subscriptionDettails.getSubscriptionDettails().get(0).getLcUtilizedCount();
        }
        else {
            lcCount = this.lcrepo.findLCCountForInactive(subscriptionDettails.getUserid());
            utilizedLcCount = this.lcrepo.findUtilzedLCCountForInactive(subscriptionDettails.getUserid());
        }
        System.out.println("lcCount: " + lcCount);
        System.out.println("utilizedLcCount: " + utilizedLcCount);
        System.out.println("Checking plan.....");
        if(lcrepo.findPlanName(subscriptionDettails.getUserid()).equalsIgnoreCase("POSTPAID_PLAN"))
        {
        	System.out.println("Postpaid Plan");
        	creditBoundary=postpaidCreditBoundary;
        }
        else
        {
        	System.out.println("Prepaid Plan");
        	creditBoundary=prepaidCreditBoundary;
        }
        System.out.println("Credit Boundary: " + this.creditBoundary);
        if(nimailc.getTransactionId().substring(0, 2).equalsIgnoreCase("BA"))
        {
        	try {
                final String transId = nimailc.getTransactionId();
                final String userId = nimailc.getUserId();
                System.out.println(transId + " " + userId);
                final NimaiLC draftDet = this.lcrepo.findByTransactionIdUserId(transId, userId);
                final Date today = new Date();
                final Calendar cal1 = Calendar.getInstance();
                final Calendar cal2 = Calendar.getInstance();
                cal1.setTime(draftDet.getValidity());
                cal1.add(5, 1);
                cal2.setTime(today);
                System.out.println("Validity Date: " + cal1);
                System.out.println("Today Date: " + cal2);
                if (cal1.compareTo(cal2) < 0) {
                    response.setStatus("Failure");
                    response.setErrMessage("Please select correct transaction validity date");
                    return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
                }
                final String sts = this.confirmLCDet(transId, userId);
                System.out.println("sts: " + sts);
                if (sts.equals("Validation Success")) {
                    final NimaiLCMaster drafDet = this.lcmasterrepo.findByTransactionIdUserId(transId, userId);
                    System.out.println("draftDet: " + draftDet);
                    final NimaiLCMaster lcDetails1 = this.convertAndUpdateStatus(drafDet);
                    final QuotationBean bean = new QuotationBean();
                    System.out.println("lcDetails1: " + lcDetails1);
                    if (!lcDetails1.getTransactionStatus().equalsIgnoreCase("Pending")) {
                        this.getAlleligibleBAnksEmail(userId, transId, 0, "LC_UPLOAD_ALERT_ToBanks", "LC_UPLOAD(DATA)", bean,"",nimailcMasterbean);
                    }
                    response.setStatus("Success");
                    response.setErrCode(lcDetails1.getTransactionStatus());
                    response.setData((Object)sts);
                    return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
                }
                response.setStatus("Failure");
                response.setErrMessage(sts);
                return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
            }
            catch (Exception e) {
                System.out.println("Exception: " + e);
                response.setStatus("Failure");
                response.setErrCode("EXE000");
                response.setErrMessage(ErrorDescription.getDescription("EXE000") + e);
                return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.BAD_REQUEST);
            }
        }
        else if(nimailc.getTransactionId().substring(0, 2).equalsIgnoreCase("CU") || nimailc.getTransactionId().substring(0, 2).equalsIgnoreCase("BC"))
        {
        	if (lcCount > utilizedLcCount - Integer.valueOf(this.creditBoundary)) {
            try {
                final String transId = nimailc.getTransactionId();
                final String userId = nimailc.getUserId();
                System.out.println(transId + " " + userId);
                final NimaiLC draftDet = this.lcrepo.findByTransactionIdUserId(transId, userId);
                final Date today = new Date();
                final Calendar cal1 = Calendar.getInstance();
                final Calendar cal2 = Calendar.getInstance();
                cal1.setTime(draftDet.getValidity());
                cal1.add(5, 1);
                cal2.setTime(today);
                System.out.println("Validity Date: " + cal1);
                System.out.println("Today Date: " + cal2);
                if (cal1.compareTo(cal2) < 0) {
                    response.setStatus("Failure");
                    response.setErrMessage("Please select correct transaction validity date");
                    return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
                }
                final String sts = this.confirmLCDet(transId, userId);
                System.out.println("sts: " + sts);
                if (sts.equals("Validation Success")) {
                    final NimaiLCMaster drafDet = this.lcmasterrepo.findByTransactionIdUserId(transId, userId);
                    System.out.println("draftDet: " + draftDet);
                    final NimaiLCMaster lcDetails1 = this.convertAndUpdateStatus(drafDet);
                    final QuotationBean bean = new QuotationBean();
                    System.out.println("lcDetails1: " + lcDetails1);
                    if (!lcDetails1.getTransactionStatus().equalsIgnoreCase("Pending")) {
                        this.getAlleligibleBAnksEmail(userId, transId, 0, "LC_UPLOAD_ALERT_ToBanks", "LC_UPLOAD(DATA)", bean,"",nimailcMasterbean);
                    }
                    updateTransactionActivity(draftDet.getUserId(), draftDet.getTransactionId(), draftDet.getBranchUserEmail(), "Placed");
                    response.setStatus("Success");
                    response.setErrCode(lcDetails1.getTransactionStatus());
                    response.setData((Object)sts);
                    return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
                }
                response.setStatus("Failure");
                response.setErrMessage(sts);
                return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
            }
            catch (Exception e) {
                System.out.println("Exception: " + e);
                response.setStatus("Failure");
                response.setErrCode("EXE000");
                response.setErrMessage(ErrorDescription.getDescription("EXE000") + e);
                return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.BAD_REQUEST);
            }
        }
        response.setStatus("Failure");
        response.setErrMessage("You had reached maximum LC Count!");
        return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
        }
        response.setStatus("Failure");
        response.setErrMessage("You had reached maximum LC Count!");
        return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
    }
    
    public List<Goods> getGoodsList() {
        return (List<Goods>)this.goodsRepo.findAll();
    }
    
    public void updateTransactionValidity(final NimaiLCMasterBean nimailc) {
        NimaiLCMaster lcDetails = null;
        System.out.println("Transaction Id: " + nimailc.getTransactionId());
        System.out.println("Validity Date: " + nimailc.getValidity());
        lcDetails = lcmasterrepo.getOne(nimailc.getTransactionId());
        lcDetails.setTransactionStatus("Active");
        lcDetails.setValidity(nimailc.getValidity());
        lcmasterrepo.save(lcDetails);
        System.out.println("Updating Quotation");
        this.quotemasterrepo.updateQuotationToRePlacedByTransId(nimailc.getTransactionId());
    }
    
    public Date getCreditExhaust(final String userId) {
        return this.lcrepo.findCreditExhaust(userId);
    }
    
    public NimaiClient getUserDetails(final String userId) {
        return this.userDao.getCustDetailsByUserId(userId);
    }
    
    /*public List<BankDetailsBean> getBanksDetails(final String userID) {
        List<NimaiClient> ncList = customerRepo.getBankListForSec(userID);
        List<BankDetailsBean> bkList = new ArrayList<BankDetailsBean>();
        for (final NimaiClient nc : ncList) {
            final BankDetailsBean bdb = new BankDetailsBean();
            bdb.setBankUserId(nc.getUserid());
            bdb.setBankName(nc.getBankNbfcName());
            bdb.setCountryName(nc.getCountryName());
            bdb.setEmailAddress(nc.getEmailAddress());
            System.out.println("===" + bdb);
            bkList.add(bdb);
        }
        return bkList;
    }*/
    
    @Transactional( rollbackFor = Exception.class)
    public ResponseEntity<Object> saveSelectBankForTransaction(String userId,SelectBankUser bankUserBean)
    {
    	NimaiClient nc=new NimaiClient();
	    
    	final GenericResponse response = new GenericResponse();
    	String subscriberType = "BANK";
    	String bankType = "UNDERWRITER";
		String userID = "";
		String accountStatus = "PENDING";
		String kycStatus = "PENDING";
    	
    	userID = this.userid.username(subscriberType, bankType);
	      List<String> existingUserIds = customerRepo.getAllUserIds();
	      System.out.println("List of User Ids: " + existingUserIds);
	      System.out.println("userID: "+userID);
	      while(existingUserIds.contains(userID)) {
	         System.out.println("Duplicate found.... Creating new userID");
	         userID = userid.newUsername(subscriberType, bankType);
	      }
	      Integer i,j=0,result=0;
	    
	    for(com.nimai.lc.bean.AdditionalUserList userAdditional:bankUserBean.getAdditionalUserList()) 
		{
	    	j++;
	    	for(i=j;i<bankUserBean.getAdditionalUserList().size();i++)
			{
	    		if(userAdditional.getEmailAddress().equalsIgnoreCase(bankUserBean.getAdditionalUserList().get(i).getEmailAddress()))
	    		{
	    			System.out.println("Duplicate email ID found");
	    			result=1;
	    		}
			}
		}
	    if(result==1)
	    {
	    	response.setStatus("Failure");
	        response.setErrMessage("Duplicate email id not allowed.");
	        return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
	    }
	    System.out.println("Bank Name: "+bankUserBean.getBankName());
	    System.out.println("Bank Country: "+bankUserBean.getBankCountry());
	    System.out.println("Saving into customer table");  
	    try
	    {
	    nc.setUserid(userID);
	    nc.setSubscriberType(subscriberType);
	    nc.setBankType(bankType);
	    nc.setEmailAddress("");
	    nc.setBankNbfcName(bankUserBean.getBankName());
	    nc.setCountryName(bankUserBean.getBankCountry());
	    nc.setRgistredCountry(bankUserBean.getBankCountry());
	    nc.setAccountStatus(accountStatus);
	    nc.setKycStatus(kycStatus);
	    nc.setAccountType("MASTER");
	    nc.setAccountSource("WEBSITE");
	    nc.setAccountCreatedDate(new Date());
	    nc.setUserMode("OFFLINE");
	    nc.setOffBauStatus("Approved");
	    nc.setMrpa(bankUserBean.getMrpa());
	    customerRepo.save(nc);
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();;
	    }
	    System.out.println("Customer Details Saved Successfully:"+nc.getUserid());
	    
	    for(com.nimai.lc.bean.AdditionalUserList userAdditional:bankUserBean.getAdditionalUserList()) 
	    {
	    System.out.println("rfw");
	    
			System.out.println(subscriberType + "   " + bankType);
String addUserId=" ";
addUserId = userid.seUsername("SEBAUSER", bankType);
	    	AdditionalUserList user=new AdditionalUserList();
	    	user.setAdditionalUserId(addUserId);
	    	user.setFirstName(userAdditional.getFirstName());
	    	user.setLastName(userAdditional.getLastName());
	    	user.setEmailAddress(userAdditional.getEmailAddress());
	    	user.setMobileNo(userAdditional.getMobileNo());
	    	user.setInsertedDate(new Date());
	    	user.setIsParent(0);
	    	user.setUserid(nc);
	    	user.setCreatedBy(userID);
	    	user.setParentUserId(userId);
	    	offlineRepo.save(user);
	    }
	    
	    response.setStatus("Success");
        response.setErrMessage("Data Added Successfully");
        return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
    }

	@Override
	public List<AdditionalUserList> getSelectBank(String userId) {
		// TODO Auto-generated method stub
		System.out.println("userId: "+userId);
		List<AdditionalUserList> aul=offlineRepo.getListOfOfflineUsers(userId);
		return aul;
	}
	
	@Override
	public List<OfflineTxnBank> getOfflineSelectBank(String userId,String tid) {
		// TODO Auto-generated method stub
		System.out.println("userId: "+userId);
		List<OfflineTxnBank> otb=offlineTxnBankRepo.getListOfOfflineTxnUsers(userId,tid);
		return otb;
	}

	@Override
	public ResponseEntity<Object> saveOfflineSelectBankForTransaction(String parentUserId,String txnId,List<OfflineTxnBankBean> offlineTxnBankBean) {
		// TODO Auto-generated method stub
		final GenericResponse response = new GenericResponse();
		offlineTxnBankRepo.deleteUsingParentID(parentUserId,txnId);
		for(OfflineTxnBankBean obb:offlineTxnBankBean)
		{
			OfflineTxnBank otb=new OfflineTxnBank();
			try {
				otb.setQuotationStatus(obb.getQuotationStatus());
			}catch(Exception e) {
				otb.setQuotationStatus(null);
				continue;
			}
			otb.setUserId(obb.getUserId());
			otb.setEmailId(obb.getEmailId());
			NimaiOfflineUser user=offUsrRepo.existsByEmailId(obb.getEmailId());
			if(user==null) {
				otb.setSeBnkUsrId("360 tf Banks");
			}else {
				otb.setSeBnkUsrId(user.getAdditionalUserId());
			}
			otb.setParentUserId(parentUserId);
			otb.setTxnId(txnId);
			offlineTxnBankRepo.save(otb);
		}
		
		response.setStatus("Success");
        response.setErrMessage("Data Added Successfully");
        return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
		
	}

	@Override
	public ResponseEntity<?> updateSelectBankForTransaction(String userId, SelectBankUser bankUserBean) {
		// TODO Auto-generated method stub
		GenericResponse response = new GenericResponse();
		Integer i,j=0,result=0;
		offlineRepo.deleteAdditionalUser(userId);
		
		
		
		
		for(com.nimai.lc.bean.AdditionalUserList userAdditional:bankUserBean.getAdditionalUserList()) 
		{
	    	j++;
	    	for(i=j;i<bankUserBean.getAdditionalUserList().size();i++)
			{
	    		if(userAdditional.getEmailAddress().equalsIgnoreCase(bankUserBean.getAdditionalUserList().get(i).getEmailAddress()))
	    		{
	    			System.out.println("Duplicate email ID found");
	    			result=1;
	    		}
			}
		}
	    if(result==1)
	    {
	    	response.setStatus("Failure");
	        response.setErrMessage("Duplicate email id not allowed.");
	        return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
	    }
		NimaiClient nc=customerRepo.findCreditTransactionByUserIdForPasscode(userId);
	    for(com.nimai.lc.bean.AdditionalUserList userAdditional:bankUserBean.getAdditionalUserList()) 
	    {
		    System.out.println("rfw");
		    String bankType = "UNDERWRITER";
				//System.out.println(subscriberType + "   " + bankType);
	String addUserId=" ";
	addUserId = userid.seUsername("SEBAUSER", bankType);
		    	AdditionalUserList user=new AdditionalUserList();
		    	user.setAdditionalUserId(addUserId);
	    	user.setFirstName(userAdditional.getFirstName());
	    	user.setLastName(userAdditional.getLastName());
	    	user.setEmailAddress(userAdditional.getEmailAddress());
	    	user.setMobileNo(userAdditional.getMobileNo());
	    	user.setInsertedDate(new Date());
	    	user.setIsParent(0);
	    	user.setUserid(nc);
	    	user.setCreatedBy(userId);
	    	user.setParentUserId(userAdditional.getParentUserId());
	    	offlineRepo.save(user);
	    }
	    
	    response.setStatus("Success");
        response.setErrMessage("Data Updated Successfully");
        return (ResponseEntity<Object>)new ResponseEntity((Object)response, HttpStatus.OK);
	}
	
	public void saveLCdetailsTemp(final NimaiLCBean nimailcbean, final String tid) {
        final NimaiLCTemp nimailc = new NimaiLCTemp();
        System.out.println("transaction id= " + tid);
        nimailc.setTransactionId(tid);
        nimailc.setUserId(nimailcbean.getUserId());
        nimailc.setRequirementType(nimailcbean.getRequirementType());
        nimailc.setlCIssuanceBank(nimailcbean.getlCIssuanceBank());
        nimailc.setlCIssuanceBranch(nimailcbean.getlCIssuanceBranch());
        nimailc.setSwiftCode(nimailcbean.getSwiftCode());
        nimailc.setlCIssuanceCountry(nimailcbean.getlCIssuanceCountry());
        nimailc.setlCIssuingDate(nimailcbean.getlCIssuingDate());
        nimailc.setlCExpiryDate(nimailcbean.getlCExpiryDate());
        nimailc.setClaimExpiryDate(nimailcbean.getClaimExpiryDate());
        nimailc.setBgType(nimailcbean.getBgType());
        nimailc.setlCValue(nimailcbean.getlCValue());
        nimailc.setlCCurrency(nimailcbean.getlCCurrency());
        nimailc.setLastShipmentDate(nimailcbean.getLastShipmentDate());
        nimailc.setNegotiationDate(nimailcbean.getNegotiationDate());
        nimailc.setPaymentPeriod(nimailcbean.getPaymentPeriod());
        nimailc.setPaymentTerms(nimailcbean.getPaymentTerms());
        nimailc.setTenorEndDate(nimailcbean.getTenorEndDate());
        nimailc.setUserType(nimailcbean.getUserType());
        nimailc.setApplicantName(nimailcbean.getApplicantName());
        nimailc.setApplicantCountry(nimailcbean.getApplicantCountry());
        nimailc.setApplicantContactPerson(nimailcbean.getApplicantContactPerson());
        nimailc.setApplicantContactPersonEmail(nimailcbean.getApplicantContactPersonEmail());
        nimailc.setBeneName(nimailcbean.getBeneName());
        nimailc.setBeneBankCountry(nimailcbean.getBeneBankCountry());
        nimailc.setBeneContactPerson(nimailcbean.getBeneContactPerson());
        nimailc.setBeneContactPersonEmail(nimailcbean.getBeneContactPersonEmail());
        nimailc.setBeneBankName(nimailcbean.getBeneBankName());
        nimailc.setBeneSwiftCode(nimailcbean.getBeneSwiftCode());
        nimailc.setBeneCountry(nimailcbean.getBeneCountry());
        nimailc.setLoadingCountry(nimailcbean.getLoadingCountry());
        nimailc.setLoadingPort(nimailcbean.getLoadingPort());
        nimailc.setDischargeCountry(nimailcbean.getDischargeCountry());
        nimailc.setDischargePort(nimailcbean.getDischargePort());
        nimailc.setChargesType(nimailcbean.getChargesType());
        System.out.println("$$$$$$$$$$ "+nimailcbean.getValidity());
        nimailc.setValidity(new SimpleDateFormat("yyyy-MM-dd")
                .format(nimailcbean.getValidity()));
        System.out.println("$$$$$$$$$$ "+nimailc.getValidity());
        nimailc.setInsertedDate(nimailcbean.getInsertedDate());
        nimailc.setInsertedBy(nimailcbean.getInsertedBy());
        nimailc.setModifiedDate(nimailcbean.getModifiedDate());
        nimailc.setModifiedBy(nimailcbean.getModifiedBy());
        nimailc.setTransactionflag(nimailcbean.getTransactionFlag());
        nimailc.setTransactionStatus(nimailcbean.getTransactionStatus());
        nimailc.setBranchUserId(nimailcbean.getBranchUserId());
        nimailc.setBranchUserEmail(nimailcbean.getBranchUserEmail());
        nimailc.setGoodsType(nimailcbean.getGoodsType());
        nimailc.setUsanceDays(nimailcbean.getUsanceDays());
        nimailc.setStartDate(nimailcbean.getStartDate());
        nimailc.setEndDate(nimailcbean.getEndDate());
        nimailc.setOriginalTenorDays(nimailcbean.getOriginalTenorDays());
        nimailc.setRefinancingPeriod(nimailcbean.getRefinancingPeriod());
        nimailc.setLcMaturityDate(nimailcbean.getLcMaturityDate());
        nimailc.setLcNumber(nimailcbean.getLcNumber());
        nimailc.setLastBeneBank(nimailcbean.getLastBeneBank());
        nimailc.setLastBeneSwiftCode(nimailcbean.getLastBeneSwiftCode());
        nimailc.setLastBankCountry(nimailcbean.getLastBankCountry());
        nimailc.setRemarks(nimailcbean.getRemarks());
        nimailc.setDiscountingPeriod(nimailcbean.getDiscountingPeriod());
        nimailc.setConfirmationPeriod(nimailcbean.getConfirmationPeriod());
        nimailc.setFinancingPeriod(nimailcbean.getFinancingPeriod());
        nimailc.setLcProForma(nimailcbean.getLcProForma());
        nimailc.setTenorFile(nimailcbean.getTenorFile());
        nimailc.setIsESGComplaint(nimailcbean.getIsESGComplaint());
        nimailc.setBillType(nimailcbean.getBillType());
        nimailc.setSecTransactionType(nimailcbean.getSecTransactionType());
        nimailc.setApplicableLaw(nimailcbean.getApplicableLaw());
        nimailc.setCommissionScheme(nimailcbean.getCommissionScheme());
        nimailc.setMinParticipationAmt(nimailcbean.getMinParticipationAmt());
        nimailc.setRetentionAmt(nimailcbean.getRetentionAmt());
        nimailc.setBenchmark(nimailcbean.getBenchmark());
        nimailc.setOtherCondition(nimailcbean.getOtherCondition());
        nimailc.setOfferedPrice(nimailcbean.getOfferedPrice());
        nimailc.setParticipationBasis(nimailcbean.getParticipationBasis());
        lctemprepo.save(nimailc);
    }

	@Override
	public void updateTransactionActivity(String userIdByQid, String transId, String emailId ,String action) {
		// TODO Auto-generated method stub
		
		TransactionActivity tr=null;
		if(action.equalsIgnoreCase("Placed"))
		{
			System.out.println("New Rec in transaction activity");
			tr=new TransactionActivity();
			tr.setUserId(userIdByQid);
			tr.setTxnId(transId);
			tr.setPlacedBy(emailId);
			Date now=new Date();
			tr.setInsertedDate(now);
			trActivity.save(tr);
		}
		if(action.equalsIgnoreCase("Accept"))
		{
			TransactionActivity ta=trActivity.getDetailByUserIdTxnId(userIdByQid,transId);
			System.out.println("ta: "+ta);
			if(ta!=null)
			{
				System.out.println("ta.getId(): "+ta.getId());
				System.out.println("Updating transaction activity");
				tr=trActivity.getOne(ta.getId());
				tr.setAcceptBy(emailId);
				Date now=new Date();
				tr.setModifiedDate(now);
				trActivity.save(tr);
			}
			else
			{
				System.out.println("New Rec in transaction activity");
				tr=new TransactionActivity();
				tr.setUserId(userIdByQid);
				tr.setTxnId(transId);
				tr.setAcceptBy(emailId);
				Date now=new Date();
				tr.setInsertedDate(now);
				trActivity.save(tr);
			}
		}
		if(action.equalsIgnoreCase("Reject"))
		{
			TransactionActivity ta=trActivity.getDetailByUserIdTxnId(userIdByQid,transId);
			System.out.println("ta: "+ta);
			if(ta!=null)
			{
				System.out.println("ta.getId(): "+ta.getId());
				System.out.println("Updating transaction activity");
				tr=trActivity.getOne(ta.getId());
				tr.setRejectBy(emailId);
				Date now=new Date();
				tr.setModifiedDate(now);
				trActivity.save(tr);
			}
			else
			{
				System.out.println("New Rec in transaction activity");
				tr=new TransactionActivity();
				tr.setUserId(userIdByQid);
				tr.setTxnId(transId);
				tr.setRejectBy(emailId);
				Date now=new Date();
				tr.setInsertedDate(now);
				trActivity.save(tr);
			}
		}
		if(action.equalsIgnoreCase("Reopen"))
		{
			TransactionActivity ta=trActivity.getDetailByUserIdTxnId(userIdByQid,transId);
			System.out.println("ta: "+ta);
			if(ta!=null)
			{
				System.out.println("ta.getId(): "+ta.getId());
				System.out.println("Updating transaction activity");
				tr=trActivity.getOne(ta.getId());
				tr.setReopenBy(emailId);
				Date now=new Date();
				tr.setModifiedDate(now);
				trActivity.save(tr);
			}
			else
			{
				System.out.println("New Rec in transaction activity");
				tr=new TransactionActivity();
				tr.setUserId(userIdByQid);
				tr.setTxnId(transId);
				tr.setReopenBy(emailId);
				Date now=new Date();
				tr.setInsertedDate(now);
				trActivity.save(tr);
			}
		}
		if(action.equalsIgnoreCase("Cancel"))
		{
			TransactionActivity ta=trActivity.getDetailByUserIdTxnId(userIdByQid,transId);
			System.out.println("ta: "+ta);
			if(ta!=null)
			{
				System.out.println("ta.getId(): "+ta.getId());
				System.out.println("Updating transaction activity");
				tr=trActivity.getOne(ta.getId());
				tr.setCancelBy(emailId);
				Date now=new Date();
				tr.setModifiedDate(now);
				trActivity.save(tr);
			}
			else
			{
				System.out.println("New Rec in transaction activity");
				tr=new TransactionActivity();
				tr.setUserId(userIdByQid);
				tr.setTxnId(transId);
				tr.setCancelBy(emailId);
				Date now=new Date();
				tr.setInsertedDate(now);
				trActivity.save(tr);
			}
		}
	}
}
