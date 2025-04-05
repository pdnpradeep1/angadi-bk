package com.ecom.pradeep.angadi_bk.service.impl;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.CategoryRepository;
import com.ecom.pradeep.angadi_bk.repo.InventoryTransactionRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductVariantRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import com.ecom.pradeep.angadi_bk.service.ProductImportExportService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductImportExportServiceImpl implements ProductImportExportService {

    private static final Logger logger = LoggerFactory.getLogger(ProductImportExportServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional
    public ImportResult importProductsFromCSV(MultipartFile file, Long storeId, String ownerEmail) throws IOException {
        logger.info("Starting CSV import for store ID: {}, owner: {}", storeId, ownerEmail);

        // Verify store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new IllegalArgumentException("You don't have permission to import products to this store");
        }

        int importedCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             // Fix for deprecated withFirstRecordAsHeader()
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

            List<CSVRecord> records = csvParser.getRecords();
            logger.info("Found {} records in CSV file", records.size());

            for (CSVRecord record : records) {
                try {
                    // Process basic product data
                    Product product = new Product();
                    product.setName(record.get("name"));
                    product.setDescription(record.get("description"));
                    product.setPrice(new BigDecimal(record.get("price")));
                    product.setStockQuantity(Integer.parseInt(record.get("stockQuantity")));
                    product.setSku(record.get("sku"));

                    // Handle category
                    String categoryName = record.get("category");
                    if (categoryName != null && !categoryName.isEmpty()) {
                        // Fix for missing findByNameAndStoreId method
                        Category category = categoryRepository.findByNameAndStore_Id(categoryName, storeId)
                                .orElseGet(() -> {
                                    Category newCategory = new Category();
                                    newCategory.setName(categoryName);
                                    newCategory.setStore(store);
                                    return categoryRepository.save(newCategory);
                                });
                        product.setCategory(category);
                    }

                    // Handle status
                    String status = record.get("status");
                    if (status != null && !status.isEmpty()) {
                        product.setStatus(status);
                    } else {
                        product.setStatus("Active");
                    }

                    // Handle image URL
                    if (record.isMapped("imageUrl")) {
                        product.setImageUrl(record.get("imageUrl"));
                    }

                    // Handle featured flag
                    if (record.isMapped("featured")) {
                        product.setFeatured(Boolean.parseBoolean(record.get("featured")));
                    }

                    // Handle original price
                    if (record.isMapped("originalPrice") && !record.get("originalPrice").isEmpty()) {
                        product.setOriginalPrice(new BigDecimal(record.get("originalPrice")));
                    }

                    // Handle low stock threshold
                    if (record.isMapped("lowStockThreshold") && !record.get("lowStockThreshold").isEmpty()) {
                        product.setLowStockThreshold(Integer.parseInt(record.get("lowStockThreshold")));
                    }

                    // Set store
                    product.setStore(store);

                    // Check for variants
                    boolean hasVariants = false;
                    if (record.isMapped("hasVariants") && !record.get("hasVariants").isEmpty()) {
                        hasVariants = Boolean.parseBoolean(record.get("hasVariants"));
                    }

                    // Save the product first to get an ID
                    Product savedProduct = productRepository.save(product);
                    logger.info("Saved product: {}", savedProduct.getName());

                    // Create inventory transaction for the initial stock
                    createInventoryTransaction(savedProduct, savedProduct.getStockQuantity(), ownerEmail);

                    // Process variants if present
                    if (hasVariants) {
                        logger.info("Processing variants for product: {}", savedProduct.getName());
                        List<ProductVariant> variants = new ArrayList<>();

                        // Process up to 3 variants
                        for (int i = 1; i <= 3; i++) {
                            String variantSkuKey = "variant" + i + "_sku";

                            // When processing variants in your CSV import
                            if (record.isMapped(variantSkuKey) && !record.get(variantSkuKey).isEmpty()) {
                                ProductVariant variant = new ProductVariant();
                                variant.setProduct(savedProduct);
                                variant.setSku(record.get(variantSkuKey));

                                // Price
                                String variantPriceKey = "variant" + i + "_price";
                                if (record.isMapped(variantPriceKey) && !record.get(variantPriceKey).isEmpty()) {
                                    variant.setPrice(new BigDecimal(record.get(variantPriceKey)));
                                } else {
                                    variant.setPrice(savedProduct.getPrice()); // Default to product price
                                }

                                // Stock quantity
                                String variantStockKey = "variant" + i + "_stock";
                                if (record.isMapped(variantStockKey) && !record.get(variantStockKey).isEmpty()) {
                                    variant.setStockQuantity(Integer.parseInt(record.get(variantStockKey)));
                                } else {
                                    variant.setStockQuantity(savedProduct.getStockQuantity()); // Default to product stock
                                }

                                // Image URL
                                String variantImageKey = "variant" + i + "_image";
                                if (record.isMapped(variantImageKey) && !record.get(variantImageKey).isEmpty()) {
                                    variant.setImageUrl(record.get(variantImageKey));
                                }

                                // Handle attributes instead of options
                                Map<String, String> attributes = new HashMap<>();

                                // Option 1
                                String option1NameKey = "variant" + i + "_option1_name";
                                String option1ValueKey = "variant" + i + "_option1_value";

                                if (record.isMapped(option1NameKey) && !record.get(option1NameKey).isEmpty() &&
                                        record.isMapped(option1ValueKey) && !record.get(option1ValueKey).isEmpty()) {
                                    attributes.put(record.get(option1NameKey), record.get(option1ValueKey));
                                }

                                // Option 2
                                String option2NameKey = "variant" + i + "_option2_name";
                                String option2ValueKey = "variant" + i + "_option2_value";

                                if (record.isMapped(option2NameKey) && !record.get(option2NameKey).isEmpty() &&
                                        record.isMapped(option2ValueKey) && !record.get(option2ValueKey).isEmpty()) {
                                    attributes.put(record.get(option2NameKey), record.get(option2ValueKey));
                                }

                                // Set the attributes map
                                variant.setAttributes(attributes);

                                // Set up the composite ID
                                ProductVariantId variantId = new ProductVariantId();
                                variantId.setProductId(savedProduct.getId());
                                variantId.setVariantId(System.currentTimeMillis() + i); // Generate a unique ID
                                variant.setId(variantId);

                                variants.add(variant);
                            }
                        }

                        // Save all variants
                        if (!variants.isEmpty()) {
                            List<ProductVariant> savedVariants = productVariantRepository.saveAll(variants);
                            logger.info("Saved {} variants for product: {}", variants.size(), savedProduct.getName());

                            // Calculate total variant quantity
                            int totalVariantQuantity = savedVariants.stream()
                                    .mapToInt(ProductVariant::getStockQuantity)
                                    .sum();

                            // Update product quantity to match total variant quantity
                            savedProduct.setStockQuantity(totalVariantQuantity);
                            productRepository.save(savedProduct);

                            // Create inventory transactions for each variant
                            for (ProductVariant variant : savedVariants) {
                                createVariantInventoryTransaction(variant, variant.getStockQuantity(), ownerEmail);
                            }

                            // Update the main product's inventory transaction
                            createInventoryTransaction(savedProduct, totalVariantQuantity, ownerEmail);
                        }
                    }

                    importedCount++;
                } catch (Exception e) {
                    logger.error("Error importing product: {}", e.getMessage(), e);
                    failedCount++;
                    errors.add("Row " + (importedCount + failedCount) + ": " + e.getMessage());
                }
            }
        }

        ImportResult result = new ImportResult();
        result.setImportedCount(importedCount);
        result.setFailedCount(failedCount);
        result.setTotalCount(importedCount + failedCount);
        result.setErrors(errors);
        result.setMessage(importedCount + " products imported successfully, " + failedCount + " failed");

        logger.info("Import completed: {} successful, {} failed", importedCount, failedCount);
        return result;
    }

    @Override
    @Transactional
    public ImportResult importProductsFromExcel(MultipartFile file, Long storeId, String ownerEmail) throws IOException {
        // Similar implementation for Excel files
        // This would be implemented with Apache POI to read Excel files
        // For brevity, I'm not including the full implementation here
        return new ImportResult();
    }

    @Override
    public Resource exportProductsToCSV(Long storeId, String ownerEmail) throws IOException {
        // Implementation for exporting products to CSV
        // For brevity, I'm not including the full implementation here
        return new ByteArrayResource(new byte[0]);
    }

    @Override
    public Resource exportProductsToExcel(Long storeId, String ownerEmail) throws IOException {
        // Implementation for exporting products to Excel
        // For brevity, I'm not including the full implementation here
        return new ByteArrayResource(new byte[0]);
    }

    // Add this new method to create inventory transactions
    private void createInventoryTransaction(Product product, int quantity, String performedBy) {
        if (quantity <= 0) {
            logger.info("Skipping inventory transaction for product {} as quantity is {}",
                    product.getName(), quantity);
            return;
        }

        try {
            // Check if product has variants
            List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            boolean hasVariants = !variants.isEmpty();

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setQuantity(quantity);
            transaction.setQuantityChange(quantity);
            transaction.setRemainingQuantity(quantity);
            transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);

            if (hasVariants) {
                // For products with variants, we're tracking at variant level
                transaction.setReason("Initial import - Parent product with variants");
                transaction.setNote("Product with variants imported via CSV. Stock is sum of variant stocks.");

                // Add variant information
                StringBuilder variantInfo = new StringBuilder("Variants: ");
                for (int i = 0; i < variants.size(); i++) {
                    ProductVariant variant = variants.get(i);
                    if (i > 0) {
                        variantInfo.append(", ");
                    }
                    variantInfo.append(variant.getSku()).append(" (").append(variant.getStockQuantity()).append(" units)");
                }
                transaction.setNote(transaction.getNote() + " - " + variantInfo.toString());
            } else {
                // For products without variants, normal tracking
                transaction.setReason("Initial import");
                transaction.setNote("Product imported via CSV");
            }

            transaction.setPerformedBy(performedBy);
            transaction.setTimestamp(LocalDateTime.now());

            inventoryTransactionRepository.save(transaction);
            logger.info("Created inventory transaction for product {}: {} units",
                    product.getName(), quantity);
        } catch (Exception e) {
            logger.error("Failed to create inventory transaction for product {}: {}",
                    product.getName(), e.getMessage(), e);
        }
    }

    // Add a new method to create inventory transactions for variants
    private void createVariantInventoryTransaction(ProductVariant variant, int quantity, String performedBy) {
        if (quantity <= 0) {
            logger.info("Skipping inventory transaction for variant {} as quantity is {}",
                    variant.getSku(), quantity);
            return;
        }

        try {
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(variant.getProduct());
            // Set the variant ID in the transaction
            transaction.setVariantId(variant.getId().getVariantId());
            
            transaction.setQuantity(quantity);
            transaction.setQuantityChange(quantity);
            transaction.setRemainingQuantity(quantity);
            transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);
            transaction.setReason("Initial import - variant");
            transaction.setPerformedBy(performedBy);

            // Include variant details in the notes
            String variantDetails = "Variant SKU: " + variant.getSku();
            if (variant.getAttributes() != null && !variant.getAttributes().isEmpty()) {
                variantDetails += ", Attributes: " + variant.getAttributes().toString();
            }
            transaction.setNote("Variant imported via CSV: " + variantDetails);

            transaction.setTimestamp(LocalDateTime.now());

            inventoryTransactionRepository.save(transaction);
            logger.info("Created inventory transaction for variant {}: {} units",
                    variant.getSku(), quantity);
        } catch (Exception e) {
            logger.error("Failed to create inventory transaction for variant {}: {}",
                    variant.getSku(), e.getMessage(), e);
        }
    }

    /**
     * Updates product stock when variant stock changes
     *
     * @param product The product to update
     * @return Updated product with recalculated stock
     */
    private Product recalculateProductStock(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

        if (!variants.isEmpty()) {
            // Sum up all variant quantities
            int totalStock = variants.stream()
                    .mapToInt(ProductVariant::getStockQuantity)
                    .sum();

            // Update product stock
            product.setStockQuantity(totalStock);
            return productRepository.save(product);
        }

        return product;
    }

    @Override
    public Resource exportProducts(Long storeId, String format, String ownerEmail) throws IOException {
        // Validate store ownership
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));
        
        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("Unauthorized to export products from this store");
        }
        
        // Call the appropriate export method based on format
        if ("csv".equalsIgnoreCase(format)) {
            return exportProductsToCSV(storeId, ownerEmail);
        } else if ("excel".equalsIgnoreCase(format)) {
            return exportProductsToExcel(storeId, ownerEmail);
        } else {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }
}