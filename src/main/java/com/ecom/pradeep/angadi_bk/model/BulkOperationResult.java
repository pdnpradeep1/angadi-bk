package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;
import java.util.List;

@Data
public class BulkOperationResult {
    private int totalRequested;
    private int processed;
    private List<Long> successIds;
    private List<Long> failedIds;

    public int getSuccessCount() {
        return successIds.size();
    }

    public int getFailedCount() {
        return failedIds.size();
    }

    public boolean hasCompleteFailed() {
        return processed > 0 && successIds.isEmpty();
    }

    public boolean hasPartialFailed() {
        return !failedIds.isEmpty() && !successIds.isEmpty();
    }

    public boolean isFullySuccessful() {
        return processed > 0 && failedIds.isEmpty();
    }
}