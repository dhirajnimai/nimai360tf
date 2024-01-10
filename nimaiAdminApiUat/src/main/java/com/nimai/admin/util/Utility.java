package com.nimai.admin.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiPostpaidSubscriptionDetails;
import com.nimai.admin.model.NimaiSubscriptionDetails;
import com.nimai.admin.model.NimaiSubscriptionVas;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nimai.admin.security.UserPrincipal;

import javax.persistence.Tuple;

public class Utility {

	public static String randomTokenKeyGenerator() {
		Random objGenerator = new Random(System.currentTimeMillis());
		StringBuilder builder = new StringBuilder();
		int randomNumberLength = 4;
		for (int i = 0; i < randomNumberLength; i++) {
			int digit = objGenerator.nextInt(10);
			builder.append(digit);
		}
		return builder.toString();
	}

	public static Date getSysDate() {
		LocalDate localDate = LocalDate.now();
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

	}

	public static String getUserCountry() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return userPrincipal.getCountry();
	}

	public static String getUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return userPrincipal.getUsername();
	}

	public static float getDecimalFormat(Object obj) {
		if (obj != null) {

			String s = obj.toString();
			if (!obj.equals("")) {
//				return Float.parseFloat(String.format("%.2f",Float.parseFloat(s)));
				return Float.parseFloat(
						(new DecimalFormat("##.##")).format(Double.parseDouble(s)));
			} else {
				return 0.0F;
			}
		} else {
			return 0.0F;
		}
	}
	public static double getReferEarnings(List<Tuple> custList, String status,
										  Double vasAmount){
		double value = 0;
		boolean isPostPaid = false;
		if(vasAmount== null || vasAmount.equals(""))
			vasAmount=0.0D;
		if(custList!=null && !custList.isEmpty()){
			if(Objects.equals(status, "all")){
				System.out.println("for all records");
				for (Tuple tuple : custList) {
					if (tuple.get("SUBSCRIPTION_NAME").equals("POSTPAID_PLAN")) {
						value += Double.parseDouble(tuple.get("min_due").toString());
						value -= Double.parseDouble(tuple.get("postpaid_discount").toString());
						isPostPaid=true;
//						value += vasAmount;
					} else if (!tuple.get("SUBSCRIPTION_NAME").equals("POSTPAID_PLAN")) {
						value += Double.parseDouble(tuple.get("GRAND_AMOUNT").toString());
					}

				}
				if(isPostPaid)
					value += vasAmount;

			}else if(Objects.equals(status, "userid")){
				for (Tuple tuple : custList) {
					if (tuple.get("SUBSCRIPTION_NAME").equals("POSTPAID_PLAN")) {
						value += Double.parseDouble(tuple.get("min_due").toString());
						value -= Double.parseDouble(tuple.get("postpaid_discount").toString());
						isPostPaid=true;
					} else if (!tuple.get("SUBSCRIPTION_NAME").equals("POSTPAID_PLAN")) {
						value += Double.parseDouble(tuple.get("GRAND_AMOUNT").toString());
					}
				}
				if(isPostPaid)
					value += vasAmount;
			}
		}
		return value;
	}



	public static String getDateTimeInFormat(String dateInString) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(dateInString);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DAY_OF_MONTH,1);
			cal.add(Calendar.SECOND,-1);
			SimpleDateFormat formmat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			String dateTime = formmat1.format(cal.getTime());
			System.out.println(dateTime);
			return dateTime;
		}catch (Exception e){
			e.printStackTrace();
			return dateInString;
		}
	}

	public static Date getDateTimeInFormatInDate(String dateInString) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(dateInString);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DAY_OF_MONTH,1);
			cal.add(Calendar.SECOND,-1);
			System.out.println("cal.getTime()----------> "+cal.getTime());
//			SimpleDateFormat formmat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//			String dateTime = formmat1.format(cal.getTime());
//			System.out.println(dateTime);
			return cal.getTime();
		}catch (Exception e){
			e.printStackTrace();
			return new Date();
		}
	}

	public static Date getDateTimeInFormatDateFrom(String dateInString) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = sdf.parse(dateInString);
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(date);
//			cal.add(Calendar.DAY_OF_MONTH,1);
//			cal.add(Calendar.SECOND,-1);
			System.out.println("cal.getTime()----------> "+date);
//			SimpleDateFormat formmat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//			String dateTime = formmat1.format(cal.getTime());
//			System.out.println(dateTime);
			return date;
		}catch (Exception e){
			e.printStackTrace();
			return new Date();
		}
	}

	public static void main(String[] args) {
		getDateTimeInFormatInDate("2021-02-22");
		getDateTimeInFormatDateFrom("2021-02-22");
//		int yearOFEarning=2;
//
//		switch(yearOFEarning) {
//		case 1:
//			System.out.println("COtton1");
//
//		case 2:
//			System.out.println("COtton2");
//
//		case 3:
//			System.out.println("COtton3");
//
//			default:
//				System.out.println("COtton4");
//		}
		
	
//		Double x = 355.59;
//		int y = 144;
//		double z = x - y;
//		DecimalFormat f = new DecimalFormat("##.00");
//		System.out.println(f.format(z));
//		System.out.println(z);
//		System.out.println("F decimal format" + getDecimalFormat(355.42453535));
	}
}

