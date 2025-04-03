package com.ecom.pradeep.angadi_bk.service.impl;

import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.*;
import com.ecom.pradeep.angadi_bk.service.ProductImportExportService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductImportExportServiceImpl implements ProductImportExportService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional
    public ImportResult importProductsFromCSV(MultipartFile file, Long storeId, String ownerEmail) throws IOException {
        ImportResult result = new ImportResult();
        
        // Verify store ownership
        Store store = verifyStoreOwnership(storeId, ownerEmail);
        
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             // Fix for deprecated withFirstRecordAsHeader()
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            
            for (CSVRecord record : csvParser.getRecords()) {
                result.incrementTotal();
                try {
                    processProductRecord(record, store);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError("Error in row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public ImportResult importProductsFromExcel(MultipartFile file, Long storeId, String ownerEmail) throws IOException {
        ImportResult result = new ImportResult();
        
        // Verify store ownership
        Store store = verifyStoreOwnership(storeId, ownerEmail);
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Get header row
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnMap = createColumnMap(headerRow);
            
            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                result.incrementTotal();
                try {
                    processProductRow(row, columnMap, store);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError("Error in row " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        
        return result;
    }

    @Override
    public Resource exportProductsToCSV(Long storeId, String ownerEmail) throws IOException {
        // Verify store ownership
        Store store = verifyStoreOwnership(storeId, ownerEmail);
        
        // Get all products for the store
        List<Product> products = productRepository.findByStoreId(storeId);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        
        // Fix for deprecated withHeader()
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Name", "Description", "Price", "Original Price", "SKU", 
                        "Stock Quantity", "Category", "Featured", "Status")
                .build())) {
            
            for (Product product : products) {
                csvPrinter.printRecord(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getOriginalPrice(),
                    product.getSku(),
                    product.getStockQuantity(),
                    product.getCategory() != null ? product.getCategory().getName() : "",
                    product.isFeatured(),
                    product.getStatus()
                );
            }
        }
        
        return new ByteArrayResource(out.toByteArray());
    }

    @Override
    public Resource exportProductsToExcel(Long storeId, String ownerEmail) throws IOException {
        // Verify store ownership
        Store store = verifyStoreOwnership(storeId, ownerEmail);
        
        // Get all products for the store
        List<Product> products = productRepository.findByStoreId(storeId);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Description", "Price", "Original Price", "SKU", 
                "Stock Quantity", "Category", "Featured", "Status"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // Fill data rows
        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            setCellValue(cell0, product.getId());
            
            Cell cell1 = row.createCell(1);
            setCellValue(cell1, product.getName());
            
            Cell cell2 = row.createCell(2);
            setCellValue(cell2, product.getDescription() != null ? product.getDescription() : "");
            
            Cell cell3 = row.createCell(3);
            setCellValue(cell3, product.getPrice());
            
            Cell cell4 = row.createCell(4);
            setCellValue(cell4, product.getOriginalPrice() != null ? product.getOriginalPrice() : 0.0);
            
            Cell cell5 = row.createCell(5);
            setCellValue(cell5, product.getSku() != null ? product.getSku() : "");
            
            Cell cell6 = row.createCell(6);
            setCellValue(cell6, product.getStockQuantity());
            
            Cell cell7 = row.createCell(7);
            setCellValue(cell7, product.getCategory() != null ? product.getCategory().getName() : "");
            
            Cell cell8 = row.createCell(8);
            setCellValue(cell8, product.isFeatured());
            
            Cell cell9 = row.createCell(9);
            setCellValue(cell9, product.getStatus() != null ? product.getStatus() : "");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return new ByteArrayResource(outputStream.toByteArray());
    }
    
    // Helper methods
    
    private Store verifyStoreOwnership(Long storeId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        
        if (!store.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You don't have permission to access this store");
        }
        
        return store;
    }
    
    private Map<String, Integer> createColumnMap(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                columnMap.put(cell.getStringCellValue(), i);
            }
        }
        return columnMap;
    }
    
    private void processProductRecord(CSVRecord record, Store store) {
        String name = record.get("Name");
        String description = record.get("Description");
        // Fix for BigDecimal conversion
        BigDecimal price = new BigDecimal(record.get("Price"));
        BigDecimal originalPrice = record.isMapped("Original Price") ? 
                new BigDecimal(record.get("Original Price")) : price;
        String sku = record.isMapped("SKU") ? record.get("SKU") : generateSku(name);
        int stockQuantity = record.isMapped("Stock Quantity") ? 
                Integer.parseInt(record.get("Stock Quantity")) : 0;
        String categoryName = record.isMapped("Category") ? record.get("Category") : null;
        boolean featured = record.isMapped("Featured") ? 
                Boolean.parseBoolean(record.get("Featured")) : false;
        String status = record.isMapped("Status") ? record.get("Status") : "ACTIVE";
        
        // Create or update product
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setSku(sku);
        product.setStockQuantity(stockQuantity);
        product.setFeatured(featured);
        product.setStatus(status);
        product.setStore(store);
        
        // Set category if provided
        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = findOrCreateCategory(categoryName, store);
            product.setCategory(category);
        }
        
        // Save product
        product = productRepository.save(product);
        
        // Create inventory transaction for initial stock
        if (stockQuantity > 0) {
            createInventoryTransaction(product, stockQuantity, "Initial import");
        }
    }
    
    private void processProductRow(Row row, Map<String, Integer> columnMap, Store store) {
        String name = getCellValueAsString(row.getCell(columnMap.get("Name")));
        String description = getCellValueAsString(row.getCell(columnMap.get("Description")));
        // Fix for BigDecimal conversion
        BigDecimal price = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(columnMap.get("Price"))));
        BigDecimal originalPrice = columnMap.containsKey("Original Price") ? 
                BigDecimal.valueOf(getCellValueAsDouble(row.getCell(columnMap.get("Original Price")))) : price;
        String sku = columnMap.containsKey("SKU") ? 
                getCellValueAsString(row.getCell(columnMap.get("SKU"))) : generateSku(name);
        int stockQuantity = columnMap.containsKey("Stock Quantity") ? 
                getCellValueAsInt(row.getCell(columnMap.get("Stock Quantity"))) : 0;
        String categoryName = columnMap.containsKey("Category") ? 
                getCellValueAsString(row.getCell(columnMap.get("Category"))) : null;
        boolean featured = columnMap.containsKey("Featured") ? 
                getCellValueAsBoolean(row.getCell(columnMap.get("Featured"))) : false;
        String status = columnMap.containsKey("Status") ? 
                getCellValueAsString(row.getCell(columnMap.get("Status"))) : "ACTIVE";
        
        // Create or update product
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setSku(sku);
        product.setStockQuantity(stockQuantity);
        product.setFeatured(featured);
        product.setStatus( status.substring(0,1).toUpperCase() + status.substring(1).toLowerCase());
        product.setStore(store);
        
        // Set category if provided
        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = findOrCreateCategory(categoryName, store);
            product.setCategory(category);
        }
        
        // Save product
        product = productRepository.save(product);
        
        // Create inventory transaction for initial stock
        if (stockQuantity > 0) {
            createInventoryTransaction(product, stockQuantity, "Initial import");
        }
    }
    
    private Category findOrCreateCategory(String categoryName, Store store) {
        // Try to find existing category by name
        List<Category> categories = categoryRepository.findByStoreId(store.getId());
        Optional<Category> existingCategory = categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst();
        
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }
        
        // Create new category
        Category category = new Category();
        category.setName(categoryName);
        category.setStore(store);
//        category.setStatus("ACTIVE");
        
        // Get max display order and increment
        Integer maxOrder = categories.stream()
                .map(Category::getDisplayOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        
        category.setDisplayOrder(maxOrder + 1);
        
        return categoryRepository.save(category);
    }
    
    private void createInventoryTransaction(Product product, int quantity, String notes) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setQuantityChange(quantity);
        
        // The method name might be different - try one of these alternatives
        transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);
        // OR if the field is directly accessible
        // transaction.type = InventoryTransaction.TransactionType.ADJUSTMENT;
        
        transaction.setNotes(notes);
        transaction.setTimestamp(LocalDateTime.now());
        
        inventoryTransactionRepository.save(transaction);
    }
    
    private String generateSku(String productName) {
        // Generate a simple SKU based on product name and timestamp
        String namePart = productName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (namePart.length() > 5) {
            namePart = namePart.substring(0, 5);
        }
        
        long timestamp = System.currentTimeMillis() % 10000;
        return namePart + "-" + timestamp;
    }
    
    // Utility methods for cell value extraction
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
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
    
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
    
    private int getCellValueAsInt(Cell cell) {
        return (int) getCellValueAsDouble(cell);
    }
    
    private boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) return false;
        
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                return cell.getNumericCellValue() != 0;
            case STRING:
                String value = cell.getStringCellValue().toLowerCase();
                return value.equals("true") || value.equals("yes") || value.equals("1");
            default:
                return false;
        }
    }
}