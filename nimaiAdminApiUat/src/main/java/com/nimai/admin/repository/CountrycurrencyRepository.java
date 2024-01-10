package com.nimai.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nimai.admin.model.Countrycurrency;

@Repository
@Transactional
public interface CountrycurrencyRepository extends JpaRepository<Countrycurrency, String>{
	@Query(value= "SELECT * FROM countrycurrency", nativeQuery = true)
	List<Countrycurrency> getCountry();
	
	@Query(value= "SELECT DISTINCT currency FROM nimai_m_currency where currency NOT LIKE '' ORDER BY  currency asc", nativeQuery = true)
	List<String> getCurrency();

}

