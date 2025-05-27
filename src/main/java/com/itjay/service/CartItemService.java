package com.itjay.service;

import com.itjay.pojo.CartItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;

@Service
public interface CartItemService {
    void addToCart(CartItem cartItem);

    List<CartItem> getCartWithProduct(Long userId);

    void deleteCartItem(Long cartItemId, Long userId);

    void clearUserCart(Long userId);
    
    // 获取用户选中的购物车项
    List<CartItem> getSelectedCartItems(Long userId, List<Long> itemIds);
    
    // 计算选中商品的总价格
    BigDecimal calculateSelectedItemsTotal(List<Long> itemIds, Long userId);
    
    // 根据产品ID删除购物车项
    void deleteCartItemByProductId(Long productId, Long userId);
    
    // 更新购物车项数量
    void updateCartItemQuantity(Long cartItemId, Integer quantity, Long userId);
}
