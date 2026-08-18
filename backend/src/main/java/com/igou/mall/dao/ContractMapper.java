package com.igou.mall.dao;

import com.igou.mall.model.entity.Contract;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContractMapper {
    @Select("SELECT * FROM contract WHERE id = #{id}")
    Contract findById(Long id);

    @Select("SELECT * FROM contract WHERE contract_code = #{contractCode}")
    Contract findByCode(String contractCode);

    @Select("SELECT * FROM contract WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Contract> findByMerchantId(Long merchantId);

    @Select("<script>SELECT * FROM contract WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}</script>")
    List<Contract> findPage(@Param("offset") int offset, @Param("limit") int limit,
                            @Param("status") String status, @Param("merchantId") Long merchantId);

    @Select("<script>SELECT COUNT(*) FROM contract WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if></script>")
    int count(@Param("status") String status, @Param("merchantId") Long merchantId);

    @Insert("INSERT INTO contract(contract_code, merchant_id, template_id, contract_type, contract_title, contract_content, file_url, sign_url, " +
            "commission_rate, deposit_amount, platform_signed, platform_sign_time, platform_signer, " +
            "merchant_signed, merchant_sign_time, status, effective_date, expire_date, remark, create_time, update_time) " +
            "VALUES(#{contractCode}, #{merchantId}, #{templateId}, #{contractType}, #{contractTitle}, #{contractContent}, #{fileUrl}, #{signUrl}, " +
            "#{commissionRate}, #{depositAmount}, #{platformSigned}, #{platformSignTime}, #{platformSigner}, " +
            "#{merchantSigned}, #{merchantSignTime}, #{status}, #{effectiveDate}, #{expireDate}, #{remark}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Contract contract);

    @Update("UPDATE contract SET contract_title=#{contractTitle}, contract_content=#{contractContent}, " +
            "file_url=#{fileUrl}, sign_url=#{signUrl}, commission_rate=#{commissionRate}, deposit_amount=#{depositAmount}, " +
            "platform_signed=#{platformSigned}, platform_sign_time=#{platformSignTime}, platform_signer=#{platformSigner}, " +
            "merchant_signed=#{merchantSigned}, merchant_sign_time=#{merchantSignTime}, " +
            "status=#{status}, effective_date=#{effectiveDate}, expire_date=#{expireDate}, remark=#{remark}, update_time=#{updateTime} WHERE id=#{id}")
    void update(Contract contract);

    @Update("UPDATE contract SET platform_signed=1, platform_sign_time=NOW(), platform_signer=#{signer}, status='SIGNED', update_time=NOW() WHERE id=#{id}")
    void platformSign(@Param("id") Long id, @Param("signer") String signer);

    @Update("UPDATE contract SET merchant_signed=1, merchant_sign_time=NOW(), status='SIGNED', update_time=NOW() WHERE id=#{id}")
    void merchantSign(Long id);
}