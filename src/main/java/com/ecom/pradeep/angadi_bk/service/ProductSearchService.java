package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.ProductDTO;
import com.ecom.pradeep.angadi_bk.model.SearchCriteria;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {
    private final ProductRepository productRepository;

    public ProductSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Search products with multiple criteria
     */
    public Page<ProductDTO> searchProducts(SearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = buildSpecification(criteria);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::fromProduct)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOs, pageable, productPage.getTotalElements());
    }

    private Specification<Product> buildSpecification(SearchCriteria criteria) {
        Specification<Product> spec = Specification.where(null);

        // Start with store filter - almost always required
        if (criteria.getStoreId() != null) {
            spec = spec.and(ProductSpecification.hasStoreId(criteria.getStoreId()));
        }

        // General search keyword across multiple fields
        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            spec = spec.and(ProductSpecification.search(criteria.getKeyword()));
        }

        // Product status (active, inactive, draft)
        if (criteria.getStatus() != null) {
            spec = spec.and(ProductSpecification.hasStatus(criteria.getStatus()));
        }

        // Category filter
        if (criteria.getCategoryId() != null) {
            spec = spec.and(ProductSpecification.hasCategoryId(criteria.getCategoryId()));
        }

        // Price range filters
        if (criteria.getMinPrice() != null) {
            spec = spec.and(ProductSpecification.hasPriceGreaterThan(BigDecimal.valueOf(criteria.getMinPrice())));
        }

        if (criteria.getMaxPrice() != null) {
            spec = spec.and(ProductSpecification.hasPriceLessThan(BigDecimal.valueOf(criteria.getMaxPrice())));
        }

        // Stock filters
        if (criteria.getInStock() != null) {
            spec = spec.and(ProductSpecification.isInStock(criteria.getInStock()));
        }

        if (criteria.getLowStock() != null) {
            spec = spec.and(ProductSpecification.isLowStock(criteria.getLowStock()));
        }

        // Featured products filter
        if (criteria.getFeatured() != null) {
            spec = spec.and(ProductSpecification.isFeatured(criteria.getFeatured()));
        }

        // Discount filter
        if (criteria.getOnSale() != null) {
            spec = spec.and(ProductSpecification.hasDiscount(criteria.getOnSale()));
        }

        // Tag filter
        if (criteria.getTagId() != null) {
            spec = spec.and(ProductSpecification.hasTag(criteria.getTagId()));
        }

        if (criteria.getTagName() != null && !criteria.getTagName().isEmpty()) {
            spec = spec.and(ProductSpecification.hasTagName(criteria.getTagName()));
        }

        return spec;
    }

    /**
     * Simple search by keyword in store
     */
    public List<ProductDTO> quickSearch(Long storeId, String keyword) {
        Specification<Product> spec = Specification
                .where(ProductSpecification.hasStoreId(storeId))
                .and(ProductSpecification.search(keyword))
                .and(ProductSpecification.hasStatus("Active"));

        return productRepository.findAll(spec).stream()
                .map(ProductDTO::fromProduct)
                .collect(Collectors.toList());
    }

    /**
     * Get featured products for a store
     */
    public List<ProductDTO> getFeaturedProducts(Long storeId) {
        return productRepository.findByStoreIdAndFeaturedTrue(storeId).stream()
                .map(ProductDTO::fromProduct)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock products for a store
     */
    public List<ProductDTO> getLowStockProducts(Long storeId) {
        return productRepository.findLowStockProductsByStore(storeId).stream()
                .map(ProductDTO::fromProduct)
                .collect(Collectors.toList());
    }
}