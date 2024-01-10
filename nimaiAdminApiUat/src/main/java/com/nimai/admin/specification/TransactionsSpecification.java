package com.nimai.admin.specification;

import static org.springframework.data.jpa.domain.Specification.where;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import com.nimai.admin.util.Utility;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMmTransaction;
import com.nimai.admin.payload.SearchRequest;

@Component
public class TransactionsSpecification extends BaseSpecification<NimaiMmTransaction, SearchRequest> {

	@Override
	public Specification<NimaiMmTransaction> getFilter(SearchRequest request) {
		return (root, query, cb) -> {
			return where(inUserId("userid", request.getUserId(),request.getIsSpecific())).
					and(inUserId("emailAddress", request.getEmailId(),request.getIsSpecific())).
					and(notValuesContains("bankType", request.getBankType()))
					.and(inUserId("mobileNumber", request.getMobileNo(),request.getIsSpecific()))
					.and(likeValuesContains("requirementType", request.getGoodsType()))
					.and(likeValuesContains("lcIssuanceCountry", request.getCountry()))
//					.and(likeValuesContains("dischargeCountry", request.getCountry()))
					.and(valuesContains("transactionStatus", request.getTxtStatus()))
					.and(likeValuesContains("requirementType", request.getRequirementType()))
					.and(iN("lcIssuanceCountry", request.getCountryNames()))
//					.and(valueBetween("lcIssuingDate", request.getDateFrom(), request.getDateTo()))
					.and(valueBetween(request.getDateType(), request.getDateFrom(), request.getDateTo()))
					.toPredicate(root, query, cb);
		};
	}

	
	
	  private Specification<NimaiMmTransaction> notValuesContains(String attribute, String value) {
		    return (root, query, cb) -> {
		        if (value == null || value.equalsIgnoreCase("all"))
		          return null; 
		        Join<NimaiMmTransaction, NimaiMCustomer> po = root.join("userId", JoinType.LEFT);
				if(value.equalsIgnoreCase("UNDERWRITER")){
					return cb.or(cb.notEqual(po.get(attribute), value),cb.isNull(po.get(attribute)));
				}
		        return cb.notEqual(po.get(attribute), value);
		      };
		  }
	
	private Specification<NimaiMmTransaction> valuesContains(String attribute, String value) {
		return (root, query, cb) -> {
			if (value == null || value.equalsIgnoreCase("all")) {
				return null;
			}
			return cb.equal(root.get(attribute), value);
		};
	}

	private Specification<NimaiMmTransaction> likeValuesContains(String attribute, String value) {
		return (root, query, cb) -> {
			if (value != null) {
				return cb.like(root.get(attribute), cb.literal("%" + value + "%"));
			} else {
				return null;
			}
		};
	}

	private Specification<NimaiMmTransaction> filterValuesContains(String entityName, NimaiMCustomer value) {
		return userAttributeContains(entityName, value);
	}

	private Specification<NimaiMmTransaction> userAttributeContains(String attribute, NimaiMCustomer value) {
		return (root, query, cb) -> {
			if (value == null) {
				return null;
			}
			return cb.equal(root.get(attribute), value);
		};
	}

	public static Specification<NimaiMmTransaction> inUserId(String attribute, String value,boolean isSpecific) {
		return (root, query, cb) -> {
			if (value == null) {
				return null;
			}
			final Join<NimaiMmTransaction, NimaiMCustomer> po = root.join("userId", JoinType.LEFT);
			if(isSpecific){
				return cb.equal(po.get(attribute),value);
			}else {
				return cb.like(po.get(attribute), cb.literal("%" + value + "%"));
			}
		};
	}

	private Specification<NimaiMmTransaction> valueBetween(String attribute, String dateFrom, String dateTo) {
		System.out.println(dateFrom + " " + dateTo);
		if(attribute == null){
			attribute = "insertedDate";
		}
		String finalAttribute = attribute;
		System.out.println("Date type filter ---------> "+finalAttribute);
		return (root, query, cb) -> {
			if (dateFrom != null && dateTo != null) {
				java.util.Date newDateTo = Utility.getDateTimeInFormatInDate(dateTo);
				java.util.Date newDateFrom = Utility.getDateTimeInFormatDateFrom(dateFrom);
				System.out.println("newDateFrom--------> "+newDateFrom);
				System.out.println("newDateTo---------> "+newDateTo);
//				Date.valueOf(LocalDate.parse(dateTo))
				return cb.between(root.get(finalAttribute), newDateFrom,
						newDateTo);
			} else {
				return null;
			}
		};
	}

//	private Specification<NimaiMmTransaction> iN(String attribute, String inValue) {
//		return (root, query, cb) -> {
//			if (inValue != null) {
//				final Join<NimaiMmTransaction, NimaiMCustomer> countryJoin = root.join("userId", JoinType.LEFT);
//				List<String> value = Stream.of(inValue.split(",", -1)).collect(Collectors.toList());
//				return countryJoin.get(attribute).in(value);
//			} else {
//				return null;
//			}
//		};
//	}
	
	private Specification<NimaiMmTransaction> iN(String attribute, String inValue) {
		return (root, query, cb) -> {
			if (inValue != null) {
				List<String> value = Stream.of(inValue.split(",", -1)).collect(Collectors.toList());
				return root.get(attribute).in(value);
			} else {
				return null;
			}
		};
	}

}
