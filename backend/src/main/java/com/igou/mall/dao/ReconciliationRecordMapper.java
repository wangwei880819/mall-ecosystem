package com.igou.mall.dao;

import com.igou.mall.model.entity.ReconciliationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReconciliationRecordMapper {
    @Select("SELECT * FROM reconciliation_record ORDER BY create_time DESC")
    List<ReconciliationRecord> findAll();

    @Select("SELECT * FROM reconciliation_record WHERE recon_period = #{reconPeriod} ORDER BY create_time DESC")
    List<ReconciliationRecord> findByPeriod(@Param("reconPeriod") String reconPeriod);

    @Select("SELECT * FROM reconciliation_record WHERE recon_code = #{reconCode}")
    ReconciliationRecord findByCode(@Param("reconCode") String reconCode);

    @Select("SELECT * FROM reconciliation_record WHERE id = #{id}")
    ReconciliationRecord findById(@Param("id") Long id);

    @Insert("INSERT INTO reconciliation_record (recon_code, recon_period, total_order_count, total_order_amount, pay_order_count, pay_order_amount, diff_count, diff_amount, status, result_detail, create_time, update_time) VALUES (#{reconCode}, #{reconPeriod}, #{totalOrderCount}, #{totalOrderAmount}, #{payOrderCount}, #{payOrderAmount}, #{diffCount}, #{diffAmount}, #{status}, #{resultDetail}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReconciliationRecord record);

    @Update("UPDATE reconciliation_record SET recon_code=#{reconCode}, recon_period=#{reconPeriod}, total_order_count=#{totalOrderCount}, total_order_amount=#{totalOrderAmount}, pay_order_count=#{payOrderCount}, pay_order_amount=#{payOrderAmount}, diff_count=#{diffCount}, diff_amount=#{diffAmount}, status=#{status}, result_detail=#{resultDetail}, update_time=NOW() WHERE id=#{id}")
    int update(ReconciliationRecord record);
}