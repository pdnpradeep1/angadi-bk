package com.ecom.pradeep.angadi_bk.controller;
import com.ecom.pradeep.angadi_bk.model.Category;
import com.ecom.pradeep.angadi_bk.service.CategoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/{storeId}")
    public Category createCategory(@PathVariable Long storeId, @RequestBody Category category, @RequestHeader("Owner-Email") String ownerEmail) {
        return categoryService.createCategory(storeId, category, ownerEmail);
    }

    @PutMapping("/{categoryId}")
    public Category updateCategory(@PathVariable Long categoryId, @RequestBody Category category, @RequestHeader("Owner-Email") String ownerEmail) {
        return categoryService.updateCategory(categoryId, category, ownerEmail);
    }

    @DeleteMapping("/{categoryId}")
    public void deleteCategory(@PathVariable Long categoryId, @RequestHeader("Owner-Email") String ownerEmail) {
        categoryService.deleteCategory(categoryId, ownerEmail);
    }

    @GetMapping("/store/{storeId}")
    public List<Category> getCategoriesByStore(@PathVariable Long storeId) {
        return categoryService.getCategoriesByStore(storeId);
    }
}
