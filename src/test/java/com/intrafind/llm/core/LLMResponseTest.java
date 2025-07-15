package com.intrafind.llm.core;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class LLMResponseTest {
    
    @Test
    public void testBasicResponse() {
        LLMResponse response = new LLMResponse("Hello!", "gpt-3.5-turbo", LLMProvider.OPENAI);
        
        assertEquals("Hello!", response.getContent());
        assertEquals("gpt-3.5-turbo", response.getModel());
        assertEquals(LLMProvider.OPENAI, response.getProvider());
        assertEquals(Optional.empty(), response.getFunctionCall());
    }
    
    @Test
    public void testWithMetadata() {
        LLMResponse response = new LLMResponse("Hello!", "gpt-3.5-turbo", LLMProvider.OPENAI);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tokens", 10);
        response.setMetadata(metadata);
        
        assertEquals(metadata, response.getMetadata());
        assertEquals(10, response.getMetadata().get("tokens"));
    }
    
    @Test
    public void testWithFunctionCall() {
        LLMResponse response = new LLMResponse("Hello!", "gpt-3.5-turbo", LLMProvider.OPENAI);
        response.setFunctionCall(Optional.of("function_name"));
        
        assertTrue(response.getFunctionCall().isPresent());
        assertEquals("function_name", response.getFunctionCall().get());
    }
    
    @Test
    public void testAllProviders() {
        LLMResponse openaiResponse = new LLMResponse("OpenAI", "gpt-4", LLMProvider.OPENAI);
        LLMResponse anthropicResponse = new LLMResponse("Anthropic", "claude-3", LLMProvider.ANTHROPIC);
        LLMResponse geminiResponse = new LLMResponse("Gemini", "gemini-pro", LLMProvider.GEMINI);
        LLMResponse mistralResponse = new LLMResponse("Mistral", "mistral-small", LLMProvider.MISTRAL);
        
        assertEquals(LLMProvider.OPENAI, openaiResponse.getProvider());
        assertEquals(LLMProvider.ANTHROPIC, anthropicResponse.getProvider());
        assertEquals(LLMProvider.GEMINI, geminiResponse.getProvider());
        assertEquals(LLMProvider.MISTRAL, mistralResponse.getProvider());
    }
}