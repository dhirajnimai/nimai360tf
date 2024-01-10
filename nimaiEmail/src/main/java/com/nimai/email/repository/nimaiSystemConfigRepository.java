package com.nimai.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.nimai.email.entity.NimaiSystemConfig;


@Repository
@Transactional
public interface nimaiSystemConfigRepository extends JpaRepository<NimaiSystemConfig, Integer>,
JpaSpecificationExecutor<NimaiSystemConfig>{

	@Query(value="select * from nimai_system_config ns where ns.id=:i",nativeQuery = true)
	NimaiSystemConfig findBySystemId(int i);

	@Query(value="select * from nimai_system_config ns where ns.system_config_entity=:systemValue",nativeQuery = true)
	NimaiSystemConfig findBySystemValue(String systemValue);
	
	@Query(value="select ns.system_config_entity_value from nimai_system_config ns where ns.system_config_entity=:string",nativeQuery = true)
	String getimagePath(String string);
	
	@Query(value="select ns.system_config_entity_value from nimai_system_config ns where ns.system_config_entity=:string",nativeQuery = true)
	String getFromEmail(String string);

	@Query(value="select ns.system_config_entity_value from nimai_system_config ns where ns.system_config_entity='360tfNumber'",nativeQuery = true)
	String findByNumber();
	
	@Query(value="select ns.system_config_entity_value from nimai_system_config ns where ns.system_config_entity='360tfEmail'",nativeQuery = true)
	String findByEmail();

	@Query(value="select ns.system_config_entity_value from nimai_system_config ns where ns.system_config_entity='CUSTOMER_LINK_EXPIRES'",nativeQuery = true)
	String findByLinkDays();

	
	@Query(value = "select ns.system_config_entity_value from nimai_system_config ns where ns.system_config_entity='referrer_earnings'", nativeQuery = true)
	  String findReferrerEarning();
	
	
	  @Query(value = "select ns.system_config_entity_value from nimai_system_config ns where ns.system_config_entity=:email", nativeQuery = true)
	  String phseTwoEmail(String email);
	
}
