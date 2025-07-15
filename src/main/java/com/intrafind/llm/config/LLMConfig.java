package com.intrafind.llm.config;

import java.util.HashMap;
import java.util.Map;

public class LLMConfig {
    private String apiKey;
    private String baseUrl;
    private int timeout;
    private Map<String, String> headers;
    
    public LLMConfig(String apiKey) {
        this.apiKey = apiKey;
        this.timeout = 30000; // 30 seconds default
        this.headers = new HashMap<>();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public LLMConfig withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
    
    public LLMConfig withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
    
    public LLMConfig withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
}