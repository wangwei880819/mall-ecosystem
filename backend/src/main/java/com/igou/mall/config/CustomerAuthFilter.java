package com.igou.mall.config;

import com.igou.mall.dao.CustomerMapper;
import com.igou.mall.model.entity.Customer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * C端用户认证过滤器
 * 从 Authorization 请求头提取 Bearer Token，解析用户ID并进行服务端验证
 */
@Component
public class CustomerAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_CUSTOMER_ID = "authCustomerId";

    private static final Set<String> PROTECTED_PATHS = new HashSet<>(Arrays.asList(
            "/api/c-mall/cart",
            "/api/c-mall/address",
            "/api/c-mall/orders",
            "/api/c-mall/auth/user-info",
            "/api/c-mall/evaluations"
    ));

    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/api/c-mall/auth/login",
            "/api/c-mall/auth/register",
            "/api/c-mall/products",
            "/api/c-mall/product-categories",
            "/api/c-mall/banners",
            "/api/c-mall/config",
            "/api/c-mall/checkout"
    ));

    private final CustomerMapper customerMapper;

    public CustomerAuthFilter(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 尝试从 Token 解析用户ID
        Long customerId = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            customerId = parseCustomerIdFromToken(token);
            if (customerId != null) {
                // 验证用户是否存在且状态正常
                Customer customer = customerMapper.findById(customerId);
                if (customer != null && !"DISABLED".equals(customer.getStatus())) {
                    request.setAttribute(ATTR_CUSTOMER_ID, customerId);
                }
            }
        }

        // 检查是否需要认证
        boolean requiresAuth = PROTECTED_PATHS.stream().anyMatch(path::startsWith);
        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(path::startsWith);

        if (requiresAuth && request.getAttribute(ATTR_CUSTOMER_ID) == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期，请重新登录\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从 Token 中解析用户ID
     * Token 格式: CUST_{userId}_{uuid16}
     */
    private Long parseCustomerIdFromToken(String token) {
        try {
            if (token.startsWith("CUST_")) {
                String withoutPrefix = token.substring(5);
                int secondUnderscore = withoutPrefix.indexOf('_');
                if (secondUnderscore > 0) {
                    return Long.parseLong(withoutPrefix.substring(0, secondUnderscore));
                }
            }
            // 兼容B端JWT token中的userId
            if (token.contains(".")) {
                // 尝试Base64解码JWT payload
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    if (payload.contains("\"userId\"")) {
                        String userIdStr = payload.replaceAll(".*\"userId\"\\s*:\\s*(\\d+).*", "$1");
                        if (userIdStr.matches("\\d+")) {
                            return Long.parseLong(userIdStr);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 只处理C端API
        return !path.startsWith("/api/c-mall/") && !path.startsWith("/api/customer/");
    }
}
