package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

public interface SetmealService {

    /*
     * 新增菜品
     *
     * */
    void addSetmeal(SetmealDTO setmealDTO);

    /*
    * 分页查询
    *
    * */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);
}
