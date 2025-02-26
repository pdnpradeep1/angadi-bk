package com.ecom.pradeep.angadi_bk.repo;
import com.ecom.pradeep.angadi_bk.model.StoreRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoreRevenueRepository extends JpaRepository<StoreRevenue, Long> {

    @Query("SELECT SUM(s.amount) FROM StoreRevenue s WHERE s.storeId = :storeId AND s.date BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumRevenueBetweenDates(Long storeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM StoreRevenue s WHERE s.storeId = :storeId AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date ASC")
    List<StoreRevenue> findRevenueBetweenDates(Long storeId, LocalDate startDate, LocalDate endDate);

}

