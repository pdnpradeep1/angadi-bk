package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Category;
import com.ecom.pradeep.angadi_bk.model.CategoryDTO;
import com.ecom.pradeep.angadi_bk.model.CategoryOrderRequest;
import com.ecom.pradeep.angadi_bk.service.CategoryService;
import com.ecom.pradeep.angadi_bk.service.ImageUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final ImageUploadService imageUploadService;

    public CategoryController(CategoryService categoryService, ImageUploadService imageUploadService) {
        this.categoryService = categoryService;
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/{storeId}")
    public ResponseEntity<CategoryDTO> createCategory(
            @PathVariable Long storeId,
            @RequestBody Category category,
            @RequestHeader("Owner-Email") String ownerEmail) {
        CategoryDTO createdCategory = categoryService.createCategory(storeId, category, ownerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody Category category,
            @RequestHeader("Owner-Email") String ownerEmail) {
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, category, ownerEmail);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId,
            @RequestHeader("Owner-Email") String ownerEmail) {
        categoryService.deleteCategory(categoryId, ownerEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean parentOnly) {
        List<CategoryDTO> categoryDTOs = categoryService.getCategoriesByStore(storeId, status, parentOnly);
        return ResponseEntity.ok(categoryDTOs);
    }

    @GetMapping("/store/{storeId}/hierarchy")
    public ResponseEntity<List<CategoryDTO>> getCategoryHierarchy(@PathVariable Long storeId) {
        List<CategoryDTO> hierarchy = categoryService.getCategoryHierarchy(storeId);
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Long categoryId) {
        CategoryDTO category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadCategoryImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageUploadService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }

    @PutMapping("/store/{storeId}/order")
    public ResponseEntity<Void> updateCategoryOrder(
            @PathVariable Long storeId,
            @RequestBody List<CategoryOrderRequest> orderRequests,
            @RequestHeader("Owner-Email") String ownerEmail) {
        categoryService.updateCategoryOrder(storeId, orderRequests, ownerEmail);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/store/{storeId}/hierarchy")
    public ResponseEntity<Void> updateCategoryHierarchy(
            @PathVariable Long storeId,
            @RequestBody List<Map<String, Object>> hierarchyData,
            @RequestHeader("Owner-Email") String ownerEmail) {
        categoryService.updateCategoryHierarchy(storeId, hierarchyData, ownerEmail);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{categoryId}/status")
    public ResponseEntity<CategoryDTO> updateCategoryStatus(
            @PathVariable Long categoryId,
            @RequestBody Map<String, String> statusUpdate,
            @RequestHeader("Owner-Email") String ownerEmail) {
        String status = statusUpdate.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }

        CategoryDTO updatedCategory = categoryService.updateCategoryStatus(categoryId, status, ownerEmail);
        return ResponseEntity.ok(updatedCategory);
    }
}