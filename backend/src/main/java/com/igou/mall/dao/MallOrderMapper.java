package com.igou.mall.dao;

import com.igou.mall.model.entity.MallOrder;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface MallOrderMapper {
    @Select("SELECT * FROM mall_order ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<MallOrder> findAll(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM mall_order WHERE id = #{id}")
    MallOrder findById(@Param("id") Long id);

    @Select("SELECT * FROM mall_order WHERE order_code = #{orderCode}")
    MallOrder findByCode(@Param("orderCode") String orderCode);

    @Select("SELECT * FROM mall_order WHERE customer_id = #{customerId} ORDER BY create_time DESC")
    List<MallOrder> findByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT * FROM mall_order WHERE customer_phone = #{customerPhone} ORDER BY create_time DESC")
    List<MallOrder> findByCustomerPhone(@Param("customerPhone") String customerPhone);

    @Select("SELECT * FROM mall_order WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<MallOrder> findByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM mall_order WHERE status = #{status} ORDER BY create_time DESC")
    List<MallOrder> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM mall_order WHERE merchant_id = #{merchantId} AND status = #{status} ORDER BY create_time DESC")
    List<MallOrder> findByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") String status);

    @Insert("INSERT INTO mall_order (order_code, customer_phone, merchant_id, product_id, product_name, product_image, price, quantity, order_amount, ai_dou_deduct, pay_amount, discount_amount, status, pay_method, delivery_address_id, remark, create_time, update_time) VALUES (#{orderCode}, #{customerPhone}, #{merchantId}, #{productId}, #{productName}, #{productImage}, #{price}, #{quantity}, #{orderAmount}, #{aiDouDeduct}, #{payAmount}, #{discountAmount}, #{status}, #{payMethod}, #{deliveryAddressId}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MallOrder mallOrder);

    @Update("UPDATE mall_order SET customer_id=#{customerId}, customer_phone=#{customerPhone}, merchant_id=#{merchantId}, product_id=#{productId}, product_name=#{productName}, product_price=#{productPrice}, quantity=#{quantity}, order_amount=#{orderAmount}, pay_amount=#{payAmount}, pay_method=#{payMethod}, pay_time=#{payTime}, status=#{status}, refund_status=#{refundStatus}, delivery_status=#{deliveryStatus}, logistics_no=#{logisticsNo}, address_id=#{addressId}, remark=#{remark}, update_time=NOW() WHERE id=#{id}")
    int update(MallOrder mallOrder);

    @Update("UPDATE mall_order SET status=#{status}, update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE mall_order SET status='DELETED', update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM mall_order")
    int count();

    @Select("SELECT COUNT(*) FROM mall_order WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    @Select("SELECT COUNT(*) FROM mall_order WHERE merchant_id = #{merchantId}")
    int countByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT COALESCE(SUM(order_amount), 0) FROM mall_order WHERE merchant_id = #{merchantId}")
    BigDecimal sumAmountByMerchantId(@Param("merchantId") Long merchantId);
}