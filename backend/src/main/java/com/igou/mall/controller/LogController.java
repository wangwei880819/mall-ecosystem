package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.SysLogMapper;
import com.igou.mall.model.entity.SysLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/log")
@CrossOrigin(origins = "*")
public class LogController {

    @Autowired
    private SysLogMapper sysLogMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        List<SysLog> list = sysLogMapper.findPage(page * size, size, logType, operator, module, result, startTime, endTime);
        int total = sysLogMapper.count(logType, operator, module, result, startTime, endTime);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("total", total);
        return Result.success(resultMap);
    }

    @GetMapping("/{id}")
    public Result<SysLog> getDetail(@PathVariable Long id) {
        SysLog sysLog = sysLogMapper.findById(id);
        if (sysLog == null) {
            return Result.error("日志不存在");
        }
        return Result.success(sysLog);
    }

    @GetMapping("/types")
    public Result<List<String>> getTypes() {
        return Result.success(Arrays.asList(
                "AUTH", "MERCHANT", "PRODUCT", "ORDER", "FINANCE", "RISK", "CUSTOMER", "AI", "SYSTEM", "OTHER"
        ));
    }

    @GetMapping("/modules")
    public Result<List<String>> getModules() {
        return Result.success(Arrays.asList(
                "商户管理", "商品管理", "订单管理", "财务管理", "风险管理", "客户管理", "系统管理", "AI服务", "C端商城", "其他"
        ));
    }
}