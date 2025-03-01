package com.ecom.pradeep.angadi_bk.specification;

import com.ecom.pradeep.angadi_bk.model.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {
        return (root, query, criteriaBuilder) ->
                name == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
    }

    public static Specification<Product> hasDescription(String description) {
        return (root, query, criteriaBuilder) ->
                description == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + description.toLowerCase() + "%"
                );
    }

    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) ->
                categoryId == null ? null : criteriaBuilder.equal(
                        root.get("category").get("id"), categoryId
                );
    }

    public static Specification<Product> hasStoreId(Long storeId) {
        return (root, query, criteriaBuilder) ->
                storeId == null ? null : criteriaBuilder.equal(
                        root.get("store").get("id"), storeId
                );
    }

    public static Specification<Product> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(
                        root.get("status"), status
                );
    }

    public static Specification<Product> isFeatured(Boolean featured) {
        return (root, query, criteriaBuilder) ->
                featured == null ? null : criteriaBuilder.equal(
                        root.get("featured"), featured
                );
    }

    public static Specification<Product> hasPriceGreaterThan(BigDecimal price) {
        return (root, query, criteriaBuilder) ->
                price == null ? null : criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), price
                );
    }

    public static Specification<Product> hasPriceLessThan(BigDecimal price) {
        return (root, query, criteriaBuilder) ->
                price == null ? null : criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), price
                );
    }

    public static Specification<Product> hasTag(Long tagId) {
        return (root, query, criteriaBuilder) -> {
            if (tagId == null) {
                return null;
            }

            Join<Object, Object> tags = root.join("tags", JoinType.INNER);
            return criteriaBuilder.equal(tags.get("id"), tagId);
        };
    }

    public static Specification<Product> hasTagName(String tagName) {
        return (root, query, criteriaBuilder) -> {
            if (tagName == null) {
                return null;
            }

            Join<Object, Object> tags = root.join("tags", JoinType.INNER);
            return criteriaBuilder.like(
                    criteriaBuilder.lower(tags.get("name")),
                    "%" + tagName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Product> isInStock(Boolean inStock) {
        return (root, query, criteriaBuilder) -> {
            if (inStock == null) {
                return null;
            }

            if (inStock) {
                return criteriaBuilder.or(
                        criteriaBuilder.greaterThan(root.get("stockQuantity"), 0),
                        criteriaBuilder.equal(root.get("stockQuantity"), -1)
                );
            } else {
                return criteriaBuilder.and(
                        criteriaBuilder.notEqual(root.get("stockQuantity"), -1),
                        criteriaBuilder.lessThanOrEqualTo(root.get("stockQuantity"), 0)
                );
            }
        };
    }

    public static Specification<Product> isLowStock(Boolean lowStock) {
        return (root, query, criteriaBuilder) -> {
            if (lowStock == null) {
                return null;
            }

            if (lowStock) {
                return criteriaBuilder.and(
                        criteriaBuilder.greaterThan(root.get("stockQuantity"), 0),
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("stockQuantity"),
                                root.get("lowStockThreshold")
                        )
                );
            } else {
                return criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("stockQuantity"), -1),
                        criteriaBuilder.greaterThan(
                                root.get("stockQuantity"),
                                root.get("lowStockThreshold")
                        )
                );
            }
        };
    }

    public static Specification<Product> hasDiscount(Boolean hasDiscount) {
        return (root, query, criteriaBuilder) -> {
            if (hasDiscount == null) {
                return null;
            }

            if (hasDiscount) {
                return criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("originalPrice")),
                        criteriaBuilder.greaterThan(
                                root.get("originalPrice"),
                                root.get("price")
                        )
                );
            } else {
                return criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("originalPrice")),
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("originalPrice"),
                                root.get("price")
                        )
                );
            }
        };
    }

    // Search across multiple fields
    public static Specification<Product> search(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return null;
            }

            String likeKeyword = "%" + keyword.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), likeKeyword)
            );
        };
    }
}