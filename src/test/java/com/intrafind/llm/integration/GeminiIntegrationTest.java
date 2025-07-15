package com.intrafind.llm.integration;

import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiIntegrationTest extends BaseIntegrationTest {
    
    @Override
    protected LLMProvider getProvider() {
        return LLMProvider.GEMINI;
    }
    
    @Override
    protected String getApiKeyEnvVar() {
        return "GEMINI_API_KEY";
    }
    
    @Override
    protected String getBaseUrlEnvVar() {
        return "GEMINI_BASE_URL";
    }
    
    @Override
    protected String getDefaultModel() {
        return "gemini-pro";
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testGeminiProModel() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("What is the capital of Japan?")
            .withModel("gemini-pro");
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertTrue(response.getContent().toLowerCase().contains("tokyo"));
        assertEquals("gemini-pro", response.getModel());
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
    public void testMaxOutputTokensParameter() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Write a long story about a space adventure")
            .withParameter("maxOutputTokens", 50);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // Response should be limited by max tokens
        assertTrue(response.getContent().length() < 500); // rough estimate
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testTopPParameter() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Generate a creative sentence about mountains")
            .withParameter("topP", 0.1);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().toLowerCase().contains("mountain"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testTopKParameter() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Complete this sentence: The weather today is")
            .withParameter("topK", 10);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().length() > 10);
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testStopSequences() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Count from 1 to 10: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10")
            .withStopSequences(Arrays.asList("5"));
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // Should stop at or before "5"
        assertFalse(response.getContent().contains("6"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMultipleRequests() {
        assumeApiKeyPresent();
        
        for (int i = 0; i < 3; i++) {
            LLMRequest request = new LLMRequest("What is " + (i * 5) + " + " + (i * 3) + "?");
            LLMResponse response = client.generate(request);
            
            assertNotNull(response);
            assertNotNull(response.getContent());
            assertTrue(response.getContent().contains(String.valueOf((i * 5) + (i * 3))));
        }
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testUsageMetadata() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Hello, how are you today?");
        LLMResponse response = client.generate(request);
        
        assertNotNull(response.getMetadata());
        assertNotNull(response.getMetadata().get("usage"));
        
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> usage = (java.util.Map<String, Object>) response.getMetadata().get("usage");
        
        assertNotNull(usage.get("promptTokenCount"));
        assertNotNull(usage.get("candidatesTokenCount"));
        assertNotNull(usage.get("totalTokenCount"));
        
        assertTrue((Integer) usage.get("promptTokenCount") > 0);
        assertTrue((Integer) usage.get("candidatesTokenCount") > 0);
        assertTrue((Integer) usage.get("totalTokenCount") > 0);
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testComplexPrompt() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Explain quantum computing in simple terms for a 10-year-old")
            .withParameter("temperature", 0.7);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().length() > 50);
        
        // Should contain quantum-related terms
        String content = response.getContent().toLowerCase();
        assertTrue(content.contains("quantum") || 
                  content.contains("computer") || 
                  content.contains("bit"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMathematicalReasoning() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("If I have 25 books and I read 8, then buy 12 more, how many books do I have?");
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().contains("29"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testCreativeWriting() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Write a short poem about artificial intelligence")
            .withParameter("temperature", 0.9);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().length() > 20);
        
        // Should contain AI-related terms
        String content = response.getContent().toLowerCase();
        assertTrue(content.contains("ai") || 
                  content.contains("artificial") || 
                  content.contains("intelligence") ||
                  content.contains("machine") ||
                  content.contains("digital"));
    }
}