package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /*
    * 新增菜品
    * */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品，{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /*
    * 分页查询
    *
    * */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询参数,{}",dishPageQueryDTO);
        PageResult pageResult= dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    * 根据id批量删除
    *
    * */
    @DeleteMapping
    public Result DeleteById(@RequestParam List<Long> ids){
        log.info("根据id批量删除数据.{}",ids);
        dishService.DeleteById(ids);
        return Result.success();

    }

    /*
    * 根据id查询菜品
    *
    * */
    @GetMapping("/{id}")
    public Result<DishVO> selecById(@PathVariable Long id){
        log.info("根据id查询菜品，{}",id);
        DishVO dishVO= dishService.selectDishWithFlavorById(id);
        return Result.success(dishVO);

    }

    /*
    * 修改菜品信息和口味
    *
    * */
    @PutMapping
    public Result update(DishDTO dishDTO){
        log.info("修改菜品信息，{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }
}
