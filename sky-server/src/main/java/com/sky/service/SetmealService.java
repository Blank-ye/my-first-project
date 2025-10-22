package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

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

    /*
    * 根据id批量删除套餐
    *
    * */
    void deleteByIds(List<Long> ids);

    /*
    * 修改套餐信息
    *
    * */
    void update(SetmealDTO setmealDTO);

    /*
    * 根据id查询套餐信息
    *
    * */
    SetmealVO getById(Long id);

    /*
    * 起售禁售套餐
    *
    * */
    void sartStop(Integer status, Long id);
}
