package com.intrafind.llm.providers.mistral;

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

public class MistralClient implements LLMClient {
    private static final String DEFAULT_BASE_URL = "https://api.mistral.ai/v1";
    private static final String DEFAULT_MODEL = "mistral-tiny";
    
    private final LLMConfig config;
    private final HttpClient httpClient;
    private final String baseUrl;
    
    public MistralClient(LLMConfig config) {
        this.config = config;
        this.httpClient = new HttpClient();
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
    }
    
    @Override
    public LLMResponse generate(LLMRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + config.getApiKey());
            headers.put("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel() != null ? request.getModel() : DEFAULT_MODEL);
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", request.getPrompt())
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
            
            String responseJson = httpClient.post(baseUrl + "/chat/completions", headers, requestBody);
            
            Map<String, Object> responseMap = JsonParser.parse(responseJson, Map.class);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> firstChoice = ((java.util.List<Map<String, Object>>) responseMap.get("choices")).get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            
            LLMResponse response = new LLMResponse(content, (String) responseMap.get("model"), LLMProvider.MISTRAL);
            
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
            throw new LLMException("Mistral API call failed", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + config.getApiKey());
            
            httpClient.post(baseUrl + "/models", headers, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public LLMProvider getProvider() {
        return LLMProvider.MISTRAL;
    }
    
    @Override
    public void close() {
        httpClient.close();
    }
}