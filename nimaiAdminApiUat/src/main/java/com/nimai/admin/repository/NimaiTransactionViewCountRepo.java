package com.nimai.admin.repository;

import com.nimai.admin.model.NimaiTransactionViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface NimaiTransactionViewCountRepo extends JpaRepository<NimaiTransactionViewCount, Long> {

    @Query(value = "SELECT * FROM nimai_trnx_view_count tvc WHERE tvc.USER_ID=(:userId) ORDER BY tvc.ID DESC LIMIT 1", nativeQuery = true)
    NimaiTransactionViewCount getViewCountById(String userId);

    @Query(value = "SELECT * FROM nimai_trnx_view_count tvc WHERE tvc.USER_ID=(:userId) ORDER BY tvc.ID DESC LIMIT 2", nativeQuery = true)
    List<NimaiTransactionViewCount> getLastTwoRowsById(String userId);
}
