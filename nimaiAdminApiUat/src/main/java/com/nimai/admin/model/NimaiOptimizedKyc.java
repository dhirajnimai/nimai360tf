
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nimai.admin.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author Sahadeo
 */
@Entity
@Table(name = "nimai_f_kyc")
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class NimaiOptimizedKyc implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;
	@Column(name = "kyc_type")
	private String kycType;

	@Column(name = "country")
	private String country;


	@Column(name = "document_Name")
	private String documentName;

	@Column(name = "document_type")
	private String documentType;



	@Column(name = "kyc_status")
	private String kycStatus;




	@Column(name = "approved_maker")
	private String approvedMaker;




	@Column(name = "comment")
	private String comment;
	
	@JoinColumn(name = "USERID", referencedColumnName = "USERID")
	@ManyToOne
	private NimaiMCustomer userid;
	

	
	
	
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public NimaiOptimizedKyc() {
	}

	public NimaiOptimizedKyc(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getKycType() {
		return kycType;
	}

	public void setKycType(String kycType) {
		this.kycType = kycType;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}



	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}



	public String getKycStatus() {
		return kycStatus;
	}

	public void setKycStatus(String kycStatus) {
		this.kycStatus = kycStatus;
	}







	public NimaiMCustomer getUserid() {
		return userid;
	}

	public void setUserid(NimaiMCustomer userid) {
		this.userid = userid;
	}

	public String getApprovedMaker() {
		return approvedMaker;
	}

	public void setApprovedMaker(String approvedMaker) {
		this.approvedMaker = approvedMaker;
	}



	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof NimaiOptimizedKyc)) {
			return false;
		}
		NimaiOptimizedKyc other = (NimaiOptimizedKyc) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.nimai.admin.model.NimaiOptimizedKyc[ id=" + id + " ]";
	}

}


