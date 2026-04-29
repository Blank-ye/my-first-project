package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
/*
* 查询店铺
*
* */
@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {

    private static final String KEY="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

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
