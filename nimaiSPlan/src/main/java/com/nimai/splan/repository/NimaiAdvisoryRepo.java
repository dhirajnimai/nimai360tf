package com.nimai.splan.repository;

import com.nimai.splan.model.NimaiAdvisory;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface NimaiAdvisoryRepo extends JpaRepository<NimaiAdvisory, String> {

 @Query(value = "SELECT * FROM nimai_m_vas na INNER JOIN nimai_m_vascoutry vas ON na.VAS_ID=vas.vas_id WHERE na.CUSTOMER_TYPE=(:customerType)  and ((vas.vas_country in (:country_name)) or (vas.vas_country ='All')) and na.status='Active' and (na.plan_type !='postpaid' or na.plan_type IS NULL)", nativeQuery = true)
  List<NimaiAdvisory> findByCountryName(@Param("country_name") String country_name, @Param("customerType") String customerType);

	@Query(value = "SELECT * FROM nimai_m_vas  WHERE plan_type ='postpaid' and status='Active'", nativeQuery = true)
	List<NimaiAdvisory> findByType();
	@Query("SELECT na FROM NimaiAdvisory na WHERE na.vas_id = (:vasId) and na.status='Active'")
	NimaiAdvisory getDataByVasId(int vasId);
	
	@Query("SELECT na FROM NimaiAdvisory na WHERE na.vas_id = (:vasId)")
	NimaiAdvisory getVASDetByVasId(int vasId);
	
	@Query(value = "select pricing from nimai_m_vas where vas_id=(:vasId)", nativeQuery = true)
	Double findPricingByVASId(Integer vasId);
	
	@Query(value="SELECT system_config_entity_value from nimai_system_config where system_config_entity='invoice_gst'", nativeQuery = true )
	Double getGSTValue();


  @Modifying
  @Query(value = "update nimai_subscription_vas set spl_serial_number=:spNo where userid=:userId and status='ACTIVE'", nativeQuery = true)
  void updateSplSerialNo(String userId, Integer spNo);


}
