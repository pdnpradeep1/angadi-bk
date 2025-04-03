package com.ecom.pradeep.angadi_bk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String type;
    private String note;
    private LocalDateTime timestamp;
}