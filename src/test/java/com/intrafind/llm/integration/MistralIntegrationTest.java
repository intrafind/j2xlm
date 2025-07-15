package com.intrafind.llm.integration;

import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MistralIntegrationTest extends BaseIntegrationTest {
    
    @Override
    protected LLMProvider getProvider() {
        return LLMProvider.MISTRAL;
    }
    
    @Override
    protected String getApiKeyEnvVar() {
        return "MISTRAL_API_KEY";
    }
    
    @Override
    protected String getBaseUrlEnvVar() {
        return "MISTRAL_BASE_URL";
    }
    
    @Override
    protected String getDefaultModel() {
        return "mistral-tiny";
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMistralSmallModel() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("What is the capital of Germany?")
            .withModel("mistral-small");
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertTrue(response.getContent().toLowerCase().contains("berlin"));
        assertEquals("mistral-small", response.getModel());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMistralMediumModel() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("What is the capital of Italy?")
            .withModel("mistral-medium");
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertTrue(response.getContent().toLowerCase().contains("rome"));
        assertEquals("mistral-medium", response.getModel());
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
    public void testMaxTokensParameter() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Write a story about a magical forest")
            .withParameter("max_tokens", 50);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // Response should be limited by max_tokens
        assertTrue(response.getContent().length() < 500); // rough estimate
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testTopPParameter() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Generate a creative sentence about space")
            .withParameter("top_p", 0.1);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().toLowerCase().contains("space"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testStopSequences() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("List numbers: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10")
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
            LLMRequest request = new LLMRequest("What is " + (i * 4) + " + " + (i * 2) + "?");
            LLMResponse response = client.generate(request);
            
            assertNotNull(response);
            assertNotNull(response.getContent());
            assertTrue(response.getContent().contains(String.valueOf((i * 4) + (i * 2))));
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
        
        assertNotNull(usage.get("prompt_tokens"));
        assertNotNull(usage.get("completion_tokens"));
        assertNotNull(usage.get("total_tokens"));
        
        assertTrue((Integer) usage.get("prompt_tokens") > 0);
        assertTrue((Integer) usage.get("completion_tokens") > 0);
        assertTrue((Integer) usage.get("total_tokens") > 0);
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testFrenchLanguage() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Bonjour, comment allez-vous? Répondez en français.")
            .withParameter("temperature", 0.7);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().length() > 10);
        
        // Should contain French words
        String content = response.getContent().toLowerCase();
        assertTrue(content.contains("bonjour") || 
                  content.contains("bien") || 
                  content.contains("merci") ||
                  content.contains("français"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testCodeGeneration() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("Write a simple Python function to calculate factorial")
            .withParameter("temperature", 0.3);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().length() > 20);
        
        // Should contain Python-related terms
        String content = response.getContent().toLowerCase();
        assertTrue(content.contains("def") || 
                  content.contains("function") || 
                  content.contains("factorial") ||
                  content.contains("python"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMathematicalReasoning() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("If I have 30 cookies and I eat 8, then bake 15 more, how many cookies do I have?");
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().contains("37"));
    }
}