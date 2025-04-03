package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;

    // Add this field to your ProductService class
    private final InventoryTransactionRepository inventoryTransactionRepository;

    // Update the constructor to include the new repository
    public ProductService(ProductRepository productRepository,
                          ProductVariantRepository productVariantRepository,
                          StoreRepository storeRepository,
                          TagRepository tagRepository,
                          CategoryRepository categoryRepository,
                          InventoryTransactionRepository inventoryTransactionRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.storeRepository = storeRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    @Transactional
    public Product createProduct(Long storeId, ProductRequest productRequest, String ownerEmail) {
        // Validate store and ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to create product in this store");
        }

        // Find category if categoryId is provided
        Category category = null;
        if (productRequest.getCategoryId() != null && !productRequest.getCategoryId().isEmpty()) {
            Long categoryId = Long.parseLong(productRequest.getCategoryId());
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

            // Verify the category belongs to the correct store
            if (!category.getStore().getId().equals(storeId)) {
                throw new RuntimeException("Category does not belong to this store");
            }
        }

        // Process tags if provided
        Set<Tag> tags = new HashSet<>();
        if (productRequest.getTagIds() != null && !productRequest.getTagIds().isEmpty()) {
            tags = new HashSet<>(tagRepository.findAllById(productRequest.getTagIds()));
        }

        // Create new product from request
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setOriginalPrice(productRequest.getOriginalPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setSku(generateSku(productRequest.getName()));
        product.setLowStockThreshold(5); // Default value
        product.setImageUrl(productRequest.getImageUrl());
        product.setAdditionalImageUrls(productRequest.getAdditionalImageUrls());
        product.setFeatured(productRequest.isFeatured());
        product.setStatus(productRequest.getStatus());
        product.setMetaTitle(productRequest.getMetaTitle());
        product.setMetaDescription(productRequest.getMetaDescription());
        product.setMetaKeywords(productRequest.getMetaKeywords());
        product.setStore(store);
        product.setCategory(category);
        product.setTags(tags);
        product.setCreatedAt(LocalDateTime.now());

        if ("Active".equals(productRequest.getStatus())) {
            product.setPublishedAt(LocalDateTime.now());
        }

        // Save product first to get the ID
        Product savedProduct = productRepository.save(product);

        // Process variants if provided
        if (productRequest.getVariants() != null && !productRequest.getVariants().isEmpty()) {
            processProductVariants(savedProduct, productRequest.getVariants());
        }

        return savedProduct;
    }

    private String generateSku(String productName) {
        // Simple SKU generation based on product name and timestamp
        String namePart = productName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (namePart.length() > 5) {
            namePart = namePart.substring(0, 5);
        }
        return namePart + "-" + System.currentTimeMillis();
    }

    private void processProductVariants(Product product, List<ProductVariantRequest> variantRequests) {
        List<ProductVariant> variants = new ArrayList<>();

        for (ProductVariantRequest variantRequest : variantRequests) {
            ProductVariant variant = new ProductVariant();

            // Set up the composite ID
            ProductVariantId variantId = new ProductVariantId();
            // Use the provided ID or generate a new one
            Long providedVariantId = variantRequest.getVariantId() != null ?
                    variantRequest.getVariantId() : variantRequest.getId();

            variantId.setVariantId(providedVariantId != null ?
                    providedVariantId : System.currentTimeMillis());
            variantId.setProductId(product.getId());
            variant.setId(variantId);

            variant.setProduct(product);
            variant.setSku(variantRequest.getSku() != null ?
                    variantRequest.getSku() : generateVariantSku(product.getSku(), variants.size() + 1));
            variant.setPrice(variantRequest.getPrice());
            variant.setOriginalPrice(variantRequest.getOriginalPrice());
            variant.setStockQuantity(variantRequest.getStockQuantity());
            variant.setImageUrl(variantRequest.getImageUrl());
            variant.setAttributes(variantRequest.getAttributes());

            variants.add(variant);
        }

        // Save all variants
        productVariantRepository.saveAll(variants);
    }

    private String generateVariantSku(String productSku, int variantNumber) {
        return productSku + "-V" + variantNumber;
    }

    @Transactional
    public Product updateProduct(Long productId, ProductRequest updatedProduct, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update this product");
        }

        // Find category if categoryId is provided
        Category category = product.getCategory();
        if (updatedProduct.getCategoryId() != null && !updatedProduct.getCategoryId().isEmpty()) {
            Long categoryId = Long.parseLong(updatedProduct.getCategoryId());
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

            // Verify the category belongs to the correct store
            if (!category.getStore().getId().equals(product.getStore().getId())) {
                throw new RuntimeException("Category does not belong to this store");
            }
        }

        // Process tags if provided
        Set<Tag> tags = product.getTags();
        if (updatedProduct.getTagIds() != null) {
            tags = new HashSet<>(tagRepository.findAllById(updatedProduct.getTagIds()));
        }

        // Update basic product details
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setOriginalPrice(updatedProduct.getOriginalPrice());
        product.setStockQuantity(updatedProduct.getStockQuantity());
        product.setImageUrl(updatedProduct.getImageUrl());
        if (updatedProduct.getAdditionalImageUrls() != null) {
            product.setAdditionalImageUrls(updatedProduct.getAdditionalImageUrls());
        }
        product.setFeatured(updatedProduct.isFeatured());
        product.setStatus(updatedProduct.getStatus());
        product.setMetaTitle(updatedProduct.getMetaTitle());
        product.setMetaDescription(updatedProduct.getMetaDescription());
        product.setMetaKeywords(updatedProduct.getMetaKeywords());
        product.setCategory(category);
        product.setTags(tags);
        product.setUpdatedAt(LocalDateTime.now());

        // Update published date if becoming active
        if ("Active".equals(updatedProduct.getStatus()) && product.getPublishedAt() == null) {
            product.setPublishedAt(LocalDateTime.now());
        }

        // Save updated product
        Product savedProduct = productRepository.save(product);

        // Process variants if provided
        if (updatedProduct.getVariants() != null) {
            // Delete existing variants first
            productVariantRepository.deleteByProductId(productId);

            // Create new variants
            processProductVariants(savedProduct, updatedProduct.getVariants());
        }

        return savedProduct;
    }

    @Transactional
    public void deleteProduct(Long productId, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to delete this product");
        }

        // Delete associated inventory transactions first
        // You need to inject InventoryTransactionRepository
        inventoryTransactionRepository.deleteByProductId(productId);

        // Delete associated variants first
        productVariantRepository.deleteByProductId(productId);

        // Then delete the product
        productRepository.delete(product);
    }

    public Page<Product> getProductsByStore(Long storeId, Pageable pageable) {
        return productRepository.findByStoreId(storeId, pageable);
    }


    public List<Product> getProductsByStore(Long storeId) {
        return productRepository.findByStoreId(storeId);
    }

    public Page<Product> getFilteredProducts(
            Long storeId,
            Long categoryId,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            String searchTerm,
            Pageable pageable) {

        // Create specification for filtering
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by store
            if (storeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("store").get("id"), storeId));
            }

            // Filter by category if provided
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Filter by status if provided
            if (status != null && !status.equalsIgnoreCase("all")) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by price range if provided
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Filter by stock if provided
            if (inStock != null) {
                if (inStock) {
                    // In stock: either stockQuantity > 0 or stockQuantity = -1 (unlimited)
                    predicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.greaterThan(root.get("stockQuantity"), 0),
                                    criteriaBuilder.equal(root.get("stockQuantity"), -1)
                            )
                    );
                } else {
                    // Out of stock: stockQuantity = 0
                    predicates.add(criteriaBuilder.equal(root.get("stockQuantity"), 0));
                }
            }

            // Search by name or description if search term is provided
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable);
    }

    public boolean isProductInStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return product.isInStock();
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductDetails(Long storeId, Long productId, String ownerEmail) {
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId + " in store with id: " + storeId));

        // Verify ownership if ownerEmail is provided
        if (ownerEmail != null && !product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to view this product's details");
        }

        // Load variants for the product
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);

        // Set variants on the product entity for the DTO conversion
        product.setVariants(variants);

        // Convert to DTO
        return ProductDTO.fromProduct(product);
    }
}