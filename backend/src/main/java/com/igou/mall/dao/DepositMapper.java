package com.igou.mall.dao;

import com.igou.mall.model.entity.Deposit;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DepositMapper {
    @Select("SELECT * FROM deposit WHERE id = #{id}")
    Deposit findById(Long id);

    @Select("SELECT * FROM deposit WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Deposit> findByMerchantId(Long merchantId);

    @Select("SELECT * FROM deposit WHERE deposit_code = #{depositCode}")
    Deposit findByCode(String depositCode);

    @Select("<script>SELECT * FROM deposit WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}</script>")
    List<Deposit> findPage(@Param("offset") int offset, @Param("limit") int limit,
                           @Param("status") String status, @Param("merchantId") Long merchantId);

    @Select("<script>SELECT COUNT(*) FROM deposit WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if></script>")
    int count(@Param("status") String status, @Param("merchantId") Long merchantId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM deposit WHERE merchant_id = #{merchantId} AND deposit_type = 'PAY' AND status = 'COMPLETED'")
    BigDecimal sumPaidByMerchantId(Long merchantId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM deposit WHERE merchant_id = #{merchantId} AND deposit_type = 'REFUND' AND status = 'COMPLETED'")
    BigDecimal sumRefundedByMerchantId(Long merchantId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM deposit WHERE merchant_id = #{merchantId} AND deposit_type = 'DEDUCT' AND status = 'COMPLETED'")
    BigDecimal sumDeductedByMerchantId(Long merchantId);

    @Insert("INSERT INTO deposit(deposit_code, merchant_id, deposit_type, amount, balance, pay_method, pay_no, status, reason, approver, approve_time, create_time) " +
            "VALUES(#{depositCode}, #{merchantId}, #{depositType}, #{amount}, #{balance}, #{payMethod}, #{payNo}, #{status}, #{reason}, #{approver}, #{approveTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Deposit deposit);

    @Update("UPDATE deposit SET status=#{status}, approver=#{approver}, approve_time=#{approveTime}, reason=#{reason} WHERE id=#{id}")
    void updateStatus(Deposit deposit);
}