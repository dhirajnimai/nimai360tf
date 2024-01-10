package com.nimai.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nimai.admin.model.NimaiFBlkgoods;
import com.nimai.admin.model.NimaiMCustomer;



public interface BlackListedGoodsRepository extends JpaRepository<NimaiFBlkgoods, Long> {

	//List<NimaiFBlkgoods> findByUserId(NimaiMCustomer nc);
	
//	@Modifying
//	@Query("delete from BlackListedGoods b where b.userId.userid =:userId")
//	void deleteBlackListedGoodsUserId(@Param("userId") String userId);
}
