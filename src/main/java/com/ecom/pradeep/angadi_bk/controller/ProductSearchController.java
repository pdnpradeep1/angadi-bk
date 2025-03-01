package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.ProductDTO;
import com.ecom.pradeep.angadi_bk.model.SearchCriteria;
import com.ecom.pradeep.angadi_bk.service.ProductSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/search")
public class ProductSearchController {
    private final ProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setStoreId(storeId);
        criteria.setKeyword(keyword);
        criteria.setStatus(status);
        criteria.setCategoryId(categoryId);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setInStock(inStock);
        criteria.setLowStock(lowStock);
        criteria.setFeatured(featured);
        criteria.setOnSale(onSale);
        criteria.setTagId(tagId);
        criteria.setTagName(tagName);
        criteria.setSortBy(sortBy);
        criteria.setSortDirection(sortDir);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductDTO> products = productSearchService.searchProducts(criteria, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/quick")
    public ResponseEntity<List<ProductDTO>> quickSearch(
            @RequestParam Long storeId,
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(productSearchService.quickSearch(storeId, keyword));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts(
            @RequestParam Long storeId
    ) {
        return ResponseEntity.ok(productSearchService.getFeaturedProducts(storeId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(
            @RequestParam Long storeId
    ) {
        return ResponseEntity.ok(productSearchService.getLowStockProducts(storeId));
    }
}