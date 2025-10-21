package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    /*
    * 跟分类id查询菜品
    *
    * */
    @Select("SELECT count(*) from dish where category_id=#{id}")
    Integer selectByCategoryId(Long id);


    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
}
