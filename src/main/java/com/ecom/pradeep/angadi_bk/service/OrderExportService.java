package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.model.OrderItem;
import com.ecom.pradeep.angadi_bk.repo.OrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderExportService {
    private final OrderRepository orderRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public OrderExportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public byte[] exportOrdersToExcel(Long storeId, Specification<Order> spec) throws IOException {
        // Retrieve all orders matching the specification
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findAll(spec, pageable);

        Workbook workbook = new XSSFWorkbook();

        // Orders overview sheet
        createOrdersSummarySheet(workbook, orders.getContent());

        // Order items sheet
        createOrderItemsSheet(workbook, orders.getContent());

        // Order status counts sheet
        createOrderStatusSheet(workbook, orders.getContent());

        // Write to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void createOrdersSummarySheet(Workbook workbook, List<Order> orders) {
        Sheet sheet = workbook.createSheet("Orders Summary");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Order ID", "Order Number", "Customer Name", "Customer Email",
                "Created Date", "Status", "Payment Status", "Payment Method",
                "Subtotal", "Shipping", "Tax", "Discount", "Total Amount", "Items Count"
        };

        // Create header cell style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // Apply header styles
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

        // Create data rows
        int rowNum = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(order.getOrderNumber());
            row.createCell(2).setCellValue(order.getCustomer().getName());
            row.createCell(3).setCellValue(order.getCustomer().getEmail());
            row.createCell(4).setCellValue(order.getCreatedAt().format(dateFormatter));
            row.createCell(5).setCellValue(order.getStatus());
            row.createCell(6).setCellValue(order.getPaymentStatus());
            row.createCell(7).setCellValue(order.getPaymentMethod());

            // Create number cell style for currency
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("₹#,##0.00"));

            Cell subtotalCell = row.createCell(8);
            subtotalCell.setCellValue(order.getSubtotal().doubleValue());
            subtotalCell.setCellStyle(currencyStyle);

            Cell shippingCell = row.createCell(9);
            shippingCell.setCellValue(order.getShippingCost().doubleValue());
            shippingCell.setCellStyle(currencyStyle);

            Cell taxCell = row.createCell(10);
            taxCell.setCellValue(order.getTax().doubleValue());
            taxCell.setCellStyle(currencyStyle);

            Cell discountCell = row.createCell(11);
            discountCell.setCellValue(order.getDiscount().doubleValue());
            discountCell.setCellStyle(currencyStyle);

            Cell totalCell = row.createCell(12);
            totalCell.setCellValue(order.getTotalAmount());
            totalCell.setCellStyle(currencyStyle);

            row.createCell(13).setCellValue(order.getOrderItems().size());
        }

        // Resize columns to fit content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createOrderItemsSheet(Workbook workbook, List<Order> orders) {
        Sheet sheet = workbook.createSheet("Order Items");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Order Number", "Product ID", "Product Name", "Quantity", "Unit Price", "Total"
        };

        // Create header cell style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // Apply header styles
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("₹#,##0.00"));

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(item.getProduct().getId());
                row.createCell(2).setCellValue(item.getProduct().getName());
                row.createCell(3).setCellValue(item.getQuantity());

                Cell priceCell = row.createCell(4);
                priceCell.setCellValue(item.getPrice().doubleValue());
                priceCell.setCellStyle(currencyStyle);

                Cell totalCell = row.createCell(5);
                totalCell.setCellValue(item.getTotal().doubleValue());
                totalCell.setCellStyle(currencyStyle);
            }
        }

        // Resize columns to fit content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createOrderStatusSheet(Workbook workbook, List<Order> orders) {
        Sheet sheet = workbook.createSheet("Order Status");

        // Count orders by status
        int pendingCount = 0;
        int processingCount = 0;
        int shippedCount = 0;
        int deliveredCount = 0;
        int cancelledCount = 0;
        int refundedCount = 0;
        int returnedCount = 0;

        // Calculate totals by status
        BigDecimal pendingTotal = BigDecimal.ZERO;
        BigDecimal processingTotal = BigDecimal.ZERO;
        BigDecimal shippedTotal = BigDecimal.ZERO;
        BigDecimal deliveredTotal = BigDecimal.ZERO;
        BigDecimal cancelledTotal = BigDecimal.ZERO;
        BigDecimal refundedTotal = BigDecimal.ZERO;
        BigDecimal returnedTotal = BigDecimal.ZERO;

        for (Order order : orders) {
            BigDecimal orderTotal = BigDecimal.valueOf(order.getTotalAmount());

            switch (order.getStatus()) {
                case "PENDING":
                    pendingCount++;
                    pendingTotal = pendingTotal.add(orderTotal);
                    break;
                case "PROCESSING":
                    processingCount++;
                    processingTotal = processingTotal.add(orderTotal);
                    break;
                case "SHIPPED":
                    shippedCount++;
                    shippedTotal = shippedTotal.add(orderTotal);
                    break;
                case "DELIVERED":
                    deliveredCount++;
                    deliveredTotal = deliveredTotal.add(orderTotal);
                    break;
                case "CANCELLED":
                    cancelledCount++;
                    cancelledTotal = cancelledTotal.add(orderTotal);
                    break;
                case "REFUNDED":
                    refundedCount++;
                    refundedTotal = refundedTotal.add(orderTotal);
                    break;
                case "RETURNED":
                    returnedCount++;
                    returnedTotal = returnedTotal.add(orderTotal);
                    break;
            }
        }

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Status", "Count", "Total Value"};

        // Create header cell style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // Apply header styles
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        CellStyle currencyStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("₹#,##0.00"));

        int rowNum = 1;

        // Pending row
        Row pendingRow = sheet.createRow(rowNum++);
        pendingRow.createCell(0).setCellValue("Pending");
        pendingRow.createCell(1).setCellValue(pendingCount);
        Cell pendingTotalCell = pendingRow.createCell(2);
        pendingTotalCell.setCellValue(pendingTotal.doubleValue());
        pendingTotalCell.setCellStyle(currencyStyle);

        // Processing row
        Row processingRow = sheet.createRow(rowNum++);
        processingRow.createCell(0).setCellValue("Processing");
        processingRow.createCell(1).setCellValue(processingCount);
        Cell processingTotalCell = processingRow.createCell(2);
        processingTotalCell.setCellValue(processingTotal.doubleValue());
        processingTotalCell.setCellStyle(currencyStyle);

        // Shipped row
        Row shippedRow = sheet.createRow(rowNum++);
        shippedRow.createCell(0).setCellValue("Shipped");
        shippedRow.createCell(1).setCellValue(shippedCount);
        Cell shippedTotalCell = shippedRow.createCell(2);
        shippedTotalCell.setCellValue(shippedTotal.doubleValue());
        shippedTotalCell.setCellStyle(currencyStyle);

        // Delivered row
        Row deliveredRow = sheet.createRow(rowNum++);
        deliveredRow.createCell(0).setCellValue("Delivered");
        deliveredRow.createCell(1).setCellValue(deliveredCount);
        Cell deliveredTotalCell = deliveredRow.createCell(2);
        deliveredTotalCell.setCellValue(deliveredTotal.doubleValue());
        deliveredTotalCell.setCellStyle(currencyStyle);

        // Cancelled row
        Row cancelledRow = sheet.createRow(rowNum++);
        cancelledRow.createCell(0).setCellValue("Cancelled");
        cancelledRow.createCell(1).setCellValue(cancelledCount);
        Cell cancelledTotalCell = cancelledRow.createCell(2);
        cancelledTotalCell.setCellValue(cancelledTotal.doubleValue());
        cancelledTotalCell.setCellStyle(currencyStyle);

        // Refunded row
        Row refundedRow = sheet.createRow(rowNum++);
        refundedRow.createCell(0).setCellValue("Refunded");
        refundedRow.createCell(1).setCellValue(refundedCount);
        Cell refundedTotalCell = refundedRow.createCell(2);
        refundedTotalCell.setCellValue(refundedTotal.doubleValue());
        refundedTotalCell.setCellStyle(currencyStyle);

        // Returned row
        Row returnedRow = sheet.createRow(rowNum++);
        returnedRow.createCell(0).setCellValue("Returned");
        returnedRow.createCell(1).setCellValue(returnedCount);
        Cell returnedTotalCell = returnedRow.createCell(2);
        returnedTotalCell.setCellValue(returnedTotal.doubleValue());
        returnedTotalCell.setCellStyle(currencyStyle);

        // Add a total row
        Row totalRow = sheet.createRow(rowNum++);
        CellStyle totalRowStyle = workbook.createCellStyle();
        Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalRowStyle.setFont(totalFont);
        totalRowStyle.setBorderTop(BorderStyle.THIN);

        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("Total");
        totalLabelCell.setCellStyle(totalRowStyle);

        Cell totalCountCell = totalRow.createCell(1);
        totalCountCell.setCellValue(orders.size());
        totalCountCell.setCellStyle(totalRowStyle);

        Cell totalValueCell = totalRow.createCell(2);
        BigDecimal grandTotal = pendingTotal.add(processingTotal).add(shippedTotal)
                .add(deliveredTotal).add(cancelledTotal).add(refundedTotal).add(returnedTotal);
        totalValueCell.setCellValue(grandTotal.doubleValue());

        CellStyle totalValueStyle = workbook.createCellStyle();
        totalValueStyle.cloneStyleFrom(totalRowStyle);
        totalValueStyle.setDataFormat(format.getFormat("₹#,##0.00"));
        totalValueCell.setCellStyle(totalValueStyle);

        // Resize columns to fit content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}