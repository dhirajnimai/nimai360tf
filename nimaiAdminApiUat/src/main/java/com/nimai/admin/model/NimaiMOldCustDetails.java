package com.nimai.admin.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "nimai_m_old_customer_details")
public class NimaiMOldCustDetails {
	
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private int goodsId;
    
    @JoinColumn(name = "USERID", referencedColumnName = "USERID")
    @ManyToOne
    private NimaiMCustomer userid;
    
    @Column(name = "old_firstname")
    private String oldFirstName;
    
    @Column(name = "old_lasname")
    private String oldastName;
    
    @Column(name = "old_email_address")
    private String oldEmailId;
    
    @Column(name = "old_mobile_number")
    private String oldMobileNumber;
    
    @Column(name = "inserted_date")
    private Date insertedDate;
    
    @Column(name = "modified_date")
    private Date modifiedDate;
    
    @Column(name = "inserted_by")
    private String insertedBy;
    
    @Column(name = "modified_by")
    private String modifiedBy;
    
    

    
    
    
	public int getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(int goodsId) {
		this.goodsId = goodsId;
	}
	public NimaiMCustomer getUserid() {
		return userid;
	}
	public void setUserid(NimaiMCustomer userid) {
		this.userid = userid;
	}
	public String getOldFirstName() {
		return oldFirstName;
	}
	public void setOldFirstName(String oldFirstName) {
		this.oldFirstName = oldFirstName;
	}
	public String getOldastName() {
		return oldastName;
	}
	public void setOldastName(String oldastName) {
		this.oldastName = oldastName;
	}
	public String getOldEmailId() {
		return oldEmailId;
	}
	public void setOldEmailId(String oldEmailId) {
		this.oldEmailId = oldEmailId;
	}
	public String getOldMobileNumber() {
		return oldMobileNumber;
	}
	public void setOldMobileNumber(String oldMobileNumber) {
		this.oldMobileNumber = oldMobileNumber;
	}
	public Date getInsertedDate() {
		return insertedDate;
	}
	public void setInsertedDate(Date insertedDate) {
		this.insertedDate = insertedDate;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getInsertedBy() {
		return insertedBy;
	}
	public void setInsertedBy(String insertedBy) {
		this.insertedBy = insertedBy;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
    
    
    
    
    
    
    
    

}
