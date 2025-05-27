package com.itjay.mapper;


import com.itjay.pojo.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("select * from product")
    List<Product> getAllProducts();

    @Select("select * from product where id = #{id}")
    Product getProductById(Long id);

}
