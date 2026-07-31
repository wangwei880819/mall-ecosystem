package com.igou.mall.dao;

import com.igou.mall.model.entity.Benefit;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BenefitMapper {

    @Select("SELECT * FROM benefit ORDER BY id DESC")
    List<Benefit> findAll();

    @Select("SELECT * FROM benefit WHERE id = #{id}")
    Benefit findById(@Param("id") Long id);

    @Select("SELECT * FROM benefit WHERE benefit_code = #{benefitCode}")
    Benefit findByCode(@Param("benefitCode") String benefitCode);

    @Select("SELECT * FROM benefit WHERE merchant_id = #{merchantId} ORDER BY id DESC")
    List<Benefit> findByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM benefit WHERE status = #{status} ORDER BY id DESC")
    List<Benefit> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM benefit WHERE benefit_name = #{name} LIMIT 1")
    Benefit findByName(@Param("name") String name);

    @Insert("INSERT INTO benefit(benefit_code, benefit_name, merchant_id, benefit_type, face_value, price, settle_price, " +
            "validity_type, validity_start, validity_end, validity_days, usage_rules, applicable_scope, exchange_method, " +
            "stock_total, stock_used, stock_daily_limit, stock_per_user, supplier_name, supplier_contact, " +
            "refund_policy, image_url, detail_desc, benefit_description, ai_tag, ai_selling_point, status) " +
            "VALUES(#{benefitCode}, #{benefitName}, #{merchantId}, #{benefitType}, #{faceValue}, #{price}, #{settlePrice}, " +
            "#{validityType}, #{validityStart}, #{validityEnd}, #{validityDays}, #{usageRules}, #{applicableScope}, #{exchangeMethod}, " +
            "#{stockTotal}, #{stockUsed}, #{stockDailyLimit}, #{stockPerUser}, #{supplierName}, #{supplierContact}, " +
            "#{refundPolicy}, #{imageUrl}, #{detailDesc}, #{benefitDescription}, #{aiTag}, #{aiSellingPoint}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Benefit benefit);

    @Update("UPDATE benefit SET benefit_name=#{benefitName}, benefit_type=#{benefitType}, face_value=#{faceValue}, " +
            "price=#{price}, settle_price=#{settlePrice}, validity_type=#{validityType}, validity_start=#{validityStart}, " +
            "validity_end=#{validityEnd}, validity_days=#{validityDays}, usage_rules=#{usageRules}, applicable_scope=#{applicableScope}, " +
            "exchange_method=#{exchangeMethod}, stock_total=#{stockTotal}, stock_used=#{stockUsed}, stock_daily_limit=#{stockDailyLimit}, " +
            "stock_per_user=#{stockPerUser}, supplier_name=#{supplierName}, supplier_contact=#{supplierContact}, " +
            "refund_policy=#{refundPolicy}, image_url=#{imageUrl}, detail_desc=#{detailDesc}, benefit_description=#{benefitDescription}, " +
            "ai_tag=#{aiTag}, ai_selling_point=#{aiSellingPoint}, status=#{status} WHERE id=#{id}")
    int update(Benefit benefit);

    @Update("UPDATE benefit SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE benefit SET stock_used=stock_used+1 WHERE id=#{id} AND stock_used < stock_total")
    int incrStockUsed(@Param("id") Long id);

    @Delete("DELETE FROM benefit WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
}
