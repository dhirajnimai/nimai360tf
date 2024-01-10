package com.nimai.splan.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.nimai.splan.model.NimaiCustomerSubscriptionGrandAmount;
import com.nimai.splan.model.NimaiMSubscription;
import com.nimai.splan.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.splan.model.NimaiTransactionViewCount;
import com.nimai.splan.model.OnlinePayment;
import com.nimai.splan.payload.CustomerSubscriptionGrandAmountBean;
import com.nimai.splan.payload.EditPostpaidBean;
import com.nimai.splan.payload.SplanRequest;
import com.nimai.splan.payload.SubscriptionAndPaymentBean;
import com.nimai.splan.payload.SubscriptionBean;
import com.nimai.splan.payload.SubscriptionPaymentBean;
import com.nimai.splan.payload.TransactionPostPaidDetail;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.nimai.splan.payload.PostpaidSubscriptionBean;

public interface SubscriptionPlanService {

	ResponseEntity<?> saveUserSubscriptionPlan(SubscriptionBean subscriptionRequest, String userID);

	ResponseEntity<?> renewSubscriptionPlan(SubscriptionBean subscriptionRequest, String userID, Integer lcUtilizedCount);

	ResponseEntity<?> findSPlanDetailsByUserId(String userId);

	ResponseEntity<?> getSPlanByUserId(String userId);
	
	ResponseEntity<?> findMSPlanDetails(String userId);

	ResponseEntity<?> findCustomerSPlanDetails(SplanRequest splanRequest);

	//SubscriptionPaymentBean initiatePayment(SubscriptionPaymentBean sPymentRequest);
	
	Map<String, Object> initiatePayment(SubscriptionPaymentBean sPymentRequest, Double grandAmt, String subsCurrency) throws PayPalRESTException;

	Map<String, Object> initiatePaymentForPostpaid(SubscriptionPaymentBean sPaymentRequest, Double grandAmt, String subsCurrency) throws PayPalRESTException;

	//Map<String, Object> executePayment(String paymentId, String payerId) throws PayPalRESTException;
	
	//HashMap<String,String> getPaymentResponse(String encResp);

	OnlinePayment checkPayment(SubscriptionPaymentBean sPymentRequest);

	NimaiMSubscription getPlanDetailsBySubscriptionId(String string);

	ResponseEntity<?> findAllSPlanDetailsForCustomer(String userId);

	ResponseEntity<?> checkForSubsidiary(SubscriptionBean subscriptionRequest);
	
	ResponseEntity<?> insertGrandAmountData(CustomerSubscriptionGrandAmountBean subscriptionRequest);

	NimaiCustomerSubscriptionGrandAmount getCustomerAmount(String userId);

	boolean checkPaymentData(int id, Double amt);

	Map<String, Object> completePayment(Payment payment,String paymentId);

	//void saveData(Payment payment, String paymentId) throws IOException;

	void saveData(String orderId, String status) throws IOException;
	
	void saveDataPostpaid(String orderId, String status) throws IOException;

	Map<String, Object> executePayment(String orderId) throws PayPalRESTException;

	ResponseEntity<?> getInactiveSPlanByUserId(String userId);

	List<SubscriptionAndPaymentBean> getLastPurchasedPlan(String userId,String planType) throws ParseException;

	ResponseEntity<?> saveUserPostPaidSPlan(SubscriptionBean subscriptionRequest,String flagged, String userID);

	ResponseEntity<?> pushPostpaidSPlanPayment(PostpaidSubscriptionBean postpaidSubscriptionRequest, String userID,String txnId,String flag,String amountField);

	ResponseEntity<?> unpaidPostpaidSubscriptionPlan(String userID, String txnID);

	ResponseEntity<?> getPostpaidFreezePlacedSubscriptionPlan(String userID);

	ResponseEntity<?> getMinAndTotalSubscriptionPlan(String userID);
	ResponseEntity<?> editPostpaidPlanDetails(String userID,String txnID, EditPostpaidBean editBean);

	ResponseEntity<?> getApprovedTransactions(String userID);

	ResponseEntity<?> getPendingTransactions(String userID);

	ResponseEntity<?> pushPostpaidSPlanPayment(String userID, String txnId);

	ResponseEntity<?> payForPostpaidSubscriptionPlan(String userID, String txnId, String amount, String discId, String discAmt, String mode2);

	ResponseEntity<?> overallPostpaidSubscriptionPlan(String userID);

	String getTransactionIdOfPending(String userId);

	void totalDuePayment(String userId, Integer vasId, Integer discId, Double discAmt,Double grAmt);
	
	void pushPostpaidSPlanPaymentUnQuoted();

	Map<String, Object> executePaymentPostPaid(String orderId) throws PayPalRESTException;

	ResponseEntity<?> getPostpaidTxnDet(String userID);

	ResponseEntity<?> overallPostpaidSubscriptionPlanBA(String userID);

	List<TransactionPostPaidDetail> getTransactionPostPaidDetail(String userId);

	ResponseEntity<?> overallPostpaidSubscriptionPlanv2(String userID);

	NimaiTransactionViewCount getViewCountByUserId(String userID);

	void updateViewCountByUserId(String userID);

	ResponseEntity<?> renewSubscriptionPlanV2(SubscriptionBean subscriptionRequest, String userID,
			Integer lcUtilizedCount);
}
