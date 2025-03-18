package com.ecom.pradeep.angadi_bk.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    // SKU for this specific variant
    @Column(nullable = false)
    private String sku;

    // Variant can have its own price
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Original price (for showing discounts)
    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // Variant-specific stock
    private int stockQuantity;

    // Variant-specific image
    private String imageUrl;

    // Attributes like color, size, etc.
    @ElementCollection
    @CollectionTable(name = "variant_attributes",
            joinColumns = @JoinColumn(name = "variant_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    // Business methods
    public boolean isInStock() {
        return this.stockQuantity > 0 || this.stockQuantity == -1;
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
}