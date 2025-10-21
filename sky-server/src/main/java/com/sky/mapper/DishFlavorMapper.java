package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    void insertFlavor(List<DishFlavor> flavors);

    //@Delete("delete from dish_flavor where dish_id=${dishId}")
    void deletById(List<Long> dishIds);
}
