package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    List<Review> findByProductId(Long productId, Sort sort);
    Page<Review> findByProductId(Long productId, Pageable pageable);


    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.id = :productId")
    double calculateAverageRating(Long productId);
}
