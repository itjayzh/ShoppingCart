package com.itjay.controller;

import com.itjay.pojo.CartItem;
import com.itjay.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;
    /**
     *根据商品的id添加到购物车
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItem cartItem) {
        try {
            cartItemService.addToCart(cartItem);
            return ResponseEntity.ok("添加成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * 获取购物车列表
     */
    @GetMapping("/listDetail")
    public ResponseEntity<?> getCartWithProduct(@RequestParam Long userId) {
        try {
            List<CartItem> items = cartItemService.getCartWithProduct(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * 删除购物车商品
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCartItem(@RequestParam Long cartItemId, @RequestParam Long userId) {
        try {
            cartItemService.deleteCartItem(cartItemId, userId);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 根据商品ID删除购物车商品
     */
    @DeleteMapping("/deleteBatch")
    public ResponseEntity<?> deleteCartItemByProductId(@RequestParam Long productId, @RequestParam Long userId) {
        try {
            cartItemService.deleteCartItemByProductId(productId, userId);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    @PutMapping("/updateQuantity")
    public ResponseEntity<?> updateCartItemQuantity(
            @RequestParam Long cartItemId, 
            @RequestParam Integer quantity, 
            @RequestParam Long userId) {
        try {
            cartItemService.updateCartItemQuantity(cartItemId, quantity, userId);
            return ResponseEntity.ok("数量更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 计算选中商品总价
     */
    @PostMapping("/calculateTotal")
    public ResponseEntity<?> calculateSelectedItemsTotal(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> itemIds = (List<Long>) request.get("itemIds");
            Long userId = Long.valueOf(request.get("userId").toString());
            
            BigDecimal total = cartItemService.calculateSelectedItemsTotal(itemIds, userId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 清空用户购物车
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearUserCart(@RequestParam Long userId) {
        try {
            cartItemService.clearUserCart(userId);
            return ResponseEntity.ok("购物车已清空");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 结算购物车
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> itemIds = (List<Long>) request.get("itemIds");
            Long userId = Long.valueOf(request.get("userId").toString());
            
            if (itemIds == null || itemIds.isEmpty()) {
                return ResponseEntity.badRequest().body("未选择商品");
            }
            
            // 计算选中商品总价
            BigDecimal totalAmount = cartItemService.calculateSelectedItemsTotal(itemIds, userId);
            
            // 清空已结算的商品
            for (Long itemId : itemIds) {
                cartItemService.deleteCartItem(itemId, userId);
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "结算成功",
                "totalAmount", totalAmount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
