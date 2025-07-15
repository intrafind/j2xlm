package com.intrafind.llm.integration;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opentest4j.TestAbortedException;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseIntegrationTest {
    
    protected LLMClient client;
    protected LLMProvider provider;
    protected String apiKey;
    
    protected abstract LLMProvider getProvider();
    protected abstract String getApiKeyEnvVar();
    protected abstract String getBaseUrlEnvVar();
    protected abstract String getDefaultModel();
    
    @BeforeEach
    public void setUp() {
        provider = getProvider();
        apiKey = System.getenv(getApiKeyEnvVar());
        
        if (apiKey != null && !apiKey.isEmpty()) {
            LLMConfig config = new LLMConfig(apiKey);
            
            // Check for base URL environment variable
            String baseUrl = System.getenv(getBaseUrlEnvVar());
            if (baseUrl != null && !baseUrl.isEmpty()) {
                config.withBaseUrl(baseUrl);
            }
            
            client = LLMClientFactory.create(provider, config);
        }
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testBasicGeneration() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("What is 2+2? Answer with just the number.");
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().trim().isEmpty());
        assertEquals(provider, response.getProvider());
        assertNotNull(response.getModel());
        
        System.out.println("Provider: " + response.getProvider().getDisplayName());
        System.out.println("Model: " + response.getModel());
        System.out.println("Response: " + response.getContent());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testWithParameters() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Generate a creative story about a robot.")
            .withParameter("temperature", 0.9)
            .withParameter("max_tokens", 100);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().trim().isEmpty());
        assertTrue(response.getContent().length() > 10);
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testWithSpecificModel() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Hello, world!")
            .withModel(getDefaultModel());
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().trim().isEmpty());
        assertEquals(getDefaultModel(), response.getModel());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testHealthCheck() {
        assumeApiKeyPresent();
        
        boolean isHealthy = client.isHealthy();
        assertTrue(isHealthy, "Client should be healthy with valid API key");
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMetadata() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Count to 5");
        LLMResponse response = client.generate(request);
        
        assertNotNull(response.getMetadata());
        assertNotNull(response.getMetadata().get("usage"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testEmptyPrompt() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("");
        
        assertThrows(Exception.class, () -> {
            client.generate(request);
        });
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testLongPrompt() {
        assumeApiKeyPresent();
        
        StringBuilder longPrompt = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longPrompt.append("This is a long prompt to test the limits of the API. ");
        }
        longPrompt.append("Please respond with 'OK'.");
        
        LLMRequest request = new LLMRequest(longPrompt.toString());
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
    }
    
    protected void assumeApiKeyPresent() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException(
                "API key not found in environment variable: " + getApiKeyEnvVar());
        }
    }
    
    protected void tearDown() {
        if (client != null) {
            client.close();
        }
    }
}