package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;

@Data
public class OrderRequest {
    private Long productId;
    private int quantity;

    // Getters and Setters
}
