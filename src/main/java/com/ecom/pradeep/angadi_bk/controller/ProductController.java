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
    public ResponseEntity<Product> createProduct(
            @PathVariable Long storeId,
            @RequestBody ProductRequest productRequest,
            @RequestHeader("Owner-Email") String ownerEmail) {
        Product createdProduct = productService.createProduct(storeId, productRequest, ownerEmail);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductRequest productRequest,
            @RequestHeader("Owner-Email") String ownerEmail) {
        Product updatedProduct = productService.updateProduct(productId, productRequest, ownerEmail);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @RequestHeader("Owner-Email") String ownerEmail) {
        productService.deleteProduct(productId, ownerEmail);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(imageUploadService.uploadImage(file));
    }

    @GetMapping("/{productId}/in-stock")
    public ResponseEntity<Boolean> isProductInStock(@PathVariable Long productId) {
        boolean inStock = productService.isProductInStock(productId);
        return ResponseEntity.ok(inStock);
    }

    @GetMapping("/{storeId}/{productId}")
    public ResponseEntity<ProductDTO> getProductDetails(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @RequestHeader(value = "Owner-Email", required = false) String ownerEmail) {
        ProductDTO productDTO = productService.getProductDetails(storeId, productId, ownerEmail);
        return ResponseEntity.ok(productDTO);
    }

    @PutMapping("/{productId}/tags")
    public ResponseEntity<Product> addTagsToProduct(
            @PathVariable Long productId,
            @RequestBody Set<Long> tagIds,
            @RequestHeader("Owner-Email") String ownerEmail) {
        Product updatedProduct = productService.addTagsToProduct(productId, tagIds, ownerEmail);
        return ResponseEntity.ok(updatedProduct);
    }
    }