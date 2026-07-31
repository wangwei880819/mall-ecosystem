package com.igou.mall.service;

import com.igou.mall.dao.SystemConfigMapper;
import com.igou.mall.model.entity.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekService {

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
        if (!isEnabled()) return null;
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) return null;

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

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
