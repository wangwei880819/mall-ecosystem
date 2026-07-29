package com.igou.mall.dao;

import com.igou.mall.model.entity.ProductCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductCategoryMapper {
    @Select("SELECT * FROM product_category WHERE id = #{id}")
    ProductCategory findById(@Param("id") Long id);

    @Select("SELECT * FROM product_category WHERE category_code = #{categoryCode}")
    ProductCategory findByCode(@Param("categoryCode") String categoryCode);

    @Select("SELECT * FROM product_category WHERE parent_id = #{parentId} ORDER BY sort_order ASC")
    List<ProductCategory> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM product_category WHERE level = #{level} ORDER BY sort_order ASC")
    List<ProductCategory> findByLevel(@Param("level") Integer level);

    @Select("SELECT * FROM product_category ORDER BY level ASC, sort_order ASC")
    List<ProductCategory> findAll();

    @Insert("INSERT INTO product_category (category_code, category_name, parent_id, level, sort_order, icon_url, status, create_time, update_time) VALUES (#{categoryCode}, #{categoryName}, #{parentId}, #{level}, #{sortOrder}, #{iconUrl}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductCategory category);

    @Update("UPDATE product_category SET category_code=#{categoryCode}, category_name=#{categoryName}, parent_id=#{parentId}, level=#{level}, sort_order=#{sortOrder}, icon_url=#{iconUrl}, status=#{status}, update_time=NOW() WHERE id=#{id}")
    int update(ProductCategory category);

    @Update("UPDATE product_category SET status='DELETED', update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);
}