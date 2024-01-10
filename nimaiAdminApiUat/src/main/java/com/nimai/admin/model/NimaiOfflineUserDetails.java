package com.nimai.admin.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "nimai_offline_users")
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class NimaiOfflineUserDetails {
	
	
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Basic(optional = false)
	    @Column(name = "offline_UId")
	    private Long offlineUId;

	   @Column(name="additional_user_id")
	   private String additionalUserId;
	   
	   @Column(name="first_name")
	private String firstName;
	   
	   @Column(name="last_name")
	private String lastName;
	   
	   @Column(name="email_address")
	private String emailAddress;
	   
	   @Column(name="mobile_number")
	private String mobileNo;
	   
	   @JoinColumn(name = "USERID", referencedColumnName = "USERID")
	    @ManyToOne
	    private NimaiMCustomer userid;
	   
	   @Column(name="created_by")
	   private String createdBy;
	   
	   @Column(name="inserted_date")
	   private Date insertedDate;
	   
	   @Column(name="approved_by")
	   private String approvedBy;
	   
	   @Column(name="approver_comment")
	   private String approverComment;
	   
	   @Column(name="approval_Date")
	   private Date approvalDate;
	   
	   @Column(name="is_parent")
	   private Integer isParent;

	@Column(name="parent_userid")
	private String parentUserId;

	public String getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(String parentUserId) {
		this.parentUserId = parentUserId;
	}

	public String getAdditionalUserId() {
		return additionalUserId;
	}

	public void setAdditionalUserId(String additionalUserId) {
		this.additionalUserId = additionalUserId;
	}

	public Date getApprovalDate() {
		return approvalDate;
	}

	public Integer getIsParent() {
		return isParent;
	}

	public void setIsParent(Integer isParent) {
		this.isParent = isParent;
	}

	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getInsertedDate() {
		return insertedDate;
	}

	public void setInsertedDate(Date insertedDate) {
		this.insertedDate = insertedDate;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public String getApproverComment() {
		return approverComment;
	}

	public void setApproverComment(String approverComment) {
		this.approverComment = approverComment;
	}


	public Long getOfflineUId() {
		return offlineUId;
	}

	public void setOfflineUId(Long offlineUId) {
		this.offlineUId = offlineUId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public NimaiMCustomer getUserid() {
		return userid;
	}

	public void setUserid(NimaiMCustomer userid) {
		this.userid = userid;
	}
	   
	   
	
	
	
	
	
}
