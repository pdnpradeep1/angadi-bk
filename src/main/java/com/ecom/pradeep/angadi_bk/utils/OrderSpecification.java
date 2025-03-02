package com.ecom.pradeep.angadi_bk.utils;

import com.ecom.pradeep.angadi_bk.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {

    // Filter by store ID
    public static Specification<Order> hasStoreId(Long storeId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"), storeId);
    }

    // Filter by status
    public static Specification<Order> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    // Filter orders created on or after a date
    public static Specification<Order> createdAtGreaterThanOrEqual(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    // Filter orders created on or before a date
    public static Specification<Order> createdAtLessThanOrEqual(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    // Filter by total amount greater than or equal to value
    public static Specification<Order> totalAmountGreaterThanOrEqual(Double amount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), amount);
    }

    // Filter by total amount less than or equal to value
    public static Specification<Order> totalAmountLessThanOrEqual(Double amount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), amount);
    }

    // Filter by customer email
    public static Specification<Order> hasCustomerEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customer").get("email")),
                        "%" + email.toLowerCase() + "%"
                );
    }

    // Search by keyword (order number, customer name, email)
    public static Specification<Order> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + keyword.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("customer").get("name")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("customer").get("email")), likePattern)
            );
        };
    }
}