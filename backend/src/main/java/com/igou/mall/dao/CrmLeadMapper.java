package com.igou.mall.dao;

import com.igou.mall.model.entity.CrmLead;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CrmLeadMapper {
    @Select("SELECT * FROM crm_lead WHERE id = #{id}")
    CrmLead findById(Long id);

    @Select("SELECT * FROM crm_lead WHERE lead_code = #{leadCode}")
    CrmLead findByCode(String leadCode);

    @Select("<script>SELECT * FROM crm_lead WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='assignedTo != null'> AND assigned_to = #{assignedTo}</if>" +
            "<if test='keyword != null'> AND (company_name LIKE CONCAT('%',#{keyword},'%') OR brand_name LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}</script>")
    List<CrmLead> findPage(@Param("offset") int offset, @Param("limit") int limit,
                           @Param("status") String status, @Param("assignedTo") String assignedTo,
                           @Param("keyword") String keyword);

    @Select("<script>SELECT COUNT(*) FROM crm_lead WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='assignedTo != null'> AND assigned_to = #{assignedTo}</if>" +
            "<if test='keyword != null'> AND (company_name LIKE CONCAT('%',#{keyword},'%') OR brand_name LIKE CONCAT('%',#{keyword},'%'))</if></script>")
    int count(@Param("status") String status, @Param("assignedTo") String assignedTo, @Param("keyword") String keyword);

    @Select("SELECT status, COUNT(*) as cnt FROM crm_lead GROUP BY status")
    List<CrmLead> countByStatus();

    @Insert("INSERT INTO crm_lead(lead_code, company_name, brand_name, industry, contact_name, contact_phone, contact_email, " +
            "source, status, intention_level, estimated_gmv, assigned_to, remark, create_time, update_time) " +
            "VALUES(#{leadCode}, #{companyName}, #{brandName}, #{industry}, #{contactName}, #{contactPhone}, #{contactEmail}, " +
            "#{source}, #{status}, #{intentionLevel}, #{estimatedGmv}, #{assignedTo}, #{remark}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CrmLead lead);

    @Update("UPDATE crm_lead SET status=#{status}, intention_level=#{intentionLevel}, assigned_to=#{assignedTo}, " +
            "lost_reason=#{lostReason}, remark=#{remark}, update_time=#{updateTime} WHERE id=#{id}")
    void update(CrmLead lead);

    @Update("UPDATE crm_lead SET status=#{status}, update_time=NOW() WHERE id=#{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}