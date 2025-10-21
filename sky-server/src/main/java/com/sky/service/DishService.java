package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {
    /*
    * 新增菜品和菜品口味
    *
    * */
    void saveWithFlavor(DishDTO dishDTO);
}
