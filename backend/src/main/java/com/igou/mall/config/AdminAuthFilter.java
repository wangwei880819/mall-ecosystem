package com.igou.mall.config;

import com.igou.mall.service.JwtService;
import io.jsonwebtoken.Claims;
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
 * 管理端认证过滤器
 * 保护 /api/ 下的管理类API，放行公开接口（登录、C端、上传等）
 */
@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/api/auth/login",
            "/api/c-mall/",
            "/api/customer/auth/",
            "/api/product/upload",
            "/api/ocr/"
    ));

    private static final Set<String> PUBLIC_METHODS = new HashSet<>(Arrays.asList(
            "OPTIONS"
    ));

    private final JwtService jwtService;

    public AdminAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // OPTIONS 预检请求放行
        if (PUBLIC_METHODS.contains(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 公开路径放行
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 验证 JWT Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "未登录，请先登录");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.extractClaims(token);
            request.setAttribute("authUserId", claims.get("userId", Long.class));
            request.setAttribute("authUsername", claims.getSubject());
            request.setAttribute("authRole", claims.get("role", String.class));
        } catch (Exception e) {
            sendUnauthorized(response, "登录已过期，请重新登录");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 只处理 /api/ 路径，排除 /api/c-mall/ (已有 CustomerAuthFilter) 和 /api/customer/auth/ (C端认证)
        return !path.startsWith("/api/")
                || path.startsWith("/api/c-mall/")
                || path.startsWith("/api/customer/auth/");
    }
}