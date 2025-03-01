package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.CategoryRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BulkProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public BulkProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Process bulk operations on products
     *
     * @param request    The bulk operation request
     * @param ownerEmail The email of the store owner for authorization
     * @return Results of the bulk operation
     */
    @Transactional
    public BulkOperationResult processBulkOperation(BulkOperationRequest request, String ownerEmail) {
        List<Product> products = productRepository.findAllById(request.getProductIds());

        // Verify all products belong to stores owned by this user
        verifyOwnership(products, ownerEmail);

        // Process based on operation type
        BulkOperationResult result = new BulkOperationResult();
        result.setTotalRequested(request.getProductIds().size());
        result.setProcessed(products.size());
        result.setSuccessIds(new ArrayList<>());
        result.setFailedIds(new ArrayList<>());

        switch (request.getOperationType()) {
            case UPDATE:
                updateProducts(products, request, result);
                break;
            case DELETE:
                deleteProducts(products, result);
                break;
            case PUBLISH:
                publishProducts(products, result);
                break;
            case UNPUBLISH:
                unpublishProducts(products, result);
                break;
            case ADJUST_PRICE:
                adjustPrices(products, request, result);
                break;
            case ADJUST_STOCK:
                adjustStock(products, request, result);
                break;
            case CHANGE_CATEGORY:
                changeCategory(products, request, result);
                break;
            case UPDATE_TAGS:
                updateTags(products, request, result);
                break;
            default:
                throw new IllegalArgumentException("Unknown operation type: " + request.getOperationType());
        }

        return result;
    }

    private void verifyOwnership(List<Product> products, String ownerEmail) {
        for (Product product : products) {
            if (!product.getStore().getOwner().getEmail().equals(ownerEmail)) {
                throw new RuntimeException("Unauthorized to modify product ID " + product.getId());
            }
        }
    }

    private void updateProducts(List<Product> products, BulkOperationRequest request, BulkOperationResult result) {
        for (Product product : products) {
            try {
                if (request.getStatus() != null) {
                    product.setStatus(request.getStatus());
                }

                if (request.getFeatured() != null) {
                    product.setFeatured(request.getFeatured());
                }

                productRepository.save(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }

    private void deleteProducts(List<Product> products, BulkOperationResult result) {
        for (Product product : products) {
            try {
                productRepository.delete(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }

    private void publishProducts(List<Product> products, BulkOperationResult result) {
        for (Product product : products) {
            try {
                product.setStatus("Active");
                product.setPublishedAt(LocalDateTime.now());
                productRepository.save(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }

    private void unpublishProducts(List<Product> products, BulkOperationResult result) {
        for (Product product : products) {
            try {
                product.setStatus("Inactive");
                productRepository.save(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }

    private void adjustPrices(List<Product> products, BulkOperationRequest request, BulkOperationResult result) {
        if (request.getPrice() == null && request.getDiscountPercentage() == null) {
            throw new IllegalArgumentException("Price adjustment requires price or discount percentage");
        }

        for (Product product : products) {
            try {
                BigDecimal currentPrice = product.getPrice();
                BigDecimal newPrice = currentPrice;

                switch (request.getPriceAdjustmentType()) {
                    case FIXED:
                        newPrice = request.getPrice();
                        break;
                    case INCREASE_AMOUNT:
                        newPrice = currentPrice.add(request.getPrice());
                        break;
                    case DECREASE_AMOUNT:
                        newPrice = currentPrice.subtract(request.getPrice());
                        break;
                    case INCREASE_PERCENT:
                        newPrice = currentPrice.multiply(
                                BigDecimal.ONE.add(
                                        BigDecimal.valueOf(request.getDiscountPercentage()).divide(BigDecimal.valueOf(100))
                                )
                        );
                        break;
                    case DECREASE_PERCENT:
                        newPrice = currentPrice.multiply(
                                BigDecimal.ONE.subtract(
                                        BigDecimal.valueOf(request.getDiscountPercentage()).divide(BigDecimal.valueOf(100))
                                )
                        );
                        break;
                }

                // Set original price if it's a decrease for a sale
                if (request.getPriceAdjustmentType() == BulkOperationRequest.PriceAdjustmentType.DECREASE_AMOUNT ||
                        request.getPriceAdjustmentType() == BulkOperationRequest.PriceAdjustmentType.DECREASE_PERCENT) {
                    product.setOriginalPrice(currentPrice);
                }

                // Ensure price doesn't go below zero
                if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    newPrice = BigDecimal.ONE; // Minimum price $1
                }

                product.setPrice(newPrice);
                productRepository.save(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }

    private void adjustStock(List<Product> products, BulkOperationRequest request, BulkOperationResult result) {
        if (request.getStockQuantity() == null) {
            throw new IllegalArgumentException("Stock adjustment requires stock quantity");
        }

        for (Product product : products) {
            try {
                product.setStockQuantity(request.getStockQuantity());
                productRepository.save(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }

    private void changeCategory(List<Product> products, BulkOperationRequest request, BulkOperationResult result) {
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Category change requires category ID");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        for (Product product : products) {
            try {
                // Make sure products and category belong to the same store
                if (!product.getStore().getId().equals(category.getStore().getId())) {
                    throw new IllegalArgumentException("Category does not belong to the same store as product " + product.getId());
                }

                product.setCategory(category);
                productRepository.save(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }

    private void updateTags(List<Product> products, BulkOperationRequest request, BulkOperationResult result) {
        // Add tags
        Set<Tag> tagsToAdd = new HashSet<>();
        if (request.getAddTagIds() != null && !request.getAddTagIds().isEmpty()) {
            tagsToAdd = new HashSet<>(tagRepository.findAllById(request.getAddTagIds()));
        }

        // Remove tags
        Set<Long> tagIdsToRemove = request.getRemoveTagIds() != null
                ? request.getRemoveTagIds()
                : new HashSet<>();

        for (Product product : products) {
            try {
                // Current tags
                Set<Tag> currentTags = product.getTags();

                // Remove specified tags
                Set<Tag> updatedTags = currentTags.stream()
                        .filter(tag -> !tagIdsToRemove.contains(tag.getId()))
                        .collect(Collectors.toSet());

                // Add new tags
                updatedTags.addAll(tagsToAdd);

                // Update product
                product.setTags(updatedTags);
                productRepository.save(product);
                result.getSuccessIds().add(product.getId());
            } catch (Exception e) {
                result.getFailedIds().add(product.getId());
            }
        }
    }
}