package com.igou.mall.dao;

import com.igou.mall.model.entity.ContractTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContractTemplateMapper {
    @Select("SELECT * FROM contract_template WHERE id = #{id}")
    ContractTemplate findById(Long id);

    @Select("SELECT * FROM contract_template WHERE status = 'ACTIVE' ORDER BY create_time DESC")
    List<ContractTemplate> findAllActive();

    @Select("<script>SELECT * FROM contract_template WHERE 1=1" +
            "<if test='type != null'> AND template_type = #{type}</if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}</script>")
    List<ContractTemplate> findPage(@Param("offset") int offset, @Param("limit") int limit,
                                     @Param("type") String type);

    @Insert("INSERT INTO contract_template(template_code, template_name, template_type, content, variables, status, create_time, update_time) " +
            "VALUES(#{templateCode}, #{templateName}, #{templateType}, #{content}, #{variables}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ContractTemplate template);

    @Update("UPDATE contract_template SET template_name=#{templateName}, template_type=#{templateType}, " +
            "content=#{content}, variables=#{variables}, status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    void update(ContractTemplate template);
}