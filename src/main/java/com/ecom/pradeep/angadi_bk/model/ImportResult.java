package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<RowError> errors = new ArrayList<>();

    @Data
    public static class RowError {
        private int rowNumber;
        private String errorMessage;

        public RowError(int rowNumber, String errorMessage) {
            this.rowNumber = rowNumber;
            this.errorMessage = errorMessage;
        }
    }

    public void addError(int rowNumber, String message) {
        errors.add(new RowError(rowNumber, message));
        failureCount++;
    }

    public void incrementSuccess() {
        successCount++;
    }
}