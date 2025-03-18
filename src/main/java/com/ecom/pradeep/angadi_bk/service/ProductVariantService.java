package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.ProductVariant;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public ProductVariantService(ProductVariantRepository productVariantRepository, ProductRepository productRepository) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
    }

    public List<ProductVariant> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }

    @Transactional
    public List<ProductVariant> saveVariants(Long productId, List<ProductVariant> variants, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Verify ownership
        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to modify this product");
        }

        // Set product reference on each variant
        for (ProductVariant variant : variants) {
            variant.setProduct(product);
        }

        // Save all variants
        return productVariantRepository.saveAll(variants);
    }

    @Transactional
    public ProductVariant updateVariant(Long variantId, ProductVariant updatedVariant, String ownerEmail) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        // Verify ownership
        if (!variant.getProduct().getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to modify this variant");
        }

        // Update fields while preserving the product reference
        variant.setSku(updatedVariant.getSku());
        variant.setPrice(updatedVariant.getPrice());
        variant.setStockQuantity(updatedVariant.getStockQuantity());
        variant.setImageUrl(updatedVariant.getImageUrl());
        variant.setAttributes(updatedVariant.getAttributes());

        return productVariantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(Long variantId, String ownerEmail) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        // Verify ownership
        if (!variant.getProduct().getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to delete this variant");
        }

        productVariantRepository.delete(variant);
    }

    @Transactional
    public ProductVariant patchVariant(Long variantId, Map<String, Object> fields, String ownerEmail) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        // Verify ownership
        if (!variant.getProduct().getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to modify this variant");
        }

        // Apply field updates
        if (fields.containsKey("sku")) {
            variant.setSku((String) fields.get("sku"));
        }
        if (fields.containsKey("price")) {
            variant.setPrice(java.math.BigDecimal.valueOf(Double.parseDouble(fields.get("price").toString())));
        }
        if (fields.containsKey("stockQuantity")) {
            variant.setStockQuantity(Integer.parseInt(fields.get("stockQuantity").toString()));
        }
        if (fields.containsKey("imageUrl")) {
            variant.setImageUrl((String) fields.get("imageUrl"));
        }
        if (fields.containsKey("attributes")) {
            // Handle attributes update - implementation depends on how attributes are stored
            @SuppressWarnings("unchecked")
            Map<String, String> attributes = (Map<String, String>) fields.get("attributes");
            variant.setAttributes(attributes);
        }

        return productVariantRepository.save(variant);
    }

    @Transactional
    public List<ProductVariant> replaceAllVariants(Long productId, List<ProductVariant> newVariants, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Verify ownership
        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to modify this product");
        }

        // Delete existing variants
        productVariantRepository.deleteByProductId(productId);

        // Set product reference on each new variant
        for (ProductVariant variant : newVariants) {
            variant.setProduct(product);
        }

        // Save all new variants
        return productVariantRepository.saveAll(newVariants);
    }
}