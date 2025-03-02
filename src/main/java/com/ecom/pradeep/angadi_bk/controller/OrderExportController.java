package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.service.OrderExportService;
import com.ecom.pradeep.angadi_bk.utils.OrderSpecification;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/orders/export")
public class OrderExportController {
    private final OrderExportService orderExportService;

    public OrderExportController(OrderExportService orderExportService) {
        this.orderExportService = orderExportService;
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<Resource> exportOrdersToExcel(
            @PathVariable Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String customerEmail
    ) throws IOException {
        // Build specification based on the filters
        OrderSpecification spec = new OrderSpecification();

        // Always filter by store ID
        spec = (OrderSpecification) spec.hasStoreId(storeId);

        // Apply additional filters if provided
        if (status != null && !status.equalsIgnoreCase("all")) {
            spec = spec.and(OrderSpecification.hasStatus(status.toUpperCase()));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and(OrderSpecification.containsKeyword(search));
        }

        if (dateFrom != null) {
            spec = spec.and(OrderSpecification.createdAtGreaterThanOrEqual(dateFrom));
        }

        if (dateTo != null) {
            spec = spec.and(OrderSpecification.createdAtLessThanOrEqual(dateTo));
        }

        if (minAmount != null) {
            spec = spec.and(OrderSpecification.totalAmountGreaterThanOrEqual(minAmount));
        }

        if (maxAmount != null) {
            spec = spec.and(OrderSpecification.totalAmountLessThanOrEqual(maxAmount));
        }

        if (customerEmail != null && !customerEmail.isEmpty()) {
            spec = spec.and(OrderSpecification.hasCustomerEmail(customerEmail));
        }

        // Generate Excel file
        byte[] excelBytes = orderExportService.exportOrdersToExcel(storeId, spec);

        // Generate filename with date
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "orders_" + storeId + "_" + date + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(excelBytes));
    }

    @GetMapping("/csv/{storeId}")
    public ResponseEntity<Resource> exportOrdersToCSV(
            @PathVariable Long storeId,
            @RequestParam(required = false) String status
    ) throws IOException {
        // Similar implementation for CSV export
        // This is a placeholder - you'll need to implement CSV export in your service

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new ByteArrayResource("Order ID,Status,Customer,Amount".getBytes()));
    }
}