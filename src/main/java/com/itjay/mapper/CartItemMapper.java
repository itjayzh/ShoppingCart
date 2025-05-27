package com.itjay.mapper;

import com.itjay.pojo.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CartItemMapper {

    @Delete("delete from cart_item where id = #{id}")
    void deleteById(Long id);

    List<CartItem> selectCartWithProduct(@Param("userId")Long userId);

    // 查询是否已有相同商品
    @Select("select id, user_id, product_id, quantity, CONVERT(selected_attributes USING utf8mb4) as selected_attributes, " +
            "image_url, product_name, price, added_time " +
            "from cart_item where user_id = #{userId} and product_id = #{productId}")
    List<CartItem> findExistingCartItem(@Param("userId") Long userId,
                                 @Param("productId") Long productId,
                                 @Param("selectedAttributes") String selectedAttributes);

    // 更新数量
    @Update("update cart_item set quantity = #{quantity} where id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    // 插入新购物车项
    @Insert("insert into cart_item (user_id, product_id, quantity, selected_attributes, price, added_time, image_url, product_name) " +
            "values (#{userId}, #{productId}, #{quantity}, #{selectedAttributes}, #{price}, now(), #{imageUrl}, #{productName})")
    int insert(CartItem item);

    // 删除购物车项（按ID删除）
    @Delete("delete from cart_item where id = #{cartItemId} and user_id = #{userId}")
    int deleteCartItem(@Param("cartItemId") Long cartItemId, @Param("userId") Long userId);

    // 清空用户购物车
    @Delete("delete from cart_item where user_id = #{userId}")
    void clearUserCart(@Param("userId") Long userId);
    
    // 获取用户选中的购物车项
    @Select("SELECT * FROM cart_item WHERE user_id = #{userId} AND id IN (${itemIds})")
    List<CartItem> getSelectedCartItems(@Param("userId") Long userId, @Param("itemIds") String itemIds);
    
    // 根据产品ID删除购物车项
    @Delete("delete from cart_item where product_id = #{productId} and user_id = #{userId}")
    int deleteCartItemByProductId(@Param("productId") Long productId, @Param("userId") Long userId);
}
