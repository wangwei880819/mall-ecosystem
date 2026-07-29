package com.igou.mall.dao;

import com.igou.mall.model.entity.RefundApply;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RefundApplyMapper {
    @Select("SELECT * FROM refund_apply WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<RefundApply> findByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM refund_apply WHERE customer_id = #{customerId} ORDER BY create_time DESC")
    List<RefundApply> findByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT * FROM refund_apply WHERE status = #{status} ORDER BY create_time DESC")
    List<RefundApply> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM refund_apply WHERE id = #{id}")
    RefundApply findById(@Param("id") Long id);

    @Select("SELECT * FROM refund_apply WHERE order_code = #{orderCode}")
    RefundApply findByOrderCode(@Param("orderCode") String orderCode);

    @Insert("INSERT INTO refund_apply (order_code, customer_id, merchant_id, product_id, refund_type, refund_amount, reason, images, status, merchant_remark, audit_time, auditor, audit_comment, refund_time, refund_no, create_time, update_time) VALUES (#{orderCode}, #{customerId}, #{merchantId}, #{productId}, #{refundType}, #{refundAmount}, #{reason}, #{images}, #{status}, #{merchantRemark}, #{auditTime}, #{auditor}, #{auditComment}, #{refundTime}, #{refundNo}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RefundApply refundApply);

    @Update("UPDATE refund_apply SET order_code=#{orderCode}, customer_id=#{customerId}, merchant_id=#{merchantId}, product_id=#{productId}, refund_type=#{refundType}, refund_amount=#{refundAmount}, reason=#{reason}, images=#{images}, status=#{status}, merchant_remark=#{merchantRemark}, audit_time=#{auditTime}, auditor=#{auditor}, audit_comment=#{auditComment}, refund_time=#{refundTime}, refund_no=#{refundNo}, update_time=NOW() WHERE id=#{id}")
    int update(RefundApply refundApply);
}