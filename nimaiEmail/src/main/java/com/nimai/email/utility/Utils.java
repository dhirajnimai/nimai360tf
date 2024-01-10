package com.nimai.email.utility;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nimai.email.entity.NimaiClient;

@Component
public class Utils {
	private static Logger logger = LoggerFactory.getLogger(EmaiInsert.class);

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd";

	public Date getLinkExpiryDate() {
		Date dNow = new Date();
		Date dafter = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(5, 2);
		dafter = cal.getTime();
		System.out.println(dafter);
		return dafter;
	}

	public Date getLinkExpiry(String days) {
		Date dNow = new Date();
		Date dafter = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(5, Integer.parseInt(days));

		dafter = cal.getTime();
		System.out.println(dafter);
		return dafter;
	}

	public Date getForgotPassExpiryLink() {
		Date dNow = new Date();
		Date dafter = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		System.out.println(cal);
		cal.add(12, 5);
		dafter = cal.getTime();
		System.out.println(dafter);
		return dafter;
	}

	public Date getLinkExDate() {
		Date dNow = new Date();
		Date dafter = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(5, 30);
		dafter = cal.getTime();
		System.out.println(dafter);
		return dafter;
	}

	public Date get15daysBeforSPlanEndDate() {
		Date dNow = new Date();
		Date dafter = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(5, 15);
		dafter = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Utils utilse = new Utils();
		Date enDate = stringDateToDate(sdf.format(dafter));
		System.out.println(dafter);
		return dafter;
	}

	public Date get7daysBeforSPlanEndDate() {
		Date dNow = new Date();
		Date dafter = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(5, 7);
		dafter = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Utils utilse = new Utils();
		Date enDate = stringDateToDate(sdf.format(dafter));
		System.out.println(dafter);
		return dafter;
	}

	public static Date stringDateToDate(String StrDate) {
		Date dateToReturn = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			dateToReturn = dateFormat.parse(StrDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateToReturn;
	}

	public static String generatePasswordResetToken() {
		Random objGenerator = new Random(System.currentTimeMillis());
		StringBuilder builder = new StringBuilder();
		int randomNumberLength = 10;
		for (int i = 0; i < randomNumberLength; i++) {
			int digit = objGenerator.nextInt(10);
			builder.append(digit);
		}
		return builder.toString();
	}

	public String getEmailDomain(String someEmail) {
		return someEmail.substring(someEmail.indexOf("@") + 1);
	}

	public static String passcodeValue() {
		DateTime dt = new DateTime();
		int seconds = dt.getSecondOfMinute();
		int date = dt.getDayOfMonth();
		Random objGenerator = new Random(System.currentTimeMillis());
		StringBuilder builder = new StringBuilder();
		int randomNumberLength = 5;
		for (int i = 0; i < randomNumberLength; i++) {
			int digit = objGenerator.nextInt(10);
			builder.append(digit);
		}
		String passCodeValue = builder.toString().concat(String.valueOf(seconds));
		String passCode = passCodeValue.replaceAll("\\s", "");
		return builder.toString();
	}

	public static Date transformFromStringToDate(String dateInString) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		Date utilDate = null;
		try {
			if (dateInString != null && !dateInString.equals(""))
				utilDate = dateFormat.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println(utilDate);
		return utilDate;
	}

	public String invoiceNumber() {
		int invoiceSeq = 0;
		int nextinvoiceSeq = invoiceSeq + 1;
		String strnextinvoiceSeq = String.valueOf(nextinvoiceSeq);
		String finalInvoiceseq = String.format("%05d",
				new Object[] { Integer.valueOf(Integer.parseInt(strnextinvoiceSeq)) });
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date dnow = new Date();
		String invoiceNumber = (new SimpleDateFormat("ddMMyyyy")).format(dnow);
		logger.info("=========Inside sendCustEodDailyReport method with schedulerData from database=========="
				+ invoiceNumber);
		System.out.println(invoiceNumber.concat(finalInvoiceseq));
		return invoiceNumber;
	}

	public String invoiceDate() {
		Date dnow = new Date();
		String invoiceDate = (new SimpleDateFormat("dd/MM/yyyy")).format(dnow);
		logger.info("=========Inside sendCustEodDailyReport method with schedulerData from database=========="
				+ invoiceDate);
		return invoiceDate;
	}

	public Double GstValue(double vasPlusSPlan, String gstValue) {
		int perValue=100;
		Double finalValueGst=Double.parseDouble(gstValue)/perValue;
		Double gstAmount = Double.valueOf(finalValueGst * vasPlusSPlan);
		logger.info("=========Inside GstValue==========" + vasPlusSPlan);
		Double value = Double.valueOf(Double.parseDouble((new DecimalFormat("##.##")).format(gstAmount)));
		return value;
	}

	public Double subtractionAmount(int number1, int number2, double number3) {
		Double value = Double.valueOf((number1 + number2) - number3);
		return value;
	}

	public Double gstPlusSPlanAmount(Double SubscriptionAmount, Double gstAmount) {
		Double totalAmount = Double.valueOf(SubscriptionAmount.doubleValue() + gstAmount.doubleValue());
		logger.info("=========Inside GstValu database==========" + SubscriptionAmount);
		return totalAmount;
	}

	public String generateLinkToken(String subscribeType, String bankType) {
		DateTime dt = new DateTime();
		int seconds = dt.getSecondOfMinute();
		int date = dt.getDayOfMonth();
		Random random = new Random(System.currentTimeMillis());
		String id = String.format("%10d", new Object[] { Integer.valueOf(random.nextInt(10000000)) });
		String userID = "";
		String initials = null;
		String newInitials = null;
		initials = subscribeType.substring(0, 2);
		newInitials = bankType.substring(0, 2);
		String tokenInitials = initials.toUpperCase() + String.valueOf(date) + String.valueOf(seconds)
				+ newInitials.toUpperCase() + id;
		String token = tokenInitials.replaceAll("\\s", "");
		return token;
	}

	public Float referrerAmount(int SubscriptionAmount, String referPercentage) {
		logger.info("========= ==========" + referPercentage);
		System.out.println("=========Inside referrerAmount==========" + referPercentage);
		Float referEarning = Float.valueOf(referPercentage);
		Float actualREarning = Float.valueOf(referEarning.floatValue() / 100.0F);
		Float gstAmount = Float.valueOf(actualREarning.floatValue() * SubscriptionAmount);
		logger.info("=========Inside GstValue==========" + gstAmount);
		System.out.println("=========Inside GstValue==========" + gstAmount);
		Float value = Float.valueOf(Float.parseFloat((new DecimalFormat("##.##")).format(gstAmount)));
		logger.info("=========Conversion referrerAmount in util==========" + value);
		System.out.println("=========Conversion referrerAmount in util==========" + value);
		return value;
	}

	public Double referrerAmount(int SubscriptionAmount) {
		Double gstAmount = (0.07) * (SubscriptionAmount);
		logger.info("=========Inside GstValue==========" + SubscriptionAmount);

		return gstAmount;
	}

	public static void main(String[] args) throws ParseException, IOException {
		Utils ut = new Utils();
		String days = "72";
//    System.out.println(ut.getLinkExpiry("84"));
//    System.out.println(ut.subtractionAmount(933, 144, 93.3D));
//    List<String> list = Arrays.asList(new String[] { "A", "B", "C", "D" });
//    Optional<String> result = list.stream().findFirst();
//    System.out.println(result.get());
		ut.getLinkExpiry(days);

	}

	public float GstValue(Float pricing) {
		float gstAmount = (float) (0.18D * pricing.floatValue());
		logger.info("=========Inside GstValue==========" + pricing);
		return gstAmount;
	}

	public static long betweenDates(Date firstDate, Date secondDate) throws IOException {
		return ChronoUnit.DAYS.between(firstDate.toInstant(), secondDate.toInstant());
	}

	public Date getthirtydaysBeforSPlanEndDate() {
		Date dNow = new Date();
		Date dafter = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dNow);
		cal.add(5, 30);
		dafter = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Utils utilse = new Utils();
		Date enDate = stringDateToDate(sdf.format(dafter));
		System.out.println(dafter);
		return dafter;
	}

	public int getNoOfyears(int month) {
		return month / 12;
	}

	public int getNoOfMonths(int month) {
		return month % 12;
	}

}
