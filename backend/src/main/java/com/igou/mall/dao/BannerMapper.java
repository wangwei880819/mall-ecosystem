package com.igou.mall.dao;

import com.igou.mall.model.entity.Banner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BannerMapper {
    @Select("SELECT * FROM banner ORDER BY sort ASC, id DESC")
    List<Banner> findAll();

    @Select("SELECT * FROM banner WHERE id = #{id}")
    Banner findById(@Param("id") Long id);

    @Insert("INSERT INTO banner (image_url, link_url, sort, status, create_time, update_time) VALUES (#{imageUrl}, #{linkUrl}, #{sort}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Banner banner);

    @Update("UPDATE banner SET image_url=#{imageUrl}, link_url=#{linkUrl}, sort=#{sort}, status=#{status}, update_time=NOW() WHERE id=#{id}")
    int update(Banner banner);

    @Delete("DELETE FROM banner WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
