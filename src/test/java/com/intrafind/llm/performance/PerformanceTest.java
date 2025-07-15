package com.intrafind.llm.performance;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.opentest4j.TestAbortedException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceTest {
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testResponseTime() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        LLMRequest request = new LLMRequest("What is 2+2?");
        
        Instant start = Instant.now();
        LLMResponse response = client.generate(request);
        Instant end = Instant.now();
        
        Duration duration = Duration.between(start, end);
        
        assertNotNull(response);
        assertNotNull(response.getContent());
        
        // Most API calls should complete within 30 seconds
        assertTrue(duration.toSeconds() < 30, 
                  "Response took too long: " + duration.toSeconds() + " seconds");
        
        System.out.println("Response time: " + duration.toMillis() + "ms");
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMultipleSequentialRequests() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        List<Duration> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            LLMRequest request = new LLMRequest("What is " + i + " + 1?");
            
            Instant start = Instant.now();
            LLMResponse response = client.generate(request);
            Instant end = Instant.now();
            
            Duration duration = Duration.between(start, end);
            responseTimes.add(duration);
            
            assertNotNull(response);
            assertNotNull(response.getContent());
            
            // Add small delay between requests
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Calculate average response time
        double avgMillis = responseTimes.stream()
            .mapToLong(Duration::toMillis)
            .average()
            .orElse(0.0);
        
        System.out.println("Average response time: " + avgMillis + "ms");
        
        // All requests should complete within reasonable time
        assertTrue(responseTimes.stream().allMatch(d -> d.toSeconds() < 30));
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testConcurrentRequests() throws InterruptedException {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        List<CompletableFuture<LLMResponse>> futures = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            final int index = i;
            CompletableFuture<LLMResponse> future = CompletableFuture.supplyAsync(() -> {
                LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
                try {
                    LLMRequest request = new LLMRequest("What is " + index + " * 2?");
                    return client.generate(request);
                } finally {
                    client.close();
                }
            }, executor);
            futures.add(future);
        }
        
        // Wait for all requests to complete
        List<LLMResponse> responses = new ArrayList<>();
        for (CompletableFuture<LLMResponse> future : futures) {
            try {
                LLMResponse response = future.get(60, TimeUnit.SECONDS);
                responses.add(response);
            } catch (Exception e) {
                fail("Concurrent request failed: " + e.getMessage());
            }
        }
        
        assertEquals(3, responses.size());
        
        for (LLMResponse response : responses) {
            assertNotNull(response);
            assertNotNull(response.getContent());
        }
        
        executor.shutdown();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testDifferentPromptSizes() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        // Small prompt
        String smallPrompt = "Hi";
        LLMRequest smallRequest = new LLMRequest(smallPrompt);
        
        Instant start = Instant.now();
        LLMResponse smallResponse = client.generate(smallRequest);
        Duration smallDuration = Duration.between(start, Instant.now());
        
        // Medium prompt
        StringBuilder mediumPrompt = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            mediumPrompt.append("This is a medium-sized prompt. ");
        }
        mediumPrompt.append("Please respond with 'OK'.");
        
        LLMRequest mediumRequest = new LLMRequest(mediumPrompt.toString());
        
        start = Instant.now();
        LLMResponse mediumResponse = client.generate(mediumRequest);
        Duration mediumDuration = Duration.between(start, Instant.now());
        
        assertNotNull(smallResponse);
        assertNotNull(mediumResponse);
        
        System.out.println("Small prompt time: " + smallDuration.toMillis() + "ms");
        System.out.println("Medium prompt time: " + mediumDuration.toMillis() + "ms");
        
        // Both should complete within reasonable time
        assertTrue(smallDuration.toSeconds() < 30);
        assertTrue(mediumDuration.toSeconds() < 30);
        
        client.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testProviderPerformanceComparison() {
        String openaiKey = System.getenv("OPENAI_API_KEY");
        String anthropicKey = System.getenv("ANTHROPIC_API_KEY");
        
        if (openaiKey == null || anthropicKey == null) {
            throw new TestAbortedException("Both API keys needed for comparison");
        }
        
        String testPrompt = "What is the capital of France?";
        
        // Test OpenAI
        LLMConfig openaiConfig = new LLMConfig(openaiKey);
        LLMClient openaiClient = LLMClientFactory.create(LLMProvider.OPENAI, openaiConfig);
        
        Instant start = Instant.now();
        LLMResponse openaiResponse = openaiClient.generate(new LLMRequest(testPrompt));
        Duration openaiDuration = Duration.between(start, Instant.now());
        
        // Test Anthropic
        LLMConfig anthropicConfig = new LLMConfig(anthropicKey);
        LLMClient anthropicClient = LLMClientFactory.create(LLMProvider.ANTHROPIC, anthropicConfig);
        
        start = Instant.now();
        LLMResponse anthropicResponse = anthropicClient.generate(new LLMRequest(testPrompt));
        Duration anthropicDuration = Duration.between(start, Instant.now());
        
        assertNotNull(openaiResponse);
        assertNotNull(anthropicResponse);
        
        System.out.println("OpenAI response time: " + openaiDuration.toMillis() + "ms");
        System.out.println("Anthropic response time: " + anthropicDuration.toMillis() + "ms");
        
        // Both should complete within reasonable time
        assertTrue(openaiDuration.toSeconds() < 30);
        assertTrue(anthropicDuration.toSeconds() < 30);
        
        openaiClient.close();
        anthropicClient.close();
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testMemoryUsage() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        // Make multiple requests
        for (int i = 0; i < 10; i++) {
            LLMRequest request = new LLMRequest("Simple test " + i);
            LLMResponse response = client.generate(request);
            assertNotNull(response);
        }
        
        client.close();
        
        // Force garbage collection again
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        
        long memoryIncrease = memoryAfter - memoryBefore;
        
        System.out.println("Memory increase: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // Memory increase should be reasonable (less than 100MB)
        assertTrue(memoryIncrease < 100 * 1024 * 1024, 
                  "Memory usage increased too much: " + (memoryIncrease / 1024 / 1024) + " MB");
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
    public void testClientReuse() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new TestAbortedException("OPENAI_API_KEY not found");
        }
        
        LLMConfig config = new LLMConfig(apiKey);
        LLMClient client = LLMClientFactory.create(LLMProvider.OPENAI, config);
        
        List<Duration> responseTimes = new ArrayList<>();
        
        // Make multiple requests with the same client
        for (int i = 0; i < 5; i++) {
            LLMRequest request = new LLMRequest("Test request " + i);
            
            Instant start = Instant.now();
            LLMResponse response = client.generate(request);
            Duration duration = Duration.between(start, Instant.now());
            
            responseTimes.add(duration);
            assertNotNull(response);
            
            // Small delay between requests
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // All requests should complete successfully
        assertEquals(5, responseTimes.size());
        
        // Response times should be consistent (no significant degradation)
        double avgTime = responseTimes.stream()
            .mapToLong(Duration::toMillis)
            .average()
            .orElse(0.0);
        
        System.out.println("Average response time with reused client: " + avgTime + "ms");
        
        client.close();
    }
}