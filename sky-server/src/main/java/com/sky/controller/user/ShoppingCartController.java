package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /*
    * 添加购物车
    *
    * */
    @PostMapping("add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车，商品为，{}",shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    /*
     * 查看购物车信息
     *
     * */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车信息");
        List<ShoppingCart> list= shoppingCartService.list();
        return Result.success(list);
    }

    /*
    * 清空购物车
    *
    * */
    @DeleteMapping("/clean")
    public Result clean(){
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }

    /*
    * 删除购物车中一条商品
    *
    * */
    @PostMapping("sub")
    public Result subDsih(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中一天商品，{}",shoppingCartDTO);
        shoppingCartService.subDish(shoppingCartDTO);
        return Result.success();
    }
}
