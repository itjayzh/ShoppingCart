package com.itjay.service.impl;

import com.itjay.mapper.OrderMapper;
import com.itjay.mapper.OrderItemMapper;
import com.itjay.pojo.Order;
import com.itjay.pojo.OrderItem;
import com.itjay.pojo.CartItem;
import com.itjay.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Order createOrder(Long userId, List<CartItem> cartItems) {
        if (userId == null || cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("参数不完整");
        }
        
        // 创建订单号 - 使用时间戳和随机数
        String orderId = generateOrderId();
        
        // 计算总金额
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 创建订单对象
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PAID"); // 初始状态为已支付
        order.setCreateTime(LocalDateTime.now());
        
        // 保存订单
        orderMapper.insert(order);
        
        // 创建订单项
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setImageUrl(cartItem.getImageUrl());
            orderItem.setSelectedAttributes(cartItem.getSelectedAttributes());
            return orderItem;
        }).collect(Collectors.toList());
        
        // 批量保存订单项
        for (OrderItem orderItem : orderItems) {
            orderItemMapper.insert(orderItem);
        }
        
        order.setOrderItems(orderItems);
        return order;
    }
    
    @Override
    public List<Order> getUserOrders(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        List<Order> orders = orderMapper.selectByUserId(userId);
        
        // 为每个订单加载订单项
        for (Order order : orders) {
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
            order.setOrderItems(orderItems);
        }
        
        return orders;
    }
    
    @Override
    public Order getOrderById(String orderId, Long userId) {
        if (orderId == null || userId == null) {
            throw new RuntimeException("参数不完整");
        }
        
        Order order = orderMapper.selectByOrderIdAndUserId(orderId, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在或无权限查看");
        }
        
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        order.setOrderItems(orderItems);
        
        return order;
    }
    
    /**
     * 生成订单号
     * 格式：年月日时分秒 + 4位随机数
     */
    private String generateOrderId() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomNum = String.valueOf((int) (Math.random() * 9000) + 1000);
        return timestamp + randomNum;
    }
} 