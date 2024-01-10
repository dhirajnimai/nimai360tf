package com.nimai.admin.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nimai.admin.model.BeneInterestedCountry;
import com.nimai.admin.model.InterestedCountry;
import com.nimai.admin.model.NimaiFBlkgoods;
import com.nimai.admin.payload.BeneInterestedCountryBean;
import com.nimai.admin.payload.BlackListedGoodsBean;
import com.nimai.admin.payload.InterestedCountryBean;
import com.nimai.admin.payload.OffLineBankRequest;
import com.nimai.admin.payload.PagedResponse;
import com.nimai.admin.payload.SearchRequest;
import com.nimai.admin.payload.SorceDetailsReponseBean;


public interface OffLineBankService {

	public ResponseEntity<?> createOrUpdateOfflineBanks(OffLineBankRequest request);

	void saveBlackListedGoods(NimaiFBlkgoods blg);

	void updateBlackListedGoods(BlackListedGoodsBean blgb);

	void saveInterestedCountry(InterestedCountry ic);

	void updateInterestedCountry(InterestedCountryBean icb);

	public PagedResponse<?> getSearchOfflineBankDetail(SearchRequest request);

	public ResponseEntity<?> updateOffBauUserStatus(String userId, String status);

	void updateBeneInterestedCountry(BeneInterestedCountryBean icb);

	void saveBeneInterestedCountry(BeneInterestedCountry ic);

	boolean existsByEmailId(OffLineBankRequest request);



	public ResponseEntity<?> editOffBauUserStatus(OffLineBankRequest request);

	boolean existsByEmIdAddUser(OffLineBankRequest request, String userType);


}
