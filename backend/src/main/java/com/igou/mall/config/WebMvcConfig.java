package com.igou.mall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/merchant/").setViewName("forward:/merchant/index.html");
        registry.addViewController("/risk/").setViewName("forward:/risk/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
        // 托管商户入驻平台和风控平台前端静态文件
        registry.addResourceHandler("/merchant/**")
                .addResourceLocations("file:///Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/deploy/frontend/merchant/");
        registry.addResourceHandler("/risk/**")
                .addResourceLocations("file:///Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/deploy/frontend/risk/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put("png", MediaType.IMAGE_PNG);
        mediaTypes.put("jpg", MediaType.IMAGE_JPEG);
        mediaTypes.put("jpeg", MediaType.IMAGE_JPEG);
        mediaTypes.put("gif", MediaType.IMAGE_GIF);
        configurer.mediaTypes(mediaTypes);
    }
}
