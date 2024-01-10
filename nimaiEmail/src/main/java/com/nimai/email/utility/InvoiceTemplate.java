package com.nimai.email.utility;

import com.itextpdf.html2pdf.ConverterProperties;

import com.itextpdf.html2pdf.HtmlConverter;
import com.nimai.email.bean.InvoiceBeanResponse;
import com.nimai.email.entity.NimaiClient;
import com.nimai.email.entity.NimaiEmailScheduler;
import com.nimai.email.entity.NimaiPostpaidSubscriptionDetails;
import com.nimai.email.entity.NimaiSubscriptionDetails;
import com.nimai.email.entity.NimaiSubscriptionVas;
import com.nimai.email.entity.NimaiSystemConfig;
import com.nimai.email.entity.OnlinePayment;
import com.nimai.email.repository.nimaiSystemConfigRepository;
import com.nimai.email.service.UserService;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InvoiceTemplate {
	private static Logger logger = LoggerFactory.getLogger(InvoiceTemplate.class);

	@Value("${invoice.location}")
	private String htmInvoicePath;

	@Value("${invoicePdf.location}")
	private String pdfInvoicePath;

	@Autowired
	UserService userEmailService;

	@Autowired
	nimaiSystemConfigRepository systemConfig;

	Utils util = new Utils();

	String pdfLocation = "D:\\Softwares\\apache-tomcat-8.5.43\\webapps\\nimaiEmail\\WEB-INF\\classes\\InvoicePdf";

	public <T> byte[] generateSplanInvoiceTemplatePdf(NimaiSubscriptionDetails subscriptionDetails,
			OnlinePayment paymentDetails, NimaiSystemConfig configDetails2, String imagePath) {
		System.out.println("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + subscriptionDetails);
		String invoiceName = subscriptionDetails.getSubscriptionId() + ".htm";
		String pattern = "MM/dd/yyyy";
		Date dnow = new Date();
		String date = (new SimpleDateFormat("dd/MM/yyyy")).format(dnow);
		String subscriptionPlanAmount = String.valueOf(subscriptionDetails.getSubscriptionAmount());
		if (subscriptionDetails.getDiscount() == null)
			subscriptionDetails.setDiscount(Double.valueOf(0.0D));
		String gst = configDetails2.getSystemEntityValue();
		String invoiceVas = "";
		String invoiceDiscount = "";
		String invoiceSubscription = "";
		String toalAmount = "";
		String calculatedgstValue = "";
		String referenceNumber = "";
		if (subscriptionDetails.getGrandAmount().doubleValue() == 0.0D) {
			if (subscriptionDetails.getPaymentMode().equalsIgnoreCase("Wire")) {
				referenceNumber = "";
			} else {
				referenceNumber = "";
			}
		} else if (subscriptionDetails.getPaymentMode().equalsIgnoreCase("Wire")) {
			referenceNumber = subscriptionDetails.getPaymentTrId();
		} else {
			referenceNumber = paymentDetails.getOrderId();
		}
		logger.info("=====================calculatedgstValue" + calculatedgstValue);
		String granTotal = String.valueOf(subscriptionDetails.getGrandAmount());
		if (subscriptionDetails.getVasAmount() == 0 && subscriptionDetails.getDiscount().doubleValue() == 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			logger.info("===============first condition=================");
			toalAmount = String.valueOf(subscriptionDetails.getSubscriptionAmount());
			calculatedgstValue = String.valueOf(this.util.GstValue(subscriptionDetails.getSubscriptionAmount(),
					configDetails2.getSystemEntityValue()));
		} else if (subscriptionDetails.getVasAmount() != 0 && subscriptionDetails.getDiscount().doubleValue() == 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			logger.info("===============second condition=================");
			Double vasPlusSPlan = Double
					.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount()));
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			System.out.println("===============second condition=================" + vasPlusSPlan);
		} else if (subscriptionDetails.getVasAmount() != 0 && subscriptionDetails.getDiscount().doubleValue() != 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			Double vasPlusSPlan = Double
					.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount())
							- subscriptionDetails.getDiscount().doubleValue());
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			logger.info("===============third condition=================" + vasPlusSPlan);
		} else if (subscriptionDetails.getVasAmount() == 0 && subscriptionDetails.getDiscount().doubleValue() != 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			Double vasPlusSPlan = Double.valueOf(
					subscriptionDetails.getSubscriptionAmount() - subscriptionDetails.getDiscount().doubleValue());
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			logger.info("===============fourth condition=================" + vasPlusSPlan);
		}
		AmountToWords words = new AmountToWords();
		String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
		String invoiceHeader = getInvoiceHeader(subscriptionDetails, date, referenceNumber, imagePath);
		if (subscriptionDetails.getVasAmount() != 0)
			invoiceVas = getInvoiceVas(subscriptionDetails);
		if (subscriptionDetails.getDiscount().doubleValue() != 0.0D) {
			String srNummber = "";
			if (subscriptionDetails.getVasAmount() != 0) {
				srNummber = "3";
			} else {
				srNummber = "2";
			}
			invoiceDiscount = getInvoiceDiscount(subscriptionDetails, srNummber);
			Double double_ = subscriptionDetails.getDiscount();
		}
		if (subscriptionDetails.getSubscriptionAmount() != 0)
			invoiceSubscription = getInvoiceSubscription(subscriptionDetails);
		String invoiceFooter = getInvoiceFooter(subscriptionDetails, granTotal, gst, calculatedgstValue, toalAmount,
				totalAmountInwords);
		try {
			logger.info("====================+++++++htmInvoicePath" + this.htmInvoicePath);
			File file = new File(this.htmInvoicePath + date + "\\" + invoiceName + ".pdf");
			String pdfPath = this.pdfLocation + "\\" + subscriptionDetails.getUserid().getUserid() + "_"
					+ subscriptionDetails.getSubscriptionId() + ".pdf";
			String DocpdfPath = this.pdfLocation + "\\" + subscriptionDetails.getUserid().getUserid() + "_"
					+ subscriptionDetails.getSubscriptionId() + ".docx";
			System.out.println("===========pdf path" + pdfPath);
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(invoiceHeader + invoiceSubscription + invoiceVas + invoiceDiscount + invoiceFooter);
			bw.close();
			String path = file.getAbsolutePath();
			HtmlConverter.convertToPdf(new FileInputStream(file), new FileOutputStream(pdfPath));
			ByteArrayOutputStream target = new ByteArrayOutputStream();
			ConverterProperties converterProperties = new ConverterProperties();
			converterProperties.setBaseUri("http://136.232.244.190:8081");
			File yourOutputFile = new File(pdfPath);
			HtmlConverter.convertToPdf(new FileInputStream(file), target, converterProperties);
			byte[] bytes = target.toByteArray();
			System.out.println(bytes.toString());
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
			baos.write(bytes, 0, bytes.length);
			System.out.println("Inside the mthod of invoicetemplate" + baos.toByteArray());
			System.out.println("Inside the mthod of invoicetemplate" + baos.toString());
			System.out.println("pdfpath==============" + pdfPath);
			FileOutputStream fos = new FileOutputStream(yourOutputFile);
			fos.write(bytes);
			fos.close();
			System.out.println("Bytes od pdf file" + bytes);
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("============Inside generateSplanInvoiceTemplate class==========");
			logger.info(
					"===========================generateSplanInvoiceTemplate:-=======================================");
			return null;
		}
	}

	public InvoiceBeanResponse genStringSplanInvoiceTemplatePdf(NimaiSubscriptionDetails subscriptionDetails,
			OnlinePayment paymentDetails, NimaiSystemConfig configDetails2, String imagePath,
			NimaiSubscriptionVas vasDetails) {
		System.out.println("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + subscriptionDetails);
		String invoiceName = subscriptionDetails.getSubscriptionId() + ".htm";
		String pattern = "MM/dd/yyyy";
		Date dnow = new Date();
		String date = (new SimpleDateFormat("dd/MM/yyyy")).format(subscriptionDetails.getSubscriptionStartDate());
		String subscriptionPlanAmount = String.valueOf(subscriptionDetails.getSubscriptionAmount());
		if (subscriptionDetails.getDiscount() == null)
			subscriptionDetails.setDiscount(Double.valueOf(0.0D));
		String gst = configDetails2.getSystemEntityValue();
		String invoiceVas = "";
		String invoiceDiscount = "";
		String invoiceSubscription = "";
		String toalAmount = "";
		String calculatedgstValue = "";
		String referenceNumber = "";
		if (subscriptionDetails.getGrandAmount().doubleValue() == 0.0D) {
			if (subscriptionDetails.getPaymentMode().equalsIgnoreCase("Wire")) {
				referenceNumber = "";
			} else {
				referenceNumber = "";
			}
		} else if (subscriptionDetails.getPaymentMode().equalsIgnoreCase("Wire")) {
			referenceNumber = subscriptionDetails.getPaymentTrId();
		} else {
			referenceNumber = paymentDetails.getOrderId();
		}
		logger.info("=====================calculatedgstValue" + calculatedgstValue);
		String granTotal = String.valueOf(subscriptionDetails.getGrandAmount());
		if (subscriptionDetails.getVasAmount() == 0 && subscriptionDetails.getDiscount().doubleValue() == 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			logger.info("===============first condition=================");
			toalAmount = String.valueOf(subscriptionDetails.getSubscriptionAmount());
			calculatedgstValue = String.valueOf(this.util.GstValue(subscriptionDetails.getSubscriptionAmount(),
					configDetails2.getSystemEntityValue()));
		} else if (subscriptionDetails.getVasAmount() != 0 && subscriptionDetails.getDiscount().doubleValue() == 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			Double vasPlusSPlan;
			logger.info("===============second condition=================");
			if (vasDetails == null) {
				vasPlusSPlan = Double
						.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount()));
				toalAmount = String.valueOf(vasPlusSPlan);
				calculatedgstValue = String
						.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			} else if (vasDetails.getSplanVasFlag() == 0) {
				vasPlusSPlan = Double.valueOf((0 + subscriptionDetails.getSubscriptionAmount()));
				toalAmount = String.valueOf(vasPlusSPlan);
				calculatedgstValue = String
						.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			} else {
				vasPlusSPlan = Double
						.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount()));
				toalAmount = String.valueOf(vasPlusSPlan);
				calculatedgstValue = String
						.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			}
			System.out.println("===============second condition=================" + vasPlusSPlan);
		} else if (subscriptionDetails.getVasAmount() != 0 && subscriptionDetails.getDiscount().doubleValue() != 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			Double vasPlusSPlan;
			if (vasDetails == null) {
				vasPlusSPlan = Double
						.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount())
								- subscriptionDetails.getDiscount().doubleValue());
				toalAmount = String.valueOf(vasPlusSPlan);
				calculatedgstValue = String
						.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			} else if (vasDetails.getSplanVasFlag() == 0) {
				vasPlusSPlan = Double.valueOf((0 + subscriptionDetails.getSubscriptionAmount())
						- subscriptionDetails.getDiscount().doubleValue());
				toalAmount = String.valueOf(vasPlusSPlan);
				calculatedgstValue = String
						.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			} else {
				vasPlusSPlan = Double
						.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount())
								- subscriptionDetails.getDiscount().doubleValue());
				toalAmount = String.valueOf(vasPlusSPlan);
				calculatedgstValue = String
						.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			}
			logger.info("===============third condition=================" + vasPlusSPlan);
		} else if (subscriptionDetails.getVasAmount() == 0 && subscriptionDetails.getDiscount().doubleValue() != 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			Double vasPlusSPlan = Double.valueOf(
					subscriptionDetails.getSubscriptionAmount() - subscriptionDetails.getDiscount().doubleValue());
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			logger.info("===============fourth condition=================" + vasPlusSPlan);
		}
		AmountToWords words = new AmountToWords();
		String invoiceHeader = getInvoiceHeader(subscriptionDetails, date, referenceNumber, imagePath);
		if (subscriptionDetails.getVasAmount() != 0)
			invoiceVas = getInvoiceVas(subscriptionDetails);
		if (subscriptionDetails.getDiscount().doubleValue() != 0.0D) {
			String srNummber = "";
			if (subscriptionDetails.getVasAmount() != 0) {
				srNummber = "3";
			} else {
				srNummber = "2";
			}
			invoiceDiscount = getInvoiceDiscount(subscriptionDetails, srNummber);
			Double double_ = subscriptionDetails.getDiscount();
		}
		if (subscriptionDetails.getSubscriptionAmount() != 0)
			invoiceSubscription = getInvoiceSubscription(subscriptionDetails);
		try {
			InvoiceBeanResponse beanResponse = new InvoiceBeanResponse();
			beanResponse.setCustomerId(subscriptionDetails.getUserid().getUserid());
			beanResponse.setInvoiceNumber(subscriptionDetails.getInvoiceId());
			beanResponse.setContactPersonName(subscriptionDetails.getUserid().getFirstName() + " "
					+ subscriptionDetails.getUserid().getLastName());
			beanResponse.setInvoiceDate(date);
			beanResponse.setCountry(subscriptionDetails.getUserid().getCountryName());
			beanResponse.setReferrenceNumber(referenceNumber);
			beanResponse.setCompanyName(subscriptionDetails.getUserid().getCompanyName());
			beanResponse.setCalculatedGstValue(calculatedgstValue);
			beanResponse.setTotalAmount(toalAmount);
			beanResponse.setGst(gst);
			beanResponse.setsPlanAmount(String.valueOf(subscriptionDetails.getSubscriptionAmount()));
			if (vasDetails == null) {
				beanResponse.setVasAmount(String.valueOf(subscriptionDetails.getVasAmount()));
				beanResponse.setVasDiscount(String.valueOf(
						Double.parseDouble((new DecimalFormat("##.##")).format(subscriptionDetails.getDiscount()))));
				beanResponse.setGrandTotal(granTotal);
				String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
				beanResponse.setAmountInWords(totalAmountInwords);
			} else if (vasDetails.getSplanVasFlag() == 0) {
				beanResponse.setVasAmount(String.valueOf(0));
				beanResponse
						.setVasDiscount(String.valueOf(Double.parseDouble((new DecimalFormat("##.##")).format(0.0D))));
				beanResponse.setGrandTotal(String.valueOf(toalAmount));
				String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(toalAmount).doubleValue());
				beanResponse.setAmountInWords(totalAmountInwords);
			} else {
				beanResponse.setVasAmount(String.valueOf(subscriptionDetails.getVasAmount()));
				beanResponse.setVasDiscount(String.valueOf(
						Double.parseDouble((new DecimalFormat("##.##")).format(subscriptionDetails.getDiscount()))));
				beanResponse.setGrandTotal(granTotal);
				String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
				beanResponse.setAmountInWords(totalAmountInwords);
			}
			return beanResponse;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("============Inside generateSplanInvoiceTemplate class==========");
			logger.info(
					"===========================generateSplanInvoiceTemplate:-=========================S==============");
			return null;
		}
	}

	public String generateSplanInvoiceTemplate(NimaiSubscriptionDetails subscriptionDetails,
			OnlinePayment paymentDetails, NimaiSystemConfig configDetails2, String imagePath) {
		System.out.println("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + subscriptionDetails);
		String invoiceName = subscriptionDetails.getSubscriptionId() + ".htm";
		String pattern = "MM/dd/yyyy";
		Date dnow = new Date();
		String date = (new SimpleDateFormat("dd/MM/yyyy")).format(dnow);
		String subscriptionPlanAmount = String.valueOf(subscriptionDetails.getSubscriptionAmount());
		if (subscriptionDetails.getDiscount() == null)
			subscriptionDetails.setDiscount(Double.valueOf(0.0D));
		String gst = configDetails2.getSystemEntityValue();
		String invoiceVas = "";
		String invoiceDiscount = "";
		String invoiceSubscription = "";
		String toalAmount = "";
		String calculatedgstValue = "";
		String referenceNumber = "";

		if (subscriptionDetails.getGrandAmount().doubleValue() == 0.0D) {
			if (subscriptionDetails.getPaymentMode().equalsIgnoreCase("Wire")) {
				referenceNumber = "";
			} else {
				referenceNumber = "";
			}
		} else if (subscriptionDetails.getPaymentMode().equalsIgnoreCase("Wire")) {
			referenceNumber = subscriptionDetails.getPaymentTrId();
		} else {
			referenceNumber = paymentDetails.getOrderId();
		}
		logger.info("=====================calculatedgstValue" + calculatedgstValue);
		String granTotal = String.valueOf(subscriptionDetails.getGrandAmount());
		if (subscriptionDetails.getVasAmount() == 0 && subscriptionDetails.getDiscount().doubleValue() == 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			logger.info("===============first condition=================");
			toalAmount = String.valueOf(subscriptionDetails.getSubscriptionAmount());
			calculatedgstValue = String.valueOf(this.util.GstValue(subscriptionDetails.getSubscriptionAmount(),
					configDetails2.getSystemEntityValue()));
		} else if (subscriptionDetails.getVasAmount() != 0 && subscriptionDetails.getDiscount().doubleValue() == 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			logger.info("===============second condition=================");
			Double vasPlusSPlan = Double
					.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount()));
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			System.out.println("===============second condition=================" + vasPlusSPlan);
		} else if (subscriptionDetails.getVasAmount() != 0 && subscriptionDetails.getDiscount().doubleValue() != 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			Double vasPlusSPlan = Double
					.valueOf((subscriptionDetails.getVasAmount() + subscriptionDetails.getSubscriptionAmount())
							- subscriptionDetails.getDiscount().doubleValue());
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			logger.info("===============third condition=================" + vasPlusSPlan);
		} else if (subscriptionDetails.getVasAmount() == 0 && subscriptionDetails.getDiscount().doubleValue() != 0.0D
				&& subscriptionDetails.getSubscriptionAmount() != 0) {
			Double vasPlusSPlan = Double.valueOf(
					subscriptionDetails.getSubscriptionAmount() - subscriptionDetails.getDiscount().doubleValue());
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan.doubleValue(), configDetails2.getSystemEntityValue()));
			logger.info("===============fourth condition=================" + vasPlusSPlan);
		}
		AmountToWords words = new AmountToWords();
		String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
		String invoiceHeader = getInvoiceHeader(subscriptionDetails, date, referenceNumber, imagePath);
		if (subscriptionDetails.getVasAmount() != 0)
			invoiceVas = getInvoiceVas(subscriptionDetails);
		if (subscriptionDetails.getDiscount().doubleValue() != 0.0D) {
			String srNummber = "";
			if (subscriptionDetails.getVasAmount() != 0) {
				srNummber = "3";
			} else {
				srNummber = "2";
			}
			invoiceDiscount = getInvoiceDiscount(subscriptionDetails, srNummber);
			Double double_ = subscriptionDetails.getDiscount();
		}
		if (subscriptionDetails.getSubscriptionAmount() != 0)
			invoiceSubscription = getInvoiceSubscription(subscriptionDetails);
		String invoiceFooter = getInvoiceFooter(subscriptionDetails, granTotal, gst, calculatedgstValue, toalAmount,
				totalAmountInwords);
		try {
			logger.info("====================+++++++htmInvoicePath" + this.htmInvoicePath);
			File file = new File(this.htmInvoicePath + date + "\\" + invoiceName);
			String pdfPath = subscriptionDetails.getUserid().getUserid() + "_" + subscriptionDetails.getSubscriptionId()
					+ ".pdf";
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			bw.write(invoiceHeader + invoiceSubscription + invoiceVas + invoiceDiscount + invoiceFooter);
			bw.close();
			String path = file.getAbsolutePath();
			HtmlConverter.convertToPdf(new FileInputStream(file), new FileOutputStream(pdfPath));
			
			
			return pdfPath;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("============Inside generateSplanInvoiceTemplate class==========");
			logger.info(
					"===========================generateSplanInvoiceTemplate:-=======================================");
			return null;
		}
	}

	String devImage = "file:///D:/Softwares/apache-tomcat-8.5.43/webapps/nimaiEmail/WEB-INF/classes/images/360TF-3.jpg";

	String localImage = "file:///D:/aadil/6thJan/nimaiEmail%20(2)/nimaiEmail/src/main/resources/images/360TF-3.jpg";

	String uatImage = "file:///usr/java/apache-tomcat-9.0.41/webapps/nimaiEmail/WEB-INF/classes/images/360TF-3.jpg";

	private String getInvoiceHeader(NimaiSubscriptionDetails subscriptionDetails, String date, String referenceNumber,
			String imagePath) {
		String invoiceNumber = "";
		invoiceNumber = subscriptionDetails.getInvoiceId();
		String header = "<!doctype html>\r\n<html>\r\n   <head>\r\n      <meta charset=\"utf-8\">\r\n      <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n      <title>A simple, clean, and responsive HTML invoice template</title> \r\n   </head>\r\n   <body> \r\n      <div class=\"invoice-box\" style=\"max-width:800px;\r\n         margin:auto;\r\n         padding:30px;\r\n         border:1px solid #eee;\r\n         box-shadow:0 0 10px rgba(0, 0, 0, .15);\r\n         font-size:16px;\r\n         line-height:24px;\r\n         font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;\r\n         color:#555;\">\r\n         <table cellpadding=\"0\" cellspacing=\"0\" style=\" font-family:'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;font-size: 15px;\r\n         line-height:inherit;\r\n         text-align:left;\">\r\n            <tr>\r\n               <td colspan=\"2\" align=\"right\" style=\"padding:5px;vertical-align:top;\">\r\n                  <img src="
				+ imagePath
				+ " width=\"200\">\r\n               </td>\r\n            </tr>\r\n             <tr><td colspan=\"2\" style=\"height: 10px;\"></td></tr>\r\n            <tr>\r\n               <td colspan=\"2\" style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;font-size: 18px;font-weight: 600;color: #000;\r\n         text-align:center;\">\r\n                  INVOICE\r\n               </td>\r\n            </tr> \r\n\r\n             <tr><td colspan=\"2\" style=\"height: 20px;\"></td></tr>\r\n            <tr>\r\n               <td style=\"padding:5px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Customer ID :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subscriptionDetails.getUserid().getUserid()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ invoiceNumber
				+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\" nowrap=\"\">\r\n                           Contact Person Name :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subscriptionDetails.getUserid().getFirstName() + " " + subscriptionDetails.getUserid().getLastName()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice Date :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ date
				+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Country : \r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subscriptionDetails.getUserid().getCountryName()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Reference No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ referenceNumber
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n       <tr>\r\n                        <th colspan=\"1\" align=\"left\" style=\"text-align: left;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        <i>Company Name: </i></th>\r\n    <td colspan=\"4\" style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subscriptionDetails.getUserid().getCompanyName()
				+ "</i>\r\n                        </td>\r\n                     </tr> +   <tr>\r\n                        <td colspan=\"4\" style=\"vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;height: 35px;\"></td>\r\n                     </tr>\r\n                  </table>\r\n               </td>\r\n            </tr>\r\n            <tr>\r\n               <td style=\"padding:0px 5px;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                           SEQ # \r\n                        </th> \r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        DESCRIPTION</th>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        AMOUNT US$</th> \r\n                     </tr>\r\n";
		return header;
	}

	private String getInvoiceFooter(NimaiSubscriptionDetails subscriptionDetails, String granTotal, String gst,
			String calculatedgstValue, String toalAmountWithGst, String totalAmountInwords) {
		String footer = "\r\n\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\">\r\n                           <strong>Total Amount</strong>\r\n                        </td> \r\n                        <td style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ toalAmountWithGst
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n\r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\">\r\n                           <strong>GST</strong><i>&#x00040;"
				+ gst
				+ "%</i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ calculatedgstValue
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                           \r\n                        </th> \r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        Grand Total</th>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        <i>"
				+ granTotal
				+ "</i>\r\n                     </th> \r\n                     </tr>\r\n                  </table>\r\n               </td>\r\n            </tr>\r\n            <tr>\r\n               <td style=\"padding:0px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:0;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"text-align: left;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           AMOUNT IN WORDS (US$):\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ totalAmountInwords
				+ "</i>\r\n                        </td> \r\n                     </tr>\r\n                      \r\n                  </table>\r\n               </td>\r\n            </tr> \r\n            <tr>\r\n               <td style=\"padding:10px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:1px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th  style=\"border-right:1px dashed #ccc;text-align: left;padding:5px;vertical-align:top;border-bottom:0px dashed #ccc;border-left: 1px dashed #ccc;CU47552;background-color: #fff;color: #1f3864;\">\r\n                           NIMAI TRADE FINTECH PTE. LTD.<br />\r\n                           <i>UEN: 202101205M</i>\r\n                        </th> \r\n                     </tr>\r\n                       <tr><td style=\"border-right:1px dashed #ccc;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: #1f3864;height:5px;\"></td></tr>\r\n                  </table>\r\n               </td>\r\n            </tr> \r\n            <tr>\r\n               <td style=\"padding:10px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:1px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"border-right:1px dashed #ccc;text-align: left;padding:5px;vertical-align:top;border-bottom:0px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: black;\">\r\n                           <strong style=\"color: #ed7d31;\">Registered Address</strong>:  <i>160, Robinson Road, #23-08 Singapore Business Federation Center, Singapore (068914)</i>\r\n                        </th> \r\n                     </tr>\r\n                       <tr><td style=\"border-right:1px dashed #ccc;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: #1f3864;height: 25px;\"></td></tr>\r\n                  </table>\r\n               </td>\r\n            </tr> \r\n             <tr><td colspan=\"2\" style=\"height: 10px;\"></td></tr>\r\n            <tr class=\"heading\">\r\n               <td style=\"padding:5px;vertical-align:top;font-size: 13px;\" align=\"center\" colspan=\"2\">\r\n                 <i>This is an electronically generated document and does not require physical signatures.</i>\r\n               </td> \r\n            </tr>\r\n\r\n             <tr><td colspan=\"2\" style=\"height: 1px;\"></td></tr>\r\n         </table>\r\n      </div> \r\n   </body>\r\n</html>";
		return footer;
	}

	private String getInvoiceSubscription(NimaiSubscriptionDetails subscriptionDetails) {
		String subscription = " <tr> \r\n                "
				+ "        <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;\">\r\n                           1\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;\">\r\n                           Subscription plan \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                             <i>"
				+ String.valueOf(subscriptionDetails.getSubscriptionAmount())
				+ "</i> </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
		return subscription;
	}

	private String getInvoiceVas(NimaiSubscriptionDetails subscriptionDetails) {
		String vas = "                     <tr> \r\n                        <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;height: 22px;\">\r\n                          2 \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;\"> \r\n                           <i>VAS Plan </i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ String.valueOf(subscriptionDetails.getVasAmount())
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
		return vas;
	}

	private String getpostInvoiceVas(NimaiEmailScheduler subscriptionDetails) {
		String vas = "                     <tr> \r\n                        <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;height: 22px;\">\r\n                          2 \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;\"> \r\n                           <i>VAS Plan </i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ String.valueOf(subscriptionDetails.getVasAmount())
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
		return vas;
	}

	private String getIndeInvoiceVas(NimaiSubscriptionVas vasDetails) {
		String vas = "                     <tr> \r\n                        <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;height: 22px;\">\r\n                          2 \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;\"> \r\n                           <i>VAS Plan </i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ String.valueOf(vasDetails.getPricing())
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
		return vas;
	}

	private String getInvoiceDiscount(NimaiSubscriptionDetails subscriptionDetails, String srNummber) {
		String discount = "                     <tr> \r\n                        <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;height: 22px;\">\r\n                         "
				+ srNummber
				+ "\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;\"> \r\n                           <i>Discount Coupon</i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ String.valueOf(
						Double.parseDouble((new DecimalFormat("##.##")).format(subscriptionDetails.getDiscount())))
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
		return discount;
	}

	private String getpostInvoiceDiscount(NimaiPostpaidSubscriptionDetails subscriptionDetails, String srNummber) {
		String discount = "                     <tr> \r\n                       "
				+ " <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;height: 22px;\">\r\n                         "
				+ srNummber
				+ "\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;\"> \r\n                           <i>Discount Coupon</i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ String.valueOf(
						Double.parseDouble((new DecimalFormat("##.##")).format(subscriptionDetails.getDiscountAmount())))
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
		return discount;
	}

	public String generateVasInvoiceTemplate(NimaiSubscriptionVas vasDetails, NimaiSubscriptionDetails subDetails,
			OnlinePayment paymentDetails, NimaiSystemConfig configDetails, String imagePath) {
		logger.info("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + vasDetails);
		String invoiceName = vasDetails.getId() + vasDetails.getSubscriptionId() + vasDetails.getVasId() + ".htm";
		String pattern = "MM/dd/yyyy";
		Date dnow = new Date();
		String date = (new SimpleDateFormat("dd/MM/yyyy")).format(dnow);
		String invoiceVas = "";
		String invoiceDiscount = "";
		String invoiceSubscription = "";
		String toalAmount = "";
		String calculatedgstValue = "";
		String referenceNumber = "";
		if (vasDetails.getMode().equalsIgnoreCase("Wire")) {
			referenceNumber = vasDetails.getPaymentTxnId();
		} else {
			referenceNumber = paymentDetails.getOrderId();
		}
		toalAmount = String.valueOf(subDetails.getVasAmount());
		String granTotal = String.valueOf(subDetails.getVasAmount());
		AmountToWords words = new AmountToWords();
		String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
		String gst = configDetails.getSystemEntityValue();
		String invoiceHeader = getVasInvoiceHeader(subDetails, date, referenceNumber, vasDetails, imagePath);
		if (subDetails.getVasAmount() != 0)
			invoiceVas = getVasDetails(subDetails);
		String invoiceFooter = getVasInvoiceFooter(subDetails, toalAmount, gst, calculatedgstValue, granTotal,
				totalAmountInwords);
		try {
			File file = new File(date + "\\" + invoiceName);
			String pdfPath = subDetails.getUserid().getUserid() + "_" + subDetails.getSubscriptionId() + ".pdf";
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(invoiceHeader + invoiceSubscription + invoiceVas + invoiceDiscount + invoiceFooter);
			bw.close();
			String path = file.getAbsolutePath();
			HtmlConverter.convertToPdf(new FileInputStream(file), new FileOutputStream(pdfPath));
			return pdfPath;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("============Inside generateSplanInvoiceTemplate class==========");
			logger.info(
					"===========================generateSplanInvoiceTemplate:-=======================================");
			return null;
		}
	}

	public InvoiceBeanResponse generateVasInvoiceResponse(NimaiSubscriptionVas vasDetails,
			NimaiSubscriptionDetails subDetails, OnlinePayment paymentDetails, NimaiSystemConfig configDetails) {
		logger.info("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + vasDetails);
		String invoiceName = vasDetails.getId() + vasDetails.getSubscriptionId() + vasDetails.getVasId() + ".htm";
		String pattern = "MM/dd/yyyy";
		Date dnow = new Date();
		String invoiceVas = "";
		String invoiceDiscount = "";
		String invoiceSubscription = "";
		String toalAmount = "";
		String calculatedgstValue = "";
		String referenceNumber = "";
		if (vasDetails.getMode().equalsIgnoreCase("Wire")) {
			referenceNumber = vasDetails.getPaymentTxnId();
		} else {
			referenceNumber = paymentDetails.getOrderId();
		}
		String date = (new SimpleDateFormat("dd/MM/yyyy")).format(vasDetails.getInsertedDate());
		toalAmount = String.valueOf(subDetails.getVasAmount());
		String granTotal = String.valueOf(subDetails.getVasAmount());
		AmountToWords words = new AmountToWords();
		String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
		String gst = configDetails.getSystemEntityValue();
		try {
			InvoiceBeanResponse response = new InvoiceBeanResponse();
			response.setCustomerId(vasDetails.getUserid().getUserid());
			response.setContactPersonName(
					vasDetails.getUserid().getFirstName() + " " + vasDetails.getUserid().getLastName());
			response.setCountry(vasDetails.getUserid().getCountryName());
			response.setCompanyName(vasDetails.getUserid().getCompanyName());
			response.setVasAmount(granTotal);
			response.setGrandTotal(granTotal);
			response.setTotalAmount(toalAmount);
			response.setInvoiceDate(date);
			response.setReferrenceNumber(referenceNumber);
			response.setGst(gst);
			response.setAmountInWords(totalAmountInwords);
			response.setInvoiceNumber(vasDetails.getInvoiceId());
			response.setVasStatus(vasDetails.getPaymentSts());
			response.setSplanSerialNumber(vasDetails.getsPLanSerialNUmber());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("============Inside generateVasInvoiceResponse class==========");
			logger.info(
					"===========================generateVasInvoiceResponse:-=======================================");
			return null;
		}
	}

	public String downloadVasInvoiceTemplate(NimaiSubscriptionVas vasDetails, NimaiSubscriptionDetails subDetails,
			OnlinePayment paymentDetails, NimaiSystemConfig configDetails, String imagePath) {
		logger.info("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + vasDetails);
		String invoiceName = vasDetails.getId() + vasDetails.getSubscriptionId() + vasDetails.getVasId() + ".htm";
		String pattern = "MM/dd/yyyy";
		Date dnow = new Date();
		String date = (new SimpleDateFormat("dd/MM/yyyy")).format(dnow);
		String invoiceVas = "";
		String invoiceDiscount = "";
		String invoiceSubscription = "";
		String toalAmount = "";
		String calculatedgstValue = "";
		String referenceNumber = "";
		if (vasDetails.getMode().equalsIgnoreCase("Wire")) {
			referenceNumber = vasDetails.getPaymentTxnId();
		} else {
			referenceNumber = paymentDetails.getOrderId();
		}
		toalAmount = String.valueOf(subDetails.getVasAmount());
		String granTotal = String.valueOf(subDetails.getVasAmount());
		AmountToWords words = new AmountToWords();
		String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
		String gst = configDetails.getSystemEntityValue();
		String invoiceHeader = getVasInvoiceHeader(subDetails, date, referenceNumber, vasDetails, imagePath);
		if (subDetails.getVasAmount() != 0)
			invoiceVas = getVasDetails(subDetails);
		String invoiceFooter = getVasInvoiceFooter(subDetails, toalAmount, gst, calculatedgstValue, granTotal,
				totalAmountInwords);
		try {
			File file = new File(date + "\\" + invoiceName);
			String pdfPath = subDetails.getUserid().getUserid() + "_" + subDetails.getSubscriptionId() + ".pdf";
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(invoiceHeader + invoiceSubscription + invoiceVas + invoiceDiscount + invoiceFooter);
			bw.close();
			String path = file.getAbsolutePath();
			HtmlConverter.convertToPdf(new FileInputStream(file), new FileOutputStream(pdfPath));
			return pdfPath;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("============Inside generateSplanInvoiceTemplate class==========");
			logger.info(
					"===========================generateSplanInvoiceTemplate:-=======================================");
			return null;
		}
	}

	private String getVasInvoiceHeader(NimaiSubscriptionDetails subDetails, String date, String referenceNumber,
			NimaiSubscriptionVas vasDetails, String imagePath) {
		String invoiceNumber = "";
		invoiceNumber = vasDetails.getInvoiceId();
		String header = "<!doctype html>\r\n<html>\r\n   <head>\r\n      <meta charset=\"utf-8\">\r\n      <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n      <title>A simple, clean, and responsive HTML invoice template</title> \r\n   </head>\r\n   <body> \r\n      <div class=\"invoice-box\" style=\"max-width:800px;\r\n         margin:auto;\r\n         padding:30px;\r\n         border:1px solid #eee;\r\n         box-shadow:0 0 10px rgba(0, 0, 0, .15);\r\n         font-size:16px;\r\n         line-height:24px;\r\n         font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;\r\n         color:#555;\">\r\n         <table cellpadding=\"0\" cellspacing=\"0\" style=\" font-family:'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;font-size: 15px;\r\n         line-height:inherit;\r\n         text-align:left;\">\r\n            <tr>\r\n               <td colspan=\"2\" align=\"right\" style=\"padding:5px;vertical-align:top;\">\r\n                  <img src="
				+ imagePath
				+ " width=\"200\">\r\n               </td>\r\n            </tr>\r\n             <tr><td colspan=\"2\" style=\"height: 10px;\"></td></tr>\r\n            <tr>\r\n               <td colspan=\"2\" style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;font-size: 18px;font-weight: 600;color: #000;\r\n         text-align:center;\">\r\n                  INVOICE\r\n               </td>\r\n            </tr> \r\n\r\n             <tr><td colspan=\"2\" style=\"height: 20px;\"></td></tr>\r\n            <tr>\r\n               <td style=\"padding:5px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Customer ID :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subDetails.getUserid().getUserid()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ invoiceNumber
				+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\" nowrap=\"\">\r\n                           Company Name :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subDetails.getUserid().getCompanyName()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice Date :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ date
				+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Country : \r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subDetails.getUserid().getCountryName()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Reference No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ referenceNumber
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                    <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\" nowrap=\"\">\r\n                           Contact Person Name: \r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subDetails.getUserid().getFirstName()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Mobile No.</th>\r\n                        \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ subDetails.getUserid().getMobileNumber()
				+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                      <tr>\r\n                        <td colspan=\"4\" style=\"vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;height: 35px;\"></td>\r\n                     </tr>\r\n                  </table>\r\n               </td>\r\n            </tr>\r\n            <tr>\r\n               <td style=\"padding:0px 5px;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                           SEQ # \r\n                        </th> \r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        DESCRIPTION</th>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        AMOUNT US$</th> \r\n                     </tr>\r\n";
		return header;
	}

	private String getVasInvoiceFooter(NimaiSubscriptionDetails subscriptionDetails, String granTotal, String gst,
			String calculatedgstValue, String toalAmountWithGst, String totalAmountInwords) {
		String footer = "\r\n\r\n<tr>                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\">\r\n                           <strong>Total Amount</strong>\r\n                        </td> \r\n                        <td style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ granTotal
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n\r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\">\r\n                           <strong>GST</strong><i>&#x00040;"
				+ gst
				+ "%</i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ calculatedgstValue
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                           \r\n                        </th> \r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        Grand Total</th>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        <i>"
				+ toalAmountWithGst
				+ "</i>\r\n                     </th> \r\n                     </tr>\r\n                  </table>\r\n               </td>\r\n            </tr>\r\n            <tr>\r\n               <td style=\"padding:0px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:0;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"text-align: left;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           AMOUNT IN WORDS (US$):\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ totalAmountInwords
				+ "</i>\r\n                        </td> \r\n                     </tr>\r\n                      \r\n                  </table>\r\n               </td>\r\n            </tr> \r\n            <tr>\r\n               <td style=\"padding:10px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:1px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th  style=\"border-right:1px dashed #ccc;text-align: left;padding:5px;vertical-align:top;border-bottom:0px dashed #ccc;border-left: 1px dashed #ccc;CU47552;background-color: #fff;color: #1f3864;\">\r\n                           NIMAI TRADE FINTECH PTE. LTD.<br />\r\n                           <i>UEN: 202101205M</i>\r\n                        </th> \r\n                     </tr>\r\n                       <tr><td style=\"border-right:1px dashed #ccc;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: #1f3864;height:5px;\"></td></tr>\r\n                  </table>\r\n               </td>\r\n            </tr> \r\n            <tr>\r\n               <td style=\"padding:10px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:1px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"border-right:1px dashed #ccc;text-align: left;padding:5px;vertical-align:top;border-bottom:0px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: black;\">\r\n                           <strong style=\"color: #ed7d31;\">Registered Address</strong>:  <i>160, Robinson Road, #23-08 Singapore Business Federation Center, Singapore (068914)</i>\r\n                        </th> \r\n                     </tr>\r\n                       <tr><td style=\"border-right:1px dashed #ccc;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: #1f3864;height: 25px;\"></td></tr>\r\n                  </table>\r\n               </td>\r\n            </tr> \r\n             <tr><td colspan=\"2\" style=\"height: 10px;\"></td></tr>\r\n            <tr class=\"heading\">\r\n               <td style=\"padding:5px;vertical-align:top;font-size: 13px;\" align=\"center\" colspan=\"2\">\r\n                 <i>This is an electronically generated document and does not require physical signatures.</i>\r\n               </td> \r\n            </tr>\r\n\r\n             <tr><td colspan=\"2\" style=\"height: 1px;\"></td></tr>\r\n         </table>\r\n      </div> \r\n   </body>\r\n</html>";
		return footer;
	}

	private String getVasDetails(NimaiSubscriptionDetails subscriptionDetails) {
		String vas = "                     <tr> \r\n                        <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;height: 22px;\">\r\n                          1 \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;\"> \r\n                           <i>VAS Plan </i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ String.valueOf(subscriptionDetails.getVasAmount())
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
		return vas;
	}

	public String generatePostSplanInvoiceTemplate(NimaiSubscriptionDetails subscriptionDetails,
			OnlinePayment paymentDetails, NimaiSystemConfig configDetails2, String imagePath,
			NimaiEmailScheduler schdulerData, NimaiPostpaidSubscriptionDetails postPaidDetails) {

		System.out.println("Inside generatePostSplanInvoiceTemplate " + subscriptionDetails);
		String invoiceName = subscriptionDetails.getSubscriptionName() + ".htm";
		String pattern = "MM/dd/yyyy";
		Date dnow = new Date();
		String date = (new SimpleDateFormat("dd/MM/yyyy")).format(dnow);
		String subscriptionPlanAmount = String.valueOf(postPaidDetails.getTotalPayment());
		if (subscriptionDetails.getDiscount() == null)
			subscriptionDetails.setDiscount(0.0D);
		String gst = configDetails2.getSystemEntityValue();
		String invoiceVas = "";
		String invoiceDiscount = "";
		String invoiceSubscription = "";
		String toalAmount = "";
		String calculatedgstValue = "";
		String referenceNumber = "";
		float vasSumAmount = 0;
		Double posSubAmount = 0.0;
		Double DisAmount = 0.0;
		String granTotal;
if(postPaidDetails.getDueType().equalsIgnoreCase("totalDue")
		&& postPaidDetails.getUserid().getSubscriberType().equalsIgnoreCase("BANK")
		&& postPaidDetails.getUserid().getBankType().equalsIgnoreCase("UNDERWRITER")) {
	posSubAmount = posSubAmount + postPaidDetails.getTotalDue();
	DisAmount = postPaidDetails.getDiscountAmount();
//	invoiceNumber = poSubAmnt.getInvoiceId();
	if (postPaidDetails.getTotalDue() == 0.0D) {
		referenceNumber = "";
	} else if (postPaidDetails.getPaymentmode().equalsIgnoreCase("Wire")) {
		referenceNumber = postPaidDetails.getPaymentTxnId();
	} else {
		referenceNumber = paymentDetails.getOrderId();
	}

}
		else if (postPaidDetails.getDueType().equalsIgnoreCase("totalDue")) {
			posSubAmount = posSubAmount + postPaidDetails.getTotalDue();
			DisAmount = postPaidDetails.getDiscountAmount();
//			invoiceNumber = poSubAmnt.getInvoiceId();
			if (postPaidDetails.getTotalDue() == 0.0D) {
				referenceNumber = "";
			} else if (postPaidDetails.getPaymentmode().equalsIgnoreCase("Wire")) {
				referenceNumber = postPaidDetails.getPaymentTxnId();
			} else {
				referenceNumber = paymentDetails.getOrderId();
			}
		} else {
			posSubAmount = posSubAmount + postPaidDetails.getMinDue();
			DisAmount = postPaidDetails.getDiscountAmount();
//			invoiceNumber = poSubAmnt.getInvoiceId();
			if (postPaidDetails.getMinDue() == 0.0D) {
				referenceNumber = "";
			} else if (postPaidDetails.getPaymentmode().equalsIgnoreCase("Wire")) {
				referenceNumber = postPaidDetails.getPaymentTxnId();
			} else {
				referenceNumber = paymentDetails.getOrderId();
			}
		}

//		if (postPaidDetails.getTotalPayment() == 0.0D) {
//			referenceNumber = "";
//		} else if (postPaidDetails.getPaymentmode().equalsIgnoreCase("Wire")) {
//			referenceNumber = postPaidDetails.getPaymentTxnId();
//		} else {
//			referenceNumber = paymentDetails.getOrderId();
//		}

//		String granTotal = String.valueOf(posSubAmount + vasSumAmount - DisAmount);
		
		logger.info("=====================calculatedgstValue" + calculatedgstValue);
		if (schdulerData.getVasAmount() == 0 && DisAmount == 0.0D
				&& posSubAmount != 0.0D) {
			logger.info("===============first condition=================");
			toalAmount = String.valueOf(posSubAmount);
			calculatedgstValue = String.valueOf(this.util.GstValue(posSubAmount,
					configDetails2.getSystemEntityValue()));
		} else if (schdulerData.getVasAmount() != 0 && DisAmount == 0.0D
				&& posSubAmount != 0.0D) {
			logger.info("===============second condition=================");
			Double vasPlusSPlan = (schdulerData.getVasAmount() + posSubAmount);
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2.getSystemEntityValue()));
			System.out.println("===============second condition=================" + vasPlusSPlan);
		} else if (schdulerData.getVasAmount() != 0 && DisAmount != 0.0D
				&& posSubAmount != 0) {
			Double vasPlusSPlan = (schdulerData.getVasAmount() + posSubAmount)
					- subscriptionDetails.getDiscount();
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2.getSystemEntityValue()));
			logger.info("===============third condition=================" + vasPlusSPlan);
		} else if (schdulerData.getVasAmount() == 0 && subscriptionDetails.getDiscount() != 0.0D
				&& posSubAmount != 0) {
			Double vasPlusSPlan = posSubAmount - DisAmount;
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2.getSystemEntityValue()));
			logger.info("===============fourth condition=================" + vasPlusSPlan);
		}else if(postPaidDetails.getDueType().equalsIgnoreCase("totalDue")
				&& postPaidDetails.getUserid().getSubscriberType().equalsIgnoreCase("BANK")
				&& postPaidDetails.getUserid().getBankType().equalsIgnoreCase("UNDERWRITER")) {
			toalAmount = String.valueOf(posSubAmount);
		} else {
			toalAmount = String.valueOf(posSubAmount);
		}
		String numberOfTransaction = "";

		granTotal = String.valueOf(Math.round(Double.parseDouble(toalAmount) +
				Double.parseDouble(calculatedgstValue)));

		String totalAmountInwords = AmountToWords.NumberToWords(Double.parseDouble(granTotal));
		String invoiceHeader = getPostaidInvoiceHeader(postPaidDetails, date, referenceNumber, imagePath);

		if (schdulerData.getVasAmount() != 0)
			invoiceVas = getpostInvoiceVas(schdulerData);
		if (subscriptionDetails.getDiscount() != 0.0D) {
			String srNummber = "";
			if (schdulerData.getVasAmount() != 0) {
				srNummber = "3";
			} else {
				srNummber = "2";
			}
			invoiceDiscount = getpostInvoiceDiscount(postPaidDetails, srNummber);
			Double double_ = subscriptionDetails.getDiscount();
		}
		if (subscriptionDetails.getSubscriptionAmount() != 0){
			invoiceSubscription = getPostPaidInvoiceSubscription(Math.round(posSubAmount));
		}else if(subscriptionDetails.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")){
			invoiceSubscription = getPostPaidInvoiceSubscription(Math.round(posSubAmount));
		}


		// String invoiceFooter = getInvoiceFooter(subscriptionDetails, granTotal, gst,
		// calculatedgstValue, toalAmount, totalAmountInwords);
		String invoiceFooter = getPostPaidInvoiceFooter( granTotal, gst, calculatedgstValue,
				String.valueOf(Math.round(Double.parseDouble(toalAmount))), totalAmountInwords);

		try {
			logger.info("====================+++++++htmInvoicePath" + this.htmInvoicePath);
			File file = new File(this.htmInvoicePath + date + "\\" + invoiceName);
			String pdfPath = subscriptionDetails.getUserid().getUserid() + "_" + subscriptionDetails.getSubscriptionId()
					+ ".pdf";
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(invoiceHeader + invoiceSubscription + invoiceVas + invoiceDiscount + invoiceFooter);
			bw.close();
			String path = file.getAbsolutePath();
			HtmlConverter.convertToPdf(new FileInputStream(file), new FileOutputStream(pdfPath));
			return pdfPath;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("============Inside generateSplanInvoiceTemplate class==========");
			logger.info(
					"===========================generateSplanInvoiceTemplate:-=======================================");
			return null;
		}

	}
	
	
	
	
	

	private String getPostPaidInvoiceFooter( String granTotal,
			String gst, String calculatedgstValue, String toalAmount, String totalAmountInwords) {

		String footer = "\r\n\r\n                    "
				+ "    <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;\">\r\n                      "
				+ "     \r\n                        </td> \r\n                        "
				+ "<td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\">\r\n                        "
				+ "   <strong>Total Amount</strong>\r\n                       " + " </td> \r\n                      "
				+ "  <td style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        "
				+ "   <i>" + toalAmount + "</i>\r\n                      "
				+ "  </td>\r\n                     </tr> \r\n\r\n              "
				+ "       <tr> \r\n                      "
				+ "  <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\">\r\n                           <strong>GST</strong><i>&#x00040;"
				+ gst
				+ "%</i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
				+ calculatedgstValue
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                           \r\n                        </th> \r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                      "
				+ "  Grand Total</th>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                       "
				+ " <i>" + granTotal
				+ "</i>\r\n                     </th> \r\n                     </tr>\r\n                  </table>\r\n               </td>\r\n            </tr>\r\n            <tr>\r\n               <td style=\"padding:0px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:0;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"text-align: left;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           AMOUNT IN WORDS (US$):\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                         "
				+ "  <i>" + totalAmountInwords
				+ "</i>\r\n                        </td> \r\n                     </tr>\r\n                      \r\n                  </table>\r\n               </td>\r\n            </tr> \r\n            <tr>\r\n               <td style=\"padding:10px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:1px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th  style=\"border-right:1px dashed #ccc;text-align: left;padding:5px;vertical-align:top;border-bottom:0px dashed #ccc;border-left: 1px dashed #ccc;CU47552;background-color: #fff;color: #1f3864;\">\r\n                           NIMAI TRADE FINTECH PTE. LTD.<br />\r\n                           <i>UEN: 202101205M</i>\r\n                        </th> \r\n                     </tr>\r\n                       <tr><td style=\"border-right:1px dashed #ccc;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: #1f3864;height:5px;\"></td></tr>\r\n                  </table>\r\n               </td>\r\n            </tr> \r\n            <tr>\r\n               <td style=\"padding:10px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:1px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                        <th style=\"border-right:1px dashed #ccc;text-align: left;padding:5px;vertical-align:top;border-bottom:0px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: black;\">\r\n                           <strong style=\"color: #ed7d31;\">Registered Address</strong>:  <i>160, Robinson Road, #23-08 Singapore Business Federation Center, Singapore (068914)</i>\r\n                    "
				+ "    </th> \r\n                     </tr>\r\n                "
				+ "       <tr><td style=\"border-right:1px dashed #ccc;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #fff;color: #1f3864;height: 25px;\"></td></tr>\r\n                "
				+ "  </table>\r\n               </td>\r\n            </tr> \r\n             <tr><td colspan=\"2\" style=\"height: 10px;\"></td></tr>\r\n            <tr class=\"heading\">\r\n               <td style=\"padding:5px;vertical-align:top;font-size: 13px;\" align=\"center\" colspan=\"2\">\r\n                 <i>This is an electronically generated document and does not require physical signatures.</i>\r\n               </td> \r\n            </tr>\r\n\r\n             <tr><td colspan=\"2\" style=\"height: 1px;\"></td></tr>\r\n         </table>\r\n      </div> \r\n   </body>\r\n</html>";
		return footer;

	}

	private String getPostPaidInvoiceSubscription(long posSubAmount) {
		String subscription = "                     <tr> \r\n                        <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;\">\r\n                           1\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;\">"
				+ "\r\n Transaction Fees \r\n                        </td> \r\n                       "
				+ " <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                             <i>"
				+ String.valueOf(posSubAmount) + "</i> </td>\r\n               "
				+ "      </tr> \r\n                     <tr> \r\n                       "
				+ " <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                   "
				+ "     <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                      "
				+ "  <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                     "
				+ "   </td>\r\n                     </tr> \r\n";
		return subscription;
	}

	private String getPostaidInvoiceHeader(NimaiPostpaidSubscriptionDetails postPaidDetails, String date,
			String referenceNumber, String imagePath) {
		String invoiceNumber = "";
		invoiceNumber = postPaidDetails.getInvoiceId();
		String header = "<!doctype html>\r\n<html>\r\n   <head>\r\n    " + "  <meta charset=\"utf-8\">\r\n     "
				+ " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n     "
				+ " <title>A simple, clean, and responsive HTML invoice template</title> \r\n "
				+ "  </head>\r\n   <body> \r\n     " + " <div class=\"invoice-box\" style=\"max-width:800px;\r\n      "
				+ "   margin:auto;\r\n         padding:30px;\r\n         " + "border:1px solid #eee;\r\n         "
				+ "box-shadow:0 0 10px rgba(0, 0, 0, .15);\r\n         font-size:16px;\r\n      "
				+ "   line-height:24px;\r\n        "
				+ " font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;\r\n    "
				+ "     color:#555;\">\r\n         " + "<table cellpadding=\"0\" cellspacing=\"0\" style=\" "
				+ "font-family:'Helvetica Neue', 'Helvetica',"
				+ " Helvetica, Arial, sans-serif;width:100%;font-size: 15px;\r\n        "
				+ " line-height:inherit;\r\n         text-align:left;\">\r\n         " + "   <tr>\r\n              "
				+ " <td colspan=\"2\" align=\"right\" style=\"padding:5px;vertical-align:top;\">\r\n "
				+ "                 <img src=" + imagePath + " width=\"200\">\r\n        "
				+ "       </td>\r\n            </tr>\r\n          "
				+ "   <tr><td colspan=\"2\" style=\"height: 10px;\"></td></tr>\r\n          " + "  <tr>\r\n           "
				+ "    <td colspan=\"2\" style=\" font-family:'Calibri', 'Helvetica', "
				+ "Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;font-size: 18px;font-weight: 600;color: #000;\r\n      "
				+ "   text-align:center;\">\r\n               " + "   INVOICE\r\n               </td>\r\n          "
				+ "  </tr> \r\n\r\n             <tr><td colspan=\"2\" style=\"height: 20px;\"></td></tr>\r\n       "
				+ "     <tr>\r\n               <td style=\"padding:5px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n     "
				+ "              <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                     "
				+ "   <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Customer ID :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                      "
				+ "     <i>" + postPaidDetails.getUserid().getUserid()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           "
				+ "<i>" + invoiceNumber
				+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\" nowrap=\"\">\r\n                           Contact Person Name :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                    "
				+ "       <i>" + postPaidDetails.getUserid().getFirstName() + " "
				+ postPaidDetails.getUserid().getLastName() + "</i>\r\n                "
				+ "        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice Date :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ date
				+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Country : \r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        "
				+ "   <i>" + postPaidDetails.getUserid().getCountryName()
				+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Reference No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
				+ referenceNumber
				+ "</i>\r\n                        </td>\r\n                     </tr> \r\n       <tr>\r\n       "
				+ "                 <th colspan=\"1\" align=\"left\" style=\"text-align: left;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        <i>Company Name: </i></th>\r\n    <td colspan=\"4\" style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n          "
				+ "                 <i>" + postPaidDetails.getUserid().getCompanyName()
				+ "</i>\r\n                        "
				+ "</td>\r\n                     </tr> +   <tr>\r\n                       "
				+ " <td colspan=\"4\" style=\"vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;height: 35px;\"></td>\r\n                     </tr>\r\n                  </table>\r\n               </td>\r\n            </tr>\r\n            <tr>\r\n               <td style=\"padding:0px 5px;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                 "
				+ "    <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                           SEQ # \r\n                        </th> \r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n             "
				+ "           DESCRIPTION</th>\r\n                     "
				+ "   <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        AMOUNT US$</th> \r\n                     </tr>\r\n";
		return header;

	}

	
public InvoiceBeanResponse generatePosVasInvoiceResponse(NimaiSubscriptionVas vasDetails,
		List<NimaiPostpaidSubscriptionDetails> posSubDetails, OnlinePayment paymentDetails,
		NimaiSystemConfig configDetails, List<NimaiSubscriptionVas> vasDetailsList) {
	// TODO Auto-generated method stub
	 logger.info("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + vasDetails);
	    String invoiceName = vasDetails.getId() + vasDetails.getSubscriptionId() + vasDetails.getVasId() + ".htm";
	    String pattern = "MM/dd/yyyy";
	    Date dnow = new Date();
	    String invoiceVas = "";
	    String invoiceDiscount = "";
	    String invoiceSubscription = "";
	    String toalAmount = "";
	    String calculatedgstValue = "";
	    String referenceNumber = "";
	    if (vasDetails.getMode().equalsIgnoreCase("Wire")) {
	      referenceNumber = vasDetails.getPaymentTxnId();
	    } else {
	      referenceNumber = paymentDetails.getOrderId();
	    } 
	    float vasSumAmount=0;
	    Double posSubAmount=0.0;
	    Double DisAmount=0.0;
	    
	    
	    if(vasDetailsList.size()>1) {
	    	for(NimaiSubscriptionVas vas:vasDetailsList) {
	    		vasSumAmount=vasSumAmount + vas.getPricing();
	    	}
	    }else {
	    	vasSumAmount=vasDetails.getPricing();
	    }
	    
	  
	    	for(NimaiPostpaidSubscriptionDetails poSubAmnt:posSubDetails) {
	    		if(poSubAmnt.getDueType().equalsIgnoreCase("totalDue")) {
	    			posSubAmount=posSubAmount + poSubAmnt.getTotalDue();
	    			DisAmount=poSubAmnt.getDiscountAmount();
	    		}else {
	    			if(poSubAmnt.getDueType().equalsIgnoreCase("totalDue")) {
	    				posSubAmount=posSubAmount + poSubAmnt.getTotalDue();
	    				DisAmount=poSubAmnt.getDiscountAmount();
	    				}else {
	    				posSubAmount=posSubAmount + poSubAmnt.getMinDue();
	    				DisAmount=poSubAmnt.getDiscountAmount();
	    			}
	    		}
	    		
	    	}
	    	
	    
	    
	    
	    String date = (new SimpleDateFormat("dd/MM/yyyy")).format(vasDetails.getInsertedDate());
	    toalAmount = String.valueOf(vasSumAmount);
	    String granTotal = String.valueOf(vasSumAmount);
	    AmountToWords words = new AmountToWords();
	    String totalAmountInwords = AmountToWords.NumberToWords(Double.valueOf(granTotal).doubleValue());
	    String gst = configDetails.getSystemEntityValue();
	    try {
	      InvoiceBeanResponse response = new InvoiceBeanResponse();
	      response.setCustomerId(vasDetails.getUserid().getUserid());
	      response.setContactPersonName(vasDetails
	          .getUserid().getFirstName() + " " + vasDetails.getUserid().getLastName());
	      response.setCountry(vasDetails.getUserid().getCountryName());
	      response.setCompanyName(vasDetails.getUserid().getCompanyName());
	      response.setVasAmount(String.valueOf(vasSumAmount));
	      response.setGrandTotal(granTotal);
	      response.setTotalAmount(toalAmount);
	      response.setInvoiceDate(date);
	      response.setReferrenceNumber(referenceNumber);
	      response.setGst(gst);
 response.setAmountInWords(totalAmountInwords);
	      response.setInvoiceNumber(vasDetails.getInvoiceId());
	      response.setVasStatus(vasDetails.getPaymentSts());
	      response.setSplanSerialNumber(vasDetails.getsPLanSerialNUmber());
	      return response;
	    } catch (Exception e) {
	      e.printStackTrace();
	      logger.info("============Inside generateVasInvoiceResponse class==========");
	      logger.info("===========================generateVasInvoiceResponse:-=======================================");
	      return null;
	    } 
	  
}


public InvoiceBeanResponse genStringPostSplanInvoiceTemplatePdf(
		OnlinePayment paymentDetails, String configDetails2, String imagePath,
		List<NimaiSubscriptionVas> vasDetailsList,List<NimaiPostpaidSubscriptionDetails> posSubDetails,
		NimaiClient cuDetails) throws Exception {
//System.out.println("Inside generateSplanInvoiceTemplategenerateSplanInvoiceTemplate " + subscriptionDetails);
//String invoiceName = subscriptionDetails.getSubscriptionId() + ".htm";
	String pattern = "MM/dd/yyyy";
	Date dnow = new Date();
	String date = (new SimpleDateFormat("dd/MM/yyyy")).format(
			posSubDetails.get(0).getPostpaidStartDate());
//String subscriptionPlanAmount = String.valueOf(subscriptionDetails.getSubscriptionAmount());
//if (subscriptionDetails.getDiscount() == null)
//	subscriptionDetails.setDiscount(Double.valueOf(0.0D));
	String gst = configDetails2;
	String invoiceVas = "";
	String invoiceDiscount = "";
	String invoiceSubscription = "";
	String toalAmount = "";
	String calculatedgstValue = "";
	String referenceNumber = "";


	float vasSumAmount = 0;
	Double posSubAmount = 0.0;
	Double DisAmount = 0.0;
	String invoiceNumber = "";

	if (vasDetailsList != null)
		if (!vasDetailsList.isEmpty()) {
			for (NimaiSubscriptionVas vas : vasDetailsList) {
				System.out.println("****************** " + vas.getPricing());
				vasSumAmount = vasSumAmount + vas.getPricing();
			}
		}
	for (NimaiPostpaidSubscriptionDetails poSubAmnt : posSubDetails) {
		if (poSubAmnt.getDueType().equalsIgnoreCase("totalDue")) {
			posSubAmount = posSubAmount + poSubAmnt.getTotalDue();
			DisAmount = poSubAmnt.getDiscountAmount();
			invoiceNumber = poSubAmnt.getInvoiceId();
			if (poSubAmnt.getTotalDue() == 0.0D) {
				referenceNumber = "";
			} else if (poSubAmnt.getPaymentmode().equalsIgnoreCase("Wire")) {
				referenceNumber = poSubAmnt.getPaymentTxnId();
			} else {
				referenceNumber = paymentDetails.getOrderId();
			}
		} else {
			if (poSubAmnt.getDueType().equalsIgnoreCase("totalDue")) {
				posSubAmount = posSubAmount + poSubAmnt.getTotalDue();
				DisAmount = poSubAmnt.getDiscountAmount();
				invoiceNumber = poSubAmnt.getInvoiceId();
				if (poSubAmnt.getTotalDue() == 0.0D) {
					referenceNumber = "";
				} else if (poSubAmnt.getPaymentmode().equalsIgnoreCase("Wire")) {
					referenceNumber = poSubAmnt.getPaymentTxnId();
				} else {
					referenceNumber = paymentDetails.getOrderId();
				}
			} else {
				posSubAmount = posSubAmount + poSubAmnt.getMinDue();
				DisAmount = poSubAmnt.getDiscountAmount();
				invoiceNumber = poSubAmnt.getInvoiceId();
				if (poSubAmnt.getMinDue() == 0.0D) {
					referenceNumber = "";
				} else if (poSubAmnt.getPaymentmode().equalsIgnoreCase("Wire")) {
					referenceNumber = poSubAmnt.getPaymentTxnId();
				} else {
					referenceNumber = paymentDetails.getOrderId();
				}
			}
		}

	}
	logger.info("=====================calculatedgstValue" + calculatedgstValue);
	//String granTotal = String.valueOf(subscriptionDetails.getGrandAmount());
	String granTotal = String.valueOf(posSubAmount + vasSumAmount - DisAmount);
	if (vasDetailsList != null) {
		if (vasDetailsList.size() == 0 && DisAmount == 0.0D
				&& posSubDetails.size() != 0) {
			logger.info("===============first condition=================");
			toalAmount = String.valueOf(posSubAmount);
			calculatedgstValue = String.valueOf(this.util.GstValue(posSubAmount,
					configDetails2));
		} else if (vasDetailsList.size() != 0 && DisAmount == 0.0D
				&& posSubDetails.size() != 0) {
			Double vasPlusSPlan;
			logger.info("===============second condition=================");
			vasPlusSPlan = (vasSumAmount + posSubAmount);
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2));
			System.out.println("===============second condition=================" + vasPlusSPlan);
		} else if (vasDetailsList.size() != 0 && DisAmount != 0.0D
				&& posSubDetails.size() != 0) {
			Double vasPlusSPlan;
			vasPlusSPlan = (vasSumAmount + posSubAmount)
					- DisAmount;
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2));
			logger.info("===============third condition=================" + vasPlusSPlan);
		} else if (vasDetailsList.size() == 0 && DisAmount != 0.0D
				&& posSubDetails.size() != 0) {
			Double vasPlusSPlan = posSubAmount - DisAmount;
			toalAmount = String.valueOf(vasPlusSPlan);
			calculatedgstValue = String
					.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2));
			logger.info("===============fourth condition=================" + vasPlusSPlan);
		}
	}
//AmountToWords words = new AmountToWords();
//String invoiceHeader = getInvoiceHeader(subscriptionDetails, date, referenceNumber, imagePath);
//if (subscriptionDetails.getVasAmount() != 0)
	//invoiceVas = getInvoiceVas(subscriptionDetails);
//if (subscriptionDetails.getDiscount() != 0.0D) {
//	String srNummber = "";
//	if (subscriptionDetails.getVasAmount() != 0) {
//		srNummber = "3";
//	} else {
//		srNummber = "2";
//	}
////	invoiceDiscount = getInvoiceDiscount(subscriptionDetails, srNummber);
//	//Double double_ = subscriptionDetails.getDiscount();
//}
	InvoiceBeanResponse beanResponse = new InvoiceBeanResponse();
//if (subscriptionDetails.getSubscriptionAmount() != 0)
	//	invoiceSubscription = getInvoiceSubscription(subscriptionDetails);
	try {


		beanResponse.setCustomerId(cuDetails.getUserid());
		beanResponse.setInvoiceNumber(invoiceNumber);
		beanResponse.setContactPersonName(cuDetails.getFirstName() + " "
				+ cuDetails.getLastName());
		beanResponse.setInvoiceDate(date);
		beanResponse.setCountry(cuDetails.getCountryName());
		beanResponse.setReferrenceNumber(referenceNumber);
		beanResponse.setCompanyName(cuDetails.getCompanyName());
		beanResponse.setCalculatedGstValue(calculatedgstValue);

		beanResponse.setGst(gst);
		beanResponse.setsPlanAmount(String.valueOf(Math.round(posSubAmount)));
		beanResponse.setVasAmount(String.valueOf(Math.round(vasSumAmount)));

		Double grandAmount = Double.valueOf(granTotal);
		double calGstValue = Double.parseDouble(calculatedgstValue);
		Double toalAmnt = Double.sum(grandAmount, calGstValue);
		toalAmount = String.valueOf(Math.round(toalAmnt));

		String totalAmountInwords = AmountToWords.NumberToWords(Double.parseDouble(toalAmount));
		beanResponse.setAmountInWords(totalAmountInwords);
		beanResponse.setGrandTotal(String.valueOf(Math.round(grandAmount)));
		beanResponse.setTotalAmount(toalAmount);
		beanResponse.setVasDiscount(String.valueOf(DisAmount));
		System.out.println("response ************* " + beanResponse);
		return beanResponse;
	} catch (Exception e) {
		e.printStackTrace();
		logger.info("============Inside generateSplanInvoiceTemplate class==========");
		logger.info(
				"===========================generateSplanInvoiceTemplate:-=========================S==============");
		return null;
	}
}



private String getPostaidInvoiceHeaderModify(NimaiClient userDetails, String date,
		   String referenceNumber, String imagePath,String invoiceId) {
String invoiceNumber = "";
invoiceNumber = invoiceId;
String header = "<!doctype html>\r\n<html>\r\n   <head>\r\n    " + "  <meta charset=\"utf-8\">\r\n     "
+ " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n     "
+ " <title>A simple, clean, and responsive HTML invoice template</title> \r\n "
+ "  </head>\r\n   <body> \r\n     " + " <div class=\"invoice-box\" style=\"max-width:800px;\r\n      "
+ "   margin:auto;\r\n         padding:30px;\r\n         " + "border:1px solid #eee;\r\n         "
+ "box-shadow:0 0 10px rgba(0, 0, 0, .15);\r\n         font-size:16px;\r\n      "
+ "   line-height:24px;\r\n        "
+ " font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;\r\n    "
+ "     color:#555;\">\r\n         " + "<table cellpadding=\"0\" cellspacing=\"0\" style=\" "
+ "font-family:'Helvetica Neue', 'Helvetica',"
+ " Helvetica, Arial, sans-serif;width:100%;font-size: 15px;\r\n        "
+ " line-height:inherit;\r\n         text-align:left;\">\r\n         " + "   <tr>\r\n              "
+ " <td colspan=\"2\" align=\"right\" style=\"padding:5px;vertical-align:top;\">\r\n "
+ "                 <img src=" + imagePath + " width=\"200\">\r\n        "
+ "       </td>\r\n            </tr>\r\n          "
+ "   <tr><td colspan=\"2\" style=\"height: 10px;\"></td></tr>\r\n          " + "  <tr>\r\n           "
+ "    <td colspan=\"2\" style=\" font-family:'Calibri', 'Helvetica', "
+ "Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;font-size: 18px;font-weight: 600;color: #000;\r\n      "
+ "   text-align:center;\">\r\n               " + "   INVOICE\r\n               </td>\r\n          "
+ "  </tr> \r\n\r\n             <tr><td colspan=\"2\" style=\"height: 20px;\"></td></tr>\r\n       "
+ "     <tr>\r\n               <td style=\"padding:5px 5px 0;vertical-align:top;\" colspan=\"2\">\r\n     "
+ "              <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                     <tr>\r\n                     "
+ "   <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Customer ID :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                      "
+ "     <i>" + userDetails.getUserid()
+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           "
+ "<i>" + invoiceNumber
+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\" nowrap=\"\">\r\n                           Contact Person Name :\r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                    "
+ "       <i>" + userDetails.getFirstName() + " "
+ userDetails.getLastName() + "</i>\r\n                "
+ "        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Invoice Date :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
+ date
+ "</i>\r\n                        </td>\r\n                     </tr>\r\n                     <tr>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           Country : \r\n                        </th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        "
+ "   <i>" + userDetails.getCountryName()
+ "</i>\r\n                        </td>\r\n                        <th style=\"padding:5px;text-align:left;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        Reference No. :</th>\r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                           <i>"
+ referenceNumber
+ "</i>\r\n                        </td>\r\n                     </tr> \r\n       <tr>\r\n       "
+ "                 <th colspan=\"1\" align=\"left\" style=\"text-align: left;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n                        <i>Company Name: </i></th>\r\n    <td colspan=\"4\" style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\">\r\n          "
+ "                 <i>" + userDetails.getCompanyName()
+ "</i>\r\n                        "
+ "</td>\r\n                     </tr> +   <tr>\r\n                       "
+ " <td colspan=\"4\" style=\"vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;height: 35px;\"></td>\r\n                     </tr>\r\n                  </table>\r\n               </td>\r\n            </tr>\r\n            <tr>\r\n               <td style=\"padding:0px 5px;vertical-align:top;\" colspan=\"2\">\r\n                   <table style=\" font-family:'Calibri', 'Helvetica', Helvetica, Arial, sans-serif;width:100%;\r\n         line-height:inherit;\r\n         text-align:left;border:1px dashed #ccc;border-left: 0px dashed #ccc;border-bottom:0px dashed #ccc;border-top:0px dashed #ccc;\" cellpadding=\"0\" cellspacing=\"0\">\r\n                 "
+ "    <tr>\r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                           SEQ # \r\n                        </th> \r\n                        <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n             "
+ "           DESCRIPTION</th>\r\n                     "
+ "   <th style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;background-color: #1f3864;color: #fff;\">\r\n                        AMOUNT US$</th> \r\n                     </tr>\r\n";
return header;

}



private String getpostInvoiceDiscountModify(String discountAmt, String srNummber) {
	String discount = "                     <tr> \r\n                       "
			+ " <td style=\"text-align: center;padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;font-weight: 600;height: 22px;\">\r\n                         "
			+ srNummber
			+ "\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: #ed7d31;\"> \r\n                           <i>Discount Coupon</i>\r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                           <i>"
			+ String.valueOf(
			Double.parseDouble((new DecimalFormat("##.##")).format(discountAmt)))
			+ "</i>\r\n                        </td>\r\n                     </tr> \r\n                     <tr> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color: black;font-weight: 600;height: 22px;\">\r\n                           \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;color:black;\"> \r\n                        </td> \r\n                        <td style=\"padding:5px;vertical-align:top;border-bottom:1px dashed #ccc;border-left: 1px dashed #ccc;\"> \r\n                        </td>\r\n                     </tr> \r\n";
	return discount;
}





public String generatePostSplanInvoiceTemplateAfterApproved(NimaiSubscriptionDetails subscriptionDetails,
		   OnlinePayment paymentDetails, NimaiSystemConfig configDetails2, String imagePath,
		   NimaiEmailScheduler schdulerData,
						List<NimaiPostpaidSubscriptionDetails> postPaidDetailsList) {

System.out.println("Inside generatePostSplanInvoiceTemplate " + subscriptionDetails);
String invoiceName = subscriptionDetails.getSubscriptionName() + ".htm";
String pattern = "MM/dd/yyyy";
Date dnow = new Date();
String date = (new SimpleDateFormat("dd/MM/yyyy")).format(postPaidDetailsList.get(0)
.getPostpaidStartDate());
//String subscriptionPlanAmount = String.valueOf(postPaidDetails.getTotalPayment());
if (subscriptionDetails.getDiscount() == null)
subscriptionDetails.setDiscount(0.0D);
String gst = configDetails2.getSystemEntityValue();
String invoiceVas = "";
String invoiceDiscount = "";
String invoiceSubscription = "";
String toalAmount = "";
String calculatedgstValue = "";
String referenceNumber = "";
float vasSumAmount = 0;
Double posSubAmount = 0.0;
Double DisAmount = 0.0;
String granTotal;



for (NimaiPostpaidSubscriptionDetails postPaidDetails:postPaidDetailsList) {
	if(postPaidDetails.getDueType()==null) {
		postPaidDetails.setDueType("NA");
	}else {
		postPaidDetails.setDueType(postPaidDetails.getDueType());
	}
if (postPaidDetails.getDueType().equalsIgnoreCase("totalDue")) {
posSubAmount = posSubAmount + postPaidDetails.getTotalDue();
DisAmount = postPaidDetails.getDiscountAmount();
//invoiceNumber = poSubAmnt.getInvoiceId();
if (postPaidDetails.getTotalDue() == 0.0D) {
referenceNumber = "";
} else if (postPaidDetails.getPaymentmode().equalsIgnoreCase("Wire")) {
referenceNumber = postPaidDetails.getPaymentTxnId();
} else {
referenceNumber = paymentDetails.getOrderId();
}
} else {
posSubAmount = posSubAmount + postPaidDetails.getMinDue();
DisAmount = postPaidDetails.getDiscountAmount();
//invoiceNumber = poSubAmnt.getInvoiceId();
if (postPaidDetails.getMinDue() == 0.0D) {
referenceNumber = "";
} else if (postPaidDetails.getPaymentmode().equalsIgnoreCase("Wire")) {
referenceNumber = postPaidDetails.getPaymentTxnId();
} else {
referenceNumber = paymentDetails.getOrderId();
}
}
}

//if (postPaidDetails.getTotalPayment() == 0.0D) {
//referenceNumber = "";
//} else if (postPaidDetails.getPaymentmode().equalsIgnoreCase("Wire")) {
//referenceNumber = postPaidDetails.getPaymentTxnId();
//} else {
//referenceNumber = paymentDetails.getOrderId();
//}

//String granTotal = String.valueOf(posSubAmount + vasSumAmount - DisAmount);
logger.info("=====================calculatedgstValue" + calculatedgstValue);
if (schdulerData.getVasAmount() == 0 && DisAmount == 0.0D
&& posSubAmount != 0.0D) {
logger.info("===============first condition=================");
toalAmount = String.valueOf(posSubAmount);
calculatedgstValue = String.valueOf(this.util.GstValue(posSubAmount,
configDetails2.getSystemEntityValue()));
} else if (schdulerData.getVasAmount() != 0 && DisAmount == 0.0D
&& posSubAmount != 0.0D) {
logger.info("===============second condition=================");
Double vasPlusSPlan = (schdulerData.getVasAmount() + posSubAmount);
toalAmount = String.valueOf(vasPlusSPlan);
calculatedgstValue = String
.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2.getSystemEntityValue()));
System.out.println("===============second condition=================" + vasPlusSPlan);
} else if (schdulerData.getVasAmount() != 0 && DisAmount != 0.0D
&& posSubAmount != 0) {
Double vasPlusSPlan = (schdulerData.getVasAmount() + posSubAmount)
- subscriptionDetails.getDiscount();
toalAmount = String.valueOf(vasPlusSPlan);
calculatedgstValue = String
.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2.getSystemEntityValue()));
logger.info("===============third condition=================" + vasPlusSPlan);
} else if (schdulerData.getVasAmount() == 0 && subscriptionDetails.getDiscount() != 0.0D
&& posSubAmount != 0) {
Double vasPlusSPlan = posSubAmount - DisAmount;
toalAmount = String.valueOf(vasPlusSPlan);
calculatedgstValue = String
.valueOf(this.util.GstValue(vasPlusSPlan, configDetails2.getSystemEntityValue()));
logger.info("===============fourth condition=================" + vasPlusSPlan);
} else {
toalAmount = String.valueOf(posSubAmount);
}
String numberOfTransaction = "";

granTotal = String.valueOf(Math.round(Double.parseDouble(toalAmount) +
Double.parseDouble(calculatedgstValue)));

String totalAmountInwords = AmountToWords.NumberToWords(Double.parseDouble(granTotal));
String invoiceHeader = getPostaidInvoiceHeaderModify(subscriptionDetails.getUserid(),
date, referenceNumber, imagePath,postPaidDetailsList.get(0).getInvoiceId());

if (schdulerData.getVasAmount() != 0)
invoiceVas = getpostInvoiceVas(schdulerData);
if (subscriptionDetails.getDiscount() != 0.0D) {
String srNummber = "";
if (schdulerData.getVasAmount() != 0) {
srNummber = "3";
} else {
srNummber = "2";
}
invoiceDiscount = getpostInvoiceDiscountModify(String.valueOf(DisAmount), srNummber);
Double double_ = subscriptionDetails.getDiscount();
}
if (subscriptionDetails.getSubscriptionAmount() != 0){
invoiceSubscription = getPostPaidInvoiceSubscription(Math.round(posSubAmount));
}else if(subscriptionDetails.getSubscriptionName().equalsIgnoreCase("POSTPAID_PLAN")){
invoiceSubscription = getPostPaidInvoiceSubscription(Math.round(posSubAmount));
}


// String invoiceFooter = getInvoiceFooter(subscriptionDetails, granTotal, gst,
// calculatedgstValue, toalAmount, totalAmountInwords);
String invoiceFooter = getPostPaidInvoiceFooter(granTotal, gst, calculatedgstValue,
String.valueOf(Math.round(Double.parseDouble(toalAmount))), totalAmountInwords);

try {
logger.info("====================+++++++htmInvoicePath" + this.htmInvoicePath);
File file = new File(this.htmInvoicePath + date + "\\" + invoiceName);
String pdfPath = subscriptionDetails.getUserid().getUserid() + "_" + subscriptionDetails.getSubscriptionId()
+ ".pdf";
file.getParentFile().mkdirs();
FileWriter writer = new FileWriter(file);
BufferedWriter bw = new BufferedWriter(new FileWriter(file));
bw.write(invoiceHeader + invoiceSubscription + invoiceVas + invoiceDiscount + invoiceFooter);
bw.close();
String path = file.getAbsolutePath();
HtmlConverter.convertToPdf(new FileInputStream(file), new FileOutputStream(pdfPath));
return pdfPath;
} catch (Exception e) {
e.printStackTrace();
logger.info("============Inside generateSplanInvoiceTemplate class==========");
logger.info(
"===========================generateSplanInvoiceTemplate:-=======================================");
return null;
}

}





















}
