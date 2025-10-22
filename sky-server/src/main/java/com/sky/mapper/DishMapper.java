package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("SELECT * from dish where id=#{id}")
    Dish selectById(Long id);

    void deleteById(List<Long > ids);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);


    /*
    * 根据分类id查询菜品
    *
    * */
    List<Dish> list(Long categoryId);
}
