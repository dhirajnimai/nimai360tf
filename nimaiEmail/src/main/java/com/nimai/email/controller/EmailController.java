
package com.nimai.email.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nimai.email.api.GenericResponse;
import com.nimai.email.bean.AdminBean;
import com.nimai.email.bean.BankDetailsBean;
import com.nimai.email.bean.BranchUserPassCodeBean;
import com.nimai.email.bean.BranchUserRequest;
import com.nimai.email.bean.InvoiceBeanResponse;
import com.nimai.email.bean.KycEmailRequest;
import com.nimai.email.bean.LcUploadBean;
import com.nimai.email.bean.ResetPassBean;
import com.nimai.email.bean.SubsidiaryBean;
import com.nimai.email.bean.UserRegistrationBean;
import com.nimai.email.dao.UserServiceDao;
import com.nimai.email.entity.NimaiClient;
import com.nimai.email.entity.NimaiEncryptedDetails;
import com.nimai.email.entity.NimaiMBranch;
import com.nimai.email.entity.NimaiMOldCustDetails;
import com.nimai.email.entity.NimaiSubscriptionDetails;
import com.nimai.email.entity.NimaiToken;
import com.nimai.email.repository.NimaiMCustomerRepo;
import com.nimai.email.repository.NimaiMOldCustRepository;
import com.nimai.email.repository.NimaiTokenRepository;
import com.nimai.email.repository.nimaiMEncryptionRepository;
import com.nimai.email.service.CaptchService;
import com.nimai.email.service.UserService;
import com.nimai.email.utility.AESUtil;
import com.nimai.email.utility.AppConstants;
import com.nimai.email.utility.EmaiInsert;
import com.nimai.email.utility.ErrorDescription;
import com.nimai.email.utility.Utils;

/**
 * @author Dhiraj
 *
 */
/**
 * @param branchUserLink
 * @return
 */
@RestController
public class EmailController {

	private static Logger logger = LoggerFactory.getLogger(EmailController.class);
	@Autowired
	EmaiInsert emailInsert;

	@Autowired
	UserServiceDao userDao;

	@Autowired
	UserService userEmailService;

	@Autowired
	CaptchService captchaService;

	@Autowired
	NimaiTokenRepository tokenRepo;

	@Autowired
	nimaiMEncryptionRepository tokenEnRepo;

	@Autowired
	private Utils utility;

	@Autowired
	NimaiMCustomerRepo custRepo;
	
	@Autowired
	NimaiMOldCustRepository oldCuRepo;


	@Autowired
	GenericResponse response;

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/sendSetPasswordLink")
	public ResponseEntity<?> sendResetPasswordLink(@RequestBody UserRegistrationBean registerLink) throws Exception {
		logger.info(
				" ================ Send resetPassword Link API is  Invoked ================" + registerLink.toString());
		GenericResponse<Object> response = new GenericResponse<Object>();
		if (registerLink.getEvent().equalsIgnoreCase(AppConstants.FORGOT_PASS_EVENT)) {
			boolean captchaVerified = captchaService.forgotVerify(registerLink.getRecaptchaResponse());
			System.out.println("Peersonal Details recaptchResponse" + registerLink.getRecaptchaResponse());
			if (!captchaVerified) {
				System.out.println(
						"INSIDE ELSE CONDITION OF sendResetPasswordLink" + registerLink.getRecaptchaResponse());
				System.out.println("INSIDE ELSE CONDITION OF sendResetPasswordLink" + captchaVerified);
				response.setMessage("Invalid Captcha");
				return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
			}
		}

		return userEmailService.sendEmail(registerLink);
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/sendAccDetails")
	public ResponseEntity<?> sendAccDetails(@RequestBody UserRegistrationBean userBean) throws Exception {
		 logger.info( " ================ Send resetPassword Link API is Invoked ================" +
				 userBean.toString());
		return userEmailService.sendDmmyAccEmail(userBean);
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping("/validatePasswordLink/{token}")
	public ResponseEntity<?> getEmployeeById(@PathVariable(value = "token") String token) {
		logger.info(" ================ Send getEmployeeById API is  Invoked ================:" + token);
		return userEmailService.validateResetPasswordLink(token);
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("/passwordChangeSuccess")
	public ResponseEntity<?> getEmployeeById(@RequestBody ResetPassBean resetBean) {
		logger.info(" ================ Send getEmployeeById API is  Invoked ================:" + resetBean.getUserId());
		return userEmailService.restSuccessEmail(resetBean);
	}

	/*
	 * email sending api for refer and subsidiary user In refer angular end should
	 * send referenceId
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/sendSubsidiaryAcivationLink")
	public ResponseEntity<?> sendSubsidiaryActivarionLink(@RequestBody SubsidiaryBean registerLink) throws Exception {
		logger.info(" ================ Send sendSubsidiaryActivarionLink API is  Invoked ================:"
				+ registerLink.toString());
		GenericResponse response = new GenericResponse();
		if ((registerLink.getEvent().equalsIgnoreCase("ADD_SUBSIDIARY"))) {
			return userEmailService.sendSubsidiaryEmail(registerLink);
		} else if (registerLink.getEvent().equalsIgnoreCase("ADD_REFER")) {
			return userEmailService.sendReferEmail(registerLink);
		} else {
			response.setMessage("Invalid Event");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/reSendSubsidiaryAcivationLink")
	public ResponseEntity<?> reSendSubActivarionLink(@RequestBody SubsidiaryBean registerLink) throws Exception {
		logger.info(" ================ Send sendSubsidiaryActivarionLink API is  Invoked ================:"
				+ registerLink.toString());
		GenericResponse response = new GenericResponse();
		if ((registerLink.getEvent().equalsIgnoreCase("ADD_SUBSIDIARY"))) {
			return userEmailService.sendSubsidiaryEmail(registerLink);
		} else if (registerLink.getEvent().equalsIgnoreCase("ADD_REFER")) {
			return userEmailService.reSendReferEmail(registerLink);
		} else {
			response.setMessage("Invalid Event");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	/*
	 * Validating api for subsidiary, refer
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping("/validateAccLink/{token}")
	public ResponseEntity<?> getSubsidiaryDetails(@PathVariable(value = "token") String token) {
		logger.info(" ================ Send getSubsidiaryDetails API is  Invoked ================:" + token);
		return userEmailService.validateSubsidiaryLink(token);
	}

	/*
	 * Validating branch user passcodeVlue
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping("/validatePasscode")
	public ResponseEntity<?> checBranchUserkPassCode(@RequestBody BranchUserPassCodeBean passCodeBean,
			HttpServletRequest request) {
		GenericResponse<Object> response = new GenericResponse<>();
		String flag = "";
		String token = request.getHeader("Authorization");
		String userid = passCodeBean.getUserId();
		String userID = "";
		String user = "";
		if (passCodeBean.getUserId().length() > 10) {

			AESUtil util = new AESUtil();
			passCodeBean.setUserMode("Offline");
			user = util.decrypt(passCodeBean.getUserId());
				//NimaiClient client = userDao.getClientDetailsbyUserId(user);
//			if (client == null ) {
//				response.setMessage("Unauthorized Access.");
//				return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
//			} 
//			
		//	else {
				try {
					NimaiEncryptedDetails exsisTingToken = userDao.getEncryDetails(user);

					if (exsisTingToken == null) {
						NimaiEncryptedDetails userToken = new NimaiEncryptedDetails();
						userToken.setUserId(user);
						userToken.setToken(passCodeBean.getUserId());
						userToken.setTokenLength(passCodeBean.getUserId().length());
						Date dnow = new Date();
						userToken.setInsertedDate(dnow);
						tokenEnRepo.save(userToken);
					} else {
						exsisTingToken.setUserId(user);
						exsisTingToken.setToken(passCodeBean.getUserId());
						exsisTingToken.setTokenLength(passCodeBean.getUserId().length());
						Date dnow = new Date();
						exsisTingToken.setInsertedDate(dnow);
						tokenEnRepo.save(exsisTingToken);
					}
				} catch (Exception e) {

					response.setMessage("Unauthorized Access.");
					return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
				}

			//}
			NimaiEncryptedDetails details = userDao.getEncryDetails(user);
			if (details.getTokenLength() != passCodeBean.getTkenLength()) {
				response.setMessage("Unauthorized Access.");
				return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
			}

			logger.info("=========checBranchUserkPassCode decrypted ID===========" + user);
		} else {
			user = passCodeBean.getUserId();
			logger.info("========checBranchUserkPassCode user ID===========" + user);
		}
		// logger.info("validatePasscode Authorization: " + token.substring(7));

		logger.info("================ checBranchUserkPassCode API Invoked ================:" + passCodeBean.toString());
		// boolean captchaVerified =
		// captchaService.verify(passCodeBean.getRecaptchaResponse());
		System.out.println("Peersonal Details recaptchResponse" + passCodeBean.getRecaptchaResponse());
		try {
			if (userid.toString().substring(0, 2).equalsIgnoreCase("CU")
					|| userid.toString().substring(0, 2).equalsIgnoreCase("BC")
					|| userid.toString().substring(0, 2).equalsIgnoreCase("RE")) {
				if (passCodeBean.getEmailid() == null || passCodeBean.getEmailid().isEmpty()) {
					NimaiClient client = userDao.getClientDetailsbyUserId(passCodeBean.getUserId());
					userID = userid + "-" + client.getEmailAddress();
				} else {
					userID = userid + "-" + passCodeBean.getEmailid();
				}

			} else {
				logger.info("=========checBranchUserkPassCode inside Else condition===========" + user);
				userID = user;
				passCodeBean.setUserId(userID);
			}
			NimaiToken tokenDetails;
			if (passCodeBean.getUserMode() == null) {
				logger.info("validatePasscode Authorization: " + token.substring(7));
				tokenDetails = tokenRepo.isTokenExists(userID, token.substring(7));
				if (tokenDetails == null) {
					response.setMessage("Unauthorized Access.");
					return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
				}
//				if (!captchaVerified) {
//
//					logger.info("INSIDE ELSE CONDITION OF CAPTCH IN authenticateUser CONTROLLER"
//							+ passCodeBean.getRecaptchaResponse());
//					logger.info("INSIDE ELSE CONDITION OF CAPTCH IN authenticateUser CONTROLLER" + captchaVerified);
//					response.setMessage("Invalid captcha");
//					try {
//						flag = "true";
//						tokenDetails.setIsInvalidCaptcha(flag);
//						tokenRepo.save(tokenDetails);
//						System.out.println("Inside true comdition for invalidCaptchaFlag" + flag);
//					} catch (Exception e) {
//						e.printStackTrace();
//						response.setErrCode("EX001");
//						response.setMessage("Error while updating invalid captcha details");
//						return new ResponseEntity<>(response, HttpStatus.OK);
//					}
//
//					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//				}

				return userEmailService.validatePassCodeValue(passCodeBean);

			} else {
				response.setMessage("Authorized Access.");
				return userEmailService.validatePassCodeValue(passCodeBean);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setMessage("OOPS! Something went wrong");
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/validateInvalidCaptcha/{userId}")
	public ResponseEntity<?> validateService(@PathVariable(value = "userId") String userId) {
		logger.info(" ================ validateInvalidCaptcha API Invoked ================:" + userId);
		GenericResponse response = new GenericResponse();
		String user = " ";
		if (userId.length() > 10) {
			AESUtil aes = new AESUtil();

			user = aes.decrypt(userId);
			logger.info("=========validateService decrypted ID===========" + user);
		} else {
			logger.info("=========validateService else condition===========" + user);
			user = userId;
		}
		return userEmailService.validateCaptcha(user);

	}

	/*
	 * branch user email sending api this api when customer will sign in as customer
	 * or bank as customer
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/sendBranchUserLink")
	public ResponseEntity<?> sendBranchUserLink(@RequestBody BranchUserRequest branchUserLink) {
		logger.info(" ================ sendBranchUserLink API Invoked ================:" + branchUserLink.toString());

		if (!branchUserLink.getEvent().equalsIgnoreCase("ADD_BRANCH_USER")) {
			response.setMessage("Invalid Event");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		NimaiClient clientUseId = userDao.getClientDetailsbyUserId(branchUserLink.getEmailId());
		if (clientUseId == null) {
			NimaiClient client = userDao.getClientDetailsbyUserId(branchUserLink.getUserId());
			if (client == null) {
				response.setErrCode("ASA001");
				response.setMessage(ErrorDescription.getDescription("ASA001"));
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} else {
				String clientDomainName = utility.getEmailDomain(client.getEmailAddress());
				String passCodeDomainName = utility.getEmailDomain(branchUserLink.getEmailId());
				if (clientDomainName.equalsIgnoreCase(passCodeDomainName)) {
					return userEmailService.sendbranchUserLink(branchUserLink);
				} else {
					response.setErrCode("ASA006");
					response.setMessage(ErrorDescription.getDescription("ASA006"));
					return new ResponseEntity<Object>(response, HttpStatus.OK);
				}
			}

		} else if (clientUseId.getAccountType().equalsIgnoreCase("SUBSIDIARY")
				|| clientUseId.getAccountType().equalsIgnoreCase("MASTER")) {
			return userEmailService.sendbranchUserLink(branchUserLink);
		} else {
			response.setErrCode("ASA019");
			response.setMessage(ErrorDescription.getDescription("ASA019"));
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}

	}

	/* branch user email sending api */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/sendBranchUserPasscode")
	public ResponseEntity<?> sendBranchUserPasscode(@RequestBody BranchUserRequest branchUserLink) {
		logger.info(
				" ================ sendBranchUserPasscode API Invoked ================:" + branchUserLink.toString());
		String user = "";
		GenericResponse response = new GenericResponse();
		if (branchUserLink.getUserId().length() > 10) {
			AESUtil util = new AESUtil();

			user = util.decrypt(branchUserLink.getUserId());
   branchUserLink.setEmailId(util.decrypt(branchUserLink.getEmailId()));
   
   

	int userEmailflag=findByEmail(branchUserLink.getEmailId());
	if(userEmailflag==1) {
		response.setErrCode("ASA021");
		//response.setTcFlag(flag);
		response.setEncryptionLength(branchUserLink.getEncryptionLength());
		response.setMessage(ErrorDescription.getDescription("ASA021"));
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
   
			NimaiEncryptedDetails details = userDao.getEncryDetails(user);

			if (details == null) {
				branchUserLink.setEncryptionLength(01);
			} else if (user.equalsIgnoreCase("BA59554")) {
				response.setUserId(user);
				response.setMessage("POC_USER");
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {

				if (details.getTokenLength() != branchUserLink.getUserId().length()) {
					response.setMessage("Unauthorise User");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

				branchUserLink.setEncryptionLength(details.getTokenLength());
			}

			logger.info("=========sendBranchUserPasscode decrypted ID===========" + user);
		} else {
			user = branchUserLink.getUserId();
			branchUserLink.setEncryptionLength(01);
			logger.info("========sendBranchUserPasscode user ID===========" + user);
		}

		if (!branchUserLink.getEvent().equalsIgnoreCase("ADD_BRANCH_USER")) {
			response.setMessage("Invalid Event");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} else if (!user.substring(0, 2).equalsIgnoreCase("BA") && !user.substring(0, 2).equalsIgnoreCase("BC")
				&& !user.substring(0, 2).equalsIgnoreCase("CU") && !user.substring(0, 2).equalsIgnoreCase("RE")
				&& !user.substring(0, 2).equalsIgnoreCase("AD") 	&& !user.substring(0, 2).equalsIgnoreCase("SE")) {
			response.setMessage("Invalid User");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

		}
if(user.substring(0, 2).equalsIgnoreCase("AD")) {
	branchUserLink.setUserType("ADDUSER");
}else {
	branchUserLink.setUserType("NRMLUSER");
}
		branchUserLink.setUserId(user);
	

		return userEmailService.sendbranchUserPasscode(branchUserLink);

	}

	
	
	private int  findByEmail(String emailId){
		int flag=0;
		try {
			NimaiMOldCustDetails cusDetails= oldCuRepo.finByEmailId(emailId);
			if(cusDetails==null) {
				return flag;
			}else {
				flag=1;
				return flag;
			}
		}catch(Exception e){
			flag=0;
			return flag;	
		}
		
		}
	
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/sendAdminSetPasswordLink")
	public ResponseEntity<?> sendAdminResetPasswordLink(@RequestBody AdminBean adminBean) throws Exception {
		logger.info(
				" ================ Send resetPaasword Link API is  Invoked ================" + adminBean.toString());
		return userEmailService.sendAdminEmail(adminBean);
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping("/sendBankDetails")
	public ResponseEntity<?> sendBAnkDetails(@RequestBody BankDetailsBean BdBean) throws Exception {
		logger.info(" ================ Send resetPaasword Link API is  Invoked ================" + BdBean.toString());
		return userEmailService.sendBankDetails(BdBean);
	}

	@CrossOrigin(value = { "*" }, allowedHeaders = { "*" })
	@RequestMapping(value = { "/downloadInvoice/{userId}/{invoiceId}" }, produces = { "application/json" }, method = {
			RequestMethod.POST })
	public ResponseEntity<?> downloadExcelReportForTxnByUserId(@PathVariable("userId") String userId,
			@PathVariable("invoiceId") String invoiceId) throws ParseException, IOException {
		GenericResponse respons = new GenericResponse();
		System.out.println("userId" + userId);
		System.out.println("invoiceId" + invoiceId);
		try {
			int subDetails = this.userEmailService.chkForInvoiceId(userId);
			List<String> userIdList = this.userEmailService.checkForGroupId(userId,invoiceId,subDetails);
			System.out.println("===========Flag"+  subDetails);
			if (subDetails == 0) {
				InvoiceBeanResponse response = this.userEmailService.getVasplanInvoiceString(userId, invoiceId);
				if (!response.getVasStatus().equalsIgnoreCase("Approved")) {
					System.out.println("Inside the if condtionns");
					System.out.println("Inside the if condtionns" + response.toString());
					System.out.println("Inside the if condtionns" + response.getSplanSerialNumber());
					NimaiSubscriptionDetails subDetail = this.userEmailService
							.getSubDetails(response.getSplanSerialNumber());
					response = this.userEmailService.getSplanInvoiceString(subDetail.getUserid().getUserid(),
							subDetail.getInvoiceId());
					respons.setData(response);
					respons.setMessage("Success");
				} else {
					System.out.println("Inside the else condtionns");
					respons.setData(response);
					respons.setMessage("Success");
				}
			}else if(subDetails==3) {
				System.out.println("===========Flag2"+  subDetails);
				InvoiceBeanResponse response = this.userEmailService.getSplanPosInvoiceString(userIdList, invoiceId);
				respons.setData(response);
				respons.setMessage("Success");
			} else {
				InvoiceBeanResponse response = this.userEmailService.getSplanInvoiceString(userId, invoiceId);
				respons.setData(response);
				respons.setMessage("Success");
			}
		} catch (Exception e) {
			logger.info("Excption Inside downloadInvoice checking invoiceId controller method");
			e.printStackTrace();
		}
		return new ResponseEntity(respons, HttpStatus.OK);
	}
}
