package com.ecom.pradeep.angadi_bk.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@ToString(exclude = {"variants"})
@EqualsAndHashCode(exclude = {"variants"})
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 10000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Original price for showing discounts
    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // -1 means unlimited stock
    private int stockQuantity;

    // SKU (Stock Keeping Unit) - unique product identifier
    private String sku;

    // Low stock threshold for alerts
    private int lowStockThreshold = 5;

    // URL of the primary image
    private String imageUrl;

    // URLs of additional product images
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> additionalImageUrls = new ArrayList<>();

    // Featured flag for highlighting products
    private boolean featured = false;

    // Status: "Active", "Inactive", "Draft"
    @Column(nullable = false)
    private String status = "Active";

    // Date when the product was published (made active)
    private LocalDateTime publishedAt;

    // Average rating calculated from reviews
    private double averageRating = 0.0;

    // Creation timestamp
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Last update timestamp
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // Add one-to-many relationship with variants
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductVariant> variants = new ArrayList<>();

    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Business methods
    public boolean isInStock() {
        return this.stockQuantity > 0 || this.stockQuantity == -1;
    }

    public boolean isLowStock() {
        return this.stockQuantity > 0 && this.stockQuantity <= this.lowStockThreshold;
    }

    public boolean hasDiscount() {
        return this.originalPrice != null && this.originalPrice.compareTo(this.price) > 0;
    }

    public BigDecimal getDiscountPercentage() {
        if (!hasDiscount()) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.ONE
                .subtract(this.price.divide(this.originalPrice, 2, BigDecimal.ROUND_HALF_UP))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity == -1) {
            return; // Unlimited stock
        }

        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (this.stockQuantity == -1) {
            return; // Unlimited stock
        }

        this.stockQuantity += quantity;
    }

    public void publish() {
        this.status = "Active";
        this.publishedAt = LocalDateTime.now();
    }

    public void unpublish() {
        this.status = "Inactive";
    }

    // Method to update the average rating when a new review is added
    public void updateAverageRating() {
        if (this.reviews.isEmpty()) {
            this.averageRating = 0.0;
            return;
        }

        double sum = 0.0;
        for (Review review : this.reviews) {
            sum += review.getRating();
        }

        this.averageRating = sum / this.reviews.size();
    }

    // Helper method to add a variant
    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    // Helper method to remove a variant
    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }
}