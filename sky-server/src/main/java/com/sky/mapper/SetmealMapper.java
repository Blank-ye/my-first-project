package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {
    /*
    * 根据分类id查询套餐数量
    *
    * */
    @Select("select count(*) from setmeal where category_id=#{id}")
    Integer selectByCategoryId(Long id);

    /*
    * 新增套餐
    *
    * */
    @AutoFill(value = OperationType.INSERT)
    void insertSetmeal(Setmeal setmeal);

    /*
    * 分页查询
    *
    * */
    List<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

    /*
     * 根据id批量删除套餐
     *
     * */
    void deleteById(List<Long> ids);


    /*
    * 根据id查询套餐
    *
    * */
    List<Setmeal> selectById(List<Long> ids);

    /*
    * 修改套餐信息
    *
    * */
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    /*
     * 根据id查询套餐信息
     *
     * */
    @Select("select * from setmeal where id=#{id}")
    Setmeal getById(Long id);


    void updateStatus(List<Long> setmealIds, Integer status);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
