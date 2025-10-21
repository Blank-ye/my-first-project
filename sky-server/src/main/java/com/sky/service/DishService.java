package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    /*
    * 新增菜品和菜品口味
    *
    * */
    void saveWithFlavor(DishDTO dishDTO);

    /*
    * 分页查询
    *
    * */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /*
    * 根据id批量删除
    *
    * */
    void DeleteById(List<Long> ids);
}
