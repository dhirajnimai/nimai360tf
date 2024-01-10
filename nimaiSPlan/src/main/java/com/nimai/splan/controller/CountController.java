package com.nimai.splan.controller;

import com.nimai.splan.model.NimaiMCustomer;
import com.nimai.splan.payload.GenericResponse;
import com.nimai.splan.service.GetCountService;
import com.nimai.splan.service.TokenService;
import com.nimai.splan.utility.ErrorDescription;
import java.util.HashMap;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"*"})
@RestController
public class CountController {
  private static final Logger logger = LoggerFactory.getLogger(CountController.class);
  
  @Autowired
  GetCountService getCountService;
  
  @Autowired
  TokenService tokenService;
  
  @CrossOrigin(value = {"*"}, allowedHeaders = {"*"})
  @RequestMapping(value = {"/getCount"}, produces = {"application/json"}, method = {RequestMethod.POST})
  public ResponseEntity<Object> getCount(@RequestBody NimaiMCustomer nimaicustomer, HttpServletRequest request) {
    logger.info("======== Getting personal details and count details =========");
    GenericResponse response = new GenericResponse();
    try {
      String userid = nimaicustomer.getUserid();
      String emailAddress = nimaicustomer.getEmailAddress();
      String userID = "";
      String token = request.getHeader("Authorization");
      System.out.println("Authorization: " + token.substring(7));
      if (userid.toString().substring(0, 2).equalsIgnoreCase("CU") || userid.toString().substring(0, 2).equalsIgnoreCase("BC") || userid.toString().substring(0, 2).equalsIgnoreCase("RE")) {
        if (emailAddress == null || emailAddress.equalsIgnoreCase("")) {
          NimaiMCustomer nc = this.getCountService.getEmailAddress(userid);
          userID = userid + "-" + nc.getEmailAddress();
        } else {
          userID = userid + "-" + emailAddress;
        } 
      } else {
        userID = userid;
      } 
      boolean validToken = this.tokenService.validateToken(userID, token.substring(7));
      if (validToken) {
        if (nimaicustomer.getUserid() != null && nimaicustomer.getEmailAddress() == null) {
          HashMap<String, Object> hashMap = this.getCountService.getCount(userid, emailAddress);
          response.setData(hashMap);
          response.setStatus("Success");
          return new ResponseEntity(response, HttpStatus.OK);
        } 
        HashMap<String, Object> outdata1 = this.getCountService.getCount(userid, emailAddress);
        response.setData(outdata1);
        response.setStatus("Success");
        return new ResponseEntity(response, HttpStatus.OK);
      } 
      response.setErrMessage("Unauthorized Access.");
      return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
    } catch (Exception e) {
      response.setStatus("Failure");
      response.setErrCode("EXE000");
      response.setErrMessage(ErrorDescription.getDescription("EXE000"));
      return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    } 
  }
}
