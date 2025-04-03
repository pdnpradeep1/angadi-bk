package com.ecom.pradeep.angadi_bk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    private int importedCount;
    private int failedCount;
    private int totalCount;
    private String message;
    private List<String> errors;
}