package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /*
    * 添加购物车
    *
    * */
    void add(ShoppingCartDTO shoppingCartDTO);

    /*
    * 查看购物车信息
    *
    * */
    List<ShoppingCart> list();

    /*
    * 清空购物车
    *
    * */
    void clean();

    /*
    * 删除购物车中一条商品
    *
    * */
    void subDish(ShoppingCartDTO shoppingCartDTO);
}
