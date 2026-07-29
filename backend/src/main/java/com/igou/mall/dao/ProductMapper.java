package com.igou.mall.dao;

import com.igou.mall.model.entity.Product;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProductMapper {
    @Select("SELECT * FROM product ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> findAll(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(@Param("id") Long id);

    @Select("SELECT * FROM product WHERE product_code = #{productCode}")
    Product findByCode(@Param("productCode") String productCode);

    @Select("SELECT * FROM product WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Product> findByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM product WHERE category = #{category} ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> findByCategory(@Param("category") String category, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM product WHERE category_id = #{categoryId} ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM product WHERE product_name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> search(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM product WHERE status = 'ON_SHELF' ORDER BY sales_count DESC LIMIT #{limit}")
    List<Product> findHotProducts(@Param("limit") int limit);

    @Select("SELECT * FROM product WHERE status = 'ON_SHELF' ORDER BY create_time DESC LIMIT #{limit}")
    List<Product> findNewProducts(@Param("limit") int limit);

    @Insert("INSERT INTO product (product_code, product_name, description, detail, category, category_id, brand, merchant_id, price, market_price, vip_price, stock, sales_count, avg_score, image_urls, status, reject_reason, ai_selling_point, ai_tag, spec, is_hot, is_new, is_recommend, sort_order, create_time, update_time) VALUES (#{productCode}, #{productName}, #{description}, #{detail}, #{category}, #{categoryId}, #{brand}, #{merchantId}, #{price}, #{marketPrice}, #{vipPrice}, #{stock}, #{salesCount}, #{avgScore}, #{imageUrls}, #{status}, #{rejectReason}, #{aiSellingPoint}, #{aiTag}, #{spec}, #{isHot}, #{isNew}, #{isRecommend}, #{sortOrder}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET product_name=#{productName}, description=#{description}, detail=#{detail}, category=#{category}, category_id=#{categoryId}, brand=#{brand}, merchant_id=#{merchantId}, price=#{price}, market_price=#{marketPrice}, vip_price=#{vipPrice}, stock=#{stock}, sales_count=#{salesCount}, avg_score=#{avgScore}, image_urls=#{imageUrls}, status=#{status}, reject_reason=#{rejectReason}, approve_reason=#{approveReason}, auditor=#{auditor}, audit_time=#{auditTime}, update_time=NOW() WHERE id=#{id}")
    int update(Product product);

    @Update("UPDATE product SET stock = stock + #{quantity}, update_time=NOW() WHERE id=#{id}")
    int updateStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("UPDATE product SET status='DELETED', update_time=NOW() WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Delete("DELETE FROM product WHERE merchant_id = #{merchantId}")
    int deleteByMerchantId(@Param("merchantId") Long merchantId);

    @Update("UPDATE product SET status='OFF_SHELF', update_time=NOW() WHERE merchant_id = #{merchantId} AND status='ON_SHELF'")
    int offShelfByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT COUNT(*) FROM product")
    int count();

    @Select("SELECT COUNT(*) FROM product WHERE category = #{category}")
    int countByCategory(@Param("category") String category);

    @Select("SELECT * FROM product WHERE status = #{status} ORDER BY create_time DESC")
    List<Product> findByStatus(@Param("status") String status);

    @Select("SELECT COUNT(*) FROM evaluation WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);

    @Select("SELECT COALESCE(AVG(score), 0) FROM evaluation WHERE product_id = #{productId}")
    BigDecimal avgScoreByProductId(@Param("productId") Long productId);
}