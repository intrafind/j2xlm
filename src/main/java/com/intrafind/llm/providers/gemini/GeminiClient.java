package com.intrafind.llm.providers.gemini;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import com.intrafind.llm.exceptions.LLMException;
import com.intrafind.llm.utils.HttpClient;
import com.intrafind.llm.utils.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class GeminiClient implements LLMClient {
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String DEFAULT_MODEL = "gemini-pro";
    
    private final LLMConfig config;
    private final HttpClient httpClient;
    private final String baseUrl;
    
    public GeminiClient(LLMConfig config) {
        this.config = config;
        this.httpClient = new HttpClient();
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
    }
    
    @Override
    public LLMResponse generate(LLMRequest request) {
        try {
            String model = request.getModel() != null ? request.getModel() : DEFAULT_MODEL;
            String url = baseUrl + "/models/" + model + ":generateContent?key=" + config.getApiKey();
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                    Map.of("text", request.getPrompt())
                })
            });
            
            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            if (request.getParameters() != null) {
                for (Map.Entry<String, Object> param : request.getParameters().entrySet()) {
                    generationConfig.put(param.getKey(), param.getValue());
                }
            }
            
            if (request.getStopSequences() != null) {
                generationConfig.put("stopSequences", request.getStopSequences());
            }
            
            if (!generationConfig.isEmpty()) {
                requestBody.put("generationConfig", generationConfig);
            }
            
            String responseJson = httpClient.post(url, headers, requestBody);
            
            Map<String, Object> responseMap = JsonParser.parse(responseJson, Map.class);
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> candidates = (java.util.List<Map<String, Object>>) responseMap.get("candidates");
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> usageMetadata = (Map<String, Object>) responseMap.get("usageMetadata");
            
            LLMResponse response = new LLMResponse(text, model, LLMProvider.GEMINI);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("usage", usageMetadata);
            response.setMetadata(metadata);
            
            return response;
            
        } catch (com.intrafind.llm.exceptions.AuthenticationException e) {
            throw e;
        } catch (com.intrafind.llm.exceptions.RateLimitException e) {
            throw e;
        } catch (com.intrafind.llm.exceptions.LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("Gemini API call failed", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            String url = baseUrl + "/models?key=" + config.getApiKey();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            httpClient.post(url, headers, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public LLMProvider getProvider() {
        return LLMProvider.GEMINI;
    }
    
    @Override
    public void close() {
        httpClient.close();
    }
}