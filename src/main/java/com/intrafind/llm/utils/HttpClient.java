package com.intrafind.llm.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intrafind.llm.exceptions.AuthenticationException;
import com.intrafind.llm.exceptions.LLMException;
import com.intrafind.llm.exceptions.RateLimitException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.util.Map;

public class HttpClient {
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public HttpClient() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    public String post(String url, Map<String, String> headers, Object body) {
        try {
            HttpPost request = new HttpPost(url);
            
            // Add headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }
            
            // Add body
            if (body != null) {
                String jsonBody = objectMapper.writeValueAsString(body);
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            }
            
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                
                if (statusCode == 401) {
                    throw new AuthenticationException("Authentication failed: " + responseBody);
                } else if (statusCode == 429) {
                    throw new RateLimitException("Rate limit exceeded: " + responseBody);
                } else if (statusCode >= 400) {
                    throw new LLMException("HTTP error " + statusCode + ": " + responseBody);
                }
                
                return responseBody;
            });
            
        } catch (IOException e) {
            throw new LLMException("HTTP request failed", e);
        }
    }
    
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            // Log or handle close exception
        }
    }
}