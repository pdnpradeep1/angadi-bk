package com.ecom.pradeep.angadi_bk.model;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int totalProcessed;
    private int successCount;
    private int errorCount;
    private List<String> errors;

    public ImportResult() {
        this.errors = new ArrayList<>();
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
        this.errorCount++;
    }

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementTotal() {
        this.totalProcessed++;
    }
}