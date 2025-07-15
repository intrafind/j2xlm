package com.intrafind.llm.utils;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import org.opentest4j.TestAbortedException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {
    
    private static final Map<LLMProvider, String> ENV_VARS = new HashMap<>();
    
    static {
        ENV_VARS.put(LLMProvider.OPENAI, "OPENAI_API_KEY");
        ENV_VARS.put(LLMProvider.ANTHROPIC, "ANTHROPIC_API_KEY");
        ENV_VARS.put(LLMProvider.GEMINI, "GEMINI_API_KEY");
        ENV_VARS.put(LLMProvider.MISTRAL, "MISTRAL_API_KEY");
    }
    
    public static String getApiKey(LLMProvider provider) {
        return System.getenv(ENV_VARS.get(provider));
    }
    
    public static boolean hasApiKey(LLMProvider provider) {
        String key = getApiKey(provider);
        return key != null && !key.isEmpty();
    }
    
    public static LLMClient createClient(LLMProvider provider) {
        String apiKey = getApiKey(provider);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key not found for provider: " + provider);
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        return LLMClientFactory.create(provider, config);
    }
    
    public static LLMClient createClientWithTimeout(LLMProvider provider, int timeoutMs) {
        String apiKey = getApiKey(provider);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key not found for provider: " + provider);
        }
        
        LLMConfig config = new LLMConfig(apiKey).withTimeout(timeoutMs);
        return LLMClientFactory.create(provider, config);
    }
    
    public static void skipIfNoApiKey(LLMProvider provider) {
        if (!hasApiKey(provider)) {
            throw new TestAbortedException(
                "API key not found for provider: " + provider);
        }
    }
    
    public static void skipIfIntegrationTestsDisabled() {
        String integrationTests = System.getenv("INTEGRATION_TESTS");
        if (!"true".equals(integrationTests)) {
            throw new TestAbortedException(
                "Integration tests disabled. Set INTEGRATION_TESTS=true to enable.");
        }
    }
    
    public static Duration measureExecutionTime(Runnable task) {
        Instant start = Instant.now();
        task.run();
        return Duration.between(start, Instant.now());
    }
    
    public static <T> TimedResult<T> measureExecutionTime(java.util.function.Supplier<T> task) {
        Instant start = Instant.now();
        T result = task.get();
        Duration duration = Duration.between(start, Instant.now());
        return new TimedResult<>(result, duration);
    }
    
    public static class TimedResult<T> {
        private final T result;
        private final Duration duration;
        
        public TimedResult(T result, Duration duration) {
            this.result = result;
            this.duration = duration;
        }
        
        public T getResult() {
            return result;
        }
        
        public Duration getDuration() {
            return duration;
        }
    }
    
    public static void assertResponseValid(LLMResponse response) {
        assert response != null : "Response should not be null";
        assert response.getContent() != null : "Response content should not be null";
        assert !response.getContent().trim().isEmpty() : "Response content should not be empty";
        assert response.getProvider() != null : "Response provider should not be null";
        assert response.getModel() != null : "Response model should not be null";
    }
    
    public static void assertResponseContains(LLMResponse response, String expectedContent) {
        assertResponseValid(response);
        assert response.getContent().toLowerCase().contains(expectedContent.toLowerCase()) : 
            "Response should contain: " + expectedContent + ", but was: " + response.getContent();
    }
    
    public static void assertResponseTime(Duration duration, long maxSeconds) {
        assert duration.toSeconds() <= maxSeconds : 
            "Response took too long: " + duration.toSeconds() + " seconds (max: " + maxSeconds + ")";
    }
    
    public static void printTestResults(String testName, LLMProvider provider, LLMResponse response, Duration duration) {
        System.out.println("=== " + testName + " ===");
        System.out.println("Provider: " + provider.getDisplayName());
        System.out.println("Model: " + response.getModel());
        System.out.println("Response time: " + duration.toMillis() + "ms");
        System.out.println("Response length: " + response.getContent().length() + " chars");
        System.out.println("Response: " + response.getContent());
        
        if (response.getMetadata() != null) {
            System.out.println("Metadata: " + response.getMetadata());
        }
        
        System.out.println("=================");
    }
    
    public static String generateLongPrompt(int wordCount) {
        StringBuilder prompt = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            prompt.append("word").append(i).append(" ");
        }
        prompt.append("Please respond with 'OK'.");
        return prompt.toString();
    }
    
    public static LLMRequest createSimpleRequest(String prompt) {
        return new LLMRequest(prompt);
    }
    
    public static LLMRequest createRequestWithParameters(String prompt, double temperature, int maxTokens) {
        return new LLMRequest(prompt)
            .withParameter("temperature", temperature)
            .withParameter("max_tokens", maxTokens);
    }
    
    public static boolean isValidJson(String jsonString) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static void waitBetweenRequests(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}