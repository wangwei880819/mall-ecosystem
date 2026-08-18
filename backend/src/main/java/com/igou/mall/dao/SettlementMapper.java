package com.igou.mall.dao;

import com.igou.mall.model.entity.Settlement;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SettlementMapper {
    @Select("SELECT * FROM settlement WHERE id = #{id}")
    Settlement findById(Long id);

    @Select("<script>" +
            "SELECT * FROM settlement WHERE 1=1 " +
            "<if test='merchantId != null'>AND merchant_id = #{merchantId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='settleType != null and settleType != \"\"'>AND settle_type = #{settleType}</if> " +
            "<if test='startTime != null'>AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'>AND create_time &lt;= #{endTime}</if> " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Settlement> findPage(@Param("offset") Integer offset, @Param("limit") Integer limit,
                              @Param("merchantId") Long merchantId, @Param("status") String status,
                              @Param("settleType") String settleType,
                              @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("<script>" +
            "SELECT COUNT(*) FROM settlement WHERE 1=1 " +
            "<if test='merchantId != null'>AND merchant_id = #{merchantId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='settleType != null and settleType != \"\"'>AND settle_type = #{settleType}</if> " +
            "<if test='startTime != null'>AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'>AND create_time &lt;= #{endTime}</if>" +
            "</script>")
    Integer count(@Param("merchantId") Long merchantId, @Param("status") String status,
                  @Param("settleType") String settleType,
                  @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("<script>" +
            "SELECT COALESCE(SUM(total_amount), 0) FROM settlement WHERE 1=1 " +
            "<if test='merchantId != null'>AND merchant_id = #{merchantId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='settleType != null and settleType != \"\"'>AND settle_type = #{settleType}</if> " +
            "<if test='startTime != null'>AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'>AND create_time &lt;= #{endTime}</if>" +
            "</script>")
    BigDecimal sumAmount(@Param("merchantId") Long merchantId, @Param("status") String status,
                         @Param("settleType") String settleType,
                         @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("<script>" +
            "SELECT * FROM settlement WHERE 1=1 " +
            "<if test='settleType != null and settleType != \"\"'>AND settle_type = #{settleType}</if> " +
            "<if test='startTime != null'>AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'>AND create_time &lt;= #{endTime}</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Settlement> findAllForExport(@Param("settleType") String settleType,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM settlement WHERE status = 'COMPLETED'")
    BigDecimal sumCompletedAmount();

    @Select("SELECT COUNT(*) FROM settlement WHERE status = 'PENDING'")
    Integer countPending();

    @Select("SELECT COUNT(*) FROM settlement WHERE status = 'COMPLETED'")
    Integer countCompleted();

    @Select("SELECT COUNT(*) FROM settlement")
    Integer countAll();

    @Select("SELECT COUNT(DISTINCT merchant_id) FROM settlement")
    Integer countDistinctMerchants();

    @Insert("INSERT INTO settlement(settle_code, merchant_id, settle_type, settle_period, total_amount, item_count, status, approver, approve_time, create_time) " +
            "VALUES(#{settleCode}, #{merchantId}, #{settleType}, #{settlePeriod}, #{totalAmount}, #{itemCount}, #{status}, #{approver}, #{approveTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Settlement settlement);

    @Update("UPDATE settlement SET settle_code=#{settleCode}, merchant_id=#{merchantId}, settle_type=#{settleType}, " +
            "settle_period=#{settlePeriod}, total_amount=#{totalAmount}, item_count=#{itemCount}, " +
            "status=#{status}, approver=#{approver}, approve_time=#{approveTime} WHERE id=#{id}")
    void update(Settlement settlement);

    @Delete("DELETE FROM settlement WHERE id=#{id}")
    void delete(Long id);
}
