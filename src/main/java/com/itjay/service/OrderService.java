package com.itjay.service;

import com.itjay.pojo.Order;
import com.itjay.pojo.CartItem;

import java.util.List;

public interface OrderService {
    /**
     * 创建订单
     * @param userId 用户ID
     * @param cartItems 购物车项列表
     * @return 订单对象
     */
    Order createOrder(Long userId, List<CartItem> cartItems);
    
    /**
     * 获取用户的所有订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> getUserOrders(Long userId);
    
    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单对象
     */
    Order getOrderById(String orderId, Long userId);
} 