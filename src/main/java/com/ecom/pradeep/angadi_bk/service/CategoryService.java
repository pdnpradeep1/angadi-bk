package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.Category;
import com.ecom.pradeep.angadi_bk.model.CategoryDTO;
import com.ecom.pradeep.angadi_bk.model.CategoryOrderRequest;
import com.ecom.pradeep.angadi_bk.model.Store;
import com.ecom.pradeep.angadi_bk.repo.CategoryRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CategoryService(CategoryRepository categoryRepository, StoreRepository storeRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CategoryDTO createCategory(Long storeId, Category category, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to create category in this store");
        }

        // Create a new Category instance to avoid detached entity issues
        Category newCategory = new Category();
        newCategory.setName(category.getName());
        newCategory.setDescription(category.getDescription());
        newCategory.setStatus(category.getStatus() != null ? category.getStatus() : "Active");
        newCategory.setImageUrl(category.getImageUrl());
        newCategory.setStore(store);

        // Set display order
        if (category.getDisplayOrder() == null) {
            Integer maxOrder = categoryRepository.findMaxDisplayOrderByStoreId(storeId);
            newCategory.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 0);
        } else {
            newCategory.setDisplayOrder(category.getDisplayOrder());
        }

        // Set parent if parentId is provided
        if (category.getParentId() != null) {
            Category parent = categoryRepository.findById(category.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            newCategory.setParent(parent);
        } else {
            newCategory.setParent(null);
        }

        // Initialize version explicitly
        newCategory.setVersion(0L);

        Category savedCategory = categoryRepository.save(newCategory);

        // Convert to DTO and set product count
        CategoryDTO dto = mapToCategoryDTO(savedCategory);
        dto.setProductCount(0L); // New category has no products

        return dto;
    }

    @Transactional
    public CategoryDTO updateCategory(Long categoryId, Category updatedCategory, String ownerEmail) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (!category.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update this category");
        }

        // Check for circular reference if parent is being updated
        if (updatedCategory.getParentId() != null) {
            if (updatedCategory.getParentId().equals(categoryId)) {
                throw new RuntimeException("A category cannot be its own parent");
            }

            // Check if new parent would create a circular reference
            if (isDescendant(updatedCategory.getParentId(), categoryId)) {
                throw new RuntimeException("Cannot make a subcategory the parent of its ancestor");
            }

            Category parent = categoryRepository.findById(updatedCategory.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        // Update fields
        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        category.setStatus(updatedCategory.getStatus());
        if (updatedCategory.getImageUrl() != null) {
            category.setImageUrl(updatedCategory.getImageUrl());
        }
        category.setUpdatedAt(LocalDateTime.now());

        Category saved = categoryRepository.save(category);

        // Convert to DTO with product count
        CategoryDTO dto = mapToCategoryDTO(saved);
        Long productCount = productRepository.countByCategoryId(saved.getId());
        dto.setProductCount(productCount);

        return dto;
    }

    @Transactional
    public void deleteCategory(Long categoryId, String ownerEmail) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (!category.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to delete this category");
        }

        // Delete all child categories recursively
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrderAsc(categoryId);
        for (Category child : children) {
            deleteCategory(child.getId(), ownerEmail);
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByStore(Long storeId, String status, Boolean parentOnly) {
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

        // Convert entities to DTOs with product counts
        return categories.stream()
                .map(category -> {
                    CategoryDTO dto = mapToCategoryDTO(category);
                    Long productCount = productRepository.countByCategoryId(category.getId());
                    dto.setProductCount(productCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryHierarchy(Long storeId) {
        // Get all parent categories
        List<Category> parentCategories = categoryRepository.findByStoreIdAndParentIsNullOrderByDisplayOrderAsc(storeId);

        // Convert to DTOs with nested children
        return buildCategoryHierarchy(parentCategories);
    }

    private List<CategoryDTO> buildCategoryHierarchy(List<Category> parentCategories) {
        return parentCategories.stream()
                .map(this::buildCategoryBranch)
                .collect(Collectors.toList());
    }

    private CategoryDTO buildCategoryBranch(Category category) {
        CategoryDTO dto = mapToCategoryDTO(category);

        // Set product count
        Long productCount = productRepository.countByCategoryId(category.getId());
        dto.setProductCount(productCount);

        // Get and process children
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrderAsc(category.getId());
        if (!children.isEmpty()) {
            List<CategoryDTO> childrenDTOs = children.stream()
                    .map(this::buildCategoryBranch)
                    .collect(Collectors.toList());
            dto.setChildren(childrenDTOs);
        } else {
            dto.setChildren(Collections.emptyList());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        CategoryDTO dto = mapToCategoryDTO(category);
        Long productCount = productRepository.countByCategoryId(categoryId);
        dto.setProductCount(productCount);

        return dto;
    }

    @Transactional
    public void updateCategoryOrder(Long storeId, List<CategoryOrderRequest> orderRequests, String ownerEmail) {
        // Verify store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update category order in this store");
        }

        // Update orders for each category
        for (CategoryOrderRequest request : orderRequests) {
            Category category = categoryRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getId()));

            // Verify category belongs to the store
            if (!category.getStore().getId().equals(storeId)) {
                throw new RuntimeException("Category does not belong to this store");
            }

            category.setDisplayOrder(request.getOrder());
            categoryRepository.save(category);
        }
    }

    @Transactional
    public void updateCategoryHierarchy(Long storeId, List<Map<String, Object>> hierarchyData, String ownerEmail) {
        // Verify store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update category hierarchy in this store");
        }

        // Process hierarchy data
        for (Map<String, Object> item : hierarchyData) {
            Long categoryId = Long.valueOf(item.get("id").toString());
            Integer order = (Integer) item.get("order");
            Object parentIdObj = item.get("parentId");
            Long parentId = parentIdObj != null ? Long.valueOf(parentIdObj.toString()) : null;

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

            // Verify category belongs to the store
            if (!category.getStore().getId().equals(storeId)) {
                throw new RuntimeException("Category does not belong to this store");
            }

            // Update parent if needed
            if (parentId != null) {
                if (parentId.equals(categoryId)) {
                    throw new RuntimeException("A category cannot be its own parent");
                }

                // Check for circular reference
                if (isDescendant(parentId, categoryId)) {
                    throw new RuntimeException("Cannot make a subcategory the parent of its ancestor");
                }

                Category parent = categoryRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
                category.setParent(parent);
            } else {
                category.setParent(null);
            }

            // Update order
            category.setDisplayOrder(order);
            categoryRepository.save(category);
        }
    }

    @Transactional
    public CategoryDTO updateCategoryStatus(Long categoryId, String status, String ownerEmail) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (!category.getStore().getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to update this category");
        }

        category.setStatus(status);
        category.setUpdatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);

        // Also update all child categories if status is inactive
        if ("Inactive".equalsIgnoreCase(status)) {
            updateChildrenStatus(categoryId, status);
        }

        // Convert to DTO with product count
        CategoryDTO dto = mapToCategoryDTO(savedCategory);
        Long productCount = productRepository.countByCategoryId(savedCategory.getId());
        dto.setProductCount(productCount);

        return dto;
    }

    private void updateChildrenStatus(Long parentId, String status) {
        List<Category> children = categoryRepository.findByParentIdOrderByDisplayOrderAsc(parentId);
        for (Category child : children) {
            child.setStatus(status);
            child.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(child);

            // Recursively update grandchildren
            updateChildrenStatus(child.getId(), status);
        }
    }

    // Helper method to check if potentialAncestor is an ancestor of categoryId
    private boolean isDescendant(Long potentialAncestorId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        Category current = category.getParent();
        while (current != null) {
            if (current.getId().equals(potentialAncestorId)) {
                return true;
            }
            current = current.getParent();
        }

        return false;
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