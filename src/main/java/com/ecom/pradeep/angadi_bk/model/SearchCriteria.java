package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;

@Data
public class SearchCriteria {
    private Long storeId;
    private String keyword;
    private String status;
    private Long categoryId;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStock;
    private Boolean lowStock;
    private Boolean featured;
    private Boolean onSale;
    private Long tagId;
    private String tagName;

    // Optional sorting instructions
    private String sortBy;
    private String sortDirection;
}