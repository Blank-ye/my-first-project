package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    List<Long> selectSetmealIdByDishId(List<Long> dishID);

    @AutoFill(value = OperationType.UPDATE)
    void updateStatus(List<Long> DishIds, Integer status);
}
