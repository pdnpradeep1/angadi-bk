package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import com.ecom.pradeep.angadi_bk.model.LowStockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, Long> {
    public boolean existsByProductIdAndAcknowledgedFalse(Long id);

    public LowStockAlert save(LowStockAlert alert);

    public List<LowStockAlert> findByProductStoreIdOrderByCreatedAtDesc(Long storeId);

    List<LowStockAlert> findByProductStoreIdAndAcknowledgedFalseOrderByCreatedAtDesc(Long storeId);
}
