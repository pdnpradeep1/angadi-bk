package com.ecom.pradeep.angadi_bk.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String status;
    private Integer displayOrder;
    private String imageUrl;
    private String slug;
    private Long productCount;
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
