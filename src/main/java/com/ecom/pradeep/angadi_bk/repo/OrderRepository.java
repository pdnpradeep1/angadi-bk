package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.customer.id = :customerId AND o.product.id = :productId")
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
}
