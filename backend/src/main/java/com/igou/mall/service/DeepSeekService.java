package com.igou.mall.service;

import com.igou.mall.dao.SystemConfigMapper;
import com.igou.mall.model.entity.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);

    @Autowired
    private SystemConfigMapper configMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";

    public boolean isEnabled() {
        SystemConfig config = configMapper.findByKey("deepseek.enabled");
        return config != null && "true".equals(config.getConfigValue());
    }

    public String getApiKey() {
        SystemConfig config = configMapper.findByKey("deepseek.api_key");
        return config != null ? config.getConfigValue() : "";
    }

    public String chat(String systemPrompt, String userMessage) {
        if (!isEnabled()) {
            log.warn("DeepSeek is not enabled");
            return null;
        }
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DeepSeek API key is not configured");
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", MODEL);
            body.put("temperature", 0.3);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new LinkedHashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            Map<String, String> userMsg = new LinkedHashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            body.put("messages", messages);

            log.info("Calling DeepSeek API with model={}, msgLen={}", MODEL, userMessage.length());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    log.info("DeepSeek API response received, length={}", content != null ? content.length() : 0);
                    return content;
                }
            }
            log.warn("DeepSeek API returned empty response: {}", response.getBody());
        } catch (Exception e) {
            log.error("DeepSeek API call failed: {}", e.getMessage(), e);
        }
        return null;
    }
}
