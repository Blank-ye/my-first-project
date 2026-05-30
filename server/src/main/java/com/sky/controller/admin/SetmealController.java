package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
* 套餐
* */
@RestController("adminSetmealController")
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
    @CacheEvict(cacheNames = "setmeal",key = "#setmealDTO.categoryId")
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
    @CacheEvict(cacheNames = "setmeal",allEntries = true)
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
    @CacheEvict(cacheNames = "setmeal",allEntries = true)
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

    /*
    * 起售禁售套餐
    *
    *
    * */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmeal",allEntries = true)
    public Result startStop(@PathVariable Integer status ,Long id ){
        log.info("起售禁售套餐，{},{}",status,id);
        setmealService.sartStop(status,id);
        return Result.success();
    }
}
