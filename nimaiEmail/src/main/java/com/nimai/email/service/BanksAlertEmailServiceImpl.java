package com.nimai.email.service;

import java.net.InetAddress;


import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;

import org.springframework.transaction.annotation.Propagation;
//import javax.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nimai.email.api.GenericResponse;
import com.nimai.email.bean.AlertToBanksBean;
import com.nimai.email.bean.EligibleEmailBeanResponse;
import com.nimai.email.bean.EligibleEmailList;
import com.nimai.email.bean.EmailSendingDetails;
import com.nimai.email.bean.QuotationAlertRequest;
import com.nimai.email.controller.BanksAlertEmailController;
import com.nimai.email.dao.BanksAlertDao;
import com.nimai.email.entity.NimaiClient;
import com.nimai.email.entity.NimaiEmailSchedulerAlertToBanks;
import com.nimai.email.entity.NimaiLC;
import com.nimai.email.entity.NimaiLCMaster;
import com.nimai.email.entity.NimaiMBranch;
import com.nimai.email.entity.NimaiOfflineUserDetails;
import com.nimai.email.entity.QuotationMaster;
import com.nimai.email.entity.TransactionSaving;
import com.nimai.email.repository.LCMasterRepository;
import com.nimai.email.repository.NimaiEmailBankSchedulerRepo;
import com.nimai.email.repository.NimaiMCustomerRepo;
import com.nimai.email.repository.NimaiOfflineUserDetailsRepository;
import com.nimai.email.utility.AppConstants;
import com.nimai.email.utility.EmaiInsert;
import com.nimai.email.utility.EmailErrorCode;
import com.nimai.email.utility.ErrorDescription;
import com.nimai.email.utility.ModelMapper;
import com.nimai.email.utility.ResetUserValidation;

@Service
@Transactional
public class BanksAlertEmailServiceImpl implements BanksALertEmailService {

	private static Logger logger = LoggerFactory.getLogger(BanksAlertEmailController.class);

	@Autowired
	BanksAlertDao userDao;

	@Autowired
	private EmaiInsert emailInsert;

	@Autowired
	EntityManagerFactory em;

	@Autowired
	ResetUserValidation resetUserValidator;

	@Autowired
	NimaiEmailBankSchedulerRepo nimaiBankRepo;

	@Autowired
	LCMasterRepository lcMasterRepo;

	@Autowired
	NimaiMCustomerRepo cusRepo;

	@Autowired
	NimaiOfflineUserDetailsRepository offLineRepo;


	@Override
	@Scheduled(fixedDelay = 50000)
	public void sendTransactionStatusToBanksByScheduled() {

		// TODO Auto-generated method stub
		logger.info("=====InsidesendTransactionStatusToBanksByScheduled method========= ");
		GenericResponse response = new GenericResponse<>();
		String emailStatus = "";
		/* query to fetch the list of data from nimaiEmailAlertsTobankSchedulerTablw */
		List<NimaiEmailSchedulerAlertToBanks> emailDetailsScheduled = userDao.getTransactionDetail();

		for (NimaiEmailSchedulerAlertToBanks schdulerData : emailDetailsScheduled) {

			NimaiEmailSchedulerAlertToBanks emailCount = nimaiBankRepo.getOne(schdulerData.getScedulerid());
			logger.info("=====InsidesendTransactionStatusToBanksByScheduled method=========schdulerData 1:"
					+ schdulerData.getCustomerid());
			logger.info("=====InsidesendTransactionStatusToBanksByScheduled method=========schdulerData 2:"
					+ schdulerData.getEmailEvent());

			if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.QUOTE_ACCEPT)
					|| schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.QUOTE_REJECTION)) {

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition emailCount.getEmailCount() schedulerId:==========3:"
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  QUOTE_ACCEPT & QUOTE_REJECTION emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():==========4:"
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  QUOTE_ACCEPT & QUOTE_REJECTION emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():==========5:"
									+ flagUpdate);
				} else {
					logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========6:");
					try {
						NimaiLC custTransactionDetails = userDao
								.getTransactioDetailsByTransId(schdulerData.getTransactionid());
						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========7:"
								+ custTransactionDetails.toString());
						QuotationMaster bnakQuotationDetails = userDao
								.getDetailsByQuoteId(schdulerData.getQuotationId());

						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========8:"
								+ bnakQuotationDetails.toString());
					
						NimaiClient customerDetails = cusRepo.getOne(schdulerData.getCustomerid());

						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========9:"
								+ customerDetails.toString());
					
						NimaiClient bankDetails = cusRepo.getOne(schdulerData.getBankUserid());

						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========10:"
								+ bankDetails.toString());
						if (custTransactionDetails != null && bnakQuotationDetails != null) {
							logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========11:");
							if(!schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")) {
								logger.info("============Inside schdulerData.getAdditionalUserId() QUOTE_ACCEPT & QUOTE_REJECTION condition==========12:");
								
							}
							
							if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
									&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
								
								logger.info("if additional conditin method for email sending to banks condition userId 16 A:"+schdulerData.getAdditionalUserId());
								logger.info("Additional userId 15:"+schdulerData.getAdditionalUserId());
								NimaiOfflineUserDetails additionalDetails = offLineRepo
										.findByOfflineUserId(schdulerData.getAdditionalUserId());
								
								/* method for email sending to Additionalbank users */
								emailInsert.sendQuotationStatusEmailAddBankUser(schdulerData.getEmailEvent(), schdulerData,
										schdulerData.getBanksEmailID(), custTransactionDetails, bnakQuotationDetails,
										bankDetails, customerDetails,additionalDetails);
							}else  {
								logger.info("method for email sending to banks condition userId 16:"+schdulerData.getAdditionalUserId());
								// to get details of customer Details and customerusrid from nimai_m_quotation
								// as userId
								/* method for email sending to banks */
								emailInsert.sendQuotationStatusEmail(schdulerData.getEmailEvent(), schdulerData,
										schdulerData.getBanksEmailID(), custTransactionDetails, bnakQuotationDetails,
										bankDetails, customerDetails);
							}
							try {
								logger.info("Inside QUOTE_ACCEPT & QUOTE_REJECTION condition schedulerId: 17:"
												+ schdulerData.getScedulerid());
								int counter = 0;
								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());

								if (flagUpdate.getEmailCount() == 0) {
									counter = 0;

								} else {
									counter = flagUpdate.getEmailCount();
								}

								logger.info("============Inside flagUpdate1  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========18:"
												+ flagUpdate);

								flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
								flagUpdate.setEmailCount(counter + 1);
								nimaiBankRepo.save(flagUpdate);
								logger.info(
										"============Inside flagUpdate2  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========19:"
												+ flagUpdate);
								// userDao.updateEmailFlag(schdulerData.getScedulerid());
							} catch (Exception e) {
								logger.info(
										"============Inside flagUpdate2  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========20:"
												);
								e.printStackTrace();
								emailStatus = AppConstants.EMAILSTATUS;

								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								flagUpdate.setEmailFlag(emailStatus);
								nimaiBankRepo.save(flagUpdate);

								
								continue;
							}

						} else {

							logger.info("=====Inside QUOTE_ACCEPT or QUOTE_REJECTION quotation id not found======21:");

							NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
									.getOne(schdulerData.getScedulerid());
							logger.info(
									"============Inside flagUpdate3  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========22:"
											+ flagUpdate);
							flagUpdate.setEmailFlag(emailStatus);
							nimaiBankRepo.save(flagUpdate);
							logger.info(
									"============Inside flagUpdate4  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========23:"
											+ flagUpdate);

							// emailStatus = AppConstants.Quote_Id_NOT_Register;
							try {
								userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);
							} catch (Exception e) {
								e.printStackTrace();
								logger.info(
										"=======Inside QUOTE_ACCEPT & QUOTE_REJECTION condition in updateInvalidflag method======24:");
								continue;
							}

						}

					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							response.setMessage("Email Sending failed");
							EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
							logger.info(
									"============email sending failed sendQuotationStatusEmail method of QUOTE_ACCEPT & QUOTE_REJECTION condition catch block========25:");
							continue;
						}
					}
				}

			} else if (schdulerData.getEmailEvent().equalsIgnoreCase("QUOTE_ACCEPT_CUSTOMER")
					|| schdulerData.getEmailEvent().equalsIgnoreCase("QUOTE_REJECTION_CUSTOMER")) {
				logger.info("============Inside QUOTE_ACCEPT_CUSTOMER & QUOTE_REJECTION_CUSTOMER condition==========26");

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside QUOTE_ACCEPT_CUSTOMER & QUOTE_REJECTION_CUSTOMER condition emailCount.getEmailCount() schedulerId:==========27:"
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  QUOTE_ACCEPT_CUSTOMER & QUOTE_REJECTION_CUSTOMER emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():==========28"
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  QUOTE_ACCEPT & QUOTE_REJECTION emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():==========29"
									+ flagUpdate);
				} else {

					try {
						NimaiLC custTransactionDetails = userDao
								.getTransactioDetailsByTransId(schdulerData.getTransactionid());
						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========30:"
								+ custTransactionDetails.toString());
						QuotationMaster bnakQuotationDetails = userDao
								.getDetailsByQuoteId(schdulerData.getQuotationId());

						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========31:"
								+ bnakQuotationDetails.toString());
						NimaiClient customerDetails = userDao.getCustDetailsByUserId(schdulerData.getCustomerid());
						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========32:"
								+ customerDetails.toString());
						NimaiClient bankDetails = userDao.getCustDetailsByUserId(schdulerData.getBankUserid());
						logger.info("============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition==========33:"
								+ bankDetails.toString());

						if (custTransactionDetails != null && bnakQuotationDetails != null) {

							try {
								/* method for sending the email to customer */
								logger.info("##########################method for sending the email to customer 34:"
										+ schdulerData.getQuotationId() + "########################");
								QuotationMaster bankQuotationDetails = userDao
										.getDetailsByQuoteId(schdulerData.getQuotationId());

								if (bankQuotationDetails != null) {
									
									String savingsDetails = schdulerData.getTrSavings();										logger.info("##########################if custDetails condition 38:"
												+ schdulerData.getQuotationId() + "########################");
										
										NimaiMBranch branchDetails = userDao.getBrDetailsByEmail(schdulerData.getPasscodeuserEmail());
                             if(branchDetails==null) {
	                          logger.info("##########################else custDetails condition 39:"
		                    	+ schdulerData.getQuotationId() + "########################");
	                           emailInsert.sendQuotationStatusEmailToCust(schdulerData.getEmailEvent(),
		                  	schdulerData, schdulerData.getCustomerEmail(), bankQuotationDetails,
		              	custTransactionDetails, bankDetails, savingsDetails);
	
                                  }else {
	                    emailInsert.sendQuotationStatusEmailToPassCodeCust(schdulerData.getEmailEvent(),
		          	schdulerData, schdulerData.getPasscodeuserEmail(), bankQuotationDetails,
		              	custTransactionDetails, bankDetails, savingsDetails, branchDetails);

                         }

									
									
									

									try {
										logger.info(
												"============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition schedulerId:========== 40:"
														+ schdulerData.getScedulerid());
										int counter = 0;
										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());

										if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
											counter = 0;
										} else {
											counter = flagUpdate.getEmailCount();
										}

										logger.info(
												"============Inside flagUpdate54  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========41:"
														+ flagUpdate);

										flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
										flagUpdate.setEmailCount(counter + 1);
										nimaiBankRepo.save(flagUpdate);
										logger.info(
												"============Inside flagUpdate55  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========42:"
														+ flagUpdate);

										// userDao.updateEmailFlag(schdulerData.getScedulerid());
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}
									// TODO: handle exception

								} else {
									logger.info(
											"Inside sendTransactionStatusToBanksByScheduled quotation id not found 43:");
									try {
										emailStatus = AppConstants.Quote_Id_NOT_Register;

										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());
										logger.info(
												"============Inside flagUpdate57  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():========== 44:"
														+ flagUpdate);
										flagUpdate.setEmailFlag(emailStatus);
										nimaiBankRepo.save(flagUpdate);
										logger.info(
												"============Inside flagUpdate58  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():========== 45:"
														+ flagUpdate);
										response.setMessage("Details not found");
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}

								}
							} catch (Exception e) {
								logger.info(
										"============Inside flagUpdate58  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():========== 46:"
												);
								if (e instanceof NullPointerException) {
									response.setMessage("Email Sending failed ");
									EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);

									continue;
								}
							}

							logger.info(
									"============Inside QUOTE_ACCEPT & QUOTE_REJECTION condition schdulerData.getScedulerid():==========47:"
											+ schdulerData.getScedulerid());
						} else {

							logger.info("=====Inside QUOTE_ACCEPT or QUOTE_REJECTION quotation id not found======48:");
							emailStatus = AppConstants.Quote_Id_NOT_Register;
							try {

								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								logger.info(
										"============Inside flagUpdate60  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========49:"
												+ flagUpdate);
								flagUpdate.setEmailFlag(emailStatus);
								nimaiBankRepo.save(flagUpdate);
								logger.info(
										"============Inside flagUpdate61  QUOTE_ACCEPT & QUOTE_REJECTION Customer condition schdulerData.getScedulerid():==========50:"
												+ flagUpdate);

							
							} catch (Exception e) {
								e.printStackTrace();
								logger.info(
										"=======Inside QUOTE_ACCEPT & QUOTE_REJECTION condition in updateInvalidflag method======51:");
								continue;
							}

						}
					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							response.setMessage("Email Sending failed");
							EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
							logger.info(
									"============email sending failed sendQuotationStatusEmail method of QUOTE_ACCEPT & QUOTE_REJECTION condition catch block========52:");
							continue;
						}
					}
				}

			} else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.WINQUOTEDATA)) {

				NimaiLC custTransactionDetails = userDao.getTransactioDetailsByTransId(schdulerData.getTransactionid());
				logger.info(
						"=====Inside Winning_Quote_Data transaction id not found" + schdulerData.getTransactionid());
				logger.info("===========Winning_Quote_Data trId:" + schdulerData.getTransactionid());
				if (custTransactionDetails != null) {
					List<QuotationMaster> rejectedBankDetails = userDao
							.getBankQuoteList(schdulerData.getTransactionid());
					logger.info("reljected details" + rejectedBankDetails.toString());
					NimaiClient customerDetails = userDao.getCustDetailsByUserId(schdulerData.getCustomerid());
					for (QuotationMaster details : rejectedBankDetails) {
						logger.info("particular data" + details.toString());
						NimaiEmailSchedulerAlertToBanks rejectBankDetails = new NimaiEmailSchedulerAlertToBanks();
						// NimaiEmailSchedulerAlertToBanks schdata=new
						// NimaiEmailSchedulerAlertToBanks();
						NimaiClient bankDetails = userDao.getCustDetailsByUserId(details.getBankUserId());
						rejectBankDetails.setBankUserName(bankDetails.getFirstName());
						rejectBankDetails.setTransactionid(schdulerData.getTransactionid());
						if (customerDetails.getUserid().substring(0, 2).equalsIgnoreCase("BA")) {
							rejectBankDetails.setCustomerCompanyName(customerDetails.getBankNbfcName());
						} else {
							rejectBankDetails.setCustomerCompanyName(customerDetails.getCompanyName());
						}
						rejectBankDetails.setBankUserid(details.getBankUserId());
						rejectBankDetails.setAmount(String.valueOf(custTransactionDetails.getlCValue()));
						rejectBankDetails.setCurrency(custTransactionDetails.getlCCurrency());
						rejectBankDetails.setBanksEmailID(bankDetails.getEmailAddress());
						rejectBankDetails.setAdditionalUserId(details.getBankUserId());
						rejectBankDetails.setEmailEvent("Winning_Quote_Alert_toBanks");
						rejectBankDetails.setEmailCount(0);
						rejectBankDetails.setEmailFlag(AppConstants.PENDINGFLG);
						userDao.saveBankSchData(rejectBankDetails);
					}

					logger.info(
							"============Inside Winning_Quote_Data & Winning_Quote_Data condition schdulerData.getScedulerid():=========="
									+ schdulerData.getScedulerid());
					try {
						userDao.updateEmailFlag(schdulerData.getScedulerid());
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

				}

				else {
					logger.info("Inside Winning_Quote_Data transaction id not found");
					response.setMessage("Details not found");
					emailStatus = AppConstants.TRIDNOREGISTER;
					try {
						NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
						logger.info(
								"============Inside flagUpdate2  Winning_Quote_Data & Winning_Quote_Data Customer condition schdulerData.getScedulerid():=========="
										+ flagUpdate);
						flagUpdate.setEmailFlag(emailStatus);
						nimaiBankRepo.save(flagUpdate);
						logger.info(
								"============Inside flagUpdate3  Winning_Quote_Data & Winning_Quote_Data Customer condition schdulerData.getScedulerid():=========="
										+ flagUpdate);

						// userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

				}

			}

			else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.WINQUOTETOBANK)) {

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside Winning_Quote_Alert_toBanks  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  Winning_Quote_Alert_toBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  Winning_Quote_Alert_toBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {
					try {
						NimaiClient bankDetails = userDao.getCustDetailsByUserId(schdulerData.getBankUserid());
						if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("BA")) {
						List<NimaiOfflineUserDetails> ofAdUserList = offLineRepo
								.findByOfflineUserIdSearch(schdulerData.getBankUserid());
		logger.info("Size of the offline user Winning_Quote_Alert_toBanks"+ofAdUserList.size());
						if (ofAdUserList.size() > 0) {
							for (NimaiOfflineUserDetails user : ofAdUserList) {
								logger.info("Additional userId"+user.getAdditionalUserId());
								NimaiEmailSchedulerAlertToBanks addBankDetails = new NimaiEmailSchedulerAlertToBanks();
								addBankDetails.setCustomerid(schdulerData.getCustomerid());
								addBankDetails.setBankUserid(user.getUserid().getUserid());
								addBankDetails.setAdditionalUserId(user.getAdditionalUserId());
								addBankDetails.setBankUserName(user.getFirstName());
								addBankDetails.setBanksEmailID(user.getEmailAddress());
								addBankDetails.setParentUserId(user.getUserid().getUserid());
								addBankDetails.setTransactionid(schdulerData.getTransactionid());
								addBankDetails.setEmailEvent(schdulerData.getEmailEvent());
								addBankDetails.setAmount(schdulerData.getAmount());
								addBankDetails.setCurrency(schdulerData.getCurrency());
								addBankDetails.setCustomerCompanyName(schdulerData.getCustomerCompanyName());
								addBankDetails.setBankUserName(schdulerData.getBankUserName());
								addBankDetails.setEmailCount(Integer.valueOf(0));
								addBankDetails.setEmailFlag(AppConstants.PENDINGFLG);
								nimaiBankRepo.save(addBankDetails);
							}
						}
						}
						//additionalUser Email Functionality
						if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
								&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
							NimaiOfflineUserDetails additionalDetails = offLineRepo
									.findByOfflineUserId(schdulerData.getAdditionalUserId());
							emailInsert.sendWinningQuoteToAlertAddBankUser(schdulerData.getEmailEvent(), schdulerData,
									bankDetails,additionalDetails);
						}else {

							emailInsert.sendWinningQuoteToAlertBank(schdulerData.getEmailEvent(), schdulerData,
									bankDetails);
						}
						
						

						logger.info(
								"============Inside Winning_Quote_Alert_toBanks condition schdulerData.getScedulerid():=========="
										+ schdulerData.getScedulerid());

						int counter = 0;
						NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
						logger.info(
								"============Inside flagUpdate3  Winning_Quote_Alert_toBanks  Customer condition schdulerData.getScedulerid():=========="
										+ flagUpdate);
						if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
							counter = 0;
						} else {
							counter = flagUpdate.getEmailCount();
						}

						flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
						flagUpdate.setEmailCount(counter + 1);
						nimaiBankRepo.save(flagUpdate);
						logger.info(
								"============Inside flagUpdate3  Winning_Quote_Alert_toBanks condition schdulerData.getScedulerid():=========="
										+ flagUpdate);


					} catch (Exception e) {
						e.printStackTrace();
						logger.info(
								"============Inside catch block Winning_Quote_Alert_toBanks condition schdulerData.getScedulerid():=========="
										+ schdulerData.getScedulerid());
						continue;
					}
				}

			}

			else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.LCREOPEN)) {
				logger.info("================LC_REOPENING_ALERT_ToBanks 1" + schdulerData.getCustomerid());
				if (emailCount.getEmailCount() == 3) {
				
					logger.info(
							"============Inside LC_REOPENING_ALERT_ToBanks  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  LC_REOPENING_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  LC_REOPENING_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {
					try {
						NimaiLC custTransactionDetails = userDao
								.getTransactioDetailsByTransId(schdulerData.getTransactionid());
						QuotationMaster bnakQuotationDetails = userDao
								.getDetailsByQuoteId(schdulerData.getQuotationId());
						NimaiClient customerDetails = userDao.getCustDetailsByUserId(schdulerData.getCustomerid());
						logger.info(
								"================LC_REOPENING_ALERT_ToBanks Customer 2:" + customerDetails.getUserid());
						NimaiClient bankDetails = userDao.getCustDetailsByUserId(schdulerData.getBankUserid());
						logger.info("================LC_REOPENING_ALERT_ToBanks Bank 3:" + bankDetails.getUserid());

					
						if (custTransactionDetails != null && bnakQuotationDetails != null && customerDetails != null
								&& bnakQuotationDetails.getQuotationStatus().equalsIgnoreCase(AppConstants.QUATSTATUSREJ)) {
							logger.info("================LC_REOPENING_ALERT_ToBanks Bank 4:" + bankDetails.getUserid());

							if (schdulerData.getCustomerid().equalsIgnoreCase(schdulerData.getBankUserid())) {
								emailStatus = "Sent(Secodary_Transaction)";
								userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);
							} else {
								if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("BA")) {
								List<NimaiOfflineUserDetails> ofAdUserList = offLineRepo
										.findByOfflineUserIdSearch(schdulerData.getBankUserid());
				logger.info("Size of the offline user "+ofAdUserList.size());
								if (ofAdUserList.size() > 0) {
									for (NimaiOfflineUserDetails user : ofAdUserList) {
										logger.info("Additional userId"+user.getAdditionalUserId());
										NimaiEmailSchedulerAlertToBanks addBankDetails = new NimaiEmailSchedulerAlertToBanks();
										addBankDetails.setCustomerid(schdulerData.getCustomerid());
										addBankDetails.setBankUserid(user.getUserid().getUserid());
										addBankDetails.setAdditionalUserId(user.getAdditionalUserId());
										addBankDetails.setBankUserName(user.getFirstName());
										addBankDetails.setBanksEmailID(user.getEmailAddress());
										addBankDetails.setParentUserId(user.getUserid().getUserid());
										addBankDetails.setTransactionid(custTransactionDetails.getTransactionId());
										addBankDetails.setEmailEvent(schdulerData.getEmailEvent());
										addBankDetails.setEmailCount(Integer.valueOf(0));
										addBankDetails.setEmailFlag(AppConstants.PENDINGFLG);
										nimaiBankRepo.save(addBankDetails);
									}
								}
								}
								if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
										&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
									NimaiOfflineUserDetails additionalDetails = offLineRepo
											.findByOfflineUserId(schdulerData.getAdditionalUserId());
									// to get details of customer Details and customerusrid from nimai_m_quotation
									// as userId
									/* method for email sending to Additinal bank users */
									emailInsert.sendLcReopeningToAlertAddBankUser(schdulerData.getEmailEvent(), schdulerData,
											schdulerData.getBanksEmailID(), custTransactionDetails, bnakQuotationDetails,
											bankDetails, customerDetails,additionalDetails);
								}else {

									// to get details of customer Details and customerusrid from nimai_m_quotation
									// as userId
									/* method for email sending to banks */
									emailInsert.sendLcReopeningToAlertBank(schdulerData.getEmailEvent(), schdulerData,
											schdulerData.getBanksEmailID(), custTransactionDetails, bnakQuotationDetails,
											bankDetails, customerDetails);
								}
								
								
								
							}

						} else {
							logger.info("Inside LC_REOPENING_ALERT_ToBanks quotation id not found");
							response.setMessage("Details not found");
							emailStatus = AppConstants.Quote_Id_NOT_Register;
							try {
								userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}

						}

						logger.info(
								"============Inside LC_REOPENING_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
										+ schdulerData.getScedulerid());
						try {

							logger.info(
									"============Inside LC_REOPENING_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
											+ schdulerData.getScedulerid());
							int counter = 0;
							NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
									.getOne(schdulerData.getScedulerid());
							logger.info(
									"============Inside flagUpdate3  LC_REOPENING_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
											+ flagUpdate);
							if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
								counter = 0;
							} else {
								counter = flagUpdate.getEmailCount();
							}
							flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
							flagUpdate.setEmailCount(counter + 1);
							nimaiBankRepo.save(flagUpdate);
							logger.info(
									"============Inside flagUpdate3  LC_REOPENING_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
											+ flagUpdate);

							// userDao.updateEmailFlag(schdulerData.getScedulerid());
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							e.printStackTrace();
							NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
									.getOne(schdulerData.getScedulerid());
							flagUpdate.setEmailFlag("Unsent");
							nimaiBankRepo.save(schdulerData);
							response.setMessage("Email Sending failed");
							EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
							continue;
						}
					}
				}

			}

			else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.BANKDETL)) {
				logger.info("============Inside Bank_Details_tocustomer condition==========");
				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside Bank_Details_tocustomer  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  Bank_Details_tocustomer emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  Bank_Details_tocustomer emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {
					QuotationMaster bnakQuotationDetails = userDao.getDetailsByQuoteId(schdulerData.getQuotationId());
					if (bnakQuotationDetails != null) {
						try {

							emailInsert.sendBankDetailstoCustomer(schdulerData.getEmailEvent(), schdulerData,
									schdulerData.getCustomerEmail(), bnakQuotationDetails);

							logger.info(
									"============Inside Bank_Details_tocustomer Customer condition schdulerData.getScedulerid():=========="
											+ schdulerData.getScedulerid());
							try {
								int counter = 0;
								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
									counter = 0;
								} else {
									counter = flagUpdate.getEmailCount();
								}
								logger.info(
										"============Inside flagUpdate3  Bank_Details_tocustomer condition schdulerData.getScedulerid():=========="
												+ flagUpdate);
								flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
								flagUpdate.setEmailCount(counter + 1);
								nimaiBankRepo.save(flagUpdate);
								logger.info(
										"============Inside flagUpdate3  Bank_Details_tocustomer condition schdulerData.getScedulerid():=========="
												+ flagUpdate);
								// userDao.updateEmailFlag(schdulerData.getScedulerid());

							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}

						}

						catch (Exception e) {
							if (e instanceof NullPointerException) {
								response.setMessage("Email Sending failed");
								EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
								continue;
							}

						}

					} else {
						logger.info("Inside sendTransactionStatusToBanksByScheduled quotation id not found");
						response.setMessage("Details not found");
						emailStatus = AppConstants.Quote_Id_NOT_Register;
						try {

							NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
									.getOne(schdulerData.getScedulerid());

							logger.info(
									"============Inside flagUpdate4  Bank_Details_tocustomer condition schdulerData.getScedulerid():=========="
											+ flagUpdate);
							flagUpdate.setEmailFlag(emailStatus);
							nimaiBankRepo.save(flagUpdate);
							logger.info(
									"============Inside flagUpdate5  Bank_Details_tocustomer condition schdulerData.getScedulerid():=========="
											+ flagUpdate);

							// userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

					}
				}

			}

			else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.QUOTEPLACEBANK)) {

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside QUOTE_PLACE_ALERT_ToBanks  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  QUOTE_PLACE_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  QUOTE_PLACE_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {

					logger.info("============Inside QUOTE_PLACE_ALERT_ToBanks condition 1==========");
					logger.info("@@@@@@@@@@@@@@@@##########################Quotatio Id:-"
							+ schdulerData.getQuotationId() + "@@@@@@@@@@@@@@@@@@@@########################");
					QuotationMaster bnakQuotationDetails = userDao.getDetailsByQuoteId(schdulerData.getQuotationId());
					if (bnakQuotationDetails != null) {
						logger.info("============Inside QUOTE_PLACE_ALERT_ToBanks condition 2==========");
						logger.info("@@@@@@@@@@@@@@@@##########################Quotatio Id:-"
								+ schdulerData.getQuotationId() + "@@@@@@@@@@@@@@@@@@@@########################");
						try {
							NimaiLC custTransactionDetails = userDao
									.getTransactioDetailsByTransId(schdulerData.getTransactionid());
							logger.info("substring value" + custTransactionDetails.getUserId().substring(0, 2));
							if (custTransactionDetails.getUserId().substring(0, 2).equalsIgnoreCase("BA")) {
								logger.info("============Inside QUOTE_PLACE_ALERT_ToBanks condition 3==========");
								NimaiClient offLineBankDetails = userDao
										.getCustDetailsByUserId(bnakQuotationDetails.getBankUserId());

								if (offLineBankDetails.getUserMode() == null) {
									logger.info(
											"============Inside QUOTE_PLACE_ALERT_ToBanks condition 2A UserMode=========="
													+ offLineBankDetails.getUserMode());
									offLineBankDetails.setUserMode("");
								}
								if (offLineBankDetails.getUserMode().equalsIgnoreCase("")) {
									logger.info("============Inside QUOTE_PLACE_ALERT_ToBanks condition 4==========");

									// to get details of customer Details and customerusrid from nimai_m_quotation
									// as userId
									NimaiClient customerDetails = userDao
											.getCustDetailsByUserId(bnakQuotationDetails.getUserId());

									emailInsert.sendQuotePlaceEmailToBanks(schdulerData.getEmailEvent(), schdulerData,
											schdulerData.getBanksEmailID(), bnakQuotationDetails, customerDetails,
											custTransactionDetails);

//									int scedulerid = schdulerData.getScedulerid();

									logger.info(
											"============Inside QUOTE_PLACE_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
													+ schdulerData.getScedulerid());
									try {

										int counter = 0;
										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());

										if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
											counter = 0;
										} else {
											counter = flagUpdate.getEmailCount();
										}

										logger.info(
												"============Inside flagUpdate5  QUOTE_PLACE_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
														+ flagUpdate);
										flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
										flagUpdate.setEmailCount(counter + 1);
										nimaiBankRepo.save(flagUpdate);
										logger.info(
												"============Inside flagUpdate5  QUOTE_PLACE_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
														+ flagUpdate);
										// userDao.updateEmailFlag(schdulerData.getScedulerid());
									} catch (Exception e) {
										e.printStackTrace();
										logger.info(
												"============Inside catch QUOTE_PLACE_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
														+ schdulerData.getScedulerid());
										continue;
									}
									logger.info(
											"============Inside OFFLINE QUOTE_PLACE_ALERT_ToBanks condition 1==========");
								} else if (offLineBankDetails.getUserMode().equalsIgnoreCase("OFFLINE")) {
									logger.info(
											"============Inside OFFLINE QUOTE_PLACE_ALERT_ToBanks condition 5==========");
									NimaiEmailSchedulerAlertToBanks offBankQuoteDetails = new NimaiEmailSchedulerAlertToBanks();
									offBankQuoteDetails.setCustomerid(offLineBankDetails.getUserid());
									offBankQuoteDetails.setTransactionid(custTransactionDetails.getTransactionId());
									offBankQuoteDetails.setQuotationId(bnakQuotationDetails.getQuotationId());
									offBankQuoteDetails.setActivityBy(schdulerData.getActivityBy());
									offBankQuoteDetails.setActivityEmail(schdulerData.getActivityEmail());
									offBankQuoteDetails.setActivityUserId(schdulerData.getActivityUserId());
									offBankQuoteDetails.setEmailEvent(AppConstants.OFFBAUQUOTEPLACE);
									offBankQuoteDetails.setEmailFlag(AppConstants.PENDINGFLG);
									offBankQuoteDetails.setBankUserid(bnakQuotationDetails.getBankUserId());
									offBankQuoteDetails.setAdditionalUserId(bnakQuotationDetails.getBankUserId());
									offBankQuoteDetails.setBankUserName(offLineBankDetails.getFirstName());
									offBankQuoteDetails.setBanksEmailID(bnakQuotationDetails.getEmailAddress());
									//offBankQuoteDetails.setBanksEmailID(schdulerData.getBanksEmailID());
									offBankQuoteDetails.setEmailCount(0);
									Date currentTime = new Date();
									offBankQuoteDetails.setInsertedDate(currentTime);
									nimaiBankRepo.save(offBankQuoteDetails);
									try {
										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());
										flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
										nimaiBankRepo.save(flagUpdate);
									} catch (Exception e) {
										logger.info(
												"============Inside OFFLINE QUOTE_PLACE_ALERT_ToBanks condition 2 updated flag ctach condition==========");
										e.printStackTrace();
									}

								}
							} else if (custTransactionDetails.getUserId().substring(0, 2).equalsIgnoreCase("BC")
									|| custTransactionDetails.getUserId().substring(0, 2).equalsIgnoreCase("CU")) {

								logger.info("============Inside QUOTE_PLACE_ALERT_ToBanks condition 6==========");
								logger.info(
										"============Inside QUOTE_PLACE_ALERT_ToBanks Customer else if new condition schdulerData.getScedulerid():=========="
												+ schdulerData.getScedulerid());

								NimaiClient offLineBankDetails = userDao
										.getCustDetailsByUserId(bnakQuotationDetails.getBankUserId());
								if (offLineBankDetails.getUserMode() == null) {
									logger.info(
											"============Inside QUOTE_PLACE_ALERT_ToBanks condition 2A UserMode=========="
													+ offLineBankDetails.getUserMode());
									offLineBankDetails.setUserMode("");
								}
								if (offLineBankDetails.getUserMode().equalsIgnoreCase("")) {
									logger.info("============Inside QUOTE_PLACE_ALERT_ToBanks condition 7==========");

									// to get details of customer Details and customerusrid from nimai_m_quotation
									// as userId
									NimaiClient customerDetails = userDao
											.getCustDetailsByUserId(bnakQuotationDetails.getUserId());

									emailInsert.sendQuotePlaceEmailToBanks(schdulerData.getEmailEvent(), schdulerData,
											schdulerData.getBanksEmailID(), bnakQuotationDetails, customerDetails,
											custTransactionDetails);

//									int scedulerid = schdulerData.getScedulerid();

									logger.info(
											"============Inside QUOTE_PLACE_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
													+ schdulerData.getScedulerid());
									try {
										int counter = 0;
										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());
										if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
											counter = 0;
										} else {
											counter = flagUpdate.getEmailCount();
										}
										logger.info(
												"============Inside flagUpdate6  QUOTE_PLACE_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
														+ flagUpdate);
										flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
										flagUpdate.setEmailCount(counter + 1);
										nimaiBankRepo.save(flagUpdate);
										logger.info(
												"============Inside flagUpdate7  QUOTE_PLACE_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
														+ flagUpdate);
										// userDao.updateEmailFlag(schdulerData.getScedulerid());
									} catch (Exception e) {
										e.printStackTrace();
										logger.info(
												"============Inside QUOTE_PLACE_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
														+ schdulerData.getScedulerid());
										continue;
									}
									logger.info(
											"============Inside OFFLINE QUOTE_PLACE_ALERT_ToBanks condition 1==========");
								} else if (offLineBankDetails.getUserMode().equalsIgnoreCase("OFFLINE")) {
									logger.info(
											"============Inside OFFLINE QUOTE_PLACE_ALERT_ToBanks condition 8==========");
									
									NimaiEmailSchedulerAlertToBanks offBankQuoteDetails = new NimaiEmailSchedulerAlertToBanks();
									offBankQuoteDetails.setCustomerid(offLineBankDetails.getUserid());
									offBankQuoteDetails.setTransactionid(custTransactionDetails.getTransactionId());
									offBankQuoteDetails.setQuotationId(bnakQuotationDetails.getQuotationId());
									offBankQuoteDetails.setEmailEvent(AppConstants.OFFBAUQUOTEPLACE);
									offBankQuoteDetails.setEmailFlag(AppConstants.PENDINGFLG);
									offBankQuoteDetails.setBankUserid(bnakQuotationDetails.getBankUserId());
									offBankQuoteDetails.setActivityBy(schdulerData.getActivityBy());
									offBankQuoteDetails.setActivityEmail(schdulerData.getActivityEmail());
									offBankQuoteDetails.setActivityUserId(schdulerData.getActivityUserId());
									offBankQuoteDetails.setBanksEmailID(schdulerData.getBanksEmailID());
									offBankQuoteDetails.setAdditionalUserId(bnakQuotationDetails.getBankUserId());
									offBankQuoteDetails.setBankUserName(offLineBankDetails.getFirstName());
									offBankQuoteDetails.setEmailCount(0);
									//offBankQuoteDetails.setBanksEmailID(bnakQuotationDetails.getEmailAddress());
									Date currentTime = new Date();
									offBankQuoteDetails.setInsertedDate(currentTime);
									nimaiBankRepo.save(offBankQuoteDetails);

									try {
										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());
										flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
										nimaiBankRepo.save(flagUpdate);
									} catch (Exception e) {
										logger.info(
												"============Inside OFFLINE QUOTE_PLACE_ALERT_ToBanks condition 8 updated flag ctach condition==========");
										e.printStackTrace();
									}
								}
							}

						} catch (Exception e) {
							if (e instanceof NullPointerException) {
								response.setMessage("Email Sending failed");
								EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
								response.setData(emailError);
								continue;
							}
						}

					} else {
						logger.info("Inside sendTransactionStatusToBanksByScheduled quotation id not found");
						emailStatus = AppConstants.Quote_Id_NOT_Register;
						try {
							userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

					}

				}
			} else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.OFFBAUQUOTEPLACE)) {

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside OffBAU_QUOTE_PLACE_ALERT_ToBanks  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  OffBAU_QUOTE_PLACE_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  OffBAU_QUOTE_PLACE_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {

					logger.info("============Inside OffBAU_QUOTE_PLACE_ALERT_ToBanks condition==========");
					logger.info("@@@@@@@@@@@@@@@@##########################Quotatio Id:-"
							+ schdulerData.getQuotationId() + "@@@@@@@@@@@@@@@@@@@@########################");
					QuotationMaster bnakQuotationDetails = userDao.getDetailsByQuoteId(schdulerData.getQuotationId());
					if (bnakQuotationDetails != null) {
						try {
							NimaiLC custTransactionDetails = userDao
									.getTransactioDetailsByTransId(schdulerData.getTransactionid());
							
							if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("BA")
									&& (!schdulerData.getActivityBy().equalsIgnoreCase("paUser"))) {
								NimaiOfflineUserDetails user = offLineRepo
										.findByOfflineUserId(schdulerData.getActivityUserId());
									logger.info("Additional userId"+user.getAdditionalUserId());
									NimaiEmailSchedulerAlertToBanks addBankDetails = new NimaiEmailSchedulerAlertToBanks();
									addBankDetails.setCustomerid(schdulerData.getCustomerid());
									addBankDetails.setBankUserid(user.getUserid().getUserid());
									addBankDetails.setAdditionalUserId(user.getAdditionalUserId());
									addBankDetails.setQuotationId(schdulerData.getQuotationId());
									addBankDetails.setBankUserName(user.getFirstName());
									addBankDetails.setActivityBy(schdulerData.getActivityBy());
									addBankDetails.setActivityEmail(schdulerData.getActivityEmail());
									addBankDetails.setActivityUserId(schdulerData.getActivityUserId());
									addBankDetails.setBanksEmailID(user.getEmailAddress());
									addBankDetails.setParentUserId(user.getUserid().getUserid());
									addBankDetails.setTransactionid(custTransactionDetails.getTransactionId());
									addBankDetails.setEmailEvent(schdulerData.getEmailEvent());
									addBankDetails.setEmailCount(Integer.valueOf(0));
									addBankDetails.setEmailFlag(AppConstants.PENDINGFLG);
									nimaiBankRepo.save(addBankDetails);
								
							
							}
							if((schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
									|| schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("SE"))
									&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
								// to get details of customer Details and customerusrid from nimai_m_quotation
								// as userId
								NimaiClient customerDetails = userDao
										.getCustDetailsByUserId(bnakQuotationDetails.getUserId());

								
								NimaiOfflineUserDetails additionalDetails = offLineRepo
										.findByOfflineUserId(schdulerData.getAdditionalUserId());
								
								emailInsert.sendQuotePlaceEmailToOffLineAddBanksUser(schdulerData.getEmailEvent(), schdulerData,
										schdulerData.getBanksEmailID(), bnakQuotationDetails, customerDetails,
										custTransactionDetails,additionalDetails);
							}else {
								// to get details of customer Details and customerusrid from nimai_m_quotation
								// as userId
								NimaiClient customerDetails;
								if(schdulerData.getActivityBy().equalsIgnoreCase("seUser")) {
									NimaiOfflineUserDetails user = offLineRepo
											.findByOfflineUserId(schdulerData.getActivityUserId());
									 customerDetails = userDao
											.getCustDetailsByUserId(user.getParentUserId());
								}else {
									 customerDetails = userDao
											.getCustDetailsByUserId(bnakQuotationDetails.getUserId());
								}
					

								emailInsert.sendQuotePlaceEmailToOffLineBanks(schdulerData.getEmailEvent(), schdulerData,
										schdulerData.getBanksEmailID(), bnakQuotationDetails, customerDetails,
										custTransactionDetails);
							}
							
//							int scedulerid = schdulerData.getScedulerid();

							logger.info(
									"============Inside OffBAU_QUOTE_PLACE_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
											+ schdulerData.getScedulerid());
							try {

								int counter = 0;
								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
									counter = 0;
								} else {
									counter = flagUpdate.getEmailCount();
								}
								logger.info(
										"============Inside flagUpdate6  OffBAU_QUOTE_PLACE_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
												+ flagUpdate);
								flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
								flagUpdate.setEmailCount(counter + 1);
								nimaiBankRepo.save(flagUpdate);
								logger.info(
										"============Inside flagUpdate7  OffBAU_QUOTE_PLACE_ALERT_ToBanks condition schdulerData.getScedulerid():=========="
												+ flagUpdate);

								// userDao.updateEmailFlag(schdulerData.getScedulerid());
							} catch (Exception e) {
								e.printStackTrace();
								logger.info(
										"============Inside QUOTE_PLACE_ALERT_ToBanks Customer condition schdulerData.getScedulerid():=========="
												+ schdulerData.getScedulerid());
								continue;
							}

						} catch (Exception e) {
							if (e instanceof NullPointerException) {
								response.setMessage("Email Sending failed");
								EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
								response.setData(emailError);
								continue;
							}
						}

					} else {
						logger.info("Inside sendTransactionStatusToBanksByScheduled quotation id not found");
						emailStatus = AppConstants.Quote_Id_NOT_Register;
						try {
							userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

					}
				}
			} else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.BIDALERT)) {

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside BId_ALERT_ToCustomer  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  BId_ALERT_ToCustomer emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  BId_ALERT_ToCustomer emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {
					try {
						/*
						 * method for sending the email to customer tht he received one bid after bank
						 * place a quote against any transactionId
						 */
		
						String event = AppConstants.BIDALERT;
						NimaiLC custTransactionDetails = userDao
								.getTransactioDetailsByTransId(schdulerData.getTransactionid());
						NimaiClient custDetails = userDao
								.getcuDetailsByEmail(schdulerData.getCustomerEmail());
						// if branch userEmail consist parent user email or passcode userEmail
						if (custDetails == null) {
							String passcodeUserEmail = schdulerData.getCustomerEmail();
							NimaiMBranch branchDetails = userDao.getBrDetailsByEmail(passcodeUserEmail);
							emailInsert.sendBidRecivedEmailToPassCodeCust(event, schdulerData, passcodeUserEmail,
									branchDetails);

						} else {
							emailInsert.sendBidRecivedEmailToCust(event, schdulerData, schdulerData.getCustomerEmail());
						}

				
						logger.info(
								"============Inside BId_ALERT_ToCustomer Customer condition schdulerData.getScedulerid():=========="
										+ schdulerData.getScedulerid());
						try {
							
							int counter = 0;
							NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
									.getOne(schdulerData.getScedulerid());
							if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
								counter = 0;
							} else {
								counter = flagUpdate.getEmailCount();
							}
							logger.info(
									"============Inside flagUpdate6  BId_ALERT_ToCustomer condition schdulerData.getScedulerid():=========="
											+ flagUpdate);
							flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
							flagUpdate.setEmailCount(counter + 1);
							nimaiBankRepo.save(flagUpdate);
							logger.info(
									"============Inside flagUpdate7  BId_ALERT_ToCustomer condition schdulerData.getScedulerid():=========="
											+ flagUpdate);
							logger.info(
									"============Inside flagUpdate8  BId_ALERT_ToCustomer condition schdulerData.getScedulerid():=========="
											+ schdulerData.getScedulerid());
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							e.printStackTrace();
							response.setMessage("Email Sending failed");
							EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
							continue;
						}
					}
				}

			} else if (schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.LC_UPLOAD)
					|| schdulerData.getEmailEvent().equalsIgnoreCase(AppConstants.LC_UPDATE)) {

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside LC_UPLOAD(DATA)  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  LC_UPLOAD(DATA) emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  LC_UPLOAD(DATA) emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {

					logger.info("============Inside LC_UPLOAD(DATA) & LC_UPLOAD(DATA) condition=========="
							+ schdulerData.getTransactionid());

					NimaiLC custTransactionDetails = userDao
							.getTransactioDetailsByTransId(schdulerData.getTransactionid());
					NimaiClient custDetails = userDao.getCustDetailsByUserId(schdulerData.getCustomerid());

					try {
						if (custTransactionDetails != null && custDetails != null) {

							if (schdulerData.getPasscodeuserEmail() == null
									|| schdulerData.getPasscodeuserEmail().isEmpty()) {
								emailInsert.sendLcStatusEmailData(schdulerData, custTransactionDetails, custDetails);
							} else {
								NimaiMBranch branchDetails = userDao
										.getBrDetailsByEmail(schdulerData.getPasscodeuserEmail());
								emailInsert.sendLcStatusEmailDataToPaUser(schdulerData, custTransactionDetails,
										custDetails, branchDetails);
							}
							logger.info(
									"============Inside LC_UPLOAD Customer condition schdulerData.getScedulerid():=========="
											+ schdulerData.getScedulerid());
							try {
								
								int counter = 0;
								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
									counter = 0;
								} else {
									counter = flagUpdate.getEmailCount();
								}
								logger.info(
										"============Inside flagUpdate6  LC_UPLOAD condition schdulerData.getScedulerid():=========="
												+ flagUpdate);
								flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
								flagUpdate.setEmailCount(counter + 1);
								nimaiBankRepo.save(flagUpdate);
								logger.info(
										"============Inside flagUpdate7  LC_UPLOAD condition schdulerData.getScedulerid():=========="
												+ flagUpdate);
								logger.info(
										"============Inside flagUpdate8  LC_UPLOAD condition schdulerData.getScedulerid():=========="
												+ schdulerData.getScedulerid());
							} catch (Exception e) {
								logger.info(
										"============Inside LC_UPLOAD Customer condition schdulerData.getScedulerid():=========="
												+ schdulerData.getScedulerid());
								e.printStackTrace();
								continue;
							}

						} else {
							logger.info("Inside sendTransactionStatusToBanksByScheduled transaction id not found");
							emailStatus = AppConstants.TRIDNOREGISTER;
							userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);

						}

					} catch (Exception e) {
						if (e instanceof NullPointerException) {
							e.printStackTrace();
							response.setMessage("Email Sending failed");
							EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
							continue;
						}
					}

				}

			} else if (schdulerData.getEmailEvent().equalsIgnoreCase("LC_UPLOAD_ALERT_ToBanks")
					|| schdulerData.getEmailEvent().equalsIgnoreCase("LC_UPDATE_ALERT_ToBanks")) {

				if (emailCount.getEmailCount() == 3) {
					logger.info(
							"============Inside LC_UPLOAD_ALERT_ToBanks  condition emailCount.getEmailCount() schedulerId:=========="
									+ schdulerData.getScedulerid());
					NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
					logger.info(
							"============Inside flagUpdate1  LC_UPLOAD_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
					flagUpdate.setEmailFlag("CountIncrease");
					nimaiBankRepo.save(flagUpdate);
					logger.info(
							"============Inside flagUpdate2  LC_UPLOAD_ALERT_ToBanks emailCount.getEmailCount() Customer condition schdulerData.getScedulerid():=========="
									+ flagUpdate);
				} else {

					logger.info(
							"============Inside LC_UPLOAD_ALERT_ToBanks & LC_UPDATE_ALERT_ToBankscondition=========="
									+ schdulerData.getTransactionid());

					logger.info(
							"============Inside LC_UPLOAD_ALERT_ToBanks & LC_UPDATE_ALERT_ToBankscondition=========="
									+ schdulerData.getTransactionid());
					NimaiLC custTransactionDetails = userDao
							.getTransactioDetailsByTransId(schdulerData.getTransactionid());
					NimaiClient custDetails = userDao.getCustDetailsByUserId(schdulerData.getCustomerid());

					NimaiClient bankDetails = userDao.getCustDetailsByUserId(schdulerData.getBankUserid());
					if (bankDetails.getUserMode() == null || bankDetails.getUserMode().equalsIgnoreCase("0")) {
						logger.info("============Inside QUOTE_PLACE_ALERT_ToBanks condition 2A UserMode=========="
								+ bankDetails.getUserMode());
						bankDetails.setUserMode("");
					}
					if (bankDetails.getUserMode().equalsIgnoreCase("")) {

						if (custTransactionDetails != null && custDetails != null) {
							if (schdulerData.getTransactionEmailStatusToBanks() == null) {
								try {
									emailInsert.sendTransactionStatusToBanks(schdulerData, custTransactionDetails,
											custDetails);

									logger.info(
											"============Inside LC_UPDATE_ALERT_ToBankscondition Customer condition schdulerData.getScedulerid():=========="
													+ schdulerData.getScedulerid());
									try {
										// userDao.updateEmailFlag(schdulerData.getScedulerid());
										int counter = 0;
										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());

										logger.info(
												"============Inside flagUpdate1 LC_UPDATE_ALERT_ToBankscondition Customer condition schdulerData.getScedulerid():=========="
														+ flagUpdate);
										if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
											counter = 0;
										} else {
											counter = flagUpdate.getEmailCount();
										}
										flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
										flagUpdate.setEmailCount(counter + 1);
										nimaiBankRepo.save(flagUpdate);
										logger.info(
												"============Inside flagUpdate2 LC_UPDATE_ALERT_ToBankscondition Customer condition schdulerData.getScedulerid():=========="
														+ flagUpdate);
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}

								} catch (Exception e) {
									if (e instanceof NullPointerException) {
										e.printStackTrace();
										response.setMessage("Email Sending failed");
										EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
										continue;
									}
								}
							} else {
								try {
									emailInsert.sendTransactionStatusToBanks(schdulerData, custTransactionDetails,
											custDetails);
									// this scheduler id updating emailTRStatus flag from pending to sent
//										
									try {
										userDao.updateTrStatusEmailFlag(
												Integer.parseInt(schdulerData.getTrScheduledId()));

										logger.info(
												"============Inside LC_UPDATE_ALERT_ToBankscondition Customer condition schdulerData.getScedulerid():=========="
														+ schdulerData.getScedulerid());

									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}

									try {
										// this scheduler id updating bank email status flag from pending to sent
										// userDao.updateEmailFlag(schdulerData.getScedulerid());
										int counter = 0;

										NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
												.getOne(schdulerData.getScedulerid());
										if (flagUpdate.getEmailCount() == null || flagUpdate.getEmailCount() == 0) {
											counter = 0;
										} else {
											counter = flagUpdate.getEmailCount();
										}

										flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
										flagUpdate.setEmailCount(counter + 1);
										nimaiBankRepo.save(flagUpdate);
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									}

								} catch (Exception e) {
									if (e instanceof NullPointerException) {
										response.setMessage("Email Sending failed");
										EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
										continue;
									}
								}
							}

						} else {
							logger.info("Inside sendTransactionStatusToBanksByScheduled transaction id not found");
							emailStatus = AppConstants.TRIDNOREGISTER;
							try {
								logger.info(
										"============Inside LC_UPDATE_ALERT_ToBankscondition Customer condition transaction id not found schdulerData.getScedulerid():=========="
												+ schdulerData.getScedulerid());
								// userDao.updateInvalidIdEmailFlag(schdulerData.getScedulerid(), emailStatus);

								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								flagUpdate.setEmailFlag(emailStatus);
								nimaiBankRepo.save(flagUpdate);

							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}

						}

					} else if (bankDetails.getUserMode().equalsIgnoreCase("OFFLINE")) {
						logger.info(
								"============Inside OFFLINE LC_UPLOAD_ALERT_ToBanks & LC_UPDATE_ALERT_ToBankscondition=========="
										+ schdulerData.getTransactionid());
						NimaiEmailSchedulerAlertToBanks offBankQuoteDetails = new NimaiEmailSchedulerAlertToBanks();
						offBankQuoteDetails.setCustomerid(custDetails.getUserid());
						
						if(schdulerData.getTrScheduledId()==null || schdulerData.getTrScheduledId().equalsIgnoreCase("No_ADD_USER") ||
								schdulerData.getTransactionid().equalsIgnoreCase("NULL")) {
							offBankQuoteDetails.setAdditionalUserId(bankDetails.getUserid());
						}else {
							offBankQuoteDetails.setAdditionalUserId(schdulerData.getTrScheduledId());
						}
						
						offBankQuoteDetails.setBankUserid(bankDetails.getUserid());
					//	offBankQuoteDetails.setBanksEmailID(bankDetails.getEmailAddress());
						offBankQuoteDetails.setBanksEmailID(schdulerData.getBanksEmailID());
						offBankQuoteDetails.setTransactionid(schdulerData.getTransactionid());
						offBankQuoteDetails.setBankUserName(bankDetails.getFirstName());
						if (schdulerData.getEmailEvent().equalsIgnoreCase("LC_UPDATE_ALERT_ToBanks")) {
							offBankQuoteDetails.setEmailEvent("OffBAU_LC_UPDATE_TOBANKS_Details");
						} else {
							offBankQuoteDetails.setEmailEvent("OffBAU_LC_UPLOAD_TOBANKS");
						}

						offBankQuoteDetails.setEmailFlag(AppConstants.PENDINGFLG);
						offBankQuoteDetails.setEmailCount(0);
						Date currentTime = new Date();
						offBankQuoteDetails.setInsertedDate(currentTime);
						nimaiBankRepo.save(offBankQuoteDetails);

						try {
							// this scheduler id updating bank email status flag from pending to sent
							// userDao.updateEmailFlag(schdulerData.getScedulerid());
							NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
									.getOne(schdulerData.getScedulerid());
							flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
							nimaiBankRepo.save(flagUpdate);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
					}
					InetAddress ip;
					try {

						ip = InetAddress.getLocalHost();
						logger.info(
								"=============================Current IP address========== : " + ip.getHostAddress());

					} catch (UnknownHostException e) {

						e.printStackTrace();
						logger.info("=============================Current IP address12========== : ");

						continue;
					}
				}
			} else if (schdulerData.getEmailEvent().equalsIgnoreCase("OffBAU_LC_UPLOAD_TOBANKS")
					|| schdulerData.getEmailEvent().equalsIgnoreCase("OffBAU_LC_UPDATE_TOBANKS_Details")) {

				logger.info(
						"============Inside OffBAU_LC_UPLOAD_TOBANKS & OffBAU_LC_UPDATE_ALERT_ToBankscondition=========="
								+ schdulerData.getTransactionid());

				NimaiLC custTransactionDetails = userDao.getTransactioDetailsByTransId(schdulerData.getTransactionid());

				logger.info(
						"============+++++++++Inside OffBAU_LC_UPLOAD_TOBANKS & OffBAU_LC_UPDATE_ALERT_ToBankscondition==========+++++++"
								+ schdulerData.getCustomerid() + schdulerData.getTransactionid());
				NimaiClient custDetails = userDao.getCustDetailsByUserId(schdulerData.getCustomerid());

				if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("BA")) {
					List<NimaiOfflineUserDetails> ofAdUserList = offLineRepo
							.findByOfflineUserIdSearch(schdulerData.getBankUserid());
	logger.info("Size of the offline user "+ofAdUserList.size());
					if (ofAdUserList.size() > 0) {
						for (NimaiOfflineUserDetails user : ofAdUserList) {
							logger.info("Additional userId"+user.getAdditionalUserId());
							NimaiEmailSchedulerAlertToBanks addBankDetails = new NimaiEmailSchedulerAlertToBanks();
							addBankDetails.setCustomerid(schdulerData.getCustomerid());
							addBankDetails.setBankUserid(user.getUserid().getUserid());
							addBankDetails.setAdditionalUserId(user.getAdditionalUserId());
							addBankDetails.setBankUserName(user.getFirstName());
							addBankDetails.setBanksEmailID(user.getEmailAddress());
							addBankDetails.setParentUserId(user.getUserid().getUserid());
							addBankDetails.setTransactionid(custTransactionDetails.getTransactionId());
							addBankDetails.setEmailEvent(schdulerData.getEmailEvent());
							addBankDetails.setEmailCount(Integer.valueOf(0));
							addBankDetails.setEmailFlag(AppConstants.PENDINGFLG);
							nimaiBankRepo.save(addBankDetails);
						}
					}
				}


				if (custTransactionDetails != null && custDetails != null) {
					List<NimaiLCMaster> masterList = new ArrayList<>();
					List<NimaiLCMaster> masList2 = new ArrayList<>();
					List priNewRequest = null;
					List secNewRequest = lcMasterRepo.findSecondaryTxnForBank(schdulerData.getBankUserid());

					List<NimaiLCMaster> masList = new ArrayList<>();
					for (Object txnDet : secNewRequest) {
						logger.info("Inside for loop 1");
						NimaiLCMaster ncb = new NimaiLCMaster();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

						ncb.setTransactionId(((Object[]) txnDet)[0] == null ? null : ((Object[]) txnDet)[0].toString());
						ncb.setlCCurrency(((Object[]) txnDet)[1] == null ? null : ((Object[]) txnDet)[1].toString());
						ncb.setlCIssuanceBank(
								((Object[]) txnDet)[2] == null ? null : ((Object[]) txnDet)[2].toString());
						Double lcValue = Double
								.valueOf((((Object[]) txnDet)[3] == null ? null : ((Object[]) txnDet)[3].toString()));
						ncb.setlCValue(lcValue);

						masList.add(ncb);
					}

					try {
						priNewRequest = lcMasterRepo.findPrimaryTxnForBank(schdulerData.getBankUserid());

						for (Object txnDet : priNewRequest) {
							logger.info("INide for loop 2");
							NimaiLCMaster ncb2 = new NimaiLCMaster();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							ncb2.setTransactionId(
									((Object[]) txnDet)[0] == null ? null : ((Object[]) txnDet)[0].toString());
							ncb2.setlCCurrency(
									((Object[]) txnDet)[1] == null ? null : ((Object[]) txnDet)[1].toString());
							ncb2.setlCIssuanceBank(
									((Object[]) txnDet)[2] == null ? null : ((Object[]) txnDet)[2].toString());
							Double lcValue = Double.valueOf(
									(((Object[]) txnDet)[3] == null ? null : ((Object[]) txnDet)[3].toString()));
							ncb2.setlCValue(lcValue);

							masList.add(ncb2);
						}

					} catch (Exception e) {
						e.printStackTrace();
						logger.info(" flagUpdate9");
					}

					List<NimaiLCMaster> combinedList = null;
					try {
						combinedList = Stream.of(masList, masList2).flatMap(x -> x.stream())
								.collect(Collectors.toList());
					} catch (Exception e) {
						logger.info("flagUpdate8");
						e.printStackTrace();
						continue;
					}

					if (schdulerData.getTransactionEmailStatusToBanks() == null) {
						try {

							if (combinedList == null || combinedList.size() == 0) {
								NimaiOfflineUserDetails additionalDetails = null;
								if((schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
										|| schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("SE"))
										&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
									additionalDetails = offLineRepo
											.findByOfflineUserId(schdulerData.getAdditionalUserId());
								}
								emailInsert.sendTransactionStatusToOffLineBanks(schdulerData, custTransactionDetails,
										custDetails, additionalDetails);
							} else {
								NimaiOfflineUserDetails additionalDetails = null;
								if((schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
										|| schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("SE"))
										&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
									additionalDetails = offLineRepo
											.findByOfflineUserId(schdulerData.getAdditionalUserId());
								}
								emailInsert.sendTransactionStatusToOffLineBanksList(schdulerData,
										custTransactionDetails, custDetails, combinedList, additionalDetails);
							}

							logger.info(
									"============Inside OffBAU_LC_UPDATE_ALERT_ToBankscondition Customer condition schdulerData.getScedulerid():=========="
											+ schdulerData.getScedulerid());
							try {

								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
								nimaiBankRepo.save(flagUpdate);

							
							} catch (Exception e) {
								logger.info(" flagUpdate7");
								e.printStackTrace();
								continue;
							}

						} catch (Exception e) {
							if (e instanceof NullPointerException) {
								logger.info(" flagUpdate6");
								e.printStackTrace();
								response.setMessage("Email Sending failed");
								EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
								continue;
							}
						}
					} else {
						try {
							if (combinedList == null || combinedList.size() == 0) {

								NimaiOfflineUserDetails additionalDetails = null;
								if (schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
										&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
									additionalDetails = offLineRepo
											.findByOfflineUserId(schdulerData.getAdditionalUserId());
								}
								emailInsert.sendTransactionStatusToOffLineBanks(schdulerData, custTransactionDetails,
										custDetails, additionalDetails);
							} else {
								NimaiOfflineUserDetails additionalDetails = null;
								if(schdulerData.getAdditionalUserId().substring(0, 2).equalsIgnoreCase("AD")
										&& schdulerData.getEmailFlag().equalsIgnoreCase(AppConstants.PENDINGFLG)) {
									additionalDetails = offLineRepo
											.findByOfflineUserId(schdulerData.getAdditionalUserId());
								}
								emailInsert.sendTransactionStatusToOffLineBanksList(schdulerData,
										custTransactionDetails, custDetails, combinedList, additionalDetails);
							}

							try {

								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
								nimaiBankRepo.save(flagUpdate);

								
								logger.info(
										"============Inside LC_UPDATE_ALERT_ToBankscondition Customer condition schdulerData.getScedulerid():=========="
												+ schdulerData.getScedulerid());
							} catch (Exception e) {
								logger.info("flagUpdate5");
								e.printStackTrace();
								continue;
							}

							try {
								NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo
										.getOne(schdulerData.getScedulerid());
								flagUpdate.setEmailFlag(AppConstants.SENTFLAG);
								nimaiBankRepo.save(flagUpdate);
								logger.info("flagUpdate1");
							
							} catch (Exception e) {
								logger.info(" flagUpdate2");
								e.printStackTrace();
								continue;
							}

						} catch (Exception e) {
							if (e instanceof NullPointerException) {
								logger.info(" flagUpdate3");
								response.setMessage("Email Sending failed");
								EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
								continue;
							}
						}
					}

				} else {
					logger.info("Inside sendTransactionStatusToBanksByScheduled transaction id not found");
					emailStatus = AppConstants.TRIDNOREGISTER;
					try {
						logger.info(
								"============Inside LC_UPDATE_ALERT_ToBankscondition Customer condition transaction id not found schdulerData.getScedulerid():=========="
										+ schdulerData.getScedulerid());
						NimaiEmailSchedulerAlertToBanks flagUpdate = nimaiBankRepo.getOne(schdulerData.getScedulerid());
						flagUpdate.setEmailFlag(emailStatus);
						nimaiBankRepo.save(flagUpdate);
					} catch (Exception e) {
						logger.info(" flagUpdate4");
						e.printStackTrace();
						continue;
					}

				}
				InetAddress ip;
				try {

					ip = InetAddress.getLocalHost();
					logger.info(
							"=============================Current IP address========== : " + ip.getHostAddress());
					

				} catch (UnknownHostException e) {
					logger.info("UnknownHostException");
					e.printStackTrace();
					continue;
				}

			}

		}
		// }

	}

	@Override
	public ResponseEntity<?> sendTransactionStatusToBanks(AlertToBanksBean alertBanksBean) {
		logger.info("=======sendTransactionStatusToBanks method invoked=======");
		GenericResponse response = new GenericResponse<>();
		List<EmailSendingDetails> emailList = alertBanksBean.getBankEmails();
		for (EmailSendingDetails emailIds : emailList) {
			String errorString = this.resetUserValidator.banksEmailValidation(alertBanksBean, emailIds.getEmailId());
			if (errorString.equalsIgnoreCase("Success")) {
				try {
					emailInsert.sendTransactionStatusToBanks(alertBanksBean.getEvent(), alertBanksBean,
							emailIds.getEmailId());
				} catch (Exception e) {
					if (e instanceof NullPointerException) {
						response.setMessage("Email Sending failed");
						EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
						response.setData(emailError);
						return new ResponseEntity<Object>(response, HttpStatus.CONFLICT);
					}
				}
			} else {
				response.setErrCode("EX000");
				response.setMessage(ErrorDescription.getDescription("EX000") + errorString.toString());
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		}
		response.setMessage(ErrorDescription.getDescription("ASA002"));
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> sendQuotationStatusToBanks(QuotationAlertRequest quotationReq) {
		logger.info("==========sendQuotationStatusToBanks method invoked=========");
		GenericResponse response = new GenericResponse<>();
		String errorString = this.resetUserValidator.quotationAlertValidation(quotationReq);
		if (errorString.equalsIgnoreCase("Success")) {

			try {
				emailInsert.sendQuotationStatusEmail(quotationReq.getEvent(), quotationReq,
						quotationReq.getBankEmailId());
				response.setMessage(ErrorDescription.getDescription("ASA002"));
				return new ResponseEntity<Object>(response, HttpStatus.OK);
			} catch (Exception e) {
				if (e instanceof NullPointerException) {
					response.setMessage("Email Sending failed");
					EmailErrorCode emailError = new EmailErrorCode("EmailNull", 409);
					response.setData(emailError);
					return new ResponseEntity<Object>(response, HttpStatus.CONFLICT);
				}
			}

		} else {
			response.setErrCode("EX000");
			response.setMessage(ErrorDescription.getDescription("EX000") + errorString.toString());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

}
