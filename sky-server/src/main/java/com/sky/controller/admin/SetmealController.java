package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.service.impl.SetmealServiceImpl;
import com.sky.vo.SetmealVO;
import jakarta.validation.constraints.Max;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /*
    * 新增套餐
    *
    * */
    @PostMapping
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐，{}",setmealDTO);
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    /*
    * 分页查询
    *
    * */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){

        log.info("分页查询，{}",setmealPageQueryDTO);
        PageResult pageResult= setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);

    }

    /*
    * 根据id批量删除套餐
    *
    * */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("根据id删除套餐，{}",ids);
        setmealService.deleteByIds(ids);
        return Result.success();
    }

    /*
    * 修改套餐信息
    *
    * */
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐信息，{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /*
    * 根据id查询套餐信息
    *
    * */
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐信息，{}",id);
        SetmealVO setmealVO= setmealService.getById(id);
        return Result.success(setmealVO);
    }
}
