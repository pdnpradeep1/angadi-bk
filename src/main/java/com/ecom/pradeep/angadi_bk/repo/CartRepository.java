package com.ecom.pradeep.angadi_bk.repo;
import com.ecom.pradeep.angadi_bk.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByCustomerId(Long customerId);
    Optional<Cart> findByCustomerIdAndProductId(Long customerId, Long productId);
}
