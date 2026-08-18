package com.igou.mall.dao;

import com.igou.mall.model.entity.CrmFollowUp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CrmFollowUpMapper {
    @Select("SELECT * FROM crm_follow_up WHERE id = #{id}")
    CrmFollowUp findById(Long id);

    @Select("SELECT * FROM crm_follow_up WHERE lead_id = #{leadId} ORDER BY create_time DESC")
    List<CrmFollowUp> findByLeadId(Long leadId);

    @Insert("INSERT INTO crm_follow_up(lead_id, follow_type, content, next_plan, next_follow_time, follow_by, create_time) " +
            "VALUES(#{leadId}, #{followType}, #{content}, #{nextPlan}, #{nextFollowTime}, #{followBy}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CrmFollowUp followUp);
}