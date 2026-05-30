package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
/*
* 店铺
* */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    private static final String KEY="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;
    /*
    *
    * 设置店铺状态
    *
    * */
    @PutMapping("/{status}")
    public Result putStatus(@PathVariable Integer status){
        log.info("设置店铺状态为：{}",status== StatusConstant.ENABLE ? "营业中" : "打样中");
        redisTemplate.opsForValue().set(KEY,String.valueOf(status));
        return Result.success();
    }

    /*
    * 查看店铺状态
    *
    * */
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        String statusStr = (String) redisTemplate.opsForValue().get(KEY);
        if (statusStr !=null){
            Integer status=Integer.valueOf(statusStr);
            log.info("当前店铺状态：{}", status==StatusConstant.ENABLE ? "营业中" : "打烊中");
            return Result.success(status);
        }else {
            throw new IllegalArgumentException("字符串为空");
        }
    }
}
