package com.itjay.controller;

import com.itjay.pojo.Product;
import com.itjay.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * 获取全部商品列表
     */
    @GetMapping("/list")
    public List<Product> getAllProducts() {
        log.info("获取全部商品列表");
        List<Product> products = productService.getAllProducts();
        return products;
    }

    /**
     * 根据商品id获取商品信息
     */
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        log.info("根据商品id获取商品信息");
        Product  product = productService.getProductById(id);

        return product;
    }

}
