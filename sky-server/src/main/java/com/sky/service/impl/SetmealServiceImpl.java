package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /*
     * 新增套餐
     *
     * */
    @Transactional
    @Override
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmeal.setStatus(StatusConstant.ENABLE);
        //在套餐中插入一体信息
        setmealMapper.insertSetmeal(setmeal);
        Long id = setmeal.getId();
        //在菜品中插入n条数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes !=null && setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(id);
            });
        }
        setmealDishMapper.setmealAddDish(setmealDishes);
    }

    /*
     * 分页查询
     *
     * */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        List<SetmealVO> list= setmealMapper.page(setmealPageQueryDTO);

        PageInfo<Object> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }

    /*
     * 根据id批量删除套餐
     *
     * */
    @Transactional
    @Override
    public void deleteByIds(List<Long> ids) {

        List<Setmeal> setmeal= setmealMapper.selectById(ids);
        for (Setmeal setmeal1 : setmeal) {
            if (setmeal1.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        setmealMapper.deleteById(ids);
        setmealDishMapper.deleteBySetmealId(ids);
    }

    /*
     * 修改套餐信息
     *
     * */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.update(setmeal);

        Long setmealId = setmeal.getId();

        //删除套餐关联的菜品
        setmealDishMapper.deleteBySetMealId(setmealId);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        setmealDishMapper.inseretBarce(setmealDishes);

    }

    /*
     * 根据id查询套餐信息
     *
     * */
    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal= setmealMapper.getById(id);

        List<SetmealDish> setmealDishes= setmealDishMapper.getWithSetmealById(id);

        SetmealVO setmealVO = new SetmealVO();

        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /*
     * 起售禁售套餐
     *
     * */
    @Override
    public void sartStop(Integer status, Long id) {
        //根据id查询套餐所含的菜品，判断是否停售，如果停售则套餐不可起售
        if (status==StatusConstant.ENABLE){
            List<Dish> dishes=  setmealDishMapper.selectDishStatusBySetmealId(id);
            if(dishes !=null && dishes.size()>0){
                dishes.forEach(dish -> {
                    if(dish.getStatus()==StatusConstant.DISABLE){
                        throw  new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setmealMapper.update(setmeal);
    }
}
