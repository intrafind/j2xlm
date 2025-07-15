package com.intrafind.llm.integration;

import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AnthropicIntegrationTest extends BaseIntegrationTest {
    
    @Override
    protected LLMProvider getProvider() {
        return LLMProvider.ANTHROPIC;
    }
    
    @Override
    protected String getApiKeyEnvVar() {
        return "ANTHROPIC_API_KEY";
    }
    
    @Override
    protected String getBaseUrlEnvVar() {
        return "ANTHROPIC_BASE_URL";
    }
    
    @Override
    protected String getDefaultModel() {
        return "claude-3-haiku-20240307";
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testClaudeSonnetModel() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("What is the capital of France?")
            .withModel("claude-3-5-sonnet-20241022");
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertTrue(response.getContent().toLowerCase().contains("paris"));
        assertEquals("claude-3-5-sonnet-20241022", response.getModel());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMaxTokensParameter() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Write a story about a cat")
            .withParameter("max_tokens", 50);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // Response should be limited by max_tokens
        assertTrue(response.getContent().length() < 500); // rough estimate
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testTemperatureParameter() {
        assumeApiKeyPresent();
        
        // Test with low temperature (more deterministic)
        LLMRequest lowTempRequest = new LLMRequest("Say 'Hello World' exactly")
            .withParameter("temperature", 0.0);
        
        LLMResponse lowTempResponse = client.generate(lowTempRequest);
        
        // Test with high temperature (more creative)
        LLMRequest highTempRequest = new LLMRequest("Say 'Hello World' exactly")
            .withParameter("temperature", 1.0);
        
        LLMResponse highTempResponse = client.generate(highTempRequest);
        
        assertNotNull(lowTempResponse.getContent());
        assertNotNull(highTempResponse.getContent());
        
        // Both should contain some form of "Hello World"
        assertTrue(lowTempResponse.getContent().toLowerCase().contains("hello"));
        assertTrue(highTempResponse.getContent().toLowerCase().contains("hello"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testStopSequences() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Count: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10")
            .withStopSequences(Arrays.asList("5"));
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // Should stop at or before "5"
        assertFalse(response.getContent().contains("6"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testTopPParameter() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Generate a creative sentence about the ocean")
            .withParameter("top_p", 0.1);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().toLowerCase().contains("ocean"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMultipleRequests() {
        assumeApiKeyPresent();
        
        for (int i = 0; i < 3; i++) {
            LLMRequest request = new LLMRequest("What is " + (i * 2) + " + " + (i * 3) + "?");
            LLMResponse response = client.generate(request);
            
            assertNotNull(response);
            assertNotNull(response.getContent());
            assertTrue(response.getContent().contains(String.valueOf((i * 2) + (i * 3))));
        }
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testUsageMetadata() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Hello, how are you?");
        LLMResponse response = client.generate(request);
        
        assertNotNull(response.getMetadata());
        assertNotNull(response.getMetadata().get("usage"));
        
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> usage = (java.util.Map<String, Object>) response.getMetadata().get("usage");
        
        assertNotNull(usage.get("input_tokens"));
        assertNotNull(usage.get("output_tokens"));
        
        assertTrue((Integer) usage.get("input_tokens") > 0);
        assertTrue((Integer) usage.get("output_tokens") > 0);
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testLongConversation() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Tell me a joke about programming")
            .withParameter("max_tokens", 200);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().length() > 10);
        
        // Should contain programming-related terms
        String content = response.getContent().toLowerCase();
        assertTrue(content.contains("programming") || 
                  content.contains("code") || 
                  content.contains("developer") ||
                  content.contains("bug") ||
                  content.contains("computer"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMathematicalReasoning() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("If I have 15 apples and I eat 3, then buy 7 more, how many apples do I have?");
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().contains("19"));
    }
}