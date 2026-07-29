package com.igou.mall.dao;

import com.igou.mall.model.entity.Invoice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InvoiceMapper {
    @Select("SELECT * FROM invoice ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Invoice> findAll(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM invoice WHERE customer_id = #{customerId} ORDER BY create_time DESC")
    List<Invoice> findByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT * FROM invoice WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Invoice> findByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM invoice WHERE order_code = #{orderCode}")
    List<Invoice> findByOrderCode(@Param("orderCode") String orderCode);

    @Select("SELECT * FROM invoice WHERE id = #{id}")
    Invoice findById(@Param("id") Long id);

    @Insert("INSERT INTO invoice (invoice_code, invoice_no, order_code, customer_id, merchant_id, invoice_type, title, tax_number, amount, status, issue_time, pdf_url, remark, create_time, update_time) VALUES (#{invoiceCode}, #{invoiceNo}, #{orderCode}, #{customerId}, #{merchantId}, #{invoiceType}, #{title}, #{taxNumber}, #{amount}, #{status}, #{issueTime}, #{pdfUrl}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Invoice invoice);

    @Update("UPDATE invoice SET invoice_code=#{invoiceCode}, invoice_no=#{invoiceNo}, order_code=#{orderCode}, customer_id=#{customerId}, merchant_id=#{merchantId}, invoice_type=#{invoiceType}, title=#{title}, tax_number=#{taxNumber}, amount=#{amount}, status=#{status}, issue_time=#{issueTime}, pdf_url=#{pdfUrl}, remark=#{remark}, update_time=NOW() WHERE id=#{id}")
    int update(Invoice invoice);
}