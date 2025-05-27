package com.itjay.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String selectedAttributes;
    private String imageUrl;
    private String productName;
    private BigDecimal price;
    private LocalDateTime addedTime;

}
