package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Category;
import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.Store;
import com.ecom.pradeep.angadi_bk.model.Tag;
import com.ecom.pradeep.angadi_bk.repo.CategoryRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import com.ecom.pradeep.angadi_bk.repo.TagRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

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

    public Product createProduct(Long storeId, Product product, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to create product in this store");
        }

        product.setStore(store);
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
    public boolean isProductInStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return product.getStockQuantity() > 0;
    }

    public Product createProduct(Long storeId, Long categoryId, Product product, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to create product in this store");
        }

        product.setStore(store);
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Product addTagsToProduct(Long productId, Set<Long> tagIds, String ownerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update product tags");
        }

        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        product.setTags(tags);
        return productRepository.save(product);
    }

//    @Cacheable(value = "productSearch", key = "#query + #categoryId + #minPrice + #maxPrice + #inStock")
//    public List<Product> searchProducts(String query, Long categoryId, Double minPrice, Double maxPrice, Boolean inStock) {
//        SearchSession searchSession = Search.session(entityManager.unwrap(Session.class));
//
//
//        return searchSession.search(Product.class)
//                .where(f -> {
//                    var bool = f.bool();
//
//                    if (query != null && !query.isEmpty()) {
//                        bool.must(f.match().fields("name", "description").matching(query));
//                    }
//                    if (categoryId != null) {
//                        bool.must(f.match().field("category.id").matching(categoryId));
//                    }
//                    if (minPrice != null) {
//                        bool.must(f.range().field("price").atLeast(minPrice));
//                    }
//                    if (maxPrice != null) {
//                        bool.must(f.range().field("price").atMost(maxPrice));
//                    }
//                    if (inStock != null && inStock) {
//                        bool.must(f.range().field("stockQuantity").greaterThan(0));
//                    }
//
//                    return bool;
//                })
//                .fetchAllHits();
//    }



}
