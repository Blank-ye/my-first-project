package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /*
     * 新增套餐
     *
     * */
    @Transactional
    @Override
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmeal.setStatus(StatusConstant.ENABLE);
        //在套餐中插入一体信息
        setmealMapper.insertSetmeal(setmeal);
        Long id = setmeal.getId();
        //在菜品中插入n条数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes !=null && setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(id);
            });
        }
        setmealDishMapper.setmealAddDish(setmealDishes);
    }
}
