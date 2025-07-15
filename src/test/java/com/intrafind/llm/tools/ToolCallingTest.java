package com.intrafind.llm.tools;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opentest4j.TestAbortedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ToolCallingTest {
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testOpenAIToolCalling() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        // Create a simple weather tool
        Map<String, Object> weatherParams = new HashMap<>();
        weatherParams.put("type", "object");
        weatherParams.put("properties", Map.of(
            "location", Map.of("type", "string", "description", "The city and state"),
            "unit", Map.of("type", "string", "enum", Arrays.asList("celsius", "fahrenheit"))
        ));
        weatherParams.put("required", Arrays.asList("location"));
        
        Tool weatherTool = new Tool("get_weather", "Get the current weather in a location", weatherParams);
        
        LLMRequest request = new LLMRequest("What's the weather like in San Francisco?")
            .withModel("gpt-3.5-turbo")
            .withTools(Arrays.asList(weatherTool));
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        // Note: OpenAI might not call the tool in every test run, but we can check the structure
        System.out.println("OpenAI Tool Calling Response: " + response.getContent());
        
        if (response.getToolCalls() != null) {
            assertFalse(response.getToolCalls().isEmpty());
            ToolCall toolCall = response.getToolCalls().get(0);
            assertEquals("get_weather", toolCall.getName());
            assertNotNull(toolCall.getArguments());
            assertTrue(toolCall.getArguments().containsKey("location"));
        }
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testOpenAIFunctionCalling() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        // Create a calculator tool
        Map<String, Object> calcParams = new HashMap<>();
        calcParams.put("type", "object");
        calcParams.put("properties", Map.of(
            "operation", Map.of("type", "string", "enum", Arrays.asList("add", "subtract", "multiply", "divide")),
            "a", Map.of("type", "number", "description", "First number"),
            "b", Map.of("type", "number", "description", "Second number")
        ));
        calcParams.put("required", Arrays.asList("operation", "a", "b"));
        
        Tool calcTool = new Tool("calculator", "Perform basic math operations", calcParams);
        
        LLMRequest request = new LLMRequest("Calculate 15 + 25")
            .withModel("gpt-3.5-turbo")
            .withTools(Arrays.asList(calcTool));
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        System.out.println("OpenAI Calculator Response: " + response.getContent());
        
        if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
            ToolCall toolCall = response.getToolCalls().get(0);
            assertEquals("calculator", toolCall.getName());
            assertNotNull(toolCall.getArguments());
            assertTrue(toolCall.getArguments().containsKey("operation"));
            assertTrue(toolCall.getArguments().containsKey("a"));
            assertTrue(toolCall.getArguments().containsKey("b"));
        }
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testAnthropicToolCalling() {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("ANTHROPIC_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.ANTHROPIC, config);
        
        // Create a simple time tool
        Map<String, Object> timeParams = new HashMap<>();
        timeParams.put("type", "object");
        timeParams.put("properties", Map.of(
            "timezone", Map.of("type", "string", "description", "The timezone to get time for")
        ));
        timeParams.put("required", Arrays.asList("timezone"));
        
        Tool timeTool = new Tool("get_time", "Get the current time in a timezone", timeParams);
        
        LLMRequest request = new LLMRequest("What time is it in New York?")
            .withModel("claude-3-5-sonnet-20241022")
            .withTools(Arrays.asList(timeTool));
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        System.out.println("Anthropic Tool Calling Response: " + response.getContent());
        
        // Note: Anthropic's tool calling might behave differently
        if (response.getToolCalls() != null) {
            assertFalse(response.getToolCalls().isEmpty());
            ToolCall toolCall = response.getToolCalls().get(0);
            assertEquals("get_time", toolCall.getName());
            assertNotNull(toolCall.getArguments());
        }
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMultipleTools() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        // Create multiple tools
        Map<String, Object> weatherParams = new HashMap<>();
        weatherParams.put("type", "object");
        weatherParams.put("properties", Map.of(
            "location", Map.of("type", "string", "description", "The city and state")
        ));
        weatherParams.put("required", Arrays.asList("location"));
        
        Map<String, Object> calcParams = new HashMap<>();
        calcParams.put("type", "object");
        calcParams.put("properties", Map.of(
            "expression", Map.of("type", "string", "description", "Mathematical expression to evaluate")
        ));
        calcParams.put("required", Arrays.asList("expression"));
        
        Tool weatherTool = new Tool("get_weather", "Get weather information", weatherParams);
        Tool calcTool = new Tool("calculate", "Perform calculations", calcParams);
        
        LLMRequest request = new LLMRequest("What's the weather in Boston and what's 12 * 8?")
            .withModel("gpt-3.5-turbo")
            .withTools(Arrays.asList(weatherTool, calcTool));
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        System.out.println("Multiple Tools Response: " + response.getContent());
        
        // The model might call multiple tools or describe what it would do
        if (response.getToolCalls() != null) {
            assertTrue(response.getToolCalls().size() <= 2);
        }
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testToolCallingWithoutTools() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("What's the weather like in Paris?")
            .withModel("gpt-3.5-turbo");
        
        LLMResponse response = client.generate(request);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        // Without tools, should not have tool calls
        assertTrue(response.getToolCalls() == null || response.getToolCalls().isEmpty());
        
        client.close();
    }
    
    @Test
    public void testToolStructure() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        params.put("properties", Map.of("location", Map.of("type", "string")));
        
        Tool tool = new Tool("get_weather", "Get weather information", params);
        
        assertEquals("get_weather", tool.getName());
        assertEquals("Get weather information", tool.getDescription());
        assertEquals(params, tool.getParameters());
    }
    
    @Test
    public void testToolCallStructure() {
        Map<String, Object> args = new HashMap<>();
        args.put("location", "New York");
        args.put("unit", "celsius");
        
        ToolCall toolCall = new ToolCall("call_123", "get_weather", args);
        
        assertEquals("call_123", toolCall.getId());
        assertEquals("get_weather", toolCall.getName());
        assertEquals(args, toolCall.getArguments());
        assertEquals("New York", toolCall.getArguments().get("location"));
        assertEquals("celsius", toolCall.getArguments().get("unit"));
    }
}