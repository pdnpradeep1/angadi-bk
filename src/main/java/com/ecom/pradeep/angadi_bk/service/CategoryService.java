package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Category;
import com.ecom.pradeep.angadi_bk.model.CategoryDTO;
import com.ecom.pradeep.angadi_bk.model.Store;
import com.ecom.pradeep.angadi_bk.repo.CategoryRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, StoreRepository storeRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    public Category createCategory(Long storeId, Category category, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to create category in this store");
        }

        category.setStore(store);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long categoryId, Category updatedCategory, String ownerEmail) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update this category");
        }

        category.setName(updatedCategory.getName());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId, String ownerEmail) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to delete this category");
        }

        categoryRepository.delete(category);
    }

    public List<CategoryDTO> getCategoriesByStore(Long storeId,String status,Boolean parentOnly) {
        List<Category> categories;

        // Determine which categories to fetch based on filters
        if (status != null && !status.isEmpty()) {
            if (Boolean.TRUE.equals(parentOnly)) {
                categories = categoryRepository.findByStoreIdAndStatusAndParentIsNullOrderByDisplayOrderAsc(storeId, status);
            } else {
                categories = categoryRepository.findByStoreIdAndStatusOrderByDisplayOrderAsc(storeId, status);
            }
        } else {
            if (Boolean.TRUE.equals(parentOnly)) {
                categories = categoryRepository.findByStoreIdAndParentIsNullOrderByDisplayOrderAsc(storeId);
            } else {
                categories = categoryRepository.findByStoreIdOrderByDisplayOrderAsc(storeId);
            }
        }

        // Convert entities to DTOs
        return categories.stream()
                .map(category -> {
                    CategoryDTO dto = mapToCategoryDTO(category);

                    // Get product count
                    Long productCount = productRepository.countByCategoryId(category.getId());
                    dto.setProductCount(productCount);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getCategoryHierarchy(Long storeId) {
        // Get all parent categories
        List<Category> parentCategories = categoryRepository.findByStoreIdAndParentIsNullOrderByDisplayOrderAsc(storeId);

        // Convert to DTOs with nested children
        return parentCategories.stream()
                .map(parent -> {
                    CategoryDTO parentDTO = mapToCategoryDTO(parent);

                    // Get product count for parent
                    Long parentProductCount = productRepository.countByCategoryId(parent.getId());
                    parentDTO.setProductCount(parentProductCount);

                    // Get children
                    List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrderAsc(parent.getId());

                    // Map children to DTOs
                    List<CategoryDTO> childrenDTOs = children.stream()
                            .map(child -> {
                                CategoryDTO childDTO = mapToCategoryDTO(child);

                                // Get product count for child
                                Long childProductCount = productRepository.countByCategoryId(child.getId());
                                childDTO.setProductCount(childProductCount);

                                return childDTO;
                            })
                            .collect(Collectors.toList());

                    // Set children in parent DTO
                    parentDTO.setChildren(childrenDTOs);

                    return parentDTO;
                })
                .collect(Collectors.toList());
    }

    // Helper method to map Category entity to CategoryDTO
    private CategoryDTO mapToCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setStatus(category.getStatus());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setSlug(category.getSlug());
        dto.setImageUrl(category.getImageUrl());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        // Add parent information if available
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        return dto;
    }
}
