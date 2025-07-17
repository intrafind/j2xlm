package com.intrafind.llm.integration;

import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAIIntegrationTest extends BaseIntegrationTest {
    
    @Override
    protected LLMProvider getProvider() {
        return LLMProvider.OPENAI;
    }
    
    @Override
    protected String getApiKeyEnvVar() {
        return "OPENAI_API_KEY";
    }
    
    @Override
    protected String getBaseUrlEnvVar() {
        return "OPENAI_BASE_URL";
    }
    
    @Override
    protected String getDefaultModel() {
        return "gpt-3.5-turbo";
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testGPT4Model() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("What is the capital of France?")
            .withModel("gpt-4");
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertTrue(response.getContent().toLowerCase().contains("paris"));
        assertEquals("gpt-4", response.getModel());
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
        
        LLMRequest request = new LLMRequest("Write a long story about a dragon")
            .withParameter("max_tokens", 50);
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // Response should be limited by max_tokens
        assertTrue(response.getContent().length() < 500); // rough estimate
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
    public void testSystemMessage() {
        assumeApiKeyPresent();
        
        LLMRequest request = new LLMRequest("What is your name?")
            .withParameter("system_message", "You are a helpful assistant named Bob.");
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // OpenAI should respond as Bob or mention the name
        assertTrue(response.getContent().toLowerCase().contains("bob") || 
                  response.getContent().toLowerCase().contains("assistant"));
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMultipleRequests() {
        assumeApiKeyPresent();
        
        for (int i = 0; i < 3; i++) {
            LLMRequest request = new LLMRequest("What is " + (i + 1) + " + " + (i + 2) + "?");
            LLMResponse response = client.generate(request);
            
            assertNotNull(response);
            assertNotNull(response.getContent());
            assertTrue(response.getContent().contains(String.valueOf((i + 1) + (i + 2))));
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
    public void testImageDescription() throws IOException {
        assumeApiKeyPresent();

        LLMRequest request = new LLMRequest("Describe the content of this image comprehensively.")
            .withImage("image/jpeg",
                Files.readAllBytes(Path.of("src/test/resources/city.jpg")));

        LLMResponse response = client.generate(request);

        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
    }
}