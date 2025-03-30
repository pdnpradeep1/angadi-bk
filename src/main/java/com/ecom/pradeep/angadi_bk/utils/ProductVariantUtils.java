package com.ecom.pradeep.angadi_bk.utils;

import com.ecom.pradeep.angadi_bk.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for transforming product and variant data
 */
public class ProductVariantUtils {

    /**
     * Convert product variants to DTOs
     * @param variants List of product variants
     * @return List of product variant DTOs
     */
    public static List<ProductVariantDTO> convertToVariantDTOs(List<ProductVariant> variants) {
        if (variants == null) {
            return Collections.emptyList();
        }

        return variants.stream()
                .map(ProductVariantDTO::fromProductVariant)
                .collect(Collectors.toList());
    }

    /**
     * Extract options map from variants
     * @param variants List of product variants
     * @return Map of option name to list of option values
     */
    public static Map<String, List<String>> extractOptionsMap(List<ProductVariant> variants) {
        Map<String, List<String>> optionsMap = new HashMap<>();

        if (variants == null || variants.isEmpty()) {
            return optionsMap;
        }

        // Process each variant
        for (ProductVariant variant : variants) {
            if (variant.getAttributes() == null || variant.getAttributes().isEmpty()) {
                continue;
            }

            // Process attributes from each variant
            for (Map.Entry<String, String> entry : variant.getAttributes().entrySet()) {
                String optionName = entry.getKey();
                String optionValue = entry.getValue();

                if (!optionsMap.containsKey(optionName)) {
                    optionsMap.put(optionName, new ArrayList<>());
                }

                List<String> values = optionsMap.get(optionName);
                if (!values.contains(optionValue)) {
                    values.add(optionValue);
                }
            }
        }

        return optionsMap;
    }

    /**
     * Create a variant name from its attributes
     * @param attributes Map of attribute name to attribute value
     * @return Generated variant name
     */
    public static String createVariantName(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "Default Variant";
        }

        return attributes.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" / "));
    }

    /**
     * Create a ProductVariant from a ProductVariantRequest
     * @param request The variant request
     * @param product The product
     * @return A new ProductVariant entity
     */
    public static ProductVariant createVariantFromRequest(ProductVariantRequest request, Product product) {
        ProductVariant variant = new ProductVariant();

        // Set up the composite ID
        ProductVariantId variantId = new ProductVariantId();
        variantId.setVariantId(request.getVariantId() != null ? request.getVariantId() : request.getId());
        variantId.setProductId(product.getId());
        variant.setId(variantId);

        // Set other properties
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setOriginalPrice(request.getOriginalPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setImageUrl(request.getImageUrl());
        variant.setAttributes(request.getAttributes());

        return variant;
    }

    /**
     * Update a ProductDTO with options map and variants details
     * @param dto The product DTO to update
     * @param variants The variants to extract options from
     * @return The updated product DTO
     */
    public static ProductDTO enrichProductDTOWithVariantInfo(ProductDTO dto, List<ProductVariant> variants) {
        // Convert to DTOs
        List<ProductVariantDTO> variantDTOs = convertToVariantDTOs(variants);

        // Get options map
        Map<String, List<String>> optionsMap = extractOptionsMap(variants);

        // Set values on DTO
        // Note: You would need to add these fields to the ProductDTO class

        return dto;
    }
}