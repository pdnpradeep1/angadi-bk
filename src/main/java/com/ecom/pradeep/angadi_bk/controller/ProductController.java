package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.service.ImageUploadService;
import com.ecom.pradeep.angadi_bk.service.ProductService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final   ImageUploadService imageUploadService;

    public ProductController(ProductService productService, ImageUploadService imageUploadService) {
        this.productService = productService;
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/{storeId}")
    public Product createProduct(@PathVariable Long storeId, @RequestBody Product product, @RequestHeader("Owner-Email") String ownerEmail) {
        return productService.createProduct(storeId, product, ownerEmail);
    }

    @PutMapping("/{productId}")
    public Product updateProduct(@PathVariable Long productId, @RequestBody Product product, @RequestHeader("Owner-Email") String ownerEmail) {
        return productService.updateProduct(productId, product, ownerEmail);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable Long productId, @RequestHeader("Owner-Email") String ownerEmail) {
        productService.deleteProduct(productId, ownerEmail);
    }

    @GetMapping("/store/{storeId}")
    public List<Product> getProductsByStore(@PathVariable Long storeId) {
        return productService.getProductsByStore(storeId);
    }

    @PostMapping("/upload-image")
    public String uploadProductImage(@RequestParam("file") MultipartFile file) {

        return imageUploadService.uploadImage(file);
    }

    @GetMapping("/{productId}/in-stock")
    public boolean isProductInStock(@PathVariable Long productId) {
        return productService.isProductInStock(productId);
    }

    @PutMapping("/{productId}/tags")
    public Product addTagsToProduct(@PathVariable Long productId, @RequestBody Set<Long> tagIds, @RequestHeader("Owner-Email") String ownerEmail) {
        return productService.addTagsToProduct(productId, tagIds, ownerEmail);
    }

//    @GetMapping("/search")
//    public List<Product> searchProducts(
//            @RequestParam(required = false) String query,
//            @RequestParam(required = false) Long categoryId,
//            @RequestParam(required = false) Double minPrice,
//            @RequestParam(required = false) Double maxPrice,
//            @RequestParam(required = false) Boolean inStock
//    ) {
//        return productService.searchProducts(query, categoryId, minPrice, maxPrice, inStock);
//    }

    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable Long productId) {
//        return productService.getProductById(productId);
        return null;
    }

}
