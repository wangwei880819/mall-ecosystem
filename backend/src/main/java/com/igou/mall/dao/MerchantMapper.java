package com.igou.mall.dao;

import com.igou.mall.model.entity.Merchant;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MerchantMapper {
    @Select("SELECT * FROM merchant ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Merchant> findAll(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM merchant WHERE id = #{id}")
    Merchant findById(@Param("id") Long id);

    @Select("SELECT * FROM merchant WHERE merchant_code = #{merchantCode}")
    Merchant findByCode(@Param("merchantCode") String merchantCode);

    @Select("SELECT * FROM merchant WHERE contact_name = #{contactName} AND password = #{password}")
    Merchant findByContactNameAndPassword(@Param("contactName") String contactName, @Param("password") String password);

    @Select("SELECT * FROM merchant WHERE contact_phone = #{contactPhone} AND password = #{password}")
    Merchant findByContactPhoneAndPassword(@Param("contactPhone") String contactPhone, @Param("password") String password);

    @Select("SELECT * FROM merchant WHERE contact_name = #{contactName}")
    Merchant findByContactName(@Param("contactName") String contactName);

    @Select("SELECT * FROM merchant WHERE contact_phone = #{contactPhone}")
    Merchant findByContactPhone(@Param("contactPhone") String contactPhone);

    @Select("SELECT * FROM merchant WHERE status = #{status} ORDER BY create_time DESC")
    List<Merchant> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM merchant WHERE onboarding_status = #{onboardingStatus} ORDER BY create_time DESC")
    List<Merchant> findByOnboardingStatus(@Param("onboardingStatus") String onboardingStatus);

    @Insert("INSERT INTO merchant (merchant_code, merchant_name, merchant_type, credit_code, legal_person, registered_capital, business_scope, contact_name, contact_phone, password, province, city, district, address, bank_name, bank_account, tax_number, onboarding_step, onboarding_status, risk_level, merchant_grade, commission_rate, settle_account, settle_cycle, status, reject_reason, industry, credit_score, legal_person_id, trademark_no, auth_chain, category_match, audit_node, audit_node_deadline, create_time, update_time) VALUES (#{merchantCode}, #{merchantName}, #{merchantType}, #{creditCode}, #{legalPerson}, #{registeredCapital}, #{businessScope}, #{contactName}, #{contactPhone}, #{password}, #{province}, #{city}, #{district}, #{address}, #{bankName}, #{bankAccount}, #{taxNumber}, #{onboardingStep}, #{onboardingStatus}, #{riskLevel}, #{merchantGrade}, #{commissionRate}, #{settleAccount}, #{settleCycle}, #{status}, #{rejectReason}, #{industry}, #{creditScore}, #{legalPersonId}, #{trademarkNo}, #{authChain}, #{categoryMatch}, #{auditNode}, #{auditNodeDeadline}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Merchant merchant);

    @Update("UPDATE merchant SET merchant_name=#{merchantName}, merchant_type=#{merchantType}, credit_code=#{creditCode}, legal_person=#{legalPerson}, registered_capital=#{registeredCapital}, business_scope=#{businessScope}, contact_name=#{contactName}, contact_phone=#{contactPhone}, province=#{province}, city=#{city}, district=#{district}, address=#{address}, bank_name=#{bankName}, bank_account=#{bankAccount}, tax_number=#{taxNumber}, onboarding_step=#{onboardingStep}, onboarding_status=#{onboardingStatus}, risk_level=#{riskLevel}, merchant_grade=#{merchantGrade}, commission_rate=#{commissionRate}, settle_account=#{settleAccount}, settle_cycle=#{settleCycle}, status=#{status}, reject_reason=#{rejectReason}, industry=#{industry}, credit_score=#{creditScore}, legal_person_id=#{legalPersonId}, trademark_no=#{trademarkNo}, auth_chain=#{authChain}, category_match=#{categoryMatch}, audit_node=#{auditNode}, audit_node_deadline=#{auditNodeDeadline}, update_time=NOW() WHERE id=#{id}")
    int update(Merchant merchant);

    @Update("UPDATE merchant SET status='DELETED', update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Delete("DELETE FROM merchant WHERE id = #{id}")
    int forceDelete(@Param("id") Long id);

    @Select("SELECT * FROM merchant WHERE audit_node = #{auditNode} AND onboarding_status = 'REVIEWING' ORDER BY create_time DESC")
    List<Merchant> findByAuditNode(@Param("auditNode") String auditNode);

    @Update("UPDATE merchant SET status='OFF_SHELF', update_time=NOW() WHERE id=#{id}")
    int offShelf(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM merchant")
    int count();

    @Select("SELECT COUNT(*) FROM merchant WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    @Update("UPDATE merchant SET onboarding_step=#{step}, onboarding_status=#{status}, update_time=NOW() WHERE id=#{id}")
    int updateOnboarding(@Param("id") Long id, @Param("step") int step, @Param("status") String status);
}