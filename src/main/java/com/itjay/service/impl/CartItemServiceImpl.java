package com.itjay.service.impl;

import com.itjay.mapper.CartItemMapper;
import com.itjay.mapper.ProductMapper;
import com.itjay.pojo.CartItem;
import com.itjay.pojo.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itjay.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {
    private static final Logger logger = LoggerFactory.getLogger(CartItemServiceImpl.class);
    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private ProductMapper productMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addToCart(CartItem item) {
        // 参数校验
        if (item.getUserId() == null || item.getProductId() == null || item.getQuantity() == null) {
            throw new RuntimeException("参数不完整");
        }

        // 查询商品信息
        Product product = productMapper.getProductById(item.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 设置商品价格和名称
        item.setPrice(product.getPrice());
        item.setProductName(product.getName());
        item.setImageUrl(product.getImageUrl());
        item.setAddedTime(LocalDateTime.now());
        
        // 确保selectedAttributes有一致的值，如果为null则设为默认值
        if (item.getSelectedAttributes() == null) {
            item.setSelectedAttributes("{}");
        }
        
        // 处理默认规格字符串，确保JSON格式一致
        try {
            if (item.getSelectedAttributes().contains("color") && item.getSelectedAttributes().contains("size")) {
                // 已经是规格格式，不做处理
            } else if (item.getSelectedAttributes().equals("{}")) {
                // 默认为空规格，不做处理
            } else {
                // 尝试规范化JSON格式
                item.setSelectedAttributes(item.getSelectedAttributes().replaceAll("\\s+", ""));
            }
        } catch (Exception e) {
            // 如果解析出错，使用默认空规格
            item.setSelectedAttributes("{}");
        }

        // 检查是否已有相同商品
        // 查询用户的所有相同商品的购物车项
        List<CartItem> userItems = cartItemMapper.findExistingCartItem(
                item.getUserId(),
                item.getProductId(),
                item.getSelectedAttributes()
        );

        // 尝试查找属性匹配的购物车项
        CartItem existing = null;
        if (userItems != null && !userItems.isEmpty()) {
            logger.debug("新添加商品属性: {}", item.getSelectedAttributes());
            for (CartItem cartItem : userItems) {
                // 判断属性是否相同（简单比较字符串去除空格后是否相等）
                String existingAttrs = cartItem.getSelectedAttributes();
                String newAttrs = item.getSelectedAttributes();

                // 记录现有购物车项属性，方便调试
                logger.debug("现有购物车项ID: {}, 属性: {}", cartItem.getId(), existingAttrs);

                if (existingAttrs == null) existingAttrs = "{}";
                if (newAttrs == null) newAttrs = "{}";

                // 去除所有空格和换行符
                existingAttrs = existingAttrs.replaceAll("[\\s\\n\\r]+", "");
                newAttrs = newAttrs.replaceAll("[\\s\\n\\r]+", "");

                logger.debug("处理后 - 现有属性: '{}', 新属性: '{}'", existingAttrs, newAttrs);

                // 判断字符串是否相等
                boolean isEqual = existingAttrs.equals(newAttrs);
                logger.debug("属性是否相等: {}", isEqual);

                if (isEqual) {
                    existing = cartItem;
                    logger.debug("找到匹配的购物车项，ID: {}", cartItem.getId());
                    break;
                }
            }
        }

        if (existing != null) {
            // 更新数量（累加新的数量到现有数量）
            int newQuantity = existing.getQuantity() + item.getQuantity();
            cartItemMapper.updateQuantity(existing.getId(), newQuantity);
        } else {
            // 新建购物车项
            cartItemMapper.insert(item);
        }
    }

    @Override
    public List<CartItem> getCartWithProduct(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        return cartItemMapper.selectCartWithProduct(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCartItem(Long cartItemId, Long userId) {
        if (cartItemId == null || userId == null) {
            throw new RuntimeException("参数不完整");
        }
        int result = cartItemMapper.deleteCartItem(cartItemId, userId);
        if (result == 0) {
            throw new RuntimeException("删除失败，购物车项不存在或无权限删除");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clearUserCart(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        cartItemMapper.clearUserCart(userId);
    }
    
    @Override
    public List<CartItem> getSelectedCartItems(Long userId, List<Long> itemIds) {
        if (userId == null || itemIds == null || itemIds.isEmpty()) {
            throw new RuntimeException("参数不完整");
        }
        
        String itemIdsStr = itemIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
                
        return cartItemMapper.getSelectedCartItems(userId, itemIdsStr);
    }
    
    @Override
    public BigDecimal calculateSelectedItemsTotal(List<Long> itemIds, Long userId) {
        if (userId == null || itemIds == null || itemIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<CartItem> selectedItems = getSelectedCartItems(userId, itemIds);

        // 使用流式计算，计算选中项的总价
        return selectedItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, (subtotal, itemTotal) -> subtotal.add(itemTotal));
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCartItemByProductId(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new RuntimeException("参数不完整");
        }
        
        int result = cartItemMapper.deleteCartItemByProductId(productId, userId);
        if (result == 0) {
            throw new RuntimeException("删除失败，购物车项不存在或无权限删除");
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCartItemQuantity(Long cartItemId, Integer quantity, Long userId) {
        if (cartItemId == null || quantity == null || userId == null) {
            throw new RuntimeException("参数不完整");
        }
        
        if (quantity <= 0) {
            throw new RuntimeException("商品数量必须大于0");
        }
        
        // 确认购物车项属于该用户
        List<CartItem> userItems = getCartWithProduct(userId);
        boolean itemBelongsToUser = userItems.stream()
                .anyMatch(item -> item.getId().equals(cartItemId));
                
        if (!itemBelongsToUser) {
            throw new RuntimeException("购物车项不存在或无权限修改");
        }
        
        cartItemMapper.updateQuantity(cartItemId, quantity);
    }
}
