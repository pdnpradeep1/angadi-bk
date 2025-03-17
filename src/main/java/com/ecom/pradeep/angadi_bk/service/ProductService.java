package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.CategoryRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import com.ecom.pradeep.angadi_bk.repo.TagRepository;
import com.ecom.pradeep.angadi_bk.utils.ProductSpecification;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;


    public ProductService(ProductRepository productRepository, StoreRepository storeRepository, TagRepository tagRepository, CategoryRepository categoryRepository, EntityManager entityManager) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
    }

    public Product createProduct(Long storeId, ProductRequest productRequest, String ownerEmail) {
        // Validate store and ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

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

        // Create new product from request
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setOriginalPrice(productRequest.getOriginalPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setImageUrl(productRequest.getImageUrl());
        product.setStatus(productRequest.getStatus());
        product.setStore(store);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());

        // Save and return the product
        return productRepository.save(product);
    }

    public Product updateProduct(Long productId, Product updatedProduct, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update this product");
        }

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setStockQuantity(updatedProduct.getStockQuantity());
        product.setImageUrl(updatedProduct.getImageUrl());

        return productRepository.save(product);
    }

    public void deleteProduct(Long productId, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to delete this product");
        }

        productRepository.delete(product);
    }

    public List<Product> getProductsByStore(Long storeId) {
        return productRepository.findByStoreId(storeId);
    }

    // New method for filtered products
    public Page<Product> getFilteredProducts(
            Long storeId,
            Long categoryId,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            String searchTerm,
            Pageable pageable) {

        Specification<Product> spec = ProductSpecification.withFilters(
                storeId, categoryId, status, minPrice, maxPrice, inStock, searchTerm);

        return productRepository.findAll(spec, pageable);
    }

    public boolean isProductInStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return product.getStockQuantity() > 0;
    }

    public Product createProduct(Long storeId, Long categoryId, Product product, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categor`y not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to create product in this store");
        }

        product.setStore(store);
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Product addTagsToProduct(Long productId, Set<Long> tagIds, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Verify ownership
        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update this product");
        }

        // Get all tags by IDs
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));

        // Set the tags on the product
        product.setTags(tags);

        return productRepository.save(product);
    }

    public ProductDTO getProductDetails(Long storeId, Long productId, String ownerEmail) {
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Convert to DTO
        return convertToProductDTO(product);
    }

    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setSku(product.getSku());
        dto.setImageUrl(product.getImageUrl());
        dto.setStatus(product.getStatus());

        // Set category info without circular references
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }
}