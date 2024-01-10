package com.nimai.admin.specification;

import static org.springframework.data.jpa.domain.Specification.where;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.nimai.admin.model.NimaiMCustomer;
import com.nimai.admin.model.NimaiMRefer;
import com.nimai.admin.payload.SearchRequest;

@Component
public class CustomerReferSearchSpecification extends BaseSpecification<NimaiMCustomer, SearchRequest> {



	@Override
	public Specification<NimaiMCustomer> getFilter(SearchRequest request) {

		return (root, query, cb) -> {
			return where(valuesContains("userid", 
					request.getUserId() == null ? null : new NimaiMCustomer(request.getUserId())))
//					.and(inUserId("kycStatus", request.getTxtStatus()))
//					.and((valuesContains("subscriberType", request.getSubscriberType())))
					.toPredicate(root, query, cb);
		};
	}
	
	private Specification<NimaiMCustomer> valuesContains(String attribute, NimaiMCustomer value) {
		return (root, query, cb) -> {
			if (value == null) {
				return null;
			}
			return cb.equal(root.get(attribute), value);
		};
	}
}
