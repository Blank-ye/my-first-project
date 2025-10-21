package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishPageQueryDTO;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /*
     * 新增菜品和菜品口味
     *
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);
        //插入一条数据
        dishMapper.insert(dish);

        Long id = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors !=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
            //插入n条数据
            dishFlavorMapper.insertFlavor(flavors);
        }
    }

    /*
    * 分页查询
    *
    * */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        List<DishVO> list= dishMapper.pageQuery(dishPageQueryDTO);

        PageInfo<DishVO> dishVOPageInfo = new PageInfo<>(list);
        return new PageResult(dishVOPageInfo.getTotal(),dishVOPageInfo.getList());
    }

    /*
    * 根据id批量删除菜品数据
    *
    * */
    @Transactional
    @Override
    public void DeleteById(List<Long> ids) {
        //菜品是否可以删除--菜品是否处于起售状态？
        for (Long id : ids) {
            Dish dish= dishMapper.selectById(id);
            if (dish.getStatus()== StatusConstant.ENABLE){
                throw  new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }

        }

        //菜品是否可以删除--菜品是否关联的有套餐？
        List<Long> setmealId= setmealDishMapper.selectSetmealIdByDishId(ids);
        if (setmealId !=null && setmealId.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品
        dishMapper.deleteById(ids);
        //删除菜品关联的口味
        dishFlavorMapper.deletById(ids);



    }
}
