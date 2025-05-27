package com.itjay.service.impl;


import com.itjay.mapper.ProductMapper;
import com.itjay.pojo.Product;
import com.itjay.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> getAllProducts() {
        return productMapper.getAllProducts();
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.getProductById(id);
    }
}
