package com.itjay.controller;

import com.itjay.pojo.CartItem;
import com.itjay.pojo.Order;
import com.itjay.service.CartItemService;
import com.itjay.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@CrossOrigin
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartItemService cartItemService;
    
    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> itemIdsObj = (List<Object>) request.get("itemIds");
            List<Long> itemIds = new ArrayList<>();
            
            // 正确处理不同类型的数字转换为Long
            if (itemIdsObj != null) {
                for (Object id : itemIdsObj) {
                    if (id instanceof Integer) {
                        itemIds.add(((Integer) id).longValue());
                    } else if (id instanceof Long) {
                        itemIds.add((Long) id);
                    } else if (id instanceof String) {
                        itemIds.add(Long.parseLong((String) id));
                    } else if (id instanceof Number) {
                        itemIds.add(((Number) id).longValue());
                    }
                }
            }
            
            Long userId = Long.valueOf(request.get("userId").toString());
            
            if (itemIds.isEmpty()) {
                return ResponseEntity.badRequest().body("未选择商品");
            }
            
            // 获取购物车中选中的商品
            List<CartItem> selectedItems = cartItemService.getSelectedCartItems(userId, itemIds);
            
            // 创建订单
            Order order = orderService.createOrder(userId, selectedItems);
            
            // 删除已结算的购物车商品
            for (Long itemId : itemIds) {
                cartItemService.deleteCartItem(itemId, userId);
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈以便调试
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 直接创建订单（不通过购物车）
     */
    @PostMapping("/createDirect")
    public ResponseEntity<?> createDirectOrder(@RequestBody Map<String, Object> request) {
        try {
            // 获取请求参数
            Long userId = Long.valueOf(request.get("userId").toString());
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String selectedAttributes = (String) request.get("selectedAttributes");
            BigDecimal price = new BigDecimal(request.get("price").toString());
            String productName = (String) request.get("productName");
            String imageUrl = (String) request.get("imageUrl");
            
            // 创建一个临时的CartItem对象
            List<CartItem> tempCartItems = new ArrayList<>();
            CartItem tempCartItem = new CartItem();
            tempCartItem.setUserId(userId);
            tempCartItem.setProductId(productId);
            tempCartItem.setQuantity(quantity);
            tempCartItem.setSelectedAttributes(selectedAttributes);
            tempCartItem.setPrice(price);
            tempCartItem.setProductName(productName);
            tempCartItem.setImageUrl(imageUrl);
            tempCartItem.setAddedTime(LocalDateTime.now());
            tempCartItems.add(tempCartItem);
            
            // 创建订单
            Order order = orderService.createOrder(userId, tempCartItems);
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈以便调试
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserOrders(@RequestParam Long userId) {
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/detail")
    public ResponseEntity<?> getOrderDetail(@RequestParam String orderId, @RequestParam Long userId) {
        try {
            Order order = orderService.getOrderById(orderId, userId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 