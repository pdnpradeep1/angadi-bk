package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.ImportResult;
import com.ecom.pradeep.angadi_bk.service.ProductImportExportService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/products/import-export")
public class ProductImportExportController {
    private final ProductImportExportService importExportService;

    public ProductImportExportController(ProductImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @PostMapping("/import/csv")
    public ResponseEntity<ImportResult> importProductsFromCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("storeId") Long storeId,
            @RequestHeader("Owner-Email") String ownerEmail) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a file");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are allowed");
        }

        ImportResult result = importExportService.importProductsFromCSV(file, storeId, ownerEmail);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/excel")
    public ResponseEntity<ImportResult> importProductsFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("storeId") Long storeId,
            @RequestHeader("Owner-Email") String ownerEmail) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a file");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Only Excel files are allowed");
        }

        ImportResult result = importExportService.importProductsFromExcel(file, storeId, ownerEmail);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/csv/{storeId}")
    public ResponseEntity<Resource> exportProductsToCSV(
            @PathVariable Long storeId,
            @RequestHeader("Owner-Email") String ownerEmail) throws IOException {

        Resource resource = importExportService.exportProductsToCSV(storeId, ownerEmail);

        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String filename = "products_" + storeId + "_" + dateStr + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @GetMapping("/export/excel/{storeId}")
    public ResponseEntity<Resource> exportProductsToExcel(
            @PathVariable Long storeId,
            @RequestHeader("Owner-Email") String ownerEmail) throws IOException {

        Resource resource = importExportService.exportProductsToExcel(storeId, ownerEmail);

        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String filename = "products_" + storeId + "_" + dateStr + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/template")
    public ResponseEntity<Resource> getImportTemplate() throws IOException {
        // Create a sample Excel template
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products Template");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Description", "Price", "Original Price", "SKU",
                "Stock Quantity", "Category", "Featured", "Status"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Add sample row
        Row sampleRow = sheet.createRow(1);
        sampleRow.createCell(0).setCellValue("Sample Product");
        sampleRow.createCell(1).setCellValue("This is a sample product description");
        sampleRow.createCell(2).setCellValue(99.99);
        sampleRow.createCell(3).setCellValue(129.99);
        sampleRow.createCell(4).setCellValue("SKU123");
        sampleRow.createCell(5).setCellValue(10);
        sampleRow.createCell(6).setCellValue("Sample Category");
        sampleRow.createCell(7).setCellValue(false);
        sampleRow.createCell(8).setCellValue("Active");

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"product_import_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
}