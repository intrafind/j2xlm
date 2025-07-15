package com.intrafind.llm.providers.anthropic;

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

public class AnthropicClient implements LLMClient {
    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com/v1";
    private static final String DEFAULT_MODEL = "claude-3-haiku-20240307";
    
    private final LLMConfig config;
    private final HttpClient httpClient;
    private final String baseUrl;
    
    public AnthropicClient(LLMConfig config) {
        this.config = config;
        this.httpClient = new HttpClient();
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
    }
    
    @Override
    public LLMResponse generate(LLMRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("x-api-key", config.getApiKey());
            headers.put("Content-Type", "application/json");
            headers.put("anthropic-version", "2023-06-01");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel() != null ? request.getModel() : DEFAULT_MODEL);
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", request.getPrompt())
            });
            requestBody.put("max_tokens", request.getParameters() != null ? 
                request.getParameters().getOrDefault("max_tokens", 1000) : 1000);
            
            // Add parameters
            if (request.getParameters() != null) {
                for (Map.Entry<String, Object> param : request.getParameters().entrySet()) {
                    if (!param.getKey().equals("max_tokens")) {
                        requestBody.put(param.getKey(), param.getValue());
                    }
                }
            }
            
            if (request.getStopSequences() != null) {
                requestBody.put("stop_sequences", request.getStopSequences());
            }
            
            String responseJson = httpClient.post(baseUrl + "/messages", headers, requestBody);
            
            Map<String, Object> responseMap = JsonParser.parse(responseJson, Map.class);
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> content = (java.util.List<Map<String, Object>>) responseMap.get("content");
            String text = (String) content.get(0).get("text");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            
            LLMResponse response = new LLMResponse(text, (String) responseMap.get("model"), LLMProvider.ANTHROPIC);
            
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
            throw new LLMException("Anthropic API call failed", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("x-api-key", config.getApiKey());
            headers.put("anthropic-version", "2023-06-01");
            
            Map<String, Object> testRequest = new HashMap<>();
            testRequest.put("model", DEFAULT_MODEL);
            testRequest.put("messages", new Object[]{
                Map.of("role", "user", "content", "Hello")
            });
            testRequest.put("max_tokens", 10);
            
            httpClient.post(baseUrl + "/messages", headers, testRequest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public LLMProvider getProvider() {
        return LLMProvider.ANTHROPIC;
    }
    
    @Override
    public void close() {
        httpClient.close();
    }
}