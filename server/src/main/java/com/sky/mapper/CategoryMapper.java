package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /*
    * 新增分类
    *
    * */
    @Insert("insert into category(type,name,sort,status,create_time,update_time,create_user,update_user)" +
            "values " +
            "(#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void save(Category category);

    /*
    * 分页查询
    *
    * */
    List<Category> page(CategoryPageQueryDTO categoryPageQueryDTO);

    /*
    * 根据id删除分类
    *
    * */
    @Delete("delete from category where id=#{id}")
    void deleteById(Long id);

    /*
    * 修改分类
    *
    * */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

    /*
    * 根据类型查询启用的分类
    *
    * */
    List<Category> selectByType(Integer type);
}
