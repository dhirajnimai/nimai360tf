package com.nimai.email.utility;

public interface AppConstants {
	String FORGOT_PASS_EVENT = "FORGOT_PASSWORD";
	String ACCOUNT_ACTIVATE_EVENT = "ACCOUNT_ACTIVATE";
	String CUST_SPLAN_EVENT = "Cust_Splan_email";
	String CUST_SPLAN_EVENT_Wire="Cust_Splan_email_Wire";
	String CUST_POST_SPLAN_EVENT_Wire="Cust_SplanPost_email_Wire_Approved";
	String CUST_SPLAN_Rejected="Cust_Splan_email_Wire_Rejected";
	String CUST_POST_SPLAN_Rejected="Cust_SplanPost_email_Wire_Rejected";
	String VAS_ADDED_EVENT = "VAS_ADDED";
	String SUBSIDIARY_ADDED_EVENT = "SUBSIDIARY_ADDEDED";
	String SUBSIDIARY_ACTIVATION_EVENT = "SUBSIDIARY_ACTIVATION_ALERT";
	String ASSIGN_NOTI_TORM = "ASSIGN_NOTIFICATION_TO_RM";
	String ASSIGN_NOTIFICATION_TO_RM_RE = "ASSIGN_NOTIFICATION_TO_RM_RE";
	String ASSIGN_NOTIFICATION_TO_RM_BC = "ASSIGN_NOTIFICATION_TO_RM_BC";
	String ASSIGN_NOTIFICATION_TO_RM_BA = "ASSIGN_NOTIFICATION_TO_RM_BA";
	String CU_SPLAN_NOTIFICATON_TORM = "CU_SPLAN_NOTIFICATON_TORM";
	String BC_SPLAN_NOTIFICATON_TORM = "BC_SPLAN_NOTIFICATON_TORM";
	String BAU_SPLAN_NOTIFICATON_TORM = "BAU_SPLAN_NOTIFICATON_TORM";
	String FIXED_COUPON_ALERT = "FIXED_COUPON_CODE_CREATED_ALERT";
	String PERCENT_COUPON_ALERT = "Percent_COUPON_CODE_CREATED_ALERT";
	String CU_VAS_TO_RM = "CU_VAS_NOTIFICATION_TORM";
	String BC_VAS_TO_RM = "BC_VAS_NOTIFICATION_TORM";
	String BAU_VAS_TO_RM = "BAU_VAS_NOTIFICATION_TORM";
	String KYC_APP_FROMRM_TO_CU = "KYC_APPROVAL_FROMRMTO_CUSTOMER";
	String KYC_REJ_FROMRM_TO_CU = "KYC_REJECTION_FROMRMTO_CUSTOMER";
	String KYC_UPLOAD = "KYC_UPLOAD";
	String KYC_APPROVED = "KYC_APPROVED";
	String KYC_REJECT = "KYC_REJECT";
	String CU_INVALID_FLAG = "UserId_NOT_Registered(Cust_Splan_email)";
	String INVALID_CU_SUBSIDIARY_ADDED = "UserId_NOT_Registered(SUBSIDIARY_ADDEDED)";
	String INVALID_CU_SUBSIDIARY_ACTIVATION_ALERT = "UserId_NOT_Registered(SUBSIDIARY_ACTIVATION_ALERT)";
	String QUOTE_ACCEPT = "QUOTE_ACCEPT";
	String QUOTE_REJECTION = "QUOTE_REJECTION";
	String Quote_Id_NOT_Register = "Quote_Id_NOT_Register";
	String Winning_Quote_Data = "Winning_Quote_Data";
	String ADMIN_ACCOUNT_ACTIVATE = "ADMIN_ACCOUNT_ACTIVATE";
	String ADD_REFER_TO_PARENT = "ADD_REFER_ALERT_TO_PARENT";
	String ADD_REFER = "ADD_REFER";
	String Invalid_ParentID = "ParentId_Not_Save";
	String STATUS = "Pending";
	String BDETAILS = "WIRE_TRANSFER";
	String ACCNAME = "Nimai Trade Fintech Limited";
	String COMPANYADDRESS1 = "106 , Robinson Road";
	String COMPANYADDRESS2 = "#23-08 SBF Center.";
	String COMPANYADDRESS3 = "Singapore";
	String BAN = "072 035650 0";
	String BANKNAME = "DBS Bank Ltd Singapore";
	String BANKADDRESS1 = "1 Maritime Square";
	String BANKADDRESS2 = ", #02-122/123 Harbourfront Centre";
	String BANKADDRESS3 = "Singapore 099253";
	String BSD = "DBSSSGSG";
	String IBN = "JP Morgan Chase Bank, N.A.";
	String IBNBSD = "CHASUS33";
	String KYC_APP_FROMRM_TO_RE = "KYC_APPROVAL_FROMRMTO_REFERRER";
	String KYC_REJ_FROMRM_TO_RE = "KYC_REJECTION_FROMRMTO_REFERRER";
	String ACCOUNT_REFER = "CUSTOMER_ACCOUNT_REFERRED";
	/*
	 * EMAILSTATUS:- email staus change after transaction rolled back but failed to
	 * update the status
	 */
	String EMAILSTATUS = "Email_Sent";
	String ACCTYPE = "DBS Corporate Multi-Currency Account";
	String BBRANCH = "Harbourfront Branch";
	String KYC_REJ_FROMRM_TO_RE_Support = "KYC_REJECTION_FROMRMTO_REFERRER_Support";
	String KYC_REJ_FROMRM_TO_CU_Support = "KYC_REJECTION_FROMRMTO_CUSTOMER_Support";
	String VAS_PLAN_WIRE_REJECTED = "VAS_PLAN_WIRE_REJECTED";
	String KYC_REJ_FROMRM_TO_BA = "KYC_REJECTION_FROMRMTO_BANk";
	String KYC_REJ_FROMRM_TO_BA_Support = "KYC_REJECTION_FROMRMTO_BANk_Support";
String PASS_USER_UPDATE_LC="Passcode_LC_UPDATE(DATA)";
String PASS_USER_UPLOAD_LC="Passcode_LC_UPLOAD(DATA)";
String LC_UPDATE="LC_UPDATE(DATA)";
String LC_UPLOAD="LC_UPLOAD(DATA)";
String PASS_USER_UPDATE_LC_PARENT="Passcode_LC_UPDATE(DATA)_Alert_TO_Parent";
String PASS_USER_UPLOAD_LC_PARENT="Passcode_LC_UPLOAD(DATA)_Alert_TO_Parent";
String BIDALERT="BId_ALERT_ToCustomer";
String OFFBAUQUOTEPLACE="OffBAU_QUOTE_PLACE_ALERT_ToBanks";
String QUOTEPLACEBANK="QUOTE_PLACE_ALERT_ToBanks";
String BANKDETL="Bank_Details_tocustome";
String LCREOPEN="LC_REOPENING_ALERT_ToBanks";
String WINQUOTETOBANK="Winning_Quote_Alert_toBanks";
String WINQUOTEDATA="Winning_Quote_Data";
String SENTFLAG="Sent";
String TRIDNOREGISTER="Tr_Id_not_Register";
String PENDINGFLG="Pending";
String QUATSTATUSREJ="Rejected";
String FAILEVENT ="PHASE_TWO_TRNSFER_FAIL";
String TOEMAIL="phaseTwoTransferToEmail";
String CCEMAIL="phaseTwoTransferCCEmail";



}