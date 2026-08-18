package com.igou.mall.dao;

import com.igou.mall.model.entity.CommissionConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommissionConfigMapper {
    @Select("SELECT * FROM commission_config WHERE id = #{id}")
    CommissionConfig findById(Long id);

    @Select("SELECT * FROM commission_config WHERE merchant_id = #{merchantId} AND status = 'ACTIVE' ORDER BY create_time DESC")
    List<CommissionConfig> findByMerchantId(Long merchantId);

    @Select("SELECT * FROM commission_config WHERE merchant_id = #{merchantId} AND category_id = #{categoryId} AND status = 'ACTIVE' ORDER BY create_time DESC LIMIT 1")
    CommissionConfig findByMerchantAndCategory(@Param("merchantId") Long merchantId, @Param("categoryId") Long categoryId);

    @Select("SELECT * FROM commission_config WHERE merchant_id = #{merchantId} AND category_id IS NULL AND status = 'ACTIVE' ORDER BY create_time DESC LIMIT 1")
    CommissionConfig findDefaultByMerchantId(Long merchantId);

    @Select("SELECT * FROM commission_config WHERE merchant_id = #{merchantId} AND settle_type = #{settleType} AND status = 'ACTIVE' ORDER BY create_time DESC LIMIT 1")
    CommissionConfig findByMerchantAndSettleType(@Param("merchantId") Long merchantId, @Param("settleType") String settleType);

    @Select("SELECT * FROM commission_config WHERE status = 'ACTIVE' ORDER BY merchant_id, settle_type, create_time DESC")
    List<CommissionConfig> findAllActive();

    @Insert("INSERT INTO commission_config(merchant_id, category_id, rate_type, commission_rate, ladder_config, effective_date, expire_date, status, settle_type, settle_period, min_settle_amount, create_time, update_time) " +
            "VALUES(#{merchantId}, #{categoryId}, #{rateType}, #{commissionRate}, #{ladderConfig}, #{effectiveDate}, #{expireDate}, #{status}, #{settleType}, #{settlePeriod}, #{minSettleAmount}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CommissionConfig config);

    @Update("UPDATE commission_config SET rate_type=#{rateType}, commission_rate=#{commissionRate}, " +
            "ladder_config=#{ladderConfig}, effective_date=#{effectiveDate}, expire_date=#{expireDate}, " +
            "settle_type=#{settleType}, settle_period=#{settlePeriod}, min_settle_amount=#{minSettleAmount}, " +
            "status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    void update(CommissionConfig config);

    @Update("UPDATE commission_config SET status='INACTIVE', update_time=NOW() WHERE merchant_id=#{merchantId} AND status='ACTIVE'")
    void deactivateByMerchantId(Long merchantId);
}