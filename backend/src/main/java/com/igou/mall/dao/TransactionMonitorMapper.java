package com.igou.mall.dao;

import com.igou.mall.model.entity.TransactionMonitor;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionMonitorMapper {
    @Select("SELECT * FROM transaction_monitor ORDER BY create_time DESC")
    List<TransactionMonitor> findAll();

    @Select("SELECT * FROM transaction_monitor WHERE monitor_type = #{monitorType} ORDER BY create_time DESC")
    List<TransactionMonitor> findByType(@Param("monitorType") String monitorType);

    @Select("SELECT * FROM transaction_monitor WHERE metric_type = #{metricType} ORDER BY create_time DESC")
    List<TransactionMonitor> findByMetricType(@Param("metricType") String metricType);

    @Select("SELECT * FROM transaction_monitor WHERE status = #{status} ORDER BY create_time DESC")
    List<TransactionMonitor> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM transaction_monitor WHERE id = #{id}")
    TransactionMonitor findById(@Param("id") Long id);

    @Insert("INSERT INTO transaction_monitor (monitor_type, metric_type, metric_value, metric_unit, threshold_min, threshold_max, status, alert_level, alert_message, remark, create_time, update_time) VALUES (#{monitorType}, #{metricType}, #{metricValue}, #{metricUnit}, #{thresholdMin}, #{thresholdMax}, #{status}, #{alertLevel}, #{alertMessage}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TransactionMonitor monitor);

    @Update("UPDATE transaction_monitor SET monitor_type=#{monitorType}, metric_type=#{metricType}, metric_value=#{metricValue}, metric_unit=#{metricUnit}, threshold_min=#{thresholdMin}, threshold_max=#{thresholdMax}, status=#{status}, alert_level=#{alertLevel}, alert_message=#{alertMessage}, remark=#{remark}, update_time=NOW() WHERE id=#{id}")
    int update(TransactionMonitor monitor);
}