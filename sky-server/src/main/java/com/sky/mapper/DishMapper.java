package com.sky.mapper;

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
}
