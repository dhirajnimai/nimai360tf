package com.nimai.admin.specification;

import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMmTransaction;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.util.Utility;

import java.lang.invoke.SerializedLambda;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TransactionTempSpecification extends BaseSpecification<NimaiMmTransaction, SearchRequest> {
  public Specification<NimaiMmTransaction> getFilter(SearchRequest request) {
    return (root, query, cb) -> Specification.
    		where(inUserId("userid", request.getUserId())).
    		and(notValuesContains("bankType", request.getBankType())).
    		and(inUserId("emailAddress", request.getEmailId())).
    		and(likeValuesContains("mobileNumber", request.getMobileNo())).
    		and(likeValuesContains("requirementType", request.getGoodsType())).
    		and(likeValuesContains("lcIssuanceCountry", request.getCountry())).
    		and(valuesContains("transactionStatus", request.getTxtStatus())).
    		and(likeValuesContains("requirementType", request.getRequirementType())).
    		and(valueBetween(request.getDateType(), request.getDateFrom(), request.getDateTo())).toPredicate(root, query, cb);
  }
  
  
  private Specification<NimaiMmTransaction> notValuesContains(String attribute, String value) {
	    return (root, query, cb) -> {
	        if (value == null || value.equalsIgnoreCase("all"))
	          return null; 
	        Join<NimaiMmTransaction, NimaiMCustomer> po = root.join("userId", JoinType.LEFT);
	        return cb.notEqual((Expression)po.get(attribute), value);
	      };
	  }
	  
  
  private Specification<NimaiMmTransaction> valuesContains(String attribute, String value) {
    return (root, query, cb) -> 
      (value == null || value.equalsIgnoreCase("all")) ? null : cb.equal((Expression)root.get(attribute), value);
  }
  
  private Specification<NimaiMmTransaction> likeValuesContains(String attribute, String value) {
    return (root, query, cb) -> (value != null) ? cb.like((Expression)root.get(attribute), cb.literal("%" + value + "%")) : null;
  }
  
  private Specification<NimaiMmTransaction> filterValuesContains(String entityName, NimaiMCustomer value) {
    return userAttributeContains(entityName, value);
  }
  
  private Specification<NimaiMmTransaction> userAttributeContains(String attribute, NimaiMCustomer value) {
    return (root, query, cb) -> (value == null) ? null : cb.equal((Expression)root.get(attribute), value);
  }
  
  public static Specification<NimaiMmTransaction> inUserId(String attribute, String value) {
    return (root, query, cb) -> {
        if (value == null)
          return null; 
        Join<NimaiMmTransaction, NimaiMCustomer> po = root.join("userId", JoinType.LEFT);
        return cb.like((Expression)po.get(attribute), cb.literal("%" + value + "%"));
      };
  }
  
  private Specification<NimaiMmTransaction> valueBetween(String attribute, String dateFrom, String dateTo) {
    System.out.println(dateFrom + " " + dateTo);
	if(attribute == null){
		attribute = "insertedDate";
	}
	String finalAttribute = attribute;
	System.out.println("Date type filter ---------> "+finalAttribute);
//    return (root, query, cb) -> 
//      (dateFrom != null && dateTo != null) ? cb.between((Expression)root.get(finalAttribute), Date.valueOf(dateFrom), Date.valueOf(LocalDate.parse(dateTo).plusDays(1L))) : null;
//  
	return (root, query, cb) -> {
		if (dateFrom != null && dateTo != null) {
			java.util.Date newDateTo = Utility.getDateTimeInFormatInDate(dateTo);
			java.util.Date newDateFrom = Utility.getDateTimeInFormatDateFrom(dateFrom);
			System.out.println("newDateFrom--------> "+newDateFrom);
			System.out.println("newDateTo---------> "+newDateTo);
//			Date.valueOf(LocalDate.parse(dateTo))
			return cb.between(root.get(finalAttribute), newDateFrom,
					newDateTo);
		} else {
			return null;
		}
	};
  }
  
  private Specification<NimaiMmTransaction> iN(String attribute, String inValue) {
    return (root, query, cb) -> {
        if (inValue != null) {
          List<String> value = (List<String>)Stream.<String>of(inValue.split(",", -1)).collect(Collectors.toList());
          return root.get(attribute).in(value);
        } 
        return null;
      };
  }
}
