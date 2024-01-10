
package com.nimai.splan;

import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.nimai.splan.service.SubscriptionPlanServiceImpl;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@EntityScan(basePackageClasses = { NimaiSPlanApplication.class, Jsr310JpaConverters.class })
@SpringBootApplication
@EnableScheduling
public class NimaiSPlanApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanServiceImpl.class);
	public static void main(String[] args) {
		SpringApplication.run(NimaiSPlanApplication.class, args);
		System.out.println(" =========== NIMAI Subscription and Discounting =========== ");
		LOGGER.info("=========== NIMAI Subscription and Discounting ===========  ");
		String str="planSplit[4] :merchantParam5=totalDue:25886";
		String str1=str.substring(str.indexOf("=")+1);
		String str2=str1.substring(str1.indexOf(":")+1);
		System.out.println(""+str1);
		System.out.println(""+str2);
		/*String str1="merchantParam2=215-129";
		System.out.println("StringUtils.countOccurrencesOf(value, \"-\"): "+StringUtils.countOccurrencesOf(str1, "-"));
		String value=str1.substring(str1.indexOf("=")+1);
		System.out.println(value);
		Double vasPrice=0d;
		if(value.contains("-"))
		{
			System.out.println("It contain -");
			int vasCount = StringUtils.countOccurrencesOf(value, "-");
	        System.out.println("Total VAS: " + vasCount);
	        String[] vasSplitted =value.split("-", vasCount + 1);
	        for (int i = 0; i < vasCount; i++) {
	        	System.out.println("vasSplitted: "+vasSplitted[i]);
	        	Double price=Double.valueOf(vasSplitted[i]);
	        	vasPrice=vasPrice+price;
	        }
	        System.out.println("vasPrice: "+vasPrice);
		}
		System.out.println();
		*//*
		String str="merchantParam5=minDue:28";
		System.out.println(str.substring(str.indexOf(":")+1));*/
		
  
	}

}
