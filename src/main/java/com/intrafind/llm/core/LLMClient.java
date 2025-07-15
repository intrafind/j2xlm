package com.intrafind.llm.core;

public interface LLMClient {
    LLMResponse generate(LLMRequest request);
    boolean isHealthy();
    LLMProvider getProvider();
    void close();
}