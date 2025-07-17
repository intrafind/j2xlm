package com.intrafind.llm.providers.openai;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMRequest.ImageDTO;
import com.intrafind.llm.core.LLMResponse;
import com.intrafind.llm.exceptions.LLMException;
import com.intrafind.llm.utils.HttpClient;
import com.intrafind.llm.utils.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OpenAIClient implements LLMClient {
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    
    private final LLMConfig config;
    private final HttpClient httpClient;
    private final String baseUrl;
    
    public OpenAIClient(LLMConfig config) {
        this.config = config;
        this.httpClient = new HttpClient();
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
    }
    
    @Override
    public LLMResponse generate(LLMRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            
            // Check if this is Azure OpenAI based on baseUrl
            if (baseUrl.contains("azure.com")) {
                headers.put("api-key", config.getApiKey());
            } else {
                headers.put("Authorization", "Bearer " + config.getApiKey());
            }
            headers.put("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel() != null ? request.getModel() : DEFAULT_MODEL);
            List<Map<String, ?>> prompt = new ArrayList<>();
            prompt.add(Map.of("type", "text", "text", request.getPrompt()));
            Optional.ofNullable(request.getImage())
                .map(ImageDTO::asDataUrl)
                .ifPresent(imageURL -> prompt.add(Map.of("type", "image_url", "image_url", Map.of("url", imageURL))));
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
            });
            
            // Add parameters
            if (request.getParameters() != null) {
                for (Map.Entry<String, Object> param : request.getParameters().entrySet()) {
                    requestBody.put(param.getKey(), param.getValue());
                }
            }
            
            if (request.getStopSequences() != null) {
                requestBody.put("stop", request.getStopSequences());
            }
            
            // For Azure OpenAI, the baseUrl already includes the full path
            String endpoint = baseUrl.contains("azure.com") ? baseUrl : baseUrl + "/chat/completions";
            String responseJson = httpClient.post(endpoint, headers, requestBody);
            
            Map<String, Object> responseMap = JsonParser.parse(responseJson, Map.class);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> firstChoice = ((java.util.List<Map<String, Object>>) responseMap.get("choices")).get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            
            LLMResponse response = new LLMResponse(content, (String) responseMap.get("model"), LLMProvider.OPENAI);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("usage", usage);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (com.intrafind.llm.exceptions.AuthenticationException e) {
            throw e;
        } catch (com.intrafind.llm.exceptions.RateLimitException e) {
            throw e;
        } catch (com.intrafind.llm.exceptions.LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("OpenAI API call failed", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            Map<String, String> headers = new HashMap<>();
            
            // Check if this is Azure OpenAI based on baseUrl
            if (baseUrl.contains("azure.com")) {
                headers.put("api-key", config.getApiKey());
                // For Azure OpenAI, we'll use a simple chat completion as health check
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("messages", new Object[]{
                    Map.of("role", "user", "content", "Hello")
                });
                requestBody.put("max_tokens", 1);
                httpClient.post(baseUrl, headers, requestBody);
            } else {
                headers.put("Authorization", "Bearer " + config.getApiKey());
                httpClient.post(baseUrl + "/models", headers, null);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public LLMProvider getProvider() {
        return LLMProvider.OPENAI;
    }
    
    @Override
    public void close() {
        httpClient.close();
    }
}