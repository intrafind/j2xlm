package com.intrafind.llm.core;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LLMRequestTest {
    
    @Test
    public void testBasicRequest() {
        LLMRequest request = new LLMRequest("Hello, world!");
        
        assertEquals("Hello, world!", request.getPrompt());
        assertNotNull(request.getParameters());
        assertTrue(request.getParameters().isEmpty());
        assertNull(request.getModel());
        assertNull(request.getStopSequences());
    }
    
    @Test
    public void testWithParameters() {
        LLMRequest request = new LLMRequest("Test prompt")
            .withParameter("temperature", 0.7)
            .withParameter("max_tokens", 100);
        
        assertEquals(0.7, request.getParameters().get("temperature"));
        assertEquals(100, request.getParameters().get("max_tokens"));
    }
    
    @Test
    public void testWithModel() {
        LLMRequest request = new LLMRequest("Test prompt")
            .withModel("gpt-4");
        
        assertEquals("gpt-4", request.getModel());
    }
    
    @Test
    public void testWithStopSequences() {
        List<String> stopSequences = Arrays.asList("STOP", "END");
        LLMRequest request = new LLMRequest("Test prompt")
            .withStopSequences(stopSequences);
        
        assertEquals(stopSequences, request.getStopSequences());
    }
    
    @Test
    public void testFluentInterface() {
        List<String> stopSequences = Arrays.asList("STOP");
        LLMRequest request = new LLMRequest("Test prompt")
            .withModel("gpt-4")
            .withParameter("temperature", 0.5)
            .withStopSequences(stopSequences);
        
        assertEquals("Test prompt", request.getPrompt());
        assertEquals("gpt-4", request.getModel());
        assertEquals(0.5, request.getParameters().get("temperature"));
        assertEquals(stopSequences, request.getStopSequences());
    }
}