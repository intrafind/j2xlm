package com.intrafind.llm.config;

import com.intrafind.llm.core.LLMClient;
import com.intrafind.llm.core.LLMProvider;
import com.intrafind.llm.providers.openai.OpenAIClient;
import com.intrafind.llm.providers.anthropic.AnthropicClient;
import com.intrafind.llm.providers.gemini.GeminiClient;
import com.intrafind.llm.providers.mistral.MistralClient;

public class LLMClientFactory {
    
    public static LLMClient create(LLMProvider provider, LLMConfig config) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        
        switch (provider) {
            case OPENAI:
                return new OpenAIClient(config);
            case ANTHROPIC:
                return new AnthropicClient(config);
            case GEMINI:
                return new GeminiClient(config);
            case MISTRAL:
                return new MistralClient(config);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}