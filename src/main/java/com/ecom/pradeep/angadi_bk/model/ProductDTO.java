package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stockQuantity;
    private String sku;
    private int lowStockThreshold = 5;
    private String imageUrl;
    private List<String> additionalImageUrls = new ArrayList<>();
    private boolean featured;
    private String status;
    private LocalDateTime publishedAt;
    private double averageRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long storeId;
    private String storeName;
    private Long categoryId;
    private String categoryName;
    private Set<TagDTO> tags = new HashSet<>();
    private List<ProductVariantDTO> variants = new ArrayList<>();
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Additional calculated fields
    private boolean inStock;
    private boolean lowStock;
    private boolean hasDiscount;
    private BigDecimal discountPercentage;

    // Nested DTO for Tag
    @Data
    public static class TagDTO {
        private Long id;
        private String name;

        public static TagDTO fromTag(Tag tag) {
            TagDTO dto = new TagDTO();
            dto.setId(tag.getId());
            dto.setName(tag.getName());
            return dto;
        }
    }

    // Convert from Entity to DTO
    public static ProductDTO fromProduct(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setSku(product.getSku());
        dto.setLowStockThreshold(product.getLowStockThreshold());
        dto.setImageUrl(product.getImageUrl());
        dto.setAdditionalImageUrls(product.getAdditionalImageUrls());
        dto.setFeatured(product.isFeatured());
        dto.setStatus(product.getStatus());
        dto.setPublishedAt(product.getPublishedAt());
        dto.setAverageRating(product.getAverageRating());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getStore() != null) {
            dto.setStoreId(product.getStore().getId());
            dto.setStoreName(product.getStore().getName());
        }

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Convert tags
        if (product.getTags() != null) {
            dto.setTags(product.getTags().stream()
                    .map(TagDTO::fromTag)
                    .collect(Collectors.toSet()));
        }

        // Convert variants
        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(ProductVariantDTO::fromProductVariant)
                    .collect(Collectors.toList()));
        }

        dto.setMetaTitle(product.getMetaTitle());
        dto.setMetaDescription(product.getMetaDescription());
        dto.setMetaKeywords(product.getMetaKeywords());

        // Calculate the additional fields
        dto.setInStock(product.isInStock());
        dto.setLowStock(product.isLowStock());
        dto.setHasDiscount(product.hasDiscount());
        dto.setDiscountPercentage(product.getDiscountPercentage());

        return dto;
    }

    // Convert from DTO to Entity (for updates)
    public void updateProduct(Product product) {
        product.setName(this.name);
        product.setDescription(this.description);
        product.setPrice(this.price);
        product.setOriginalPrice(this.originalPrice);
        product.setStockQuantity(this.stockQuantity);
        product.setSku(this.sku);
        product.setLowStockThreshold(this.lowStockThreshold);
        product.setImageUrl(this.imageUrl);
        product.setAdditionalImageUrls(this.additionalImageUrls);
        product.setFeatured(this.featured);
        product.setStatus(this.status);
        product.setMetaTitle(this.metaTitle);
        product.setMetaDescription(this.metaDescription);
        product.setMetaKeywords(this.metaKeywords);

        // Note: Category, Store, Tags, and Variants should be handled separately
        // as they require looking up entities from repositories
    }
}