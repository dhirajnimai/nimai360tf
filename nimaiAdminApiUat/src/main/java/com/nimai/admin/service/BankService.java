package com.nimai.admin.service;

import com.nimai.admin.payload.*;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import com.nimai.admin.model.NimaiMCustomer;

import java.util.List;

public interface BankService
{
    List<QuotationDetailsResponse> getQuotesUserId(final String userId);
    
    List<KycSDetailResponse> getKycDetailsUserId(final NimaiMCustomer userId);
    
    List<PlanOfPaymentDetailsResponse> getPlanOPayDetails(final String userId);
    
    PagedResponse<?> getSearchBankDetail(final SearchRequest request);
    
    ResponseEntity<?> kycStatusUpdate(final KycBDetailResponse data);
    
    PagedResponse<?> getBankQuoteList(final SearchRequest request);
    
    ResponseEntity<?> makerKycStatusUpdate(final KycBDetailResponse data);
    
    PagedResponse<?> getMakerApprovedKyc(final SearchRequest request);
    
    ResponseEntity<?> wireTranferStatusUpdate(final VasUpdateRequestBody request);
    
    PagedResponse<?> getWireTransferList(final SearchRequest request);
    
    ResponseEntity<BankDetailsResponse> getBankDetailUserId(final String userid);
    
    KycBDetailResponse getMakerApprovedKycByKycId(final SearchRequest request);
    
    ResponseEntity<?> checkDuplicateCouponCode(final CouponBean request);
    
    ResponseEntity<?> checkDuplicateSPLan(final SPlanBean request);
    
    PagedResponse<?> getVasWireTransferList(final SearchRequest request);
    
    ResponseEntity<?> transactionStatusUpdate(final TransactionRequestBody request);
    
    ResponseEntity<?> makerTransactionStatusUpdate(final TransactionRequestBody request);
    
    ResponseEntity<?> kycFiledSave(final KycFiledBean data);
    
    ResponseEntity<?> kycViewFieldData(final KycFiledBean data);
    
    List<PreferredBankListResponse> getBankList();
    
    ResponseEntity<?> createOrUpdatePreferredBank(@Valid final PreferredBankRequest request);
    
    List<PreferredBankListResponse> viewPreferredBanks(@Valid final PreferredBankRequest request);
    
    ResponseEntity<?> createOrUpdateBankRating(@Valid final BankRatingRequest request);
    
    ResponseEntity<?> viewBankRatingDetails(@Valid final BankRatingRequest request);
    
    ResponseEntity<?> createOrUpdateUploadedPreferredBankList(final String userId, final MultipartFile file) throws IOException;
    
    List<?> viewUploadedPreferredBanks(@Valid final PreferredBankRequest request);
    
    List<?> viewMasterRating(final String string);
    
    List<?> viewAgency();
    
    List<?> getSourceDetailsList();
    
    ResponseEntity<?> saveSourceDetails(final SorceDetailsReponseBean data);

	ResponseEntity<?> wireTranferStatusUpdatePostPaid(VasUpdateRequestBody request);

	List<PlanOfPaymentDetailsResponse> getPlanOPostPaidPayDetails(String userId);

	PagedResponse<?> getvasWirePostPaidTransferList(SearchRequest request);

	NimaiPostpaidSubscriptionRes getCurrentPlanOPostPaidPayDetails(String userId);

	ResponseEntity<?> interestedCountryUpdate(InterestedCountryBean[] data, String userId);

	ResponseEntity<?> beneInterestedCountriesUpdate(BeneInterestedCountryBean[] data, String userId);

    ResponseEntity<?> addViewCount(TranxViewCount count);
    ResponseEntity<?> viewCount(String userId);
}