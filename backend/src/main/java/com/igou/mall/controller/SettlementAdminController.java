package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.SettlementMapper;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.dao.CommissionConfigMapper;
import com.igou.mall.model.entity.Settlement;
import com.igou.mall.model.entity.Merchant;
import com.igou.mall.model.entity.CommissionConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/settlement")
@CrossOrigin(origins = "*")
public class SettlementAdminController {

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private CommissionConfigMapper commissionConfigMapper;

    @GetMapping("/rules")
    public Result<List<Map<String, Object>>> getSettlementRules() {
        List<CommissionConfig> configs = commissionConfigMapper.findAllActive();
        List<Map<String, Object>> rules = new ArrayList<>();
        for (CommissionConfig c : configs) {
            Map<String, Object> rule = new HashMap<>();
            rule.put("id", c.getId());
            rule.put("merchantId", c.getMerchantId());
            rule.put("commissionRate", c.getCommissionRate());
            rule.put("rateType", c.getRateType());
            rule.put("settleType", c.getSettleType());
            rule.put("settlePeriod", c.getSettlePeriod());
            rule.put("minSettleAmount", c.getMinSettleAmount());
            Merchant merchant = merchantMapper.findById(c.getMerchantId());
            rule.put("merchantName", merchant != null ? merchant.getMerchantName() : "商户" + c.getMerchantId());
            rules.add(rule);
        }
        return Result.success(rules);
    }

    @PostMapping("/rules")
    public Result<String> saveSettlementRule(@RequestBody Map<String, Object> rule) {
        Long merchantId = Long.valueOf(rule.get("merchantId").toString());
        BigDecimal commissionRate = rule.get("commissionRate") != null
                ? new BigDecimal(rule.get("commissionRate").toString())
                : new BigDecimal("0.05");
        String settleType = rule.get("settleType") != null && !rule.get("settleType").toString().isEmpty()
                ? rule.get("settleType").toString() : "COMMISSION";
        String settlePeriod = rule.get("settlePeriod") != null ? rule.get("settlePeriod").toString() : "MONTHLY";
        BigDecimal minSettleAmount = rule.get("minSettleAmount") != null
                ? new BigDecimal(rule.get("minSettleAmount").toString())
                : new BigDecimal("100");

        // 更新或创建配置
        CommissionConfig config = commissionConfigMapper.findByMerchantAndSettleType(merchantId, settleType);
        if (config != null) {
            config.setCommissionRate(commissionRate);
            config.setSettlePeriod(settlePeriod);
            config.setMinSettleAmount(minSettleAmount);
            config.setUpdateTime(java.time.LocalDateTime.now());
            commissionConfigMapper.update(config);
        } else {
            config = new CommissionConfig();
            config.setMerchantId(merchantId);
            config.setRateType("FIXED");
            config.setCommissionRate(commissionRate);
            config.setSettleType(settleType);
            config.setSettlePeriod(settlePeriod);
            config.setMinSettleAmount(minSettleAmount);
            config.setStatus("ACTIVE");
            config.setCreateTime(java.time.LocalDateTime.now());
            config.setUpdateTime(java.time.LocalDateTime.now());
            commissionConfigMapper.insert(config);
        }

        return Result.success("规则保存成功");
    }

    @GetMapping("/records")
    public Result<List<Map<String, Object>>> getSettlementRecords(
            @RequestParam(required = false) String settleType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        List<Settlement> settlements = settlementMapper.findPage(0, 500, null, null, settleType, startTime, endTime);
        List<Map<String, Object>> records = settlements.stream().map(s -> {
            Map<String, Object> record = new HashMap<>();
            record.put("id", s.getId());
            record.put("settleCode", s.getSettleCode());
            record.put("merchantId", s.getMerchantId());
            record.put("settleType", s.getSettleType());
            record.put("settlePeriod", s.getSettlePeriod());
            record.put("totalAmount", s.getTotalAmount());
            record.put("itemCount", s.getItemCount());
            record.put("status", s.getStatus());
            record.put("approver", s.getApprover());
            record.put("approveTime", s.getApproveTime());
            record.put("createTime", s.getCreateTime());
            Merchant merchant = merchantMapper.findById(s.getMerchantId());
            record.put("merchant", merchant != null ? merchant.getMerchantName() : ("商户" + s.getMerchantId()));
            return record;
        }).collect(Collectors.toList());
        return Result.success(records);
    }

    @GetMapping("/records/total")
    public Result<Map<String, Object>> getSettlementTotal(
            @RequestParam(required = false) String settleType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        BigDecimal totalAmount = settlementMapper.sumAmount(null, null, settleType, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        return Result.success(result);
    }

    @GetMapping("/records/export")
    public void exportSettlementRecords(
            @RequestParam(required = false) String settleType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletResponse response) throws Exception {
        List<Settlement> settlements = settlementMapper.findAllForExport(settleType, startTime, endTime);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("结算记录");

        // 表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"结算编号", "所属商户", "结算类型", "结算周期", "金额", "笔数", "状态", "审批人", "审批时间", "创建时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据行
        Map<String, String> typeMap = Map.of("AI_DOU", "AI豆结算", "COMMISSION", "佣金结算", "EXPANSION", "商拓费结算");
        Map<String, String> statusMap = Map.of("COMPLETED", "已完成", "PENDING", "待审批", "PAID", "已支付");

        for (int i = 0; i < settlements.size(); i++) {
            Settlement s = settlements.get(i);
            Row row = sheet.createRow(i + 1);
            Merchant merchant = merchantMapper.findById(s.getMerchantId());
            String merchantName = merchant != null ? merchant.getMerchantName() : ("商户" + s.getMerchantId());

            row.createCell(0).setCellValue(s.getSettleCode());
            row.createCell(1).setCellValue(merchantName);
            row.createCell(2).setCellValue(typeMap.getOrDefault(s.getSettleType(), s.getSettleType()));
            row.createCell(3).setCellValue(s.getSettlePeriod());
            row.createCell(4).setCellValue(s.getTotalAmount() != null ? s.getTotalAmount().doubleValue() : 0);
            row.createCell(5).setCellValue(s.getItemCount() != null ? s.getItemCount() : 0);
            row.createCell(6).setCellValue(statusMap.getOrDefault(s.getStatus(), s.getStatus()));
            row.createCell(7).setCellValue(s.getApprover() != null ? s.getApprover() : "");
            row.createCell(8).setCellValue(s.getApproveTime() != null ? s.getApproveTime().toString() : "");
            row.createCell(9).setCellValue(s.getCreateTime() != null ? s.getCreateTime().toString() : "");
        }

        // 自动列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("结算记录_" + System.currentTimeMillis() + ".xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        OutputStream os = response.getOutputStream();
        workbook.write(os);
        workbook.close();
        os.flush();
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getSettlementOverview() {
        Map<String, Object> overview = new HashMap<>();
        BigDecimal totalSettled = settlementMapper.sumCompletedAmount();
        Integer pendingCount = settlementMapper.countPending();
        Integer completedCount = settlementMapper.countCompleted();

        overview.put("totalAmount", totalSettled != null ? totalSettled : BigDecimal.ZERO);
        overview.put("completedCount", completedCount != null ? completedCount : 0);
        overview.put("pendingCount", pendingCount != null ? pendingCount : 0);
        overview.put("totalCount", settlementMapper.countAll());
        overview.put("merchantCount", settlementMapper.countDistinctMerchants());

        return Result.success(overview);
    }
}
