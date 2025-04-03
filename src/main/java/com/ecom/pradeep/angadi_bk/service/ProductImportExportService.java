package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.ImportResult;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductImportExportService {
    
    ImportResult importProductsFromCSV(MultipartFile file, Long storeId, String ownerEmail) throws IOException;
    
    ImportResult importProductsFromExcel(MultipartFile file, Long storeId, String ownerEmail) throws IOException;
    
    Resource exportProductsToCSV(Long storeId, String ownerEmail) throws IOException;
    
    Resource exportProductsToExcel(Long storeId, String ownerEmail) throws IOException;
}