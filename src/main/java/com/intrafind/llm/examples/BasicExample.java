package com.intrafind.llm.examples;

import com.intrafind.llm.config.LLMConfig;
import com.intrafind.llm.config.LLMClientFactory;
import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.core.LLMRequest;
import com.intrafind.llm.core.LLMResponse;

public class BasicExample {
    
    public static void main(String[] args) {
        // Example with OpenAI
        LLMConfig openaiConfig = new LLMConfig("your-openai-api-key");
        LLMClient openaiClient = LLMClientFactory.create(LLMProvider.OPENAI, openaiConfig);
        
        LLMRequest request = new LLMRequest("Explain quantum computing in simple terms")
            .withParameter("temperature", 0.7)
            .withParameter("max_tokens", 150);
        
        LLMResponse response = openaiClient.generate(request);
        
        System.out.println("Provider: " + response.getProvider().getDisplayName());
        System.out.println("Model: " + response.getModel());
        System.out.println("Response: " + response.getContent());
        System.out.println("Usage: " + response.getMetadata().get("usage"));
        
        openaiClient.close();
        
        // Example with Anthropic
        LLMConfig anthropicConfig = new LLMConfig("your-anthropic-api-key");
        LLMClient anthropicClient = LLMClientFactory.create(LLMProvider.ANTHROPIC, anthropicConfig);
        
        LLMRequest anthropicRequest = new LLMRequest("What is the capital of France?")
            .withModel("claude-3-sonnet-20240229");
        
        LLMResponse anthropicResponse = anthropicClient.generate(anthropicRequest);
        
        System.out.println("\nProvider: " + anthropicResponse.getProvider().getDisplayName());
        System.out.println("Response: " + anthropicResponse.getContent());
        
        anthropicClient.close();
    }
}