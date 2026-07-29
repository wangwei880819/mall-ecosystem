package com.igou.mall.dao;

import com.igou.mall.model.entity.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerMapper {
    @Select("SELECT * FROM customer WHERE id = #{id}")
    Customer findById(Long id);

    @Select("SELECT * FROM customer WHERE phone = #{phone}")
    Customer findByPhone(String phone);

    @Insert("INSERT INTO customer(phone, password, nickname, avatar, email, vip_level, total_amount, order_count, birth_date, gender, register_time, last_login_time, status, create_time, update_time) VALUES(#{phone}, #{password}, #{nickname}, #{avatar}, #{email}, #{vipLevel}, #{totalAmount}, #{orderCount}, #{birthDate}, #{gender}, #{registerTime}, #{lastLoginTime}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Customer customer);

    @Update("UPDATE customer SET phone=#{phone}, password=#{password}, nickname=#{nickname}, avatar=#{avatar}, email=#{email}, vip_level=#{vipLevel}, total_amount=#{totalAmount}, order_count=#{orderCount}, birth_date=#{birthDate}, gender=#{gender}, register_time=#{registerTime}, last_login_time=#{lastLoginTime}, status=#{status}, update_time=NOW() WHERE id=#{id}")
    void update(Customer customer);

    @Delete("DELETE FROM customer WHERE id=#{id}")
    void delete(Long id);

    @Select("SELECT * FROM customer ORDER BY register_time DESC LIMIT #{offset}, #{limit}")
    List<Customer> findPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM customer")
    Integer count();

    @Select("SELECT * FROM customer WHERE nickname LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')")
    List<Customer> search(@Param("keyword") String keyword);

    @Update("UPDATE customer SET vip_level=#{vipLevel} WHERE id=#{id}")
    void updateVipLevel(@Param("id") Long id, @Param("vipLevel") String vipLevel);
}