package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.ProductDTO;
import com.ecom.pradeep.angadi_bk.model.ProductRequest;
import com.ecom.pradeep.angadi_bk.service.ImageUploadService;
import com.ecom.pradeep.angadi_bk.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ImageUploadService imageUploadService;

    public ProductController(ProductService productService, ImageUploadService imageUploadService) {
        this.productService = productService;
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/{storeId}")
    public Product createProduct(@PathVariable Long storeId, @RequestBody ProductRequest productRequest, @RequestHeader("Owner-Email") String ownerEmail) {
        return productService.createProduct(storeId, productRequest, ownerEmail);
    }

    @PutMapping("/{productId}")
    public Product updateProduct(@PathVariable Long productId, @RequestBody Product product, @RequestHeader("Owner-Email") String ownerEmail) {
        return productService.updateProduct(productId, product, ownerEmail);
    }

    @DeleteMapping("/{storeId}/{productId}")
    public void deleteProduct(@PathVariable Long productId, @RequestHeader("Owner-Email") String ownerEmail) {
        productService.deleteProduct(productId, ownerEmail);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getProductsByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        // Parse sort parameters
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // If no filters, return simple list
        if (categoryId == null && status == null && minPrice == null &&
                maxPrice == null && inStock == null && (search == null || search.isEmpty())) {
            return ResponseEntity.ok(productService.getProductsByStore(storeId));
        }

        // Otherwise use filtering
        Page<Product> products = productService.getFilteredProducts(
                storeId, categoryId, status, minPrice, maxPrice, inStock, search, pageable);

        return ResponseEntity.ok(products);
    }

    @PostMapping("/upload-image")
    public String uploadProductImage(@RequestParam("file") MultipartFile file) {
        return imageUploadService.uploadImage(file);
    }

    @GetMapping("/{productId}/in-stock")
    public boolean isProductInStock(@PathVariable Long productId) {
        return productService.isProductInStock(productId);
    }

    @GetMapping("/{storeId}/{productId}")
    public ProductDTO getProductDetails(@PathVariable Long storeId, @PathVariable Long productId, @RequestHeader("Owner-Email") String ownerEmail) {
        return productService.getProductDetails(storeId, productId, ownerEmail);
    }

    @PutMapping("/{productId}/tags")
    public Product addTagsToProduct(@PathVariable Long productId, @RequestBody Set<Long> tagIds, @RequestHeader("Owner-Email") String ownerEmail) {
        return productService.addTagsToProduct(productId, tagIds, ownerEmail);
    }

    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable Long productId) {
        // This is a placeholder or stub method and needs implementation
        return null;
    }
}