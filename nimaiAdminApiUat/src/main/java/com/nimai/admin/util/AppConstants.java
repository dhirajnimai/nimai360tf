package com.nimai.admin.util;

public interface AppConstants {
    String DEFAULT_PAGE_NUMBER = "0";
    String DEFAULT_PAGE_SIZE = "30";

    int MAX_PAGE_SIZE = 50;
    
    int PASSWORD_RESET_FLAG = 0;
    
    String LOGIN_STATUS = "ACTIVE";
    String APPROVAL_PENDING = "Pending";
    String SUBSIDIARY_ACTIVATION_EVENT = "SUBSIDIARY_ACTIVATION_ALERT";
    String ACCOUNT_REFER = "CUSTOMER_ACCOUNT_REFERRED";
    String RXILUSER="RE54153";

    String SUCCESSMSG="Success";
    String FAILMSG="fail";
    String ENUMTYPE="MASTER_USER";
    String VALUABSENT="NA";
    String SUBACCTYPE="SUBSIDIARY";
    String BATYPE="BANK";
    String REGTYPEBOTH="Impo_Expo";
    String REGTBOTH="Both";
    String REGTYIMP="Impoter";
    String REGTYPEIMP="Importer";
    String REGTYEXP="Expoter";
    String REGTYEXPORTER="Exporter";
    String ClIENTREGEXPIMP="EXPORTER_AND_IMPORTER";
    String CLIENTREGIMP="IMPORTER";
    String CLIENTREGEXP="EXPORTER";
    String CLIENTSUBTYPE="BANK_AS_UNDERWRITER";
    String CLIENTBASUBTYPE="BANK_CUSTOMER";
    String MIGGCURL="migration_group_company_url";
    String MIGCREATEUSRURL="migration_create_user_url";
    String MIGUPDATEPASSUEL="migration_update_password_url";
    String MIGMASTERTOKENURL="migration_master_token_url";
    String MIGPERDETAILSURL="migration_personal_details_url";
    String MIGBUSINDETAILSURL="migration_business_details_url";
    String BANKTYPE="UNDERWRITER";
    String CUBANKTYPE="CUSTOMER";
    String PEEMAILSTTS ="Pending";
    String FAILEVENT ="PHASE_TWO_TRNSFER_FAIL";
}
