package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByProductIdOrderByTimestampDesc(Long productId);

    List<InventoryTransaction> findTop10ByProductStoreIdOrderByTimestampDesc(Long storeId);

    List<InventoryTransaction> findByProductStoreIdAndTypeOrderByTimestampDesc(
            Long storeId, InventoryTransaction.TransactionType type);
}