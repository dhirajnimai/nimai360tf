package com.nimai.email.api;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class GenericResponse <T> implements Serializable {
	private static final long serialVersionUID = 1L;

private String message;
	
	private T data;
	
	private List list;
	
	private int flag;
	 
	private Map map;
	
	private String errCode ;
	
	private int id;
	
	private File file;
	
	private T addData;
	
	private String tcFlag;
	
	private int encryptionLength;
	
	private String userId;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTcFlag() {
		return tcFlag;
	}

	public void setTcFlag(String tcFlag) {
		this.tcFlag = tcFlag;
	}

	public int getEncryptionLength() {
		return encryptionLength;
	}

	public void setEncryptionLength(int encryptionLength) {
		this.encryptionLength = encryptionLength;
	}

	public T getAddData() {
		return addData;
	}

	public void setAddData(T addData) {
		this.addData = addData;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}



}
