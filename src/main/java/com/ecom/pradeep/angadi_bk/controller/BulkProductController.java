package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.BulkOperationRequest;
import com.ecom.pradeep.angadi_bk.model.BulkOperationResult;
import com.ecom.pradeep.angadi_bk.service.BulkProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/bulk")
public class BulkProductController {
    private final BulkProductService bulkProductService;

    public BulkProductController(BulkProductService bulkProductService) {
        this.bulkProductService = bulkProductService;
    }

    @PostMapping("/operation")
    public ResponseEntity<BulkOperationResult> processBulkOperation(
            @RequestBody BulkOperationRequest request,
            @RequestHeader("Owner-Email") String ownerEmail) {

        // Validate request
        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            throw new IllegalArgumentException("Product IDs are required");
        }

        if (request.getOperationType() == null) {
            throw new IllegalArgumentException("Operation type is required");
        }

        // Process bulk operation
        BulkOperationResult result = bulkProductService.processBulkOperation(request, ownerEmail);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/delete")
    public ResponseEntity<BulkOperationResult> bulkDelete(
            @RequestBody List<Long> productIds,
            @RequestHeader("Owner-Email") String ownerEmail) {

        BulkOperationRequest request = new BulkOperationRequest();
        request.setProductIds(productIds);
        request.setOperationType(BulkOperationRequest.OperationType.DELETE);

        BulkOperationResult result = bulkProductService.processBulkOperation(request, ownerEmail);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/publish")
    public ResponseEntity<BulkOperationResult> bulkPublish(
            @RequestBody List<Long> productIds,
            @RequestHeader("Owner-Email") String ownerEmail) {

        BulkOperationRequest request = new BulkOperationRequest();
        request.setProductIds(productIds);
        request.setOperationType(BulkOperationRequest.OperationType.PUBLISH);

        BulkOperationResult result = bulkProductService.processBulkOperation(request, ownerEmail);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/unpublish")
    public ResponseEntity<BulkOperationResult> bulkUnpublish(
            @RequestBody List<Long> productIds,
            @RequestHeader("Owner-Email") String ownerEmail) {

        BulkOperationRequest request = new BulkOperationRequest();
        request.setProductIds(productIds);
        request.setOperationType(BulkOperationRequest.OperationType.UNPUBLISH);

        BulkOperationResult result = bulkProductService.processBulkOperation(request, ownerEmail);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/change-category/{categoryId}")
    public ResponseEntity<BulkOperationResult> changeCategory(
            @PathVariable Long categoryId,
            @RequestBody List<Long> productIds,
            @RequestHeader("Owner-Email") String ownerEmail) {

        BulkOperationRequest request = new BulkOperationRequest();
        request.setProductIds(productIds);
        request.setCategoryId(categoryId);
        request.setOperationType(BulkOperationRequest.OperationType.CHANGE_CATEGORY);

        BulkOperationResult result = bulkProductService.processBulkOperation(request, ownerEmail);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/apply-discount")
    public ResponseEntity<BulkOperationResult> applyDiscount(
            @RequestParam Double discountPercentage,
            @RequestBody List<Long> productIds,
            @RequestHeader("Owner-Email") String ownerEmail) {

        BulkOperationRequest request = new BulkOperationRequest();
        request.setProductIds(productIds);
        request.setDiscountPercentage(discountPercentage);
        request.setOperationType(BulkOperationRequest.OperationType.ADJUST_PRICE);
        request.setPriceAdjustmentType(BulkOperationRequest.PriceAdjustmentType.DECREASE_PERCENT);

        BulkOperationResult result = bulkProductService.processBulkOperation(request, ownerEmail);

        return ResponseEntity.ok(result);
    }
}