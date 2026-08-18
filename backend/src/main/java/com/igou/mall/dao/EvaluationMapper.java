package com.igou.mall.dao;

import com.igou.mall.model.entity.Evaluation;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface EvaluationMapper {
    @Select("SELECT * FROM evaluation ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Evaluation> findAll(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT * FROM evaluation WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Evaluation> findByMerchant(@Param("merchantId") Long merchantId);

    @Select("SELECT AVG((score_quality + score_delivery + score_service + score_aftersale + score_value) / 5.0) " +
            "FROM evaluation WHERE merchant_id = #{merchantId}")
    Double avgScoreByMerchant(@Param("merchantId") Long merchantId);

    @Insert("INSERT INTO evaluation(order_id, merchant_id, product_id, user_phone, score_quality, " +
            "score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status) " +
            "VALUES(#{orderId}, #{merchantId}, #{productId}, #{userPhone}, #{scoreQuality}, #{scoreDelivery}, " +
            "#{scoreService}, #{scoreAftersale}, #{scoreValue}, #{content}, #{tags}, #{sentiment}, #{aiStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Evaluation evaluation);

    @Update("UPDATE evaluation SET merchant_reply = #{reply}, reply_time = NOW() WHERE id = #{id}")
    int reply(@Param("id") Long id, @Param("reply") String reply);

    @Select("SELECT COUNT(*) FROM evaluation WHERE sentiment = 'NEGATIVE'")
    int negativeCount();

    @Select("SELECT * FROM evaluation WHERE order_id = #{orderId} ORDER BY create_time DESC LIMIT 1")
    Evaluation findByOrderId(@Param("orderId") Long orderId);
}
