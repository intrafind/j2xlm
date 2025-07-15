package com.intrafind.llm.error;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.exceptions.AuthenticationException;
import com.intrafind.llm.exceptions.LLMException;
import com.intrafind.llm.exceptions.RateLimitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opentest4j.TestAbortedException;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorHandlingTest {
    
    @Test
    public void testInvalidApiKey() {
        LLMConfig config = new LLMConfig("invalid-api-key");
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello, world!");
        
        assertThrows(AuthenticationException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testEmptyApiKey() {
        LLMConfig config = new LLMConfig("");
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello, world!");
        
        assertThrows(AuthenticationException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testNullApiKey() {
        LLMConfig config = new LLMConfig(null);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello, world!");
        
        assertThrows(AuthenticationException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testHealthCheckWithInvalidKey() {
        LLMConfig config = new LLMConfig("invalid-key");
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        assertFalse(client.isHealthy());
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testHealthCheckWithValidKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        assertTrue(client.isHealthy());
        
        client.close();
    }
    
    @Test
    public void testInvalidModel() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello, world!")
            .withModel("non-existent-model");
        
        assertThrows(LLMException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testNullRequest() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        assertThrows(Exception.class, () -> {
            client.generate(null);
        });
        
        client.close();
    }
    
    @Test
    public void testEmptyPrompt() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("");
        
        assertThrows(LLMException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testInvalidParameters() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello, world!")
            .withParameter("temperature", 5.0) // Invalid temperature (should be 0-2)
            .withParameter("max_tokens", -100); // Invalid max_tokens
        
        assertThrows(LLMException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testInvalidBaseUrl() {
        LLMConfig config = new LLMConfig("test-key")
            .withBaseUrl("https://invalid-url-that-does-not-exist.com");
        
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello, world!");
        
        assertThrows(LLMException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testTimeout() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey)
            .withTimeout(1); // 1ms timeout - should fail
        
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello, world!");
        
        assertThrows(LLMException.class, () -> {
            client.generate(request);
        });
        
        client.close();
    }
    
    @Test
    public void testAllProvidersWithInvalidKeys() {
        LLMProvider[] providers = {
            LLMProvider.OPENAI,
            LLMProvider.ANTHROPIC,
            LLMProvider.GEMINI,
            LLMProvider.MISTRAL
        };
        
        for (LLMProvider provider : providers) {
            LLMConfig config = new LLMConfig("invalid-key");
            LLMClient client = LLMClientFactory.create(provider, config);
            
            LLMRequest request = new LLMRequest("Test");
            
            assertThrows(Exception.class, () -> {
                client.generate(request);
            }, "Provider " + provider + " should throw exception with invalid key");
            
            assertFalse(client.isHealthy(), "Provider " + provider + " should not be healthy with invalid key");
            
            client.close();
        }
    }
    
    @Test
    public void testExceptionMessages() {
        LLMConfig config = new LLMConfig("sk-invalid");
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("Hello");
        
        try {
            client.generate(request);
            fail("Should have thrown an exception");
        } catch (AuthenticationException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Authentication failed") || 
                      e.getMessage().contains("401"));
        } catch (LLMException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().length() > 0);
        }
        
        client.close();
    }
    
    @Test
    public void testExceptionHierarchy() {
        AuthenticationException authEx = new AuthenticationException("Auth failed");
        assertTrue(authEx instanceof LLMException);
        assertTrue(authEx instanceof RuntimeException);
        
        RateLimitException rateEx = new RateLimitException("Rate limit exceeded");
        assertTrue(rateEx instanceof LLMException);
        assertTrue(rateEx instanceof RuntimeException);
        
        LLMException llmEx = new LLMException("General LLM error");
        assertTrue(llmEx instanceof RuntimeException);
    }
}