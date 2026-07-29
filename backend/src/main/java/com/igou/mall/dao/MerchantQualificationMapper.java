package com.igou.mall.dao;

import com.igou.mall.model.entity.MerchantQualification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MerchantQualificationMapper {
    @Select("SELECT * FROM merchant_qualification WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<MerchantQualification> findByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM merchant_qualification WHERE id = #{id}")
    MerchantQualification findById(@Param("id") Long id);

    @Insert("INSERT INTO merchant_qualification (merchant_id, doc_type, doc_name, doc_number, doc_url, status, audit_time, audit_comment, create_time, update_time) VALUES (#{merchantId}, #{docType}, #{docName}, #{docNumber}, #{docUrl}, #{status}, #{auditTime}, #{auditComment}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MerchantQualification qualification);

    @Update("UPDATE merchant_qualification SET merchant_id=#{merchantId}, doc_type=#{docType}, doc_name=#{docName}, doc_number=#{docNumber}, doc_url=#{docUrl}, status=#{status}, audit_time=#{auditTime}, audit_comment=#{auditComment}, update_time=NOW() WHERE id=#{id}")
    int update(MerchantQualification qualification);

    @Delete("DELETE FROM merchant_qualification WHERE id = #{id}")
    int delete(@Param("id") Long id);
}