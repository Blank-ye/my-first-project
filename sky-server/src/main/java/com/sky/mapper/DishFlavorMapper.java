package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    void insertFlavor(List<DishFlavor> flavors);

    //@Delete("delete from dish_flavor where dish_id=${dishId}")
    void deleteById(List<Long> dishIds);

    @Select("select * from dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> selectByDishId(Long dishId);
}
