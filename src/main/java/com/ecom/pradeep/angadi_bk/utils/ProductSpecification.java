package com.ecom.pradeep.angadi_bk.utils;

import com.ecom.pradeep.angadi_bk.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilters(Long storeId, Long categoryId, String status,
                                                     BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
                                                     String searchTerm) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by store
            if (storeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("store").get("id"), storeId));
            }

            // Filter by category if provided
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Filter by status if provided
            if (status != null && !status.equalsIgnoreCase("all")) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by price range if provided
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Filter by stock if provided
            if (inStock != null) {
                if (inStock) {
                    // In stock: either stockQuantity > 0 or stockQuantity = -1 (unlimited)
                    predicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.greaterThan(root.get("stockQuantity"), 0),
                                    criteriaBuilder.equal(root.get("stockQuantity"), -1)
                            )
                    );
                } else {
                    // Out of stock: stockQuantity = 0
                    predicates.add(criteriaBuilder.equal(root.get("stockQuantity"), 0));
                }
            }

            // Search by name or description if search term is provided
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}