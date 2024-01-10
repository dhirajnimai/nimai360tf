package com.nimai.email.dao;

import java.util.ArrayList;




import java.util.List;

import javax.persistence.NoResultException;

import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;
import com.nimai.email.entity.NimaiClient;
import com.nimai.email.entity.NimaiEmailSchedulerAlertToBanks;
import com.nimai.email.entity.NimaiLC;
import com.nimai.email.entity.NimaiMBranch;
import com.nimai.email.entity.QuotationMaster;
import com.nimai.email.entity.TransactionSaving;

@Repository
public class BanksAlertDaoImpl implements BanksAlertDao {

	private static Logger logger = LoggerFactory.getLogger(BanksAlertDaoImpl.class);
	@Autowired
	SessionFactory sessionFactory;

	@Override
	public NimaiEmailSchedulerAlertToBanks saveSchdulerData(NimaiEmailSchedulerAlertToBanks schedulerEntity) {
		logger.info("inside save saveSchdulerData method of BanksAlertDaoImpl class");
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(schedulerEntity);
			return schedulerEntity;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return schedulerEntity;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<NimaiEmailSchedulerAlertToBanks> getTransactionDetail() {
		logger.info("inside save getTransactionDetail method of BanksAlertDaoImpl class");
		List<NimaiEmailSchedulerAlertToBanks> emailDetailsScheduled = new ArrayList<>();
		try { 
			Session session = sessionFactory.getCurrentSession();
			logger.info("inside session1 getTransactionDetail method of BanksAlertDaoImpl class");
			Query query = session
					.createQuery("from NimaiEmailSchedulerAlertToBanks nb where nb.emailFlag = :emailFlag and nb.emailCount<=3 ORDER BY nb.scedulerid DESC");
			query.setParameter("emailFlag", "pending");
			emailDetailsScheduled = query.getResultList();
			logger.info("inside  session2 getTransactionDetail method of BanksAlertDaoImpl class");
			return emailDetailsScheduled;
			
		} catch (NoResultException nre) {
			logger.info("inside catch session1 getTransactionDetail method of BanksAlertDaoImpl class");
			nre.printStackTrace();
			return null;
		}
	}

	@Override
	public void deleteEmailTransactionDetail(int scedulerid) {
		// TODO Auto-generated method stub
		logger.info("inside save deleteEmailTransactionDetail method of BanksAlertDaoImpl class");
		try {
			Session session = sessionFactory.getCurrentSession();
			NimaiEmailSchedulerAlertToBanks email = (NimaiEmailSchedulerAlertToBanks) session
					.load(NimaiEmailSchedulerAlertToBanks.class, new Integer(scedulerid));
			if (null != email) {
				session.delete(email);
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		}
	}

	@Override
	public void updateEmailFlag(int scedulerid) {
		// TODO Auto-generated method stub
		logger.info("inside save updateEmailFlag method of BanksAlertDaoImpl class");
		try {
			Session session = sessionFactory.getCurrentSession();
//		    UPDATE nimai_email_scheduler_alerts_tobanks ns
//		  SET ns.Email_Status='Sent' where
//		  ns.scheduler_Id=77403
//			
//			
//			NimaiEmailSchedulerAlertToBanks email = (NimaiEmailSchedulerAlertToBanks) session
//					.load(NimaiEmailSchedulerAlertToBanks.class, new Integer(scedulerid));
//			if (null != email) {
//				email.setEmailFlag("sent");
//				session.update(email);
//				
//			}
			String emailFlag="sent";
			Query query = session.createQuery(
					"UPDATE NimaiEmailSchedulerAlertToBanks nb set nb.emailFlag= :emailFlag where nb.scedulerid= :scedulerid");
			query.setParameter("emailFlag", emailFlag);
			query.setParameter("scedulerid", scedulerid);
			

			query.executeUpdate();
			
		} catch (NoResultException nre) {
			
			logger.info("============Inside updateEmailFlag method of BanksAlertDaoImpl transaction rollback exception==============");
			
			nre.printStackTrace();
		}
	}

	@Override
	public void updateInvalidIdEmailFlag(int scedulerid, String emailstatus) {
		// TODO Auto-generated method stub
		logger.info("inside save updateInvalidIdEmailFlag method of BanksAlertDaoImpl class");
		try {
			Session session = sessionFactory.getCurrentSession();
//			NimaiEmailSchedulerAlertToBanks email = (NimaiEmailSchedulerAlertToBanks) session
//					.load(NimaiEmailSchedulerAlertToBanks.class, new Integer(scedulerid));
//			if (null != email) {
//				email.setEmailFlag(emailstatus);
//				session.update(email);
//			}

			
			//String emailFlag="sent";
			Query query = session.createQuery(
					"UPDATE NimaiEmailSchedulerAlertToBanks nb set nb.emailFlag= :emailstatus where nb.scedulerid= :scedulerid");
			query.setParameter("emailstatus", emailstatus);
			query.setParameter("scedulerid", scedulerid);
			

			query.executeUpdate();
			
			
			
		} catch (NoResultException nre) {
			logger.info("============Inside updateInvalidIdEmailFlag method of BanksAlertDaoImpl==============");
			nre.printStackTrace();
		}
	}

	@Override
	public NimaiLC getTransactioDetailsByTransId(String transactionid) {
		logger.info("=======inside save getTransactioDetailsByTransIs method of BanksAlertDaoImpl class=========");
		NimaiLC results = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("from NimaiLC n where n.transactionId = :transactionid ", NimaiLC.class);
			query.setParameter("transactionid", transactionid);
			results = (NimaiLC) query.getSingleResult();
		} catch (NoResultException nre) {
			nre.printStackTrace();
			return null;
		}

		return results;
	}
	@Override
	public QuotationMaster getTransactioDetailsByTrQId(String transactionid,int quotationId,String bankuserId) {
		logger.info("=======inside save getTransactioDetailsByTrQId method of BanksAlertDaoImpl class=========");
		QuotationMaster results = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("from QuotationMaster n where n.transactionId.transactionId = :transactionid and n.userid.userid=bankuserId and n.quotationId=:quotationId ", QuotationMaster.class);
			query.setParameter("transactionid", transactionid);
			query.setParameter("quotationId", quotationId);
			query.setParameter("bankuserId", bankuserId);
			results = (QuotationMaster) query.getSingleResult();
		} catch (NoResultException nre) {
			nre.printStackTrace();
			return null;
		}

		return results;
	}

	@Override
	public NimaiClient getCustDetailsByUserId(String custUserId) {
		logger.info("inside save getCustDetailsByUserId method of BanksAlertDaoImpl class userId"+custUserId);
		
		logger.info("=================inside save getCustDetailsByUserId methos22123 of BanksAlertDaoImpl class userId================+++++++++++="+custUserId);
		NimaiClient results = null;

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("from NimaiClient n where n.userid = :custUserId ", NimaiClient.class);
			query.setParameter("custUserId", custUserId);
			results = (NimaiClient) query.getSingleResult();
		} catch (NoResultException nre) {
			nre.printStackTrace();
			return null;
		}

		return results;

	}

	@Override
	public QuotationMaster getDetailsByQuoteId(int quotationId) {
		logger.info("inside save getTransactioDetailsByTransIs method of BanksAlertDaoImpl class getDetailsByQuoteId");
		QuotationMaster results = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("from QuotationMaster n where n.quotationId = :quotationId ",
					QuotationMaster.class);
			query.setParameter("quotationId", quotationId);
			results = (QuotationMaster) query.getSingleResult();
			System.out.println("#########################@@@@@@@@@@@@@@@@" + results.toString());
		} catch (NoResultException nre) {
			nre.printStackTrace();
			return null;
		}

		return results;
	}

	@Override
	public List<NimaiEmailSchedulerAlertToBanks> getTransactionDetailByTrEmailStatus() {
		List<NimaiEmailSchedulerAlertToBanks> emailDetailsScheduled = new ArrayList<>();
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(
					"from NimaiEmailSchedulerAlertToBanks nb where nb.transactionEmailStatusToBanks = :emailFlag");
			query.setParameter("emailFlag", "pending");
			emailDetailsScheduled = query.getResultList();
			return emailDetailsScheduled;
		} catch (NoResultException nre) {
			nre.printStackTrace();
			return null;
		}
	}

	@Override
	public void updateTrStatusEmailFlag(int schedulerId) {
		logger.info("inside save updateTrStatusEmailFlag method of BanksAlertDaoImpl class");
		try {
			Session session = sessionFactory.getCurrentSession();
			NimaiEmailSchedulerAlertToBanks email = (NimaiEmailSchedulerAlertToBanks) session
					.load(NimaiEmailSchedulerAlertToBanks.class, new Integer(schedulerId));
			if (null != email) {
				email.setTransactionEmailStatusToBanks("sent");
				session.update(email);
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		}
	}

	@Override
	public void updateTREmailStatus(int scedulerid) {
		// TODO Auto-generated method stub
		logger.info("inside save updateTREmailStatus method of BanksAlertDaoImpl class");
		try {
			Session session = sessionFactory.getCurrentSession();
			NimaiEmailSchedulerAlertToBanks email = (NimaiEmailSchedulerAlertToBanks) session
					.load(NimaiEmailSchedulerAlertToBanks.class, new Integer(scedulerid));
			if (null != email) {
				email.setTransactionEmailStatusToBanks("In-Process");
				session.update(email);
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		}
	}

	@Override
	public void updateBankEmailFlag(int scedulerid) {
	
		
		// TODO Auto-generated method stub
		try {
			Session session = sessionFactory.getCurrentSession();
			NimaiEmailSchedulerAlertToBanks email = (NimaiEmailSchedulerAlertToBanks) session
					.load(NimaiEmailSchedulerAlertToBanks.class, new Integer(scedulerid));
			if (null != email) {
				email.setEmailFlag("sent");
				email.setTransactionEmailStatusToBanks("sent");
				session.update(email);
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		}
	}

	@Override
	public List<QuotationMaster> getBankQuoteList(String transactionid) {
		logger.info("inside save getTransactionDetail method of BanksAlertDaoImpl class");
		List<QuotationMaster> emailDetailsScheduled = new ArrayList<>();
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("from QuotationMaster nb where nb.quotationStatus = :quotationStatus and"
					+ " nb.transactionId = :transactionid");
			query.setParameter("quotationStatus", "Expired");
			query.setParameter("transactionid", transactionid);
			emailDetailsScheduled = query.getResultList();
			return emailDetailsScheduled;
		} catch (NoResultException nre) {
			nre.printStackTrace();
			return null;
		}
	}

	@Override
	public NimaiEmailSchedulerAlertToBanks saveBankSchData(NimaiEmailSchedulerAlertToBanks schdulerData) {
		logger.info("inside save saveSchdulerData method of BanksAlertDaoImpl class");
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(schdulerData);
			return schdulerData;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return schdulerData;
	}

	@Override
	public TransactionSaving getSavingDetails(String transactionid) {
		logger.info("inside save TransactionSaving method of BanksAlertDaoImpl class 1:");
		TransactionSaving results = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("from TransactionSaving n where n.transactionid = :transactionid ", TransactionSaving.class);
			query.setParameter("transactionid", transactionid);
			results = (TransactionSaving) query.getSingleResult();
		} catch (NoResultException nre) {
			logger.info("inside save TransactionSaving method of BanksAlertDaoImpl class catch 2:");
			nre.printStackTrace();
			return null;
		}

		return results;
	}

	@Override
	public NimaiMBranch getBrDetailsByEmail(String passcodeuserEmail) {
		NimaiMBranch result = null;
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("From NimaiMBranch b WHERE emailId=:emailId order by b.insertTime desc");
			query.setParameter("emailId", passcodeuserEmail);
			query.setMaxResults(1);
			result = (NimaiMBranch) query.getSingleResult();
		} catch (NoResultException re) {
			return null;
		}
		return result;
	}

	@Override
	public NimaiClient getcuDetailsByEmail(String branchUserEmail) {
		// TODO Auto-generated method stub
		logger.info("inside save getCustDetailsByUserId method of BanksAlertDaoImpl class branchUserEmail"+branchUserEmail);
		NimaiClient results = null;

		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("from NimaiClient n where n.emailAddress = :branchUserEmail ", NimaiClient.class);
			query.setParameter("branchUserEmail", branchUserEmail);
			results = (NimaiClient) query.getSingleResult();
		} catch (NoResultException nre) {
			nre.printStackTrace();
			return null;
		}

		return results;
	}
}
