package com.ecom.pradeep.angadi_bk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Description field for better categorization
    private String description;

    // Status to enable/disable categories
    @Column(nullable = false)
    private String status = "Active"; // "Active" or "Inactive"

    // Display order for custom sorting
    private Integer displayOrder = 0;

    // Optional parent-child relationship for hierarchical categories
    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    // Children categories if using hierarchical structure
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Category> children = new ArrayList<>();

    // Many categories belong to one store
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIgnore
    private Store store;

    // Image URL for category icon/thumbnail
    private String imageUrl;

    // Metadata for SEO purposes
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Slug for pretty URLs (e.g., "fresh-fruits" instead of "category/5")
    @Column(unique = true)
    private String slug;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Transient field (not stored in DB) to count products in this category
    @Transient
    private Long productCount;

    @Transient
    private Long parentId;

    @Version
    private Long version = 0L;




    // Helper method to set slug from name
    @PrePersist
    @PreUpdate
    private void prepareData() {
        if (slug == null || slug.trim().isEmpty()) {
            // Convert name to slug format (lowercase, hyphens instead of spaces)
            setSlug(name.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "") // Remove special chars
                    .replaceAll("\\s+", "-")); // Replace spaces with hyphens
        }
    }

    // Helper method to check if category is a parent category
    public boolean isParentCategory() {
        return parent == null;
    }

    // Helper method to check if category has children
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}