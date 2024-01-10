package com.nimai.admin.payload;

public class ApiResponse {
  private Boolean success;
  
  private String message;
  
  private String uniqueKey;
  private String status;
  private Object content;

  private int KeyId;
  
  public ApiResponse(Boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public ApiResponse(Boolean success, String message,String status) {
    this.success = success;
    this.message = message;
    this.status = status;
  }

  public ApiResponse(Boolean success, String message,Object content) {
    this.success = success;
    this.message = message;
    this.content = content;
  }

  public ApiResponse(Boolean success, String message,Object content,String status) {
    this.success = success;
    this.message = message;
    this.content = content;
    this.status = status;
  }
//  
//  public ApiResponse(Boolean success, String message, String uniqueKey) {
//    this.success = success;
//    this.message = message;
//    this.uniqueKey = uniqueKey;
//  }
  
//  public ApiResponse(Boolean success, String message, int KeyId) {
//    this.success = success;
//    this.message = message;
//    this.KeyId = KeyId;
//  }
//  
  public int getKeyId() {
    return this.KeyId;
  }
  
  public void setKeyId(int keyId) {
    this.KeyId = keyId;
  }
  
  public Boolean getSuccess() {
    return this.success;
  }
  
  public void setSuccess(Boolean success) {
    this.success = success;
  }
  
  public String getMessage() {
    return this.message;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  public String getUniqueKey() {
    return this.uniqueKey;
  }
  
  public void setUniqueKey(String uniqueKey) {
    this.uniqueKey = uniqueKey;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
  public Object getContent() {
    return content;
  }

  public void setContent(Object content) {
    this.content = content;
  }
}
