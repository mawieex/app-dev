package com.appdev.MindFlow.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import org.springframework.core.ParameterizedTypeReference;

@RestController
@RequestMapping("/api")
public class MoodSuggestionController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @PostMapping("/mood-suggest")
    public Map<String, String> suggestMood(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        String mood = callGeminiForMood(text);
        Map<String, String> response = new HashMap<>();
        response.put("mood", mood);
        return response;
    }

    private String callGeminiForMood(String text) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", "Given this journal entry, suggest the most appropriate mood (Happy, Calm, Neutral, Anxious, Sad) as a single word. Only return the mood word. Entry: " + text);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(part));
        contents.add(content);
        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<String, Object> partMap = parts.get(0);
                        String mood = ((String) partMap.get("text")).trim();
                        // Only return the first word, capitalized
                        return mood.split("\\s+")[0].substring(0, 1).toUpperCase() + mood.split("\\s+")[0].substring(1).toLowerCase();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
} 