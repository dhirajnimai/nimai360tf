package com.nimai.splan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.nimai.splan.model.NimaiSubscriptionVas;

import javax.transaction.Transactional;

public interface NimaiSubscriptionVasRepo extends JpaRepository<NimaiSubscriptionVas, Integer>
{
	@Query("SELECT na FROM NimaiSubscriptionVas na WHERE na.userId = (:userId)")
	List<NimaiSubscriptionVas> findAllByUserId(String userId);

	@Query("SELECT na FROM NimaiSubscriptionVas na WHERE na.userId = (:userId) and na.status = 'Active'")
	List<NimaiSubscriptionVas> findActiveVASByUserId(String userId);

	@Modifying
	@Transactional
	@Query(value = "update nimai_subscription_vas  set nimai_subscription_vas.invoice_id= ?2,payment_status='Approved' WHERE nimai_subscription_vas.userid= ?1 AND (payment_status='Pending' or payment_status='Rejected') and status='Active'",nativeQuery = true)
	void updatePaymentTransactionId(String userid,String generatePaymentTtransactionID);

	@Modifying
	@Transactional
	@Query(value = "update nimai_subscription_vas  set nimai_subscription_vas.invoice_id= ?2 WHERE nimai_subscription_vas.userid= ?1 AND (payment_status='Pending' or payment_status='Rejected') and status='Active'",nativeQuery = true)
	void updatePaymentTransactionIdVAS(String userid,String generatePaymentTtransactionID);


}
