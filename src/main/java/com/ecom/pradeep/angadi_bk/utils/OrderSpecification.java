package com.ecom.pradeep.angadi_bk.utils;

import com.ecom.pradeep.angadi_bk.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification implements Specification<Order> {
    private Specification<Order> spec;

    public OrderSpecification() {
        this.spec = Specification.where(null);
    }

    public OrderSpecification(Specification<Order> spec) {
        this.spec = spec;
    }

    @Override
    public jakarta.persistence.criteria.Predicate toPredicate(
            jakarta.persistence.criteria.Root<Order> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        return this.spec.toPredicate(root, query, criteriaBuilder);
    }

    public OrderSpecification and(Specification<Order> other) {
        this.spec = this.spec.and(other);
        return this;
    }

    public OrderSpecification or(Specification<Order> other) {
        this.spec = this.spec.or(other);
        return this;
    }

    public static Specification<Order> hasStoreId(Long storeId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<Order> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Order> createdAtGreaterThanOrEqual(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Order> createdAtLessThanOrEqual(LocalDateTime date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Order> totalAmountGreaterThanOrEqual(Double amount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), amount);
    }

    public static Specification<Order> totalAmountLessThanOrEqual(Double amount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), amount);
    }

    public static Specification<Order> hasCustomerEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customer").get("email")),
                        "%" + email.toLowerCase() + "%"
                );
    }

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