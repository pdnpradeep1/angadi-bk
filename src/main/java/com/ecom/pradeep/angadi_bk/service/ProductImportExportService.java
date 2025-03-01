package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.Category;
import com.ecom.pradeep.angadi_bk.model.ImportResult;
import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.Store;
import com.ecom.pradeep.angadi_bk.repo.CategoryRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductImportExportService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    // CSV Headers
    private static final String[] CSV_HEADERS = {
            "ID", "Name", "Description", "Price", "Original Price", "SKU",
            "Stock Quantity", "Category", "Featured", "Status"
    };

    public ProductImportExportService(
            ProductRepository productRepository,
            StoreRepository storeRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Import products from CSV file
     */
    @Transactional
    public ImportResult importProductsFromCSV(MultipartFile file, Long storeId, String ownerEmail) throws IOException {
        // Check store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to import products to this store");
        }

        // Parse CSV
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            ImportResult result = new ImportResult();
            result.setTotalRows((int) csvParser.getRecordNumber());

            // Get categories for this store
            Map<String, Category> categoryMap = getCategoryMapForStore(storeId);

            for (CSVRecord csvRecord : csvParser) {
                try {
                    Product product = new Product();

                    // Required fields
                    String name = csvRecord.get("Name");
                    if (name == null || name.isEmpty()) {
                        result.addError((int) csvRecord.getRecordNumber(), "Product name is required");
                        continue;
                    }
                    product.setName(name);

                    // Optional description
                    if (csvRecord.isMapped("Description")) {
                        product.setDescription(csvRecord.get("Description"));
                    }

                    // Required price
                    try {
                        BigDecimal price = new BigDecimal(csvRecord.get("Price"));
                        product.setPrice(price);
                    } catch (Exception e) {
                        result.addError((int) csvRecord.getRecordNumber(), "Invalid price format");
                        continue;
                    }

                    // Optional original price
                    if (csvRecord.isMapped("Original Price") && !csvRecord.get("Original Price").isEmpty()) {
                        try {
                            BigDecimal originalPrice = new BigDecimal(csvRecord.get("Original Price"));
                            product.setOriginalPrice(originalPrice);
                        } catch (Exception e) {
                            // Not critical, can continue
                        }
                    }

                    // SKU
                    if (csvRecord.isMapped("SKU")) {
                        product.setSku(csvRecord.get("SKU"));
                    }

                    // Stock Quantity
                    if (csvRecord.isMapped("Stock Quantity")) {
                        try {
                            String stockStr = csvRecord.get("Stock Quantity");
                            if ("Unlimited".equalsIgnoreCase(stockStr)) {
                                product.setStockQuantity(-1);
                            } else {
                                product.setStockQuantity(Integer.parseInt(stockStr));
                            }
                        } catch (Exception e) {
                            // Default to 0 if invalid
                            product.setStockQuantity(0);
                        }
                    } else {
                        product.setStockQuantity(0);
                    }

                    // Featured
                    if (csvRecord.isMapped("Featured")) {
                        product.setFeatured(Boolean.parseBoolean(csvRecord.get("Featured")));
                    }

                    // Status
                    if (csvRecord.isMapped("Status")) {
                        String status = csvRecord.get("Status");
                        if (status == null || status.isEmpty()) {
                            product.setStatus("Inactive");
                        } else {
                            product.setStatus(status);
                        }
                    } else {
                        product.setStatus("Inactive");
                    }

                    // Set store
                    product.setStore(store);

                    // Set category
                    String categoryName = csvRecord.get("Category");
                    if (categoryName == null || categoryName.isEmpty()) {
                        result.addError((int) csvRecord.getRecordNumber(), "Category is required");
                        continue;
                    }

                    Category category = categoryMap.get(categoryName.trim().toLowerCase());
                    if (category == null) {
                        // Create category if it doesn't exist
                        category = new Category();
                        category.setName(categoryName);
                        category.setStore(store);
                        category = categoryRepository.save(category);
                        categoryMap.put(categoryName.trim().toLowerCase(), category);
                    }
                    product.setCategory(category);

                    // Set creation timestamp
                    product.setCreatedAt(LocalDateTime.now());

                    // Save product
                    productRepository.save(product);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.addError((int) csvRecord.getRecordNumber(), "Error processing row: " + e.getMessage());
                }
            }

            return result;
        }
    }

    /**
     * Import products from Excel file
     */
    @Transactional
    public ImportResult importProductsFromExcel(MultipartFile file, Long storeId, String ownerEmail) throws IOException {
        // Check store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to import products to this store");
        }

        // Parse Excel
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            ImportResult result = new ImportResult();
            result.setTotalRows(sheet.getPhysicalNumberOfRows() - 1); // Exclude header row

            // Get categories for this store
            Map<String, Category> categoryMap = getCategoryMapForStore(storeId);

            // Get header row and map column indices
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    columnMap.put(cell.getStringCellValue().trim(), i);
                }
            }

            // Validate required columns
            if (!columnMap.containsKey("Name") || !columnMap.containsKey("Price") || !columnMap.containsKey("Category")) {
                throw new IllegalArgumentException("Required columns missing: Name, Price, and Category are required");
            }

            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Product product = new Product();

                    // Required fields
                    Cell nameCell = row.getCell(columnMap.get("Name"));
                    if (nameCell == null) {
                        result.addError(i + 1, "Product name is required");
                        continue;
                    }
                    product.setName(getCellValueAsString(nameCell));

                    // Optional description
                    if (columnMap.containsKey("Description")) {
                        Cell descCell = row.getCell(columnMap.get("Description"));
                        if (descCell != null) {
                            product.setDescription(getCellValueAsString(descCell));
                        }
                    }

                    // Required price
                    Cell priceCell = row.getCell(columnMap.get("Price"));
                    if (priceCell == null) {
                        result.addError(i + 1, "Price is required");
                        continue;
                    }
                    try {
                        if (priceCell.getCellType() == CellType.NUMERIC) {
                            product.setPrice(BigDecimal.valueOf(priceCell.getNumericCellValue()));
                        } else {
                            product.setPrice(new BigDecimal(priceCell.getStringCellValue()));
                        }
                    } catch (Exception e) {
                        result.addError(i + 1, "Invalid price format");
                        continue;
                    }

                    // Optional original price
                    if (columnMap.containsKey("Original Price")) {
                        Cell originalPriceCell = row.getCell(columnMap.get("Original Price"));
                        if (originalPriceCell != null) {
                            try {
                                if (originalPriceCell.getCellType() == CellType.NUMERIC) {
                                    product.setOriginalPrice(BigDecimal.valueOf(originalPriceCell.getNumericCellValue()));
                                } else if (!getCellValueAsString(originalPriceCell).isEmpty()) {
                                    product.setOriginalPrice(new BigDecimal(getCellValueAsString(originalPriceCell)));
                                }
                            } catch (Exception e) {
                                // Not critical, can continue
                            }
                        }
                    }

                    // SKU
                    if (columnMap.containsKey("SKU")) {
                        Cell skuCell = row.getCell(columnMap.get("SKU"));
                        if (skuCell != null) {
                            product.setSku(getCellValueAsString(skuCell));
                        }
                    }

                    // Stock Quantity
                    if (columnMap.containsKey("Stock Quantity")) {
                        Cell stockCell = row.getCell(columnMap.get("Stock Quantity"));
                        if (stockCell != null) {
                            String stockStr = getCellValueAsString(stockCell);
                            if ("Unlimited".equalsIgnoreCase(stockStr)) {
                                product.setStockQuantity(-1);
                            } else {
                                try {
                                    if (stockCell.getCellType() == CellType.NUMERIC) {
                                        product.setStockQuantity((int) stockCell.getNumericCellValue());
                                    } else {
                                        product.setStockQuantity(Integer.parseInt(stockStr));
                                    }
                                } catch (Exception e) {
                                    product.setStockQuantity(0);
                                }
                            }
                        } else {
                            product.setStockQuantity(0);
                        }
                    } else {
                        product.setStockQuantity(0);
                    }

                    // Featured
                    if (columnMap.containsKey("Featured")) {
                        Cell featuredCell = row.getCell(columnMap.get("Featured"));
                        if (featuredCell != null) {
                            if (featuredCell.getCellType() == CellType.BOOLEAN) {
                                product.setFeatured(featuredCell.getBooleanCellValue());
                            } else {
                                String featuredStr = getCellValueAsString(featuredCell);
                                product.setFeatured(Boolean.parseBoolean(featuredStr));
                            }
                        }
                    }

                    // Status
                    if (columnMap.containsKey("Status")) {
                        Cell statusCell = row.getCell(columnMap.get("Status"));
                        if (statusCell != null) {
                            String status = getCellValueAsString(statusCell);
                            if (status.isEmpty()) {
                                product.setStatus("Inactive");
                            } else {
                                product.setStatus(status);
                            }
                        } else {
                            product.setStatus("Inactive");
                        }
                    } else {
                        product.setStatus("Inactive");
                    }

                    // Set store
                    product.setStore(store);

                    // Set category
                    Cell categoryCell = row.getCell(columnMap.get("Category"));
                    if (categoryCell == null) {
                        result.addError(i + 1, "Category is required");
                        continue;
                    }

                    String categoryName = getCellValueAsString(categoryCell);
                    if (categoryName.isEmpty()) {
                        result.addError(i + 1, "Category is required");
                        continue;
                    }

                    Category category = categoryMap.get(categoryName.trim().toLowerCase());
                    if (category == null) {
                        // Create category if it doesn't exist
                        category = new Category();
                        category.setName(categoryName);
                        category.setStore(store);
                        category = categoryRepository.save(category);
                        categoryMap.put(categoryName.trim().toLowerCase(), category);
                    }
                    product.setCategory(category);

                    // Set creation timestamp
                    product.setCreatedAt(LocalDateTime.now());

                    // Save product
                    productRepository.save(product);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.addError(i + 1, "Error processing row: " + e.getMessage());
                }
            }

            return result;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // Prevent scientific notation for numbers
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    /**
     * Build a map of category name to Category entity for a specific store
     */
    private Map<String, Category> getCategoryMapForStore(Long storeId) {
        List<Category> categories = categoryRepository.findByStoreId(storeId);
        Map<String, Category> categoryMap = new HashMap<>();

        for (Category category : categories) {
            categoryMap.put(category.getName().trim().toLowerCase(), category);
        }

        return categoryMap;
    }

    /**
     * Export products to CSV file
     */
    public Resource exportProductsToCSV(Long storeId, String ownerEmail) throws IOException {
        // Check store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to export products from this store");
        }

        // Get products for store
        List<Product> products = productRepository.findByStoreId(storeId);

        // Create CSV in memory
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (
                OutputStreamWriter writer = new OutputStreamWriter(out);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader(CSV_HEADERS))
        ) {
            // Add product data
            for (Product product : products) {
                csvPrinter.printRecord(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getOriginalPrice(),
                        product.getSku(),
                        product.getStockQuantity() == -1 ? "Unlimited" : product.getStockQuantity(),
                        product.getCategory().getName(),
                        product.isFeatured(),
                        product.getStatus()
                );
            }

            csvPrinter.flush();
        }

        return new ByteArrayResource(out.toByteArray());
    }

    /**
     * Export products to Excel file
     */
    public Resource exportProductsToExcel(Long storeId, String ownerEmail) throws IOException {
        // Check store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to export products from this store");
        }

        // Get products for store
        List<Product> products = productRepository.findByStoreId(storeId);

        // Create workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < CSV_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(CSV_HEADERS[i]);
        }

        // Create data rows
        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getName());

            if (product.getDescription() != null) {
                row.createCell(2).setCellValue(product.getDescription());
            }

            Cell priceCell = row.createCell(3);
            priceCell.setCellValue(product.getPrice().doubleValue());

            if (product.getOriginalPrice() != null) {
                Cell originalPriceCell = row.createCell(4);
                originalPriceCell.setCellValue(product.getOriginalPrice().doubleValue());
            }

            if (product.getSku() != null) {
                row.createCell(5).setCellValue(product.getSku());
            }

            if (product.getStockQuantity() == -1) {
                row.createCell(6).setCellValue("Unlimited");
            } else {
                row.createCell(6).setCellValue(product.getStockQuantity());
            }

            row.createCell(7).setCellValue(product.getCategory().getName());
            row.createCell(8).setCellValue(product.isFeatured());
            row.createCell(9).setCellValue(product.getStatus());
        }

        // Auto-size columns
        for (int i = 0; i < CSV_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }
}