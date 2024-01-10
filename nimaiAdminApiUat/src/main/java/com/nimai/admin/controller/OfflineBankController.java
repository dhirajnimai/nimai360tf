package com.nimai.admin.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.PastOrPresent;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nimai.admin.model.Countrycurrency;
import com.nimai.admin.model.GenericResponse;
import com.nimai.admin.model.Goods;
import com.nimai.admin.payload.EmployeeListRequest;
import com.nimai.admin.payload.OffLineBankRequest;
import com.nimai.admin.payload.PagedResponse;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.repository.CountrycurrencyRepository;
import com.nimai.admin.repository.CustomerRepository;
import com.nimai.admin.repository.GoodsRepository;
import com.nimai.admin.service.BankService;
import com.nimai.admin.service.OffLineBankService;




@RestController
@CrossOrigin(origins = { "*" })
@RequestMapping({ "/api/offBank" })
public class OfflineBankController {

    private static Logger logger;
    
    @Autowired
    OffLineBankService bankService;
    
    @Autowired
	GoodsRepository goodsRepo;
    
    @Autowired
	CustomerRepository customerRepository;
    
	@Autowired
	CountrycurrencyRepository countryrepo;
  
	  @PostMapping({ "/save" })
	public ResponseEntity<?> createOrUpdateOfflineBanks(@RequestBody OffLineBankRequest request){
		  GenericResponse response=new GenericResponse<>();
		  Boolean checkForDuplicateEmailId=bankService.existsByEmailId(request);
		  if(checkForDuplicateEmailId==true) {
			  
				return bankService.createOrUpdateOfflineBanks(request);
		  }else {
			  
				response.setErrCode("ASA002");
				response.setMessage("Email Id Already exists");
				return new ResponseEntity<Object>(response, HttpStatus.OK);
		  }
		 
		  

	}



	@PostMapping({ "/checkDuplicateEmailAddUser/{userType}" })
	public ResponseEntity<?> emailIdCheck(@PathVariable("userType") String userType, @RequestBody OffLineBankRequest request){
		GenericResponse response=new GenericResponse<>();
		boolean checkForDuplicateEmailId=bankService.existsByEmIdAddUser(request,userType);
		if(checkForDuplicateEmailId) {
			response.setErrCode("ASA01");
			response.setMessage("Success");
		}else {
			response.setErrCode("ASA002");
			response.setMessage("Email Id Already exists");
		}
		return new ResponseEntity<Object>(response, HttpStatus.OK);

	}

	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getGoodsData", produces = "application/json", method = RequestMethod.GET)
	public List<Goods> getGoods() {
		// return lcservice.getGoods();
		try {
			return (List<Goods>) goodsRepo.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		return null;
	}
	
	@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getCurrency", produces = "application/json", method = RequestMethod.GET)
	public List<String> getCurrency() {
		try {
			return (List<String>) countryrepo.getCurrency();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		return null;

	}
	
		@CrossOrigin(value = "*", allowedHeaders = "*")
	@RequestMapping(value = "/getcountryData", produces = "application/json", method = RequestMethod.GET)
	public List<Countrycurrency> getCountry() {
		try {
			return (List<Countrycurrency>) countryrepo.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		return null;

	}
		   @PostMapping({ "/offLineSearchList" })
		    public PagedResponse<?> getSearchCustomer(@RequestBody final SearchRequest request) {
		        return (PagedResponse<?>)this.bankService.getSearchOfflineBankDetail(request);
		    }
	
		   
			@PostMapping("/updateOffBauUserStatus")
			public ResponseEntity<?> updateOffBauUserStatus(@RequestBody Map<String, String> data) {
				return bankService.updateOffBauUserStatus(data.get("empId"), data.get("status"));

			}
			
			
			@PostMapping("/editOffLineUser")
			public ResponseEntity<?> editOffBauUserStatus(@RequestBody OffLineBankRequest request) {
			
					  
					  return bankService.editOffBauUserStatus(request);
				
				

			}
			
			
			
			
			
			
			
}
