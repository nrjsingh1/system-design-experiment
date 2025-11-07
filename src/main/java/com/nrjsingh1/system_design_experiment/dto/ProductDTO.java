package com.nrjsingh1.system_design_experiment.dto;

import com.nrjsingh1.system_design_experiment.model.Product;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;

    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        return dto;
    }
}