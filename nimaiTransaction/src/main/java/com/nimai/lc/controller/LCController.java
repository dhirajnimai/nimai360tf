package com.nimai.lc.controller;

import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nimai.lc.NimaiTransactionApplication;
import com.nimai.lc.bean.CustomerTransactionBean;
import com.nimai.lc.bean.NewRequestBean;
import com.nimai.lc.bean.NimaiCustomerBean;
import com.nimai.lc.bean.NimaiLCBean;
import com.nimai.lc.bean.NimaiLCMasterBean;
import com.nimai.lc.bean.NimaiLCPortBean;
import com.nimai.lc.bean.OfflineTxnBankBean;
import com.nimai.lc.bean.QuotationBean;
import com.nimai.lc.bean.QuotationMasterBean;
import com.nimai.lc.bean.SelectBankUser;
import com.nimai.lc.entity.AdditionalUserList;
import com.nimai.lc.entity.Countrycurrency;
import com.nimai.lc.entity.Goods;
import com.nimai.lc.entity.NewRequestEntity;
import com.nimai.lc.entity.NimaiClient;
import com.nimai.lc.entity.NewRequestEntity;
import com.nimai.lc.entity.NimaiLC;
import com.nimai.lc.entity.NimaiLCMaster;
import com.nimai.lc.entity.NimaiLCPort;
import com.nimai.lc.entity.NimaiSubscriptionDetails;
import com.nimai.lc.entity.OfflineTxnBank;
import com.nimai.lc.entity.Quotation;
import com.nimai.lc.payload.GenericResponse;
import com.nimai.lc.repository.CountrycurrencyRepository;
import com.nimai.lc.repository.GoodsRepository;
import com.nimai.lc.repository.LCMasterRepository;
import com.nimai.lc.repository.LCRepository;
import com.nimai.lc.repository.NimaiClientRepository;
import com.nimai.lc.service.CSVService;
import com.nimai.lc.service.LCService;
import com.nimai.lc.service.QuotationService;
import com.nimai.lc.utility.ErrorDescription;
import com.nimai.lc.utility.NimaiLCValidation;

@CrossOrigin(origins = "*")
@RestController
public class LCController {

	private static final Logger logger = LoggerFactory.getLogger(NimaiTransactionApplication.class);

	@Autowired
	LCService lcservice;
	
	@Autowired
	CSVService csvFileService;

	@Autowired
	QuotationService quotationService;

	@Autowired
	NimaiLCValidation lcValid;
	
	@Autowired
	NimaiClientRepository cuRepo;

	@Autowired
	GoodsRepository goodsRepo;

	@Autowired
	LCRepository lcrepo;

	@Autowired
	LCMasterRepository lcmasterrepo;
	
	@Autowired
	GenericResponse response;

	@Autowired
	CountrycurrencyRepository countryrepo;

	private String creditBoundary;
	
	@Value("${postpaid.credit.boundary}")
    private String postpaidCreditBoundary;
    
    @Value("${prepaid.credit.boundary}")
    private String prepaidCreditBoundary;
    
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@PostMapping(value = "/saveLCToDraft", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> saveLCdetails(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Save LC To Draft ===========");
		GenericResponse response = new GenericResponse<>();
		// GenericResponse<Object> response = new GenericResponse<Object>();
		String userId=nimailcbean.getUserId();
		String statusString = "success"; // this.lcValid.validateLCDetails(nimailcbean);
		if (statusString.equalsIgnoreCase("Success")) {
			try {
				if(nimailcbean.getUserId()==null)
				{
					System.out.println("UserID is null");
					userId=lcrepo.getUserIdByApplicantNameBeneName(nimailcbean.getApplicantName(),nimailcbean.getBeneName());
				}
				String tid = generateTransactionId(userId, nimailcbean.getRequirementType(),
						nimailcbean.getlCIssuanceCountry());
				nimailcbean.setUserId(userId);
				lcservice.saveLCdetails(nimailcbean, tid);

				response.setStatus("Success");
				response.setData(tid);
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} catch (Exception e) {
				response.setStatus("Failure");
				response.setErrCode("EXE000");
				response.setErrMessage(ErrorDescription.getDescription("EXE000"));
				return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
			}
		} else {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(statusString);
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}
	}

	private String generateTransactionId(String userid, String transType, String countryName) {
		// TODO Auto-generated method stub

		StringBuffer newtransactionId = new StringBuffer();
		newtransactionId.append(userid.substring(0, 2));
		newtransactionId.append(lcservice.generateYear());
		newtransactionId.append(lcservice.generateCountryCode(countryName));
		newtransactionId.append(lcservice.generateTransactionType(transType));
		newtransactionId.append(lcservice.generateSerialNo());

		System.out.println(" TRANSACTION ID :::::::::::: " + newtransactionId.toString());
		return newtransactionId.toString();

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getcountryData", produces = "application/json", method = RequestMethod.GET)
	public List<Countrycurrency> getCountry() {
		try {
			return (List<Countrycurrency>) countryrepo.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		return null;

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getAllTransactionDetails", produces = "application/json", method = RequestMethod.GET)
	public ResponseEntity<?> getTransactiondetails() {
		logger.info("=========== Get all transaction details ===========");
		GenericResponse response = new GenericResponse<>();
		List<NimaiLCMaster> transactions = lcservice.getAllTransactionDetails();
		if (transactions.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			// lcservice.setDataToList(transactions);
			response.setData(transactions);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
	}

	/*
	 * public List<NimaiLC> getAllTransaction() { return
	 * lcservice.getAllTransactionDetails(); }
	 */

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getSpecificTxnDetailByTxnId", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getSpecificTransaction(@RequestBody NimaiLCMasterBean nimailcbean) {
		logger.info("=========== Get Specific Transaction By TransactionId ===========");
		GenericResponse response = new GenericResponse<>();
		String transactionId = nimailcbean.getTransactionId();
		System.out.println("" + transactionId);
		NimaiLCMaster trans = lcservice.getSpecificTransactionDetail(transactionId);
		if (trans == null) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(trans);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getTransactionDetailByUserId", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getTransactionByUserId(@RequestBody NimaiLCMasterBean nimailc) {
		logger.info("=========== Get Transaction By UserId ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = nimailc.getUserId();
		String branchEmailId = nimailc.getBranchUserEmail();
		List<NimaiLCMaster> transactions = lcservice.getTransactionDetailByUserId(userId, branchEmailId);
		if (transactions.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(transactions);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getAllTxnDetailsByStatus/{status}", produces = "application/json", method = RequestMethod.GET)
	public ResponseEntity<?> getAllTransactionByStatus(@PathVariable(value = "status") String status) {
		logger.info("=========== Get Transaction By Status ===========");
		System.out.println("Requested Status: " + status);
		GenericResponse response = new GenericResponse<>();

		List<NimaiLCMaster> transactions = lcservice.getAllTransactionDetailsByStatus(status);

		if (transactions.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(transactions);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getAllTxnByUserIdAndStatus", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getTransactionByUserIdAndStatus(@RequestBody NimaiLCMasterBean nimailc) {
		logger.info("=========== Get Transactions By UserId and Status ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = nimailc.getUserId();
		String status = nimailc.getTransactionStatus();
		String branchEmailId = nimailc.getBranchUserEmail();
//		lcservice.updateQuotationReceivedForValidityDateExp(userId);
		List<NimaiLCMaster> transactions = lcservice.getTransactionDetailByUserIdAndStatus(userId, status,
				branchEmailId);
		if (transactions.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(transactions);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/cloneLC", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> cloneLCDetails(@RequestBody NimaiLCBean nimailcBean) {
		logger.info("=========== Clonning LC ===========");
		GenericResponse response = new GenericResponse<>();
		try {
			String transId = nimailcBean.getTransactionId();
			NimaiLCMaster nimailcDetails = lcservice.getSpecificTransactionDetail(transId);
			// String updatedtid = generateTransactionId(nimailcDetails.getUserId(),
			// nimailcDetails.getRequirementType());

			// lcservice.cloneLCDetail(transId, updatedtid);
			// System.out.println("nimaibean: "+nimailcBean.toString());
			// System.out.println("nimaiEntity: "+nimailcDetails.toString());
			if (nimailcDetails == null) {
				response.setStatus("Failure");
				response.setErrCode("ASA002");
				response.setErrMessage(ErrorDescription.getDescription("ASA002"));
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				response.setStatus("Success");
				response.setData(nimailcDetails);
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}
		} catch (Exception e) {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(ErrorDescription.getDescription("EXE000"));
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/confirmLC", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<Object> confirmLCDetails(@RequestBody NimaiLCBean nimailc) {
		logger.info("=========== Confirming LC ===========");
		GenericResponse response = new GenericResponse<>();
		// Integer lcCount,utilizedLcCount;
		String statusString = "Success"; // this.lcValid.validateUserTransaction(nimailc);
		// String obtainUserId=lcservice.checkMasterForSubsidiary(nimailc.getUserId());
		NimaiClient userDetails = cuRepo.getOne(nimailc.getUserId());
		NimaiClient obtainUserId = lcservice.checkMasterSubsidiary(userDetails.getAccountType(), nimailc.getUserId(),
				userDetails);

		if (obtainUserId == null) {
			response.setStatus("Failure");
			response.setErrMessage("Please Select Subscription Plan");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
		try {
			System.out.println("ObtainUserID: "+obtainUserId.getUserid());
			String subsStatus=lcmasterrepo.findActivePlanByUserId(obtainUserId.getUserid());
			if(subsStatus.equalsIgnoreCase("Active"))
				return lcservice.saveTempLc(obtainUserId, nimailc);
			else
				return lcservice.saveTempLc(obtainUserId, nimailc);
			

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus("Failure");
			System.out.println("You are not Subscribe to a Plan. Please Subscribe");
			response.setErrMessage("You are not Subscribe to a Plan. Please Subscribe");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/checkLCCount", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> checkLCCount(@RequestBody NimaiLCBean nimailc) {
		logger.info("=========== Checking LC Count ===========");
		GenericResponse response = new GenericResponse<>();
		String userid=nimailc.getUserId();
		String accounttype=lcservice.getAccountType(userid);
		System.out.println("Account type: "+accounttype);
		if(accounttype.equalsIgnoreCase("subsidiary"))
		{
			userid=lcservice.getAccountSource(userid);
		}
		String statusString = "Success"; // this.lcValid.validateUserTransaction(nimailc);
		Integer lcCount,utilizedLcCount;
		try {
			lcCount = lcservice.getLcCount(userid);
			utilizedLcCount = lcservice.getUtilizedLcCount(userid);
			System.out.println("Counts for User: " + userid);
			System.out.println("LC Count: " + lcCount);
			System.out.println("LC Utilzed Count: " + utilizedLcCount);
			if(lcrepo.findPlanName(userid).equalsIgnoreCase("POSTPAID_PLAN"))
	        {
	        	System.out.println("Postpaid Plan");
	        	creditBoundary=postpaidCreditBoundary;
	        }
	        else
	        {
	        	System.out.println("Prepaid Plan");
	        	creditBoundary=prepaidCreditBoundary;
	        }
			System.out.println("Credit Boundary: "+creditBoundary);
			/*if (lcCount == 0) {
				response.setStatus("Failure");
				response.setErrMessage("You had reached maximum LC Count! Please Renew Your Subscription Plan");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else if (lcCount > utilizedLcCount) {
				response.setStatus("Success");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				response.setStatus("Failure");
				response.setErrMessage("You had reached maximum LC Count! Please Renew Your Subscription Plan");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}*/
			
			if (lcCount == (utilizedLcCount-Integer.valueOf(creditBoundary))) {
				response.setStatus("Failure");
				response.setErrMessage("You had reached maximum LC Count! Please Renew Your Subscription Plan");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else if (lcCount >= (utilizedLcCount-Integer.valueOf(creditBoundary))) {
				response.setStatus("Success");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				response.setStatus("Failure");
				response.setErrMessage("You had reached maximum LC Count! Please Renew Your Subscription Plan");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}
		} catch (NullPointerException ne) {
			String sts=lcrepo.findLatestStatusForSubscription(userid);
			lcCount=lcrepo.findLCCountForInactive(userid);
			utilizedLcCount=lcrepo.findUtilzedLCCountForInactive(userid);
			if((lcCount == (utilizedLcCount-Integer.valueOf(creditBoundary))) && sts.equalsIgnoreCase("inactive"))
			{
				response.setStatus("Failure");
				System.out.println("You had reached maximum LC Count and plan is expired. Please Renew Your Subscription Plan");
				response.setErrMessage("You are not Subscribe to a Plan. Please Subscribe");
			}
			else
			{
				response.setStatus("Success");
				//System.out.println("");
				//response.setErrMessage("");
			}
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	/*
	 * @CrossOrigin(value = "*", allowedHeaders = "*")
	 * 
	 * @RequestMapping(value = "/calculateQuote", produces = "application/json",
	 * method = RequestMethod.POST) public ResponseEntity<?>
	 * calculateQuote(@RequestBody NimaiLCBean nimailc) { GenericResponse response =
	 * new GenericResponse<>();
	 * 
	 * try { HashMap<String, Integer>
	 * outputFields=lcservice.calculateQuote(nimailc.getTransactionId());
	 * response.setStatus("Success"); response.setData(outputFields); return new
	 * ResponseEntity<Object>(response, HttpStatus.OK); } catch (Exception e) {
	 * response.setStatus("Failure"); response.setErrCode("EXE000");
	 * response.setErrMessage("Exception: "+e); return new
	 * ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST); } }
	 */

	/*
	 * @CrossOrigin(value = "*", allowedHeaders = "*")
	 * 
	 * @RequestMapping(value = "/placeQuotation", produces = "application/json",
	 * method = RequestMethod.POST) public ResponseEntity<?>
	 * placeQuotation(@RequestBody QuotationBean quotationBean) { GenericResponse
	 * response = new GenericResponse<>(); try {
	 * 
	 * String transId = quotationBean.getTransactionId(); NimaiLC
	 * transDetails=lcservice.checkTransaction(transId); if(transDetails==null) {
	 * response.setStatus("Failure"); response.setErrCode("ASA002");
	 * response.setErrMessage(ErrorDescription.getDescription("ASA002")); return new
	 * ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST); } else {
	 * quotationService.saveQuotationdetails(quotationBean);
	 * lcservice.quotePlace(transId); response.setStatus("Success"); return new
	 * ResponseEntity<Object>(response, HttpStatus.OK); } //
	 * System.out.println("nimaibean: "+nimailcBean.toString()); //
	 * System.out.println("nimaiEntity: "+nimailcDetails.toString());
	 * 
	 * } catch (Exception e) { response.setStatus("Failure");
	 * response.setErrCode("EXE000");
	 * response.setErrMessage(ErrorDescription.getDescription("EXE000")); return new
	 * ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST); }
	 * 
	 * }
	 */

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getNewRequestsForBankSecondary", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getTransactionForBankSecondary(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Get new Request for Bank Secondary ===========");
		GenericResponse response = new GenericResponse<>();
		String userid = nimailcbean.getUserId();
		//String requirement=nimailcbean.getRequirementType();
		// List<NimaiLCMaster> transactions =
		// lcservice.getAllTransactionForBank(userid);
		String obtainUserId = userid;
		// lcservice.checkMasterForSubsidiary(userid);
		List<NimaiLCMaster> newRequest = lcservice.getAllTransactionForBankSec(obtainUserId,nimailcbean);
		if (newRequest.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(newRequest);
			response.setStatus("Success");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getAllNewRequestsForBank", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getTransactionForBank(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Get new Request for Bank ===========");
		GenericResponse response = new GenericResponse<>();
		String userid = nimailcbean.getUserId();
		String requirement=nimailcbean.getRequirementType();
		// List<NimaiLCMaster> transactions =
		// lcservice.getAllTransactionForBank(userid);
		String obtainUserId = userid;
		// lcservice.checkMasterForSubsidiary(userid);
		List<NewRequestEntity> newRequest = lcservice.getAllTransactionForBank(obtainUserId,requirement);
		if (newRequest.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(newRequest);
			response.setStatus("Success");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@PostMapping(value = "/updateMasterLC", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateLCdetails(@RequestBody NimaiLCMasterBean nimailcbean) {
		logger.info("=========== Updating Master LC ===========");
		GenericResponse response = new GenericResponse<>();
		String transId = nimailcbean.getTransactionId();
		String userId = nimailcbean.getUserId();
		// GenericResponse<Object> response = new GenericResponse<Object>();
		// String statusString = this.lcValid.validateLCDetails(nimailcbean);
		// if (statusString.equalsIgnoreCase("Success"))
		// {
		try {

			lcservice.moveToHistory(transId, userId);
			lcservice.saveLCMasterdetails(nimailcbean, transId);
			//lcservice.getAlleligibleBAnksEmail(userId, transId, 0, "LC_UPDATE_ALERT_ToBanks", "LC_UPDATE(DATA)");
			response.setStatus("Success");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + e);
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}
		/*
		 * } else { response.setStatus("Failure"); response.setErrCode("EXE000");
		 * response.setErrMessage(statusString); return new
		 * ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST); }
		 */
	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getDraftTransactionByUserId", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getDraftTransaction(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Get Draft Transaction By UserId ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = nimailcbean.getUserId();
		String branchEmailId = nimailcbean.getBranchUserEmail();
		
		List<NimaiLC> draftTransactions = lcservice.getAllDraftTransactionDetails(userId, branchEmailId);
		if (draftTransactions.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			// lcservice.setDataToList(transactions);
			response.setData(draftTransactions);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@PostMapping(value = "/updateLCDraft", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateDraftLCdetails(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Updating draft LC ===========");
		GenericResponse response = new GenericResponse<>();
		String userId=nimailcbean.getUserId();
		String statusString = "Success";// this.lcValid.validateLCDetails(nimailcbean);
		if (statusString.equalsIgnoreCase("Success")) {
			try {
				if(nimailcbean.getUserId()==null)
				{
					userId=lcrepo.getUserIdByApplicantNameBeneName(nimailcbean.getApplicantName(),nimailcbean.getBeneName());
				}
				String newtid = generateTransactionId(userId, nimailcbean.getRequirementType(),
						nimailcbean.getlCIssuanceCountry());
				nimailcbean.setUserId(userId);
				lcservice.updateDraftLCdetails(nimailcbean, newtid);
				response.setStatus("Success");
				response.setData(newtid);
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} catch (Exception e) {
				response.setStatus("Failure");
				response.setErrCode("EXE000");
				response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + e);
				return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
			}
		} else {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(statusString);
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getSpecificDraftTxnDetailByTxnId", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getSpecificDraftTransaction(@RequestBody NimaiLCMasterBean nimailcbean) {
		logger.info("=========== Get Specific Draft Transaction By TransactionId ===========");
		GenericResponse response = new GenericResponse<>();
		String transactionId = nimailcbean.getTransactionId();
		System.out.println("" + transactionId);
		NimaiLC trans = lcservice.getSpecificDraftTransactionDetail(transactionId);
		if (trans == null) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(trans);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/reopenTransactionByTxnIdUserId/{emailId}", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> reopenTransactionByTxnIdUserId(@PathVariable("emailId") String emailId,@RequestBody NimaiLCMasterBean nimailcbean)
			throws ParseException {
		logger.info("=========== Reopen Transaction By TransactionId and UserId ===========");
		GenericResponse response = new GenericResponse<>();
		String transactionId = nimailcbean.getTransactionId();
		String userId = nimailcbean.getUserId();

		// userId=obtainUserId;
		System.out.println("Reopening Transaction: " + transactionId + " for user: " + userId);
		NimaiLCMaster trans = lcservice.checkTransaction(transactionId);
		if (trans == null) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			Integer qid = null;
			try {
				qid = quotationService.getRejectedQuotationByTransactionId(transactionId);
			} catch (Exception e) {
				lcservice.updateTransactionStatusToActive(transactionId, userId);
				lcservice.updateReopenCounter(transactionId);
				response.setStatus("Failure");
				response.setErrMessage("No Quote Available for Re-Placed");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}
			System.out.println("Rejected QuotationId: " + qid);
			if (qid == null) {
				response.setStatus("Failure");
				response.setErrMessage("No Rejected Quotation Present");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				lcservice.updateTransactionStatusToActive(transactionId, userId);
				lcservice.updateReopenCounter(transactionId);
				Integer reopenCounter = lcservice.getReopenCounter(transactionId);
				String obtainUserId = lcservice.checkMasterForSubsidiary(userId);
				/*if (reopenCounter <= 3) {
					lcservice.updateLCUtilized(obtainUserId);
				} else {
					lcservice.updateLCUtilizedReopen4Times(obtainUserId);
				}*/
				System.out.println("Reopen Counter: " + reopenCounter);
				quotationService.updateQuotationStatusForReopenToRePlaced(qid, transactionId);
				QuotationBean bean =new QuotationBean();
				NimaiLCMasterBean nimailcmasterbean=new NimaiLCMasterBean();
				if(transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
				{
					lcservice.updateTransactionActivity(userId, transactionId, emailId, "Reopen");
				}
				lcservice.getAlleligibleBAnksEmail(userId, transactionId, 0, "LC_REOPENING_ALERT_ToBanks", "",bean,"",nimailcmasterbean);

				response.setStatus("Success");// .setData(trans);
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}

		}
	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@PostMapping(value = "/getBankCountForCountry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getBankCountdetails(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Getting Bank Count for  ===========");
		GenericResponse response = new GenericResponse<>();
		// GenericResponse<Object> response = new GenericResponse<Object>();
		String countryName = nimailcbean.getlCIssuanceCountry();
		try {
			Integer noOfBanks = lcservice.getNoOfBanksAgainstCountry(countryName);
			if (noOfBanks != 0) {
				response.setStatus("Success");
				response.setData(noOfBanks + " Banks Available for the country you selected");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				response.setStatus("Success");
				response.setData("No Banks Available for the country you selected");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}
		} catch (Exception e) {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(ErrorDescription.getDescription("EXE000"));
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/deleteDraftTransactionByTxnId", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> deleteDraftTransaction(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Deleting Draft Transaction By transactionId ===========");
		GenericResponse response = new GenericResponse<>();
		String transactionId = nimailcbean.getTransactionId();
		List<NimaiLC> draftTransactions = lcservice.getDraftTransactionDetails(transactionId);
		if (draftTransactions.isEmpty()) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			// lcservice.setDataToList(transactions);
			lcservice.deleteDraftTransaction(transactionId);
			response.setData("Transaction " + transactionId + " Deleted Successfully");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/closeTransaction", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> closeTransaction(@RequestBody NimaiLCBean nimailcbean) {
		logger.info("=========== Closing Transaction ===========");
		GenericResponse response = new GenericResponse<>();
		String transactionId = nimailcbean.getTransactionId();
		String userId = nimailcbean.getUserId();
		String reason = nimailcbean.getStatusReason();
		try {
			lcservice.updateTransactionForClosed(transactionId, userId, reason);

			response.setStatus("Transaction Closed Successfully");
			return new ResponseEntity<Object>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + e);
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getTxnForCustomerByUserIdAndStatus", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getTransactionForCustomerByUserIdAndStatus(@RequestBody NimaiLCMasterBean nimailc)
			throws ParseException {
		logger.info("=========== Get Transactions By UserId and Status ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = nimailc.getUserId();
		String status = nimailc.getTransactionStatus();
		String branchEmailId = nimailc.getBranchUserEmail();
//		lcservice.updateQuotationReceivedForValidityDateExp(userId);
		List<CustomerTransactionBean> transactions = lcservice.getTransactionForCustomerByUserIdAndStatus(userId,
				status, branchEmailId);
		if (transactions.isEmpty() || transactions == null) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(transactions);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/checkDuplicateLC", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getLCStatusForDuplicate(@RequestBody NimaiLCMasterBean nimailc) {
		logger.info("=========== Checking Duplicate LC ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = nimailc.getUserId();
		String transactionId = nimailc.getTransactionId();
		int matches = lcservice.getSpecificDraftTransactionDetailForDuplicate(userId, transactionId);
		if (matches < 3) {
			response.setStatus("Success");
			response.setErrMessage("No Duplicate LC");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setStatus("Success");
			response.setErrMessage("Duplicate LC");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/validateTransaction", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> validateTransaction(@RequestBody QuotationMasterBean quoteBean) {
		logger.info("=========== Validating Transaction ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = quoteBean.getUserId();
		String transactionId = quoteBean.getTransactionId();
		String bankUserId = quoteBean.getBankUserId();
		NimaiLCMaster trans = lcservice.getSpecificTransactionDetail(transactionId);
		if (trans == null) {
			response.setStatus("Failure");
			response.setErrCode("ASA002");
			response.setErrMessage(ErrorDescription.getDescription("ASA002"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			quotationService.updateQuotationStatusForFreezeToPlaced(transactionId, bankUserId);
			response.setStatus("Validate Success");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getPortByCountry", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getPortByCountryName(@RequestBody NimaiLCPortBean nimailcport) {
		logger.info("=========== Get Port By Country Name ===========");
		GenericResponse response = new GenericResponse<>();
		String countryName = nimailcport.getCountryName();
		System.out.println("Port for Country: " + countryName);
		List<NimaiLCPort> port = lcservice.getPortListByCountry(countryName);
		response.setData(port);
		response.setStatus("Success");
		return new ResponseEntity<Object>(response, HttpStatus.OK);

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/cancelTransaction/{emailId}", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> cancelTransaction(@PathVariable("emailId") String emailId,@RequestBody NimaiLCMasterBean nimailcbean) {
		logger.info("=========== Cancel Transaction ===========");
		GenericResponse response = new GenericResponse<>();
		String transactionId = nimailcbean.getTransactionId();
		String userId = nimailcbean.getUserId();
		try {
			//NimaiLCMaster trans = lcservice.checkTransaction(transactionId);
			lcservice.updateTransactionForCancel(transactionId, userId);
			if(transactionId.substring(0, 2).equalsIgnoreCase("CU") || transactionId.substring(0, 2).equalsIgnoreCase("BC"))
			{
				lcservice.updateTransactionActivity(userId, transactionId, emailId, "Cancel");
			}
			response.setStatus("Transaction Cancel Successfully");
			return new ResponseEntity<Object>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + e);
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getCreditTxnForCustomerByUserId", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getCreditTransactionForCustomerByUserId(@RequestBody NimaiCustomerBean nimaiCustomer)
			throws ParseException {
		logger.info("=========== Get Credit and Transactions By UserId ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = nimaiCustomer.getUserid();
		Date fromDate = nimaiCustomer.getTxnInsertedDate();
		Date toDate = nimaiCustomer.getTxnDate();
		String subsidiaryName = nimaiCustomer.getCompanyName();
		String passcodeUser = nimaiCustomer.getPasscodeUser();
		List<NimaiCustomerBean> creditTxnDet = null;
		/*
		 * if(userId.substring(0, 3).equalsIgnoreCase("All")) {
		 * userId=userId.substring(3); }
		 * System.out.println("UserId after removing all: "+userId);
		 */
		try {
			if (subsidiaryName.equalsIgnoreCase("") && passcodeUser.equalsIgnoreCase("")) {
				if (fromDate == null && toDate == null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId);

				} else if (fromDate != null && toDate == null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, fromDate);
				} else if (fromDate == null && toDate != null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, new Date());
				} else {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, fromDate, toDate);
				}
			} else if (!subsidiaryName.equalsIgnoreCase("") && passcodeUser.equalsIgnoreCase("")) {
				if (fromDate == null && toDate == null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, subsidiaryName);

				} else if (fromDate != null && toDate == null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, fromDate, subsidiaryName);
				} else if (fromDate == null && toDate != null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, new Date(), subsidiaryName);
				} else {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, fromDate, toDate, subsidiaryName);
				}
			} else {
				if (fromDate == null && toDate == null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, subsidiaryName, passcodeUser);

				} else if (fromDate != null && toDate == null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, fromDate, subsidiaryName,
							passcodeUser);
				} else if (fromDate == null && toDate != null) {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, new Date(), subsidiaryName,
							passcodeUser);
				} else {
					creditTxnDet = lcservice.getCreditTxnForCustomerByUserId(userId, fromDate, toDate, subsidiaryName,
							passcodeUser);
				}
			}
			if (creditTxnDet.isEmpty() || creditTxnDet == null) {
				response.setStatus("Failure");
				response.setErrMessage("No Data Available");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				response.setData(creditTxnDet);
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}
		} catch (Exception e) {
			response.setStatus("Failure");
			response.setErrMessage("No Data Available");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getCreditTxnForCustomerByBankUserId", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> getCreditTransactionForCustomerByBankUserId(@RequestBody NimaiCustomerBean nimaiCustomer)
			throws ParseException {
		logger.info("=========== Get Credit and Transactions By UserId ===========");
		GenericResponse response = new GenericResponse<>();
		String userId = nimaiCustomer.getUserid();
		// String subsidiaryName= nimaiCustomer.getCompanyName();
		Date fromDate = nimaiCustomer.getTxnInsertedDate();
		Date toDate = nimaiCustomer.getTxnDate();
		List<NimaiCustomerBean> creditTxnDet = null;
		try {

			if (fromDate == null && toDate == null) {
				creditTxnDet = lcservice.getCreditTxnForCustomerByBankUserId(userId);

			} else if (fromDate != null && toDate == null) {
				creditTxnDet = lcservice.getCreditTxnForCustomerByBankUserId(userId, fromDate);
			} else if (fromDate == null && toDate != null) {
				creditTxnDet = lcservice.getCreditTxnForCustomerByBankUserId(userId, new Date());
			} else {
				creditTxnDet = lcservice.getCreditTxnForCustomerByBankUserId(userId, fromDate, toDate);
			}

			if (creditTxnDet.isEmpty() || creditTxnDet == null) {
				response.setStatus("Failure");
				response.setErrMessage("No Data Available");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				response.setData(creditTxnDet);
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			}
		} catch (Exception e) {
			response.setStatus("Failure");
			response.setErrMessage("No Data Available");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getGoodsData", produces = "application/json", method = RequestMethod.GET)
	public List<Goods> getGoods() {
		// return lcservice.getGoods();
		try {
			return (List<Goods>) goodsRepo.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		return null;
	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getGoodsData1", produces = "application/json", method = RequestMethod.GET)
	public ResponseEntity<?> getGoods1()
			throws ParseException {
		logger.info("=========== Get Transactions By UserId and Status ===========");
		GenericResponse response = new GenericResponse<>();
		List<Goods> goodsList = lcservice.getGoodsList();
		if (goodsList.isEmpty() || goodsList == null) {
			response.setStatus("Failure");
			response.setErrMessage("No Goods Available");
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		} else {
			response.setData(goodsList);
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getCurrency", produces = "application/json", method = RequestMethod.GET)
	public List<String> getCurrency() {
		try {
			return (List<String>) countryrepo.getCurrency();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		return null;

	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/downloadExcelReportForTransaction", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> downloadExcelReportForTxnByUserId(@RequestBody NimaiLCMasterBean nimailc) throws ParseException 
	{
		Date d=new Date();
		String filename = "TrasanctionDetail_"+nimailc.getUserId()+"_"+d+".csv";
	    InputStreamResource file = new InputStreamResource(csvFileService.loadDataForCustomer(nimailc.getUserId(),nimailc.getTransactionStatus(),nimailc.getBranchUserEmail()));
	    return ResponseEntity.ok()
	        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
	        .contentType(MediaType.parseMediaType("application/csv"))
	        .body(file);
	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/updateTransactionValidity", produces = "application/json", method = RequestMethod.POST)
	public ResponseEntity<?> updateValidityForTransaction(@RequestBody NimaiLCMasterBean nimailc)
	{
		logger.info("=========== Updating Transaction Validity ===========");
		GenericResponse response = new GenericResponse<>();

		String statusString = "Success";// this.lcValid.validateLCDetails(nimailcbean);
		if (statusString.equalsIgnoreCase("Success")) {
			try {
				lcservice.updateTransactionValidity(nimailc);
				response.setStatus("Success");
				response.setData("");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} catch (Exception e) {
				response.setStatus("Failure");
				response.setErrCode("EXE000");
				System.out.println("Error:"+e);
				response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + e);
				return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
			}
		} else {
			response.setStatus("Failure");
			response.setErrCode("EXE000");
			response.setErrMessage(statusString);
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value="*",allowedHeaders="*")
	@RequestMapping(value="updateTnC/{userId}",produces="application/json",method=RequestMethod.POST)
	public ResponseEntity<?> updateTermsAndCondition(@PathVariable("userId") String userId) 
	{
		logger.info("=========== Updating Terms and Conditions of Automated BAAU===========");
	    GenericResponse response = new GenericResponse();
	    try 
	    {
	    	lcmasterrepo.updateTnC(userId);
		    response.setStatus("Success");
		    response.setData("");
		    return new ResponseEntity(response, HttpStatus.OK);
		} 
	    catch (Exception var4) 
	    {
		    response.setStatus("Failure");
		    response.setErrCode("EXE000");
		    System.out.println("Error:" + var4);
		    response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + var4);
		    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value="/getBaauStatus/{emailId}", produces="application/json", method=RequestMethod.GET)
	public ResponseEntity<?> getBaauStatus(@PathVariable("emailId") String emailId) 
	{
		GenericResponse response = new GenericResponse();

		      try {
		         String userMode = cuRepo.findUserMode(emailId);
		         response.setData(userMode);
		         response.setStatus("Success");
		         return new ResponseEntity(response, HttpStatus.OK);
		      } catch (Exception var4) {
		         var4.printStackTrace();
		         response.setStatus("Failure");
		         response.setErrMessage("");
		         return new ResponseEntity(response, HttpStatus.OK);
		      }
	}
	
	@CrossOrigin(value="*",allowedHeaders="*")
	@RequestMapping(value="saveSelectBank/{userId}",produces="application/json",method=RequestMethod.POST)
	public ResponseEntity<?> saveSelectBank(@PathVariable("userId") String userId,@RequestBody SelectBankUser bankUserBean) 
	{
		logger.info("=========== Saving Select Bank ===========");
	    GenericResponse response = new GenericResponse();
	    try 
	    {
	    	return lcservice.saveSelectBankForTransaction(userId,bankUserBean);
		    //response.setStatus("Success");
		    //response.setData("Data Added Successfully");
		    //return new ResponseEntity(response, HttpStatus.OK);
		} 
	    catch (Exception var4) 
	    {
		    response.setStatus("Failure");
		    response.setErrCode("EXE000");
		    System.out.println("Error:" + var4);
		    response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + var4);
		    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value="*",allowedHeaders="*")
	@RequestMapping(value="updateSelectBank/{userId}",produces="application/json",method=RequestMethod.POST)
	public ResponseEntity<?> updateSelectBank(@PathVariable("userId") String userId,@RequestBody SelectBankUser bankUserBean) 
	{
		logger.info("=========== Saving Select Bank ===========");
	    GenericResponse response = new GenericResponse();
	    try 
	    {
	    	return lcservice.updateSelectBankForTransaction(userId,bankUserBean);
		    //response.setStatus("Success");
		    //response.setData("Data Added Successfully");
		    //return new ResponseEntity(response, HttpStatus.OK);
		} 
	    catch (Exception var4) 
	    {
		    response.setStatus("Failure");
		    response.setErrCode("EXE000");
		    System.out.println("Error:" + var4);
		    response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + var4);
		    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value="*",allowedHeaders="*")
	@RequestMapping(value="updateSelectBankForTxn/{parentUserId}/{txnId}",produces="application/json",method=RequestMethod.POST)
	public ResponseEntity<?> updateSelectBank(@PathVariable("parentUserId") String parentUserId,@PathVariable("txnId") String txnId,@RequestBody List<OfflineTxnBankBean> offlineTxnBankBean) 
	{
		logger.info("=========== Updating offline Select Bank ===========");
	    GenericResponse response = new GenericResponse();
	    try 
	    {
	    	lcservice.saveOfflineSelectBankForTransaction(parentUserId,txnId,offlineTxnBankBean);
		    response.setStatus("Success");
		    response.setData("Data Added Successfully");
		    return new ResponseEntity(response, HttpStatus.OK);
		} 
	    catch (Exception var4) 
	    {
		    response.setStatus("Failure");
		    response.setErrCode("EXE000");
		    System.out.println("Error:" + var4);
		    response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + var4);
		    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value="*",allowedHeaders="*")
	@RequestMapping(value="viewSelectBank/{userId}",produces="application/json",method=RequestMethod.POST)
	public ResponseEntity<?> viewSelectBank(@PathVariable("userId") String userId) 
	{
		logger.info("=========== Getting Select Bank ===========");
	    GenericResponse response = new GenericResponse();
	    try 
	    {
	    	List<AdditionalUserList> sbu=lcservice.getSelectBank(userId);
	    	
		    response.setStatus("Success");
		    response.setData(sbu);
		    return new ResponseEntity(response, HttpStatus.OK);
		} 
	    catch (Exception var4) 
	    {
		    response.setStatus("Failure");
		    response.setErrCode("EXE000");
		    System.out.println("Error:" + var4);
		    response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + var4);
		    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value="*",allowedHeaders="*")
	@RequestMapping(value="viewSelectBankForTxn/{parentUserId}/{txnId}",produces="application/json",method=RequestMethod.POST)
	public ResponseEntity<?> viewSelectBankForTxn(@PathVariable("parentUserId") String parentUserId,
			@PathVariable("txnId") String txnId) 
	{
		logger.info("=========== View offline Select Bank ===========");
	    GenericResponse response = new GenericResponse();
	    try 
	    {
	    	List<OfflineTxnBank> otb=lcservice.getOfflineSelectBank(parentUserId,txnId);
		    response.setStatus("Success");
		    response.setData(otb);
		    return new ResponseEntity(response, HttpStatus.OK);
		} 
	    catch (Exception var4) 
	    {
		    response.setStatus("Failure");
		    response.setErrCode("EXE000");
		    System.out.println("Error:" + var4);
		    response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + var4);
		    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value="*",allowedHeaders="*")
	@RequestMapping(value="saveSelectBankEmailToScheduler/{parentUserId}/{txnId}/{mode}",produces="application/json",method=RequestMethod.POST)
	public ResponseEntity<?> saveSelectBankEmailToScheduler(@PathVariable("parentUserId") String parentUserId,
			@PathVariable("txnId") String txnId,@PathVariable("mode") String mode) 
	{
		logger.info("=========== saveSelectBankEmailToScheduler ===========");
		System.out.println("=========== saveSelectBankEmailToScheduler ===========");
	    GenericResponse response = new GenericResponse();
	    try 
	    {
	    	QuotationBean bean = new QuotationBean();
	    	NimaiLCMasterBean nimailcmasterbean=new NimaiLCMasterBean();
	    	System.out.println("mode: "+mode);
	    	if(mode.equalsIgnoreCase("update"))
	    	{
	    		System.out.println("Call method for Update");
	    		
	    		lcservice.getAlleligibleBAnksEmail(parentUserId, txnId, 0, "LC_UPDATE_ALERT_ToBanks", "LC_UPDATE(DATA)", bean,"selectbank",nimailcmasterbean);
	    	}
	    	else
	    	{
	    		System.out.println("Call method for save");
	    		lcservice.getAlleligibleBAnksEmail(parentUserId, txnId, 0, "LC_UPLOAD_ALERT_ToBanks", "LC_UPLOAD(DATA)", bean,"selectbank",nimailcmasterbean);
	    	}
	    	response.setStatus("Success");
		    //response.setData(otb);
		    return new ResponseEntity(response, HttpStatus.OK);
		} 
	    catch (Exception var4) 
	    {
		    response.setStatus("Failure");
		    response.setErrCode("EXE000");
		    System.out.println("Error:" + var4);
		    response.setErrMessage(ErrorDescription.getDescription("EXE000") + " " + var4);
		    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value="/getTxnAccess/{userId}", produces="application/json", method=RequestMethod.GET)
	public ResponseEntity<?> getTxnAccess(@PathVariable("userId") String userId) 
	{
		GenericResponse response = new GenericResponse();

		      try {
		    	  String access="";
		    	  try
		    	  {
		    		  System.out.println("userId: "+userId+"---");
			         access = lcmasterrepo.findAccessibility(userId);
			         System.out.println("Access: "+access);
			         if(access==null)
			         {
			        	 access="No";
			         }
		    	  }
		    	  catch(NullPointerException ne)
		    	  {
		    		  access="No";
		    	  }
		    	  response.setData(access);
			      response.setStatus("Success");
			      return new ResponseEntity(response, HttpStatus.OK);
		      } catch (Exception var4) {
		         var4.printStackTrace();
		         response.setStatus("Failure");
		         response.setErrMessage("");
		         return new ResponseEntity(response, HttpStatus.OK);
		      }
	}

}
