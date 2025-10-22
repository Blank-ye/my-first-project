package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    /*
    * 往套餐中新增菜品
    *
    * */
    void setmealAddDish(List<SetmealDish> setmealDishes);

    List<Long> selectSetmealIdByDishId(List<Long> dishID);



    /*
    * 根据套餐id删除关联的套餐
    *
    * */
    void deleteBySetmealId(List<Long> ids);

    @Delete("delete from setmeal_dish where setmeal_id=#{setmeal}")
    void deleteBySetMealId(Long setmealId);


    void inseretBarce(List<SetmealDish> setmealDishes);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId} ")
    List<SetmealDish> getWithSetmealById(Long setmealId);

    @Select("select d.*  from dish d LEFT JOIN setmeal_dish s on s.dish_id = d.id where s.setmeal_id=#{setmealId}")
    List<Dish> selectDishStatusBySetmealId(Long setmealId);
}
