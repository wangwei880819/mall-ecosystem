package com.igou.mall.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igou.mall.dao.SysLogMapper;
import com.igou.mall.model.entity.SysLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_PARAM_LENGTH = 4000;
    private static final int MAX_RESPONSE_LENGTH = 4000;

    @Autowired
    private SysLogMapper sysLogMapper;

    @Around("execution(* com.igou.mall.controller..*(..)) && !execution(* com.igou.mall.controller.LogController.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        SysLog sysLog = new SysLog();
        sysLog.setCreateTime(LocalDateTime.now());

        String requestUri = "";
        String requestMethod = "";
        String operator = "anonymous";
        Long operatorId = null;

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestUri = request.getRequestURI();
            requestMethod = request.getMethod();
            sysLog.setIpAddress(getClientIp(request));
            sysLog.setUserAgent(request.getHeader("User-Agent"));

            // 从 JWT 或 request attribute 中获取用户信息
            Object userId = request.getAttribute("authUserId");
            Object username = request.getAttribute("authUsername");
            if (userId != null) {
                operatorId = (Long) userId;
            }
            if (username != null) {
                operator = (String) username;
            }
        }

        // 提取模块和操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = signature.getDeclaringType().getSimpleName();
        sysLog.setModule(extractModule(className, requestUri));
        sysLog.setOperation(extractOperation(requestMethod, requestUri));
        sysLog.setLogType(extractLogType(requestUri));

        sysLog.setRequestUri(requestUri);
        sysLog.setRequestMethod(requestMethod);
        sysLog.setOperator(operator);
        sysLog.setOperatorId(operatorId);

        // 记录请求参数
        try {
            String params = buildParams(joinPoint.getArgs());
            sysLog.setRequestParams(truncate(params, MAX_PARAM_LENGTH));
        } catch (Exception e) {
            sysLog.setRequestParams("[参数解析失败]");
        }

        Object result;
        try {
            result = joinPoint.proceed();
            sysLog.setResult("SUCCESS");
            try {
                String responseStr = serializeResponse(result);
                sysLog.setResponseBody(truncate(responseStr, MAX_RESPONSE_LENGTH));
            } catch (Exception e) {
                sysLog.setResponseBody(truncate("[序列化失败] " + result.getClass().getSimpleName(), 200));
            }
        } catch (Throwable e) {
            sysLog.setResult("FAILURE");
            sysLog.setErrorMessage(truncate(e.getMessage(), 1000));
            try {
                sysLogMapper.insert(sysLog);
            } catch (Exception ex) {
                log.error("日志记录失败", ex);
            }
            throw e;
        }

        sysLog.setCostTime(System.currentTimeMillis() - startTime);

        try {
            sysLogMapper.insert(sysLog);
        } catch (Exception e) {
            log.error("日志记录失败", e);
        }

        return result;
    }

    private String extractModule(String className, String uri) {
        if (uri.contains("/merchant")) return "商户管理";
        if (uri.contains("/product")) return "商品管理";
        if (uri.contains("/order")) return "订单管理";
        if (uri.contains("/settlement") || uri.contains("/finance") || uri.contains("/deposit")) return "财务管理";
        if (uri.contains("/risk") || uri.contains("/audit")) return "风险管理";
        if (uri.contains("/customer")) return "客户管理";
        if (uri.contains("/auth") || uri.contains("/user") || uri.contains("/role") || uri.contains("/menu")) return "系统管理";
        if (uri.contains("/ai") || uri.contains("/ocr")) return "AI服务";
        if (uri.contains("/c-mall")) return "C端商城";
        if (uri.contains("/log")) return "日志管理";
        return "其他";
    }

    private String extractOperation(String method, String uri) {
        if ("POST".equals(method)) {
            if (uri.contains("/login")) return "登录";
            if (uri.contains("/pay")) return "缴纳";
            if (uri.contains("/refund")) return "退款";
            if (uri.contains("/deduct")) return "扣除";
            if (uri.contains("/approve") || uri.contains("/audit")) return "审核";
            return "新增";
        }
        if ("PUT".equals(method)) {
            if (uri.contains("/approve")) return "审批通过";
            if (uri.contains("/reject")) return "审批驳回";
            return "修改";
        }
        if ("DELETE".equals(method)) return "删除";
        if ("GET".equals(method)) return "查询";
        return "其他";
    }

    private String extractLogType(String uri) {
        if (uri.contains("/auth")) return "AUTH";
        if (uri.contains("/merchant")) return "MERCHANT";
        if (uri.contains("/product")) return "PRODUCT";
        if (uri.contains("/order")) return "ORDER";
        if (uri.contains("/settlement") || uri.contains("/finance") || uri.contains("/deposit")) return "FINANCE";
        if (uri.contains("/risk") || uri.contains("/audit")) return "RISK";
        if (uri.contains("/customer")) return "CUSTOMER";
        if (uri.contains("/ai") || uri.contains("/ocr")) return "AI";
        if (uri.contains("/user") || uri.contains("/role") || uri.contains("/menu")) return "SYSTEM";
        return "OTHER";
    }

    private String buildParams(Object[] args) {
        if (args == null || args.length == 0) return "";
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) arg;
                filtered.add("{fileName:" + file.getOriginalFilename() + ", size:" + file.getSize() + "}");
            } else if (arg instanceof HttpServletRequest) {
                filtered.add("[HttpServletRequest]");
            } else {
                filtered.add(arg);
            }
        }
        try {
            return objectMapper.writeValueAsString(filtered);
        } catch (JsonProcessingException e) {
            return filtered.toString();
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...[truncated]";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String serializeResponse(Object result) {
        if (result == null) return "null";
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            // JSON序列化失败（如Hibernate代理对象），降级为toString
            try {
                return result.toString();
            } catch (Exception ex) {
                return result.getClass().getSimpleName();
            }
        }
    }
}