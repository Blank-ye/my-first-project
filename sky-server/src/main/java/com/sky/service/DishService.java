package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

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

    /*
     * 根据id查询菜品
     *
     * */
    DishVO selectDishWithFlavorById(Long id);

    /*
     * 修改菜品信息和口味
     *
     * */
    void updateWithFlavor(DishDTO dishDTO);

    /*
     * 起售，停售菜品
     *
     * */
    void updateStatus(Integer status, Long id);

    /*
    * 根据分类id查询菜品
    *
    * */
    List<Dish> list(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
