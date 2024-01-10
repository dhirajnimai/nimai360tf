package com.nimai.email.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "NIMAI_M_ENCRYPTION")
public class NimaiEncryptedDetails {
	
	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "token")
	private String token;

	@Column(name = "INSERTED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date insertedDate;
	
	@Column(name = "TOKEN_LENGTH")
	private int tokenLength;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getInsertedDate() {
		return insertedDate;
	}

	public void setInsertedDate(Date insertedDate) {
		this.insertedDate = insertedDate;
	}

	public int getTokenLength() {
		return tokenLength;
	}

	public void setTokenLength(int tokenLength) {
		this.tokenLength = tokenLength;
	}




	

}
