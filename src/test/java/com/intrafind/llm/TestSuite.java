package com.intrafind.llm;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import com.intrafind.llm.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opentest4j.TestAbortedException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class TestSuite {
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testAllProvidersBasicFunctionality() {
        LLMProvider[] providers = {
            LLMProvider.OPENAI,
            LLMProvider.ANTHROPIC,
            LLMProvider.GEMINI,
            LLMProvider.MISTRAL
        };
        
        for (LLMProvider provider : providers) {
            if (!TestUtils.hasApiKey(provider)) {
                System.out.println("Skipping " + provider + " - no API key");
                continue;
            }
            
            System.out.println("Testing " + provider.getDisplayName());
            
            LLMClient client = TestUtils.createClient(provider);
            
            try {
                // Test basic functionality
                LLMRequest request = new LLMRequest("What is 2+2? Answer with just the number.");
                
                TestUtils.TimedResult<LLMResponse> timedResult = TestUtils.measureExecutionTime(() -> 
                    client.generate(request));
                
                LLMResponse response = timedResult.getResult();
                Duration duration = timedResult.getDuration();
                
                TestUtils.assertResponseValid(response);
                TestUtils.assertResponseTime(duration, 30);
                
                assertTrue(response.getContent().contains("4"));
                
                TestUtils.printTestResults("Basic Test", provider, response, duration);
                
                // Test health check
                assertTrue(client.isHealthy(), provider + " should be healthy");
                
            } finally {
                client.close();
            }
        }
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testProviderSpecificModels() {
        // Test OpenAI models
        if (TestUtils.hasApiKey(LLMProvider.OPENAI)) {
            testOpenAIModels();
        }
        
        // Test Anthropic models
        if (TestUtils.hasApiKey(LLMProvider.ANTHROPIC)) {
            testAnthropicModels();
        }
        
        // Test Gemini models
        if (TestUtils.hasApiKey(LLMProvider.GEMINI)) {
            testGeminiModels();
        }
        
        // Test Mistral models
        if (TestUtils.hasApiKey(LLMProvider.MISTRAL)) {
            testMistralModels();
        }
    }
    
    private void testOpenAIModels() {
        String[] models = {"gpt-3.5-turbo", "gpt-4"};
        
        for (String model : models) {
            LLMClient client = TestUtils.createClient(LLMProvider.OPENAI);
            
            try {
                LLMRequest request = new LLMRequest("Hello, world!")
                    .withModel(model);
                
                LLMResponse response = client.generate(request);
                
                TestUtils.assertResponseValid(response);
                assertEquals(model, response.getModel());
                
                System.out.println("OpenAI " + model + " test passed");
                
            } catch (Exception e) {
                System.out.println("OpenAI " + model + " test failed (might not have access): " + e.getMessage());
            } finally {
                client.close();
            }
        }
    }
    
    private void testAnthropicModels() {
        String[] models = {"claude-3-haiku-20240307", "claude-3-5-sonnet-20241022"};
        
        for (String model : models) {
            LLMClient client = TestUtils.createClient(LLMProvider.ANTHROPIC);
            
            try {
                LLMRequest request = new LLMRequest("Hello, world!")
                    .withModel(model);
                
                LLMResponse response = client.generate(request);
                
                TestUtils.assertResponseValid(response);
                assertEquals(model, response.getModel());
                
                System.out.println("Anthropic " + model + " test passed");
                
            } catch (Exception e) {
                System.out.println("Anthropic " + model + " test failed (might not have access): " + e.getMessage());
            } finally {
                client.close();
            }
        }
    }
    
    private void testGeminiModels() {
        String[] models = {"gemini-pro"};
        
        for (String model : models) {
            LLMClient client = TestUtils.createClient(LLMProvider.GEMINI);
            
            try {
                LLMRequest request = new LLMRequest("Hello, world!")
                    .withModel(model);
                
                LLMResponse response = client.generate(request);
                
                TestUtils.assertResponseValid(response);
                assertEquals(model, response.getModel());
                
                System.out.println("Gemini " + model + " test passed");
                
            } catch (Exception e) {
                System.out.println("Gemini " + model + " test failed (might not have access): " + e.getMessage());
            } finally {
                client.close();
            }
        }
    }
    
    private void testMistralModels() {
        String[] models = {"mistral-tiny", "mistral-small", "mistral-medium"};
        
        for (String model : models) {
            LLMClient client = TestUtils.createClient(LLMProvider.MISTRAL);
            
            try {
                LLMRequest request = new LLMRequest("Hello, world!")
                    .withModel(model);
                
                LLMResponse response = client.generate(request);
                
                TestUtils.assertResponseValid(response);
                assertEquals(model, response.getModel());
                
                System.out.println("Mistral " + model + " test passed");
                
            } catch (Exception e) {
                System.out.println("Mistral " + model + " test failed (might not have access): " + e.getMessage());
            } finally {
                client.close();
            }
        }
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testParameterVariations() {
        if (!TestUtils.hasApiKey(LLMProvider.OPENAI)) {
            throw new TestAbortedException("OpenAI API key not found");
        }
        
        LLMClient client = TestUtils.createClient(LLMProvider.OPENAI);
        
        try {
            // Test different temperatures
            double[] temperatures = {0.0, 0.5, 1.0};
            
            for (double temp : temperatures) {
                LLMRequest request = new LLMRequest("Generate a creative sentence about cats")
                    .withParameter("temperature", temp)
                    .withParameter("max_tokens", 50);
                
                LLMResponse response = client.generate(request);
                
                TestUtils.assertResponseValid(response);
                TestUtils.assertResponseContains(response, "cat");
                
                System.out.println("Temperature " + temp + " test passed");
            }
            
            // Test different max_tokens
            int[] maxTokens = {10, 50, 100};
            
            for (int tokens : maxTokens) {
                LLMRequest request = new LLMRequest("Write a story about a robot")
                    .withParameter("max_tokens", tokens);
                
                LLMResponse response = client.generate(request);
                
                TestUtils.assertResponseValid(response);
                
                System.out.println("Max tokens " + tokens + " test passed");
            }
            
        } finally {
            client.close();
        }
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void runFullTestSuite() {
        System.out.println("=== J2XLM Full Test Suite ===");
        
        // Test all providers
        testAllProvidersBasicFunctionality();
        
        // Test specific models
        testProviderSpecificModels();
        
        // Test parameters
        testParameterVariations();
        
        System.out.println("=== Test Suite Complete ===");
    }
}