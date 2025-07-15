package com.intrafind.llm.config;

import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.providers.openai.OpenAIClient;
import com.intrafind.llm.providers.anthropic.AnthropicClient;
import com.intrafind.llm.providers.gemini.GeminiClient;
import com.intrafind.llm.providers.mistral.MistralClient;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LLMClientFactoryTest {
    
    @Test
    public void testCreateOpenAIClient() {
        LLMConfig config = new LLMConfig("test-api-key");
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        assertNotNull(client);
        assertTrue(client instanceof OpenAIClient);
        assertEquals(LLMProvider.OPENAI, client.getProvider());
    }
    
    @Test
    public void testCreateAnthropicClient() {
        LLMConfig config = new LLMConfig("test-api-key");
        LLMClient client = LLMClientFactory.create(LLMProvider.ANTHROPIC, config);
        
        assertNotNull(client);
        assertTrue(client instanceof AnthropicClient);
        assertEquals(LLMProvider.ANTHROPIC, client.getProvider());
    }
    
    @Test
    public void testCreateGeminiClient() {
        LLMConfig config = new LLMConfig("test-api-key");
        LLMClient client = LLMClientFactory.create(LLMProvider.GEMINI, config);
        
        assertNotNull(client);
        assertTrue(client instanceof GeminiClient);
        assertEquals(LLMProvider.GEMINI, client.getProvider());
    }
    
    @Test
    public void testCreateMistralClient() {
        LLMConfig config = new LLMConfig("test-api-key");
        LLMClient client = LLMClientFactory.create(LLMProvider.MISTRAL, config);
        
        assertNotNull(client);
        assertTrue(client instanceof MistralClient);
        assertEquals(LLMProvider.MISTRAL, client.getProvider());
    }
    
    @Test
    public void testUnsupportedProvider() {
        LLMConfig config = new LLMConfig("test-api-key");
        
        assertThrows(IllegalArgumentException.class, () -> {
            LLMClientFactory.create(null, config);
        });
    }
}