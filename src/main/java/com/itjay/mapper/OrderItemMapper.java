package com.itjay.mapper;

import com.itjay.pojo.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    
    @Insert("INSERT INTO order_items (order_id, product_id, product_name, price, quantity, image_url, selected_attributes) " +
            "VALUES (#{orderId}, #{productId}, #{productName}, #{price}, #{quantity}, #{imageUrl}, #{selectedAttributes})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem orderItem);
    
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "productId", column = "product_id"),
        @Result(property = "productName", column = "product_name"),
        @Result(property = "price", column = "price"),
        @Result(property = "quantity", column = "quantity"),
        @Result(property = "imageUrl", column = "image_url"),
        @Result(property = "selectedAttributes", column = "selected_attributes")
    })
    List<OrderItem> selectByOrderId(Long orderId);
} 