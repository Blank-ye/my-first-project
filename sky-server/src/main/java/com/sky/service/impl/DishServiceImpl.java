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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
    @Autowired
    private DishService dishService;

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
        dishFlavorMapper.deleteById(ids);



    }

    /*
     * 根据id查询菜品
     *
     * */
    @Override
    public DishVO selectDishWithFlavorById(Long id) {
        //根据id查询菜品
        Dish dish = dishMapper.selectById(id);

        //根据菜品id查询口味
        List<DishFlavor> dishFlavor= dishFlavorMapper.selectByDishId(id);
        //返回dishvo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavor);
        return dishVO;
    }

    /*
     * 修改菜品信息和口味
     *
     * */
    @Transactional
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品基本信息
        dishMapper.update(dish);

        //先删除菜品的口味
        dishFlavorMapper.selectByDishId(dishDTO.getId());

        //在重新插入口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors !=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //插入n条数据
            dishFlavorMapper.insertFlavor(flavors);
        }
    }

    /*
     * 起售，停售菜品
     *
     * */
    @Transactional
    @Override
    public void updateStatus(Integer status, Long id) {
        //修改菜品状态
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.update(dish);

        //如果菜品停售它所关联的套餐也要停售
        if (status==StatusConstant.DISABLE){
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            List<Long> DishIds = setmealDishMapper.selectSetmealIdByDishId(ids);
            //如果一个菜品关联多个套餐批量修改套餐的状态
            if (DishIds !=null && DishIds.size()>0){
                setmealDishMapper.updateStatus(DishIds,status);
            }
        }
    }

    /*
     * 根据分类id查询菜品
     *
     * */
    @Override
    public List<Dish> list(Long categoryId) {
        List<Dish> dishes= dishMapper.list(categoryId);
        return dishes;
    }
}
