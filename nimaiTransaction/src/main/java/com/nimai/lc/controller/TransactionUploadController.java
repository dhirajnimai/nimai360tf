package com.nimai.lc.controller;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimai.lc.bean.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nimai.lc.entity.NimaiClient;
import com.nimai.lc.repository.LCMasterRepository;
import com.nimai.lc.repository.NimaiClientRepository;
import com.nimai.lc.service.LCService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({ "/upload" })
public class TransactionUploadController {

	@Autowired
	private LCController lCController;

	@Autowired
	private LCService lcservice;

	@Autowired
	private NimaiClientRepository cuRepo;

	@Autowired
	private LCMasterRepository lcmasterrepo;

	//@PostMapping("/uploadTr")
	public String uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws InvalidFormatException, ParseException {
		response.setContentType("application/octet-stream");
		String message = "";
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=users_" + "test" + ".xlsx";
		response.setHeader(headerKey, headerValue);

		try (ServletOutputStream outputStream = response.getOutputStream();
				InputStream inputStream = file.getInputStream();
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);) {
			System.out.println("=======================condition 1");
			validateExcel(workbook);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}


	@CrossOrigin(value = "*", allowedHeaders = "*")
	@PostMapping("/uploadTr")
	public ResponseEntity<?> uploadBulkTransaction(@RequestBody BulkTrUploadrequest request) {
		GenericResponse response=new GenericResponse<>();
		List<NimaiLCBean> beanList = new ArrayList<>();
		try{
			System.out.println("list size from front end"+request.getBulkTrList().size());
			for(NimaiLCBean beanBulk:request.getBulkTrList()) {
				NimaiLCBean bean=new NimaiLCBean();
				//bean.setSelector(beanBulk.getSelector());
				bean.setUserId(beanBulk.getUserId());
				bean.setRequirementType(beanBulk.getRequirementType());
				bean.setlCIssuanceBank(beanBulk.getlCIssuanceBank());
				bean.setlCIssuanceBranch(beanBulk.getlCIssuanceBranch());
				bean.setSwiftCode(beanBulk.getSwiftCode());
				bean.setlCIssuanceCountry(beanBulk.getlCIssuanceCountry());
				bean.setBranchUserEmail(beanBulk.getBranchUserEmail());
				bean.setlCValue(beanBulk.getlCValue());
				bean.setlCCurrency(beanBulk.getlCCurrency());
				bean.setLastShipmentDate(beanBulk.getLastShipmentDate());
				bean.setNegotiationDate(beanBulk.getNegotiationDate());
				bean.setGoodsType(beanBulk.getGoodsType());
				bean.setConfirmationPeriod(beanBulk.getConfirmationPeriod());
				bean.setPaymentTerms(beanBulk.getPaymentTerms());
				bean.setStartDate(beanBulk.getStartDate());
				bean.setApplicantName(beanBulk.getApplicantName());
				bean.setApplicantCountry(beanBulk.getApplicantCountry());
				bean.setBeneName(beanBulk.getBeneName());
				bean.setBeneBankCountry(beanBulk.getBeneBankCountry());
				bean.setBeneBankName(beanBulk.getBeneBankName());
				bean.setBeneSwiftCode(beanBulk.getBeneSwiftCode());
				bean.setBeneCountry(beanBulk.getBeneCountry());
				bean.setLoadingCountry(beanBulk.getLoadingCountry());
				bean.setLoadingPort(beanBulk.getLoadingPort());
				bean.setDischargeCountry(beanBulk.getDischargeCountry());
				bean.setDischargePort(beanBulk.getDischargePort());
				bean.setChargesType(beanBulk.getChargesType());
				bean.setIsESGComplaint(beanBulk.getIsESGComplaint());
				bean.setUserType(beanBulk.getUserType());
				bean.setBeneContactPerson(beanBulk.getBeneContactPerson());
				bean.setBeneContactPersonEmail(beanBulk.getBeneContactPersonEmail());
				bean.setApplicantContactPerson(beanBulk.getApplicantContactPerson());
				bean.setApplicantContactPersonEmail(beanBulk.getApplicantContactPersonEmail());
				bean.setDiscountingPeriod(beanBulk.getDiscountingPeriod());
				bean.setLastBankCountry(beanBulk.getLastBankCountry());
				bean.setLastBeneBank(beanBulk.getLastBeneBank());
				bean.setLastBeneSwiftCode(beanBulk.getLastBeneSwiftCode());
				bean.setLcMaturityDate(beanBulk.getLcMaturityDate());
				bean.setLcNumber(beanBulk.getLcNumber());
				bean.setOriginalTenorDays(beanBulk.getOriginalTenorDays());
				bean.setRefinancingPeriod(beanBulk.getRefinancingPeriod());
				bean.setlCExpiryDate(beanBulk.getlCExpiryDate());
				bean.setClaimExpiryDate(beanBulk.getClaimExpiryDate());
				bean.setBgType(beanBulk.getBgType());
				bean.setBillType(beanBulk.getBillType());
				bean.setValidity(beanBulk.getValidity());
				bean.setIndex(beanBulk.getIndex());
//				bean.setlCIssuingDate();
				beanList.add(bean);
			}



			boolean	isEligibleForPersist=true;

			persistTransaction(request.getBulkTrList(), isEligibleForPersist);

			response.setMessage("Success");
			return new ResponseEntity<>(response, HttpStatus.OK);
		}catch (Exception ex){
			response.setMessage("Failure");
			response.setErrCode("EXE000");
			response.setData(ex);
			return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
		}
		
		
	}
	
	
	public void validateExcel(XSSFWorkbook workbook) {
		System.out.println("=======================condition 2");
		try {
			Sheet datatypeSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = datatypeSheet.iterator();
			List<NimaiLCBean> beanList = new ArrayList<>();
			int rowNumber = 0;
			Boolean isEligibleForPersist = true;
			while (iterator.hasNext()) {
				Row currentRow = iterator.next();

				if (rowNumber == 0) {
					rowNumber++;
					
					continue;
				}
				Iterator<Cell> cellIterator = currentRow.iterator();

				NimaiLCBean bean = new NimaiLCBean();
				List<Integer> skipCells = Arrays.asList(10, 11, 12, 16, 29, 40, 44, 45);
				int cellIdx = 0;
				String exceptionMessage = "";
				while (cellIterator.hasNext()) {
					Cell currentCell = cellIterator.next();
					try {
						if (currentCell.getStringCellValue().equalsIgnoreCase("null")) {
							if (skipCells.contains(cellIdx)) {
								cellIdx++;
								continue;
							}
							currentCell.setCellValue("");
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

					try {
						switch (cellIdx) {
						case 0:
							currentCell.setCellType(CellType.STRING);
							bean.setSelector(currentCell.getStringCellValue());
							System.out.println("=======================condition 3"+currentCell.getStringCellValue());
							break;
						case 1:
							currentCell.setCellType(CellType.STRING);
							bean.setUserId(currentCell.getStringCellValue());
							break;
						case 2:
							currentCell.setCellType(CellType.STRING);
							bean.setRequirementType(currentCell.getStringCellValue());
							break;
						case 3:
							currentCell.setCellType(CellType.STRING);
							bean.setlCIssuanceBank(currentCell.getStringCellValue());
							break;
						case 4:
							currentCell.setCellType(CellType.STRING);
							bean.setlCIssuanceBranch(currentCell.getStringCellValue());
							break;
						case 5:
							currentCell.setCellType(CellType.STRING);
							bean.setSwiftCode(currentCell.getStringCellValue());
							break;
						case 6:
							currentCell.setCellType(CellType.STRING);
							bean.setlCIssuanceCountry(currentCell.getStringCellValue());
							break;
						case 7:
							currentCell.setCellType(CellType.STRING);
							bean.setBranchUserEmail(currentCell.getStringCellValue());
							break;
						case 8:
							currentCell.setCellType(CellType.NUMERIC);
							bean.setlCValue(currentCell.getNumericCellValue());
							break;
						case 9:
							currentCell.setCellType(CellType.STRING);
							bean.setlCCurrency(currentCell.getStringCellValue());
							break;
						case 10:
							bean.setlCIssuingDate(currentCell.getDateCellValue());
							break;
						case 11:
							bean.setLastShipmentDate(currentCell.getDateCellValue());
							break;
						case 12:
							bean.setNegotiationDate(currentCell.getDateCellValue());
							break;
						case 13:
							currentCell.setCellType(CellType.STRING);
							bean.setGoodsType(currentCell.getStringCellValue());
							break;
						case 14:
							currentCell.setCellType(CellType.STRING);
							bean.setConfirmationPeriod(currentCell.getStringCellValue());
							break;
						case 15:
							currentCell.setCellType(CellType.STRING);
							bean.setPaymentTerms(currentCell.getStringCellValue());
							break;
						case 16:
							bean.setStartDate(currentCell.getDateCellValue());
							break;
						case 17:
							currentCell.setCellType(CellType.STRING);
							bean.setApplicantName(currentCell.getStringCellValue());
							break;
						case 18:
							currentCell.setCellType(CellType.STRING);
							bean.setApplicantCountry(currentCell.getStringCellValue());
							break;
						case 19:
							currentCell.setCellType(CellType.STRING);
							bean.setBeneName(currentCell.getStringCellValue());
							break;
						case 20:
							currentCell.setCellType(CellType.STRING);
							bean.setBeneBankCountry(currentCell.getStringCellValue());
							break;
						case 21:
							currentCell.setCellType(CellType.STRING);
							bean.setBeneBankName(currentCell.getStringCellValue());
							break;
						case 22:
							currentCell.setCellType(CellType.STRING);
							bean.setBeneSwiftCode(currentCell.getStringCellValue());
							break;
						case 23:
							currentCell.setCellType(CellType.STRING);
							bean.setBeneCountry(currentCell.getStringCellValue());
							break;
						case 24:
							currentCell.setCellType(CellType.STRING);
							bean.setLoadingCountry(currentCell.getStringCellValue());
							break;
						case 25:
							currentCell.setCellType(CellType.STRING);
							bean.setLoadingPort(currentCell.getStringCellValue());
							break;
						case 26:
							currentCell.setCellType(CellType.STRING);
							bean.setDischargeCountry(currentCell.getStringCellValue());
							break;
						case 27:
							currentCell.setCellType(CellType.STRING);
							bean.setDischargePort(currentCell.getStringCellValue());
							break;
						case 28:
							currentCell.setCellType(CellType.STRING);
							bean.setChargesType(currentCell.getStringCellValue());
							break;
						case 29:
							bean.setValidity(currentCell.getDateCellValue());
							break;
						case 30:
							currentCell.setCellType(CellType.STRING);
							bean.setIsESGComplaint(currentCell.getStringCellValue());
							break;
						case 31:
							bean.setUserType(currentCell.getStringCellValue());
							break;
						case 32:
							currentCell.setCellType(CellType.STRING);
							bean.setBeneContactPerson(currentCell.getStringCellValue());
							break;
						case 33:
							currentCell.setCellType(CellType.STRING);
							bean.setBeneContactPersonEmail(currentCell.getStringCellValue());
							break;
						case 34:
							currentCell.setCellType(CellType.STRING);
							bean.setApplicantContactPerson(currentCell.getStringCellValue());
							break;
						case 35:
							currentCell.setCellType(CellType.STRING);
							bean.setApplicantContactPersonEmail(currentCell.getStringCellValue());
							break;
						case 36:
							currentCell.setCellType(CellType.STRING);
							bean.setDiscountingPeriod(currentCell.getStringCellValue());
							break;
						case 37:
							currentCell.setCellType(CellType.STRING);
							bean.setLastBankCountry(currentCell.getStringCellValue());
							break;
						case 38:
							currentCell.setCellType(CellType.STRING);
							bean.setLastBeneBank(currentCell.getStringCellValue());
							break;
						case 39:
							currentCell.setCellType(CellType.STRING);
							bean.setLastBeneSwiftCode(currentCell.getStringCellValue());
							break;
						case 40:
							bean.setLcMaturityDate(currentCell.getDateCellValue());
							break;
						case 41:
							currentCell.setCellType(CellType.STRING);
							bean.setLcNumber(currentCell.getStringCellValue());
							break;
						case 42:
							currentCell.setCellType(CellType.STRING);
							bean.setOriginalTenorDays(Integer.parseInt(currentCell.getStringCellValue().isEmpty() ? "0"
									: currentCell.getStringCellValue()));
							break;
						case 43:
							currentCell.setCellType(CellType.STRING);
							bean.setRefinancingPeriod(currentCell.getStringCellValue());
							break;
						case 44:
							bean.setlCExpiryDate(currentCell.getDateCellValue());
							break;
						case 45:
							bean.setClaimExpiryDate(currentCell.getDateCellValue());
							break;
						case 46:
							currentCell.setCellType(CellType.STRING);
							bean.setBgType(currentCell.getStringCellValue());
							break;
						case 47:
							currentCell.setCellType(CellType.STRING);
							bean.setBillType(currentCell.getStringCellValue());
							break;
						default:
							break;
						}
						cellIdx++;
					} catch (Exception e) {
						System.out.println("=======================condition 4"+currentCell.getStringCellValue());
						System.out.println("=======================condition 5"+cellIdx);
						isEligibleForPersist = false;
						exceptionMessage = e.getMessage();
					}
				}
				Map<String, String> errorMap = validateBean(bean, cellIdx, exceptionMessage);
				if (errorMap.isEmpty()) {
					System.out.println("=======================++++++++++condition 6"+cellIdx);
					System.out.println("=======================++++++++++errorMap 6");
					beanList.add(bean);
				} else {
					System.out.println("=======================rownumber 7"+rowNumber);
					System.out.println("=======================condition 7"+cellIdx);
					Cell cell = currentRow.createCell(49, CellType.STRING);
					cell.setCellValue(errorMap.toString());
					System.out.println("=======================condition 8"+errorMap.toString());
					isEligibleForPersist = false;
				}
			}
			persistTransaction(beanList, isEligibleForPersist);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	private void persistTransaction(List<NimaiLCBean> beanList, Boolean isEligibleForPersist) {
		System.out.println("=======================persistTransaction condition 7"+isEligibleForPersist);
		List<NimaiLCBean> saveBeanList = new ArrayList<>();
		boolean isErrorReceived = false;
		if (isEligibleForPersist) {
			try {
				for (NimaiLCBean bean : beanList) {
					bean.setInsertedBy(bean.getUserId());
					bean.setInsertedDate(new Date());
					bean.setModifiedBy(bean.getUserId());
					bean.setModifiedDate(new Date());
					bean.setLcProForma("");
					// for discounting
					bean.setRemarks("");
					String tid = generateTransactionId(bean.getUserId(), bean.getRequirementType(),
							bean.getlCIssuanceCountry());
					System.out.println("$$$$$$$$ "+bean.getlCExpiryDate());
					lcservice.saveLCdetailsTemp(bean, tid);
					bean.setTransactionId(tid);
					System.out.println("##############################################");
					System.out.println("TransactionId: "+bean.getTransactionId()+" Index: "+bean.getIndex());
					NimaiClient userDetails = cuRepo.getOne(bean.getUserId());
					NimaiClient obtainUserId = lcservice.checkMasterSubsidiary(userDetails.getAccountType(),
							bean.getUserId(), userDetails);
					if (obtainUserId != null) {
						String subsStatus = lcmasterrepo.findActivePlanByUserId(obtainUserId.getUserid());
						if (subsStatus.equalsIgnoreCase("Active"))
							lcservice.saveTempLc(obtainUserId, bean);
						else
							lcservice.saveTempLc(obtainUserId, bean);
						saveBeanList.add(bean);
					}
				}
				System.out.println("+++++++++++++=================savebean list size"+saveBeanList.size());
			} catch (Exception e) {
				e.printStackTrace();
				isErrorReceived = true;
			}
			if (isErrorReceived) {
				for (NimaiLCBean lcBean : saveBeanList) {
					lcservice.updateTransactionForCancel(lcBean.getTransactionId(), lcBean.getUserId());
				}
			}

		}
	}

	private Map<String, String> validateBean(NimaiLCBean bean, int cellindex, String message) {
		System.out.println("=======================validateBean condition 8"+cellindex);
		Map<String, String> errorMap = new HashMap<>();
		List<String> requirementTypeList = Arrays.asList("Confirmation", "Discounting", "ConfirmAndDiscount",
				"Refinance", "Banker", "BillAvalisation", "BankGuarantee");
		List<String> commonValidation = Arrays.asList("Confirmation", "Discounting", "ConfirmAndDiscount", "Banker");
		try {

			if (!message.isEmpty() || !message.equalsIgnoreCase(" ")) {
				errorMap.put("exception occurs", message);
			}

			if (cellindex != 48) {
				errorMap.put("48 column needed", "Please enter minimum 48 column.");
			}

			if (bean.getUserType() == null || bean.getUserType().length()==0) {
				errorMap.put("UserType", "UserType cannot be null or empty.");
			}

			if (bean.getRequirementType() == null || bean.getRequirementType().length()==0) {
				errorMap.put("RequirementType", "RequirementType cannot be null or empty.");
			}

			if (!requirementTypeList.contains(bean.getRequirementType())) {
				errorMap.put("RequirementType", "Please provide vaild RequirementType.");
			}

			if (bean.getBranchUserEmail() == null || bean.getBranchUserEmail().length()==0) {
				errorMap.put("BranchUserEmail", "BranchUserEmail cannot be null or empty.");
			}

			if (bean.getStartDate() == null) {
				errorMap.put("StartDate", "StartDate cannot be null or empty.");
			}

			if (bean.getUserId().contains("BC")) {

				if (bean.getBeneName() == null || bean.getBeneName().length()==0) {
					errorMap.put("BeneName", "BeneName cannot be null or empty.");
				}

				if (bean.getBeneCountry() == null || bean.getBeneCountry().length()==0) {
					errorMap.put("BeneCountry", "BeneCountry cannot be null or empty.");
				}

				if (bean.getBeneContactPerson() == null || bean.getBeneContactPerson().length()==0) {
					errorMap.put("BeneContactPerson", "BeneContactPerson be null or empty.");
				}

				if (bean.getBeneContactPersonEmail() == null || bean.getBeneContactPersonEmail().length()==0) {
					errorMap.put("BeneContactPersonEmail", "BeneContactPersonEmail email cannot be null or empty.");
				}

				if (bean.getApplicantName() == null || bean.getApplicantName().length()==0) {
					errorMap.put("ApplicantName", "ApplicantName cannot be null or empty.");
				}

				if (bean.getApplicantCountry() == null || bean.getApplicantCountry().length()==0) {
					errorMap.put("ApplicantCountry", "ApplicantCountry cannot be null or empty.");
				}

				if (bean.getApplicantContactPerson() == null || bean.getApplicantContactPerson().length()==0) {
					errorMap.put("ApplicantContactPerson", "ApplicantContactPerson cannot be null or empty.");
				}

				if (bean.getApplicantContactPersonEmail() == null || bean.getApplicantContactPersonEmail().length()==0) {
					errorMap.put("ApplicantContactPersonEmail",
							"Applicant contact person email cannot be null or empty.");
				}
			}

			if (bean.getUserType() != null) {
				if ("Applicant".equalsIgnoreCase(bean.getUserType())) {

					if (bean.getBeneName() == null || bean.getBeneName().length()==0) {
						errorMap.put("BeneName", "BeneName cannot be null or empty.");
					}

					if (bean.getBeneCountry() == null || bean.getBeneCountry().length()==0) {
						errorMap.put("BeneCountry", "BeneCountry cannot be null or empty.");
					}

					if (bean.getBeneContactPerson() == null || bean.getBeneContactPerson().length()==0) {
						errorMap.put("BeneContactPerson", "BeneContactPerson be null or empty.");
					}

					if (bean.getBeneContactPersonEmail() == null || bean.getBeneContactPersonEmail().length()==0) {
						errorMap.put("BeneContactPersonEmail", "BeneContactPersonEmail email cannot be null or empty.");
					}
				}

				if ("Beneficiary".equalsIgnoreCase(bean.getUserType())) {

					if (bean.getApplicantName() == null || bean.getApplicantName().length()==0) {
						errorMap.put("ApplicantName", "ApplicantName cannot be null or empty.");
					}

					if (bean.getApplicantCountry() == null || bean.getApplicantCountry().equalsIgnoreCase(" ") || bean.getApplicantCountry().length()==0) {
						errorMap.put("ApplicantCountry", "ApplicantCountry cannot be null or empty.");
					}

					if (bean.getApplicantContactPerson() == null || bean.getApplicantContactPerson().length()==0) {
						errorMap.put("ApplicantContactPerson", "ApplicantContactPerson cannot be null or empty.");
					}

					if (bean.getApplicantContactPersonEmail() == null
							|| bean.getApplicantContactPersonEmail().length()==0) {
						errorMap.put("ApplicantContactPersonEmail",
								"Applicant contact person email cannot be null or empty.");
					}
				}
			}

			if (bean.getValidity() == null) {
				errorMap.put("Validity", "Validity cannot be null or empty.");
			}

			if (bean.getBeneBankCountry() == null || bean.getBeneBankCountry().length()==0) {
				errorMap.put("BeneBankCountry", "BeneBankCountry cannot be null or empty.");
			}

			if (bean.getBeneBankName() == null || bean.getBeneBankName().length()==0) {
				errorMap.put("BeneBankName", "BeneBankName cannot be null or empty.");
			}

			if (bean.getBeneSwiftCode() == null || bean.getBeneSwiftCode().length()==0) {
				errorMap.put("BeneSwiftCode", "BeneSwiftCode cannot be null or empty.");
			}

			if (bean.getlCIssuanceBranch() == null || bean.getlCIssuanceBranch().length()==0) {
				errorMap.put("lCIssuanceBranch", "lCIssuanceBranch cannot be null or empty.");
			}

			if (bean.getlCIssuanceBank() == null || bean.getlCIssuanceBank().length()==0) {
				errorMap.put("lCIssuanceBank", "lCIssuanceBank cannot be null or empty.");
			}

			if (bean.getlCIssuanceCountry() == null || bean.getlCIssuanceCountry().length()==0) {
				errorMap.put("lCIssuanceCountry", "lCIssuanceCountry cannot be null or empty.");
			}

			if (bean.getSwiftCode() == null || bean.getSwiftCode().length()==0) {
				errorMap.put("SwiftCode", "SwiftCode cannot be null or empty.");
			}

			if (commonValidation.contains(bean.getRequirementType())) {

				if (bean.getlCValue() == null) {
					errorMap.put("lCValue", "lCValue cannot be null or empty.");
				}

				if (bean.getlCCurrency() == null || bean.getlCCurrency().length()==0) {
					errorMap.put("lCCurrency", "lCCurrency cannot be null or empty.");
				}

				if (bean.getlCIssuingDate() == null) {
					errorMap.put("lCIssuingDate", "lCIssuingDate cannot be null or empty.");
				}

				if (bean.getNegotiationDate() == null) {
					errorMap.put("NegotiationDate", "NegotiationDate cannot be null or empty.");
				}

				if (bean.getLastShipmentDate() == null) {
					errorMap.put("LastShipmentDate", "LastShipmentDate cannot be null or empty.");
				}
			}

			if ("BillAvalisation".equalsIgnoreCase(bean.getRequirementType())) {

				if (bean.getlCValue() == null) {
					errorMap.put("lCValue", "lCValue cannot be null or empty.");
				}

				if (bean.getlCCurrency() == null || bean.getlCCurrency().length()==0) {
					errorMap.put("lCCurrency", "lCCurrency cannot be null or empty.");
				}

				if (bean.getlCIssuingDate() == null) {
					errorMap.put("lCIssuingDate", "lCIssuingDate cannot be null or empty.");
				}

				if (bean.getLcMaturityDate() == null) {
					errorMap.put("LcMaturityDate", "LcMaturityDate cannot be null or empty.");
				}

				if (bean.getLastShipmentDate() == null) {
					errorMap.put("LastShipmentDate", "LastShipmentDate cannot be null or empty.");
				}

				if (bean.getBillType() == null) {
					errorMap.put("BillType", "BillType cannot be null or empty.");
				}
			}

			if ("Refinance".equalsIgnoreCase(bean.getRequirementType())) {

				if (bean.getlCValue() == null) {
					errorMap.put("lCValue", "lCValue cannot be null or empty.");
				}

				if (bean.getlCCurrency() == null || bean.getlCCurrency().length()==0) {
					errorMap.put("lCCurrency", "lCCurrency cannot be null or empty.");
				}

				if (bean.getlCIssuingDate() == null) {
					errorMap.put("lCIssuingDate", "lCIssuingDate cannot be null or empty.");
				}
			}

			if ("BankGuarantee".equalsIgnoreCase(bean.getRequirementType())) {
				if (bean.getlCValue() == null) {
					errorMap.put("lCValue", "lCValue cannot be null or empty.");
				}

				if (bean.getlCCurrency() == null || bean.getlCCurrency().length()==0) {
					errorMap.put("lCCurrency", "lCCurrency cannot be null or empty.");
				}

				if (bean.getlCIssuingDate() == null) {
					errorMap.put("lCIssuingDate", "lCIssuingDate cannot be null or empty.");
				}

				if (bean.getClaimExpiryDate() == null) {
					errorMap.put("ClaimExpiryDate", "ClaimExpiryDate cannot be null or empty.");
				}

				if (bean.getlCExpiryDate() == null) {
					errorMap.put("BgExpiryDate", "BgExpiryDate cannot be null or empty.");
				}

				if (bean.getBgType() == null || bean.getBgType().equalsIgnoreCase("null")) {
					errorMap.put("BgType", "BgType cannot be null or empty.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMap.put("Internal Server Error", e.getMessage());
		}
		return errorMap;
	}
	
	
	private String generateTransactionId(String userid, String transType, String countryName) {
		// TODO Auto-generated method stub

		StringBuffer newtransactionId = new StringBuffer();
		newtransactionId.append(userid.substring(0, 2));
		newtransactionId.append(lcservice.generateYear());
		newtransactionId.append(lcservice.generateCountryCode(countryName));
		newtransactionId.append(lcservice.generateTransactionType(transType));
		newtransactionId.append(lcservice.generateSerialNo());

		System.out.println(" TRANSACTION ID :::::::::::: " + newtransactionId.toString());
		return newtransactionId.toString();

	}
}
