package com.itjay.mapper;

import com.itjay.pojo.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {
    
    @Insert("INSERT INTO orders (order_id, user_id, total_amount, status, create_time) " +
            "VALUES (#{orderId}, #{userId}, #{totalAmount}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);
    
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "totalAmount", column = "total_amount"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time")
    })
    List<Order> selectByUserId(Long userId);
    
    @Select("SELECT * FROM orders WHERE order_id = #{orderId} AND user_id = #{userId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "totalAmount", column = "total_amount"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time")
    })
    Order selectByOrderIdAndUserId(String orderId, Long userId);
} 