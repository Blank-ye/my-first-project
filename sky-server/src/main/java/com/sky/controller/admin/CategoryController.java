package com.sky.controller.admin;



import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.impl.CategoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/*
* 分类
* */
@RestController("adminCategoryController")
@Slf4j
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    private CategoryServiceImpl categoryService;

    /*
    * 新增分类
    *
    * */
    @PostMapping
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类，{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /*
    * 分页查询
    *
    * */
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询，{}",categoryPageQueryDTO);
        PageResult pageResult= categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);

    }

    /*
    * 删除分类
    *
    * */
    @DeleteMapping
    public Result deleteById(Long id){
        log.info("删除id，{}",id);
        categoryService.deleteById(id);
        return Result.success();
    }


    /*
    * 修改分类
    *
    * */
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类，{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /*
    * 启用，禁用分类
    *
    * */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status ,Long id ){
        log.info("启用禁用分类，{}，{}",status,id);
        categoryService.startOrStop(status,id);
        return Result.success();

    }

    /*
    * 根据类型查询状态为启用的分类
    *
    * */
    @GetMapping("/list")
    public Result<List<Category>> selectByType(Integer type){
        log.info("根据类型查询启用的分类，{}",type);
        List<Category> list= categoryService.selectByType(type);
        return Result.success(list);
    }
}
